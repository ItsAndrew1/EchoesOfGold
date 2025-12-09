//Developed by _ItsAndrew_
package me.andrew.EchoesOfGold;

import me.andrew.EchoesOfGold.GUIs.*;

import org.bukkit.Bukkit;
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

public final class EchoesOfGold extends JavaPlugin implements Listener{
    private YMLfiles treasures;
    private YMLfiles books;
    private YMLfiles playerdata;
    private EventScoreboard scoreboardManager;
    private EventProgress eventProgressManager;
    private int hintsGuiSize;
    private int savedDuration = getConfig().getInt("saving-duration");
    private final Map<UUID, Consumer<String>> chatInput = new HashMap<>(); //This is for player's input in the treasure GUIs
    int bookCount = 0;
    private TreasureManager treasureManager;
    private EventBossBar bossBar;

    private boolean eventActive;
    private long eventDuration;

    //Defining the GUIs
    private MainManageGUI manageGUI;
    private RewardsChoiceGUI rewardsChoiceGUI;
    private ManageTreasuresGUI manageTreasuresGUI;
    private AllTreasuresGUI allTreasuresGUI;
    private AddRewardsGUI addRewardsGUI;
    private HintsGUI hintsGUI;
    private String treasureManagerChoice;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        hintsGuiSize = getConfig().getInt("hints-gui.gui-rows") * 9;

        //Defining the YML files and main objects
        treasures = new YMLfiles(this, "treasures.yml");
        books = new YMLfiles(this, "books.yml");
        treasureManager = new TreasureManager(this);
        bossBar = new EventBossBar(this);
        playerdata = new YMLfiles(this, "playerdata.yml");
        scoreboardManager = new EventScoreboard(this);
        eventProgressManager = new EventProgress(this);

        //Defining the GUIs
        manageTreasuresGUI = new ManageTreasuresGUI(this);
        rewardsChoiceGUI = new RewardsChoiceGUI(this);
        manageGUI = new MainManageGUI(this);
        allTreasuresGUI = new AllTreasuresGUI(this);
        addRewardsGUI = new AddRewardsGUI(this);
        hintsGUI = new HintsGUI(this);

        //Setting the commands and their tabs
        getCommand("eog").setExecutor(new CommandManager(this));
        getCommand("eog").setTabCompleter(new CommandTabs(this));
        getCommand("hints").setExecutor(new CommandManager(this));

        getServer().getPluginManager().registerEvents(manageGUI, this); //Events of mainManageGUI
        getServer().getPluginManager().registerEvents(manageTreasuresGUI, this); //Events of manageTreasuresGUI
        getServer().getPluginManager().registerEvents(rewardsChoiceGUI, this); //Events of rewardsChoiceGUI
        getServer().getPluginManager().registerEvents(allTreasuresGUI, this); //Events of allTreasuresGUI
        getServer().getPluginManager().registerEvents(hintsGUI, this); //Events of hints GUI
        getServer().getPluginManager().registerEvents(new TreasureClickEvent(this), this); //Events of treasure click
        getServer().getPluginManager().registerEvents(this, this); //Events of the main plugin class
        getServer().getPluginManager().registerEvents(eventProgressManager, this); //Events of EventProgress class
        getServer().getPluginManager().registerEvents(addRewardsGUI, this); //Events of AddRewards GUI

        //Regaining data after a reload
        if(savedDuration > 0){
            eventDuration = savedDuration;
            getEventProgressManager().startEvent(formatTime(eventDuration));
            eventActive = true;

            //Restart the boss bar (if it is toggled)
            boolean toggleBossBar = getConfig().getBoolean("boss-bar");
            if(toggleBossBar) bossBar.startBossBar();

            //Restart the scoreboard (if it is toggled)
            boolean toggleScoreboard = getConfig().getBoolean("scoreboard");
            if(toggleScoreboard) scoreboardManager.startScoreboard();

            treasureManager.spawnTreasures();
            treasureManager.spawnChestParticles();
            getLogger().warning("[ECHOES OF GOLD] Resumed event!");
        }
        else{
            eventActive = false;
            for(Player p : Bukkit.getOnlinePlayers()){
                p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }
        }

        //Check if the hintsGUI size is good
        if(hintsGuiSize < 9 || hintsGuiSize > 54){
            Bukkit.getLogger().warning("[E.O.G] The value gui-rows is invalid. The value must be between 1 and 6.");
        }

        //Check if everything for hintsGUI in 'config.yml' is right.
        try{
            //This is for exitButton (if it is toggled)
            boolean exitButtonToggle = getConfig().getBoolean("hints-gui.gui-exit-button.toggle");
            if(exitButtonToggle){
                String exitButtonMaterialString = getConfig().getString("hints-gui.gui-exit-button.material").toUpperCase();
                Material exitButtonMaterial = Material.matchMaterial(exitButtonMaterialString);
                int exitButtonSlot = getConfig().getInt("hints-gui.gui-exit-button.slot");

                if(exitButtonMaterial == null){
                    Bukkit.getLogger().warning("[E.O.G] Invalid material for gui-exit-button.material.");
                }
                if(exitButtonSlot < 1 || exitButtonSlot > getHintsGUISize()){
                    Bukkit.getLogger().warning("[E.O.G] Invalid value for gui-exit-button.slot! The value must be between 1 and "+ getHintsGUISize()+"!");
                }
            }
        } catch (Exception e){
            Bukkit.getLogger().warning("[E.O.G] "+e.getMessage()); //This is if the value for gui-exit-button.toggle and slot are not valid
        }

