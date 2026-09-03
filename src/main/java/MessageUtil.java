import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public final class MessageUtil {
    private MessageUtil() { }

    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static void send(CommandSender sender, String message) {
        sender.sendMessage(color(message));
    }

    public static void sendRaw(CommandSender sender, String prefix, String value) {
        sender.sendMessage(color(prefix) + value);
    }
}
