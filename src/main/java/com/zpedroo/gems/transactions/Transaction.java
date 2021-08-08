package com.zpedroo.gems.transactions;

import org.bukkit.OfflinePlayer;

import java.math.BigInteger;
import java.util.Date;

public class Transaction {

    private OfflinePlayer actor;
    private OfflinePlayer target;
    private BigInteger amount;
    private TransactionType type;
    private Date date;
    private Integer id;

    public Transaction(OfflinePlayer actor, OfflinePlayer target, BigInteger amount, TransactionType type, Date date, Integer id) {
        this.actor = actor;
        this.target = target;
        this.amount = amount;
        this.type = type;
        this.date = date;
        this.id = id;
    }

    public OfflinePlayer getActor() {
        return actor;
    }

    public OfflinePlayer getTarget() {
        return target;
    }

    public BigInteger getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public Date getDate() {
        return date;
    }

    public Integer getID() {
        return id;
    }
}