package pl.mbrzozowski.ranger.bot.events.writing;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.helpers.CategoryAndChannelID;
import pl.mbrzozowski.ranger.helpers.Commands;
import pl.mbrzozowski.ranger.helpers.Users;
import pl.mbrzozowski.ranger.response.EmbedInfo;
import pl.mbrzozowski.ranger.server.service.ServerService;

@Slf4j
public class ServerServiceCmd extends Proccess {

    private final ServerService serverService;

    public ServerServiceCmd(ServerService serverService) {
        this.serverService = serverService;
    }

    @Override
    public void proccessMessage(@NotNull MessageReceivedEvent event) {
        String channelID = event.getChannel().getId();
        boolean isAdmin = Users.isAdmin(event.getAuthor().getId());
        if (event.getMessage().getContentRaw().equalsIgnoreCase(Commands.EMBED_SERVER_SERVICE) && isAdmin) {
            log.info(event.getAuthor() + " - msg({}) - creates embed for server service", event.getMessage().getContentRaw());
            event.getMessage().delete().submit();
            EmbedInfo.serverService(event.getChannel());
        } else if (event.getMessage().getContentRaw().equalsIgnoreCase(Commands.CLOSE) && isServerCategory(channelID)) {
            log.info("{} - msg({}) - closes channel in server category", event.getAuthor(), event.getMessage().getContentRaw());
            serverService.closeChannel(event);
        } else if (event.getMessage().getContentRaw().equalsIgnoreCase(Commands.UPDATE_WL) && isAdmin && event.isFromType(ChannelType.PRIVATE)) {
            event.getMessage().reply("Usługa wyłączona").queue();
//            new Thread(() -> {
//                Whitelist whitelist = new Whitelist();
//                whitelist.whitelistUpdate();
//            }).start();
        } else {
            getNextProccess().proccessMessage(event);
        }
    }

    private boolean isServerCategory(String channelID) {
        TextChannel textChannel = DiscordBot.getJda().getTextChannelById(channelID);
        if (textChannel != null) {
            Category parentCategory = textChannel.getParentCategory();
            if (parentCategory != null) {
                return parentCategory.getId().equalsIgnoreCase(CategoryAndChannelID.CATEGORY_SERVER);
            }
        }
        return false;
    }
}
