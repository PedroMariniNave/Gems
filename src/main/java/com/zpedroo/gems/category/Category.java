package com.zpedroo.gems.category;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class Category {

    private FileConfiguration file;
    private String title;
    private Integer size;
    private List<CategoryItem> items;

    public Category(FileConfiguration file, String title, Integer size, List<CategoryItem> items) {
        this.file = file;
        this.title = title;
        this.size = size;
        this.items = items;
    }

    public FileConfiguration getFile() {
        return file;
    }

    public String getTitle() {
        return title;
    }

    public Integer getSize() {
        return size;
    }

    public List<CategoryItem> getItems() {
        return items;
    }
}