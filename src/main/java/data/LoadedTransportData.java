package main.java.data;

import java.util.List;

import main.java.model.City;
import main.java.model.Departure;
import main.java.model.Station;

/**
 * Jednostavan kontejner za rezultate parsiranja JSON-a.
 */
public class LoadedTransportData {
    /**
     * Matrica gradova
     */
    private final City[][] countryMap;
    /**
     * Lista svih stanica
     */
    private final List<Station> stations;
    /**
     * Lista svih odlazaka
     */
    private final List<Departure> departures;

    /**
     * @param countryMap matrica {@link City} objekata dimenzija n x m
     * @param stations   lista {@link Station} objekata
     * @param departures lista {@link Departure} objekata
     */
    public LoadedTransportData(City[][] countryMap, List<Station> stations, List<Departure> departures) {
        this.countryMap = countryMap;
        this.stations = stations;
        this.departures = departures;
    }

    /**
     * Vraća matricu gradova
     *
     * @return matricu gradova n x m
     */
    public City[][] getCountryMap() {
        return countryMap;
    }

    /**
     * Vraća listu svih stanica
     *
     * @return listu svih stanica
     */
    public List<Station> getStations() {
        return stations;
    }

    /**
     * Vraća listu svih definisanih polazaka
     *
     * @return listu svih definisanih polazaka
     */
    public List<Departure> getDepartures() {
        return departures;
    }
}
