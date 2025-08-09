package main.java.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import main.java.model.Station;

/**
 * Čvor u grafu transportnih stanica.
 * Čuva referencu na {@link Station} i listu izlaznih ivica ({@link GraphEdge}).
 */
public class GraphNode {
    /**
     * Stanica koju predstavlja ovaj čvor.
     */
    private final Station station;
    /**
     * Liste svih izlaznih grana iz ovog čvora.
     */
    private final List<GraphEdge> edges;

    /**
     * Kreira novi čvor vezan uz zadanu stanicu.
     *
     * @param station model stanice koju ovaj čvor predstavlja
     */
    public GraphNode(Station station) {
        this.station = station;
        this.edges = new ArrayList<>();
    }

    /**
     * Vraća pripadajuću stanicu
     *
     * @return pripadajuća stanica
     */
    public Station getStation() {
        return station;
    }

    /**
     * Vraća listu ivica koji idu iz ovoga čvora prema drugim čvorovima
     *
     * @return lista ivica koji idu iz ovog čvora prema drugim čvorovima
     */
    public List<GraphEdge> getEdges() {
        return edges;
    }

    /**
     * Dodaje ivicu iz ovog čvora prema nekom cilju.
     *
     * @param edge ivica koji sadrži ciljnu stanicu i informacije o polasku
     */
    public void addEdge(GraphEdge edge) {
        edges.add(edge);
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
        if (!(o instanceof GraphNode))
            return false;
        GraphNode that = (GraphNode) o;
        return station.equals(that.station);
    }

    /**
     * Vraća hash kod objekta
     *
     * @return hash kod objekta
     */
    @Override
    public int hashCode() {
        return Objects.hash(station);
    }

    /**
     * Ispis
     *
     * @return prikaz čvora u grafu kao string
     */
    @Override
    public String toString() {
        return "GraphNode{" + "station=" + station.getCode() + '}';
    }
}
