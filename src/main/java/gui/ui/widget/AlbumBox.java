package gui.ui.widget;

import gui.ui.AppTheme;
import gui.util.AlbumCard;
import gui.model.AppModel;
import gui.util.ImageTools;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Callback;

import java.io.IOException;
import java.util.HashMap;

public class AlbumBox extends HBox {
    public static HashMap<String, Image> imageHashMap = new HashMap<>();

    public AlbumBox(AlbumCard albumCard, AppModel model, AppTheme theme, boolean isSearch, boolean cardAdded, int cardNum) {
        ImageView imageView = new ImageView();
        try {
            Image image;
            if(imageHashMap.containsKey(albumCard.getImageURL())) {
                image = imageHashMap.get(albumCard.getImageURL());
            } else {
                image = ImageTools.retrieveImage(albumCard.getImageURL());
                imageHashMap.put(albumCard.getImageURL(), image);
            }
            imageView.setImage(image);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        ObservableList<String> ratingOptions = FXCollections.observableArrayList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");

        HBox leftBox = new HBox();
        HBox rightBox = new HBox();
        Label numberLabel = new Label(String.valueOf(++cardNum));
        Label albumName = new Label(albumCard.getAlbumName());
        Button artistName = new Button(albumCard.getArtist());
        Button addButton = new Button("Add to Library");
        Button removeAlbum = new Button("Remove");
        ComboBox<String> ratingDropDown = new ComboBox<>(ratingOptions);

        this.setBorder(new Border(new BorderStroke(Color.web(theme.lightColor), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        this.setPadding(new Insets(5, 12, 5, 12));
        leftBox.setSpacing(25);
        leftBox.setPrefWidth(400);
        rightBox.setSpacing(50);
        rightBox.setAlignment(Pos.CENTER_LEFT);
        imageView.setFitHeight(50);
        imageView.setFitWidth(50);
        numberLabel.setMinWidth(20);
        numberLabel.setStyle("-fx-text-fill: " + theme.extraLightColor);
        numberLabel.setFont(Font.font("Verdana", FontWeight.NORMAL, 15));
        albumName.setWrapText(true);
        albumName.setStyle("-fx-text-fill: " + theme.extraLightColor);
        albumName.setFont(Font.font("Verdana", FontWeight.NORMAL, 15));
        artistName.setPrefWidth(150);
        artistName.setAlignment(Pos.CENTER_LEFT);
        artistName.setWrapText(true);
        artistName.setStyle("-fx-background-color: " + theme.mediumColor + "; -fx-text-fill: " + theme.extraLightColor);
        artistName.setFont(Font.font("Verdana", FontWeight.NORMAL, 15));
        if(cardAdded) {
            addButton.setText("Added");
            addButton.setStyle("-fx-background-color: " + theme.darkColorHighlight + "; -fx-text-fill: " + theme.extraLightColor);
        } else addButton.setStyle("-fx-background-color: " + theme.darkColor + "; -fx-text-fill: " + theme.extraLightColor);
        addButton.setFont(Font.font("Verdana", FontWeight.NORMAL, 12));
        removeAlbum.setStyle("-fx-background-color: " + theme.darkColor + "; -fx-text-fill: " + theme.extraLightColor);
        removeAlbum.setFont(Font.font("Verdana", FontWeight.NORMAL, 12));
        ratingDropDown.setValue(albumCard.getRating() == 0  ? " " : String.valueOf((int) albumCard.getRating()));
        ratingDropDown.setStyle("-fx-background-color: " + theme.extraLightColor);

        this.getChildren().addAll(leftBox, rightBox);
        leftBox.getChildren().addAll(numberLabel, imageView, albumName);
        if(!isSearch) rightBox.getChildren().addAll(artistName, ratingDropDown, removeAlbum);
        else rightBox.getChildren().addAll(artistName, addButton);

        artistName.hoverProperty().addListener((observable, oldValue, newValue) -> {
            artistName.setUnderline(newValue);
        });
        addButton.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if(addButton.getText().equals("Add to Library")) {
                if (newValue) addButton.setStyle("-fx-background-color: " + theme.darkColorHighlight + "; -fx-text-fill: " + theme.extraLightColor);
                else addButton.setStyle("-fx-background-color: " + theme.darkColor + "; -fx-text-fill: " + theme.extraLightColor);
            }
        });
        removeAlbum.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) removeAlbum.setStyle("-fx-background-color: " + theme.darkColorHighlight + "; -fx-text-fill: " + theme.extraLightColor);
            else removeAlbum.setStyle("-fx-background-color: " + theme.darkColor + "; -fx-text-fill: " + theme.extraLightColor);
        });

        artistName.setOnAction(e -> {});

        addButton.setOnAction(e -> {
            model.addAlbumToLibrary(albumCard);
            addButton.setText("Added");
            addButton.setStyle("-fx-background-color: " + theme.darkColorHighlight + "; -fx-text-fill: " + theme.extraLightColor);
        });

        removeAlbum.setOnAction(e -> {
            model.removeAlbumFromLibrary(albumCard.getAlbumID());
        });

        ratingDropDown.setOnAction(e -> {
            int rating = Integer.parseInt(String.valueOf(ratingDropDown.getValue()));
            model.rateAlbum(albumCard.getAlbumID(), rating);
        });
    }
}
