package pl.mbrzozowski.ranger.bot.events;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.event.EventService;
import pl.mbrzozowski.ranger.helpers.CategoryAndChannelID;
import pl.mbrzozowski.ranger.model.ImplCleaner;
import pl.mbrzozowski.ranger.recruit.RecruitBlackListService;
import pl.mbrzozowski.ranger.recruit.RecruitsService;
import pl.mbrzozowski.ranger.role.RoleService;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class GuildListener extends ListenerAdapter {

    private final RecruitBlackListService recruitBlackListService;
    private final SlashCommandListener slashCommandListener;
    private final RecruitsService recruitsService;
    private final EventService eventService;
    private final RoleService roleService;
    private final ImplCleaner implCleaner;

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        if (event.getGuild().getId().equals(CategoryAndChannelID.RANGERSPL_GUILD_ID)) {
            ArrayList<CommandData> commandData = new ArrayList<>();
            getCommandList(commandData);
            recruitsService.cleanDB(event);
            implCleaner.autoDeleteChannels();
            implCleaner.autoCloseChannel();
            event.getGuild().updateCommands().addCommands(commandData).queue();
        }
    }

    private void getCommandList(ArrayList<CommandData> commandData) {
        slashCommandListener.getCommandData(commandData);
        roleService.getCommandsToList(commandData);
        recruitBlackListService.getCommandList(commandData);
        recruitsService.getCommandList(commandData);
    }

}
