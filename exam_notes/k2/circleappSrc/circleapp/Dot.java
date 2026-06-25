package com.example.circleapp;

/**
 * ZADANIE 3 + 5 — model pojedynczego koła ("kropki").
 *
 * record = niemutowalny nosiciel danych. Kompilator sam generuje:
 *   - konstruktor kanoniczny Dot(int, int, String, int),
 *   - akcesory: x(), y(), color(), radius()   (UWAGA: bez przedrostka get),
 *   - equals(), hashCode(), toString().
 *
 * Kolor trzymamy jako String w formacie hex "#RRGGBB" — łatwo go przesłać siecią
 * i zapisać do kolumny TEXT w bazie, a po stronie UI odczytać przez Color.web(...).
 */
public record Dot(int x, int y, String color, int radius) {

    // Separator pól w wiadomości. Hex koloru nie zawiera ';', więc split jest bezpieczny.
    private static final String SEP = ";";

    /**
     * ZAD. 3 — metoda STATYCZNA: skleja luźne parametry w jeden String do wysłania siecią.
     * Format: "x;y;#RRGGBB;radius", np. "120;80;#FF0000;25".
     * Statyczna, bo działa na surowych danych — nie potrzebuje istniejącego obiektu Dot.
     */
    public static String toMessage(int x, int y, String color, int radius) {
        return x + SEP + y + SEP + color + SEP + radius;
    }

    /**
     * ZAD. 3 — metoda STATYCZNA odwrotna: odczytuje powyższy String i buduje z niego obiekt Dot.
     * To "fabryka statyczna" — typowy wzorzec do tworzenia obiektu z formatu zewnętrznego.
     */
    public static Dot fromMessage(String message) {
        String[] parts = message.split(SEP);
        return new Dot(
                Integer.parseInt(parts[0]),   // x
                Integer.parseInt(parts[1]),   // y
                parts[2],                     // color (#RRGGBB)
                Integer.parseInt(parts[3])    // radius
        );
    }

    /**
     * ZAD. 5 — pomocnicza, NIESTATYCZNA metoda: buduje wiadomość z pól TEGO obiektu.
     * Wygodna, gdy mamy już gotowy Dot (np. odczytany z bazy) i chcemy go rozesłać.
     * Deleguje do wersji statycznej, żeby format był w jednym miejscu.
     */
    public String toMessage() {
        return toMessage(x, y, color, radius);
    }
}
