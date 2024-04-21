package handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import utils.LoadSave;
import objects.Map;

public class MapHandler {

    private ArrayList<Map> maps = new ArrayList<>();

    public MapHandler() {
        loadMaps();
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

    public ArrayList<Map> getMaps() {
        return maps;
    }
}
