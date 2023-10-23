package gui.main;

import api.spotify.UserAuthentication;
import api.spotify.albumController;
import api.spotify.artistController;
import api.spotify.userController;
import api.tools.JsonTree;
import database.*;
import gui.util.AlbumCard;
import gui.util.Observer;
import gui.model.AppModel;
import gui.scenes.AppScene;
import gui.scenes.SearchScene;
import gui.ui.AppTheme;
import gui.ui.widget.Histogram;
import gui.util.ImageTools;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
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
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.io.*;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.*;

public class medleyApp extends Application implements Observer<AppModel, String> {
    private final Connection databaseConnection;

    private final Group search = new Group();
    private final ScrollPane sc2 = new ScrollPane(search);
    private final VBox searchList = new VBox();

    private final ArrayList<Scene> libraryScenes = new ArrayList<>();
    private final ArrayList<VBox> albumLists = new ArrayList<>();
    private final HashMap<String, Image> imageHashMap = new HashMap<>();

    private final String username;
    private final int WIDTH = 1050;
    private final int HEIGHT = 750;

    private ArrayList<AlbumCard> albumCards;
    private ArrayList<AlbumCard> searchCards = new ArrayList<>();

    private int userID;
    private int ALBUMS_PER_SCENE;
    private int numLibraryScenes = 0;
    private int currentAlbumScene = 0;
    private int libraryAlbumInfoFlag = -1;
    private int libraryArtistInfoFlag = -1;

    private String token;
    private Stage stage;
    private Scene searchScene;
    private Scene settingsScene;
    private String windowTheme;
    private String sortMethod = "name";
    private String sortOrder = "asc";

    private String darkColor;
    private String darkColorHighlight;
    private String mediumColor;
    private String lightColor;
    private String extraLightColor;
    private String extraLightColorHighlight;
    private String contrastColor;
    private String contrastColorHighlight;

    AppModel model;

    public static void main(String[] args) {
        Application.launch(args);
    }


    public medleyApp() throws IOException {
        // connect to postgresql database
        Database database = new Database();
        databaseConnection = database.getDatabaseConnection();

        // user login/authentication to spotify api
        String clientID = "469af18e875a4fa1a58390d147ed924e";
        String clientSecret = "b142d702a4674c37b84c5928f482a7e5";
        String redirectURI = "http://localhost:8080";
        UserAuthentication userAuth = new UserAuthentication(clientID, clientSecret, redirectURI);
        do {
            token = userAuth.getUserAuthToken();
        } while(token.equals("invalid_grant"));

        // user login to postgresql database
        username = userController.getUserInfo(token, "username");
        Response response = loginController.attemptUserLogin(databaseConnection, username);
        if(response.status == Status.SUCCESS) {
            userID = Integer.parseInt(response.message);
        } else {
            System.out.println(response.message);
        }

        model = new AppModel(databaseConnection, userID, token);
    }


