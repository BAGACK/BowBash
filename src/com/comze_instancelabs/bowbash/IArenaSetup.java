package com.comze_instancelabs.bowbash;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.ArenaSetup;
import com.comze_instancelabs.minigamesapi.MinigamesAPI;
import com.comze_instancelabs.minigamesapi.util.Util;
import com.comze_instancelabs.minigamesapi.util.Validator;

public class IArenaSetup extends ArenaSetup {

	@Override
	public Arena saveArena(JavaPlugin plugin, String arenaname) {
		if (!Validator.isArenaValid(plugin, arenaname)) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Arena " + arenaname + " appears to be invalid.");
			return null;
		}
		IArena a = Main.initArena(arenaname);
		MinigamesAPI.getAPI().pinstances.get(plugin).arenaSetup.setArenaVIP(plugin, arenaname, false);
		MinigamesAPI.getAPI().pinstances.get(plugin).addArena(a);
		return a;
	}
	
}
