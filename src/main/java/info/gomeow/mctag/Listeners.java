package info.gomeow.mctag;

import info.gomeow.mctag.util.State;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;

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
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.isOp() && MCTag.UPDATE) {
            player.sendMessage(ChatColor.GREEN + "Version " + MCTag.NEWVERSION + " of PlayerVaults is up for download!");
            player.sendMessage(ChatColor.GREEN + MCTag.LINK + " to view the changelog and download!");
        }
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (plugin.getConfig().getBoolean("block-commands", true)) {
            Player player = event.getPlayer();
            Match match = plugin.getManager().getMatch(player);
            if (match != null) {
                if (match.state == State.INGAME) {
                    if (!player.hasPermission("mctag.bypass")) {
                        if (!event.getMessage().toLowerCase().startsWith("/leave")) {
                            event.setCancelled(true);
                            player.sendMessage(ChatColor.DARK_RED + "You cannot use commands ingame. Please use /leave if you need to leave.");
                        }
                    }
                }
            }
        }
    }

}
