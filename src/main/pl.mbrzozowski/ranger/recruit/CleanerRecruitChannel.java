//package ranger.recruit;
//
//import net.dv8tion.jda.api.JDA;
//import net.dv8tion.jda.api.entities.Message;
//import net.dv8tion.jda.api.entities.MessageEmbed;
//import ranger.Repository;
//import ranger.embed.EmbedSettings;
//import ranger.model.CleanerChannel;
//import ranger.model.MemberWithPrivateChannel;
//
//import java.time.OffsetDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//public class CleanerRecruitChannel implements CleanerChannel {
//
//    private final RecruitsService recruitsService;
//    private final List<MemberWithPrivateChannel> activeRecruits;
//    private final List<MemberWithPrivateChannel> recruitsToDelete = new ArrayList<>();
//    private final int DELAY_IN_DAYS = 5;
//
//    public CleanerRecruitChannel(RecruitsService recruitsService, List<MemberWithPrivateChannel> activeRecruits) {
//        this.recruitsService = recruitsService;
//        this.activeRecruits = activeRecruits;
//    }
//
//    public void clean() {
//        if (!activeRecruits.isEmpty()) {
//            JDA jda = Repository.getJda();
//            for (int i = 0; i < activeRecruits.size(); i++) {
//                String channelID = activeRecruits.get(i).getChannelID();
//                List<Message> complete = jda.getTextChannelById(channelID).getHistory().retrievePast(100).complete();
//                if (isTimeToRemove(complete)) {
//                    recruitsToDelete.add(activeRecruits.get(i));
//                }
//            }
//        }
//        Repository.getRecruits().deleteChannels(recruitsToDelete);
//    }
//
//    private boolean isTimeToRemove(List<Message> messages) {
//        int indexMessage = searchResultMessage(messages);
//        if (indexMessage != -1) {
//            OffsetDateTime timeCreated = messages.get(indexMessage).getTimeCreated();
//            OffsetDateTime timeNow = OffsetDateTime.now();
//            timeCreated = timeCreated.plusDays(DELAY_IN_DAYS);
//            if (timeCreated.isBefore(timeNow)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private int searchResultMessage(List<Message> messages) {
//        for (int i = 0; i < messages.size(); i++) {
//            List<MessageEmbed> embeds = messages.get(i).getEmbeds();
//            if (checkEmbeds(embeds)) {
//                return i;
//            }
//        }
//        return -1;
//    }
//
//    /**
//     * @param embeds -sprawdzane embeds
//     * @return true jeśli wiadomść zawiera wynik rekrutacji, w innym przyadku false
//     */
//    public static boolean checkEmbeds(List<MessageEmbed> embeds) {
//        if (!embeds.isEmpty()) {
//            return isEmbedTitle(embeds.get(0));
//        }
//        return false;
//    }
//
//    /**
//     * @param embed sprawdzany embed
//     * @return true jeśli Embed to wynik rekrutacji, w innym przypadku false
//     */
//    private static boolean isEmbedTitle(MessageEmbed embed) {
//        String title = embed.getTitle();
//        if (title != null && title.length() >= EmbedSettings.RESULT.length()) {
//            title = title.substring(0, EmbedSettings.RESULT.length());
//            if (title.equalsIgnoreCase(EmbedSettings.RESULT)) {
//                return true;
//            }
//        }
//        return false;
//    }
//}
