package com.savagepvp.prueba;

import com.savagepvp.prueba.command.PruebaCommand;
import com.savagepvp.prueba.gui.EntryListGui;
import com.savagepvp.prueba.repository.KeyValueRepository;
import com.savagepvp.prueba.repository.SqliteKeyValueRepository;
import com.savagepvp.prueba.service.KeyValueService;
import com.savagepvp.prueba.validation.KeyValidator;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;

public final class SavagePvPPlugin extends JavaPlugin {
    private KeyValueRepository repository;
    private KeyValueService keyValueService;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().severe("No se pudo crear la carpeta de datos.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        String fileName = getConfig().getString("storage.file", "data.db");
        try {
            repository = new SqliteKeyValueRepository(new File(getDataFolder(), fileName).getPath());
        } catch (SQLException exception) {
            getLogger().severe("No se pudo inicializar SQLite: " + exception.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        keyValueService = new KeyValueService(repository, new KeyValidator());
        EntryListGui gui = new EntryListGui(this);
        getServer().getPluginManager().registerEvents(gui, this);

        PluginCommand command = getCommand("prueba");
        if (command == null) {
            getLogger().severe("El comando /prueba no está registrado en plugin.yml.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        PruebaCommand executor = new PruebaCommand(keyValueService, gui);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
        getLogger().info("SavagePvP-Prueba habilitado (Java 21, SQLite asíncrono).");
    }

    @Override
    public void onDisable() {
        if (repository != null) repository.close();
    }

    public KeyValueService getKeyValueService() {
        return keyValueService;
    }
}
