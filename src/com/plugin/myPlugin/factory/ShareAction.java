package com.plugin.myPlugin.factory;

import com.orhanobut.logger.Logger;
import com.plugin.myPlugin.utils.JsonWrapUtils;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONObject;

/**
 * Created by YX on 2017/8/20.
 */

public class ShareAction extends IPluginAction {

    // [{"pic":"https:\/\/m.baidu.com\/static\/index\/plus\/plus_logo.png",
    // "title":"ceshititle",
    // "desc":"ceshidesc",
    // "url":"https:\/\/www.baidu.com\/"}]
    @Override
    public void doAction(CordovaPlugin plugin, JSONObject jsonObject, final CallbackContext callbackContext) {
        String title = jsonObject.optString("title");
        String imagePath = jsonObject.optString("pic");
        String desc = jsonObject.optString("desc");
        String url = jsonObject.optString("url");
        Logger.d("show Share [title:" + title + ", imagePath:" + imagePath + ", desc:" + desc + ", url:" + url + "]");
        showUmengShare(plugin.cordova, title, url, desc, imagePath, callbackContext);
    }

    private void showUmengShare(CordovaInterface cordova, String title, String url, String desc, String imagePath, final CallbackContext callbackContext) {
        UMWeb web = new UMWeb(url);
        web.setTitle(title);//标题
        web.setThumb(new UMImage(cordova.getActivity(), imagePath));
        web.setDescription(desc);//描述
        new com.umeng.socialize.ShareAction(cordova.getActivity())
                .withMedia(web)
                .setDisplayList(SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE, SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE)
                .setCallback(new UMShareListener() {
                    @Override
                    public void onStart(SHARE_MEDIA share_media) {

                    }

                    @Override
                    public void onResult(SHARE_MEDIA share_media) {
                        try {
                            Logger.i("分享成功 onResult SHARE_MEDIA = " + share_media);
                            JSONObject jo = new JSONObject();
                            jo.put("code", "1");
                            jo.put("msg", "分享成功");
                            callbackContext.success(JsonWrapUtils.wrapData(jo));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(SHARE_MEDIA share_media, Throwable throwable) {
                        try {
                            Logger.i("分享失败 onError SHARE_MEDIA = " + share_media + throwable);
                            JSONObject jo = new JSONObject();
                            jo.put("code", "0");
                            jo.put("msg", "分享失败");
                            callbackContext.error(JsonWrapUtils.wrapData(jo));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancel(SHARE_MEDIA share_media) {
                        try {
                            Logger.i("分享取消 onCancel SHARE_MEDIA = " + share_media);
                            JSONObject jo = new JSONObject();
                            jo.put("code", "0");
                            jo.put("msg", "分享取消");
                            callbackContext.error(JsonWrapUtils.wrapData(jo));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .open();
    }
}
