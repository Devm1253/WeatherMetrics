import java.util.List;
import java.util.Locale;

public interface WeatherAnalytics {

    static List<WeatherData> filter(
            List<WeatherData> days, int year, int month) {

        return days.stream()
                .filter(day -> day.date().getYear() == year)
                .filter(day -> month == 0
                        || day.date().getMonthValue() == month)
                .toList();
    }

    static double average(List<WeatherData> days) {
        return days.stream()
                .mapToDouble(day -> day.temperature())
                .average()
                .orElse(0);
    }

    static double highest(List<WeatherData> days) {
        return days.stream()
                .mapToDouble(day -> day.temperature())
                .max()
                .orElse(0);
    }

    static double lowest(List<WeatherData> days) {
        return days.stream()
                .mapToDouble(day -> day.temperature())
                .min()
                .orElse(0);
    }

    static long above(List<WeatherData> days, double threshold) {
        return days.stream()
                .filter(day -> day.temperature() > threshold)
                .count();
    }

    static long rainyDays(List<WeatherData> days) {
        return days.stream()
                .filter(day -> day.precipitation() > 0)
                .count();
    }

    static double humidity(List<WeatherData> days) {
        return days.stream()
                .mapToInt(day -> day.humidity())
                .average()
                .orElse(0);
    }

    static double precipitation(List<WeatherData> days) {
        return days.stream()
                .mapToDouble(day -> day.precipitation())
                .sum();
    }

    static String number(double value) {
        return String.format(Locale.US, "%.1f", value);
    }

    static String summary(List<WeatherData> days) {
        return """
                Historical observations: %d
                Rainy days: %d
                Temperatures: Celsius. Rainfall: millimeters.
                """.formatted(days.size(), rainyDays(days));
    }
}