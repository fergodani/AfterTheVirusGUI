package com.atv.screens;

import com.atv.MainGame;
import com.atv.actors.CardActor;
import com.atv.gui.GuiPlayerInteraction;
import com.atv.models.Game;
import com.atv.models.cards.Card;
import com.atv.models.cards.PermanentCard;
import com.atv.models.cards.ZombieCard;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameScreen extends BaseScreen {

    private static final float HAND_MARGIN = 8f;
    private static final float MAX_CARD_WIDTH = 100f;
    private static final float FAN_MAX_ANGLE = 25f;
    private static final float FAN_ANGLE_PER_CARD = 12f;
    private static final float FAN_DROP = 25f;
    private static final float CARD_TRANSITION_DURATION = 0.15f;
    private static final float ZONE_GAP = 4f;
    private static final float ZONE_OVERLAY_OPACITY = 0.35f;
    private static final float PLAYER_HAND_AREA_HEIGHT = 160f;
    private static final float ACTION_ZONE_HEIGHT = 96f;
    private static final float ACTION_BUTTON_MIN_HEIGHT = 56f;
    private static final float PLAYER_ZONE_HEIGHT_WEIGHT = 2f;
    private static final float PLAYER_PILES_AREA_WIDTH = 150f;
    private static final float PILE_CARD_WIDTH = 58f;

    SpriteBatch spriteBatch;
    Texture backgroundTexture;
    private final Texture zoneOverlayTexture;
    private final Stage stage;
    private final Skin skin;
    private final Table playerZone;
    private final Table zombieZone;
    private final Table explorationZone;
    private final Table actionZone;
    private final Group handGroup;
    private final GuiPlayerInteraction playerInteraction;
    private final Map<String, Texture> cardTextures = new HashMap<>();
    private final Map<Card, Boolean> displayedPreparedStates = new HashMap<>();
    private final List<Card> displayedZombieArea = new ArrayList<>();
    private final List<Card> displayedExplorationArea = new ArrayList<>();
    private final List<Card> displayedHand = new ArrayList<>();
    private Label actionMessage;
    private int displayedDeckSize;
    private int displayedDiscardSize;
    private int displayedWave;
    private int displayedSurvivors;
    private boolean displayedArmsDamaged;
    private boolean displayedLegsDamaged;
    private int displayedZombieDeckSize;
    private int displayedExplorationDeckSize;

    public GameScreen(MainGame game) {
        super(game);
        spriteBatch = new SpriteBatch();
        backgroundTexture = new Texture(Gdx.files.internal("background.jpg"));
        zoneOverlayTexture = createZoneOverlayTexture();
        stage = new Stage(game.viewport);
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        playerZone = new Table();
        zombieZone = new Table();
        explorationZone = new Table();
        actionZone = new Table();
        handGroup = new Group();
        playerInteraction = new GuiPlayerInteraction(this::setActionMessage, this::refreshSelectionUi);
        game.gameModel.setPlayerInteraction(playerInteraction);
        playerZone.setDebug(false);
        zombieZone.setDebug(false);
        explorationZone.setDebug(false);
        stage.addActor(playerZone);
        stage.addActor(zombieZone);
        stage.addActor(explorationZone);
        stage.addActor(actionZone);
        stage.addActor(handGroup);
        buildActionZone();
        refreshPlayerZone();
        refreshZombieZone();
        refreshExplorationZone();
        layoutZones();
        buildHand();
    }

    private Texture createZoneOverlayTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 1f, 1f, 1f);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void buildHand() {
        handGroup.clearChildren();
        List<Card> hand = game.gameModel.getPlayerHand();
        displayedHand.clear();
        displayedHand.addAll(hand);
        int count = hand.size();
        if (count == 0) {
            return;
        }
        float availableWidth = MainGame.WORLD_WIDTH - 2 * HAND_MARGIN;
        float cardWidth = Math.min(MAX_CARD_WIDTH, availableWidth / count);
        // Las cartas se solapan para que el abanico quepa en pantalla
        float step = count > 1 ? Math.min(cardWidth * 0.6f, (availableWidth - cardWidth) / (count - 1)) : 0;
        float startX = (MainGame.WORLD_WIDTH - (cardWidth + step * (count - 1))) / 2f;
        float maxAngle = count > 1 ? Math.min(FAN_MAX_ANGLE, FAN_ANGLE_PER_CARD * (count - 1) / 2f) : 0;

        for (int i = 0; i < count; i++) {
            Card card = hand.get(i);
            CardActor actor = new CardActor(card, textureFor(card), cardWidth);
            // t va de -1 (izquierda) a 1 (derecha)
            float t = count > 1 ? (2f * i / (count - 1)) - 1f : 0f;
            actor.setOrigin(cardWidth / 2f, 0);
            actor.setRotation(-t * maxAngle);
            actor.setPosition(startX + i * step, -FAN_DROP * t * t);
            actor.setSelected(playerInteraction.isCardSelected(card), zoneOverlayTexture);
            enableDragging(actor);
            enableHandSelection(actor);
            handGroup.addActor(actor);
        }
    }

    private void buildActionZone() {
        actionZone.clearChildren();
        actionZone.defaults().pad(4f);
        if (playerInteraction.isSelectingDamage()) {
            actionZone.add(createActionButton("Brazos", playerInteraction::selectArmsDamage))
                .height(ACTION_BUTTON_MIN_HEIGHT).expandX().fillX();
            actionZone.add(createActionButton("Piernas", playerInteraction::selectLegsDamage))
                .height(ACTION_BUTTON_MIN_HEIGHT).expandX().fillX();
            actionZone.row();
            actionMessage = new Label("", skin);
            actionMessage.setWrap(true);
            actionZone.add(actionMessage).colspan(2).expandX().fillX();
            setActionMessage("¿Dónde quieres recibir el daño?");
            return;
        }
        if (playerInteraction.isSelectingCards()) {
            int selectedCount = playerInteraction.getSelectedCardCount();
            int requiredCount = playerInteraction.getRequiredSelectionCount();
            actionZone.add(createActionButton(
                "Confirmar descarte (" + selectedCount + "/" + requiredCount + ")",
                playerInteraction::confirmCardSelection
            )).height(ACTION_BUTTON_MIN_HEIGHT).expandX().fillX();
            actionZone.add(createActionButton("Cancelar", playerInteraction::cancelCardSelection))
                .height(ACTION_BUTTON_MIN_HEIGHT).expandX().fillX();
            actionZone.row();
            actionMessage = new Label("", skin);
            actionMessage.setWrap(true);
            actionZone.add(actionMessage).colspan(2).expandX().fillX();
            setActionMessage("Selecciona las cartas que quieres descartar.");
            return;
        }
        actionZone.add(createActionButton("Explorar", () -> game.gameModel.explore()))
            .height(ACTION_BUTTON_MIN_HEIGHT).expandX().fillX();
        actionZone.add(createActionButton("Pasar", () -> game.gameModel.pass()))
            .height(ACTION_BUTTON_MIN_HEIGHT).expandX().fillX();
        Card selectedCard = playerInteraction.getSelectedCard();
        int actionColumns = 2;
        if (selectedCard instanceof PermanentCard && game.gameModel.getPlayerArea().contains(selectedCard)) {
            String actionName = ((PermanentCard) selectedCard).isPrepared() ? "Usar" : "Preparar";
            actionZone.add(createActionButton(actionName, this::useSelectedPermanentCard))
                .height(ACTION_BUTTON_MIN_HEIGHT).expandX().fillX();
            actionColumns++;
        }
        if (game.gameModel.getExplorationArea().contains(selectedCard)) {
            actionZone.add(createActionButton("Obtener", this::retrieveSelectedExplorationCard))
                .height(ACTION_BUTTON_MIN_HEIGHT).expandX().fillX();
            actionColumns++;
        }
        actionZone.row();
        actionMessage = new Label("", skin);
        actionMessage.setWrap(true);
        actionZone.add(actionMessage).colspan(actionColumns).expandX().fillX();
    }

    private TextButton createActionButton(String text, Runnable action) {
        TextButton button = new TextButton(text, skin, "action-button");
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!game.gameModel.isGameOver() && !game.gameModel.isGameWon()) {
                    action.run();
                }
            }
        });
        return button;
    }

    private void setActionMessage(String message) {
        if (actionMessage != null) {
            actionMessage.setText(message);
        }
    }

    private void refreshSelectionUi() {
        buildActionZone();
        buildHand();
        refreshPlayerZone();
        refreshExplorationZone();
    }

    private void useSelectedPermanentCard() {
        Card selectedCard = playerInteraction.getSelectedCard();
        if (!(selectedCard instanceof PermanentCard) || !game.gameModel.getPlayerArea().contains(selectedCard)) {
            return;
        }
        if (((PermanentCard) selectedCard).isPrepared()) {
            game.gameModel.useCard();
        } else {
            game.gameModel.prepareCard();
        }
        buildActionZone();
        refreshPlayerZone();
    }

    private void retrieveSelectedExplorationCard() {
        Card selectedCard = playerInteraction.getSelectedCard();
        if (!game.gameModel.getExplorationArea().contains(selectedCard)) {
            return;
        }
        game.gameModel.retrieve();
        refreshExplorationZone();
        refreshPlayerZone();
        buildActionZone();
    }

    private void refreshPlayerZone() {
        playerZone.clearChildren();
        //playerZone.defaults().pad(2f);
        playerZone.add(createPilesArea()).width(PLAYER_PILES_AREA_WIDTH).fillY();
        playerZone.add(createPlayedCardsArea()).expand().fill();
        displayedPreparedStates.clear();
        for (Card card : game.gameModel.getPlayerArea()) {
            displayedPreparedStates.put(card, isPrepared(card));
        }
        displayedDeckSize = game.gameModel.getPlayerDeck().size();
        displayedDiscardSize = game.gameModel.getPlayerDiscard().size();
        displayedWave = game.gameModel.getWave();
        displayedSurvivors = game.gameModel.getSurvivorsRescued();
        displayedArmsDamaged = game.gameModel.isArmsDamaged();
        displayedLegsDamaged = game.gameModel.isLegsDamaged();
        playerZone.invalidateHierarchy();
    }

    private Table createPilesArea() {
        Table pilesArea = new Table();
        pilesArea.defaults().pad(1f);
        pilesArea.add(createPile("Mazo", new Image(cardBackTexture()), game.gameModel.getPlayerDeck().size()))
            .expand().fill();
        pilesArea.add(createPile("Descarte", discardTopCard(), game.gameModel.getPlayerDiscard().size()))
            .expand().fill();
        pilesArea.row();
        pilesArea.add(createPlayerStatus()).colspan(2).fillX();
        return pilesArea;
    }

    private Table createPlayerStatus() {
        Table status = new Table();
        status.add(new Label("Ronda: " + game.gameModel.getWave(), skin)).left();
        status.row();
        status.add(new Label(
            "Supervivientes: " + game.gameModel.getSurvivorsRescued() + "/" + Game.VICTORY_SURVIVOR_COUNT,
            skin
        )).right().row();
        status.add(createHealthIndicator("Brazos", game.gameModel.isArmsDamaged())).colspan(2).left().row();
        status.add(createHealthIndicator("Piernas", game.gameModel.isLegsDamaged())).colspan(2).left();
        return status;
    }

    private Label createHealthIndicator(String bodyPart, boolean damaged) {
        Label indicator = new Label(bodyPart + ": " + (damaged ? "herido" : "bien"), skin);
        indicator.setColor(damaged ? Color.SCARLET : Color.LIME);
        return indicator;
    }

    private Table createPile(String title, Actor card, int count) {
        Table pile = new Table();
        pile.add(new Label(title + " (" + count + ")", skin)).center().row();
        pile.add(card).size(PILE_CARD_WIDTH, PILE_CARD_WIDTH * cardHeightRatio(card)).center();
        return pile;
    }

    private Actor discardTopCard() {
        List<Card> discard = game.gameModel.getPlayerDiscard();
        if (discard == null) {
            return new Image(cardBackTexture());
        }
        Card topCard = null;
        for (int i = discard.size() - 1; i >= 0; i--) {
            if (discard.get(i) != null) {
                topCard = discard.get(i);
                break;
            }
        }
        if (topCard == null) {
            return new Image(cardBackTexture());
        }
        return new Image(new TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(
            textureFor(topCard)
        )));
    }

    private Texture cardBackTexture() {
        return cardTextures.computeIfAbsent("ATFBackCard.jpg", path -> new Texture(Gdx.files.internal(path)));
    }

    private float cardHeightRatio(Actor card) {
        if (card instanceof Image) {
            Image image = (Image) card;
            float width = image.getDrawable().getMinWidth();
            return width == 0f ? 1f : image.getDrawable().getMinHeight() / width;
        }
        return 1f;
    }

    private ScrollPane createPlayedCardsArea() {
        Table playedCards = new Table();
        playedCards.defaults().pad(2f);
        List<Card> cardsInPlay = game.gameModel.getPlayerArea();
        if (cardsInPlay.isEmpty()) {
            playedCards.add(new Label("No hay cartas en juego", skin)).expand().center();
        } else {
            float availableWidth = game.viewport.getWorldWidth() - PLAYER_PILES_AREA_WIDTH - 2 * HAND_MARGIN;
            float usedWidth = 0f;
            for (Card card : cardsInPlay) {
                Actor cardActor = createPlayedCardActor(card);
                if (usedWidth > 0f && usedWidth + cardActor.getWidth() + 4f > availableWidth) {
                    playedCards.row();
                    usedWidth = 0f;
                }
                playedCards.add(cardActor).size(cardActor.getWidth(), cardActor.getHeight());
                usedWidth += cardActor.getWidth() + 4f;
            }
        }
        ScrollPane pane = new ScrollPane(playedCards, skin);
        pane.setScrollingDisabled(true, false);
        pane.setFadeScrollBars(false);
        return pane;
    }

    private Actor createPlayedCardActor(Card card) {
        CardActor actor = new CardActor(card, textureFor(card), MAX_CARD_WIDTH);
        actor.setOrigin(actor.getWidth() / 2f, actor.getHeight() / 2f);
        actor.setSelected(card.equals(playerInteraction.getSelectedCard()), zoneOverlayTexture);
        enableZoom(actor);
        if (isPrepared(card)) {
            return actor;
        }

        Group slot = new Group();
        slot.setSize(actor.getHeight(), actor.getWidth());
        actor.setRotation(-90f);
        actor.setPosition(
            (slot.getWidth() - actor.getWidth()) / 2f,
            (slot.getHeight() - actor.getHeight()) / 2f
        );
        slot.addActor(actor);
        return slot;
    }

    private boolean isPrepared(Card card) {
        return !(card instanceof PermanentCard) || ((PermanentCard) card).isPrepared();
    }

    private void refreshPlayerZoneIfChanged() {
        List<Card> playerArea = game.gameModel.getPlayerArea();
        if (displayedDeckSize != game.gameModel.getPlayerDeck().size()
            || displayedDiscardSize != game.gameModel.getPlayerDiscard().size()
            || displayedWave != game.gameModel.getWave()
            || displayedSurvivors != game.gameModel.getSurvivorsRescued()
            || displayedArmsDamaged != game.gameModel.isArmsDamaged()
            || displayedLegsDamaged != game.gameModel.isLegsDamaged()
            || displayedPreparedStates.size() != playerArea.size()) {
            refreshPlayerZone();
            return;
        }
        for (Card card : playerArea) {
            if (!displayedPreparedStates.containsKey(card)
                || displayedPreparedStates.get(card) != isPrepared(card)) {
                refreshPlayerZone();
                return;
            }
        }
    }

    private void refreshZombieZone() {
        zombieZone.clearChildren();
        //zombieZone.defaults().pad(4f);
        zombieZone.add(createPile(
            "Zombis",
            new Image(cardBackTexture()),
            game.gameModel.getZombieDeck().size()
        ));
        zombieZone.add(createZombieCardsArea()).expand().fill();
        displayedZombieArea.clear();
        displayedZombieArea.addAll(game.gameModel.getZombieArea());
        displayedZombieDeckSize = game.gameModel.getZombieDeck().size();
        zombieZone.invalidateHierarchy();
    }

    private ScrollPane createZombieCardsArea() {
        Table zombiesInPlay = new Table();
        zombiesInPlay.defaults().pad(2f);
        List<Card> zombies = game.gameModel.getZombieArea();
        if (zombies.isEmpty()) {
            zombiesInPlay.add(new Label("No hay zombis en juego", skin)).expand().center();
        } else {
            float availableWidth = MainGame.WORLD_WIDTH - 2 * HAND_MARGIN;
            float cardWidth = Math.min(MAX_CARD_WIDTH, availableWidth / zombies.size());
            for (Card zombie : zombies) {
                CardActor actor = new CardActor(zombie, textureFor(zombie), cardWidth);
                actor.setOrigin(actor.getWidth() / 2f, actor.getHeight() / 2f);
                //enableZoom(actor);
                zombiesInPlay.add(actor);
            }
        }
        ScrollPane pane = new ScrollPane(zombiesInPlay, skin);
        pane.setScrollingDisabled(false, true);
        pane.setFadeScrollBars(false);
        return pane;
    }

    private void refreshZombieZoneIfChanged() {
        if (displayedZombieDeckSize != game.gameModel.getZombieDeck().size()
            || !displayedZombieArea.equals(game.gameModel.getZombieArea())) {
            refreshZombieZone();
        }
    }

    private void refreshExplorationZone() {
        explorationZone.clearChildren();
        explorationZone.defaults().pad(4f);
        explorationZone.add(createPile(
            "Exploración",
            new Image(cardBackTexture()),
            game.gameModel.getExplorationDeck().size()
        ));
        explorationZone.add(createExplorationCardsArea()).expand().fill();
        displayedExplorationArea.clear();
        displayedExplorationArea.addAll(game.gameModel.getExplorationArea());
        displayedExplorationDeckSize = game.gameModel.getExplorationDeck().size();
        explorationZone.invalidateHierarchy();
    }

    private ScrollPane createExplorationCardsArea() {
        Table explorationCards = new Table();
        //explorationCards.defaults().pad(2f);
        List<Card> cards = game.gameModel.getExplorationArea();
        if (cards.isEmpty()) {
            explorationCards.add(new Label("No hay cartas en exploración", skin)).expand().center();
        } else {
            float availableWidth = MainGame.WORLD_WIDTH - 2 * HAND_MARGIN;
            float cardWidth = Math.min(MAX_CARD_WIDTH, availableWidth / cards.size());
            for (Card card : cards) {
                CardActor actor = new CardActor(card, textureFor(card), cardWidth);
                actor.setOrigin(actor.getWidth() / 2f, actor.getHeight() / 2f);
                actor.setSelected(card.equals(playerInteraction.getSelectedCard()), zoneOverlayTexture);
                enableZoom(actor, true);
                explorationCards.add(actor);
            }
        }
        ScrollPane pane = new ScrollPane(explorationCards, skin);
        pane.setScrollingDisabled(false, true);
        pane.setFadeScrollBars(false);
        return pane;
    }

    private void refreshExplorationZoneIfChanged() {
        if (displayedExplorationDeckSize != game.gameModel.getExplorationDeck().size()
            || !displayedExplorationArea.equals(game.gameModel.getExplorationArea())) {
            refreshExplorationZone();
        }
    }

    private void refreshHandIfChanged() {
        if (!displayedHand.equals(game.gameModel.getPlayerHand())) {
            buildHand();
        }
    }

    private void layoutZones() {
        float worldWidth = game.viewport.getWorldWidth();
        float worldHeight = game.viewport.getWorldHeight();
        float availableHeight = worldHeight - PLAYER_HAND_AREA_HEIGHT - ACTION_ZONE_HEIGHT;
        float smallerZoneHeight = (availableHeight - 3 * ZONE_GAP) / (PLAYER_ZONE_HEIGHT_WEIGHT + 2f);
        float playerZoneHeight = smallerZoneHeight * PLAYER_ZONE_HEIGHT_WEIGHT;
        float playerZoneY = PLAYER_HAND_AREA_HEIGHT + ACTION_ZONE_HEIGHT + ZONE_GAP;
        playerZone.setBounds(0f, playerZoneY, worldWidth, playerZoneHeight);
        playerZone.invalidateHierarchy();
        zombieZone.setBounds(
            0f,
            playerZoneY + playerZoneHeight + ZONE_GAP,
            worldWidth,
            smallerZoneHeight
        );
        zombieZone.invalidateHierarchy();
        explorationZone.setBounds(
            0f,
            playerZoneY + playerZoneHeight + smallerZoneHeight + 2f * ZONE_GAP,
            worldWidth,
            smallerZoneHeight
        );
        explorationZone.invalidateHierarchy();
        actionZone.setBounds(0f, PLAYER_HAND_AREA_HEIGHT, worldWidth, ACTION_ZONE_HEIGHT);
        actionZone.invalidateHierarchy();
    }

    private void enableZoom(final CardActor actor) {
        enableZoom(actor, false);
    }

    private void enableZoom(final CardActor actor, boolean isExplorationDeck) {
        actor.addListener(new ActorGestureListener(20f, 0.4f, 0.5f, Float.MAX_VALUE) {
            private CardActor zoomedActor;

            @Override
            public boolean longPress(Actor pressedActor, float x, float y) {
                Vector2 position = actor.localToStageCoordinates(new Vector2());
                zoomedActor = new CardActor(actor.getCard(), textureFor(actor.getCard()), actor.getWidth());
                zoomedActor.setOrigin(zoomedActor.getWidth() / 2f, zoomedActor.getHeight() / 2f);
                zoomedActor.setPosition(
                    stage.getViewport().getWorldWidth() / 2f - zoomedActor.getWidth() / 2f,
                    position.y
                );
                zoomedActor.addAction(Actions.scaleTo(3.5f, 3.5f, CARD_TRANSITION_DURATION, Interpolation.sineOut));
                zoomedActor.setY(position.y + (isExplorationDeck ? -350f : 170f));
                stage.addActor(zoomedActor);
                actor.setVisible(false);
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                if (zoomedActor != null) {
                    zoomedActor.remove();
                    zoomedActor = null;
                    actor.setVisible(true);
                }
            }

            @Override
            public void tap(InputEvent event, float x, float y, int count, int button) {
                if (count == 1) {
                    if (actor.getCard().equals(playerInteraction.getSelectedCard())) {
                        playerInteraction.deselect();
                        setActionMessage("Deseleccionada: " + actor.getCard().getName());
                    } else {
                        playerInteraction.select(actor.getCard());
                        setActionMessage("Seleccionada: " + actor.getCard().getName());
                    }
                    buildActionZone();
                    refreshPlayerZone();
                    refreshExplorationZone();
                }
            }
        });
    }

    private void enableDragging(final CardActor actor) {
        actor.addListener(new DragListener() {
            private float dragOffsetX;
            private float dragOffsetY;

            private float originalRotation = actor.getRotation();
            private float originalX = actor.getX();
            private float originalY = actor.getY();

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (!super.touchDown(event, x, y, pointer, button)) {
                    return false;
                }
                dragOffsetX = actor.getX() - event.getStageX();
                dragOffsetY = 0f;
                actor.toFront();
                actor.setRotation(0);
                actor.clearActions();
                actor.addAction(Actions.scaleTo(4f, 4f, CARD_TRANSITION_DURATION, Interpolation.sineOut));
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                if (isInsidePlayerZone(event.getStageX(), event.getStageY()) && playCard(actor)) {
                    actor.clearActions();
                    return;
                }
                actor.clearActions();
                actor.addAction(Actions.parallel(
                    Actions.moveTo(originalX, originalY, CARD_TRANSITION_DURATION, Interpolation.sineIn),
                    Actions.rotateTo(originalRotation, CARD_TRANSITION_DURATION, Interpolation.sineIn),
                    Actions.scaleTo(1f, 1f, CARD_TRANSITION_DURATION, Interpolation.sineIn)
                ));
            }

            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                actor.setPosition(event.getStageX() + dragOffsetX, event.getStageY() + dragOffsetY);
            }
        });
    }

    private void enableHandSelection(final CardActor actor) {
        actor.addListener(new ActorGestureListener() {
            @Override
            public void tap(InputEvent event, float x, float y, int count, int button) {
                if (count == 1 && playerInteraction.isSelectingCards()) {
                    playerInteraction.toggleCardSelection(actor.getCard());
                }
            }
        });
    }

    private boolean isInsidePlayerZone(float stageX, float stageY) {
        return stageX >= playerZone.getX()
            && stageX <= playerZone.getX() + playerZone.getWidth()
            && stageY >= playerZone.getY()
            && stageY <= playerZone.getY() + playerZone.getHeight();
    }

    private boolean playCard(CardActor actor) {
        playerInteraction.select(actor.getCard());
        game.gameModel.playCard();
        return !game.gameModel.getPlayerHand().contains(actor.getCard());
    }

    private Texture textureFor(Card card) {
        String path = "cards/" + cardAssetName(card);
        if (!Gdx.files.internal(path).exists()) {
            path = "ATFBackCard.jpg";
        }
        return cardTextures.computeIfAbsent(path, p -> new Texture(Gdx.files.internal(p)));
    }

    private String cardAssetName(Card card) {
        if (card instanceof ZombieCard) {
            return "Zombie" + ((ZombieCard) card).getValue() + ".png";
        }
        if ("Med Kit".equals(card.getName())) {
            return "Medkit.png";
        }
        return card.getName().replace(' ', '_') + ".png";
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        refreshPlayerZoneIfChanged();
        refreshZombieZoneIfChanged();
        refreshExplorationZoneIfChanged();
        refreshHandIfChanged();
        spriteBatch.setProjectionMatrix(game.viewport.getCamera().combined);
        spriteBatch.begin();

        float worldWidth = this.game.viewport.getWorldWidth();
        float worldHeight = this.game.viewport.getWorldHeight();
        spriteBatch.setColor(1f, 1f, 1f, 0.5f);
        spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
        drawZoneOverlays(worldWidth, worldHeight);

        spriteBatch.end();

        stage.act(delta);
        stage.draw();
    }

    private void drawZoneOverlays(float worldWidth, float worldHeight) {
        float availableHeight = worldHeight - PLAYER_HAND_AREA_HEIGHT - ACTION_ZONE_HEIGHT;
        float smallerZoneHeight = (availableHeight - 3 * ZONE_GAP) / (PLAYER_ZONE_HEIGHT_WEIGHT + 2f);
        float playerZoneHeight = smallerZoneHeight * PLAYER_ZONE_HEIGHT_WEIGHT;
        float playerZoneY = PLAYER_HAND_AREA_HEIGHT + ACTION_ZONE_HEIGHT + ZONE_GAP;
        spriteBatch.setColor(0f, 0f, 0f, ZONE_OVERLAY_OPACITY);
        spriteBatch.draw(
            zoneOverlayTexture,
            0,
            playerZoneY + playerZoneHeight + smallerZoneHeight + 2f * ZONE_GAP,
            worldWidth,
            smallerZoneHeight
        );
        spriteBatch.draw(
            zoneOverlayTexture,
            0,
            playerZoneY + playerZoneHeight + ZONE_GAP,
            worldWidth,
            smallerZoneHeight
        );
        spriteBatch.draw(zoneOverlayTexture, 0, playerZoneY, worldWidth, playerZoneHeight);
        spriteBatch.draw(zoneOverlayTexture, 0, PLAYER_HAND_AREA_HEIGHT, worldWidth, ACTION_ZONE_HEIGHT);
        spriteBatch.setColor(1f, 1f, 1f, 1f);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        refreshPlayerZone();
        layoutZones();
    }

    @Override
    public void dispose() {
        stage.dispose();
        spriteBatch.dispose();
        backgroundTexture.dispose();
        zoneOverlayTexture.dispose();
        skin.dispose();
        for (Texture texture : cardTextures.values()) {
            texture.dispose();
        }
    }
}
