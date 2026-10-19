import java.time.LocalDate;

public record WeatherData(
        LocalDate date,
        double temperature,
        int humidity,
        double precipitation
) {

public WeatherData {
    if (humidity < 0 || humidity > 100|| precipitation < 0) {
        throw new IllegalArgumentException("Weather values r not accepted");
    }

}public String calculatedCondition() {
    if (precipitation > 0) {
        return temperature <= 0 ? "Snowy" : "Rainy";
    }

    if (humidity >= 95) {
        return "Foggy";
    }

    if (humidity >= 75) {
        return "Cloudy";
    }

    return "Sunny";
}

    }
