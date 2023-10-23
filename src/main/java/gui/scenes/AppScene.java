package gui.scenes;

import gui.model.AppModel;
import gui.ui.AppTheme;
import gui.util.Observer;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class AppScene extends Scene implements Observer<AppModel, String> {
    public final int WIDTH = 1050;
    public final int HEIGHT = 750;
    private final BorderPane root = new BorderPane();
    private final BorderPane heading = new BorderPane();
    private final HBox headingTabs = new HBox();
    private final BorderPane listPane = new BorderPane();
    private final Pane leftMargin = new Pane();
    private final Pane rightMargin = new Pane();
    private final ScrollPane scrollPane = new ScrollPane(listPane);
    private final Label medleyLabel = new Label();
    private final ImageView profileImageView = new ImageView();
    private final Button switchSceneSearch = new Button();
    private final Button switchSceneLibrary = new Button();
    private final Button settingsButton = new Button();

    protected final VBox contentPane = new VBox();

    private final AppTheme theme;
    private AppModel model;

    public AppScene(AppModel model, Image profileImage, AppTheme theme) {
        super(new Pane());
        this.model = model;
        this.theme = theme;
        setGuiContent(profileImage);
        setGuiStyle();
        setGuiHierarchy();
        setGuiActions();
        this.setRoot(root);
    }

    @Override
    public void update(AppModel model, String msg) {}

    public AppModel getModel() {return model;}
    public void setModel(AppModel model) {this.model = model;}

    public AppTheme getTheme() {return theme;}

    private void setGuiContent(Image profileImage) {
        medleyLabel.setText("Medley");
        switchSceneLibrary.setText("Your Library");
        switchSceneSearch.setText("Add New Album");
        profileImageView.setImage(profileImage);
        settingsButton.setGraphic(profileImageView);
    }

    private void setGuiStyle() {
        heading.setStyle("-fx-background-color: " + theme.darkColor);
        headingTabs.setSpacing(10);
        heading.setMaxWidth(WIDTH - 14);
        medleyLabel.setFont(new Font("Impact", 30));
        medleyLabel.setStyle("-fx-text-fill: " + theme.extraLightColor);
        leftMargin.setPrefSize(100, HEIGHT - 75);
        leftMargin.setStyle("-fx-background-color: " + theme.lightColor);
        rightMargin.setStyle("-fx-background-color: " + theme.lightColor);
        rightMargin.setPrefSize(100, HEIGHT - 75);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        contentPane.setPrefSize(WIDTH - 230, HEIGHT - 75);
        contentPane.setStyle("-fx-background-color: " + theme.mediumColor);
        switchSceneSearch.setStyle("-fx-background-color: " + theme.darkColor + " ; -fx-text-fill: " + theme.extraLightColor);
        switchSceneSearch.setFont(Font.font("Verdana", FontWeight.NORMAL, 12));
        switchSceneLibrary.setStyle("-fx-background-color: " + theme.darkColor + "; -fx-text-fill: " + theme.extraLightColor);
        switchSceneLibrary.setFont(Font.font("Verdana", FontWeight.NORMAL, 12));
        profileImageView.setFitHeight(40);
        profileImageView.setFitWidth(40);
        settingsButton.setStyle("-fx-background-color: " + theme.darkColor);
    }

    private void setGuiHierarchy() {
        root.setTop(heading);
        root.setCenter(scrollPane);
        heading.setLeft(medleyLabel);
        heading.setRight(headingTabs);
        headingTabs.getChildren().addAll(switchSceneLibrary, switchSceneSearch, settingsButton);
        listPane.setCenter(contentPane);
        listPane.setLeft(leftMargin);
        listPane.setRight(rightMargin);
    }

    private void setGuiActions() {
        switchSceneLibrary.hoverProperty().addListener((observable, oldValue, newValue) -> switchSceneLibrary.setUnderline(newValue));
        switchSceneSearch.hoverProperty().addListener((observable, oldValue, newValue) -> switchSceneSearch.setUnderline(newValue));
        settingsButton.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) settingsButton.setStyle("-fx-background-color: " + theme.darkColorHighlight);
            else settingsButton.setStyle("-fx-background-color: " + theme.darkColor);
        });
        switchSceneLibrary.setOnAction(e -> model.setCurrentScene("library"));
        switchSceneSearch.setOnAction(e -> model.setCurrentScene("search"));
        settingsButton.setOnAction(e -> model.setCurrentScene("profile"));
    }
}
