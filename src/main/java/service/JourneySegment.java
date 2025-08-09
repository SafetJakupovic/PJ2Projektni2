package main.java.service;

import main.java.model.RouteSegment;

/**
 * Obogaćuje RouteSegment informacijama o apsolutnom vremenu polaska i dolaska (u minutama od početka prvog dana).
 * Koristi se za prikaz u tabelama i za izračun stvarnog trajanja.
 */
public class JourneySegment {
    /**
     * Originalni segment rute.
     */
    private final RouteSegment segment;
    /**
     * Apsolutno vreme polaska (u minutima od početka prvog dana).
     */
    private final int departureMinuteAbs;
    /**
     * Apsolutno vreme dolaska (polazak + trajanje).
     */
    private final int arrivalMinuteAbs;

    /**
     * Konstruktor računa apsolutno vrijeme dolaska na temelju trajanja.
     *
     * @param segment            originalni RouteSegment
     * @param departureMinuteAbs apsolutno vrijeme polaska u minutama
     */
    public JourneySegment(RouteSegment segment, int departureMinuteAbs) {
        this.segment = segment;
        this.departureMinuteAbs = departureMinuteAbs;
        this.arrivalMinuteAbs = departureMinuteAbs
                + (int) segment.getDeparture().getDuration().toMinutes();
    }

    /**
     * Vraća segment
     *
     * @return originalni segment
     */
    public RouteSegment getSegment() {
        return segment;
    }

    /**
     * Vraća departureMinuteAbs
     *
     * @return apsolutno vrijeme polaska (u minutama)
     */
    public int getDepartureMinuteAbs() {
        return departureMinuteAbs;
    }

    /**
     * Vraća arrivalMinuteAbs
     *
     * @return apsolutno vrijeme dolaska (u minutama)
     */
    public int getArrivalMinuteAbs() {
        return arrivalMinuteAbs;
    }

    /**
     * Vraća tip prevoza
     *
     * @return tip prevoza kao tekst (BUS ili RAIL)
     */
    public String getType() {
        return segment.getDeparture().getType().name();
    }

    /**
     * @return trajanje segmenta u minutama
     */
    public long getDurationMinutes() {
        return segment.getDeparture().getDuration().toMinutes();
    }

    /**
     * Vraća cijenu segmenta
     *
     * @return cijena segmenta
     */
    public int getPrice() {
        return segment.getDeparture().getPrice();
    }
}
