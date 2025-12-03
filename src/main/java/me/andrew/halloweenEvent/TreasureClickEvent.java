//Developed by _ItsAndrew_
package me.andrew.halloweenEvent;

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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;


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
                //Sound and firework
                Sound treasureClick = Registry.SOUNDS.get(NamespacedKey.minecraft(plugin.getConfig().getString("treasure-click-sound").toLowerCase()));
                float tcsVolume = plugin.getConfig().getInt("tcs-volume");
                float tcsPitch = plugin.getConfig().getInt("tcs-pitch");

                player.playSound(player.getLocation(), treasureClick, tcsVolume, tcsPitch);
                spawnFirework(player);

                //Handling the rewards and other data
                handleTreasureFound(player, key);
                if(plugin.getPlayerData().getConfig().getBoolean("players." + player.getName() + ".found." + true)) continue;
                plugin.getPlayerData().saveConfig();
                plugin.getPlayerData().reloadConfig();
                plugin.getScoreboardManager().refreshAll();
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

            player.playSound(player.getLocation(), treasureAlreadyFound, tafVolume, tafPitch);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("treasure-already-found")));
            return;
        }

        //Giving the rewards to players
        String mainRewardsPath = "treasures."+treasureID+".rewards";
        if(treasures.getConfigurationSection(mainRewardsPath) == null || treasures.getConfigurationSection(mainRewardsPath).getKeys(false).isEmpty()){ //Checking if the treasure has rewards or not
            Sound treasureHasNoItems = Registry.SOUNDS.get(NamespacedKey.minecraft(plugin.getConfig().getString("treasure-has-no-items-sound").toLowerCase()));
            float thniVolume = plugin.getConfig().getInt("thni-volume");
            float thniPitch = plugin.getConfig().getInt("thni-pitch");

            player.playSound(player.getLocation(), treasureHasNoItems, thniVolume, thniPitch);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("treasure-has-no-rewards")));
            return;
        }

        //Sound and firework
        Sound treasureClick = Registry.SOUNDS.get(NamespacedKey.minecraft(plugin.getConfig().getString("treasure-click-sound").toLowerCase()));
        float tcsVolume = plugin.getConfig().getInt("tcs-volume");
        float tcsPitch = plugin.getConfig().getInt("tcs-pitch");

        player.playSound(player.getLocation(), treasureClick, tcsVolume, tcsPitch);
        spawnFirework(player);

        for(String item : treasures.getConfigurationSection(mainRewardsPath).getKeys(false)){
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

    public void spawnFirework(Player player){
        boolean toggleFireworks = plugin.getConfig().getBoolean("toggle-fireworks-effect");
        if(!toggleFireworks) return;

        Firework firework =  (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK_ROCKET);
        FireworkMeta fwMeta = firework.getFireworkMeta();

        //Adding the colors and effects
        fwMeta.addEffect(FireworkEffect.builder()
                        .withColor(Color.AQUA, Color.PURPLE)
                        .with(FireworkEffect.Type.BALL)
                        .flicker(true)
                        .trail(true)
                        .build());
        fwMeta.setPower(1);
        firework.setFireworkMeta(fwMeta);

        //Detonating the firework
        Bukkit.getScheduler().runTaskLater(plugin, firework::detonate, 1L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        if(!plugin.eventActive) return;

        plugin.getBossBar().addPlayer(event.getPlayer());
        plugin.getTreasureManager().spawnChestParticles();
    }
}
