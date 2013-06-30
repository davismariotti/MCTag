package info.gomeow.mctag;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

public class GameManager {

    private Map<String, Game> games = new HashMap<String, Game>();

    public GameManager() {

    }

    public Game getGame(String name) {
        return games.get(name);
    }

    public Game getGame(Player player) {
        for(Game match:games.values()) {
            if(match.containsPlayer(player)) {
                return match;
            }
        }
        return null;
    }
    
    public Collection<Game> getGames() {
        return games.values();
    }

    public void addPlayer(String name, Player player) {
        Game match = games.get(name);
        if(match != null) {
            if(match.getState() == GameState.LOBBY) {
                match.addPlayer(player);
            }
        }
    }

}
