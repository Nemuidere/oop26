
 * =====================================================================================
 *  CZĘŚĆ A — TEORIA PAKIETÓW
 * =====================================================================================
 *
 *  PAKIET (package)
 *  - To grupa powiązanych klas + przestrzeń nazw. Dwie klasy o tej samej nazwie mogą
 *    współistnieć, jeśli są w różnych pakietach (np. java.util.Date vs java.sql.Date).
 *  - Deklaracja MUSI być pierwszą instrukcją w pliku:  `package auth;`
 *  - Nazwa pakietu = ścieżka katalogów. Klasa w pakiecie `auth` leży w katalogu `auth/`.
 *    Pakiet `pl.cwel.site.auth` -> katalogi pl/cwel/site/auth/.
 *  - Konwencja nazewnicza: małe litery, odwrócona domena, np. `io.github.cwel.site`.
 *    W tym zadaniu treść narzuca proste nazwy pakietów `database` i `auth` — używam ich
 *    dosłownie, ale w prawdziwym projekcie poprzedza się je domeną (groupId Mavena).
 *
 *  IMPORT
 *    import database.DatabaseConnection;      // import pojedynczej klasy (zalecane)
 *    import database.*;                       // import gwiazdkowy — wszystkie klasy pakietu
 *    import static org.mindrot.jbcrypt.BCrypt.hashpw;  // import statyczny — metoda/pole
 *  - Klasy z tego samego pakietu i z java.lang (String, System...) NIE wymagają importu.
 *  - Import gwiazdkowy NIE jest rekurencyjny: `import java.*` nie wciąga `java.util`.
 *
 *  MODYFIKATORY DOSTĘPU w kontekście pakietów (kluczowe dla tego tematu):
 *    public          -> widoczne wszędzie
 *    protected       -> w tym samym pakiecie + w podklasach (także z innego pakietu)
 *    (brak/default)  -> TYLKO w obrębie tego samego pakietu = "package-private"
 *    private         -> tylko w obrębie tej klasy
 *  -> Dlatego pole `Connection` jest `private`, a dostęp do niego dajemy publicznym
 *     akcesorem — enkapsulacja działa też na granicy pakietów.
 *
 * =====================================================================================
 *  CZĘŚĆ B — MAVEN W PIGUŁCE
 * =====================================================================================
 *
 *  Maven to narzędzie budowania + zarządzania zależnościami. Projekt opisuje pom.xml.
 *
 *  STANDARDOWY UKŁAD KATALOGÓW (convention over configuration):
 *    site/
 *    ├── pom.xml
 *    └── src/
 *        ├── main/java/        <- kod produkcyjny (tu pakiety database/, auth/, klasa Main)
 *        ├── main/resources/   <- pliki nie-kodowe (np. baza, konfiguracja)
 *        └── test/java/        <- testy
 *    Po zbudowaniu artefakty trafiają do  target/.
 *
 *  WSPÓŁRZĘDNE ARTEFAKTU (GAV) — jednoznaczny identyfikator biblioteki:
 *    groupId    : np. io.github.cwel   (odwrócona domena)
 *    artifactId : np. site             (nazwa projektu)
 *    version    : np. 1.0.0
 *
 *  CYKL ŻYCIA (najważniejsze fazy, każda kolejna wykonuje poprzednie):
 *    validate -> compile -> test -> package -> verify -> install -> deploy
 *      package  : zbuduj .jar w target/
 *      install  : skopiuj .jar do lokalnego repo ~/.m2/repository (dostępne lokalnie)
 *      deploy   : wyślij .jar do zdalnego repo (np. GitHub Packages) — patrz Zad. 3
 *
 *  ZALEŻNOŚĆ (dependency) = obca biblioteka pobierana automatycznie z repozytorium
 *  (domyślnie Maven Central). Deklarujemy ją współrzędnymi GAV w <dependencies>.
 *
 * =====================================================================================
 *  ZADANIE 1 — projekt `site`, pakiet `database`, klasa DatabaseConnection + Main (test)
 * =====================================================================================
 */

// ===== FILE: site/pom.xml =====
/*
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- GAV biblioteki. OWNER w groupId zwykle = io.github.<twoja-nazwa-na-githubie> -->
    <groupId>io.github.cwel</groupId>
    <artifactId>site</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>          <!-- budujemy bibliotekę jako .jar -->

    <properties>
        <maven.compiler.release>17</maven.compiler.release> <!-- record wymaga Javy 16+ -->
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- Sterownik JDBC do SQLite. JAR zawiera natywne biblioteki dla Win/Mac/Linux. -->
        <dependency>
            <groupId>org.xerial</groupId>
            <artifactId>sqlite-jdbc</artifactId>
            <version>3.53.2.0</version>
        </dependency>

        <!-- jBCrypt — hashowanie haseł algorytmem bcrypt (BCrypt.hashpw / checkpw). -->
        <dependency>
            <groupId>org.mindrot</groupId>
            <artifactId>jbcrypt</artifactId>
            <version>0.4</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- exec-maven-plugin: uruchamia klasę Main bez budowania uber-jara.
                 Użycie:  mvn -q compile exec:java -Dexec.mainClass=Main         -->
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.5.0</version>
                <configuration>
                    <mainClass>Main</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
*/

