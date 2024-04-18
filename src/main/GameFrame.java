package main;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.JFrame;

public class GameFrame extends JFrame {

	public GameFrame(GameScreen gameScreen) {

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		add(gameScreen);
		pack();
		setResizable(false);
		setTitle("Kings and Castles");
		setLocationRelativeTo(null);
		setVisible(true);
		addWindowFocusListener(new WindowFocusListener() {

			@Override
			public void windowGainedFocus(WindowEvent e) {

			};

			@Override
			public void windowLostFocus(WindowEvent e) {
				gameScreen.getGame().windowFocusLost();
			}
		});
	}
	
}
