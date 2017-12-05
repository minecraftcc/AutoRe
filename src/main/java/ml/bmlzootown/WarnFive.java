package ml.bmlzootown;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class WarnFive extends BukkitRunnable{

    private final JavaPlugin plugin;

    public WarnFive(JavaPlugin plugin) {
        this.plugin = plugin;
    }


    @Override
    public void run() {
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        plugin.getServer().broadcastMessage(ChatColor.RED + "[AutoRe] Restarting server in " + ChatColor.YELLOW + "5" + ChatColor.RED + " minutes.");
        Bukkit.dispatchCommand(console, "title @a times 10 60 10");
        Bukkit.dispatchCommand(console, "title @a subtitle {\"text\":\"A server restart will occur in 5 minutes.\"}");
        Bukkit.dispatchCommand(console, "title @a title {\"text\": \"Restarting soon!\",\"color\": \"red\"}");
    }

}
