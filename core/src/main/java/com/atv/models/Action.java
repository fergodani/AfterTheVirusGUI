package com.atv.models;

import com.atv.models.cards.Card;

@FunctionalInterface
public interface Action {
    void execute(Game game, Card card);
}
