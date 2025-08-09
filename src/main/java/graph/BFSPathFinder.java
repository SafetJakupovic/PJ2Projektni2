package main.java.graph;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import main.java.model.Departure;
import main.java.model.Route;
import main.java.model.RouteSegment;
import main.java.model.Station;

/**
 * Pronalazi putanje s najmanjim brojem presjedanja (BFS pretraga).
 * Svaki transfer (autobus->voz ili obrnuto) tretira kao dodatni korak.
 */
public class BFSPathFinder implements IPathFinder {

    /**
     * {@inheritDoc}
     *
     * Ova implementacija radi Breadth‑First pretragu po grafu
     * i mjeri broj promjena tipa prevoza (presjedanja).
     */
    @Override
    public List<Route> findPaths(TransportGraph graph,
                                 Station source,
                                 Station destination,
                                 int topN) {
        // Helper-klasa za čuvanje stanja u redu
        class NodeEntry {
            GraphNode node;  // trenutni čvor
            NodeEntry prev; // prethodni čvor u putanji
            Departure departure; // Departure kojim smo stigli ovdje
            int transfers;  // broj presedanja dosad
            int arrivalMinute; // apsolutno vreme dolaska u minutama

            NodeEntry(GraphNode node, NodeEntry prev, Departure departure, int transfers, int arrivalMinute) {
                this.node = node;
                this.prev = prev;
                this.departure = departure;
                this.transfers = transfers;
                this.arrivalMinute = arrivalMinute;
            }
        }

        GraphNode targetNode = graph.getNode(destination);

        Queue<NodeEntry> queue = new LinkedList<>();
        Map<GraphNode, Integer> bestTransfers = new HashMap<>();
        Map<GraphNode, NodeEntry> finished = new HashMap<>();

        // U redu enqueue‑ujemo obe stanice (BUS i RAIL) u gradu source
        for (Station s : graph.getNodeMap().keySet()) {
            if (s.getCity().equals(source.getCity())) {
                GraphNode node = graph.getNode(s);
                queue.add(new NodeEntry(node, null, null, 0, 0));
                bestTransfers.put(node, 0);
            }
        }

        // BFS petlja
        while (!queue.isEmpty()) {
            NodeEntry cur = queue.poll();
            if (finished.containsKey(cur.node))
                continue;
            finished.put(cur.node, cur);
            if (cur.node.equals(targetNode))
                break;

            for (GraphEdge edge : cur.node.getEdges()) {
                GraphNode neigh = edge.getTarget();
                Departure d = edge.getDeparture();

                // kad najprije stignemo: curr.arrivalMinute + minTransferTime
                int earliest = cur.arrivalMinute + (int) d.getMinTransferTime().toMinutes();

                boolean isTransfer = d.getFrom().getCity().equals(d.getTo().getCity())
                        && d.getFrom().getType() != d.getTo().getType();

                // izračun apsolutnog vremena dolaska
                int arrivalMin;
                if (isTransfer)
                    // transfer: krećemo odmah, dodajemo trajanje vožnje
                    arrivalMin = earliest + (int) d.getDuration().toMinutes();
                else {
                    // raspoređeni polazak: računamo sledeći mogući termin
                    int depDayMin = d.getDepartureTime().getHour() * 60
                            + d.getDepartureTime().getMinute();
                    int dayOff = earliest / 1440;
                    int dayMin = earliest % 1440;
                    int actualDep = dayOff * 1440
                            + (depDayMin >= dayMin ? depDayMin : depDayMin + 1440);
                    arrivalMin = actualDep + (int) d.getDuration().toMinutes();
                }

                // novi broj presjedanja
                int newTransfers = cur.prev == null ? 0
                        : cur.transfers + ((cur.departure.getType() != d.getType()) ? 1 : 0);

                // ako smo ovim entry-jem bolji za susjeda, enqueue
                Integer best = bestTransfers.get(neigh);
                if (best == null || newTransfers < best) {
                    bestTransfers.put(neigh, newTransfers);
                    queue.add(new NodeEntry(neigh, cur, d, newTransfers, arrivalMin));
                }
            }
        }

        // Ako nismo stigli do cilja, vraćamo praznu listu
        NodeEntry end = finished.get(targetNode);
        if (end == null)
            return Collections.emptyList();

        // Rekonstrukcija puta
        LinkedList<RouteSegment> segs = new LinkedList<>();
        for (NodeEntry it = end; it.prev != null; it = it.prev) {
            segs.addFirst(new RouteSegment(it.departure));
        }

        // Sastavljanje Route objekta
        int price = segs.stream().mapToInt(s -> s.getDeparture().getPrice()).sum();
        int transfers = end.transfers;
        Duration totalDuration = Duration.ofMinutes(end.arrivalMinute);
        Route route = new Route(segs, totalDuration, price, transfers);
        return Collections.singletonList(route);
    }
}
