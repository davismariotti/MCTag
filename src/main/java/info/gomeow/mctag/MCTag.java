package info.gomeow.mctag;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class MCTag extends JavaPlugin {

    private static MCTag instance;

    private GameManager manager;
    private File dataFile;
    private YamlConfiguration data;

    public void onEnable() {
        instance = this;
        manager = new GameManager();
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

    public GameManager getManager() {
        return manager;
    }

    public static MCTag getInstance() {
        return instance;
    }

}
