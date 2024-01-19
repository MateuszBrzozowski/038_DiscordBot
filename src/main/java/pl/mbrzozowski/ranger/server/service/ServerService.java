package pl.mbrzozowski.ranger.server.service;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pl.mbrzozowski.ranger.DiscordBot;
import pl.mbrzozowski.ranger.event.ButtonClickType;
import pl.mbrzozowski.ranger.helpers.CategoryAndChannelID;
import pl.mbrzozowski.ranger.helpers.RoleID;
import pl.mbrzozowski.ranger.helpers.Users;
import pl.mbrzozowski.ranger.repository.main.ClientRepository;
import pl.mbrzozowski.ranger.response.EmbedInfo;
import pl.mbrzozowski.ranger.response.EmbedSettings;
import pl.mbrzozowski.ranger.response.ResponseMessage;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ServerService {

    private final ClientRepository clientRepository;
    private final Collection<Permission> permissions = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND);

    public ServerService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public void buttonClick(@NotNull ButtonInteractionEvent event, @NotNull ButtonClickType buttonType) {
        log.info(event.getUser() + " - button type: " + buttonType);
        if (!userHasActiveReport(event.getUser().getId())) {
            createChannel(event, buttonType);
            event.deferEdit().queue();
        } else {
            ResponseMessage.cantCreateServerServiceChannel(event);
        }
    }

    public void closeChannel(@NotNull MessageReceivedEvent event) {
        Optional<Client> clientOptional = findByChannelId(event.getChannel().getId());
        if (clientOptional.isPresent()) {
            event.getMessage().delete().submit();
            closeChannel(clientOptional.get(), Users.getUserNicknameFromID(event.getAuthor().getId()));
        }
    }

    public void closeChannel(@NotNull ButtonInteractionEvent event) {
        Optional<Client> clientOptional = clientRepository.findByChannelId(event.getChannel().getId());
        clientOptional.ifPresent(client -> closeChannel(client, Users.getUserNicknameFromID(event.getUser().getId())));
    }

    public void closeChannel(Client client, String whoClose) {
        JDA jda = DiscordBot.getJda();
        Guild guild = jda.getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (guild != null) {
            TextChannel textChannel = guild.getTextChannelById(client.getChannelId());
            Member member = guild.getMemberById(client.getUserId());
            if (textChannel != null) {
                if (member != null) {
                    textChannel
                            .getManager()
                            .putPermissionOverride(member, null, permissions)
                            .queue(unused -> log.info("{} - Close channel - permission override for member:{}", textChannel, member));
                }
                EmbedInfo.closeServerServiceChannel(whoClose, textChannel);
                clientCloseChannelSave(client);
            }
        }
    }

    private void clientCloseChannelSave(@NotNull Client client) {
        client.setIsClose(true);
        client.setCloseTimestamp(LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")).toLocalDateTime());
        clientRepository.save(client);
        log.info("Client=({}) channel closed", client);
    }

    public void removeChannel(@NotNull ButtonInteractionEvent event) {
        deleteByChannelId(event.getChannel().getId());
    }

    private void createChannel(@NotNull ButtonInteractionEvent event, ButtonClickType buttonType) {
        String userID = event.getUser().getId();
        String userName = event.getUser().getName();
        Guild guild = DiscordBot.getJda().getGuildById(CategoryAndChannelID.RANGERSPL_GUILD_ID);
        if (guild != null) {
            Category category = guild.getCategoryById(CategoryAndChannelID.CATEGORY_SERVER);
            String channelName = channelNamePrefix(buttonType) + userName;
            if (category != null) {
                guild.createTextChannel(channelName, category)
                        .addPermissionOverride(guild.getPublicRole(), null, permissions)
                        .addMemberPermissionOverride(Long.parseLong(userID), permissions, null)
                        .addRolePermissionOverride(Long.parseLong(RoleID.MODERATOR), permissions, null)
                        .addRolePermissionOverride(Long.parseLong(RoleID.RADA_KLANU), permissions, null)
                        .addRolePermissionOverride(Long.parseLong(RoleID.SERVER_ADMIN), permissions, null)
                        .queue(channel -> {
                            sendEmbedStartChannel(userID, channel, buttonType);
                            addUser(userID, userName, channel.getId());
                        });
            }
        }
    }

    private void sendEmbedStartChannel(String userID, TextChannel channel, @NotNull ButtonClickType buttonType) {
        switch (buttonType) {
            case REPORT -> EmbedInfo.sendEmbedReport(userID, channel);
            case UNBAN -> EmbedInfo.sendEmbedUnban(userID, channel);
            case CONTACT -> EmbedInfo.sendEmbedContact(userID, channel);
        }
    }

    private String channelNamePrefix(ButtonClickType buttonType) {
        return switch (buttonType) {
            case REPORT -> EmbedSettings.BOOK_RED + "┋report-";
            case UNBAN -> EmbedSettings.BOOK_BLUE + "┋unban-";
            case CONTACT -> EmbedSettings.BOOK_GREEN + "┋contact-";
            default -> "";
        };
    }

    private void addUser(String userId, String userName, String channelId) {
        Client client = Client.builder()
                .userId(userId)
                .channelId(channelId)
                .userName(userName)
                .isClose(false)
                .build();
        clientRepository.save(client);
    }

    protected boolean userHasActiveReport(String userID) {
        List<Client> clients = clientRepository.findByUserId(userID);
        clients = clients.stream()
                .filter(client -> !client.getIsClose())
                .collect(Collectors.toList());
        return clients.size() != 0;
    }

    public void deleteChannelById(String channelId) {
        TextChannel textChannel = DiscordBot.getJda().getTextChannelById(channelId);
        if (textChannel != null) {
            textChannel.delete().reason("Uplynął termin utrzymywania kanalu").queue();
            log.info("Deleted server service channel by id " + channelId);
        }
        deleteByChannelId(channelId);
    }

    public void deleteByChannelId(String channelID) {
        clientRepository.deleteByChannelId(channelID);
        log.info("Check and deleted channel if exist from DB for Server Service (channelId={})", channelID);
    }

    public Optional<Client> findByChannelId(String channelID) {
        return clientRepository.findByChannelId(channelID);
    }

    public List<Client> findAll() {
        return clientRepository.findAll();
    }
}
