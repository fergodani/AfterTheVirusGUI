package com.atv.gui;

import com.atv.models.cards.Card;
import com.atv.models.cards.ZombieCard;
import com.atv.models.views.PlayerInteraction;

import java.util.ArrayList;
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
    private final Runnable selectionStateChanged;
    private Card selectedCard;
    private String lastMessage;
    private List<Card> cardsAvailableForSelection = Collections.emptyList();
    private final List<Card> selectedCards = new ArrayList<>();
    private int requiredSelectionCount;
    private Consumer<List<Card>> selectionConsumer;
    private Runnable armsDamageAction;
    private Runnable legsDamageAction;

    public GuiPlayerInteraction(Consumer<String> messageSink) {
        this(messageSink, () -> { });
    }

    public GuiPlayerInteraction(Consumer<String> messageSink, Runnable selectionStateChanged) {
        this.messageSink = messageSink;
        this.selectionStateChanged = selectionStateChanged;
    }

    public void select(Card card) {
        selectedCard = card;
    }

    public void deselect() {
        selectedCard = null;
    }

    public Card getSelectedCard() {
        return selectedCard;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public boolean isSelectingCards() {
        return selectionConsumer != null;
    }

    public boolean isSelectingDamage() {
        return armsDamageAction != null;
    }

    public void selectArmsDamage() {
        selectDamageAction(armsDamageAction);
    }

    public void selectLegsDamage() {
        selectDamageAction(legsDamageAction);
    }

    public boolean isCardSelected(Card card) {
        return selectedCards.contains(card);
    }

    public int getRequiredSelectionCount() {
        return requiredSelectionCount;
    }

    public int getSelectedCardCount() {
        return selectedCards.size();
    }

    public void toggleCardSelection(Card card) {
        if (!isSelectingCards() || !cardsAvailableForSelection.contains(card)) {
            return;
        }
        if (selectedCards.remove(card)) {
            selectionStateChanged.run();
            return;
        }
        if (selectedCards.size() == requiredSelectionCount) {
            showMessage("Debes seleccionar exactamente " + requiredSelectionCount + " cartas.");
            return;
        }
        selectedCards.add(card);
        selectionStateChanged.run();
    }

    public void confirmCardSelection() {
        if (!isSelectingCards() || selectedCards.size() != requiredSelectionCount) {
            showMessage("Selecciona exactamente " + requiredSelectionCount + " cartas.");
            return;
        }
        Consumer<List<Card>> consumer = selectionConsumer;
        List<Card> selection = new ArrayList<>(selectedCards);
        clearCardSelection();
        consumer.accept(selection);
        selectionStateChanged.run();
    }

    public void cancelCardSelection() {
        if (!isSelectingCards()) {
            return;
        }
        clearCardSelection();
        selectionStateChanged.run();
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
    public void selectCards(List<Card> cardsAvailable, int quantity, Consumer<List<Card>> selectionConsumer) {
        if (cardsAvailable == null || cardsAvailable.size() < quantity) {
            showMessage("No tienes suficientes cartas.");
            return;
        }
        cardsAvailableForSelection = new ArrayList<>(cardsAvailable);
        selectedCards.clear();
        requiredSelectionCount = quantity;
        this.selectionConsumer = selectionConsumer;
        showMessage("Selecciona " + quantity + " carta" + (quantity == 1 ? "" : "s") + " para descartar.");
        selectionStateChanged.run();
    }

    @Override
    public void selectDamage(Runnable armsAction, Runnable legsAction) {
        armsDamageAction = armsAction;
        legsDamageAction = legsAction;
        showMessage("¿Dónde quieres recibir el daño?");
        selectionStateChanged.run();
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

    private void clearCardSelection() {
        cardsAvailableForSelection = Collections.emptyList();
        selectedCards.clear();
        requiredSelectionCount = 0;
        selectionConsumer = null;
    }

    private void selectDamageAction(Runnable damageAction) {
        if (damageAction == null) {
            return;
        }
        armsDamageAction = null;
        legsDamageAction = null;
        damageAction.run();
        selectionStateChanged.run();
    }
}
