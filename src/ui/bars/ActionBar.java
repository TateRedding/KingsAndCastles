package ui.bars;

import entities.buildings.Refinery;
import entities.buildings.StorageHut;
import entities.units.Laborer;
import entities.units.Unit;
import gamestates.Play;
import main.Game;
import entities.Entity;
import objects.Player;
import ui.buttons.Button;
import ui.buttons.ImageButton;
import ui.buttons.TextButton;
import ui.overlays.BuildingSelection;
import ui.overlays.Overlay;
import utils.ImageLoader;
import utils.RenderText;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

import static entities.buildings.Building.BUILDING;
import static entities.buildings.Building.*;
import static entities.buildings.Refinery.R_MAX_COAL;
import static entities.buildings.Refinery.R_MAX_IRON;
import static entities.buildings.StorageHut.SH_MAX_LOGS;
import static entities.buildings.StorageHut.SH_MAX_STONE;
import static entities.units.Laborer.*;
import static main.Game.*;
import static ui.bars.TopBar.TOP_BAR_HEIGHT;
import static ui.buttons.Button.*;

public class ActionBar extends BottomBar {

    private static final float MAX_BUTTON_SPRITE_SIZE = TILE_SIZE * 2;
    private static final float MAX_SELECTION_SPRITE_SIZE = TILE_SIZE * 4;

    private Play play;
    private Player player;

    private int selectedBuildingButtonType = VILLAGE;
    private Entity selectedEntity;

    private TextButton chooseBuilding, pause;
    private ImageButton buildButton, laborerSpawn, meleeUnitSpawn, rangedUnitSpawn;
    private ArrayList<Button> actionBarButtons = new ArrayList<>();

    private boolean showLaborerSpawnButton, showCombatUnitSpawnButtons;

    private ArrayList<BufferedImage> buildingCostIcons = new ArrayList<>(Arrays.asList(
            ImageLoader.icons[ICON_GOLD],
            ImageLoader.icons[ICON_LOG],
            ImageLoader.icons[ICON_STONE],
            ImageLoader.icons[ICON_IRON],
            ImageLoader.icons[ICON_COAL]
    ));
    private ArrayList<BufferedImage> laborerInventoryIcons = new ArrayList<>(buildingCostIcons.subList(1, 5));
    private ArrayList<BufferedImage> storageHutInventoryIcons = new ArrayList<>(laborerInventoryIcons.subList(0, 2));
    private ArrayList<BufferedImage> refineryInventoryIcons = new ArrayList<>(laborerInventoryIcons.subList(2, 4));

    private ArrayList<Integer> laborerMaxInventoryCounts = new ArrayList<>(Arrays.asList(
            L_MAX_LOGS,
            L_MAX_STONE,
            L_MAX_IRON,
            L_MAX_COAL
    ));
    private ArrayList<Integer> storageHutMaxInventoryCounts = new ArrayList<>(Arrays.asList(
            SH_MAX_LOGS,
            SH_MAX_STONE
    ));
    private ArrayList<Integer> refineryMaxInventoryCounts = new ArrayList<>(Arrays.asList(
            R_MAX_IRON,
            R_MAX_COAL
    ));

    public ActionBar(Play play) {
        super(play);
        this.play = (Play) mapState;
        this.player = play.getPlayerByID(play.getActivePlayerID());
        initBuildButtons();
        initSpawnButtons();

        String pauseText = (play.isPaused() ? "Unpause" : "Pause");

        pause = new TextButton(TEXT_SMALL_SHORT, save.getBounds().x, save.getBounds().y + save.getBounds().height + BOTTOM_BAR_OPTION_BUTTONS_Y_OFFSET, 21f, pauseText);
        actionBarButtons.add(pause);
    }

    private void initBuildButtons() {
        int xOffset = 128;
        float yOffset = (float) (BOTTOM_BAR_HEIGHT - (getButtonHeight(TEXT_SMALL_LONG) + getButtonHeight(SPRITE))) / 3;

        int buildButtonXStart = xOffset + (getButtonWidth(TEXT_SMALL_LONG) - getButtonWidth(SPRITE)) / 2;
        float scale = getSelectedBuildingSpriteScale();
        buildButton = new ImageButton(SPRITE, buildButtonXStart, BOTTOM_BAR_Y + (int) yOffset, ImageLoader.buildings[selectedBuildingButtonType], scale);

        chooseBuilding = new TextButton(TEXT_SMALL_LONG, xOffset, BOTTOM_BAR_Y + getButtonHeight(SPRITE) + (int) (yOffset * 2), 22f, "Choose Building");

        actionBarButtons.addAll(Arrays.asList(chooseBuilding, buildButton));
    }

