package com.comze_instancelabs.bowbash;

import org.bukkit.plugin.java.JavaPlugin;

import com.comze_instancelabs.minigamesapi.MinigamesAPI;
import com.comze_instancelabs.minigamesapi.config.ShopConfig;

public class IShopConfig extends ShopConfig {

	public IShopConfig(JavaPlugin plugin) {
		super(plugin, false);
		this.getConfig().addDefault("config.shop_items.grenades_1.name", "Grenades 1");
		this.getConfig().addDefault("config.shop_items.grenades_1.enabled", true);
		this.getConfig().addDefault("config.shop_items.grenades_1.uses_items", true);
		this.getConfig().addDefault("config.shop_items.grenades_1.items", "344*1");
		this.getConfig().addDefault("config.shop_items.grenades_1.icon", "344*1");
		this.getConfig().addDefault("config.shop_items.grenades_1.lore", "Persistant grenades boost.");
		this.getConfig().addDefault("config.shop_items.grenades_1.requires_money", true);
		this.getConfig().addDefault("config.shop_items.grenades_1.requires_permission", false);
		this.getConfig().addDefault("config.shop_items.grenades_1.money_amount", 3000);
		this.getConfig().addDefault("config.shop_items.grenades_1.permission_node", MinigamesAPI.getAPI().getPermissionShopPrefix() + ".grenades_1");

		this.getConfig().addDefault("config.shop_items.grenades_2.name", "Grenades 2");
		this.getConfig().addDefault("config.shop_items.grenades_2.enabled", true);
		this.getConfig().addDefault("config.shop_items.grenades_2.uses_items", true);
		this.getConfig().addDefault("config.shop_items.grenades_2.items", "344*2");
		this.getConfig().addDefault("config.shop_items.grenades_2.icon", "344*2");
		this.getConfig().addDefault("config.shop_items.grenades_2.lore", "Persistant grenades boost.");
		this.getConfig().addDefault("config.shop_items.grenades_2.requires_money", true);
		this.getConfig().addDefault("config.shop_items.grenades_2.requires_permission", false);
		this.getConfig().addDefault("config.shop_items.grenades_2.money_amount", 5000);
		this.getConfig().addDefault("config.shop_items.grenades_2.permission_node", MinigamesAPI.getAPI().getPermissionShopPrefix() + ".grenades_2");

		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
	}

}
