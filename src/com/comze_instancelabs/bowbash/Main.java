package com.comze_instancelabs.bowbash;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.ArenaSetup;
import com.comze_instancelabs.minigamesapi.ArenaState;
import com.comze_instancelabs.minigamesapi.MinigamesAPI;
import com.comze_instancelabs.minigamesapi.PluginInstance;
import com.comze_instancelabs.minigamesapi.config.ArenasConfig;
import com.comze_instancelabs.minigamesapi.config.DefaultConfig;
import com.comze_instancelabs.minigamesapi.config.MessagesConfig;
import com.comze_instancelabs.minigamesapi.config.StatsConfig;
import com.comze_instancelabs.minigamesapi.util.Util;
import com.comze_instancelabs.minigamesapi.util.Validator;

public class Main extends JavaPlugin implements Listener {

	// allow selecting team

	MinigamesAPI api = null;
	PluginInstance pli = null;
	static Main m = null;
	IArenaScoreboard scoreboard;
	ICommandHandler cmdhandler = new ICommandHandler();

	public static HashMap<String, String> pteam = new HashMap<String, String>();
	public HashMap<String, Integer> pbrokenblocks = new HashMap<String, Integer>();

	public int u_glass_rad = 2;

	public void onEnable() {
		m = this;
		api = MinigamesAPI.getAPI().setupAPI(this, "bowbash", IArena.class, new ArenasConfig(this), new MessagesConfig(this), new IClassesConfig(this), new StatsConfig(this, false), new DefaultConfig(this, false), false);
		PluginInstance pinstance = api.pinstances.get(this);
		pinstance.addLoadedArenas(loadArenas(this, pinstance.getArenasConfig()));
		Bukkit.getPluginManager().registerEvents(this, this);
		scoreboard = new IArenaScoreboard(pinstance, this);
		pinstance.scoreboardManager = scoreboard;
		pinstance.arenaSetup = new IArenaSetup();
		
		try {
			pinstance.getClass().getMethod("setAchievementGuiEnabled", boolean.class);
			pinstance.setAchievementGuiEnabled(true);
		} catch (NoSuchMethodException e) {
			System.out.println("Update your MinigamesLib to the latest version to use the Achievement Gui.");
		}
		int versionnumber = Integer.parseInt(MinigamesAPI.getAPI().getDescription().getVersion().replaceAll("\\.", ""));
		if (versionnumber > 181) {
			com.comze_instancelabs.bowbash.Util.loadShop(this, pinstance);
		} else {
			System.out.println("Update your MinigamesLib to the latest version to use the Shop.");
		}
		pli = pinstance;

		this.getConfig().addDefault("config.powerup_spawn_percentage", 10);
		this.getConfig().addDefault("config.unlimited_glass_radius", 2);

		this.getConfig().options().copyDefaults(true);
		this.saveConfig();

		u_glass_rad = this.getConfig().getInt("config.unlimited_glass_radius");

		boolean continue_ = false;
		for (Method m : pli.getArenaAchievements().getClass().getMethods()) {
			if (m.getName().equalsIgnoreCase("addDefaultAchievement")) {
				continue_ = true;
			}
		}
		if (continue_) {
			pli.getArenaAchievements().addDefaultAchievement("destroy_hundred_blocks_with_bow", "Destroy 100 blocks with your bow in one game!", 100);
			pli.getArenaAchievements().addDefaultAchievement("destroy_thousand_blocks_with_bow_alltime", "Destroy 1000 blocks with your bow all-time!", 1000);
			pli.getArenaAchievements().addDefaultAchievement("win_game_with_one_life", "Win a game with one life left!", 200);
			pli.getAchievementsConfig().getConfig().options().copyDefaults(true);
			pli.getAchievementsConfig().saveConfig();
		}
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
		ArenaSetup s = MinigamesAPI.getAPI().pinstances.get(m).arenaSetup;
		a.init(Util.getSignLocationFromArena(m, arena), Util.getAllSpawns(m, arena), Util.getMainLobby(m), Util.getComponentForArena(m, arena, "lobby"), s.getPlayerCount(m, arena, true), s.getPlayerCount(m, arena, false), s.getArenaVIP(m, arena));
		return a;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		boolean ret = cmdhandler.handleArgs(this, "mgbowbash", "/" + cmd.getName(), sender, args);
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("setdefaultscore")) {
				if (args.length > 2) {
					if (Validator.isArenaValid(this, args[1]) && Util.isNumeric(args[2])) {
						pli.getArenasConfig().getConfig().set("arenas." + args[1] + ".default_score", Integer.parseInt(args[2]));
						pli.getArenasConfig().saveConfig();
						sender.sendMessage(ChatColor.GREEN + "Successfully set default arena score.");
					} else {
						sender.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.RED + "-" + ChatColor.DARK_GRAY + "]" + ChatColor.GRAY + " Usage: " + cmd.getName() + " " + args[0] + " <arena> <score>");
					}
				} else {
					sender.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.RED + "-" + ChatColor.DARK_GRAY + "]" + ChatColor.GRAY + " Usage: " + cmd.getName() + " " + args[0] + " <arena> <score>");
				}
			}
		}
		return ret;
	}

	public static void addArmor(String p_) {
		Player p = Bukkit.getPlayer(p_);

		ItemStack lhelmet = new ItemStack(Material.LEATHER_HELMET, 1);
		LeatherArmorMeta lam = (LeatherArmorMeta) lhelmet.getItemMeta();

		ItemStack lboots = new ItemStack(Material.LEATHER_BOOTS, 1);
		LeatherArmorMeta lam1 = (LeatherArmorMeta) lboots.getItemMeta();

		ItemStack lchestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
		LeatherArmorMeta lam2 = (LeatherArmorMeta) lchestplate.getItemMeta();

		ItemStack lleggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
		LeatherArmorMeta lam3 = (LeatherArmorMeta) lleggings.getItemMeta();

		if (m.pteam.containsKey(p_)) {
			Color c = Color.BLACK;
			if (m.pteam.get(p_).equalsIgnoreCase("red")) {
				c = Color.RED;
			} else {
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

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMove(PlayerMoveEvent event) {
		final Player p = event.getPlayer();
		if (pli.global_players.containsKey(p.getName())) {
			IArena a = (IArena) pli.global_players.get(p.getName());
			if (a.getArenaState() == ArenaState.INGAME) {
				if (p.getLocation().getY() < 0) {
					// player fell
					if (pteam.containsKey(p.getName())) {
						String team = pteam.get(p.getName());
						if (team.equalsIgnoreCase("red")) {
							if (!a.addBluePoints()) {
								Util.teleportPlayerFixed(p, a.getSpawns().get(0));
								Main.addArmor(p.getName());
							}
						} else {
							if (!a.addRedPoints()) {
								Util.teleportPlayerFixed(p, a.getSpawns().get(1));
								Main.addArmor(p.getName());
							}
						}
						scoreboard.updateScoreboard(a);
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if (pli.global_players.containsKey(p.getName())) {
				IArena a = (IArena) pli.global_players.get(p.getName());
				if (a.getArenaState() == ArenaState.INGAME) {
					p.setHealth(20D);
				}
			}
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if (pli.global_players.containsKey(p.getName())) {
				IArena a = (IArena) pli.global_players.get(p.getName());
				if (a.getArenaState() == ArenaState.INGAME) {
					p.setHealth(20D);
					return;
				}
			}
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player) {
			final Player p = (Player) event.getEntity();
			if (pli.global_players.containsKey(p.getName())) {
				IArena a = (IArena) pli.global_players.get(p.getName());
				if (a.getArenaState() == ArenaState.INGAME) {
					if (event.getDamager() instanceof Player) {
						Player p2 = (Player) event.getDamager();
						if (m.pteam.get(p.getName()).equalsIgnoreCase(m.pteam.get(p2.getName()))) {
							// same team
							event.setCancelled(true);
						}
						p2.setHealth(20D);
					} else if (event.getDamager() instanceof Arrow) {
						Arrow ar = (Arrow) event.getDamager();
						if (ar.getShooter() instanceof Player) {
							Player p2 = (Player) ar.getShooter();
							if (m.pteam.get(p.getName()).equalsIgnoreCase(m.pteam.get(p2.getName()))) {
								// same team
								event.setCancelled(true);
							}
							p2.setHealth(20D);
						}
					}
					Bukkit.getScheduler().runTaskLater(this, new Runnable() {
						public void run() {
							p.setHealth(20D);
						}
					}, 5L);
					return;
				}
			}
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player p = event.getPlayer();
		if (pli.global_players.containsKey(p.getName())) {
			IArena a = (IArena) pli.global_players.get(p.getName());
			if (a.getArenaState() == ArenaState.INGAME) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlayerPicupItem(PlayerPickupItemEvent event) {
		Player p = event.getPlayer();
		if (pli.global_players.containsKey(p.getName())) {
			IArena a = (IArena) pli.global_players.get(p.getName());
			if (a.getArenaState() == ArenaState.INGAME) {
				if (event.getItem().getItemStack().getType() == Material.POTION) {
					p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 200, 1));
					p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
					p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
					event.setCancelled(true);
					event.getItem().remove();
				}
				for (Entity e : p.getNearbyEntities(3D, 3D, 3D)) {
					if (e instanceof Chicken) {
						e.remove();
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlace(BlockPlaceEvent event) {
		final Player p = event.getPlayer();
		if (pli.global_players.containsKey(p.getName())) {
			IArena a = (IArena) pli.global_players.get(p.getName());
			if (a.getArenaState() == ArenaState.INGAME) {
				if (isProtected(a, event.getBlock().getLocation(), 2)) {
					event.setCancelled(true);
					return;
				}
				if (event.getBlock().getType() == Material.STAINED_GLASS) {
					a.getSmartReset().addChanged(event.getBlock(), false);
				}
			}
		}
	}

	@EventHandler
	public void onClick(PlayerInteractEvent event) {
		if (event.hasBlock() && (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			final Player p = event.getPlayer();
			if (pli.global_players.containsKey(p.getName())) {
				IArena a = (IArena) pli.global_players.get(p.getName());
				if (a.getArenaState() == ArenaState.INGAME) {
					if (isProtected(a, event.getClickedBlock().getLocation(), u_glass_rad)) {
						byte data = event.getClickedBlock().getData();
						p.getInventory().addItem(new ItemStack(Material.STAINED_GLASS, 1, data));
						p.updateInventory();
					}
				}
			}
		}
	}

	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		final Player p = event.getPlayer();
		if (pli.global_players.containsKey(p.getName())) {
			IArena a = (IArena) pli.global_players.get(p.getName());
			if (a.getArenaState() == ArenaState.INGAME) {
				if (!isProtected(a, event.getBlock().getLocation(), 2)) {
					a.getSmartReset().addChanged(event.getBlock(), false);
					byte data = event.getBlock().getData();
					p.getInventory().addItem(new ItemStack(Material.STAINED_GLASS, 1, data));
					p.updateInventory();
					event.getBlock().setType(Material.AIR);
					event.setCancelled(true);
				} else {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onHit(ProjectileHitEvent event) {
		if (event.getEntity().getShooter() instanceof Player) {
			Player p = (Player) event.getEntity().getShooter();
			if (pli.global_players.containsKey(p.getName())) {
				IArena a = (IArena) pli.global_players.get(p.getName());
				if (a.getArenaState() == ArenaState.INGAME) {

					BlockIterator bi = new BlockIterator(event.getEntity().getWorld(), event.getEntity().getLocation().toVector(), event.getEntity().getVelocity().normalize(), 0.0D, 4);
					Block hit = null;
					while (bi.hasNext()) {
						hit = bi.next();
						if (hit.getTypeId() != 0) {
							break;
						}
					}
					try {
						if (isProtected(a, hit.getLocation(), 2)) {
							event.getEntity().remove();
							return;
						}
						a.getSmartReset().addChanged(hit, false);
						if (hit.getType() == Material.STAINED_GLASS) {
							hit.setTypeId(0);
						} else if (hit.getType() == Material.STONE) { // stone -> cobblestone
							hit.setTypeId(4);
						} else if (hit.getType() == Material.COBBLESTONE) {
							hit.setTypeId(0);
						} else if (hit.getTypeId() == 98 && hit.getData() != (byte) 2) { // smooth bricks -> cracked bricks
							hit.setTypeIdAndData(98, (byte) 2, true);
						} else if (hit.getTypeId() == 98 && hit.getData() == (byte) 2) {
							hit.setTypeId(0);
						} else if (hit.getTypeId() == 43) { // double stone slabs -> stone slabs
							hit.setTypeId(44);
						} else if (hit.getTypeId() == 44) {
							hit.setTypeId(0);
						} else if (hit.getType() == Material.GRASS) { // grass -> dirt
							hit.setTypeId(3);
						} else if (hit.getTypeId() == 5) { // wood -> wood slabs
							hit.setTypeId(126);
						} else if (hit.getTypeId() == 126) {
							hit.setTypeId(0);
						}
						if (hit.getTypeId() == 0) {
							if (pbrokenblocks.containsKey(p.getName())) {
								pbrokenblocks.put(p.getName(), pbrokenblocks.get(p.getName()) + 1);
							} else {
								pbrokenblocks.put(p.getName(), 1);
							}
							if (pbrokenblocks.get(p.getName()) > 99) {
								pli.getArenaAchievements().setAchievementDone(p.getName(), "destroy_hundred_blocks_with_bow", false);
							}
						}

						event.getEntity().remove();
					} catch (Exception ex) {

					}
				}
			}
		}
	}

	@EventHandler
	public void onEgg(PlayerEggThrowEvent event) {
		if (pli.global_players.containsKey(event.getPlayer().getName())) {
			event.setHatching(false);
		}
	}

	@EventHandler
	public void onProjectileLand(ProjectileHitEvent e) {
		if (e.getEntity().getShooter() instanceof Player) {
			boolean mega = false;
			if (e.getEntity() instanceof Egg) {
				mega = false;
			} else if (e.getEntity() instanceof Snowball) {
				mega = true;
			} else {
				return;
			}

			Player p = (Player) e.getEntity().getShooter();
			if (p == null) {
				return;
			}
			if (MinigamesAPI.getAPI().pinstances.get(m).global_players.containsKey(p.getName())) {
				final IArena a = (IArena) MinigamesAPI.getAPI().pinstances.get(m).global_players.get(p.getName());
				BlockIterator bi = new BlockIterator(e.getEntity().getWorld(), e.getEntity().getLocation().toVector(), e.getEntity().getVelocity().normalize(), 0.0D, 4);
				Block hit = null;
				while (bi.hasNext()) {
					hit = bi.next();
					if (hit.getTypeId() != 0) {
						break;
					}
				}
				try {
					Location l = hit.getLocation();
					l.getWorld().createExplosion(l.getX(), l.getY(), l.getZ(), 2F, false, false);
					if (hit.getType() == Material.STAINED_GLASS) {
						if (mega) {
							for (int x = 1; x <= 5; x++) {
								for (int z = 1; z <= 5; z++) {
									Block b = l.getWorld().getBlockAt(new Location(l.getWorld(), l.getBlockX() + x - 3, l.getBlockY(), l.getBlockZ() + z - 3));
									if (!isProtected(a, b.getLocation(), 2)) {
										a.getSmartReset().addChanged(b, false);
										b.setTypeId(0);
									}
								}
							}
						} else {
							for (int x = 1; x <= 3; x++) {
								for (int z = 1; z <= 3; z++) {
									Block b = l.getWorld().getBlockAt(new Location(l.getWorld(), l.getBlockX() + x - 2, l.getBlockY(), l.getBlockZ() + z - 2));
									if (!isProtected(a, b.getLocation(), 2)) {
										a.getSmartReset().addChanged(b, false);
										b.setTypeId(0);
									}
								}
							}
						}
						a.getSmartReset().addChanged(hit, false);
						hit.setTypeId(0);
					}
				} catch (Exception ex) {

				}
			}

		}
	}

	public boolean isProtected(IArena a, Location l, int radius) {
		for (Location spawn : a.getSpawns()) {
			if (Math.abs(spawn.getBlockX() - l.getBlockX()) < radius && Math.abs(spawn.getBlockZ() - l.getBlockZ()) < radius) {
				return true;
			}
		}
		return false;
	}

}
