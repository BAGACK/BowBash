package com.comze_instancelabs.bowbash;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.ArenaState;
import com.comze_instancelabs.minigamesapi.ArenaType;
import com.comze_instancelabs.minigamesapi.Classes;
import com.comze_instancelabs.minigamesapi.MinigamesAPI;
import com.comze_instancelabs.minigamesapi.util.ArenaScoreboard;
import com.comze_instancelabs.minigamesapi.util.Util;
import com.comze_instancelabs.minigamesapi.util.Validator;

public class IArena extends Arena {

	public static Main m;

	int blue = 4;
	int red = 4;

	boolean cteam = true;

	public IArena(Main m, String arena_id) {
		super(m, arena_id, ArenaType.REGENERATION);
		this.m = m;
	}

	public boolean addBluePoints() {
		blue++;
		red--;
		if (red < 1) {
			for (String p_ : this.getAllPlayers()) {
				if (m.pteam.containsKey(p_)) {
					if (m.pteam.get(p_).equalsIgnoreCase("red")) {
						MinigamesAPI.getAPI().global_lost.put(p_, this);
					}
				}
			}
			this.stop();
			return true;
		}
		return false;
	}

	public boolean addRedPoints() {
		red++;
		blue--;
		if (blue < 1) {
			for (String p_ : this.getAllPlayers()) {
				if (m.pteam.containsKey(p_)) {
					if (m.pteam.get(p_).equalsIgnoreCase("blue")) {
						MinigamesAPI.getAPI().global_lost.put(p_, this);
					}
				}
			}
			this.stop();
			return true;
		}
		return false;
	}

	@Override
	public void joinPlayerLobby(String playername) {
		super.joinPlayerLobby(playername);
		if (cteam) {
			m.pteam.put(playername, "red");
			cteam = false;
		} else {
			m.pteam.put(playername, "blue");
			cteam = true;
		}

	}

	@Override
	public void spectate(String playername) {

	}

	BukkitTask tt;
	int currentingamecount;

	@Override
	public void start() {
		int t = this.getAllPlayers().size() / 2;
		red = Math.max(2, t);
		blue = Math.max(2, t);
		final IArena a = this;
		// super.start();

		try {
			Bukkit.getScheduler().cancelTask(this.getTaskId());
		} catch (Exception e) {
		}
		currentingamecount = MinigamesAPI.getAPI().pinstances.get(m).getIngameCountdown();
		for (String p_ : a.getArena().getAllPlayers()) {
			Player p = Bukkit.getPlayer(p_);
			p.setWalkSpeed(0.0F);
			if (m.pteam.get(p_).equalsIgnoreCase("red")) {
				Util.teleportPlayerFixed(p, a.getSpawns().get(0));
			} else if (m.pteam.get(p_).equalsIgnoreCase("blue")) {
				Util.teleportPlayerFixed(p, a.getSpawns().get(1));
			}
		}
		MinigamesAPI.getAPI().pinstances.get(m).scoreboardManager.updateScoreboard(a);
		this.setTaskId(Bukkit.getScheduler().runTaskTimer(MinigamesAPI.getAPI(), new Runnable() {
			public void run() {
				currentingamecount--;
				if (currentingamecount == 60 || currentingamecount == 30 || currentingamecount == 15 || currentingamecount == 10 || currentingamecount < 6) {
					for (String p_ : a.getAllPlayers()) {
						if (Validator.isPlayerOnline(p_)) {
							Player p = Bukkit.getPlayer(p_);
							p.sendMessage(MinigamesAPI.getAPI().pinstances.get(m).getMessagesConfig().starting_in.replaceAll("<count>", Integer.toString(currentingamecount)));
						}
					}
				}
				if (currentingamecount < 1) {
					a.getArena().setArenaState(ArenaState.INGAME);
					for (String p_ : a.getAllPlayers()) {
						if (!Classes.hasClass(m, p_)) {
							Classes.setClass(m, "default", p_);
						}
						Classes.getClass(m, p_);
						Player p = Bukkit.getPlayer(p_);
						p.setWalkSpeed(0.2F);
					}
					try {
						Bukkit.getScheduler().cancelTask(a.getTaskId());
					} catch (Exception e) {
					}
				}
			}
		}, 5L, 20).getTaskId());

		m.scoreboard.updateScoreboard(this);
		tt = Bukkit.getScheduler().runTaskTimer(m, new Runnable() {
			public void run() {
				if (a.getArenaState() == ArenaState.INGAME) {
					Bukkit.getScheduler().runTaskLater(m, new Runnable() {
						public void run() {
							for (String p_ : a.getAllPlayers()) {
								Main.addArmor(p_);
							}
						}
					}, 20L);
					tt.cancel();
				}
			}
		}, 20L, 20L);
	}

}