        try{
            //This is for noHintsItem (if it is toggle)
            String noHintsItemMaterialString = getConfig().getString("hints-gui.gui-no-hints-item.material").toUpperCase();
            Material noHintsItemMaterial = Material.matchMaterial(noHintsItemMaterialString);
            int noHintsItemSlot = getConfig().getInt("hints-gui.gui-no-hints-item.slot");

            if(noHintsItemMaterial == null){
                Bukkit.getLogger().warning("[E.O.G] Invalid material for gui-no-hints-item.material.");
            }
            if(noHintsItemSlot < 1 || noHintsItemSlot > getHintsGUISize()){
                Bukkit.getLogger().warning("[E.O.G] Invalid value for gui-no-hints-item.slot! The value must be between 1 and "+ getHintsGUISize()+"!");
            }
        } catch (Exception e){
            Bukkit.getLogger().warning("[E.O.G] "+e.getMessage()); //Checking the boolean and for the slot
        }

        try{
            //This is for hintsGUI decorations (if they are toggled)
            boolean toggleDecorations = getConfig().getBoolean("hints-gui.toggle-decorations");
            if(toggleDecorations){
                String decorationItemString = getConfig().getString("hints-gui.decoration-material");
                Material decorationItem = Material.matchMaterial(decorationItemString.toUpperCase());

                if(decorationItem == null){
                    Bukkit.getLogger().warning("[E.O.G] Invalid material for the decoration item in hintsGUI.");
                }
            }
        } catch (Exception e){
            Bukkit.getLogger().warning("[E.O.G] "+e.getMessage()); //This is for checking the boolean!
        }

        try{
            //This is for the hintsGUI info item (if it's toggled)
            boolean toggleInfoItem = getConfig().getBoolean("hints-gui.info-item-toggle");
            if(toggleInfoItem){
                String infoItemString = getConfig().getString("hints-gui.info-item-material");
                Material infoItem = Material.matchMaterial(infoItemString.toUpperCase());
                int infoItemSlot = getConfig().getInt("hints-gui.info-item-slot");

                //Checks the material and the slot
                if(infoItem == null){
                    Bukkit.getLogger().warning("[E.O.G] Invalid material for the info item in hintsGUI.");
                }
                if(infoItemSlot < 1 || infoItemSlot > getHintsGUISize()){
                    Bukkit.getLogger().warning("[E.O.G] Invalid slot for the info item in hintsGUI.");
                }
            }
        } catch (Exception e){
            Bukkit.getLogger().warning("[E.O.G] "+e.getMessage()); //This is for the boolean and for the slot
        }
    }

    //Saving data after shutting down the server
    @Override
    public void onDisable(){
        saveConfig();
        playerdata.saveConfig();
        books.saveConfig();
        treasures.saveConfig();

        savedDuration = getConfig().getInt("saved-duration");
        Bukkit.getLogger().info("Echoes of Gold shut down successfully!");
    }

    public void waitForPlayerInput(Player player, Consumer<String> callback){
        chatInput.put(player.getUniqueId(), callback);
    }

    private String formatTime(long seconds){
        long days = seconds / 86000;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder sb = new StringBuilder();

        if(days > 0) sb.append(days).append("d ");
        if(hours > 0 || days > 0) sb.append(hours).append("h ");
        if(minutes > 0 || hours > 0 || days > 0) sb.append(minutes).append("m ");
        sb.append(secs).append("s ");

        return sb.toString().trim();
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
        return bossBar;
    }
    public EventScoreboard getScoreboardManager(){
        return scoreboardManager;
    }
    public EventProgress getEventProgressManager(){
        return eventProgressManager;
    }
    public int getHintsGUISize(){
        return hintsGuiSize;
    }

    //Getter and setter for eventActive boolean
    public boolean isEventActive(){
        return eventActive;
    }
    public void setEventActive(boolean value){
        eventActive = value;
    }

    //Getter and setter for eventDuration
    public long getEventDuration(){
        return eventDuration;
    }
    public void setEventDuration(long duration){
        eventDuration = duration;
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
    public AddRewardsGUI getRewardsGUI(){
        return addRewardsGUI;
    }
    public RewardsChoiceGUI getRewardsChoiceGUI(){
        return rewardsChoiceGUI;
    }
    public HintsGUI getHintsGUI(){
        return hintsGUI;
    }

    //Setter and getter for treasureManagerChoice
    public void setTreasureManagerChoice(String choice){
        treasureManagerChoice = choice;
    }
    public String getTreasureManagerChoice(){
        return treasureManagerChoice;
    }
}