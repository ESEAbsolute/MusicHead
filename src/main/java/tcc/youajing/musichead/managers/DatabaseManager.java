package tcc.youajing.musichead.managers;

import org.bukkit.Bukkit;
import tcc.youajing.musichead.MusicHead;
import tcc.youajing.musichead.objects.PlayerSettings;

import java.io.File;
import java.sql.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class DatabaseManager {
    private final MusicHead plugin;
    private Connection connection;
    private final Map<UUID, PlayerSettings> settingsCache = new ConcurrentHashMap<>();

    public DatabaseManager(MusicHead plugin) {
        this.plugin = plugin;
    }

    public void init() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + new File(dataFolder, "database.db").getAbsolutePath());
            
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS player_settings (" +
                        "uuid TEXT PRIMARY KEY, " +
                        "volume INTEGER DEFAULT 100, " +
                        "enabled INTEGER DEFAULT 1" +
                        ")");
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database", e);
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to close database connection", e);
        }
    }

    public PlayerSettings getSettings(UUID uuid) {
        if (settingsCache.containsKey(uuid)) {
            return settingsCache.get(uuid);
        }

        PlayerSettings settings = loadSettingsFromDb(uuid);
        settingsCache.put(uuid, settings);
        return settings;
    }

    private PlayerSettings loadSettingsFromDb(UUID uuid) {
        String query = "SELECT volume, enabled FROM player_settings WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new PlayerSettings(rs.getInt("volume"), rs.getInt("enabled") == 1);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load settings for " + uuid, e);
        }
        // Default settings
        return new PlayerSettings(100, true);
    }

    public void updateSettings(UUID uuid, PlayerSettings settings) {
        settingsCache.put(uuid, settings);
        plugin.getServer().getAsyncScheduler().runNow(plugin, scheduledTask -> {
            String query = "INSERT OR REPLACE INTO player_settings (uuid, volume, enabled) VALUES (?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, uuid.toString());
                ps.setInt(2, settings.getVolume());
                ps.setInt(3, settings.isMusicEnabled() ? 1 : 0);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save settings for " + uuid, e);
            }
        });
    }
    
    public void invalidateCache(UUID uuid) {
        settingsCache.remove(uuid);
    }
}
