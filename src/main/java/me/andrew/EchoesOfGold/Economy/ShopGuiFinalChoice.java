package me.andrew.EchoesOfGold.Economy;

import me.andrew.EchoesOfGold.EchoesOfGold;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ShopGuiFinalChoice implements Listener {
    private final EchoesOfGold plugin;
    private final Map<UUID, String> selectedItemIDs = new HashMap<>();

    public ShopGuiFinalChoice(EchoesOfGold plugin) {
        this.plugin = plugin;
    }

    public void showGui(Player player, String itemID) {
        selectedItemIDs.put(player.getUniqueId(), itemID);

        //Creating the gui
        String guiTitle = "Are you sure?";
        int guiSize = 54;
        Inventory shopFinalChoice = Bukkit.createInventory(null, guiSize, guiTitle);

        //Getting the gui choice
        ShopGuiChoice guiChoice = plugin.getShopGUI().getGuiChoices().get(player.getUniqueId());

        //Displaying the 2 choice buttons
        String greenButtonDisplayName = ChatColor.translateAlternateColorCodes('&', guiChoice.equals(ShopGuiChoice.SHOP) ? "&a&lPurchase" : "&e&lRemove");
        ItemStack greenButton = createButton(Material.GREEN_CONCRETE, greenButtonDisplayName);
        shopFinalChoice.setItem(41, greenButton);

        ItemStack cancelButton = createButton(Material.RED_CONCRETE, ChatColor.translateAlternateColorCodes('&', "&c&lCancel"));
        shopFinalChoice.setItem(39, cancelButton);

        //Displaying the item
        ItemStack finalItem = plugin.getConfig().getItemStack("economy.shop-gui.items."+itemID+".item").clone();
        double finalItemPrice = plugin.getConfig().getDouble("economy.shop-gui.items."+itemID+".price");
        ItemMeta finalItemMeta = finalItem.getItemMeta();
        List<String> finalItemLore = finalItemMeta.hasLore() ? finalItemMeta.getLore() : new ArrayList<>();
        finalItemLore.add(" ");

        String loreLine;
        if(guiChoice.equals(ShopGuiChoice.SHOP)) loreLine = "&aPrice: &f"+finalItemPrice;
        else loreLine = "&eClick to remove the item.";
        finalItemLore.add(ChatColor.translateAlternateColorCodes('&', loreLine));
        finalItemMeta.setLore(finalItemLore);
        finalItem.setItemMeta(finalItemMeta);
        shopFinalChoice.setItem(22, finalItem);

        player.openInventory(shopFinalChoice);
    }

    private ItemStack createButton(Material material, String displayName){
        ItemStack button = new ItemStack(material);
        ItemMeta buttonMeta = button.getItemMeta();
        buttonMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        button.setItemMeta(buttonMeta);
        return button;
    }

    @EventHandler
    public void onGuiLeave(InventoryCloseEvent event){
        String guiTitle = "Are you sure?";
        if(!event.getView().getTitle().equals(guiTitle)) return;

        if(plugin.getShopGUI().getSwitchStances().remove(event.getPlayer().getUniqueId())) return;

        //Removing the player keys from the maps
        selectedItemIDs.remove(event.getPlayer().getUniqueId());
        plugin.getShopGUI().removePlayerFromMaps(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent event) {
        if(!(event.getWhoClicked() instanceof Player player)) return;
        if(!event.getView().getTitle().equals("Are you sure?")) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if(clickedItem == null) return;

        ItemMeta clickedItemMeta = clickedItem.getItemMeta();
        if(clickedItemMeta == null) return;

        //Getting the gui choice
        ShopGuiChoice guiChoice = plugin.getShopGUI().getGuiChoices().get(player.getUniqueId());

        //If the player clicks on the cancel button
        Material cancelButton = Material.RED_CONCRETE;
        if(clickedItem.getType() == cancelButton){
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            plugin.getShopGUI().getSwitchStances().add(player.getUniqueId());
            plugin.getShopGUI().showGUI(player, 1, guiChoice);
            return;
        }

        //Getting the item id
        String itemId = selectedItemIDs.get(player.getUniqueId());

        //If the player clicks on the purchase button
        Material greenButton = Material.GREEN_CONCRETE;
        if(clickedItem.getType() == greenButton){
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.4f);

            switch(guiChoice){
                case SHOP -> handleShopClick(player, itemId);
                case REMOVE_ITEM -> handleRemoveItemClick(player, itemId);
            }
        }
    }

    private void handleShopClick(Player player, String itemId){
        ItemStack itemToGive = plugin.getConfig().getItemStack("economy.shop-gui.items." + itemId + ".item");
        double itemPrice = plugin.getConfig().getDouble("economy.shop-gui.items." + itemId + ".price");
        player.getInventory().addItem(itemToGive);

        //Withdrawing from the player's balance
        plugin.getEconomyProvider().withdrawBalance(itemPrice, player);

        String chatMessage = plugin.getConfig().getString("economy.shop-gui.successful-purchase-message", "&aSuccessfully bought &l%item_name%&a!")
                .replace("%item_name%", itemToGive.getType().name())
                .replace("%item_price%", String.valueOf(itemPrice));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', chatMessage));
        player.closeInventory();
    }

    private void handleRemoveItemClick(Player player, String itemId){
        FileConfiguration mainConfig = plugin.getConfig();

        //Removing the item from config
        String pathToRemove = "economy.shop-gui.items." + itemId;
        mainConfig.set(pathToRemove, null);
        plugin.saveConfig();

        player.closeInventory();
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aItem with ID &e&l"+itemId+" &aremoved from the shop!"));
    }
}
