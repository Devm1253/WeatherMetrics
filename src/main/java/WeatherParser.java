import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public interface WeatherParser {

    static List<WeatherData> read(Path path) throws IOException {
        return parse(Files.readAllLines(path));
    }

    static List<WeatherData> sample() throws IOException {
        try (var input = WeatherParser.class
                .getResourceAsStream("/weatherdata.csv")) {

            String text = new String(
                    input.readAllBytes(), StandardCharsets.UTF_8);
            return parse(text.lines().toList());
        }
    }

    private static List<WeatherData> parse(List<String> lines) {
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("The CSV is empty.");
        }

        if (!lines.getFirst().strip().equalsIgnoreCase(
                "date,temperature,humidity,precipitation")) {
            throw new IllegalArgumentException("Incorrect CSV header.");
        }

        List<WeatherData> days = new ArrayList<>();

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);

            if (line.isBlank()) {
                continue;
            }


            try {
                String[] values = line.split(",", -1);

                if (values.length != 4) {
                    throw new IllegalArgumentException("Expected four values.");
                }

                LocalDate date = LocalDate.parse(values[0].strip());
                double temperature = Double.parseDouble(values[1].strip());
                int humidity = Integer.parseInt(values[2].strip());
                double precipitation = Double.parseDouble(values[3].strip());

                days.add(new WeatherData( date, temperature, humidity, precipitation));

            } catch (IllegalArgumentException
                     | java.time.format.DateTimeParseException error) {
                throw new IllegalArgumentException(
                        "Invalid weather data on row " + (i + 1), error);
            }
        }

        if (days.isEmpty()) {
            throw new IllegalArgumentException("No weather observations.");
        }

        days.sort((x, y ) -> x.date().compareTo(y.date()));
        return days;
    } }