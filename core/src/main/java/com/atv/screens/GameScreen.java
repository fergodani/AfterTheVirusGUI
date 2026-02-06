package com.atv.screens;

import com.atv.MainGame;
import com.atv.actors.CardActor;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameScreen extends BaseScreen {

    private Stage stage;

    public GameScreen(MainGame game) {
        super(game);
    }

    @Override
    public void show() {
        Skin skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        stage = new Stage();
        stage.setViewport(game.viewport);
        Gdx.input.setInputProcessor(stage);

        Table mainTable = new Table();
        mainTable.setDebug(false);
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        HorizontalGroup scoutGroup = new HorizontalGroup();

        HorizontalGroup scoutArea = new HorizontalGroup();
        HorizontalGroup zombiesArea = new HorizontalGroup();
        HorizontalGroup playArea = new HorizontalGroup();
        HorizontalGroup playerHand = new HorizontalGroup();

        ScrollPane scoutPane = new ScrollPane(scoutArea);
        scoutPane.setScrollingDisabled(false, true);
        ScrollPane zombiesPane = new ScrollPane(zombiesArea);
        zombiesPane.setScrollingDisabled(false, true);
        ScrollPane playPane = new ScrollPane(playArea);
        playPane.setScrollingDisabled(false, true);
        ScrollPane handPane = new ScrollPane(playerHand);
        handPane.setScrollingDisabled(false, true);

        for (int i = 0; i < 10; i++) {
            scoutArea.addActor(createCard("ATFBackCard.jpg", 75));
            zombiesArea.addActor(createCard("ATFBackCard.jpg", 75));
            playArea.addActor(createCard("ATFBackCard.jpg", 75));
            playerHand.addActor(createCard("ATFBackCard.jpg", 75));
        }

        mainTable.add(scoutPane).expandX().fillX().expandY().row();
        mainTable.add(zombiesPane).expandX().fillX().expandY().row();
        mainTable.add(playPane).expandX().fillX().expandY().row();
        mainTable.add(handPane).fillX().expandY().row();
    }

    private CardActor createCard(String path) {
        return new CardActor(path);
    }

    private CardActor createCard(String path, int width) {
        return new CardActor(path, width);
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
        stage.dispose();
    }
}
