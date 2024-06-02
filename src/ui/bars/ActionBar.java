package ui.bars;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import entities.Entity;
import gamestates.Play;
import objects.GameObject;
import resources.ResourceObject;
import utils.ImageLoader;

public class ActionBar extends BottomBar {

    private Play play;

    public ActionBar(Play play) {
        this.play = play;
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void render(Graphics g) {
        super.render(g);
        if (play.getSelectedGameObject() != null)
            renderSelectedGameObject(g);
    }

    private void renderSelectedGameObject(Graphics g) {
        GameObject go = play.getSelectedGameObject();
        float scale = 2.0f;

        if (go.getCategory() == GameObject.ENTITY) {
            BufferedImage sprite = switch (go.getCategory()) {
                case GameObject.ENTITY -> Entity.getSprite(go.getType());
                case GameObject.RESOURCE -> {
                    ResourceObject re = (ResourceObject) go;
                    yield ImageLoader.resourceObjects[re.getType()][re.getSpriteId()];
                }
                default -> null;
            };

            if (sprite != null) {
                int spriteWidth = (int) (sprite.getWidth() * scale);
                int spriteHeight = (int) (sprite.getHeight() * scale);
                int xStart = (UI_WIDTH - spriteWidth) / 2;
                int yStart = BOTTOM_BAR_Y + (BOTTOM_BAR_HEIGHT - spriteHeight) / 2;
                g.drawImage(sprite, xStart, yStart, spriteWidth, spriteHeight, null);
            }
        }

    }

    @Override
    public void mousePressed(int x, int y, int button) {
        super.mousePressed(x, y, button);
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        super.mouseReleased(x, y, button);
        if (button == MouseEvent.BUTTON1)
            if (save.getBounds().contains(x, y) && save.isMousePressed())
                play.saveGame();
        save.reset(x, y);
    }

    @Override
    public void mouseMoved(int x, int y) {
        super.mouseMoved(x, y);
    }

    public Play getPlay() {
        return play;
    }

}
