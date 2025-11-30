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

import java.util.ArrayList;
import java.util.List;

public class AllTreasuresGUI implements Listener{
    private final TreasureHunt plugin;

    public AllTreasuresGUI(TreasureHunt plugin){
        this.plugin = plugin;
    }

    public void showAllTreasuresGUI(Player player){
        FileConfiguration treasures = plugin.getTreasures().getConfig();
        int allTreasuresGuiSize = 54;
        Inventory allTreasuresGUI = Bukkit.createInventory(null, allTreasuresGuiSize, "Choose treasure");

        //Decorations
        for(int i = 0; i<9; i++){
            if(i == 4) continue;
            ItemStack blackStainedGlass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta meta = blackStainedGlass.getItemMeta();
            meta.setDisplayName(" ");

            blackStainedGlass.setItemMeta(meta);
            allTreasuresGUI.setItem(i, blackStainedGlass);
        }
        for(int i = 45; i<54; i++){
            ItemStack blackStainedGlass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta meta = blackStainedGlass.getItemMeta();
            meta.setDisplayName(" ");

            blackStainedGlass.setItemMeta(meta);
            allTreasuresGUI.setItem(i, blackStainedGlass);
        }

        //InfoSign
        ItemStack infoSignItem = new ItemStack(Material.OAK_SIGN);
        ItemMeta isMeta = infoSignItem.getItemMeta();
        isMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&fChoose a treasure to &l"+plugin.getTreasureManagerChoice()+"&f!"));
        infoSignItem.setItemMeta(isMeta);
        allTreasuresGUI.setItem(4, infoSignItem);

        //Return button
        ItemStack returnItem = new ItemStack(Material.SPECTRAL_ARROW);
        ItemMeta riMeta = returnItem.getItemMeta();
        riMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lRETURN"));
        returnItem.setItemMeta(riMeta);
        allTreasuresGUI.setItem(40, returnItem);


        //Displaying the treasures
        //Check if there are treasures configured
        if(!treasures.isConfigurationSection("treasures")){
            //NoTreasures item
            ItemStack noTreasuresItem = new ItemStack(Material.BARRIER);
            ItemMeta ntiMeta = noTreasuresItem.getItemMeta();

            ntiMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eThere are no treasures configured!"));
            noTreasuresItem.setItemMeta(ntiMeta);

            allTreasuresGUI.setItem(22, noTreasuresItem);
            player.openInventory(allTreasuresGUI);
            return;
        }
        int slot = 10;
        for(String treasure : treasures.getConfigurationSection("treasures").getKeys(false)){
            //This is only for decoration purpose, so that the treasures are being displayed in a certain way
            if(slot == 17 || slot == 26) slot+=2;

            String mainPath = "treasures."+treasure;
            String treasureTitle = ChatColor.translateAlternateColorCodes('&', "&d&l"+treasures.getString(mainPath+".title"));

            ItemStack treasureItem = new ItemStack(Material.CHEST);
            ItemMeta tiMeta = treasureItem.getItemMeta();

            tiMeta.setDisplayName(treasureTitle);

            //Treasure lore
            int treasureX = treasures.getInt(mainPath+".x");
            int treasureY = treasures.getInt(mainPath+".y");
            int treasureZ = treasures.getInt(mainPath+".z");
            String treasureWorld = treasures.getString(mainPath+".world");

            List<String> coloredLore = new ArrayList<>();
            coloredLore.add(" ");
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', "&7 - Location: &l"+treasureX+" "+treasureY+" "+treasureZ));
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', "&7 - World: &l"+treasureWorld));
            tiMeta.setLore(coloredLore);

            treasureItem.setItemMeta(tiMeta);
            allTreasuresGUI.setItem(slot, treasureItem);
            slot++;
        }

        player.openInventory(allTreasuresGUI);
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent event){
        event.setCancelled(true);
        if(!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack clickedItem = event.getCurrentItem();
        if(clickedItem == null || clickedItem.getType() == Material.AIR) return;

        ItemMeta clickedItemMeta = clickedItem.getItemMeta();
        if(clickedItemMeta == null) return;

        NamespacedKey clickSoundKey = NamespacedKey.minecraft("ui.button.click");
        Sound clickSound = Registry.SOUNDS.get(clickSoundKey);


    }
}
