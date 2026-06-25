package com.example.circleapp.server;

import com.example.circleapp.Dot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * ZADANIE 2 + 5 — serwerowa reprezentacja JEDNEGO podłączonego klienta.
 *
 * Klasa jest package-private (bez "public") — używa jej wyłącznie {@link Server} z tego pakietu.
 * To przykład tego, jak modyfikator domyślny zamyka klasę w obrębie pakietu (temat: pakiety).
 *
 * Każdy klient obsługiwany jest w osobnym wątku (extends Thread), bo readLine() blokuje
 * w oczekiwaniu na dane — nie można tym blokować obsługi pozostałych klientów.
 */
class ClientThread extends Thread {

    private final Socket socket;
    private final Server server;
    private final BufferedReader in;     // wejście: odbiór wiadomości od klienta
    private final PrintWriter out;       // wyjście: wysyłka wiadomości do klienta (auto-flush = true)

    ClientThread(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    /** Wysyła pojedynczą wiadomość do tego klienta. Wołane przez Server.broadcast(). */
    void send(String message) {
        out.println(message);
    }

    @Override
    public void run() {
        try {
            // ZAD.5 — "chwilę po dołączeniu" wyślij nowemu klientowi wszystkie zapisane koła.
            // Krótka pauza daje stronie klienta moment na uruchomienie własnego wątku nasłuchującego.
            Thread.sleep(100);
            for (Dot dot : server.getSavedDots()) {
                send(dot.toMessage());        // korzystamy z niestatycznej Dot.toMessage() (Zad.5)
            }

            // ZAD.2 — każdą otrzymaną od klienta wiadomość przekaż dalej przez broadcast serwera.
            String line;
            while ((line = in.readLine()) != null) {
                server.broadcast(line);
            }
        } catch (IOException | InterruptedException e) {
            // np. klient zamknął połączenie — wychodzimy z pętli i sprzątamy
        } finally {
            server.removeClient(this);
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
