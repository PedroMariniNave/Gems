package com.zpedroo.gems.utils.menus;

import com.zpedroo.gems.category.Category;
import com.zpedroo.gems.category.CategoryItem;
import com.zpedroo.gems.listeners.PlayerChatListener;
import com.zpedroo.gems.managers.CategoryManager;
import com.zpedroo.gems.managers.DataManager;
import com.zpedroo.gems.player.PlayerData;
import com.zpedroo.gems.transactions.Transaction;
import com.zpedroo.gems.utils.FileUtils;
import com.zpedroo.gems.utils.builder.InventoryBuilder;
import com.zpedroo.gems.utils.builder.InventoryUtils;
import com.zpedroo.gems.utils.builder.ItemBuilder;
import com.zpedroo.gems.utils.config.Messages;
import com.zpedroo.gems.utils.formatter.NumberFormatter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Menus {

    private static Menus instance;
    public static Menus getInstance() { return instance; }

    private InventoryUtils inventoryUtils;

    public Menus() {
        instance = this;
        this.inventoryUtils = new InventoryUtils();
    }

    public void openShopMenu(Player player) {
        FileUtils.Files file = FileUtils.Files.SHOP;

        int size = FileUtils.get().getInt(file, "Inventory.size");
        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        Inventory inventory = Bukkit.createInventory(null, size, title);

        for (String items : FileUtils.get().getSection(file, "Inventory.items")) {
            if (items == null) continue;

            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + items).build();
            int slot = FileUtils.get().getInt(file, "Inventory.items." + items + ".slot");
            String actionStr = FileUtils.get().getString(file, "Inventory.items." + items + ".action");

            if (!StringUtils.equals(actionStr, "NULL")) {
                if (actionStr.contains("OPEN:")) {
                    String categoryName = actionStr.split(":")[1];
                    Category category = CategoryManager.getInstance().getCategoryDataCache().getCategory(categoryName);
                    if (category == null) continue;

                    getInventoryUtils().addAction(inventory, item, () -> {
                        openCategoryMenu(player, category);
                    }, InventoryUtils.ActionClick.ALL);
                }
            }

            inventory.setItem(slot, item);
        }

        player.openInventory(inventory);
    }

    public void openInfoMenu(Player player, OfflinePlayer target) {
        int size = FileUtils.get().getInt(FileUtils.Files.INFO, "Inventory.size");
        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(FileUtils.Files.INFO, "Inventory.title"));

        Inventory inventory = Bukkit.createInventory(null, size, title);

        for (String str : FileUtils.get().getSection(FileUtils.Files.INFO, "Inventory.items")) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.INFO).get(), "Inventory.items." + str, new String[]{
                    "{player}",
                    "{gems}"
            }, new String[]{
                    target.getName(),
                    NumberFormatter.getInstance().format(DataManager.getInstance().load(target).getGems())
            }).build();
            int slot = FileUtils.get().getInt(FileUtils.Files.INFO, "Inventory.items." + str + ".slot");

            inventory.setItem(slot, item);
        }

        player.openInventory(inventory);
    }

    public void openCategoryMenu(Player player, Category category) {
        FileConfiguration file = category.getFile();

        int size = category.getSize();
        String title = category.getTitle();
        Inventory inventory = Bukkit.createInventory(null, size, title);

        List<ItemBuilder> builders = new ArrayList<>(64);
        for (CategoryItem item : category.getItems()) {
            if (item == null) continue;

            ItemStack display = item.getDisplay().clone();
            int slot = item.getSlot();
            InventoryUtils.Action action = new InventoryUtils.Action(InventoryUtils.ActionClick.ALL, display, () -> {
                if (!item.needSelectAmount()) {
                    BigInteger finalPrice = item.getPrice().multiply(BigInteger.ONE);
                    PlayerData data = DataManager.getInstance().load(player);
                    if (data == null) return;

                    if (data.getGems().compareTo(finalPrice) < 0) {
                        player.sendMessage(Messages.INSUFFICIENT_GEMS);
                        return;
                    }

                    data.removeGems(finalPrice);
                    if (item.getShopItem() != null) {
                        player.getInventory().addItem(item.getShopItem());
                    }

                    for (String cmd : item.getCommands()) {
                        if (cmd == null) continue;

                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), StringUtils.replaceEach(cmd, new String[]{
                                "{player}",
                                "{amount}"
                        }, new String[]{
                                player.getName(),
                                "1"
                        }));
                    }

                    player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 100f);
                    return;
                }

                player.closeInventory();

                for (int i = 0; i < 25; ++i) {
                    player.sendMessage("");
                }

                for (String msg : Messages.CHOOSE_AMOUNT) {
                    if (msg == null) continue;

                    player.sendMessage(StringUtils.replaceEach(msg, new String[]{
                            "{item}",
                            "{price}"
                    }, new String[]{
                            item.getDisplay().hasItemMeta() ? item.getDisplay().getItemMeta().hasDisplayName() ? item.getDisplay().getItemMeta().getDisplayName() : item.getDisplay().getType().toString() : item.getDisplay().getType().toString(),
                            NumberFormatter.getInstance().format(item.getPrice())
                    }));
                }

                PlayerChatListener.getPlayerChat().put(player, new PlayerChatListener.PlayerChat(player, item));
            });

            builders.add(ItemBuilder.build(display, slot, action));
        }

        if (file.contains("Inventory.displays")) {
            for (String display : file.getConfigurationSection("Inventory.displays").getKeys(false)) {
                if (display == null) continue;

                ItemStack item = ItemBuilder.build(file, "Inventory.displays." + display).build();
                int slot = file.getInt("Inventory.displays." + display + ".slot");

                inventory.setItem(slot, item);
            }
        }

        InventoryBuilder.build(player, inventory, title, builders);
    }

    public void openMainMenu(Player player) {
        int size = FileUtils.get().getInt(FileUtils.Files.MAIN, "Inventory.size");
        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(FileUtils.Files.MAIN, "Inventory.title"));

        Inventory inventory = Bukkit.createInventory(null, size, title);
        List<ItemBuilder> builders = new ArrayList<>(32);

        for (String str : FileUtils.get().getSection(FileUtils.Files.MAIN, "Inventory.items")) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.MAIN).get(), "Inventory.items." + str, new String[]{
                    "{player}",
                    "{gems}"
            }, new String[]{
                    player.getName(),
                    NumberFormatter.getInstance().format(DataManager.getInstance().load(player).getGems())
            }).build();
            int slot = FileUtils.get().getInt(FileUtils.Files.MAIN, "Inventory.items." + str + ".slot");
            String actionStr = FileUtils.get().getString(FileUtils.Files.MAIN, "Inventory.items." + str + ".action");
            InventoryUtils.Action action = null;

            if (!StringUtils.equals(actionStr, "NULL")) {
                if (actionStr.contains("OPEN:")) {
                    String categoryName = actionStr.split(":")[1];
                    Category category = CategoryManager.getInstance().getCategoryDataCache().getCategory(categoryName);
                    if (category == null) continue;

                    getInventoryUtils().addAction(inventory, item, () -> {
                        openCategoryMenu(player, category);
                    }, InventoryUtils.ActionClick.ALL);
                }

                switch (actionStr) {
                    case "TRANSACTIONS" -> action = new InventoryUtils.Action(InventoryUtils.ActionClick.ALL, item, () -> {
                        openTransactionsMenu(player);
                    });
                    case "TOP" -> action = new InventoryUtils.Action(InventoryUtils.ActionClick.ALL, item, () -> {
                        openTopMenu(player);
                    });
                    case "SHOP" -> action = new InventoryUtils.Action(InventoryUtils.ActionClick.ALL, item, () -> {
                        openShopMenu(player);
                    });
                }
            }

            builders.add(ItemBuilder.build(item, slot, action));
        }

        InventoryBuilder.build(player, inventory, title, builders);
    }

    public void openTransactionsMenu(Player player) {
        int size = FileUtils.get().getInt(FileUtils.Files.TRANSACTIONS, "Inventory.size");
        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(FileUtils.Files.TRANSACTIONS, "Inventory.title"));

        Inventory inventory = Bukkit.createInventory(null, size, title);
        List<ItemBuilder> builders = new ArrayList<>(256);
        List<Transaction> transactions = DataManager.getInstance().load(player).getTransactions();

        ItemStack item = null;

        if (transactions.size() > 0) {
            int i = -1;
            String[] slots = FileUtils.get().getString(FileUtils.Files.TRANSACTIONS, "Inventory.slots").replace(" ", "").split(",");
            for (Transaction transaction : transactions) {
                if (++i >= slots.length) i = 0;

                String type = transaction.getType().toString().toLowerCase();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                item = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.TRANSACTIONS).get(), "Inventory.items." + type, new String[]{
                        "{actor}",
                        "{target}",
                        "{amount}",
                        "{date}",
                        "{id}"
                }, new String[]{
                        transaction.getActor().getName(),
                        transaction.getTarget().getName(),
                        NumberFormatter.getInstance().format(transaction.getAmount()),
                        dateFormat.format(transaction.getDate()),
                        String.valueOf(transaction.getID())
                }).build();
                int slot = Integer.parseInt(slots[i]);

                builders.add(ItemBuilder.build(item, slot, null));
            }
        } else {
            item = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.TRANSACTIONS).get(), "Empty").build();
            int slot = FileUtils.get().getInt(FileUtils.Files.TRANSACTIONS, "Empty.slot");

            builders.add(ItemBuilder.build(item, slot, null));
        }

        ItemStack nextPageItem = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.TRANSACTIONS).get(), "Next-Page").build();
        ItemStack previousPageItem = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.TRANSACTIONS).get(), "Previous-Page").build();

        int nextPageSlot = FileUtils.get().getInt(FileUtils.Files.TRANSACTIONS, "Next-Page.slot");
        int previousPageSlot = FileUtils.get().getInt(FileUtils.Files.TRANSACTIONS, "Previous-Page.slot");

        InventoryBuilder.build(player, inventory, title, builders, nextPageSlot, previousPageSlot, nextPageItem, previousPageItem);
    }

    public void openTopMenu(Player player) {
        int size = FileUtils.get().getInt(FileUtils.Files.TOP, "Inventory.size");
        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(FileUtils.Files.TOP, "Inventory.title"));

        Inventory inventory = Bukkit.createInventory(null, size, title);

        int pos = 0;
        String[] topSlots = FileUtils.get().getString(FileUtils.Files.TOP, "Inventory.slots").replace(" ", "").split(",");

        int slot = -1;
        ItemStack item = null;

        for (PlayerData data : DataManager.getInstance().getDataCache().getTop()) {
            slot = Integer.parseInt(topSlots[pos]);
            item = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.TOP).get(), "Item", new String[]{
                    "{player}",
                    "{pos}",
                    "{gems}"
            }, new String[]{
                    Bukkit.getOfflinePlayer(data.getUUID()).getName(),
                    String.valueOf(++pos),
                    NumberFormatter.getInstance().format(data.getGems())
            }).build();

            inventory.setItem(slot, item);
        }

        player.openInventory(inventory);
    }

    private InventoryUtils getInventoryUtils() {
        return inventoryUtils;
    }
}
