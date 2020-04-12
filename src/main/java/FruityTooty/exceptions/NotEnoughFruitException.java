package FruityTooty.exceptions;

import FruityTooty.model.FruitStand;

public class NotEnoughFruitException extends Exception {
    FruitStand stand;

    public NotEnoughFruitException(String message, FruitStand stand) {
        super(message);
        this.stand = stand;
    }
    public NotEnoughFruitException(FruitStand stand) {
        this("Not enough fruit", stand);
    }

    public FruitStand getStand() {
        return stand;
    }
}