// ===== FILE: site/src/main/java/database/DatabaseConnection.java =====

package database;                          // 1. deklaracja pakietu — zawsze pierwsza linia

import java.sql.Connection;                // 2. typy JDBC z pakietu java.sql
import java.sql.DriverManager;             //    DriverManager wydaje połączenia
import java.sql.SQLException;              //    wyjątek kontrolowany (checked)

/**
 * Opakowuje pojedyncze połączenie JDBC do bazy SQLite.
 * Klasa publiczna -> widoczna z innych pakietów (np. z auth).
 */
public class DatabaseConnection {

    // Pole prywatne: enkapsulacja. Spoza klasy NIE da się go podmienić bezpośrednio.
    private Connection connection;

    /** Publiczny akcesor (getter) do połączenia — wymagany przez treść zadania. */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Łączy się z bazą SQLite spod podanej ścieżki.
     * SQLite jest "plikowy": URL "jdbc:sqlite:<ścieżka>" otworzy plik, a jeśli nie istnieje
     * — utworzy go przy pierwszym zapisie. Brak osobnego serwera bazy.
     *
     * Od JDBC 4.0 sterownik rejestruje się sam przez ServiceLoader
     * (META-INF/services/java.sql.Driver), więc Class.forName("org.sqlite.JDBC")
     * jest już zbędne (zostawione w komentarzu jako wiedza "legacy").
     */
    public void connect(String path) throws SQLException {
        // Class.forName("org.sqlite.JDBC");          // dawniej wymagane, dziś niepotrzebne
        String url = "jdbc:sqlite:" + path;
        connection = DriverManager.getConnection(url);
    }

    /** Zamyka połączenie, jeśli jest otwarte. */
    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            connection = null;             // pozwala wykryć ponowne użycie po rozłączeniu
        }
    }
}

// ===== FILE: site/src/main/java/Main.java   (wersja dla ZADANIA 1) =====
/*
   Klasa Main leży POZA pakietami (pakiet domyślny), zgodnie z treścią.
   Test: utworzenie tabeli, zapis wiersza i odczyt — czyli pełne CRUD na minimalnym przykładzie.

import database.DatabaseConnection;        // import klasy z pakietu database
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) throws SQLException {
        DatabaseConnection db = new DatabaseConnection();
        db.connect("site.db");             // utworzy plik site.db w katalogu roboczym

        // 1) UTWORZENIE tabeli. Statement -> zapytania bez parametrów.
        try (Statement st = db.getConnection().createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS notes (
                    id   INTEGER PRIMARY KEY AUTOINCREMENT,
                    text TEXT NOT NULL
                )
            """);
        }

        // 2) ZAPIS. PreparedStatement -> bezpieczne wstawianie wartości (ochrona przed SQL injection).
        //    Parametry numerowane są OD 1 (nie od 0!).
        try (PreparedStatement ps =
                 db.getConnection().prepareStatement("INSERT INTO notes(text) VALUES (?)")) {
            ps.setString(1, "Pierwsza notatka");
            ps.executeUpdate();            // INSERT/UPDATE/DELETE -> executeUpdate()
        }

        // 3) ODCZYT. ResultSet -> kursor po wynikach; next() przesuwa do kolejnego wiersza.
        try (Statement st = db.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT id, text FROM notes")) {   // SELECT -> executeQuery()
            while (rs.next()) {
                System.out.println(rs.getInt("id") + ": " + rs.getString("text"));
            }
        }

        db.disconnect();
    }
}
*/

/* =====================================================================================
 *  ZADANIE 2 — pakiet `auth`: record Account + klasa AccountManager (bcrypt) + test
 * =====================================================================================
 *  Nowości:
 *   - record: zwięzła, NIEMUTOWALNA klasa-nosiciel danych. Kompilator generuje
 *     konstruktor kanoniczny, akcesory id()/username(), equals/hashCode/toString.
 *   - bcrypt: hasła NIGDY nie trzymamy jawnie. hashpw() dokleja losową "sól" i jest wolny
 *     z założenia (utrudnia łamanie). Weryfikacja: checkpw(podane, zapisanyHash).
 */

// ===== FILE: site/src/main/java/auth/Account.java =====

package auth;

/**
 * Rekord = niemutowalny komplet danych konta.
 * Równoważny klasie z prywatnymi finalnymi polami + getterami + equals/hashCode/toString,
 * tylko zapisany w jednej linii. Akcesory nazywają się id() oraz username() (bez "get").
 */
