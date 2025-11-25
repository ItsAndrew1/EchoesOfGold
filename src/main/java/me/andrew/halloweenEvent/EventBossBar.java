package me.andrew.halloweenEvent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


public class EventBossBar{
    private final TreasureHunt plugin;
    private BossBar bossBar;
    private long endTime;
    private BukkitRunnable task;

    public EventBossBar(TreasureHunt plugin){
        this.plugin = plugin;
    }

    public void start(long durationMillis){
        bossBar = Bukkit.createBossBar(ChatColor.GOLD + plugin.getConfig().getString("bar-title"), BarColor.PURPLE, BarStyle.SOLID);

        for(Player p : Bukkit.getOnlinePlayers()){
            bossBar.addPlayer(p);
        }

        this.endTime = System.currentTimeMillis() + durationMillis;
        task = new BukkitRunnable() {
            @Override
            public void run() {
                long remaining = endTime - System.currentTimeMillis();
                if(remaining <= 0){
                    bossBar.setProgress(0);
                    plugin.getTreasureManager().removeTreasures();
                    plugin.getTreasureManager().cancelAllTreasureParticles();
                    bossBar.setTitle(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("bar-title-ended")));
                    cancel();
                    return;
                }
                String timeLeft = formatTime(remaining);
                bossBar.setTitle(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("bar-title-time-left") + plugin.getConfig().getString("time-left-style") + timeLeft + plugin.getConfig().getString("bar-after-time")));
                double progress = (double) remaining / durationMillis;
                bossBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
            }
        };
        task.runTaskTimer(plugin, 0L, 20L);
    }

    //Formats the time
    public String formatTime(long millis){
        long seconds = millis/1000;
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

    public void stop(){
        if(task!=null) task.cancel();
        if(bossBar!= null) bossBar.removeAll();
    }

    public boolean isActive(){
        return task != null && !task.isCancelled();
    }
    public long getEndTime(){
        return endTime;
    }
    public void addPlayer(Player player){
        if(bossBar != null) bossBar.addPlayer(player);
    }
}