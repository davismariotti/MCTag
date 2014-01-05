package info.gomeow.mctag.matches;

import info.gomeow.mctag.MCTag;
import info.gomeow.mctag.Manager;
import info.gomeow.mctag.TagInfo;
import info.gomeow.mctag.util.Equip;
import info.gomeow.mctag.util.FireworkEffectPlayer;
import info.gomeow.mctag.util.GameMode;
import info.gomeow.mctag.util.GameState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
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

public class Normal implements Match {

    private final MCTag plugin;

    private final String name;
    private GameMode mode = GameMode.NORMAL;
    private final ConfigurationSection config;
    private final Map<String, TagInfo> players = new HashMap<String, TagInfo>(); // Name, tags
    private GameState state = GameState.LOBBY;

    private BukkitRunnable startRun;
    private boolean starting = false;

    private BukkitRunnable endRun;
    private BukkitRunnable xpRun;

    private final int minSize = 2;

    private final long matchDuration;

    private String it = "";
    private String lastIt = "";

    private final Random rand = new Random();

    private Location spawn;
    private final Set<Location> signs = new HashSet<Location>();

    private boolean safe = false;

    private boolean safeperiod;
    private boolean allowtagbacks;

    private final ScoreboardManager scoreboardManager;
    private Scoreboard scoreboard;
    private Objective scores;

    public Normal(String n, ConfigurationSection section) {
        plugin = MCTag.instance;
        matchDuration = plugin.getConfig().getLong("match-duration", 60L) * 20L;
        name = n;
        d("Initializing.");
        scoreboardManager = Bukkit.getScoreboardManager();
        config = section;
        spawn = Manager.getLocation(config.getString("spawn"));
        safeperiod = config.getBoolean("safeperiod", true);
        allowtagbacks = config.getBoolean("allowtagbacks", false);
        int debug = 0;
        for (String s : config.getStringList("signs")) {
            debug++;
            signs.add(Manager.getLocation2(s));
        }
        d("Spawn set: " + Manager.locToString(spawn, false));
        d("Safe period: " + safeperiod);
        d("Allowtagbacks: " + allowtagbacks);
        d("Initialized " + debug + " sign(s).");
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
        d("Adding sign.");
        signs.add(l);
        new BukkitRunnable() {

            @Override
            public void run() {
                updateSigns();
            }

        }.runTaskLater(plugin, 2L);
    }

