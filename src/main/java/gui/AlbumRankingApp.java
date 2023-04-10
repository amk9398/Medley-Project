package gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class AlbumRankingApp  {
    // extends Application

    /*
    Button albumButton1 = new Button();
    Button albumButton2 = new Button();
    ArrayList<String> albums;

    public AlbumRankingApp() throws IOException {
        this.albums = parseAlbumInfo();
        this.albumButton1.setGraphic(getAlbumImageView(this.albums.get(13)));
        this.albumButton2.setGraphic(getAlbumImageView(this.albums.get(15)));
    }

    @Override
    public void start(Stage stage) throws Exception {
        HBox mainPane = new HBox();
        mainPane.getChildren().add(this.albumButton1);
        mainPane.getChildren().add(this.albumButton2);

        Scene scene = new Scene(mainPane);
        stage.setScene(scene);
        stage.show();
    }

    public ArrayList<String> parseAlbumInfo() throws IOException {
        BufferedReader in = new BufferedReader(new FileReader("src/main/java/data/albums_info_2"));
        ArrayList<String> albums = new ArrayList<>();
        String line = in.readLine();
        while (line != null) {
            albums.add(line.split(",")[0] + line.split(",")[1]);
            line = in.readLine();
        }
        return albums;
    }


    public ImageView getAlbumImageView(String albumName) throws IOException {
        String auth_token = T.getToken();
        ImageView imageView1 = new ImageView();
        imageView1.setImage(T.retrieveImage(T.retrieveImageUrl(T.retrieveAlbumID(albumName, auth_token), auth_token)));
        imageView1.setFitWidth(200);
        imageView1.setFitHeight(200);
        return imageView1;
    }

     */


}
