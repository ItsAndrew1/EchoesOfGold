package me.andrew.EchoesOfGold;

import io.papermc.paper.event.player.AsyncChatCommandDecorateEvent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventProgress implements Listener {
    private final EchoesOfGold plugin;
    private long duration;
    private long fullDuration;
    private BukkitRunnable task;

    public EventProgress(EchoesOfGold plugin){
        this.plugin = plugin;
    }

    public void startEvent(String durationString){
        duration = parseDuration(durationString);
        fullDuration = parseDuration(durationString);
        plugin.setEventDuration(duration);

        eventProgress();
    }

    private void eventProgress(){
        task = new BukkitRunnable() {
            @Override
            public void run() {
                duration--;
                plugin.setEventDuration(duration);
                if(duration == 0){
                    eventFinishMessage();
                    stopEvent();
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
                    String phMessage;
                    if(messageLine.contains("%three_quarters_time%")){
                        phMessage = ChatColor.translateAlternateColorCodes('&', messageLine.replace("%three_quarters_time%", String.valueOf(threeQuartersTime)));
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
                    String phMessage;
                    if(messageLine.contains("%half_time%")){
                        phMessage = ChatColor.translateAlternateColorCodes('&', messageLine.replace("%half_time%", String.valueOf(halfTime)));
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

                    //Check if the title has the %% placeholder
                    if(title.contains("%half_time%")) title = title.replace("%half_time%", String.valueOf(halfTime));
                    if(subtitle.contains("%half_time%")) subtitle = subtitle.replace("%half_time%", String.valueOf(halfTime));

                    p.sendTitle(ChatColor.translateAlternateColorCodes('&', title), ChatColor.translateAlternateColorCodes('&', subtitle));
                }

                p.playSound(p.getLocation(), thtSound, thtsVolume, thtsPitch);
            }
            return;
        }
    }

    private void eventFinishMessage(){

        for(Player p : Bukkit.getOnlinePlayers()){
            p.sendMessage("Salut!");
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
    public void playerJoin(PlayerJoinEvent event){
        if(!plugin.isEventActive()) return; //If the event isn't active, it returns.
        Player player = event.getPlayer();

        //Check if the boss bar and scoreboard are toggled
        boolean toggleBossBar = plugin.getConfig().getBoolean("boss-bar");
        boolean toggleScoreboard = plugin.getConfig().getBoolean("scoreboard");
        if(toggleBossBar) plugin.getBossBar().addPlayer(player);
        if(toggleScoreboard) plugin.getScoreboardManager().createScoreboard(player);

        //Spawns the treasures and their particles
        plugin.getTreasureManager().spawnTreasures();
        plugin.getTreasureManager().spawnChestParticles();
    }
}
