package handlers;

import gamestates.Play;

import java.io.Serializable;

public abstract class CombatEntityHandler implements Serializable {

    protected Play play;

    public CombatEntityHandler(Play play) {
        this.play = play;
    }

    public Play getPlay() {
        return play;
    }
}
