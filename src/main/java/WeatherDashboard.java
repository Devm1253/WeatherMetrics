import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public record WeatherDashboard(
        Stage stage,
        List<WeatherData> data,
        SimpleIntegerProperty page,
        ComboBox<Integer> year,
        ComboBox<String> month,
        TextField threshold,
        Label[] values,
        LineChart<String, Number> temperatures,
        BarChart<String, Number> rainfall,
        HBox week,
        Button previous,
        Button next,
        Label message
) {

    public static void open() {
        var dashboard = new WeatherDashboard(
                new Stage(), new ArrayList<>(),
                new SimpleIntegerProperty(0),
                new ComboBox<>(), new ComboBox<>(),
                new TextField("30"), new Label[6],
                new LineChart<>(new CategoryAxis(), new NumberAxis()),
                new BarChart<>(new CategoryAxis(), new NumberAxis()),
                new HBox(10), new Button("<"), new Button(">"),
                new Label()
        );

        dashboard.build();
    }

    private void build() {
        month.getItems().add("Entire year");

        for (Month m : Month.values()) {
            String name = m.toString().toLowerCase(Locale.ROOT);
            month.getItems().add(
                    Character.toUpperCase(name.charAt(0)) + name.substring(1));
        }

        month.getSelectionModel().select(0);
        threshold.setPrefWidth(65);

        year.setOnAction(event -> reset());
        month.setOnAction(event -> reset());
        threshold.textProperty().addListener(
                (observable, oldValue, newValue) -> refresh());

        previous.setOnAction(event -> {
            page.set(page.get() - 1);
            refresh();
        });

        next.setOnAction(event -> {
            page.set(page.get() + 1);
            refresh();
        });

        var importButton = new Button("Import CSV");
        importButton.getStyleClass().add("import-button");

        importButton.setOnAction(event -> {
            var chooser = new FileChooser();
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV files", "*.csv"));

            var file = chooser.showOpenDialog(stage);

            if (file != null) {
                try {
                    load(WeatherParser.read(file.toPath()), file.getName());
                } catch (Exception error) {
                }
            }
        });

        var title = new Label("WeatherMetrics");
        title.getStyleClass().add("title");

        var spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        var header = new HBox(12, title, spacer, year, month,
                new Label("Threshold:"), threshold,
                new Label("°C"), importButton);
        header.setAlignment(Pos.CENTER_LEFT);

        var statistics = new HBox(10);
        String[] names = {
                "Average Temperature", "Highest Temperature",
                "Lowest Temperature", "Days Above Threshold",
                "Average Humidity", "Total Precipitation"
        };

        for (int i = 0; i < names.length; i++) {
            values[i] = new Label("0");
            values[i].getStyleClass().add("stat-value");

            var name = new Label(names[i]);
            name.setWrapText(true);

            var card = new VBox(8, name, values[i]);
            card.getStyleClass().addAll("stat-card", "stat-" + i);
            card.setPrefWidth(220);
            card.setMinWidth(0);
            HBox.setHgrow(card, Priority.ALWAYS);
            statistics.getChildren().add(card);
        }

        temperatures.setLegendVisible(false);
        temperatures.setAnimated(false);
        rainfall.setLegendVisible(false);
        rainfall.setAnimated(false);

        temperatures.getYAxis().setLabel("Temperature (°C)");
        rainfall.getYAxis().setLabel("Precipitation (mm)");

        var graphs = new HBox(12,
                panel("Temperature Trend", temperatures),
                panel("Precipitation Trend", rainfall));

        var navigation = new HBox(10, previous, next);
        var report = panel("7-Day Historical Report",
                new VBox(10, navigation, week));

        var root = new VBox(18,
                header, message, statistics, graphs, report);
        root.setPadding(new Insets(20));
        root.setMinWidth(1050);

        var scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);

        var scene = new Scene(scroll, 1440, 850);
        scene.getStylesheets().add(
                WeatherDashboard.class.getResource("/styles.css")
                        .toExternalForm());

        stage.setTitle("WeatherMetrics");
        stage.setScene(scene);
        stage.show();

        try {
            load(WeatherParser.sample(), "Sample historical data");
        } catch (Exception error) {
        }
    }

    private VBox panel(String title, Node content) {
        var heading = new Label(title);
        heading.getStyleClass().add("section-title");

        var box = new VBox(12, heading, content);
        box.getStyleClass().add("panel");
        box.setMinWidth(0);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private void load(List<WeatherData> loaded, String name) {
        data.clear();
        data.addAll(loaded);

        year.getItems().clear();
        year.getItems().addAll(data.stream()
                .map(day -> day.date().getYear())
                .distinct()
                .toList());

        var latest = data.getLast().date();
        year.setValue(latest.getYear());
        month.getSelectionModel().select(latest.getMonthValue());
        message.setText(name);
        reset();
    }

    private void reset() {
        page.set(0);
        refresh();
    }

    /// Updates the display when a filter or history page changes.
    private void refresh() {
        if (year.getValue() == null) {
            return;
        }

        int selectedMonth = month.getSelectionModel().getSelectedIndex();
        var days = WeatherAnalytics.filter(
                data, year.getValue(), selectedMonth);

        values[0].setText(WeatherAnalytics.number(
                WeatherAnalytics.average(days)) + "°C");
        values[1].setText(WeatherAnalytics.number(
                WeatherAnalytics.highest(days)) + "°C");
        values[2].setText(WeatherAnalytics.number(
                WeatherAnalytics.lowest(days)) + "°C");
        values[4].setText(WeatherAnalytics.number(
                WeatherAnalytics.humidity(days)) + "%");
        values[5].setText(WeatherAnalytics.number(
                WeatherAnalytics.precipitation(days)) + " mm");

        try {
            double limit = Double.parseDouble(threshold.getText());
            values[3].setText(
                    Long.toString(WeatherAnalytics.above(days, limit)));
        } catch (NumberFormatException error) {
            values[3].setText("Enter a number");
        }

        var temperatureSeries = new XYChart.Series<String, Number>();
        var rainfallSeries = new XYChart.Series<String, Number>();

        if (selectedMonth == 0) {
            for (Month m : Month.values()) {
                var monthly = days.stream()
                        .filter(day -> day.date().getMonth() == m)
                        .toList();

                if (!monthly.isEmpty()) {
                    String label = m.toString().substring(0, 3);
                    temperatureSeries.getData().add(new XYChart.Data<>(
                            label, WeatherAnalytics.average(monthly)));
                    rainfallSeries.getData().add(new XYChart.Data<>(
                            label, WeatherAnalytics.precipitation(monthly)));
                }
            }
        } else {
            for (var day : days) {
                String label = Integer.toString(day.date().getDayOfMonth());
                temperatureSeries.getData().add(
                        new XYChart.Data<>(label, day.temperature()));
                rainfallSeries.getData().add(
                        new XYChart.Data<>(label, day.precipitation()));
            }
        }

        temperatures.getData().setAll(List.of(temperatureSeries));
        rainfall.getData().setAll(List.of(rainfallSeries));

        week.getChildren().clear();
        int start = page.get() * 7;
        int end = Math.min(start + 7, days.size());

        for (int i = start; i < end; i++) {
            var day = days.get(i);
            var card = new VBox(8,
                    new Label(day.date().toString()),
                    weatherImage(day.calculatedCondition()),
                    new Label(WeatherAnalytics.number(
                            day.temperature()) + "°C"));

            card.setAlignment(Pos.CENTER);
            card.getStyleClass().add("day-card");
            card.setPrefWidth(180);
            HBox.setHgrow(card, Priority.ALWAYS);
            week.getChildren().add(card);
        }

        if (days.isEmpty()) {
            week.getChildren().add(new Label("No historical records."));
        }

        previous.setDisable(page.get() == 0);
        next.setDisable(end >= days.size());

        stage.setTitle("WeatherMetrics");
    }

    private Node weatherImage(String condition) {
        var resource = WeatherDashboard.class.getResource(
                "/images/" + condition.toLowerCase(Locale.ROOT) + ".png");

        if (resource != null) {
            var image = new Image(resource.toExternalForm());

            if (!image.isError()) {
                var view = new ImageView(image);
                view.setFitWidth(60);
                view.setFitHeight(50);
                view.setPreserveRatio(true);
                return view;
            }
        }

        return new Label(condition);
    }

}