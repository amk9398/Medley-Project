package gui.ui;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class AppTheme {
    public String darkColor;
    public String darkColorHighlight;
    public String mediumColor;
    public String lightColor;
    public String extraLightColor;
    public String extraLightColorHighlight;
    public String contrastColor;
    public String contrastColorHighlight;

    /** loads and sets gui class colors from the appropriate file */
    public AppTheme(String windowTheme) {
        String filename = switch (windowTheme) {
            case "Classic" -> "src/main/java/data/classic_theme.txt";
            case "Embers" -> "src/main/java/data/embers_theme.txt";
            case "Petal" -> "src/main/java/data/petal_theme.txt";
            default -> "src/main/java/data/sea_wave_theme.txt";
        };

        try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
            darkColor = br.readLine();
            darkColorHighlight = br.readLine();
            mediumColor = br.readLine();
            lightColor = br.readLine();
            extraLightColor = br.readLine();
            extraLightColorHighlight = br.readLine();
            contrastColor = br.readLine();
            contrastColorHighlight = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
