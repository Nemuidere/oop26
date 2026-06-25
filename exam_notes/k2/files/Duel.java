package pl.umcs.oop.game;

/**
 * ETAP 2 (Krok 4, 6, 7, 9) + ETAP 3 (Krok 5) — pojedyncza gra między dwoma graczami.
 */
public class Duel {

    private final Player player1;
    private final Player player2;

    // Gesty graczy. null = gracz jeszcze nie zagrał.
    private Gesture gesture1;
    private Gesture gesture2;

    /**
     * ETAP 3, KROK 5 — pole "co zrobić, gdy pojedynek się skończy".
     * Runnable to gotowy interfejs funkcyjny: metoda run() nic nie przyjmuje i nic nie zwraca
     * (dokładnie tego wymaga treść). Domyślnie pusty (() -> {}), żeby uniknąć NullPointerException,
     * dopóki ktoś (Server) nie ustawi własnej funkcji przez setOnEnd().
     */
    private Runnable onEnd = () -> { };

    /**
     * KROK 4 — przyjmuje obu uczestników i od razu "wprowadza" ich w ten pojedynek
     * (enterDuel ustawia im pole duel na ten obiekt).
     */
    public Duel(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        player1.enterDuel(this);
        player2.enterDuel(this);
    }

    /**
     * KROK 7 — zagnieżdżony rekord z wynikiem: kto wygrał, kto przegrał.
     * Zagnieżdżony, bo Result ma sens tylko w kontekście pojedynku.
     */
    public record Result(Player winner, Player loser) { }

    /** ETAP 3, KROK 5 — mutator pola onEnd. */
    public void setOnEnd(Runnable onEnd) {
        this.onEnd = onEnd;
    }

    /**
     * KROK 6 — przyjmuje gest od konkretnego gracza i zapisuje go we właściwym polu.
     * Gdy obaj gracze już zagrali, odpala onEnd (Etap 3, Krok 5).
     */
    public void handleGesture(Player player, Gesture gesture) {
        if (player == player1) {
            gesture1 = gesture;
        } else if (player == player2) {
            gesture2 = gesture;
        }
        if (gesture1 != null && gesture2 != null) {
            onEnd.run();
        }
    }

    /**
     * KROK 7 — ocenia wynik: zwraca Result przy wygranej, a null przy remisie.
     * KROK 9 — to jest właściwe miejsce na leaveDuel(): pojedynek się skończył,
     * więc zwalniamy obu graczy (ich pole duel wraca do null).
     */
    public Result evaluate() {
        int comparison = gesture1.compareWith(gesture2);

        Result result;
        if (comparison == 0) {
            result = null;                              // remis
        } else if (comparison > 0) {
            result = new Result(player1, player2);      // wygrał gracz 1
        } else {
            result = new Result(player2, player1);      // wygrał gracz 2
        }

        // Koniec gry -> zwolnij graczy (Krok 9).
        player1.leaveDuel();
        player2.leaveDuel();
        return result;
    }
}
