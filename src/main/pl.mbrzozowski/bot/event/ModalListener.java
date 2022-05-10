package bot.event;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import recrut.RecruitOpinions;

public class ModalListener extends ListenerAdapter {

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (event.getModalId().equalsIgnoreCase("customID")){
            RecruitOpinions recruitOpinions = new RecruitOpinions();
            recruitOpinions.submitForm(event);
        }
    }
}
