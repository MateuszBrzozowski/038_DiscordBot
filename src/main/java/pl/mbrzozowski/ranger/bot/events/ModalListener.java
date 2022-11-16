package pl.mbrzozowski.ranger.bot.events;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.helpers.ComponentId;
import pl.mbrzozowski.ranger.recruit.RecruitOpinions;

@Slf4j
public class ModalListener extends ListenerAdapter {

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        log.info("[EVENT] - Modal interaction event");
        if (event.getModalId().equalsIgnoreCase(ComponentId.RECRUIT_OPINION_MODAL)) {
            RecruitOpinions recruitOpinions = new RecruitOpinions();
            recruitOpinions.submitForm(event);
        }
    }
}
