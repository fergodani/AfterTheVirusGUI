package com.atv.screens;

import com.atv.MainGame;
import com.atv.gui.GuiPlayerInteraction;
import com.atv.models.Character;
import com.atv.models.Game;
import com.atv.models.cards.Card;
import com.atv.models.cards.ZombieCard;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.HashMap;
import java.util.Map;

public class GameScreen extends BaseScreen {

    private static final float CARD_WIDTH = 95f;
    private static final float CARD_HEIGHT = 124f;

    private Stage stage;
    private Skin skin;
    private Game model;
    private GuiPlayerInteraction interaction;
    private Label statusLabel;
    private final Map<String, Texture> cardTextures = new HashMap<String, Texture>();

    public GameScreen(MainGame game) {
        super(game);
    }

    @Override
    public void show() {
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        stage = new Stage(game.viewport);
        Gdx.input.setInputProcessor(stage);
        showCharacterSelection();
    }

    private void showCharacterSelection() {
        disposeCardTextures();
        stage.clear();
        Table table = new Table(skin);
        table.setFillParent(true);
        table.defaults().pad(8f).expandX().fillX();
        table.add(new Label("After the Virus", skin)).row();
        table.add(new Label("Elige un personaje para empezar", skin)).row();

        int index = 0;
        for (Character character : Character.values()) {
            ImageButton button = new ImageButton(
                new TextureRegionDrawable(new TextureRegion(textureForPath("characters/" + character.getName() + ".png")))
            );
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                    startGame(character);
                }
            });
            Table characterCard = new Table(skin);
            characterCard.add(button).size(200f, 143f).row();
            characterCard.add(new Label(character.getName(), skin));
            table.add(characterCard).expandX().fillX();
            if (++index % 2 == 0) {
                table.row();
            }
        }
        stage.addActor(table);
    }

    private void startGame(Character character) {
        model = new Game();
        interaction = new GuiPlayerInteraction(this::setStatus);
        model.setPlayerInteraction(interaction);
        model.init(character);
        buildBoard();
    }

    private void buildBoard() {
        disposeCardTextures();
        stage.clear();

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        float zoneHeight = game.viewport.getWorldHeight() / 3f;
        mainTable.add(createZone("Exploración", model.getExplorationArea()))
            .expandX().fill().height(zoneHeight).row();
        mainTable.add(createZone("Zombis", model.getZombieArea()))
            .expandX().fill().height(zoneHeight).row();
        mainTable.add(createPlayerZone()).expandX().fill().height(zoneHeight);
    }

    private Table createZone(String title, Iterable<Card> cards) {
        Table zone = new Table(skin);
        zone.defaults().pad(3f);
        zone.add(new Label(title, skin)).left().row();
        zone.add(createCardsPane(cards)).expand().fill();
        return zone;
    }

    private Table createPlayerZone() {
        Table zone = new Table(skin);
        zone.defaults().pad(2f);
        zone.add(new Label(gameState(), skin)).expandX().fillX().row();
        zone.add(new Label("En juego", skin)).left().row();
        zone.add(createCardsPane(model.getPlayerArea())).expandX().fillX().height(CARD_HEIGHT + 6f).row();

        Table actions = new Table(skin);
        addAction(actions, "Jugar", new Runnable() {
            @Override
            public void run() {
                model.playCard();
            }
        });
        addAction(actions, "Explorar", new Runnable() {
            @Override
            public void run() {
                model.explore();
            }
        });
        addAction(actions, "Recuperar", new Runnable() {
            @Override
            public void run() {
                model.retrieve();
            }
        });
        addAction(actions, "Preparar", new Runnable() {
            @Override
            public void run() {
                model.prepareCard();
            }
        });
        addAction(actions, "Usar", new Runnable() {
            @Override
            public void run() {
                model.useCard();
            }
        });
        addAction(actions, "Pasar", new Runnable() {
            @Override
            public void run() {
                model.pass();
            }
        });
        zone.add(actions).expandX().fillX().row();

        statusLabel = new Label("", skin);
        statusLabel.setWrap(true);
        zone.add(statusLabel).expandX().fillX().row();
        zone.add(new Label("Mano", skin)).left().row();
        zone.add(createCardsPane(model.getPlayerHand())).expandX().fillX().height(CARD_HEIGHT + 12f);
        String lastMessage = interaction.getLastMessage();
        setStatus(lastMessage == null ? "Toca una carta y después una acción." : lastMessage);
        return zone;
    }

    private ScrollPane createCardsPane(Iterable<Card> cards) {
        Table cardsTable = new Table(skin);
        for (Card card : cards) {
            Image image = new Image(new TextureRegionDrawable(new TextureRegion(textureFor(card))));
            image.setSize(CARD_WIDTH, CARD_HEIGHT);
            if (card.equals(interaction.getSelectedCard())) {
                image.setColor(Color.GOLD);
            }
            image.addListener(new ActorGestureListener(20, 0.01f, 0.2f, Integer.MAX_VALUE) {
                @Override
                public void tap(InputEvent event, float x, float y, int count, int button) {
                    interaction.select(card);
                    buildBoard();
                    setStatus("Seleccionada: " + card.getName());
                }

                @Override
                public boolean longPress(Actor actor, float x, float y) {
                    showCardZoom(card);
                    return true;
                }
            });
            cardsTable.add(image).size(CARD_WIDTH, CARD_HEIGHT).pad(2f);
        }
        ScrollPane pane = new ScrollPane(cardsTable, skin);
        pane.setScrollingDisabled(false, true);
        pane.setFadeScrollBars(false);
        return pane;
    }


    private void showCardZoom(Card card) {
        Dialog dialog = new Dialog(card.getName(), skin);
        dialog.setMovable(false);
        dialog.getContentTable().add(
            new Image(new TextureRegionDrawable(new TextureRegion(textureFor(card))))
        ).expand().fill();
        dialog.button("Cerrar");
        dialog.show(stage);
        dialog.setSize(CARD_WIDTH * 3, CARD_HEIGHT * 3);
        dialog.setPosition(stage.getViewport().getWorldWidth()/2f, stage.getViewport().getWorldHeight()/2f);
    }

    private void addAction(Table actions, String text, Runnable action) {
        TextButton button = new TextButton(text, skin);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                if (model.isGameOver() || model.isGameWon()) {
                    return;
                }
                action.run();
                buildBoard();
                if (model.isGameOver()) {
                    setStatus("La partida ha terminado: los zombis te han vencido.");
                } else if (model.isGameWon()) {
                    setStatus("Victoria: has rescatado " + Game.VICTORY_SURVIVOR_COUNT + " supervivientes.");
                }
            }
        });
        actions.add(button).expandX().fillX();
    }

    private Texture textureFor(Card card) {
        String path = "cards/" + cardAssetName(card);
        if (!Gdx.files.internal(path).exists()) {
            path = "ATFBackCard.jpg";
        }
        return textureForPath(path);
    }

    private Texture textureForPath(String path) {
        Texture texture = cardTextures.get(path);
        if (texture == null) {
            texture = new Texture(Gdx.files.internal(path));
            cardTextures.put(path, texture);
        }
        return texture;
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

    private void disposeCardTextures() {
        for (Texture texture : cardTextures.values()) {
            texture.dispose();
        }
        cardTextures.clear();
    }

    private String gameState() {
        return "Oleada " + model.getWave()
            + "  |  Rescatados " + model.getSurvivorsRescued() + "/" + Game.VICTORY_SURVIVOR_COUNT
            + "  |  Brazos: " + (model.isArmsDamaged() ? "heridos" : "bien")
            + "  |  Piernas: " + (model.isLegsDamaged() ? "heridas" : "bien");
    }

    private void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        disposeCardTextures();
        stage.dispose();
        skin.dispose();
    }
}
