package me.andrew.halloweenEvent.GUIs;

import me.andrew.halloweenEvent.TreasureHunt;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class AllTreasuresGUI implements Listener{
    private final TreasureHunt plugin;

    public AllTreasuresGUI(TreasureHunt plugin){
        this.plugin = plugin;
    }

    public void showAllTreasuresGUI(Player player){
        int allTreasuresGuiSize = 54;
        Inventory allTreasuresGUI = Bukkit.createInventory(null, allTreasuresGuiSize, "Choose treasure");

        player.openInventory(allTreasuresGUI);
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent event){

    }
}
