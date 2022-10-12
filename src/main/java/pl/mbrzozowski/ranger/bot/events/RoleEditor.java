package pl.mbrzozowski.ranger.bot.events;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.helpers.CategoryAndChannelID;
import pl.mbrzozowski.ranger.helpers.RangerLogger;
import pl.mbrzozowski.ranger.helpers.Users;

public class RoleEditor {

//    JDA jda = DiscordBot.getJda();

    public void addRemoveRole(String userID, String roleString) {
        Role role = DiscordBot.getJda().getRoleById(roleString);
        Guild guild = DiscordBot.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (role != null) {
            boolean hasRole = Users.hasUserRole(userID, role.getId());
            if (!hasRole) {
                guild.addRoleToMember(UserSnowflake.fromId(userID), role).queue();
                sendConfirmation(role, userID, true);
                RangerLogger.info(Users.getUserNicknameFromID(userID) + " - Przypisał rolę **" + role.getName() + "**");
            } else {
                guild.removeRoleFromMember(UserSnowflake.fromId(userID), role).queue();
                sendConfirmation(role, userID, false);
                RangerLogger.info(Users.getUserNicknameFromID(userID) + " - Usunął rolę **" + role.getName() + "**");
            }
        }
    }

    /**
     * @param role    Rola z serwera
     * @param addRole true - jesli rola jest dodawana, false - jeśli rola jest usuwana
     */
    private void sendConfirmation(Role role, String userID, boolean addRole) {
        DiscordBot.getJda().getUserById(userID).openPrivateChannel().queue(privateChannel -> {
            String msg = "**Rangers Polska**: " + role.getName() + ": ";
            if (addRole) {
                msg += "Gave you the role!";
            } else {
                msg += "Took away the role!";
            }
            privateChannel.sendMessage(msg).queue();
        });
    }
}
