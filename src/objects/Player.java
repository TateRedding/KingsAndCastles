package objects;

import entities.buildings.Building;
import gamestates.Play;

import java.io.Serializable;

import static entities.buildings.Building.VILLAGE;

public class Player implements Serializable {

    private Play play;
    private long playerID;

    private int coal = 100;
    private int food = 10;
    private int gold = 350;
    private int iron = 100;
    private int stone = 150;
    private int logs = 100;

    private int population, maxPopulation;

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

        if (buildingType == VILLAGE)
            maxPopulation += 4;
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

    public int getMaxPopulation() {
        return maxPopulation;
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