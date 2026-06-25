package pl.umcs.oop.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testy z pakietu game (ten sam pakiet co Player/Duel), dzięki czemu mają dostęp
 * także do metod o widoczności pakietowej.
 */
class DuelTest {

    /**
     * ETAP 2, KROK 5 — po utworzeniu pojedynku obaj gracze powinni "być w pojedynku".
     */
    @Test
    void bothPlayersAreDuellingAfterDuelCreated() {
        Player p1 = new Player();
        Player p2 = new Player();

        new Duel(p1, p2);   // konstruktor woła enterDuel() dla obu

        assertTrue(p1.isDuelling());
        assertTrue(p2.isDuelling());
    }

    /**
     * ETAP 2, KROK 8 — wygrana jednego z graczy.
     * ROCK bije SCISSORS, więc winner == p1, loser == p2.
     */
    @Test
    void evaluateReturnsResultWhenSomeoneWins() {
        Player p1 = new Player();
        Player p2 = new Player();
        Duel duel = new Duel(p1, p2);

        duel.handleGesture(p1, Gesture.ROCK);
        duel.handleGesture(p2, Gesture.SCISSORS);

        Duel.Result result = duel.evaluate();

        assertEquals(p1, result.winner());
        assertEquals(p2, result.loser());
    }

    /**
     * ETAP 2, KROK 8 — remis: evaluate() zwraca null.
     */
    @Test
    void evaluateReturnsNullOnDraw() {
        Player p1 = new Player();
        Player p2 = new Player();
        Duel duel = new Duel(p1, p2);

        duel.handleGesture(p1, Gesture.PAPER);
        duel.handleGesture(p2, Gesture.PAPER);

        assertNull(duel.evaluate());
    }
}
