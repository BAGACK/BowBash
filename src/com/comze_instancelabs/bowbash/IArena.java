package com.comze_instancelabs.bowbash;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.MinigamesAPI;

public class IArena extends Arena {

	public static Main  m;
	
	int blue = 4;
	int red = 4;
	
	public IArena(Main m, String arena_id) {
		super(m, arena_id);
		this.m = m;
	}

	
	public void updateBluePoints(boolean add){
		if(add){
			blue++;
		}else{
			blue--;
		}
		if(blue < 1){
			for(String p_ : this.getAllPlayers()){
				if(m.pteam.containsKey(p_)){
					if(m.pteam.get(p_).equalsIgnoreCase("blue")){
						MinigamesAPI.getAPI().global_lost.put(p_, this);
					}
				}
			}
			this.stop();
		}
	}
	
	public void updateRedPoints(boolean add){
		if(add){
			red++;
		}else{
			red--;
		}
		if(red < 1){
			for(String p_ : this.getAllPlayers()){
				if(m.pteam.containsKey(p_)){
					if(m.pteam.get(p_).equalsIgnoreCase("red")){
						MinigamesAPI.getAPI().global_lost.put(p_, this);
					}
				}
			}
			this.stop();
		}
	}
	
	@Override
	public void joinPlayerLobby(String playername){
		super.joinPlayerLobby(playername);
		// TODO add to team
		
	}
	
	@Override
	public void start(){
		super.start();
		for(String p_ : this.getAllPlayers()){
			Player p = Bukkit.getPlayer(p_);
			
			ItemStack bow = new ItemStack(Material.BOW, 1);
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
			p.updateInventory();
		}
	}
	
}
