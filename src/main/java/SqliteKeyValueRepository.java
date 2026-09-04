import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SqliteKeyValueRepository implements KeyValueRepository {
    private final ExecutorService executor;
    private final CompletableFuture<Connection> connectionFuture;

    public SqliteKeyValueRepository(String databasePath) {
        executor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "savagepvp-sqlite");
            thread.setDaemon(true);
            return thread;
        });
        connectionFuture = CompletableFuture.supplyAsync(() -> open(databasePath), executor);
    }

    private Connection open(String databasePath) {
        try {
            // The JDBC driver is bundled inside the plugin JAR. Explicitly loading it
            // keeps DriverManager discovery reliable when the JAR is built as a fat JAR.
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS key_values (key TEXT PRIMARY KEY, value TEXT NOT NULL)");
            }
            return connection;
        } catch (ClassNotFoundException exception) {
            throw new RepositoryException("SQLite JDBC driver is not available", exception);
        } catch (SQLException exception) {
            throw new RepositoryException("Unable to initialize SQLite", exception);
        }
    }

    @Override
    public CompletableFuture<Void> save(String key, String value) {
        return connectionFuture.thenRunAsync(() -> {
            try (PreparedStatement statement = connection().prepareStatement(
                    "INSERT OR REPLACE INTO key_values(key, value) VALUES(?, ?)")) {
                statement.setString(1, key);
                statement.setString(2, value);
                statement.executeUpdate();
            } catch (SQLException exception) {
                throw new RepositoryException("Unable to save key/value entry", exception);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Optional<KeyValueEntry>> find(String key) {
        return connectionFuture.thenApplyAsync(ignored -> {
            try (PreparedStatement statement = connection().prepareStatement(
                    "SELECT key, value FROM key_values WHERE key = ?")) {
                statement.setString(1, key);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) return Optional.empty();
                    return Optional.of(new KeyValueEntry(result.getString("key"), result.getString("value")));
                }
            } catch (SQLException exception) {
                throw new RepositoryException("Unable to find key/value entry", exception);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<List<KeyValueEntry>> findAll() {
        return connectionFuture.thenApplyAsync(ignored -> {
            List<KeyValueEntry> entries = new ArrayList<>();
            try (PreparedStatement statement = connection().prepareStatement(
                    "SELECT key, value FROM key_values ORDER BY key COLLATE NOCASE ASC");
                 ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    entries.add(new KeyValueEntry(result.getString("key"), result.getString("value")));
                }
                return entries;
            } catch (SQLException exception) {
                throw new RepositoryException("Unable to list key/value entries", exception);
            }
        }, executor);
    }

    private Connection connection() {
        return connectionFuture.join();
    }

    @Override
    public void close() {
        if (connectionFuture.isDone() && !connectionFuture.isCompletedExceptionally()) {
            try {
                connectionFuture.join().close();
            } catch (SQLException ignored) {
            }
        }
        executor.shutdownNow();
    }

    public static final class RepositoryException extends RuntimeException {
        public RepositoryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
