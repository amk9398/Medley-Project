package gui.scenes;

import gui.model.AppModel;
import gui.ui.widget.AlbumBox;
import gui.ui.AppTheme;
import gui.ui.widget.Histogram;
import gui.ui.widget.ProfileDisplayBox;
import gui.util.AlbumCard;
import javafx.geometry.Insets;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.*;

public class ProfileScene extends AppScene {
    private final VBox histogramPane = new VBox();
    private final Label histogramLabel = new Label();
    private final Histogram ratingHistogram = new Histogram(new CategoryAxis(), new NumberAxis(), "Score", "Frequency");
    private ProfileDisplayBox topAlbumBox = new ProfileDisplayBox();
    private ProfileDisplayBox topArtistBox = new ProfileDisplayBox();

    public ProfileScene(AppModel model, Image profileImage, AppTheme theme) {
        super(model, profileImage, theme);
        setGuiContent();
        setGuiStyle();
        setGuiHierarchy();
        setGuiActions();
        updateProfile();
    }

    @Override
    public void update(AppModel model, String msg) {
        setModel(model);
        switch (msg) {
            case "rate album", "add album to library", "remove album from library" -> updateProfile();
        }
    }

    private void updateProfile() {
        updateTopAlbumBox();
        updateTopArtistBox();
        updateHistogram();
    }

    private void updateTopAlbumBox() {
        List<AlbumCard> albumCards = new ArrayList<>(getModel().libraryCards);
        albumCards.sort((o1, o2) -> (int) (o2.getRating() - o1.getRating()));
        albumCards = albumCards.stream().filter(card -> card.getRating() != 0).toList();
        List<String> albumItems = albumCards.subList(0, Math.min(albumCards.size(), 10)).stream().map(AlbumCard::getAlbumName).toList();
        List<Image> albumImages = albumCards.subList(0, Math.min(albumCards.size(), 3)).stream().map(AlbumCard::getImageURL).map(AlbumBox.imageHashMap::get).toList();
        topAlbumBox = new ProfileDisplayBox(albumItems, albumImages, getTheme(), "Your top albums");
        contentPane.getChildren().set(0, topAlbumBox);
    }

    // todo: rewrite
    private void updateTopArtistBox() {
        List<AlbumCard> albumCards = new ArrayList<>(getModel().libraryCards);
        HashMap<String, String> artistNameMap = new HashMap<>();
        HashMap<String, Double> artistRatingMap = new HashMap<>();
        for (AlbumCard albumCard : albumCards) {
            String artistID = albumCard.getArtistID();
            if (!artistNameMap.containsKey(artistID)) {
                artistNameMap.put(artistID, albumCards.stream().filter(card -> card.getArtistID().equals(artistID))
                        .map(AlbumCard::getArtist).toList().get(0));
                double x = albumCards.stream().filter(card -> card.getArtistID().equals(artistID))
                        .map(AlbumCard::getRating).filter(rating -> rating != 0).mapToDouble(a -> a).average()
                        .orElse(0.0);
                artistRatingMap.put(artistID, x);
            }
        }
        List<Map.Entry<String, Double>> artistRatingList = new ArrayList<>(artistRatingMap.entrySet());
        artistRatingList.sort(((o1, o2) -> (int) (o2.getValue() - o1.getValue())));
        artistRatingList = artistRatingList.stream().filter(entry -> entry.getValue() != 0).toList();
        List<String> topArtistIDs = artistRatingList.stream().map(Map.Entry::getKey).toList().subList(0, Math.min(artistRatingList.size(), 10));
        List<String> artistItems = topArtistIDs.stream().map(artistNameMap::get).toList();
        List<Image> artistImages = topArtistIDs.subList(0, Math.min(artistRatingList.size(), 3)).stream().map(getModel()::getArtistImage).toList();
        topArtistBox = new ProfileDisplayBox(artistItems, artistImages, getTheme(), "Your top artists");
        contentPane.getChildren().set(1, topArtistBox);
    }

    private void updateHistogram() {
        List<Float> ratingList = getModel().libraryCards.stream().map(AlbumCard::getRating).filter(rating -> rating != 0).toList();
        ArrayList<Float> ratingData = new ArrayList<>();
        for (int i = 0; i < 10; i++) ratingData.add(0f);
        for (float rating : ratingList) {
            ratingData.set((int) rating - 1, ratingData.get((int) (rating - 1)) + 1);
        }
        ratingHistogram.addData(ratingData);
    }

    public void setGuiContent() {
        histogramLabel.setText("Your ratings");
    }

    public void setGuiStyle() {
        histogramPane.setBorder(new Border(new BorderStroke(Color.web(getTheme().lightColor), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        histogramPane.setSpacing(15);
        histogramPane.setPadding(new Insets(5, 12, 5, 12));
        histogramPane.setMinHeight(500);
        histogramLabel.setStyle("-fx-text-fill: " + getTheme().extraLightColor);
        histogramLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
    }

    public void setGuiHierarchy() {
        contentPane.getChildren().addAll(topAlbumBox, topArtistBox, histogramPane);
        histogramPane.getChildren().addAll(histogramLabel, ratingHistogram);
    }

    public void setGuiActions() {

    }
}
