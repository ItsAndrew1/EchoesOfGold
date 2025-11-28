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

        //The item click sound
        NamespacedKey clickSoundKey= NamespacedKey.minecraft("ui.button.click");
        Sound clickButton = Registry.SOUNDS.get(clickSoundKey);

        //Decorations
        for(int i = 0; i<9; i++){
            if(i == 4){
                ItemStack infoSignItem = new ItemStack(Material.OAK_SIGN);
                ItemMeta infoSignMeta = infoSignItem.getItemMeta();
                infoSignMeta.setDisplayName("&f&lMANAGE TREASURES");

                int slot = 4;
                infoSignItem.setItemMeta(infoSignMeta);
                treasureManagerInv.setItem(slot, infoSignItem);
            }

            ItemStack blackStainedGlass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta blackStainedGlassMeta = blackStainedGlass.getItemMeta();
            blackStainedGlassMeta.setDisplayName("");
            blackStainedGlass.setItemMeta(blackStainedGlassMeta);

            treasureManagerInv.setItem(i, blackStainedGlass);
        }
        for(int i = 45; i<54; i++){
            ItemStack blackStainedGlass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta blackStainedGlassMeta = blackStainedGlass.getItemMeta();
            blackStainedGlassMeta.setDisplayName("");
            blackStainedGlass.setItemMeta(blackStainedGlassMeta);

            treasureManagerInv.setItem(i, blackStainedGlass);
        }

        //19 21 23 25
        //Create treasures button
        int createTreasureSlot = 19;
        ItemStack createTreasureButton = new ItemStack(Material.SLIME_BALL);
        ItemMeta ctbMeta = createTreasureButton.getItemMeta();

        ctbMeta.setDisplayName("&a&lCREATE TREASURE");
        ctbMeta.setLore(List.of("", ChatColor.translateAlternateColorCodes('&', "&7Create a new treasure!")));
        createTreasureButton.setItemMeta(ctbMeta);

        treasureManagerInv.setItem(createTreasureSlot, createTreasureButton);

        //Delete treasures button
        int deleteTreasuresSlot = 21;
        ItemStack deleteTreasureButton = new ItemStack(Material.BARRIER);
        ItemMeta dtbMeta = deleteTreasureButton.getItemMeta();

        dtbMeta.setDisplayName("&c&lDELETE TREASURE");
        dtbMeta.setLore(List.of("", ChatColor.translateAlternateColorCodes('&', "&7")));
        deleteTreasureButton.setItemMeta(dtbMeta);

        treasureManagerInv.setItem(deleteTreasuresSlot, deleteTreasureButton);

        //SetLocation button
        int setLocationSlot = 23;
        ItemStack setLocationButton = new ItemStack(Material.COMPASS);
        ItemMeta slbMeta = setLocationButton.getItemMeta();

        slbMeta.setDisplayName("&d&lSET LOCATION TREASURE");
        slbMeta.setLore(List.of(ChatColor.translateAlternateColorCodes('&', "&7Set the world for a treasure!")));
        setLocationButton.setItemMeta(slbMeta);

        treasureManagerInv.setItem(setLocationSlot, setLocationButton);

        //SetWorld button
        ItemStack setWorldButton = new ItemStack(Material.MAP);
        ItemMeta swbMeta = setWorldButton.getItemMeta();
        int setWorldSlot = 25;

        swbMeta.setDisplayName("&b&lSET WORLD TREASURE");
        swbMeta.setLore(List.of(ChatColor.translateAlternateColorCodes('&', "&7Set the world for a treasure!")));
        setWorldButton.setItemMeta(swbMeta);

        treasureManagerInv.setItem(setWorldSlot, setWorldButton);

        //Return button
        int returnButtonSlot = 40;
        ItemStack returnButton = new ItemStack(Material.SPECTRAL_ARROW);
        ItemMeta rbMeta = returnButton.getItemMeta();

        rbMeta.setDisplayName("&c&lRETURN");
        returnButton.setItemMeta(rbMeta);

        treasureManagerInv.setItem(returnButtonSlot, returnButton);
    }

    @EventHandler
    public void onTreasureManagerClick(InventoryClickEvent event){
        event.setCancelled(true);

        if(!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack clickedItem = event.getCurrentItem();
        if(clickedItem == null || clickedItem.getType().equals(Material.AIR) || clickedItem.getType().equals(Material.BLACK_STAINED_GLASS_PANE)) return;

        ItemMeta clickedItemMeta = clickedItem.getItemMeta();
        if(clickedItemMeta == null) return;


    }
}
