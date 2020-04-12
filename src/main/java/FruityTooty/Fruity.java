package FruityTooty;

import FruityTooty.command.*;
import FruityTooty.model.FruitStand;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

public class Fruity {
    private static final String DISCORD_TOKEN = System.getenv("DISCORD_TOKEN");
    public static final String COMMAND_PREFIX = "=";

    public static Map<Snowflake, FruitStand> fruitStands;
    public static Map<String, Command> commands;

    public static void main(String[] args) {
        DiscordClientBuilder builder = new DiscordClientBuilder(DISCORD_TOKEN);

        fruitStands = new HashMap<>();
        commands = new HashMap<>();
        populateCommandsMap(commands);

        DiscordClient client = builder.build();

        client.getEventDispatcher().on(ReadyEvent.class)
                .subscribe(readyEvent -> {
                    User self = readyEvent.getSelf();
                    System.out.println(String.format("Logged in as %s#%s", self.getUsername(), self.getDiscriminator()));

                    readyEvent.getClient().updatePresence(Presence.idle());
                });

        initMessageHandler(client);

        client.login().block();
    }

    private static void logMessage(Message message) {
        Flux.concat(
                Mono.just("New message in channel ["),
                message.getChannel().map(Channel::getMention),
                Mono.just("]: "),
                Mono.justOrEmpty(message.getContent())
        ).subscribe(System.out::println);
    }

    private static void initMessageHandler(DiscordClient client) {
        //If it starts with COMMAND_PREFIX, send the message to commandHandler
        client.getEventDispatcher().on(MessageCreateEvent.class)
                .flatMap(event -> Mono.justOrEmpty(event.getMessage().getContent())
                    .flatMap(content -> Flux.fromIterable(commands.entrySet())
                            .filter(entry -> content.startsWith(COMMAND_PREFIX + entry.getKey()))
                            .flatMap(entry -> entry.getValue().execute(event))
                            .next()
                    )
                ).subscribe();
    }

    private static void populateCommandsMap(Map<String, Command> map) {
        map.put("ping", new PingCommand());
        map.put("help", new HelpCommand());
        map.put("newstand", new NewStandCommand());
        map.put("stand", new StandCommand());
        map.put("work", new WorkCommand());
    }

}
