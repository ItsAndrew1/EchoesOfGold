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
    private int hintsGuiSize;
    private final Map<UUID, Consumer<String>> chatInput = new HashMap<>(); //This is for player's input in the treasure GUIs
    int bookCount = 0;
    boolean eventActive;
    private TreasureManager treasureManager;
    private EventBossBar bar;

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
        bar = new EventBossBar(this);
        playerdata = new YMLfiles(this, "playerdata.yml");
        scoreboardManager = new EventScoreboard(this);
        hintsGUI = new HintsGUI(this);

        //Setting the GUIs
        manageTreasuresGUI = new ManageTreasuresGUI(this);
        rewardsChoiceGUI = new RewardsChoiceGUI(this);
        manageGUI = new MainManageGUI(this);
        allTreasuresGUI = new AllTreasuresGUI(this);
        addRewardsGUI = new AddRewardsGUI(this);

        //Setting the commands and their tabs
        getCommand("eog").setExecutor(new CommandManager(this));
        getCommand("eog").setTabCompleter(new CommandTabs(this));
        getCommand("hints").setExecutor(new CommandManager(this));

        //Setting the events
        getServer().getPluginManager().registerEvents(new TreasureClickEvent(this), this);
        getServer().getPluginManager().registerEvents(manageGUI, this);
        getServer().getPluginManager().registerEvents(manageTreasuresGUI, this);
        getServer().getPluginManager().registerEvents(rewardsChoiceGUI, this);
        getServer().getPluginManager().registerEvents(allTreasuresGUI, this);
        getServer().getPluginManager().registerEvents(new HintsGUI(this), this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(addRewardsGUI, this);

        //Regaining data after a reload
        if(getConfig().contains("duration")){
            long endTime = getConfig().getLong("duration");
            long remaining = endTime - System.currentTimeMillis();

            if(remaining > 0){
                eventActive = true;

                //Restart the boss bar (if it is toggled)
                boolean toggleBossBar = getConfig().getBoolean("boss-bar");
                if(toggleBossBar){
                    bar.startBossBar(remaining);
                    for(Player p : Bukkit.getOnlinePlayers()) bar.addPlayer(p);
                }

                //Restart the scoreboard (if it is toggled)
                boolean toggleScoreboard = getConfig().getBoolean("scoreboard");
                if(toggleScoreboard) scoreboardManager.updateScoreboard();

                treasureManager.spawnTreasures();
                getLogger().warning("Resumed event!");
            }
            else{
                eventActive = false;
                for(Player p : Bukkit.getOnlinePlayers()){
                    p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                }
            }
        }

        //Check if the hintsGUI size is good
        if(hintsGuiSize < 9 || hintsGuiSize > 54){
            Bukkit.getLogger().warning("[TREASUREHUNT] The value gui-rows is invalid. The value must be between 1 and 6.");
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
                    Bukkit.getLogger().warning("[TREASUREHUNT] Invalid material for gui-exit-button.material.");
                }
                if(exitButtonSlot < 1 || exitButtonSlot > getHintsGUISize()){
                    Bukkit.getLogger().warning("[TREASUREHUNT] Invalid value for gui-exit-button.slot! The value must be between 1 and "+ getHintsGUISize()+"!");
                }
            }
        } catch (Exception e){
            Bukkit.getLogger().warning("[TREASUREHUNT] "+e.getMessage()); //This is if the value for gui-exit-button.toggle and slot are not valid
        }

        try{
            //This is for noHintsItem (if it is toggle)
            String noHintsItemMaterialString = getConfig().getString("hints-gui.gui-no-hints-item.material").toUpperCase();
            Material noHintsItemMaterial = Material.matchMaterial(noHintsItemMaterialString);
            int noHintsItemSlot = getConfig().getInt("hints-gui.gui-no-hints-item.slot");

            if(noHintsItemMaterial == null){
                Bukkit.getLogger().warning("[TREASUREHUNT] Invalid material for gui-no-hints-item.material.");
            }
            if(noHintsItemSlot < 1 || noHintsItemSlot > getHintsGUISize()){
                Bukkit.getLogger().warning("[TREASUREHUNT] Invalid value for gui-no-hints-item.slot! The value must be between 1 and "+ getHintsGUISize()+"!");
            }
        } catch (Exception e){
            Bukkit.getLogger().warning("[TREASUREHUNT] "+e.getMessage()); //Checking the boolean and for the slot
        }

        try{
            //This is for hintsGUI decorations (if they are toggled)
            boolean toggleDecorations = getConfig().getBoolean("hints-gui.toggle-decorations");
            if(toggleDecorations){
                String decorationItemString = getConfig().getString("hints-gui.decoration-material");
                Material decorationItem = Material.matchMaterial(decorationItemString.toUpperCase());

                if(decorationItem == null){
                    Bukkit.getLogger().warning("[TREASUREHUNT] Invalid material for the decoration item in hintsGUI.");
                }
            }
        } catch (Exception e){
            Bukkit.getLogger().warning("[TREASUREHUNT] "+e.getMessage()); //This is for checking the boolean!
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
                    Bukkit.getLogger().warning("[TREASUREHUNT] Invalid material for the info item in hintsGUI.");
                }
                if(infoItemSlot < 1 || infoItemSlot > getHintsGUISize()){
                    Bukkit.getLogger().warning("[TREASUREHUNT] Invalid slot for the info item in hintsGUI.");
                }
            }
        } catch (Exception e){
            Bukkit.getLogger().warning("[TREASUREHUNT] "+e.getMessage()); //This is for the boolean and for the slot
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
        if(!eventActive){
            getConfig().set("duration", null);
        }
        Bukkit.getLogger().info("HalloweenEvent shut down successfully!");
    }

    public void waitForPlayerInput(Player player, Consumer<String> callback){
        chatInput.put(player.getUniqueId(), callback);
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
    public int getHintsGUISize(){
        return hintsGuiSize;
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