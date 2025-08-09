package main.java.ui.controller;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import main.java.data.JsonDataLoader;
import main.java.data.LoadedTransportData;
import main.java.graph.PathFinderFactory;
import main.java.graph.TransportGraph;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import main.java.model.*;
import main.java.service.*;
import main.java.util.MapCanvas;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Kontroler glavnog prozora aplikacije.
 * Rukuje izborom parametara, prikazom rute, mapom i statistikama.
 */
public class MainController {
    /**
     * Padajući meni za početnu stanicu.
     */
    @FXML
    private ComboBox<Station> sourceCombo;
    /**
     * Padajući meni za odredište.
     */
    @FXML
    private ComboBox<Station> destCombo;
    /**
     * Padajući meni za kriterijum.
     */
    @FXML
    private ComboBox<String> criteriaCombo;
    /**
     * Dugme za pronalaženje rute.
     */
    @FXML
    private Button findButton;
    /**
     * Dugme za Top 5 ruta.
     */
    @FXML
    private Button moreButton;
    /**
     * Tabela detalja puta.
     */
    @FXML
    private TableView<JourneySegment> routeTable;
    /**
     * Kolona tipa (BUS/RAIL).
     */
    @FXML
    private TableColumn<JourneySegment, String> typeCol;
    /**
     * Kolona polaska.
     */
    @FXML
    private TableColumn<JourneySegment, String> fromCol;
    /**
     * Kolona dolaska.
     */
    @FXML
    private TableColumn<JourneySegment, String> toCol;
    /**
     * Kolona trajanja segmenta.
     */
    @FXML
    private TableColumn<JourneySegment, Integer> durCol;
    /**
     * Kolona cijene segmenta.
     */
    @FXML
    private TableColumn<JourneySegment, Integer> priceCol;
    /**
     * Kolona vremena polaska.
     */
    @FXML
    private TableColumn<JourneySegment, String> departCol;
    /**
     * Tekstualni status (trajanje, cijena, transferi).
     */
    @FXML
    private Text statusText;
    /**
     * Prikaz broja prodatih karata.
     */
    @FXML
    private Label ticketsSoldLabel;
    /**
     * Prikaz ukupne zarade.
     */
    @FXML
    private Label totalRevenueLabel;
    /**
     * Platno za prikaz mape i stanica.
     */
    @FXML
    private MapCanvas mapCanvas;
    /**
     * Dugme za kupovinu karte.
     */
    @FXML
    private Button purchaseButton;

    /**
     * Skladišti graf za pretragu.
     */
    private TransportGraph graph;
    /**
     * Poslovna logika traženja ruta.
     */
    private RouteService routeService;
    /**
     * Format za vrijeme HH:mm.
     */
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
    /**
     * Sadrži informacije o prodatim kartama.
     */
    private SalesStatsService statsService = new SalesStatsService();
    /**
     * Omogućava kupovinu karte.
     */
    private final TicketService ticketService = new TicketService();

    /**
     * Trajanje poslednje pronađene rute.
     */
    private long actualDuration; // čuva trajanje posljednje pronađene rute

    /**
     * Izabrana početna stanice.
     */
    Station from;
    /**
     * Izabrano odredište.
     */
    Station to;
    /**
     * Izabrani kriterijum kao String.
     */
    String crit;
    /**
     * Interni enum kriterijuma.
     */
    PathFinderFactory.Strategy strategy;

