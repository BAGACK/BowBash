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

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.ArenaState;
import com.comze_instancelabs.minigamesapi.Classes;
import com.comze_instancelabs.minigamesapi.MinigamesAPI;
import com.comze_instancelabs.minigamesapi.util.ArenaScoreboard;
import com.comze_instancelabs.minigamesapi.util.Util;
import com.comze_instancelabs.minigamesapi.util.Validator;

public class IArena extends Arena {

	public static Main m;

	int blue = 4;
	int red = 4;

	boolean cred = true;
	
	public IArena(Main m, String arena_id) {
		super(m, arena_id);
		this.m = m;
	}

	public void updateBluePoints(boolean add) {
		if (add) {
			blue++;
		} else {
			blue--;
		}
		if (blue < 1) {
			for (String p_ : this.getAllPlayers()) {
				if (m.pteam.containsKey(p_)) {
					if (m.pteam.get(p_).equalsIgnoreCase("blue")) {
						MinigamesAPI.getAPI().global_lost.put(p_, this);
					}
				}
			}
			this.stop();
		}
	}

	public void updateRedPoints(boolean add) {
		if (add) {
			red++;
		} else {
			red--;
		}
		if (red < 1) {
			for (String p_ : this.getAllPlayers()) {
				if (m.pteam.containsKey(p_)) {
					if (m.pteam.get(p_).equalsIgnoreCase("red")) {
						MinigamesAPI.getAPI().global_lost.put(p_, this);
					}
				}
			}
			this.stop();
		}
	}

	@Override
	public void joinPlayerLobby(String playername) {
		super.joinPlayerLobby(playername);
		if(cred){
			m.pteam.put(playername, "red");
			cred = false;
		}else{
			m.pteam.put(playername, "blue");
			cred = true;
		}
		
	}

	@Override
	public void spectate(String playername){
		
	}
	
	@Override
	public void start() {
		super.start();
		int t = this.getAllPlayers().size() / 2;
		red = t;
		blue = t;
		for (String p_ : this.getAllPlayers()) {
			Player p = Bukkit.getPlayer(p_);

			/*ItemStack bow = new ItemStack(Material.BOW, 1);
			ItemMeta bowm = bow.getItemMeta();
			bowm.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
			bowm.addEnchant(Enchantment.ARROW_KNOCKBACK, 2, true);
			bowm.addEnchant(Enchantment.KNOCKBACK, 2, true);
			bowm.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "BOW");
			bow.setItemMeta(bowm);

			ItemStack stick = new ItemStack(Material.STICK, 1);
			ItemMeta stickm = stick.getItemMeta();
			stickm.addEnchant(Enchantment.KNOCKBACK, 5, true);
			stickm.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "SLAPPER");
			stick.setItemMeta(stickm);

			p.getInventory().addItem(bow);
			p.getInventory().addItem(stick);
			p.getInventory().addItem(new ItemStack(Material.ARROW, 1));
			p.updateInventory();*/
			
			ItemStack lhelmet = new ItemStack(Material.LEATHER_HELMET, 1);
		    LeatherArmorMeta lam = (LeatherArmorMeta)lhelmet.getItemMeta();
		    
		    ItemStack lboots = new ItemStack(Material.LEATHER_BOOTS, 1);
		    LeatherArmorMeta lam1 = (LeatherArmorMeta)lboots.getItemMeta();
		    
		    ItemStack lchestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
		    LeatherArmorMeta lam2 = (LeatherArmorMeta)lchestplate.getItemMeta();
		    
		    ItemStack lleggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
		    LeatherArmorMeta lam3 = (LeatherArmorMeta)lleggings.getItemMeta();

		    if(m.pteam.containsKey(p_)){
		    	Color c = Color.BLACK;
		    	if(m.pteam.get(p_).equalsIgnoreCase("red")){
		    		c = Color.RED;
		    	}else{
		    		c = Color.BLUE;
		    	}
		    	lam3.setColor(c);
			    lam2.setColor(c);
			    lam1.setColor(c);
			    lam.setColor(c);
		    }
		    
		    lhelmet.setItemMeta(lam);
		    lboots.setItemMeta(lam1);
		    lchestplate.setItemMeta(lam2);
		    lleggings.setItemMeta(lam3);
		    
		    p.getInventory().setBoots(lboots);
		    p.getInventory().setHelmet(lhelmet);
		    p.getInventory().setChestplate(lchestplate);
		    p.getInventory().setLeggings(lleggings);
		    p.updateInventory();
		}
	}

}
