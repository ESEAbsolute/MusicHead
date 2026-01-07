package tcc.youajing.musichead.managers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.JukeboxPlayableComponent;
import tcc.youajing.musichead.MusicHead;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class RegistryManager {
    private final MusicHead plugin;
    private static final Map<String, ReplacementData> LANG_OVERRIDES = new HashMap<>();
    private static final Map<String, ReplacementData> SOUND_OVERRIDES = new HashMap<>();
    private static final Gson GSON = new Gson();

    public RegistryManager(MusicHead plugin) {
        this.plugin = plugin;
    }

    // Replacement Logic

    public void loadOverrides() {
        loadOverrideData("lang.json", LANG_OVERRIDES);
        loadOverrideData( "sounds.json", SOUND_OVERRIDES);
    }

    private void loadOverrideData(String fileName, Map<String, ReplacementData> targetMap) {
        targetMap.clear();
        File file = new File(plugin.getDataFolder(), "key_override/" + fileName);
        if (!file.exists()) {
            File dir = file.getParentFile();
            if (!dir.exists()) dir.mkdirs();
            
            if (plugin.getResource("key_override/" + fileName) != null) {
                plugin.saveResource("key_override/" + fileName, false);
            } else {
                return;
            }
        }

        try (FileReader reader = new FileReader(file)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            if (json != null) {
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    if (entry.getValue().isJsonObject()) {
                        JsonObject data = entry.getValue().getAsJsonObject();
                        String replacement = null;
                        if (data.has("replacement")) replacement = data.get("replacement").getAsString();

                        String fallback = data.has("fallback") ? data.get("fallback").getAsString() : null;
                        targetMap.put(entry.getKey(), new ReplacementData(replacement, fallback));
                    }
                }
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load " + fileName, e);
        }
    }

    public static ReplacementData getLangReplacement(String path) {
        return LANG_OVERRIDES.get(path);
    }

    public static ReplacementData getSoundReplacement(String key) {
        return SOUND_OVERRIDES.get(key);
    }

    public record ReplacementData(String replacement, String fallback) {
    }

    // Music Logic

    public static NamespacedKey getSoundNamespacedKey(ItemStack item) {
        NamespacedKey namespacedKey = getNamespacedKeyFromJukeboxPlayable(item);
        if (namespacedKey == null) {
            return getNamespacedKeyFromItemFallback(item);
        }
        return namespacedKey;
    }

    private static NamespacedKey getNamespacedKeyFromJukeboxPlayable(ItemStack item) {
        NamespacedKey namespacedKey = getJukeboxPlayableMusicNamespacedKey(item);
        if (namespacedKey == null) {
            return null;
        }
        if (namespacedKey.getNamespace().equals("minecraft")) {
            return NamespacedKey.minecraft("music_disc." + namespacedKey.getKey());
        }
        return namespacedKey;
    }

    // Only get "jukebox_playable" component
    @SuppressWarnings("UnstableApiUsage")
    private static NamespacedKey getJukeboxPlayableMusicNamespacedKey(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        if (meta.hasJukeboxPlayable()) {
            JukeboxPlayableComponent component = meta.getJukeboxPlayable();
            return component.getSongKey();
        }
        return null;
    }

    private static NamespacedKey getNamespacedKeyFromItemFallback(ItemStack item) {
        String music = getMusicKeyFromItemFallback(item);
        if (music == null) {
            return null;
        }
        return NamespacedKey.minecraft(music);
    }

    private static String getMusicKeyFromItemFallback(ItemStack item) {
        String name = getNameFromItemFallback(item);
        if (name != null) {
            return "music_disc." + name;
        }
        return null;
    }

    public static String getNameFromItemFallback(ItemStack item) {
        Material material = item.getType();
        return switch (material) {
            case MUSIC_DISC_13 -> "13";
            case MUSIC_DISC_CAT -> "cat";
            case MUSIC_DISC_BLOCKS -> "blocks";
            case MUSIC_DISC_CHIRP -> "chirp";
            case MUSIC_DISC_FAR -> "far";
            case MUSIC_DISC_MALL -> "mall";
            case MUSIC_DISC_MELLOHI -> "mellohi";
            case MUSIC_DISC_STAL -> "stal";
            case MUSIC_DISC_STRAD -> "strad";
            case MUSIC_DISC_WARD -> "ward";
            case MUSIC_DISC_11 -> "11";
            case MUSIC_DISC_WAIT -> "wait";
            case MUSIC_DISC_PIGSTEP -> "pigstep";
            case MUSIC_DISC_OTHERSIDE -> "otherside";
            case MUSIC_DISC_5 -> "5";
            case MUSIC_DISC_RELIC -> "relic";
            case MUSIC_DISC_PRECIPICE -> "precipice";
            case MUSIC_DISC_CREATOR -> "creator";
            case MUSIC_DISC_CREATOR_MUSIC_BOX -> "creator_music_box";
            case MUSIC_DISC_TEARS -> "tears";
            default -> null;
        };
    }
}
