//Developed by _ItsAndrew_
package me.andrew.EchoesOfGold.Economy;

import me.andrew.EchoesOfGold.EchoesOfGold;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {
    private final EchoesOfGold plugin;
    private Connection dbConnection;

    public DatabaseManager(EchoesOfGold plugin){
        this.plugin = plugin;
    }

    //Main method for making the connection with the database
    public void connectDB() throws SQLException{
        FileConfiguration mainConfig = plugin.getConfig();

        //Getting the database type
        String databaseType = mainConfig.getString("economy.internal-economy.database-system.type", "sqlite");
        if(!databaseType.equals("sqlite") && !databaseType.equals("mysql")) databaseType = "sqlite";

        //If the type is 'sqlite', it creates a .db file inside the EchoesOfGold folder (located in the plugins folder)
        if(databaseType.equals("sqlite")){
            String fileName = mainConfig.getString("economy.internal-economy.database-system.file-name", "database.db");
            File dbFile = new File(plugin.getDataFolder(), fileName);

            String url = "jdbc:sqlite:"+dbFile.getAbsolutePath();
            dbConnection = DriverManager.getConnection(url);
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

            String url = "jdbc:mysql://"+host+":"+port+"/"+database +
                    "?useSSL=false&autoReconnect=true&characterEncoding=utf8";
            dbConnection = DriverManager.getConnection(url, username, password);
        }

        //Creating the players table
        String playersTable = """
                CREATE TABLE IF NOT EXISTS players (
                crt INTEGER PRIMARY KEY AUTO_INCREMENT,
                uuid TEXT UNIQUE NOT NULL,
                balance INTEGER DEFAULT 0
                balance_during_event INTEGER DEFAULT 0
                """;
        try(PreparedStatement statement = dbConnection.prepareStatement(playersTable)){
            statement.executeUpdate();
        }
    }

    public Connection getDbConnection(){
        return dbConnection;
    }
}
