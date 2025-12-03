//Developed by _ItsAndrew_
package me.andrew.halloweenEvent.GUIs;

import me.andrew.halloweenEvent.TreasureHunt;
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

import java.util.*;

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
        isMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&f&lMANAGE YOUR TREASURES: "));

        List<String> coloredInfoSignLore = new ArrayList<>();
        coloredInfoSignLore.add(ChatColor.translateAlternateColorCodes('&', ""));
        coloredInfoSignLore.add(ChatColor.translateAlternateColorCodes('&', "&7 - &lcreate &7treasures"));
        coloredInfoSignLore.add(ChatColor.translateAlternateColorCodes('&', "&7 - &ldelete &7treasures"));
        coloredInfoSignLore.add(ChatColor.translateAlternateColorCodes('&', "&7 - &lsetWorld &7for treasures"));
        coloredInfoSignLore.add(ChatColor.translateAlternateColorCodes('&', "&7 - &lsetLocation &7for treasures"));
        coloredInfoSignLore.add(ChatColor.translateAlternateColorCodes('&', "&7 - &lsetFacing &7for treasures"));
        isMeta.setLore(coloredInfoSignLore);

        infoSign.setItemMeta(isMeta);
        treasureManagerInv.setItem(infoSignSlot, infoSign);

        //Create treasures button
        int createTreasureSlot = 18;
        ItemStack createTreasureButton = new ItemStack(Material.SLIME_BALL);
        ItemMeta ctbMeta = createTreasureButton.getItemMeta();

        ctbMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a&lCREATE TREASURE"));
        ctbMeta.setLore(List.of("", ChatColor.translateAlternateColorCodes('&', "&7Create a new treasure!")));
        createTreasureButton.setItemMeta(ctbMeta);

        treasureManagerInv.setItem(createTreasureSlot, createTreasureButton);

        //Delete treasures button
        int deleteTreasuresSlot = 20;
        ItemStack deleteTreasureButton = new ItemStack(Material.BARRIER);
        ItemMeta dtbMeta = deleteTreasureButton.getItemMeta();

        dtbMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&4&lDELETE TREASURE"));
        dtbMeta.setLore(List.of("", ChatColor.translateAlternateColorCodes('&', "&7Delete one of the treasures!")));
        deleteTreasureButton.setItemMeta(dtbMeta);

        treasureManagerInv.setItem(deleteTreasuresSlot, deleteTreasureButton);

        //SetLocation button
        int setLocationSlot = 22;
        ItemStack setLocationButton = new ItemStack(Material.COMPASS);
        ItemMeta slbMeta = setLocationButton.getItemMeta();

        slbMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&5&lSET TREASURE LOCATION"));
        slbMeta.setLore(List.of("", ChatColor.translateAlternateColorCodes('&', "&7Set the &llocation&7 for a treasure!")));
        setLocationButton.setItemMeta(slbMeta);

        treasureManagerInv.setItem(setLocationSlot, setLocationButton);

        //SetWorld button
        ItemStack setWorldButton = new ItemStack(Material.MAP);
        ItemMeta swbMeta = setWorldButton.getItemMeta();
        int setWorldSlot = 24;

        swbMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b&lSET TREASURE WORLD"));
        swbMeta.setLore(List.of("", ChatColor.translateAlternateColorCodes('&', "&7Set the &lworld &7for a treasure!")));
        setWorldButton.setItemMeta(swbMeta);

        treasureManagerInv.setItem(setWorldSlot, setWorldButton);

        //SetFacing button
        ItemStack setFacingButton = new ItemStack(Material.GLOW_ITEM_FRAME);
        ItemMeta sfbMeta = setFacingButton.getItemMeta();
        int setFacingSlot = 26;

        sfbMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&9&lSET TREASURE FACING"));
        List<String> coloredSfbLore = new ArrayList<>(); //Adding lore
        coloredSfbLore.add("");
        coloredSfbLore.add(ChatColor.translateAlternateColorCodes('&', "&7Set the &lfacing &7for a treasure!"));
        coloredSfbLore.add(ChatColor.translateAlternateColorCodes('&', "&e&lTIP: &eValues may be 'NORTH', 'SOUTH', 'NORTH_EAST', etc."));
        sfbMeta.setLore(coloredSfbLore);
        setFacingButton.setItemMeta(sfbMeta);

        treasureManagerInv.setItem(setFacingSlot, setFacingButton);

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
        if(!(event.getWhoClicked() instanceof Player player)) return;
        if(!event.getView().getTitle().equals("Manage treasures")) return;

        event.setCancelled(true);

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
            plugin.getManageGUI().showMainManageGui(player);
        }

        //If the player clicks on the createTreasure button
        Material createButton = Material.SLIME_BALL;
        if (clickedItem.getType() == createButton){
            player.closeInventory();
            createTreasure(player);
        }

        //If the player clicks on the deleteTreasure button
        Material deleteButton = Material.BARRIER;
        if(clickedItem.getType() == deleteButton){
            player.playSound(player.getLocation(), clickButtonSound, 1f, 1f);
            plugin.setTreasureManagerChoice("delete");
            plugin.getAllTreasuresGUI().showAllTreasuresGUI(player);
        }

        //If the player click on the setLocationTreasure button
        Material setLocationButton = Material.COMPASS;
        if(clickedItem.getType() == setLocationButton){
            player.playSound(player.getLocation(), clickButtonSound, 1f, 1f);
            plugin.setTreasureManagerChoice("set location");
            plugin.getAllTreasuresGUI().showAllTreasuresGUI(player);
        }

        //If the player clicks on the setWorld button
        Material setWorldButton = Material.MAP;
        if(clickedItem.getType() == setWorldButton){
            player.playSound(player.getLocation(), clickButtonSound, 1f, 1f);
            plugin.setTreasureManagerChoice("set world");
            plugin.getAllTreasuresGUI().showAllTreasuresGUI(player);
        }

        //If the player clicks on setFacing button
        Material setFacingButton = Material.GLOW_ITEM_FRAME;
        if(clickedItem.getType() == setFacingButton){
            player.playSound(player.getLocation(), clickButtonSound, 1f, 1f);
            plugin.setTreasureManagerChoice("set facing");
            plugin.getAllTreasuresGUI().showAllTreasuresGUI(player);
        }
    }

    //Saves the data of a treasure to treasures.yml
    public void createTreasure(Player player) {
        FileConfiguration treasures = plugin.getTreasures().getConfig();
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aEnter a name for your treasure:"));

        //Waits for the player input
        plugin.waitForPlayerInput(player, input -> {
            String treasureName = input.trim();

            //Checks if the input is empty
            if (treasureName.isEmpty()) {
                Sound invalidSound = Registry.SOUNDS.get(NamespacedKey.minecraft("entity.villager.no"));
                player.playSound(player.getLocation(), invalidSound, 1f, 0.9f);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThe name cannot be empty!"));
                return;
            }

            //Checks if there is a treasure with the input's name
            if (treasures.isConfigurationSection("treasures") && treasures.getConfigurationSection("treasures").getKeys(false).contains(treasureName)) {
                Sound duplicateSound = Registry.SOUNDS.get(NamespacedKey.minecraft("entity.villager.no"));
                player.playSound(player.getLocation(), duplicateSound, 1f, 0.9f);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cA treasure with name &l"+treasureName+" &calready exists!"));
                return;
            }

            String path = "treasures." + treasureName;
            treasures.set(path + ".rewards", "");
            treasures.set(path + ".title", treasureName);
            treasures.set(path + ".facing", "");
            treasures.set(path + ".world", "");
            treasures.set(path + ".x", "");
            treasures.set(path + ".y", "");
            treasures.set(path + ".z", "");
            plugin.getTreasures().saveConfig();

            Sound successSound = Registry.SOUNDS.get(NamespacedKey.minecraft("entity.experience_orb.pickup"));
            player.playSound(player.getLocation(), successSound, 1f, 1f);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSaved treasure &l"+treasureName+"&a!"));

            //Re-opens the manageTreasuresGUI after 0.5 secs
            new BukkitRunnable(){
                @Override
                public void run(){
                    plugin.getManageGUI().showMainManageGui(player);
                }
            }.runTaskLater(plugin, 10L);
        });
    }
}
