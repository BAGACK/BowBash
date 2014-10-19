package com.comze_instancelabs.bowbash;

import org.bukkit.plugin.java.JavaPlugin;

import com.comze_instancelabs.minigamesapi.PluginInstance;

public class Util {

	public static void loadShop(JavaPlugin plugin, PluginInstance pli) {
		pli.setShopConfig(new IShopConfig(plugin));
	}

}
