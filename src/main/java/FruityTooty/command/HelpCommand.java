package FruityTooty.command;

import FruityTooty.Fruity;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class HelpCommand implements Command {
    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        return event.getMessage().getChannel().flatMap(channel ->
                channel.createMessage("Type `" + Fruity.COMMAND_PREFIX + "newstand` to set up a new fruit stand!")
        ).then();
    }
}
