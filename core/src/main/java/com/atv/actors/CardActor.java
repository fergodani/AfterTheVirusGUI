package com.atv.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class CardActor extends Actor {

    private Texture texture;

    /**
     * Constructor que carga la textura y establece el tamaño por defecto (200px de ancho)
     * manteniendo el aspect ratio de la imagen original.
     */
    public CardActor(String path) {
        this(path, 100f);
    }

    /**
     * Constructor que permite especificar el ancho deseado.
     * El alto se calcula automáticamente manteniendo la proporción de la imagen.
     */
    public CardActor(String path, float desiredWidth) {
        this.texture = new Texture(path);

        // Calcular el alto manteniendo el aspect ratio
        float aspectRatio = (float) texture.getHeight() / (float) texture.getWidth();
        float calculatedHeight = desiredWidth * aspectRatio;

        // Establecer el tamaño del actor
        setSize(desiredWidth, calculatedHeight);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
    }

    /**
     * Libera los recursos de la textura.
     * Debe ser llamado cuando ya no se necesite la carta.
     */
    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }

}
