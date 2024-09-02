package ui.bars;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

import gamestates.Play;
import main.Game;
import objects.Player;
import utils.ImageLoader;
import utils.RenderText;

public class GameStatBar extends TopBar {

    private Play play;
    private Player player;

    private int cols = 9;
    private int rows = 3;
    private float colWidth, rowHeight;

    public GameStatBar(Play play) {
        this.play = play;
        this.player = play.getPlayerByID(play.getActivePlayerID());
        this.colWidth = (float) (UI_WIDTH - (UI_WIDTH - play.getMiniMap().getBounds().x)) / cols;
        this.rowHeight = (float) TOP_BAR_HEIGHT / rows;
    }

    public void update() {

    }

    @Override
    public void render(Graphics g) {
        super.render(g);
        renderResourceStats(g);
    }

    private void renderResourceStats(Graphics g) {
        ArrayList<BufferedImage> icons = new ArrayList<>(Arrays.asList(
                ImageLoader.icons[ICON_POP],
                ImageLoader.icons[ICON_GOLD],
                ImageLoader.icons[ICON_FOOD],
                ImageLoader.icons[ICON_LOG],
                ImageLoader.icons[ICON_STONE],
                ImageLoader.icons[ICON_IRON],
                ImageLoader.icons[ICON_COAL]
        ));
        ArrayList<String> counts = new ArrayList<>(Arrays.asList(
                player.getPopulation() + "/" + player.getMaxPopulation(),
                String.valueOf(player.getGold()),
                String.valueOf(player.getFood()),
                String.valueOf(player.getLogs()),
                String.valueOf(player.getStone()),
                String.valueOf(player.getIron()),
                String.valueOf(player.getCoal())
        ));
        int maxDisplay = Math.min(icons.size(), counts.size());
        int textXOffset = 4;
        int startingCol = 2;
        float iconScale = 2.0f;
        g.setFont(Game.getGameFont(32f));
        g.setColor(Color.BLACK);
        for (int i = 0; i < maxDisplay; i++) {
            BufferedImage icon = icons.get(i);
            String count = counts.get(i);
            int iconWidth = (int) (icon.getWidth() * iconScale);
            int iconHeight = (int) (icon.getHeight() * iconScale);
            int totalWidth = iconWidth + textXOffset + g.getFontMetrics().stringWidth(count);
            int iconX = (int) ((i + startingCol) * colWidth + (colWidth - totalWidth) / 2);
            int iconY = (int) ((rowHeight - iconHeight) / 2);
            int textX = iconX + iconWidth + textXOffset;
            g.drawImage(icon, iconX, iconY, iconWidth, iconHeight, null);
            RenderText.renderText(g, count, RenderText.LEFT, RenderText.CENTER, textX, 0, totalWidth - textXOffset - iconWidth, (int) rowHeight);
        }
    }

    public void mousePressed(int x, int y, int button) {

    }

    public void mouseReleased(int x, int y, int button) {

    }

    public void mouseMoved(int x, int y) {

    }

    public Play getPlay() {
        return play;
    }

}
