package objects;

import entities.buildings.Building;
import gamestates.Play;

import java.io.Serializable;

import static entities.buildings.Building.VILLAGE;

public class Player implements Serializable {

    private Play play;
    private long playerID;

    private int coal = 0;
    private int food = 25;
    private int gold = 1000;
    private int iron = 0;
    private int logs = 0;
    private int stone = 0;

    private int population, maxPopulation;

    private boolean isHuman;

    public Player(Play play, long playerID, boolean isHuman) {
        this.play = play;
        this.playerID = playerID;
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

    public int getLogs() {
        return logs;
    }

    public void setLogs(int logs) {
        this.logs = logs;
    }

    public int getMaxPopulation() {
        return maxPopulation;
    }

    public void setMaxPopulation(int maxPopulation) {
        this.maxPopulation = maxPopulation;
    }

    public Play getPlay() {
        return play;
    }

    public long getPlayerID() {
        return playerID;
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
}