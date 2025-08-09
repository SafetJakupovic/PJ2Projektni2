package main.java.service;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyLongWrapper;
import main.java.model.Route;

/**
 * DTO za prikaz top N ruta u posebnom prozoru.
 * Sadrži redni broj (rank), trajanje, stvarno trajanje (ako se razlikuje),
 * cijenu, broj transfera i samu Route.
 */
public class RouteSummary {
    /**
     * Redni broj rute u top-listi (1..N).
     */
    private final ReadOnlyIntegerWrapper rank;
    /**
     * Teorijsko ukupno trajanje rute (Duration).
     */
    private final ReadOnlyLongWrapper duration;
    /**
     * Ukupna cijena rute.
     */
    private final ReadOnlyIntegerWrapper price;
    /**
     * Broj presjedanja rute.
     */
    private final ReadOnlyIntegerWrapper transfers;
    /**
     * Sam objekat rute, potreban za kasniju kupovinu.
     */
    private final Route route;
    /**
     * Pravi (apsolutni) broj minuta putovanja za top-rute.
     */
    private final ReadOnlyLongWrapper actualDuration;

    /**
     * Konstruktor koji postavlja rank i koristi standardno (teoretsko)
     * trajanje iz Route.getTotalDuration().
     *
     * @param rank  redni broj rute u top listi
     * @param route instanca pronađene rute
     */
    public RouteSummary(int rank, Route route) {
        this(rank, route, route.getTotalDuration().toMinutes());
    }

    /**
     * Konstruktor s dodatkom stvarnog (apsolutnog) trajanja u minutama.
     *
     * @param rank              redni broj
     * @param route             ruta
     * @param actualDurationMin stvarno trajanje uključujući čekanja
     */
    public RouteSummary(int rank, Route route, long actualDurationMin) {
        this.rank = new ReadOnlyIntegerWrapper(rank);
        this.duration = new ReadOnlyLongWrapper(route.getTotalDuration().toMinutes());
        this.actualDuration = new ReadOnlyLongWrapper(actualDurationMin);
        this.price = new ReadOnlyIntegerWrapper(route.getTotalPrice());
        this.transfers = new ReadOnlyIntegerWrapper(route.getTransfers());
        this.route = route;
    }

    /**
     * Vraća actualDuration
     *
     * @return stvarno trajanje (apsolutno)
     */
    public ReadOnlyLongWrapper actualDurationProperty() {
        return actualDuration;
    }

    public int getRank() {
        return rank.get();
    }

    /**
     * Vraća rank
     *
     * @return redni broj rute
     */
    public ReadOnlyIntegerProperty rankProperty() {
        return rank;
    }

    public long getDuration() {
        return duration.get();
    }

    /**
     * Vraća duration
     *
     * @return trajanje (prema sumi segmenta)
     */
    public ReadOnlyLongProperty durationProperty() {
        return duration;
    }

    public int getPrice() {
        return price.get();
    }

    /**
     * Vraća price
     *
     * @return cijena karte
     */
    public ReadOnlyIntegerProperty priceProperty() {
        return price;
    }

    public int getTransfers() {
        return transfers.get();
    }

    /**
     * Vraća transfer
     *
     * @return broj transfera
     */
    public ReadOnlyIntegerProperty transfersProperty() {
        return transfers;
    }

    /**
     * Vraća route
     *
     * @return originalna Route instanca
     */
    public Route getRoute() {
        return route;
    }
}
