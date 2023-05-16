package gui;

import api.spotify.UserAuthentication;
import api.spotify.albumController;
import api.spotify.userController;
import database.*;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.control.ScrollBar;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;

public class medleyApp extends Application {
    Connection databaseConnection;
    ArrayList<AlbumCard> albumCards;
    ArrayList<AlbumCard> searchCards = new ArrayList<>();
    VBox albumList = new VBox();
    VBox searchList = new VBox();

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
    }

    @Override
    public void init() {

    }

    Group library = new Group();
    Group search = new Group();
    Scene libraryScene = new Scene(library);
    Scene searchScene = new Scene(search);

    @Override
    public void start(Stage stage) {
        stage.setScene(libraryScene);
        stage.setTitle("Medley");
        stage.setHeight(600);
        stage.setWidth(1000);
        stage.show();

        drawLibraryScene(stage);
        drawSearchScene(stage);
    }

    public void drawLibraryScene(Stage stage) {
        Label medleyLabel = new Label("Medley");
        Label welcomeLabel = new Label("Welcome " + username + "!");
        Label libraryLabel = new Label("Your Library:");
        HBox heading = new HBox();
        heading.getChildren().addAll(medleyLabel, welcomeLabel);
        ScrollBar scrollbar = new ScrollBar();
        scrollbar.setMin(0);
        scrollbar.setMax(800);
        scrollbar.setValue(0);
        scrollbar.setOrientation(Orientation.VERTICAL);
        Button switchSceneSearch = new Button("Go to Search");
        Button switchSceneLibrary = new Button("Go to Library");
        switchSceneSearch.setOnAction(e -> stage.setScene(searchScene));
        switchSceneLibrary.setOnAction(e -> stage.setScene(libraryScene));
        albumList.setPadding(new Insets(10));
        albumList.setSpacing(8);
        albumCards = libraryController.getUserAlbums(databaseConnection, userID);
        for(HBox hBox : setAlbumCardList(albumCards, false)) albumList.getChildren().add(hBox);

        heading.setSpacing(700);
        heading.setLayoutX(50);
        heading.setLayoutY(0);
        libraryLabel.setLayoutX(100);
        libraryLabel.setLayoutY(25);
        albumList.setLayoutX(100);
        albumList.setLayoutY(50);
        scrollbar.setLayoutX(975);
        scrollbar.setLayoutY(0);
        switchSceneSearch.setLayoutX(0);
        switchSceneSearch.setLayoutY(50);
        switchSceneLibrary.setLayoutX(0);
        switchSceneLibrary.setLayoutY(100);

        library.getChildren().addAll(heading, libraryLabel, albumList, scrollbar, switchSceneSearch, switchSceneLibrary);
    }

    public void drawSearchScene(Stage stage) {
        Label medleyLabel = new Label("Medley");
        Label welcomeLabel = new Label("Welcome " + username + "!");
        HBox heading = new HBox();
        heading.getChildren().addAll(medleyLabel, welcomeLabel);
        ScrollBar scrollbar = new ScrollBar();
        scrollbar.setMin(0);
        scrollbar.setMax(800);
        scrollbar.setValue(0);
        scrollbar.setOrientation(Orientation.VERTICAL);
        Button switchSceneSearch = new Button("Go to Search");
        Button switchSceneLibrary = new Button("Go to Library");
        TextField searchField = new TextField();
        Button searchButton = new Button("Search");
        switchSceneLibrary.setOnAction(e -> {
            albumList.getChildren().clear();
            albumCards = libraryController.getUserAlbums(databaseConnection, userID);
            for(HBox hBox : setAlbumCardList(albumCards, false)) albumList.getChildren().add(hBox);
            stage.setScene(libraryScene);
        });
        searchList.setPadding(new Insets(10));
        searchList.setSpacing(8);
        searchButton.setOnAction(e -> {
            try {
                String query = searchField.getText();
                searchCards = albumController.searchResults(query, token, 5);
                System.out.println(searchCards);
                searchList.getChildren().clear();
                for(HBox hBox : setAlbumCardList(searchCards, true)) searchList.getChildren().add(hBox);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        });

        heading.setSpacing(700);
        heading.setLayoutX(50);
        heading.setLayoutY(0);
        scrollbar.setLayoutX(975);
        scrollbar.setLayoutY(0);
        switchSceneSearch.setLayoutX(0);
        switchSceneSearch.setLayoutY(50);
        switchSceneLibrary.setLayoutX(0);
        switchSceneLibrary.setLayoutY(100);
        searchField.setLayoutX(100);
        searchField.setLayoutY(25);
        searchButton.setLayoutX(200);
        searchButton.setLayoutY(25);
        searchList.setLayoutX(100);
        searchList.setLayoutY(50);

        search.getChildren().addAll(heading, scrollbar, switchSceneSearch, switchSceneLibrary,
                searchField, searchButton, searchList);
    }

    public ArrayList<HBox> setAlbumCardList(ArrayList<AlbumCard> cards, boolean showButton) {
        ArrayList<HBox> list = new ArrayList<>();
        for(AlbumCard card : cards) {
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
            if(showButton) {
                addButton.setOnAction(e -> {
                    Response res = libraryController.addAlbumToUserLibrary(databaseConnection, userID, card);
                    if (res.status == Status.ERROR) System.out.println(res.message);
                });
            }

            albumInfo.getChildren().addAll(imageView, albumName, artistName);
            if(showButton) albumInfo.getChildren().add(addButton);
            list.add(albumInfo);
        }

        return list;
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

}
