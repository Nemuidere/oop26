package pl.umcs.oop.game;

/**
 * ETAP 2, KROK 1-2 — gest w grze.
 *
 * enum = typ wyliczeniowy o skończonym zbiorze wartości (tu: trzy gesty).
 * Każda wartość (ROCK/PAPER/SCISSORS) to gotowy, jedyny obiekt tego typu (singleton),
 * więc można je bezpiecznie porównywać przez == .
 */
public enum Gesture {
    ROCK,
    PAPER,
    SCISSORS;

    /**
     * KROK 1 — zamienia napis z sieci na gest: "r"->ROCK, "p"->PAPER, "s"->SCISSORS.
     * Dla nieznanych napisów zwraca null — dzięki temu serwer łatwo zignoruje
     * wiadomości inne niż "r"/"p"/"s" (Etap 3, Krok 4).
     */
    public static Gesture fromString(String text) {
        return switch (text) {
            case "r" -> ROCK;
            case "p" -> PAPER;
            case "s" -> SCISSORS;
            default -> null;
        };
    }

    /**
     * KROK 2 — porównuje TEN gest z innym:
     *   0  -> remis (te same gesty),
     *   1  -> ten gest wygrywa z argumentem,
     *  -1  -> ten gest przegrywa.
     * Zasada: ROCK bije SCISSORS, PAPER bije ROCK, SCISSORS bije PAPER.
     * (Treść: ROCK < PAPER, PAPER < SCISSORS, SCISSORS < ROCK — czyli "<" znaczy "przegrywa z".)
     */
    public int compareWith(Gesture other) {
        if (this == other) {
            return 0;
        }
        return switch (this) {
            case ROCK     -> (other == SCISSORS) ? 1 : -1;
            case PAPER    -> (other == ROCK)     ? 1 : -1;
            case SCISSORS -> (other == PAPER)    ? 1 : -1;
        };
    }
}
