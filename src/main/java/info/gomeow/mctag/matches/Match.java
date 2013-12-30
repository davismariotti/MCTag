package info.gomeow.mctag.matches;

import info.gomeow.mctag.util.GameMode;
import info.gomeow.mctag.util.GameState;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public interface Match {

    public String getName();

    public Set<Sign> getSigns();

    public void addSign(Location l);

    public void removeSign(Location l);

    public boolean containsSign(Location l);

    public void setSpawn(Location location);

    public void setMode(GameMode mode);

    public void setTagbacks(boolean bool);

    public void setSafeperiod(boolean bool);

    public boolean isSafe();

    public boolean isTagbacks();

    public String getLastIt();

    public GameState getState();

    public void updateSigns();

    public void addPlayer(Player player);

    public void removePlayer(Player player);

    public Set<Player> getPlayers();

    public String getIT();

    public void tag(Player tagger, Player tagged);

    public int givePoint(Player player);

    public void broadcast(String message);

    public int getSize();

    public boolean containsPlayer(Player player);

    //public void countdown();

    //public void startGame();

    public void reset(boolean hard);

    public void removePotionEffects(Player player);

    public void setupScoreboard();

    public void d(Object o);

}