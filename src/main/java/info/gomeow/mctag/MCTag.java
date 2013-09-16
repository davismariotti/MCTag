package info.gomeow.mctag;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import info.gomeow.mctag.util.Updater;

public class MCTag extends JavaPlugin {

    static MCTag instance;
    static Logger LOG;
    public static boolean UPDATE = false;
    public static String NEWVERSION = "";
    public static String LINK = "";

    Manager manager;
    File dataFile;
    YamlConfiguration data;

    public void onEnable() {
        instance = this;
        LOG = getLogger();
        manager = new Manager(this);
    }

    public void loadData() {
        File f = new File(getDataFolder(), "data.yml");
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataFile = f;
        data = YamlConfiguration.loadConfiguration(f);
        saveDefaultConfig();
        checkUpdate();
    }

    public YamlConfiguration getData() {
        return data;
    }

    public void saveData() {
        try {
            data.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Manager getManager() {
        return manager;
    }

    /**
     * Checks for available updates.
     */
    public void checkUpdate() {
        new BukkitRunnable() {

            public void run() {
                if (getConfig().getBoolean("check-update")) {
                    if (getConfig().getBoolean("check-update")) {
                        try {
                            Updater u = new Updater(getDescription().getVersion());
                            if (UPDATE = u.getUpdate()) {
                                LINK = u.getLink();
                                NEWVERSION = u.getNewVersion();
                            }
                        } catch (Exception e) {
                            getLogger().log(Level.WARNING, "Failed to check for updates.");
                            getLogger().log(Level.WARNING, "Report this stack trace to gomeow or hawkfalcon.");
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.runTaskAsynchronously(this);
    }

}
