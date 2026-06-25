package com.example.circleapp;

import com.example.circleapp.client.ServerThread;
import com.example.circleapp.server.Server;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

/**
 * ZADANIE 1 + 4 — kontroler okna.
 *
 * Pola @FXML są wstrzykiwane przez FXMLLoader na podstawie fx:id z pliku app-view.fxml.
 *
 * UWAGA (Zad.4): kontroler ma KONSTRUKTOR Z PARAMETRAMI (Server, ServerThread). To ważne,
 * bo standardowo FXML tworzy kontroler bezargumentowo. Dlatego w Main NIE używamy atrybutu
 * fx:controller, tylko ręcznie podajemy instancję: loader.setController(new Controller(...)).
 */
public class Controller {

    @FXML private Canvas canvas;
    @FXML private ColorPicker colorPicker;
    @FXML private Slider radiusSlider;

    // server jest wstrzykiwany zgodnie z treścią Zad.4. W tym wariancie serwer startuje w Main,
    // więc pole służy głównie jako referencja (np. do zamknięcia czy odpytania bazy).
    private final Server server;
    private final ServerThread serverThread;

    public Controller(Server server, ServerThread serverThread) {
        this.server = server;
        this.serverThread = serverThread;

        // ZAD.4 — konstruktor USTAWIA konsumenta tak, by robił to, co dawna metoda rysująca.
        // Lambda odczyta pole canvas dopiero w momencie WYWOŁANIA (gdy koło dotrze z serwera),
        // a wtedy @FXML jest już wstrzyknięte — więc canvas nie będzie null.
        // Dane przychodzą na wątku sieciowym, a JavaFX wolno aktualizować TYLKO z wątku UI,
        // dlatego rysowanie owijamy w Platform.runLater(...).
        serverThread.setOnDot(dot -> Platform.runLater(() -> draw(dot)));
    }

    /**
     * ZAD.1 (zmodyfikowane przez ZAD.4): po kliknięciu w kanwę NIE rysujemy bezpośrednio,
     * tylko wysyłamy parametry koła do serwera. Koło narysuje się, gdy wróci do nas
     * przez broadcast i trafi do konsumenta. (Dzięki temu wszyscy klienci widzą to samo.)
     */
    @FXML
    public void onMouseClicked(MouseEvent event) {
        int x = (int) event.getX();                 // środek koła = miejsce kliknięcia
        int y = (int) event.getY();
        int radius = (int) radiusSlider.getValue(); // promień ze suwaka
        String color = toHex(colorPicker.getValue()); // kolor z ColorPickera -> "#RRGGBB"

        serverThread.send(x, y, color, radius);
    }

    /** Rysowanie pojedynczego koła na kanwie (dawna logika z Zad.1, teraz wołana przez konsumenta). */
    private void draw(Dot dot) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.web(dot.color()));
        // fillOval rysuje od lewego-górnego rogu prostokąta opisanego na kole,
        // więc odejmujemy promień, aby środek wypadł w punkcie (x, y).
        gc.fillOval(dot.x() - dot.radius(), dot.y() - dot.radius(),
                    dot.radius() * 2.0, dot.radius() * 2.0);
    }

    /** Zamiana koloru JavaFX (składowe 0..1) na zapis hex "#RRGGBB" do wysłania/zapisu. */
    private static String toHex(Color c) {
        return String.format("#%02X%02X%02X",
                (int) Math.round(c.getRed()   * 255),
                (int) Math.round(c.getGreen() * 255),
                (int) Math.round(c.getBlue()  * 255));
    }
}
