package info.gomeow.mctag;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class Listeners implements Listener {

    MCTag plugin;

    public Listeners(MCTag mct) {
        plugin = mct;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if(event.getEntityType() == EntityType.PLAYER && event.getDamager().getType() == EntityType.PLAYER) {
            Player player = (Player) event.getEntity();
            Player damager = (Player) event.getDamager();
            Match match = plugin.getManager().getMatch(player);
            if((match != null) && (match.equals(plugin.getManager().getMatch(damager)))) {
                // TODO Add config option to configure protecting players
                event.setCancelled(true);
                if(match.getIT().equals(damager.getName())) {
                    match.tag(damager, player);
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        Player player = (Player) event.getPlayer();
        if(event.getRightClicked() instanceof Player) {
            Player interacted = (Player) event.getRightClicked();
            Match match = plugin.getManager().getMatch(player);
            Match match2 = plugin.getManager().getMatch(interacted);
            if(match != null && match2 != null) {
                if(match.getName().equals(match2.getName())) {
                    // TODO check if player is tagger
                }
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

}
