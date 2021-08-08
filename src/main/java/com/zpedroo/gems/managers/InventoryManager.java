package com.zpedroo.gems.managers;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InventoryManager {

    private static InventoryManager instance;
    public static InventoryManager getInstance() { return instance; }

    public InventoryManager() {
        instance = this;
    }

    public Integer getFreeSpace(Player player, ItemStack item) {
        int free = 0;

        for (int slot = 0; slot < 36; ++slot) {
            ItemStack items = player.getInventory().getItem(slot);
            if (items == null || items.getType().equals(Material.AIR)) {
                free += item.getMaxStackSize();
                continue;
            }

            if (!items.isSimilar(item)) continue;

            free += item.getMaxStackSize() - items.getAmount();
        }

        return free;
    }
}