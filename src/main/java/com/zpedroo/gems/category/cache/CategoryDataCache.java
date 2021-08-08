package com.zpedroo.gems.category.cache;

import com.zpedroo.gems.category.Category;

import java.util.HashMap;

public class CategoryDataCache {

    private HashMap<String, Category> categories;

    public CategoryDataCache() {
        this.categories = new HashMap<>(32);
    }

    public HashMap<String, Category> getCategories() {
        return categories;
    }

    public Category getCategory(String category) {
        return categories.get(category);
    }
}