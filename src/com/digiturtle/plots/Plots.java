package com.digiturtle.plots;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.digiturtle.plots.PlotRegistry.ChunkInfo;

public class Plots extends JavaPlugin {
	
	private PlotRegistry plotRegistry = new PlotRegistry();
	
	private static final String FILE = "plugins/plots.txt";
	
	public static final String ADMIN_PERMISSION = "plots.admin";
	
	private String readFromFile(String file) {
		if (!new File(file).exists()) {
			return null;
		}
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
			String text = "";
			String line;
			while ((line = reader.readLine()) != null) {
				text += line + "\n";
			}
			return text;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private void writeToFile(String file, String text) {
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false)))) {
			writer.write(text);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onEnable() {
		getServer().getPluginManager().registerEvents(new PlotsListener(this), this);
		plotRegistry.load(readFromFile(FILE));
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (label.equalsIgnoreCase("plots") && sender instanceof Player) {
			Player player = (Player) sender;
			if (args.length == 1 && args[0].equalsIgnoreCase("info")) {
				Chunk chunk = player.getLocation().getChunk();
				ChunkInfo info = plotRegistry.getChunkInfo(chunk);
				if (info != null) {
					player.sendMessage("");
					player.sendMessage(ChatColor.GOLD + "About this Chunk");
					player.sendMessage(ChatColor.YELLOW + "Owners: " + (info.getOwners().size() == 0 ? "none" : plotRegistry.toNamedString(info.getOwners())));
					player.sendMessage(ChatColor.YELLOW + "Others with Access: " + (info.getUsersWithAccess().size() == 0 ? "none" : plotRegistry.toNamedString(info.getUsersWithAccess())));
				} else {
					player.sendMessage(ChatColor.RED + "This chunk is not claimed.");
				}
			}
			else if (args.length == 1 && args[0].equalsIgnoreCase("claim")) {
				if (plotRegistry.isChunkClaimed(player.getLocation().getChunk())) {
					player.sendMessage(ChatColor.YELLOW + "Chunk is already claimed!");
				} else {
					plotRegistry.claimChunk(player.getLocation().getChunk(), player);
					Chunk chunk = player.getLocation().getChunk();
					ArrayList<Material> ignoredTypes = new ArrayList<>(Arrays.asList(Material.AIR, Material.LONG_GRASS, 
							Material.RED_ROSE, Material.YELLOW_FLOWER, Material.SUGAR_CANE_BLOCK, Material.SUGAR_CANE));
					for (int x = 0; x <= 15; x++) {
						for (int z = 0; z <= 15; z++) {
							if (x > 0 && x < 15 && z > 0 && z < 15) {
								// Ignore
							} else {
								int y = 127;
								for (; y > 0; y--) {
									Block block = chunk.getBlock(x, y, z);
									if (!ignoredTypes.contains(block.getType())) {
										break;
									}
								}
								chunk.getBlock(x, y, z).setType(Material.COBBLESTONE);
							}
						}
					}
					player.sendMessage(ChatColor.GREEN + "Chunk claimed.");
				}
			}
			else if (args.length == 1 && args[0].equalsIgnoreCase("unclaim")) {
				if (!plotRegistry.isChunkClaimed(player.getLocation().getChunk())) {
					player.sendMessage(ChatColor.YELLOW + "Chunk is not claimed!");
				} else {
					plotRegistry.unclaimChunk(player.getLocation().getChunk(), player);
					player.sendMessage(ChatColor.GREEN + "Chunk unclaimed.");
				}
			}
			else if (args.length == 2 && args[0].equalsIgnoreCase("add")) {
				if (plotRegistry.isOwner(player.getLocation().getChunk(), player)) {
					Player target =  Bukkit.getPlayer(UUID.fromString(args[1]));
					Chunk chunk = player.getLocation().getChunk();
					if (!plotRegistry.canAccessChunk(chunk, target)) {
						plotRegistry.addAccess(player.getLocation().getChunk(), target);
						player.sendMessage(ChatColor.GREEN + "Access added for " + args[1] + ".");
					} else {
						player.sendMessage(ChatColor.YELLOW + target.getDisplayName() + " already not have access.");
					}
				} else {
					player.sendMessage(ChatColor.RED + "You don't own this chunk!");
				}
			}
			else if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
				if (plotRegistry.isOwner(player.getLocation().getChunk(), player)) {
					Player target =  Bukkit.getPlayer(UUID.fromString(args[1]));
					Chunk chunk = player.getLocation().getChunk();
					if (plotRegistry.canAccessChunk(chunk, target)) {
						plotRegistry.removeAccess(player.getLocation().getChunk(), target);
						player.sendMessage(ChatColor.GREEN + "Access removed for " + args[1]);
					} else {
						player.sendMessage(ChatColor.YELLOW + target.getDisplayName() + " did not have access.");
					}
				} else {
					player.sendMessage(ChatColor.RED + "You don't own this chunk!");
				}
			}
			else {
				player.sendMessage("");
				player.sendMessage(ChatColor.GOLD + "Plot Manager Commands:");
				player.sendMessage(ChatColor.YELLOW + "/plots info: List information about the chunk you are standing in");
				player.sendMessage(ChatColor.YELLOW + "/plots claim: Claim the chunk you are standing in");
				player.sendMessage(ChatColor.YELLOW + "/plots unclaim: Unclaim the chunk you are standing in");
				player.sendMessage(ChatColor.YELLOW + "/plots add <name>: Add access for a user for the chunk you are standing in");
				player.sendMessage(ChatColor.YELLOW + "/plots remove <name>: Remove access for a user for the chunk you are standing in");
			}
			return true;
		}
		return false;
	}
	
	public void onDisable() {
		writeToFile(FILE, plotRegistry.save());
	}
	
	public PlotRegistry getPlotRegistry() {
		return plotRegistry;
	}
	
}
