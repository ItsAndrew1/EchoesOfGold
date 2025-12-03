//Developed by _ItsAndrew_
package me.andrew.halloweenEvent.GUIs;

import me.andrew.halloweenEvent.TreasureHunt;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AllTreasuresGUI implements Listener{
    private final TreasureHunt plugin;
    private final NamespacedKey container;
    private String treasureID;

    public AllTreasuresGUI(TreasureHunt plugin){
        this.plugin = plugin;
        this.container = new NamespacedKey(plugin, "treasure-id");
    }

    public void showAllTreasuresGUI(Player player){
        FileConfiguration treasures = plugin.getTreasures().getConfig();
        int allTreasuresGuiSize = 54;
        Inventory allTreasuresGUI = Bukkit.createInventory(null, allTreasuresGuiSize, "Choose treasure");

        //Decorations
        for(int i = 0; i<9; i++){
            if(i == 4) continue;
            ItemStack blackStainedGlass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta meta = blackStainedGlass.getItemMeta();
            meta.setDisplayName(" ");

            blackStainedGlass.setItemMeta(meta);
            allTreasuresGUI.setItem(i, blackStainedGlass);
        }
        for(int i = 45; i<54; i++){
            ItemStack blackStainedGlass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta meta = blackStainedGlass.getItemMeta();
            meta.setDisplayName(" ");

            blackStainedGlass.setItemMeta(meta);
            allTreasuresGUI.setItem(i, blackStainedGlass);
        }

        //InfoSign
        ItemStack infoSignItem = new ItemStack(Material.OAK_SIGN);
        ItemMeta isMeta = infoSignItem.getItemMeta();
        isMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&fChoose a treasure to &l"+plugin.getTreasureManagerChoice()+"&f!"));
        infoSignItem.setItemMeta(isMeta);
        allTreasuresGUI.setItem(4, infoSignItem);

        //Return button
        ItemStack returnItem = new ItemStack(Material.SPECTRAL_ARROW);
        ItemMeta riMeta = returnItem.getItemMeta();
        riMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lRETURN"));
        returnItem.setItemMeta(riMeta);
        allTreasuresGUI.setItem(40, returnItem);

        //Displaying the treasures
        //Check if there are treasures configured
        if(treasures.getConfigurationSection("treasures") == null || treasures.getConfigurationSection("treasures").getKeys(false).isEmpty()){
            //NoTreasures item
            ItemStack noTreasuresItem = new ItemStack(Material.BARRIER);
            ItemMeta ntiMeta = noTreasuresItem.getItemMeta();

            ntiMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eThere are no treasures configured!"));
            noTreasuresItem.setItemMeta(ntiMeta);

            allTreasuresGUI.setItem(22, noTreasuresItem);
            player.openInventory(allTreasuresGUI);
            return;
        }

        //If the staff clicked on manage rewards
        if(plugin.getTreasureManagerChoice().equalsIgnoreCase("add rewards") || plugin.getTreasureManagerChoice().equalsIgnoreCase("remove rewards")){
            int slot = 10;
            for(String treasure : treasures.getConfigurationSection("treasures").getKeys(false)){
                if(slot == 17 || slot == 26) slot+=2; //This is for decoration purpose

                String mainPath = "treasures."+treasure;
                String treasureTitle = ChatColor.translateAlternateColorCodes('&', "&d&l"+treasures.getString(mainPath+".title"));

                ItemStack treasureItem = new ItemStack(Material.CHEST);
                ItemMeta tiMeta = treasureItem.getItemMeta();
                tiMeta.setDisplayName(treasureTitle); //The display name of each treasure

                //Adding the treasure inside the container
                tiMeta.getPersistentDataContainer().set(this.container, PersistentDataType.STRING, treasure);

                //Setting the lore of each treasure (items+enchantments)
                tiMeta.setLore(addingRewardsLore(treasure));

                treasureItem.setItemMeta(tiMeta);
                allTreasuresGUI.setItem(slot, treasureItem);
                slot++;
            }

            player.openInventory(allTreasuresGUI);
            return;
        }

        //If the staff clicked on manage treasures
        int slot = 10;
        for(String treasure : treasures.getConfigurationSection("treasures").getKeys(false)){
            //This is only for decoration purpose, so that the treasures are being displayed in a certain way
            if(slot == 17 || slot == 26) slot+=2;

            String mainPath = "treasures."+treasure;
            String treasureTitle = ChatColor.translateAlternateColorCodes('&', "&d&l"+treasures.getString(mainPath+".title"));

            ItemStack treasureItem = new ItemStack(Material.CHEST);
            ItemMeta tiMeta = treasureItem.getItemMeta();

            tiMeta.setDisplayName(treasureTitle);

            //Treasure lore
            int treasureX = treasures.getInt(mainPath+".x");
            int treasureY = treasures.getInt(mainPath+".y");
            int treasureZ = treasures.getInt(mainPath+".z");
            String treasureWorld = treasures.getString(mainPath+".world");
            String treasureFacing = treasures.getString(mainPath+".facing");

            List<String> coloredLore = new ArrayList<>();
            coloredLore.add(" ");
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', "&7 - Location: &l"+treasureX+" "+treasureY+" "+treasureZ));
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', "&7 - World: &l"+treasureWorld));
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', "&7 - Facing: &l"+treasureFacing));
            tiMeta.setLore(coloredLore);

            //Adding the treasure to the data container
            tiMeta.getPersistentDataContainer().set(this.container, PersistentDataType.STRING, treasure);

            treasureItem.setItemMeta(tiMeta);
            allTreasuresGUI.setItem(slot, treasureItem);
            slot++;
        }

        player.openInventory(allTreasuresGUI);
    }

    public List<String> addingRewardsLore(String treasureID){
        FileConfiguration treasures = plugin.getTreasures().getConfig();
        List<String> coloredLore = new ArrayList<>();

        //Check if there are any rewards for the treasure
        ConfigurationSection rewardsSection = treasures.getConfigurationSection("treasures."+treasureID+".rewards");
        if(rewardsSection == null || rewardsSection.getKeys(false).isEmpty()){
            coloredLore.add("");
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', "&cThere are no rewards for this treasure!"));
            return coloredLore;
        }

        coloredLore.add("");
        coloredLore.add(ChatColor.translateAlternateColorCodes('&', "&5ITEMS:"));
        for(String reward : treasures.getConfigurationSection("treasures."+treasureID+".rewards").getKeys(false)){
            int rewardQuantity = treasures.getInt("treasures."+treasureID+".rewards."+reward+".quantity");
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', " &7∘ "+reward+": "+rewardQuantity));

            //Check if the reward has enchantments
            if(treasures.isConfigurationSection("treasures."+treasureID+".rewards."+reward+".enchantments")){
                //Adding the reward's enchantments and levels
                for(String enchant : treasures.getConfigurationSection("treasures."+treasureID+".rewards."+reward+".enchantments").getKeys(false)){
                    int enchantLevel = treasures.getInt("treasures."+treasureID+".rewards."+reward+".enchantments."+enchant);
                    coloredLore.add(ChatColor.translateAlternateColorCodes('&', "     &6◦ "+enchant+": "+enchantLevel));
                }
            }
        }
        return coloredLore;
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent event){
        if(!(event.getWhoClicked() instanceof Player player)) return;
        if(!event.getView().getTitle().equalsIgnoreCase("Choose treasure")) return;
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if(clickedItem == null || clickedItem.getType() == Material.AIR) return;

        ItemMeta clickedItemMeta = clickedItem.getItemMeta();
        if(clickedItemMeta == null) return;

        NamespacedKey clickSoundKey = NamespacedKey.minecraft("ui.button.click");
        Sound clickSound = Registry.SOUNDS.get(clickSoundKey);
        String manageTreasureChoice = plugin.getTreasureManagerChoice();

        //If the player clicks on the return button
        Material returnButton = Material.SPECTRAL_ARROW;
        if(clickedItem.getType().equals(returnButton)){
            player.playSound(player.getLocation(), clickSound, 1f, 1f);
            plugin.getManageGUI().showMainManageGui(player);
        }

        //If the player clicks on the infoSign item
        Material infoSign = Material.OAK_SIGN;
        if(clickedItem.getType().equals(infoSign)) return;

        //If the player clicks on noTreasures item
        Material noTreasures = Material.BARRIER;
        if(clickedItem.getType().equals(noTreasures)) return;

        //Gets the treasure that it is clicked
        treasureID = clickedItemMeta.getPersistentDataContainer().get(this.container, PersistentDataType.STRING);
        if(treasureID == null) return;

        FileConfiguration treasures = plugin.getTreasures().getConfig();
        String chatPrefix = plugin.getConfig().getString("chat-prefix");
        String treasurePath = "treasures."+treasureID;

        Sound invalidCoord = Registry.SOUNDS.get(NamespacedKey.minecraft("block.note_block.bass"));
        Sound goodValue = Registry.SOUNDS.get(NamespacedKey.minecraft("entity.player.levelup"));
        switch(manageTreasureChoice){
            case "delete":
                treasures.set(treasurePath, null);
                plugin.getTreasures().saveConfig();

                player.playSound(player.getLocation(), goodValue, 1f, 1.4f);
                player.closeInventory();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &aTreasure &l"+treasureID+" &adeleted successfully."));

                //Re-opens the manageTreasuresGUI after 0.5 secs
                new BukkitRunnable(){
                    @Override
                    public void run(){
                        plugin.getManageGUI().showMainManageGui(player);
                    }
                }.runTaskLater(plugin, 10L);
                break;

            case "set location":
                player.closeInventory();

                //Setting the coordinates
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aEnter the X coord of the location:"));
                plugin.waitForPlayerInput(player, input1 -> {
                    //Coordinate X
                    int coordX;
                    try{
                        coordX = Integer.parseInt(input1);
                    } catch (Exception e){
                        player.playSound(player.getLocation(), invalidCoord, 1.2f, 1f);
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cThe coordinate must be a number."));

                        //Re-opens the manageTreasuresGUI after 0.5 secs
                        new BukkitRunnable(){
                            @Override
                            public void run(){
                                plugin.getManageGUI().showMainManageGui(player);
                            }
                        }.runTaskLater(plugin, 10L);
                        return;
                    }

                    treasures.set(treasurePath+".x", coordX);
                    player.playSound(player.getLocation(), goodValue, 1f, 1.4f);
                    plugin.getTreasures().saveConfig();

                    //Coordinate Y
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aEnter the Y coord of the location:"));
                    plugin.waitForPlayerInput(player, input2 -> {
                        int coordY;
                        try{
                            coordY = Integer.parseInt(input2);
                        } catch (Exception e){
                            player.playSound(player.getLocation(), invalidCoord, 1.3f, 1f);
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cThe coordinate must be a number."));

                            //Re-opens the manageTreasuresGUI after 0.5 secs
                            new BukkitRunnable(){
                                @Override
                                public void run(){
                                    plugin.getManageGUI().showMainManageGui(player);
                                }
                            }.runTaskLater(plugin, 10L);
                            return;
                        }

                        treasures.set(treasurePath+".y", coordY);
                        plugin.getTreasures().saveConfig();
                        player.playSound(player.getLocation(), goodValue, 1f, 1.4f);

                        //Coordinate Z
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aEnter the Z coord of the location: "));
                        plugin.waitForPlayerInput(player, input3 -> {
                            int coordZ;
                            try{
                                coordZ = Integer.parseInt(input3);
                            } catch (Exception e){
                                player.playSound(player.getLocation(), invalidCoord, 1.3f, 1f);
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cThe coordinate must be a number."));

                                //Re-opens the manageTreasuresGUI after 0.5 secs
                                new BukkitRunnable(){
                                    @Override
                                    public void run(){
                                        plugin.getManageGUI().showMainManageGui(player);
                                    }
                                }.runTaskLater(plugin, 10L);
                                return;
                            }

                            treasures.set(treasurePath+".z", coordZ);
                            plugin.getTreasures().saveConfig();
                            player.playSound(player.getLocation(), goodValue, 1f, 1.4f);

                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &aLocation &l"+input1+" "+input2+" "+input3+" &asaved for treasure &l"+treasureID+"&a!"));

                            //Re-opens the manageTreasuresGUI after 0.5 secs
                            new BukkitRunnable(){
                                @Override
                                public void run(){
                                    plugin.getManageGUI().showMainManageGui(player);
                                }
                            }.runTaskLater(plugin, 10L);
                        });
                    });
                });
                break;

            case "set world":
                player.closeInventory();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aEnter the world for treasure &l"+treasureID+"&a:"));

                //Setting the world for the treasure
                plugin.waitForPlayerInput(player, input -> {
                    treasures.set(treasurePath+".world", input);
                    plugin.getTreasures().saveConfig();
                    player.playSound(player.getLocation(), goodValue, 1f, 1.4f);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &aWorld &l"+input+" &asaved for treasure &l"+treasureID+"&a!"));

                    //Re-opens the mainManageGUI after 0.5 secs
                    new BukkitRunnable(){
                        @Override
                        public void run(){
                            plugin.getManageGUI().showMainManageGui(player);
                        }
                    }.runTaskLater(plugin, 10L);
                });
                break;

            case "set facing":
                player.closeInventory();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aEnter the facing for treasure &l"+treasureID+"&a:"));

                //Setting the facing for the treasure
                plugin.waitForPlayerInput(player, input -> {
                    //Checking if the facing is valid (long if xD)
                    if(input.equalsIgnoreCase("NORTH") || input.equalsIgnoreCase("SOUTH") || input.equalsIgnoreCase("EAST") || input.equalsIgnoreCase("WEST") || input.equalsIgnoreCase("SOUTH_WEST") || input.equalsIgnoreCase("SOUTH_EAST") || input.equalsIgnoreCase("NORTH_EAST") || input.equalsIgnoreCase("NORTH_WEST")){
                        treasures.set(treasurePath+".facing", input);
                        plugin.getTreasures().saveConfig();

                        player.playSound(player.getLocation(), goodValue, 1f, 1.4f);
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &aFacing &l"+input+" &asaved for treasure &l"+treasureID+"&a!"));

                        //Re-opens the mainManageGUI after 0.5 secs
                        new BukkitRunnable(){
                            @Override
                            public void run(){
                                plugin.getManageGUI().showMainManageGui(player);
                            }
                        }.runTaskLater(plugin, 10L);
                        return;
                    }

                    player.playSound(player.getLocation(), invalidCoord, 1.3f, 1f);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix+" &cThe value for facing is invalid!"));

                    //Re-opens the mainManageGUI after 0.5 secs
                    new BukkitRunnable(){
                        @Override
                        public void run(){
                            plugin.getManageGUI().showMainManageGui(player);
                        }
                    }.runTaskLater(plugin, 10L);
                });
                break;

            case "add rewards":
                player.playSound(player.getLocation(), clickSound, 1f, 1f);
                plugin.getRewardsGUI().showAddRewardsGUI(player, treasureID);
                break;

            case "remove rewards":
                player.closeInventory();
                Sound removeRewardsSound = Registry.SOUNDS.get(NamespacedKey.minecraft("block.note_block.pling"));

                //Check if the treasure has items in it
                if(!treasures.isConfigurationSection("treasures."+treasureID+".rewards")){
                    Sound noItemsFound = Registry.SOUNDS.get(NamespacedKey.minecraft("entity.villager.no"));
                    player.playSound(player.getLocation(), noItemsFound, 1f, 0.8f);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThere are no rewards in treasure &l"+treasureID+"&c!"));

                    //Shows the mainManageGUI to the player after 0.5 secs
                    new BukkitRunnable(){
                        @Override
                        public void run(){
                            plugin.getManageGUI().showMainManageGui(player);
                        }
                    }.runTaskLater(plugin, 10L);
                    return;
                }

                player.playSound(player.getLocation(), removeRewardsSound, 1f, 1f);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aEnter an item to &lremove &afrom treasure &l"+treasureID+"&a:"));
                plugin.waitForPlayerInput(player, input -> {
                    String itemToDelete = input;

                    //Check if the item exists in the designated treasure
                    if(!Objects.requireNonNull(treasures.getConfigurationSection("treasures."+treasureID+".rewards")).contains(itemToDelete)){
                        Sound itemNotFound = Registry.SOUNDS.get(NamespacedKey.minecraft("entity.enderman.teleport"));
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cItem &l"+itemToDelete+" &cnot found in treasure &l"+treasureID+"&c!"));
                        player.playSound(player.getLocation(), itemNotFound, 1f, 1f);

                        //Shows the mainManageGUI to the player after 0.5 secs
                        new BukkitRunnable(){
                            @Override
                            public void run(){
                                plugin.getManageGUI().showMainManageGui(player);
                            }
                        }.runTaskLater(plugin, 10L);
                        return;
                    }

                    //Sets the reward section to null
                    treasures.set("treasures."+treasureID+".rewards."+itemToDelete, null);
                    plugin.getTreasures().saveConfig();

                    Sound itemDeleted = Registry.SOUNDS.get(NamespacedKey.minecraft("entity.villager.celebrate"));
                    player.playSound(player.getLocation(), itemDeleted, 1f, 1.2f);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aItem &l"+itemToDelete+" &aremoved from treasure &l"+treasureID+"&a!"));

                    //Shows the mainManageGUI to the player after 0.5 secs
                    new BukkitRunnable(){
                        @Override
                        public void run(){
                            plugin.getManageGUI().showMainManageGui(player);
                        }
                    }.runTaskLater(plugin, 10L);
                });
                break;
        }
    }
}
