package pl.umcs.oop.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ETAP 1 (Krok 4) + ETAP 4 — dostarczona klasa Database, przeniesiona do pakietu server
 * i uzupełniona o realne połączenie z bazą SQLite.
 *
 * Tabela (plik users.db w katalogu projektu):
 *   users(login TEXT PRIMARY KEY, password TEXT NOT NULL, points INTEGER NOT NULL)
 *
 * W ETAPIE 1 metoda authenticate() po prostu zwracała true (wersja-zaślepka z PDF).
 * Poniżej jest już docelowa implementacja z ETAPU 4.
 */
public class Database {

    private final Connection connection;

    public Database() {
        try {
            // SQLite jest plikowy: łączymy się z plikiem users.db w katalogu roboczym.
            this.connection = DriverManager.getConnection("jdbc:sqlite:users.db");
        } catch (SQLException e) {
            throw new RuntimeException("Nie udało się połączyć z bazą users.db: " + e.getMessage(), e);
        }
    }

    /**
     * ETAP 4, KROK 1 — true tylko gdy istnieje wiersz o danym loginie i haśle.
     * PreparedStatement (parametry "?") chroni przed SQL injection.
     */
    public boolean authenticate(String login, String password) {
        String sql = "SELECT 1 FROM users WHERE login = ? AND password = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, login);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();   // jest wiersz => dane poprawne
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * ETAP 4, KROK 2 — zwycięzca +1 punkt, przegrany -1 punkt.
     */
    public void updateLeaderboard(String winner, String loser) {
        String addPoint = "UPDATE users SET points = points + 1 WHERE login = ?";
        String subPoint = "UPDATE users SET points = points - 1 WHERE login = ?";
        try (PreparedStatement add = connection.prepareStatement(addPoint);
             PreparedStatement sub = connection.prepareStatement(subPoint)) {
            add.setString(1, winner);
            add.executeUpdate();
            sub.setString(1, loser);
            sub.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * ETAP 4, KROK 3 — ranking: login -> punkty, posortowany malejąco.
     * LinkedHashMap zachowuje kolejność wstawiania, a wstawiamy już posortowane
     * przez "ORDER BY points DESC", więc kolejność rankingu jest zachowana.
     */
    public Map<String, Integer> getLeaderboard() {
        Map<String, Integer> leaderboard = new LinkedHashMap<>();
        String sql = "SELECT login, points FROM users ORDER BY points DESC";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                leaderboard.put(rs.getString("login"), rs.getInt("points"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return leaderboard;
    }
}
