package src.main;

import static main.Game.SCREEN_HEIGHT;
import static main.Game.SCREEN_WIDTH;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import inputs.KeyInputs;
import inputs.MouseInputs;

public class GameScreen extends JPanel {

	private Game game;
	private MouseInputs mouseInputs;

	public GameScreen(Game game) {
		this.game = game;

		mouseInputs = new MouseInputs(game);

		setPanelSize();
		addMouseListener(mouseInputs);
		addMouseMotionListener(mouseInputs);
		addMouseWheelListener(mouseInputs);
		addKeyListener(new KeyInputs(game));
	}

	private void setPanelSize() {
		Dimension size = new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT);
		setPreferredSize(size);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		game.render(g);
	}

	public Game getGame() {
		return game;
	}
	
}
