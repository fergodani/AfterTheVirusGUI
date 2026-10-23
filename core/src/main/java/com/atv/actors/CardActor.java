package com.atv.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.atv.models.cards.Card;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class CardActor extends Actor {

    private static final float SELECTION_BORDER_WIDTH = 4f;

    private Texture texture;
    private Texture selectionTexture;
    private boolean ownsTexture = true;
    private boolean selected;
    private Card card;

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

    /** Crea un actor para una carta del modelo usando una textura compartida (no se libera en dispose). */
    public CardActor(Card card, Texture texture, float desiredWidth) {
        this.card = card;
        this.texture = texture;
        this.ownsTexture = false;
        setSize(desiredWidth, desiredWidth * texture.getHeight() / texture.getWidth());
    }

    public Card getCard() {
        return card;
    }

    public void setSelected(boolean selected, Texture selectionTexture) {
        this.selected = selected;
        this.selectionTexture = selectionTexture;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        batch.draw(texture, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(),
            getScaleX(), getScaleY(), getRotation(), 0, 0, texture.getWidth(), texture.getHeight(), false, false);
        if (selected && selectionTexture != null) {
            drawSelectionBorder(batch, parentAlpha);
        }
        batch.setColor(Color.WHITE);
    }

    private void drawSelectionBorder(Batch batch, float parentAlpha) {
        float borderWidth = Math.min(SELECTION_BORDER_WIDTH, Math.min(getWidth(), getHeight()) / 2f);
        batch.setColor(Color.GOLD.r, Color.GOLD.g, Color.GOLD.b, parentAlpha);
        drawSelectionEdge(batch, 0f, 0f, getWidth(), borderWidth);
        drawSelectionEdge(batch, 0f, getHeight() - borderWidth, getWidth(), borderWidth);
        drawSelectionEdge(batch, 0f, borderWidth, borderWidth, getHeight() - 2f * borderWidth);
        drawSelectionEdge(batch, getWidth() - borderWidth, borderWidth, borderWidth, getHeight() - 2f * borderWidth);
    }

    private void drawSelectionEdge(Batch batch, float x, float y, float width, float height) {
        batch.draw(
            selectionTexture,
            getX() + x,
            getY() + y,
            getOriginX() - x,
            getOriginY() - y,
            width,
            height,
            getScaleX(),
            getScaleY(),
            getRotation(),
            0,
            0,
            selectionTexture.getWidth(),
            selectionTexture.getHeight(),
            false,
            false
        );
    }

    /**
     * Libera los recursos de la textura.
     * Debe ser llamado cuando ya no se necesite la carta.
     */
    public void dispose() {
        if (ownsTexture && texture != null) {
            texture.dispose();
        }
    }

}
