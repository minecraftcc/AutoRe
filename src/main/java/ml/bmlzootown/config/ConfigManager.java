package ml.bmlzootown.config;

import ml.bmlzootown.AutoRe;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;

public class ConfigManager {
    static Plugin pl = AutoRe.getP();

    public static void setConfigDefaults() {
        FileConfiguration config = pl.getConfig();
        config.options().header("warning-times are set in seconds before restart");
        List<Integer> times = Arrays.asList(1800, 600, 300, 60, 30);
        config.addDefault("enabled", true);
        config.addDefault("warning-times", times);
        config.addDefault("interval.h", 4);
        config.addDefault("interval.m", 0);
        config.addDefault("interval.s", 0);
        config.addDefault("restart-command", "stop");
        config.options().copyDefaults(true);
        pl.saveConfig();
    }

    public static boolean isEnabled() { return pl.getConfig().getBoolean("enabled"); }

    public static List<Integer> getWarningTimes() { return pl.getConfig().getIntegerList("warning-times"); }

    public static Long getH() { return pl.getConfig().getLong("interval.h"); }

    public static Long getM() { return pl.getConfig().getLong("interval.m"); }

    public static Long getS() { return pl.getConfig().getLong("interval.s"); }

    public static String getCommand() { return pl.getConfig().getString("restart-command"); }
}
