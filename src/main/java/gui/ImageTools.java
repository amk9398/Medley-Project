package gui;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class ImageTools {
    public static Image retrieveImage(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        BufferedImage bufferedImage = ImageIO.read(url);
        Image image = SwingFXUtils.toFXImage(bufferedImage, null);
        return image;
    }
}
