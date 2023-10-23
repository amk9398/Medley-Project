package gui.main;

import api.spotify.UserAuthentication;
import api.spotify.userController;
import database.Database;
import database.Response;
import database.Status;
import database.loginController;
import gui.util.Observer;
import gui.model.AppModel;
import gui.scenes.AppScene;
import gui.scenes.LibraryScene;
import gui.scenes.ProfileScene;
import gui.scenes.SearchScene;
import gui.ui.AppTheme;
import gui.util.ImageTools;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;

public class Medley extends Application implements Observer<AppModel, String> {
    private final int WIDTH = 1050;
    private final int HEIGHT = 750;
    private final Connection databaseConnection;
    private Stage stage;
    private String token;
    private String username;
    private int userID;
    private AppModel model;
    private String windowTheme;
    private AppScene libraryScene;
    private AppScene searchScene;
    private AppScene profileScene;
    private Image profileImage = null;
    private AppTheme theme;

    public static void main(String[] args) {
        Application.launch(args);
    }


    public Medley() throws IOException {
        Database database = new Database();
        databaseConnection = database.getDatabaseConnection();

        String clientID = "469af18e875a4fa1a58390d147ed924e";
        String clientSecret = "b142d702a4674c37b84c5928f482a7e5";
        String redirectURI = "http://localhost:8080";
        UserAuthentication userAuth = new UserAuthentication(clientID, clientSecret, redirectURI);
        do {
            token = userAuth.getUserAuthToken();
        } while(token.equals("invalid_grant"));

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
        try {
            profileImage = ImageTools.retrieveImage(userController.getUserInfo(token, "image"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        HashMap<String, String> preferences = loginController.getUserPreferences(databaseConnection, userID);
        windowTheme = preferences.get("theme");
        theme = new AppTheme(windowTheme);

        stage = primaryStage;
        stage.setWidth(WIDTH);
        stage.setHeight(HEIGHT);
        stage.setResizable(false);
        stage.setTitle("Medley");
        stage.getIcons().add(icon);
        stage.show();

        libraryScene = new LibraryScene(model, profileImage, new AppTheme(windowTheme));
        searchScene = new SearchScene(model, profileImage, new AppTheme(windowTheme));
        profileScene = new ProfileScene(model, profileImage, new AppTheme(windowTheme));

        model.addObserver(this);
        model.addObserver(libraryScene);
        model.addObserver(searchScene);
        model.addObserver(profileScene);

        model.setCurrentScene("library");
    }

    @Override
    public void update(AppModel model, String msg) {
        System.out.println(msg);
        if (msg.equals("set current scene")) {
            switch (model.getCurrentScene()) {
                case "library" -> stage.setScene(libraryScene);
                case "search" -> stage.setScene(searchScene);
                case "profile" -> stage.setScene(profileScene);
            }
        }
        refreshStage();
    }

    public void refreshStage() {
        stage.setHeight((int) stage.getHeight() == HEIGHT ? HEIGHT + 1 : HEIGHT);
    }
}
