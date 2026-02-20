//Developed by _ItsAndrew_
package me.andrew.EchoesOfGold;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

//This is for when you type in the command and you press TAB to auto complete
public class CommandTabs implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args){
        if(command.getName().equalsIgnoreCase("eog")){
            if(args.length == 1){
                return Arrays.asList("enable","disable","reload", "treasures", "help");
            }
        }

        return new ArrayList<>();
    }
}
