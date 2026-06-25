package com.example.circleapp;

import com.example.circleapp.client.ServerThread;
import com.example.circleapp.server.Server;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Punkt wejścia aplikacji JavaFX.
 *
 * Dla uproszczenia demonstracji w jednym procesie startuje serwer ORAZ łączy się z nim
 * jeden klient (ServerThread). Uruchamiając aplikację kolejny raz na drugim komputerze
 * (z ServerThread wskazującym IP serwera), zobaczysz koła rysowane przez innych — bo wszystko
 * przechodzi przez serwer i jego broadcast.
 */
public class Main extends Application {

    private static final int PORT = 12345;
    private Server server;

    @Override
    public void start(Stage stage) throws Exception {
        // 1) Serwer: łączy się z bazą i startuje pętlę akceptującą klientów (Zad.2 i 5).
        server = new Server(PORT, "dots.db");
        server.start();

        // 2) Klient: ServerThread nawiązuje połączenie z serwerem (Zad.2 i 4).
        ServerThread serverThread = new ServerThread("localhost", PORT);

        // 3) ZAD.4 — kontroler dostaje Server i ServerThread przez konstruktor.
        //    Konstruktor z parametrami => ustawiamy kontroler ręcznie (zamiast fx:controller).
        FXMLLoader loader = new FXMLLoader(getClass().getResource("app-view.fxml"));
        loader.setController(new Controller(server, serverThread));

        Scene scene = new Scene(loader.load());
        stage.setTitle("Circle App");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        if (server != null) server.shutdown();   // zamknięcie socketu i bazy przy zamykaniu okna
    }

    public static void main(String[] args) {
        launch(args);
    }
}
