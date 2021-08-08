package com.zpedroo.gems.listeners;

import br.com.devpaulo.legendchat.api.events.ChatMessageEvent;
import com.zpedroo.gems.Gems;
import com.zpedroo.gems.category.CategoryItem;
import com.zpedroo.gems.managers.DataManager;
import com.zpedroo.gems.managers.InventoryManager;
import com.zpedroo.gems.player.PlayerData;
import com.zpedroo.gems.utils.config.Messages;
import com.zpedroo.gems.utils.formatter.NumberFormatter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.math.BigInteger;
import java.util.HashMap;

public class PlayerChatListener implements Listener {

    private static HashMap<Player, PlayerChat> playerChat;

    static {
        playerChat = new HashMap<>(16);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(ChatMessageEvent event) {
        if (!getPlayerChat().containsKey(event.getSender())) return;

        event.setCancelled(true);

        PlayerChat playerChat = getPlayerChat().remove(event.getSender());
        Player player = playerChat.getPlayer();

        BigInteger amount = NumberFormatter.getInstance().filter(event.getMessage());

        if (amount.signum() <= 0) {
            player.sendMessage(Messages.INVALID_AMOUNT);
            return;
        }

        CategoryItem categoryItem = playerChat.getCategoryItem();

        BigInteger finalPrice = categoryItem.getPrice().multiply(amount);
        PlayerData data = DataManager.getInstance().load(player);
        if (data == null) return;

        if (data.getGems().compareTo(finalPrice) < 0) {
            player.sendMessage(Messages.INSUFFICIENT_GEMS);
            return;
        }

        Integer limit = categoryItem.getShopItem() != null ? (categoryItem.getShopItem().getMaxStackSize() == 1 ? 36 : 2304) : -1;
        if (limit != -1 && amount.compareTo(BigInteger.valueOf(limit)) > 0) amount = BigInteger.valueOf(limit);

        if (categoryItem.getShopItem() != null) {
            Integer freeSpace = InventoryManager.getInstance().getFreeSpace(player, categoryItem.getShopItem());
            if (BigInteger.valueOf(freeSpace).compareTo(amount) < 0) {
                player.sendMessage(StringUtils.replaceEach(Messages.NEED_SPACE, new String[]{
                        "{has}",
                        "{need}"
                }, new String[]{
                        NumberFormatter.getInstance().formatDecimal(freeSpace.doubleValue()),
                        NumberFormatter.getInstance().formatDecimal(amount.doubleValue())
                }));
                return;
            }
        }

        data.removeGems(finalPrice);
        if (categoryItem.getShopItem() != null) {
            for (int i = 0; i < amount.intValue(); ++i) {
                player.getInventory().addItem(categoryItem.getShopItem());
            }
        }

        for (String cmd : categoryItem.getCommands()) {
            if (cmd == null) continue;

            final BigInteger finalAmount = amount;
            Gems.get().getServer().getScheduler().runTaskLater(Gems.get(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), StringUtils.replaceEach(cmd, new String[]{
                    "{player}",
                    "{amount}"
            }, new String[]{
                    player.getName(),
                    finalAmount.multiply(categoryItem.getDefaultAmount()).toString()
            })), 0L);
        }

        for (String msg : Messages.SUCCESSFUL_PURCHASED) {
            if (msg == null) continue;

            player.sendMessage(StringUtils.replaceEach(msg, new String[]{
                    "{item}",
                    "{amount}",
                    "{price}"
            }, new String[]{
                    categoryItem.getDisplay().hasItemMeta() ? categoryItem.getDisplay().getItemMeta().hasDisplayName() ? categoryItem.getDisplay().getItemMeta().getDisplayName() : categoryItem.getDisplay().getType().toString() : categoryItem.getDisplay().getType().toString(),
                    NumberFormatter.getInstance().formatDecimal(amount.doubleValue()),
                    NumberFormatter.getInstance().format(finalPrice)
            }));
        }

        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 100f);
    }

    public static HashMap<Player, PlayerChat> getPlayerChat() {
        return playerChat;
    }

    public static class PlayerChat {

        private Player player;
        private CategoryItem categoryItem;

        public PlayerChat(Player player, CategoryItem categoryItem) {
            this.player = player;
            this.categoryItem = categoryItem;
        }

        public Player getPlayer() {
            return player;
        }

        public CategoryItem getCategoryItem() {
            return categoryItem;
        }
    }
}