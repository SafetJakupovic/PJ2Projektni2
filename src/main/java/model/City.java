package main.java.model;

/**
 * Predstavlja grad u mreži države identifikovan koordinatama (row, col) i imenom.
 * Koristi se za mapiranje lokacija stanica u TransportGraph i na interaktivnoj mapi.
 */
public class City {
    /**
     * Redni broj (x-koordinata) grada u matrici.
     */
    private final int row;
    /**
     * Kolona (y-koordinata) grada u matrici.
     */
    private final int col;
    /**
     * Jedinstveno ime grada (npr. "G_2_3").
     */
    private final String name;

    /**
     * Konstruktor.
     *
     * @param row  broj reda u matričnom rasporedu
     * @param col  broj kolone u matričnom rasporedu
     * @param name jedinstveno ime grada
     */
    public City(int row, int col, String name) {
        this.row = row;
        this.col = col;
        this.name = name;
    }

    /**
     * Vraća row
     *
     * @return broj reda u matričnom rasporedu
     */
    public int getRow() {
        return row;
    }

    /**
     * Vraća col
     *
     * @return broj kolone u matričnom rasporedu
     */
    public int getCol() {
        return col;
    }

    /**
     * Vraća name
     *
     * @return jedinstveno ime grada
     */
    public String getName() {
        return name;
    }

    /**
     * Ispis
     *
     * @return tekstualni opis grada
     */
    @Override
    public String toString() {
        return "City{name='" + name + "', row=" + row + ", col=" + col + '}';
    }

    /**
     * Gradovi su jednaki ako imaju iste koordinate i ime.
     *
     * @param o drugi objekat
     * @return da li su objekti jednaki
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof City))
            return false;
        City city = (City) o;
        return row == city.row && col == city.col && name.equals(city.name);
    }

    /**
     * Vraća hashCode na osnovu reda, kolone i imena
     *
     * @return generiše hash na osnovu reda, kolone i imena.
     */
    @Override
    public int hashCode() {
        int result = Integer.hashCode(row);
        result = 31 * result + Integer.hashCode(col);
        result = 31 * result + name.hashCode();
        return result;
    }
}
