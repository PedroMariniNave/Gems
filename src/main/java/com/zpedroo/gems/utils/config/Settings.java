package com.zpedroo.gems.utils.config;

import com.zpedroo.gems.utils.FileUtils;

import java.util.List;

public class Settings {

    public static final Integer TAX_PER_TRANSACTION = FileUtils.get().getInt(FileUtils.Files.CONFIG, "Settings.tax-per-transaction");

    public static final Long TOP_UPDATE = FileUtils.get().getLong(FileUtils.Files.CONFIG, "Settings.gems-top-update");

    public static final String GEMS_CMD = FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.commands.gems.cmd");

    public static final List<String> GEMS_ALIASES = FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Settings.commands.gems.aliases");
}