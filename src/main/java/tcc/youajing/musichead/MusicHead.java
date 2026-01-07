package tcc.youajing.musichead;

import crypticlib.BukkitPlugin;
import org.bukkit.Bukkit;
import tcc.youajing.musichead.commands.MusicHeadCommand;
import tcc.youajing.musichead.listeners.PlayerArmorChangeListener;
import tcc.youajing.musichead.managers.ConfigManager;
import tcc.youajing.musichead.managers.DatabaseManager;
import tcc.youajing.musichead.managers.MusicManager;
import tcc.youajing.musichead.managers.RegistryManager;
import tcc.youajing.musichead.messages.MessageUtils;

import java.util.logging.Level;

public class MusicHead extends BukkitPlugin {
    private ConfigManager configManager;
    private MessageUtils messageUtils;
    private DatabaseManager databaseManager;
    private MusicManager musicManager;
    private RegistryManager soundRegistryManager;

    @Override
    public void enable() {
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        messageUtils = new MessageUtils(this);

        databaseManager = new DatabaseManager(this);
        databaseManager.init();

        musicManager = new MusicManager(this);
        musicManager.loadMusicDurations();

        soundRegistryManager = new RegistryManager(this);
        soundRegistryManager.loadOverrides();

        registerCommands();
        registerListeners();

        messageUtils.log(Level.INFO, "log.plugin.enable.header");
        messageUtils.log(Level.INFO, "log.plugin.enable.message");
        messageUtils.log(Level.INFO, "log.plugin.enable.version", getDescription().getVersion());
        messageUtils.log(Level.INFO, "log.plugin.enable.author", String.valueOf(getDescription().getAuthors()));
        messageUtils.log(Level.INFO, "log.plugin.enable.footer");
    }

    private void registerCommands() {
        getCommand("musichead").setExecutor(new MusicHeadCommand(this));
        getCommand("musichead").setTabCompleter(new MusicHeadCommand(this));
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerArmorChangeListener(this), this);
    }

    @Override
    public void disable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        messageUtils.log(Level.INFO, "log.plugin.disable.header");
        messageUtils.log(Level.INFO, "log.plugin.disable.message");
        messageUtils.log(Level.INFO, "log.plugin.disable.footer");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageUtils getMessageUtils() {
        return messageUtils;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public MusicManager getMusicManager() {
        return musicManager;
    }

    public RegistryManager getSoundRegistryManager() {
        return soundRegistryManager;
    }
}
