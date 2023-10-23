package gui.scenes;

import gui.model.AppModel;
import gui.ui.widget.AlbumBox;
import gui.ui.AppTheme;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SearchScene extends AppScene {
    private final HBox searchBox = new HBox();
    private final TextField searchField = new TextField();
    private final Button searchButton = new Button("Search");
    private final Button mergeButton = new Button();
    private final VBox searchList = new VBox();

    public SearchScene(AppModel model, Image profileImage, AppTheme theme) {
        super(model, profileImage, theme);
        setGuiContent();
        setGuiStyle();
        setGuiHierarchy();
        setGuiActions();
        updateSearchList();
    }

    @Override
    public void update(AppModel model, String msg) {
        setModel(model);
        if (msg.equals("update search cards")) {
            updateSearchList();
        }
    }

    private void updateSearchList() {
        searchList.getChildren().clear();
        for (int i = 0; i < getModel().searchCards.size(); i++) {
            boolean cardAdded = getModel().libraryCards.contains(getModel().searchCards.get(i));
            searchList.getChildren().add(new AlbumBox(getModel().searchCards.get(i), getModel(), getTheme(), true, cardAdded, i));
        }
    }

    private void setGuiContent() {
        mergeButton.setText("Merge Spotify Library");
        searchButton.setText("Search");
    }

    private void setGuiStyle() {
        searchBox.setAlignment(Pos.TOP_CENTER);
        searchBox.setStyle("-fx-background-color: " + getTheme().mediumColor);
        searchBox.setSpacing(10);
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-background-color: " + getTheme().extraLightColor);
        searchButton.setStyle("-fx-background-color: " + getTheme().extraLightColor);
        mergeButton.setPrefSize(60, 60);
        mergeButton.setWrapText(true);
        mergeButton.setEffect(new DropShadow());
        mergeButton.setStyle("-fx-background-color: " + getTheme().contrastColor + "; -fx-text-fill: " + getTheme().extraLightColor);
        searchList.setStyle("-fx-background-color: " + getTheme().mediumColor);
    }

    private void setGuiHierarchy() {
        contentPane.getChildren().addAll(searchBox, searchList);
        searchBox.getChildren().addAll(searchField, searchButton, mergeButton);
    }

    private void setGuiActions() {
        mergeButton.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) mergeButton.setStyle("-fx-background-color: " + getTheme().contrastColorHighlight +
                    "; -fx-text-fill: " + getTheme().extraLightColor);
            else mergeButton.setStyle("-fx-background-color: " + getTheme().contrastColor +
                    "; -fx-text-fill: " + getTheme().extraLightColor);
        });
        searchButton.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) searchButton.setStyle("-fx-background-color: " + getTheme().extraLightColorHighlight);
            else searchButton.setStyle("-fx-background-color: " + getTheme().extraLightColor);
        });
        searchButton.setOnAction(e -> getModel().updateSearchCards(searchField.getText()));
        mergeButton.setOnAction(e -> getModel().mergeSpotifyLibrary());
    }
}
