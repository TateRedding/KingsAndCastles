package ui.bars;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import static main.Game.SCREEN_WIDTH;
import static ui.buttons.Button.TEXT_SMALL;
import static ui.buttons.Button.SPRITE;
import static ui.buttons.Button.getButtonHeight;
import static ui.buttons.Button.getButtonWidth;

import gamestates.Edit;
import ui.buttons.TextButton;
import ui.buttons.SpriteButton;
import utils.ImageLoader;
import utils.RenderText;

public class EditorBar extends BottomBar {

    private Edit edit;
    private TextButton save;
    private ArrayList<SpriteButton> spriteButtons = new ArrayList<SpriteButton>();

    private boolean showCastleZoneWarning;

    public EditorBar(Edit edit) {
        this.edit = edit;
        Rectangle menuBounds = menu.getBounds();
        save = new TextButton(TEXT_SMALL, "Save", 28f, menuBounds.x, menuBounds.y + menuBounds.height + 5);
        initTileButtons();
    }

    private void initTileButtons() {
        int numButtons = ImageLoader.editorBarButtonSprites.size();
        int xOffset = 15;
        int buttonWidth = getButtonWidth(SPRITE);
        int buttonHeight = getButtonHeight(SPRITE);
        int x = (SCREEN_WIDTH - (buttonWidth * numButtons + xOffset * (numButtons - 1))) / 2;
        int y = BOTTOM_BAR_Y + ((BOTTOM_BAR_HEIGHT - buttonHeight) / 2);

        for (int i = 0; i < ImageLoader.editorBarButtonSprites.size(); i++) {
            spriteButtons.add(new SpriteButton(ImageLoader.editorBarButtonSprites.get(i), x, y));
            x += buttonWidth + xOffset;
        }
    }

    @Override
    public void update() {
        super.update();
        save.update();
        for (SpriteButton sb : spriteButtons) {
            sb.update();
        }
    }

    @Override
    public void render(Graphics g) {
        super.render(g);
        save.render(g);
        for (SpriteButton sb : spriteButtons) {
            sb.render(g);
        }

        if (showCastleZoneWarning) {
            int warningX = save.getBounds().x + save.getBounds().width;
            int warningWidth = spriteButtons.get(0).getBounds().x - warningX;
            g.setColor(Color.RED);
            g.setFont(g.getFont().deriveFont(28f));
            String[] warning = {"Castle Zones must", "be the same", "number of tiles", "for every player!"};
            RenderText.renderText(g, warning, RenderText.CENTER, RenderText.CENTER, warningX, BOTTOM_BAR_Y, warningWidth, BOTTOM_BAR_HEIGHT);
        }
    }

    @Override
    public void mousePressed(int x, int y, int button) {
        super.mousePressed(x, y, button);
        if (button == MouseEvent.BUTTON1)
            if (save.getBounds().contains(x, y))
                save.setMousePressed(true);
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
        save.setMouseOver(false);
        if (save.getBounds().contains(x, y))
            save.setMouseOver(true);
        for (SpriteButton sb : spriteButtons) {
            sb.setMouseOver(false);
            if (sb.getBounds().contains(x, y))
                sb.setMouseOver(true);
        }
    }

    public Edit getEdit() {
        return edit;
    }

    public TextButton getSave() {
        return save;
    }

    public void setShowCastleZoneWarning(boolean showCastleZoneWarning) {
        this.showCastleZoneWarning = showCastleZoneWarning;
    }

}
