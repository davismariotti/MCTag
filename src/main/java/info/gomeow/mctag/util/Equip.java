package info.gomeow.mctag.util;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class Equip {

    public static void equipIt(Player player) {
        ItemStack item = new ItemStack(Material.LEATHER_HELMET);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(Color.RED);
        item.setItemMeta(meta);
        player.getInventory().setHelmet(item.clone());

        item = new ItemStack(Material.LEATHER_CHESTPLATE);
        meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(Color.RED);
        item.setItemMeta(meta);
        player.getInventory().setChestplate(item.clone());

        item = new ItemStack(Material.LEATHER_LEGGINGS);
        meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(Color.RED);
        item.setItemMeta(meta);
        player.getInventory().setLeggings(item.clone());

        item = new ItemStack(Material.LEATHER_BOOTS);
        meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(Color.RED);
        item.setItemMeta(meta);
        player.getInventory().setBoots(item.clone());
    }

    public static void equipOther(Player player) {
        ItemStack item = new ItemStack(Material.LEATHER_HELMET);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(Color.BLUE);
        item.setItemMeta(meta);
        player.getInventory().setHelmet(item.clone());

        item = new ItemStack(Material.LEATHER_CHESTPLATE);
        meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(Color.BLUE);
        item.setItemMeta(meta);
        player.getInventory().setChestplate(item.clone());

        item = new ItemStack(Material.LEATHER_LEGGINGS);
        meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(Color.BLUE);
        item.setItemMeta(meta);
        player.getInventory().setLeggings(item.clone());

        item = new ItemStack(Material.LEATHER_BOOTS);
        meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(Color.BLUE);
        item.setItemMeta(meta);
        player.getInventory().setBoots(item.clone());
    }

}
