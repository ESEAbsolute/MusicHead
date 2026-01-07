package tcc.youajing.musichead;

import crypticlib.BukkitPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import tcc.youajing.musichead.listeners.PlayerArmorChangeListener;
import tcc.youajing.musichead.managers.ConfigManager;
import tcc.youajing.musichead.managers.DatabaseManager;
import tcc.youajing.musichead.managers.MusicManager;
import tcc.youajing.musichead.managers.RegistryManager;
import tcc.youajing.musichead.messages.MessageUtils;

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

        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[MusicHead]" + ChatColor.BLUE + "# MusicHead plugin has enabled! #");
    }

    private void registerCommands() {
        getCommand("musichead").setExecutor(new tcc.youajing.musichead.commands.MusicHeadCommand(this));
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerArmorChangeListener(this), this);
    }

    @Override
    public void disable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[MusicHead]" + ChatColor.RED + "# MusicHead plugin has disabled! #");
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
