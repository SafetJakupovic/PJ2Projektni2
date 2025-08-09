package main.java.graph;

/**
 * Fabrika za kreiranje IPathFinder instanci prema strategiji.
 */
public class PathFinderFactory {
    /**
     * Strategije pretrage.
     */
    public enum Strategy {
        /**
         * Najkraće vrijeme putovanja (Dijkstra: TIME)
         */
        FASTEST,
        /**
         * Najniža cijena (Dijkstra: COST)
         */
        CHEAPEST,
        /**
         * Najmanji broj presjedanja (BFS)
         */
        LEAST_TRANSFERS
    }

    /**
     * Overload za topN=1
     *
     * @param strategy kriterijum/strategija
     * @return rezultat create metode sa topN=1
     */
    public static IPathFinder create(Strategy strategy) {
        return create(strategy, 1);
    }

    /**
     * Kreira odgovarajući finder za zadatu strategiju i topN.
     *
     * @param strategy poželjna strategija
     * @param topN     koliko puteva želimo (1 ili više)
     * @return IPathFinder instanca
     */
    public static IPathFinder create(Strategy strategy, int topN) {
        IPathFinder base = switch (strategy) {
            case FASTEST -> new DijkstraPathFinder(DijkstraPathFinder.Criteria.TIME);
            case CHEAPEST -> new DijkstraPathFinder(DijkstraPathFinder.Criteria.COST);
            case LEAST_TRANSFERS -> new BFSPathFinder();
        };
        if (topN > 1)
            return new KShortestPathFinder(base);
        return base;
    }
}
