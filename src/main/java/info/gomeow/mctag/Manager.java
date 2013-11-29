package info.gomeow.mctag;

import info.gomeow.mctag.util.InventoryData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Manager {

    MCTag plugin;

    Map<String, Match> matches = new HashMap<String, Match>();

    Location lobby;

    static HashMap<String, InventoryData> inventories = new HashMap<String, InventoryData>();

    public Manager(MCTag mct) {
        plugin = mct;
        if(plugin.getData().contains("lobby")) {
            lobby = Manager.getLocation(plugin.getData().getString("lobby"));
        } else {
            lobby = Bukkit.getWorlds().get(0).getSpawnLocation();
        }
        d("Lobby initialized: " + Manager.locToString(lobby, true));
        if (plugin.getData().isConfigurationSection("maps")) {
            for (String key : plugin.getData().getConfigurationSection("maps").getKeys(false)) {
                ConfigurationSection section = plugin.getData().getConfigurationSection("maps." + key);
                if (section.contains("spawn")) {
                    matches.put(key, new Match(key, section));
                    plugin.getLogger().info("Arena found: " + key); // TODO Remove debug
                }
            }
        }
    }

    public Match getMatch(String name) {
        return matches.get(name);
    }

    public Match getMatch(Player player) {
        for (Match match : matches.values()) {
            if (match.containsPlayer(player)) {
                return match;
            }
        }
        return null;
    }

    public Match getMatch(Location sign) {
        for (Match match : matches.values()) {
            if (match.containsSign(sign)) {
                return match;
            }
        }
        return null;
    }

    public Collection<Match> getMatches() {
        return matches.values();
    }

    public void addMatch(Match match) {
        matches.put(match.getName(), match);
    }

    public void setLobby(Location l) {
        lobby = l;
    }

    public Location getLobby() {
        return lobby;
    }

    public static Location getLocation(String loc) {
        String[] split = loc.split(";");
        World world = Bukkit.getWorld(split[0]);
        double x = Double.parseDouble(split[1]);
        double y = Double.parseDouble(split[2]);
        double z = Double.parseDouble(split[3]);
        float yaw = Float.parseFloat(split[4]);
        float pitch = Float.parseFloat(split[5]);
        return new Location(world, x, y, z, yaw, pitch);
    }

    public static Location getLocation2(String loc) {
        String[] split = loc.split(";");
        World world = Bukkit.getWorld(split[0]);
        double x = Double.parseDouble(split[1]);
        double y = Double.parseDouble(split[2]);
        double z = Double.parseDouble(split[3]);
        return new Location(world, x, y, z);
    }

    public static String locToString(Location l, boolean pitchYaw) {
        String loc = l.getWorld().getName() + ";" + l.getX() + ";" + l.getY() + ";" + l.getZ();
        if (pitchYaw) {
            loc = loc + ";" + l.getY() + ";" + l.getPitch();
        }
        return loc;
    }

    public static boolean mapExists(String name) {
        if (MCTag.instance.getData().isConfigurationSection("maps")) {
            return MCTag.instance.getData().getConfigurationSection("maps").contains(name);
        }
        return false;
    }

    public Match signExists(Location l) {
        for (Match match : matches.values()) {
            if (match.containsSign(l)) {
                return match;
            }
        }
        return null;
    }

    public static void saveInventory(Player player) {
        d("Saving inventory: " + player.getName());
        inventories.put(player.getName(), new InventoryData(player.getInventory()));
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[]{});
    }

    public static void loadInventory(Player player) {
        d("Loading inventory: " + player.getName());
        String name = player.getName();
        InventoryData i = inventories.get(name);
        inventories.remove(name);
        player.getInventory().setContents(i.getContents());
        player.getInventory().setArmorContents(i.getArmorContents());
    }

    public static void d(Object o) { // Debug
        if (MCTag.instance.getConfig().getBoolean("debug-mode", false)) {
            MCTag.instance.getLogger().info(o.toString());
        }
    }

}
