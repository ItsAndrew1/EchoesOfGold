//Developed by _ItsAndrew_
package me.andrew.halloweenEvent.GUIs;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import me.andrew.halloweenEvent.TreasureHunt;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;


public class RewardsChoiceGUI implements Listener{
    private final TreasureHunt plugin;

    //The add/remove rewards buttons
    private ItemStack removeButton;
    private ItemStack addButton;

    public RewardsChoiceGUI(TreasureHunt plugin){
        this.plugin = plugin;
    }

    public void showRewardsChoiceGUI(Player player){
        int rewardsChoiceSize = 54;
        Inventory rewardsChoice = Bukkit.createInventory(null, rewardsChoiceSize, "Choose reward option");

        //Decorations
        ItemStack blackGlassPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta bgpMeta = blackGlassPane.getItemMeta();
        for(int i = 0; i<=8; i++){
            if(i == 4) continue;

            bgpMeta.setDisplayName(" ");
            rewardsChoice.setItem(i, blackGlassPane);
        }
        for(int i = 45; i<54; i++){
            bgpMeta.setDisplayName(" ");
            rewardsChoice.setItem(i, blackGlassPane);
        }

        //InfoSign item
        ItemStack infoSign = new ItemStack(Material.OAK_SIGN);
        ItemMeta isMeta = infoSign.getItemMeta();
        isMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&f&lAdd Rewards &for &lRemove Rewards"));
        infoSign.setItemMeta(isMeta);
        rewardsChoice.setItem(4, infoSign);

        //Add rewards button
        //This strings is a value of a custom-head
        String base64AddButton = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTc3YmI2NmZjNzNhOTdjZWZjYjNhNGJmZGNjYjEyMjgxZjQ0ZGQzMjZjY2QwZmYzOWQ0N2U5ODViZmVmZjM0MyJ9fX0=";
        addButton = getHead(base64AddButton, ChatColor.translateAlternateColorCodes('&', "&a&lADD REWARDS"));
        rewardsChoice.setItem(20, addButton);

        //Remove rewards button
        //This string is a value of a custom-head
        String base64RemoveButton = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGU0YjhiOGQyMzYyYzg2NGUwNjIzMDE0ODdkOTRkMzI3MmE2YjU3MGFmYmY4MGMyYzViMTQ4Yzk1NDU3OWQ0NiJ9fX0=";
        removeButton = getHead(base64RemoveButton, ChatColor.translateAlternateColorCodes('&', "&4&lREMOVE REWARDS"));
        rewardsChoice.setItem(24, removeButton);

        //Return button
        ItemStack returnButton = new ItemStack(Material.SPECTRAL_ARROW);
        ItemMeta rbMeta = returnButton.getItemMeta();
        rbMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lRETURN"));
        returnButton.setItemMeta(rbMeta);
        rewardsChoice.setItem(40, returnButton);

        player.openInventory(rewardsChoice);
    }

    //Getting the custom material for the add/remove rewards buttons
    public static ItemStack getHead(String base64, String name){
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();

        headMeta.setDisplayName(name);
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.setProperty(new ProfileProperty("textures", base64));
        headMeta.setPlayerProfile(profile);
        head.setItemMeta(headMeta);

        return head;
    }

    @EventHandler
    public void onRewardsChoiceClick(InventoryClickEvent event){
        if(!(event.getWhoClicked() instanceof Player player)) return;
        if(!event.getView().getTitle().equalsIgnoreCase("Choose reward option")) return;

        event.setCancelled(true); //Doesn't let the player take any items from the GUI

        ItemStack clickedItem = event.getCurrentItem();
        if(clickedItem == null || clickedItem.getType().equals(Material.AIR)) return;

        //Returns if the player clicks on the decorations/infoSign
        if(clickedItem.getType().equals(Material.OAK_SIGN) || clickedItem.getType().equals(Material.BLACK_STAINED_GLASS_PANE)) return;

        ItemMeta clickedItemMeta = clickedItem.getItemMeta();
        if(clickedItemMeta == null) return;

        Sound clickSound = Registry.SOUNDS.get(NamespacedKey.minecraft("ui.button.click")); //Sound for button click

        //If the player clicks on return button
        Material returnButtonMat = Material.SPECTRAL_ARROW;
        if(clickedItem.getType().equals(returnButtonMat)){
            player.playSound(player.getLocation(), clickSound, 1f, 1f);
            plugin.getManageGUI().showMainManageGui(player);
            return;
        }

        //If the player clicks on add rewards button
        if(clickedItem.equals(addButton)){
            player.playSound(player.getLocation(), clickSound, 1f,1f);
            plugin.setTreasureManagerChoice("add rewards");
            plugin.getAllTreasuresGUI().showAllTreasuresGUI(player);
            return;
        }

        //If the player clicks on remove rewards button
        if(clickedItem.equals(removeButton)){
            player.playSound(player.getLocation(), clickSound, 1f, 1f);
            plugin.setTreasureManagerChoice("remove rewards");
            plugin.getAllTreasuresGUI().showAllTreasuresGUI(player);
        }
    }
}
