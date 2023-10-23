package gui.ui.widget;

import gui.ui.AppTheme;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class ProfileDisplayBox extends VBox {
    public ProfileDisplayBox() {super();}

    public ProfileDisplayBox(List<String> items, List<Image> images, AppTheme theme, String headingText) {
        Label headingLabel = new Label(headingText);
        HBox topThreeBox = new HBox();
        VBox topTenBox = new VBox();

        this.setBorder(new Border(new BorderStroke(Color.web(theme.lightColor), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        this.setSpacing(15);
        this.setPadding(new Insets(5, 12, 5, 12));
        headingLabel.setStyle("-fx-text-fill: " + theme.extraLightColor);
        headingLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        topThreeBox.setSpacing(10);
        topTenBox.setSpacing(5);

        for (int i = 0; i < 3; i++) {
            if (i >= images.size()) break;

            HBox displayBox = new HBox();
            VBox imageBox = new VBox();
            Label numLabel = new Label("#" + (i+1));
            Label displayLabel = new Label(items.get(i));
            ImageView imageView = new ImageView(images.get(i));

            displayBox.setSpacing(15);
            imageBox.setSpacing(5);
            numLabel.setStyle("-fx-text-fill: " + theme.extraLightColor);
            numLabel.setFont(Font.font("Verdana", FontWeight.NORMAL, 20));
            displayLabel.setStyle("-fx-text-fill: " + theme.extraLightColor);
            displayLabel.setFont(Font.font("Verdana", FontWeight.NORMAL, 16));
            displayLabel.setWrapText(true);
            displayLabel.setMaxWidth(200);
            imageView.setFitWidth(200);
            imageView.setFitHeight(200);

            topThreeBox.getChildren().add(displayBox);
            displayBox.getChildren().addAll(numLabel, imageBox);
            imageBox.getChildren().addAll(imageView, displayLabel);
        }

        for (int i = 3; i < 10; i++) {
            if (i >= items.size()) break;

            HBox listBox = new HBox();
            Label numLabel = new Label("#" + (i+1) + ":");
            Label itemLabel = new Label(items.get(i));

            listBox.setSpacing(5);
            numLabel.setStyle("-fx-text-fill: " + theme.extraLightColor);
            numLabel.setFont(Font.font("Verdana", FontWeight.NORMAL, 15));
            itemLabel.setStyle("-fx-text-fill: " + theme.extraLightColor);
            itemLabel.setFont(Font.font("Verdana", FontWeight.NORMAL, 15));

            topTenBox.getChildren().add(listBox);
            listBox.getChildren().addAll(numLabel, itemLabel);
        }

        this.getChildren().addAll(headingLabel, topThreeBox, topTenBox);
    }
}
