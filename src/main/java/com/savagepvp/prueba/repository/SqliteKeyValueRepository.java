package com.savagepvp.prueba.repository;

import com.savagepvp.prueba.model.KeyValueEntry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SqliteKeyValueRepository implements KeyValueRepository {
    private final Connection connection;
    private final ExecutorService executor;

    public SqliteKeyValueRepository(String databasePath) throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "savagepvp-sqlite");
            thread.setDaemon(true);
            return thread;
        });
        initialize();
    }

    private void initialize() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS key_values (key TEXT PRIMARY KEY, value TEXT NOT NULL)");
        }
    }

    @Override
    public CompletableFuture<Void> save(String key, String value) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO key_values(key, value) VALUES(?, ?) " +
                    "ON CONFLICT(key) DO UPDATE SET value = excluded.value";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
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
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement statement = connection.prepareStatement(
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
        return CompletableFuture.supplyAsync(() -> {
            List<KeyValueEntry> entries = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(
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

    @Override
    public void close() {
        executor.shutdown();
        try {
            connection.close();
        } catch (SQLException ignored) {
        }
    }

    public static final class RepositoryException extends RuntimeException {
        public RepositoryException(String message, Throwable cause) { super(message, cause); }
    }
}
