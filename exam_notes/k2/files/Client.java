package pl.umcs.oop.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * POMOCNICZY klient konsolowy — NIE jest częścią zadania kolokwium (serwer można testować
 * też przez `telnet localhost 12345` albo `nc localhost 12345`). Dodany, żeby łatwo było
 * sprawdzić działanie: uruchom serwer, potem dwa razy tego klienta (np. alice/s i bob/e),
 * wpisz login przeciwnika u jednego z nich, a następnie u obu gest r/p/s.
 */
public class Client {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345)) {
            System.out.println("Połączono z serwerem.");
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner consoleIn = new Scanner(System.in);

            // osobny wątek na odbiór komunikatów z serwera
            new Receiver(socket).start();

            // wszystko z konsoli wysyłamy do serwera (login, hasło, login przeciwnika, gesty)
            while (consoleIn.hasNextLine()) {
                String line = consoleIn.nextLine();
                if (line.equals("exit")) {
                    break;
                }
                out.println(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
