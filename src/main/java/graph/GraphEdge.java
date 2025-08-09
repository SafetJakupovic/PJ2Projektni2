package main.java.graph;

import java.util.Objects;

import main.java.model.Departure;

/**
 * Ivica u grafu, povezuje jedan GraphNode s drugim putem jednog odlaska.
 * Čuva ciljnu stanicu (GraphNode) i pripadajući {@link Departure}.
 */
public class GraphEdge {
    /**
     * Čvor u koji vodi ova grana.
     */
    private final GraphNode target;
    /**
     * Informacije o polasku
     */
    private final Departure departure;

    /**
     * Konstruktor.
     *
     * @param target    čvor do kojeg ova ivica vodi
     * @param departure informacije o polasku (vrijeme, cijena, trajanje)
     */
    public GraphEdge(GraphNode target, Departure departure) {
        this.target = target;
        this.departure = departure;
    }

    /**
     * Vraća ciljni čvor
     *
     * @return ciljni čvor (stanica)
     */
    public GraphNode getTarget() {
        return target;
    }

    /**
     * Vraća detalje odlaska
     *
     * @return detalji odlaska (Departure)
     */
    public Departure getDeparture() {
        return departure;
    }

    /**
     * Ispis
     *
     * @return prikaz ivice grafa kao string
     */
    @Override
    public String toString() {
        return String.format("Do %s preko %s", target.getStation().getCode(), departure);
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
        if (!(o instanceof GraphEdge))
            return false;
        GraphEdge that = (GraphEdge) o;
        return target.equals(that.target) && departure.equals(that.departure);
    }

    /**
     * Vraća hash kod objekta
     *
     * @return hash kod objekta
     */
    @Override
    public int hashCode() {
        return Objects.hash(target, departure);
    }
}
