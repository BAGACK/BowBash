package com.comze_instancelabs.bowbash;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.MinigamesAPI;
import com.comze_instancelabs.minigamesapi.util.ArenaScoreboard;

public class IArenaScoreboard extends ArenaScoreboard {

	static Scoreboard board;
	static Objective objective;

	JavaPlugin plugin = null;
	
	public IArenaScoreboard(JavaPlugin plugin){
		this.plugin = plugin;
	}
	
	public void updateScoreboard(final IArena arena) {
		for (String p_ : arena.getAllPlayers()) {
			Player p = Bukkit.getPlayer(p_);
			if (board == null) {
				board = Bukkit.getScoreboardManager().getNewScoreboard();
			}
			if (objective == null) {
				objective = board.registerNewObjective("test", "dummy");
			}

			objective.setDisplaySlot(DisplaySlot.SIDEBAR);

			objective.setDisplayName("[" + arena.getName() + "]");

			board.resetScores(Bukkit.getOfflinePlayer(ChatColor.BLUE + Integer.toString(arena.blue - 1) + ChatColor.GRAY + "  :"));
			board.resetScores(Bukkit.getOfflinePlayer(ChatColor.BLUE + Integer.toString(arena.blue + 1) + ChatColor.GRAY + "  :"));
			objective.getScore(Bukkit.getOfflinePlayer(ChatColor.BLUE + Integer.toString(arena.blue) + ChatColor.GRAY + "  :")).setScore(arena.red);

			p.setScoreboard(board);
		}
	}

	@Override
	public void updateScoreboard(JavaPlugin plugin, final Arena arena) {
		IArena a = (IArena) MinigamesAPI.getAPI().pinstances.get(plugin).getArenaByName(arena.getName());
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
