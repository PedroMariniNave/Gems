package com.zpedroo.gems.managers;

import com.zpedroo.gems.mysql.DBConnection;
import com.zpedroo.gems.player.PlayerData;
import com.zpedroo.gems.player.cache.DataCache;
import org.bukkit.OfflinePlayer;

import java.util.HashSet;

public class DataManager {

    private static DataManager instance;
    public static DataManager getInstance() { return instance; }

    private DataCache dataCache;

    public DataManager() {
        instance = this;
        this.dataCache = new DataCache();
    }

    public PlayerData load(OfflinePlayer player) {
        if (getDataCache().getPlayerData(player.getUniqueId()) != null) return getDataCache().getPlayerData(player.getUniqueId());

        PlayerData data = DBConnection.getInstance().getDBManager().load(player);
        cache(data);

        return data;
    }

    public void saveAll() {
        new HashSet<>(getDataCache().getPlayerData().values()).forEach(data -> {
            if (data == null) return;
            if (!data.isQueueUpdate()) return;

            DBConnection.getInstance().getDBManager().save(data);
        });
    }

    public DataCache getDataCache() {
        return dataCache;
    }

    public Boolean hasAccount(OfflinePlayer player) {
        if (getDataCache().getPlayerData().containsKey(player.getUniqueId())) return true;

        return DBConnection.getInstance().getDBManager().contains(player.getUniqueId().toString(), "uuid");
    }

    private void cache(PlayerData data) {
        getDataCache().getPlayerData().put(data.getUUID(), data);
    }
}
