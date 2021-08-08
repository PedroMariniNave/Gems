package com.zpedroo.gems.listeners;

import com.zpedroo.gems.managers.DataManager;
import com.zpedroo.gems.player.PlayerData;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;

public class PlayerGeneralListeners implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getItem() == null || event.getItem().getType().equals(Material.AIR)) return;

        ItemStack item = event.getItem().clone();
        NBTItem nbt = new NBTItem(item);
        if (!nbt.hasKey("GemsAmount")) return;

        Player player = event.getPlayer();
        PlayerData data = DataManager.getInstance().load(player);
        BigInteger amount = new BigInteger(nbt.getString("GemsAmount"));

        if (player.isSneaking()) {
            amount = amount.multiply(BigInteger.valueOf(item.getAmount()));
        } else {
            item.setAmount(1);
        }

        data.addGems(amount);
        player.getInventory().removeItem(item);

        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 10f, 0.5f);
    }
}