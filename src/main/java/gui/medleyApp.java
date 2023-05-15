package gui;

import api.spotify.UserAuthentication;
import api.spotify.albumController;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;

public class medleyApp extends Application {
    VBox albumList = new VBox();
    ArrayList<AlbumCard> albumCards = new ArrayList<>();

    static String clientID = "469af18e875a4fa1a58390d147ed924e";
    static String clientSecret = "b142d702a4674c37b84c5928f482a7e5";
    String redirectURI = "http://localhost:8080";

    String token;

    public medleyApp() throws IOException {
        UserAuthentication userAuth = new UserAuthentication(clientID, clientSecret, redirectURI);
        token = userAuth.getUserAuthToken();
        albumCards = albumController.getUserAlbums(token);
    }

    @Override
    public void init() {

    }

    @Override
    public void start(Stage stage) {
        BorderPane borderPane = new BorderPane();

        Label topLabel = new Label("Welcome to Medley!");
        borderPane.setTop(topLabel);

        albumList.setPadding(new Insets(10));
        albumList.setSpacing(8);

        for(AlbumCard card : albumCards) {
            HBox albumInfo = new HBox();
            albumInfo.setPadding(new Insets(15, 12, 15, 12));
            albumInfo.setSpacing(10);

            ImageView imageView = new ImageView();
            try {
                String albumID = card.getAlbumID();
                String imageUrl = albumController.retrieveImageUrl(albumID, token);
                imageView.setImage(ImageTools.retrieveImage(imageUrl));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            imageView.setFitHeight(100);
            imageView.setFitWidth(100);
            albumInfo.getChildren().add(imageView);

            Label albumName = new Label(card.getAlbumName());
            albumInfo.getChildren().add(albumName);

            Label artistName = new Label(card.getArtist());
            albumInfo.getChildren().add(artistName);

            albumList.getChildren().add(albumInfo);
        }
        borderPane.setCenter(albumList);

        Scene scene = new Scene(borderPane);
        stage.setScene(scene);
        stage.setTitle("Medley");
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

}
