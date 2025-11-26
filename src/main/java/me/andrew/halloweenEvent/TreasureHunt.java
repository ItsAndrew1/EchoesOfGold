package me.andrew.halloweenEvent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class TreasureHunt extends JavaPlugin{
    private YMLfiles treasures;
    private YMLfiles books;
    private YMLfiles playerdata;
    private EventScoreboard scoreboardManager;
    private int guiSize;
    int bookCount = 0;
    boolean eventActive;
    TreasureManager treasureManager;
    EventBossBar bar;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        guiSize = getConfig().getInt("hints-gui.gui-rows") * 9;

        //Defining the YML files and main objects
        treasures = new YMLfiles(this, "treasures.yml");
        books = new YMLfiles(this, "books.yml");
        treasureManager = new TreasureManager(this);
        bar = new EventBossBar(this);
        playerdata = new YMLfiles(this, "playerdata.yml");
        scoreboardManager = new EventScoreboard(this);
        Hints hintsAccess = new Hints(this);

        //Setting the commands and their tabs
        getCommand("treasurehunt").setExecutor(new CommandManager(this, hintsAccess));
        getCommand("treasurehunt").setTabCompleter(new CommandTabs(this));
        getCommand("hints").setExecutor(new CommandManager(this, hintsAccess));

        //Setting the events
        getServer().getPluginManager().registerEvents(new TreasureClickEvent(this), this);
        getServer().getPluginManager().registerEvents(new Hints(this), this);

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

        //Check if everything for hint-gui is right

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
}