package tcc.youajing.musichead.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import tcc.youajing.musichead.MusicHead;
import tcc.youajing.musichead.objects.PlayerSettings;
import tcc.youajing.musichead.messages.MessageLevel;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MusicManager {
    private final MusicHead plugin;
    private final Map<String, Long> musicDurations = new HashMap<>();
    private final Map<UUID, ScheduledTask> activeTasks = new HashMap<>();

    public MusicManager(MusicHead plugin) {
        this.plugin = plugin;
    }

    public void loadMusicDurations() {
        musicDurations.clear();
        File file = new File(plugin.getDataFolder(), "musicinfo.yml");
        if (!file.exists()) {
            plugin.saveResource("musicinfo.yml", false);
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(true)) {
            String durationStr = config.getString(key);
            if (durationStr != null) {
                long ticks = parseDuration(durationStr);
                if (ticks > 0) {
                    musicDurations.put(key, ticks);
                    plugin.getMessageUtils().log(
                            MessageLevel.SUCCESS,
                            "message.music.duration_load",
                            key,
                            durationStr,
                            "%d".formatted(ticks)
                    );
                }
            }
        }
    }

    private long parseDuration(String durationStr) {
        try {
            durationStr = durationStr.replace(".", ":");
            String[] parts = durationStr.split(":");
            if (parts.length == 2) {
                int minutes = Integer.parseInt(parts[0]);
                int seconds = Integer.parseInt(parts[1]);
                return (minutes * 60L + seconds) * 20L;
            }
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Invalid music duration format: " + durationStr);
        }
        return 0;
    }

    public void playMusic(Player player, ItemStack item, String soundKey) {
        stopMusic(player);

        if (soundKey == null) return;

        // Check for sound key replacement
        RegistryManager.ReplacementData replacement = RegistryManager.getSoundReplacement(soundKey);
        String finalSoundKey = (replacement != null && replacement.replacement() != null) ? replacement.replacement() : soundKey;
        
        // Convert soundKey to musicinfo key format (colon to period)
        String lookupKey = soundKey.replace(":", ".");
        
        for (Player p : player.getWorld().getPlayers()) {
            PlayerSettings receiver = plugin.getDatabaseManager().getSettings(p.getUniqueId());
            float v = receiver.getVolume() / 100.0f;
            if (p != player && (!receiver.isMusicEnabled() || v <= 0f)) continue;
            p.playSound(player.getLocation(), finalSoundKey, v, 1.0f);
        }

        // Schedule replay or warn if missing duration
        Long durationTicks = musicDurations.get(lookupKey);
        if (durationTicks != null) {
            ScheduledTask task = player.getScheduler().runDelayed(
                    plugin,
                    scheduledTask -> {
                        if (player.isOnline() && isValidHelmet(player, item)) {
                            playMusic(player, item, soundKey);
                        }
                    },
                    () -> activeTasks.remove(player.getUniqueId()),
                    durationTicks
            );
            if (task != null) {
                activeTasks.put(player.getUniqueId(), task);
            }
        } else {
            plugin.getMessageUtils().sendMessage(
                    player,
                    MessageLevel.WARNING,
                    "message.music.duration_missing",
                    soundKey,
                    lookupKey
            );
        }
    }

    public void stopMusic(Player player) {
        ScheduledTask task = activeTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }
    
    private boolean isValidHelmet(Player player, ItemStack item) {
        ItemStack helmet = player.getInventory().getHelmet();
        return helmet != null && helmet.isSimilar(item);
    }
}