    private void initSpawnButtons() {
        int xOffset = 8;
        float scale = 2.0f;
        int xStart = ((UI_WIDTH - (int) MAX_SELECTION_SPRITE_SIZE) / 2) + (int) MAX_SELECTION_SPRITE_SIZE + xOffset;
        int yStart = BOTTOM_BAR_Y + (BOTTOM_BAR_HEIGHT - getButtonHeight(SPRITE)) / 2;
        laborerSpawn = new ImageButton(SPRITE, xStart, yStart, ImageLoader.laborer[IDLE][DOWN][0], scale);
        meleeUnitSpawn = new ImageButton(SPRITE, xStart, yStart, null, scale);
        rangedUnitSpawn = new ImageButton(SPRITE, xStart + getButtonWidth(SPRITE) + xOffset, yStart, null, scale);
    }

    @Override
    public void update() {
        super.update();
        for (Button b : actionBarButtons)
            b.update();

        boolean canSpawn = player.getPopulation() < player.getMaxPopulation();
        laborerSpawn.setDisabled(!canSpawn);
        meleeUnitSpawn.setDisabled(!canSpawn);
        rangedUnitSpawn.setDisabled(!canSpawn);

        if (showLaborerSpawnButton)
            laborerSpawn.update();

        if (showCombatUnitSpawnButtons) {
            meleeUnitSpawn.update();
            rangedUnitSpawn.update();
        }
    }

    @Override
    public void render(Graphics g) {
        super.render(g);

        for (Button b : actionBarButtons)
            b.render(g);

        drawBuildingCost(g);

        if (selectedEntity != null) {
            BufferedImage sprite = null;
            int entityType = selectedEntity.getEntityType();
            int subType = selectedEntity.getSubType();
            if (entityType == UNIT)
                sprite = Unit.getSprite(subType, IDLE, DOWN, 0);
            else if (entityType == BUILDING)
                sprite = ImageLoader.buildings[subType];

            drawSelection(g, sprite);

            if (showLaborerSpawnButton)
                laborerSpawn.render(g);

            if (showCombatUnitSpawnButtons) {
                meleeUnitSpawn.render(g);
                rangedUnitSpawn.render(g);
            }

            if (entityType == UNIT && subType == LABORER)
                drawInventory(g, entityType, subType, laborerInventoryIcons, laborerMaxInventoryCounts);
            else if (entityType == BUILDING) {
                if (subType == STORAGE_HUT)
                    drawInventory(g, entityType, subType, storageHutInventoryIcons, storageHutMaxInventoryCounts);
                else if (subType == REFINERY)
                    drawInventory(g, entityType, subType, refineryInventoryIcons, refineryMaxInventoryCounts);
            }
        }
    }

    private void drawBuildingCost(Graphics g) {
        ArrayList<Integer> costs = new ArrayList<>(Arrays.asList(
                getCostGold(selectedBuildingButtonType),
                getCostLogs(selectedBuildingButtonType),
                getCostStone(selectedBuildingButtonType),
                getCostIron(selectedBuildingButtonType),
                getCostCoal(selectedBuildingButtonType)
        ));
        int maxDisplay = Math.min(buildingCostIcons.size(), costs.size());
        int textXOffset = 2;
        int xOffset = 8;
        g.setFont(Game.getGameFont(20f));
        g.setColor(Color.BLACK);
        for (int i = 0; i < maxDisplay; i++) {
            int cost = costs.get(i);
            if (cost <= 0)
                continue;
            BufferedImage icon = buildingCostIcons.get(i);
            int iconWidth = icon.getWidth();
            int iconHeight = icon.getHeight();
            int iconX = buildButton.getBounds().x + buildButton.getBounds().width + xOffset;
            int iconY = buildButton.getBounds().y + (iconHeight * i);
            int textX = iconX + iconWidth + textXOffset;
            g.drawImage(icon, iconX, iconY, iconWidth, iconHeight, null);
            RenderText.renderText(g, String.valueOf(cost), RenderText.LEFT, RenderText.CENTER, textX, iconY, g.getFontMetrics().stringWidth(String.valueOf(cost)), iconHeight);
        }
    }

    private void drawSelection(Graphics g, BufferedImage sprite) {
        if (sprite != null) {
            float scale = Math.min(2.0f, MAX_SELECTION_SPRITE_SIZE / Math.max(sprite.getWidth(), sprite.getHeight()));
            int spriteWidth = (int) (sprite.getWidth() * scale);
            int spriteHeight = (int) (sprite.getHeight() * scale);
            int xStart = (UI_WIDTH - spriteWidth) / 2;
            int yStart = BOTTOM_BAR_Y + (BOTTOM_BAR_HEIGHT - spriteHeight) / 2;
            g.drawImage(sprite, xStart, yStart, spriteWidth, spriteHeight, null);
        }
    }

