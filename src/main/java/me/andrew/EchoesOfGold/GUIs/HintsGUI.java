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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HintsGUI implements Listener {
    private final EchoesOfGold plugin;
    private final NamespacedKey key;

    public HintsGUI(EchoesOfGold plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "hint-id");
    }

    public void hintsGUI(Player player, int hintCount) {
        FileConfiguration playerData = plugin.getPlayerData().getConfig();
        FileConfiguration treasureData = plugin.getTreasures().getConfig();

        int invSize = plugin.getHintsGUISize();
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("hints-gui.gui-title"));
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

        //Starting slot
        int slot = toggleDecoration ? 9 : 0;

        //Displaying the hints
        ConfigurationSection treasures = treasureData.getConfigurationSection("treasures");
        List<String> treasuresList = treasures.getKeys(false).stream().toList();
        for(int i = hintCount; i<=treasuresList.size(); i++){
            //Getting the data of the hint
            String treasure = treasuresList.get(i);
            String hintDisplayName = treasures.getString("treasures." + treasure + ".hint.title");
            List<String> hintPages = treasures.getStringList("treasures." + treasure + ".hint.pages");

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
                    BookMeta unlockedHintMeta = (BookMeta) unlockedHintItem.getItemMeta();

                    unlockedHintMeta.setTitle(ChatColor.translateAlternateColorCodes('&', hintDisplayName));
                }
            }

            slot++;
            hintCount++;
        }

        player.openInventory(hintsGUI);
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

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("hints-gui.gui-title"));
        if (!event.getView().getTitle().equals(title)) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;

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

        //If the player clicks on noHintsItem
        String noHintsItemMaterialString = plugin.getConfig().getString("hints-gui.gui-no-hints-item.material");
        Material noHintsItemMaterial = Material.matchMaterial(noHintsItemMaterialString.toUpperCase());
        if(clicked.getType().equals(noHintsItemMaterial)) return;


    }
}