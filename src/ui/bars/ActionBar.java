package ui.bars;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

import entities.units.Unit;
import gamestates.Play;
import main.Game;
import objects.Entity;
import ui.buttons.Button;
import ui.buttons.ImageButton;
import ui.buttons.TextButton;
import ui.overlays.BuildingSelection;
import ui.overlays.Overlay;
import utils.ImageLoader;
import utils.RenderText;

import static entities.buildings.Building.*;
import static main.Game.*;
import static ui.bars.TopBar.TOP_BAR_HEIGHT;
import static ui.buttons.Button.*;
import static ui.buttons.Button.SPRITE;

public class ActionBar extends BottomBar {

    private static final float MAX_BUTTON_SPRITE_SIZE = TILE_SIZE * 2;

    private Play play;

    private int selectedBuildingType = VILLAGE;

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

        int buildButtonXStart = xOffset + (getButtonWidth(TEXT_SMALL_LONG) - getButtonWidth(SPRITE)) / 2;
        float scale = getSelectedBuildingSpriteScale();
        buildButton = new ImageButton(SPRITE, buildButtonXStart, BOTTOM_BAR_Y + (int) yOffset, ImageLoader.buildings[selectedBuildingType], scale);

        buildingInterfaceButton = new TextButton(TEXT_SMALL_LONG, xOffset, BOTTOM_BAR_Y + getButtonHeight(SPRITE) + (int) (yOffset * 2), 22f, "Choose Building");

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

        Entity selectedEntity = play.getSelectedEntity();
        if (selectedEntity != null) {
            BufferedImage sprite = null;
            if (selectedEntity.getEntityType() == Entity.UNIT)
                sprite = Unit.getSprite(selectedEntity.getSubType(), Unit.IDLE, Unit.DOWN, 0);
            else if (selectedEntity.getEntityType() == Entity.BUILDING) {
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

    private float getSelectedBuildingSpriteScale() {
        return MAX_BUTTON_SPRITE_SIZE / Math.max((float) getBuildingTileWidth(selectedBuildingType) * TILE_SIZE, (float) getBuildingTileHeight(selectedBuildingType) * TILE_SIZE);
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
        if (button == MouseEvent.BUTTON1 && !play.isShowBuildingSelection()) {
            if (save.getBounds().contains(x, y) && save.isMousePressed())
                play.saveGame();
            if (buildButton.getBounds().contains(x, y) && buildButton.isMousePressed()) {
                play.setSelectedBuildingType(selectedBuildingType);
                play.setSelectedEntity(null);
            }
            if (buildingInterfaceButton.getBounds().contains(x, y) && buildingInterfaceButton.isMousePressed()) {
                if (play.getBuildingSelection() == null) {
                    int xStart = (GAME_AREA_WIDTH - Overlay.getOverlayWidth(Overlay.OVERLAY_LARGE)) / 2;
                    int yStart = TOP_BAR_HEIGHT + (GAME_AREA_HEIGHT - Overlay.getOverlayHeight(Overlay.OVERLAY_LARGE)) / 2;
                    play.setBuildingSelection(new BuildingSelection(xStart, yStart, play));
                }
                play.setShowBuildingSelection(true);
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

    public void setSelectedBuildingType(int selectedBuildingType) {
        this.selectedBuildingType = selectedBuildingType;
        float scale = getSelectedBuildingSpriteScale();
        buildButton.setDisplayImage(ImageLoader.buildings[selectedBuildingType]);
        buildButton.setImageScale(scale);
    }
}
