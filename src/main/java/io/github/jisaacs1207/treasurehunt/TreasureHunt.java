package io.github.jisaacs1207.treasurehunt;

import java.util.Calendar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import net.milkbowl.vault.economy.Economy;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;



public final class TreasureHunt extends JavaPlugin implements Listener{

	public int radius;
	public static Economy econ = null;
	@Override
	public void onEnable() {
		saveDefaultConfig();
		setupEconomy();
		getLogger().info("Yargh, TreasureHunt is enabled.");
		saveDefaultConfig();
		radius = this.getConfig().getInt("radius");
		getServer().getPluginManager().registerEvents(this, this);
		getWorldGuard();
		getLogger().info("WorldGuard hooked.");
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
            	double lootChanceX = Math.random();
            	if(lootChanceX<0.5){
            		reloadConfig();
            		for(Player p : Bukkit.getOnlinePlayers()){
						boolean paid = getConfig().getBoolean("alreadyPaid."+p);
						if(paid) getConfig().set("alreadyPaid."+p, false);
					}
					saveConfig();
            		World world = Bukkit.getServer().getWorld("world");
            		for(Player p : Bukkit.getOnlinePlayers()){
            			p.playSound(p.getLocation(),Sound.SPLASH,10, 0);
            			p.playSound(p.getLocation(),Sound.ZOMBIE_REMEDY,7, 0);
            		}
    				world.getBlockAt(getConfig().getInt("location.x"), getConfig().getInt("location.y"), getConfig().getInt("location.z")).setType(Material.AIR);
    				Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "["+ChatColor.BLUE +"CAPT."+ ChatColor.GOLD +"] " + ChatColor.RED + "Stankbeard: "+ChatColor.YELLOW+"YARGH! TIME TO HUNT TREASURE!");
    				Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "["+ChatColor.BLUE +"CAPT."+ ChatColor.GOLD +"] " + ChatColor.RED + "Stankbeard: "+ChatColor.YELLOW+"I placed a crystal somewhere in the world.");
    				Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "["+ChatColor.BLUE +"CAPT."+ ChatColor.GOLD +"] " + ChatColor.RED + "Stankbeard: "+ChatColor.YELLOW+"Right-Click yer compass to start huntin.");
    				Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "["+ChatColor.BLUE +"CAPT."+ ChatColor.GOLD +"] " + ChatColor.RED + "Stankbeard: "+ChatColor.YELLOW+"Spend yer crystals at me shop at spawn!");
    				int rndRadius = 0;
    				int rndX = 0;
    				int rndZ = 0;
    				int rndY = 0;
    				boolean inRegion = true;
    				double negChanceX=0;
    				double negChanceZ=0;
    				rndRadius = 1 + (int)(Math.random()*radius);
					rndX = 1 + (int)(Math.random()*rndRadius);
					rndZ = (int) Math.sqrt((rndRadius*rndRadius) - (rndX*rndX));
					rndY = 61;
					negChanceX = Math.random();
					negChanceZ = Math.random();
					if(negChanceX<0.5)rndX=rndX-(rndX*2);
					if(negChanceZ<0.5)rndZ=rndZ-(rndZ*2);
    				String block = "STATIONARY_WATER";
    				block = world.getBlockAt(rndX, rndY, rndZ).getType().toString();
    				while(inRegion){
						while(!block.equalsIgnoreCase("AIR")){
							if(block.equalsIgnoreCase("STATIONARY_WATER")){
								rndRadius = 1 + (int)(Math.random()*radius);
								rndX = 1 + (int)(Math.random()*rndRadius);
								rndZ = (int) Math.sqrt((rndRadius*rndRadius) - (rndX*rndX));
								rndY = 61;
								negChanceX = Math.random();
								negChanceZ = Math.random();
								if(negChanceX<0.5)rndX=rndX-(rndX*2);
								if(negChanceZ<0.5)rndZ=rndZ-(rndZ*2);
							}
							rndY = rndY+1;
							block = world.getBlockAt(rndX, rndY, rndZ).getType().toString();
						}
						org.bukkit.Location wgTestBlock = world.getBlockAt(rndX, rndY, rndZ).getLocation();
						RegionManager wgCurrWorldRM = getWorldGuard().getRegionManager(world); // regionManager for current world
						ApplicableRegionSet arSet = wgCurrWorldRM.getApplicableRegions(wgTestBlock);
						if(arSet.size() > 0){
							inRegion=true;
							block = "STATIONARY_WATER";
						}
						else inRegion=false;
					}		
    				getConfig().set("location.x", rndX);
    				getConfig().set("location.y", rndY);
    				getConfig().set("location.z", rndZ);
    				world.getBlockAt(rndX, rndY, rndZ).setTypeId(203);
    				getConfig().set("live", true);
    				saveConfig();
            	}
            }
        }, 6000L, 64800L);
	}
 
	@Override
	public void onDisable() {
		getLogger().info("Yargh, TreasureHunt is disabled.");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (args.length == 0) {
			if (commandLabel.equalsIgnoreCase("th")){
				if(sender.isOp()){
					reloadConfig();
            		for(Player p : Bukkit.getOnlinePlayers()){
						boolean paid = getConfig().getBoolean("alreadyPaid."+p.getName());
						if(paid) getConfig().set("alreadyPaid."+p.getName(), false);
					}
					saveConfig();
					World world = this.getServer().getWorld("world");
	        		for(Player p : Bukkit.getOnlinePlayers()){
	        			p.playSound(p.getLocation(),Sound.SPLASH,10, 0);
	        			p.playSound(p.getLocation(),Sound.ZOMBIE_REMEDY,7, 0);
	        		}
					world.getBlockAt(getConfig().getInt("location.x"), getConfig().getInt("location.y"), getConfig().getInt("location.z")).setType(Material.AIR);
					this.getServer().broadcastMessage(ChatColor.GOLD + "["+ChatColor.BLUE +"CAPT."+ ChatColor.GOLD +"] " + ChatColor.RED + "Stankbeard: "+ChatColor.YELLOW+"YARGH! TIME TO HUNT TREASURE!");
					this.getServer().broadcastMessage(ChatColor.GOLD + "["+ChatColor.BLUE +"CAPT."+ ChatColor.GOLD +"] " + ChatColor.RED + "Stankbeard: "+ChatColor.YELLOW+"I placed a crystal somewhere in the world.");
					this.getServer().broadcastMessage(ChatColor.GOLD + "["+ChatColor.BLUE +"CAPT."+ ChatColor.GOLD +"] " + ChatColor.RED + "Stankbeard: "+ChatColor.YELLOW+"Right-Click yer compass to start huntin.");
					this.getServer().broadcastMessage(ChatColor.GOLD + "["+ChatColor.BLUE +"CAPT."+ ChatColor.GOLD +"] " + ChatColor.RED + "Stankbeard: "+ChatColor.YELLOW+"Spend yer crystals at me shop at spawn!");
					int rndRadius = 0;
					int rndX = 0;
					int rndZ = 0;
					int rndY = 0;
					
					boolean inRegion = true;
					double negChanceX=0;
					double negChanceZ=0;
					rndRadius = 1 + (int)(Math.random()*radius);
					rndX = 1 + (int)(Math.random()*rndRadius);
					rndZ = (int) Math.sqrt((rndRadius*rndRadius) - (rndX*rndX));
					rndY = 61;
					negChanceX = Math.random();
					negChanceZ = Math.random();
					if(negChanceX<0.5)rndX=rndX-(rndX*2);
					if(negChanceZ<0.5)rndZ=rndZ-(rndZ*2);
					String block = "STATIONARY_WATER";
					
					block = world.getBlockAt(rndX, rndY, rndZ).getType().toString();
					while(inRegion){
						while(!block.equalsIgnoreCase("AIR")){
							if(block.equalsIgnoreCase("STATIONARY_WATER")){
								rndRadius = 1 + (int)(Math.random()*radius);
								rndX = 1 + (int)(Math.random()*rndRadius);
								rndZ = (int) Math.sqrt((rndRadius*rndRadius) - (rndX*rndX));
								rndY = 61;
								negChanceX = Math.random();
								negChanceZ = Math.random();
								if(negChanceX<0.5)rndX=rndX-(rndX*2);
								if(negChanceZ<0.5)rndZ=rndZ-(rndZ*2);
							}
							rndY = rndY+1;
							block = world.getBlockAt(rndX, rndY, rndZ).getType().toString();
						}
						org.bukkit.Location wgTestBlock = world.getBlockAt(rndX, rndY, rndZ).getLocation();
						RegionManager wgCurrWorldRM = getWorldGuard().getRegionManager(world); // regionManager for current world
						ApplicableRegionSet arSet = wgCurrWorldRM.getApplicableRegions(wgTestBlock);
						if(arSet.size() > 0){
							inRegion=true;
							block = "STATIONARY_WATER";
						}
						else inRegion=false;
					}	
					getConfig().set("location.x", rndX);
					getConfig().set("location.y", rndY);
					getConfig().set("location.z", rndZ);
					world.getBlockAt(rndX, rndY, rndZ).setTypeId(203);
					getConfig().set("live", true);
					saveConfig();
				}
				else{
					sender.sendMessage("Huh?");
				}
			}
		}
		return false;
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void onPlayerInteract(PlayerInteractEvent event){
		Action pAction = event.getAction();
		if (pAction == Action.RIGHT_CLICK_AIR || pAction == Action.RIGHT_CLICK_BLOCK ) {
			Player player = event.getPlayer();
			int inhand = player.getInventory().getItemInHand().getTypeId();
			reloadConfig();
			boolean live = getConfig().getBoolean("live");		
			if(inhand==345){
				if(live==true){
					if(player.getWorld().getName().equalsIgnoreCase("world")){
						int x = getConfig().getInt("location.x");
						int y = getConfig().getInt("location.y");
						int z = getConfig().getInt("location.z");
						Location loc = Bukkit.getServer().getWorld("world").getBlockAt(x,y,z).getLocation();
						player.setCompassTarget(loc);
						if(player.isOp())player.sendMessage(x + "," + y + "," +z);
						player.sendMessage(ChatColor.YELLOW + "Compass aligned."+ChatColor.GREEN+" Happy hunting!");
						player.playSound(player.getLocation(), Sound.ANVIL_USE, 10, 3);
					}
					else player.sendMessage("You need to be in the main world to find treasure.");
				}
				else player.sendMessage("Sorry, there are no treasures to hunt right now!");
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void onCrystalBreak(BlockBreakEvent event){		
		if(event.getBlock().getTypeId()==203){
			if(getConfig().getBoolean("live")==true)
			{
				this.reloadConfig();
				String playerName = event.getPlayer().getName();
				Player player = event.getPlayer();
				int eventX= event.getBlock().getLocation().getBlockX();
				int eventZ= event.getBlock().getLocation().getBlockZ();
				int eventY= event.getBlock().getLocation().getBlockY();
				
				int x = getConfig().getInt("location.x");
				int y = getConfig().getInt("location.y");
				int z = getConfig().getInt("location.z");
				long timeNow = Calendar.getInstance().getTimeInMillis();
				long lastWin = getConfig().getLong("wonToday."+playerName+".lastWin");
				if((x==eventX)&&(z==eventZ)&&(y==eventY)){
					if(timeNow-lastWin>1728000){
						getConfig().set("wonToday."+playerName+".times", 0);
						saveConfig();
						this.reloadConfig();
					}
					String lastWinner = getConfig().getString("lastWinner");
					boolean paid = getConfig().getBoolean("alreadyPaid."+playerName);
					int timeWonToday = getConfig().getInt("wonToday."+player.getName()+".times");
					if(timeWonToday>2){
						if(!paid){
							Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "["+ChatColor.BLUE +"CAPT."+ ChatColor.GOLD +"] " + ChatColor.RED + "Stankbeard: "+ ChatColor.GREEN+ playerName +ChatColor.YELLOW+" has found me treasure!");
							Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "["+ChatColor.BLUE +"CAPT."+ ChatColor.GOLD +"] " + ChatColor.RED + "Stankbeard: "+ ChatColor.GREEN+"They left it for someone else, though!");
							Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "["+ChatColor.BLUE +"CAPT."+ ChatColor.GOLD +"] " + ChatColor.RED + "Stankbeard: "+ ChatColor.YELLOW+"The hunt is still on!");
							int randomCash = 50 + (int)(Math.random()*300); 
							econ.depositPlayer(playerName, randomCash);
							player.sendMessage("");
							player.sendMessage(ChatColor.GOLD + "["+ChatColor.BLUE +"CAPT."+ ChatColor.GOLD +"] " + ChatColor.RED + "Stankbeard: "+ ChatColor.YELLOW+"You've won 3 times in the last 24h! Here's " + randomCash + " shards.");
							player.sendMessage(ChatColor.GOLD + "["+ChatColor.BLUE +"CAPT."+ ChatColor.GOLD +"] " + ChatColor.RED + "Stankbeard: "+ ChatColor.RED+"Now jog on, sunshine, afore ya get hurt!");
							getConfig().set("alreadyPaid."+playerName, true);
							saveConfig();
							event.setCancelled(true);
						}
						else{
							player.sendMessage(ChatColor.GOLD + "["+ChatColor.BLUE +"CAPT."+ ChatColor.GOLD +"] " + ChatColor.RED + "Stankbeard: "+ ChatColor.RED+"I SAID BUGGER OFF, YA GREEDY TWIZZLER!");
							event.setCancelled(true);
						}
						
					}
					else if(!lastWinner.equalsIgnoreCase(playerName)){
						Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "["+ChatColor.BLUE +"CAPT."+ ChatColor.GOLD +"] " + ChatColor.RED + "Stankbeard: "+ ChatColor.GREEN+ playerName +ChatColor.YELLOW+" has found me treasure!");
						Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "["+ChatColor.BLUE +"CAPT."+ ChatColor.GOLD +"] " + ChatColor.RED + "Stankbeard: "+ ChatColor.YELLOW+"Better luck next time... HAW!");
						getConfig().set("live", false);
						getConfig().set("lastWinner", playerName);
						for(Player p : Bukkit.getOnlinePlayers()){
							paid = getConfig().getBoolean("alreadyPaid."+p.getName());
							if(paid) getConfig().set("alreadyPaid."+p.getName(), false);
						}
						
						getConfig().set("wonToday."+playerName+".times",timeWonToday+1);
						int randomCash = 50 + (int)(Math.random()*250);

						timeWonToday=timeWonToday+1;
						player.sendMessage(ChatColor.GOLD + "["+ChatColor.BLUE +"CAPT."+ ChatColor.GOLD +"] " + ChatColor.RED + "Stankbeard: "+ ChatColor.YELLOW+"You've won "+ timeWonToday + "/3 times today!");
						player.sendMessage(ChatColor.GOLD + "["+ChatColor.BLUE +"CAPT."+ ChatColor.GOLD +"] " + ChatColor.RED + "Stankbeard: "+ ChatColor.YELLOW+"Here's " + randomCash + " shards. Now beat it!");
						econ.depositPlayer(playerName, randomCash);
						
						getConfig().set("wonToday."+player.getName()+".lastWin",Calendar.getInstance().getTimeInMillis());
						saveConfig();
					}
					else{
						if(!paid){
							
							Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "["+ChatColor.BLUE +"CAPT."+ ChatColor.GOLD +"] " + ChatColor.RED + "Stankbeard: "+ ChatColor.GREEN+ playerName +ChatColor.YELLOW+" has found me treasure!");
							Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "["+ChatColor.BLUE +"CAPT."+ ChatColor.GOLD +"] " + ChatColor.RED + "Stankbeard: "+ ChatColor.GREEN+"They left it for someone else, though!");
							Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "["+ChatColor.BLUE +"CAPT."+ ChatColor.GOLD +"] " + ChatColor.RED + "Stankbeard: "+ ChatColor.YELLOW+"The hunt is still on!");
							int randomCash = 50 + (int)(Math.random()*500); 
							econ.depositPlayer(playerName, randomCash);
							player.sendMessage("");
							player.sendMessage(ChatColor.GOLD + "["+ChatColor.BLUE +"CAPT."+ ChatColor.GOLD +"] " + ChatColor.RED + "Stankbeard: "+ ChatColor.YELLOW+"You won last time! Here's " + randomCash + " shards.");
							player.sendMessage(ChatColor.GOLD + "["+ChatColor.BLUE +"CAPT."+ ChatColor.GOLD +"] " + ChatColor.RED + "Stankbeard: "+ ChatColor.RED+"Bugger off.");
							getConfig().set("alreadyPaid."+playerName, true);
							saveConfig();
							event.setCancelled(true);
						}
						else{
							player.sendMessage(ChatColor.GOLD + "["+ChatColor.BLUE +"CAPT."+ ChatColor.GOLD +"] " + ChatColor.RED + "Stankbeard: "+ ChatColor.RED+"I SAID BUGGER OFF, YA GREEDY TWIZZLER!");
							event.setCancelled(true);
						}
					}	
				}
			}
		}
	}
	@EventHandler(priority=EventPriority.LOW)
    public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
        if((player.getInventory().getItemInHand().getTypeId()==25026)||(player.getInventory().getItemInHand().getTypeId()==25028)){
        	
        	int rndX =  -5 + (int)(Math.random()*5);
			int rndZ =  -5 + (int)(Math.random()*5);
			int rndY =  -5 + (int)(Math.random()*5);
			//double negChanceX = Math.random();
			//double negChanceZ = Math.random();
        	player.setVelocity(new Vector(rndX, rndY, rndZ).multiply(1D));
        	player.sendMessage("Holding that has made movement difficult in your armor!");
        	player.sendMessage("Emptying your hands should solve the problem.");
        }
    }
    
	private WorldGuardPlugin getWorldGuard() {
	    Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

	    // WorldGuard may not be loaded
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        //May disable Plugin
	        return null; // Maybe you want throw an exception instead
	    }
	    return (WorldGuardPlugin) plugin;
	}
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
}
