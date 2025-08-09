package main.java.service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import main.java.graph.IPathFinder;
import main.java.graph.PathFinderFactory;
import main.java.graph.TransportGraph;
import main.java.model.Departure;
import main.java.model.Route;
import main.java.model.RouteSegment;
import main.java.model.Station;

/**
 * Poslovna logika za pronalaženje i pripremu ruta.
 * Koristi TransportGraph i IPathFinder-e da vrati top N ruta, te obogaćuje rutu apsolutnim vremenima za prikaz.
 */
public class RouteService {
    /**
     * Graf u kojem tražimo puteve.
     */
    private final TransportGraph graph;

    /**
     * Konstruktor.
     *
     * @param graph inicijalni graf
     */
    public RouteService(TransportGraph graph) {
        this.graph = graph;
    }

    /**
     * Poziva PathFinderFactory.create(...) za dobivanje odgovarajućeg algoritma, te vraća listu najboljih ruta.
     *
     * @param from     početna stanica
     * @param to       odredišna stanica
     * @param strategy odabrani kriterijum
     * @param topN     koliko ruta vratiti
     * @return lista topN Route objekata
     */
    public List<Route> findRoutes(Station from, Station to, PathFinderFactory.Strategy strategy, int topN) {
        IPathFinder pathFinder = PathFinderFactory.create(strategy, topN);
        List<Route> routes = pathFinder.findPaths(graph, from, to, topN);
        return routes;
    }

    /**
     * Na temelju već pronađene Route, računa apsolutna vremena polaska i dolaska za svaki segment, vraća listu JourneySegment-a.
     * Uzima već izračunatu rutu (objekt Route koji sadrži listu RouteSegment)
     * i za svaki segment računa apsolutno vrijeme polaska i dolaska u minutama od početka prvog dana.
     *
     * @param route ruta čiji se segmenti žele anotirati
     * @return obogaćeni segmenti s apsolutnim vremenima
     */
    public List<JourneySegment> annotateRoute(Route route) {
        List<JourneySegment> out = new ArrayList<>();
        int currentMin = 0;

        // Prođi kroz sve segmente rute
        for (int i = 0; i < route.getSegments().size(); i++) {
            RouteSegment seg = route.getSegments().get(i);
            Departure d = seg.getDeparture();
            boolean isTransfer = d.getFrom().getCity().equals(d.getTo().getCity());

            // Najranije kretanje: svoje dolazno vrijeme + minTransferTime
            int earliest = currentMin + (int) d.getMinTransferTime().toMinutes();
            int schedule = d.getDepartureTime().getHour() * 60 + d.getDepartureTime().getMinute();
            int depAbs;
            if (i == 0)
                // Prvi segment: nema čekanja na prethodni, koristimo raspored kao apsolutno
                depAbs = schedule;
            else if (isTransfer)
                // Ako je transfer ne čekamo sledeći red vožnje već krećemo odmah kad stignemo
                depAbs = earliest;
            else {
                // Svi ostali segmenti slijede raspored vožnje:
                //  izračunamo broj prošlih dana: earliest / 1440
                //  minute unutar dana: earliest % 1440
                //  ako je sledeći polazak istog dana (schedule >= dayMin),
                //      koristimo dayOffset*1440 + schedule,
                //      inače preskačemo u idući dan i dodajemo 1440.
                int dayOff = earliest / 1440;
                int dayMin = earliest % 1440;
                depAbs = dayOff * 1440 + (schedule >= dayMin ? schedule : schedule + 1440);
            }
            JourneySegment js = new JourneySegment(seg, depAbs);

            // Kreiraj JourneySegment: on u konstruktoru izračuna arrivalMinuteAbs
            out.add(js);

            // Apsolutno vrijeme dolaska postaje baza za sljedeći segment
            currentMin = js.getArrivalMinuteAbs();
        }
        return out;
    }

}
