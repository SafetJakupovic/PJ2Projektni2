package main.java.graph;

import java.util.List;

import main.java.model.Route;
import main.java.model.Station;

/**
 * Interfejs za algoritme pretraživanja puta u TransportGraphu.
 * Implementacije mogu koristiti različite strategije (Dijkstra, BFS, K‑shortest…).
 */
public interface IPathFinder {
    /**
     * Pronađe do topN različitih ruta između source i destination.
     *
     * @param graph       graf nad kojim se izvršava pretraga
     * @param source      početna stanica
     * @param destination odredišna stanica
     * @param topN        maksimalan broj ruta koje se vraćaju
     * @return lista pronađenih {@link Route} objekata, ili prazna lista ako nema putanje
     */
    List<Route> findPaths(TransportGraph graph, Station source, Station destination, int topN);
}
