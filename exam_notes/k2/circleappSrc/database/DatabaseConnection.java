package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Połączenie z bazą SQLite (ten sam wzorzec, co w repo wykładowcy: connect / disconnect / getter).
 * SQLite jest "plikowy": URL "jdbc:sqlite:<ścieżka>" otwiera plik bazy, a jeśli nie istnieje
 * — tworzy go przy pierwszej operacji zapisu.
 */
public class DatabaseConnection {

    private Connection connection;

    public Connection getConnection() {
        return this.connection;
    }

    public void connect(String dbPath) throws SQLException {
        String url = "jdbc:sqlite:" + dbPath;
        this.connection = DriverManager.getConnection(url);
        System.out.println("Połączono z bazą: " + dbPath);
    }

    public void disconnect() throws SQLException {
        if (this.connection != null && !this.connection.isClosed()) {
            this.connection.close();
            System.out.println("Rozłączono z bazą.");
        }
    }
}
