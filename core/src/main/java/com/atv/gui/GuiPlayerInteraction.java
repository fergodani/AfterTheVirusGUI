package com.atv.gui;

import com.atv.models.cards.Card;
import com.atv.models.cards.ZombieCard;
import com.atv.models.views.PlayerInteraction;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Bridges the synchronous rules engine with the card currently selected in the
 * Scene2D board. When a chained rule needs another choice, the first legal
 * card is used so the engine remains playable until that rule is modelled as
 * an asynchronous interaction.
 */
public class GuiPlayerInteraction implements PlayerInteraction {

    private final Consumer<String> messageSink;
    private Card selectedCard;
    private String lastMessage;

    public GuiPlayerInteraction(Consumer<String> messageSink) {
        this.messageSink = messageSink;
    }

    public void select(Card card) {
        selectedCard = card;
    }

    public Card getSelectedCard() {
        return selectedCard;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    @Override
    public Card selectCard(List<Card> cardsAvailable) {
        if (cardsAvailable == null || cardsAvailable.isEmpty()) {
            showMessage("No hay cartas disponibles.");
            return null;
        }
        if (selectedCard != null && cardsAvailable.contains(selectedCard)) {
            Card result = selectedCard;
            selectedCard = null;
            return result;
        }
        Card fallback = cardsAvailable.get(0);
        showMessage("Se ha seleccionado automáticamente: " + fallback.getName());
        return fallback;
    }

    @Override
    public List<Card> selectCards(List<Card> cardsAvailable) {
        Card selected = selectCard(cardsAvailable);
        return selected == null ? Collections.<Card>emptyList() : Collections.singletonList(selected);
    }

    @Override
    public int selectOption(String message, int numberOfOptions) {
        showMessage(message + "\nSe aplica la última opción disponible.");
        return numberOfOptions;
    }

    @Override
    public void killZombies(List<Card> zombies, int quantity) {
        changeZombies(zombies, quantity, false);
    }

    @Override
    public void discardZombies(List<Card> zombies, int quantity) {
        changeZombies(zombies, quantity, true);
    }

    @Override
    public void showMessage(String message) {
        lastMessage = message;
        messageSink.accept(message);
    }

    private void changeZombies(List<Card> cards, int quantity, boolean discard) {
        for (int i = 0; i < quantity; i++) {
            ZombieCard zombie = selectZombie(cards);
            if (zombie == null) {
                return;
            }
            zombie.setZombiesLeft(zombie.getZombiesLeft() - 1);
            if (discard) {
                zombie.setZombiesDiscarded(zombie.getZombiesDiscarded() + 1);
            }
        }
    }

    private ZombieCard selectZombie(List<Card> cards) {
        Card selected = selectCard(cards);
        return selected instanceof ZombieCard ? (ZombieCard) selected : null;
    }
}
