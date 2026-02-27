//Developed by _ItsAndrew_
package me.andrew.EchoesOfGold.Economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class VaultEconomyProvider implements EconomyProvider{
    private final Economy vault;

    public VaultEconomyProvider(Economy vault){
        this.vault = vault;
    }

    @Override
    public double getBalance(OfflinePlayer player){
        if(vault.hasAccount(player)) return vault.getBalance(player);
        return 0;
    }

    @Override
    public void addBalance(double amount, OfflinePlayer player) {
        vault.depositPlayer(player, amount);
    }

    @Override
    public boolean withdrawBalance(double amount, OfflinePlayer player) {
        return vault.withdrawPlayer(player, amount).transactionSuccess();
    }

    @Override
    public void setupAccounts(){
        for(OfflinePlayer p : Bukkit.getOfflinePlayers()){
            if(!vault.hasAccount(p)) vault.createPlayerAccount(p);
        }
    }

    @Override
    public boolean hasEnough(OfflinePlayer player, double amount){
        return vault.has(player, amount);
    }
}
