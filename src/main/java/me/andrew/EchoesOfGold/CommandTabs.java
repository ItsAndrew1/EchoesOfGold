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
    private final EchoesOfGold plugin;

    public CommandTabs(EchoesOfGold plugin){
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args){
        boolean toggleInternalEconomy = plugin.getConfig().getBoolean("economy.internal-economy.toggle", true);

        if(command.getName().equalsIgnoreCase("eog")){
            if(args.length == 1){
                List<String> tabs = new ArrayList<>();
                tabs.add("enable");
                tabs.add("disable");
                tabs.add("reload");
                tabs.add("treasures");
                if(toggleInternalEconomy) tabs.add("shop");
                tabs.add("help");
                return tabs;
            }

            if(args.length == 2 && args[0].equals("shop") && toggleInternalEconomy){
                return Arrays.asList("addItem", "removeItem");
            }
        }

        return new ArrayList<>();
    }
}
