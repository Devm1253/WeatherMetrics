# WeatherMetrics

My CSC 311 project analyzes historical weather CSV files in one JavaFX window.
The included 2024–2025 data is made-up sample data, not real observations or forecasts.

## Run on Mac

Install JDK 23 or newer and Maven. In the project folder, run:

```sh
mvn compile javafx:run
```

The sample data loads automatically. Choose a year and month (or Entire year),
and enter a Celsius threshold. Results update automatically. Import CSV replaces
the sample data; a bad import leaves the previous dataset in place.

## Data and calculations

```csv
date,min_temp,max_temp,humidity,precipitation,condition
2025-10-15,12,20,65,0.0,Sunny
2025-10-16,10,18,78,0.0,Cloudy
```

Use unquoted comma-separated values, one unique date per row. Conditions are
Sunny, Cloudy, Rainy, Snowy, or Foggy. Humidity must be 0–100; rainfall must be
nonnegative. Blank rows are skipped. Invalid rows show an error with a row number.

The six cards show average temperature, highest maximum, lowest minimum, days
whose maximum is strictly above the threshold, average humidity, and total
precipitation. Daily average means `(minimum + maximum) / 2`; monthly and yearly
averages give each observation equal weight. Rainy days (> 0 mm) appear below
the report. Charts use daily readings for a month and monthly averages/totals
for an entire year. Months without observations are omitted.

The original `Date,Temperature,Humidity,Precipitation` format also works. Its
single temperature is used directly; no daily min/max is invented. Conditions
appear as Unknown. Empty periods show N/A for averages and extrema, and zero
for counts and rainfall totals.

The 7-Day Historical Report shows up to seven sorted observations, their date,
condition image, and maximum Celsius temperature (or the sole original-format
reading). The arrows move seven records at a time within the selected period.
Missing dates are not filled in; these cards are historical records, not forecasts.

## Weather images

Put `sunny.png`, `cloudy.png`, `rainy.png`, `snowy.png`, and `foggy.png` in
`src/main/resources/images/`, then rebuild. Missing or unreadable images show
plain condition text. No images need to be downloaded for the app to run.

## Java features

The five existing Java files use records and interfaces, streams and lambdas,
an enhanced switch for condition names, a text-block summary, and Markdown
Javadoc. `Platform.startup` starts JavaFX, so no conventional Application
subclass is needed. Maven compiles for Java 23. There are no tests or CI workflows.

## Windows launcher

A Windows executable has **not** been built or verified on this Mac.
Run the following in PowerShell on Windows with JDK 23+ and Maven installed:

```powershell
mvn clean compile dependency:copy-dependencies "-DincludeScope=runtime" "-DoutputDirectory=target/package-input"
jar --create --file target/package-input/WeatherMetrics.jar -C target/classes .
jpackage --type app-image --name WeatherMetrics --app-version 1.0 --input target/package-input --main-jar WeatherMetrics.jar --main-class com.weatherwise.WeatherApp --dest target/windows
.\target\windows\WeatherMetrics\WeatherMetrics.exe
```

This builds a folder containing an `.exe` launcher and its runtime, without a
third-party installer tool. Keep the whole folder together. Check sample loading,
CSV import, filters, charts, and both history arrows on Windows before sharing it.
After checking it, zip that whole folder for a portable release. A full `--type exe`
installer additionally needs WiX; the app-image route is simpler.
[Oracle's jpackage guide](https://docs.oracle.com/en/java/javase/25/jpackage/packaging-overview.html)
explains the platform requirements. Do not publish a release until approved.
