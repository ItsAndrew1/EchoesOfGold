//Developed by _ItsAndrew_
package me.andrew.EchoesOfGold.Economy;

import org.bukkit.configuration.file.FileConfiguration;

public enum ShopGuiChoice {
    SHOP {
        @Override
        public String getShopGuiTitle(FileConfiguration mainConfig) {
            return mainConfig.getString("economy.shop-gui.shop-title", "Event Shop");
        }
    },

    REMOVE_ITEM{
        @Override
        public String getShopGuiTitle(FileConfiguration mainConfig){
            return mainConfig.getString("economy.shop-gui.remove-item-title", "Click To Remove");
        }
    };

    public abstract String getShopGuiTitle(FileConfiguration mainConfig);
}
