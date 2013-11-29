package info.gomeow.mctag;

import info.gomeow.mctag.util.GameMode;
import info.gomeow.mctag.util.GameState;

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
        if (cmd.getName().equalsIgnoreCase("tag")) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.GOLD + " -- MCTag Help -- ");
                sender.sendMessage(ChatColor.GOLD + "/" + label + " join <match> - Joins a match.");
                sender.sendMessage(ChatColor.GOLD + "/leave - Leaves the match.");
                sender.sendMessage(ChatColor.GOLD + "/" + label + " setlobby - Sets the lobby.");
                sender.sendMessage(ChatColor.GOLD + "/" + label + " create <match> - Creates a match.");
                sender.sendMessage(ChatColor.GOLD + "/" + label + " delete <match> - Deletes a match.");
                sender.sendMessage(ChatColor.GOLD + "/" + label + " set <match> <option> - Sets a match option.");
            } else {
                if (args[0].equalsIgnoreCase("create")) {
                    if (sender.hasPermission("mctag.admin")) {
                        if (args.length == 2) {
                            String name = args[1].toLowerCase();
                            if (!Manager.mapExists(name)) {
                                plugin.getData().set("maps." + name + ".mode", "NORMAL");
                                plugin.saveData();
                                sender.sendMessage(ChatColor.GREEN + "Match created!");
                                d("Match created: " + name);
                            } else {
                                sender.sendMessage(ChatColor.RED + "That match already exists!");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " create <match>");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
                    }
                } else if (args[0].equalsIgnoreCase("delete")) {
                    if (sender.hasPermission("mctag.admin")) {
                        if (args.length == 2) {
                            String name = args[1].toLowerCase();
                            if (Manager.mapExists(name)) {
                                plugin.getData().set("maps." + name, null);
                                plugin.saveData();
                                sender.sendMessage(ChatColor.GREEN + "Match Deleted!");
                                d("Match deleted: " + name);
                            } else {
                                sender.sendMessage(ChatColor.RED + "That match doesn't exist!");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " delete <match>");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
                    }
                } else if (args[0].equalsIgnoreCase("setlobby")) {
                    if (sender.hasPermission("mctag.admin")) {
                        if (sender instanceof Player) {
                            Player player = (Player) sender;
                            Location l = player.getLocation();
                            plugin.getManager().setLobby(l);
                            String loc = l.getWorld().getName() + ";" + l.getX() + ";" + l.getY() + ";" + l.getZ() + ";" + l.getYaw() + ";" + l.getPitch();
                            plugin.getData().set("lobby", loc);
                            plugin.saveData();
                            player.sendMessage(ChatColor.GREEN + "Lobby set!");
                            d("Lobby set: " + loc);
                        } else {
                            sender.sendMessage(ChatColor.RED + "You must be a player to do that!");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
                    }
                } else if (args[0].equalsIgnoreCase("join")) {
                    if (sender.hasPermission("mctag.join")) {
                        if (sender instanceof Player) {
                            Player player = (Player) sender;
                            if (args.length == 2) {
                                String name = args[1].toLowerCase();
                                Match match = plugin.getManager().getMatch(name);
                                if (match != null) {
                                    if (match.getState() == GameState.LOBBY) {
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
                } else if (args[0].equalsIgnoreCase("setspawn")) {
                    if (sender.hasPermission("mctag.admin")) {
                        if (args.length == 2) {
                            if (sender instanceof Player) {
                                Player player = (Player) sender;
                                String name = args[1].toLowerCase();
                                if (Manager.mapExists(name)) {
                                    Match match = plugin.manager.getMatch(name);
                                    plugin.getData().set("maps." + name + ".spawn", Manager.locToString(player.getLocation(), true));
                                    plugin.saveData();
                                    if (match != null) {
                                        match.spawn = player.getLocation();
                                    }
                                    d(name + ": Spawnpoint set: " + Manager.locToString(player.getLocation(), true));
                                } else {
                                    sender.sendMessage(ChatColor.RED + "That match doesn't exist!");
                                }
                            } else {
                                sender.sendMessage(ChatColor.RED + "You must be a player to do that!");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " setspawn <match>");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
                    }
                } else if (args[0].equalsIgnoreCase("set")) {
                    if (sender.hasPermission("mctag.admin")) {
                        if (args.length >= 4) {
                            String name = args[1].toLowerCase();
                            if (Manager.mapExists(name)) {
                                Match match = plugin.manager.getMatch(name);
                                if (args[2].equalsIgnoreCase("mode")) {
                                    GameMode mode = GameMode.valueOf(args[3].toUpperCase());
                                    if (mode != null) {
                                        plugin.getData().set("maps." + name + ".mode", mode.toString());
                                        plugin.saveData();
                                        if (match != null) {
                                            match.mode = mode;
                                        }
                                        d(name + ": Mode set: " + mode.name());
                                    } else {
                                        sender.sendMessage(ChatColor.RED + "Usage: /" + label + " set <match> mode <normal/freeze>");
                                    }
                                } else if (args[2].equalsIgnoreCase("tagbacks")) {
                                    try {
                                        boolean bool = getBoolean(args[3]);
                                        plugin.getData().set("maps." + name + ".tagbacks", bool);
                                        plugin.saveData();
                                        if (match != null) {
                                            match.tagbacks = bool;
                                        }
                                        d(name + ": Tagbacks set: " + bool);
                                    } catch (IllegalArgumentException e) {
                                        sender.sendMessage(ChatColor.RED + "Usage: /" + label + " set <match> tagbacks <true/false>");
                                    }
                                } else if (args[2].equalsIgnoreCase("safeperiod")) {
                                    try {
                                        boolean bool = getBoolean(args[3]);
                                        plugin.getData().set("maps." + name + ".safeperiod", bool);
                                        plugin.saveData();
                                        if (match != null) {
                                            match.safeperiod = bool;
                                        }
                                        d(name + ": Safeperiod set: " + bool);
                                    } catch (IllegalArgumentException e) {
                                        sender.sendMessage(ChatColor.RED + "Usage: /" + label + " set <match> safeperiod <true/false>");
                                    }
                                } else {
                                    sender.sendMessage(ChatColor.RED + "That is not a valid command!");
                                }
                            } else {
                                sender.sendMessage(ChatColor.RED + "That match doesn't exist!");
                            }
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "That is not a valid command!");
                }
            }
        } else {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                Match match = plugin.getManager().getMatch(player);
                if (match != null) {
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

    public boolean getBoolean(String arg) throws IllegalArgumentException {
        if (arg.equalsIgnoreCase("true")) {
            return true;
        } else if (arg.equalsIgnoreCase("false")) {
            return false;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static void d(Object o) { // Debug
        if (MCTag.instance.getConfig().getBoolean("debug-mode", false)) {
            MCTag.instance.getLogger().info(o.toString());
        }
    }
}