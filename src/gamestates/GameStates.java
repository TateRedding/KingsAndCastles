package gamestates;

public enum GameStates {

	CREDITS, EDIT, EDIT_MAP_SELECT, LOAD_GAME, MENU, PLAY, PLAY_MAP_SELECT;

	public static GameStates gameState = MENU;

	public static void setGameState(GameStates state) {
		gameState = state;
	}

}
