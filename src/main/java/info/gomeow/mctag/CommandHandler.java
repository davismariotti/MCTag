package info.gomeow.mctag;

import info.gomeow.mctag.util.State;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements CommandExecutor {

    MCTag plugin;

    public CommandHandler(MCTag mct) {
        plugin = mct;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("mctag")) {
            if(args.length == 0) {
                sender.sendMessage(ChatColor.GOLD + " -- MCTag Help -- ");
                sender.sendMessage(ChatColor.GOLD + "/" + label + " join <match> - Joins a match.");
                sender.sendMessage(ChatColor.GOLD + "/leave - Leaves the match");
                sender.sendMessage(ChatColor.GOLD + "/" + label + " setlobby - Sets the lobby");
                sender.sendMessage(ChatColor.GOLD + "/" + label + " create <match> - Creates a match");
                sender.sendMessage(ChatColor.GOLD + "/" + label + " delete <match> - Deletes a match");
            } else {
                if(args[0].equalsIgnoreCase("create")) {
                    if(sender.hasPermission("mctag.admin")) {
                        if(args.length == 2) {
                            String name = args[1].toLowerCase();
                            if(!Manager.mapExists(name)) {
                                plugin.getData().set("maps." + name + ".placeholder", true);
                                plugin.saveData();
                                sender.sendMessage(ChatColor.GREEN + "Match created!");
                            } else {
                                sender.sendMessage(ChatColor.RED + "That match already exists!");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " create <match>");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
                    }
                } else if(args[0].equalsIgnoreCase("delete")) {
                    if(sender.hasPermission("mctag.admin")) {
                        if(args.length == 2) {
                            String name = args[1].toLowerCase();
                            if(Manager.mapExists(name)) {
                                plugin.getData().set("maps." + name, null);
                                plugin.saveData();
                                sender.sendMessage(ChatColor.GREEN + "Match Deleted!");
                            } else {
                                sender.sendMessage(ChatColor.RED + "That match doesn't exist!");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " delete <match>");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
                    }
                } else if(args[0].equalsIgnoreCase("setlobby")) {
                    if(sender.hasPermission("mctag.admin")) {
                        if(sender instanceof Player) {
                            Player player = (Player) sender;
                            Location l = player.getLocation();
                            plugin.getManager().setLobby(l);
                            String loc = l.getWorld().getName() + ";" + l.getX() + ";" + l.getY() + ";" + l.getZ() + ";" + l.getYaw() + ";" + l.getPitch();
                            plugin.getData().set("lobby", loc);
                            plugin.saveData();
                            player.sendMessage(ChatColor.GREEN + "Lobby set!");
                        } else {
                            sender.sendMessage(ChatColor.RED + "You must be a player to do that!");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
                    }
                } else if(args[0].equalsIgnoreCase("join")) {
                    if(sender.hasPermission("mctag.join")) {
                        if(sender instanceof Player) {
                            Player player = (Player) sender;
                            if(args.length == 2) {
                                String name = args[1].toLowerCase();
                                Match match = plugin.getManager().getMatch(name);
                                if(match != null) {
                                    if(match.getState() == State.LOBBY) {
                                        match.addPlayer(player);
                                    } else {
                                        sender.sendMessage(ChatColor.RED + "That match is already in progress!");
                                    }
                                } else {
                                    sender.sendMessage(ChatColor.RED + "That match doesn't exist!");
                                }
                            } else {
                                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " join <match>");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "You must be a player to do that!");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "That is not a valid command!");
                }
            }
        } else {
            if(sender instanceof Player) {
                Player player = (Player) sender;
                Match match = plugin.getManager().getMatch(player);
                if(match != null) {
                    match.removePlayer(player);
                    sender.sendMessage(ChatColor.GOLD + "You have left the match!");
                } else {
                    sender.sendMessage(ChatColor.RED + "You must be in a match to do that!");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You must be a player to do that!");
            }
        }
        return true;
    }
}