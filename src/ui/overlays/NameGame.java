package ui.overlays;

import ui.TextBox;

import static ui.TextBox.TEXT;

public class NameGame extends Overlay {

    private TextBox name;

    public NameGame(int x, int y) {
        super(x, y);

        int nameX = bounds.x + (bounds.width - TextBox.getWidth(TEXT)) / 2;
        int nameY = 0;

        name = new TextBox(TEXT, nameX, nameY);
    }


}
