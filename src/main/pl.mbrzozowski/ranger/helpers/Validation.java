package ranger.helpers;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
public class Validation {

    private static final String datePattern = "dd.MM.yyyy";

    public static boolean isDateFormat(String source) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("d.M.yyyy HH:mm");
        try {
            log.info(source);
            LocalDateTime date = LocalDateTime.parse(source + " 23:59", dateTimeFormatter);
            log.info("Przekonwertowano datę");
            return true;
        } catch (DateTimeParseException e) {
            log.info("Nie udało sie przekonwertować daty");
            RangerLogger.info(String.format("Nieprawidłowa data %s. Format daty: \"%s\"", source, datePattern));
            return false;
        }
    }

    /**
     * @param s Czas w formacie HH:mm lub H:mm
     * @return Zwraca true jeżeli czas jes podany w poprawnej formie, w innym przypadku zwraca false.
     */
    public static boolean isTimeFormat(String s) {
//        s = timeCorrect(s);
        if (s.length() == 5) {
            if (s.substring(2, 3).equalsIgnoreCase(":")) {
                if (isDecimal(s.substring(0, 1)) && isDecimal(s.substring(1, 2)) && isDecimal(s.substring(3, 4)) && isDecimal(s.substring(4, 5))) {
                    int hour = Integer.parseInt(s.substring(0, 2));
                    int min = Integer.parseInt(s.substring(3, 5));
                    if (hour >= 0 && hour <= 23 && min >= 0 && min <= 59) {
                        return true;
                    } else log.info("Zly format - godzina lub czas wyszły za zakres");
                } else log.info("Zly format - to nie jest liczba");
            } else log.info("Zly format :");
        }
        return false;
    }

    public static String timeCorrect(String timeOld) {
        if (timeOld.length() == 4) {
            timeOld = "0" + timeOld;
        }
        return timeOld;
    }

    private static boolean isDecimal(String s) {
        for (int i = 0; i < 10; i++) {
            if (s.equals(String.valueOf(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sprawdza czy data i czas jest później niż obecna data i czas.
     *
     * @param dateTime Data i czas w formacie dd.MM.yyyy HH:mm
     * @return Zwraca true jeżeli podany czas w parametrze jest później niż obecny. W innym przypadku zwraca false.
     */
    public static boolean eventDateTimeAfterNow(String dateTime) {
        LocalDateTime dateTimeNow = LocalDateTime.now(ZoneId.of("Europe/Paris"));
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("d.M.yyyy HH:mm");
        LocalDateTime eventDateTime = null;
        try {
            eventDateTime = LocalDateTime.parse(dateTime, dateTimeFormatter);
        } catch (DateTimeParseException e) {
            RangerLogger.info("Nieprawidłowa data i czas [" + dateTime + "]");
            return false;
        }
        return eventDateTime.isAfter(dateTimeNow);
    }

    public static boolean eventDateAfterNow(LocalDateTime dateTime) {
        LocalDateTime dateTimeNow = LocalDateTime.now(ZoneId.of("Europe/Paris"));
        return dateTime.isAfter(dateTimeNow);
    }
}
