//Developed by _ItsAndrew_
package me.andrew.EchoesOfGold.Economy;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class InternalEconomyProvider implements EconomyProvider{
    private final DatabaseManager dbManager;
    private final Connection dbConnection;

    public InternalEconomyProvider(DatabaseManager dbManager){
        this.dbManager = dbManager;
        this.dbConnection = dbManager.getDbConnection();
    }

    @Override
    public double getBalance(OfflinePlayer Player) {
        return dbManager.getPlayerBalance(Player.getUniqueId().toString());
    }

    @Override
    public void addBalance(double amount, OfflinePlayer Player) {
        dbManager.insertIntoPlayerBalance(Player.getUniqueId().toString(), amount);
    }

    @Override
    public void withdrawBalance(double amount, OfflinePlayer Player) {
        dbManager.withdrawFromPlayer(Player.getUniqueId().toString(), amount);
    }

    @Override
    public void setupAccounts(){
        for(Player p : Bukkit.getOnlinePlayers()){
            if(dbManager.isPlayerInDatabase(p.getUniqueId().toString())) continue;

            String sql = "INSERT INTO players (uuid, balance) VALUES (?, 0)";
            try(PreparedStatement ps = dbConnection.prepareStatement(sql)){
                ps.setString(1, p.getUniqueId().toString());
                ps.executeUpdate();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean hasEnough(OfflinePlayer player, double amount){
        String query = "SELECT balance FROM players WHERE uuid = ?";
        try(PreparedStatement statement = dbConnection.prepareStatement(query)){
            statement.setString(1, player.getUniqueId().toString());
            try(ResultSet resultSet = statement.executeQuery()){
                if(resultSet.next()) return resultSet.getDouble("balance") >= amount;
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return false;
    }
}
