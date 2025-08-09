package main.java.graph;

import java.time.Duration;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import main.java.model.Departure;
import main.java.model.Route;
import main.java.model.RouteSegment;
import main.java.model.Station;

/**
 * Dijkstra algoritam za pronalaženje puta prema kriterijumu: vrijeme ili cijena.
 */
public class DijkstraPathFinder implements IPathFinder {
    /**
     * Kriterijum optimizacije puta
     */
    public enum Criteria {
        TIME, COST
    }

    /**
     * Kriterijum ili strategija (vrijeme ili cijena)
     */
    private final Criteria criteria;

    public DijkstraPathFinder(Criteria criteria) {
        this.criteria = criteria;
    }

    /**
     * {@inheritDoc}
     *
     * Petlja po prioritetnom redu, gdje je težina:
     * - ako je TIME: (apsolutno vrijeme dolaska – vrijeme prvog polaska)
     * - ako je COST: suma cijena karata
     *
     * U obzir se uzimaju transferi (minimalno vreme prelaska stanica).
     */
    @Override
    public List<Route> findPaths(TransportGraph graph, Station source, Station destination, int topN) {
        // Helper-klasa čuva stanje u pq: čvor, težinu, vreme dolaska, vreme prvog polaska
        class NodeEntry {
            GraphNode node;
            long weight;
            int arrivalMinute;
            int firstDepartureMinute;
            NodeEntry prev;
            Departure departure;

            NodeEntry(GraphNode node, long weight, int arrivalMinute,
                      int firstDepartureMinute, NodeEntry prev, Departure departure) {
                this.node = node;
                this.weight = weight;
                this.arrivalMinute = arrivalMinute;
                this.firstDepartureMinute = firstDepartureMinute;
                this.prev = prev;
                this.departure = departure;
            }
        }

        PriorityQueue<NodeEntry> pq = new PriorityQueue<>(Comparator.comparingLong(e -> e.weight));
        Map<GraphNode, Long> best = new HashMap<>();
        Map<GraphNode, NodeEntry> finished = new HashMap<>();

        GraphNode target = graph.getNode(destination);

        // enqueue početne stanice (bus i rail) iz source grada
        for (Station s : graph.getNodeMap().keySet()) {
            // ako već imamo bolju putanju do cur.node, preskačemo
            if (s.getCity().equals(source.getCity())) {
                GraphNode node = graph.getNode(s);
                pq.add(new NodeEntry(node, 0L, 0, -1, null, null));
            }
        }

        while (!pq.isEmpty()) {
            NodeEntry cur = pq.poll();
            if (best.containsKey(cur.node) && cur.weight > best.get(cur.node))
                continue;
            best.put(cur.node, cur.weight);
            finished.put(cur.node, cur);
            if (cur.node.equals(target))
                break;

            for (GraphEdge edge : cur.node.getEdges()) {
                GraphNode neigh = edge.getTarget();
                Departure d = edge.getDeparture();

                // najranije kad možemo da sjednemo na ovaj dep
                int earliest = cur.arrivalMinute + (int) d.getMinTransferTime().toMinutes();

                boolean isTransfer = d.getFrom().getCity().equals(d.getTo().getCity());

                if (cur.prev == null && isTransfer)
                    continue;

                // izračun vremena dolaska
                int arrivalMin;
                if (isTransfer)
                    // transferna ivica: krećemo odmah
                    arrivalMin = earliest + (int) d.getDuration().toMinutes();
                else {
                    // raspoređeni polazak
                    int depMinOfDay = d.getDepartureTime().getHour() * 60 + d.getDepartureTime().getMinute();
                    int dayOffset = earliest / 1440;
                    int dayMin = earliest % 1440;
                    int actualDep = dayOffset * 1440 + (depMinOfDay >= dayMin ? depMinOfDay : depMinOfDay + 1440);
                    arrivalMin = actualDep + (int) d.getDuration().toMinutes();
                }

                // vreme prvog polaska u ovoj putanji
                int firstDep = cur.firstDepartureMinute >= 0 ? cur.firstDepartureMinute
                        : (arrivalMin - (int) d.getDuration().toMinutes());
                // nova težina
                long newWeight = (criteria == Criteria.TIME)
                        ? arrivalMin - firstDep
                        : cur.weight + d.getPrice();

                // push u pq ako smo bolji
                if (!best.containsKey(neigh) || newWeight < best.get(neigh)) {
                    pq.add(new NodeEntry(neigh, newWeight, arrivalMin, firstDep, cur, d));
                }
            }
        }
        // ako nema puta
        NodeEntry end = finished.get(target);
        if (end == null)
            return Collections.emptyList();

        // rekonstrukcija segmenata u listu
        LinkedList<RouteSegment> segs = new LinkedList<>();
        for (NodeEntry it = end; it.prev != null; it = it.prev) {
            segs.addFirst(new RouteSegment(it.departure));
        }

        // sastavljanje Route
        int price = segs.stream().mapToInt(s -> s.getDeparture().getPrice()).sum();
        int transfers = Route.computeTransfers(segs);
        Duration travelTime = Duration.ofMinutes(end.arrivalMinute - end.firstDepartureMinute);

        Route route = new Route(segs, travelTime, price, transfers);
        return Collections.singletonList(route);
    }
}
