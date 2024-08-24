package ui.bars;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

import entities.Entity;
import gamestates.Play;
import objects.GameObject;
import objects.SelectableGameObject;
import ui.buttons.ImageButton;
import utils.ImageLoader;

import static main.Game.SCREEN_WIDTH;
import static ui.buttons.Button.*;
import static ui.buttons.Button.SPRITE;

public class ActionBar extends BottomBar {

    private Play play;

    private ArrayList<ImageButton> spriteButtons = new ArrayList<>();
    private ArrayList<String> buttonLabels = new ArrayList<>();

    public ActionBar(Play play) {
        this.play = play;
        initTileButtons();
    }

    private void initTileButtons() {
        int numButtons = ImageLoader.buildings.length;
        int xOffset = 24;
        int buttonWidth = getButtonWidth(SPRITE);
        int buttonHeight = getButtonHeight(SPRITE);
        int x = (SCREEN_WIDTH - (buttonWidth * numButtons + xOffset * (numButtons - 1))) / 2;
        int y = BOTTOM_BAR_Y + ((BOTTOM_BAR_HEIGHT - buttonHeight) / 2);
        float scale = 2.0f;

        for (int i = 0; i < ImageLoader.buildings.length; i++) {
            spriteButtons.add(new ImageButton(SPRITE, x, y, ImageLoader.buildings[i], scale));
            x += buttonWidth + xOffset;
        }
        buttonLabels.addAll(Arrays.asList("Farm"));
    }

    @Override
    public void update() {
        super.update();
        if (play.getSelectedSGO() == null)
            for (ImageButton sb : spriteButtons)
                sb.update();
    }

    @Override
    public void render(Graphics g) {
        super.render(g);
        SelectableGameObject sgo = play.getSelectedSGO();
        if (sgo == null) {
            renderSpriteButtons(g);
        } else {
            BufferedImage sprite = null;
            if (sgo.getType() == GameObject.ENTITY)
                sprite = Entity.getSprite(((Entity) sgo).getEntityType(), Entity.IDLE, Entity.DOWN, 0);
            else if (sgo.getType() == GameObject.BUILDING) {
                //sprite = building sprite
            }
            renderSelection(g, sprite);
        }
    }

    private void renderSpriteButtons(Graphics g) {
        for (int i = 0; i < spriteButtons.size(); i++) {
            ImageButton button = spriteButtons.get(i);
            String label = buttonLabels.get(i);
            button.render(g);
            int startX = button.getBounds().x + (button.getBounds().width - g.getFontMetrics().stringWidth(label)) / 2;
            int startY = button.getBounds().y - 8;
            g.drawString(label, startX, startY);
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
        if (play.getSelectedSGO() == null)
            for (ImageButton sb : spriteButtons)
                if (sb.getBounds().contains(x, y))
                    sb.setMousePressed(true);
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        super.mouseReleased(x, y, button);
        if (button == MouseEvent.BUTTON1) {
            if (save.getBounds().contains(x, y) && save.isMousePressed())
                play.saveGame();
            for (int i = 0; i < spriteButtons.size(); i++)
                if (spriteButtons.get(i).getBounds().contains(x, y) && spriteButtons.get(i).isMousePressed())
                    play.setSelectedBuildingType(i);
        }
        save.reset(x, y);
        for (ImageButton sb : spriteButtons)
            sb.reset(x, y);
    }

    @Override
    public void mouseMoved(int x, int y) {
        super.mouseMoved(x, y);
        if (play.getSelectedSGO() == null)
            for (ImageButton sb : spriteButtons) {
                sb.setMouseOver(false);
                if (sb.getBounds().contains(x, y))
                    sb.setMouseOver(true);
            }
    }

    public Play getPlay() {
        return play;
    }

}
