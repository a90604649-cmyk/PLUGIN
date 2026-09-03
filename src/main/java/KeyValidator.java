import java.util.regex.Pattern;

public final class KeyValidator {
    private static final Pattern VALID_KEY = Pattern.compile("^[a-zA-Z0-9_]{1,16}$");

    public boolean isValid(String key) {
        return key != null && VALID_KEY.matcher(key).matches();
    }
}
