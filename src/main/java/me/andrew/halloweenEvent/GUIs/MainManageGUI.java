//Developed by _ItsAndrew_
package me.andrew.halloweenEvent.GUIs;

import me.andrew.halloweenEvent.TreasureHunt;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MainManageGUI implements Listener {
    TreasureHunt plugin;

    public MainManageGUI(TreasureHunt plugin){
        this.plugin = plugin;
    }

    //Shows the main GUI that open when you run /th treasures
    public void showMainManageGui(Player player){
        int invSize = 54;
        Inventory mainManageInv = Bukkit.createInventory(null, invSize, "Manage");

        //Set decorations and info sign
        for(int i = 0; i<9; i++){
            if(i == 4) continue;
            ItemStack blackGlassPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta blackGlassPaneMeta = blackGlassPane.getItemMeta();

            blackGlassPaneMeta.setDisplayName(" ");
            blackGlassPane.setItemMeta(blackGlassPaneMeta);

            mainManageInv.setItem(i, blackGlassPane);
        }
        for(int i = 45; i<54; i++){
            ItemStack blackGlassPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta blackGlassPaneMeta = blackGlassPane.getItemMeta();

            blackGlassPaneMeta.setDisplayName(" ");
            blackGlassPane.setItemMeta(blackGlassPaneMeta);

            mainManageInv.setItem(i, blackGlassPane);
        }

        //Info sign button
        ItemStack infoSignButton = new ItemStack(Material.OAK_SIGN);
        ItemMeta isbMeta = infoSignButton.getItemMeta();
        isbMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&f&lManage treasures and rewards!"));
        infoSignButton.setItemMeta(isbMeta);
        mainManageInv.setItem(4, infoSignButton);

        //Exit button
        ItemStack exitButton = new ItemStack(Material.RED_CONCRETE);
        ItemMeta exitButtonMeta = exitButton.getItemMeta();
        exitButtonMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lEXIT"));
        exitButton.setItemMeta(exitButtonMeta);
        mainManageInv.setItem(40, exitButton);

        //Manage Treasures button
        ItemStack manageTreasures = new ItemStack(Material.ENDER_CHEST);
        ItemMeta manageTreasuresMeta = manageTreasures.getItemMeta();
        manageTreasuresMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a&lMANAGE TREASURES"));

        List<String> manageTreasuresLore = new ArrayList<>();
        manageTreasuresLore.add("");
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
        manageRewardsMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a&lMANAGE REWARDS"));

        List<String> manageRewardsLore = new ArrayList<>();
        manageRewardsLore.add("");
        manageRewardsLore.add(ChatColor.translateAlternateColorCodes('&', "&7Click to manage: "));
        manageRewardsLore.add(ChatColor.translateAlternateColorCodes('&', "&7&l - adding items to treasures"));
        manageRewardsLore.add(ChatColor.translateAlternateColorCodes('&', "&7&l - deleting items from treasures"));
        manageRewardsMeta.setLore(manageRewardsLore);

        manageRewards.setItemMeta(manageRewardsMeta);
        mainManageInv.setItem(24, manageRewards);

        player.openInventory(mainManageInv);
    }

    @EventHandler
    public void onMainManageGuiClick(InventoryClickEvent event){
        event.setCancelled(true);
        if(!(event.getWhoClicked() instanceof Player player)) return;

        //If the  player clicks on nothing or black stained glass pane
        ItemStack clickedItem = event.getCurrentItem();
        if(clickedItem == null || clickedItem.getType().equals(Material.AIR) || clickedItem.getType().equals(Material.BLACK_STAINED_GLASS_PANE)) return;

        ItemMeta clickedItemMeta = clickedItem.getItemMeta();
        if(clickedItemMeta == null) return;

        //If the player clicks on the infoSign
        ItemStack infoSign = new ItemStack(Material.OAK_SIGN);
        if(clickedItem.equals(infoSign)) return;

        //Sound for clicking on the items
        NamespacedKey exitButtonSoundCheck = NamespacedKey.minecraft("ui.button.click");
        Sound clickItemSound = Registry.SOUNDS.get(exitButtonSoundCheck);

        //If the player clicks on exitButton
        Material exitButton = Material.RED_CONCRETE;
        if(clickedItem.getType() == exitButton){
            player.playSound(player.getLocation(), clickItemSound, 1f, 1f);
            player.closeInventory();
        }

        //If the player clicks on manageTreasures
        Material manageTreasureButton = Material.ENDER_CHEST;
        if(clickedItem.getType() == manageTreasureButton){
            player.playSound(player.getLocation(), clickItemSound, 1f, 1f);
            plugin.getManageTreasuresGUI().showTreasureManagersGUI(player);
        }

        //If the player clicks on manageRewards
        Material manageRewardsButton = Material.GOLD_INGOT;
        if(clickedItem.getType() == manageRewardsButton){
            player.playSound(player.getLocation(), clickItemSound, 1f, 1f);

        }
    }
}
