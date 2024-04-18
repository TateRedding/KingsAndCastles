package src.handlers;

import java.io.File;
import java.util.ArrayList;

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

		if (files != null)
			for (File file : files)
				if (file.getPath().endsWith(LoadSave.mapFileExtension))
					maps.add(LoadSave.loadMap(file));

	}

	public ArrayList<Map> getMaps() {
		return maps;
	}

}
