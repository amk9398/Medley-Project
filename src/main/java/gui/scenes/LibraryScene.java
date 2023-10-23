package gui.scenes;

import gui.ui.widget.AlbumBox;
import gui.model.AppModel;
import gui.ui.AppTheme;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class LibraryScene extends AppScene {
    private final HBox albumCardLabels = new HBox();
    private final VBox albumCardBox = new VBox();
    private final Pane emptyPane = new Pane();
    private final HBox albumLabelBox = new HBox();
    private final HBox artistLabelBox = new HBox();
    private final HBox ratingLabelBox = new HBox();
    private final Button albumLabel = new Button();
    private final Button artistLabel = new Button();
    private final Button ratingLabel = new Button();
    private final Region sortCarat = new Region();

    public LibraryScene(AppModel model, Image profileImage, AppTheme theme) {
        super(model, profileImage, theme);
        setGuiContent();
        setGuiStyle();
        setGuiHierarchy();
        setGuiActions();
        updateSortCarat();
        updateAlbumCardBox();
    }

    @Override
    public void update(AppModel model, String msg) {
        setModel(model);
        switch (msg) {
            case "set sort method" -> {
                updateSortCarat();
                updateAlbumCardBox();
            }
            case "add album to library", "remove album from library", "set current library page" -> updateAlbumCardBox();
        }
    }

    private void updateSortCarat() {
        if(getModel().sortOrder.equals("asc")) sortCarat.setStyle("-fx-background-color: " + getTheme().extraLightColor + "; -fx-shape: 'M0 0 L10 0 L5 5 z'; -fx-scale-shape: false");
        else sortCarat.setStyle("-fx-background-color: " + getTheme().extraLightColor + "; -fx-shape: 'M0 0 L10 0 L5 -5 z'; -fx-scale-shape: false");
        albumLabelBox.getChildren().clear();
        artistLabelBox.getChildren().clear();
        ratingLabelBox.getChildren().clear();
        albumLabelBox.getChildren().add(albumLabel);
        artistLabelBox.getChildren().add(artistLabel);
        ratingLabelBox.getChildren().add(ratingLabel);
        switch (getModel().sortMethod) {
            case "name" -> albumLabelBox.getChildren().add(sortCarat);
            case "artist" -> artistLabelBox.getChildren().add(sortCarat);
            case "rating" -> ratingLabelBox.getChildren().add(sortCarat);
            default -> {}
        }
    }

    private void updateAlbumCardBox() {
        albumCardBox.getChildren().clear();
        for (int i = 0; i < getModel().libraryCards.size(); i++) {
            albumCardBox.getChildren().add(new AlbumBox(getModel().libraryCards.get(i), getModel(), getTheme(), false, false, i));
        }
    }

    public void setGuiContent() {
        albumLabel.setText("Name");
        artistLabel.setText("Artist");
        ratingLabel.setText("Rating");
    }

    public void setGuiStyle() {
        albumCardBox.setStyle("-fx-background-color: " + getTheme().mediumColor);
        albumCardLabels.setPrefSize(WIDTH - 230, 25);
        albumCardLabels.setStyle("-fx-background-color: " + getTheme().darkColor);
        emptyPane.setPrefWidth(50);
        albumLabel.setStyle("-fx-background-color: " + getTheme().darkColor + "; -fx-text-fill: " + getTheme().extraLightColor + "; -fx-font-weight: bold; -fx-font-size: 12pt; -fx-alignment: top_left");
        artistLabel.setStyle("-fx-background-color: " + getTheme().darkColor + "; -fx-text-fill: " + getTheme().extraLightColor + "; -fx-font-weight: bold; -fx-font-size: 12pt; -fx-alignment: top_left");
        ratingLabel.setStyle("-fx-background-color: " + getTheme().darkColor + "; -fx-text-fill: "  + getTheme().extraLightColor + "; -fx-font-weight: bold; -fx-font-size: 12pt; -fx-alignment: top_left");
        albumLabelBox.setPrefWidth(365);
        albumLabelBox.setPadding(new Insets(0, 0, 0, 75));
        artistLabelBox.setPrefWidth(185);
        ratingLabelBox.setPrefWidth(150);
    }

    public void setGuiHierarchy() {
        contentPane.getChildren().addAll(albumCardLabels, albumCardBox);
        albumCardLabels.getChildren().addAll(emptyPane, albumLabelBox, artistLabelBox, ratingLabelBox);
        albumLabelBox.getChildren().add(albumLabel);
        artistLabelBox.getChildren().add(artistLabel);
        ratingLabelBox.getChildren().add(ratingLabel);
    }

    public void setGuiActions() {
        albumLabel.hoverProperty().addListener((observable, oldValue, newValue) -> albumLabel.setUnderline(newValue));
        artistLabel.hoverProperty().addListener((observable, oldValue, newValue) -> artistLabel.setUnderline(newValue));
        ratingLabel.hoverProperty().addListener((observable, oldValue, newValue) -> ratingLabel.setUnderline(newValue));
        albumLabel.setOnAction(e -> getModel().setSortMethod("name"));
        artistLabel.setOnAction(e -> getModel().setSortMethod("artist"));
        ratingLabel.setOnAction(e -> getModel().setSortMethod("rating"));
    }
}
