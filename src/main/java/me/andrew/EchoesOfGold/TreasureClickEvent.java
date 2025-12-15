//Developed by _ItsAndrew_
package me.andrew.EchoesOfGold;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;


public class TreasureClickEvent implements Listener{
    EchoesOfGold plugin;
    int foundCount;

    public TreasureClickEvent(EchoesOfGold plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onTreasureClick(PlayerInteractEvent event){
        FileConfiguration treasures = plugin.getTreasures().getConfig();

        //If the event is canceled, returns
        if(!plugin.isEventActive()) return;

        if(event.getHand() != EquipmentSlot.HAND) return;
        if(event.getClickedBlock() == null) return;
        if(event.getClickedBlock().getType() != Material.PLAYER_HEAD) return;

        //Check if there are any treasures
        ConfigurationSection treasuresSection = treasures.getConfigurationSection("treasures");
        if(treasuresSection == null || treasuresSection.getKeys(false).isEmpty()) return;

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
                //Handling the rewards and other data
                handleTreasureFound(player, key);
                if(plugin.getPlayerData().getConfig().getBoolean("players." + player.getName() + ".found." + true)) continue;
                plugin.getPlayerData().saveConfig();
                plugin.getPlayerData().reloadConfig();

                //Refreshes the scoreboard if it is toggled
                boolean toggleScoreboard = plugin.getConfig().getBoolean("scoreboard");
                if(toggleScoreboard){
                    plugin.getScoreboardManager().refreshAll();
                }
                event.setCancelled(true);
            }
        }
    }

    private void handleTreasureFound(Player player, String treasureID){
        FileConfiguration data = plugin.getPlayerData().getConfig();
        FileConfiguration treasures = plugin.getTreasures().getConfig();
        String playerPath = "players." + player.getName();

        BukkitTask task = plugin.getTreasureManager().getTreasureParticleTasks().remove(treasureID);
        if(task != null){
            task.cancel();
        }

        //If the player already found this treasure
        boolean alreadyFound = data.getBoolean(playerPath + ".found."+treasureID, false);
        if(alreadyFound){
            Sound treasureAlreadyFound = Registry.SOUNDS.get(NamespacedKey.minecraft(plugin.getConfig().getString("treasure-found-sound").toLowerCase()));
            float tafVolume = plugin.getConfig().getInt("tfs-volume");
            float tafPitch = plugin.getConfig().getInt("tfs-pitch");

            //Spawning the particle
            double treasureX = treasures.getDouble("treasures."+treasureID+".x") + 0.5;
            double treasureY = treasures.getDouble("treasures."+treasureID+".y");
            double treasureZ = treasures.getDouble("treasures."+treasureID+".z") + 0.5;

            Location loc = new Location(Bukkit.getWorld("treasures."+treasureID+".world"), treasureX, treasureY, treasureZ);
            player.spawnParticle(getParticleFromConfig(), loc, 35, 0.2, 0.4, 0.2, 0);

            player.playSound(player.getLocation(), treasureAlreadyFound, tafVolume, tafPitch);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("treasure-already-found")));
            return;
        }

        //Giving the rewards to players
        String mainRewardsPath = "treasures."+treasureID+".rewards";
        Sound treasureClick = Registry.SOUNDS.get(NamespacedKey.minecraft(plugin.getConfig().getString("treasure-click-sound").toLowerCase()));
        float tcsVolume = plugin.getConfig().getInt("tcs-volume");
        float tcsPitch = plugin.getConfig().getInt("tcs-pitch");

        //If there aren't any rewards to that treasure
        ConfigurationSection treasureRewards = treasures.getConfigurationSection(mainRewardsPath);
        if(treasureRewards == null || treasureRewards.getKeys(false).isEmpty()){
            //Sound and firework
            player.playSound(player.getLocation(), treasureClick, tcsVolume, tcsPitch);
            spawnFirework(player, treasureID);

            //Setting the data for the player in 'playerdata.yml'
            data.set(playerPath + ".found." + treasureID, true);
            foundCount = data.getInt(playerPath +".treasures-found", 0) + 1;
            data.set(playerPath + ".treasures-found", foundCount);
            plugin.getPlayerData().saveConfig();

            //If they found all rewards
            if(foundCount == treasures.getInt("max-treasures")){
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("player-found-all-treasures")));
                foundCount++;
                return;
            }

            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("treasure-found")));
            return;
        }

        //Sound and firework
        player.playSound(player.getLocation(), treasureClick, tcsVolume, tcsPitch);
        spawnFirework(player, treasureID);

        for(String item : treasureRewards.getKeys(false)){
            int itemQuantity = treasures.getInt(mainRewardsPath+"."+item+".quantity");
            Material itemMaterial = Material.matchMaterial(item.toUpperCase());

            ItemStack reward = new ItemStack(itemMaterial, itemQuantity);
            ItemMeta rewardMeta = reward.getItemMeta();

            //Setting the enchantments for the reward (if there are any)
            ConfigurationSection enchantSection = treasures.getConfigurationSection(mainRewardsPath+"."+item+".enchantments");
            if(enchantSection != null){
                for(String enchant : treasures.getConfigurationSection(mainRewardsPath+"."+item+".enchantments").getKeys(false)){
                    int enchantLevel = treasures.getInt(mainRewardsPath+"."+item+".enchantments."+enchant);
                    Enchantment realEnchant = Enchantment.getByName(enchant);
                    rewardMeta.addEnchant(realEnchant, enchantLevel, true);
                }
            }

            //Check if the player has enough inventory space
            if(player.getInventory().firstEmpty() == -1){
                Sound noInventorySpace = Registry.SOUNDS.get(NamespacedKey.minecraft(plugin.getConfig().getString("no-inventory-space-sound").toLowerCase()));
                float nissVolume = plugin.getConfig().getInt("niss-volume");
                float nissPitch = plugin.getConfig().getInt("niss-pitch");

                player.playSound(player.getLocation(), noInventorySpace, nissVolume, nissPitch);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("no-inventory-space-message")));
                return;
            }

            reward.setItemMeta(rewardMeta);
            player.getInventory().addItem(reward);
        }

        //Setting the data for the player in 'playerdata.yml'
        data.set(playerPath + ".found." + treasureID, true);
        foundCount = data.getInt(playerPath +".treasures-found", 0) + 1;
        data.set(playerPath + ".treasures-found", foundCount);
        plugin.getPlayerData().saveConfig();

        //If they found all rewards
        if(foundCount == treasures.getInt("max-treasures")){
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("player-found-all-treasures")));
            foundCount++;
            return;
        }

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("treasure-found")));
    }

    private void spawnFirework(Player player, String treasure){
        FileConfiguration treasures = plugin.getTreasures().getConfig();

        boolean toggleFireworks = plugin.getConfig().getBoolean("toggle-fireworks-effect");
        String fwMainColor = plugin.getConfig().getString("fw-main-color");
        String fwSecondaryColor = plugin.getConfig().getString("fw-second-color");

        if(!toggleFireworks) return;

        Location playerLocation = new Location(Bukkit.getWorld(treasures.getString("treasures."+treasure+".world")), player.getX(), player.getY()+1.5, player.getZ());
        Firework firework =  (Firework) player.getWorld().spawnEntity(playerLocation, EntityType.FIREWORK_ROCKET);
        FireworkMeta fwMeta = firework.getFireworkMeta();

        //Adding the colors and effects
        fwMeta.addEffect(FireworkEffect.builder()
                        .withColor(getColorFromHex(fwMainColor.toUpperCase()), getColorFromHex(fwSecondaryColor.toUpperCase()))
                        .with(FireworkEffect.Type.BALL)
                        .flicker(true)
                        .trail(true)
                        .build());
        fwMeta.setPower(1);
        firework.setFireworkMeta(fwMeta);

        //Detonating the firework
        Bukkit.getScheduler().runTaskLater(plugin, firework::detonate, 1L);
    }

    //Gets the color from 'config.yml' for the firework
    private Color getColorFromHex(String hex){
        return Color.fromRGB(
                Integer.valueOf(hex.substring(1, 3), 16),
                Integer.valueOf(hex.substring(3, 5), 16),
                Integer.valueOf(hex.substring(5, 7), 16)
        );
    }

    //Gets the particle from 'config.yml'
    private Particle getParticleFromConfig(){
        Particle p = Particle.valueOf(plugin.getConfig().getString("treasure-found-particle"));
        return p;
    }
}
