//Developed by _ItsAndrew_
package me.andrew.EchoesOfGold;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class TreasureManager{
    EchoesOfGold plugin;

    BukkitRunnable spawnChests;
    private final Map<UUID, Map<String, BukkitTask>> treasureParticleTasks = new HashMap<>();
    public TreasureManager(EchoesOfGold plugin){
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

    //Spawns the particles for the treasures
    public void spawnChestParticles() {
        FileConfiguration treasures = plugin.getTreasures().getConfig();
        FileConfiguration data = plugin.getPlayerData().getConfig();

        //Check if particles are toggled
        boolean toggleChestParticles = plugin.getConfig().getBoolean("treasures-particle.toggle");
        if(!toggleChestParticles) return;

        //Check if there are any treasures configured
        ConfigurationSection treasuresSection = treasures.getConfigurationSection("treasures");
        if (treasuresSection == null || treasuresSection.getKeys(false).isEmpty()) return;

        //Stores the particle tasks in a nested HashMap
        for(Player p : Bukkit.getOnlinePlayers()){

            Map<String, BukkitTask> playerParticleTask = new HashMap<>();

            for (String key : treasures.getConfigurationSection("treasures").getKeys(false)) {
                String path = "treasures." + key;

                //Skips if the player found all treasures
                if(playerFoundAll(p)) continue;
                if (playerParticleTask.containsKey(key)) continue; //Skips if the treasure already has the particles enabled

                double x = treasures.getDouble(path + ".x") + 0.5;
                double y = treasures.getDouble(path + ".y");
                double z = treasures.getDouble(path + ".z") + 0.5;
                Location loc = new Location(Bukkit.getWorld(treasures.getString(path + ".world")), x, y, z);

                BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    if (!data.getBoolean("players." + p.getName() + ".found." + key)) {
                        //Check if the particle is valid or not
                        Particle chestParticle;
                        try {
                            String value = plugin.getConfig().getString("treasures-particle.particle").toUpperCase();
                            chestParticle = getParticleFromConfig(value);

                            //Gets the data for the particle
                            int particleCount = plugin.getConfig().getInt("treasures-particle.count");
                            double particleOffsetX = plugin.getConfig().getDouble("treasures-particle.offsetX");
                            double particleOFFSETY = plugin.getConfig().getDouble("treasures-particle.offsetY");
                            double particleOffsetZ = plugin.getConfig().getDouble("treasures-particle.offsetZ");
                            double particleExtra = plugin.getConfig().getDouble("treasures-particle.extra");

                            p.spawnParticle(chestParticle, loc, particleCount, particleOffsetX, particleOFFSETY, particleOffsetZ, particleExtra);
                        } catch (Exception e) {
                            Bukkit.getLogger().warning("[EchoesOfGold] Value of 'treasures-particle' is INVALID! See message:");
                            Bukkit.getLogger().warning(e.getMessage());
                            return;
                        }
                    }
                },0L, 10L);

                //Attaching the task to the treasure
                playerParticleTask.put(key, task);
            }

            //Attaching the treasure particle tasks to the player
            treasureParticleTasks.put(p.getUniqueId(), playerParticleTask);
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
    }

    //Get the top 3 players (used for SCOREBOARD and FINAL CHAT MESSAGE)
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

    public Map<UUID, Map<String, BukkitTask>> getTreasureParticleTasks(){
        return treasureParticleTasks;
    }

    public void cancelParticles(){
        for(OfflinePlayer p : Bukkit.getOfflinePlayers()){
            //Getting the map attached to the player
            Map<String, BukkitTask> tasks;
            try{
                tasks = treasureParticleTasks.get(p.getUniqueId());

                //Looping through all the  treasures
                ConfigurationSection treasures = plugin.getTreasures().getConfig().getConfigurationSection("treasures");
                for(String treasureId : treasures.getKeys(false)){
                    BukkitTask task = tasks.get(treasureId);
                    if(!task.isCancelled()) task.cancel(); //Cancelling the particle task if they aren't already
                }

                //Clearing the map
                tasks.clear();
            } catch (Exception e){
                continue;
            }
        }
    }

    private Particle getParticleFromConfig(String value){
        return Particle.valueOf(value.toUpperCase());
    }

    private boolean playerFoundAll(OfflinePlayer p){
        FileConfiguration data = plugin.getPlayerData().getConfig();

        boolean allFound = true;
        for(String key : plugin.getTreasures().getConfig().getConfigurationSection("treasures").getKeys(false)){
            if (data.isConfigurationSection("players")) {
                if (!data.getBoolean("player." + p.getName() + ".found." + key)) {
                    allFound = false;
                    break;
                }
            }
        }

        return allFound;
    }
}