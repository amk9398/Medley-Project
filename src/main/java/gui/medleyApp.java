package gui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.application.Application;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

public class medleyApp extends Application {
    Image kidsSeeGhostsImage = new Image(new FileInputStream("src/main/java/data/album_art/Kids_See_Ghosts_Cover.png"));

    public medleyApp() throws FileNotFoundException {
    }

    @Override
    public void init() {

    }

    @Override
    public void start(Stage stage) {
        BorderPane borderPane = new BorderPane();
        HBox taskBar = new HBox();
        taskBar.getChildren().add(new Label("Welcome to Medley!"));
        borderPane.setTop(taskBar);
        GridPane albumView = new GridPane();
        for (int i=0; i<2; i++) {
            for (int j=0; j<2; j++) {
                ImageView imageView = new ImageView();
                imageView.setImage(this.kidsSeeGhostsImage);
                albumView.add(imageView, j, i);
            }
        }
        albumView.setHgap(10);
        albumView.setVgap(10);
        albumView.setAlignment(Pos.CENTER);
        borderPane.setCenter(albumView);
        Scene scene = new Scene(borderPane);
        stage.setScene(scene);
        stage.setTitle("Medley");
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

}
