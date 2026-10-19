import javafx.application.Platform;

public record WeatherApp() {
    public static void main(String[] args) {
        Platform.startup(() -> {
            WeatherDashboard.open();
        });
    }
}