    @Override
    public void start(Stage primaryStage) {
        Image icon = new Image("C:\\Users\\aaron\\eclipse-workspace\\MedleyBeta\\src\\main\\java\\data\\img.png");
        Image profileImage = null;
        try {
            profileImage = ImageTools.retrieveImage(userController.getUserInfo(token, "image"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // load user preferences
        HashMap<String, String> preferences = loginController.getUserPreferences(databaseConnection, userID);
        ALBUMS_PER_SCENE = Integer.parseInt(preferences.get("albums_per_page"));
        windowTheme = preferences.get("theme");

        // set up stage
        stage = primaryStage;
        stage.setWidth(WIDTH);
        stage.setHeight(HEIGHT);
        stage.setResizable(false);
        stage.setTitle("Medley");
        stage.getIcons().add(icon);
        stage.show();

        AppScene appScene = new SearchScene(model, profileImage, new AppTheme(windowTheme));
        stage.setScene(appScene);
        model.addObserver(this);
        model.addObserver(appScene);

        refreshStage();
        // set up scenes
        // loadTheme();
        // refreshLibrary();

        // searchScene = drawSearchScene();
        // drawSettingsScene();
    }

    @Override
    public void update(AppModel model, String msg) {
        System.out.println(msg);
        refreshStage();
    }


    /** used to ensure that all gui elements appear as they are programmed to */
    public void refreshStage() {
        stage.setHeight((int) stage.getHeight() == HEIGHT ? HEIGHT + 1 : HEIGHT);
    }


    /** resets old library scenes and draws new ones for any change to the library */
    public void refreshLibrary() {refreshLibrary(0);}
    public void refreshLibrary(int num) {
        /*
        // reset
        libraryScenes.clear();
        albumLists.clear();
        albumCards = libraryController.getUserAlbums(databaseConnection, userID, sortMethod, sortOrder);
        numLibraryScenes = (albumCards.size() + ALBUMS_PER_SCENE - 1) / ALBUMS_PER_SCENE;

        // redraw library scenes
        int i = 0;
        do {
            libraryScenes.add(drawLibraryScene(stage, i));
        } while(++i < numLibraryScenes);

        stage.setScene(libraryScenes.get(num));
        refreshStage();
         */
        refreshLibraryInBackground();
        setLibraryScenes();
    }


    public void refreshLibraryInBackground() {
        // reset
        libraryScenes.clear();
        albumLists.clear();
        albumCards = libraryController.getUserAlbums(databaseConnection, userID, sortMethod, sortOrder);
        numLibraryScenes = (albumCards.size() + ALBUMS_PER_SCENE - 1) / ALBUMS_PER_SCENE;

        // redraw library scenes
        int i = 0;
        do {
            libraryScenes.add(drawLibraryScene(stage, i));
        } while(++i < numLibraryScenes);
    }


    public void setLibraryScenes() {
        stage.setScene(libraryScenes.get(0));
        refreshStage();
    }



    public Scene drawLibraryScene(Stage stage, int num) {
        refreshStage();
        albumLists.add(createAlbumVBox(num));
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
        Button coverArtLabel = new Button("Album Cover");
        Button albumLabel = new Button("Name");
        Button artistLabel = new Button("Artist");
        Button ratingLabel = new Button("Rating");
        Button switchSceneSearch = new Button("Add New Album");
        Button switchSceneLibrary = new Button("Your Library");
        ComboBox<String> pageDropdown = new ComboBox<>(pageOptions);
        ImageView profileImage = null;
        try {profileImage = new ImageView(ImageTools.retrieveImage(userController.getUserInfo(token, "image")));}
        catch (IOException e) {e.printStackTrace();}
        Button settingsButton = new Button();
        HBox albumBox = new HBox(albumLabel);
        HBox artistBox = new HBox(artistLabel);
        HBox ratingBox = new HBox(ratingLabel);

        albumLists.get(num).setStyle("-fx-background-color: " + mediumColor);
        heading.setStyle("-fx-background-color: " + darkColor);
        headingTabs.setSpacing(10);
        heading.setMaxWidth(WIDTH - 14);
        medleyLabel.setFont(new Font("Impact", 30));
        medleyLabel.setStyle("-fx-text-fill: " + extraLightColor);
        albumCardLabels.setPrefSize(WIDTH - 230, 25);
        albumCardLabels.setStyle("-fx-background-color: " + darkColor);
        emptyPane.setPrefWidth(50);
        coverArtLabel.setPrefWidth(125);
        coverArtLabel.setStyle("-fx-background-color: " + darkColor + "; -fx-text-fill: " + extraLightColor + "; -fx-font-weight: bold; -fx-font-size: 12pt; -fx-alignment: top_left");
        albumBox.setPrefWidth(275);
        albumLabel.setStyle("-fx-background-color: " + darkColor + "; -fx-text-fill: " + extraLightColor + "; -fx-font-weight: bold; -fx-font-size: 12pt; -fx-alignment: top_left");
        artistBox.setPrefWidth(150);
        artistLabel.setStyle("-fx-background-color: " + darkColor + "; -fx-text-fill: " + extraLightColor + "; -fx-font-weight: bold; -fx-font-size: 12pt; -fx-alignment: top_left");
        ratingBox.setPrefWidth(150);
        ratingLabel.setStyle("-fx-background-color: " + darkColor + "; -fx-text-fill: "  + extraLightColor + "; -fx-font-weight: bold; -fx-font-size: 12pt; -fx-alignment: top_left");
        leftMargin.setPrefSize(100, HEIGHT - 75);
        leftMargin.setStyle("-fx-background-color: " + lightColor);
        rightMargin.setStyle("-fx-background-color: " + lightColor);
        rightMargin.setPrefSize(100, HEIGHT - 75);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        switchSceneSearch.setStyle("-fx-background-color: " + darkColor + "; -fx-text-fill: " + extraLightColor);
        switchSceneLibrary.setStyle("-fx-background-color: " + darkColor + "; -fx-text-fill: " + extraLightColor);
        switchSceneLibrary.setUnderline(true);
        pageDropdown.setValue(String.valueOf(num + 1));
        pageDropdown.setStyle("-fx-background-color: " + extraLightColor);
        Objects.requireNonNull(profileImage).setFitHeight(40);
        profileImage.setFitWidth(40);
        settingsButton.setGraphic(profileImage);
        settingsButton.setStyle("-fx-background-color: " + darkColor);
        Region triangle = new Region();
        if(sortOrder.equals("asc")) triangle.setStyle("-fx-background-color: " + extraLightColor + "; -fx-shape: 'M0 0 L10 0 L5 5 z'; -fx-scale-shape: false");
        if(sortOrder.equals("desc")) triangle.setStyle("-fx-background-color: " + extraLightColor + "; -fx-shape: 'M0 0 L10 0 L5 -5 z'; -fx-scale-shape: false");

        root.setTop(heading);
        root.setCenter(scrollPane);
        heading.setLeft(medleyLabel);
        heading.setRight(headingTabs);
        headingTabs.getChildren().addAll(switchSceneLibrary, switchSceneSearch, settingsButton);
        albumCardLabels.getChildren().addAll(emptyPane, coverArtLabel, albumBox, artistBox, ratingBox, pageDropdown);
        listPane.setCenter(centerPane);
        listPane.setLeft(leftMargin);
        listPane.setRight(rightMargin);

        switch (sortMethod) {
            case "name" -> albumBox.getChildren().add(triangle);
            case "artist" -> artistBox.getChildren().add(triangle);
            case "rating" -> ratingBox.getChildren().add(triangle);
            default -> {}
        }

        switchSceneSearch.hoverProperty().addListener((observable, oldValue, newValue) -> switchSceneSearch.setUnderline(newValue));
        albumLabel.hoverProperty().addListener((observable, oldValue, newValue) -> albumLabel.setUnderline(newValue));
        artistLabel.hoverProperty().addListener((observable, oldValue, newValue) -> artistLabel.setUnderline(newValue));
        ratingLabel.hoverProperty().addListener((observable, oldValue, newValue) -> ratingLabel.setUnderline(newValue));
        settingsButton.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) settingsButton.setStyle("-fx-background-color: " + darkColorHighlight);
            else settingsButton.setStyle("-fx-background-color: " + darkColor);
        });

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

        pageDropdown.setOnAction(e -> {
            currentAlbumScene = Integer.parseInt(pageDropdown.getValue()) - 1;
            stage.setScene(libraryScenes.get(currentAlbumScene));
            refreshLibrary(currentAlbumScene);
        });

        settingsButton.setOnAction(e -> {
            drawSettingsScene();
            stage.setScene(settingsScene);
        });

        return new Scene(root, WIDTH, HEIGHT);
    }


