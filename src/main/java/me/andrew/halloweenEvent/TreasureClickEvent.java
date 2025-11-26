//Developed by _ItsAndrew_
package me.andrew.halloweenEvent;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class TreasureClickEvent implements Listener {
    TreasureHunt plugin;
    int foundCount;

    public TreasureClickEvent(TreasureHunt plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onTreasureClick(PlayerInteractEvent event){
        FileConfiguration treasures = plugin.getTreasures().getConfig();
        if(event.getHand() != EquipmentSlot.HAND) return;
        if(event.getClickedBlock() == null) return;
        if(event.getClickedBlock().getType() != Material.PLAYER_HEAD) return;

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Location loc = block.getLocation();

        for(String key : treasures.getConfigurationSection("treasures").getKeys(false)){
            String path = "treasures." + key;
            double x = treasures.getDouble(path + ".x");
            double y = treasures.getDouble(path + ".y");
            double z = treasures.getDouble(path + ".z");
            String worldName = treasures.getString(path + ".world");

            if(loc.getWorld().getName().equalsIgnoreCase(worldName) && loc.getBlockX() == x && loc.getBlockY() == y && loc.getBlockZ() == z){
                handleTreasureFound(player, key);
                if(plugin.getPlayerData().getConfig().getBoolean("players." + player.getName() + ".found." + true)) continue;
                plugin.getPlayerData().saveConfig();
                plugin.getPlayerData().reloadConfig();
                plugin.getScoreboardManager().refreshAll();
                event.setCancelled(true);
                return;
            }
        }
    }

    private void handleTreasureFound(Player player, String treasureID){
        FileConfiguration data = plugin.getPlayerData().getConfig();
        String playerPath = "players." + player.getName();

        BukkitTask task = plugin.getTreasureManager().getTreasureParticleTasks().remove(treasureID);
        if(task != null){
            task.cancel();
        }

        boolean alreadyFound = data.getBoolean(playerPath + ".found."+treasureID, false);
        if(alreadyFound){
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("treasure-already-found")));
            return;
        }

        data.set(playerPath + ".found." + treasureID, true);
        foundCount = data.getInt(playerPath +".treasures-found", 0) + 1;
        data.set(playerPath + ".treasures-found", foundCount);
        plugin.getPlayerData().saveConfig();

        if(plugin.getConfig().isConfigurationSection("reward-tiers." + foundCount)){
            List<String> commands = plugin.getConfig().getStringList("reward-tiers."+foundCount+".commands");
            for(String cmd : commands){
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
            }
        }

        if(foundCount == plugin.getTreasures().getConfig().getInt("max-treasures")){
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("player-found-all-treasures")));
            foundCount++;
            return;
        }

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("treasure-found")));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        if(!plugin.eventActive) return;

        plugin.getBossBar().addPlayer(event.getPlayer());
        plugin.getTreasureManager().spawnChestParticles();
    }
}
