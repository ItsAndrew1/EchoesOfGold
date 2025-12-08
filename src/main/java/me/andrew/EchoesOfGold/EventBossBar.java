//Developed by _ItsAndrew_
package me.andrew.EchoesOfGold;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class EventBossBar{
    private final EchoesOfGold plugin;
    private BossBar bossBar;
    private long duration;
    private long fullDuration;
    private BukkitRunnable task;

    public EventBossBar(EchoesOfGold plugin){
        this.plugin = plugin;
    }

    public void startBossBar(){
        bossBar = Bukkit.createBossBar(ChatColor.GOLD + plugin.getConfig().getString("bar-title"), BarColor.PURPLE, BarStyle.SOLID);

        //Displays the bar to every player
        for(Player p : Bukkit.getOnlinePlayers()){
            bossBar.addPlayer(p);
        }

        String eventDuration = plugin.getConfig().getString("event-duration");
        fullDuration = getFullDuration(eventDuration);
        //Displays the timer on the boss bar
        task = new BukkitRunnable() {
            @Override
            public void run() {
                duration = plugin.getEventProgressManager().getDuration();
                if(duration == 0){
                    bossBar.setProgress(0);
                    bossBar.setTitle(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("bar-title-ended")));
                    cancel();
                    return;
                }

                try{
                    String timeLeft = formatTime(duration);
                    bossBar.setTitle(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("bar-title-time-left").replace("%time_left%", timeLeft)));
                    double progress = (double) duration / fullDuration;
                    bossBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
                } catch (Exception e){
                    Bukkit.getLogger().warning("[ECHOES OF GOLD] There was an error with the boss bar.");
                    Bukkit.getLogger().warning("[ECHOES OF GOLD] "+e.getMessage());
                    task.cancel();
                }
            }
        };
        task.runTaskTimer(plugin, 0L, 20L);
    }

    //Builds the string for displaying the timer
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

    //Helps to get the full duration of the event
    private long getFullDuration(String input){
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

    public void stopBossBar(){
        if(task!=null) task.cancel();
        if(bossBar!= null) bossBar.removeAll();
    }

    public void addPlayer(Player player){
        if(bossBar != null) bossBar.addPlayer(player);
    }
}