package gui.ui.widget;

import javafx.scene.chart.*;
import java.util.List;

public class Histogram extends BarChart<String, Number> {

    public Histogram(Axis<String> axis, Axis<Number> axis1, String axisLabel, String axis1Label) {
        super(axis, axis1);
        axis.setLabel(axisLabel);
        axis1.setLabel(axis1Label);
    }

    public void addData(List<String> labels, List<Double> data, int bins) {
        XYChart.Series series = new XYChart.Series();
        for (int i = 0; i < bins; i++) {
            series.getData().add(new XYChart.Data(labels.get(i), data.get(i)));
        }
        this.getData().addAll(series);
    }

    public void addData(List<Float> data) {
        this.getData().clear();
        XYChart.Series series = new XYChart.Series();
        for (int i = 0; i < data.size(); i++) {
            series.getData().add(new XYChart.Data(String.valueOf(i+1), data.get(i)));
        }
        this.getData().addAll(series);
    }
}
