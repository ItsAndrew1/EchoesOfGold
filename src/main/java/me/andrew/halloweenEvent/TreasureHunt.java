//Developed by _ItsAndrew_
package me.andrew.halloweenEvent;

import me.andrew.halloweenEvent.GUIs.AllTreasuresGUI;
import me.andrew.halloweenEvent.GUIs.HintsGUI;
import me.andrew.halloweenEvent.GUIs.MainManageGUI;

import me.andrew.halloweenEvent.GUIs.ManageTreasuresGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public final class TreasureHunt extends JavaPlugin implements Listener{
    private YMLfiles treasures;
    private YMLfiles books;
    private YMLfiles playerdata;
    private EventScoreboard scoreboardManager;
    private int guiSize;
    private final Map<UUID, Consumer<String>> chatInput = new HashMap<>(); //This is for player's input in the treasure GUIs
    int bookCount = 0;
    boolean eventActive;
    TreasureManager treasureManager;
    EventBossBar bar;

    //Defining the GUIs
    private  MainManageGUI manageGUI;
    private ManageTreasuresGUI manageTreasuresGUI;
    private AllTreasuresGUI allTreasuresGUI;
    private String treasureManagerChoice;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        guiSize = getConfig().getInt("hints-gui.gui-rows") * 9;
        HintsGUI hintsGUIAccess = new HintsGUI(this);

        //Defining the YML files and main objects
        treasures = new YMLfiles(this, "treasures.yml");
        books = new YMLfiles(this, "books.yml");
        treasureManager = new TreasureManager(this);
        bar = new EventBossBar(this);
        playerdata = new YMLfiles(this, "playerdata.yml");
        scoreboardManager = new EventScoreboard(this);

        //Setting the GUIs
        manageTreasuresGUI = new ManageTreasuresGUI(this);
        manageGUI = new MainManageGUI(this);
        allTreasuresGUI = new AllTreasuresGUI(this);

        //Setting the commands and their tabs
        getCommand("treasurehunt").setExecutor(new CommandManager(this, hintsGUIAccess));
        getCommand("treasurehunt").setTabCompleter(new CommandTabs(this));
        getCommand("hints").setExecutor(new CommandManager(this, hintsGUIAccess));

        //Setting the events
        getServer().getPluginManager().registerEvents(new TreasureClickEvent(this), this);
        getServer().getPluginManager().registerEvents(new MainManageGUI(this), this);
        getServer().getPluginManager().registerEvents(new ManageTreasuresGUI(this), this);
        getServer().getPluginManager().registerEvents(new AllTreasuresGUI(this), this);
        getServer().getPluginManager().registerEvents(new HintsGUI(this), this);
        getServer().getPluginManager().registerEvents(this, this);

        //Regaining data after a reload
        if(getConfig().contains("duration")){
            long endTime = getConfig().getLong("duration");
            long remaining = endTime - System.currentTimeMillis();

            if(remaining > 0){
                eventActive = true;
                bar.start(remaining);
                getScoreboardManager().updateScoreboard();

                for (Player p : Bukkit.getOnlinePlayers()){
                    bar.addPlayer(p);
                    getTreasureManager().spawnChestParticles();
                }
                treasureManager.spawnTreasures();
                getLogger().info("Resumed event countdown and treasures.");
            }
            else{
                eventActive = false;
                for(Player p : Bukkit.getOnlinePlayers()){
                    p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                }
                getLogger().info("Event had already ended before a restart.");
            }
        }

        //Check if the size is good
        if(guiSize < 9 || guiSize > 54){
            Bukkit.getLogger().warning("[TH] The value gui-rows is invalid. The value must be between 1 and 6.");
        }

        //Check if everything for hint-gui is right.
        try{
            //This is for exitButton (if it is toggled)
            boolean exitButtonToggle = getConfig().getBoolean("hints-gui.gui-exit-button.toggle");
            if(exitButtonToggle){
                String exitButtonMaterialString = getConfig().getString("hints-gui.gui-exit-button.material").toUpperCase();
                Material exitButtonMaterial = Material.matchMaterial(exitButtonMaterialString);
                int exitButtonSlot = getConfig().getInt("hints-gui.gui-exit-button.slot");

                if(exitButtonMaterial == null){
                    Bukkit.getLogger().warning("[TH] Invalid material for gui-exit-button.material.");
                }
                if(exitButtonSlot < 1 || exitButtonSlot > getGuiSize()){
                    Bukkit.getLogger().warning("[TH] Invalid value for gui-exit-button.slot! The value must be between 1 and "+getGuiSize()+"!");
                }
            }
        } catch (Exception e){
            Bukkit.getLogger().warning("[TH] "+e.getMessage()); //This is if the value of gui-exit-button.toggle is not valid
        }
        try{
            //This is for noHintsItem (if it is toggle)
            boolean noHintsItemToggle = getConfig().getBoolean("hints-gui.gui-no-hints-item.toggle");
            if(noHintsItemToggle){
                String noHintsItemMaterialString = getConfig().getString("hints-gui.gui-no-hints-item.material").toUpperCase();
                Material noHintsItemMaterial = Material.matchMaterial(noHintsItemMaterialString);
                int noHintsItemSlot = getConfig().getInt("hints-gui.gui-no-hints-item.slot");

                if(noHintsItemMaterial == null){
                    Bukkit.getLogger().warning("[TH] Invalid material for gui-no-hints-item.material.");
                }
                if(noHintsItemSlot < 1 || noHintsItemSlot > getGuiSize()){
                    Bukkit.getLogger().warning("[TH] Invalid value for gui-no-hints-item.slot! The value must be between 1 and "+getGuiSize()+"!");
                }
            }
        } catch (Exception e){
            Bukkit.getLogger().warning("[TH] "+e.getMessage());
        }

        //Check if the coordonates of the enable command are ok
        String coordX = getConfig().getString("teleport-location-x");
        String coordY = getConfig().getString("teleport-location-x");
        String coordZ = getConfig().getString("teleport-location-z");
        if(coordX != null && coordY != null && coordZ != null){
            try{
                int intCoordX = Integer.parseInt(coordX);
                int intCoordY = Integer.parseInt(coordY);
                int intCoordZ = Integer.parseInt(coordZ);
            } catch (NumberFormatException e){
                Bukkit.getLogger().warning("[TH] The enable command coordonates MUST be a number!");
                Bukkit.getLogger().warning("[TH] Error message: "+e.getMessage());
            }
        }
        else{
            Bukkit.getLogger().warning("[TH] One/more coordonates from the enable command are null!");
        }
    }

    //Saving data after shutting down the server
    @Override
    public void onDisable(){
        playerdata.saveConfig();
        books.saveConfig();
        treasures.saveConfig();
        if(bar != null && bar.isActive()){
            eventActive = true;
            getConfig().set("duration", bar.getEndTime());
            saveConfig();
        }
        else{
            getConfig().set("duration", null);
        }
        Bukkit.getLogger().info("HalloweenEvent shut down successfully!");
    }

    public void waitForPlayerInput(Player player, Consumer<String> callback){
        chatInput.put(player.getUniqueId(), callback);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&lEnter a name for your treasure:"));
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if(!chatInput.containsKey(playerUUID)) return;
        event.setCancelled(true);

        String input = event.getMessage();
        Consumer<String> callback = chatInput.remove(playerUUID);
        Bukkit.getScheduler().runTask(this, () -> callback.accept(input));
    }

    //Getters
    public YMLfiles getTreasures(){
        return treasures;
    }
    public YMLfiles getBooks(){
        return books;
    }
    public TreasureManager getTreasureManager(){
        return treasureManager;
    }
    public YMLfiles  getPlayerData(){
        return playerdata;
    }
    public EventBossBar getBossBar(){
        return bar;
    }
    public EventScoreboard getScoreboardManager(){
        return scoreboardManager;
    }
    public int getGuiSize(){
        return guiSize;
    }

    //Getters for the GUIs
    public MainManageGUI getManageGUI(){
        return manageGUI;
    }
    public ManageTreasuresGUI getManageTreasuresGUI(){
        return manageTreasuresGUI;
    }
    public AllTreasuresGUI getAllTreasuresGUI(){
        return allTreasuresGUI;
    }

    //Setter and getter for treasureManagerChoice
    public void setTreasureManagerChoice(String choice){
        treasureManagerChoice = choice;
    }
    public String getTreasureManagerChoice(){
        return treasureManagerChoice;
    }
}