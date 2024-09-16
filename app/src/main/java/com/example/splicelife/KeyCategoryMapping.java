package com.example.splicelife;

import android.content.Context;
import android.content.res.Resources;

import java.util.HashMap;
import java.util.Map;

public class KeyCategoryMapping {
    private Map<String, String> keyCategoryMap = new HashMap<>();

    public KeyCategoryMapping(Context context) {
        loadKeyCategoryMapping(context);
    }

    private void loadKeyCategoryMapping(Context context) {
        Resources res = context.getResources();
        String[] categoryArray = res.getStringArray(R.array.categories);

        // Iterate through the categories and map each key to its category
        for (String category : categoryArray) {
            int arrayId = res.getIdentifier(category, "array", context.getPackageName());
            if (arrayId != 0) {
                mapKeysToCategory(res, arrayId, category);
            }
        }
    }

    private void mapKeysToCategory(Resources res, int arrayId, String category) {
        String[] keys = res.getStringArray(arrayId);
        for (String key : keys) {
            keyCategoryMap.put(key, category);
        }
    }

    public String getCategory(String key) {
        return keyCategoryMap.getOrDefault(key, "Uncategorized");
    }

    public Map<String, String> getKeyCategoryMap() {
        return keyCategoryMap;
    }
}
