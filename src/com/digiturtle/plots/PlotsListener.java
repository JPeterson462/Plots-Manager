package com.digiturtle.plots;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlotsListener implements Listener {

	private Plots plots;
	
	public PlotsListener(Plots plots) {
		this.plots = plots;
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (!plots.getPlotRegistry().canAccessChunk(event.getBlock().getChunk(), event.getPlayer()) && !event.getPlayer().hasPermission(Plots.ADMIN_PERMISSION)) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "You cannot break blocks in this area");
		}
	}
	
	@EventHandler
	public void onBlockExplode(BlockExplodeEvent event) {
		if (plots.getPlotRegistry().getChunkInfo(event.getBlock().getChunk()) != null) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (!plots.getPlotRegistry().canAccessChunk(event.getBlock().getChunk(), event.getPlayer()) && !event.getPlayer().hasPermission(Plots.ADMIN_PERMISSION)) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "You cannot place blocks in this area");
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Chunk source = event.getFrom().getChunk();
		Chunk destination = event.getTo().getChunk();
		if (source.getX() != destination.getX() || source.getZ() != destination.getZ()) {
			if (plots.getPlotRegistry().isChunkClaimed(destination)) {
				event.getPlayer().sendMessage(ChatColor.GRAY + "Chunk owned by " + toString(plots.getPlotRegistry().getChunkInfo(destination).getOwners()));
			}
		}
	}
	
	private String toString(ArrayList<UUID> uuid) {
		String output = "";
		for (int i = 0; i < uuid.size(); i++) {
			if (i > 0) {
				output += ", ";
			}
			output += Bukkit.getPlayer(uuid.get(i)).getDisplayName();
		}
		return output;
	}
	
}
