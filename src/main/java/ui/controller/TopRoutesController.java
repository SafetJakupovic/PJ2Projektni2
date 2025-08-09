package main.java.ui.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import main.java.model.Route;
import main.java.service.JourneySegment;
import main.java.service.RouteService;
import main.java.service.RouteSummary;
import main.java.service.TicketService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Kontroler za TopRoutesView: prikazuje listu RouteSummary i dugmad za kupovinu.
 */
public class TopRoutesController {
    /**
     * Tabela top-ruta.
     */
    @FXML
    private TableView<RouteSummary> summaryTable;
    /**
     * Kolona rednog broja rute.
     */
    @FXML
    private TableColumn<RouteSummary, Number> rankCol;
    /**
     * Kolona trajanja rute.
     */
    @FXML
    private TableColumn<RouteSummary, Number> durationCol;
    /**
     * Kolona cijene rute.
     */
    @FXML
    private TableColumn<RouteSummary, Number> priceCol;
    /**
     * Kolona broja presjedanja.
     */
    @FXML
    private TableColumn<RouteSummary, Number> transfersCol;
    /**
     * Kolona dugmeta “Kupi kartu”.
     */
    @FXML
    private TableColumn<RouteSummary, Void> actionCol;

    /**
     * Za ponovno anotiranje i kupovinu.
     */
    private RouteService routeService;
    /**
     * Dozvoljava kupovinu karata.
     */
    private TicketService ticketService = new TicketService();

    /**
     * Inicijalizuje tabelu podacima i postavlja dugmad „Kupi kartu“.
     *
     * @param routeService instanca servisa za ponovni izračun (ako treba)
     * @param topRoutes    lista top 5 ruta
     */
    public void initData(RouteService routeService, List<Route> topRoutes) {
        this.routeService = routeService;

        List<RouteSummary> summaries = new ArrayList<>();

        for (int i = 0; i < topRoutes.size(); i++) {
            Route route = topRoutes.get(i);
            List<JourneySegment> segments = routeService.annotateRoute(route);
            if (segments.isEmpty())
                continue;

            int start = segments.get(0).getDepartureMinuteAbs();
            int end = segments.get(segments.size() - 1).getArrivalMinuteAbs();
            long actualMin = end - start;

            summaries.add(new RouteSummary(i + 1, route, actualMin));
        }

        rankCol.setCellValueFactory(cell -> cell.getValue().rankProperty());
        durationCol.setCellValueFactory(cell -> cell.getValue().actualDurationProperty());
        priceCol.setCellValueFactory(cell -> cell.getValue().priceProperty());
        transfersCol.setCellValueFactory(cell -> cell.getValue().transfersProperty());

        actionCol.setCellFactory(getActionCellFactory());
        actionCol.setCellValueFactory(cellData -> null);

        summaryTable.setItems(FXCollections.observableArrayList(summaries));
    }

    /**
     * Pravljenje ćelije koja sadrži dugme „Kupi kartu“ za svaku rutu.
     *
     * @return actionCellFactory
     */
    private Callback<TableColumn<RouteSummary, Void>, TableCell<RouteSummary, Void>> getActionCellFactory() {
        return col -> new TableCell<>() {
            private final Button purchaseButton = new Button("Kupi kartu");

            {
                purchaseButton.setOnAction(event -> {
                    RouteSummary summary = getTableView().getItems().get(getIndex());
                    try {
                        Path path = ticketService.purchase(summary.getRoute(), summary.actualDurationProperty().get());
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
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty)
                    setGraphic(null);
                else
                    setGraphic(purchaseButton);
            }
        };
    }

    /**
     * Zatvara prozor TopRoutesView.
     */
    @FXML
    private void onClose() {
        ((Stage) summaryTable.getScene().getWindow()).close();
    }
}