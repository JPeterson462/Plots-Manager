package com.digiturtle.plots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

public class PlotRegistry {
	
	private HashMap<Integer, ArrayList<ChunkInfo>> plots = new HashMap<>();
	
	public void load(String text) {
		if (text == null) {
			return;
		}
		String[] lines = text.split("\n");
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].length() == 0) {
				continue;
			}
			String[] parts = lines[i].split(";");
			String[] id = parts[0].split(","), owners = parts.length < 2 ? new String[0] : parts[1].split(","), access = parts.length < 3 ? new String[0] : parts[2].split(",");
			ChunkReference reference = new ChunkReference(Integer.parseInt(id[0]), Integer.parseInt(id[1]));
			ArrayList<UUID> ownerList = new ArrayList<>();
			for (int j = 0; j < owners.length; j++) {
				ownerList.add(UUID.fromString(owners[j]));
			}
			ArrayList<UUID> accessList = new ArrayList<>();
			for (int j = 0; j < access.length; j++) {
				accessList.add(UUID.fromString(access[j]));
			}
			int hash = reference.hashCode();
			if (!plots.containsKey(hash)) {
				plots.put(hash, new ArrayList<>());
			}
			ChunkInfo info = new ChunkInfo();
			info.owners = ownerList;
			info.usersWithAccess = accessList;
			info.reference = reference;
			plots.get(hash).add(info);
		}
	}
	
	public String save() {
		String text = "";
		for (Map.Entry<Integer, ArrayList<ChunkInfo>> plotBucket : plots.entrySet()) {
			ArrayList<ChunkInfo> bucket = plotBucket.getValue();
			for (int i = 0; i < bucket.size(); i++) {
				ChunkInfo info = bucket.get(i);
				text += "\n" + info.reference.x + "," + info.reference.z + ";" + toString(info.owners) + ";" + toString(info.usersWithAccess);
			}
		}
		if (text.length() == 0) {
			return "";
		}
		return text.substring(1);
	}
	
	public String toString(ArrayList<UUID> list) {
		String text = "";
		for (int i = 0; i < list.size(); i++) {
			if (i > 0) {
				text += ",";
			}
			text += list.get(i).toString();
		}
		return text;
	}
	
	public String toNamedString(ArrayList<UUID> list) {
		String text = "";
		for (int i = 0; i < list.size(); i++) {
			if (i > 0) {
				text += ",";
			}
			text += Bukkit.getPlayer(list.get(i)).getDisplayName();
		}
		return text;
	}
	
	public boolean canAccessChunk(Chunk chunk, Player player) {
		ChunkInfo info = getChunkInfo(chunk);
		if (info == null) {
			return player.hasPermission(Plots.ADMIN_PERMISSION);
		}
		return info != null && (info.owners.contains(player.getUniqueId()) || info.usersWithAccess.contains(player.getUniqueId()));
	}
	
	public boolean isOwner(Chunk chunk, Player player) {
		ChunkInfo info = getChunkInfo(chunk);
		return info != null && info.owners.contains(player.getUniqueId());
	}
	
	public void addAccess(Chunk chunk, Player player) {
		ChunkInfo info = getChunkInfo(chunk);
		if (info != null) {
			info.usersWithAccess.add(player.getUniqueId());
		}
	}
	
	public void removeAccess(Chunk chunk, Player player) {
		ChunkInfo info = getChunkInfo(chunk);
		if (info != null) {
			info.usersWithAccess.remove(player.getUniqueId());
		}
	}
	
	public boolean isChunkClaimed(Chunk chunk) {
		ChunkInfo info = getChunkInfo(chunk);
		return info != null;
	}
	
	public void claimChunk(Chunk chunk, Player player) {
		ChunkInfo info = getChunkInfo(chunk);
		if (info == null) {
			info = new ChunkInfo();
			info.owners.add(player.getUniqueId());
			info.reference = new ChunkReference(chunk.getX(), chunk.getZ());
			if (plots.containsKey(info.reference.hashCode())) {
				plots.get(info.reference.hashCode()).add(info);
			} else {
				plots.put(info.reference.hashCode(), new ArrayList<ChunkInfo>(Arrays.asList(info)));
			}
		}
	}
	
	public void unclaimChunk(Chunk chunk, Player player) {
		ChunkInfo info = getChunkInfo(chunk);
		if (info != null && info.owners.contains(player.getUniqueId())) {
			plots.get(info.reference.hashCode()).remove(info);
		}
	}
	
	public ChunkInfo getChunkInfo(Chunk chunk) {
		int x = chunk.getX(), z = chunk.getZ();
		int hash = new ChunkReference(x, z).hashCode();
		ArrayList<ChunkInfo> bucket = plots.get(hash);
		if (bucket != null) {
			for (int i = 0; i < bucket.size(); i++) {
				ChunkInfo info = bucket.get(i);
				if (info.reference.x == x && info.reference.z == z) {
					return info;
				}
			}
		}
		return null;
	}
	
	class ChunkInfo {
		
		private ChunkReference reference;
		
		private ArrayList<UUID> usersWithAccess = new ArrayList<>();
		
		private ArrayList<UUID> owners = new ArrayList<>();

		public ChunkReference getReference() {
			return reference;
		}

		public ArrayList<UUID> getUsersWithAccess() {
			return usersWithAccess;
		}

		public ArrayList<UUID> getOwners() {
			return owners;
		}
		
	}
	
	class ChunkReference {
		
		private int x, z;
		
		public ChunkReference(int x, int z) {
			this.x = x;
			this.z = z;
		}
		
		public int hashCode() {
			return x * 1000 + z;
		}

		public int getX() {
			return x;
		}

		public int getZ() {
			return z;
		}
	
	}

}
