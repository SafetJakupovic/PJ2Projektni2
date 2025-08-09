package main.java.service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servis za skupljanje statistike o prodatim kartama:
 * ukupan broj računa i ukupna zarada iz tekstualnih fajlova.
 */
public class SalesStatsService {
    /**
     * Direktorijum u kojem se nalaze .txt računi
     */
    private static final Path RECIEPTS_DIR = Paths.get("racuni");

    /**
     * Broji koliko je fajlova-računa (*.txt) u direktorijumu.
     *
     * @return ukupan broj prodatih karata (računa)
     */
    public int getTotalTicketsSold() {
        try {
            if (!Files.exists(RECIEPTS_DIR))
                return 0;
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(RECIEPTS_DIR, "*.txt")) {
                int count = 0;
                for (Path p : ds)
                    count++;
                return count;
            }
        } catch (IOException e) {
            throw new RuntimeException("Greska pri ucitavanju računa", e);
        }
    }

    /**
     * Sabira vrednosti „Ukupna cijena:“ iz svakog fajla-računa.
     *
     * @return ukupna zarada (suma cijena iz svih računa)
     */
    public long getTotalRevenue() {
        if (!Files.exists(RECIEPTS_DIR))
            return 0;
        try {
            long sum = 0;
            List<Path> files = Files.list(RECIEPTS_DIR)
                    .filter(p -> p.toString().endsWith(".txt"))
                    .collect(Collectors.toList());
            for (Path file : files) {
                List<String> lines = Files.readAllLines(file);
                for (String line : lines) {
                    if (line.startsWith("Ukupna cijena:")) {
                        String[] parts = line.split(":")[1].trim().split(" ");
                        long price = Long.parseLong(parts[0]);
                        sum += price;
                        break;
                    }
                }
            }
            return sum;
        } catch (IOException e) {
            throw new RuntimeException("Greska pri ucitavanju računa", e);
        }
    }
}