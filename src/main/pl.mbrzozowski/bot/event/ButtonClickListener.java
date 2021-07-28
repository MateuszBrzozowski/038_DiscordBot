package bot.event;

import event.ButtonClick;
import ranger.Repository;
import recrut.Recruits;
import event.Event;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranger.RangerBot;

public class ButtonClickListener extends ListenerAdapter {

    protected static final Logger logger = LoggerFactory.getLogger(RangerBot.class.getName());

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        Recruits recrut = Repository.getRecruits();
        if (event.getComponentId().equalsIgnoreCase("newRecrutTEST")) {
            recrut.newPodanie(event);
        } else if (event.getComponentId().equalsIgnoreCase("recrutY")) {
            event.deferEdit().queue();
            recrut.confirm(event.getUser().getId(), event.getChannel(), event.getMessage().getId());
        } else if (event.getComponentId().equalsIgnoreCase("recrutN")) {
            event.deferEdit().queue();
            recrut.cancel(event.getUser().getId(), event.getChannel(), event.getMessage().getId());
        }

        Event events = Repository.getEvent();
        int indexOfMatch = events.getIndexActiveEvent(event.getMessage().getId());
        if (indexOfMatch >= 0) {
            event.deferEdit().queue();
            if (event.getComponentId().equalsIgnoreCase("in_" + event.getMessage().getId())) {
                events.buttonClick(event,indexOfMatch, ButtonClick.SIGN_IN);
            } else if (event.getComponentId().equalsIgnoreCase("reserve_" + event.getMessage().getId())) {
                events.buttonClick(event,indexOfMatch, ButtonClick.SIGN_IN_RESERVE);
            } else if (event.getComponentId().equalsIgnoreCase("out_" + event.getMessage().getId())) {
                events.buttonClick(event,indexOfMatch, ButtonClick.SIGN_OUT);
            }
            events.updateEmbed(event, indexOfMatch);
        }
    }


}
