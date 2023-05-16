package gui;

import api.spotify.UserAuthentication;
import api.spotify.albumController;
import api.spotify.userController;
import database.*;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.application.Application;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.io.*;
import java.sql.Connection;
import java.util.ArrayList;

public class medleyApp extends Application {
    Connection databaseConnection;
    ArrayList<AlbumCard> albumCards;
    VBox albumList = new VBox();

    static String clientID = "469af18e875a4fa1a58390d147ed924e";
    static String clientSecret = "b142d702a4674c37b84c5928f482a7e5";
    String redirectURI = "http://localhost:8080";

    String token;
    String username;
    int userID;


    public medleyApp() throws IOException {
        Database database = new Database();
        databaseConnection = database.getDatabaseConnection();

        UserAuthentication userAuth = new UserAuthentication(clientID, clientSecret, redirectURI);
        token = userAuth.getUserAuthToken();

        username = userController.getUserInfo(token, "username");
        Response response = loginController.attemptUserLogin(databaseConnection, username);
        if(response.status == Status.SUCCESS) {
            userID = Integer.parseInt(response.message);
        }

        albumCards = albumController.getUserAlbums(token);
    }

    @Override
    public void init() {

    }

    @Override
    public void start(Stage stage) {
        BorderPane borderPane = new BorderPane();

        // heading
        Label topLabel = new Label("Welcome " + username + "!");
        Button switchSearchScene = new Button("Go to Search");
        VBox vb = new VBox();
        vb.getChildren().addAll(topLabel, switchSearchScene);
        vb.setSpacing(30);
        borderPane.setTop(vb);

        Group root = new Group();
        Scene searchScene = new Scene(root);

        // search bar
        TextField textField = new TextField();
        Button searchButton = new Button("Search");
        searchButton.setOnAction(actionEvent -> {
            String query = textField.getText();
            try {
                albumCards = albumController.searchResults(query, token, 3);
                albumList.getChildren().clear();
                setAlbumList();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Button switchAlbumsScene = new Button("Go to album list");
        HBox hb = new HBox();
        hb.getChildren().addAll(textField, searchButton, switchAlbumsScene);
        hb.setSpacing(10);

        // album list
        setAlbumList();
        borderPane.setCenter(albumList);

        Scene scene = new Scene(borderPane);
        stage.setScene(scene);

        VBox v2 = new VBox();
        v2.getChildren().addAll(hb, albumList);
        root.getChildren().add(v2);

        switchSearchScene.setOnAction(e -> {
            stage.setScene(searchScene);
            albumList.getChildren().clear();
        });
        switchAlbumsScene.setOnAction(e -> {
            stage.setScene(scene);
            textField.clear();
        });

        stage.setTitle("Medley");
        stage.setHeight(500);
        stage.setWidth(800);
        stage.show();
    }

    public void setAlbumList() {
        albumList.setPadding(new Insets(10));
        albumList.setSpacing(8);
        for(AlbumCard card : albumCards) {
            HBox albumInfo = new HBox();
            albumInfo.setPadding(new Insets(15, 12, 15, 12));
            albumInfo.setSpacing(10);

            ImageView imageView = new ImageView();
            try {
                imageView.setImage(ImageTools.retrieveImage(card.getImageURL()));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            imageView.setFitHeight(100);
            imageView.setFitWidth(100);

            Label albumName = new Label(card.getAlbumName());
            Label artistName = new Label(card.getArtist());

            Button addButton = new Button("Add to Library");
            addButton.setOnAction(e -> {
                Response res = libraryController.addAlbumToUserLibrary(databaseConnection, userID, card);
                if(res.status == Status.ERROR) System.out.println(res.message);
            });

            albumInfo.getChildren().addAll(imageView, albumName, artistName, addButton);
            albumList.getChildren().add(albumInfo);
        }
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

}
