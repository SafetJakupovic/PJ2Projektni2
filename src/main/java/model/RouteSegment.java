package main.java.model;

/**
 * Omotač nad jednim Departure-om unutar rute.
 * Rute se sastoje od jedne ili više segmenataeute.
 * Departure nosi samo podatke o jednom  polasku, dok RouteSegment enkapsulira tu vožnju unutar konteksta rute.
 */
public class RouteSegment {
    /**
     * Polazak koji predstavlja ovaj segmet (departure).
     */
    private final Departure departure;

    /**
     * @param departure jedan od segmenata puta
     */
    public RouteSegment(Departure departure) {
        this.departure = departure;
    }

    /**
     * Vraća departure
     *
     * @return trenutni segment vožnje
     */
    public Departure getDeparture() {
        return departure;
    }

    /**
     * Ispis
     *
     * @return segment rute kao string
     */
    @Override
    public String toString() {
        return departure.toString();
    }

    /**
     * Vraća da li su objekti jednaki
     *
     * @param o drugi objekat
     * @return da li su objekti jednaki
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RouteSegment))
            return false;
        RouteSegment that = (RouteSegment) o;
        return departure.equals(that.departure);
    }

    /**
     * Vraća hash kod objekta
     *
     * @return hash kod objekta
     */
    @Override
    public int hashCode() {
        return departure.hashCode();
    }
}
