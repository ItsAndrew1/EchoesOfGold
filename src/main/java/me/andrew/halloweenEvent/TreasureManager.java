//Developed by _ItsAndrew_
package me.andrew.halloweenEvent;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class TreasureManager{
    TreasureHunt plugin;

    BukkitRunnable spawnChests;
    private final Map<String, BukkitTask> treasureParticleTasks = new HashMap<>();
    public TreasureManager(TreasureHunt plugin){
        this.plugin = plugin;
    }

    //Spawns treasures
    public void spawnTreasures(){
        String valueOfCustomHead = plugin.getConfig().getString("custom-head");
        FileConfiguration treasures = plugin.getTreasures().getConfig();

        //Checks if there are any treasures configured
        if(treasures.getConfigurationSection("treasures") == null || treasures.getConfigurationSection("treasures").getKeys(false).isEmpty()){
            Bukkit.getLogger().warning("No treasures found in treasures.yml");

            return;
        }

        for(String key : treasures.getConfigurationSection("treasures").getKeys(false)){
            String path = "treasures." + key;

            World world = Bukkit.getWorld(treasures.getString(path + ".world"));
            if(world == null){
                Bukkit.getLogger().warning("World not found for treasure: " + key);
                continue;
            }

            double x = treasures.getDouble(path + ".x");
            double y = treasures.getDouble(path + ".y");
            double z = treasures.getDouble(path + ".z");

            Location loc = new Location(world, x, y, z);
            Block block = loc.getBlock();
            block.setType(Material.PLAYER_HEAD, false);

            spawnChests = new BukkitRunnable(){
                @Override
                public void run() {
                    if (!(block.getState() instanceof Skull skull)) return;

                    //Set the treasure rotation if the 'facing' of the treasure isn't null
                    if(treasures.getString(path+".facing") != null){
                        skull.setRotation(BlockFace.valueOf(treasures.getString(path + ".facing").toUpperCase()));
                    }

                    PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                    profile.setProperty(new ProfileProperty("textures", valueOfCustomHead));

                    skull.setPlayerProfile(profile);
                    skull.update(true, false);
                }
            };
            spawnChests.runTaskLater(plugin, 2L);
        }
    }

    //Chest Particles
    public void spawnChestParticles() {
        FileConfiguration treasures = plugin.getTreasures().getConfig();
        FileConfiguration data = plugin.getPlayerData().getConfig();

        if (!treasures.isConfigurationSection("treasures")) return;

        for (String key : treasures.getConfigurationSection("treasures").getKeys(false)) {
            String path = "treasures." + key;
            boolean allFound = true;
            if (data.isConfigurationSection("players")) {
                for (String playerName : data.getConfigurationSection("players").getKeys(false)) {
                    if (!data.getBoolean("player." + playerName + ".found." + key)) {
                        allFound = false;
                        break;
                    }
                }
            }
            if (allFound) continue;
            if (treasureParticleTasks.containsKey(key)) continue;

            double x = treasures.getDouble(path + ".x") + 0.5;
            double y = treasures.getDouble(path + ".y");
            double z = treasures.getDouble(path + ".z") + 0.5;
            Location loc = new Location(Bukkit.getWorld(treasures.getString(path + ".world")), x, y, z);

            BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!data.getBoolean("players." + p.getName() + ".found." + key)) {
                        p.spawnParticle(getParticleFromConfig(), loc, 40, 0.4, 0.5, 0.4, 0);
                    }
                }
            }, 0L, 10L);

            treasureParticleTasks.put(key, task);
        }
    }

    //Removes the treasures
    public void removeTreasures(){
        FileConfiguration treasures = plugin.getTreasures().getConfig();

        if(!treasures.isConfigurationSection("treasures")) return;

        for(String key : treasures.getConfigurationSection("treasures").getKeys(false)){
            String path = "treasures." + key;
            World world = Bukkit.getWorld(treasures.getString(path + ".world"));
            if(world == null) continue;

            double x = treasures.getDouble(path + ".x");
            double y = treasures.getDouble(path + ".y");
            double z = treasures.getDouble(path + ".z");

            Location loc = new Location(world, x, y ,z);
            Block block = loc.getBlock();

            if(block.getType() == Material.PLAYER_HEAD || block.getType() == Material.PLAYER_WALL_HEAD){
                block.setType(Material.AIR);
            }
        }
        Bukkit.getLogger().info("[TH] All treasures have been removed!");
    }

    //Get the top 3 players (used for SCOREBOARD)
    public List<Map.Entry<String, Integer>> getTopPlayers() {
        plugin.getPlayerData().reloadConfig();
        FileConfiguration data = plugin.getPlayerData().getConfig();

        Map<String, Integer> scores = new HashMap<>();

        if (data.isConfigurationSection("players")) {
            for (String playerName : data.getConfigurationSection("players").getKeys(false)) {
                int foundCount = data.getInt("players." + playerName + ".treasures-found", 0);
                scores.put(playerName, foundCount);
            }
        }

        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(scores.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        if (sorted.size() > 3) {
            sorted = sorted.subList(0, 3);
        }

        return sorted;
    }

    public Map<String, BukkitTask> getTreasureParticleTasks(){
        return treasureParticleTasks;
    }

    public void cancelAllTreasureParticles(){
        for(BukkitTask task : treasureParticleTasks.values()){
            if(task != null) task.cancel();
        }
        treasureParticleTasks.clear();
    }

    public Particle getParticleFromConfig(){
        Particle p = Particle.valueOf(plugin.getConfig().getString("particle"));
        return p;
    }
}