package main.java.util;

import javafx.beans.NamedArg;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import main.java.data.LoadedTransportData;
import main.java.model.City;
import main.java.model.RouteSegment;
import main.java.model.Station;
import main.java.model.TransportType;


import java.util.List;
import java.util.function.BiConsumer;

/**
 * Platno za prikaz interaktivne mape države:
 * - iscrtava gradove u matrici (countryMap),
 * - iscrtava autobuske/željezničke stanice,
 * - meijnja boju izabranih polazišta/destinacija,
 * - može da obilježi optimalni put (highlightPath),
 * - prijem događaja klika na stanicu (handleClick).
 */
public class MapCanvas extends Canvas {
    /**
     * Matrica gradova n x m za crtanje.
     */
    private City[][] countryMap;
    /**
     * Lista svih stanica (autobus + voz).
     */
    private List<Station> stations;
    /**
     * Broj redova u matrici.
     */
    private int rows;
    /**
     * Broj kolona u matrici.
     */
    private int cols;
    /**
     * Margina (px) između ivica i prvog/red poslednjeg grada.
     */
    private double margin = 20;
    /**
     * Callback pri izboru stanice (stanica, true=source/false=dest).
     */
    private BiConsumer<Station, Boolean> onStationSelected;
    /**
     * Trenutno izabrana polazna stanica.
     */
    private Station selectedSource;
    /**
     * Trenutno izabrana odredišna stanica.
     */
    private Station selectedDest;
    /**
     * Ruta koju treba obilježiti na mapi.
     */
    private List<RouteSegment> highlightedPath;
    /**
     * Toggle: sledeći klik bira željezničku (true) ili autobusku (false) stanicu.
     */
    private boolean changeStation = false;

    /**
     * Podrazumijevane dimenzije platna 600×600 i inicijalizacija.
     */
    public MapCanvas() {
        super(600, 600);
        init();
    }

    /**
     * Konstruktor s podešenom širinom/visinom, za FXML-injekciju @NamedArg.
     */
    public MapCanvas(@NamedArg("width") double width, @NamedArg("height") double height) {
        super(width, height);
        init();
    }

    /**
     * Jednokratna inicijalizacija: skaliranje i klik-događaj.
     */
    private void init() {
        widthProperty().addListener(evt -> drawMap());
        heightProperty().addListener(evt -> drawMap());
        setOnMouseClicked(this::handleClick);
    }

    /**
     * Overriduje isResizable da vraća true
     *
     * @return da li se može mijenjati veličina (može)
     */
    @Override
    public boolean isResizable() {
        return true;
    }

    /**
     * Vraća getWidth rezultat iz Canvas klase
     *
     * @param height visina platna
     * @return rezulzaz getWidth
     */
    @Override
    public double prefWidth(double height) {
        return getWidth();
    }

    /**
     * Vraća getHeight rezultat iz Canvas klase
     *
     * @param width visina platna
     * @return rezulzaz getHeight
     */
    @Override
    public double prefHeight(double width) {
        return getHeight();
    }

    /**
     * Učitava mapu i stanice, postavlja rows/cols i iscrtava.
     *
     * @param ltd učitani json fajl podaci
     */
    public void initialize(LoadedTransportData ltd) {
        this.countryMap = ltd.getCountryMap();
        this.stations = ltd.getStations();
        this.rows = countryMap.length;
        this.cols = countryMap[0].length;
        drawMap();
    }


    /**
     * Postavlja listu segmenata puta koje treba obilježiti zelenom bojom i osvježava prikaz.
     *
     * @param segments lista segmenata rute
     */
    public void highlightPath(List<RouteSegment> segments) {
        this.highlightedPath = segments;
        drawMap();
    }

