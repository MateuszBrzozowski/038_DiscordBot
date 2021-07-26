package ranger;

import event.Event;
import event.EventsGeneratorModel;
import model.DiceGames;
import net.dv8tion.jda.api.JDA;
import recrut.Recruits;

public class Repository {

    private static Event event = new Event();
    private static Recruits recruits = new Recruits();
    private static DiceGames diceGames = new DiceGames();
    private static EventsGeneratorModel eventsGeneratorModel = new EventsGeneratorModel();
    private static JDA jda;

    public static Event getEvent() {
        return event;
    }

    public static Recruits getRecruits() {
        return recruits;
    }

    public static DiceGames getDiceGames() {
        return diceGames;
    }

    public static EventsGeneratorModel getEventsGeneratorModel() {
        return eventsGeneratorModel;
    }

    public static JDA getJda() {
        return jda;
    }

    static void setJDA(JDA j) {
        jda = j;
    }
}