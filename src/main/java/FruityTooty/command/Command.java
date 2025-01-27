package FruityTooty.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public interface Command {
    Mono<Void> execute(MessageCreateEvent event);
}
