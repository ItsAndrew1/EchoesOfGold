//Developed by _ItsAndrew_
package me.andrew.EchoesOfGold.Economy;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.andrew.EchoesOfGold.EchoesOfGold;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.*;

public class DatabaseManager {
    private final EchoesOfGold plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(EchoesOfGold plugin){
        this.plugin = plugin;
    }

    //Main method for making the connection with the database
    public void connectDB() throws SQLException{
        FileConfiguration mainConfig = plugin.getConfig();
        HikariConfig config = new HikariConfig();

        //Getting the database type
        String databaseType = mainConfig.getString("economy.internal-economy.database-system.type", "sqlite");

        //If the type is 'sqlite', it creates a .db file inside the EchoesOfGold folder (located in the plugins folder)
        if(databaseType.equals("sqlite")){
            String fileName = mainConfig.getString("economy.internal-economy.database-system.file-name", "database.db");
            File dbFile = new File(plugin.getDataFolder(), fileName);

            config.setDriverClassName("org.sqlite.JDBC");
            config.setMaximumPoolSize(1);
            config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());

            config.addDataSourceProperty("busy_timeout", 10000);
            config.addDataSourceProperty("journal_mode", "WAL");
            config.addDataSourceProperty("sync_mode", "NORMAL");
        }

        //If the type is 'mysql', it sets up the connection with the data from the config file
        else{
            String host = mainConfig.getString("economy.internal-economy.database-system.host");
            String port = mainConfig.getString("economy.internal-economy.database-system.port");
            String database = mainConfig.getString("economy.internal-economy.database-system.database");
            String username = mainConfig.getString("economy.internal-economy.database-system.username");
            String password = mainConfig.getString("economy.internal-economy.database-system.password");

            //If one of them is null, shuts down the plugin
            if(host == null || port == null || database == null || username == null || password == null){
                plugin.getLogger().warning("[E.O.G] The database is enabled but there is missing data! Disabling plugin...");
                plugin.getServer().getPluginManager().disablePlugin(plugin);
                return;
            }

            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database +
                    "?useSSL=false&allowPublicKeyRetrieval=true");
            config.setUsername(username);
            config.setPassword(password);

            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);

            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        }

        config.setPoolName("Echoes Of Gold Database Pool");
        config.setConnectionTimeout(10000);
        config.setMaxLifetime(1800000); //30 Minutes Max Lifetime for a connection
        this.dataSource = new HikariDataSource(config);

        //Creating the players table
        String playersTable = """
                CREATE TABLE IF NOT EXISTS players (
                crt INTEGER PRIMARY KEY AUTOINCREMENT,
                uuid TEXT UNIQUE NOT NULL,
                balance DECIMAL(12,2) DEFAULT 0.00
            );
        """;
        try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(playersTable)){
            statement.executeUpdate();
        }

        plugin.getLogger().info("[E.O.G] Database connection established!");
    }

    public boolean isPlayerInDatabase(String uuid){
        String query = "SELECT * FROM players WHERE uuid = ?";
        try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(query)){
            statement.setString(1, uuid);
            return statement.executeQuery().next();
        } catch (SQLException e){
            plugin.getLogger().warning("[E.O.G] There was an error checking if the player is in the database!");
            return false;
        }
    }

    public double getPlayerBalance(String uuid){
        String query = "SELECT balance FROM players WHERE uuid = ?";
        try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(query)){
            statement.setString(1, uuid);
            try(ResultSet resultSet = statement.executeQuery()){
                if(resultSet.next()) return resultSet.getDouble("balance");
            }
        } catch (SQLException e){
            plugin.getLogger().warning("[E.O.G] There was an error getting the player balance!");
        }

        return 0;
    }

    public void insertIntoPlayerBalance(String uuid, double amount){
        double playerBalance = 0;

        //Getting the player values
        String sql = "SELECT balance FROM players WHERE uuid = ?";
        try(Connection connection = dataSource.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)){
             ps.setString(1, uuid);
             try(ResultSet rs = ps.executeQuery()){
                 if(rs.next()) playerBalance = rs.getDouble("balance");
             }
        } catch (SQLException e){
            plugin.getLogger().warning("[E.O.G] There was an error inserting the player balance!");
        }

        //Updating the values with the amount
        playerBalance += amount;

        //Updating the values in the database
        String updateQuery = "UPDATE players SET balance = ? WHERE uuid = ?";
        try(Connection connection = dataSource.getConnection(); PreparedStatement ps = connection.prepareStatement(updateQuery)){
            ps.setDouble(1, playerBalance);
            ps.setString(2, uuid);
            ps.executeUpdate();
        } catch (SQLException e){
            plugin.getLogger().warning("[E.O.G] There was an error inserting the player balance!");
        }
    }

    public void withdrawFromPlayer(String uuid, double amount){
        String query = "UPDATE players SET balance = balance - ? WHERE uuid = ?";
        try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(query)){
            statement.setDouble(1, amount);
            statement.setString(2, uuid);
            statement.executeUpdate();
        } catch (SQLException e){
            plugin.getLogger().warning("[E.O.G] There was an error withdrawing from the player!");
        }
    }

    public HikariDataSource getDataSource(){
        return dataSource;
    }
}
