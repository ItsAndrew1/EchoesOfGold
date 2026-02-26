//Developed by _ItsAndrew_
package me.andrew.EchoesOfGold;

import net.milkbowl.vault.economy.Economy;
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

import java.sql.Connection;
import java.sql.PreparedStatement;
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

                    //Check if the economy object isn't null (if it is toggled)
                    boolean toggleEconomy = plugin.getConfig().getBoolean("economy.toggle-using-economy", false);
                    if(toggleEconomy && plugin.getEconomy() == null){
                        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cYou have economy enabled but there is no economy/Vault plugin found!"));
                        player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                        return true;
                    }

                    //Checking if each treasure has the coins set (if the economy is on)
                    if(plugin.getEconomy() != null){
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
                    }

                    //Sets up the economy (if it is toggled)
                    boolean internalEconomy = plugin.getConfig().getBoolean("economy.internal-economy.toggle", true);
                    if(!internalEconomy) setupPlayerAccounts(); //If the internal economy is toggled off, it sets up the Vault Account for the player
                    else{ //Else, it writes the players in the .db file
                        Connection dbConnection = plugin.getDbManager().getDbConnection();
                        for(Player p : Bukkit.getOnlinePlayers()){
                            if(plugin.getDbManager().isPlayerInDatabase(p.getUniqueId().toString())) continue;

                            String sql = "INSERT INTO players (uuid, balance, balance_during_event) VALUES (?, 0, 0)";
                            try(PreparedStatement ps = dbConnection.prepareStatement(sql)){
                                ps.setString(1, p.getUniqueId().toString());
                                ps.executeUpdate();
                            } catch (Exception e){
                                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cThere was an error while trying to write the players in the database! See message below: "));
                                player.playSound(player.getLocation(), invalidValue, 1f, 1f);
                                plugin.getLogger().warning("[E.O.G] Error while writing players in database: " + e.getMessage());
                                return true;
                            }
                        }
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
                boolean toggleEconomy = plugin.getConfig().getBoolean("economy.toggle-using-economy", false);
                if(toggleEconomy) data.set(path + ".coins-gathered", 0);

                if(treasures.isConfigurationSection("treasures")){
                    for(String key : treasures.getConfigurationSection("treasures").getKeys(false)){
                        data.set(path + ".found." + key, false);
                    }
                }
            }
        }
        plugin.getPlayerData().saveConfig();
    }

    //Creates the accounts for each player
    private void setupPlayerAccounts(){
        Economy economy = plugin.getEconomy();

        for(OfflinePlayer p : Bukkit.getOfflinePlayers()){
            if(!economy.hasAccount(p)) economy.createPlayerAccount(p);
        }

        plugin.getLogger().info("[E.O.G] Bank Accounts successfully created for each player of the server.");
    }
}
