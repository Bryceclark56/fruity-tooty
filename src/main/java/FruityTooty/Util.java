package FruityTooty;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Util {

    //TODO: More intelligent method of pluralising words
    public static String pluralise(String singular) {
        int singularEnd = singular.length() - 1;
        int lastChar = singular.charAt(singularEnd);

        StringBuilder plural = new StringBuilder(singular);

        plural.append('s');

        return plural.toString();
    }

    public static List<String> getMessageTokens(String content) {
        return Arrays.asList(content.substring(1).split(" "));
    }
}
