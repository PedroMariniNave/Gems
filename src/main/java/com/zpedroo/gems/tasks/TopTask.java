package com.zpedroo.gems.tasks;

import com.zpedroo.gems.Gems;
import com.zpedroo.gems.managers.DataManager;
import com.zpedroo.gems.mysql.DBConnection;
import org.bukkit.scheduler.BukkitRunnable;

import static com.zpedroo.gems.utils.config.Settings.TOP_UPDATE;

public class TopTask extends BukkitRunnable {

    public TopTask(Gems gems) {
        this.runTaskTimerAsynchronously(gems, TOP_UPDATE, TOP_UPDATE);
    }

    @Override
    public void run() {
        DataManager.getInstance().saveAll();
        DataManager.getInstance().getDataCache().setTop(DBConnection.getInstance().getDBManager().getTop());
    }
}