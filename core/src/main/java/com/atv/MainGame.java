package com.atv;

import com.atv.screens.GameScreen;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class MainGame extends Game {
    // Dimensiones virtuales del mundo del juego (en unidades)
    // Se verán igual en todas las plataformas, escalándose proporcionalmente
    public static final float WORLD_WIDTH = 480f;
    public static final float WORLD_HEIGHT = 800f;

    public ExtendViewport viewport;

    @Override
    public void create() {
        viewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT);
        this.setScreen(new GameScreen(this));
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

}
