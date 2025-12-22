package tcc.youajing.musichead;

import crypticlib.BukkitPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class MusicHead extends BukkitPlugin {

    @Override
    public void enable() {
        //TODO
        // 初始化团队管理器和监听器

        PlayerArmorChangeListener Listener = new PlayerArmorChangeListener(this);
        // 注册命令和监听器
        getServer().getPluginManager().registerEvents(Listener, this);
        // team,启动！
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[MusicHead]" + ChatColor.AQUA + "######################");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[MusicHead]" + ChatColor.AQUA + "#                    #");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[MusicHead]" + ChatColor.AQUA + "#头戴音乐已启动#");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[MusicHead]" + ChatColor.AQUA + "#                    #");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[MusicHead]" + ChatColor.AQUA + "######################");
    }

    @Override
    public void disable() {
        //TODO

        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[MusicHead]" + ChatColor.RED + "######################");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[MusicHead]" + ChatColor.RED + "#                    #");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[MusicHead]" + ChatColor.RED + "#头戴音乐已关闭#");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[MusicHead]" + ChatColor.RED + "#                    #");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[MusicHead]" + ChatColor.RED + "######################");


    }



}