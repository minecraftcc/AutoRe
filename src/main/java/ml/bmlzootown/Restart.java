package ml.bmlzootown;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Restart extends BukkitRunnable{

    private final JavaPlugin plugin;

    public Restart(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getServer().broadcastMessage(ChatColor.RED + "[AutoRe] Restarting now!");
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        Bukkit.dispatchCommand(console, AutoRe.getCmd());
    }
}
