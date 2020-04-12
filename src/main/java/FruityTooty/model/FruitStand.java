package FruityTooty.model;

import FruityTooty.exceptions.NotEnoughFruitException;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

public class FruitStand {
    public static final String DEFAULT_FRUIT = "Fruit";

    Snowflake ownerId;

    String standName;
    long establishedDate; //In milliseconds

    String fruitName;

    int totalFruitSold;
    int fruitSoldPerHour;
    int fruitSoldPerWork;

    int fruitPerDay; //Replenishes fruit supply
    int fruitSupply;

    double dollarPerFruit;

    double money; //In dollars

    public FruitStand(String standName, User founder) {
        ownerId = founder.getId();

        this.standName = standName;
        establishedDate = System.currentTimeMillis();

        this.fruitName = DEFAULT_FRUIT;

        fruitSoldPerHour = 10;
        fruitSoldPerWork = 3;

        //TODO: set up fruit per day timer
        fruitPerDay = 2000;
        fruitSupply = 10;

        dollarPerFruit = 2.0;

        money = 1000.0;

        setSellTimer();
        setResupplyTimer();
    }

    private void setResupplyTimer() {
        Flux.interval(Duration.ofDays(1), Duration.ofDays(1))
                .doOnNext(input -> setFruitSupply(getFruitSupply() + fruitPerDay))
                .subscribe();
    }

    private void setSellTimer() {
        Flux.interval(Duration.ofHours(1), Duration.ofHours(1))
                .flatMap(input -> {
                    try { return sellFruit(fruitSoldPerHour); }
                    catch (NotEnoughFruitException e) {
                        return Flux.error(Exceptions.propagate(e));
                    }
                })
                .onErrorResume(e -> e.getClass().equals(NotEnoughFruitException.class), e -> Mono.empty())
                .subscribe();
    }

    public synchronized Mono<Void> sellFruit(int amount) throws NotEnoughFruitException {
        if (amount > fruitSupply) {
            throw new NotEnoughFruitException("Not enough fruit to sell. Only " + amount + " fruit left.", this);
        }

        fruitSupply -= amount;
        totalFruitSold += amount;

        money += amount * dollarPerFruit;

        return Mono.empty();
    }

    public Mono<Void> doWork() throws NotEnoughFruitException {
        return sellFruit(fruitSoldPerWork);
    }

    public Snowflake getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Snowflake ownerId) {
        this.ownerId = ownerId;
    }

    public String getStandName() {
        return standName;
    }

    public void setStandName(String newName) {
        standName = newName;
    }

    public long getEstablishedDate() {
        return establishedDate;
    }

    public String getFruitName() {
        return fruitName;
    }

    public void setFruitName(String fruitName) {
        this.fruitName = fruitName;
    }

    public double getMoney() {
        return money;
    }

    public int getFruitSupply() {
        return fruitSupply;
    }

    public synchronized void setFruitSupply(int amount) {
        fruitSupply = amount;
    }

    public int getFruitSoldPerHour() {
        return fruitSoldPerHour;
    }

    public int getFruitSoldPerWork() {
        return fruitSoldPerWork;
    }
}

