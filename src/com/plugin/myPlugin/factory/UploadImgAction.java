package com.plugin.myPlugin.factory;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.orhanobut.logger.Logger;
import com.plugin.myPlugin.utils.JsonWrapUtils;
import com.yunlinker.ygsh.sh.R;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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

public class UploadImgAction extends IPluginAction {

    /**
     * 请求类型 1：拍照；2：相册；3：裁剪
     */
    private static final int TAKE_TYPE = 1;
    private static final int ALBUM_TYPE = 2;
    private static final int CROP_TYPE = 3;
    private PopupWindow mPopupWindow;
    private CordovaPlugin mPlugin;
    private String mFilename = "sunshinelife.jpg";
    private Uri imageUri = Uri.parse("file:///" + Environment.getExternalStorageDirectory().getPath() + "/" + mFilename);
    private JSONObject mJSONObject;
    private CallbackContext mCallbackContext;
    private OSS oss;
    private int mCount;

    @Override
    public void doAction(final CordovaPlugin plugin, JSONObject jsonObject, CallbackContext callbackContext) {
        LayoutInflater inflater = LayoutInflater.from(plugin.cordova.getActivity());
        View view = inflater.inflate(R.layout.popup_take_photo_layout, null);
        mPlugin = plugin;
        mJSONObject = jsonObject;
        try {
            mCount = jsonObject.getInt("count");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mCallbackContext = callbackContext;
        mPopupWindow = new PopupWindow(view,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        mPopupWindow.setBackgroundDrawable(dw);
        mPopupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
        mPopupWindow.showAtLocation(plugin.cordova.getActivity().getCurrentFocus(),
                Gravity.BOTTOM, 0, 0);
        setOnClickEvent(plugin, view);
    }

    private void setOnClickEvent(final CordovaPlugin plugin, View view) {
        TextView takePhoto = (TextView) view.findViewById(R.id.take_photo);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
            }
        });
        TextView selectFromAlbum = (TextView) view.findViewById(R.id.select_from_album);
        selectFromAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //从相册选择
                selectFromAlbum();
            }
        });
        TextView cancel = (TextView) view.findViewById(R.id.take_photo_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //取消
                mPopupWindow.dismiss();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Logger.d("onActivityResult: " + requestCode + " resultCode" + resultCode);
        if (resultCode == 0) {//返回
            return;
        }
        switch (requestCode) {
            case TAKE_TYPE:
                // count为1的时候目前是上传头像的场景.
                // count大于1的时候是评论的场景. 不需要裁剪.
                // TODO: 需要重构, 暂时先这么判断
                if (mCount == 1) {
                    cropPhoto(imageUri);//裁剪图片
                } else {
                    uploadPic();
                }
                break;
            case ALBUM_TYPE:
                // count为1的时候目前是上传头像的场景.
                // count大于1的时候是评论的场景. 不需要裁剪.
                // TODO: 需要重构, 暂时先这么判断
                // content://media/external/images/media/92317
                if (mCount == 1) {
                    cropPhoto(intent.getData());
                } else {
                    imageUri = intent.getData();
                    uploadPic();
                }
                break;
            case CROP_TYPE:
                uploadPic();
                break;
        }
    }

    // 通过Uri拿绝对路径
    private static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    /**
     * 上传头像
     */
    private void uploadPic() {
        try {
            final String url = mJSONObject.getString("url");
            String timestamp = mJSONObject.getString("timestamp");
            String sign = mJSONObject.getString("sign");
            OkHttpClient okHttpClient = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("timestamp", timestamp)
                    .add("sign", sign)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String str = response.body().string();
                    Logger.i("uploadPic: " + str);
                    JSONObject resultJb = null;
                    try {
//                        {"status":"200",
//                                "AccessKeyId":"STS.GEtzwf1SXmUgn3Wkwc3yBtRmD",
//                                "AccessKeySecret":"CsXxaA2sCVC7LLvLVksfw6dSLWpqJiy8gSVKaWUfncmn",
//                                "SecurityToken":"CAIShwJ1q6Ft5B2yfSjIpobBMc3S3Ix52peMbBXmj3c2P/Zum5fGpjz2IHhKe3RsAeAZtPk2mm1X6/8SlqQqFsceGhCfN5Erv8oIqZoAghNI+J7b16cNrbH4M0rxYkeJ8a2/SuH9S8ynCZXJQlvYlyh17KLnfDG5JTKMOoGIjpgVBbZ+HHPPD1x8CcxROxFppeIDKHLVLozNCBPxhXfKB0ca0WgVy0EHsPTkk5PBtUeG1wWnkbFI+76ceMb0M5NeW75kSMqw0eBMca7M7TVd8RAi9t0t1PIbpGuf7o7HXgAPvkrbarrOgdRrLR5kYK8hALJDr/X6mvB+t/bai4Pt0RFJMPHrlaUopk1qqxqAAZKiUed92nHx+rqUp9/uDL1far+PkkpsTe4GqC8Nx9KIfUhkKUUlUHsPqfUM6N4lKtoLkzaeIrvcVPz/+MWbafbU+h2W2azEvG/CqDKiaqIOMPOh5ww2wGPXirjwI197Y5Jj7WkZIrBz+TDt735APw1Ltb/Y5syS3S+AqhEdjE23"
//                                ,"Expiration":"2017-08-26T10:42:04Z",
//                                "dir":"2017-08",
//                                "filename":"1503743224223"}
                        resultJb = new JSONObject(str);
                        String AccessKeyId = resultJb.getString("AccessKeyId");
                        String AccessKeySecret = resultJb.getString("AccessKeySecret");
                        String SecurityToken = resultJb.getString("SecurityToken");
                        String dir = resultJb.getString("dir");
                        String filename = resultJb.getString("filename");
                        final String imgName = dir + "/" + filename + ".jpg";
                        OSSCredentialProvider credentialProvider = new OSSStsTokenCredentialProvider(AccessKeyId, AccessKeySecret, SecurityToken);
                        ClientConfiguration conf = new ClientConfiguration();
                        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
                        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
                        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
                        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
                        OSSLog.enableLog();
                        oss = new OSSClient(mPlugin.cordova.getActivity().getApplicationContext(), "http://oss-cn-shenzhen.aliyuncs.com", credentialProvider, conf);

                        String path;
                        if (imageUri.getScheme().equals("content")) {
                            path = getRealFilePath(mPlugin.cordova.getActivity(), imageUri);
                            // 缩放图片,减小size以便快速上传.
                            path = scalePic(path, 800f);
                        } else {
                            path = imageUri.getPath();
                        }

                        PutObjectRequest put = new PutObjectRequest("ygsh", imgName, path);
                        OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
                            @Override
                            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                                Logger.d("UploadSuccess");
                                JSONObject callbackJsonObject = new JSONObject();
                                try {
//                    {code: 成功1，失败0, msg: 描述, imgUrl: [2017-03/12345.jpg,2017-03/12346.jpg]}
                                    JSONArray array = new JSONArray();
                                    array.put(imgName);
                                    callbackJsonObject.put("code", 1);
                                    callbackJsonObject.put("msg", "上传成功");
                                    callbackJsonObject.put("imgUrl", array);
                                    JSONObject data = JsonWrapUtils.wrapData(callbackJsonObject);
                                    mCallbackContext.success(data);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                                // 请求异常
                                if (clientExcepion != null) {
                                    // 本地异常如网络异常等
                                    clientExcepion.printStackTrace();
                                }
                                if (serviceException != null) {
                                    // 服务异常
                                    Logger.e(serviceException.getErrorCode());
                                    Logger.e(serviceException.getRequestId());
                                    Logger.e(serviceException.getHostId());
                                    Logger.e(serviceException.getRawMessage());
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String scalePic(String path, float maxHW) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        //TODO: 没必要先加载到内存中.
//        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
//        options.inJustDecodeBounds = false;
        if (imageHeight <= maxHW && imageWidth <= maxHW) {
            return path;
        } else {
            String outFilePath = Uri.parse("file:///" + Environment.getExternalStorageDirectory().getPath() + "/" + mFilename).getPath();
            File file = new File(outFilePath);
            try {
                float scale = maxHW / Math.max(imageHeight, imageWidth);
                Matrix matrix = new Matrix();
                matrix.postScale(scale, scale);
                Logger.i("scale imgage: " + scale);

                Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, imageWidth, imageHeight, matrix, true);

                FileOutputStream out = new FileOutputStream(file);
                if (scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
                    out.flush();
                    out.close();
                }
            } catch (Exception e) {
                Logger.e(e, "", "");
            }
            return file.getPath();
        }
    }

    /**
     * 拍照
     */
    private void takePhoto() {
        mPopupWindow.dismiss();
        Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        mPlugin.cordova.startActivityForResult(mPlugin, takeIntent, TAKE_TYPE);
    }

    /**
     * 相册
     */
    private void selectFromAlbum() {
        mPopupWindow.dismiss();
        Intent albumIntent = new Intent(Intent.ACTION_PICK, null);
        albumIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        mPlugin.cordova.startActivityForResult(mPlugin, albumIntent, ALBUM_TYPE);
    }

    /**
     * 裁剪图片
     *
     * @param data
     */
    private void cropPhoto(Uri data) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(data, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 800);
        intent.putExtra("outputY", 800);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        mPlugin.cordova.startActivityForResult(mPlugin, intent, CROP_TYPE);
    }
}
