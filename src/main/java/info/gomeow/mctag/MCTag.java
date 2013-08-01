package info.gomeow.mctag;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class MCTag extends JavaPlugin {

    static MCTag instance;
    static Logger LOG;

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
        if(!f.exists()) {
            try {
                f.createNewFile();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        dataFile = f;
        data = YamlConfiguration.loadConfiguration(f);
    }

    public YamlConfiguration getData() {
        return data;
    }

    public void saveData() {
        try {
            data.save(dataFile);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public Manager getManager() {
        return manager;
    }

}
