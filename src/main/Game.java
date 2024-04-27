package main;

import static ui.bars.BottomBar.BOTTOM_BAR_HEIGHT;
import static ui.bars.TopBar.TOP_BAR_HEIGHT;

import java.awt.*;

import javax.swing.JFrame;

import gamestates.Credits;
import gamestates.Edit;
import gamestates.EditMapSelect;
import gamestates.GameStates;
import gamestates.LoadGame;
import gamestates.Menu;
import gamestates.Play;
import gamestates.PlayMapSelect;
import gamestates.State;
import handlers.SaveFileHandler;
import handlers.TileHandler;
import objects.Map;
import utils.LoadSave;
import utils.ImageLoader;

public class Game extends JFrame implements Runnable {

    private GameScreen gameScreen;
    private Thread gameThread;

    private final double FPS_SET = 120.0;
    private final double UPS_SET = 60.0;

    public final static int TILE_SIZE = 32;
    public final static int GAME_AREA_TILE_HEIGHT = 20;
    public final static int GAME_AREA_TILE_WIDTH = 36;
    public final static int GAME_AREA_HEIGHT = TILE_SIZE * GAME_AREA_TILE_HEIGHT;
    public final static int GAME_AREA_WIDTH = TILE_SIZE * GAME_AREA_TILE_WIDTH;
    public final static int SCREEN_WIDTH = GAME_AREA_WIDTH;
    public final static int SCREEN_HEIGHT = GAME_AREA_HEIGHT + TOP_BAR_HEIGHT + BOTTOM_BAR_HEIGHT;
    private static Font gameFont;

    private Credits credits;
    private Edit edit;
    private EditMapSelect editMapSelect;
    private LoadGame loadGame;
    private SaveFileHandler saveFileHandler;
    private Menu menu;
    private Play play;
    private PlayMapSelect playMapSelect;
    private TileHandler tileHandler;

    public Game() {
        LoadSave.createFolders();
        LoadSave.loadFont();
        gameFont = LoadSave.silverModified.deriveFont(Font.BOLD);
        ImageLoader.loadImages();

        initHandlers();
        initStates();

        gameScreen = new GameScreen(this);
        new GameFrame(gameScreen);
        gameScreen.requestFocus();
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.start();
    }

    private void initHandlers() {
        saveFileHandler = new SaveFileHandler();
        tileHandler = new TileHandler(this);
    }

    private void initStates() {
        credits = new Credits(this);
        editMapSelect = new EditMapSelect(this);
        loadGame = new LoadGame(this);
        menu = new Menu(this);
        playMapSelect = new PlayMapSelect(this);
    }

    private void start() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double timePerFrame = 1000000000.0 / FPS_SET;
        double timePerUpdate = 1000000000.0 / UPS_SET;
        long lastCheck = System.currentTimeMillis();
        long previousTime = System.nanoTime();

        int frames = 0;
        int updates = 0;

        double deltaU = 0;
        double deltaF = 0;

        while (true) {

            long currentTime = System.nanoTime();

            deltaU += (currentTime - previousTime) / timePerUpdate;
            deltaF += (currentTime - previousTime) / timePerFrame;
            previousTime = currentTime;

            if (deltaU >= 1) {
                updateGame();
                updates++;
                deltaU--;
            }

            if (deltaF >= 1) {
                gameScreen.repaint();
                frames++;
                deltaF--;
            }

            if (System.currentTimeMillis() - lastCheck >= 1000) {
                lastCheck = System.currentTimeMillis();
                System.out.println("FPS: " + frames + " | UPS: " + updates);
                frames = 0;
                updates = 0;
            }
        }
    }

    public State getCurrentGameState() {
        return switch (GameStates.gameState) {
            case CREDITS -> credits;
            case EDIT -> edit;
            case EDIT_MAP_SELECT -> editMapSelect;
            case LOAD_GAME -> loadGame;
            case MENU -> menu;
            case PLAY -> play;
            case PLAY_MAP_SELECT -> playMapSelect;
        };
    }

    private void updateGame() {
        getCurrentGameState().update();
    }

    public void render(Graphics g) {
        getCurrentGameState().render(g);
    }

    public void startGame(Play play) {
        this.play = play;
        GameStates.setGameState(GameStates.PLAY);
    }

    public void editMap(Map map) {
        edit = new Edit(this, map);
        GameStates.setGameState(GameStates.EDIT);
    }

    public void windowFocusLost() {
        // Pause the game
    }

    public Edit getEdit() {
        return edit;
    }

    public EditMapSelect getEditMapSelect() {
        return editMapSelect;
    }

    public static Font getGameFont(float size) {
        return gameFont.deriveFont(size);
    }

    public LoadGame getLoadGame() {
        return loadGame;
    }

    public Play getPlay() {
        return play;
    }

    public PlayMapSelect getPlayMapSelect() {
        return playMapSelect;
    }

    public SaveFileHandler getSaveFileHandler() {
        return saveFileHandler;
    }

    public TileHandler getTileHandler() {
        return tileHandler;
    }

}
