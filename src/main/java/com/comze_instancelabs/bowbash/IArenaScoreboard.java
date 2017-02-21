package com.comze_instancelabs.bowbash;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.ArenaConfigStrings;
import com.comze_instancelabs.minigamesapi.MinigamesAPI;
import com.comze_instancelabs.minigamesapi.PluginInstance;
import com.comze_instancelabs.minigamesapi.util.ArenaScoreboard;

public class IArenaScoreboard extends ArenaScoreboard {

	HashMap<String, Scoreboard> ascore = new HashMap<String, Scoreboard>();
	HashMap<String, Objective> aobjective = new HashMap<String, Objective>();

	JavaPlugin plugin = null;
	boolean custom = false;

	public IArenaScoreboard(PluginInstance pli, JavaPlugin plugin) {
		super(pli, plugin);
		custom = plugin.getConfig().getBoolean(ArenaConfigStrings.CONFIG_USE_CUSTOM_SCOREBOARD);
		this.plugin = plugin;
	}

	public void updateScoreboard(final IArena arena) {
		if (custom) {
			super.updateScoreboard(plugin, arena);
		}
		for (String p_ : arena.getAllPlayers()) {
			Player p = Bukkit.getPlayer(p_);
			if (!ascore.containsKey(arena.getInternalName())) {
				ascore.put(arena.getInternalName(), Bukkit.getScoreboardManager().getNewScoreboard());
			}
			final Scoreboard scoreboard = ascore.get(arena.getInternalName());
			if (!aobjective.containsKey(arena.getInternalName())) {
				aobjective.put(arena.getInternalName(), scoreboard.registerNewObjective(arena.getInternalName(), "dummy"));
			}

			final Objective objective = aobjective.get(arena.getInternalName());
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);

			objective.setDisplayName(MinigamesAPI.getAPI().pinstances.get(plugin).getMessagesConfig().scoreboard_title.replaceAll("<arena>", arena.getDisplayName()));

			reset(scoreboard, ChatColor.BLUE + Integer.toString(arena.blue - 1) + ChatColor.GRAY + "  :");
			reset(scoreboard, ChatColor.BLUE + Integer.toString(arena.blue + 1) + ChatColor.GRAY + "  :");
			objective.getScore(ChatColor.BLUE + Integer.toString(arena.blue) + ChatColor.GRAY + "  :").setScore(arena.red);

			p.setScoreboard(scoreboard);
		}
	}

	@Override
	public void updateScoreboard(JavaPlugin plugin, final Arena arena) {
		IArena a = (IArena) MinigamesAPI.getAPI().pinstances.get(plugin).getArenaByName(arena.getInternalName());
		this.updateScoreboard(a);
	}

	@Override
	public void removeScoreboard(String arena, Player p) {
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard sc = manager.getNewScoreboard();
		sc.clearSlot(DisplaySlot.SIDEBAR);
		p.setScoreboard(sc);
	}

}
