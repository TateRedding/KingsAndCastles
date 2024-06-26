package objects;

import gamestates.Play;

import java.io.Serializable;

public class Player implements Serializable {

    private Play play;
    private int playerNum;

    private int coal = 0;
    private int food = 100;
    private int gold = 10000;
    private int iron = 0;
    private int population = 1;
    private int stone = 250;
    private int wood = 500;

    private boolean isHuman;

    public Player(Play play, int playerNum, boolean isHuman) {
        this.play = play;
        this.playerNum = playerNum;
        this.isHuman = isHuman;
    }

    public int getCoal() {
        return coal;
    }

    public void setCoal(int coal) {
        this.coal = coal;
    }

    public int getFood() {
        return food;
    }

    public void setFood(int food) {
        this.food = food;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int getIron() {
        return iron;
    }

    public void setIron(int iron) {
        this.iron = iron;
    }

    public Play getPlay() {
        return play;
    }

    public int getPlayerNum() {
        return playerNum;
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public int getStone() {
        return stone;
    }

    public void setStone(int stone) {
        this.stone = stone;
    }

    public int getWood() {
        return wood;
    }

    public void setWood(int wood) {
        this.wood = wood;
    }
}