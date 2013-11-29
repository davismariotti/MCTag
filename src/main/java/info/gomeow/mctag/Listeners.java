package info.gomeow.mctag;

import info.gomeow.mctag.util.GameState;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class Listeners implements Listener {

    MCTag plugin;

    public Listeners(MCTag mct) {
        plugin = mct;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntityType() == EntityType.PLAYER && event.getDamager().getType() == EntityType.PLAYER) {
            Player player = (Player) event.getEntity(); // tagged
            Player damager = (Player) event.getDamager(); // tagger
            tag(damager, player, event);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        Player player = (Player) event.getPlayer(); // tagger
        if (event.getRightClicked().getType() == EntityType.PLAYER) {
            Player interacted = (Player) event.getRightClicked(); // tagged
            tag(player, interacted, event);
        }
    }

    public void tag(Player tagger, Player tagged, Cancellable event) {
        Match match = plugin.getManager().getMatch(tagger);
        if ((match != null) && (match.equals(plugin.getManager().getMatch(tagged)))) {
            event.setCancelled(true);
            if (!match.safe) {
                if (!match.tagbacks && tagged.getName().equalsIgnoreCase(match.lastIt)) {
                    if (match.getIT().equals(tagger.getName())) {
                        match.tag(tagger, tagged);
                    }
                } else {
                    tagger.sendMessage(ChatColor.RED + "That player was just IT, no tagbacks!");
                }
            } else {
                tagger.sendMessage(ChatColor.RED + "Please wait until the safe period is over.");
            }
        }
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (plugin.getConfig().getBoolean("block-commands", true)) {
            Player player = event.getPlayer();
            Match match = plugin.getManager().getMatch(player);
            if (match != null) {
                if (match.state == GameState.INGAME) {
                    if (!player.hasPermission("mctag.bypass")) {
                        if (!event.getMessage().toLowerCase().startsWith("/leave")) {
                            event.setCancelled(true);
                            player.sendMessage(ChatColor.DARK_RED + "You cannot use commands ingame. Please use /leave if you need to leave.");
                            d("Blocked command by " + player.getName() + " (" + event.getMessage() + ")");
                        }
                    }
                }
            }
        }
    }


    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (event.getLine(0).equals("[Join]")) {
            String name = event.getLine(1).toLowerCase();
            if (Manager.mapExists(name)) {
                Location location = event.getBlock().getLocation();
                String loc = Manager.locToString(location, false);
                List<String> locs = plugin.getData().getStringList("maps." + name + ".signs");
                locs.add(loc);
                plugin.getData().set("maps." + name + ".signs", locs);
                plugin.saveData();
                Match match = plugin.getManager().getMatch(name);
                if (match != null) {
                    match.addSign(location);
                }
            } else {
                event.getPlayer().sendMessage(ChatColor.RED + "That match does not exist.");
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block.getState() instanceof Sign) {
                Sign sign = (Sign) block.getState();
                String[] lines = sign.getLines();
                if (ChatColor.stripColor(lines[0]).equalsIgnoreCase("[join]")) {
                    if (!lines[1].equalsIgnoreCase("")) {
                        Match match = plugin.getManager().signExists(block.getLocation());
                        if (match != null) {
                            event.setCancelled(true);
                            if (!match.isFull()) {
                                if (match.getState() == GameState.LOBBY) {
                                    if (plugin.getManager().getMatch(player) != null && plugin.getManager().getMatch(player) != match) {
                                        plugin.getManager().getMatch(player).removePlayer(player);
                                    }
                                    match.addPlayer(player);
                                } else {
                                    player.sendMessage(ChatColor.RED + "That match is already is progress!");
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "That match is full!");
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        removeSign(event.getBlock());
    }

    @EventHandler
    public void onPhysics(BlockPhysicsEvent event) {
        removeSign(event.getBlock());
    }

    public void removeSign(Block block) {
        Location location = block.getLocation();
        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("[Join]")) {
                String name = ChatColor.stripColor(sign.getLine(1)).toLowerCase();
                if (!name.equalsIgnoreCase("")) {
                    if (Manager.mapExists(name)) {
                        String loc = Manager.locToString(location, false);
                        List<String> locs = plugin.getData().getStringList("maps." + name + ".signs");
                        locs.remove(loc);
                        plugin.getData().set("maps." + name + ".signs", locs);
                        plugin.saveData();
                        Match match = plugin.getManager().getMatch(name);
                        if (match != null) {
                            match.removeSign(location);
                        }
                    }
                }
            }
        }
    }

    public static void d(Object o) { // Debug
        if (MCTag.instance.getConfig().getBoolean("debug-mode", false)) {
            MCTag.instance.getLogger().info(o.toString());
        }
    }
}
