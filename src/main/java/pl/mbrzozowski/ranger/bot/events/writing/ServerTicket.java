package pl.mbrzozowski.ranger.bot.events.writing;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import pl.mbrzozowski.ranger.server.service.transcription.TranscriptionService;

@RequiredArgsConstructor
public class ServerTicket extends Proccess {

    private final TranscriptionService transcriptionService;

    @Override
    public void proccessMessage(@NotNull MessageReceivedEvent event) {
        if (!event.getAuthor().isBot() && transcriptionService.isTicket(event)) {
            transcriptionService.saveMessage(event);
        }
        getNextProccess().proccessMessage(event);
    }
}
