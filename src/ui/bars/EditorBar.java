package ui.bars;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

import static gamestates.Edit.*;
import static main.Game.SCREEN_WIDTH;
import static main.Game.getGameFont;
import static objects.Tile.WATER_SAND;
import static ui.buttons.Button.*;

import gamestates.Edit;
import main.Game;
import ui.buttons.ImageButton;
import utils.ImageLoader;
import utils.RenderText;

public class EditorBar extends BottomBar {

    private Edit edit;
    private ArrayList<ImageButton> spriteButtons = new ArrayList<>();
    private ArrayList<ImageButton> brushButtons = new ArrayList<>();
    private ArrayList<String> buttonLabels = new ArrayList<>();
    private ImageButton brushCircle, brushDown, brushSquare, brushUp;

    private boolean showCastleZoneWarning, showBrushButtons;
    private int brushSize;

    public EditorBar(Edit edit) {
        this.edit = edit;
        this.brushSize = edit.getBrushSize();
        initTileButtons();
        initBrushButtons();
    }

    private void initTileButtons() {
        int numButtons = ImageLoader.editorBarButtonSprites.size();
        int xOffset = 24;
        int buttonWidth = getButtonWidth(SPRITE);
        int buttonHeight = getButtonHeight(SPRITE);
        int x = (SCREEN_WIDTH - (buttonWidth * numButtons + xOffset * (numButtons - 1))) / 2;
        int y = BOTTOM_BAR_Y + ((BOTTOM_BAR_HEIGHT - buttonHeight) / 2);
        float scale = 2.0f;

        for (int i = 0; i < ImageLoader.editorBarButtonSprites.size(); i++) {
            spriteButtons.add(new ImageButton(SPRITE, x, y, ImageLoader.editorBarButtonSprites.get(i), scale));
            x += buttonWidth + xOffset;
        }
        buttonLabels.addAll(Arrays.asList("Grass", "Dirt", "Sand", "Watery Grass", "Watery Sand", "Castle Zones", "Gold Mine"));
    }

    private void initBrushButtons() {
        int xOffset = 16;
        int yOffset = 32;
        float scale = 1.0f;
        Rectangle lastSpriteButtonBounds = spriteButtons.get(spriteButtons.size() - 1).getBounds();
        int lastSpriteButtonEndX = lastSpriteButtonBounds.x + lastSpriteButtonBounds.width;

        int topYStart = BOTTOM_BAR_Y + (BOTTOM_BAR_HEIGHT - (getButtonHeight(ICON) * 2 + yOffset)) / 2;
        int bottomYStart = topYStart + getButtonHeight(ICON) + yOffset;
        int rightXStart = lastSpriteButtonEndX + ((UI_WIDTH - lastSpriteButtonEndX) - (getButtonWidth(ICON) * 2 + xOffset)) / 2;
        int leftXStart = rightXStart + getButtonWidth(ICON) + xOffset;
        brushSquare = new ImageButton(ICON, rightXStart, topYStart, ImageLoader.icons[ICON_SQUARE], scale);
        brushCircle = new ImageButton(ICON, leftXStart, topYStart, ImageLoader.icons[ICON_CIRCLE], scale);
        brushUp = new ImageButton(ICON, rightXStart, bottomYStart, ImageLoader.icons[ICON_UP], scale);
        brushDown = new ImageButton(ICON, leftXStart, bottomYStart, ImageLoader.icons[ICON_DOWN], scale);

        brushButtons.addAll(Arrays.asList(brushSquare, brushCircle, brushUp, brushDown));
    }

    @Override
    public void update() {
        super.update();
        this.brushSize = edit.getBrushSize();
        brushDown.setDisabled(brushSize <= 1);
        brushUp.setDisabled(brushSize >= edit.getMaxBrushSize());

        for (ImageButton sb : spriteButtons)
            sb.update();

        int selectedType = edit.getSelectedType();
        showBrushButtons = (selectedType != -1 && selectedType != CASTLE_ZONE && selectedType != GOLD_MINE);
        if (showBrushButtons)
            for (ImageButton bb : brushButtons)
                bb.update();
    }

