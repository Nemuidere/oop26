package com.example.circleapp.server;

import com.example.circleapp.Dot;
import database.DatabaseConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ZADANIE 2 + 5 — serwer.
 *
 * - Posiada ServerSocket, do którego łączą się klienci.
 * - Każdy podłączony klient jest reprezentowany przez obiekt {@link ClientThread}.
 * - broadcast() rozsyła wiadomość do wszystkich klientów (Zad.2)
 *   oraz zapisuje koło do bazy (Zad.5).
 * - Przy starcie łączy się z bazą i tworzy tabelę dot (Zad.5).
 */
public class Server {

    private final int port;

    // Lista podłączonych klientów. CopyOnWriteArrayList jest bezpieczna wątkowo:
    // wątek akceptujący dopisuje klientów, a broadcast() iteruje po liście — bez ręcznej synchronizacji iteracji.
    private final List<ClientThread> clients = new CopyOnWriteArrayList<>();

    private final DatabaseConnection db = new DatabaseConnection();

    private ServerSocket serverSocket;
    private volatile boolean running;     // volatile: zmieniane przez shutdown(), czytane w pętli wątku akceptującego

    /**
     * Konstruktor: ZAD.5 — serwer łączy się z bazą i przygotowuje tabelę już na starcie.
     */
    public Server(int port, String dbPath) throws SQLException {
        this.port = port;
        db.connect(dbPath);
        initSchema();
    }

    /** ZAD.5 — tabela z treści zadania (dodane IF NOT EXISTS, by można było odpalać wielokrotnie). */
    private void initSchema() throws SQLException {
        try (Statement st = db.getConnection().createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS dot(
                    id     INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    x      INTEGER NOT NULL,
                    y      INTEGER NOT NULL,
                    color  TEXT NOT NULL,
                    radius INTEGER NOT NULL
                )
            """);
        }
    }

    /** Uruchamia pętlę akceptującą w osobnym wątku (żeby nie blokować wątku UI JavaFX). */
    public void start() {
        running = true;
        Thread acceptThread = new Thread(this::acceptLoop, "server-accept");
        acceptThread.setDaemon(true);   // wątek-demon: nie wstrzymuje zamknięcia aplikacji
        acceptThread.start();
    }

    private void acceptLoop() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Serwer nasłuchuje na porcie " + port);
            while (running) {
                Socket socket = serverSocket.accept();          // czeka na nowego klienta
                ClientThread client = new ClientThread(socket, this);
                clients.add(client);
                client.start();                                  // od tej chwili klient ma własny wątek
            }
        } catch (IOException e) {
            if (running) System.err.println("Błąd serwera: " + e.getMessage());
            // gdy running == false, wyjątek to skutek zamknięcia socketu w shutdown() — ignorujemy
        }
    }

    /**
     * ZAD.2 — rozsyła otrzymaną wiadomość do WSZYSTKICH klientów.
     * ZAD.5 — dodatkowo zapisuje koło do bazy.
     * synchronized: kolejne wywołania (z różnych wątków klienckich) nie nachodzą na siebie przy zapisie.
     */
    public synchronized void broadcast(String message) {
        saveDot(Dot.fromMessage(message));            // Zad.5: zapis do bazy
        for (ClientThread client : clients) {
            client.send(message);                     // rozsyłka do każdego klienta
        }
    }

    /** ZAD.5 — zapis pojedynczego koła do tabeli dot. */
    public void saveDot(Dot dot) {
        String sql = "INSERT INTO dot(x, y, color, radius) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, dot.x());
            ps.setInt(2, dot.y());
            ps.setString(3, dot.color());
            ps.setInt(4, dot.radius());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Nie udało się zapisać koła: " + e.getMessage(), e);
        }
    }

    /** ZAD.5 — odczyt wszystkich zapisanych kół jako listy Dot. */
    public List<Dot> getSavedDots() {
        List<Dot> dots = new ArrayList<>();
        String sql = "SELECT x, y, color, radius FROM dot ORDER BY id";
        try (Statement st = db.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                dots.add(new Dot(
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getString("color"),
                        rs.getInt("radius")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Nie udało się odczytać kół: " + e.getMessage(), e);
        }
        return dots;
    }

    /** Usuwa klienta z listy (wołane przez ClientThread przy rozłączeniu). */
    void removeClient(ClientThread client) {
        clients.remove(client);
    }

    /** Porządne zamknięcie: zatrzymuje pętlę, zamyka socket i bazę (wołane z Main.stop()). */
    public void shutdown() throws SQLException {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {
        }
        db.disconnect();
    }
}
