# Kolokwium 2 — serwer gry „papier, kamień, nożyce" (krok po kroku)

To rozwiązanie egzaminu z 30.06.2025, dopasowane do konwencji z repo prowadzącego
(`pl.umcs.oop`, sieć w stylu projektu `chat`, dostarczona klasa `Database`, baza `users.db`).

> Najwięcej punktów jest za sam działający serwer (Etap 1). Dlatego Etap 1 opisuję
> najdokładniej — jak zrozumiesz tę część, reszta to dokładanie logiki gry.

---

## 1. O co w ogóle chodzi (sieć w 2 minuty)

Dwa programy gadają ze sobą przez **gniazdo (socket)** — to taki „telefon": jedna strona
pisze tekst, druga go czyta.

- **Serwer** czeka na połączenia. Używa do tego `ServerSocket` (centrala telefoniczna).
  Wywołanie `serverSocket.accept()` **blokuje** program i czeka, aż ktoś zadzwoni.
  Gdy klient się połączy, `accept()` zwraca zwykły `Socket` (połączenie z tym jednym klientem).
- **Klient** tworzy `Socket("adres", port)` i od razu jest połączony.
- Przez `Socket` czytamy (`Scanner` na `getInputStream()`) i piszemy
  (`PrintWriter` na `getOutputStream()`, z `true` = automatyczne wysyłanie po każdej linii).

**Dlaczego wątki?** `accept()` i czytanie linii **blokują**. Gdyby serwer obsługiwał klienta
w tej samej pętli co `accept()`, to podczas rozmowy z jednym klientem nie przyjmowałby
nowych. Rozwiązanie: każdy klient dostaje **własny wątek** (`Thread`). Wątek to „równoległy
tor wykonania" — kilka rzeczy dzieje się naraz. Stąd `ClientHandler extends Thread`.

---

## 2. Uruchomienie i test

1. Plik `users.db` musi leżeć w katalogu projektu (jest dołączony).
2. Uruchom serwer: klasa `pl.umcs.oop.server.Server`, metoda `main` (port 12345).
3. Testowanie (potrzebujesz **dwóch** klientów, bo to gra dla dwojga). Najprościej:
   - dwa terminale: `nc localhost 12345` (lub `telnet localhost 12345`), albo
   - dwa razy uruchom `pl.umcs.oop.client.Client`.
4. W każdym kliencie wpisz login i hasło (np. `alice`/`s` i `bob`/`e` — dane są w bazie).
5. U `alice` wpisz `bob` (wyzwanie). Potem u obu wpisz gest: `r`, `p` albo `s`.
6. Serwer w konsoli wypisze ranking. Testy: `mvn test`.

Dane logowania w bazie: alice/s, bob/e, charlie/c, dave/r, eve/e, frank/t.

---

## 3. Mapa klas

```
pl.umcs.oop
├── server
│   ├── Server         — nasłuch, lista klientów, wyzwania, ranking
│   ├── ClientHandler  — obsługa 1 klienta; dziedziczy po Player (klient JEST graczem)
│   └── Database        — autentykacja + ranking w SQLite (dostarczona, uzupełniona)
├── game
│   ├── Gesture        — enum ROCK/PAPER/SCISSORS + reguły gry
│   ├── Player         — uczestnik pojedynku (sztywny schemat z PDF)
│   └── Duel           — pojedyncza gra: gesty, wynik, „co zrobić po końcu"
└── client             — pomocniczy klient do testów (poza zakresem zadania)
```

---

## 4. Przejście przez zadanie krok po kroku

### ETAP 1 — serwer z autentykacją (tu są punkty!)

**Krok 1 — `Server.listen()` + `main()`.**
`listen()` otwiera `ServerSocket` i w nieskończonej pętli woła `accept()`. `main()` tworzy
serwer i woła `listen()`. To absolutne minimum „stojącego serwera".

**Krok 2 — `ClientHandler` (wielu klientów naraz).**
Dla każdego połączenia serwer tworzy nowy `ClientHandler` i robi `.start()` — to uruchamia
jego metodę `run()` w **osobnym wątku**. Dzięki temu pętla `accept()` od razu wraca po
kolejnego klienta. „Serwer może wysyłać wiadomości klientom" realizujemy metodą
`ClientHandler.send(...)` (serwer ją woła, bo to handler trzyma `PrintWriter` do klienta).

**Krok 3 — lista klientów.**
`Server` trzyma `List<ClientHandler> clients`. Dodajemy w `listen()` przy połączeniu,
usuwamy w bloku `finally` metody `run()` (`server.removeClient(this)`) przy rozłączeniu.
Lista to `CopyOnWriteArrayList` — bezpieczna, gdy kilka wątków naraz ją czyta/zmienia.

