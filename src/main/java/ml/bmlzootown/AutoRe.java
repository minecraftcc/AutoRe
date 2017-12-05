package ml.bmlzootown;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class AutoRe extends JavaPlugin {
    private static final int TPS = 20;

    private BukkitTask task_warnthirty = null;
    private BukkitTask task_warnone = null;
    private BukkitTask task_warnfive = null;
    private BukkitTask task_warnten = null;
    private BukkitTask task_restart = null;

    private long current_h=0;
    private long current_m=0;
    private long current_s=0;
    private boolean enabled = false;
    private static String command = "stop";

    private static long startTime;
    private static long stopTime;

    private String retlog(String s){
        getLogger().info(s);
        return s;
    }

    public static String getCmd() {
        return command;
    }

    public static String remainingTime() {
        double seconds = (stopTime - System.nanoTime()) / 1000000000.0;
        double s = seconds % 60;
        double totalminutes = seconds / 60;
        double m = totalminutes % 60;
        double totalhours = totalminutes /60;
        double h = totalhours % 60;
        return (int)Math.floor(h) + "h " + (int)Math.floor(m) + "m " + (int)Math.floor(s) + "s";
    }

    private long timeToTicks(long h,long m, long s){
        s+=60*(m + 60*h);
        return s*TPS;
    }

    private void loadConfig(){
        saveDefaultConfig();
        reloadConfig();
        enabled = this.getConfig().getBoolean("interval.enabled");
        current_h = this.getConfig().getLong("interval.time.h");
        current_m = this.getConfig().getLong("interval.time.m");
        current_s = this.getConfig().getLong("interval.time.s");
        command = this.getConfig().getString("restart-command");
    }

    private String reload() {
        cancelRestart();
        loadConfig();
        if (enabled) {
            scheduleRestart(current_h,current_m,current_s);
        }
        return retlog(ChatColor.RED + "[AutoRe] " + ChatColor.WHITE + "Reloaded config.");
    }

    private String cancelRestart(){
        enabled=false;
        if(task_warnthirty == null) {
            return retlog(ChatColor.RED + "[AutoRe] " + ChatColor.WHITE + "No restart scheduled.");
        }
        getLogger().info(ChatColor.RED + "[AutoRe] " + ChatColor.WHITE + "Cancelling restart.");
        if(task_warnthirty != null) {
            task_warnthirty.cancel();
        }
        if(task_warnone != null) {
            task_warnone.cancel();
        }
        if(task_warnfive != null) {
            task_warnfive.cancel();
        }
        if(task_warnten != null) {
            task_warnten.cancel();
        }
        if(task_restart != null) {
            task_restart.cancel();
        }
        current_s = 0;
        current_m = 0;
        current_h = 0;
        task_warnthirty = null;
        task_restart = null;
        return retlog(ChatColor.RED + "[AutoRe] " + ChatColor.WHITE + "Restart cancelled.");

    }

    private String scheduleRestart(long h, long m, long s){
        cancelRestart();
        enabled = true;
        current_h = h;
        current_m = m;
        current_s = s;
        startTime = System.nanoTime();
        long seconds = ((h*60)*60) + (m*60) + s;
        stopTime = (seconds * 1000000000) + startTime;

        if (seconds > 0) {
            task_restart = new Restart(this).runTaskLater(this, timeToTicks(0,0, seconds));
            if (seconds >= 30) {
                task_warnthirty = new Warn(this).runTaskLater(this, timeToTicks(0, 0, (seconds - 30)));
                if (seconds >= 60) {
                    task_warnone = new WarnOne(this).runTaskLater(this, timeToTicks(0,0,(seconds-60)));
                    if (seconds >= 300) {
                        task_warnfive = new WarnFive(this).runTaskLater(this, timeToTicks(0,0,(seconds-300)));
                        if (seconds >= 600 ) {
                            task_warnten = new WarnTen(this).runTaskLater(this, timeToTicks(0,0,(seconds-600)));
                        }
                    }
                }
            }
        }

        return retlog(ChatColor.RED + "[AutoRe] " + ChatColor.WHITE + "Scheduled restart for " + ChatColor.AQUA + (int)current_h + "h " + (int)current_m + "m " + (int)current_s + "s " + ChatColor.WHITE +"from now.");
    }


    @Override
    public void onEnable() {
        reload();
        getLogger().info(ChatColor.RED + "[AutoRe] " + ChatColor.WHITE + "Enabled");
    }

    @Override
    public void onDisable() {
        cancelRestart();
        getLogger().info(ChatColor.RED + "[AutoRe] " + ChatColor.WHITE + "Disabled");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("ar") || cmd.getName().equalsIgnoreCase("autore")) {
            if(!sender.hasPermission("autore.ar")){ sender.sendMessage("You don't have permission to do this."); return true; }
            else{
                if (args.length==0) {
                    sender.sendMessage(ChatColor.RED + "[AutoRe] " + ChatColor.WHITE + "AutoRe commands:");
                    sender.sendMessage("/ar <hours> <minutes> <seconds> (30 second min. for warning)");
                    sender.sendMessage("/ar cancel");
                    sender.sendMessage("/ar now");
                    sender.sendMessage("/ar reload");
                    sender.sendMessage("/ar status");
                }
                if(args.length==1){
                    if(args[0].equalsIgnoreCase("now")){
                        sender.sendMessage(scheduleRestart(0,0,1));
                        return true;
                    }
                    if(args[0].equalsIgnoreCase("cancel")){
                        sender.sendMessage(cancelRestart());
                        return true;
                    }
                    if(args[0].equalsIgnoreCase("reload")){
                        sender.sendMessage(reload());
                        return true;
                    }
                    if(args[0].equalsIgnoreCase("status")){
                        String[] status={"ENABLED","DISABLED"};
                        int i=enabled?0:1;
                        sender.sendMessage(ChatColor.RED + "[AutoRe] " + ChatColor.WHITE + "Restarting in " + ChatColor.AQUA + remainingTime());
                        return true;
                    }
                }
                if(args.length==3){
                    try {
                        sender.sendMessage(scheduleRestart(Integer.parseInt(args[0]),Integer.parseInt(args[1]),Integer.parseInt(args[2])));
                        return true;
                    } catch (NumberFormatException e) {
                        sender.sendMessage(retlog(ChatColor.RED + "[AutoRe] " + ChatColor.WHITE + "The time values entered could not be understood."));
                        return false;
                    }
                }
            }
        }

        return false;
    }






}