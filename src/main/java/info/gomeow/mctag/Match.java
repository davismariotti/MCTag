package info.gomeow.mctag;

import info.gomeow.mctag.util.Equip;
import info.gomeow.mctag.util.GameMode;
import info.gomeow.mctag.util.State;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class Match {

    String name;
    GameMode mode = GameMode.NORMAL;
    ConfigurationSection config;
    Map<String, Integer> players = new HashMap<String, Integer>(); // Name, tags
    State state = State.LOBBY;

    BukkitRunnable startRun;
    boolean starting = false;

    BukkitRunnable endRun;

    int minSize = 2;

    String it = "";
    String lastIt = "";

    Random rand = new Random();

    Location spawn;
    Set<Location> signs = new HashSet<Location>();

    boolean safe = false;

    boolean safeperiod;
    boolean tagbacks;

    final ScoreboardManager scoreboardManager;
    Scoreboard scoreboard;
    Objective scores;

    public Match(String n, ConfigurationSection section) {
        scoreboardManager = Bukkit.getScoreboardManager();
        name = n;
        config = section;
        spawn = Manager.getLocation(config.getString("spawn"));
        safeperiod = config.getBoolean("safeperiod", true);
        safeperiod = config.getBoolean("tagbacks", true);
        updateSigns();
    }

    public String getName() {
        return name;
    }

    public Set<Sign> getSigns() {
        Set<Sign> signBlocks = new HashSet<Sign>();
        for (Location l : signs) {
            if (l.getBlock().getState() instanceof Sign) {
                signBlocks.add((Sign) l.getBlock().getState());
            }
        }
        return signBlocks;
    }

    public void addSign(Location l) {
        signs.add(l);
        updateSigns();
    }

    public void removeSign(Location l) {
        signs.remove(l);
        updateSigns();
    }

    public boolean containsSign(Location l) {
        for (Location s : signs) {
            if (l.getX() == s.getX() && l.getY() == s.getY() && l.getZ() == s.getZ()) {
                return true;
            }
        }
        return false;
    }

    public void updateSigns() {
        for (Sign sign : getSigns()) {
            sign.setLine(0, ChatColor.YELLOW + "[Join]");
            sign.setLine(1, ChatColor.BOLD + name);
            sign.setLine(2, ChatColor.BLACK + "" + players.size() + " players");
            sign.setLine(3, (state == State.LOBBY) ? ChatColor.GREEN + "In Lobby" : ChatColor.DARK_RED + "In Game");
            sign.update();
        }
    }

    public void addPlayer(Player player) {
        players.put(player.getName(), 0);
        broadcast(ChatColor.GOLD + player.getName() + " has joined the match! (" + players.size() + " players in match)");
        if (players.size() >= MCTag.instance.getConfig().getInt("minimum-players", 4)) {
            countdown();
        }
        updateSigns();
    }

    public void removePlayer(Player player) {
        int temp = (state == State.INGAME) ? 0 : 1;
        players.remove(player.getName());
        if (players.size() <= minSize) {
            if (startRun != null) {
                startRun.cancel();
            }
            if ((state == State.LOBBY && starting) || (state == State.INGAME)) {
                broadcast(ChatColor.DARK_RED + "Not enough players to continue!");
            }
            starting = false;
        }
        if (temp == 0) {
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            removePotionEffects(player);
            player.teleport(MCTag.instance.getManager().getLobby());
            Manager.loadInventory(player);
        }
        setupScoreboard();
        updateSigns();
    }

    public Set<Player> getPlayers() {
        Set<Player> plys = new HashSet<Player>();
        for (String name : players.keySet()) {
            plys.add(Bukkit.getPlayerExact(name));
        }
        return plys;
    }

    public String getIT() {
        return it;
    }

    public void tag(Player tagger, Player tagged) {
        int tags = players.get(tagger.getName());
        tags++;
        players.put(tagger.getName(), tags);
        Equip.equipIt(tagged);
        Equip.equipOther(tagger);
        it = tagged.getName();
        lastIt = tagger.getName();
        setupScoreboard();
    }

    public int givePoint(Player player) {
        int current = players.get(player.getName()) + 1;
        players.put(player.getName(), current);
        return current;
    }

    public void broadcast(String message) {
        for (Player player : getPlayers()) {
            player.sendMessage(message);
        }
    }

    public int getSize() {
        return players.size();
    }

    public boolean isFull() {
        return false; // TODO
    }

    public boolean containsPlayer(Player player) {
        return players.containsKey(player.getName());
    }

    public State getState() {
        return state;
    }

    public void countdown() {
        if (!starting) {
            starting = true;
            startRun = new BukkitRunnable() {

                int time = MCTag.instance.getConfig().getInt("countdown-time", 20) + 1;

                public void run() {
                    if (starting) {
                        time--;
                        if (time <= 0) {
                            startGame();
                            cancel();
                        } else if ((time % 5 == 0) || (time <= 5)) {
                            broadcast(ChatColor.AQUA + "Match starting in " + time + " seconds.");
                        }
                    } else {
                        cancel();
                    }
                }
            };
            startRun.runTaskTimer(MCTag.instance, 0L, 20L);
        }
    }

    public void startGame() {
        state = State.INGAME;
        updateSigns();
        int item = rand.nextInt(players.size());
        int i = 0;
        for (String name : players.keySet()) {
            if (i == item) {
                it = name;
            }
            i++;
        }
        for (Player player : getPlayers()) {
            Manager.saveInventory(player);
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            removePotionEffects(player);
            if (player.getName().equalsIgnoreCase(it)) {
                Equip.equipIt(player);
            } else {
                Equip.equipOther(player);
            }
            player.setHealth(player.getMaxHealth());
        }
        setupScoreboard();
        if (safeperiod) {
            safe = true;
            new BukkitRunnable() {
                @Override
                public void run() {
                    safe = false;
                }
            }.runTaskLater(MCTag.instance, MCTag.instance.getConfig().getLong("safe-period-time", 20L) * 20);
        }

        endRun = new BukkitRunnable() {

            public void run() {
                reset(false);
            }
        };
        endRun.runTaskLater(MCTag.instance, MCTag.instance.getConfig().getLong("match-duration", 20L) * 20);
    }

    public void reset(boolean hard) {
        if (!hard) {
            // TODO Stats
        }
        starting = false;
        if (state == State.INGAME) {
            state = State.LOBBY;
            for (Player player : getPlayers()) {
                player.setHealth(player.getMaxHealth());
                player.setFoodLevel(20);
                removePotionEffects(player);
                player.teleport(MCTag.instance.getManager().getLobby());
                Manager.loadInventory(player);
            }
            players.clear();
            it = null;
        }
        updateSigns();
    }

    public void removePotionEffects(Player player) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    public void setupScoreboard() {
        if (state == State.INGAME) {
            scoreboard = scoreboardManager.getNewScoreboard();
            scores = scoreboard.registerNewObjective("Tags", "dummy");
            scores.setDisplaySlot(DisplaySlot.SIDEBAR);
            scores.setDisplayName(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + ChatColor.UNDERLINE + "Tags");
            for (Player player : getPlayers()) {
                player.setScoreboard(scoreboard);
                ChatColor color;
                if (player.getName().equals(it)) {
                    color = ChatColor.RED;
                } else {
                    color = ChatColor.BLUE;
                }
                String name = color + player.getName();
                if (name.length() >= 16) {
                    name = name.substring(0, 15);
                }
                Score score = scores.getScore(Bukkit.getOfflinePlayer(name));
                score.setScore(players.get(player.getName()));
            }
        }
    }

}