    private void drawInventory(Graphics g, int entityType, int subType, ArrayList<BufferedImage> icons, ArrayList<Integer> maxCounts) {
        ArrayList<Integer> currCounts = getCounts(entityType, subType);

        int maxDisplay = Math.min(icons.size(), currCounts.size());
        int textXOffset = 2;
        int xOffset = 8;
        int yOffset = 32;
        g.setFont(Game.getGameFont(32f));
        g.setColor(Color.BLACK);

        int xStart = ((UI_WIDTH - (int) MAX_SELECTION_SPRITE_SIZE) / 2) + (int) MAX_SELECTION_SPRITE_SIZE + xOffset;
        int yStart = BOTTOM_BAR_Y + yOffset;
        int titleHeight = g.getFontMetrics().getHeight();
        String title = "Inventory";
        RenderText.renderText(g, title, RenderText.LEFT, RenderText.TOP, xStart, yStart, g.getFontMetrics().stringWidth(title), titleHeight);

        g.setFont(Game.getGameFont(20f));
        for (int i = 0; i < maxDisplay; i++) {
            int count = currCounts.get(i);
            BufferedImage icon = icons.get(i);
            int iconWidth = icon.getWidth();
            int iconHeight = icon.getHeight();
            int iconY = yStart + titleHeight + (iconHeight * i);
            int textX = xStart + iconWidth + textXOffset;
            g.drawImage(icon, xStart, iconY, iconWidth, iconHeight, null);
            String countText = String.valueOf(count);
            if (currCounts.get(i) >= maxCounts.get(i)) {
                g.setColor(Color.RED);
                countText += " Inventory full!";
            } else
                g.setColor(Color.BLACK);
            RenderText.renderText(g, countText, RenderText.LEFT, RenderText.CENTER, textX, iconY, g.getFontMetrics().stringWidth(String.valueOf(count)), iconHeight);
        }
    }

    private ArrayList<Integer> getCounts(int entityType, int subType) {
        ArrayList<Integer> currCounts = null;
        if (entityType == UNIT && subType == LABORER) {
            Laborer l = (Laborer) selectedEntity;
            currCounts = new ArrayList<>(Arrays.asList(
                    l.getLogs(),
                    l.getStone(),
                    l.getIron(),
                    l.getCoal()
            ));
        } else if (entityType == BUILDING) {
            if (subType == STORAGE_HUT) {
                StorageHut sh = (StorageHut) selectedEntity;
                currCounts = new ArrayList<>(Arrays.asList(
                        sh.getLogs(),
                        sh.getStone()
                ));
            } else if (subType == REFINERY) {
                Refinery r = (Refinery) selectedEntity;
                currCounts = new ArrayList<>(Arrays.asList(
                        r.getIron(),
                        r.getCoal()
                ));
            }
        }
        return currCounts;
    }

    private float getSelectedBuildingSpriteScale() {
        return MAX_BUTTON_SPRITE_SIZE / Math.max((float) getBuildingTileWidth(selectedBuildingButtonType) * TILE_SIZE, (float) getBuildingTileHeight(selectedBuildingButtonType) * TILE_SIZE);
    }

