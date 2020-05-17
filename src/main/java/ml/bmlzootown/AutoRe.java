package ml.bmlzootown;

import ml.bmlzootown.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AutoRe extends JavaPlugin {
    private static AutoRe plugin;
    private static Logger log = Bukkit.getLogger();

    private long current_h = 0;
    private long current_m = 0;
    private long current_s = 0;

    private static long startTime;
    private static long stopTime;
    private static long seconds;

    private boolean enabled = true;

    private static String command = "stop";

    private static List<Integer> warnings = new ArrayList<>();
    private static List<TimerTask> warningTasks = new ArrayList<>();
    private static Timer warningTask;
    private static Timer restartTask;

    public void onEnable() {
        plugin = this;
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            ConfigManager.setConfigDefaults();
        }
        reload();
    }

    public void onDisable() {
        warnings = null;
        warningTask = null;
        warningTasks = null;
        restartTask = null;
    }

    public static Plugin getP() {
        return plugin;
    }

    private void loadConfig() {
        //saveDefaultConfig();
        reloadConfig();
        command = ConfigManager.getCommand();
        enabled = ConfigManager.isEnabled();
        current_h = ConfigManager.getH();
        current_m = ConfigManager.getM();
        current_s = ConfigManager.getS();
        warnings = ConfigManager.getWarningTimes();
    }

    private void reload() {
        cancelRestart();
        loadConfig();
        if (enabled) {
            scheduleRestart(current_h, current_m, current_s);
        }
        log.log(Level.INFO, ChatColor.RED + "[AutoRe] " + ChatColor.WHITE + "Reloaded config.");
    }

    private String cancelRestart() {
        enabled = false;

        if (warningTasks == null || warningTasks.isEmpty()) {
            //log.log(Level.INFO, ChatColor.RED + "[AutoRe] " + ChatColor.WHITE + "No restart scheduled.");
            return (ChatColor.RED + "[AutoRe] " + ChatColor.WHITE + "No restart scheduled.");
        }

        log.log(Level.INFO, ChatColor.RED + "[AutoRe] " + ChatColor.WHITE + "Cancelling restart.");
        for (TimerTask time : warningTasks) {
            time.cancel();
        }
        warningTask.cancel();
        restartTask.cancel();
        current_s = 0;
        current_m = 0;
        current_h = 0;
        warningTasks = new ArrayList<>();
        restartTask = new Timer();
        warningTask = new Timer();
        log.log(Level.INFO, ChatColor.RED + "[AutoRe] " + ChatColor.WHITE + "Restart cancelled.");
        return ChatColor.RED + "[AutoRe] " + ChatColor.WHITE + "Restart cancelled.";
    }

    private void scheduleRestart(long h, long m, long s) {
        cancelRestart();
        enabled = true;
        current_h = h;
        current_m = m;
        current_s = s;

        startTime = System.nanoTime();
        seconds = ((h*60)*60) + (m*60) + s;
        stopTime = (seconds * 1000000000) + startTime;

        for (int warn : warnings) {
            if (seconds > warn) {
                scheduleWarning(warn);
            }
        }

        restartTask = new Timer();
        TimerTask reTask = new TimerTask() {
            @Override
            public void run() {
                plugin.getServer().broadcastMessage(ChatColor.RED + "[AutoRe] Restarting now!");
                ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                try {
                    Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                        Bukkit.dispatchCommand(console, command);
                        return true;
                    }).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        };
        restartTask.schedule(reTask, (seconds * 1000));
        log.log(Level.INFO, ChatColor.RED + "[AutoRe] " + ChatColor.WHITE + "Restart Scheduled: " + current_h + "h " + current_m + "m " + current_s + "s");
    }

    private void scheduleWarning(int time) {
        Timer warning = new Timer(true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                if (time > 59) {
                    int minutes = time / 60;
                    if (minutes > 59) {
                        int hours = time / 3600;
                        if (hours > 24) {
                            try {
                                Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                                    Bukkit.broadcastMessage(ChatColor.RED + "[AutoRe] Restarting server in " + ChatColor.YELLOW + hours + ChatColor.RED + " hours.");
                                    for (Player p : Bukkit.getOnlinePlayers()) {
                                        p.sendTitle(
                                                ChatColor.RED + "Restarting some time next year, I think!",
                                                "A server restart will occur in " + hours + " hours.",
                                                10 ,
                                                60,
                                                10);
                                    }
                                    return true;
                                }).get();
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }
                        } else  {
                            try {
                                Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                                    Bukkit.broadcastMessage(ChatColor.RED + "[AutoRe] Restarting server in " + ChatColor.YELLOW + hours + ChatColor.RED + " hours.");
                                    for (Player p : Bukkit.getOnlinePlayers()) {
                                        p.sendTitle(
                                                ChatColor.RED + "Restarting... soon?",
                                                "A server restart will occur in " + hours + " hours.",
                                                10 ,
                                                60,
                                                10);
                                    }
                                    return true;
                                }).get();
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        try {
                            Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                                Bukkit.broadcastMessage(ChatColor.RED + "[AutoRe] Restarting server in " + ChatColor.YELLOW + minutes + ChatColor.RED + " minutes.");
                                for (Player p : Bukkit.getOnlinePlayers()) {
                                    p.sendTitle(
                                            ChatColor.RED + "Restarting soon!",
                                            "A server restart will occur in " + minutes + " minutes.",
                                            10 ,
                                            60,
                                            10);
                                }
                                return true;
                            }).get();
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                            Bukkit.broadcastMessage(ChatColor.RED + "[AutoRe] Restarting server in " + ChatColor.YELLOW + time + ChatColor.RED + " seconds.");
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                p.sendTitle(
                                        ChatColor.RED + "Restarting soon!",
                                        "A server restart will occur in " + time + " seconds.",
                                        10 ,
                                        60,
                                        10);
                            }
                            return true;
                        }).get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        warning.schedule(task, ((seconds - time) * 1000));
        warningTask = warning;
        warningTasks.add(task);
    }

    private static String remainingTime() {
        double seconds = (stopTime - System.nanoTime()) / 1000000000.0;
        double s = seconds % 60;
        double totalminutes = seconds / 60;
        double m = totalminutes % 60;
        double totalhours = totalminutes / 60;
        double h = totalhours % 60;
        return (int) Math.floor(h) + "h " + (int) Math.floor(m) + "m " + (int) Math.floor(s) + "s";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("ar") || cmd.getName().equalsIgnoreCase("autore")) {
            if(!sender.hasPermission("autore.ar")){
                sender.sendMessage("You don't have permission to do this.");
                return true;
            } else{
                if (args.length == 0) {
                    sender.sendMessage(ChatColor.RED + "[AutoRe] " + ChatColor.WHITE + "AutoRe command(s):");
                    if (sender.hasPermission("autore.admin")) {
                        sender.sendMessage("/ar <hours> <minutes> <seconds> (30 second min. for warning)");
                        sender.sendMessage("/ar cancel");
                        sender.sendMessage("/ar now");
                        sender.sendMessage("/ar reload");
                    }
                    sender.sendMessage("/ar status");
                }
                if(args.length == 1){
                    if (sender.hasPermission("autore.admin")) {
                        if (args[0].equalsIgnoreCase("now")) {
                            scheduleRestart(0, 0, 1);
                            sender.sendMessage(ChatColor.RED + "[AutoRe] " + ChatColor.WHITE + "Impatience accepted -- restarting...");
                            return true;
                        }
                        if (args[0].equalsIgnoreCase("cancel")) {
                            sender.sendMessage(cancelRestart());
                            return true;
                        }
                        if (args[0].equalsIgnoreCase("reload")) {
                            reload();
                            sender.sendMessage(ChatColor.RED + "[AutoRe] " + ChatColor.WHITE + "Config reloaded...");
                            return true;
                        }
                    }
                    if(args[0].equalsIgnoreCase("status")){
                        sender.sendMessage(ChatColor.RED + "[AutoRe] " + ChatColor.WHITE + "Restarting in " + ChatColor.AQUA + remainingTime());
                        return true;
                    }
                }
                if(args.length == 3){
                    if (sender.hasPermission("autore.admin")) {
                        try {
                            scheduleRestart(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                            sender.sendMessage(ChatColor.RED + "[AutoRe] " + ChatColor.WHITE + "Server restarting in " + remainingTime());
                            return true;
                        } catch (NumberFormatException e) {
                            sender.sendMessage(ChatColor.RED + "[AutoRe] " + ChatColor.WHITE + "The time values entered could not be understood.");
                            return false;
                        }
                    }
                }
            }
        }

        return false;
    }


}
