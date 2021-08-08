package com.zpedroo.gems.commands;

import com.zpedroo.gems.category.Category;
import com.zpedroo.gems.managers.CategoryManager;
import com.zpedroo.gems.managers.DataManager;
import com.zpedroo.gems.player.PlayerData;
import com.zpedroo.gems.transactions.TransactionType;
import com.zpedroo.gems.utils.FileUtils;
import com.zpedroo.gems.utils.builder.ItemBuilder;
import com.zpedroo.gems.utils.config.Messages;
import com.zpedroo.gems.utils.config.Settings;
import com.zpedroo.gems.utils.formatter.NumberFormatter;
import com.zpedroo.gems.utils.menus.Menus;
import de.tr7zw.nbtapi.NBTItem;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class GemsCmd implements CommandExecutor {

    private HashMap<Player, TransactionConfirm> confirm;
    private ItemStack item;

    public GemsCmd() {
        this.confirm = new HashMap<>(32);
        this.item = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.CONFIG).get(), "Item").build();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;
        OfflinePlayer target = null;

        PlayerData targetData = null;
        BigInteger amount = null;

        if (args.length == 1) {
            if (player == null) return true;

            switch (args[0].toUpperCase()) {
                case "TOP", "TOP10" -> {
                    Menus.getInstance().openTopMenu(player);
                    return true;
                }
            }

            target = Bukkit.getOfflinePlayer(args[0]);

            if (!DataManager.getInstance().hasAccount(target)) {
                player.sendMessage(Messages.NEVER_SEEN);
                return true;
            }

            Menus.getInstance().openInfoMenu(player, target);
            return true;
        }

        if (args.length == 3) {
            switch (args[0].toUpperCase()) {
                case "SEND", "PAY", "ENVIAR" -> {
                    if (player == null) return true;

                    target = Bukkit.getOfflinePlayer(args[1]);
                    if (StringUtils.equals(player.getName(), target.getName())) {
                        player.sendMessage(Messages.TARGET_IS_SENDER);
                        return true;
                    }

                    if (!DataManager.getInstance().hasAccount(target)) {
                        player.sendMessage(Messages.NEVER_SEEN);
                        return true;
                    }

                    amount = NumberFormatter.getInstance().filter(args[2]);
                    if (amount.signum() <= 0) {
                        player.sendMessage(Messages.INVALID_AMOUNT);
                        return true;
                    }

                    if (amount.compareTo(BigInteger.valueOf(Settings.TAX_PER_TRANSACTION)) < 0) {
                        player.sendMessage(StringUtils.replaceEach(Messages.PAY_MIN, new String[]{
                                "{tax}"
                        }, new String[]{
                                Settings.TAX_PER_TRANSACTION.toString()
                        }));
                        return true;
                    }

                    if (confirm.containsKey(player)) {
                        TransactionConfirm transactionConfirm = confirm.remove(player);
                        if (StringUtils.equals(transactionConfirm.getTarget().getUniqueId().toString(), target.getUniqueId().toString())) {
                            if (transactionConfirm.getAmount().compareTo(amount) == 0) {
                                transactionConfirm.confirm();
                                return true;
                            }
                        }

                        // if amount or target is different, a new transaction confirm will be created
                    }

                    BigInteger toGive = amount.subtract(amount.multiply(BigInteger.valueOf(Settings.TAX_PER_TRANSACTION)).divide(BigInteger.valueOf(100)));
                    TransactionConfirm transactionConfirm = new TransactionConfirm(player, target, amount, toGive);
                    confirm.put(player, transactionConfirm);

                    for (String msg : Messages.CONFIRM) {
                        if (msg == null) continue;

                        player.sendMessage(StringUtils.replaceEach(msg, new String[]{
                                "{target}",
                                "{amount}",
                                "{received}"
                        }, new String[]{
                                target.getName(),
                                NumberFormatter.getInstance().format(amount),
                                NumberFormatter.getInstance().format(toGive)
                        }));
                    }
                    return true;
                }

                case "GIVE", "ADD", "SET" -> {
                    if (!sender.hasPermission("gems.admin")) return true;

                    target = Bukkit.getOfflinePlayer(args[1]);
                    targetData = DataManager.getInstance().load(target);
                    if (!DataManager.getInstance().hasAccount(target)) {
                        sender.sendMessage(Messages.NEVER_SEEN);
                        return true;
                    }

                    amount = NumberFormatter.getInstance().filter(args[2]);
                    if (amount.signum() <= 0) {
                        sender.sendMessage(Messages.INVALID_AMOUNT);
                        return true;
                    }

                    String action = null;
                    if (StringUtils.equals(args[0].toUpperCase(), "GIVE")) {
                        targetData.addGems(amount);
                        action = "GIVE";
                    } else {
                        targetData.setGems(amount);
                        action = "SET";
                    }

                    if (player == null) return true;

                    Date date = new Date();
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                    addLog("[" + format.format(date) + "]: " + action + " | " + player.getName() + " -> " + target.getName() + " (" + NumberFormatter.getInstance().format(amount) + ")");
                    return true;
                }

                case "OPEN" -> {
                    if (!sender.hasPermission("gems.admin")) return true;

                    target = Bukkit.getPlayer(args[1]);
                    if (target == null) return true;

                    Category category = CategoryManager.getInstance().getCategoryDataCache().getCategory(args[2]);
                    if (category == null) return true;

                    Menus.getInstance().openCategoryMenu((Player) target, category);
                    return true;
                }

                case "ITEM" -> {
                    if (!sender.hasPermission("gems.admin")) return true;

                    target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage(Messages.OFFLINE_PLAYER);
                        return true;
                    }

                    amount = NumberFormatter.getInstance().filter(args[2]);
                    if (amount.signum() <= 0) {
                        sender.sendMessage(Messages.INVALID_AMOUNT);
                        return true;
                    }

                    ((Player) target).getInventory().addItem(getItem(amount));

                    if (player == null) return true;

                    Date date = new Date();
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                    addLog("[" + format.format(date) + "]: ITEM | " + player.getName() + " -> " + target.getName() + " (" + NumberFormatter.getInstance().format(amount) + ")");
                    return true;
                }
            }
        }

        if (player == null) return true;

        Menus.getInstance().openMainMenu(player);
        return true;
    }

    private void addLog(String log) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(FileUtils.get().getFile(FileUtils.Files.LOGS).getFile().getCanonicalPath(), true));
            writer.newLine();
            writer.write(log);
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    class TransactionConfirm {

        private Player player;
        private OfflinePlayer target;
        private BigInteger amount;
        private BigInteger toGive;

        public TransactionConfirm(Player player, OfflinePlayer target, BigInteger amount, BigInteger toGive) {
            this.player = player;
            this.target = target;
            this.amount = amount;
            this.toGive = toGive;
        }

        public Player getPlayer() {
            return player;
        }

        public OfflinePlayer getTarget() {
            return target;
        }

        public BigInteger getAmount() {
            return amount;
        }

        public BigInteger getToGive() {
            return toGive;
        }

        public void confirm() {
            PlayerData playerData = DataManager.getInstance().load(player);
            if (playerData.getGems().compareTo(amount) < 0) {
                player.sendMessage(Messages.INSUFFICIENT_GEMS);
                return;
            }

            Integer id = FileUtils.get().getInt(FileUtils.Files.TRANSACTIONS, "last-id") + 1;
            PlayerData targetData = DataManager.getInstance().load(target);

            playerData.removeGems(amount);
            targetData.addGems(toGive);
            playerData.addTransaction(player, target, amount, TransactionType.REMOVE, id);
            targetData.addTransaction(player, target, toGive, TransactionType.ADD, id);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

            for (String msg : Messages.GEMS_SENT) {
                player.sendMessage(StringUtils.replaceEach(msg, new String[]{
                        "{target}",
                        "{amount}",
                        "{received}",
                        "{id}"
                }, new String[]{
                        target.getName(),
                        NumberFormatter.getInstance().format(amount),
                        NumberFormatter.getInstance().format(toGive),
                        id.toString()
                }));
            }

            Player targetPlayer = Bukkit.getPlayer(target.getUniqueId()); // need to be player to play sound
            if (targetPlayer != null) {
                targetPlayer.playSound(targetPlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                for (String msg : Messages.GEMS_RECEIVED) {
                    targetPlayer.sendMessage(StringUtils.replaceEach(msg, new String[]{
                            "{actor}",
                            "{amount}",
                            "{received}",
                            "{id}"
                    }, new String[]{
                            player.getName(),
                            NumberFormatter.getInstance().format(amount),
                            NumberFormatter.getInstance().format(toGive),
                            id.toString()
                    }));
                }
            }

            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            addLog("[" + format.format(date) + "]: SEND | " + player.getName() + " -> " + target.getName() + " (" + NumberFormatter.getInstance().format(amount) + ") [ID: " + id + "]");
            FileUtils.get().getFile(FileUtils.Files.TRANSACTIONS).get().set("last-id", id);
            FileUtils.get().getFile(FileUtils.Files.TRANSACTIONS).save();
        }
    }

    private ItemStack getItem(BigInteger amount) {
        NBTItem nbt = new NBTItem(item.clone());
        nbt.setString("GemsAmount", amount.toString());

        ItemStack item = nbt.getItem();

        if (item.getItemMeta() != null) {
            String displayName = item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : null;
            List<String> lore = item.getItemMeta().hasLore() ? item.getItemMeta().getLore() : null;
            ItemMeta meta = item.getItemMeta();

            if (displayName != null) meta.setDisplayName(StringUtils.replaceEach(displayName, new String[] {
                    "{amount}"
            }, new String[] {
                    NumberFormatter.getInstance().format(amount)
            }));

            if (lore != null) {
                List<String> newLore = new ArrayList<>(lore.size());

                for (String str : lore) {
                    newLore.add(StringUtils.replaceEach(str, new String[] {
                            "{amount}"
                    }, new String[] {
                            NumberFormatter.getInstance().format(amount)
                    }));
                }

                meta.setLore(newLore);
            }

            item.setItemMeta(meta);
        }

        return item;
    }
}