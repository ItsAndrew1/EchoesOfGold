package me.andrew.EchoesOfGold.GUIs;

import me.andrew.EchoesOfGold.EchoesOfGold;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ShopGUI implements Listener {
    private final EchoesOfGold plugin;

    public ShopGUI(EchoesOfGold plugin) {
        this.plugin = plugin;
    }

    public void showGUI(Player player, int page){
        FileConfiguration mainConfig = plugin.getConfig();

        //Getting the size of the gui
        int guiRows = mainConfig.getInt("economy.shop-gui.rows", 6);
        if(guiRows < 1 || guiRows > 6) guiRows = 6;
        int guiSize = guiRows * 9;

        //Getting the GUI's title
        String title = ChatColor.translateAlternateColorCodes('&', mainConfig.getString("economy.shop-gui.title", "Event Shop") + " (Page "+page+")");

        //Creating the GUI
        Inventory shopGUI = Bukkit.createInventory(null, guiSize, title);


        //Setting the exit button
        String materialTXT = mainConfig.getString("economy.shop-gui.exit-item.material", "red_concrete"); //Default value is 'red_concrete'
        int exitButtonSlot = mainConfig.getInt("economy.shop-gui.exit-item.slot", 49);

        ItemStack exitButton = new ItemStack(Material.matchMaterial(materialTXT.toUpperCase()));
        ItemMeta ebMeta = exitButton.getItemMeta();

        //Setting the display name
        String ebDisplayName = ChatColor.translateAlternateColorCodes('&', mainConfig.getString("economy.shop-gui.exit-item.display-name", "&c&lEXIT"));
        ebMeta.setDisplayName(ebDisplayName);
        exitButton.setItemMeta(ebMeta);
        shopGUI.setItem(exitButtonSlot, exitButton);


        //Adding the decoration if it is toggled
        boolean toggleDeco = mainConfig.getBoolean("economy.shop-gui.decoration.toggle", true);
        if(toggleDeco){
            String decoMaterialTXT = mainConfig.getString("economy.shop-gui.decoration.material", "black_stained_glass_pane");
            ItemStack decoItem = new ItemStack(Material.matchMaterial(decoMaterialTXT.toUpperCase()));
            ItemMeta diMeta = decoItem.getItemMeta();

            //Setting the display name
            String diDisplayName = mainConfig.getString("economy.shop-gui.decoration.display-name", " ");
            diMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', diDisplayName));

            decoItem.setItemMeta(diMeta);
            for(int i = 0; i<9; i++) shopGUI.setItem(i, decoItem);
        }


        //Displaying the 'no items' item (if there aren't any items configured
        ConfigurationSection shopItems = mainConfig.getConfigurationSection("economy.shop-gui.items");
        if(shopItems.getKeys(false).isEmpty()){
            String noHintsMaterialTXT = mainConfig.getString("economy.shop-gui.no-items-item.material", "barrier");
            ItemStack noHintsItem = new ItemStack(Material.matchMaterial(noHintsMaterialTXT.toUpperCase()));
            ItemMeta nhiMeta =  noHintsItem.getItemMeta();

            //Setting the display name
            String nhiDisplayName = mainConfig.getString("economy.shop-gui.no-items-item.display-name", "&eThere are no items set yet!");
            nhiMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', nhiDisplayName));

            noHintsItem.setItemMeta(nhiMeta);
            int nhiSlot = mainConfig.getInt("economy.shop-gui.no-items-item.slot", 31);
            shopGUI.setItem(nhiSlot, noHintsItem);
        }

        //Else, displays the items
        else{

        }

        player.openInventory(shopGUI);
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent event){

    }
}
