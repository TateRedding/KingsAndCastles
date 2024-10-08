package handlers;

import main.Game;
import objects.Tile;
import utils.ImageLoader;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

import static main.Game.toPixelX;
import static main.Game.toPixelY;

public class TileHandler {

    private Game game;
    public static ArrayList<Integer> bitmaskIds = new ArrayList<>(Arrays.asList(0, 2, 8, 10, 11, 16, 18, 22, 24, 26, 27,
            30, 31, 64, 66, 72, 74, 75, 80, 82, 86, 88, 90, 91, 94, 95, 104, 106, 107, 120, 122, 123, 126, 127, 208,
            210, 214, 216, 218, 219, 222, 223, 248, 250, 251, 254, 255));

    public TileHandler(Game game) {
        this.game = game;
    }

    public void drawTiles(Tile[][] tileData, Graphics g, int xOffset, int yOffset) {
        for (int y = 0; y < tileData.length; y++)
            for (int x = 0; x < tileData[y].length; x++) {
                Tile currTile = tileData[y][x];
                int idx = bitmaskIds.indexOf(currTile.getBitmaskId());
                if (idx == -1)
                    continue;
                g.drawImage(ImageLoader.tiles.get(currTile.getTileType()).get(idx), toPixelX(x) - xOffset, toPixelY(y) - yOffset, null);
            }
    }

}
