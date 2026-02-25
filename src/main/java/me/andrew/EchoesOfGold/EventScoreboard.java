//Developed by _ItsAndrew_
package me.andrew.EchoesOfGold;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EventScoreboard{
    EchoesOfGold plugin;
    BukkitRunnable task;

    public EventScoreboard(EchoesOfGold plugin){
        this.plugin = plugin;
    }

    public void startScoreboard(){
        int interval = plugin.getConfig().getInt("scoreboard-interval") * 20;

        task = new BukkitRunnable(){
            @Override
            public void run(){
                for(Player player : Bukkit.getOnlinePlayers()){
                    updateScoreboard(player);
                }
            }
        };
        task.runTaskTimer(plugin, 0L, interval);
    }

    public void updateScoreboard(Player player){
        plugin.getTreasures().reloadConfig();
        plugin.getPlayerData().reloadConfig();
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.getObjective("sidebar");
        FileConfiguration playerdata = plugin.getPlayerData().getConfig();
        FileConfiguration treasures = plugin.getTreasures().getConfig();

        if(objective == null){
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            objective = scoreboard.registerNewObjective("sidebar", "dummy", ChatColor.translateAlternateColorCodes('&',plugin.getConfig().getString("scoreboard-title")));
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        for(String entry:scoreboard.getEntries()){
            scoreboard.resetScores(entry);
        }

        List<String> lines = plugin.getConfig().getStringList("scoreboard-lines");

        int score = lines.size();
        for(String line : lines){
            //Skipping the line if it is the one displaying the coins (if the economy isn't toggled/doesn't work)
            if(!isEconomyWorking() && line.contains("%coins_gathered%")) continue;

            List<Map.Entry<UUID, Integer>> top = plugin.getTreasureManager().getTopPlayers();
            UUID top1UUID = !top.isEmpty() ? top.getFirst().getKey() : null;
            UUID top2UUID = top.size() > 1 ? top.get(1).getKey() : null;
            UUID top3UUID = top.size() > 2 ? top.get(2).getKey() : null;
            int top1count = !top.isEmpty() ? top.getFirst().getValue() : 0;
            int top2count = top.size() > 1 ? top.get(1).getValue() : 0;
            int top3count = top.size() > 2 ? top.get(2).getValue() : 0;

            String top1name = top1UUID != null ? Bukkit.getOfflinePlayer(top1UUID).getName() : "None";
            String top2name = top2UUID != null ? Bukkit.getOfflinePlayer(top2UUID).getName() : "None";
            String top3name = top3UUID != null ? Bukkit.getOfflinePlayer(top3UUID).getName() : "None";
            String parsed = line
                    .replace("%player_name%", player.getName())
                    .replace("%player_treasures%", String.valueOf(playerdata.getInt("players." + player.getUniqueId() + ".treasures-found")))
                    .replace("%max_treasures%", String.valueOf(treasures.getInt("nr-of-treasures")))
                    .replace("%top1_name%", top1name)
                    .replace("%top2_name%", top2name)
                    .replace("%top3_name%", top3name)
                    .replace("%top1_count%", String.valueOf(top1count))
                    .replace("%top2_count%", String.valueOf(top2count))
                    .replace("%coins_gathered%", String.valueOf(playerdata.getDouble("players." + player.getUniqueId() + ".coins-gathered")))
                    .replace("%top3_count%", String.valueOf(top3count));
            String coloredLine = ChatColor.translateAlternateColorCodes('&', parsed);

            while(scoreboard.getEntries().contains(coloredLine)){
                coloredLine+= ChatColor.COLOR_CHAR;
            }
            objective.getScore(coloredLine).setScore(score);
            score--;
        }
        player.setScoreboard(scoreboard);
    }

    public void stopScoreboard(Player player){
        //If scoreboard isn't toggled, it returns
        boolean toggleScoreboard = plugin.getConfig().getBoolean("scoreboard");
        if(!toggleScoreboard) return;

        task.cancel();
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    private boolean isEconomyWorking(){
        FileConfiguration mainConfig = plugin.getConfig();

        //Checking if the economy is toggled
        boolean toggleEconomy = mainConfig.getBoolean("economy.toggle-using-economy");
        if(!toggleEconomy) return false;

        //Checking if the economy provider isn't null
        return plugin.getEconomy() != null;
    }
}
