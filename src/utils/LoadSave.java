package utils;

import gamestates.Debug;
import gamestates.Play;
import main.Game;
import objects.Entity;
import objects.Map;
import ui.MiniMap;
import ui.bars.ActionBar;
import ui.bars.GameStatBar;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.EnumMap;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class LoadSave {

    public static String homePath = System.getProperty("user.home");
    public static String parentFolder = "Kings and Castles";
    public static String parentFolderPath = homePath + File.separator + parentFolder;

    public static String mapFolder = "maps";
    public static String mapFileExtension = ".kacmap";
    public static String mapPath = parentFolderPath + File.separator + mapFolder;
    public static String previewImageSuffix = "_preview.png";

    public static String gameFolder = "saves";
    public static String gameFileExtension = ".kacsave";
    public static String gamePath = parentFolderPath + File.separator + gameFolder;

    private static String configFileName = "debug.config";
    private static String configFileFullPath = parentFolderPath + File.separator + configFileName;

    private static String fontName = "SilverModified.ttf";
    public static Font silverModified;

    public static void createFolders() {
        String[] folders = {mapPath, gamePath};
        for (String folderPath : folders) {
            File folder = new File(folderPath);
            if (!folder.exists())
                folder.mkdir();
        }
    }

    public static void loadFont() {
        try (InputStream is = LoadSave.class.getClassLoader().getResourceAsStream(fontName)) {
            silverModified = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(silverModified);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
    }

    public static BufferedImage loadImage(String fileName) {
        try (InputStream is = LoadSave.class.getClassLoader().getResourceAsStream(fileName)) {
            BufferedImage img = ImageIO.read(is);
            BufferedImage convertedImg = new BufferedImage(img.getWidth(), img.getHeight(), TYPE_INT_ARGB);
            convertedImg.getGraphics().drawImage(img, 0, 0, null);
            return convertedImg;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Map loadMap(File mapFile) {
        try (FileInputStream fis = new FileInputStream(mapFile);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (Map) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void saveMap(Map map) {
        File mapFile = new File(mapPath + File.separator + map.getName() + mapFileExtension);
        if (mapFile.exists())
            System.out.println("Saving Map...");
        else {
            System.out.println("Creating new map file");
            createNewFile(mapFile);
        }
        writeMapToFile(map, mapFile);
        ImageLoader.createMapPreviewImage(map);
    }

    private static void writeMapToFile(Map map, File mapFile) {
        try (FileOutputStream fileStream = new FileOutputStream(mapFile);
             ObjectOutputStream objectStream = new ObjectOutputStream(fileStream)) {
            objectStream.writeObject(map);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createNewFile(File file) {
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteMapFile(Map map) {
        deleteFile(new File(mapPath + File.separator + map.getName() + mapFileExtension), map.getName() + mapFileExtension);
        deleteFile(new File(mapPath + File.separator + map.getName() + previewImageSuffix), map.getName() + previewImageSuffix);
    }

    public static void clearMaps() {
        clearFolder(new File(mapPath), "map");
    }

    public static Play loadGame(File gameFile) {
        try (FileInputStream fis = new FileInputStream(gameFile);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (Play) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void saveGame(Play game) {
        File gameFile = new File(gamePath + File.separator + game.getName() + gameFileExtension);
        if (gameFile.exists())
            System.out.println("Saving game...");
        else {
            System.out.println("Creating new game file");
            createNewFile(gameFile);
        }
        writeGameToFile(game, gameFile);
    }

    private static void writeGameToFile(Play play, File gameFile) {
        Game game = play.getGame();
        ActionBar actionBar = play.getActionBar();
        GameStatBar gameStatBar = play.getGameStatBar();
        MiniMap miniMap = play.getMiniMap();
        int selectedBuildingType = play.getSelectedBuildingType();
        Entity selectedEntity = play.getSelectedEntity();

        play.setGame(null);
        play.setSelectedEntity(null);
        play.setActionBar(null);
        play.setGameStatBar(null);
        play.setMiniMap(null);
        play.setSelectedBuildingType(-1);

        play.setClickAction(-1);
        play.setBuildingSelection(null);

        try (FileOutputStream fileStream = new FileOutputStream(gameFile);
             ObjectOutputStream objectStream = new ObjectOutputStream(fileStream)) {
            objectStream.writeObject(play);
        } catch (IOException e) {
            e.printStackTrace();
        }

        play.setGame(game);
        play.setActionBar(actionBar);
        play.setSelectedEntity(selectedEntity);
        play.setGameStatBar(gameStatBar);
        play.setMiniMap(miniMap);
        play.setSelectedBuildingType(selectedBuildingType);
    }

    public static void deleteGameFile(Play game) {
        deleteFile(new File(gamePath + File.separator + game.getName() + gameFileExtension), game.getName() + gameFileExtension);
    }

    public static void clearGames() {
        clearFolder(new File(gamePath), "game");
    }

    public static EnumMap<Debug.DebugToggle, Boolean> loadDebugConfig() {
        EnumMap<Debug.DebugToggle, Boolean> config = new EnumMap<>(Debug.DebugToggle.class);
        try (BufferedReader reader = new BufferedReader(new FileReader(configFileFullPath))) {
            String line;
            while ((line = reader.readLine()) != null)
                for (Debug.DebugToggle toggle : Debug.DebugToggle.values())
                    if (line.contains("\"" + toggle.getLabel() + "\":")) {
                        boolean value = line.contains("true");
                        config.put(toggle, value);
                    }
            System.out.println("Successfully loaded debug configuration from file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    public static void saveDebugConfig(EnumMap<Debug.DebugToggle, Boolean> config) {
        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        int count = 0;
        for (Debug.DebugToggle toggle : Debug.DebugToggle.values()) {
            sb.append("  \"").append(toggle.getLabel()).append("\": ")
                    .append(config.get(toggle));
            if (++count < Debug.DebugToggle.values().length)
                sb.append(",\n");
            else
                sb.append("\n");
        }

        sb.append("}");
        try (FileWriter file = new FileWriter(configFileFullPath)) {
            file.write(sb.toString());
            System.out.println("Successfully saved debug configuration to file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void deleteFile(File file, String fileName) {
        if (file.exists() && file.delete()) {
            System.out.println(fileName + " deleted successfully.");
        } else {
            System.out.println("Failed to delete " + fileName);
        }
    }

    private static void clearFolder(File folder, String folderType) {
        boolean success = true;
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null)
                for (File file : files)
                    if (file.isFile() && !file.delete()) {
                        success = false;
                        System.out.println("Failed to delete " + file.getName());
                    }
        } else
            System.out.println("Could not locate " + folderType + " folder.");

        if (success)
            System.out.println("Successfully cleared " + folderType + " folder.");
    }
}
