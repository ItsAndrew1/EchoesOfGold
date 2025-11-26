//Developed by _ItsAndrew_
package me.andrew.halloweenEvent;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Hints implements Listener {
    private final TreasureHunt plugin;
    private final NamespacedKey key;

    public Hints(TreasureHunt plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "hint-id");
    }

    public void hintsGUI(Player player) {
        FileConfiguration books = plugin.getBooks().getConfig();
        FileConfiguration playerData = plugin.getPlayerData().getConfig();

        int foundCount = playerData.getInt("players." + player.getName() + ".treasures-found", 0);
        int invSize = plugin.getGuiSize();
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("hints-gui.gui-title"));
        Inventory inv = Bukkit.createInventory(null, invSize, title);

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
            inv.setItem(exitButtonSlot, exitButton);
        }

        //NoHints Item
        boolean toggleNoHintsItem = plugin.getConfig().getBoolean("hints-gui.gui-no-hints-item.toggle");
        if(toggleNoHintsItem && !books.isConfigurationSection("books")){
            String noHintsItemMaterialString = plugin.getConfig().getString("hints-gui.gui-no-hints-item.material");
            int noHintsItemSlot = plugin.getConfig().getInt("hints-gui.gui-no-hints-item.slot");
            Material noHintsItemMaterial = Material.matchMaterial(noHintsItemMaterialString.toUpperCase());

            ItemStack noHintsItem = new ItemStack(noHintsItemMaterial);
            ItemMeta noHintsItemMeta = noHintsItem.getItemMeta();

            List<String> noHintsItemLore = plugin.getConfig().getStringList("hints-gui.gui-no-hints-item.lore");
            List<String> coloredNoHintsItemLore = new ArrayList<>();
            if(noHintsItemLore.isEmpty()){
                noHintsItemMeta.setLore(null);
                return;
            }

            for(String rawLine : noHintsItemLore){
                String coloredLine = ChatColor.translateAlternateColorCodes('&', rawLine);
                coloredNoHintsItemLore.add(coloredLine);
            }
            noHintsItemMeta.setLore(coloredNoHintsItemLore);
            noHintsItem.setItemMeta(noHintsItemMeta);
            inv.setItem(noHintsItemSlot, noHintsItem);
        }

        if(books.isConfigurationSection("books")){
            for (String key : books.getConfigurationSection("books").getKeys(false)) {
                String path = "books." + key;
                int bookSlot = books.getInt(path+".gui-slot");
                String titleBook = ChatColor.translateAlternateColorCodes('&', books.getString(path + ".title"));
                int unlockAfter = books.getInt(path + ".unlock-after", 0);

                ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
                ItemMeta meta = book.getItemMeta();

                meta.getPersistentDataContainer().set(this.key, PersistentDataType.STRING, key);

                if (foundCount >= unlockAfter) {
                    meta.setDisplayName(ChatColor.GOLD + titleBook);
                    meta.setLore(List.of(ChatColor.GREEN + "✔ Unlocked"));
                }
                else {
                    book.setType(Material.BOOK);
                    meta.setDisplayName(ChatColor.DARK_GRAY + "???");
                    meta.setLore(List.of(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("hints-locked-lore")), ChatColor.GRAY + "Find " + unlockAfter + " treasures to unlock"));
                }

                book.setItemMeta(meta);
                inv.setItem(bookSlot, book);
            }
        }

        player.openInventory(inv);
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

        String bookId = meta.getPersistentDataContainer().get(this.key, PersistentDataType.STRING);
        if (bookId == null) return;

        //If the player clicks on exitButton
        String exitButtonMaterialString = plugin.getConfig().getString("hints-gui.gui-exit-button.material");
        Material exitButtonMaterial = Material.matchMaterial(exitButtonMaterialString.toUpperCase());
        if(clicked.getType().equals(exitButtonMaterial)){
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
        if(clicked.getType().equals(noHintsItemMaterial)){
            String noHintsItemSoundString = plugin.getConfig().getString("no-hints-item-sound");
            float nhisVolume = plugin.getConfig().getInt("nhis-volume");
            float nhisPitch = plugin.getConfig().getInt("nhis-pitch");
            NamespacedKey nhisCheck = NamespacedKey.minecraft(noHintsItemSoundString.toLowerCase());
            Sound noHintsItemSound = Registry.SOUNDS.get(nhisCheck);

            String nhiChatMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("hints-gui.gui-no-hints-item.chat-message"));
            player.sendMessage(nhiChatMessage);
            player.playSound(player.getLocation(), noHintsItemSound, nhisVolume, nhisPitch);
            player.closeInventory();
            return;
        }

        FileConfiguration books = plugin.getBooks().getConfig();
        FileConfiguration playerData = plugin.getPlayerData().getConfig();

        int foundCount = playerData.getInt("players." + player.getName() + ".treasures-found", 0);
        String path = "books." + bookId;
        int unlockAfter = books.getInt(path + ".unlock-after", 0);

        if (foundCount < unlockAfter) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("hint-not-found-yet")));
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
        player.openBook(realBook);
    }
}