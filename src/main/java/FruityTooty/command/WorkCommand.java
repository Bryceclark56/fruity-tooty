package FruityTooty.command;

import FruityTooty.model.FruitStand;
import FruityTooty.Fruity;
import FruityTooty.exceptions.NotEnoughFruitException;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class WorkCommand implements Command {

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        return event.getMessage().getChannel()
                .flatMap(channel -> Mono.justOrEmpty(event.getMessage().getAuthor())
                        .map(author -> Mono.justOrEmpty(Fruity.fruitStands.get(author.getId()))
                                .flatMap(stand -> {
                                    try {
                                        return stand.doWork().thenReturn(stand);
                                    } catch (NotEnoughFruitException e) {
                                        return Mono.error(e);
                                    }
                                })
                                .map(FruitStand::getFruitSoldPerWork)
                                .map(fruitSold -> "You did some additional work at your stand and sold " + fruitSold + " fruit!")
                                .switchIfEmpty(Mono.just("You need to set up a fruit stand first!"))
                                .onErrorResume(error -> {
                                    if (error instanceof NotEnoughFruitException) {
                                        return Mono.just("Unable to work stand, not enough fruit in your supply! " +
                                                "You only have **" + ((NotEnoughFruitException) error).getStand().getFruitSupply() + "** fruit left!");
                                    }
                                    else return Mono.error(error);
                                })

                        ).flatMap(monoMsg -> monoMsg.flatMap(channel::createMessage))
                ).then();
    }
}