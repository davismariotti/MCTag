package info.gomeow.mctag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Game {

    private String name;
    private Map<String, Object> players = new HashMap<String, Object>(); // Name, TBD
    private GameState state = GameState.LOBBY;

    public Game(String n) {
        name = n;
    }

    public String getName() {
        return name;
    }

    public void addPlayer(Player player) {
        // TODO Add code
    }

    public void removePlayer(Player player) {
        // TODO Add code
    }

    public Set<Player> getPlayers() {
        Set<Player> plys = new HashSet<Player>();
        for(String name:players.keySet()) {
            plys.add(Bukkit.getPlayerExact(name));
        }
        return plys;
    }

    public int getSize() {
        return players.size();
    }

    public boolean containsPlayer(Player player) {
        return players.containsKey(player.getName());
    }

    public GameState getState() {
        return state;
    }

}
