package FruityTooty.command;

import FruityTooty.Util;
import FruityTooty.model.FruitStand;
import FruityTooty.Fruity;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Commands relating to management of a user's fruit stand
 */
public class StandCommand implements Command {
    Map<String, Command> subCommands;

    public StandCommand() {
        subCommands = new HashMap<>();

        subCommands.put("show", new StandShowCommand());
        subCommands.put("name", new StandNameCommand());
        subCommands.put("fruitname", new StandFruitNameCommand());
    }



    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        return Mono.justOrEmpty(event.getMessage().getContent())
                .flatMap(content -> Mono.justOrEmpty(event.getMessage().getAuthor()).flatMap(author -> {
                    FruitStand stand = Fruity.fruitStands.get(author.getId());

                    if (stand == null) {
                        return event.getMessage().getChannel().flatMap(channel ->
                                channel.createMessage("You need to set up a fruit stand first!"));
                    }
                    else {
                        List<String> tokens = Util.getMessageTokens(content);

                        //Empty subCommand is a shortcut for show
                        if (tokens.size() < 2) {
                            return subCommands.get("show").execute(event);
                        }

                        return Flux.fromIterable(subCommands.entrySet())
                                .filter(entry -> tokens.get(1).equals(entry.getKey()))
                                .flatMap(entry -> entry.getValue().execute(event))
                                .next();
                    }
                })).then();
    }

    static class StandShowCommand implements Command {

        public String getFormattedDate(FruitStand stand) {
            Date established = new Date(stand.getEstablishedDate());
            DateFormat formatter = new SimpleDateFormat("y-M-d H:m:s");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

            return formatter.format(established);
        }

        @Override
        public Mono<Void> execute(MessageCreateEvent event) {
            //TODO: Create embed template for this
            return event.getMessage().getChannel().flatMap(channel ->
                    Mono.justOrEmpty(event.getMessage().getAuthor()).flatMap(author -> Mono.just(Fruity.fruitStands.get(author.getId()))
                            .flatMap(stand -> channel.createMessage(messageSpec -> messageSpec.setEmbed(embedSpec ->
                            embedSpec.setColor(new Color(153, 0, 255)) //Purple
                                    .setTitle(stand.getStandName())
                                    .setDescription("Owner: " + event.getMessage().getAuthor().get().getUsername())
                                    .addField("Cash", "$" + stand.getMoney(), true)
                                    .addField("Fruit Supply", stand.getFruitSupply() + " " + stand.getFruitName(), true)
                                    .addField("Fruit Sold (per hour)", stand.getFruitSoldPerHour() + " " + stand.getFruitName(), false)
                                    //TODO: Set to Discord guild server timezone!!
                                    .setFooter("Est. " + getFormattedDate(stand) + " (UTC)", null)))

            ))).then();
        }
    }

    static class StandNameCommand implements Command {

        @Override
        public Mono<Void> execute(MessageCreateEvent event) {
            return Mono.justOrEmpty(event.getMessage().getContent())
                    .flatMap(content -> Mono.justOrEmpty(event.getMessage().getAuthor())
                        .flatMap(author -> Mono.justOrEmpty(Fruity.fruitStands.get(author.getId()))
                                .flatMap(stand -> {
                                    List<String> tokens = Util.getMessageTokens(content);

                                    if (tokens.size() < 3) {
                                        return event.getMessage().getChannel()
                                                .flatMap(channel -> channel.createMessage("Invalid name, cannot be blank!"));
                                    }

                                    String newName = content.substring(content.indexOf("stand name") + 11); //Get rid of commands
                                    String oldName = stand.getStandName();
                                    stand.setStandName(newName);
                                    return event.getMessage().getChannel()
                                            .flatMap(channel -> channel.createMessage("Successfully changed your stand name from **" + oldName + "** to **" + stand.getStandName() + "**!"));
                                })
                        )
                    ).then();
        }
    }

    static class StandFruitNameCommand implements Command {

        public Mono<Void> execute(MessageCreateEvent event) {
            return Mono.justOrEmpty(event.getMessage().getContent())
                    .flatMap(content -> Mono.justOrEmpty(event.getMessage().getAuthor())
                            .flatMap(author -> Mono.justOrEmpty(Fruity.fruitStands.get(author.getId()))
                                    .flatMap(stand -> {
                                        List<String> tokens = Util.getMessageTokens(content);

                                        if (tokens.size() < 3) {
                                            return event.getMessage().getChannel()
                                                    .flatMap(channel -> channel.createMessage("Invalid name, cannot be blank!"));
                                        }

                                        String newName = content.substring(content.indexOf("stand fruitname") + 16); //Get rid of commands
                                        String oldName = stand.getFruitName();
                                        stand.setFruitName(newName);
                                        return event.getMessage().getChannel()
                                                .flatMap(channel -> channel.createMessage("Successfully changed your fruit name from **" + oldName + "** to **" + stand.getFruitName() + "**!"));
                                    })
                            )
                    ).then();
        }
    }
}
