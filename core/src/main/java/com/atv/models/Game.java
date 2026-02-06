package com.atv.models;

import java.util.*;
import java.util.stream.Collectors;
import com.atv.models.cards.*;
import com.atv.utils.CardFactory;
import com.atv.models.views.*;

public class Game {

    private Stack<Card> playerDeck = new Stack<>();
    private Stack<Card> zombieDeck = new Stack<>();
    private Stack<Card> explorationDeck = new Stack<>();
    private List<Card> playerArea = new ArrayList<>();
    private List<Card> zombieArea = new ArrayList<>();
    private List<Card> explorationArea = new ArrayList<>();
    private List<Card> playerDiscard = new ArrayList<>();
    private List<Card> playerHand = new ArrayList<>();
    private List<Card> cardsDestroyed = new ArrayList<>();
    private PlayerInteraction playerInteraction;
    private int defends = 0;

    private int wave = 1;
    private int survivorsRescued;
    private boolean canRetrieveMedicalEquipment = false;
    private boolean isRifleInPlay = false;
    private boolean armsDamaged = false;
    private boolean legsDamaged = false;
    private boolean isGameOver = false;

    public void init(Character character) {
        CardFactory cardFactory = CardFactory.getInstance();
        cardFactory.createExplorationDeck();
        cardFactory.createZombieDeck();
        this.playerDeck = cardFactory.createPlayerDeck(character);
        this.zombieDeck = cardFactory.getZombieDeck();
        this.explorationDeck = cardFactory.getExplorationDeck();

        for (Card card : this.playerDeck) {
            if (card.getName().equals(character.getCardInPlay())) {
                this.playerArea.add(card);
                if (((PermanentCard) card).getPrepareCost() == 0) {
                    ((PermanentCard) card).setPrepared(true);
                }
            }
        }
        this.playerDeck.removeAll(this.playerArea);

        Collections.shuffle(this.playerDeck);
        Collections.shuffle(this.explorationDeck);
        for (int i = 0; i < 5; i++) {
            Card card = this.playerDeck.pop();
            if (card instanceof ZombieCard) {
                this.zombieArea.add(card);
            } else {
                this.playerHand.add(card);
            }
        }

        // Remove
        for (Card card : this.explorationDeck) {
            if (card.getName().equals("Rifle")) {
                this.playerHand.add(card);
            }
        }
    }

    public void playCard() {
        if (this.playerHand.isEmpty()) {
            this.playerInteraction.showMessage("No hay cartas en la mano.");
            return;
        }
        Card card = this.playerInteraction.selectCard(this.playerHand);
        if (card instanceof EventCard) {
            if (isLegsDamaged() && card.getName().equals("Run")) {
                this.playerInteraction.showMessage("No puedes jugar la carta Run.");
                return;
            }
            this.playerHand.remove(card);
            card.play(this);
        } else {
            this.playerHand.remove(card);
            this.playerArea.add(card);
            if (((PermanentCard)card).getEnterAction() != null) {
                ((PermanentCard)card).getEnterAction().execute(this, card);
            }
            if (((PermanentCard) card).getPrepareCost() == 0 && !(card instanceof ShootingCard)) {
                ((PermanentCard) card).setPrepared(true);
            }
        }

    }

    public void explore() {
        if (this.explorationDeck.isEmpty()) {
            this.playerInteraction.showMessage("No hay cartas en el mazo de exploración.");
            return;
        }
        discard(1);
        Card card = this.explorationDeck.pop();
        this.explorationArea.add(card);
    }

    public void retrieve() {
        if (this.explorationArea.isEmpty()) {
            this.playerInteraction.showMessage("No hay cartas en la zona de exploración.");
            return;
        }
        Card card = this.playerInteraction.selectCard(this.explorationArea);
        if (discard(card.getExplorationCost())) {
            this.explorationArea.remove(card);
            this.playerDiscard.add(card);

        }
    }

    public void prepareCard() {
        Card card = this.playerInteraction.selectCard(this.playerArea);
        if (((PermanentCard) card).isPrepared()) {
            this.playerInteraction.showMessage("La carta ya está preparada.");
            return;
        }

        ((PermanentCard) card).prepare(this);

    }

    public void useCard() {
        Card card = this.playerInteraction.selectCard(this.playerArea);
        if (card != null) {
            if (!((PermanentCard) card).isPrepared()) {
                this.playerInteraction.showMessage("The card is not prepared.");
            } else {
                card.play(this);
            }
        }
    }

