package me.andrew.halloweenEvent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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

import java.util.List;

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

        if (!books.isConfigurationSection("books")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("hints-no-books-configured")));
            return;
        }

        int foundCount = playerData.getInt("players." + player.getName() + ".treasures-found", 0);
        int size = ((books.getConfigurationSection("books").getKeys(false).size() - 1) / 9 + 1) * 9;
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("hints-gui-title"));
        Inventory inv = Bukkit.createInventory(null, size, title);

        for (String key : books.getConfigurationSection("books").getKeys(false)) {
            String path = "books." + key;
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
                book.setType(Material.WRITTEN_BOOK);
                meta.setDisplayName(ChatColor.DARK_GRAY + "???");
                meta.setLore(List.of(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("hints-locked-lore")), ChatColor.GRAY + "Find " + unlockAfter + " treasures to unlock"));
            }

            book.setItemMeta(meta);
            inv.addItem(book);
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("hints-gui-title"));
        if (!event.getView().getTitle().equals(title)) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;

        String bookId = meta.getPersistentDataContainer().get(this.key, PersistentDataType.STRING);
        if (bookId == null) return;

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