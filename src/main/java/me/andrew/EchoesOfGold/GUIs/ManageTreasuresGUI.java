//Developed by _ItsAndrew_
package me.andrew.EchoesOfGold.GUIs;

import me.andrew.EchoesOfGold.EchoesOfGold;
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
    private final EchoesOfGold plugin;

    public ManageTreasuresGUI(EchoesOfGold plugin){
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
        List<String> coloredInfoSignLore = new ArrayList<>();
        coloredInfoSignLore.add(ChatColor.translateAlternateColorCodes('&', ""));
        coloredInfoSignLore.add(ChatColor.translateAlternateColorCodes('&', "&7 - &lcreate &7treasures"));
        coloredInfoSignLore.add(ChatColor.translateAlternateColorCodes('&', "&7 - &ldelete &7treasures"));
        coloredInfoSignLore.add(ChatColor.translateAlternateColorCodes('&', "&7 - &lsetWorld &7for treasures"));
        coloredInfoSignLore.add(ChatColor.translateAlternateColorCodes('&', "&7 - &lsetLocation &7for treasures"));
        coloredInfoSignLore.add(ChatColor.translateAlternateColorCodes('&', "&7 - &lsetFacing &7for treasures"));

        ItemStack infoSignItem = createButton(Material.OAK_SIGN, ChatColor.translateAlternateColorCodes('&', "&f&lMANAGE YOUR TREASURES: "), coloredInfoSignLore);
        treasureManagerInv.setItem(infoSignSlot, infoSignItem);

        //Create treasures button
        int createTreasureSlot = 10;
        List<String> createButtonLore = new ArrayList<>();
        createButtonLore.add(ChatColor.translateAlternateColorCodes('&', "&7Create a new treasure!"));

        ItemStack createTreasureButton = createButton(Material.SLIME_BALL, ChatColor.translateAlternateColorCodes('&', "&a&lCREATE TREASURE"), createButtonLore);
        treasureManagerInv.setItem(createTreasureSlot, createTreasureButton);

        //Delete treasures button
        int deleteTreasuresSlot = 12;
        List<String> deleteButtonLore = new ArrayList<>();
        deleteButtonLore.add(ChatColor.translateAlternateColorCodes('&', "&7Delete one of the treasures!"));

        ItemStack deleteTreasureButton = createButton(Material.BARRIER, ChatColor.translateAlternateColorCodes('&', "&4&lDELETE TREASURE"), deleteButtonLore);
        treasureManagerInv.setItem(deleteTreasuresSlot, deleteTreasureButton);

        //SetLocation button
        int setLocationSlot = 14;
        List<String> locationButtonLore = new ArrayList<>();
        locationButtonLore.add(ChatColor.translateAlternateColorCodes('&', "&7Set the &llocation &7of a treasure!"));

        ItemStack setLocationButton = createButton(Material.COMPASS, ChatColor.translateAlternateColorCodes('&', "&5&lSET TREASURE LOCATION"), locationButtonLore);
        treasureManagerInv.setItem(setLocationSlot, setLocationButton);

        //Set Coins button
        int setCoinsButtonSlot = 22;
        List<String> setCoinsLore = new ArrayList<>();
        setCoinsLore.add(ChatColor.translateAlternateColorCodes('&', "&7Set the &lcoins &7for a treasure!"));
        setCoinsLore.add(" ");
        setCoinsLore.add(ChatColor.translateAlternateColorCodes('&', "&cNote that you need the &lECONOMY &ctoggled &lON!"));

        ItemStack setCoinsButton = createButton(Material.GOLD_INGOT, ChatColor.translateAlternateColorCodes('&', "&6&lSET TREASURE COINS"), setCoinsLore);
        treasureManagerInv.setItem(setCoinsButtonSlot,  setCoinsButton);

        //SetWorld button
        int setWorldSlot = 16;
        List<String> worldButtonLore = new ArrayList<>();
        worldButtonLore.add(ChatColor.translateAlternateColorCodes('&', "&7Set the &lworld &7for a treasure!"));
        worldButtonLore.add(" ");
        worldButtonLore.add(ChatColor.translateAlternateColorCodes('&', "&e&lTIP: &eLook for the world name in your server's folder."));

        ItemStack setWorldButton = createButton(Material.MAP, ChatColor.translateAlternateColorCodes('&', "&b&lSET TREASURE WORLD"), worldButtonLore);
        treasureManagerInv.setItem(setWorldSlot, setWorldButton);

        //SetFacing button
        int setFacingSlot = 20;
        List<String> coloredSfbLore = new ArrayList<>(); //Adding lore
        coloredSfbLore.add(ChatColor.translateAlternateColorCodes('&', "&7Set the &lfacing &7for a treasure!"));
        coloredSfbLore.add(ChatColor.translateAlternateColorCodes('&', "&e&lTIP: &eValues may be 'NORTH', 'SOUTH', 'NORTH_EAST', etc."));

        ItemStack setFacingButton = createButton(Material.GLOW_ITEM_FRAME, ChatColor.translateAlternateColorCodes('&', "&9&lSET TREASURE FACING"), coloredSfbLore);
        treasureManagerInv.setItem(setFacingSlot, setFacingButton);

        //SetHint button
        int setHintSlot = 24;
        List<String> setHintLore = new ArrayList<>();
        setHintLore.add(ChatColor.translateAlternateColorCodes('&', "&7Set the &lhint &7for a treasure!"));
        setHintLore.add(" ");
        setHintLore.add(ChatColor.translateAlternateColorCodes('&', "&cNote that you need &lhints &ctoggled!"));

        ItemStack setHintButton = createButton(Material.WRITABLE_BOOK, ChatColor.translateAlternateColorCodes('&', "&2&lSET TREASURE HINT"), setHintLore);
        treasureManagerInv.setItem(setHintSlot, setHintButton);

        //Return button
        int returnButtonSlot = 40;
        ItemStack returnButton = createButton(Material.SPECTRAL_ARROW, ChatColor.translateAlternateColorCodes('&', "&c&lRETURN"), null);
        treasureManagerInv.setItem(returnButtonSlot, returnButton);

        player.openInventory(treasureManagerInv);
    }

    private ItemStack createButton(Material material, String displayName, List<String> lore){
        ItemStack button = new ItemStack(material);
        ItemMeta buttonMeta = button.getItemMeta();
        buttonMeta.setDisplayName(displayName);

        //Setting the lore if it's not null
        if(lore != null) buttonMeta.setLore(lore);

        button.setItemMeta(buttonMeta);
        return button;
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

        //The chat prefix
        String chatPrefix = plugin.getConfig().getString("chat-prefix");

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

        //If the player clicks on the setCoins button
        Material setCoinsButton = Material.GOLD_INGOT;
        if(clickedItem.getType() == setCoinsButton){
            //Checking if the economy is toggled or if it is working properly
            boolean toggleEconomy = plugin.getConfig().getBoolean("economy.toggle-using-economy", false);
            if(!toggleEconomy){
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cYou cannot do that because the economy is &ldisabled &cor it's not working properly!"));
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);

                openGuiAgain(player);
                return;
            }

            player.playSound(player.getLocation(), clickButtonSound, 1f, 1f);
            plugin.setTreasureManagerChoice("set coins");
            plugin.getAllTreasuresGUI().showAllTreasuresGUI(player);
        }

        //If the player clicks on setFacing button
        Material setFacingButton = Material.GLOW_ITEM_FRAME;
        if(clickedItem.getType() == setFacingButton){
            player.playSound(player.getLocation(), clickButtonSound, 1f, 1f);
            plugin.setTreasureManagerChoice("set facing");
            plugin.getAllTreasuresGUI().showAllTreasuresGUI(player);
        }

        //If the player clicks on setHint Button
        Material setHintButton = Material.WRITABLE_BOOK;
        if(clickedItem.getType() == setHintButton){
            //Checking if the hints are toggled
            boolean toggleHints = plugin.getConfig().getBoolean("hints-gui.toggle-hints", true);
            if(!toggleHints || !isEconomyWorking()){
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cYou cannot do that since you have the hints &ldisabled&c!"));

                openGuiAgain(player);
                return;
            }

            player.playSound(player.getLocation(), clickButtonSound, 1f, 1f);
            plugin.setTreasureManagerChoice("set hint");
            plugin.getAllTreasuresGUI().showAllTreasuresGUI(player);
        }
    }

    private boolean isEconomyWorking(){
        FileConfiguration mainConfig = plugin.getConfig();

        //Checking if the economy is toggled
        boolean toggleEconomy = mainConfig.getBoolean("economy.toggle-using-economy");
        if(!toggleEconomy) return false;

        //Checking if the economy provider isn't null
        return plugin.getEconomy() != null;
    }

    //Helper method for creating and setting a treasure's data in 'treasures.yml'
    private void createTreasure(Player player) {
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
            treasures.set(path + ".coins", "");
            treasures.set(path + ".title", treasureName);
            treasures.set(path + ".facing", "");
            treasures.set(path + ".world", "");
            treasures.set(path + ".x", "");
            treasures.set(path + ".y", "");
            treasures.set(path + ".z", "");

            //Also setting the hint if they are toggled
            boolean toggleHints = plugin.getConfig().getBoolean("hints-gui.toggle-hints", true);
            if(toggleHints) treasures.set(path + ".hints", "");

            plugin.getTreasures().saveConfig();

            Sound successSound = Registry.SOUNDS.get(NamespacedKey.minecraft("entity.player.levelup"));
            player.playSound(player.getLocation(), successSound, 1f, 1.4f);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSaved treasure &l"+treasureName+"&a!"));

            openGuiAgain(player);
        });
    }

    //Helper method for opening the Main Manage GUI after 1/2 secs
    private void openGuiAgain(Player player){
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getManageGUI().showMainManageGui(player);
        }, 10L);
    }
}