    public void removeSign(Location l) {
        d("Removing sign.");
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

    public void setSpawn(Location location) {
        spawn = location;
    }

    public void setMode(GameMode mode) {
        this.mode = mode;
    }

    public void setAllowtagbacks(boolean bool) {
        allowtagbacks = bool;
    }

    public void setSafeperiod(boolean bool) {
        safeperiod = bool;
    }

    public boolean isSafe() {
        return safe;
    }

    public boolean isAllowtagbacks() {
        return allowtagbacks;
    }

    public String getLastIt() {
        return lastIt;
    }

    public void updateSigns() {
        d("Updating signs");
        int debug = 0;
        for (Sign sign : getSigns()) {
            debug++;
            sign.setLine(0, ChatColor.YELLOW + "[Join]");
            sign.setLine(1, ChatColor.BOLD + name);
            sign.setLine(2, ChatColor.BLACK + "" + players.size() + " players");
            sign.setLine(3, (state == GameState.LOBBY) ? ChatColor.GREEN + "In Lobby" : ChatColor.DARK_RED + "In Game");
            sign.update();
        }
        d(debug + " sign(s) updated.");
    }

    public void addPlayer(Player player) {
        d("Adding player: " + player.getName());
        players.put(player.getName(), new TagInfo());
        broadcast(ChatColor.GOLD + player.getName() + " has joined the match! (" + players.size() + " players in match)");
        if (players.size() >= plugin.getConfig().getInt("minimum-players", 4)) {
            d("Reached minimum players.");
            countdown();
        }
        updateSigns();
    }

    public void removePlayer(Player player) {
        if (player.getName().equalsIgnoreCase(it)) {
            int item = rand.nextInt(players.size());
            int i = 0;
            for (String name : players.keySet()) {
                if (i == item) {
                    it = name;
                }
                i++;
            }
            broadcast(ChatColor.GOLD + it + " is now IT!");
            Equip.equipIt(Bukkit.getPlayerExact(it));
            fireworks(Bukkit.getPlayerExact(it).getLocation());
            lastIt = player.getName();
            broadcast(ChatColor.GOLD + "Current IT has left. New selected IT: " + it);
            d("Selected IT: " + it);
            d("Old IT: " + lastIt);
            setupScoreboard();
        }
        d("Removing player: " + player.getName());
        int temp = (state == GameState.INGAME) ? 0 : 1;
        players.remove(player.getName());
        if (players.size() <= minSize - 1) {
            d("Not enough players to continue.");
            if (startRun != null) {
                startRun.cancel();
            }
            if ((state == GameState.LOBBY && starting) || (state == GameState.INGAME)) {
                broadcast(ChatColor.DARK_RED + "Not enough players to continue!");
                reset(false);
            }
            starting = false;
        }
        if (temp == 0) {
            d("Resetting player: " + player.getName());
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            removePotionEffects(player);
            player.teleport(plugin.getManager().getLobby());
            Manager.loadInventory(player);
            player.setScoreboard(scoreboardManager.getNewScoreboard());
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
        d(tagger.getName() + " has tagged " + tagged.getName());
        broadcast(ChatColor.GOLD + tagged.getName() + " is now IT!");
        d(tagger.getName() + " tagged " + tagged.getName());
        TagInfo info = players.get(tagger.getName());
        info.addTags();
        players.put(tagger.getName(), info);
        info = players.get(tagged.getName());
        info.addTagged();
        players.put(tagged.getName(), info);
        Equip.equipIt(tagged);
        Equip.equipOther(tagger);
        fireworks(tagger.getLocation());
        it = tagged.getName();
        lastIt = tagger.getName();
        d("Selected IT: " + it);
        d("Old IT: " + lastIt);
        setupScoreboard();
    }

    public void broadcast(String message) {
        d("Broadcasting: " + ChatColor.stripColor(message));
        for (Player player : getPlayers()) {
            player.sendMessage(message);
        }
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

    public void countdown() {
        if (!starting) {
            d("Starting countdown.");
            starting = true;
            startRun = new BukkitRunnable() {

                int time = plugin.getConfig().getInt("countdown-time", 20) + 1;

                public void run() {
                    if (starting) {
                        d("Countdown time: " + time);
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
            startRun.runTaskTimer(plugin, 0L, 20L);
        }
    }

    public void startGame() {
        d("Starting game.");
        state = GameState.INGAME;
        updateSigns();
        int item = rand.nextInt(players.size());
        int i = 0;
        for (String name : players.keySet()) {
            if (i == item) {
                it = name;
            }
            i++;
        }
        d("Selected IT: " + it);
        for (Player player : getPlayers()) {
            Manager.saveInventory(player);
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            removePotionEffects(player);
            if (player.getName().equalsIgnoreCase(it)) {
                d("Equipping IT: " + player.getName());
                Equip.equipIt(player);
            } else {
                d("Equipping other: " + player.getName());
                Equip.equipOther(player);
            }
            player.setHealth(player.getMaxHealth());
            player.teleport(spawn);
        }
        fireworks(spawn);
        setupScoreboard();
        if (safeperiod) {
            d("Safe period active");
            safe = true;
            new BukkitRunnable() {
                @Override
                public void run() {
                    d("Safe period no longer active.");
                    broadcast(ChatColor.GOLD + "Safe period no longer active.");
                    safe = false;
                }
            }.runTaskLater(plugin, plugin.getConfig().getLong("safe-period-time", 5L) * 20);
        }

        endRun = new BukkitRunnable() {

            public void run() {
                d("Ending game.");
                reset(false);
            }
        };
        endRun.runTaskLater(plugin, matchDuration);
        xpBar();
    }

    public void xpBar() {
        xpRun = new BukkitRunnable() {

            int secondsPassed = 0;

            @Override
            public void run() {
                secondsPassed++;
                for (Player player : getPlayers()) {
                    player.setLevel((int) (matchDuration / 20) - secondsPassed);
                    player.setExp(((matchDuration - (secondsPassed * 20)) / matchDuration) < 1 ? (float) (matchDuration - (secondsPassed * 20)) / matchDuration : (float) 1);
                }
            }

        };
        xpRun.runTaskTimer(plugin, 0L, 20L);
    }

    public void reset(boolean hard) {
        endRun.cancel();
        xpRun.cancel();
        d("Resetting, Hard: " + hard);
        if (!hard) {
            String winner = "";
            int t = -1;
            for (Map.Entry<String, TagInfo> entry : players.entrySet()) {
                int i = entry.getValue().getTags();
                if (i > t) {
                    winner = entry.getKey();
                    t = i;
                }
            }
            d("Winner: " + winner);
            Bukkit.broadcastMessage(ChatColor.GOLD + winner + " has won on match " + name);
            int wins = plugin.getStats().getInt(winner + ".wins", 0);
            wins++;
            plugin.getStats().set(winner + ".wins", wins);
            for (Map.Entry<String, TagInfo> entry : players.entrySet()) {
                int totalTags = plugin.getStats().getInt(entry.getKey() + ".tags", 0);
                int totalTagged = plugin.getStats().getInt(entry.getKey() + ".tagged", 0);
                totalTags = totalTags + entry.getValue().getTags();
                totalTagged = totalTagged + entry.getValue().getTagged();
                plugin.getStats().set(entry.getKey() + ".tags", totalTags);
                plugin.getStats().set(entry.getKey() + ".tagged", totalTagged);
            }
            plugin.saveStats();
            broadcast(ChatColor.GOLD + "Winner: " + winner + "!");
        }
        starting = false;
        if (state == GameState.INGAME) {
            state = GameState.LOBBY;
            for (Player player : getPlayers()) {
                player.setHealth(player.getMaxHealth());
                player.setFoodLevel(20);
                removePotionEffects(player);
                player.teleport(plugin.getManager().getLobby());
                Manager.loadInventory(player);
                player.setScoreboard(scoreboardManager.getNewScoreboard());
            }
            players.clear();
            it = null;
        }
        updateSigns();
    }

    public void removePotionEffects(Player player) {
        d("Removing potion effects for " + player.getName());
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    public void setupScoreboard() {
        d("Setting up scoreboard.");
        if (state == GameState.INGAME) {
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
                d("Player: " + player.getName() + ", SB color: " + color.name());
                String name = color + player.getName();
                if (name.length() >= 16) {
                    name = name.substring(0, 15);
                }
                Score score = scores.getScore(Bukkit.getOfflinePlayer(name));
                score.setScore(players.get(player.getName()).getTags());
            }
        }
    }

    public void fireworks(Location location) {
        for (int x = 0; x < 5; x++) {
            try {
                FireworkEffectPlayer.playFirework(location, FireworkEffect.builder().with(FireworkEffect.Type.BALL).flicker(false).withColor(Color.RED).trail(false).build());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void d(Object o) { // Debug
        if (plugin.getConfig().getBoolean("debug-mode", false)) {
            plugin.getLogger().info(name + ": " + o.toString());
        }
    }

}