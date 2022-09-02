package ranger.event;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.Repository;
import ranger.embed.EmbedInfo;
import ranger.embed.EmbedSettings;
import ranger.helpers.CategoryAndChannelID;
import ranger.helpers.RangerLogger;
import ranger.model.CleanerChannel;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CleanerEventChannel implements CleanerChannel {

    protected static final Logger logger = LoggerFactory.getLogger(EmbedInfo.class.getName());
    private final EventService eventService;

    public CleanerEventChannel(EventService eventService) {
        this.eventService = eventService;
    }

    public void clean() {
        JDA jda = Repository.getJda();
        Category category = jda.getCategoryById(CategoryAndChannelID.CATEGORY_EVENT_ID);
        if (category != null) {
            List<TextChannel> textChannels = category.getTextChannels();
            if (!textChannels.isEmpty()) {
                for (TextChannel textChannel : textChannels) {
                    Event event = eventService.isActiveMatchChannelID(textChannel.getId());
                    if (event != null) {
                        textChannel.retrievePinnedMessages().queue(messages -> {
                            if (isTimeToRemove(messages)) {
                                String channelID = messages.get(0).getChannel().getId();
                                deleteChannel(channelID);
                            }
                        });
                    }
                }
            }

        }
    }

    private boolean isTimeToRemove(List<Message> messages) {
        int index = searchEventMessage(messages);
        if (index != -1) {
            String dateTimeString = getDateTimeFromEmbed(messages.get(index));
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("d.MM.yyyy HH:mm");
            LocalDateTime dateNow = LocalDateTime.now(ZoneId.of("Europe/Paris"));
            LocalDateTime dateEvent = LocalDateTime.parse(dateTimeString, dateFormat);
            int DELAY_IN_DAYS = 30;
            dateEvent = dateEvent.plusDays(DELAY_IN_DAYS);
            return dateEvent.isBefore(dateNow);
        }
        return false;
    }

    private String getDateTimeFromEmbed(Message message) {
        List<MessageEmbed.Field> fields = message.getEmbeds().get(0).getFields();
        String date = fields.get(0).getValue();
        String time = fields.get(2).getValue();
        return date + " " + time;
    }

    private int searchEventMessage(List<Message> messages) {
        for (int i = 0; i < messages.size(); i++) {
            List<MessageEmbed> embeds = messages.get(i).getEmbeds();
            if (checkEmbeds(embeds)) {
                return i;
            }
        }
        return -1;
    }

    private boolean checkEmbeds(List<MessageEmbed> embeds) {
        if (embeds.isEmpty()) {
            return false;
        }
        List<MessageEmbed.Field> fields = embeds.get(0).getFields();
        if (!fields.isEmpty()) {
            if (!fields.get(0).getName().equalsIgnoreCase(EmbedSettings.WHEN_DATE)) {
                return false;
            }
            if (!fields.get(2).getName().equalsIgnoreCase(EmbedSettings.WHEN_TIME)) {
                return false;
            }
            if (fields.get(4).getName().length() >= EmbedSettings.NAME_LIST.length()) {
                String list = fields.get(4).getName().substring(0, EmbedSettings.NAME_LIST.length());
                if (!list.equalsIgnoreCase(EmbedSettings.NAME_LIST)) {
                    return false;
                }
            }
            if (fields.get(6).getName().length() >= EmbedSettings.NAME_LIST_RESERVE.length()) {
                String reserve = fields.get(6).getName().substring(0, EmbedSettings.NAME_LIST_RESERVE.length());
                if (!reserve.equalsIgnoreCase(EmbedSettings.NAME_LIST_RESERVE)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private void deleteChannel(String channelID) {
        JDA jda = Repository.getJda();
        RangerLogger.info("Upłynął czas utrzymywania kanału - Usunięto pomyślnie - [" + jda.getTextChannelById(channelID).getName() + "]");
        jda.getTextChannelById(channelID).delete().reason("Upłynął czas utrzymywania kanału").queue();
    }
}