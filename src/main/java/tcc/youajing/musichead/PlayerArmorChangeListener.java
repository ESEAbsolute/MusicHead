package tcc.youajing.musichead;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.JukeboxPlayableComponent;

import java.util.Objects;


public class PlayerArmorChangeListener implements Listener {
    private final MusicHead plugin;


    public PlayerArmorChangeListener(MusicHead plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerArmorChange(PlayerArmorChangeEvent event) {
        Player player = event.getPlayer();
        ItemStack oldItem;
        ItemStack newItem;
        PlayerArmorChangeEvent.SlotType slotType = event.getSlotType();

        if (slotType != PlayerArmorChangeEvent.SlotType.HEAD) {
            return;
        }

        oldItem = event.getOldItem();
        newItem = event.getNewItem();

        if (isValidDisc(oldItem)) {
            String sound = getJukeboxPlayableMusicNamespacedKey(oldItem);
            World world = player.getWorld();
            if (sound != null) {
                world.stopSound(SoundStop.named(Key.key(sound)));
            } else {
                Sound fallback = getSoundFromItemFallback(newItem);
                if (fallback != null) {
                    world.stopSound(SoundStop.named(fallback));
                }
            }
        }

        if (isValidDisc(newItem)) {
            String sound = getJukeboxPlayableMusicNamespacedKey(newItem);
            World world = player.getWorld();

            if (sound != null) {
                world.playSound(player, sound, 1.0f, 1.0f);
            } else {
                Sound fallback = getSoundFromItemFallback(newItem);
                if (fallback != null) {
                    world.playSound(player, fallback, 1.0f, 1.0f);
                }
            }

            Component actionbarMessage = Component.empty()
                    .append(Component.text("- 你现在是一个 ").color(NamedTextColor.AQUA))
                    .append(getMusicNameAsTranslatableComponent(newItem))
                    .append(Component.text(" 唱片机 -").color(NamedTextColor.AQUA));

            player.sendActionBar(actionbarMessage);
        }
    }

    private static boolean isValidDisc(ItemStack item) {
        if (item.hasItemMeta()) {
            return item.getItemMeta().hasJukeboxPlayable();
        }
        return item.getType().isRecord();
    }

    public static NamespacedKey getRawJukeboxPlayableComponentValue(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        if (meta.hasJukeboxPlayable()) {
            JukeboxPlayableComponent component = meta.getJukeboxPlayable();
//            MusicHead.INSTANCE.getLogger().info(component.getSongKey().toString());
            return component.getSongKey();
        }
        return null;
    }

    public static String getJukeboxPlayableMusicNamespacedKey(ItemStack item) {
        NamespacedKey raw = getRawJukeboxPlayableComponentValue(item);
        if (raw == null) return null;
        return raw.getNamespace() + ":music_disc." + raw.getKey();
    }

    public static Component getMusicNameAsTranslatableComponent(ItemStack item) {
        if (!isValidDisc(item)) return Component.empty();

        NamespacedKey value = getRawJukeboxPlayableComponentValue(item);

        if (value != null) {
            return Component.translatable("jukebox_song." + value.getNamespace() + "." + value.getKey())
                    .color(NamedTextColor.YELLOW);
        }

        String name = getNameFromItemFallback(item);
        if (name != null) {
            return Component.translatable("jukebox_song.minecraft." + name)
                    .color(NamedTextColor.YELLOW);
        }
        return Component.empty();
    }

    private static Sound getSoundFromItemFallback(ItemStack item) {
        String music = getNameFromItemFallback(item);
        if (music == null) {
            return null;
        }
        return Objects.requireNonNull(Registry.SOUNDS.get(NamespacedKey.minecraft("music_disc.%s".formatted(music))), "Missing sound " + music);
    }


    private static String getNameFromItemFallback(ItemStack item) {
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
            default -> null;
        };
    }
}
