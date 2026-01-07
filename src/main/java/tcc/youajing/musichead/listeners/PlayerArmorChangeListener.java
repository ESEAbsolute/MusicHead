package tcc.youajing.musichead.listeners;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import tcc.youajing.musichead.MusicHead;
import tcc.youajing.musichead.managers.RegistryManager;

public class PlayerArmorChangeListener implements Listener {
    private final MusicHead plugin;

    public PlayerArmorChangeListener(MusicHead plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerArmorChange(PlayerArmorChangeEvent event) {
        if (event.getSlot() != EquipmentSlot.HEAD) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack oldItem = event.getOldItem();
        ItemStack newItem = event.getNewItem();

        if (isValidDisc(oldItem)) {
            plugin.getMusicManager().stopMusic(player);

            NamespacedKey sound = RegistryManager.getSoundNamespacedKey(oldItem);
            World world = player.getWorld();

            RegistryManager.ReplacementData replacement = RegistryManager.getSoundReplacement(String.valueOf(sound));
            NamespacedKey finalSoundKey = (replacement != null && replacement.replacement() != null) ?
                    NamespacedKey.fromString(replacement.replacement()) : sound;
            if (finalSoundKey != null) {
                world.stopSound(SoundStop.named(finalSoundKey));
            }
        }

        if (isValidDisc(newItem)) {
            NamespacedKey key = RegistryManager.getSoundNamespacedKey(newItem);
            String soundKey = key.toString();

            // Play music via manager (handles settings and replay)
            plugin.getMusicManager().playMusic(player, newItem, soundKey);

            Component actionbarMessage = Component.empty()
                    .append(Component.text(plugin.getMessageUtils().get("message.now_playing_prefix")).color(NamedTextColor.AQUA))
                    .append(getMusicNameAsTranslatableComponent(newItem))
                    .append(Component.text(plugin.getMessageUtils().get("message.now_playing_suffix")).color(NamedTextColor.AQUA));

            player.sendActionBar(actionbarMessage);
        }
    }

    private static boolean isValidDisc(ItemStack item) {
        if (item.hasItemMeta()) {
            return item.getItemMeta().hasJukeboxPlayable() || item.getType().isRecord();
        }
        return item.getType().isRecord();
    }

    public static Component getMusicNameAsTranslatableComponent(ItemStack item) {
        if (!isValidDisc(item)) return Component.empty();

        NamespacedKey value = RegistryManager.getSoundNamespacedKey(item);

        if (value != null) {
            String key;
            if (value.getNamespace().equals("minecraft")) {
                key = "jukebox_song.minecraft." + value.getKey().replace("music_disc.", "");
            } else {
                key = "jukebox_song." + value.getNamespace() + "." + value.getKey();
            }

            RegistryManager.ReplacementData replacement = RegistryManager.getLangReplacement(key);

            if (replacement != null && replacement.replacement() != null) {
                var builder = Component.translatable().key(replacement.replacement()).color(NamedTextColor.YELLOW);
                if (replacement.fallback() != null) {
                    builder.fallback(replacement.fallback());
                }
                return builder.build();
            }

            if (replacement != null && replacement.fallback() != null) {
                return Component.text(replacement.fallback()).color(NamedTextColor.YELLOW);
            }

            return Component.translatable(key).color(NamedTextColor.YELLOW);
        }
        return Component.empty();
    }
}
