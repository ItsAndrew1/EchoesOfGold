//Developed by _ItsAndrew_
package me.andrew.halloweenEvent;

import io.papermc.paper.event.player.AsyncChatCommandDecorateEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ManageTreasuresGUIs implements Listener {
    TreasureHunt plugin;

    public ManageTreasuresGUIs(TreasureHunt plugin){
        this.plugin = plugin;
    }

    //Shows the main GUI that open when you run /th treasures
    public void showMainManageGui(){
        FileConfiguration treasures = plugin.getTreasures().getConfig();

        int invSize = 54;
        Inventory mainManageInv = Bukkit.createInventory(null, invSize, "Manage");

        //Set decorations and info sign
        for(int i = 0; i<9; i++){
            if(i == 4){
                int infoSignSlot = 4;
                ItemStack infoSign = new ItemStack(Material.OAK_SIGN);
                ItemMeta infoSignMeta = infoSign.getItemMeta();

                infoSignMeta.setDisplayName("&f&lMANAGE TREASURES AND REWARDS");
                infoSign.setItemMeta(infoSignMeta);

                mainManageInv.setItem(infoSignSlot, infoSign);
            }
            ItemStack blackGlassPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta blackGlassPaneMeta = blackGlassPane.getItemMeta();

            blackGlassPaneMeta.setDisplayName("");
            blackGlassPane.setItemMeta(blackGlassPaneMeta);

            mainManageInv.setItem(i, blackGlassPane);
        }
        for(int i = 45; i<54; i++){
            ItemStack blackGlassPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta blackGlassPaneMeta = blackGlassPane.getItemMeta();

            blackGlassPaneMeta.setDisplayName("");
            blackGlassPane.setItemMeta(blackGlassPaneMeta);

            mainManageInv.setItem(i, blackGlassPane);
        }

        //Exit button
        ItemStack exitButton = new ItemStack(Material.RED_CONCRETE);
        ItemMeta exitButtonMeta = exitButton.getItemMeta();
        exitButtonMeta.setDisplayName("&c&lEXIT");
        exitButton.setItemMeta(exitButtonMeta);
        mainManageInv.setItem(40, exitButton);


        //Manage Treasures button
        ItemStack manageTreasures = new ItemStack(Material.ENDER_CHEST);
        ItemMeta manageTreasuresMeta = manageTreasures.getItemMeta();
        manageTreasuresMeta.setDisplayName("&a&lMANAGE TREASURES");

        List<String> manageTreasuresLore = new ArrayList<>();
        manageTreasuresLore.add(ChatColor.translateAlternateColorCodes('&', "&7Click to manage: "));
        manageTreasuresLore.add(ChatColor.translateAlternateColorCodes('&', "&7&l - creating treasures"));
        manageTreasuresLore.add(ChatColor.translateAlternateColorCodes('&', "&7&l - deleting treasures"));
        manageTreasuresLore.add(ChatColor.translateAlternateColorCodes('&', "&7&l - configuring treasures"));
        manageTreasuresMeta.setLore(manageTreasuresLore);

        manageTreasures.setItemMeta(manageTreasuresMeta);
        mainManageInv.setItem(20, manageTreasures);


        //Manage Rewards button
        ItemStack manageRewards = new ItemStack(Material.GOLD_INGOT);
        ItemMeta manageRewardsMeta = manageRewards.getItemMeta();
        manageRewardsMeta.setDisplayName("&aMANAGE REWARDS");

        List<String> manageRewardsLore = new ArrayList<>();
        manageTreasuresLore.add(ChatColor.translateAlternateColorCodes('&', "&7Click to manage: "));
        manageTreasuresLore.add(ChatColor.translateAlternateColorCodes('&', "&7&l - adding items to treasures"));
        manageTreasuresLore.add(ChatColor.translateAlternateColorCodes('&', "&7&l - deleting items from treasures"));
        manageTreasuresMeta.setLore(manageRewardsLore);

        manageTreasures.setItemMeta(manageRewardsMeta);
        mainManageInv.setItem(24, manageRewards);
    }

    @EventHandler
    public void onMainManageGuiClick(InventoryClickEvent event){
        event.setCancelled(true);

        if(!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack clickedItem = event.getCurrentItem();
        if(clickedItem == null || clickedItem.getType().equals(Material.AIR)) return;

        ItemMeta clickedItemMeta = clickedItem.getItemMeta();
        if(clickedItemMeta == null) return;

        //Will continue with the click event tomorrow
    }
}
