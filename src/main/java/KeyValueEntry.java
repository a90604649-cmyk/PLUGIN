import java.util.Objects;

public final class KeyValueEntry {
    private final String key;
    private final String value;

    public KeyValueEntry(String key, String value) {
        this.key = Objects.requireNonNull(key, "key");
        this.value = Objects.requireNonNull(value, "value");
    }

    public String getKey() { return key; }
    public String getValue() { return value; }
}
