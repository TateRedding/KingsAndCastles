package ui.bars;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

import static main.Game.SCREEN_WIDTH;
import static main.Game.getGameFont;
import static objects.Tile.WATER_SAND;
import static ui.buttons.Button.TEXT_SMALL;
import static ui.buttons.Button.SPRITE;
import static ui.buttons.Button.getButtonHeight;
import static ui.buttons.Button.getButtonWidth;

import gamestates.Edit;
import main.Game;
import ui.buttons.TextButton;
import ui.buttons.SpriteButton;
import utils.ImageLoader;
import utils.RenderText;

public class EditorBar extends BottomBar {

    private Edit edit;
    private ArrayList<SpriteButton> spriteButtons = new ArrayList<>();
    private ArrayList<String> buttonLabels = new ArrayList<>();

    private boolean showCastleZoneWarning;

    public EditorBar(Edit edit) {
        this.edit = edit;
        initTileButtons();
    }

    private void initTileButtons() {
        int numButtons = ImageLoader.editorBarButtonSprites.size();
        int xOffset = 24;
        int buttonWidth = getButtonWidth(SPRITE);
        int buttonHeight = getButtonHeight(SPRITE);
        int x = (SCREEN_WIDTH - (buttonWidth * numButtons + xOffset * (numButtons - 1))) / 2;
        int y = BOTTOM_BAR_Y + ((BOTTOM_BAR_HEIGHT - buttonHeight) / 2);

        for (int i = 0; i < ImageLoader.editorBarButtonSprites.size(); i++) {
            spriteButtons.add(new SpriteButton(ImageLoader.editorBarButtonSprites.get(i), x, y));
            x += buttonWidth + xOffset;
        }
        buttonLabels.addAll(Arrays.asList("Grass", "Dirt", "Sand", "Watery Grass", "Watery Sand", "Castle Zones", "Gold Mine"));
    }

    @Override
    public void update() {
        super.update();
        for (SpriteButton sb : spriteButtons) {
            sb.update();
        }
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
    }

    private void renderSpriteButtons(Graphics g) {
        for (int i = 0; i < spriteButtons.size(); i++) {
            SpriteButton button = spriteButtons.get(i);
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


    @Override
    public void mousePressed(int x, int y, int button) {
        super.mousePressed(x, y, button);
        for (SpriteButton sb : spriteButtons)
            if (sb.getBounds().contains(x, y))
                sb.setMousePressed(true);
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        super.mouseReleased(x, y, button);
        if (button == MouseEvent.BUTTON1)
            if (save.getBounds().contains(x, y) && save.isMousePressed())
                edit.saveMap();
        for (int i = 0; i < spriteButtons.size(); i++) {
            SpriteButton sb = spriteButtons.get(i);
            if (sb.getBounds().contains(x, y) && sb.isMousePressed())
                edit.setSelectedType(i);
        }

        for (SpriteButton sb : spriteButtons)
            sb.reset(x, y);
        save.reset(x, y);
    }

    @Override
    public void mouseMoved(int x, int y) {
        super.mouseMoved(x, y);
        for (SpriteButton sb : spriteButtons) {
            sb.setMouseOver(false);
            if (sb.getBounds().contains(x, y))
                sb.setMouseOver(true);
        }
    }

    public Edit getEdit() {
        return edit;
    }

    public void setShowCastleZoneWarning(boolean showCastleZoneWarning) {
        this.showCastleZoneWarning = showCastleZoneWarning;
    }

}
