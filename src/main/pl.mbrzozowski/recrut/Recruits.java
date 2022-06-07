package recrut;

import embed.EmbedInfo;
import embed.EmbedSettings;
import helpers.*;
import model.MemberOfServer;
import model.MemberWithPrivateChannel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.Repository;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public class Recruits {

    private final List<MemberWithPrivateChannel> activeRecruits = new ArrayList<>();
    private final List<MemberOfServer> thinkingRecruits = new ArrayList<>();
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final Collection<Permission> permissions = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND);
    private final Collection<Permission> permViewChannel = EnumSet.of(Permission.VIEW_CHANNEL);

    /**
     * @param userName Nazwa użytkownika
     * @param userID   ID użytkownika
     */
    public void createChannelForNewRecrut(String userName, String userID) {
        JDA jda = Repository.getJda();
        Guild guild = jda.getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        List<Category> categories = guild.getCategories();
        for (Category cat : categories) {
            if (cat.getId().equals(CategoryAndChannelID.CATEGORY_RECRUT_ID)) {
                guild.createTextChannel("rekrut-" + userName, cat)
                        .addPermissionOverride(guild.getPublicRole(), null, permissions)
                        .addMemberPermissionOverride(Long.parseLong(userID), permissions, null)
                        .addRolePermissionOverride(Long.parseLong(RoleID.CLAN_MEMBER_ID), permViewChannel, null)
                        .queue(textChannel -> {
                            EmbedBuilder builder = new EmbedBuilder();
                            builder.setColor(Color.GREEN);
                            builder.setThumbnail("https://rangerspolska.pl/styles/Hexagon/theme/images/logo.png");
                            builder.setDescription("Obowiązkowo uzupełnij formularz oraz przeczytaj manual - pomoże Ci w ogarnięciu gry");
                            builder.addField("Formularz rekrutacyjny:", "https://forms.gle/fbTQSdxBVq3zU7FW9", false);
                            builder.addField("Manual:", "https://drive.google.com/file/d/1qTHVBEkpMUBUpTaIUR3TNGk9WAuZv8s8/view", false);
                            builder.addField("TeamSpeak3:", "daniolab.pl:6969", false);
                            textChannel.sendMessage("Cześć <@" + userID + ">!\n" +
                                    "Cieszymy się, że złożyłeś podanie do klanu. Od tego momentu rozpoczyna się Twój okres rekrutacyjny pod okiem <@&" + RoleID.DRILL_INSTRUCTOR_ID + "> oraz innych członków klanu.\n" +
                                    "<@&" + RoleID.RADA_KLANU + "> ")
                                    .setEmbeds(builder.build())
                                    .queue();
                            textChannel.sendMessage("Wkrótce skontaktuje się z Tobą Drill. Oczekuj na wiadomość.")
                                    .setActionRow(
                                            Button.primary(ComponentId.RECRUIT_IN, " "),
                                            Button.secondary(ComponentId.RECRUIT_CLOSE_CHANNEL, " "),
                                            Button.success(ComponentId.RECRUIT_POSITIVE, " "),
                                            Button.danger(ComponentId.RECRUIT_NEGATIVE, " "))
                                    .queue();
                            addUserToList(userID, userName, textChannel.getId());
                        });
            }
        }
        logger.info("Nowe podanie złożone.");
    }

    public void initialize() {
        System.out.println(isMaxRecruits());

//        startUpList();
//        CleanerRecruitChannel cleaner = new CleanerRecruitChannel(activeRecruits);
//        cleaner.clean();
    }

    public void newPodanie(@NotNull ButtonInteractionEvent event) {
        String userName = event.getUser().getName();
        String userID = event.getUser().getId();
        if (!checkUser(userID)) {
            if (!checkThinkingUser(userID)) {
                if (!isMaxRecruits()) {
                    if (!Users.hasUserRoleAnotherClan(event.getUser().getId())) {
                        if (!Users.hasUserRole(event.getUser().getId(), RoleID.CLAN_MEMBER_ID)) {
                            confirmMessage(userID, userName);
                        } else EmbedInfo.userIsInClanMember(userID);
                    } else EmbedInfo.userIsInClan(userID);
                } else EmbedInfo.maxRecrutis(userID);
            }
        } else EmbedInfo.userHaveRecrutChannel(userID);
    }

    private boolean isMaxRecruits() {
        int MAX_CHANNELS = 50;
        int howManyChannelsNow = howManyChannelsInCategory(CategoryAndChannelID.CATEGORY_RECRUT_ID);
        return howManyChannelsNow >= MAX_CHANNELS;
    }

    private int howManyChannelsInCategory(String categoryID) {
        Guild guildRangersPL = Repository.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        return guildRangersPL.getCategoryById(categoryID).getChannels().size();
    }


    private void confirmMessage(String userID, String userName) {
        RangerLogger.info("Użytkownik [" + userName + "] chce złożyć podanie.");
        thinkingRecruits.add(new MemberOfServer(userID, userName));
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.YELLOW);
            builder.setTitle("Potwierdź czy chcesz złożyć podanie?");
            builder.setDescription("Po potwierdzeniu rozpocznie się Twój okres rekrutacyjny w naszym klanie. Skontaktuję się z Tobą jeden z naszych Drillów aby wprowadzić Cię" +
                    " w nasze szeregi. Poprosimy również o wypełnienie krótkiego formularza.");
            builder.setThumbnail(EmbedSettings.THUMBNAIL);
            privateChannel.sendMessageEmbeds(builder.build()).setActionRow(Button.success(ComponentId.NEW_RECRUT_CONFIRM, "Potwierdzam"), Button.danger(ComponentId.NEW_RECRUT_DISCARD, "Rezygnuję")).queue(message -> {
                Thread timer = new Thread(() -> {
                    try {
                        Thread.sleep(1000 * 60 * 2); //2 minuty oczekiwania na odpowiedź
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (userIsThinking(userID) >= 0) {
                        sendCancelInfo(privateChannel);
                        cancel(userID, privateChannel, message.getId());
                        disableButtons(privateChannel, message.getId());
                    }
                });
                timer.start();
            });
        });
    }

    private void sendCancelInfo(PrivateChannel privateChannel) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.RED);
        builder.setThumbnail(EmbedSettings.THUMBNAIL_WARNING);
        builder.setTitle("Uwaga");
        builder.setDescription("Brak odpowiedzi. Anuluje podanie.");
        privateChannel.sendMessageEmbeds(builder.build()).queue();
    }

    public void confirm(String userID, MessageChannel privateChannel, String messageID) {
        int index = userIsThinking(userID);
        if (index >= 0) {
            createChannelForNewRecrut(thinkingRecruits.get(index).getUserName(), userID);
            thinkingRecruits.remove(index);
        } else {
            sendMessageBotReload(userID);
        }
        disableButtons(privateChannel, messageID);
    }

    public void cancel(String userID, MessageChannel privateChannel, String messageID) {
        int index = userIsThinking(userID);
        if (index >= 0) {
            RangerLogger.info("Użytkownik [" + thinkingRecruits.get(index).getUserName() + "] zrezygnował ze złożenia podania.");
            thinkingRecruits.remove(index);
        } else {
            sendMessageBotReload(userID);
        }
        disableButtons(privateChannel, messageID);
    }

    private void sendMessageBotReload(String userID) {
        JDA jda = Repository.getJda();
        jda.getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessage("UPS! Coś poszło nie tak. Jeżeli chcesz złóż ponownie podanie.").queue();
        });
    }

    /**
     * @param userID ID użytkowanika który składa podanie
     * @return Zwraca index na liście thinkingRecruits, jeżeli użytkownik dostał wiadomość z prośbą o potwierdzenie
     * złożenie podania i dalej ma możliwość akceptacji; W innym przypadku zwraca -1
     */
    private int userIsThinking(String userID) {
        for (int i = 0; i < thinkingRecruits.size(); i++) {
            if (userID.equalsIgnoreCase(thinkingRecruits.get(i).getUserID())) {
                return i;
            }
        }
        return -1;
    }

    public void disableButtons(MessageChannel channel, String messageID) {
        channel.retrieveMessageById(messageID).queue(message -> {
            List<MessageEmbed> embeds = message.getEmbeds();
            MessageEmbed messageEmbed = embeds.get(0);
            message.editMessageEmbeds(messageEmbed).setActionRow(Button.success(ComponentId.NEW_RECRUT_CONFIRM, "Potwierdzam").asDisabled(), Button.danger(ComponentId.NEW_RECRUT_DISCARD, "Rezygnuję").asDisabled()).queue();
        });
    }

    private void addUserToList(String userID, String userName, String channelID) {
        MemberWithPrivateChannel member = new MemberWithPrivateChannel(userID, userName, channelID);
        activeRecruits.add(member);
        addUserToDataBase(userID, userName, channelID);
    }

    private void addUserToDataBase(String userID, String userName, String channelID) {
        RecruitDatabase rdb = new RecruitDatabase();
        rdb.addUser(userID, userName, channelID);
    }

    private void startUpList() {
        RecruitDatabase rdb = new RecruitDatabase();
        ResultSet resultSet = rdb.getAllRecrut();
        List<MemberWithPrivateChannel> recruitsToDeleteDataBase = new ArrayList<>();
        this.activeRecruits.clear();
        List<TextChannel> allTextChannels = Repository.getJda().getTextChannels();

        if (resultSet != null) {
            while (true) {
                try {
                    if (!resultSet.next()) break;
                    else {
                        String userID = resultSet.getString("userID");
                        String userName = resultSet.getString("userName");
                        String channelID = resultSet.getString("channelID");
                        MemberWithPrivateChannel recrut = new MemberWithPrivateChannel(userID, userName, channelID);
                        boolean isActive = false;
                        for (TextChannel tc : allTextChannels) {
                            if (tc.getId().equalsIgnoreCase(channelID)) {
                                isActive = true;
                                break;
                            }
                        }
                        if (isActive) {
                            activeRecruits.add(recrut);
                        } else {
                            recruitsToDeleteDataBase.add(recrut);
                        }
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        for (MemberWithPrivateChannel rc : recruitsToDeleteDataBase) {
            RemoveRecrutFromDataBase(rc.getChannelID());
        }
    }

    /**
     * @param userID ID użytkownika którego sprawdzamy
     * @return Zwraca true jeśli użytkownik ma otwarty kanał rekrutacji. W innym przypadku zwraca false.
     */
    private boolean checkUser(String userID) {
        for (MemberWithPrivateChannel member : activeRecruits) {
            if (member.getUserID().equalsIgnoreCase(userID)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param userID ID użytkownika którego sprawdzamy
     * @return Zwraca true jeśli użytkownik kliknął Złóż podanie i program cozekuje na odpowiedź. W innym przypoadku
     * zwraca false.
     */
    private boolean checkThinkingUser(String userID) {
        for (MemberOfServer member : thinkingRecruits) {
            if (member.getUserID().equalsIgnoreCase(userID)) {
                return true;
            }
        }
        return false;
    }

    public void deleteChannelByID(String channelID) {
        for (int i = 0; i < activeRecruits.size(); i++) {
            if (channelID.equalsIgnoreCase(activeRecruits.get(i).getChannelID())) {
                removeRoleFromUserID(activeRecruits.get(i).getUserID());
                activeRecruits.remove(i);
                RemoveRecrutFromDataBase(channelID);
                logger.info("Pozostało aktywnych rekrutacji: {}", activeRecruits.size());
            }
        }
    }

    public void removeRoleFromUserID(String userID) {
        JDA jda = Repository.getJda();
        jda.getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID).retrieveMemberById(userID).queue(member -> {
            List<Role> roles = member.getRoles();
            for (Role r : roles) {
                if (r.getId().equalsIgnoreCase(RoleID.RECRUT_ID)) {
                    member.getGuild().removeRoleFromMember(member, r).queue();
                    break;
                }
            }
        });
    }

    private void RemoveRecrutFromDataBase(String channelID) {
        RecruitDatabase rdb = new RecruitDatabase();
        rdb.removeUser(channelID);
    }

    public void closeChannel(MessageReceivedEvent event) {
        TextChannel textChannel = event.getTextChannel();
        String userID = event.getAuthor().getId();
        closeChannel(textChannel, userID);
    }

    private int getIndexOfRecrut(String channelID) {
        for (int i = 0; i < activeRecruits.size(); i++) {
            if (activeRecruits.get(i).getChannelID().equalsIgnoreCase(channelID)) {
                return i;
            }
        }
        return -1;
    }

    public void reOpenChannel(MessageReceivedEvent event) {
        MessageChannel messageChannel = event.getChannel();
        boolean isRecruitChannel = isRecruitChannel(messageChannel.getId());
        if (isRecruitChannel) {
            int indexOfRecrut = getIndexOfRecrut(messageChannel.getId());
            Member member = event.getGuild().getMemberById(activeRecruits.get(indexOfRecrut).getUserID());
            TextChannelManager manager = event.getTextChannel().getManager();
            manager.putPermissionOverride(event.getGuild().getRoleById(RoleID.CLAN_MEMBER_ID), permViewChannel, null);
            if (member != null)
                manager.putPermissionOverride(member, permissions, null);
            manager.queue();
            EmbedInfo.openChannel(event.getAuthor().getId(), event.getTextChannel());
        }
    }

    public boolean isRecruitChannel(String channelID) {
        for (MemberWithPrivateChannel ar : activeRecruits) {
            if (ar.getChannelID().equalsIgnoreCase(channelID)) {
                return true;
            }
        }
        return false;
    }

    public String getRecruitIDFromChannelID(String channelID) {
        for (MemberWithPrivateChannel ar : activeRecruits) {
            if (ar.getChannelID().equalsIgnoreCase(channelID)) {
                return ar.getUserID();
            }
        }
        return "-1";
    }

    public void deleteChannels(List<MemberWithPrivateChannel> listToDelete) {
        JDA jda = Repository.getJda();
        RecruitDatabase rdb = new RecruitDatabase();
        for (int i = 0; i < listToDelete.size(); i++) {
            int indexOfRecrut = getIndexOfRecrut(listToDelete.get(i).getChannelID());
            String userName = listToDelete.get(i).getUserName();
            activeRecruits.remove(indexOfRecrut);
            rdb.removeUser(listToDelete.get(i).getChannelID());
            logger.info("Pozostało aktywnych rekrutacji: {}", activeRecruits.size());
            jda.getTextChannelById(listToDelete.get(i).getChannelID()).delete().reason("Rekrutacja zakończona, upłynął czas wyświetlania informacji").queue();
            RangerLogger.info("Upłynął czas utrzymywania kanału - Usunięto pomyślnie kanał rekruta - [" + userName + "]");
        }
    }

    public void sendInfo(PrivateChannel privateChannel) {
        EmbedBuilder activeRecruitsBuilder = new EmbedBuilder();
        activeRecruitsBuilder.setColor(Color.RED);
        activeRecruitsBuilder.setTitle("Rekruci");
        activeRecruitsBuilder.addField("Aktywnych rekrutacji", String.valueOf(activeRecruits.size()), false);
        privateChannel.sendMessageEmbeds(activeRecruitsBuilder.build()).queue();
        for (MemberWithPrivateChannel r : activeRecruits) {
            JDA jda = Repository.getJda();
            String channelName = jda.getTextChannelById(r.getChannelID()).getName();
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.WHITE);
            builder.addField("ID użytkownika", r.getUserID(), false);
            builder.addField("Nazwa użytkownika", r.getUserName(), true);
            builder.addField("ID kanału", r.getChannelID(), false);
            builder.addField("Nazwa kanału", channelName, true);
            privateChannel.sendMessageEmbeds(builder.build()).queue();
        }
    }

    public void positiveResult(TextChannel channel) {
        if (isRecruitChannel(channel.getId())) {
            JDA jda = Repository.getJda();
            Guild guild = jda.getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
            removeSmallRInTag(channel.getId(), guild);
            Role roleClanMember = jda.getRoleById(RoleID.CLAN_MEMBER_ID);
            Role roleRecruit = jda.getRoleById(RoleID.RECRUT_ID);
            String recruitID = getRecruitIDFromChannelID(channel.getId());
            boolean hasRoleClanMember = Users.hasUserRole(recruitID, RoleID.CLAN_MEMBER_ID);
            boolean hasRoleRecrut = Users.hasUserRole(recruitID, RoleID.RECRUT_ID);
            if (!hasRoleClanMember) {
                guild.addRoleToMember(UserSnowflake.fromId(recruitID), roleClanMember).submit();
            }
            if (hasRoleRecrut) {
                guild.removeRoleFromMember(UserSnowflake.fromId(recruitID), roleRecruit).submit();
            }
        }
    }

    private void removeSmallRInTag(String channelId, Guild guild) {
        String userID = getRecruitIDFromChannelID(channelId);
        String userNickname = Users.getUserNicknameFromID(userID);
        if (userNickname.contains("<rRangersPL>")) {
            userNickname = userNickname.replace("<rRangersPL>", "<RangersPL>");
            guild.getMemberById(userID).modifyNickname(userNickname).submit();
        }
    }

    public void negativeResult(TextChannel channel) {
        if (isRecruitChannel(channel.getId())) {
            JDA jda = Repository.getJda();
            Guild guild = jda.getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
            removeTagFromNick(channel.getId(), guild);
            Role roleRecrut = jda.getRoleById(RoleID.RECRUT_ID);
            String recruitID = getRecruitIDFromChannelID(channel.getId());
            boolean hasRoleRecrut = Users.hasUserRole(recruitID, RoleID.RECRUT_ID);
            if (hasRoleRecrut) {
                guild.removeRoleFromMember(UserSnowflake.fromId(recruitID), roleRecrut).submit();
            }
        }
    }

    private void removeTagFromNick(String channelID, Guild guild) {
        String userID = getRecruitIDFromChannelID(channelID);
        String userNickname = Users.getUserNicknameFromID(userID);
        if (userNickname.contains("<rRangersPL>")) {
            userNickname = userNickname.replace("<rRangersPL>", "");
            guild.getMemberById(userID).modifyNickname(userNickname).submit();
        }
    }

    private void addRoleRecruit(String channelID) {
        JDA jda = Repository.getJda();
        Guild guild = jda.getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        Role roleRecruit = jda.getRoleById(RoleID.RECRUT_ID);
        String userID = getRecruitIDFromChannelID(channelID);
        boolean hasRoleRocruit = Users.hasUserRole(userID, roleRecruit.getId());
        if (!hasRoleRocruit) {
            logger.info("daje role rekrut");
            Member member = guild.getMemberById(userID);
            guild.addRoleToMember(member, roleRecruit).complete();
        }
    }

    private void changeRecruitNickname(Guild guild, String channelID) {
        String userID = getRecruitIDFromChannelID(channelID);
        String nicknameOld = Users.getUserNicknameFromID(userID);
        if (!isNicknameRNGSuffix(nicknameOld)) {
            logger.info("Zmieniam nick");
            guild.getMemberById(userID).modifyNickname(nicknameOld + "<rRangersPL>").complete();
        }
    }

    protected boolean isNicknameRNGSuffix(String nickname) {
        nickname = nickname.replace(" ", "");
        if (nickname.endsWith("<rRangersPL>")) return true;
        else return nickname.endsWith("<RangersPL>");
    }

    public boolean isResult(TextChannel textChannel) {
        List<Message> messages = textChannel.getHistory().retrievePast(100).complete();
        for (int i = 0; i < messages.size(); i++) {
            List<MessageEmbed> embeds = messages.get(i).getEmbeds();
            if (CleanerRecruitChannel.checkEmbeds(embeds)) {
                return true;
            }
        }
        return false;
    }

    public void accepted(ButtonInteractionEvent event) {
        if (!isAccepted(event.getTextChannel())) {
            if (isRecruitChannel(event.getChannel().getId())) {
                EmbedInfo.recruitAccepted(Users.getUserNicknameFromID(event.getUser().getId()), event.getTextChannel());
                addRoleRecruit(event.getTextChannel().getId());
                changeRecruitNickname(event.getGuild(), event.getTextChannel().getId());
            }
        }
    }

    private boolean isAccepted(TextChannel textChannel) {
        List<Message> messages = textChannel.getHistory().retrievePast(100).complete();
        for (int i = 0; i < messages.size(); i++) {
            List<MessageEmbed> embeds = messages.get(i).getEmbeds();
            if (checkEmbedsIsAccepted(embeds)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkEmbedsIsAccepted(List<MessageEmbed> embeds) {
        if (!embeds.isEmpty()) {
            return isEmbedIsAccepted(embeds.get(0));
        }
        return false;
    }

    private boolean isEmbedIsAccepted(MessageEmbed embed) {
        String title = embed.getTitle();
        String description = embed.getDescription();
        String pattern = "Przyjęty na rekrutację przez: ";
        if (title != null && title.equalsIgnoreCase("Przyjęty")) {
            if (description != null && description.length() >= pattern.length()) {
                description = description.substring(0, pattern.length());
                if (description.equalsIgnoreCase(pattern)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void closeChannel(ButtonInteractionEvent event) {
        TextChannel textChannel = event.getTextChannel();
        String userID = event.getUser().getId();
        closeChannel(textChannel, userID);
    }

    private void closeChannel(TextChannel textChannel, String userID) {
        JDA jda = Repository.getJda();
        Guild guild = jda.getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        boolean isRecruitChannel = isRecruitChannel(textChannel.getId());
        if (isRecruitChannel) {
            int indexOfRecrut = getIndexOfRecrut(textChannel.getId());
            Member member = guild.getMemberById(activeRecruits.get(indexOfRecrut).getUserID());
            TextChannelManager manager = textChannel.getManager();
            manager.putPermissionOverride(guild.getRoleById(RoleID.CLAN_MEMBER_ID), null, permViewChannel);
            if (member != null)
                manager.putPermissionOverride(member, null, permissions);
            manager.queue();
            EmbedInfo.closeChannel(userID, textChannel);
        }
    }
}
