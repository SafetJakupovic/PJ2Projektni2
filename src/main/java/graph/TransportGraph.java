package main.java.graph;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.java.data.LoadedTransportData;
import main.java.model.City;
import main.java.model.Departure;
import main.java.model.Station;
import main.java.model.TransportType;

/**
 * Građenje i održavanje grafa svih stanica i njihovih ivica
 * na temelju učitanih podataka. Dodaje i interne ivice za brze transfere
 * (autobuska stanica - zeljeznicka stanica unutar istog grada).
 */
public class TransportGraph {
    /**
     * Mapa: za svaku stanicu čuva odgovarajući GraphNode.
     */
    private final Map<Station, GraphNode> nodeMap = new HashMap<>();

    /**
     * Privatni no‑arg konstruktor za potrebe kloniranja
     */
    private TransportGraph() {
    }

    /**
     * Glavni konstruktor grafa.
     * Za svaku stanicu kreira čvor.
     * Za svaki {@link Departure} dodaje ivicu iz polazne u odredišnu stanicu.
     * Za autobusku i željezničku u istom gradu dodaje dodatnu ivicu trajanja 5 minuta.
     *
     * @param data učitani podaci s listom stanica i polazaka
     */
    public TransportGraph(LoadedTransportData data) {
        // Kreiraj sve čvorove za svaku stanicu
        for (Station station : data.getStations()) {
            nodeMap.put(station, new GraphNode(station));
        }

        // Kreiraj ivice iz polazaka
        for (Departure dep : data.getDepartures()) {
            GraphNode fromNode = nodeMap.get(dep.getFrom());
            GraphNode toNode = nodeMap.get(dep.getTo());
            if (fromNode != null && toNode != null) {
                fromNode.addEdge(new GraphEdge(toNode, dep));
            }
        }

        // Dodaj pomoćne ivice unutar istih gradova
        Map<City, List<Station>> byCity = new HashMap<>();
        for (Station s : data.getStations()) {
            byCity.computeIfAbsent(s.getCity(), c -> new ArrayList<>()).add(s);
        }
        for (List<Station> sts : byCity.values()) {
            // Pronađi autobusku ili željezničku stanicu
            Station bus = sts.stream()
                    .filter(s -> s.getType() == TransportType.BUS)
                    .findFirst()
                    .orElse(null);
            Station rail = sts.stream().
                    filter(s -> s.getType() == TransportType.RAIL)
                    .findFirst()
                    .orElse(null);
            if (bus != null && rail != null) {
                GraphNode busNode = nodeMap.get(bus);
                GraphNode railNode = nodeMap.get(rail);

                Departure busToRail = new Departure(bus, rail, TransportType.RAIL, LocalTime.MIN,
                        Duration.ofMinutes(5), 0, Duration.ZERO);
                Departure railToBus = new Departure(rail, bus, TransportType.BUS, LocalTime.MIN,
                        Duration.ofMinutes(5), 0, Duration.ZERO);

                busNode.addEdge(new GraphEdge(railNode, busToRail));
                railNode.addEdge(new GraphEdge(busNode, railToBus));
            }
        }
    }

    /**
     * Vraća odgoravajući {@link GraphNode} ili null ako nema čvora
     *
     * @param station stanica čiji se čvor vraća
     * @return odgovarajući {@link GraphNode} ili null ako nema čvora
     */
    public GraphNode getNode(Station station) {
        return nodeMap.get(station);
    }

    /**
     * Vraća nodeMap
     *
     * @return mapiranje svih stanica na njihove čvorove
     */
    public Map<Station, GraphNode> getNodeMap() {
        return nodeMap;
    }

    /**
     * Stvara duboku kopiju ovog grafa (nova instanca čvorova i ivica,
     * ali iste Station i Departure objekte).
     * Koristi se pri izvođenju K‑shortest algoritma kako bi se mogle privremeno
     * uklanjati ivice bez uticaja na original.
     *
     * @return kopija grafa
     */
    public TransportGraph deepClone() {
        TransportGraph clone = new TransportGraph();
        for (GraphNode node : nodeMap.values()) {
            Station s = node.getStation();
            clone.nodeMap.put(s, new GraphNode(s));
        }

        for (GraphNode node : nodeMap.values()) {
            GraphNode cloneNode = clone.nodeMap.get(node.getStation());
            for (GraphEdge e : node.getEdges()) {
                GraphNode cloneTarget = clone.nodeMap.get(e.getTarget().getStation());
                cloneNode.addEdge(new GraphEdge(cloneTarget, e.getDeparture()));
            }
        }
        return clone;
    }

    /**
     * Uklanja ivicu iz grafa (koristi se u K‑shortest metodi).
     *
     * @param from izvorna stanica ivice
     * @param to   ciljna stanica ivice
     * @param dep  specifična {@link Departure} ivica koji se uklanja
     */
    public void removeEdge(Station from, Station to, Departure dep) {
        GraphNode fromNode = nodeMap.get(from);
        if (fromNode == null) return;
        fromNode.getEdges().removeIf(e -> e.getDeparture().equals(dep) && e.getTarget().getStation().equals(to));
    }
}
