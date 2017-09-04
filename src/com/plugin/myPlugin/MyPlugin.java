package com.plugin.myPlugin;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.plugin.myPlugin.factory.IPluginAction;
import com.plugin.myPlugin.factory.PluginActionFactory;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * This class echoes a string called from JavaScript.
 */
public class MyPlugin extends CordovaPlugin {
    private static final String TAG = "MyPlugin";
    IPluginAction mPluginAction;
    private Activity mActivity;
    private JSONObject mJsonObject;
    private CallbackContext mCallbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (args.length() <= 0) {
            return false;
        }
        Log.i(TAG, "Plugin execute action = " + action + " , args = " + args.toString());
        try {
            mJsonObject = args.getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mPluginAction = PluginActionFactory.createPluginAction(action);
        if (mPluginAction == null) {
            return false;
        }
        mActivity = this.cordova.getActivity();
        if (!checkPermission()) {
            mCallbackContext = callbackContext;
            mPluginAction.doAction(this, mJsonObject, mCallbackContext);
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG, "onActivityResult: " + requestCode + "resultCode" + resultCode);
        if (mPluginAction != null) {
            mPluginAction.onActivityResult(requestCode, resultCode, intent);
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    /**
     * 处理运行时权限回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     * @throws JSONException
     */
    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        Log.i(TAG, "onRequestPermissionResult: requestCode" + requestCode);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(mActivity, "必须同意所有权限才能正常使用本程序", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    mPluginAction.doAction(this, mJsonObject, mCallbackContext);
                } else {
                    Toast.makeText(mActivity, "发生未知错误", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
        if (mPluginAction != null) {
            mPluginAction.onRequestPermissionResult(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionResult(requestCode, permissions, grantResults);
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT < 23) {
            return false;
        }
        List<String> permissionList = new ArrayList<>();
        addPermission(permissionList, Manifest.permission.ACCESS_FINE_LOCATION);
        addPermission(permissionList, Manifest.permission.READ_PHONE_STATE);
        addPermission(permissionList, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        addPermission(permissionList, Manifest.permission.CALL_PHONE);
        addPermission(permissionList, Manifest.permission.CAMERA);
        //屏蔽掉的权限无法申请，待查看影响及原因。
        addPermission(permissionList, Manifest.permission.READ_LOGS);
        addPermission(permissionList, Manifest.permission.SET_DEBUG_APP);
        addPermission(permissionList, Manifest.permission.GET_ACCOUNTS);
        //此2种权限比较特殊，如果要申请，需要跳到设置界面中打开。
        // Settings.ACTION_MANAGE_OVERLAY_PERMISSIO、Settings.ACTION_MANAGE_WRITE_SETTINGS
//      addPermission(permissionList, Manifest.permission.SYSTEM_ALERT_WINDOW);
//      addPermission(permissionList, Manifest.permission.WRITE_APN_SETTINGS);
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            cordova.requestPermissions(this, 1, permissions);
            return true;
        } else {
            return false;
        }
    }

    private void addPermission(List<String> permissionList, String permission) {
        if (cordova.hasPermission(permission)) {
            permissionList.add(permission);
        }
    }
}
