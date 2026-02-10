package me.andrew.EchoesOfGold;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventProgress implements Listener {
    private final EchoesOfGold plugin;
    private long duration;
    private long fullDuration;
    private int countDownValue;
    private BukkitRunnable task;

    public EventProgress(EchoesOfGold plugin){
        this.plugin = plugin;
    }

    public void startEvent(String durationString){
        duration = parseDuration(durationString);

        String fullDurationString = plugin.getConfig().getString("event-duration");
        fullDuration = parseDuration(fullDurationString);
        plugin.setEventDuration(duration);

        countDownValue = plugin.getConfig().getInt("event-final-time.final-countdown.seconds-to-start");
        eventProgress();
    }

    private void eventProgress(){
        task = new BukkitRunnable() {
            @Override
            public void run() {
                duration--;
                plugin.setEventDuration(duration);
                plugin.getConfig().set("saving-duration", duration);
                plugin.saveConfig();

                //Starts the cooldown event
                if(duration <= countDownValue && duration > 0){
                    //Getting the sound and sound's volume/pitch
                    Sound countdownSound = Registry.SOUNDS.get(NamespacedKey.minecraft(plugin.getConfig().getString("countdown-sound").toLowerCase()));
                    float csVolume = plugin.getConfig().getInt("cs-volume");
                    float csPitch = plugin.getConfig().getInt("cs-pitch");

                    //Getting the title/subtitle and replacing the %% placeholder
                    String countdownTitle = plugin.getConfig().getString("event-final-time.final-countdown.title");
                    String countdownSubtitle = plugin.getConfig().getString("event-final-time.final-countdown.subtitle");
                    if(countdownTitle.contains("%cooldown_time%")) countdownTitle =  countdownTitle.replace("%cooldown_time%", String.valueOf(duration));
                    if(countdownSubtitle.contains("%cooldown_time%"))countdownSubtitle = countdownSubtitle.replace("%cooldown_time%", String.valueOf(duration));

                    for(Player p : Bukkit.getOnlinePlayers()){
                        p.sendTitle(ChatColor.translateAlternateColorCodes('&', countdownTitle), ChatColor.translateAlternateColorCodes('&', countdownSubtitle));
                        p.playSound(p.getLocation(), countdownSound, csVolume, csPitch);
                    }
                }

                //Finishes the event
                if(duration == 0){
                    eventFinishChatMessage();

                    plugin.getTreasureManager().removeTreasures();

                    //Removing every the particles of the treasures attached to all players
                    for(OfflinePlayer p : Bukkit.getOfflinePlayers()) plugin.getTreasureManager().cancelParticles();

                    plugin.getPlayerData().getConfig().set("players", null);
                    plugin.getPlayerData().saveConfig();

                    //Stops the boss bar/scoreboard
                    long bossBarDelay = plugin.getConfig().getLong("stop-boss-bar-interval");
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            plugin.getBossBar().stopBossBar();
                        }
                    }.runTaskLater(plugin, bossBarDelay*20L);
                    for(Player p : Bukkit.getOnlinePlayers()){
                        plugin.getScoreboardManager().stopScoreboard(p);
                    }
                    task.cancel();
                    plugin.setEventActive(false);
                }

                //Displays messages in chat based on the timer
                showEventChatMessages();
            }
        };
        task.runTaskTimer(plugin, 0L, 20L); //Runs the task every second
    }

    private void showEventChatMessages(){
        //Check if these messages are toggled.
        boolean toggleMessages = plugin.getConfig().getBoolean("toggle-event-messages");
        if(!toggleMessages) return;

        long threeQuartersTime = (fullDuration * 3)/4;
        long halfTime = fullDuration/2;
        long oneQuarterTime = fullDuration/4;

        if(plugin.getEventDuration() == threeQuartersTime){
            //Check if the threeQuarterTime message is toggled
            boolean toggle3Qmessage = plugin.getConfig().getBoolean("event-three-quarters-time.toggle");
            if(!toggle3Qmessage) return;

            Sound tqtSound = Registry.SOUNDS.get(NamespacedKey.minecraft(plugin.getConfig().getString("event-three-quarter-time-sound").toLowerCase()));
            float tqtsVolume = plugin.getConfig().getInt("etqts-volume");
            float tqtsPitch = plugin.getConfig().getInt("etqts-pitch");

            List<String> threeQmessageLines = plugin.getConfig().getStringList("event-three-quarters-time.chat-message-lines");
            for(Player p : Bukkit.getOnlinePlayers()){
                for(String messageLine : threeQmessageLines){ //Displays the message
                    //Replaces the %% placeholder if the line contains it
                    if(messageLine.contains("%three_quarters_time%")){
                        String phMessage = ChatColor.translateAlternateColorCodes('&', messageLine.replace("%three_quarters_time%", String.valueOf(threeQuartersTime)));
                        p.sendMessage(phMessage);
                    }
                    else{
                        String coloredMessageLine = ChatColor.translateAlternateColorCodes('&', messageLine);
                        p.sendMessage(coloredMessageLine);
                    }
                }

                //Sends the players a title/subtitle if they are toggled
                boolean toggleTitleSubtitle = plugin.getConfig().getBoolean("event-three-quarters-time.toggle-title-subtitle");
                if(toggleTitleSubtitle){
                    String title, subtitle;
                    title = plugin.getConfig().getString("event-three-quarters-time.title.message");
                    subtitle = plugin.getConfig().getString("event-three-quarters-time.subtitle.message");

                    //Check if the title has the %% placeholder
                    if(title.contains("%three_quarters_time%")){
                        title = plugin.getConfig().getString("event-three-quarters-time.title.message").replace("%three_quarters_time%", String.valueOf(threeQuartersTime));
                    }
                    //Check if the subtitle has the %% placeholder
                    if(subtitle.contains("%three_quarters_time%")){
                        subtitle = plugin.getConfig().getString("event-three-quarters-time.subtitle.message").replace("%three_quarters_time%", String.valueOf(threeQuartersTime));
                    }

                    p.sendTitle(ChatColor.translateAlternateColorCodes('&', title), ChatColor.translateAlternateColorCodes('&', subtitle)); //Sends the title/subtitle
                }
                p.playSound(p.getLocation(), tqtSound, tqtsVolume, tqtsPitch);
            }
            return;
        }

        if(plugin.getEventDuration() == halfTime){
            //Check if the halfTime message is toggled
            boolean toggleHalfTimeMessage = plugin.getConfig().getBoolean("event-half-time.toggle");
            if(!toggleHalfTimeMessage) return;

            Sound thtSound = Registry.SOUNDS.get(NamespacedKey.minecraft(plugin.getConfig().getString("event-half-time-sound").toLowerCase()));
            float thtsVolume = plugin.getConfig().getInt("ehts-volume");
            float thtsPitch = plugin.getConfig().getInt("ehts-pitch");

            List<String> messageLines = plugin.getConfig().getStringList("event-half-time.chat-message-lines");
            for(Player p : Bukkit.getOnlinePlayers()){
                for(String messageLine : messageLines){ //Displays the message
                    //Replaces the %% placeholder if the line contains it
                    if(messageLine.contains("%half_time%")){
                        String phMessage = ChatColor.translateAlternateColorCodes('&', messageLine.replace("%half_time%", String.valueOf(halfTime)));
                        p.sendMessage(phMessage);
                    }
                    else{
                        String coloredLine = ChatColor.translateAlternateColorCodes('&', messageLine);
                        p.sendMessage(coloredLine);
                    }
                }

                //Send the players title/subtitle if they are toggled
                boolean toggleTitleSubtitle = plugin.getConfig().getBoolean("event-half-time.toggle-title-subtitle");
                if(toggleTitleSubtitle){
                    String title, subtitle;
                    title = plugin.getConfig().getString("event-half-time.title.message");
                    subtitle = plugin.getConfig().getString("event-half-time.subtitle.message");

                    //Check if the title/subtitle has the %% placeholder
                    if(title.contains("%half_time%")) title = title.replace("%half_time%", String.valueOf(halfTime));
                    if(subtitle.contains("%half_time%")) subtitle = subtitle.replace("%half_time%", String.valueOf(halfTime));

                    p.sendTitle(ChatColor.translateAlternateColorCodes('&', title), ChatColor.translateAlternateColorCodes('&', subtitle));
                }

                p.playSound(p.getLocation(), thtSound, thtsVolume, thtsPitch);
            }
            return;
        }

        if(plugin.getEventDuration() == oneQuarterTime){
            //Check if the oneQuarterTime message is toggled
            boolean oqtToggle = plugin.getConfig().getBoolean("event-one-quarter-time.toggle");
            if(!oqtToggle) return;

            Sound oqtSound = Registry.SOUNDS.get(NamespacedKey.minecraft(plugin.getConfig().getString("event-one-quarter-time-sound").toLowerCase()));
            float oqtsVolume = plugin.getConfig().getInt("eoqts-volume");
            float oqtsPitch = plugin.getConfig().getInt("eoqts-pitch");

            List<String> chatMessageLines = plugin.getConfig().getStringList("event-one-quarter-time.chat-message-lines");
            for(Player p : Bukkit.getOnlinePlayers()){
                for(String messageLine : chatMessageLines){
                    //Check if the message line has the %% placeholder
                    if(messageLine.contains("%one_quarter_time%")){
                        String phMessage = ChatColor.translateAlternateColorCodes('&', messageLine.replace("%one_quarter_time%", String.valueOf(oneQuarterTime)));
                        p.sendMessage(phMessage);
                    }
                    else{
                        String normalLine = ChatColor.translateAlternateColorCodes('&', messageLine);
                        p.sendMessage(normalLine);
                    }
                }

                //Send the players title and subtitle if they are toggled
                boolean toggleTitleSubtitle = plugin.getConfig().getBoolean("event-one-quarter-time.toggle-title-subtitle");
                if(toggleTitleSubtitle){
                    String title = plugin.getConfig().getString("event-one-quarter-time.title.message");
                    String subtitle = plugin.getConfig().getString("event-one-quarter-time.subtitle.message");

                    //Check if the title/subtitle contains %% placeholder
                    if(title.contains("%one_quarter_time%")) title = title.replace("%one_quarter_time%", String.valueOf(oneQuarterTime));
                    if(subtitle.contains("%one_quarter_time%")) subtitle = subtitle.replace("%one_quarter_time%", String.valueOf(oneQuarterTime));

                    p.sendTitle(ChatColor.translateAlternateColorCodes('&', title), ChatColor.translateAlternateColorCodes('&', subtitle));
                }

                p.playSound(p.getLocation(), oqtSound, oqtsVolume, oqtsPitch); //Plays the sound to the players
            }
        }
    }

    private void eventFinishChatMessage(){
        //Check if the message is toggled
        boolean toggleMessage = plugin.getConfig().getBoolean("event-final-time.finish-event-message.toggle");
        if(!toggleMessage) return;

        //Sends the chat message
        List<String> messageLines = plugin.getConfig().getStringList("event-final-time.finish-event-message.message-lines");
        List<Map.Entry<String, Integer>> top3Players = plugin.getTreasureManager().getTopPlayers();
        for(Player p : Bukkit.getOnlinePlayers()){
            String top1player = !top3Players.isEmpty() ? top3Players.getFirst().getKey() : "None";
            int t1pTreasures = !top3Players.isEmpty() ? top3Players.getFirst().getValue() : 0;
            String top2player = top3Players.size() > 1 ? top3Players.get(1).getKey() : "None";
            int t2pTreasures = top3Players.size() > 1 ? top3Players.get(1).getValue() : 0;
            String top3player = top3Players.size() > 2 ? top3Players.get(2).getKey() : "None";
            int t3pTreasures = top3Players.size() > 2 ? top3Players.get(2).getValue() : 0;

            Sound eventFinishSound = Registry.SOUNDS.get(NamespacedKey.minecraft(plugin.getConfig().getString("event-finish-sound").toLowerCase()));
            float efsVolume = plugin.getConfig().getInt("efs-volume");
            float efsPitch = plugin.getConfig().getInt("efs-pitch");

            String title = plugin.getConfig().getString("event-final-time.finish-event-title");
            String subtitle = plugin.getConfig().getString("event-final-time.finish-event-subtitle");

            for(String messageLine : messageLines){
                title = title.replace("%player_name%", p.getName());
                subtitle = subtitle.replace("%player_name%", p.getName());
                String phLine = messageLine
                        .replace("%top1_name%", top1player)
                        .replace("%top2_name%", top2player)
                        .replace("%top3_name%", top3player)
                        .replace("%top1_count%", String.valueOf(t1pTreasures))
                        .replace("%top2_count%", String.valueOf(t2pTreasures))
                        .replace("%top3_count%", String.valueOf(t3pTreasures));

                String coloredParsed = ChatColor.translateAlternateColorCodes('&', phLine);
                p.sendMessage(coloredParsed);
                p.sendTitle(ChatColor.translateAlternateColorCodes('&', title),  ChatColor.translateAlternateColorCodes('&', subtitle));
            }
            p.playSound(p.getLocation(), eventFinishSound, efsVolume, efsPitch);
        }
    }

    public void stopEvent(){
        if(task!=null) task.cancel();
    } //Helps with stopping the event
    public long getDuration(){
        return duration;
    } //Helps with getting the duration of the event

    //Parses the duration of the event from config.yml (bar-timer)
    private long parseDuration(String input){
        long seconds = 0;
        Matcher m = Pattern.compile("(\\d+)([dhms])").matcher(input.toLowerCase());
        while (m.find()) {
            int value = Integer.parseInt(m.group(1));
            switch (m.group(2)) {
                case "d" -> seconds += value * 86400L;
                case "h" -> seconds += value * 3600L;
                case "m" -> seconds += value * 60L;
                case "s" -> seconds += value;
            }
        }
        return seconds;
    }

    //If a player joins during the event
    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        if (!plugin.isEventActive()) return; //If the event isn't active, it returns.
        Player targetPlayer = event.getPlayer();

        //Check if the boss bar and scoreboard are toggled
        boolean toggleBossBar = plugin.getConfig().getBoolean("boss-bar");
        boolean toggleScoreboard = plugin.getConfig().getBoolean("scoreboard");
        if (toggleBossBar) plugin.getBossBar().addPlayer(targetPlayer);
        if (toggleScoreboard) plugin.getScoreboardManager().updateScoreboard(targetPlayer);

        //Spawns the treasures and their particles
        plugin.getTreasureManager().spawnTreasures();
        plugin.getTreasureManager().spawnChestParticles();

        ConfigurationSection players = plugin.getPlayerData().getConfig().getConfigurationSection("players");
        FileConfiguration data = plugin.getPlayerData().getConfig();
        FileConfiguration treasuresConfig = plugin.getTreasures().getConfig();

        if (!players.contains(targetPlayer.getName())) {
            String path = "players." + targetPlayer.getName();
            data.set(path + ".treasures-found", 0);

            if (treasuresConfig.isConfigurationSection("treasures")) {
                for (String key : treasuresConfig.getConfigurationSection("treasures").getKeys(false)) {
                    data.set(path + ".found." + key, false);
                }
            }

            //Also creates an account for him (if the economy is toggled)
            boolean toggleEconomy = plugin.getConfig().getBoolean("toggle-using-economy", false);
            if (toggleEconomy) plugin.getEconomy().createPlayerAccount(targetPlayer);
        }
    }
}
