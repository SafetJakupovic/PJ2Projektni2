package main.java.model;

import java.util.Objects;

/**
 * Predstavlja stanicu (autobusku ili željezničku) unutar grada.
 * Svaka stanica je vezana uz svoj City, ima jedinstveni kod i tip prevoza.
 */
public class Station {
    /**
     * Grad u kojem se nalazi ova stanica.
     */
    private final City city;
    /**
     * Jedinstveni kod stanice (npr. "A_1_2" ili "Z_3_4").
     */
    private final String code;
    /**
     * Tip stanice (BUS ili RAIL).
     */
    private final TransportType type;

    /**
     * Konstruktor.
     *
     * @param city grad u kojem se stanica nalazi
     * @param code jedinstveni kod stanice
     * @param type tip prevoza (BUS ili RAIL)
     */
    public Station(City city, String code, TransportType type) {
        this.city = city;
        this.code = code;
        this.type = type;
    }

    /**
     * Vraća city
     *
     * @return grad kojem pripada ova stanica
     */
    public City getCity() {
        return city;
    }

    /**
     * Vraaća code
     *
     * @return jedinstveni kod stanice
     */
    public String getCode() {
        return code;
    }

    /**
     * Vraća type
     *
     * @return tip prevoza na ovoj stanici
     */
    public TransportType getType() {
        return type;
    }

    /**
     * Ispis
     *
     * @return staica kao string
     */
    @Override
    public String toString() {
        return "Station{" +
                "code='" + code + '\'' +
                ", city=" + city.getName() +
                ", type=" + type +
                '}';
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
        if (!(o instanceof Station))
            return false;
        Station other = (Station) o;
        return code.equals(other.code) && type == other.type && city.equals(other.city);
    }

    /**
     * Vraća hash kod objekta
     *
     * @return hash kod objekta
     */
    @Override
    public int hashCode() {
        return Objects.hash(code, type, city);
    }
}