    public void pass() {
        int totalZombies = this.zombieArea.stream().reduce(0, (acc, card) -> acc + ((ZombieCard) card).getZombiesLeft(), Integer::sum);
        if (totalZombies == 0) {
            nextRound();
            return;
        }
        if (totalZombies > 2
            || isLegsDamaged() && isArmsDamaged()
            || totalZombies == 2 && (isLegsDamaged() || isArmsDamaged())
        ) {
            this.setGameOver(true);
            return;
        } else if (totalZombies == 2) {
            this.setLegsDamaged(true);
            this.setArmsDamaged(true);
        } else if (totalZombies == 1) {
            if (isLegsDamaged() && !isArmsDamaged()) {
                this.setArmsDamaged(true);
            } else if (isArmsDamaged() && !isLegsDamaged()) {
                this.setLegsDamaged(true);
            } else {
                int option = this.playerInteraction.selectOption("¿Dónde quieres recibir el daño?\n1. Brazos\n2. Piernas", 2);
                switch (option) {
                    case 1:
                        this.setArmsDamaged(true);
                        break;
                    case 2:
                        this.setLegsDamaged(true);
                        break;
                    default:
                        this.playerInteraction.showMessage("Opción no válida.");
                }
            }
        }
        nextRound();
    }

    private void nextRound() {
        for (int i = 0; i < 5; i++) {
            if (this.playerDeck.isEmpty()) {
                this.shuffleDeck();
            }
            Card card = this.playerDeck.pop();
            if (card instanceof ZombieCard) {
                this.zombieArea.add(card);
            } else {
                this.playerHand.add(card);
            }
        }
    }

    private void shuffleDeck() {
        this.wave++;
        for (int i = 0; i < wave; i++) {
            if (this.zombieDeck.isEmpty()) {
                this.playerInteraction.showMessage("No hay cartas en el mazo de zombis.");
                return;
            }
            this.playerDiscard.add(this.zombieDeck.pop());
        }
        this.playerDeck.addAll(this.playerDiscard);
        this.playerDiscard.clear();
        Collections.shuffle(this.playerDeck);
    }

    public boolean discard(int quantity) {
        if (this.playerHand.isEmpty()) {
            this.playerInteraction.showMessage("No hay cartas en la mano.");
            return false;
        }
        if (quantity > this.playerHand.size()) {
            this.playerInteraction.showMessage("No tienes suficientes cartas.");
            return false;
        }
        for (int i = 0; i < quantity; i++) {
            Card card = this.playerInteraction.selectCard(this.playerHand);
            if (card != null) {
                this.playerHand.remove(card);
                this.playerDiscard.add(card);
            }
        }
        return true;
    }

    public boolean discard(String cardName) {
        Card card = this.playerHand.stream()
            .filter(c -> c.getName().equals(cardName))
            .findFirst()
            .orElse(null);
        if (card != null) {
            this.playerHand.remove(card);
            this.playerDiscard.add(card);
            return true;
        }
        this.playerInteraction.showMessage("No tienes la carta " + cardName + " en la mano.");
        return false;
    }

    public void kill(ZombieCard card) {
        this.zombieArea.remove(card);
        this.zombieDeck.push(card);
    }

    public void kill(int quantity) {
        this.playerInteraction.killZombies(zombieArea, quantity);

        List<Card> zombiesToRemove = new ArrayList<>();
        for (Card zombie : this.zombieArea) {
            if (((ZombieCard) zombie).getZombiesLeft() == 0 && ((ZombieCard) zombie).getZombiesDiscarded() == 0) {
                zombiesToRemove.add(zombie);
                this.zombieDeck.push(zombie);
            } else if (((ZombieCard) zombie).getZombiesLeft() == 0 && ((ZombieCard) zombie).getZombiesDiscarded() > 0) {
                zombiesToRemove.add(zombie);
                this.playerDiscard.add(zombie);
            }
        }
        this.zombieArea.removeAll(zombiesToRemove);
    }

    public void discardZombie(int quantity) {
        this.playerInteraction.discardZombies(zombieArea, quantity);
    }

    public void destroy(Card card) {
        this.playerArea.remove(card);
        this.cardsDestroyed.add(card);
    }

    public List<Card> getZombiesInDiscard() {
        return this.playerDiscard.stream()
            .filter((card) -> card instanceof ZombieCard)
            .collect(Collectors.toList());
    }

