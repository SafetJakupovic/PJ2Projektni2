package main.java.graph;

import main.java.model.Departure;
import main.java.model.Route;
import main.java.model.RouteSegment;
import main.java.model.Station;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Generički pronalazač top N najkraćih puteva (Yenov algoritam).
 * Koristi osnovni IPathFinder (Dijkstra ili BFS) kao „spur“ generator.
 */
public class KShortestPathFinder implements IPathFinder {
    /**
     * Osnovni pronalazač (Dijkstra ili BFS) koji koristimo za „spur“ puteve.
     */
    private final IPathFinder baseFinder;

    /**
     * @param baseFinder osnovni finder za jedan najkraći put
     */
    public KShortestPathFinder(IPathFinder baseFinder) {
        this.baseFinder = baseFinder;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementacija Yenovog algoritma:
     * 1) Doda se prvi (najbolji) put u result
     * 2) Za k = 1..topN-1:
     * – Svaki prefiks (rootPath) primejra uklanja sve pokupljene ivice
     * – Generiše se spur ruta iz kraja prefiksa
     * – Kombinuje se rootPath + spurPath i ubacuje u PQ
     * – Izdvaja se sledeći najjeftiniji kandidat koji nije duplikat
     */
    @Override
    public List<Route> findPaths(TransportGraph graph, Station source, Station destination, int topN) {
        List<Route> result = new ArrayList<>();

        // Prvi put
        List<Route> first = baseFinder.findPaths(graph, source, destination, 1);
        if (first.isEmpty())
            return result;

        result.add(first.get(0));

        // Min‑heap za kandidate
        PriorityQueue<Candidate> pq = new PriorityQueue<>(Comparator.comparingLong(c -> c.cost));

        // Generiši k-1 dodatnih puteva
        for (int k = 1; k < topN; k++) {
            Route previous = result.get(k - 1);
            List<RouteSegment> prevSegs = previous.getSegments();

            for (int i = 0; i < prevSegs.size(); i++) {
                Station spurNode = prevSegs.get(i).getDeparture().getFrom();
                List<RouteSegment> rootPath = new ArrayList<>(prevSegs.subList(0, i));
                // Kloniraj graf i ukloni ivice korištene u dosadašnjim rezultatima
                TransportGraph cloned = graph.deepClone();
                for (Route r : result) {
                    List<RouteSegment> segs = r.getSegments();
                    if (segs.size() > i && segs.subList(0, i).equals(rootPath)) {
                        Departure d = segs.get(i).getDeparture();
                        cloned.removeEdge(d.getFrom(), d.getTo(), d);
                    }
                }

                // Generiši spur rutu
                List<Route> spurList = baseFinder.findPaths(cloned, spurNode, destination, 1);
                if (spurList.isEmpty())
                    continue;

                // Spoji root + spur segmente
                List<RouteSegment> spurSegs = spurList.get(0).getSegments();
                List<RouteSegment> totalSegs = new ArrayList<>(rootPath);
                totalSegs.addAll(spurSegs);
                Route newRoute = new Route(totalSegs);
                long cost = computeCost(newRoute);
                pq.offer(new Candidate(newRoute, cost));
            }

            // Izvuci sledeći netaknut kandidat
            Candidate c;
            do {
                c = pq.poll();
            } while (c != null && result.contains(c.route));
            if (c == null) break;
            result.add(c.route);
        }
        return result;
    }

    /**
     * Računa težinu rute zavisno od tipa baseFinder-a
     */
    private long computeCost(Route r) {
        if (baseFinder instanceof DijkstraPathFinder)
            // ako je cena nenulta, uzmi cenu, inače trajanje
            return r.getTotalPrice() > 0 ? r.getTotalPrice() : r.getTotalDuration().toMinutes();
        else
            // za BFS gledamo broj transfera
            return r.getTransfers();
    }


    /**
     * Interni DTO za kandidate u heap-u
     */
    private static class Candidate {
        final Route route;
        final long cost;

        Candidate(Route route, long cost) {
            this.route = route;
            this.cost = cost;
        }
    }
}
