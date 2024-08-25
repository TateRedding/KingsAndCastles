package ui.bars;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

import entities.Entity;
import gamestates.Play;
import main.Game;
import objects.GameObject;
import objects.SelectableGameObject;
import ui.buttons.Button;
import ui.buttons.ImageButton;
import ui.buttons.TextButton;
import utils.ImageLoader;
import utils.RenderText;

import static buildings.Building.*;
import static main.Game.TILE_SIZE;
import static ui.buttons.Button.*;
import static ui.buttons.Button.SPRITE;

public class ActionBar extends BottomBar {

    private Play play;

    private int selectedBuildingType = FARM;

    private TextButton buildingInterfaceButton;
    private ImageButton buildButton;
    private ArrayList<Button> buttons = new ArrayList<>();

    public ActionBar(Play play) {
        this.play = play;
        initButtons();
    }

    private void initButtons() {
        int xOffset = 128;
        float yOffset = (float) (BOTTOM_BAR_HEIGHT - (getButtonHeight(TEXT_SMALL_LONG) + getButtonHeight(SPRITE))) / 3;

        BufferedImage sprite = getBuildingSprite(selectedBuildingType);
        int buildButtonXStart = xOffset + (getButtonWidth(TEXT_SMALL_LONG) - getButtonWidth(SPRITE)) / 2;
        float maxSize = 64.0f;
        float scale = maxSize / Math.max((float) getBuildingWidth(selectedBuildingType) * TILE_SIZE, (float) getBuildingHeight(selectedBuildingType) * TILE_SIZE);
        buildButton = new ImageButton(SPRITE, buildButtonXStart, BOTTOM_BAR_Y + (int) yOffset, sprite, scale);

        buildingInterfaceButton = new TextButton(TEXT_SMALL_LONG, xOffset, BOTTOM_BAR_Y + getButtonHeight(SPRITE) + (int) (yOffset * 2), 24.0f, getBuildingName(selectedBuildingType));

        buttons.addAll(Arrays.asList(buildingInterfaceButton, buildButton));
    }

    @Override
    public void update() {
        super.update();
        for (Button b : buttons)
            b.update();
    }

    @Override
    public void render(Graphics g) {
        super.render(g);

        for (Button b : buttons)
            b.render(g);

        renderBuildingCost(g);

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

    private void renderBuildingCost(Graphics g) {
        ArrayList<BufferedImage> icons = new ArrayList<>(Arrays.asList(
                ImageLoader.icons[ICON_GOLD],
                ImageLoader.icons[ICON_LOG],
                ImageLoader.icons[ICON_STONE],
                ImageLoader.icons[ICON_IRON],
                ImageLoader.icons[ICON_COAL]
        ));

        ArrayList<Integer> costs = new ArrayList<>(Arrays.asList(
                getCostGold(selectedBuildingType),
                getCostLogs(selectedBuildingType),
                getCostStone(selectedBuildingType),
                getCostIron(selectedBuildingType),
                getCostCoal(selectedBuildingType)
        ));
        int maxDisplay = Math.min(icons.size(), costs.size());
        int textXOffset = 2;
        int xOffset = 8;
        g.setFont(Game.getGameFont(20f));
        g.setColor(Color.BLACK);
        for (int i = 0; i < maxDisplay; i++) {
            int cost = costs.get(i);
            if (cost <= 0)
                continue;
            BufferedImage icon = icons.get(i);
            int iconWidth = (int) (icon.getWidth());
            int iconHeight = (int) (icon.getHeight());
            int iconX = buildButton.getBounds().x + buildButton.getBounds().width + xOffset;
            int iconY = buildButton.getBounds().y + (iconHeight * i);
            int textX = iconX + iconWidth + textXOffset;
            g.drawImage(icon, iconX, iconY, iconWidth, iconHeight, null);
            RenderText.renderText(g, String.valueOf(cost), RenderText.LEFT, RenderText.CENTER, textX, iconY, g.getFontMetrics().stringWidth(String.valueOf(cost)), iconHeight);
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
        for (Button b : buttons)
            if (b.getBounds().contains(x, y))
                b.setMousePressed(true);
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        super.mouseReleased(x, y, button);
        if (button == MouseEvent.BUTTON1) {
            if (save.getBounds().contains(x, y) && save.isMousePressed())
                play.saveGame();
            if (buildButton.getBounds().contains(x, y) && buildButton.isMousePressed()) {
                play.setSelectedBuildingType(selectedBuildingType);
                play.setSelectedSGO(null);
            }
        }
        save.reset(x, y);
        for (Button b : buttons)
            b.reset(x, y);
    }

    @Override
    public void mouseMoved(int x, int y) {
        super.mouseMoved(x, y);
        for (Button b : buttons) {
            b.setMouseOver(false);
            if (b.getBounds().contains(x, y))
                b.setMouseOver(true);
        }
    }

    public Play getPlay() {
        return play;
    }

}
