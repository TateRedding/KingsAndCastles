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

import objects.Map;

public class LoadSave {

    public static String homePath = System.getProperty("user.home");
    public static String gameFolder = "Kings and Castles";
    public static String mapFolder = "maps";
    public static String mapFileExtension = ".kacmap";
    public static String mapPath = homePath + File.separator + gameFolder + File.separator + mapFolder;
    public static String previewImageSuffix = "_preview.png";
    public static String saveFolder = "saves";
    public static String saveFileExtension = ".kacsave";
    public static String savePath = homePath + File.separator + gameFolder + File.separator + saveFolder;

    private static String fontName = "SilverModified.ttf";
    public static Font silverModified;

    public static void createFolders() {
        File folder = new File(homePath + File.separator + gameFolder);
        if (!folder.exists())
            folder.mkdir();
        folder = new File(mapPath);
        if (!folder.exists())
            folder.mkdir();
        folder = new File(savePath);
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
            return;
        } else {
            try {
                mapFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            writeMapToFile(map, mapFile);
        }
    }

}
