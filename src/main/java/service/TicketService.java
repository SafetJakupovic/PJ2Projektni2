package main.java.service;

import main.java.model.Departure;
import main.java.model.Route;
import main.java.model.RouteSegment;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Servis za kupovinu karte: generiše tekstualni račun i čuva ga.
 */
public class TicketService {
    /**
     * Folder u koji se čuvaju tekstualni računi.
     */
    private static final String RECEIPTS_DIR = "racuni";
    /**
     * Format za datum u nazivu fajla i u računu.
     */
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /**
     * Format za vrijeme izdavanja karte.
     */
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Kreira direktorijum za račune ako ne postoji.
     */
    public TicketService() {
        Path path = Paths.get(RECEIPTS_DIR);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException("Nemoguce kreirati direktorijum za racune", e);
            }
        }
    }

    /**
     * Generiše i snima račun za datu rutu.
     *
     * @param route          ruta koja se kupuje
     * @param actualDuration stvarno trajanje putovanja u minutama
     * @return Path do kreiranog fajla-računa
     * @throws IOException ako ne može da piše fajl
     */
    public Path purchase(Route route, long actualDuration) throws IOException {
        String filename = "racun_" + LocalDate.now().format(DATE_FORMAT) + "_" + UUID.randomUUID().toString().substring(0, 8) + ".txt";
        Path receiptPath = Paths.get(RECEIPTS_DIR, filename);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(receiptPath.toFile()))) {
            writer.write("--- Račun za kupovinu karte ---");
            writer.newLine();
            writer.write("Datum: " + LocalDate.now().format(DATE_FORMAT));
            writer.newLine();
            writer.write("Vrijeme izdavanja: " + LocalTime.now().format(TIME_FORMAT));
            writer.newLine();
            writer.newLine();
            writer.write("Relacija: ");
            writer.newLine();

            List<RouteSegment> segments = route.getSegments();
            for (RouteSegment segment : segments) {
                Departure d = segment.getDeparture();
                if (!d.getFrom().getCity().equals(d.getTo().getCity()))
                    writer.write(String.format(" - %s -> %s (polazak %s, trajanje %d min, %d BAM)",
                            d.getFrom().getCode(),
                            d.getTo().getCode(),
                            d.getDepartureTime().format(TIME_FORMAT),
                            d.getDuration().toMinutes(),
                            d.getPrice()));
                else
                    writer.write(String.format(" - %s -> %s (trajanje %d min)",
                            d.getFrom().getCode(),
                            d.getTo().getCode(),
                            d.getDuration().toMinutes()));
                writer.newLine();
            }
            writer.newLine();
            writer.write(String.format("Ukupno vrijeme putovanja: %dh %dm", actualDuration / 60, actualDuration % 60));
            writer.newLine();
            writer.write(String.format("Ukupna cijena: %d BAM", route.getTotalPrice()));
            writer.newLine();
            writer.write(String.format("Broj presjedanja: %d", route.getTransfers()));
            writer.newLine();
        }
        return receiptPath;
    }

    /**
     * Preklopna metoda koja koristi ukupno trajanje iz Route objekta.
     * Kompatibilnosti radi.
     *
     * @param route ruta
     * @return poziv purchse metode
     * @throws IOException problemi sa fajlom
     */
    public Path purchase(Route route) throws IOException {
        return purchase(route, route.getTotalDuration().toMinutes());
    }
}