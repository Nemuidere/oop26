package pl.umcs.oop.game;

/**
 * ETAP 2, KROK 3-4 — uczestnik pojedynku.
 *
 * UWAGA: do tej klasy NIE wolno dopisywać własnych pól ani metod (treść zadania).
 * Schemat z PDF:
 *     -duel : Duel
 *     ? makeGesture(gesture : Gesture) : void
 *     ? enterDuel(duel : Duel) : void
 *     ? leaveDuel() : void
 *     ? isDuelling() : boolean
 * "?" = widoczność do dobrania samodzielnie — ma być MAKSYMALNIE RESTRYKCYJNA,
 * ale wystarczająca, by aplikacja działała. Uzasadnienie przy każdej metodzie.
 *
 * Klasa jest public, bo dziedziczy z niej ClientHandler z pakietu server (Etap 3).
 */
public class Player {

    // private — zgodnie ze schematem ("-duel"). Świat zewnętrzny nie rusza pola wprost.
    private Duel duel;

    /**
     * protected — wołane przez podklasę ClientHandler (inny pakiet) na rzecz `this`.
     * package-private by nie wystarczyło (ClientHandler jest w pakiecie server),
     * a public byłoby zbyt luźne. Deleguje gest do bieżącego pojedynku (Etap 2, Krok 6).
     */
    protected void makeGesture(Gesture gesture) {
        if (duel != null) {
            duel.handleGesture(this, gesture);
        }
    }

    /**
     * package-private (brak modyfikatora) — wołane wyłącznie przez klasę Duel z TEGO pakietu.
     * Ustawia bieżący pojedynek gracza (Etap 2, Krok 4).
     */
    void enterDuel(Duel duel) {
        this.duel = duel;
    }

    /**
     * package-private — wołane przez Duel (ten pakiet) po zakończeniu gry (Etap 2, Krok 9).
     * Zerowanie pola duel oznacza "gracz jest wolny".
     */
    void leaveDuel() {
        this.duel = null;
    }

    /**
     * public — wołane przez Server (inny pakiet) przy sprawdzaniu, czy można kogoś wyzwać,
     * oraz przez testy jednostkowe. Stąd musi być publiczne.
     */
    public boolean isDuelling() {
        return duel != null;
    }
}
