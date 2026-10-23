package com.atv.screens;

import com.atv.MainGame;
import com.atv.gui.GuiPlayerInteraction;
import com.atv.models.Character;
import com.atv.models.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;

public class CharacterSelectionScreen extends BaseScreen{

    private Stage stage;
    private Skin skin;
    private SpriteBatch spriteBatch;
    private Texture backgroundTexture;

    public CharacterSelectionScreen(MainGame game) {
        super(game);
        spriteBatch = new SpriteBatch();
        backgroundTexture = new Texture(Gdx.files.internal("background.jpg"));
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        stage = new Stage(game.viewport);
        Gdx.input.setInputProcessor(stage);
        showCharacterSelection();
    }

    private void showCharacterSelection() {
        stage.clear();
        Table table = new Table(skin);
        table.setFillParent(true);
        table.defaults().pad(8f).expandX().fillX();
        table.add(new Label("After the Virus", skin)).row();
        table.add(new Label("Elige un personaje para empezar", skin)).row();

        int index = 0;
        for (com.atv.models.Character character : Character.values()) {
            ImageButton button = new ImageButton(
                new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("characters/" + character.getName() + ".png"))))
            );
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                    game.startGame(character);
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

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        spriteBatch.setProjectionMatrix(game.viewport.getCamera().combined);
        spriteBatch.begin();
        spriteBatch.setColor(1f, 1f, 1f, 0.5f);
        spriteBatch.draw(backgroundTexture, 0, 0, game.viewport.getWorldWidth(), game.viewport.getWorldHeight());
        spriteBatch.setColor(1f, 1f, 1f, 1f);
        spriteBatch.end();
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
        skin.dispose();
        spriteBatch.dispose();
        backgroundTexture.dispose();
    }
}
