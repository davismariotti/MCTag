package info.gomeow.mctag;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class Listeners implements Listener {

    MCTag plugin;

    public Listeners(MCTag mct) {
        plugin = mct;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if(event.getEntityType() == EntityType.PLAYER) {
            Player player = (Player) event.getEntity();
            Match match = plugin.getManager().getMatch(player);
            if(match != null) {
                // TODO Add config option to configure protecting players
                event.setCancelled(true);
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

}
