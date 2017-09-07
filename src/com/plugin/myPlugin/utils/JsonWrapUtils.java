package com.plugin.myPlugin.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * author:  Allen <br>
 * date:  2017/09/07 14:34<br>
 * description:
 */

public class JsonWrapUtils {
    private static final String TAG = "JsonWrapUtils";
    public static JSONObject wrapData(JSONObject jsonObject) {
        JSONObject result = new JSONObject();
        try {
            result.put("data", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "result callback" + jsonObject);
        return result;
    }
}
