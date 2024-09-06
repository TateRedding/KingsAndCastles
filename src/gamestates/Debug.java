package gamestates;

import main.Game;
import ui.buttons.TextButton;
import utils.LoadSave;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.io.IOException;
import java.io.FileWriter;

import static ui.buttons.Button.*;
import static utils.LoadSave.loadDebugConfig;
import static utils.LoadSave.saveDebugConfig;

public class Debug extends State {

    public enum DebugToggle {
        SHOW_PATHS("Show paths"),
        SHOW_HITBOXES("Show hitboxes"),
        SHOW_CHUNK_BORDERS("Show chunk borders"),
        SHOW_TARGET_HITBOXES("Show target hitboxes");

        private final String label;

        DebugToggle(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public static EnumMap<DebugToggle, Boolean> config;

    private static final int TEXT_X_OFFSET = 16;

    private TextButton menu, clearMaps, clearGames, saveSettings;
    private ArrayList<TextButton> buttons = new ArrayList<>();
    private ArrayList<TextButton> toggleButtons = new ArrayList<>();

    public Debug(Game game) {
        super(game);
        initButtons();

        config = new EnumMap<>(DebugToggle.class);
        for (DebugToggle toggle : DebugToggle.values())
            config.put(toggle, false);

        config = loadDebugConfig();
    }

    private void initButtons() {
        float fontSize = 26f;
        int yOffset = 16;
        int buttonHeight = getButtonHeight(TEXT_SMALL_LONG);
        int x = 32;
        int y = 32;

        menu = new TextButton(TEXT_SMALL_LONG, x, y, fontSize, "Menu");
        clearMaps = new TextButton(TEXT_SMALL_LONG, x, y += buttonHeight + yOffset, fontSize, "Clear Maps");
        clearGames = new TextButton(TEXT_SMALL_LONG, x, y += buttonHeight + yOffset, fontSize, "Clear Games");

        for (DebugToggle toggle : DebugToggle.values()) {
            TextButton toggleButton = new TextButton(TEXT_SMALL_LONG, x, y += buttonHeight + yOffset, fontSize, "Toggle");
            toggleButtons.add(toggleButton);
        }

        saveSettings = new TextButton(TEXT_SMALL_LONG, x, y += buttonHeight + yOffset, fontSize, "Save Settings");

        buttons.addAll(Arrays.asList(menu, clearMaps, clearGames, saveSettings));
        buttons.addAll(toggleButtons);
    }


    @Override
    public void update() {
        for (TextButton tb : buttons)
            tb.update();
    }

    @Override
    public void render(Graphics g) {
        for (TextButton tb : buttons)
            tb.render(g);

        for (int i = 0; i < DebugToggle.values().length; i++) {
            DebugToggle toggle = DebugToggle.values()[i];
            renderLabel(g, toggleButtons.get(i), toggle.getLabel(), config.get(toggle));
        }
    }

    private void renderLabel(Graphics g, TextButton button, String label, boolean value) {
        Rectangle bounds = button.getBounds();
        int xStart = bounds.x + bounds.width + TEXT_X_OFFSET;
        int yStart = bounds.y + bounds.height;

        g.drawString(label + ": " + (value ? "True" : "False"), xStart, yStart);
    }

    @Override
    public void mousePressed(int x, int y, int button) {
        if (button == MouseEvent.BUTTON1)
            for (TextButton tb : buttons)
                if (tb.getBounds().contains(x, y))
                    tb.setMousePressed(true);
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        if (button == MouseEvent.BUTTON1) {
            if (menu.getBounds().contains(x, y) && menu.isMousePressed()) {
                GameStates.setGameState(GameStates.MENU);
            } else if (clearMaps.getBounds().contains(x, y) && clearMaps.isMousePressed()) {
                LoadSave.clearMaps();
                game.getSaveFileHandler().loadMaps();
            } else if (clearGames.getBounds().contains(x, y) && clearGames.isMousePressed()) {
                LoadSave.clearGames();
                game.getSaveFileHandler().loadGames();
            } else if (saveSettings.getBounds().contains(x, y) && saveSettings.isMousePressed()) {
                saveDebugConfig(config);
            } else {
                for (int i = 0; i < DebugToggle.values().length; i++) {
                    TextButton toggleButton = toggleButtons.get(i);
                    if (toggleButton.getBounds().contains(x, y) && toggleButton.isMousePressed()) {
                        DebugToggle toggle = DebugToggle.values()[i];
                        config.put(toggle, !config.get(toggle));
                    }
                }
            }
        }

        for (TextButton tb : buttons)
            tb.reset(x, y);
    }


    @Override
    public void mouseMoved(int x, int y) {
        for (TextButton tb : buttons)
            tb.setMouseOver(false);

        for (TextButton tb : buttons)
            if (tb.getBounds().contains(x, y))
                tb.setMouseOver(true);
    }
}
