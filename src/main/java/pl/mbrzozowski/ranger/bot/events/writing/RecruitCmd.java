package pl.mbrzozowski.ranger.bot.events.writing;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import pl.mbrzozowski.ranger.helpers.Commands;
import pl.mbrzozowski.ranger.recruit.RecruitsService;
import pl.mbrzozowski.ranger.response.EmbedInfo;

public class RecruitCmd extends Proccess {

    private final RecruitsService recruitsService;

    public RecruitCmd(MessageReceivedEvent messageReceived, RecruitsService recruitsService) {
        super(messageReceived);
        this.recruitsService = recruitsService;
    }

    @Override
    public void proccessMessage(Message message) {
        String cmd = message.getWords()[0];
        if (message.isTextChannel()) {
            if (cmd.equalsIgnoreCase(Commands.START_REKRUT)) {
                messageReceived.getMessage().delete().submit();
                EmbedInfo.recruiter(messageReceived);
            } else if (cmd.equalsIgnoreCase(Commands.NEGATIVE)) {
                messageReceived.getMessage().delete().submit();
                recruitsService.negativeResult(message.getUserID(), messageReceived.getChannel().asTextChannel());
            } else if (cmd.equalsIgnoreCase(Commands.POSITIVE)) {
                messageReceived.getMessage().delete().submit();
                recruitsService.positiveResult(message.getUserID(), messageReceived.getChannel().asTextChannel());
            } else {
                getNextProccess().proccessMessage(message);
            }
        } else {
            getNextProccess().proccessMessage(message);
        }
    }
}
