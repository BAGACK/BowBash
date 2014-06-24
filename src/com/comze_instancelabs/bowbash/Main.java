package com.comze_instancelabs.bowbash;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.ArenaSetup;
import com.comze_instancelabs.minigamesapi.ArenaState;
import com.comze_instancelabs.minigamesapi.MinigamesAPI;
import com.comze_instancelabs.minigamesapi.PluginInstance;
import com.comze_instancelabs.minigamesapi.config.ArenasConfig;
import com.comze_instancelabs.minigamesapi.util.Util;
import com.comze_instancelabs.minigamesapi.util.Validator;

public class Main extends JavaPlugin implements Listener {

	// glass shot by you w/ a bow gets removed [DONE - TEST]
	// both teams start with amount of players (as points)
	// override scoreboard with points

	MinigamesAPI api = null;
	static Main m = null;

	public static HashMap<String, String> pteam = new HashMap<String, String>();

	public void onEnable() {
		m = this;
		api = MinigamesAPI.getAPI().setupAPI(this, IArena.class);
		PluginInstance pinstance = MinigamesAPI.getAPI().pinstances.get(this);
		pinstance.addLoadedArenas(loadArenas(this, pinstance.getArenasConfig()));
		Bukkit.getPluginManager().registerEvents(this, this);
	}

	public static ArrayList<Arena> loadArenas(JavaPlugin plugin, ArenasConfig cf) {
		ArrayList<Arena> ret = new ArrayList<Arena>();
		FileConfiguration config = cf.getConfig();
		if (!config.isSet("arenas")) {
			return ret;
		}
		for (String arena : config.getConfigurationSection("arenas.").getKeys(false)) {
			if (Validator.isArenaValid(plugin, arena, cf.getConfig())) {
				ret.add(initArena(arena));
			}
		}
		return ret;
	}

	public static IArena initArena(String arena) {
		IArena a = new IArena(m, arena);
		a.init(Util.getSignLocationFromArena(m, arena), Util.getAllSpawns(m, arena), Util.getMainLobby(m), Util.getComponentForArena(m, arena, "lobby"), ArenaSetup.getPlayerCount(m, arena, true), ArenaSetup.getPlayerCount(m, arena, false), ArenaSetup.getArenaVIP(m, arena));
		return a;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return api.getCommandHandler().handleArgs(this, "mgcolormatch", "/" + cmd.getName(), sender, args);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMove(PlayerMoveEvent event) {
		final Player p = event.getPlayer();
		if (MinigamesAPI.getAPI().global_players.containsKey(p.getName())) {
			IArena a = (IArena) MinigamesAPI.getAPI().global_players.get(p.getName());
			if (a.getArenaState() == ArenaState.INGAME) {
				if (p.getLocation().getY() < 0) {
					// player fell
					if (pteam.containsKey(p.getName())) {
						String team = pteam.get(p.getName());
						if (team.equalsIgnoreCase("red")) {
							a.updateBluePoints(true);
							a.updateRedPoints(false);
						} else {
							a.updateBluePoints(false);
							a.updateRedPoints(true);
						}
						// respawn player
						Util.teleportPlayerFixed(p, a.getSpawns().get(0));
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if (MinigamesAPI.getAPI().global_players.containsKey(p.getName())) {
				IArena a = (IArena) MinigamesAPI.getAPI().global_players.get(p.getName());
				if (a.getArenaState() == ArenaState.INGAME) {
					p.setHealth(20D);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player p = event.getPlayer();
		if (MinigamesAPI.getAPI().global_players.containsKey(p.getName())) {
			IArena a = (IArena) MinigamesAPI.getAPI().global_players.get(p.getName());
			if (a.getArenaState() == ArenaState.INGAME) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		final Player p = event.getPlayer();
		if (MinigamesAPI.getAPI().global_players.containsKey(p.getName())) {
			IArena a = (IArena) MinigamesAPI.getAPI().global_players.get(p.getName());
			if (a.getArenaState() == ArenaState.INGAME) {
				if (event.getBlock().getType() == Material.STAINED_GLASS) {
					byte data = event.getBlock().getData();
					p.getInventory().addItem(new ItemStack(Material.STAINED_GLASS, 1, data));
					p.updateInventory();
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onHit(ProjectileHitEvent event) {
		if (event.getEntity().getShooter() instanceof Player) {
			Player p = (Player) event.getEntity().getShooter();
			if (MinigamesAPI.getAPI().global_players.containsKey(p.getName())) {
				IArena a = (IArena) MinigamesAPI.getAPI().global_players.get(p.getName());
				if (a.getArenaState() == ArenaState.INGAME) {
					if(event.getEntity().getLocation().getBlock() != null){
						Block b = event.getEntity().getLocation().getBlock();
						if(b.getType() == Material.STAINED_GLASS){
							b.setType(Material.AIR);
						}
					}
				}
			}
		}
	}

}
