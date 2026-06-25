# Powtórzenie do 2. kolokwium — Circle App (JavaFX + sieć + baza)

Projekt Maven (JavaFX 17.0.6 + sqlite-jdbc). Pakiet główny: `com.example.circleapp`
(zgodny ze startowym kontrolerem wykładowcy), podpakiety `server` i `client`,
oraz osobny pakiet `database` (jak w `Site_Lab9`).

## Uruchomienie
```
mvn clean javafx:run
```
Klikasz w kanwę → powstaje koło o kolorze z ColorPickera i promieniu ze suwaka.

## Struktura
```
src/main/java/
├── com/example/circleapp/
│   ├── Main.java          (Application: start serwera + klienta, wstrzyknięcie do kontrolera)
│   ├── Controller.java    (Zad.1 rysowanie, Zad.4 send() + konsument)
│   ├── Dot.java           (Zad.3+5 model + (de)serializacja wiadomości)
│   ├── server/
│   │   ├── Server.java        (Zad.2 ServerSocket+broadcast, Zad.5 baza)
│   │   └── ClientThread.java  (Zad.2 obsługa klienta, Zad.5 wysyłka historii)
│   └── client/
│       └── ServerThread.java  (Zad.2 send(), Zad.4 Consumer<Dot>)
└── database/
    └── DatabaseConnection.java
src/main/resources/com/example/circleapp/app-view.fxml
```

## Przepływ danych (kluczowe!)
Po Zad.4 koło NIE jest rysowane od razu po kliknięciu. Trasa jest taka:

```
klik w kanwę
  → Controller.onMouseClicked  → serverThread.send(x,y,color,radius)
      → [sieć] → Server (ClientThread czyta linię) → server.broadcast(msg)
          → zapis do bazy (Zad.5) + rozesłanie do WSZYSTKICH klientów
              → ServerThread.listen() odbiera → konsument.accept(dot)
                  → Platform.runLater(() -> draw(dot))  → koło na kanwie
```
Dzięki temu każdy podłączony klient widzi koła wszystkich pozostałych, a baza
przechowuje pełną historię.

## Co które zadanie wnosi

**Zad.1 — JavaFX/Canvas.** Kontrolki z FXML (`@FXML`), `Canvas.getGraphicsContext2D()`,
`fillOval` rysuje od rogu prostokąta, więc środek w punkcie kliknięcia uzyskujemy
odejmując promień: `fillOval(x-r, y-r, 2r, 2r)`.

**Zad.2 — pakiety + sieć.** `server.Server` trzyma `ServerSocket`; klienci to
`server.ClientThread` (klasa package-private — używa jej tylko `Server`).
`client.ServerThread.send(...)` wysyła dane. Każdy klient w osobnym wątku, bo
`readLine()` blokuje. `broadcast()` rozsyła do wszystkich.

**Zad.3 — record + (de)serializacja.** `Dot(x,y,color,radius)`. Statyczne
`toMessage(...)` (z luźnych parametrów) i `fromMessage(String)` (fabryka z formatu
sieciowego "x;y;#RRGGBB;radius").

**Zad.4 — wstrzykiwanie + konsument.** Kontroler ma konstruktor `(Server, ServerThread)`.
`ServerThread` ma pole `Consumer<Dot>` + mutator `setOnDot(...)`. Konstruktor kontrolera
ustawia konsumenta na „narysuj koło". Kliknięcie tylko wysyła (`send`), rysowanie idzie
przez konsumenta po powrocie danych z serwera.

**Zad.5 — baza.** Serwer łączy się z bazą na starcie i tworzy tabelę `dot`.
`saveDot(Dot)`, `getSavedDots() : List<Dot>`. `broadcast()` zapisuje koło.
Nowemu klientowi tuż po połączeniu wysyłana jest cała historia. Niestatyczna
`Dot.toMessage()` buduje wiadomość z pól obiektu (do rozsyłki kół odczytanych z bazy).

## Cztery pułapki, o które najczęściej pytają

1. **Kontroler z konstruktorem parametrowym a FXML.** FXML domyślnie tworzy kontroler
   bezargumentowo. Skoro mamy konstruktor z parametrami, w pliku FXML usuwamy
   `fx:controller`, a w `Main` robimy `loader.setController(new Controller(server, serverThread))`
   PRZED `loader.load()`. (Alternatywa: `loader.setControllerFactory(...)`.)

2. **Konsument ustawiany w konstruktorze, a canvas wstrzykiwany później.** Konstruktor
   wykonuje się przed wstrzyknięciem `@FXML`, więc `canvas` jest tam jeszcze `null`.
   To nie problem, bo konsument to lambda — pole `canvas` czytamy dopiero przy wywołaniu
   (gdy koło dotrze), a wtedy jest już wstrzyknięte.

3. **Wątek UI.** Dane sieciowe przychodzą na wątku nasłuchującym, ale JavaFX wolno ruszać
   tylko z wątku UI → rysowanie owijamy w `Platform.runLater(...)`.

4. **module-info.** Projekt celowo NIE ma `module-info.java` (jak `shop` wykładowcy).
   Wtedy JavaFX i sterownik SQLite działają razem na classpath bez konfliktów modułów,
   a FXML widzi kontroler bez `opens`. Jeśli chcesz wersję modularną, dodaj:
   ```
   module com.example.circleapp {
       requires javafx.controls;
       requires javafx.fxml;
       requires java.sql;
       requires org.xerial.sqlitejdbc;            // nazwa modułu automatycznego sqlite-jdbc
       opens com.example.circleapp to javafx.fxml; // FXML używa refleksji na kontrolerze
       exports com.example.circleapp;
   }
   ```
   (Wariant bez modułów jest pewniejszy na kolokwium — mniej rzeczy do pomylenia.)

## Uwaga o `app-view.fxml`
Pliku nie ma w repo (był „dołączony" do zadania). Odtworzyłem go na podstawie pól
startowego kontrolera (`canvas`, `colorPicker`, `radiusSlider`). Jeśli oryginał miał
inne `fx:id` lub dodatkowe przyciski — wyrównaj nazwy między FXML a `@FXML`.
