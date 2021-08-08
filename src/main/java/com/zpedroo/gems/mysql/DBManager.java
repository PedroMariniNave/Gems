package com.zpedroo.gems.mysql;

import com.zpedroo.gems.player.PlayerData;
import com.zpedroo.gems.transactions.Transaction;
import com.zpedroo.gems.transactions.TransactionType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.math.BigInteger;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class DBManager {

    public void save(PlayerData data) {
        if (contains(data.getUUID().toString(), "uuid")) {
            String query = "UPDATE `" + DBConnection.TABLE + "` SET" +
                    "`uuid`='" + data.getUUID().toString() + "', " +
                    "`gems`='" + data.getGems().toString() + "', " +
                    "`transactions`='" + serializeTransactions(data.getTransactions()) + "' " +
                    "WHERE `uuid`='" + data.getUUID().toString() + "';";
            executeUpdate(query);
            return;
        }

        String query = "INSERT INTO `" + DBConnection.TABLE + "` (`uuid`, `gems`, `transactions`) VALUES " +
                "('" + data.getUUID().toString() + "', " +
                "'" + data.getGems().toString() + "', " +
                "'" + serializeTransactions(data.getTransactions()) + "');";
        executeUpdate(query);
    }

    public HashMap<UUID, PlayerData> loadData() {
        HashMap<UUID, PlayerData> ret = new HashMap<>(2048);

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;
        String query = "SELECT * FROM `" + DBConnection.TABLE + "`;";

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            result = preparedStatement.executeQuery();

            while (result.next()) {
                UUID uuid = UUID.fromString(result.getString(1));
                BigInteger gems = result.getBigDecimal(2).toBigInteger();
                List<Transaction> transactions = deserializeTransactions(result.getString(3));

                ret.put(uuid, new PlayerData(uuid, gems, transactions));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnections(connection, result, preparedStatement, null);
        }

        return ret;
    }

    public PlayerData load(OfflinePlayer player) {
        if (!contains(player.getUniqueId().toString(), "uuid")) {
            PlayerData data = new PlayerData(player.getUniqueId(), BigInteger.ZERO, null);
            data.setQueue(true);
            return data;
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;
        String query = "SELECT * FROM `" + DBConnection.TABLE + "` WHERE `uuid`='" + player.getUniqueId().toString() + "';";

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            result = preparedStatement.executeQuery();

            if (result.next()) {
                UUID uuid = UUID.fromString(result.getString(1));
                BigInteger gems = result.getBigDecimal(2).toBigInteger();
                List<Transaction> transactions = deserializeTransactions(result.getString(3));

                return new PlayerData(uuid, gems, transactions);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnections(connection, result, preparedStatement, null);
        }

        return null;
    }

    public List<PlayerData> getTop() {
        List<PlayerData> top = new ArrayList<>(10);

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;
        String query = "SELECT * FROM `" + DBConnection.TABLE + "` ORDER BY `gems` DESC LIMIT 10;";

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            result = preparedStatement.executeQuery();

            while (result.next()) {
                UUID uuid = UUID.fromString(result.getString(1));
                BigInteger gems = result.getBigDecimal(2).toBigInteger();
                List<Transaction> transactions = deserializeTransactions(result.getString(3));

                top.add(new PlayerData(uuid, gems, transactions));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnections(connection, result, preparedStatement, null);
        }

        return top;
    }

    public Boolean contains(String value, String column) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;
        String query = "SELECT `" + column + "` FROM `" + DBConnection.TABLE + "` WHERE `" + column + "`='" + value + "';";

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            result = preparedStatement.executeQuery();
            return result.next();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnections(connection, result, preparedStatement, null);
        }

        return false;
    }

    private void executeUpdate(String query) {
        Connection connection = null;
        Statement statement = null;

        try {
            connection = getConnection();
            statement = connection.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnections(connection, null, null, statement);
        }
    }

    protected void createTable() {
        String query = "CREATE TABLE IF NOT EXISTS `" + DBConnection.TABLE + "` (`uuid` VARCHAR(255) NOT NULL, `gems` DECIMAL(40,0) NOT NULL DEFAULT '0', `transactions` LONGTEXT NOT NULL, PRIMARY KEY(`uuid`));";
        executeUpdate(query);
    }

    private void closeConnections(Connection connection, ResultSet resultSet, PreparedStatement preparedStatement, Statement statement) {
        try {
            if (connection != null) connection.close();
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
            if (statement != null) statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException, NullPointerException {
        return DBConnection.getInstance().getConnection();
    }

    private String serializeTransactions(List<Transaction> transactions) {
        StringBuilder serialized = new StringBuilder();
        for (Transaction transaction : transactions) {
            serialized.append(transaction.getActor().getName()).append("#")
                    .append(transaction.getTarget().getName()).append("#")
                    .append(transaction.getAmount().toString()).append("#")
                    .append(transaction.getType().toString()).append("#")
                    .append(transaction.getDate().getTime()).append("#")
                    .append(transaction.getID()).append(",");
        }

        return serialized.toString();
    }

    private List<Transaction> deserializeTransactions(String serialized) {
        if (serialized == null || serialized.isEmpty()) return null;

        List<Transaction> transactions = new LinkedList<>();

        String[] split = serialized.split(",");

        for (String str : split) {
            String[] strSplit = str.split("#");

            OfflinePlayer actor = Bukkit.getOfflinePlayer(strSplit[0]);
            OfflinePlayer target = Bukkit.getOfflinePlayer(strSplit[1]);
            BigInteger amount = new BigInteger(strSplit[2]);
            TransactionType type = TransactionType.valueOf(strSplit[3]);
            Date date = new Date(Long.parseLong(strSplit[4]));
            int id = Integer.parseInt(strSplit[5]);

            transactions.add(new Transaction(actor, target, amount, type, date, id));
        }

        return transactions;
    }
}