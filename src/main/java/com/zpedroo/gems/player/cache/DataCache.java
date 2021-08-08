package com.zpedroo.gems.player.cache;

import com.zpedroo.gems.mysql.DBConnection;
import com.zpedroo.gems.player.PlayerData;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DataCache {

    private HashMap<UUID, PlayerData> playerDataCache;
    private List<PlayerData> top;

    public DataCache() {
        this.playerDataCache = DBConnection.getInstance().getDBManager().loadData();
        this.top = DBConnection.getInstance().getDBManager().getTop();
    }

    public HashMap<UUID, PlayerData> getPlayerData() {
        return playerDataCache;
    }

    public PlayerData getPlayerData(UUID uuid) {
        if (!playerDataCache.containsKey(uuid)) return null;

        return playerDataCache.get(uuid);
    }

    public List<PlayerData> getTop() {
        return top;
    }

    public void setTop(List<PlayerData> top) {
        this.top = top;
    }
}