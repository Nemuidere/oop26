package pl.umcs.oop.server;

import pl.umcs.oop.game.Duel;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Serwer gry "papier, kamień, nożyce".
 *
 * ETAP 1: nasłuch (listen), lista połączonych klientów, dostęp do autentykacji.
 * ETAP 3: wyzywanie na pojedynek (challengeToDuel) i jego rozpoczynanie (startDuel).
 * ETAP 4: aktualizacja i wyświetlanie rankingu.
 */
public class Server {

    private final int port;

    // ETAP 1, KROK 3 — lista aktualnie połączonych klientów.
    // CopyOnWriteArrayList jest bezpieczna wątkowo: wiele wątków klientów może ją czytać
    // (np. szukając przeciwnika), a wątek nasłuchujący dopisywać — bez ręcznej synchronizacji.
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    // ETAP 1, KROK 4 — obiekt bazy danych wewnątrz serwera.
    private final Database database = new Database();

    public Server(int port) {
        this.port = port;
    }

    /**
     * ETAP 1, KROK 1 — w nieskończonej pętli przyjmuje nowych klientów.
     * KROK 2 — dla każdego klienta tworzy osobny ClientHandler (własny wątek),
     * dzięki czemu serwer obsługuje wielu klientów jednocześnie.
     * KROK 3 — nowy klient trafia na listę.
     */
    public void listen() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serwer nasłuchuje na porcie " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();      // czeka (blokuje) na klienta
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);                             // KROK 3: dodanie przy połączeniu
                handler.start();                                  // uruchamia wątek obsługi klienta
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** ETAP 1, KROK 3 — usunięcie klienta przy rozłączeniu (woła ClientHandler w finally). */
    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    /**
     * ETAP 1, KROK 4 — udostępnia ClientHandlerom metodę authenticate() bazy.
     * ClientHandler nie sięga do Database bezpośrednio, tylko prosi serwer (enkapsulacja).
     */
    public boolean authenticate(String login, String password) {
        return database.authenticate(login, password);
    }

    /**
     * ETAP 3, KROK 1-2 + KROK 7 — obsługa wyzwania.
     * Szuka na liście klienta o podanym loginie i startuje pojedynek, pilnując zasad:
     *  - nie można wyzwać samego siebie,
     *  - nie można wyzwać kogoś, kto już się pojedynkuje,
     *  - nieznany login => komunikat do wyzywającego.
     */
    public void challengeToDuel(ClientHandler challenger, String challengeeLogin) {
        // KROK 7: zakaz wyzywania samego siebie
        if (challengeeLogin.equals(challenger.getLogin())) {
            challenger.send("Nie możesz wyzwać samego siebie.");
            return;
        }

        // KROK 2: szukamy przeciwnika po loginie
        ClientHandler challengee = null;
        for (ClientHandler client : clients) {
            if (challengeeLogin.equals(client.getLogin())) {
                challengee = client;
                break;
            }
        }

        if (challengee == null) {
            challenger.send("Nie znaleziono gracza o loginie: " + challengeeLogin);
            return;
        }

        // KROK 7: zakaz wyzywania kogoś, kto już gra
        if (challengee.isDuelling()) {
            challenger.send("Gracz " + challengeeLogin + " jest już w trakcie pojedynku.");
            return;
        }

        startDuel(challenger, challengee);
    }

    /**
     * ETAP 3, KROK 3 + KROK 6 — tworzy pojedynek, informuje obu graczy o starcie,
     * a w polu onEnd ustawia logikę finału (ocena + komunikaty + ranking).
     */
    private void startDuel(ClientHandler challenger, ClientHandler challengee) {
        Duel duel = new Duel(challenger, challengee);   // ClientHandler JEST Playerem (dziedziczy)

        challenger.send("Pojedynek z " + challengee.getLogin() + " rozpoczęty! Wyślij gest: r / p / s");
        challengee.send("Pojedynek z " + challenger.getLogin() + " rozpoczęty! Wyślij gest: r / p / s");

        // KROK 6: gdy obaj zagrają, handleGesture odpali tę funkcję.
        duel.setOnEnd(() -> {
            Duel.Result result = duel.evaluate();
            if (result == null) {
                // remis
                challenger.send("Remis!");
                challengee.send("Remis!");
            } else {
                // winner/loser to Player, ale faktycznie są to ClientHandlery -> rzutujemy, by wysłać wiadomość
                ClientHandler winner = (ClientHandler) result.winner();
                ClientHandler loser  = (ClientHandler) result.loser();
                winner.send("Wygrałeś!");
                loser.send("Przegrałeś!");
                // ETAP 4, KROK 2: punkty tylko po grze nieremisowej
                database.updateLeaderboard(winner.getLogin(), loser.getLogin());
            }
            // ETAP 4, KROK 3: ranking po KAŻDEJ zakończonej grze
            printLeaderboard();
        });
    }

    /** ETAP 4, KROK 3 — wypisuje ranking na konsoli serwera (malejąco). */
    public void printLeaderboard() {
        System.out.println("=== Ranking ===");
        database.getLeaderboard().forEach(
                (login, points) -> System.out.println(login + ": " + points));
    }

    /** ETAP 1, KROK 1 — uruchomienie aplikacji. */
    public static void main(String[] args) {
        new Server(12345).listen();
    }
}
