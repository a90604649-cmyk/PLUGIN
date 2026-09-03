package com.savagepvp.prueba.repository;

import com.savagepvp.prueba.model.KeyValueEntry;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface KeyValueRepository {
    CompletableFuture<Void> save(String key, String value);
    CompletableFuture<Optional<KeyValueEntry>> find(String key);
    CompletableFuture<List<KeyValueEntry>> findAll();
    void close();
}
