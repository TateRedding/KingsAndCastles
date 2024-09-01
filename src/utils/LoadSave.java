package utils;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.imageio.ImageIO;

import gamestates.Play;
import main.Game;
import objects.Entity;
import objects.Map;
import ui.MiniMap;
import ui.bars.ActionBar;
import ui.bars.GameStatBar;

public class LoadSave {

    public static String homePath = System.getProperty("user.home");
    public static String mainFolder = "Kings and Castles";
    public static String mapFolder = "maps";
    public static String mapFileExtension = ".kacmap";
    public static String mapPath = homePath + File.separator + mainFolder + File.separator + mapFolder;
    public static String previewImageSuffix = "_preview.png";
    public static String gameFolder = "saves";
    public static String gameFileExtension = ".kacsave";
    public static String gamePath = homePath + File.separator + mainFolder + File.separator + gameFolder;

    private static String fontName = "SilverModified.ttf";
    public static Font silverModified;

    public static void createFolders() {
        File folder = new File(homePath + File.separator + mainFolder);
        if (!folder.exists())
            folder.mkdir();
        folder = new File(mapPath);
        if (!folder.exists())
            folder.mkdir();
        folder = new File(gamePath);
        if (!folder.exists())
            folder.mkdir();
    }

    public static void loadFont() {
        InputStream is = LoadSave.class.getClassLoader().getResourceAsStream(fontName);
        try {
            silverModified = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(silverModified);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static BufferedImage loadImage(String fileName) {
        BufferedImage img = null;
        InputStream is = LoadSave.class.getClassLoader().getResourceAsStream(fileName);
        try {
            img = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        BufferedImage convertedImg = new BufferedImage(img.getWidth(), img.getHeight(), TYPE_INT_ARGB);
        convertedImg.getGraphics().drawImage(img, 0, 0, null);

        return convertedImg;
    }

    public static Map loadMap(File mapFile) {
        Map map = null;
        try {
            FileInputStream fis = new FileInputStream(mapFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            map = (Map) ois.readObject();
            ois.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }

    public static void saveMap(Map map) {
        File mapFile = new File(mapPath + File.separator + map.getName() + mapFileExtension);
        if (mapFile.exists()) {
            System.out.println("Saving Map...");
            writeMapToFile(map, mapFile);
        } else {
            System.out.println("Creating new map file");
            createMapFile(map, mapFile);
        }
        ImageLoader.createMapPreviewImage(map);
    }

    private static void writeMapToFile(Map map, File mapFile) {
        try {
            FileOutputStream fileStream = new FileOutputStream(mapFile);
            ObjectOutputStream objectStream = new ObjectOutputStream(fileStream);
            objectStream.writeObject(map);
            objectStream.close();
            fileStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createMapFile(Map map, File mapFile) {
        if (mapFile.exists()) {
            System.out.println("File: " + mapFile + " already exists");
        } else {
            try {
                mapFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            writeMapToFile(map, mapFile);
        }
    }

    public static void deleteMapFile(Map map) {
        String mapFileName = map.getName() + mapFileExtension;
        String imageName = map.getName() + previewImageSuffix;
        File mapFile = new File(mapPath + File.separator + mapFileName);
        if (mapFile.exists()) {
            boolean deleted = mapFile.delete();
            if (deleted)
                System.out.println(mapFileName + " deleted successfully.");
            else
                System.out.println("Failed to delete " + mapFileName);
        } else
            System.out.println("Could not locate " + mapFileName);

        File imageFile = new File(mapPath + File.separator + imageName);
        if (imageFile.exists()) {
            boolean deleted = imageFile.delete();
            if (deleted)
                System.out.println(imageName + " deleted successfully.");
            else
                System.out.println("Failed to delete " + imageName);
        } else
            System.out.println("Could not locate " + imageName);
    }

    public static void clearMaps() {
        File mapFolder = new File(mapPath);
        boolean success = true;
        if (mapFolder.exists()) {
            File[] files = mapFolder.listFiles();
            if (files != null)
                for (File file : files)
                    if (file.isFile()) {
                        boolean deleted = file.delete();
                        if (!deleted) {
                            success = false;
                            System.out.println("Failed to delete " + file.getName());
                        }
                    }
        } else
            System.out.println("Could not locate map folder.");
        if (success)
            System.out.println("Successfully cleared map folder.");
    }

    public static Play loadGame(File gameFile) {
        Play game = null;
        try {
            FileInputStream fis = new FileInputStream(gameFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            game = (Play) ois.readObject();
            ois.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return game;
    }

    public static void saveGame(Play game) {
        File gameFile = new File(gamePath + File.separator + game.getName() + gameFileExtension);
        if (gameFile.exists()) {
            System.out.println("Saving game...");
            writeGameToFile(game, gameFile);
        } else {
            System.out.println("Creating new game file");
            createGameFile(game, gameFile);
        }
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

        try {
            FileOutputStream fileStream = new FileOutputStream(gameFile);
            ObjectOutputStream objectStream = new ObjectOutputStream(fileStream);
            objectStream.writeObject(play);
            objectStream.close();
            fileStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        play.setGame(game);
        play.setActionBar(actionBar);
        play.setSelectedEntity(selectedEntity);
        play.setGameStatBar(gameStatBar);
        play.setMiniMap(miniMap);
        play.setSelectedBuildingType(selectedBuildingType);
    }

    public static void createGameFile(Play game, File gameFile) {
        if (gameFile.exists()) {
            System.out.println("File: " + gameFile + " already exists");
        } else {
            try {
                gameFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            writeGameToFile(game, gameFile);
        }
    }

    public static void deleteGameFile(Play game) {
        String gameFileName = game.getName() + gameFileExtension;
        File gameFile = new File(gamePath + File.separator + gameFileName);
        if (gameFile.exists()) {
            boolean deleted = gameFile.delete();
            if (deleted)
                System.out.println(gameFileName + " deleted successfully.");
            else
                System.out.println("Failed to delete " + gameFileName);
        } else
            System.out.println("Could not locate " + gameFileName);

    }

    public static void clearGames() {
        File gameFolder = new File(gamePath);
        boolean success = true;
        if (gameFolder.exists()) {
            File[] files = gameFolder.listFiles();
            if (files != null)
                for (File file : files)
                    if (file.isFile()) {
                        boolean deleted = file.delete();
                        if (!deleted) {
                            success = false;
                            System.out.println("Failed to delete " + file.getName());
                        }
                    }
        } else
            System.out.println("Could not locate game folder.");
        if (success)
            System.out.println("Successfully cleared game folder.");
    }

}
