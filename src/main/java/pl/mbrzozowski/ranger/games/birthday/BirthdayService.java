package pl.mbrzozowski.ranger.games.birthday;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.games.SlashCommandGame;
import pl.mbrzozowski.ranger.guild.SlashCommands;
import pl.mbrzozowski.ranger.repository.main.BirthdayRepository;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BirthdayService implements SlashCommandGame {

    private final BirthdayRepository birthdayRepository;

    private void save(Birthday birthday) {
        birthdayRepository.save(birthday);
    }

    @NotNull
    private List<Birthday> findAll() {
        return birthdayRepository.findAll();
    }

    private Optional<Birthday> findByUserId(String userId) {
        return birthdayRepository.findByUserId(userId);
    }

    public void setDate(@NotNull SlashCommandInteractionEvent event) {
        int day = Objects.requireNonNull(event.getOption(SlashCommands.BIRTHDAY_DAY.getName())).getAsInt();
        int month = Objects.requireNonNull(event.getOption(SlashCommands.BIRTHDAY_MONTH.getName())).getAsInt();
        int year = Objects.requireNonNull(event.getOption(SlashCommands.BIRTHDAY_YEAR.getName())).getAsInt();
        if (year <= 1900 || LocalDateTime.now().getYear() - year <= 10) {
            event.reply("Sprawdź dane i spróbuj ponownie!").setEphemeral(true).queue();
            log.info("Year less than 1900 or years under 10");
            return;
        }
        LocalDate date;
        try {
            date = LocalDate.of(year, month, day);
        } catch (Exception e) {
            event.reply("Nieprawidłowa data! Sprawdź dane i spróbuj ponownie").setEphemeral(true).queue();
            log.info("Cannot convert to date - {}.{}.{}", day, month, year);
            return;
        }
        Optional<Birthday> optional = findByUserId(event.getUser().getId());
        Birthday birthday;
        if (optional.isPresent()) {
            birthday = optional.get();
            birthday.setDate(date);
            birthday.setUserName(event.getUser().getName());
            log.info("User is exists. Set new date");
        } else {
            birthday = Birthday.builder().date(date).userName(event.getUser().getName()).userId(event.getUser().getId()).build();
            log.info("New user. Set date of birthday");
        }
        save(birthday);
        event.reply("Data " + date.getDayOfMonth() + "." + String.format("%02d", date.getMonthValue()) + "." +
                date.getYear() + " została zapisana").setEphemeral(true).queue();
        log.info("Date of birthday saved {}", birthday);
    }

    @Override
    public void start(@NotNull SlashCommandInteractionEvent event) {
        List<Birthday> all = findAll();
        List<Birthday> today = BirthdayProvider.getListWithToday(all);
        List<Birthday> next = BirthdayProvider.getListWithSortedSinceNow(all);
        log.info("all.size={}, today.size={}, next.size={}", all.size(), today.size(), next.size());
        if (today.isEmpty() && next.isEmpty()) {
            event.reply("Brak zapisanych użytkowników!").queue();
            log.info("Empty DB with birthdays all.size={}", all.size());
            return;
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(new Color(151, 1, 95));
        builder.setDescription("# Urodzinki");
        if (today.size() >= 1) {
            builder.addField("Dzisiaj urodziny obchodzi(ą):", BirthdayProvider.getStringTodayBirthday(today), false);
        }
        if (next.size() >= 1) {
            builder.addField("Następne urodziny:", BirthdayProvider.getStringNextBirthday(next), false);
        }
        event.replyEmbeds(builder.build()).queue();
        log.info("Embed with birthdays info sent");
    }

    @Override
    public void getSlashCommandsList(@NotNull ArrayList<CommandData> commandData) {
        commandData.add(Commands.slash(SlashCommands.BIRTHDAY.getName(), SlashCommands.BIRTHDAY.getDescription()));
        commandData.add(Commands.slash(SlashCommands.BIRTHDAY_SET.getName(), SlashCommands.BIRTHDAY_SET.getDescription())
                .addOption(OptionType.INTEGER, SlashCommands.BIRTHDAY_DAY.getName(), SlashCommands.BIRTHDAY_DAY.getName(), true)
                .addOption(OptionType.INTEGER, SlashCommands.BIRTHDAY_MONTH.getName(), SlashCommands.BIRTHDAY_MONTH.getName(), true)
                .addOption(OptionType.INTEGER, SlashCommands.BIRTHDAY_YEAR.getName(), SlashCommands.BIRTHDAY_YEAR.getName(), true));
    }
}