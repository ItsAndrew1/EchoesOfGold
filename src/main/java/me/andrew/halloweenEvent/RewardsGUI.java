package me.andrew.halloweenEvent;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RewardsGUI implements Listener {
    TreasureHunt plugin;

    public RewardsGUI(TreasureHunt plugin){
        this.plugin = plugin;
    }

    public void manageRewardsGUI(){
        FileConfiguration rewards = plugin.getRewards().getConfig();


    }

    @EventHandler
    public void onManageRewardsClick(InventoryClickEvent event){
        FileConfiguration rewards = plugin.getRewards().getConfig();
        if(!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack clickedItem = event.getCurrentItem();
        if(clickedItem == null || clickedItem.getType() == Material.AIR) return;

        ItemMeta clickedItemMeta = clickedItem.getItemMeta();
        if(clickedItemMeta == null) return;

        String rewardsGuiTitle = rewards.getString("rewards-gui.title");
        if(!event.getView().getTitle().equalsIgnoreCase(rewardsGuiTitle)) return;


    }
}