    /**
     * Inicijalizacija prije prikaza prozora:
     * - učitavanje statistika,
     * - parsiranje JSON podataka,
     * - popunjavanje combo boxeva,
     * - podešavanje cell factory i bindinga dugmadi,
     * - inicijalizacija interaktivne mape.
     *
     * @throws IOException
     */
    @FXML
    private void initialize() throws IOException {
        // Prikaz statistike
        int sold = statsService.getTotalTicketsSold();
        long revenue = statsService.getTotalRevenue();
        ticketsSoldLabel.setText("Prodanih karata: " + sold);
        totalRevenueLabel.setText("Ukupna zarada: " + revenue);

        LoadedTransportData data = JsonDataLoader.load("transport_data.json");
        graph = new TransportGraph(data);
        routeService = new RouteService(graph);

        var stations = FXCollections.observableArrayList(data.getStations());
        sourceCombo.setItems(stations);
        destCombo.setItems(stations);
        criteriaCombo.setItems(FXCollections.observableArrayList("Vrijeme", "Cijena", "Presjedanja"));

        typeCol.setCellValueFactory(cell -> {
            Departure d = cell.getValue().getSegment().getDeparture();
            boolean isTransfer = d.getFrom().getCity().equals(d.getTo().getCity());
            String text = isTransfer ? "" : (d.getType() == TransportType.BUS ? "AUTOBUS" : "VOZ");
            return new ReadOnlyStringWrapper(text);
        });
        fromCol.setCellValueFactory(cell -> {
            Departure d = cell.getValue().getSegment().getDeparture();
            boolean isTransfer = d.getFrom().getCity().equals(d.getTo().getCity());
            String text = isTransfer ? d.getFrom().getCode() : d.getFrom().getCity().getName();
            return new ReadOnlyStringWrapper(text);
        });
        toCol.setCellValueFactory(cell -> {
            Departure d = cell.getValue().getSegment().getDeparture();
            boolean isTransfer = d.getFrom().getCity().equals(d.getTo().getCity());
            String text = isTransfer ? d.getTo().getCode() : d.getTo().getCity().getName();
            return new ReadOnlyStringWrapper(text);
        });
        departCol.setCellValueFactory(cell -> {
            int absMin = cell.getValue().getDepartureMinuteAbs();
            int dayMin = absMin % 1440;
            LocalTime t = LocalTime.of(dayMin / 60, dayMin % 60);
            return new ReadOnlyStringWrapper(t.format(timeFmt));
        });
        durCol.setCellValueFactory(cell -> new SimpleIntegerProperty((int) cell.getValue().getDurationMinutes()).asObject());
        priceCol.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getPrice()).asObject());

        // Onemogući dugmad dok nema kompletnih parametara
        BooleanBinding inputIncomplete =
                sourceCombo.valueProperty().isNull()
                        .or(destCombo.valueProperty().isNull())
                        .or(criteriaCombo.valueProperty().isNull());

        findButton.disableProperty().bind(inputIncomplete);
        //moreButton.disableProperty().bind(inputIncomplete);
        //purchaseButton.disableProperty().bind(inputIncomplete);

        findButton.setOnAction(e -> onFindRoute());
        moreButton.setOnAction(e -> showTopRoutes());

        // 7) Inicijalizuj mapu i callback za klik na stanicu
        mapCanvas.initialize(data);
        mapCanvas.setOnStationSelected((station, isSource) -> {
            if (isSource) sourceCombo.setValue(station);
            else destCombo.setValue(station);
        });
    }

    /**
     * Priprema vrijednosti iz UI elemenata (combo boxeva).
     */
    private void setValues() {
        from = sourceCombo.getValue();
        to = destCombo.getValue();
        crit = criteriaCombo.getValue();
        if (from == null || to == null || crit == null) {
            statusText.setText("Odaberite sve parametre");
            return;
        }

        strategy = switch (crit) {
            case "Vrijeme" -> PathFinderFactory.Strategy.FASTEST;
            case "Cijena" -> PathFinderFactory.Strategy.CHEAPEST;
            case "Presjedanja" -> PathFinderFactory.Strategy.LEAST_TRANSFERS;
            default -> null;
        };
    }

    /**
     * Handler za findButton:
     * - pronalazi rutu,
     * - prikazuje je u tabeli i na mapi,
     * - omogućava ostala dugmad.
     */
    private void onFindRoute() {
        setValues();

        List<Route> routes = routeService.findRoutes(from, to, strategy, 1);

        if (routes.isEmpty()) {
            statusText.setText("Nije pronadjena ruta");
            routeTable.getItems().clear();
        } else {
            Route r = routes.get(0);
            List<JourneySegment> journey = routeService.annotateRoute(r);
            routeTable.setItems(FXCollections.observableArrayList(journey));

            mapCanvas.highlightPath(Collections.emptyList());
            List<RouteSegment> segments = r.getSegments();
            mapCanvas.highlightPath(segments);

            actualDuration = r.getTotalDuration().toMinutes();

            statusText.setText(String.format("Trajanje: %dh %dm, Cijena: %d, Transfera: %d", actualDuration / 60, actualDuration % 60, r.getTotalPrice(), r.getTransfers()));

            moreButton.setDisable(false);
            purchaseButton.setDisable(false);
        }
    }

    /**
     * Otvara prozor sa top 5 ruta.
     */
    private void showTopRoutes() {
        setValues();

        List<Route> topRoutes = routeService.findRoutes(from, to, strategy, 5);

        List<RouteSummary> summaries = new ArrayList<>();
        for (int i = 0; i < topRoutes.size(); i++) {
            summaries.add(new RouteSummary(i + 1, topRoutes.get(i)));
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/view/TopRoutesView.fxml"));
            Parent root = loader.load();
            TopRoutesController ctrl = loader.getController();
            ctrl.initData(routeService, topRoutes);

            Stage stage = new Stage();
            stage.setTitle("Top 5 ruta");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handler za dugme „Kupi kartu“.
     */
    public void onPurchase() {
        if (routeTable.getItems().isEmpty())
            return;
        List<RouteSegment> segments = routeTable.getItems().stream()
                .map(JourneySegment::getSegment)
                .collect(Collectors.toList());
        Route route = new Route(segments);
        try {
            Path path = ticketService.purchase(route, actualDuration);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Karta kupljena");
            alert.setHeaderText(null);
            alert.setContentText("Račun sačuvan: " + path.toString());
            alert.showAndWait();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Greska");
            alert.setHeaderText(null);
            alert.setContentText("Nije moguće generisati račun: " + e.getMessage());
            alert.showAndWait();
        }
    }
}