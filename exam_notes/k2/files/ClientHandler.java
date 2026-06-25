package pl.umcs.oop.server;

import pl.umcs.oop.game.Gesture;
import pl.umcs.oop.game.Player;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * ETAP 1 (Krok 2, 5) + ETAP 3 (Krok 1, 4) — obsługa JEDNEGO klienta po stronie serwera.
 *
 * extends Thread  -> każdy klient ma własny wątek (readLine/nextLine blokuje, więc nie można
 *                    jednym wątkiem obsługiwać wszystkich naraz).
 * extends Player  -> (ETAP 3, KROK 1) klient JEST jednocześnie graczem: dziedziczy makeGesture(),
 *                    isDuelling() itd., więc może brać udział w pojedynku.
 */
public class ClientHandler extends Player {

    private final Socket socket;
    private final Server server;
    private PrintWriter out;        // do wysyłania wiadomości do tego klienta
    private String login;           // ETAP 1, KROK 5 — zapamiętany login po autentykacji

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    /** Wysyła pojedynczą wiadomość do tego klienta (serwer korzysta z tego np. w pojedynku). */
    public void send(String message) {
        out.println(message);
    }

    public String getLogin() {
        return login;
    }

    @Override
    public void run() {
        try {
            Scanner in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);  // true = auto-flush

            // ETAP 1, KROK 5 — autentykacja: pytamy o login, potem o hasło.
            out.println("Podaj login:");
            String enteredLogin = in.nextLine();
            out.println("Podaj hasło:");
            String enteredPassword = in.nextLine();

            if (!server.authenticate(enteredLogin, enteredPassword)) {
                out.println("Błędny login lub hasło. Rozłączam.");
                return;   // blok finally usunie klienta z listy i zamknie socket
            }
            this.login = enteredLogin;   // KROK 5: zapis loginu w obiekcie klienta
            out.println("Zalogowano jako " + login + ". Wpisz login przeciwnika, aby go wyzwać.");

            // Główna pętla wiadomości od klienta.
            while (in.hasNextLine()) {
                String message = in.nextLine();

                if (isDuelling()) {
                    // ETAP 3, KROK 4 — w trakcie pojedynku wiadomość traktujemy jako gest.
                    Gesture gesture = Gesture.fromString(message);
                    if (gesture != null) {
                        makeGesture(gesture);   // odziedziczona z Player; wywoła handleGesture pojedynku
                    }
                    // inne napisy niż r/p/s -> ignorujemy (fromString zwrócił null)
                } else {
                    // ETAP 3, KROK 1 — poza pojedynkiem wiadomość to login do wyzwania.
                    server.challengeToDuel(this, message);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // ETAP 1, KROK 3 — usunięcie klienta przy rozłączeniu.
            server.removeClient(this);
            try {
                socket.close();
            } catch (IOException ignored) {
            }
            System.out.println("Rozłączono: " + login);
        }
    }
}
