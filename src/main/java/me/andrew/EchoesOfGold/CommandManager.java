//Developed by _ItsAndrew_
package me.andrew.EchoesOfGold;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommandManager implements CommandExecutor{
    private final EchoesOfGold plugin;

    public CommandManager(EchoesOfGold plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        FileConfiguration treasures = plugin.getTreasures().getConfig();
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
                    if(plugin.isEventActive()){
                        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cThe game is &lalready active&c!"));
                        player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                        return true;
                    }

                    //Check if the coordonates of the enable command are ok
                    String coordX = plugin.getConfig().getString("teleport-location-x");
                    String coordY = plugin.getConfig().getString("teleport-location-x");
                    String coordZ = plugin.getConfig().getString("teleport-location-z");
                    if(coordX != null && coordY != null && coordZ != null){
                        try{
                            int intCoordX = Integer.parseInt(coordX);
                            int intCoordY = Integer.parseInt(coordY);
                            int intCoordZ = Integer.parseInt(coordZ);
                        } catch (NumberFormatException e){
                            player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cOne/More values of the teleport coordinates are &linvalid&c!"));
                            return true;
                        }
                    }
                    else{
                        player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cOne/More coordinates of the teleport location are &lnull&c!"));
                        return true;
                    }

                    //Check if the event has treasures configured.
                    ConfigurationSection treasureSection = treasures.getConfigurationSection("treasures");
                    if(treasureSection == null || treasureSection.getKeys(false).isEmpty()){
                        player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cThere are no treasures configured! Use &l/eog treasures &cto set up some."));
                        return true;
                    }

                    //Checking if each treasure has the coins set and setting the players accounts (if the economy is on)
                    if(plugin.getEconomyProvider() != null){
                        boolean allSet = true;

                        ConfigurationSection allTreasures = treasures.getConfigurationSection("treasures");
                        for(String treasure : allTreasures.getKeys(false)){
                            String mainPath = "treasures." + treasure;
                            int coins = treasures.getInt(mainPath+".coins", 0);

                            if(coins == 0){
                                allSet = false;
                                break;
                            }
                        }

                        if(!allSet){
                            player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cSome treasures in &ltreasures.yml &cdon't have the coins set!"));
                            return true;
                        }

                        //Setting up the accounts
                        plugin.getEconomyProvider().setupAccounts();
                    }

                    //Checking if the number of treasures is equal with the value set for 'nr-of-treasures'
                    int nrOfTreasures = treasures.getInt("nr-of-treasures");
                    if(nrOfTreasures == 0){
                        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cThe value of &lnr-of-treasures &cin &ltreasures.yml &cis NULL!"));
                        player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                        return true;
                    }

                    if(nrOfTreasures != treasureSection.getKeys(false).size()){
                        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cThe number of treasures in &ltreasures.yml &cdoesn't match the value set for &lnr-of-treasures&c!"));
                        player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                        return true;
                    }

                    //Get all the details about the start-event-sound. Prints an error if the sound is invalid
                    String startEventSoundString = plugin.getConfig().getString("start-event-sound");
                    float startEventSoundVolume = plugin.getConfig().getInt("ses-volume");
                    float startEventSoundPitch = plugin.getConfig().getInt("ses-pitch");
                    Sound startEventSound;
                    try{
                        NamespacedKey checkStartEventSound = NamespacedKey.minecraft(startEventSoundString.toLowerCase());
                        startEventSound = Registry.SOUNDS.get(checkStartEventSound);
                    } catch (Exception e){
                        player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cThere is a problem with &lstart-event-sound&c!"));
                        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &c"+e.getMessage()));
                        return true;
                    }

                    //Gets all the players and opens the book after openBookDelay seconds
                    initializePlayers();
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
                    String durationString = plugin.getConfig().getString("event-duration");
                    plugin.getEventProgressManager().startEvent(durationString);

                    //If the value boss-bar is toggled, spawns the boss bar with the timer.
                    if(plugin.getConfig().getString("boss-bar").equalsIgnoreCase("true")){
                        plugin.getBossBar().startBossBar();
                    }

                    //If the scoreboard is toggled, it shows up on the screen.
                    if(plugin.getConfig().getString("scoreboard").equalsIgnoreCase("true")){
                        plugin.getScoreboardManager().startScoreboard();
                    }

                    //Starts the event
                    plugin.setEventActive(true);
                    Bukkit.getLogger().info("[E.O.G] Event started successfully");
                    break;

                case "disable":
                    if(!plugin.isEventActive()){
                        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cThe game is &lalready disabled&c!"));
                        player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                        return true;
                    }
                    for(Player p : Bukkit.getOnlinePlayers()){
                        plugin.getScoreboardManager().stopScoreboard(p);
                    }
                    plugin.getTreasureManager().removeTreasures();

                    //Removing the particles of the treasures attached to all players
                    plugin.getTreasureManager().cancelParticles();

                    plugin.getPlayerData().getConfig().set("players", null);
                    plugin.getPlayerData().saveConfig();

                    //Disable the boss bar if it is toggled
                    boolean toggleBossBar = plugin.getConfig().getBoolean("boss-bar");
                    if(toggleBossBar) plugin.getBossBar().stopBossBar();

                    Bukkit.getLogger().info("[ECHOES OF GOLD] Event successfully disabled!");
                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &aEvent successfully &ldisabled&a!"));
                    player.playSound(player.getLocation(), goodValue, 1f, 1.4f);
                    plugin.getEventProgressManager().stopEvent();
                    plugin.setEventActive(false);
                    break;

                case "reload":
                    if(plugin.isEventActive()){
                        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cYou must disable the event to use this command!"));
                        player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                        return true;
                    }

                    plugin.reloadConfig();
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
                        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cUsage: &l/eog treasures"));
                        player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                        return true;
                    }

                    plugin.getManageGUI().showMainManageGui(player);
                    player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                    break;

                case "shop": //This will only work if the internal economy is toggled on
                    boolean toggleInternalEconomy = plugin.getConfig().getBoolean("economy.internal-economy.toggle", true);
                    if(!toggleInternalEconomy) {
                        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cYou cannot use this since the internal economy is not enabled!"));
                        player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                        return true;
                    }

                    if(strings.length < 2){
                        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cUsage: &l/eog shop <addItem | removeItem>"));
                        player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                        return true;
                    }

                    String shopArgument = strings[1];
                    switch(shopArgument){
                        case "addItem" -> {
                            if(strings.length < 3){
                                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cUsage: &l/eog shop addItem <item> <price>"));
                                player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                                return true;
                            }

                            ItemStack itemInHand = player.getInventory().getItemInMainHand();
                            ConfigurationSection itemsSection = plugin.getConfig().getConfigurationSection("economy.shop-gui.items");
                            if(itemInHand.getType().equals(Material.AIR)){
                                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cYou must be holding an item in your hand!"));
                                player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                                return true;
                            }

                            //Checking if the item is already in the shop
                            if(itemAlreadyInShop(itemInHand)){
                                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cThis item is already in the shop!"));
                                player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                                return true;
                            }

                            //Saving the item in the 'items' section
                            int crt = itemsSection.getKeys(false).isEmpty() ? 1 : itemsSection.getKeys(false).size() + 1;
                            String itemPath = "economy.shop-gui.items."+ crt +".item";
                            plugin.getConfig().set(itemPath, itemInHand);

                            double price = Double.parseDouble(strings[2]);
                            plugin.getConfig().set("economy.shop-gui.items."+ crt +".price", price);
                            plugin.saveConfig();

                            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &aItem &l"+itemInHand.getType().name()+" &aadded to the shop!"));
                            player.playSound(player.getLocation(), goodValue, 1f, 1.4f);
                            return true;
                        }

                        case "removeItem" -> {

                        }
                    }


                    break;

                default:
                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cUnknown command. Use &l/eog help &cfor info."));
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

            if(!plugin.isEventActive()){
                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThe event is not enabled."));
                player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                return true;
            }

            if(strings.length > 0){
                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cUsage: &l/hints"));
                player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                return true;
            }

            plugin.getHintsGUI().hintsGUI(player, 1);
            player.playSound(player.getLocation(), invalidValue, 1f, 1f);
            return true;
        }
        return false;
    }

    //Opens the book in /treasurehunt enable
    private void openStartBook(Player player){
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

    private boolean itemAlreadyInShop(ItemStack item){
        FileConfiguration mainConfid = plugin.getConfig();
        ConfigurationSection items = mainConfid.getConfigurationSection("economy.shop-gui.items");
        if(items == null) return false;

        for(String key : items.getKeys(false)){
            ItemStack itemInShop = plugin.getConfig().getItemStack("economy.shop-gui.items."+key+".item");
            if(itemInShop.getType().equals(item.getType())){
                return true;
            }
        }

        return false;
    }

    //Teleports the players at the start of the game
    private void teleportPlayers(Player player){
        Location teleportLocation = new Location(player.getWorld(), plugin.getConfig().getInt("teleport-location-x"), plugin.getConfig().getInt("teleport-location-y"), plugin.getConfig().getInt("teleport-location-z"));
        player.teleport(teleportLocation);
    }

    //Helper method to initialize players in 'playerdata.yml'
    private void initializePlayers(){
        FileConfiguration data = plugin.getPlayerData().getConfig();
        FileConfiguration treasures = plugin.getTreasures().getConfig();

        for(Player p : Bukkit.getOnlinePlayers()){
            String path = "players." + p.getUniqueId();

            if(!data.isConfigurationSection(path)){
                data.set(path + ".treasures-found", 0);

                //Adding 'coins-gathered' to player's data if the economy is toggled and working
                if(plugin.getEconomyProvider() != null) data.set(path + ".coins-gathered", 0);

                if(treasures.isConfigurationSection("treasures")){
                    for(String key : treasures.getConfigurationSection("treasures").getKeys(false)){
                        data.set(path + ".found." + key, false);
                    }
                }
            }
        }
        plugin.getPlayerData().saveConfig();
    }
}
