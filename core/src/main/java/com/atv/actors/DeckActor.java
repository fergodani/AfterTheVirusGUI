package com.atv.actors;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class DeckActor extends Actor {

    Texture texture;
    Label numberOfCards;


    public DeckActor(String path) {
        texture = new Texture(path);
        numberOfCards = new Texture();
    }
}
