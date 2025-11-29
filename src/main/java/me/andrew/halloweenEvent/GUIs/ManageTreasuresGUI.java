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

import java.util.List;

public class ManageTreasuresGUI implements Listener{
    private final TreasureHunt plugin;

    public ManageTreasuresGUI(TreasureHunt plugin){
        this.plugin = plugin;
    }

    public void showTreasureManagersGUI(Player player){
        int invSize = 54;
        Inventory treasureManagerInv = Bukkit.createInventory(null, invSize, "Manage treasures");

        //Decorations
        for(int i = 0; i<9; i++){
            if(i == 4) continue;

            ItemStack blackStainedGlass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta blackStainedGlassMeta = blackStainedGlass.getItemMeta();
            blackStainedGlassMeta.setDisplayName(" ");
            blackStainedGlass.setItemMeta(blackStainedGlassMeta);

            treasureManagerInv.setItem(i, blackStainedGlass);
        }
        for(int i = 45; i<54; i++){
            ItemStack blackStainedGlass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta blackStainedGlassMeta = blackStainedGlass.getItemMeta();
            blackStainedGlassMeta.setDisplayName(" ");
            blackStainedGlass.setItemMeta(blackStainedGlassMeta);

            treasureManagerInv.setItem(i, blackStainedGlass);
        }

        //InfoSign button
        int infoSignSlot = 4;
        ItemStack infoSign = new ItemStack(Material.OAK_SIGN);
        ItemMeta isMeta = infoSign.getItemMeta();
        isMeta.setDisplayName("&f&lMANAGE YOUR TREASURES: ");

        //Continue with lore

        //Create treasures button
        int createTreasureSlot = 19;
        ItemStack createTreasureButton = new ItemStack(Material.SLIME_BALL);
        ItemMeta ctbMeta = createTreasureButton.getItemMeta();

        ctbMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a&lCREATE TREASURE"));
        ctbMeta.setLore(List.of("", ChatColor.translateAlternateColorCodes('&', "&7Create a new treasure!")));
        createTreasureButton.setItemMeta(ctbMeta);

        treasureManagerInv.setItem(createTreasureSlot, createTreasureButton);

        //Delete treasures button
        int deleteTreasuresSlot = 21;
        ItemStack deleteTreasureButton = new ItemStack(Material.BARRIER);
        ItemMeta dtbMeta = deleteTreasureButton.getItemMeta();

        dtbMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&4&lDELETE TREASURE"));
        dtbMeta.setLore(List.of("", ChatColor.translateAlternateColorCodes('&', "&7Delete one of the treasures!")));
        deleteTreasureButton.setItemMeta(dtbMeta);

        treasureManagerInv.setItem(deleteTreasuresSlot, deleteTreasureButton);

        //SetLocation button
        int setLocationSlot = 23;
        ItemStack setLocationButton = new ItemStack(Material.COMPASS);
        ItemMeta slbMeta = setLocationButton.getItemMeta();

        slbMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&d&lSET TREASURE LOCATION"));
        slbMeta.setLore(List.of("", ChatColor.translateAlternateColorCodes('&', "&7Set the &llocation&7 for a treasure!")));
        setLocationButton.setItemMeta(slbMeta);

        treasureManagerInv.setItem(setLocationSlot, setLocationButton);

        //SetWorld button
        ItemStack setWorldButton = new ItemStack(Material.MAP);
        ItemMeta swbMeta = setWorldButton.getItemMeta();
        int setWorldSlot = 25;

        swbMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b&lSET TREASURE WORLD"));
        swbMeta.setLore(List.of("", ChatColor.translateAlternateColorCodes('&', "&7Set the world for a treasure!")));
        setWorldButton.setItemMeta(swbMeta);

        treasureManagerInv.setItem(setWorldSlot, setWorldButton);

        //Return button
        int returnButtonSlot = 40;
        ItemStack returnButton = new ItemStack(Material.SPECTRAL_ARROW);
        ItemMeta rbMeta = returnButton.getItemMeta();

        rbMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lRETURN"));
        returnButton.setItemMeta(rbMeta);

        treasureManagerInv.setItem(returnButtonSlot, returnButton);

        player.openInventory(treasureManagerInv);
    }

    @EventHandler
    public void onTreasureManagerClick(InventoryClickEvent event){
        event.setCancelled(true);

        if(!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack clickedItem = event.getCurrentItem();
        if(clickedItem == null || clickedItem.getType().equals(Material.AIR) || clickedItem.getType().equals(Material.BLACK_STAINED_GLASS_PANE)) return;

        ItemMeta clickedItemMeta = clickedItem.getItemMeta();
        if(clickedItemMeta == null) return;

        //The item click sound
        NamespacedKey clickSoundKey= NamespacedKey.minecraft("ui.button.click");
        Sound clickButtonSound = Registry.SOUNDS.get(clickSoundKey);

        //If the player clicks on the return button
        Material returnButton = Material.SPECTRAL_ARROW;
        if(clickedItem.getType() == returnButton){
            player.playSound(player.getLocation(), clickButtonSound, 1f, 1f);
            player.closeInventory();
            plugin.getManageGUI().showMainManageGui(player);
        }

        //If the player clicks on the createTreasure button
        Material createButton = Material.SLIME_BALL;
        if (clickedItem.getType() == createButton){

        }

        //If the player clicks on the deleteTreasure button
        Material deleteButton = Material.BARRIER;
        if(clickedItem.getType() == deleteButton){
            player.playSound(player.getLocation(), clickButtonSound, 1f, 1f);
            player.closeInventory();
            plugin.getAllTreasuresGUI().showAllTreasuresGUI(player);
            plugin.setTreasureManagerChoice("delete");
        }

        //If the player click on the setLocationTreasure button
        Material setLocationButton = Material.COMPASS;
        if(clickedItem.getType() == setLocationButton){
            player.playSound(player.getLocation(), clickButtonSound, 1f, 1f);
            player.closeInventory();
            plugin.getAllTreasuresGUI().showAllTreasuresGUI(player);
            plugin.setTreasureManagerChoice("setlocation");
        }

        //If the player clicks on the setWorld button
        Material setWorldButton = Material.MAP;
        if(clickedItem.getType() == setWorldButton){
            player.playSound(player.getLocation(), clickButtonSound, 1f, 1f);
            player.closeInventory();
            plugin.getAllTreasuresGUI().showAllTreasuresGUI(player);
            plugin.setTreasureManagerChoice("setworld");
        }
    }
}
