import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class KeyValueService {
    private final KeyValueRepository repository;
    private final KeyValidator keyValidator;

    public KeyValueService(KeyValueRepository repository, KeyValidator keyValidator) {
        this.repository = repository;
        this.keyValidator = keyValidator;
    }

    public CompletableFuture<Void> save(String key, String value) {
        if (!keyValidator.isValid(key)) {
            return failedFuture(new IllegalArgumentException(
                    "La key debe contener solo letras, números y guiones bajos, y tener entre 1 y 16 caracteres."));
        }
        if (value == null) {
            return failedFuture(new IllegalArgumentException("La value no puede ser null."));
        }
        return repository.save(key, value);
    }

    public CompletableFuture<Optional<KeyValueEntry>> find(String key) {
        if (!keyValidator.isValid(key)) {
            return failedFuture(new IllegalArgumentException("La key no es válida."));
        }
        return repository.find(key);
    }

    public CompletableFuture<List<KeyValueEntry>> findAll() {
        return repository.findAll();
    }

    private static <T> CompletableFuture<T> failedFuture(Throwable throwable) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(throwable);
        return future;
    }
}
