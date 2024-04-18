package src.gamestates;

import static main.Game.SCREEN_HEIGHT;
import static main.Game.SCREEN_WIDTH;
import static ui.buttons.Button.TEXT_LARGE;
import static ui.buttons.Button.getButtonHeight;
import static ui.buttons.Button.getButtonWidth;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

import main.Game;
import ui.buttons.TextButton;

public class Menu extends State implements StateMethods {

	private TextButton newGame, loadGame, editMap, quit;
	private ArrayList<TextButton> buttons = new ArrayList<TextButton>();

	public Menu(Game game) {
		super(game);
		initButtons();
	}

	private void initButtons() {
		float fontSize = 46f;
		int numButtons = 4;
		int yOffset = 30;
		int buttonHeight = getButtonHeight(TEXT_LARGE);
		int x = (SCREEN_WIDTH - getButtonWidth(TEXT_LARGE)) / 2;
		int y = (SCREEN_HEIGHT - (buttonHeight * numButtons + yOffset * (numButtons - 1))) / 2;
		
		newGame = new TextButton(TEXT_LARGE, "New Game", fontSize, x, y);
		loadGame = new TextButton(TEXT_LARGE, "Load Game", fontSize, x, y += buttonHeight + yOffset);
		editMap = new TextButton(TEXT_LARGE, "Edit Map", fontSize, x, y += buttonHeight + yOffset);
		quit = new TextButton(TEXT_LARGE, "Quit", fontSize, x, y += buttonHeight + yOffset);
		
		buttons.addAll(Arrays.asList(newGame, loadGame, editMap, quit));
	}

	@Override
	public void update() {
		for (TextButton b : buttons)
			b.update();
	}

	@Override
	public void render(Graphics g) {
		for (TextButton b : buttons)
			b.render(g);
	}

	@Override
	public void mousePressed(int x, int y, int button) {
		if (button == MouseEvent.BUTTON1)
			for (TextButton b : buttons)
				if (b.getBounds().contains(x, y))
					b.setMousePressed(true);
	}

	@Override
	public void mouseReleased(int x, int y, int button) {
		if (button == MouseEvent.BUTTON1)
			if (newGame.getBounds().contains(x, y) && newGame.isMousePressed())
				GameStates.setGameState(GameStates.PLAY_MAP_SELECT);
			else if (loadGame.getBounds().contains(x, y) && loadGame.isMousePressed())
				GameStates.setGameState(GameStates.LOAD_GAME);
			else if (editMap.getBounds().contains(x, y) && editMap.isMousePressed())
				GameStates.setGameState(GameStates.EDIT_MAP_SELECT);
			else if (quit.getBounds().contains(x, y) && quit.isMousePressed()) {
				System.exit(0);
			}

		for (TextButton b : buttons)
			b.reset();
	}

	@Override
	public void mouseMoved(int x, int y) {
		for (TextButton b : buttons)
			b.setMouseOver(false);

		for (TextButton b : buttons)
			if (b.getBounds().contains(x, y))
				b.setMouseOver(true);
	}

}
