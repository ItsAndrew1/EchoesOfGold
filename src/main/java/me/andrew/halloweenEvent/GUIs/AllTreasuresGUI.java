package me.andrew.halloweenEvent.GUIs;

import me.andrew.halloweenEvent.TreasureHunt;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class AllTreasuresGUI implements Listener{
    private final TreasureHunt plugin;

    public AllTreasuresGUI(TreasureHunt plugin){
        this.plugin = plugin;
    }

    public void showAllTreasuresGUI(Player player){

    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent event){

    }
}