    public void reloadRifle() {
        List<Card> ammo = this.playerInteraction.selectCards(this.playerHand);

        Optional<Card> rifle = this.playerArea.stream()
            .filter((card) -> card.getName().equals("Rifle"))
            .findFirst();
        if (rifle.isPresent()) {
            ((ShootingCard)rifle.get()).addAmmo(ammo);
            this.playerHand.removeAll(ammo);
        }
    }

    public void incrementSurvivorsRescued(int value) {
        this.survivorsRescued += value;
    }

    public Stack<Card> getPlayerDeck() {
        return playerDeck;
    }

    public void setPlayerDeck(Stack<Card> playerDeck) {
        this.playerDeck = playerDeck;
    }

    public Stack<Card> getZombieDeck() {
        return zombieDeck;
    }

    public void setZombieDeck(Stack<Card> zombieDeck) {
        this.zombieDeck = zombieDeck;
    }

    public Stack<Card> getExplorationDeck() {
        return explorationDeck;
    }

    public void setExplorationDeck(Stack<Card> explorationDeck) {
        this.explorationDeck = explorationDeck;
    }

    public List<Card> getPlayerArea() {
        return playerArea;
    }

    public void setPlayerArea(List<Card> playerArea) {
        this.playerArea = playerArea;
    }

    public List<Card> getZombieArea() {
        return zombieArea;
    }

    public void setZombieArea(List<Card> zombieArea) {
        this.zombieArea = zombieArea;
    }

    public List<Card> getExplorationArea() {
        return explorationArea;
    }

    public void setExplorationArea(List<Card> explorationArea) {
        this.explorationArea = explorationArea;
    }

    public List<Card> getPlayerDiscard() {
        return playerDiscard;
    }

    public void setPlayerDiscard(List<Card> playerDiscard) {
        this.playerDiscard = playerDiscard;
    }

    public List<Card> getPlayerHand() {
        return playerHand;
    }

    public void setPlayerHand(List<Card> playerHand) {
        this.playerHand = playerHand;
    }

    public List<Card> getCardsDestroyed() {
        return cardsDestroyed;
    }

    public void setCardsDestroyed(List<Card> cardsDestroyed) {
        this.cardsDestroyed = cardsDestroyed;
    }

    public int getWave() {
        return wave;
    }

    public void setWave(int wave) {
        this.wave = wave;
    }

    public int getSurvivorsRescued() {
        return survivorsRescued;
    }

    public void setSurvivorsRescued(int survivorsRescued) {
        this.survivorsRescued = survivorsRescued;
    }

    public PlayerInteraction getPlayerInteraction() {
        return playerInteraction;
    }

    public int getDefends() {
        return defends;
    }

    public void setDefends(int defends) {
        this.defends = defends;
    }

    public boolean isCanRetrieveMedicalEquipment() {
        return canRetrieveMedicalEquipment;
    }

    public void setCanRetrieveMedicalEquipment(boolean canRetrieveMedicalEquipment) {
        this.canRetrieveMedicalEquipment = canRetrieveMedicalEquipment;
    }

    public boolean isArmsDamaged() {
        return armsDamaged;
    }

    public void setArmsDamaged(boolean armsDamaged) {
        this.armsDamaged = armsDamaged;
        if (this.armsDamaged) {
            List<Card> weaponsPrepared = this.getPlayerArea()
                .stream().filter((card -> (card.getSubType().equals("Striking") || card.getSubType().equals("Shooting"))
                    && ((PermanentCard) card).isPrepared()))
                .collect(Collectors.toList());
            if (weaponsPrepared.size() > 1) {
                this.getPlayerInteraction().showMessage("No puedes tener más de una arma preparada. Destruye una.");

                StringBuilder message = new StringBuilder("¿Qué arma quieres destruir?\n");
                for (Card card : weaponsPrepared) {
                    message.append("1. ").append(card.getName()).append("\n");
                }
                int option = this.getPlayerInteraction().selectOption(
                    message.toString(),
                    weaponsPrepared.size()
                );
                this.destroy(weaponsPrepared.get(option - 1));
            }
        }
    }

    public boolean isLegsDamaged() {
        return legsDamaged;
    }

    public void setLegsDamaged(boolean legsDamaged) {
        this.legsDamaged = legsDamaged;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public void setGameOver(boolean gameOver) {
        isGameOver = gameOver;
    }

    public void setPlayerInteraction(PlayerInteraction playerInteraction) {
        this.playerInteraction = playerInteraction;
    }

    public boolean isRifleInPlay() {
        return isRifleInPlay;
    }

    public void setRifleInPlay(boolean rifleInPlay) {
        isRifleInPlay = rifleInPlay;
    }
}
