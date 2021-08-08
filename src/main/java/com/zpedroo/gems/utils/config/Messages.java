package com.zpedroo.gems.utils.config;

import com.zpedroo.gems.utils.FileUtils;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class Messages {

    public static final String NEVER_SEEN = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.never-seen"));

    public static final String OFFLINE_PLAYER = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.offline-player"));

    public static final String INVALID_AMOUNT = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.invalid-amount"));

    public static final String TARGET_IS_SENDER = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.send-to-yourself"));

    public static final String INSUFFICIENT_GEMS = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.insufficient-gems"));

    public static final String NEED_SPACE = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.need-space"));

    public static final String PAY_MIN = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.pay-min"));

    public static final List<String> GEMS_RECEIVED = getColored(FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Messages.gems-received"));

    public static final List<String> GEMS_SENT = getColored(FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Messages.gems-sent"));

    public static final List<String> CONFIRM = getColored(FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Messages.confirm"));

    public static final List<String> CHOOSE_AMOUNT = getColored(FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Messages.choose-amount"));

    public static final List<String> SUCCESSFUL_PURCHASED = getColored(FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Messages.successful-purchased"));

    private static String getColored(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    private static List<String> getColored(List<String> list) {
        List<String> colored = new ArrayList<>();
        for (String str : list) {
            colored.add(getColored(str));
        }

        return colored;
    }
}