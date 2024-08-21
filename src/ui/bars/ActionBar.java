package ui.bars;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import entities.Entity;
import gamestates.Play;
import objects.GameObject;
import objects.SelectableGameObject;

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
        SelectableGameObject sgo = play.getSelectedSGO();
        if (sgo != null) {
            BufferedImage sprite = null;
            if (sgo.getType() == GameObject.ENTITY)
                sprite = Entity.getSprite(((Entity) sgo).getEntityType(), Entity.IDLE, Entity.DOWN, 0);
            else if (sgo.getType() == GameObject.BUILDING) {
                //sprite = building sprite
            }
            renderSelection(g, sprite);
        }
    }

    private void renderSelection(Graphics g, BufferedImage sprite) {
        float scale = 2.0f;
        if (sprite != null) {
            int spriteWidth = (int) (sprite.getWidth() * scale);
            int spriteHeight = (int) (sprite.getHeight() * scale);
            int xStart = (UI_WIDTH - spriteWidth) / 2;
            int yStart = BOTTOM_BAR_Y + (BOTTOM_BAR_HEIGHT - spriteHeight) / 2;
            g.drawImage(sprite, xStart, yStart, spriteWidth, spriteHeight, null);
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
