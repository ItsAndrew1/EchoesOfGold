//Developed by _ItsAndrew_
package me.andrew.EchoesOfGold.GUIs;

import me.andrew.EchoesOfGold.EchoesOfGold;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddRewardsGUI implements Listener {
    private final EchoesOfGold plugin;
    private String treasure;
    private Inventory rewards;

    public AddRewardsGUI(EchoesOfGold plugin){
        this.plugin = plugin;
    }

    public void showAddRewardsGUI(Player player, String treasureID){
        int rewardsGUIsize = 9;
        this.treasure = treasureID;
        rewards = Bukkit.createInventory(null, rewardsGUIsize, "Enter rewards");

        //Setting the button for putting the rewards to a treasure
        ItemStack putRewardsButton = new ItemStack(Material.GREEN_CONCRETE);
        ItemMeta prbMeta = putRewardsButton.getItemMeta();
        prbMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aPut rewards in treasure &l"+treasure));
        putRewardsButton.setItemMeta(prbMeta);
        rewards.setItem(8, putRewardsButton);

        player.openInventory(rewards);
    }

    public void putRewardsInTreasure(List<ItemStack> contents){
        FileConfiguration treasures = plugin.getTreasures().getConfig();
        String mainRewardsPath = "treasures."+treasure+".rewards.";

        for(ItemStack item : contents){
            if(item == null || item.getType().equals(Material.AIR)) continue;

            Material rewardMat = item.getType();
            String rawMaterial = rewardMat.toString().toLowerCase();
            int rewardQuantity = item.getAmount();

            treasures.set(mainRewardsPath+rawMaterial+".material", rawMaterial);
            treasures.set(mainRewardsPath+rawMaterial+".quantity", rewardQuantity);
            plugin.getTreasures().saveConfig();

            //Setting the enchantments for each item
            ItemMeta rewardMeta = item.getItemMeta();
            if(rewardMeta != null && rewardMeta.hasEnchants()){
                Map<String, Integer> enchants = new HashMap<>();
                Map<org.bukkit.enchantments.Enchantment, Integer> trueEnchants = item.getEnchantments();

                //Putting the enchantments in the 'enchants' map
                for(Map.Entry<org.bukkit.enchantments.Enchantment, Integer> e : trueEnchants.entrySet()){
                    enchants.put(e.getKey().getKey().getKey().toLowerCase(), e.getValue());
                }

                //Saving the enchants and the level in treasures.yml
                for(Map.Entry<String, Integer> e : enchants.entrySet()){
                    treasures.set(mainRewardsPath+rawMaterial+".enchantments."+e.getKey(), e.getValue());
                    plugin.getTreasures().saveConfig();
                }
            }
            else{ //Sets the enchantments to null if there aren't any
                treasures.set(mainRewardsPath+rawMaterial+".enchantments", "");
                plugin.getTreasures().saveConfig();
            }
        }
    }

    @EventHandler
    public void rewardsGuiClick(InventoryClickEvent event){
        if(!(event.getWhoClicked() instanceof Player player)) return;
        if(!event.getView().getTitle().equalsIgnoreCase("Enter rewards")) return;

        event.setCancelled(false); //Lets the player put and get items from GUI

        ItemStack clickedItem = event.getCurrentItem();
        if(clickedItem == null || clickedItem.getType().equals(Material.AIR)) return;

        //The sound when the player clicks the button
        Sound buttonSound = Registry.SOUNDS.get(NamespacedKey.minecraft("ui.button.click"));

        //The chat prefix
        String prefix = plugin.getConfig().getString("chat-prefix");

        //If the player clicks on the putRewardsButton
        Material putRewardsButton = Material.GREEN_CONCRETE;
        if(clickedItem.getType().equals(putRewardsButton)){
            event.setCancelled(true); //Doesn't let the player take the button

            List<ItemStack> rewardsItems = new ArrayList<>();
            for(int i = 0; i<=7; i++){
                if(rewards.getItem(i) == null || rewards.getItem(i).getType().equals(Material.AIR)) continue;
                rewardsItems.add(rewards.getItem(i));
            }

            //Check if the slots are empty
            if(rewardsItems.isEmpty()){
                player.closeInventory();
                player.playSound(player.getLocation(), buttonSound, 1f, 1f);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix+" &aSaved &cno rewards&a in treasure &l"+treasure+"&a!"));

                //Opens the main GUI after 0.5 secs
                new BukkitRunnable(){
                    @Override
                    public void run(){
                        plugin.getManageGUI().showMainManageGui(player);
                    }
                }.runTaskLater(plugin, 10L);
                return;
            }

            putRewardsInTreasure(rewardsItems); //Puts the rewards in the specific treasure
            player.closeInventory();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix+" &aSaved rewards for treasure &l"+treasure+"&a!"));
            player.playSound(player.getLocation(), buttonSound, 1f, 1f);

            //Opens the main GUI after 0.5 secs
            new BukkitRunnable(){
                @Override
                public void run(){
                    plugin.getManageGUI().showMainManageGui(player);
                }
            }.runTaskLater(plugin, 10L);
        }
    }
}