    public Scene drawSearchScene() {
        Thread t = new Thread(this::refreshLibraryInBackground);
        t.start();

        BorderPane root = new BorderPane();
        BorderPane heading = new BorderPane();
        HBox headingTabs = new HBox();
        BorderPane listPane = new BorderPane();
        VBox centerPane = new VBox();
        Pane leftMargin = new Pane();
        Pane rightMargin = new Pane();
        ScrollPane scrollPane = new ScrollPane(listPane);
        Label medleyLabel = new Label("Medley");
        Button switchSceneSearch = new Button("Add New Album");
        Button switchSceneLibrary = new Button("Your Library");
        Button addAllButton = new Button("Add Spotify Library");
        TextField searchField = new TextField();
        Button searchButton = new Button("Search");
        HBox searchBox = new HBox(searchField, searchButton);
        ImageView profileImage = null;
        try {profileImage = new ImageView(ImageTools.retrieveImage(userController.getUserInfo(token, "image")));}
        catch (IOException e) {e.printStackTrace();}
        Button settingsButton = new Button();

        searchList.setStyle("-fx-background-color: " + mediumColor);
        heading.setStyle("-fx-background-color: " + darkColor);
        headingTabs.setSpacing(10);
        heading.setMaxWidth(WIDTH - 14);
        medleyLabel.setFont(new Font("Impact", 30));
        medleyLabel.setStyle("-fx-text-fill: " + extraLightColor);
        leftMargin.setPrefSize(100, HEIGHT - 75);
        leftMargin.setStyle("-fx-background-color: " + lightColor);
        rightMargin.setStyle("-fx-background-color: " + lightColor);
        rightMargin.setPrefSize(100, HEIGHT - 75);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        centerPane.setPrefSize(WIDTH - 230, HEIGHT - 75);
        centerPane.setStyle("-fx-background-color: " + mediumColor);
        searchBox.setAlignment(Pos.TOP_CENTER);
        searchBox.setStyle("-fx-background-color: " + mediumColor);
        searchBox.setSpacing(10);
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-background-color: " + extraLightColor);
        searchButton.setStyle("-fx-background-color: " + extraLightColor);
        switchSceneSearch.setStyle("-fx-background-color: " + darkColor + " ; -fx-text-fill: " + extraLightColor);
        switchSceneSearch.setUnderline(true);
        switchSceneLibrary.setStyle("-fx-background-color: " + darkColor + "; -fx-text-fill: " + extraLightColor);
        Objects.requireNonNull(profileImage).setFitHeight(40);
        profileImage.setFitWidth(40);
        addAllButton.setPrefSize(60, 60);
        addAllButton.setWrapText(true);
        addAllButton.setEffect(new DropShadow());
        addAllButton.setStyle("-fx-background-color: " + contrastColor + "; -fx-text-fill: " + extraLightColor);
        settingsButton.setGraphic(profileImage);
        settingsButton.setStyle("-fx-background-color: " + darkColor);

        root.setTop(heading);
        root.setCenter(scrollPane);
        heading.setLeft(medleyLabel);
        heading.setRight(headingTabs);
        headingTabs.getChildren().addAll(switchSceneLibrary, switchSceneSearch, settingsButton);
        listPane.setCenter(centerPane);
        listPane.setLeft(leftMargin);
        listPane.setRight(rightMargin);
        centerPane.getChildren().addAll(searchBox, searchList);
        rightMargin.getChildren().add(addAllButton);

        switchSceneLibrary.hoverProperty().addListener((observable, oldValue, newValue) -> switchSceneLibrary.setUnderline(newValue));
        addAllButton.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) addAllButton.setStyle("-fx-background-color: " + contrastColorHighlight + "; -fx-text-fill: " + extraLightColor);
            else addAllButton.setStyle("-fx-background-color: " + contrastColor + "; -fx-text-fill: " + extraLightColor);
        });
        searchButton.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) searchButton.setStyle("-fx-background-color: " + extraLightColorHighlight);
            else searchButton.setStyle("-fx-background-color: " + extraLightColor);
        });
        settingsButton.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) settingsButton.setStyle("-fx-background-color: " + darkColorHighlight);
            else settingsButton.setStyle("-fx-background-color: " + darkColor);
        });

        switchSceneLibrary.setOnAction(e -> setLibraryScenes());

        searchButton.setOnAction(e -> {
            try {
                String query = searchField.getText();
                if(query.length() > 0) searchCards = albumController.searchResults(query, token, 12);
                searchList.getChildren().clear();
                for(HBox hBox : setAlbumCardList(searchCards, true, -1, -1)) searchList.getChildren().add(hBox);
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

        settingsButton.setOnAction(e -> {
            drawSettingsScene();
            stage.setScene(settingsScene);
        });

        return new Scene(root, WIDTH, HEIGHT);
    }


    public void drawSettingsScene() {
        Thread t = new Thread(this::refreshLibraryInBackground);
        t.start();

        refreshStage();
        ArrayList<String> topRatedAlbums = libraryController.topRatedAlbums(databaseConnection, userID, 3);
        ArrayList<String> topRatedArtists = libraryController.topRatedArtist(databaseConnection, userID, 3);

        BorderPane root = new BorderPane();
        BorderPane heading = new BorderPane();
        HBox headingTabs = new HBox();
        BorderPane listPane = new BorderPane();
        VBox centerPane = new VBox();
        Pane leftMargin = new Pane();
        Pane rightMargin = new Pane();
        ScrollPane scrollPane = new ScrollPane(listPane);
        Label medleyLabel = new Label("Medley");
        Button switchSceneSearch = new Button("Add New Album");
        Button switchSceneLibrary = new Button("Your Library");
        ImageView profileImage = null;
        try {profileImage = new ImageView(ImageTools.retrieveImage(userController.getUserInfo(token, "image")));}
        catch (IOException e) {e.printStackTrace();}
        Button settingsButton = new Button();
        VBox topAlbumPane = new VBox();
        VBox topArtistPane = new VBox();
        VBox histogramPane = new VBox();
        Label topAlbumLabel = new Label("Your top albums");
        HBox topAlbumList = new HBox();
        Label topArtistLabel = new Label("Your top artists");
        HBox topArtistList = new HBox();
        Label histogramLabel = new Label("Your ratings");
        Histogram ratingHistogram = new Histogram(new CategoryAxis(), new NumberAxis(), "Score", "Frequency");
        // ratingHistogram.addData(libraryController.getRatingData(databaseConnection, userID));

        heading.setStyle("-fx-background-color: " + darkColor);
        headingTabs.setSpacing(10);
        heading.setMaxWidth(WIDTH - 14);
        medleyLabel.setFont(new Font("Impact", 30));
        medleyLabel.setStyle("-fx-text-fill: " + extraLightColor);
        leftMargin.setPrefSize(100, HEIGHT - 75);
        leftMargin.setStyle("-fx-background-color: " + lightColor);
        rightMargin.setStyle("-fx-background-color: " + lightColor);
        rightMargin.setPrefSize(100, HEIGHT - 75);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setFitToHeight(true);
        centerPane.setPrefSize(WIDTH - 230, HEIGHT - 75);
        centerPane.setStyle("-fx-background-color: " + mediumColor);
        switchSceneSearch.setStyle("-fx-background-color: " + darkColor + "; -fx-text-fill: " + extraLightColor);
        switchSceneLibrary.setStyle("-fx-background-color: " + darkColor + "; -fx-text-fill: " + extraLightColor);
        Objects.requireNonNull(profileImage).setFitHeight(40);
        profileImage.setFitWidth(40);
        settingsButton.setGraphic(profileImage);
        settingsButton.setStyle("-fx-background-color: " + darkColorHighlight);
        topAlbumPane.setBorder(new Border(new BorderStroke(Color.web(lightColor), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        topAlbumPane.setSpacing(15);
        topAlbumPane.setPadding(new Insets(5, 12, 5, 12));
        topAlbumLabel.setStyle("-fx-text-fill: " + extraLightColor);
        topAlbumLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        topAlbumList.setSpacing(25);
        topArtistPane.setBorder(new Border(new BorderStroke(Color.web(lightColor), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        topArtistPane.setSpacing(15);
        topArtistPane.setPadding(new Insets(5, 12, 5, 12));
        topArtistLabel.setStyle("-fx-text-fill: " + extraLightColor);
        topArtistLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        topArtistList.setSpacing(25);
        histogramPane.setBorder(new Border(new BorderStroke(Color.web(lightColor), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        histogramPane.setSpacing(15);
        histogramPane.setPadding(new Insets(5, 12, 5, 12));
        histogramPane.setMinHeight(500);
        histogramLabel.setStyle("-fx-text-fill: " + extraLightColor);
        histogramLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 20));


        root.setTop(heading);
        root.setCenter(scrollPane);
        heading.setLeft(medleyLabel);
        heading.setRight(headingTabs);
        headingTabs.getChildren().addAll(switchSceneLibrary, switchSceneSearch, settingsButton);
        listPane.setCenter(centerPane);
        listPane.setLeft(leftMargin);
        listPane.setRight(rightMargin);
        centerPane.getChildren().addAll(topAlbumPane, topArtistPane, histogramPane);
        topAlbumPane.getChildren().addAll(topAlbumLabel, topAlbumList);
        topArtistPane.getChildren().addAll(topArtistLabel, topArtistList);
        histogramPane.getChildren().addAll(histogramLabel, ratingHistogram);

        for (int i = 0; i < topRatedAlbums.size(); i++) {
            String albumRating = topRatedAlbums.get(i);
            String album = albumRating.split(": ")[0];
            HBox albumBox = new HBox();
            VBox imageBox = new VBox();
            Label numLabel = new Label("#" + (i+1));
            Label albumLabel = new Label(album);
            albumBox.setSpacing(10);
            imageBox.setSpacing(5);
            numLabel.setStyle("-fx-text-fill: " + extraLightColor);
            numLabel.setFont(Font.font("Verdana", FontWeight.NORMAL, 20));
            albumLabel.setStyle("-fx-text-fill: " + extraLightColor);
            albumLabel.setFont(Font.font("Verdana", FontWeight.NORMAL, 16));
            albumLabel.setWrapText(true);
            albumLabel.setMaxWidth(200);
            ImageView imageView = new ImageView();
            try {
                String imageURL = libraryController.getAlbumImageURL(databaseConnection, album).message;
                Image image = ImageTools.retrieveImage(imageURL);
                imageView = new ImageView(image);
                imageView.setFitWidth(200);
                imageView.setFitHeight(200);
            } catch (IOException ioe) {ioe.printStackTrace();}
            imageBox.getChildren().addAll(imageView, albumLabel);
            albumBox.getChildren().addAll(numLabel, imageBox);
            topAlbumList.getChildren().add(albumBox);
        }

        for (int i = 0; i < topRatedArtists.size(); i++) {
            String artistScore = topRatedArtists.get(i);
            String artist = artistScore.split(": ")[0];
            String score = artistScore.split(": ")[1];
            HBox artistBox = new HBox();
            VBox imageBox = new VBox();
            Label numLabel = new Label("#" + (i+1));
            Label albumLabel = new Label(artistScore);
            artistBox.setSpacing(10);
            imageBox.setSpacing(5);
            numLabel.setStyle("-fx-text-fill: " + extraLightColor);
            numLabel.setFont(Font.font("Verdana", FontWeight.NORMAL, 20));
            albumLabel.setStyle("-fx-text-fill: " + extraLightColor);
            albumLabel.setFont(Font.font("Verdana", FontWeight.NORMAL, 16));
            albumLabel.setWrapText(true);
            albumLabel.setMaxWidth(200);
            ImageView imageView = new ImageView();
            try {
                String artistID = libraryController.getArtistID(databaseConnection, artist).message;
                String imageURL = artistController.getArtistImageURL(token, artistID);
                Image image = ImageTools.retrieveImage(imageURL);
                imageView = new ImageView(image);
                imageView.setFitWidth(200);
                imageView.setFitHeight(200);
            } catch (IOException ioe) {ioe.printStackTrace();}
            imageBox.getChildren().addAll(imageView, albumLabel);
            artistBox.getChildren().addAll(numLabel, imageBox);
            topArtistList.getChildren().add(artistBox);
        }

        switchSceneSearch.hoverProperty().addListener((observable, oldValue, newValue) -> switchSceneSearch.setUnderline(newValue));
        switchSceneLibrary.hoverProperty().addListener((observable, oldValue, newValue) -> switchSceneLibrary.setUnderline(newValue));
        switchSceneSearch.setOnAction(e -> {
            searchScene = drawSearchScene();
            stage.setScene(searchScene);
        });
        switchSceneLibrary.setOnAction(e -> setLibraryScenes());

        settingsScene = new Scene(root, WIDTH, HEIGHT);
        //return new Scene(root, WIDTH, HEIGHT);

    }


    public Scene drawSettingsScene1() {
        float sum = 0;
        float count = 0;
        for(AlbumCard card : albumCards) if(card.getRating() > 0) {
            sum += card.getRating();
            count++;
        }
        float average = sum / count;
        ObservableList<String> themeOptions = FXCollections.observableArrayList("Classic", "Sea Wave", "Embers", "Petal");

        BorderPane root = new BorderPane();
        BorderPane heading = new BorderPane();
        HBox headingTabs = new HBox();
        BorderPane listPane = new BorderPane();
        VBox centerPane = new VBox();
        Pane leftMargin = new Pane();
        Pane rightMargin = new Pane();
        ScrollPane scrollPane = new ScrollPane(listPane);
        VBox profileBox = new VBox();
        VBox settingsBox = new VBox();
        HBox albumsPerPageBox = new HBox();
        HBox themeBox = new HBox();
        Label medleyLabel = new Label("Medley");
        Label profileLabel = new Label("Profile:");
        Label settingsLabel = new Label("Settings:");
        Label usernameLabel = new Label("Username: " + username);
        Label numAlbumsLabel = new Label("Albums saved: " + albumCards.size());
        Label averageRatingLabel = new Label("Average score: " + new DecimalFormat("0.00").format(average));
        Label albumsPerPageLabel = new Label("Albums per page: ");
        Label themeLabel = new Label("Theme: ");
        Button switchSceneSearch = new Button("Add New Album");
        Button switchSceneLibrary = new Button("Your Library");
        Button applyButton = new Button("Apply");
        TextField albumsPerPageField = new TextField();
        ComboBox<String> themeDropdown = new ComboBox<>(themeOptions);
        ImageView profileImage = null;
        try {profileImage = new ImageView(ImageTools.retrieveImage(userController.getUserInfo(token, "image")));}
        catch (IOException e) {e.printStackTrace();}
        Button settingsButton = new Button();
        Histogram ratingHistogram = new Histogram(new CategoryAxis(), new NumberAxis(), "Rating", "Frequency");
        // ratingHistogram.addData(libraryController.getRatingData(databaseConnection, userID));

        heading.setStyle("-fx-background-color: " + darkColor);
        headingTabs.setSpacing(10);
        heading.setMaxWidth(WIDTH - 14);
        medleyLabel.setFont(new Font("Impact", 30));
        medleyLabel.setStyle("-fx-text-fill: " + extraLightColor);
        leftMargin.setPrefSize(100, HEIGHT - 75);
        leftMargin.setStyle("-fx-background-color: " + lightColor);
        rightMargin.setStyle("-fx-background-color: " + lightColor);
        rightMargin.setPrefSize(100, HEIGHT - 75);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        centerPane.setPrefSize(WIDTH - 230, HEIGHT - 75);
        centerPane.setStyle("-fx-background-color: " + mediumColor);
        switchSceneSearch.setStyle("-fx-background-color: " + darkColor + "; -fx-text-fill: " + extraLightColor);
        switchSceneLibrary.setStyle("-fx-background-color: " + darkColor + "; -fx-text-fill: " + extraLightColor);
        Objects.requireNonNull(profileImage).setFitHeight(40);
        profileImage.setFitWidth(40);
        settingsButton.setGraphic(profileImage);
        settingsButton.setStyle("-fx-background-color: " + darkColorHighlight);
        centerPane.setSpacing(50);
        profileBox.setSpacing(10);
        profileBox.setPadding(new Insets(15));
        settingsBox.setSpacing(10);
        settingsBox.setPadding(new Insets(15));
        profileLabel.setStyle("-fx-text-fill: " + extraLightColor + "; -fx-font-weight: bold; -fx-font-size: 16");
        settingsLabel.setStyle("-fx-text-fill: " + extraLightColor + "; -fx-font-weight: bold; -fx-font-size: 16");
        usernameLabel.setStyle("-fx-text-fill: " + extraLightColor + "; -fx-font-size: 12");
        numAlbumsLabel.setStyle("-fx-text-fill: " + extraLightColor + "; -fx-font-size: 12");
        averageRatingLabel.setStyle("-fx-text-fill: " + extraLightColor + "; -fx-font-size: 12");
        albumsPerPageLabel.setStyle("-fx-text-fill: " + extraLightColor + "; -fx-font-size: 12");
        themeLabel.setStyle("-fx-text-fill: " + extraLightColor + "; -fx-font-size: 12");
        albumsPerPageBox.setSpacing(10);
        themeBox.setSpacing(10);
        albumsPerPageField.setStyle("-fx-background-color: " + extraLightColor);
        albumsPerPageField.setText(String.valueOf(ALBUMS_PER_SCENE));
        themeDropdown.setStyle("-fx-background-color: " + extraLightColor);
        themeDropdown.setValue(windowTheme);
        applyButton.setStyle("-fx-background-color: " + darkColor + "; -fx-text-fill: " + extraLightColor);

        root.setTop(heading);
        root.setCenter(scrollPane);
        heading.setLeft(medleyLabel);
        heading.setRight(headingTabs);
        headingTabs.getChildren().addAll(switchSceneLibrary, switchSceneSearch, settingsButton);
        listPane.setCenter(centerPane);
        listPane.setLeft(leftMargin);
        listPane.setRight(rightMargin);
        centerPane.getChildren().addAll(profileBox, settingsBox);
        profileBox.getChildren().addAll(profileLabel, usernameLabel, numAlbumsLabel, averageRatingLabel, ratingHistogram);
        settingsBox.getChildren().addAll(settingsLabel, albumsPerPageBox, themeBox, applyButton);
        albumsPerPageBox.getChildren().addAll(albumsPerPageLabel, albumsPerPageField);
        themeBox.getChildren().addAll(themeLabel, themeDropdown);

        switchSceneSearch.hoverProperty().addListener((observable, oldValue, newValue) -> switchSceneSearch.setUnderline(newValue));
        switchSceneLibrary.hoverProperty().addListener((observable, oldValue, newValue) -> switchSceneLibrary.setUnderline(newValue));
        switchSceneSearch.setOnAction(e -> {
            searchScene = drawSearchScene();
            stage.setScene(searchScene);
        });
        switchSceneLibrary.setOnAction(e -> refreshLibrary());

        applyButton.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) applyButton.setStyle("-fx-background-color: " + darkColorHighlight + "; -fx-text-fill: " + extraLightColor);
            else applyButton.setStyle("-fx-background-color: " + darkColor + "; -fx-text-fill: " + extraLightColor);
        });

        applyButton.setOnAction(e -> {
            try {
                ALBUMS_PER_SCENE = Integer.parseInt(albumsPerPageField.getText());
            } catch(NumberFormatException ignored) {}

            windowTheme = themeDropdown.getValue();
            loadTheme();
            albumsPerPageField.clear();

            drawSettingsScene();
            searchScene = drawSearchScene();
            stage.setScene(settingsScene);

            Response res = loginController.updateUserPreferences(databaseConnection, userID, ALBUMS_PER_SCENE, windowTheme);
            if(res.status == Status.ERROR) System.out.println(res.message);
        });

        return new Scene(root, WIDTH, HEIGHT);
    }


    /** creates an album card, the main visual element used for displaying an
     * album. can be used for both the library and search scenes */
    public ArrayList<HBox> setAlbumCardList(ArrayList<AlbumCard> cards, boolean isSearch, int albumInfoFlag, int artistInfoFlag) {
        ArrayList<HBox> list = new ArrayList<>();
        int i = 0;
        int count = 0;
        for(AlbumCard card : cards) {
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

            ObservableList<String> ratingOptions = FXCollections.observableArrayList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
            Response res1 = libraryController.checkAlbumInUserLibrary(databaseConnection, userID, card.getAlbumID());

            HBox albumInfo = new HBox();
            HBox leftBox = new HBox();
            HBox rightBox = new HBox();
            Label numberLabel = new Label(String.valueOf(++i));
            Label albumName = new Label(card.getAlbumName());
            Button artistName = new Button(card.getArtist());
            Button addButton = new Button("Add to Library");
            Button removeAlbum = new Button("Remove");
            ComboBox<String> ratingDropDown = new ComboBox<>(ratingOptions);

            albumInfo.setBorder(new Border(new BorderStroke(Color.web(lightColor), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            albumInfo.setPadding(new Insets(5, 12, 5, 12));
            albumInfo.setSpacing(200);
            leftBox.setSpacing(25);
            leftBox.setPrefWidth(250);
            rightBox.setSpacing(50);
            imageView.setFitHeight(50);
            imageView.setFitWidth(50);
            numberLabel.setMinWidth(20);
            numberLabel.setStyle("-fx-text-fill: " + extraLightColor);
            albumName.setWrapText(true);
            albumName.setStyle("-fx-text-fill: " + extraLightColor);
            albumName.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
            artistName.setPrefWidth(100);
            artistName.setWrapText(true);
            artistName.setStyle("-fx-background-color: " + mediumColor + "; -fx-text-fill: " + extraLightColor);
            artistName.setFont(Font.font("Verdana", FontWeight.NORMAL, 15));
            if (count == artistInfoFlag) artistName.setUnderline(true);
            if(res1.status == Status.SUCCESS) {
                addButton.setText("Added");
                addButton.setStyle("-fx-background-color: " + darkColorHighlight + "; -fx-text-fill: " + extraLightColor);
            } else addButton.setStyle("-fx-background-color: " + darkColor + "; -fx-text-fill: " + extraLightColor);
            removeAlbum.setStyle("-fx-background-color: " + darkColor + "; -fx-text-fill: " + extraLightColor);
            ratingDropDown.setValue(card.getRating() == 0  ? " " : String.valueOf((int) card.getRating()));
            ratingDropDown.setStyle("-fx-background-color: " + extraLightColor);

            albumInfo.getChildren().addAll(leftBox, rightBox);
            leftBox.getChildren().addAll(numberLabel, imageView, albumName);
            if(!isSearch) rightBox.getChildren().addAll(artistName, ratingDropDown, removeAlbum);
            else rightBox.getChildren().addAll(artistName, addButton);

            artistName.hoverProperty().addListener((observable, oldValue, newValue) -> {
                artistName.setUnderline(newValue);
            });
            addButton.hoverProperty().addListener((observable, oldValue, newValue) -> {
                if(addButton.getText().equals("Add to Library")) {
                    if (newValue) addButton.setStyle("-fx-background-color: " + darkColorHighlight + "; -fx-text-fill: " + extraLightColor);
                    else addButton.setStyle("-fx-background-color: " + darkColor + "; -fx-text-fill: " + extraLightColor);
                }
            });
            removeAlbum.hoverProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue) removeAlbum.setStyle("-fx-background-color: " + darkColorHighlight + "; -fx-text-fill: " + extraLightColor);
                else removeAlbum.setStyle("-fx-background-color: " + darkColor + "; -fx-text-fill: " + extraLightColor);
            });

            int finalCount = count;
            artistName.setOnAction(e -> {
                if (libraryArtistInfoFlag == -1) {
                    albumLists.get(currentAlbumScene).getChildren().add(finalCount + 1, artistInfoBox(card.getArtistID()));
                    libraryArtistInfoFlag = finalCount;
                } else if (finalCount == libraryArtistInfoFlag) {
                    albumLists.get(currentAlbumScene).getChildren().remove(finalCount + 1);
                    libraryArtistInfoFlag = -1;
                } else {
                    albumLists.get(currentAlbumScene).getChildren().remove(libraryArtistInfoFlag + 1);
                    albumLists.get(currentAlbumScene).getChildren().add(finalCount + 1, artistInfoBox(card.getArtistID()));
                    libraryArtistInfoFlag = finalCount;
                }
                refreshStage();
            });

            addButton.setOnAction(e -> {
                Response res = libraryController.addAlbumToUserLibrary(databaseConnection, userID, card);
                if (res.status == Status.ERROR) System.out.println(res.message);
                addButton.setText("Added");
                addButton.setStyle("-fx-background-color: " + darkColorHighlight + "; -fx-text-fill: " + extraLightColor);
            });

            removeAlbum.setOnAction(e -> {
                libraryController.removeAlbumFromUserLibrary(databaseConnection, userID, card.getAlbumID());
                int savePage = currentAlbumScene;
                refreshLibrary();
                stage.setScene(libraryScenes.get(savePage != numLibraryScenes ? savePage : savePage - 1));
            });

            ratingDropDown.setOnAction(e -> {
                int rating = Integer.parseInt(String.valueOf(ratingDropDown.getValue()));
                Response res = libraryController.rateAlbum(databaseConnection, userID, card.getAlbumID(), rating);
                if(res.status == Status.ERROR) System.out.println(res.message);
            });

            list.add(albumInfo);
            count++;
        }

        return list;
    }


    public HBox artistInfoBox(String artistID) {
        try {
            JsonTree artistInfo = artistController.getArtistInfo(token, artistID);
            String name = artistInfo.get("name").retrieveValue();
            int followers = Integer.parseInt(artistInfo.get("followers").get("total").retrieveValue());
            String topGenre = artistInfo.get("genres").retrieveValue().split("\"")[3];
            String artistImageURL = artistInfo.get("images").get("0").get("url").retrieveValue();
            ArrayList<String> topTracks = artistController.getArtistTracks(token, artistID);
            String relatedArtist = artistController.getRelatedArtist(token, artistID);

            HBox artistCard = new HBox();
            HBox artistBox = new HBox();
            VBox infoBox = new VBox();
            VBox trackBox = new VBox();
            VBox leftMargin = new VBox();
            VBox rightMargin = new VBox();
            ImageView imageView = new ImageView(ImageTools.retrieveImage(artistImageURL));
            Label nameLabel = new Label(name);
            Label followerLabel = new Label("Followers: " + formatFollowerNumber(followers));
            Label genreLabel = new Label("Top Genre: " + topGenre);
            Label relatedArtistLabel = new Label("Related Artist: " + relatedArtist);
            Label topTracksLabel = new Label("Top tracks:");

            artistCard.setBorder(new Border(new BorderStroke(Color.web(lightColor), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            artistBox.setPadding(new Insets(5, 5, 5, 50));
            artistBox.setPrefWidth(725);
            artistBox.setSpacing(50);
            infoBox.setSpacing(15);
            leftMargin.setPrefWidth(50);
            leftMargin.setStyle("-fx-background-color: " + lightColor);
            rightMargin.setPrefWidth(50);
            rightMargin.setStyle("-fx-background-color: " + lightColor);
            imageView.setFitWidth(100);
            imageView.setFitHeight(100);
            nameLabel.setStyle("-fx-text-fill: " + extraLightColor);
            nameLabel.setFont(Font.font("Verdana", FontWeight.NORMAL, 12));
            followerLabel.setStyle("-fx-text-fill: " + extraLightColor);
            followerLabel.setFont(Font.font("Verdana", FontWeight.NORMAL, 12));
            genreLabel.setStyle("-fx-text-fill: " + extraLightColor);
            genreLabel.setFont(Font.font("Verdana", FontWeight.NORMAL, 12));
            relatedArtistLabel.setStyle("-fx-text-fill: " + extraLightColor);
            relatedArtistLabel.setFont(Font.font("Verdana", FontWeight.NORMAL, 12));
            topTracksLabel.setStyle("-fx-text-fill: " + extraLightColor);
            topTracksLabel.setFont(Font.font("Verdana", FontWeight.NORMAL, 12));

            infoBox.getChildren().addAll(nameLabel, followerLabel, genreLabel, relatedArtistLabel);
            trackBox.getChildren().add(topTracksLabel);
            for (int i = 0; i < topTracks.size(); i++) {
                Label label = new Label((i + 1) + ": " + topTracks.get(i));
                label.setStyle("-fx-text-fill: " + extraLightColor);
                label.setFont(Font.font("Verdana", FontWeight.NORMAL, 12));
                trackBox.getChildren().add(label);
            }
            artistBox.getChildren().addAll(imageView, infoBox, trackBox);
            artistCard.getChildren().addAll(leftMargin, artistBox, rightMargin);
            return artistCard;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }


    public String formatFollowerNumber(int followers) {
        if (followers < 1000) return String.valueOf(followers);
        else if (followers < 1000000) return String.format("%.1f", (float) followers / 1000) + "K";
        else return String.format("%.1f", (float) followers / 1000000) + "M";
    }


    /** creates a vbox from the result of setAlbumCardList for the library scene */
    public VBox createAlbumVBox(int num) {
        VBox albumList = new VBox();
        // albumCards = libraryController.getUserAlbums(databaseConnection, userID, sortMethod, sortOrder);
        int i = 0;
        ArrayList<HBox> albumCardList = setAlbumCardList(albumCards, false, libraryAlbumInfoFlag, libraryArtistInfoFlag);
        for(HBox hBox : albumCardList) {
            if(i >= num * ALBUMS_PER_SCENE) albumList.getChildren().add(hBox);
            if(i >= (num + 1) * ALBUMS_PER_SCENE - 1) break;
            i++;
        }

        albumList.setPrefSize(WIDTH - 230, HEIGHT - 75);
        return albumList;
    }


    /** loads and sets gui class colors from the appropriate file */
    public void loadTheme() {
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
