package com.plugin.myPlugin.factory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.alipay.sdk.app.PayTask;
import com.orhanobut.logger.Logger;
import com.plugin.myPlugin.utils.JsonWrapUtils;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by YX on 2017/8/20.
 */

public class PayAction extends IPluginAction {
    public static final String ACTION_WECHAT_CALLBACK = "ACTION_WECHAT_CALLBACK";
    private static final String PAY_TYPE_ALIPAY = "1";  //支付宝app
    private static final String PAY_TYPE_WECHAT = "3";  //微信app

    private CordovaPlugin mPlugin;
    private CallbackContext mCallbackContext;

    private BroadcastReceiver mWechatPayCallbackReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_WECHAT_CALLBACK.equals(intent.getAction())) {
                Logger.i(ACTION_WECHAT_CALLBACK + " " + intent.toString());
                payCallback(0 == intent.getIntExtra("pay_result", -1));
            }
        }
    };

    @Override
    public void doAction(CordovaPlugin plugin, JSONObject jsonObject, CallbackContext callbackContext) {
        mPlugin = plugin;
        mCallbackContext = callbackContext;

        String type = jsonObject.optString("type");
        String orderId = jsonObject.optString("orderId");
        Logger.i("type[1、支付宝app ;2、支付宝即时到账；3、微信app;4、微信公众号;5、微信扫码]:" + type + ", orderId:" + orderId);

        mPlugin.cordova.getActivity().registerReceiver(mWechatPayCallbackReceiver, new IntentFilter(ACTION_WECHAT_CALLBACK));

        // 通过订单号跑订单接口信息接口获取订单信息, 然后再根据订单信息下单支付.
//        getOrderInfo(type, orderId);
//        getOrderInfo(PAY_TYPE_ALIPAY, orderId);
        getOrderInfo(PAY_TYPE_WECHAT, orderId);
    }

    private void getOrderInfo(final String type, String orderId) {
        final String url = "http://39.108.54.14:8080/ygsh/api/pay/getPayInfo?type=" + type + "&ordersid=" + orderId;
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Logger.i("get payInfo from " + url);
                Logger.json(result);
                // {"code":1,"msg":"",
                // "data":"alipay_sdk=alipay-sdk-java-dynamicVersionNo&
                // app_id=2017082308344253&
                // biz_content=%7B%22body%22%3A%22%E9%98%B3%E5%85%89%E7%94%9F%E6%B4%BB%22%2C%22out_trade_no%22%3A%22170902195910002%22%2C%22product_code%22%3A%22QUICK_MSECURITY_PAY%22%2C%22subject%22%3A%22%E9%98%B3%E5%85%89%E7%94%9F%E6%B4%BB%22%2C%22timeout_express%22%3A%2230m%22%2C%22total_amount%22%3A%220.01%22%7D&charset=utf-8&format=json&method=alipay.trade.app.pay&notify_url=http%3A%2F%2F39.108.54.14%3A8080%2Fygsh%2Fapi%2Fpay%2FaliPayResult&sign=eaUjQ6kXX93IVWNX6eSazA5DDpe%2BML7Yqa%2BrY9gtHjEtxtAhPo2HcDnIDF1TaqsQP2r2ClsstvUsB4ZD5f6LyPkuD3u8KMrUyVKa%2FcxxX3veVLPQdzZsDr2H8KZ39iIb88rgtfbN3LeDUJ1RSSHhJtg%2F%2FMm3ScbZMemnbCUnK9oC40ojYK58Mm1fPxjjtw14panrM%2FNd6DnLFohEuz7ZxfzeIyE03%2BSJ3Zojv4EUj6X6ynxWoVuAvFk4OG7HokKP8c3kyp2Mq6N5c73dThef%2B8JlR1wTHqtDLo4h5IjTPEs5eL9XgKOUs5XkjoOFqLoNTtQF97tG6UEN4g23e%2F84Ug%3D%3D&sign_type=RSA2&timestamp=2017-09-02+19%3A59%3A15&version=1.0&sign=eaUjQ6kXX93IVWNX6eSazA5DDpe%2BML7Yqa%2BrY9gtHjEtxtAhPo2HcDnIDF1TaqsQP2r2ClsstvUsB4ZD5f6LyPkuD3u8KMrUyVKa%2FcxxX3veVLPQdzZsDr2H8KZ39iIb88rgtfbN3LeDUJ1RSSHhJtg%2F%2FMm3ScbZMemnbCUnK9oC40ojYK58Mm1fPxjjtw14panrM%2FNd6DnLFohEuz7ZxfzeIyE03%2BSJ3Zojv4EUj6X6ynxWoVuAvFk4OG7HokKP8c3kyp2Mq6N5c73dThef%2B8JlR1wTHqtDLo4h5IjTPEs5eL9XgKOUs5XkjoOFqLoNTtQF97tG6UEN4g23e%2F84Ug%3D%3D","totalCount":0,"curPage":0,"pageSize":0,"success":true}
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String orderInfo = jsonObject.getString("data");
                    orderPay(type, orderInfo);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void orderPay(String type, final String orderInfo) {
        if (PAY_TYPE_ALIPAY.equals(type)) {
            Runnable payRunnable = new Runnable() {

                @Override
                public void run() {
                    PayTask alipay = new PayTask(mPlugin.cordova.getActivity());
                    Map<String, String> result = alipay.payV2(orderInfo, true);
                    Logger.i("get payResult" + result.toString());

                    PayResult payResult = new PayResult(result);
                    String resultStatus = payResult.getResultStatus();

                    //支付结果的回调
                    payCallback(TextUtils.equals(resultStatus, "9000"));
                }
            };
            // 必须异步调用
            Thread payThread = new Thread(payRunnable);
            payThread.start();
        } else if (PAY_TYPE_WECHAT.equals(type)) {
            //{
            //    code: 1,
            //            msg: "",
            //        data: {
            //        package: "Sign=WXPay",
            //            code_url: null,
            //            appid: "wxd46fd7b11f8fb67f",
            //            sign: "EF012B1F3105C54758EBA499C9745FB1",
            //            partnerid: "1487000552",
            //            prepayid: "wx201709061843592bc717819e0114385436",
            //            noncestr: "xew02va77hxcf1isoa16vakb8c2avvly",
            //            timestamp: 1504694639
            //          },
            //    totalCount: 0,
            //            curPage: 0,
            //        pageSize: 0,
            //        success: true
            //}
            Logger.i("get payResult");
            Logger.json(orderInfo);
            try {
                JSONObject jsonObject = new JSONObject(orderInfo);

                String appId = jsonObject.getString("appid");
                String partnerId = jsonObject.getString("partnerid");
                String prepayId = jsonObject.getString("prepayid");
                String packageValue = jsonObject.getString("package");
                String nonceStr = jsonObject.getString("noncestr");
                String timestamp = jsonObject.getString("timestamp");
                String sign = jsonObject.getString("sign");

                IWXAPI iwxapi = WXAPIFactory.createWXAPI(mPlugin.cordova.getActivity(), appId);
                iwxapi.registerApp(appId);

                PayReq payRequest = new PayReq();
                payRequest.appId = appId;
                payRequest.partnerId = partnerId;
                payRequest.prepayId = prepayId;
                payRequest.packageValue = packageValue;
                payRequest.nonceStr = nonceStr;
                payRequest.timeStamp = timestamp;
                payRequest.sign = sign;

                iwxapi.sendReq(payRequest);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void payCallback(boolean success) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code", success ? "1" : "0");
            jsonObject.put("msg", success ? "支付成功" : "支付失败");
            if (success) {
                mCallbackContext.success(JsonWrapUtils.wrapData(jsonObject));
            } else {
                mCallbackContext.error(JsonWrapUtils.wrapData(jsonObject));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

class PayResult {
    private String resultStatus;
    private String result;
    private String memo;

    public PayResult(Map<String, String> rawResult) {
        if (rawResult == null) {
            return;
        }

        for (String key : rawResult.keySet()) {
            if (TextUtils.equals(key, "resultStatus")) {
                resultStatus = rawResult.get(key);
            } else if (TextUtils.equals(key, "result")) {
                result = rawResult.get(key);
            } else if (TextUtils.equals(key, "memo")) {
                memo = rawResult.get(key);
            }
        }
    }

    @Override
    public String toString() {
        return "resultStatus={" + resultStatus + "};memo={" + memo
                + "};result={" + result + "}";
    }

    /**
     * @return the resultStatus
     */
    public String getResultStatus() {
        return resultStatus;
    }

    /**
     * @return the memo
     */
    public String getMemo() {
        return memo;
    }

    /**
     * @return the result
     */
    public String getResult() {
        return result;
    }
}
