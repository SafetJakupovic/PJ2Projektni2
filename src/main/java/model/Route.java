package main.java.model;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Predstavlja izračunatu rutu: listu segmenata, ukupno trajanje, cijenu i broj transfera.
 */
public class Route {
    /**
     * Segmenti rute (svaki segment je jedan {@link RouteSegment}).
     */
    private final List<RouteSegment> segments;
    /**
     * Ukupno trajanje putovanja (suma trajanja + transfer vremena).
     */
    private final Duration totalDuration;
    /**
     * Ukupna cijena svih segmenata.
     */
    private final int totalPrice;
    /**
     * Broj presjedanja (promjena tipa prevoza).
     */
    private final int transfers;

    /**
     * Glavni konstruktor koji automatski računa trajanje, cijenu i transfere na temelju liste segmenata.
     *
     * @param segments lista RouteSegment objekata
     */
    public Route(List<RouteSegment> segments) {
        this.segments = Collections.unmodifiableList(new ArrayList<>(segments));
        Duration durationSum = Duration.ZERO;
        int priceSum = 0;
        int transferCount = 0;

        for (int i = 0; i < segments.size(); i++) {
            RouteSegment seg = segments.get(i);
            durationSum = durationSum.plus(seg.getDeparture().getDuration());
            priceSum += seg.getDeparture().getPrice();
            if (i > 0) {
                TransportType prevType = segments.get(i - 1).getDeparture().getType();
                TransportType currType = seg.getDeparture().getType();
                if (prevType != currType)
                    transferCount++;
                durationSum = durationSum.plus(seg.getDeparture().getMinTransferTime());
            }
        }

        this.totalDuration = durationSum;
        this.totalPrice = priceSum;
        this.transfers = transferCount;
    }

    /**
     * Ručno kreiranje rute sa zadatim vrijednostima.
     * Koristi se kod kada već postoje tačne vrijednosti (npr. iz DijkstraPathFinder).
     */
    public Route(List<RouteSegment> segments, Duration totalDuration, int totalPrice, int transfers) {
        this.segments = Collections.unmodifiableList(new ArrayList<>(segments));
        this.totalDuration = totalDuration;
        this.totalPrice = totalPrice;
        this.transfers = transfers;
    }

    /**
     * Metoda za brojanje transfera u listi segmenata.
     *
     * @param segs lista segmenata
     * @return broj transfera
     */
    public static int computeTransfers(List<RouteSegment> segs) {
        int t = 0;
        for (int i = 1; i < segs.size(); i++) {
            if (segs.get(i).getDeparture().getType() != segs.get(i - 1).getDeparture().getType())
                t++;
        }
        return t;
    }

    /**
     * Vraća segments
     *
     * @return nepromjenjiva lista segmenata
     */
    public List<RouteSegment> getSegments() {
        return segments;
    }

    /**
     * Vraća totalDuration
     *
     * @return ukupno trajanje kao Duration
     */
    public Duration getTotalDuration() {
        return totalDuration;
    }

    /**
     * Vraća totalPrice
     *
     * @return ukupna cijena karte
     */
    public int getTotalPrice() {
        return totalPrice;
    }

    /**
     * Vraća transfers
     *
     * @return broj transfera
     */
    public int getTransfers() {
        return transfers;
    }

    /**
     * Ispis
     *
     * @return ruta kao string
     */
    @Override
    public String toString() {
        return "Route{" +
                "segments=" + segments +
                ", totalDuration=" + totalDuration.toMinutes() + "min" +
                ", totalPrice=" + totalPrice +
                ", transfers=" + transfers +
                '}';
    }

    /**
     * Vraća da li su objekti jednaki
     *
     * @param o drugi objekat
     * @return da li su objekti jednaki
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Route))
            return false;
        Route route = (Route) o;
        return totalPrice == route.totalPrice &&
                transfers == route.transfers &&
                Objects.equals(segments, route.segments) &&
                Objects.equals(totalDuration, route.totalDuration);
    }

    /**
     * Vraća hash kod objekta
     *
     * @return hash kod objekta
     */
    @Override
    public int hashCode() {
        return Objects.hash(segments, totalDuration, totalPrice, transfers);
    }
}
