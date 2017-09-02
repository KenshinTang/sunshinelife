package com.plugin.myPlugin.factory;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.alipay.sdk.app.PayTask;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by YX on 2017/8/20.
 */

public class PayAction extends IPluginAction {
    private static final String TAG = "PayAction";

    private static final String PAY_TYPE_ALIPAY = "1";  //支付宝app
    private static final String PAY_TYPE_WECHAT = "3";  //微信app

    private CordovaPlugin mPlugin;
    private CallbackContext mCallbackContext;

    @Override
    public void doAction(CordovaPlugin plugin, JSONObject jsonObject, CallbackContext callbackContext) {
        mPlugin = plugin;
        mCallbackContext = callbackContext;

        String type = jsonObject.optString("type");
        String orderId = jsonObject.optString("orderId");
        Log.i(TAG, "type[1、支付宝app ;2、支付宝即时到账；3、微信app;4、微信公众号;5、微信扫码]:" + type + ", orderId:" + orderId);

        // 通过订单号跑订单接口信息接口获取订单信息, 然后再根据订单信息下单支付.
        getOrderInfo(type, orderId);
    }

    private void getOrderInfo(final String type, String orderId) {
        String url = "http://39.108.54.14:8080/ygsh/api/pay/getPayInfo?type=" + type + "&ordersid=" + orderId;
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
                Log.i(TAG, "get payInfo : " + result);
                // {"code":1,"msg":"",
                // "data":"alipay_sdk=alipay-sdk-java-dynamicVersionNo&app_id=2017082308344253&biz_content=%7B%22body%22%3A%22%E9%98%B3%E5%85%89%E7%94%9F%E6%B4%BB%22%2C%22out_trade_no%22%3A%22170902195910002%22%2C%22product_code%22%3A%22QUICK_MSECURITY_PAY%22%2C%22subject%22%3A%22%E9%98%B3%E5%85%89%E7%94%9F%E6%B4%BB%22%2C%22timeout_express%22%3A%2230m%22%2C%22total_amount%22%3A%220.01%22%7D&charset=utf-8&format=json&method=alipay.trade.app.pay&notify_url=http%3A%2F%2F39.108.54.14%3A8080%2Fygsh%2Fapi%2Fpay%2FaliPayResult&sign=eaUjQ6kXX93IVWNX6eSazA5DDpe%2BML7Yqa%2BrY9gtHjEtxtAhPo2HcDnIDF1TaqsQP2r2ClsstvUsB4ZD5f6LyPkuD3u8KMrUyVKa%2FcxxX3veVLPQdzZsDr2H8KZ39iIb88rgtfbN3LeDUJ1RSSHhJtg%2F%2FMm3ScbZMemnbCUnK9oC40ojYK58Mm1fPxjjtw14panrM%2FNd6DnLFohEuz7ZxfzeIyE03%2BSJ3Zojv4EUj6X6ynxWoVuAvFk4OG7HokKP8c3kyp2Mq6N5c73dThef%2B8JlR1wTHqtDLo4h5IjTPEs5eL9XgKOUs5XkjoOFqLoNTtQF97tG6UEN4g23e%2F84Ug%3D%3D&sign_type=RSA2&timestamp=2017-09-02+19%3A59%3A15&version=1.0&sign=eaUjQ6kXX93IVWNX6eSazA5DDpe%2BML7Yqa%2BrY9gtHjEtxtAhPo2HcDnIDF1TaqsQP2r2ClsstvUsB4ZD5f6LyPkuD3u8KMrUyVKa%2FcxxX3veVLPQdzZsDr2H8KZ39iIb88rgtfbN3LeDUJ1RSSHhJtg%2F%2FMm3ScbZMemnbCUnK9oC40ojYK58Mm1fPxjjtw14panrM%2FNd6DnLFohEuz7ZxfzeIyE03%2BSJ3Zojv4EUj6X6ynxWoVuAvFk4OG7HokKP8c3kyp2Mq6N5c73dThef%2B8JlR1wTHqtDLo4h5IjTPEs5eL9XgKOUs5XkjoOFqLoNTtQF97tG6UEN4g23e%2F84Ug%3D%3D","totalCount":0,"curPage":0,"pageSize":0,"success":true}
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
                    Log.i(TAG, "get payResult" + result.toString());

                    PayResult payResult = new PayResult(result);
                    String resultStatus = payResult.getResultStatus();
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("code", "1");
                            jsonObject.put("msg", "支付成功");
                            mCallbackContext.success(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("code", "0");
                            jsonObject.put("msg", "支付失败");
                            mCallbackContext.error(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            // 必须异步调用
            Thread payThread = new Thread(payRunnable);
            payThread.start();
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
