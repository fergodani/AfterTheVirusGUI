package com.atv.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameTest {

    @Test
    void winsAfterRescuingEightSurvivors() {
        Game game = new Game();

        game.incrementSurvivorsRescued(Game.VICTORY_SURVIVOR_COUNT - 1);
        assertFalse(game.isGameWon());

        game.incrementSurvivorsRescued(1);
        assertTrue(game.isGameWon());
    }
}
