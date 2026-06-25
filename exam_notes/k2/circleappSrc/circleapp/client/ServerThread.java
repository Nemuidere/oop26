package com.example.circleapp.client;

import com.example.circleapp.Dot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * ZADANIE 2 + 4 — kliencka reprezentacja połączenia z serwerem.
 *
 * Nazwa "ServerThread" znaczy: "wątek, który po stronie klienta obsługuje serwer".
 *  - send(...)  -> wysyła parametry koła do serwera (Zad.2),
 *  - listen()   -> w osobnym wątku odbiera koła z serwera i przekazuje je konsumentowi (Zad.4).
 *
 * ZAD.4 — pole typu Consumer<Dot> + mutator. Dzięki temu klasa sieciowa NIE wie nic o JavaFX
 * ani o rysowaniu. Wstrzykujemy jej zachowanie z zewnątrz (kontroler ustawi "narysuj koło").
 * To luźne powiązanie (separacja warstwy sieciowej od warstwy UI).
 */
public class ServerThread {

    private final BufferedReader in;
    private final PrintWriter out;

    // ZAD.4 — pole konsumenta. Domyślnie "nic nie rób", żeby nie było NullPointerException,
    // zanim ktoś ustawi właściwe zachowanie przez setOnDot().
    private Consumer<Dot> onDot = dot -> { };

    public ServerThread(String host, int port) throws IOException {
        Socket socket = new Socket(host, port);                  // nawiązanie połączenia z serwerem
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);

        // Odbiór musi działać równolegle z UI, więc nasłuchujemy w osobnym wątku-demonie.
        Thread listener = new Thread(this::listen, "server-listener");
        listener.setDaemon(true);
        listener.start();
    }

    /** ZAD.4 — mutator pola konsumenta. */
    public void setOnDot(Consumer<Dot> onDot) {
        this.onDot = onDot;
    }

    /** ZAD.2/4 — wysyła do serwera parametry koła (zamiast rysować je lokalnie). */
    public void send(int x, int y, String color, int radius) {
        out.println(Dot.toMessage(x, y, color, radius));
    }

    /** Wątek nasłuchujący: dla każdego koła otrzymanego z serwera woła konsumenta. */
    private void listen() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                onDot.accept(Dot.fromMessage(line));   // ZAD.4: konsument dla danych każdego koła
            }
        } catch (IOException e) {
            System.err.println("Utracono połączenie z serwerem: " + e.getMessage());
        }
    }
}
