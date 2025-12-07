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

    public void hintsGUI(Player player) {
        FileConfiguration books = plugin.getBooks().getConfig();
        FileConfiguration playerData = plugin.getPlayerData().getConfig();

        int foundCount = playerData.getInt("players." + player.getName() + ".treasures-found", 0);
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
            for(int i = 45; i<=53; i++){
                ItemStack decItem = new ItemStack(Material.matchMaterial(decorationItem.toUpperCase()));
                ItemMeta diMeta = decItem.getItemMeta();
                diMeta.setDisplayName(" ");
                decItem.setItemMeta(diMeta);

                hintsGUI.setItem(i, decItem);
            }
        }

        //If the info item is toggled, sets the item into it's designated slot
        boolean toggleInfoItem = plugin.getConfig().getBoolean("hints-gui.info-item-toggle");
        if(toggleInfoItem){
            int infoItemSlot = plugin.getConfig().getInt("hints-gui.info-item-slot");
            String stringInfoItemMaterial = plugin.getConfig().getString("hints-gui.info-item-material");
            String infoItemDisplayName = plugin.getConfig().getString("hints-gui.info-item-title");

            ItemStack infoItem = new ItemStack(Material.matchMaterial(stringInfoItemMaterial.toUpperCase()));
            ItemMeta iiMeta = infoItem.getItemMeta();
            iiMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', infoItemDisplayName));

            infoItem.setItemMeta(iiMeta);
            hintsGUI.setItem(infoItemSlot, infoItem);
        }

        //Exit button
        boolean toggleExitButton = plugin.getConfig().getBoolean("hints-gui.gui-exit-button.toggle");
        if(toggleExitButton){
            String exitButtonMaterialString = plugin.getConfig().getString("hints-gui.gui-exit-button.material");
            int exitButtonSlot = plugin.getConfig().getInt("hints-gui.gui-exit-button.slot");
            Material exitButtonMaterial = Material.matchMaterial(exitButtonMaterialString.toUpperCase());

            ItemStack exitButton = new ItemStack(exitButtonMaterial);
            ItemMeta exitButtonMeta = exitButton.getItemMeta();

            exitButtonMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.getConfig().getString("hints-gui.gui-exit-button.title"))));
            exitButton.setItemMeta(exitButtonMeta);
            hintsGUI.setItem(exitButtonSlot, exitButton);
        }

        //NoHints Item
        ConfigurationSection hints = books.getConfigurationSection("books");
        int nhiSlot = plugin.getConfig().getInt("hints-gui.gui-no-hints-item.slot");
        if(hints == null || hints.getKeys(false).isEmpty()){
            ItemStack noHintsItem = new ItemStack(Material.matchMaterial(plugin.getConfig().getString("hints-gui.gui-no-hints-item.material").toUpperCase()));
            ItemMeta nhiMeta = noHintsItem.getItemMeta();

            String nhiDisplayName = plugin.getConfig().getString("hints-gui.gui-no-hints-item.title");
            nhiMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', nhiDisplayName));
            noHintsItem.setItemMeta(nhiMeta);

            hintsGUI.setItem(nhiSlot, noHintsItem);
            player.openInventory(hintsGUI);
            return;
        }

        int slot = 10;
        for (String key : books.getConfigurationSection("books").getKeys(false)) {
            String path = "books." + key;

            //This makes the display of the hints more elegant :)
            if(slot == 17 || slot == 18 || slot == 26 || slot == 27 || slot == 35 || slot == 36 || slot == 44) continue;

            String titleBook = ChatColor.translateAlternateColorCodes('&', books.getString(path + ".title"));
            int unlockAfter = books.getInt(path + ".unlock-after", 0);

            ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
            BookMeta meta = (BookMeta) Bukkit.getItemFactory().getItemMeta(Material.WRITTEN_BOOK);

            meta.getPersistentDataContainer().set(this.key, PersistentDataType.STRING, key);

            //Setting the meta for each book
            if (foundCount >= unlockAfter) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', titleBook));
                meta.setAuthor(null);

                //Adding the lore
                List<String> coloredLore = new ArrayList<>();
                for(String loreLine : plugin.getConfig().getStringList("hints-gui.known-hint-lore")){
                    String coloredLoreLine = ChatColor.translateAlternateColorCodes('&', loreLine);
                    coloredLore.add(coloredLoreLine);
                }
                meta.setLore(coloredLore);
                book.setItemMeta(meta);
                hintsGUI.setItem(slot, book);
            }
            else {
                int remainingTreasures = unlockAfter - foundCount;
                ItemStack unknownHint = new ItemStack(Material.BOOK);
                ItemMeta uhMeta = unknownHint.getItemMeta();
                String displayName= plugin.getConfig().getString("hints-gui.unknown-hint-title");
                uhMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));

                //Adding the lore
                List<String> coloredLore = new ArrayList<>();
                for(String loreLine : plugin.getConfig().getStringList("hints-gui.unknown-hint-lore")){
                    String coloredLoreLine = ChatColor.translateAlternateColorCodes('&', loreLine);
                    coloredLore.add(coloredLoreLine);
                }
                coloredLore.add(ChatColor.translateAlternateColorCodes('&', "&7Find &l"+remainingTreasures+" &7more treasure(s) to unlock this hint!"));
                uhMeta.setLore(coloredLore);
                unknownHint.setItemMeta(uhMeta);
                hintsGUI.setItem(slot, unknownHint);
            }
            slot++;
        }

        player.openInventory(hintsGUI);
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

        //If the player clicks on an Unknown Hint
        Material unknownHintMat = Material.BOOK;
        if(clicked.getType().equals(unknownHintMat)){
            String stringSound = plugin.getConfig().getString("player-clicked-on-unknown-hint-sound").toLowerCase();
            Sound unknownHintSound = Registry.SOUNDS.get(NamespacedKey.minecraft(stringSound));
            float uhsVolume = plugin.getConfig().getInt("pcouhs-volume");
            float uhsPitch = plugin.getConfig().getInt("pcouhs-pitch");

            player.playSound(player.getLocation(), unknownHintSound, uhsVolume, uhsPitch);
            return;
        }

        //Check if the clicked item is one of the book Hints
        String bookId = meta.getPersistentDataContainer().get(this.key, PersistentDataType.STRING);
        if (bookId == null) return;

        FileConfiguration books = plugin.getBooks().getConfig();
        FileConfiguration playerData = plugin.getPlayerData().getConfig();

        int foundCount = playerData.getInt("players." + player.getName() + ".treasures-found", 0);
        String path = "books." + bookId;
        int unlockAfter = books.getInt(path + ".unlock-after", 0);

        if (foundCount < unlockAfter) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("hint-not-found-yet")));
            player.closeInventory();
            return;
        }

        BookMeta realMeta = (BookMeta) Bukkit.getItemFactory().getItemMeta(Material.WRITTEN_BOOK);
        realMeta.setTitle(ChatColor.translateAlternateColorCodes('&', books.getString(path + ".title")));
        for (String page : books.getStringList(path + ".pages")) {
            realMeta.addPage(ChatColor.translateAlternateColorCodes('&', page));
        }

        ItemStack realBook = new ItemStack(Material.WRITTEN_BOOK);
        realBook.setItemMeta(realMeta);

        player.closeInventory();

        String openHintSound = plugin.getConfig().getString("hint-open-sound");
        float ohsVolume = plugin.getConfig().getInt("hos-volume");
        float ohsPitch = plugin.getConfig().getInt("hos-pitch");
        Sound openHint = Registry.SOUNDS.get(NamespacedKey.minecraft(openHintSound.toLowerCase()));

        player.playSound(player.getLocation(), openHint, ohsVolume, ohsPitch);
        player.openBook(realBook);
    }
}