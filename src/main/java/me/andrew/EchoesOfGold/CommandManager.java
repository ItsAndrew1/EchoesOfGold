//Developed by _ItsAndrew_
package me.andrew.EchoesOfGold;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandManager implements CommandExecutor{
    EchoesOfGold plugin;

    public CommandManager(EchoesOfGold plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        FileConfiguration books = plugin.getBooks().getConfig();
        Player player = (Player) commandSender;
        String chatPrefix = plugin.getConfig().getString("chat-prefix");

        //Defining the sounds
        Sound goodValue = Registry.SOUNDS.get(NamespacedKey.minecraft("entity.player.levelup"));
        Sound invalidValue = Registry.SOUNDS.get(NamespacedKey.minecraft("entity.enderman.teleport"));

        if(command.getName().equalsIgnoreCase("eog")){
            if(!commandSender.hasPermission("eog.admin")){
                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cYou don't have permission to run this command!"));
                player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                return true;
            }
            if(strings.length == 0){
                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cUsage: &l/treasurehunt <enable | disable | treasures | reload | hints | help>"));
                player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                return true;
            }

            switch(strings[0].toLowerCase()){
                case "enable":
                    if(plugin.eventActive){
                        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cThe game is &lalready active&c!"));
                        player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                        break;
                    }
                    else{
                        plugin.eventActive = true;

                        //Gets all the details about the start-event-sound. Prints an error if the sound is invalid
                        String startEventSoundString = plugin.getConfig().getString("start-event-sound");
                        float startEventSoundVolume = plugin.getConfig().getInt("ses-volume");
                        float startEventSoundPitch = plugin.getConfig().getInt("ses-pitch");
                        Sound startEventSound;
                        try{
                            NamespacedKey checkStartEventSound = NamespacedKey.minecraft(startEventSoundString.toLowerCase());
                            startEventSound = Registry.SOUNDS.get(checkStartEventSound);
                        } catch (Exception e){
                            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cThere is a problem with &lstart-event-sound &c!"));
                            Bukkit.getLogger().warning("[TREASUREHUNT] "+e.getMessage());
                            return true;
                        }

                        //Gets all the players and opens the book after openBookDelay seconds
                        getPlayers();
                        long openBookDelay = plugin.getConfig().getInt("start-book.delay") * 20L;
                        for(Player p : Bukkit.getOnlinePlayers()){
                            teleportPlayers(p);
                            p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                            p.sendTitle(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("title")), ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("subtitle")), 10, 60, 20);

                            new BukkitRunnable(){
                                @Override
                                public void run(){
                                    openStartBook(p);
                                }
                            }.runTaskLater(plugin, openBookDelay);
                            p.playSound(p.getLocation(), startEventSound, startEventSoundVolume, startEventSoundPitch);
                        }
                        plugin.getTreasureManager().spawnTreasures();
                        plugin.getTreasureManager().spawnChestParticles();

                        //Starts the duration
                        long duration = parseDuration(plugin.getConfig().getString("bar-timer"));
                        long endTime = System.currentTimeMillis() + duration;
                        plugin.getConfig().set("duration", endTime);
                        plugin.saveConfig();

                        //If the value of boss-bar is true, spawns the boss bar with the timer.
                        if(plugin.getConfig().getString("boss-bar").equalsIgnoreCase("true")){
                            plugin.getBossBar().startBossBar(duration);
                        }

                        //If the value of scoreboard is true, it shows up on the screen.
                        if(plugin.getConfig().getString("scoreboard").equalsIgnoreCase("true")){
                            plugin.getScoreboardManager().updateScoreboard();
                        }
                    }
                    break;

                case "disable":
                    if(!plugin.eventActive){
                        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cThe game is &lalready disabled&c!"));
                        player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                        return true;
                    }
                    for(Player p : Bukkit.getOnlinePlayers()){
                        plugin.getScoreboardManager().stopScoreboard(p);
                    }
                    plugin.getTreasureManager().removeTreasures();
                    plugin.getTreasureManager().cancelAllTreasureParticles();
                    plugin.getPlayerData().getConfig().set("players", null);
                    plugin.getPlayerData().saveConfig();
                    plugin.getBossBar().stop();
                    Bukkit.getLogger().info("[TREASUREHUNT] Treasure Hunt successfully disabled!");
                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &aTreasure Hunt successfully &ldisabled&a!"));
                    player.playSound(player.getLocation(), goodValue, 1f, 1.4f);
                    plugin.eventActive = false;
                    break;

                case "reload":
                    if(plugin.eventActive){
                        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cYou must disable the event to use this command!"));
                        player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                        return true;
                    }

                    plugin.reloadConfig();
                    plugin.getBooks().reloadConfig();
                    plugin.getTreasures().reloadConfig();
                    plugin.getPlayerData().reloadConfig();
                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &aTreasure Hunt reloaded successfully!"));
                    player.playSound(player.getLocation(), goodValue, 1f, 1.4f);
                    break;

                case "help":
                    player.playSound(player.getLocation(), goodValue, 1f, 1.4f);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("help-message.title")));
                    for(String line : plugin.getConfig().getStringList("help-message.lines")){
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
                    }
                    break;

                case "treasures":
                    if(strings.length > 1){
                        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cUsage: &l/treasurehunt treasures"));
                        player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                        return true;
                    }

                    plugin.getManageGUI().showMainManageGui(player);
                    player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                    break;

                case "hints":
                    ItemStack item = player.getInventory().getItemInMainHand(); //Gets the item that the player is holding in the main hand
                    if(strings.length < 2){
                        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cUsage: &l/treasurehunt books <create | delete | manage>"));
                        player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                        return true;
                    }
                    if(plugin.eventActive){
                        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cYou must disable the event to use this command!"));
                        player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                        return true;
                    }

                    switch(strings[1].toLowerCase()){
                        case "manage":
                            //Checking if there are any hints configured
                            if(books.getConfigurationSection("books") == null || books.getConfigurationSection("books").getKeys(false).isEmpty()){
                                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cThere are no hints configured!"));
                                player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                                return true;
                            }

                            if(strings.length < 3){
                                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cUsage: &l/treasurehunt hints manage <hint> ..."));
                                player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                                return true;
                            }

                            switch(strings[3].toLowerCase()){
                                case "setauthor":
                                    if(strings.length < 5){
                                        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cUsage: &l/th hints manage <hint> setauthor <author>"));
                                        player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                                        return true;
                                    }
                                    String author = strings[4];
                                    books.set("books."+strings[2]+".author", author);
                                    plugin.getBooks().saveConfig();

                                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &aAuthor &r"+author+" &asaved for book &l"+strings[2]+"&a!"));
                                    player.playSound(player.getLocation(), goodValue, 1f, 1.4f);
                                    break;

                                case "settitle":
                                    if(strings.length < 5){
                                        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cUsage: &l/th hints manage <hint> settitle <title>"));
                                        player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                                        return true;
                                    }

                                    String title = strings[4];
                                    books.set("books."+strings[2]+".title", title);
                                    plugin.getBooks().saveConfig();

                                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &aTitle &r"+title+" &asaved for book &l"+strings[2]+"&a!"));
                                    player.playSound(player.getLocation(), goodValue, 1f, 1.4f);
                                    break;

                                default:
                                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cUnknown command. Use &l/treasurehunt help &cfor info."));
                                    player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                                    break;
                            }
                            break;

                        case "create":
                            if(strings.length < 3){
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cUsage: &l/treasurehunt books create <name>"));
                                player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                                return true;
                            }
                            if(item.getType() != Material.WRITTEN_BOOK){
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cYou must hold a &lwritten book &cin your main hand!"));
                                player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                                return true;
                            }
                            String name = strings[2];
                            String path = "books." + name;

                            BookMeta meta = (BookMeta) item.getItemMeta();

                            books.set(path + ".title", meta.getTitle());
                            books.set(path + ".author", meta.getAuthor());
                            books.set(path + ".unlock-after", plugin.bookCount+1);
                            books.set(path + ".pages", meta.getPages());

                            plugin.getBooks().saveConfig();
                            plugin.bookCount++;
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &aBook &l"+name+"&a saved to &lbooks.yml!"));
                            player.playSound(player.getLocation(), goodValue, 1f, 1.4f);
                            break;

                        case "delete":
                            if(!books.isConfigurationSection("books")){
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cYou have no books created!"));
                                player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                                return true;
                            }
                            else if(strings.length < 3){
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cUsage: &l/treasurehunt books delete <name>"));
                                player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                                return true;
                            }
                            else{
                                String bookName = strings[2];
                                if(Objects.requireNonNull(books.getConfigurationSection("books")).getKeys(false).contains(bookName)){
                                    books.set("books." + bookName, null);
                                    plugin.getBooks().saveConfig();
                                    plugin.bookCount--;
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &aBook &l" + bookName + "&a successfully deleted!"));
                                    player.playSound(player.getLocation(), goodValue, 1f, 1.4f);
                                }
                                else{
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cBook &l" + bookName + "&c doesn't exit!"));
                                    player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                                }
                            }
                            break;

                        default:
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cUnknown command. Use &l/treasurehunt help &cfor info."));
                            player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                            break;
                    }
                    break;

                default:
                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cUnknown command. Use &l/treasurehunt help &cfor info."));
                    player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                    break;
            }
            return true;
        }

        //Hints command
        if(command.getName().equalsIgnoreCase("hints")){
            if(!commandSender.hasPermission("eog.use")){
                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou don't have permission to run this command!"));
                player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                return true;
            }
            if(!plugin.eventActive){
                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThe event is not enabled."));
                player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                return true;
            }
            if(strings.length > 0){
                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cUsage: &l/hints"));
                player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                return true;
            }

            plugin.getHintsGUI().hintsGUI(player);
            player.playSound(player.getLocation(), invalidValue, 1f, 1f);
            return true;
        }
        return false;
    }

    //Opens the book in /treasurehunt enable
    public void openStartBook(Player player){
        String author = plugin.getConfig().getString("start-book.author");
        String title = plugin.getConfig().getString("start-book.title");
        List<String> pages = plugin.getConfig().getStringList("start-book.pages");

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        meta.setAuthor(author);
        meta.setTitle(title);

        for(String page : pages){
            String rawPage = ChatColor.translateAlternateColorCodes('&', page);
            meta.addPage(rawPage);
        }
        book.setItemMeta(meta);
        player.openBook(book);
    }

    //teleports the players in /treasurehunt enable
    public void teleportPlayers(Player player){
        Location teleportLocation = new Location(player.getWorld(), plugin.getConfig().getInt("teleport-location-x"), plugin.getConfig().getInt("teleport-location-y"), plugin.getConfig().getInt("teleport-location-z"));
        player.teleport(teleportLocation);
    }


    public void getPlayers(){
        FileConfiguration data = plugin.getPlayerData().getConfig();
        FileConfiguration treasure = plugin.getTreasures().getConfig();

        for(Player p : Bukkit.getOnlinePlayers()){
            String path = "players." + p.getName();

            if(!data.isConfigurationSection(path)){
                data.set(path + ".treasures-found", 0);

                if(treasure.isConfigurationSection("treasures")){
                    for(String key : treasure.getConfigurationSection("treasures").getKeys(false)){
                        data.set(path + ".found." + key, false);
                    }
                }
            }
        }
        plugin.getPlayerData().saveConfig();
    }

    //Parses the duration of the event from config.yml (bar-timer)
    public long parseDuration(String input) {
        long millis = 0;
        Matcher m = Pattern.compile("(\\d+)([dhms])").matcher(input.toLowerCase());
        while (m.find()) {
            int value = Integer.parseInt(m.group(1));
            switch (m.group(2)) {
                case "d" -> millis += value * 86400000L;
                case "h" -> millis += value * 3600000L;
                case "m" -> millis += value * 60000L;
                case "s" -> millis += value * 1000L;
            }
        }
        return millis;
    }
}
