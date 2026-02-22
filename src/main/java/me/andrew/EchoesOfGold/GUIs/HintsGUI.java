//Developed by _ItsAndrew_
package me.andrew.EchoesOfGold.GUIs;

import me.andrew.EchoesOfGold.EchoesOfGold;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class HintsGUI implements Listener {
    private final EchoesOfGold plugin;
    private final NamespacedKey key;

    public HintsGUI(EchoesOfGold plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "hint-id");
    }

    public void hintsGUI(Player player, int page) {
        FileConfiguration playerData = plugin.getPlayerData().getConfig();
        FileConfiguration treasureData = plugin.getTreasures().getConfig();

        int invSize = plugin.getHintsGUISize();
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("hints-gui.gui-title") + "(Page "+page+")");
        Inventory hintsGUI = Bukkit.createInventory(null, invSize, title);

        //If the decoration is toggled, sets the decoration
        boolean toggleDecoration = plugin.getConfig().getBoolean("hints-gui.toggle-decorations");
        if(toggleDecoration){
            String decorationItem = plugin.getConfig().getString("hints-gui.decoration-material");
            for(int i = 0; i<=8; i++){
                //Check if the info item is toggled and skips it's slot
                boolean toggleInfoItem = plugin.getConfig().getBoolean("hints-gui.info-item-toggle");
                int infoItemSlot = plugin.getConfig().getInt("hints-gui.info-item-slot");
                if(toggleInfoItem){
                    if(i == infoItemSlot) continue;
                }

                ItemStack decItem = new ItemStack(Material.matchMaterial(decorationItem.toUpperCase()));
                ItemMeta diMeta = decItem.getItemMeta();
                diMeta.setDisplayName(" ");
                decItem.setItemMeta(diMeta);

                hintsGUI.setItem(i, decItem);
            }
        }

        //If the info item is toggled, sets it into it's designated slot
        boolean toggleInfoItem = plugin.getConfig().getBoolean("hints-gui.info-item.toggle");
        if(toggleInfoItem){
            int infoItemSlot = plugin.getConfig().getInt("hints-gui.info-item.slot");
            String stringInfoItemMaterial = plugin.getConfig().getString("hints-gui.info-item.material");
            String infoItemDisplayName = plugin.getConfig().getString("hints-gui.info-item.title");

            ItemStack infoItem = new ItemStack(Material.matchMaterial(stringInfoItemMaterial.toUpperCase()));
            ItemMeta iiMeta = infoItem.getItemMeta();
            iiMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', infoItemDisplayName));

            infoItem.setItemMeta(iiMeta);
            hintsGUI.setItem(infoItemSlot, infoItem);
        }

        //NoHints Item
        int nhiSlot = plugin.getConfig().getInt("hints-gui.gui-no-hints-item.slot");
        if(!thereAreHints()){
            ItemStack noHintsItem = new ItemStack(Material.matchMaterial(plugin.getConfig().getString("hints-gui.gui-no-hints-item.material").toUpperCase()));
            ItemMeta nhiMeta = noHintsItem.getItemMeta();

            String nhiDisplayName = plugin.getConfig().getString("hints-gui.gui-no-hints-item.title");
            nhiMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', nhiDisplayName));
            noHintsItem.setItemMeta(nhiMeta);

            hintsGUI.setItem(nhiSlot, noHintsItem);
            player.openInventory(hintsGUI);
            return;
        }

        int slot = toggleDecoration ? 9 : 0;
        int numberOfHints = getNumberOfHints();
        int maximumNrOfHintsPerPage = toggleDecoration ? 36 : 45;
        int offset = (page - 1) * maximumNrOfHintsPerPage;
        int endIndex = Math.min(offset + maximumNrOfHintsPerPage, numberOfHints);

        //Displaying the hints
        ConfigurationSection treasures = treasureData.getConfigurationSection("treasures");
        List<String> treasuresList = treasures.getKeys(false).stream().toList();
        for(int i = 0, j = offset; i<45; i++, j++){
            //Getting the data of the hint
            String treasure = treasuresList.get(j);
            String hintDisplayName = treasures.getString("treasures." + treasure + ".hint.title");

            for(String p : playerData.getConfigurationSection("players").getKeys(false)){
                boolean treasureFound = playerData.getBoolean("players." + p + ".found."+treasure);

                //If the player found the treasure, it sets it as locked
                if(!treasureFound){
                    ItemStack lockedHintItem = new ItemStack(Material.BOOK);
                    ItemMeta lockedHintMeta = lockedHintItem.getItemMeta();

                    //Setting the display name
                    lockedHintMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', hintDisplayName));

                    //Setting the lore
                    List<String> lockedHintLore = new ArrayList<>();
                    for(String line : plugin.getConfig().getStringList("hints-gui.locked-hint-lore")) lockedHintLore.add(ChatColor.translateAlternateColorCodes('&', line));
                    lockedHintMeta.setLore(lockedHintLore);

                    lockedHintItem.setItemMeta(lockedHintMeta);
                    hintsGUI.setItem(slot, lockedHintItem);
                }

                //Else, we show the normal hint
                else{
                    ItemStack unlockedHintItem = new ItemStack(Material.WRITTEN_BOOK);
                    ItemMeta unlockedHintMeta = unlockedHintItem.getItemMeta();

                    //Setting the display name
                    unlockedHintMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', hintDisplayName));

                    //Setting the lore
                    List<String> unlockedHintLore = new ArrayList<>();
                    for(String line : plugin.getConfig().getStringList("hints-gui.unlocked-hint-lore")) unlockedHintLore.add(ChatColor.translateAlternateColorCodes('&', line));
                    unlockedHintMeta.setLore(unlockedHintLore);

                    //Saving the ID of the treasure to the meta
                    unlockedHintMeta.getPersistentDataContainer().set(this.key, PersistentDataType.STRING, treasure);

                    //Adding the lore item to the GUI
                    unlockedHintItem.setItemMeta(unlockedHintMeta);
                    hintsGUI.setItem(slot+i, unlockedHintItem);
                }
            }

            slot++;
        }

        //Adding the next page button
        if(endIndex < numberOfHints){
            ItemStack nextPageItem = new ItemStack(Material.ARROW);
            ItemMeta nextPageMeta = nextPageItem.getItemMeta();
            nextPageMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a▶ Next Page"));
            nextPageItem.setItemMeta(nextPageMeta);
            hintsGUI.setItem(53, nextPageItem);
        }

        //Adding the Previous Page Button
        if(page > 1){
            ItemStack previousPageItem = new ItemStack(Material.ARROW);
            ItemMeta previousPageMeta = previousPageItem.getItemMeta();
            previousPageMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a◀ Previous Page"));
            previousPageItem.setItemMeta(previousPageMeta);
            hintsGUI.setItem(45, previousPageItem);
        }

        player.openInventory(hintsGUI);
    }

    private int getNumberOfHints(){
        FileConfiguration treasures = plugin.getTreasures().getConfig();

        int numberOfHints = 0;
        for(String treasure : treasures.getConfigurationSection("treasures").getKeys(false)){
            String mainPath = "treasures."+treasure;
            ConfigurationSection treasureHint = treasures.getConfigurationSection(mainPath+".hint");

            if(treasureHint != null) numberOfHints++;
        }

        return numberOfHints;
    }

    private boolean thereAreHints(){
        FileConfiguration treasures = plugin.getTreasures().getConfig();
        ConfigurationSection treasuresSection = treasures.getConfigurationSection("treasures");

        if(treasuresSection == null) return false;

        for(String key : treasuresSection.getKeys(false)) {
            ConfigurationSection treasureHint = treasures.getConfigurationSection("treasures."+key+".hint");
            if(treasureHint != null) return true;
        }

        return false;
    }

    private int getPageNr(String title){
        String parsedTitle = title.replaceAll("[^0-9]", "");
        return Integer.parseInt(parsedTitle);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("hints-gui.gui-title"));
        if (!event.getView().getTitle().contains(title)) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ItemMeta clickedMeta = clicked.getItemMeta();
        if (clickedMeta == null) return;

        //If the player clicks on exitButton
        String exitButtonMaterialString = plugin.getConfig().getString("hints-gui.gui-exit-button.material");
        Material exitButtonMaterial = Material.matchMaterial(exitButtonMaterialString.toUpperCase());
        if(clicked.getType() == exitButtonMaterial){
            String exitButtonSoundString = plugin.getConfig().getString("exit-button-sound");
            float ebsVolume = plugin.getConfig().getInt("ebs-volume");
            float ebsPitch = plugin.getConfig().getInt("ebs-pitch");
            NamespacedKey exitButtonSoundCheck = NamespacedKey.minecraft(exitButtonSoundString.toLowerCase());
            Sound exitButtonSound = Registry.SOUNDS.get(exitButtonSoundCheck);

            player.playSound(player.getLocation(), exitButtonSound, ebsVolume, ebsPitch);
            player.closeInventory();
            return;
        }

        //If the player clicks on Previous Page Button
        if(clickedMeta.getDisplayName().contains("Previous Page")){
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            hintsGUI(player, getPageNr(event.getView().getTitle()) - 1);
            return;
        }

        //If the player clicks on Next Page Button
        if(clickedMeta.getDisplayName().contains("Next Page")){
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            hintsGUI(player, getPageNr(event.getView().getTitle()) + 1);
        }

        //Getting the clicked treasure of the hint from the clicked meta
        String treasure = clickedMeta.getPersistentDataContainer().get(this.key, PersistentDataType.STRING);

        //Building the book
        FileConfiguration treasures = plugin.getTreasures().getConfig();
        BookMeta bookMeta = (BookMeta) clicked.getItemMeta();

        String bookTitle = treasures.getString("treasures."+treasure+".hint.title");
        List<String> bookPages = treasures.getStringList("treasures."+treasure+".hint.pages");

        //Setting the title and pages
        bookMeta.setTitle(bookTitle);
        bookMeta.setPages(bookPages);

        //Opening the book
        clicked.setItemMeta(bookMeta);

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        player.closeInventory();
        player.openBook(bookMeta);
    }
}