public record Account(int id, String username) {}

// ===== FILE: site/src/main/java/auth/AccountManager.java =====

package auth;

import database.DatabaseConnection;        // używamy klasy z SĄSIEDNIEGO pakietu -> import
import org.mindrot.jbcrypt.BCrypt;         // klasa z zależności jbcrypt
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Logika kont: rejestracja, uwierzytelnianie, pobieranie kont.
 * Współpracuje z DatabaseConnection (wstrzykniętym przez konstruktor — luźne powiązanie).
 */
public class AccountManager {

    private final DatabaseConnection db;   // final -> ustawiane raz, w konstruktorze

    public AccountManager(DatabaseConnection db) {
        this.db = db;
    }

    /** Tworzy tabelę kont, jeśli nie istnieje. Hash bcrypt mieści się w TEXT. */
    public void initSchema() throws SQLException {
        try (Statement st = db.getConnection().createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS accounts (
                    id            INTEGER PRIMARY KEY AUTOINCREMENT,
                    username      TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL
                )
            """);
        }
    }

    /**
     * register — dodaje użytkownika. Zapisuje HASH hasła, nigdy hasła jawnego.
     * gensalt() generuje losową sól, dzięki czemu dwa identyczne hasła dają różne hashe.
     */
    public void register(String username, String password) throws SQLException {
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                 "INSERT INTO accounts(username, password_hash) VALUES (?, ?)")) {
            ps.setString(1, username);
            ps.setString(2, hash);
            ps.executeUpdate();
        }
    }

    /**
     * authenticate — true, gdy podane hasło pasuje do zapisanego hasha.
     * checkpw() sam odczytuje sól z hasha i porównuje bezpiecznie.
     */
    public boolean authenticate(String username, String password) throws SQLException {
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                 "SELECT password_hash FROM accounts WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;                  // nie ma takiego użytkownika
                return BCrypt.checkpw(password, rs.getString("password_hash"));
            }
        }
    }

    /** getAccount po nazwie użytkownika. Zwraca null, gdy konta nie ma. */
    public Account getAccount(String username) throws SQLException {
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                 "SELECT id, username FROM accounts WHERE username = ?")) {
            ps.setString(1, username);
            return readOne(ps);
        }
    }

    /**
     * getAccount po id. PRZECIĄŻENIE (overloading): ta sama nazwa metody, inny typ
     * argumentu (int zamiast String) — kompilator wybiera wersję po typie.
     */
    public Account getAccount(int id) throws SQLException {
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                 "SELECT id, username FROM accounts WHERE id = ?")) {
            ps.setInt(1, id);
            return readOne(ps);
        }
    }

    // Prywatny helper — wspólny odczyt jednego wiersza do rekordu Account.
    // (Lepszą praktyką byłby zwrot Optional<Account>, ale treść mówi o obiekcie Account.)
    private Account readOne(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) return null;
            return new Account(rs.getInt("id"), rs.getString("username"));
        }
    }
}

// ===== FILE: site/src/main/java/Main.java   (wersja dla ZADANIA 2) =====
/*
import database.DatabaseConnection;
import auth.Account;
import auth.AccountManager;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        DatabaseConnection db = new DatabaseConnection();
        db.connect("site.db");

        AccountManager accounts = new AccountManager(db);
        accounts.initSchema();
        accounts.register("cwel", "tajneHaslo123");

        System.out.println(accounts.authenticate("cwel", "tajneHaslo123")); // true
        System.out.println(accounts.authenticate("cwel", "zleHaslo"));      // false

        Account a = accounts.getAccount("cwel");
        System.out.println(a);                                              // Account[id=1, username=cwel]
        System.out.println(accounts.getAccount(a.id()).username());         // cwel  (po id)

        db.disconnect();
    }
}
*/

/* =====================================================================================
 *  ZADANIE 3 — publikacja `site` do GitHub Packages i użycie jej w projekcie `shop`
 * =====================================================================================
 *
 *  KROK 0. Usuń Main z `site` (zachowaj jej treść — wkleimy ją do `shop/music/Main.java`).
 *          Biblioteka nie powinna mieć punktu wejścia main.
 *
 *  KROK 1. W pom.xml `site` dodaj sekcję publikacji (gdzie deploy ma wysłać .jar).
 *          OWNER i REPO = nazwa właściciela i repozytorium na GitHubie (małe litery!).
 */

// ===== FILE: site/pom.xml  -> dopisz wewnątrz <project> =====
/*
    <distributionManagement>
        <repository>
            <id>github</id>                <!-- MUSI zgadzać się z <id> w settings.xml -->
            <name>GitHub Packages — site</name>
            <url>https://maven.pkg.github.com/OWNER/REPO</url>
        </repository>
    </distributionManagement>
*/

/*
 *  KROK 2. Uwierzytelnienie. GitHub Packages wymaga tokenu PAT (classic) z uprawnieniami
 *          write:packages (do publikacji) i read:packages (do pobierania).
 *          Token i login trzymamy w ~/.m2/settings.xml (NIE w pom.xml -> nie trafia do repo).
 */

// ===== FILE: ~/.m2/settings.xml =====
/*
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0">
    <servers>
        <server>
            <id>github</id>               <!-- musi pasować do <id> repozytorium -->
            <username>TWOJA_NAZWA_GITHUB</username>
            <password>PAT_Z_UPRAWNIENIAMI_PACKAGES</password>
        </server>
    </servers>
</settings>
*/

/*
 *  KROK 3. Opublikuj bibliotekę (z katalogu projektu site):
 *
 *      mvn clean deploy
 *
 *  Po sukcesie pakiet io.github.cwel:site:1.0.0 jest widoczny w zakładce
 *  "Packages" repozytorium na GitHubie.
 *
 *  KROK 4. Nowy projekt `shop` z pakietem `music`. W jego pom.xml trzeba zrobić DWIE rzeczy:
 *          (a) wskazać <repositories>, skąd pobierać `site` (Maven Central tego nie zna),
 *          (b) dodać `site` jako <dependency>.
 */

// ===== FILE: shop/pom.xml =====
/*
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.github.cwel</groupId>
    <artifactId>shop</artifactId>
    <version>1.0.0</version>

    <properties>
        <maven.compiler.release>17</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <!-- (a) Repozytorium do POBIERANIA. Ten sam URL co w distributionManagement site. -->
    <repositories>
        <repository>
            <id>github</id>               <!-- znowu pasuje do server.id w settings.xml -->
            <url>https://maven.pkg.github.com/OWNER/REPO</url>
        </repository>
    </repositories>

    <dependencies>
        <!-- (b) nasza opublikowana biblioteka. Maven pobierze też jej tranzytywne
                 zależności: sqlite-jdbc i jbcrypt — nie trzeba ich dopisywać ręcznie. -->
        <dependency>
            <groupId>io.github.cwel</groupId>
            <artifactId>site</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.5.0</version>
                <configuration>
                    <mainClass>music.Main</mainClass>   <!-- Main jest teraz w pakiecie music -->
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
*/

// ===== FILE: shop/src/main/java/music/Main.java =====
/*
   To dawna Main z `site`, dostosowana:
   - dodana deklaracja `package music;`
   - klasy DatabaseConnection / AccountManager / Account są teraz w OBCEJ bibliotece,
     ale importuje się je tak samo jak wcześniej (te same pakiety database / auth).
     Maven sam dołącza site.jar do classpath przez <dependency>.

package music;                              // klasa należy do pakietu music

import database.DatabaseConnection;         // import z biblioteki site
import auth.Account;
import auth.AccountManager;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        DatabaseConnection db = new DatabaseConnection();
        db.connect("shop.db");

        AccountManager accounts = new AccountManager(db);
        accounts.initSchema();
        accounts.register("klient", "haslo!");

        System.out.println(accounts.authenticate("klient", "haslo!")); // true
        Account a = accounts.getAccount("klient");
        System.out.println(a);

        db.disconnect();
    }
}

   Uruchomienie (z katalogu shop):
       mvn -q compile exec:java
*/

/* =====================================================================================
 *  SZYBKIE PODSUMOWANIE (ściąga na kolokwium)
 * =====================================================================================
 *   - package = pierwsza linia pliku; nazwa = ścieżka katalogów; konwencja: odwrócona domena.
 *   - dostęp domyślny (brak słowa) = widoczność tylko w obrębie pakietu (package-private).
 *   - import: klasa / gwiazdka / static; klasy z java.lang i z własnego pakietu bez importu.
 *   - Maven: src/main/java, pom.xml, GAV (groupId:artifactId:version), <dependencies>.
 *   - fazy: compile -> test -> package -> install(lokalnie) -> deploy(zdalnie).
 *   - JDBC SQLite: URL "jdbc:sqlite:plik"; Statement vs PreparedStatement (parametry od 1);
 *     executeUpdate() dla INSERT/UPDATE/DELETE, executeQuery() dla SELECT; ResultSet.next().
 *   - bcrypt: hashpw(haslo, gensalt()) przy zapisie; checkpw(haslo, hash) przy logowaniu.
 *   - record Account(int id, String username): niemutowalny; akcesory id(), username().
 *   - GitHub Packages: distributionManagement (publikacja) + repositories (pobieranie),
 *     auth przez server id="github" w ~/.m2/settings.xml (login + PAT). Publikacja: mvn deploy.
 * ===================================================================================== */