**Krok 4 — `Database` w serwerze.**
Wrzucamy dostarczoną klasę do pakietu `server` (dodajemy `package pl.umcs.oop.server;`).
`Server` ma pole `Database database`. ClientHandlerom dajemy dostęp **pośrednio**: przez
`Server.authenticate(login, hasło)`, które woła `database.authenticate(...)`. (Handler nie
dotyka bazy bezpośrednio — to ładniejsza enkapsulacja.) Na tym etapie `authenticate` zwraca
jeszcze `true` (zaślepka); prawdziwą wersję piszemy w Etapie 4.

**Krok 5 — autentykacja w rozmowie.**
W `run()`: serwer wysyła „Podaj login:", czyta odpowiedź, wysyła „Podaj hasło:", czyta.
Woła `server.authenticate(...)`. Jeśli `false` — wysyła komunikat i `return` (a `finally`
usunie klienta i zamknie socket). Jeśli `true` — zapisuje login w polu `login` handlera.

➡️ Po Etapie 1 masz serwer, do którego można się połączyć i zalogować. To zwykle gros punktów.

### ETAP 2 — logika gry (czysta, bez sieci — łatwo testowalna)

**Krok 1 — `enum Gesture`** z `ROCK/PAPER/SCISSORS`. `fromString` mapuje „r/p/s" na gesty
(nieznane → `null`, co ułatwi ignorowanie błędnych wiadomości). Użyto *switch expression*
(`switch (x) { case ... -> ...; }`) — nowszej, zwięzłej formy switcha zwracającej wartość.

**Krok 2 — `compareWith`.** Zwraca 0 (remis), 1 (ten gest wygrywa), -1 (przegrywa).
Reguła z treści: ROCK<PAPER<SCISSORS<ROCK, czyli „<" = „przegrywa z".

**Krok 3 — `Player` wg sztywnego schematu.** Nie wolno dodawać pól/metod. Pole `duel`
(prywatne) mówi, w jakim pojedynku gracz jest teraz. Widoczność metod dobrana
**maksymalnie restrykcyjnie** (szczegóły w komentarzach pliku): `makeGesture` → `protected`
(woła ją podklasa `ClientHandler`), `enterDuel`/`leaveDuel` → pakietowe (woła `Duel`),
`isDuelling` → `public` (woła `Server` z innego pakietu i testy).

**Krok 4 — `Duel`.** Trzyma dwóch `Player` i ich `Gesture`. Konstruktor woła `enterDuel`
dla obu (ustawia im `duel` na siebie). `leaveDuel` zeruje `duel`, `isDuelling` sprawdza `!= null`.

**Krok 5 — test JUnit.** Tworzy graczy + pojedynek i sprawdza, że oboje `isDuelling()`.
JUnit: `@Test` oznacza metodę testową, `assertTrue(...)` wymusza warunek.

**Krok 6 — `handleGesture(Player, Gesture)`.** Zapisuje gest właściwego gracza.
`Player.makeGesture` po prostu deleguje do `duel.handleGesture(this, gest)`.

**Krok 7 — rekord `Result(winner, loser)` + `evaluate()`.** `evaluate` porównuje gesty:
remis → `null`, inaczej → `Result` z odpowiednim zwycięzcą. `record` = niemutowalny
nosiciel danych (auto-gettery `winner()`, `loser()`).

**Krok 8 — dwa testy `evaluate()`:** wygrana (ROCK vs SCISSORS → winner) i remis (→ `null`).

**Krok 9 — gdzie wołać `leaveDuel()`?** Po zakończeniu gry, czyli **w `evaluate()`** — gdy
wynik jest już policzony, oboje gracze są zwalniani (`duel = null`).

### ETAP 3 — spięcie gry z serwerem

**Krok 1 — `ClientHandler extends Player`.** Od teraz klient *jest* graczem — dziedziczy
`makeGesture`, `isDuelling` itd. W `run()` rozdzielamy wiadomości: jeśli klient jest w
pojedynku → to gest; jeśli nie → to login do wyzwania (→ `server.challengeToDuel(this, msg)`).

**Krok 2 — `challengeToDuel`.** Przeszukuje listę klientów po loginie. Znalazł → `startDuel`.
Nie znalazł → komunikat do wyzywającego.

**Krok 3 — `startDuel`.** Tworzy `Duel(challenger, challengee)` (oba to `ClientHandler`,
więc i `Player`) i wysyła obu info o starcie.

**Krok 4 — gest w trakcie gry.** Tylko „p/r/s"; inne wiadomości ignorujemy (`fromString`
zwraca `null` → nic nie robimy).

