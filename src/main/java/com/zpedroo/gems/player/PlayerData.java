package com.zpedroo.gems.player;

import com.zpedroo.gems.transactions.Transaction;
import com.zpedroo.gems.transactions.TransactionType;
import org.bukkit.OfflinePlayer;

import java.math.BigInteger;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class PlayerData {

    private UUID uuid;
    private BigInteger gems;
    private List<Transaction> transactions;
    private Boolean update;

    public PlayerData(UUID uuid, BigInteger gems, List<Transaction> transactions) {
        this.uuid = uuid;
        this.gems = gems;
        this.transactions = transactions == null ? new LinkedList<>() : transactions;
        this.update = false;
    }

    public UUID getUUID() {
        return uuid;
    }

    public BigInteger getGems() {
        return gems;
    }

    public void addGems(BigInteger amount) {
        if (amount.signum() < 0) return;

        this.gems = gems.add(amount);
        this.setQueue(true);
    }

    public void removeGems(BigInteger amount) {
        if (amount.signum() < 0) return;

        this.gems = gems.subtract(amount);
        this.setQueue(true);

        if (gems.signum() < 0) gems = BigInteger.ZERO;
    }

    public void setGems(BigInteger amount) {
        if (amount.signum() < 0) return;

        this.gems = amount;
        this.setQueue(true);

        if (gems.signum() < 0) gems = BigInteger.ZERO;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public Boolean isQueueUpdate() {
        return update;
    }

    public void setQueue(Boolean status) {
        this.update = status;
    }

    public void addTransaction(OfflinePlayer actor, OfflinePlayer target, BigInteger amount, TransactionType type, Integer id) {
        if (getTransactions().size() == 225) getTransactions().remove(0);

        getTransactions().add(new Transaction(actor, target, amount, type, new Date(), id));
    }
}