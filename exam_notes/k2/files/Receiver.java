package pl.umcs.oop.client;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

/** Wątek wypisujący na konsolę wszystko, co przyśle serwer (dla klienta testowego). */
public class Receiver extends Thread {
    private final Socket socket;

    public Receiver(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            Scanner in = new Scanner(socket.getInputStream());
            while (in.hasNextLine()) {
                System.out.println(in.nextLine());
            }
        } catch (IOException e) {
            System.out.println("Rozłączono z serwerem.");
        }
    }
}
