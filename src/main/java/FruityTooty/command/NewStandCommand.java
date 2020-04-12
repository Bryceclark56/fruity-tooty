package FruityTooty.command;

import FruityTooty.model.FruitStand;
import FruityTooty.Fruity;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;

/**
 * Creates a new stand for a Discord user.
 */
public class NewStandCommand implements Command {

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        return event.getMessage().getChannel()
                .flatMap(channel -> Mono.justOrEmpty(event.getMessage().getAuthor())
                        .map(author -> {
                            if (Fruity.fruitStands.get(author.getId()) == null) {
                                Fruity.fruitStands.put(author.getId(), new FruitStand("Fruit Stand", author));
                                return "A new stand has been created!";
                            }
                            else {
                                //TODO: Mention user
                                return "You already have a stand, " + author.getUsername() + "!";
                            }
                        }).flatMap(channel::createMessage)).then();
    }
}