    @Override
    public void render(Graphics g) {
        super.render(g);
        g.setColor(Color.BLACK);
        g.setFont(getGameFont(24f));

        renderSpriteButtons(g);
        if (showCastleZoneWarning)
            renderCastleZoneWarning(g);
        if (edit.getSelectedType() == WATER_SAND)
            renderWaterSandFinePrint(g);

        if (showBrushButtons) {
            renderBrushText(g);
            for (ImageButton bb : brushButtons)
                bb.render(g);
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

    private void renderCastleZoneWarning(Graphics g) {
        int warningX = save.getBounds().x + save.getBounds().width;
        int warningWidth = spriteButtons.get(0).getBounds().x - warningX;
        g.setColor(Color.RED);
        g.setFont(g.getFont().deriveFont(28f));
        String[] warning = {"Castle Zones must", "be the same", "number of tiles", "for every player!"};
        RenderText.renderText(g, warning, RenderText.CENTER, RenderText.CENTER, warningX, BOTTOM_BAR_Y, warningWidth, BOTTOM_BAR_HEIGHT);
    }

    private void renderWaterSandFinePrint(Graphics g) {
        String finePrint = "Due to tile set limitations, watery sand tiles are best not placed adjacent to dirt tiles or map edges.";
        int startY = spriteButtons.get(0).getBounds().y + spriteButtons.get(0).getBounds().height;
        int height = BOTTOM_BAR_Y + BOTTOM_BAR_HEIGHT - startY;
        g.setFont(Game.getGameFont(26f));
        RenderText.renderText(g, finePrint, RenderText.CENTER, RenderText.CENTER, 0, startY, UI_WIDTH, height);
    }

    private void renderBrushText(Graphics g) {
        String shape = "Brush Shape";
        String size = "Brush Size: " + edit.getBrushSize();
        Rectangle brushSquareBounds = brushSquare.getBounds();
        Rectangle brushUpBounds = brushUp.getBounds();
        g.setFont(Game.getGameFont(26f));
        FontMetrics fm = g.getFontMetrics();

        int shapeYStart = brushSquareBounds.y - fm.getDescent() - 2;
        int xOffset = brushCircle.getBounds().x - (brushSquareBounds.x + brushSquareBounds.width);
        int shapeXStart = brushSquareBounds.x + brushSquareBounds.width + (xOffset - fm.stringWidth(shape)) / 2;
        g.drawString(shape, shapeXStart, shapeYStart);

        int sizeYStart = brushUpBounds.y - fm.getDescent() - 2;
        int sizeXStart = brushUpBounds.x + brushUpBounds.width + (xOffset - fm.stringWidth(size)) / 2;
        g.drawString(size, sizeXStart, sizeYStart);
    }


    @Override
    public void mousePressed(int x, int y, int button) {
        super.mousePressed(x, y, button);
        for (ImageButton sb : spriteButtons)
            if (sb.getBounds().contains(x, y))
                sb.setMousePressed(true);
        for (ImageButton bb : brushButtons)
            if (bb.getBounds().contains(x, y))
                bb.setMousePressed(true);

    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        super.mouseReleased(x, y, button);
        if (button == MouseEvent.BUTTON1) {
            if (save.getBounds().contains(x, y) && save.isMousePressed())
                edit.saveMap();
            else if (brushSquare.getBounds().contains(x, y) && brushSquare.isMousePressed())
                edit.setBrushShape(SQUARE);
            else if (brushCircle.getBounds().contains(x, y) && brushCircle.isMousePressed())
                edit.setBrushShape(CIRCLE);
            else if (brushUp.getBounds().contains(x, y) && brushUp.isMousePressed()) {
                if (brushSize < edit.getMaxBrushSize()) {
                    brushSize++;
                    edit.setBrushSize(brushSize);
                }
            } else if (brushDown.getBounds().contains(x, y) && brushDown.isMousePressed())
                if (brushSize > 1) {
                    brushSize--;
                    edit.setBrushSize(brushSize);
                }

            for (int i = 0; i < spriteButtons.size(); i++) {
                ImageButton sb = spriteButtons.get(i);
                if (sb.getBounds().contains(x, y) && sb.isMousePressed())
                    edit.setSelectedType(i);
            }
        }

        for (ImageButton sb : spriteButtons)
            sb.reset(x, y);
        for (ImageButton bb : brushButtons)
            bb.reset(x, y);

        save.reset(x, y);
    }

    @Override
    public void mouseMoved(int x, int y) {
        super.mouseMoved(x, y);
        for (ImageButton sb : spriteButtons) {
            sb.setMouseOver(false);
            if (sb.getBounds().contains(x, y))
                sb.setMouseOver(true);
        }
        for (ImageButton bb : brushButtons) {
            bb.setMouseOver(false);
            if (bb.getBounds().contains(x, y))
                bb.setMouseOver(true);
        }
    }

    public Edit getEdit() {
        return edit;
    }

    public void setShowCastleZoneWarning(boolean showCastleZoneWarning) {
        this.showCastleZoneWarning = showCastleZoneWarning;
    }

}
