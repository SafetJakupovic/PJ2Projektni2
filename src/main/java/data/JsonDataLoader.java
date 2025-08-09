package main.java.data;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.java.model.City;
import main.java.model.Departure;
import main.java.model.Station;
import main.java.model.TransportType;

/**
 * Učitava podatke o državi, stanicama i polascima iz JSON resursa.
 * Parsira JSON fajl u sledeće objekte:
 * matricu {@code City[][] countryMap},
 * listu {@code Station} objekata,
 * listu {@code Departure} objekata.
 */
public class JsonDataLoader {
    /**
     * Učitava i parsira JSON fajl sa putanjom unutar resources direktorijuma.
     *
     * @param filePath putanja unutar classpath-a
     * @return objekat {@link LoadedTransportData} sa mapom gradova, stanicama i polascima
     * @throws IOException ako ne može da se učita ili parsira fajl
     */
    public static LoadedTransportData load(String filePath) throws IOException {
        InputStream is = JsonDataLoader.class.getClassLoader().getResourceAsStream(filePath);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;

            City[][] countryMap = null;
            List<Station> stations = new ArrayList<>();
            List<Departure> departures = new ArrayList<>();
            Map<String, City> cityMap = new HashMap<>();
            Map<String, Station> stationMap = new HashMap<>();
            List<List<City>> tempMap = new ArrayList<>();
            boolean inMap = false;
            boolean inStations = false;
            boolean inDepartures = false;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Prelazak izmedu sekcija map, stanica i polazaka
                if (line.startsWith("\"countryMap\"")) {
                    inMap = true;
                    continue;
                }
                if (line.startsWith("\"stations\"")) {
                    inMap = false;
                    inStations = true;
                    continue;
                }
                if (line.startsWith("\"departures\"")) {
                    inStations = false;
                    inDepartures = true;
                    continue;
                }

                // Parsiranje redova matrice gradova
                if (inMap && line.startsWith("[\"")) {
                    String content = line;
                    content = content.substring(1);
                    if (content.endsWith("],"))
                        content = content.substring(0, content.length() - 2);
                    else if (content.endsWith("]"))
                        content = content.substring(0, content.length() - 1);
                    content = content.trim();

                    String[] names = content.split(",\\s");
                    List<City> row = new ArrayList<>();
                    int rowIndex = tempMap.size();
                    for (int i = 0; i < names.length; i++) {
                        String name = names[i].replace("\"", "");
                        City city = new City(rowIndex, i, name);
                        row.add(city);
                        cityMap.put(name, city);
                    }
                    tempMap.add(row);
                }

                // Parsiranje stanica
                if (inStations && line.startsWith("{") && (line.endsWith("}") || line.endsWith("},"))) {
                    String cleanLine = line.endsWith("},") ? line.substring(0, line.length() - 1) : line;

                    String body = cleanLine.substring(1, cleanLine.length() - 1);
                    Map<String, String> fields = parseFields(body);

                    String cityName = fields.get("city");
                    String busStation = fields.get("busStation");
                    String trainStation = fields.get("trainStation");

                    City city = cityMap.get(cityName);
                    Station bus = new Station(city, busStation, TransportType.BUS);
                    Station rail = new Station(city, trainStation, TransportType.RAIL);

                    stations.add(bus);
                    stations.add(rail);
                    stationMap.put(busStation, bus);
                    stationMap.put(trainStation, rail);
                }

                // Parsiranje polazaka
                if (inDepartures && line.startsWith("{") && (line.endsWith("}") || line.endsWith("},"))) {
                    String cleanLine = line.endsWith("},") ? line.substring(0, line.length() - 1) : line;

                    String body = cleanLine.substring(1, cleanLine.length() - 1);
                    Map<String, String> fields = parseFields(body);

                    String typeStr = fields.get("type");
                    String fromCode = fields.get("from");
                    String toName = fields.get("to");

                    TransportType type = typeStr.equals("autobus") ? TransportType.BUS : TransportType.RAIL;
                    Station from = stationMap.get(fromCode);
                    City toCity = cityMap.get(toName);
                    String prefix = (type == TransportType.BUS) ? "A" : "Z";
                    String toCode = prefix + toCity.getName().substring(1);
                    Station to = stationMap.get(toCode);

                    LocalTime departureTime = LocalTime.parse(fields.get("departureTime"));
                    Duration duration = Duration.ofMinutes(Integer.parseInt(fields.get("duration")));
                    Duration minTransferTime = Duration.ofMinutes(Integer.parseInt(fields.get("minTransferTime")));
                    int price = Integer.parseInt(fields.get("price"));

                    departures.add(new Departure(from, to, type, departureTime, duration, price, minTransferTime));
                }
            }

            // Pretvori privremenu listu u matricu
            int rows = tempMap.size();
            int cols = rows > 0 ? tempMap.get(0).size() : 0;
            countryMap = new City[rows][cols];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    countryMap[i][j] = tempMap.get(i).get(j);
                }
            }

            return new LoadedTransportData(countryMap, stations, departures);
        }
    }

    /**
     * Pomoćna metoda za parsiranje JSON polja oblika "key":"value", ...
     *
     * @param body tekst bez vitičastih zagrada
     * @return mapa kategorija - vrijednost
     */
    private static Map<String, String> parseFields(String body) {
        Map<String, String> map = new HashMap<>();
        String[] pairs = body.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            String key = kv[0].trim().replace("\"", "");
            String val = kv[1].trim();
            if (val.startsWith("\"") && val.endsWith("\"")) {
                val = val.substring(1, val.length() - 1);
            }
            map.put(key, val);
        }
        return map;
    }
}