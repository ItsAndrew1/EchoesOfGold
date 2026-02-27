//Developed by _ItsAndrew_
package me.andrew.EchoesOfGold.Economy;

import org.bukkit.OfflinePlayer;

public interface EconomyProvider {
    //Method for getting the balance of a player
    double getBalance(OfflinePlayer Player);

    //Method for adding
    void addBalance(double amount, OfflinePlayer Player);

    //Method for withdrawing
    boolean withdrawBalance(double amount, OfflinePlayer Player);

    //Method for setting up accounts
    void setupAccounts();

    boolean hasEnough(OfflinePlayer player, double amount);
}
