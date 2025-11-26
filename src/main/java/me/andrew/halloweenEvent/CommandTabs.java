//Developed by _ItsAndrew_
package me.andrew.halloweenEvent;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

//This is for when you type in the command and you press TAB to auto complete
public class CommandTabs implements TabCompleter {
    TreasureHunt plugin;
    public CommandTabs(TreasureHunt plugin){
        this.plugin = plugin;
    }
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args){

        if(command.getName().equalsIgnoreCase("treasurehunt")){
            if(args.length == 1){
                return Arrays.asList("enable","disable","reload", "hints", "help");
            }
            else if(args.length == 2 && args[0].equalsIgnoreCase("hints")){
                return Arrays.asList("create", "delete", "manage");
            }
            else if(args.length == 3 && args[1].equalsIgnoreCase("manage")){
                FileConfiguration books = plugin.getBooks().getConfig();
                List<String> hints = new ArrayList<>();
                hints.addAll(books.getConfigurationSection("books").getKeys(false));
                if(hints.isEmpty()){
                    return Collections.emptyList();
                }
                return hints;
            }
            else if(args.length == 4 && args[1].equalsIgnoreCase("manage")){
                return Arrays.asList("settitle", "setguislot", "setauthor");
            }
        }
        return new ArrayList<>();
    }
}
