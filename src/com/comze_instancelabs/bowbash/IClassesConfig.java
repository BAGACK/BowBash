package com.comze_instancelabs.bowbash;

import org.bukkit.plugin.java.JavaPlugin;

import com.comze_instancelabs.minigamesapi.config.ClassesConfig;

public class IClassesConfig extends ClassesConfig {

	public IClassesConfig(JavaPlugin plugin) {
		super(plugin, true);
		this.getConfig().options().header("Used for saving classes. Default class:");
		this.getConfig().addDefault("config.kits.default.name", "Default");
		this.getConfig().addDefault("config.kits.default.items", "261:0#ARROW_INFINITE:1#KNOCKBACK:2*1;280:0#KNOCKBACK:4*1;262:0*");
		this.getConfig().addDefault("config.kits.default.lore", "The Default class.");
		this.getConfig().addDefault("config.kits.default.requires_money", false);
		this.getConfig().addDefault("config.kits.default.requires_permission", false);
		this.getConfig().addDefault("config.kits.default.money_amount", 100);
		this.getConfig().addDefault("config.kits.default.permission_node", "minigames.kits.default");

		this.getConfig().addDefault("config.kits.pro.name", "Pro");
		this.getConfig().addDefault("config.kits.pro.items", "261:0#ARROW_INFINITE:1#KNOCKBACK:3*1;280:0#KNOCKBACK:6*1;262:0*1;344*1");
		this.getConfig().addDefault("config.kits.pro.lore", "The Pro class.");
		this.getConfig().addDefault("config.kits.pro.requires_money", false);
		this.getConfig().addDefault("config.kits.pro.requires_permission", false);
		this.getConfig().addDefault("config.kits.pro.money_amount", 100);
		this.getConfig().addDefault("config.kits.pro.permission_node", "minigames.pro.default");

		this.getConfig().addDefault("config.kits.leet.name", "1337");
		this.getConfig().addDefault("config.kits.leet.items", "261:0#ARROW_INFINITE:1#KNOCKBACK:5*1;280:0#KNOCKBACK:10*1;262:0*1;332*1;344*1");
		this.getConfig().addDefault("config.kits.leet.lore", "The 1337 class.");
		this.getConfig().addDefault("config.kits.leet.requires_money", false);
		this.getConfig().addDefault("config.kits.leet.requires_permission", false);
		this.getConfig().addDefault("config.kits.leet.money_amount", 100);
		this.getConfig().addDefault("config.kits.leet.permission_node", "minigames.leet.default");
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
	}

}
