package com.comze_instancelabs.bowbash;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.ArenaConfigStrings;
import com.comze_instancelabs.minigamesapi.ArenaState;
import com.comze_instancelabs.minigamesapi.ArenaType;
import com.comze_instancelabs.minigamesapi.MinigamesAPI;
import com.comze_instancelabs.minigamesapi.util.Util;

public class IArena extends Arena {

	public Main m;

	int blue = 4;
	int red = 4;

	boolean cteam = true;
	BukkitTask powerup_task;
	public ArrayList<String> team_was_at_one = new ArrayList<String>();

	public IArena(Main m, String arena_id) {
		super(m, arena_id, ArenaType.REGENERATION);
		this.m = m;
	}

	public boolean addBluePoints() {
		blue++;
		red--;
		if (red == 1) {
			team_was_at_one.add("red");
		}
		if (red < 1) {
			boolean b = team_was_at_one.contains("blue");
			for (String p_ : this.getAllPlayers()) {
				if (m.pteam.containsKey(p_)) {
					if (m.pteam.get(p_).equalsIgnoreCase("red")) {
						MinigamesAPI.getAPI().pinstances.get(m).global_lost.put(p_, this);
					} else {
						if (b) {
							MinigamesAPI.getAPI().pinstances.get(m).getArenaAchievements().setAchievementDone(p_, "win_game_with_one_life", false);
						}
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
		if (blue == 1) {
			team_was_at_one.add("blue");
		}
		if (blue < 1) {
			boolean b = team_was_at_one.contains("red");
			for (String p_ : this.getAllPlayers()) {
				if (m.pteam.containsKey(p_)) {
					if (m.pteam.get(p_).equalsIgnoreCase("blue")) {
						MinigamesAPI.getAPI().pinstances.get(m).global_lost.put(p_, this);
					} else {
						if (b) {
							MinigamesAPI.getAPI().pinstances.get(m).getArenaAchievements().setAchievementDone(p_, "", false);
						}
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
	public void spectate(final String playername) {
		this.onEliminated(playername);
	}

	BukkitTask tt;
	int currentingamecount;

	@Override
	public void start(boolean tp) {
		int t = this.getAllPlayers().size() / 2;
		red = Math.max(2, t);
		blue = Math.max(2, t);

		FileConfiguration config = MinigamesAPI.getAPI().pinstances.get(m).getArenasConfig().getConfig();
		if (config.isSet(ArenaConfigStrings.ARENAS_PREFIX + this.getName() + ".default_score")) {
			red = config.getInt(ArenaConfigStrings.ARENAS_PREFIX + this.getName() + ".default_score");
			blue = config.getInt(ArenaConfigStrings.ARENAS_PREFIX + this.getName() + ".default_score");
		}

		final IArena a = this;

		for (String p_ : a.getArena().getAllPlayers()) {
			Player p = Bukkit.getPlayer(p_);
			if (m.pteam.get(p_).equalsIgnoreCase("red")) {
				Util.teleportPlayerFixed(p, a.getSpawns().get(0));
			} else if (m.pteam.get(p_).equalsIgnoreCase("blue")) {
				Util.teleportPlayerFixed(p, a.getSpawns().get(1));
			}
		}

		super.start(false);

		m.scoreboard.updateScoreboard(this);
		tt = Bukkit.getScheduler().runTaskTimer(m, new Runnable() {
			public void run() {
				if (a.getArenaState() == ArenaState.INGAME) {
					Bukkit.getScheduler().runTaskLater(m, new Runnable() {
						public void run() {
							for (String p_ : a.getAllPlayers()) {
								m.addArmor(p_);
							}
						}
					}, 20L);
					tt.cancel();
				}
			}
		}, 20L, 20L);
	}

	int failcount = 0;

	@Override
	public void started() {
		final IArena a = this;
		powerup_task = Bukkit.getScheduler().runTaskTimer(m, new Runnable() {
			public void run() {
				if (Math.random() * 100 <= m.getConfig().getInt("config.powerup_spawn_percentage")) {
					try {
						Player p = Bukkit.getPlayer(a.getAllPlayers().get((int) Math.random() * (a.getAllPlayers().size() - 1)));
						if (p != null) {
							boolean spawn = true;
							if (MinigamesAPI.getAPI().pinstances.get(m).global_lost.containsKey(p.getName())) {
								// player is a spectator, retry
								p = Bukkit.getPlayer(a.getAllPlayers().get((int) Math.random() * (a.getAllPlayers().size() - 1)));
								if (p != null) {
									if (MinigamesAPI.getAPI().pinstances.get(m).global_lost.containsKey(p.getName())) {
										spawn = false;
									}
								} else {
									spawn = false;
								}
							}
							if (spawn) {
								Util.spawnPowerup(m, a, p.getLocation().clone().add(0D, 5D, 0D), getItemStack());
							}
						}
					} catch (Exception e) {
						if (a != null) {
							if (a.getArenaState() != ArenaState.INGAME) {
								if (powerup_task != null) {
									if (MinigamesAPI.debug)
									{
										IArena.this.getPlugin().getLogger().fine("Cancelled powerup task.");
									}
									powerup_task.cancel();
								}
							}
						}
						Bukkit.getLogger().info("Use the latest MinigamesLib version to get powerups.");
						failcount++;
						if (failcount > 2) {
							if (powerup_task != null) {
								if (MinigamesAPI.debug)
								{
									IArena.this.getPlugin().getLogger().fine("Cancelled powerup task.");
								}
								powerup_task.cancel();
							}
						}
					}
				}
			}
		}, 60, 60);
	}

	public ItemStack getItemStack() {
		double i = Math.random() * 100;
		ItemStack ret = new ItemStack(Material.STAINED_GLASS, 32);
		if (i <= 20) { // ~20%
			// get a speed and jump boost
			ret = new ItemStack(Material.POTION);
		} else if (i > 20 && i <= 50) { // ~30%
			// get a bomb
			ret = new ItemStack(Material.EGG);
		} else if (i > 50 && i <= 90) { // ~40%
			// get some stained glass
			ret = new ItemStack(Material.STAINED_GLASS, 32);
		} else if (i > 90) { // ~10%
			// get a very good bow
			ItemStack bow = new ItemStack(Material.BOW);
			ItemMeta im = bow.getItemMeta();
			im.addEnchant(Enchantment.ARROW_KNOCKBACK, 5, true);
			im.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
			bow.setItemMeta(im);
			ret = bow;
		}
		return ret;
	}

	/*
	 * @Override public void reset() { final Arena a = this; Bukkit.getScheduler().runTask(m, new Runnable() { public void run() {
	 * Util.loadArenaFromFileSYNC(m, a); } }); }
	 */

	@Override
	public void leavePlayer(String p_, boolean arg1, boolean arg2) {
		if (m.pbrokenblocks.containsKey(p_)) {
			int temp = 0;
			if (m.getConfig().isSet("tempblocks." + p_)) {
				temp = m.getConfig().getInt("tempblocks." + p_);
			}
			temp += m.pbrokenblocks.get(p_);
			m.getConfig().set("tempblocks." + p_, temp);
			m.saveConfig();
			if (temp > 999) {
				MinigamesAPI.getAPI().getPluginInstance(m).getArenaAchievements().setAchievementDone(p_, "destroy_thousand_blocks_with_bow_alltime", false);
			}
			m.pbrokenblocks.remove(p_);
		}
		super.leavePlayer(p_, arg1, arg2);
	}

	@Override
	public void stop() {
		for (String p_ : this.getAllPlayers()) {
			Player p = Bukkit.getPlayer(p_);
			if (p != null) {
				for (Entity e : p.getNearbyEntities(50D, 50D, 50D)) {
					if (e instanceof Chicken) {
						e.remove();
					}
				}
			}
		}
		super.stop();
		if (powerup_task != null) {
			powerup_task.cancel();
		}
	}

}