    @Override
    public void mousePressed(int x, int y, int button) {
        super.mousePressed(x, y, button);
        if (button == MouseEvent.BUTTON1) {
            for (Button b : actionBarButtons)
                if (b.getBounds().contains(x, y))
                    b.setMousePressed(true);

            if (showLaborerSpawnButton)
                if (laborerSpawn.getBounds().contains(x, y))
                    laborerSpawn.setMousePressed(true);

            if (showCombatUnitSpawnButtons) {
                if (meleeUnitSpawn.getBounds().contains(x, y))
                    meleeUnitSpawn.setMousePressed(true);
                else if (rangedUnitSpawn.getBounds().contains(x, y))
                    rangedUnitSpawn.setMousePressed(true);
            }
        }
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        super.mouseReleased(x, y, button);
        if (button == MouseEvent.BUTTON1 && !play.isShowBuildingSelection()) {
            if (save.getBounds().contains(x, y) && save.isMousePressed())
                play.saveGame();
            else if (pause.getBounds().contains(x, y) && pause.isMousePressed()) {
                play.setPaused(!play.isPaused());
                pause.setText(play.isPaused() ? "Unpause" : "Pause");
            } else if (buildButton.getBounds().contains(x, y) && buildButton.isMousePressed()) {
                play.setSelectedBuildingType(selectedBuildingButtonType);
                play.setSelectedEntity(null);
            } else if (chooseBuilding.getBounds().contains(x, y) && chooseBuilding.isMousePressed()) {
                if (play.getBuildingSelection() == null) {
                    int xStart = (GAME_AREA_WIDTH - Overlay.getOverlayWidth(Overlay.OVERLAY_LARGE)) / 2;
                    int yStart = TOP_BAR_HEIGHT + (GAME_AREA_HEIGHT - Overlay.getOverlayHeight(Overlay.OVERLAY_LARGE)) / 2;
                    play.setBuildingSelection(new BuildingSelection(xStart, yStart, play));
                }
                play.setShowBuildingSelection(true);
            } else {
                if (showLaborerSpawnButton)
                    if (laborerSpawn.getBounds().contains(x, y) && laborerSpawn.isMousePressed())
                        play.spawnUnit(LABORER);
                if (showCombatUnitSpawnButtons) {
                    if (meleeUnitSpawn.getBounds().contains(x, y) && meleeUnitSpawn.isMousePressed()) {
                        switch (selectedEntity.getSubType()) {
                            // Contains placeholders
                            case BARRACKS_TIER_1 -> play.spawnUnit(BRUTE);
                            case BARRACKS_TIER_2 -> play.spawnUnit(BRUTE);
                            case BARRACKS_TIER_3 -> play.spawnUnit(BRUTE);
                        }
                    } else if (rangedUnitSpawn.getBounds().contains(x, y) && rangedUnitSpawn.isMousePressed()) {
                        switch (selectedEntity.getSubType()) {
                            // Contains placeholders
                            case BARRACKS_TIER_1 -> play.spawnUnit(BRUTE);
                            case BARRACKS_TIER_2 -> play.spawnUnit(BRUTE);
                            case BARRACKS_TIER_3 -> play.spawnUnit(BRUTE);
                        }
                    }
                }
            }
        }
        save.reset(x, y);
        laborerSpawn.reset(x, y);
        meleeUnitSpawn.reset(x, y);
        rangedUnitSpawn.reset(x, y);
        for (Button b : actionBarButtons)
            b.reset(x, y);
    }

    @Override
    public void mouseMoved(int x, int y) {
        super.mouseMoved(x, y);
        laborerSpawn.setMouseOver(false);
        meleeUnitSpawn.setMouseOver(false);
        rangedUnitSpawn.setMouseOver(false);
        for (Button b : actionBarButtons) {
            b.setMouseOver(false);
            if (b.getBounds().contains(x, y))
                b.setMouseOver(true);
        }
        if (showLaborerSpawnButton)
            if (laborerSpawn.getBounds().contains(x, y))
                laborerSpawn.setMouseOver(true);

        if (showCombatUnitSpawnButtons) {
            if (meleeUnitSpawn.getBounds().contains(x, y))
                meleeUnitSpawn.setMouseOver(true);
            else if (rangedUnitSpawn.getBounds().contains(x, y))
                rangedUnitSpawn.setMouseOver(true);
        }
    }

    public Play getPlay() {
        return play;
    }

    public void setSelectedBuildingButtonType(int selectedBuildingButtonType) {
        this.selectedBuildingButtonType = selectedBuildingButtonType;
        float scale = getSelectedBuildingSpriteScale();
        buildButton.setDisplayImage(ImageLoader.buildings[selectedBuildingButtonType]);
        buildButton.setImageScale(scale);
    }

    public void setSelectedEntity(Entity selectedEntity) {
        this.selectedEntity = selectedEntity;
        showLaborerSpawnButton = false;
        showCombatUnitSpawnButtons = false;
        if (selectedEntity != null && selectedEntity.getEntityType() == BUILDING) {
            switch (selectedEntity.getSubType()) {
                case VILLAGE:
                    showLaborerSpawnButton = true;
                    break;
                case BARRACKS_TIER_1:
                    showCombatUnitSpawnButtons = true;
                    meleeUnitSpawn.setDisplayImage(ImageLoader.brute[IDLE][DOWN][0]);
                    // Placeholder
                    rangedUnitSpawn.setDisplayImage(ImageLoader.brute[IDLE][DOWN][0]);
                    break;
                case BARRACKS_TIER_2:
                    showCombatUnitSpawnButtons = true;
                    // Placeholder
                    meleeUnitSpawn.setDisplayImage(ImageLoader.brute[IDLE][DOWN][0]);
                    // Placeholder
                    rangedUnitSpawn.setDisplayImage(ImageLoader.brute[IDLE][DOWN][0]);
                    break;
                case BARRACKS_TIER_3:
                    showCombatUnitSpawnButtons = true;
                    // Placeholder
                    meleeUnitSpawn.setDisplayImage(ImageLoader.brute[IDLE][DOWN][0]);
                    // Placeholder
                    rangedUnitSpawn.setDisplayImage(ImageLoader.brute[IDLE][DOWN][0]);
                    break;
            }
        }
    }
}
