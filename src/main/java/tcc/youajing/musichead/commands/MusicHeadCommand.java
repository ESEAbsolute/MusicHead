package tcc.youajing.musichead.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import tcc.youajing.musichead.MusicHead;
import tcc.youajing.musichead.messages.MessageLevel;
import tcc.youajing.musichead.objects.PlayerSettings;

import java.util.ArrayList;
import java.util.List;

public class MusicHeadCommand implements CommandExecutor, TabCompleter {
    private final MusicHead plugin;

    public MusicHeadCommand(MusicHead plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NonNull [] args) {
        if (args.length == 0) {
            plugin.getMessageUtils().sendMessage(sender, MessageLevel.INFO, "command.usage", getUsageOptions(sender));
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("reload")) {
            if (!sender.hasPermission("musichead.command.reload")) {
                plugin.getMessageUtils().sendMessage(sender, MessageLevel.ERROR, "command.no_permission");
                return true;
            }
            plugin.getConfigManager().loadConfig();
            plugin.getMusicManager().loadMusicDurations();
            plugin.getSoundRegistryManager().loadOverrides();
            plugin.getMessageUtils().sendMessage(sender, MessageLevel.SUCCESS, "command.reload.success");
            return true;
        }

        if (!(sender instanceof Player player)) {
            plugin.getMessageUtils().sendMessage(sender, MessageLevel.ERROR, "command.player_only");
            return true;
        }

        switch (subCommand) {
            case "volume" -> {
                if (!player.hasPermission("musichead.command.volume")) {
                    plugin.getMessageUtils().sendMessage(sender, MessageLevel.ERROR, "command.no_permission");
                    return true;
                }
                if (args.length < 2) {
                    plugin.getMessageUtils().sendMessage(sender, MessageLevel.ERROR, "command.volume.usage");
                    return true;
                }
                try {
                    int vol = Integer.parseInt(args[1]);
                    if (vol < 0 || vol > 100) {
                        plugin.getMessageUtils().sendMessage(sender, MessageLevel.ERROR, "command.volume.invalid");
                        return true;
                    }
                    PlayerSettings settings = plugin.getDatabaseManager().getSettings(player.getUniqueId());
                    settings.setVolume(vol);
                    plugin.getDatabaseManager().updateSettings(player.getUniqueId(), settings);
                    plugin.getMessageUtils().sendMessage(sender, MessageLevel.SUCCESS, "command.volume.success", String.valueOf(vol));
                } catch (NumberFormatException e) {
                    plugin.getMessageUtils().sendMessage(sender, MessageLevel.ERROR, "command.volume.invalid");
                }
            }
            case "toggle" -> {
                if (!player.hasPermission("musichead.command.toggle")) {
                    plugin.getMessageUtils().sendMessage(sender, MessageLevel.ERROR, "command.no_permission");
                    return true;
                }
                PlayerSettings settings = plugin.getDatabaseManager().getSettings(player.getUniqueId());
                boolean newState = !settings.isMusicEnabled();
                settings.setMusicEnabled(newState);
                plugin.getDatabaseManager().updateSettings(player.getUniqueId(), settings);

                String key = newState ? "command.toggle.on" : "command.toggle.off";
                plugin.getMessageUtils().sendMessage(sender, MessageLevel.SUCCESS, key);

                if (!newState) {
                    plugin.getMusicManager().stopMusic(player);
                }
            }
            case "enable" -> {
                if (!player.hasPermission("musichead.command.enable")) {
                    plugin.getMessageUtils().sendMessage(sender, MessageLevel.ERROR, "command.no_permission");
                    return true;
                }
                PlayerSettings settings = plugin.getDatabaseManager().getSettings(player.getUniqueId());
                if (settings.isMusicEnabled()) {
                    plugin.getMessageUtils().sendMessage(sender, MessageLevel.INFO, "command.enable.already_enabled");
                    return true;
                }
                settings.setMusicEnabled(true);
                plugin.getDatabaseManager().updateSettings(player.getUniqueId(), settings);
                plugin.getMessageUtils().sendMessage(sender, MessageLevel.SUCCESS, "command.toggle.on");
            }
            case "disable" -> {
                if (!player.hasPermission("musichead.command.disable")) {
                    plugin.getMessageUtils().sendMessage(sender, MessageLevel.ERROR, "command.no_permission");
                    return true;
                }
                PlayerSettings settings = plugin.getDatabaseManager().getSettings(player.getUniqueId());
                if (!settings.isMusicEnabled()) {
                    plugin.getMessageUtils().sendMessage(sender, MessageLevel.INFO, "command.disable.already_disabled");
                    return true;
                }
                settings.setMusicEnabled(false);
                plugin.getDatabaseManager().updateSettings(player.getUniqueId(), settings);
                plugin.getMessageUtils().sendMessage(sender, MessageLevel.SUCCESS, "command.toggle.off");
                plugin.getMusicManager().stopMusic(player);
            }
            default -> plugin.getMessageUtils().sendMessage(sender, MessageLevel.INFO, "command.usage", getUsageOptions(sender));
        }
        return true;
    }
    
    private String getUsageOptions(CommandSender sender) {
        List<String> list = new ArrayList<>();
        if (sender.hasPermission("musichead.command.volume")) list.add("volume");
        if (sender.hasPermission("musichead.command.toggle")) list.add("toggle");
        if (sender.hasPermission("musichead.command.enable")) list.add("enable");
        if (sender.hasPermission("musichead.command.disable")) list.add("disable");
        if (sender.hasPermission("musichead.command.reload")) list.add("reload");
        return String.join("|", list);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NonNull [] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if (sender.hasPermission("musichead.command.volume")) completions.add("volume");
            if (sender.hasPermission("musichead.command.toggle")) completions.add("toggle");
            if (sender.hasPermission("musichead.command.enable")) completions.add("enable");
            if (sender.hasPermission("musichead.command.disable")) completions.add("disable");
            if (sender.hasPermission("musichead.command.reload")) completions.add("reload");
            return completions;
        }
        return List.of();
    }
}
