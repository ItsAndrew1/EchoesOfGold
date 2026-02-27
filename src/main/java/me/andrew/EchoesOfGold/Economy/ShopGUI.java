//Developed by _ItsAndrew_
package me.andrew.EchoesOfGold.Economy;

import me.andrew.EchoesOfGold.EchoesOfGold;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ShopGUI implements Listener {
    private final EchoesOfGold plugin;
    private final NamespacedKey shopKey;

    public ShopGUI(EchoesOfGold plugin) {
        this.plugin = plugin;
        this.shopKey = new NamespacedKey(plugin, "shop-item-id");
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
        ItemStack exitButton = createButton(Material.matchMaterial(materialTXT.toUpperCase()), mainConfig.getString("economy.shop-gui.exit-item.display-name", "&c&lEXIT"));
        shopGUI.setItem(exitButtonSlot, exitButton);

        //Adding the decoration if it is toggled
        boolean toggleDeco = mainConfig.getBoolean("economy.shop-gui.decoration.toggle", true);
        if(toggleDeco){
            String decoMaterialTXT = mainConfig.getString("economy.shop-gui.decoration.material", "black_stained_glass_pane");
            ItemStack decoItem = createButton(Material.matchMaterial(decoMaterialTXT.toUpperCase()), mainConfig.getString("economy.shop-gui.decoration.display-name", " "));
            for(int i = 0; i<9; i++) shopGUI.setItem(i, decoItem);
        }

        //Getting data for displaying the items
        ConfigurationSection shopItems = mainConfig.getConfigurationSection("economy.shop-gui.items");
        int startingSlot = toggleDeco ? 9 : 0;
        int maximumNrOfItemsPerPage = toggleDeco ? 36 : 45;
        List<String> itemList = shopItems.getKeys(false).stream().toList();
        int offset = (page - 1) * maximumNrOfItemsPerPage;
        int endIndex = Math.min(offset + maximumNrOfItemsPerPage, itemList.size());

        //Displaying the 'no items' item (if there aren't any items configured)
        if(shopItems.getKeys(false).isEmpty()){
            String noHintsMaterialTXT = mainConfig.getString("economy.shop-gui.no-items-item.material", "barrier");
            ItemStack noHintsItem = createButton(Material.matchMaterial(noHintsMaterialTXT.toUpperCase()), mainConfig.getString("economy.shop-gui.no-items-item.display-name", "&eThere are no items set yet!"));
            int nhiSlot = mainConfig.getInt("economy.shop-gui.no-items-item.slot", 31);
            shopGUI.setItem(nhiSlot, noHintsItem);
        }

        //Else, displays the items
        else{
            for(int i = 0, j = offset; i<45 && j < itemList.size(); i++, j++){
                ItemStack shopItem = shopItems.getItemStack(itemList.get(j) + ".item");
                double shopItemPrice = shopItems.getDouble(itemList.get(j) + ".price");
                String itemPriceLore = mainConfig.getString("economy.shop-gui.displaying-the-price", "&aPrice: &f%item_price%")
                        .replace("%item_price%", String.valueOf(shopItemPrice));

                //Building the item lore
                List<String> shopItemLore = shopItem.hasItemMeta() ? shopItem.getItemMeta().getLore() : new ArrayList<>();
                shopItemLore.add(" ");
                shopItemLore.add(ChatColor.translateAlternateColorCodes('&', itemPriceLore));
                shopItemLore.add(plugin.getEconomyProvider().hasEnough(player, shopItemPrice) ? "&7Click to purchase!" : "&cNot enough money!");
                shopItem.getItemMeta().setLore(shopItemLore);

                //Saving an ID for each item
                shopItem.getItemMeta().getPersistentDataContainer().set(shopKey, PersistentDataType.INTEGER, j);

                //Displaying the item
                shopGUI.setItem(startingSlot+i, shopItem);
            }
        }

        //Displaying the next page button
        if(endIndex < itemList.size()){
            ItemStack nextPageButton = createButton(Material.ARROW, "&aNext Page ▶");
            shopGUI.setItem(53, nextPageButton);
        }

        //Displaying the previous page button
        if(page > 1){
            ItemStack previousPageButton = createButton(Material.ARROW, "&a◀ Previous Page");
            shopGUI.setItem(45, previousPageButton);
        }

        player.openInventory(shopGUI);
    }

    private ItemStack createButton(Material material, String displayName){
        ItemStack button = new ItemStack(material);
        ItemMeta buttonMeta = button.getItemMeta();
        buttonMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));


        button.setItemMeta(buttonMeta);
        return button;
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent event){

    }
}
