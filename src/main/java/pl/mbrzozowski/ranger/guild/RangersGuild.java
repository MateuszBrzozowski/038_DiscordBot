package pl.mbrzozowski.ranger.guild;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.mbrzozowski.ranger.DiscordBot;

public class RangersGuild {

    private static final String RANGERS_PL_GUILD_ID = "311976984861212672";
    public static final int MAX_CATEGORY_CHANNELS = 50;
    public static final int MAX_GUILD_CHANNELS = 500;

    @Nullable
    public static Guild getGuild() {
        return DiscordBot.getJda().getGuildById(RANGERS_PL_GUILD_ID);
    }

    @NotNull
    public static String getLinkToMessage(final String channelId, final String messageId) {
        return "https://discord.com/channels/" + RANGERS_PL_GUILD_ID + "/" + channelId + "/" + messageId;
    }

    public static boolean compareCategoryId(@NotNull String categoryId, @NotNull CategoryId category) {
        return categoryId.equals(category.getId());
    }

    @Nullable
    public static Category getCategory(CategoryId category) {
        Guild guild = getGuild();
        if (guild == null) {
            return null;
        }
        return guild.getCategoryById(category.getId());
    }

    public static int howManyChannelsInCategory(CategoryId categoryId) {
        Category category = getCategory(categoryId);
        if (category != null) {
            return category.getChannels().size();
        }
        return MAX_CATEGORY_CHANNELS + 1;
    }

    @Nullable
    public static TextChannel getTextChannel(ChannelsId channelsId) {
        Guild guild = getGuild();
        if (guild == null) {
            return null;
        }
        return guild.getTextChannelById(channelsId.getId());
    }

    public static boolean compareChannelId(@NotNull String channelId, @NotNull ChannelsId channel) {
        return channelId.equals(channel.getId());
    }

    public static boolean isSpaceInCategory(CategoryId categoryId) {
        int channelsCount = howManyChannelsInCategory(categoryId);
        return channelsCount < MAX_CATEGORY_CHANNELS;
    }

    public static boolean isNoSpaceOnGuild() {
        Guild guild = getGuild();
        if (guild == null) {
            return true;
        }
        return guild.getTextChannels().size() >= MAX_GUILD_CHANNELS;
    }

    public enum CategoryId {
        RECRUIT("694916869252972570"),
        SERVER("694911873317077073"),
        EVENT("861657261403406376");

        private final String id;

        CategoryId(String id) {
            this.id = id;
        }

        private String getId() {
            return id;
        }
    }

    public enum ChannelsId {
        BOT_LOGGER("860096729457098762"),
        RECRUIT_OPINIONS("973453689316597800"),
        DRILL_INSTRUCTOR_HQ("977884495510388806");

        private final String id;

        ChannelsId(String id) {
            this.id = id;
        }

        private String getId() {
            return id;
        }
    }
}