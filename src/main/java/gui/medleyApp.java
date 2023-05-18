package gui;

import api.spotify.UserAuthentication;
import api.spotify.albumController;
import api.spotify.userController;
import database.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
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
import java.util.ArrayList;
import java.util.HashMap;

public class medleyApp extends Application {
    Connection databaseConnection;
    ArrayList<AlbumCard> albumCards;
    ArrayList<AlbumCard> searchCards = new ArrayList<>();
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
    Group search = new Group();
    ScrollPane sc2 = new ScrollPane(search);
    Scene searchScene = new Scene(sc2);
    String sortMethod = "name";
    ArrayList<Scene> libraryScenes = new ArrayList<>();
    ArrayList<VBox> albumLists = new ArrayList<>();
    int numLibraryScenes = 0;
    final int ALBUMS_PER_SCENE = 6;
    int currentAlbumScene = 0;


    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        refreshLibrary();
        stage.setTitle("Medley");
        stage.setHeight(600);
        stage.setWidth(1000);
        Image icon = new Image("C:\\Users\\aaron\\eclipse-workspace\\MedleyBeta\\src\\main\\java\\data\\img.png");
        stage.getIcons().add(icon);
        stage.show();

        drawSearchScene(stage);
    }


    public void refreshLibrary() {
        libraryScenes.clear();
        albumLists.clear();
        albumCards = libraryController.getUserAlbums(databaseConnection, userID, sortMethod);
        numLibraryScenes = (albumCards.size() + ALBUMS_PER_SCENE - 1) / ALBUMS_PER_SCENE;
        int i = 0;
        do {
            libraryScenes.add(drawLibraryScene(stage, i));
        } while(++i < numLibraryScenes);

        stage.setScene(libraryScenes.get(0));
        refreshStage();
    }


    public Scene drawLibraryScene(Stage stage, int num) {
        // headings and labels
        Label medleyLabel = new Label("Medley");
        Label welcomeLabel = new Label("Welcome " + username + "!");
        Label libraryLabel = new Label("Your Library:");
        HBox heading = new HBox();
        Label sortLabel = new Label("Sort by ");
        Label pageLabel = new Label("Page ");
        heading.getChildren().addAll(medleyLabel, welcomeLabel);
        Button switchSceneSearch = new Button("Go to Search");
        Button switchSceneLibrary = new Button("Go to Library");

        // switch to search action
        switchSceneSearch.setOnAction(e -> {
            stage.setScene(searchScene);
            sc2.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
            refreshStage();
        });

        // album list setup
        albumLists.add(createAlbumVBox(num));

        // sort options
        ObservableList<String> sortOptions = FXCollections.observableArrayList("name", "artist", "rating");
        ComboBox<String> sortDropdown = new ComboBox<>(sortOptions);
        sortDropdown.setValue(sortMethod);
        sortDropdown.setOnAction(e -> {
            sortMethod = String.valueOf(sortDropdown.getValue());
            refreshLibrary();
        });

        // add all library albums button & action
        Button addAllButton = new Button("Add Spotify Library");
        addAllButton.setOnAction(e -> {
            try {
                for(AlbumCard card: albumController.getUserAlbums(token)) {
                    libraryController.addAlbumToUserLibrary(databaseConnection, userID, card);
                }
                refreshLibrary();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        // change page dropdown

        ObservableList<String> pageOptions = FXCollections.observableArrayList();
        for(int i = 0; i < numLibraryScenes; i++) pageOptions.add(String.valueOf((i+1)));
        ComboBox<String> pageDropdown = new ComboBox<>(pageOptions);
        pageDropdown.setValue(String.valueOf(num + 1));
        pageDropdown.setOnAction(e -> {
            currentAlbumScene = Integer.parseInt(pageDropdown.getValue()) - 1;
            stage.setScene(libraryScenes.get(currentAlbumScene));
            refreshStage();
        });

        // positioning
        heading.setSpacing(700);
        heading.setLayoutX(50);
        heading.setLayoutY(0);
        libraryLabel.setLayoutX(100);
        libraryLabel.setLayoutY(25);
        albumLists.get(num).setLayoutX(100);
        albumLists.get(num).setLayoutY(50);
        switchSceneSearch.setLayoutX(0);
        switchSceneSearch.setLayoutY(50);
        switchSceneLibrary.setLayoutX(0);
        switchSceneLibrary.setLayoutY(100);
        sortDropdown.setLayoutX(500);
        sortDropdown.setLayoutY(25);
        sortLabel.setLayoutX(450);
        sortLabel.setLayoutY(25);
        addAllButton.setLayoutX(800);
        addAllButton.setLayoutY(50);
        pageDropdown.setLayoutX(300);
        pageDropdown.setLayoutY(25);
        pageLabel.setLayoutX(250);
        pageLabel.setLayoutY(25);

        Group library = new Group();
        ScrollPane sc = new ScrollPane(library);
        Scene libraryScene = new Scene(sc);
        library.getChildren().addAll(heading, libraryLabel, albumLists.get(num), switchSceneSearch,
                switchSceneLibrary, sortDropdown, sortLabel, addAllButton, pageDropdown, pageLabel);
        return libraryScene;
    }


    public void drawSearchScene(Stage stage) {
        // headings and labels
        Label medleyLabel = new Label("Medley");
        Label welcomeLabel = new Label("Welcome " + username + "!");
        HBox heading = new HBox();
        heading.getChildren().addAll(medleyLabel, welcomeLabel);
        Button switchSceneSearch = new Button("Go to Search");
        Button switchSceneLibrary = new Button("Go to Library");
        TextField searchField = new TextField();
        Button searchButton = new Button("Search");

        // switch to library scene action
        switchSceneLibrary.setOnAction(e -> refreshLibrary());

        // search button action
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

        // positioning
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
        searchList.setPadding(new Insets(10));
        searchList.setSpacing(8);

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
            ComboBox<String> ratingDropDown = new ComboBox<>(ratingOptions);
            ratingDropDown.setValue(card.getRating() == 0  ? " " : String.valueOf((int) card.getRating()));
            ratingDropDown.setOnAction(e -> {
                int rating = Integer.parseInt(String.valueOf(ratingDropDown.getValue()));
                Response res = libraryController.rateAlbum(databaseConnection, userID, card.getAlbumID(), rating);
                if(res.status == Status.ERROR) System.out.println(res.message);
            });

            Button removeAlbum = new Button("Remove from library");
            removeAlbum.setOnAction(e -> {
                libraryController.removeAlbumFromUserLibrary(databaseConnection, userID, card.getAlbumID());
                refreshLibrary();
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


    public VBox createAlbumVBox(int num) {
        VBox albumList = new VBox();
        albumList.setPadding(new Insets(10));
        albumList.setSpacing(8);
        albumCards = libraryController.getUserAlbums(databaseConnection, userID, sortMethod);
        int i = 0;
        for(HBox hBox : setAlbumCardList(albumCards, false)) {

            if(i >= num * ALBUMS_PER_SCENE) albumList.getChildren().add(hBox);
            if(i >= (num + 1) * ALBUMS_PER_SCENE - 1) break;
            i++;
        }
        return albumList;
    }


    public static void main(String[] args) {
        Application.launch(args);
    }

}