**Krok 5 — pole `onEnd` w `Duel`.** Interfejs funkcyjny „nic nie bierze, nic nie zwraca" =
gotowy `Runnable`. `handleGesture` odpala `onEnd.run()`, gdy **obaj** zagrali. Domyślnie
`onEnd` jest puste (`() -> {}`), żeby nie psuć testów z Etapu 2 (tam nikt go nie ustawia).

**Krok 6 — ustawienie `onEnd` w `startDuel`.** Funkcja: woła `evaluate()` i informuje graczy
o remisie / wygranej / porażce.

**Krok 7 — zakazy.** Nie można wyzwać samego siebie ani kogoś, kto już gra — wtedy
komunikat do wyzywającego.

### ETAP 4 — zapis wyników (baza)

**Krok 1 — `authenticate`** sprawdza login+hasło w tabeli `users` (`SELECT ... WHERE login=? AND password=?`).
`PreparedStatement` z „?" chroni przed SQL injection.

**Krok 2 — `updateLeaderboard(winner, loser)`** robi `points+1` zwycięzcy i `points-1`
przegranemu. Wołane w `onEnd` tylko po grze **nieremisowej**.

**Krok 3 — `getLeaderboard()`** zwraca `Map<login, punkty>` posortowaną malejąco
(`ORDER BY points DESC` + `LinkedHashMap`, który zachowuje kolejność wstawiania).
`Server.printLeaderboard()` wypisuje ją na konsoli po **każdej** zakończonej grze.

---

## 5. Pełny przepływ jednej gry (jak to wszystko działa razem)

```
alice i bob łączą się i logują (Etap 1)
alice wpisuje "bob"  → ClientHandler.run widzi, że alice NIE gra → server.challengeToDuel(alice,"bob")
   → znaleziono boba, wolny → startDuel(alice, bob)
       → new Duel(alice, bob)  (oboje enterDuel → isDuelling()==true)
       → onEnd ustawione: po obu gestach policz wynik i poinformuj
alice wpisuje "r", bob wpisuje "s"  → bo oboje są w pojedynku, run() robi makeGesture(...)
   → Duel.handleGesture zapisuje gesty; po DRUGIM odpala onEnd
       → evaluate(): ROCK bije SCISSORS → Result(winner=alice, loser=bob); leaveDuel obojga
       → alice: "Wygrałeś!", bob: "Przegrałeś!"
       → updateLeaderboard("alice","bob")  → ranking na konsoli serwera
```

---

## 6. Mini-słowniczek pojęć (gdyby coś było nowe)

- **socket / ServerSocket** — połączenie sieciowe / „centrala" przyjmująca połączenia.
- **wątek (Thread)** — równoległy tor wykonania; `extends Thread` + `start()` → `run()` leci obok.
- **enum** — typ o skończonym zbiorze nazwanych wartości.
- **record** — krótki, niemutowalny nosiciel danych z auto-getterami/equals/hashCode.
- **interfejs funkcyjny / `Runnable`** — „opakowana funkcja"; `Runnable.run()` nic nie bierze i nie zwraca.
- **dziedziczenie** — `ClientHandler extends Player` → handler ma wszystko, co Player, plus swoje.
- **widoczność** — `private` < pakietowa (brak słowa) < `protected` < `public`; bierz najwęższą, która działa.
- **PreparedStatement** — zapytanie SQL z „?", bezpieczne i wygodne do podstawiania wartości.

---

## 7. Drobne pułapki

- **`onEnd` musi mieć wartość domyślną** (`() -> {}`), inaczej testy z Etapu 2 (bez serwera)
  wywaliłyby `NullPointerException` przy drugim geście.
- **Rzutowanie `Player` → `ClientHandler`** w `startDuel`/`onEnd`: `Result` trzyma `Player`,
  ale realnie to `ClientHandler`, więc by wysłać wiadomość rzutujemy. Bezpieczne, bo w grze
  uczestniczą wyłącznie ClientHandlery.
- **`users.db` w katalogu roboczym** — `jdbc:sqlite:users.db` szuka pliku względem miejsca
  uruchomienia. Jak baza „nie działa", to prawie zawsze zła ścieżka do pliku.
- **Kolejność pisania a kolejność oddawania** — PDF proponuje budować Player w Etapie 2, a
  dziedziczenie `ClientHandler extends Player` dopiero w Etapie 3. W gotowym rozwiązaniu
  jest to już połączone; gdybyś pisał od zera na kolokwium — trzymaj się kolejności z PDF,
  bo wcześniejsze kroki dają pewne punkty zanim ruszysz trudniejsze.
```
