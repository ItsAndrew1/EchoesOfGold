//Developed by _ItsAndrew_
package me.andrew.EchoesOfGold.Economy;

import me.andrew.EchoesOfGold.EchoesOfGold;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ShopItem implements Listener {
    private final EchoesOfGold plugin;

    public ShopItem(EchoesOfGold plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if(!areEconomyAndShopItemWorking()) return;

        Player player = event.getPlayer();

        //Checking if the player is in a lobby world
        int hotbarSlot = plugin.getConfig().getInt("economy.shop-item.hotbar-slot", 2);
        if(hotbarSlot < 0 || hotbarSlot > 8) hotbarSlot = 2;
        ItemStack shopItem = createShopItem();

        if(!isPlayerInLobbyWorld(player)){
            if(player.getInventory().contains(shopItem)) player.getInventory().remove(shopItem);
            return;
        };

        PlayerInventory playerInv = player.getInventory();
        playerInv.setItem(hotbarSlot, shopItem);
    }

    @EventHandler
    public void onItemInteract(PlayerInteractEvent event) {
        //Checking if the item and economy are toggled
        if(!areEconomyAndShopItemWorking()) return;

        Player player = event.getPlayer();

        //Checking if the player is in a lobby world
        if(!isPlayerInLobbyWorld(player)) return;

        //Checking the item and item meta
        ItemStack interactItem = event.getItem();
        if(interactItem == null) return;

        ItemMeta interactMeta = interactItem.getItemMeta();
        if(interactMeta == null) return;

        //Checking the interactions
        Action playerInteraction = event.getAction();
        if(!playerInteraction.isLeftClick() && !playerInteraction.isRightClick()) return;
        if(event.getHand() != EquipmentSlot.HAND) return;

        //Opening the shop on the item interaction
        ItemStack shopItem = createShopItem();
        if(interactItem.equals(shopItem)) plugin.getShopGUI().showGUI(player, 1, ShopGuiChoice.SHOP);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if(!areEconomyAndShopItemWorking()) return;
        Player player = event.getPlayer();
        FileConfiguration mainConfig = plugin.getConfig();
        List<String> lobbyWorlds = mainConfig.getStringList("economy.shop-item.lobby-worlds");

        if(lobbyWorlds.contains(event.getFrom().getName()) && isPlayerInLobbyWorld(player)) return;
        if(!lobbyWorlds.contains(event.getFrom().getName()) && !isPlayerInLobbyWorld(player)) return;

        PlayerInventory playerInv = player.getInventory();
        if(lobbyWorlds.contains(event.getFrom().getName()) && !isPlayerInLobbyWorld(player)) {
            int hotbarSlot = mainConfig.getInt("economy.shop-item.hotbar-slot", 2);
            if(hotbarSlot < 0 || hotbarSlot > 8) hotbarSlot = 2;
            playerInv.setItem(hotbarSlot, null);
        }

        if(!lobbyWorlds.contains(event.getFrom().getName()) && isPlayerInLobbyWorld(player)) {
            ItemStack shopItem = createShopItem();
            int hotbarSlot = mainConfig.getInt("economy.shop-item.hotbar-slot", 2);
            if(hotbarSlot < 0 || hotbarSlot > 8) hotbarSlot = 2;
            playerInv.setItem(hotbarSlot, shopItem);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if(!areEconomyAndShopItemWorking()) return;
        Player player = event.getPlayer();

        //Checking if the player is one of the lobby worlds
        if(!isPlayerInLobbyWorld(player)) return;

        ItemStack shopItem = createShopItem();
        if(event.getItemDrop().getItemStack().equals(shopItem)) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInvClick(InventoryClickEvent event){
        if(!areEconomyAndShopItemWorking()) return;
        Player player = (Player) event.getWhoClicked();

        //Checking if the player is in one of the lobby worlds
        if(!isPlayerInLobbyWorld(player)) return;

        ItemStack clickedItem = event.getCurrentItem();
        if(clickedItem == null) return;

        ItemStack shopItem = createShopItem();
        if(event.getCurrentItem().equals(shopItem)) event.setCancelled(true);
    }

    private ItemStack createShopItem(){
        FileConfiguration mainConfig = plugin.getConfig();
        Material shopItemMaterial = Material.matchMaterial(mainConfig.getString("economy.shop-item.material", "EMERALD"));
        ItemStack shopItem = new ItemStack(shopItemMaterial);
        ItemMeta shopItemMeta = shopItem.getItemMeta();

        //Setting the display name
        String siDisplayItem = mainConfig.getString("economy.shop-item.display-item", "&6&lEVENT SHOP");
        shopItemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',  siDisplayItem));

        //Setting the lore
        List<String> coloredLore = new ArrayList<>();
        for(String loreLine : mainConfig.getStringList("economy.shop-item.lore")){
            coloredLore.add(ChatColor.translateAlternateColorCodes('&',  loreLine));
        }
        shopItemMeta.setLore(coloredLore);

        shopItem.setItemMeta(shopItemMeta);
        return shopItem;
    }

    private boolean isPlayerInLobbyWorld(Player player){
        return plugin.getConfig().getStringList("economy.shop-item.lobby-worlds").contains(player.getWorld().getName());
    }

    private boolean areEconomyAndShopItemWorking(){
        FileConfiguration mainConfig = plugin.getConfig();

        //Checking if the economy is toggled
        if(plugin.getEconomyProvider() == null){
            plugin.getLogger().warning("The economy is not enabled! Disabling shop item...");
            return false;
        }

        //Checking if the internal economy is toggled
        if(!mainConfig.getBoolean("economy.internal-economy.toggle", false)){
            plugin.getLogger().warning("The internal economy is not enabled! Disabling shop item...");
            return false;
        }

        //Checking if the shop item is toggled
        if(!mainConfig.getBoolean("economy.shop-item.toggle", false)){
            plugin.getLogger().warning("The shop item is not enabled! Disabling shop item...");
            return false;
        }

        return true;
    }
}
