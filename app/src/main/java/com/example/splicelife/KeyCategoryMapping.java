package com.example.splicelife;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

public class KeyCategoryMapping {
    private Map<String, String> keyCategoryMap = new HashMap<>();

    public KeyCategoryMapping(Context context) {
        loadKeyCategoryMapping(context);
    }

    private void loadKeyCategoryMapping(Context context) {
        String[] keyCategoryArray = context.getResources().getStringArray(R.array.beltParameters);
        for (String keyCategory : keyCategoryArray) {
            String[] parts = keyCategory.split(":");
            if (parts.length == 2) {
                String key = parts[0].trim();
                String category = parts[1].trim();
                keyCategoryMap.put(key, category);
            }
        }
    }

    public String getCategory(String key) {
        return keyCategoryMap.getOrDefault(key, "Uncategorized");
    }

    public Map<String, String> getKeyCategoryMap() {
        return keyCategoryMap;
    }
}
