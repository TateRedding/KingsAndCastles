package handlers;

import gamestates.Play;
import objects.Map;
import utils.LoadSave;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class SaveFileHandler {

    private ArrayList<Map> maps = new ArrayList<>();
    private ArrayList<Play> games = new ArrayList<>();

    public SaveFileHandler() {
        loadMaps();
        loadGames();
    }

    public void loadMaps() {
        maps.clear();

        File mapFolder = new File(LoadSave.mapPath);
        File[] files = mapFolder.listFiles();
        if (files != null) {
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File file1, File file2) {
                    return Long.compare(file2.lastModified(), file1.lastModified());
                }
            });
            for (File file : files)
                if (file.getPath().endsWith(LoadSave.mapFileExtension))
                    maps.add(LoadSave.loadMap(file));
        }
    }

    public void loadGames() {
        games.clear();

        File gameFolder = new File(LoadSave.gamePath);
        File[] files = gameFolder.listFiles();
        if (files != null) {
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File file1, File file2) {
                    return Long.compare(file2.lastModified(), file1.lastModified());
                }
            });
            for (File file : files)
                if (file.getPath().endsWith(LoadSave.gameFileExtension))
                    games.add(LoadSave.loadGame(file));
        }
    }

    public void saveMap(Map map) {
        LoadSave.saveMap(map);
        for (int i = 0; i < maps.size(); i++) {
            if (maps.get(i).getName().equals(map.getName())) {
                maps.remove(i);
                break;
            }
        }
        maps.add(0, map);
    }

    public void saveGame(Play play) {
        LoadSave.saveGame(play);
        for (int i = 0; i < games.size(); i++) {
            if (games.get(i).getName().equals(play.getName())) {
                games.remove(i);
                break;
            }
        }
        games.add(0, play);
    }

    public ArrayList<Map> getMaps() {
        return maps;
    }

    public ArrayList<Play> getGames() {
        return games;
    }
}
