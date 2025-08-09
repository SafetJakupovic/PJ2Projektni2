package main.java.model;

import java.time.Duration;
import java.time.LocalTime;

/**
 * Predstavlja jednu vožnju između dvije stanice.
 * Sadrži vrijeme polaska, trajanje, cijenu i minimalno vrijeme za transfer.
 */
public class Departure {
    /**
     * Stanica polaska.
     */
    private final Station from;
    /**
     * Stanica dolaska.
     */
    private final Station to;
    /**
     * Tip prevoza (BUS ili RAIL).
     */
    private final TransportType type;
    /**
     * Redovno vrijeme polaska (LocalTime).
     */
    private final LocalTime departureTime;
    /**
     * Koliko minuta traje putovanje.
     */
    private final Duration duration;
    /**
     * Cijena karte u BAM.
     */
    private final int price;
    /**
     * Minimalno vrijeme (u minutima) za presjedanje.
     */
    private final Duration minTransferTime;

    /**
     * Konstruktor.
     *
     * @param from            polazna stanica
     * @param to              odredišna stanica
     * @param type            tip prevoza
     * @param departureTime   lokalno vrijeme polaska
     * @param duration        trajanje vožnje
     * @param price           cijena karte
     * @param minTransferTime minimalno vrijeme čekanja prije transfera
     */
    public Departure(Station from, Station to, TransportType type, LocalTime departureTime, Duration duration, int price,
                     Duration minTransferTime) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.departureTime = departureTime;
        this.duration = duration;
        this.price = price;
        this.minTransferTime = minTransferTime;
    }

    /**
     * Vraća from
     *
     * @return polazna stanica
     */
    public Station getFrom() {
        return from;
    }

    /**
     * Vraća to
     *
     * @return odredišna stanica
     */
    public Station getTo() {
        return to;
    }

    /**
     * Vraća type
     *
     * @return tip prijevoza
     */
    public TransportType getType() {
        return type;
    }

    /**
     * Vraća departureTime
     *
     * @return vrijeme polaska
     */
    public LocalTime getDepartureTime() {
        return departureTime;
    }

    /**
     * Vraća duration
     *
     * @return trajanje vožnje
     */
    public Duration getDuration() {
        return duration;
    }

    /**
     * Vraća price
     *
     * @return cijena karte
     */
    public int getPrice() {
        return price;
    }

    /**
     * Vraća minTransferTime
     *
     * @return minimalno vrijeme transfera
     */
    public Duration getMinTransferTime() {
        return minTransferTime;
    }

    /**
     * Ispis
     *
     * @return Segmenta vožnje kao string
     */
    @Override
    public String toString() {
        return String.format("%s: %s → %s (%s, %d min, %d BAM)", type, from.getCode(), to.getCode(), departureTime,
                duration.toMinutes(), price);
    }
}
