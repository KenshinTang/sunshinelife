package com.plugin.myPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * author:  Allen <br>
 * date:  2017/8/25/025 17:37 <br>
 * description:
 */

public class Properties {
    private static Map<String, String> jumpClassMapping = new HashMap<>();

    public static void addProperties(String key, String value) {
        jumpClassMapping.put(key, value);
    }

    public static String getProperties(String key) {
        return jumpClassMapping.get(key);
    }

}
