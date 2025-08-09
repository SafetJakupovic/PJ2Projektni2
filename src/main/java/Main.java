package main.java;

import java.io.IOException;
import java.util.List;

import main.java.data.JsonDataLoader;
import main.java.data.LoadedTransportData;
import main.java.graph.PathFinderFactory;
import main.java.graph.TransportGraph;
import main.java.model.Departure;
import main.java.model.Route;
import main.java.model.RouteSegment;
import main.java.model.Station;
import main.java.service.RouteService;

public class Main {
  public static void main(String[] args) {
    try {
      LoadedTransportData data = JsonDataLoader.load("transport_data.json");
      List<Station> stations = data.getStations();

      TransportGraph graph = new TransportGraph(data);
      RouteService routeService = new RouteService(graph);

      Station from = findStationByCode(stations, "A_0_0");
      Station to = findStationByCode(stations, "Z_4_4");

      System.out.println("=== Najbrza ruta ===");
      List<Route> fastest = routeService.findRoutes(from, to, PathFinderFactory.Strategy.FASTEST, 1);
      if (fastest.isEmpty()) {
        System.out.println("Nema dostupne rute.");
      } else {
        Route r = fastest.get(0);
        printRoute(r);
      }

      System.out.println("\n=== Najjeftinija ruta ===");
      List<Route> cheapest = routeService.findRoutes(from, to,
          PathFinderFactory.Strategy.CHEAPEST, 1);
      if (cheapest.isEmpty()) {
        System.out.println("Nema dostupne rute.");
      } else {
        printRoute(cheapest.get(0));
      }

      System.out.println("\n=== Ruta sa najmanje presjedanja ===");
      List<Route> least_transfers = routeService.findRoutes(from, to, PathFinderFactory.Strategy.LEAST_TRANSFERS, 1);
      if (least_transfers.isEmpty()) {
        System.out.println("Nema dostupne rute.");
      } else {
        printRoute(least_transfers.get(0));
      }
    } catch (IOException e) {
      System.err.println("Greska pri ucitavanju podataka: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static Station findStationByCode(List<Station> stations, String code) {
    return stations.stream()
        .filter(s -> s.getCode().equals(code))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Nepostojeca stanica: " + code));
  }

  private static void printRoute(Route route) {
    String fmtHeader = "%-6s | %-6s | %-6s | %-8s | %-4s | %-5s%n";
    String fmtRow = "%-6s | %-6s | %-6s | %-8s | %-4d | %-5d%n";

    System.out.printf(fmtHeader, "Type", "From", "To", "Depart", "Dur", "Price");
    System.out.println("-------+--------+--------+----------+------+-------");

    for (RouteSegment seg : route.getSegments()) {
      Departure d = seg.getDeparture();
      System.out.printf(fmtRow,
          d.getType(),
          d.getFrom().getCode(),
          d.getTo().getCode(),
          d.getDepartureTime(),
          d.getDuration().toMinutes(),
          d.getPrice());
    }

    System.out.println("-------+--------+--------+----------+------+-------");
    System.out.printf("Total time: %d min,  Total price: %4d BAM,  Transfers: %d%n",
        route.getTotalDuration().toMinutes(),
        route.getTotalPrice(),
        route.getTransfers());
    System.out.println();
  }
}