    /**
     * Glavna rutina iscrtavanja:
     * 1) pozadinske linije mreže
     * 2) imena gradova
     * 3) obilježavanje izabranih (source/dest)
     * 4) markeri stanica (plavo/crveno)
     * 5) linije puta (ako je highlightPath!=null)
     */
    private void drawMap() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());
        if (countryMap == null) return;

        double w = getWidth() - 2 * margin;
        double h = getHeight() - 2 * margin;
        double cellW = cols > 1 ? w / (cols - 1) : w;
        double cellH = rows > 1 ? h / (rows - 1) : h;

        // iscrtaj mrežu
        gc.setStroke(Color.LIGHTGRAY);
        for (int i = 0; i < rows; i++) {
            double y = margin + i * cellH;
            gc.strokeLine(margin, y, margin + w, y);
        }
        for (int j = 0; j < cols; j++) {
            double x = margin + j * cellW;
            gc.strokeLine(x, margin, x, margin + h);
        }

        // imena gradova +  selekcija
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double x = margin + j * cellW;
                double y = margin + i * cellH;
                City city = countryMap[i][j];

                // pozicija teksta: lijevo ili desno pri marginama
                String name = city.getName();
                Text helper = new Text(name);
                helper.setFont(gc.getFont());
                double textWidth = helper.getLayoutBounds().getWidth();
                double txtX;
                if (j == 0)
                    txtX = x + 4;
                else if (j == cols - 1)
                    txtX = x - textWidth - 4;
                else
                    txtX = x + 4;
                double txtY = y - 4;

                // selektovani source/dest
                if (selectedSource != null && city.equals(selectedSource.getCity())) {
                    gc.setFill(Color.rgb(0, 200, 0, 0.3));
                    gc.fillOval(x - cellW / 2, y - cellH / 2, cellW, cellH);
                } else if (selectedDest != null && city.equals(selectedDest.getCity())) {
                    gc.setFill(Color.rgb(200, 0, 0, 0.3));
                    gc.fillOval(x - cellW / 2, y - cellH / 2, cellW, cellH);
                }

                gc.setFill(Color.BLACK);
                gc.fillText(city.getName(), txtX, txtY);
            }
        }
        // markeri stanica
        for (Station s : stations) {
            City c = s.getCity();
            int i = c.getRow(), j = c.getCol();
            double x = margin + j * cellW;
            double y = margin + i * cellH;
            gc.setFill(s.getType() == TransportType.BUS ? Color.BLUE : Color.RED);
            gc.fillRect(x - 4, y - 4, 8, 8);
            gc.setStroke(Color.BLACK);
            gc.strokeRect(x - 4, y - 4, 8, 8);
        }

        // obilježavanje puta
        if (highlightedPath != null) {
            gc.setStroke(Color.GREEN);
            gc.setLineWidth(3);
            for (RouteSegment seg : highlightedPath) {
                Station from = seg.getDeparture().getFrom();
                Station to = seg.getDeparture().getTo();
                City c1 = from.getCity(), c2 = to.getCity();
                double x1 = margin + c1.getCol() * cellW;
                double y1 = margin + c1.getRow() * cellH;
                double x2 = margin + c2.getCol() * cellW;
                double y2 = margin + c2.getRow() * cellH;
                gc.strokeLine(x1, y1, x2, y2);
            }
        }
    }

    /**
     * Rukuje klikom miša:
     * - pretvara koordinate u indeks grada,
     * - bira nasumično A_ ili Z_ stanicu,
     * - poziva callback onStationSelected.
     *
     * @param e aktivnost miša
     */
    private void handleClick(MouseEvent e) {
        if (countryMap == null) return;
        double mx = e.getX(), my = e.getY();
        double w = getWidth() - 2 * margin;
        double h = getHeight() - 2 * margin;
        double cellW = cols > 1 ? w / (cols - 1) : w;
        double cellH = rows > 1 ? h / (rows - 1) : h;

        int j = (int) Math.round((mx - margin) / cellW);
        int i = (int) Math.round((my - margin) / cellH);
        if (i < 0 || i >= rows || j < 0 || j >= cols) return;

        String prefix = changeStation ? "Z" : "A";
        String code = prefix + "_" + i + "_" + j;
        changeStation = !changeStation;

        Station chosen = stations.stream()
                .filter(s -> s.getCode().equals(code))
                .findFirst().orElse(null);
        if (chosen != null && onStationSelected != null) {
            boolean isSource = e.getButton() == MouseButton.PRIMARY;
            if (isSource) selectedSource = chosen;
            else selectedDest = chosen;
            drawMap();
            onStationSelected.accept(chosen, isSource);
        }
    }


    /**
     * Postavlja callback koji prosljeđuje:
     * (izabrana stanica, true=source/false=dest).
     *
     * @param callback  Funkcija koja prima stanicu i boolean vrijednost — true ako je stanica izabrana kao početna, false ako je izabrana kao krajnja.
     */
    public void setOnStationSelected(BiConsumer<Station, Boolean> callback) {
        this.onStationSelected = callback;
    }
}