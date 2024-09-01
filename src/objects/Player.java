package objects;

import entities.buildings.Building;
import gamestates.Play;

import java.io.Serializable;

public class Player implements Serializable {

    private Play play;
    private long playerID;

    private int coal = 1000;
    private int food = 100;
    private int gold = 10000;
    private int iron = 1000;
    private int population = 2;
    private int stone = 250;
    private int logs = 500;

    private boolean isHuman;

    public Player(Play play, long playerID, boolean isHuman) {
        this.play = play;
        this.playerID = playerID;
        this.isHuman = isHuman;
    }

    public void buildBuilding(int buildingType) {
        coal -= Building.getCostCoal(buildingType);
        gold -= Building.getCostGold(buildingType);
        iron -= Building.getCostIron(buildingType);
        stone -= Building.getCostStone(buildingType);
        logs -= Building.getCostLogs(buildingType);
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

    public int getLogs() {
        return logs;
    }

    public void setLogs(int logs) {
        this.logs = logs;
    }
}