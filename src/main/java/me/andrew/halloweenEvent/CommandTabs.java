package me.andrew.halloweenEvent;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
                return Arrays.asList("enable","disable","reload","books", "help");
            }
            else if(args.length == 2 && args[0].equalsIgnoreCase("books")){
                return Arrays.asList("create", "delete", "list");
            }
        }
        return new ArrayList<>();
    }
}
