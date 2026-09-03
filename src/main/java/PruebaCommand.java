import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class PruebaCommand implements CommandExecutor, TabCompleter {
    private static final String PERMISSION = "prueba.use";

    private final KeyValueService service;
    private final EntryListGui listGui;

    public PruebaCommand(KeyValueService service, EntryListGui listGui) {
        this.service = service;
        this.listGui = listGui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            MessageUtil.send(sender, "&cNo tienes permiso para usar este comando.");
            return true;
        }
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "save": return handleSave(sender, args);
            case "view": return handleView(sender, args);
            case "list": return handleList(sender);
            default:
                sendUsage(sender);
                return true;
        }
    }

    private boolean handleSave(CommandSender sender, String[] args) {
        if (args.length < 3) {
            MessageUtil.send(sender, "&eUso: /prueba save <key> <value>");
            return true;
        }

        String key = args[1];
        String value = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        service.save(key, value).whenComplete((ignored, throwable) -> runOnMain(sender, () -> {
            if (throwable != null) {
                MessageUtil.send(sender, "&cNo se pudo guardar: " + rootMessage(throwable));
                return;
            }
            MessageUtil.send(sender, "&aRegistro &f" + key + " &aguardado correctamente.");
        }));
        return true;
    }

    private boolean handleView(CommandSender sender, String[] args) {
        if (args.length != 2) {
            MessageUtil.send(sender, "&eUso: /prueba view <key>");
            return true;
        }

        String key = args[1];
        service.find(key).whenComplete((result, throwable) -> runOnMain(sender, () -> {
            if (throwable != null) {
                MessageUtil.send(sender, "&cNo se pudo consultar: " + rootMessage(throwable));
                return;
            }
            if (!result.isPresent()) {
                MessageUtil.send(sender, "&cNo existe ningún registro con la key &f" + key + "&c.");
                return;
            }
            KeyValueEntry entry = result.get();
            MessageUtil.send(sender, "&6&m------------------------------");
            MessageUtil.send(sender, "&eKey: &f" + entry.getKey());
            MessageUtil.sendRaw(sender, "&eValue: &f", entry.getValue());
            MessageUtil.send(sender, "&6&m------------------------------");
        }));
        return true;
    }

    private boolean handleList(CommandSender sender) {
        if (!(sender instanceof Player)) {
            MessageUtil.send(sender, "&cEste subcomando requiere un jugador.");
            return true;
        }
        Player player = (Player) sender;
        service.findAll().whenComplete((entries, throwable) -> runOnMain(player, () -> {
            if (throwable != null) {
                MessageUtil.send(player, "&cNo se pudo cargar la lista: " + rootMessage(throwable));
                return;
            }
            listGui.open(player, entries, 0);
        }));
        return true;
    }

    private void sendUsage(CommandSender sender) {
        MessageUtil.send(sender, "&6&m------------------------------");
        MessageUtil.send(sender, "&e/prueba save <key> <value>");
        MessageUtil.send(sender, "&e/prueba view <key>");
        MessageUtil.send(sender, "&e/prueba list");
        MessageUtil.send(sender, "&6&m------------------------------");
    }

    private void runOnMain(CommandSender sender, Runnable task) {
        listGui.getPlugin().getServer().getScheduler().runTask(listGui.getPlugin(), task);
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) current = current.getCause();
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> options = Arrays.asList("save", "view", "list");
            List<String> result = new ArrayList<>();
            for (String option : options) {
                if (option.startsWith(args[0].toLowerCase())) result.add(option);
            }
            return result;
        }
        return Collections.emptyList();
    }
}
