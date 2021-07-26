package bot.event;

import event.Event;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;
import ranger.Repository;

public class MessageUpdate extends ListenerAdapter {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        Event e = Repository.getEvent();
        if (e.getIndexActiveEvent(event.getMessageId()) != -1) {
            e.removeEvent(event.getMessageId());
        }

    }
}