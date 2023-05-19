package gui;

import api.spotify.UserAuthentication;
import api.spotify.albumController;
import api.spotify.userController;
import database.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.application.Application;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.*;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

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
    Scene searchScene;
    String sortMethod = "name";
    String sortOrder = "asc";
    ArrayList<Scene> libraryScenes = new ArrayList<>();
    ArrayList<VBox> albumLists = new ArrayList<>();
    int numLibraryScenes = 0;
    final int ALBUMS_PER_SCENE = 25;
    final int WIDTH = 1050;
    final int HEIGHT = 750;
    int currentAlbumScene = 0;


    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setWidth(WIDTH);
        stage.setHeight(HEIGHT);
        stage.setResizable(false);
        stage.setTitle("Medley");
        Image icon = new Image("C:\\Users\\aaron\\eclipse-workspace\\MedleyBeta\\src\\main\\java\\data\\img.png");
        stage.getIcons().add(icon);
        stage.show();

        refreshLibrary();
        searchScene = drawSearchScene();
    }

    public void refreshStage() {
        stage.setHeight((int) stage.getHeight() == HEIGHT ? HEIGHT + 1 : HEIGHT);
    }

    public void refreshLibrary() {refreshLibrary(0);}

    public void refreshLibrary(int num) {
        libraryScenes.clear();
        albumLists.clear();
        albumCards = libraryController.getUserAlbums(databaseConnection, userID, sortMethod, sortOrder);
        numLibraryScenes = (albumCards.size() + ALBUMS_PER_SCENE - 1) / ALBUMS_PER_SCENE;
        int i = 0;
        do {
            libraryScenes.add(drawLibraryScene(stage, i));
        } while(++i < numLibraryScenes);

        stage.setScene(libraryScenes.get(num));
        refreshStage();
    }


    public Scene drawLibraryScene(Stage stage, int num) {
        albumLists.add(createAlbumVBox(num));
        ObservableList<String> sortOptions = FXCollections.observableArrayList("name", "artist", "rating");
        ObservableList<String> pageOptions = FXCollections.observableArrayList();
        for(int i = 0; i < numLibraryScenes; i++) pageOptions.add(String.valueOf((i+1)));

        BorderPane root = new BorderPane();
        BorderPane heading = new BorderPane();
        HBox headingTabs = new HBox();
        BorderPane listPane = new BorderPane();
        ScrollPane scrollPane = new ScrollPane(listPane);
        HBox albumCardLabels = new HBox();
        VBox centerPane = new VBox(albumCardLabels, albumLists.get(num));
        Pane leftMargin = new Pane();
        Pane rightMargin = new Pane();
        Pane emptyPane = new Pane();
        Label medleyLabel = new Label("Medley");
        Button coverArtLabel = new Button("Cover Art");
        Button albumLabel = new Button("Name");
        Button artistLabel = new Button("Artist");
        Button ratingLabel = new Button("Rating");
        Button switchSceneSearch = new Button("Add New Album");
        Button switchSceneLibrary = new Button("Your Library");
        ComboBox<String> sortDropdown = new ComboBox<>(sortOptions);
        ComboBox<String> pageDropdown = new ComboBox<>(pageOptions);
        ImageView profileImage = new ImageView(new Image("C:\\Users\\aaron\\eclipse-workspace\\MedleyBeta\\src\\main\\java\\data\\profile_icon.png"));

        albumLists.get(num).setStyle("-fx-background-color: #018ABD");
        heading.setStyle("-fx-background-color: #004581");
        headingTabs.setSpacing(10);
        heading.setMaxWidth(WIDTH - 14);
        medleyLabel.setFont(new Font("Impact", 30));
        medleyLabel.setStyle("-fx-text-fill: #DDE8F0");
        albumCardLabels.setPrefSize(WIDTH - 230, 25);
        albumCardLabels.setStyle("-fx-background-color: #004581");
        emptyPane.setPrefWidth(60);
        coverArtLabel.setPrefWidth(125);
        coverArtLabel.setStyle("-fx-background-color: #004581; -fx-text-fill: #DDE8F0; -fx-font-weight: bold; -fx-font-size: 12pt; -fx-alignment: top_left");
        albumLabel.setPrefWidth(275);
        albumLabel.setStyle("-fx-background-color: #004581; -fx-text-fill: #DDE8F0; -fx-font-weight: bold; -fx-font-size: 12pt; -fx-alignment: top_left");
        artistLabel.setPrefWidth(150);
        artistLabel.setStyle("-fx-background-color: #004581; -fx-text-fill: #DDE8F0; -fx-font-weight: bold; -fx-font-size: 12pt; -fx-alignment: top_left");
        ratingLabel.setPrefWidth(150);
        ratingLabel.setStyle("-fx-background-color: #004581; -fx-text-fill: #DDE8F0; -fx-font-weight: bold; -fx-font-size: 12pt; -fx-alignment: top_left");
        leftMargin.setPrefSize(100, HEIGHT - 75);
        leftMargin.setStyle("-fx-background-color: #97CBDC");
        rightMargin.setStyle("-fx-background-color: #97CBDC");
        rightMargin.setPrefSize(100, HEIGHT - 75);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        switchSceneSearch.setStyle("-fx-background-color: #004581; -fx-text-fill: #DDE8F0");
        switchSceneLibrary.setStyle("-fx-background-color: #004581; -fx-text-fill: #DDE8F0");
        sortDropdown.setValue(sortMethod);
        pageDropdown.setValue(String.valueOf(num + 1));
        profileImage.setFitHeight(40);
        profileImage.setFitWidth(40);

        root.setTop(heading);
        root.setCenter(scrollPane);
        heading.setLeft(medleyLabel);
        heading.setRight(headingTabs);
        headingTabs.getChildren().addAll(switchSceneLibrary, switchSceneSearch, profileImage);
        albumCardLabels.getChildren().addAll(emptyPane, coverArtLabel, albumLabel, artistLabel, ratingLabel, pageDropdown);
        listPane.setCenter(centerPane);
        listPane.setLeft(leftMargin);
        listPane.setRight(rightMargin);

        switchSceneSearch.hoverProperty().addListener((observable, oldValue, newValue) -> switchSceneSearch.setUnderline(newValue));
        switchSceneLibrary.hoverProperty().addListener((observable, oldValue, newValue) -> switchSceneLibrary.setUnderline(newValue));
        albumLabel.hoverProperty().addListener((observable, oldValue, newValue) -> albumLabel.setUnderline(newValue));
        artistLabel.hoverProperty().addListener((observable, oldValue, newValue) -> artistLabel.setUnderline(newValue));
        ratingLabel.hoverProperty().addListener((observable, oldValue, newValue) -> ratingLabel.setUnderline(newValue));

        switchSceneSearch.setOnAction(e -> {
            stage.setScene(searchScene);
            sc2.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
            refreshStage();
        });

        albumLabel.setOnAction(e -> {
            if(!sortMethod.equals("name")) sortOrder = "asc";
            else sortOrder = sortOrder.equals("asc") ? "desc" : "asc";
            sortMethod = "name";
            refreshLibrary();
        });

        artistLabel.setOnAction(e -> {
            if(!sortMethod.equals("artist")) sortOrder = "asc";
            else sortOrder = sortOrder.equals("asc") ? "desc" : "asc";
            sortMethod = "artist";
            refreshLibrary();
        });

        ratingLabel.setOnAction(e -> {
            if(!sortMethod.equals("rating")) sortOrder = "asc";
            else sortOrder = sortOrder.equals("asc") ? "desc" : "asc";
            sortMethod = "rating";
            refreshLibrary();
        });

        sortDropdown.setOnAction(e -> {
            sortMethod = String.valueOf(sortDropdown.getValue());
            refreshLibrary();
        });

        pageDropdown.setOnAction(e -> {
            currentAlbumScene = Integer.parseInt(pageDropdown.getValue()) - 1;
            stage.setScene(libraryScenes.get(currentAlbumScene));
            refreshLibrary(currentAlbumScene);
        });

        return new Scene(root, WIDTH, HEIGHT);
    }


    public Scene drawSearchScene() {
        BorderPane root = new BorderPane();
        BorderPane heading = new BorderPane();
        HBox headingTabs = new HBox();
        BorderPane listPane = new BorderPane();
        VBox centerPane = new VBox();
        Pane leftMargin = new Pane();
        Pane rightMargin = new Pane();
        Pane emptyPane = new Pane();
        ScrollPane scrollPane = new ScrollPane(listPane);
        Label medleyLabel = new Label("Medley");
        Button switchSceneSearch = new Button("Add New Album");
        Button switchSceneLibrary = new Button("Your Library");
        Button addAllButton = new Button("Add Spotify Library");
        TextField searchField = new TextField();
        Button searchButton = new Button("Search");
        HBox searchBox = new HBox(searchField, searchButton);
        ImageView profileImage = new ImageView(new Image("C:\\Users\\aaron\\eclipse-workspace\\MedleyBeta\\src\\main\\java\\data\\profile_icon.png"));

        searchList.setStyle("-fx-background-color: #018ABD");
        heading.setStyle("-fx-background-color: #004581");
        headingTabs.setSpacing(10);
        heading.setMaxWidth(WIDTH - 14);
        medleyLabel.setFont(new Font("Impact", 30));
        medleyLabel.setStyle("-fx-text-fill: #DDE8F0");
        leftMargin.setPrefSize(100, HEIGHT - 75);
        leftMargin.setStyle("-fx-background-color: #97CBDC");
        rightMargin.setStyle("-fx-background-color: #97CBDC");
        rightMargin.setPrefSize(100, HEIGHT - 75);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        centerPane.setPrefSize(WIDTH - 230, HEIGHT - 75);
        centerPane.setStyle("-fx-background-color: #018ABD");
        searchBox.setAlignment(Pos.TOP_CENTER);
        searchBox.setStyle("-fx-background-color: #004581");
        searchBox.setSpacing(10);
        searchField.setPrefWidth(250);
        switchSceneSearch.setStyle("-fx-background-color: #004581; -fx-text-fill: #DDE8F0");
        switchSceneLibrary.setStyle("-fx-background-color: #004581; -fx-text-fill: #DDE8F0");
        profileImage.setFitHeight(40);
        profileImage.setFitWidth(40);
        addAllButton.setPrefSize(60, 60);
        addAllButton.setWrapText(true);
        addAllButton.setEffect(new DropShadow());
        addAllButton.setStyle("-fx-background-color: #F54768; -fx-text-fill: #DDE8F0");

        root.setTop(heading);
        root.setCenter(scrollPane);
        heading.setLeft(medleyLabel);
        heading.setRight(headingTabs);
        headingTabs.getChildren().addAll(switchSceneLibrary, switchSceneSearch, profileImage);
        listPane.setCenter(centerPane);
        listPane.setLeft(leftMargin);
        listPane.setRight(rightMargin);
        centerPane.getChildren().addAll(searchBox, searchList);
        rightMargin.getChildren().add(addAllButton);

        switchSceneSearch.hoverProperty().addListener((observable, oldValue, newValue) -> switchSceneSearch.setUnderline(newValue));
        switchSceneLibrary.hoverProperty().addListener((observable, oldValue, newValue) -> switchSceneLibrary.setUnderline(newValue));
        switchSceneLibrary.setOnAction(e -> refreshLibrary());

        searchButton.setOnAction(e -> {
            try {
                String query = searchField.getText();
                searchCards = albumController.searchResults(query, token, 12);
                searchList.getChildren().clear();
                for(HBox hBox : setAlbumCardList(searchCards, true)) searchList.getChildren().add(hBox);
                refreshStage();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        });

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

        return new Scene(root, WIDTH, HEIGHT);
    }


    public ArrayList<HBox> setAlbumCardList(ArrayList<AlbumCard> cards, boolean isSearch) {
        ArrayList<HBox> list = new ArrayList<>();
        int i = 0;
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

            Label numberLabel = new Label(String.valueOf(++i));
            numberLabel.setMinWidth(20);
            numberLabel.setStyle("-fx-text-fill: #DDE8F0");
            Label albumName = new Label(card.getAlbumName());
            albumName.setWrapText(true);
            albumName.setStyle("-fx-text-fill: #DDE8F0");
            Label artistName = new Label(card.getArtist());
            artistName.setPrefWidth(100);
            artistName.setWrapText(true);
            artistName.setStyle("-fx-text-fill: #DDE8F0");

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

            Button removeAlbum = new Button("Remove");
            removeAlbum.setOnAction(e -> {
                libraryController.removeAlbumFromUserLibrary(databaseConnection, userID, card.getAlbumID());
                int savePage = currentAlbumScene;
                refreshLibrary();
                stage.setScene(libraryScenes.get(savePage != numLibraryScenes ? savePage : savePage - 1));
            });

            HBox leftBox = new HBox();
            HBox rightBox = new HBox();
            leftBox.getChildren().addAll(numberLabel, imageView, albumName);
            leftBox.setSpacing(25);
            leftBox.setPrefWidth(250);

            if(!isSearch) {
                rightBox.getChildren().addAll(artistName, ratingDropDown, removeAlbum);
            } else {
                rightBox.getChildren().addAll(artistName, addButton);
            }
            rightBox.setSpacing(50);
            albumInfo.getChildren().addAll(leftBox, rightBox);
            albumInfo.setSpacing(200);

            albumInfo.setBorder(new Border(new BorderStroke(Color.rgb(151, 203, 220), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            list.add(albumInfo);
        }

        return list;
    }


    public VBox createAlbumVBox(int num) {
        VBox albumList = new VBox();
        albumCards = libraryController.getUserAlbums(databaseConnection, userID, sortMethod, sortOrder);
        int i = 0;
        for(HBox hBox : setAlbumCardList(albumCards, false)) {

            if(i >= num * ALBUMS_PER_SCENE) albumList.getChildren().add(hBox);
            if(i >= (num + 1) * ALBUMS_PER_SCENE - 1) break;
            i++;
        }

        albumList.setPrefSize(WIDTH - 230, HEIGHT - 75);
        return albumList;
    }


    public static void main(String[] args) {
        Application.launch(args);
    }

}
