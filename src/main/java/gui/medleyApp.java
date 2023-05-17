package gui;

import api.spotify.UserAuthentication;
import api.spotify.albumController;
import api.spotify.userController;
import database.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.*;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

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
    HashMap<String, Image> imageHashMap = new HashMap<>();

    public medleyApp() throws IOException {
        Database database = new Database();
        databaseConnection = database.getDatabaseConnection();

        UserAuthentication userAuth = new UserAuthentication(clientID, clientSecret, redirectURI);
        token = userAuth.getUserAuthToken();

        username = userController.getUserInfo(token, "username");
        Response response = loginController.attemptUserLogin(databaseConnection, username);
        if(response.status == Status.SUCCESS) {
            userID = Integer.parseInt(response.message);
        } else System.out.println(response.message);
    }

    @Override
    public void init() {}

    Stage stage;
    Group library = new Group();
    Group search = new Group();
    ScrollPane sc1 = new ScrollPane(library);
    ScrollPane sc2 = new ScrollPane(search);
    Scene libraryScene = new Scene(sc1);
    Scene searchScene = new Scene(sc2);
    String sortMethod = "name";

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
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
        switchSceneSearch.setOnAction(e -> {
            stage.setScene(searchScene);
            sc2.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
            refreshStage();
        });
        albumList.setPadding(new Insets(10));
        albumList.setSpacing(8);
        albumCards = libraryController.getUserAlbums(databaseConnection, userID, sortMethod);
        for(HBox hBox : setAlbumCardList(albumCards, false)) albumList.getChildren().add(hBox);
        ObservableList<String> sortOptions = FXCollections.observableArrayList("name", "artist", "rating");
        ComboBox sortDropdown = new ComboBox(sortOptions);
        sortDropdown.setValue(sortMethod);
        sortDropdown.setOnAction(e -> {
            sortMethod = String.valueOf(sortDropdown.getValue());
            albumCards = libraryController.getUserAlbums(databaseConnection, userID, sortMethod);
            albumList.getChildren().clear();
            for(HBox hBox : setAlbumCardList(albumCards, false)) albumList.getChildren().add(hBox);
            refreshStage();
        });


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
        sortDropdown.setLayoutX(250);
        sortDropdown.setLayoutY(25);

        library.getChildren().addAll(heading, libraryLabel, albumList, switchSceneSearch, switchSceneLibrary, sortDropdown);
    }

    public void drawSearchScene(Stage stage) {
        Label medleyLabel = new Label("Medley");
        Label welcomeLabel = new Label("Welcome " + username + "!");
        HBox heading = new HBox();
        heading.getChildren().addAll(medleyLabel, welcomeLabel);
        Button switchSceneSearch = new Button("Go to Search");
        Button switchSceneLibrary = new Button("Go to Library");
        TextField searchField = new TextField();
        Button searchButton = new Button("Search");
        switchSceneLibrary.setOnAction(e -> {
            albumList.getChildren().clear();
            albumCards = libraryController.getUserAlbums(databaseConnection, userID, sortMethod);
            for(HBox hBox : setAlbumCardList(albumCards, false)) albumList.getChildren().add(hBox);
            stage.setScene(libraryScene);
            refreshStage();
        });
        searchList.setPadding(new Insets(10));
        searchList.setSpacing(8);
        searchButton.setOnAction(e -> {
            try {
                String query = searchField.getText();
                searchCards = albumController.searchResults(query, token, 8);
                searchList.getChildren().clear();
                for(HBox hBox : setAlbumCardList(searchCards, true)) searchList.getChildren().add(hBox);
                refreshStage();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        });

        heading.setSpacing(700);
        heading.setLayoutX(50);
        heading.setLayoutY(0);
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

        search.getChildren().addAll(heading, switchSceneSearch, switchSceneLibrary,
                searchField, searchButton, searchList);
    }

    public void refreshStage() {stage.setHeight(stage.getHeight() == 600 ? 600.1 : 600);}

    public ArrayList<HBox> setAlbumCardList(ArrayList<AlbumCard> cards, boolean isSearch) {
        ArrayList<HBox> list = new ArrayList<>();
        for(AlbumCard card : cards) {
            HBox albumInfo = new HBox();
            albumInfo.setPadding(new Insets(15, 12, 15, 12));
            albumInfo.setSpacing(10);

            ImageView imageView = new ImageView();
            try {
                Image image;
                if(imageHashMap.containsKey(card.getImageURL())) {
                    image = imageHashMap.get(card.getImageURL());
                } else {
                    image = ImageTools.retrieveImage(card.getImageURL());
                    imageHashMap.put(card.getImageURL(), image);
                }
                imageView.setImage(image);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            imageView.setFitHeight(100);
            imageView.setFitWidth(100);

            Label albumName = new Label(card.getAlbumName());
            Label artistName = new Label(card.getArtist());

            Button addButton = new Button("Add to Library");
            if(isSearch) {
                addButton.setOnAction(e -> {
                    Response res = libraryController.addAlbumToUserLibrary(databaseConnection, userID, card);
                    if (res.status == Status.ERROR) System.out.println(res.message);
                });
            }

            ObservableList<String> ratingOptions = FXCollections.observableArrayList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
            ComboBox ratingDropDown = new ComboBox(ratingOptions);
            ratingDropDown.setValue(card.getRating() == 0  ? " " : (int) card.getRating());
            ratingDropDown.setOnAction(e -> {
                int rating = Integer.parseInt(String.valueOf(ratingDropDown.getValue()));
                Response res = libraryController.rateAlbum(databaseConnection, userID, card.getAlbumID(), rating);
                if(res.status == Status.ERROR) System.out.println(res.message);
            });

            Button removeAlbum = new Button("Remove from library");
            removeAlbum.setOnAction(e -> {
                libraryController.removeAlbumFromUserLibrary(databaseConnection, userID, card.getAlbumID());
                albumCards = libraryController.getUserAlbums(databaseConnection, userID, sortMethod);
                albumList.getChildren().clear();
                for(HBox hBox : setAlbumCardList(albumCards, false)) albumList.getChildren().add(hBox);
            });

            albumInfo.getChildren().addAll(imageView, albumName, artistName);
            if(isSearch) albumInfo.getChildren().add(addButton);
            if(!isSearch) {
                albumInfo.getChildren().add(ratingDropDown);
                albumInfo.getChildren().add(removeAlbum);
            }
            list.add(albumInfo);
        }

        return list;
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

}
