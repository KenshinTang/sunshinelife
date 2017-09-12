package com.yunlinker.ygsh.wxapi;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.orhanobut.logger.Logger;
import com.plugin.myPlugin.factory.PayAction;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;


public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

    private static final String TAG = "WXPayEntryActivity";

    private IWXAPI mIWXAPI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 支付的id和key, 和登录的不一样!!
        // "wxd46fd7b11f8fb67f", "ae71ec7670176dc478a86dafdef48462"
        mIWXAPI = WXAPIFactory.createWXAPI(this, "wxd46fd7b11f8fb67f");
        mIWXAPI.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        mIWXAPI.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
    }

    @Override
    public void onResp(BaseResp resp) {
        Logger.d("onPayFinish, errCode = " + resp.errCode + ", errStr = " + resp.errStr + ", " + resp);

        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            Intent intent = new Intent(PayAction.ACTION_WECHAT_CALLBACK);
            intent.putExtra("pay_result", resp.errCode);
            sendBroadcast(intent);
        }
        finish();
    }
}