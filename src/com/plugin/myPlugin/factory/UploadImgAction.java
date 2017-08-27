package com.plugin.myPlugin.factory;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.yunlinker.ygsh.R;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by YX on 2017/8/20.
 */

public class UploadImgAction extends IPluginAction {

    /**
     * 请求类型 1：拍照；2：相册；3：裁剪
     */
    public static final int TAKE_TYPE = 1;
    public static final int ALBUM_TYPE = 2;
    public static final int CROP_TYPE = 3;
    public static final int MY_PERMISSIONS_REQUEST_CAMERA_CODE = 5;
    public static final int MY_PERMISSIONS_REQUEST_STORAGE_CODE = 6;
    private PopupWindow mPopupWindow;
    private CordovaPlugin mPlugin;
    private String mPath = Environment.getExternalStorageDirectory().getPath()+"/myHead/";
    private String mFilename = "sunshinelife.jpg";
    private Uri imageUri = Uri.parse("file:///"+Environment.getExternalStorageDirectory().getPath()+"/"+mFilename);
    public static final String SUCCESS = "success";
    public static final String FAILURE = "failed";
    private static final int TIME_OUT = 5 * 60 * 1000; //超时时间
    private static final String CHARSET = "utf-8"; //设置编码
    private JSONObject mJSONObject;
    private CallbackContext mCallbackContext;

    @Override
    public void doAction(final CordovaPlugin plugin, JSONObject jsonObject, CallbackContext callbackContext) {
        LayoutInflater inflater = LayoutInflater.from(plugin.cordova.getActivity());
        View view = inflater.inflate(R.layout.popup_take_photo_layout, null);
        this.mPlugin = plugin;
        this.mJSONObject = jsonObject;
        this.mCallbackContext = callbackContext;
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
                //Android 6.0 需要检查权限 ，对于没有权限的需要先申请权限
                if (ContextCompat.checkSelfPermission(plugin.cordova.getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    //申请拍照权限
                    ActivityCompat.requestPermissions(plugin.cordova.getActivity(), new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA_CODE);
                } else {
                    //拍照
                    takePhoto();
                }
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
        switch (requestCode) {
            case TAKE_TYPE:
                cropPhoto(imageUri,CROP_TYPE);//裁剪图片
                break;
            case ALBUM_TYPE:
                cropPhoto(imageUri,CROP_TYPE);
                break;
            case CROP_TYPE:
                if (imageUri != null) {
                    Bitmap bitmap = decodeUriAsBitmap(imageUri);
                    if (bitmap != null) {
                        Log.d("allen", "onActivityResult: mHead" + bitmap.getWidth() + "height" + bitmap.getHeight());
                        //Android 6.0 需要检查权限 ，对于没有权限的需要先申请权限
                        if (ContextCompat.checkSelfPermission(mPlugin.cordova.getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            //申请拍照权限
                            ActivityCompat.requestPermissions(mPlugin.cordova.getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_STORAGE_CODE);
                        } else {
                            setPicToView(bitmap);//保存在SD卡中
                        }
                        uploadPic(mPath+mFilename);
                    }
                }
                break;
        }
    }

    /**
     * 上传头像
     */
    private void uploadPic(String filePath) {
//      final File file = new File(filePath);
        try {
            final String url = mJSONObject.getString("url");
            String timestamp = mJSONObject.getString("timestamp");
            String sign = mJSONObject.getString("sign");
            final HashMap<String, String> params = new HashMap<>();
            params.put("timestamp", timestamp);
            params.put("sign", sign);
            RequestParams requestParams = new RequestParams();
            String sendUrl = getUrl(url, params);
            String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension("png");
            requestParams.addBodyParameter("imgFile", new File(filePath), mime);

            HttpUtils httpUtils = new HttpUtils();
            httpUtils.send(HttpRequest.HttpMethod.POST, sendUrl, requestParams, new RequestCallBack<Object>() {
                @Override
                public void onSuccess(ResponseInfo<Object> responseInfo) {
                    Log.d("allen", "success");
                    try {
                        JSONObject resultJb = new JSONObject(responseInfo.result.toString());
                        String dir = resultJb.getString("dir");
                        String filename = resultJb.getString("filename");
                        String imgName = dir + "/" + filename + ".jpg";
                        JSONObject dataObject = new JSONObject();
                        JSONObject callbackJsonObject = new JSONObject();
                        dataObject.put("data", callbackJsonObject);
//                    {code: 成功1，失败0, msg: 描述, imgUrl: [2017-03/12345.jpg,2017-03/12346.jpg]}
                        JSONArray array = new JSONArray();
                        array.put(imgName);
                        callbackJsonObject.put("code", 1);
                        callbackJsonObject.put("msg", "上传成功");
                        callbackJsonObject.put("imgUrl", array);
                        mCallbackContext.success(dataObject);
                        Log.i("allen", "callbackMessage : " + dataObject.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(HttpException e, String s) {

                }
            });


//            Executors.newCachedThreadPool().execute(new Runnable() {
//                @Override
//                public void run() {
//                    uploadFile(file, getUrl(url, params));
//                }
//            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * android上传文件到服务器
     *
     * @param file       需要上传的文件
     * @param requestURL 请求的rul
     * @return 返回响应的内容
     */
    public String uploadFile(File file, String requestURL) {
        String result = null;
        String BOUNDARY = UUID.randomUUID().toString(); //边界标识 随机生成
        String PREFIX = "--", LINE_END = "\r\n";
        String CONTENT_TYPE = "multipart/form-data"; //内容类型
        if (TextUtils.isEmpty(requestURL)) {
            Log.i("allen", "请求url为空");
            return FAILURE;
        }
        try {
            Log.i("allen", "传入的上传url" + requestURL);
            URL url = new URL(requestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIME_OUT);
            conn.setConnectTimeout(TIME_OUT);
            conn.setDoInput(true); //允许输入流
            conn.setDoOutput(true); //允许输出流
            conn.setUseCaches(false); //不允许使用缓存
            conn.setRequestMethod("POST"); //请求方式
            conn.setRequestProperty("Charset", CHARSET);
            //设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
            if (file != null) {
                /** * 当文件不为空，把文件包装并且上传 */
                OutputStream outputSteam = conn.getOutputStream();
                DataOutputStream dos = new DataOutputStream(outputSteam);
                /**
                 * 这里重点注意：
                 * name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件
                 * filename是文件的名字，包含后缀名的 比如:abc.png
                 */
                dos.write((PREFIX + BOUNDARY + LINE_END + "Content-Disposition: form-data; mFilename=\"img\"; filename=\"" + file.getName() + "\"" + LINE_END + "Content-Type: application/octet-stream; charset=" + CHARSET + LINE_END + LINE_END).getBytes());
                InputStream is = new FileInputStream(file);
                byte[] bytes = new byte[1024];
                int len = 0;
                while ((len = is.read(bytes)) != -1) {
                    dos.write(bytes, 0, len);
                }
                is.close();
                dos.write(LINE_END.getBytes());
                byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
                dos.write(end_data);
                dos.flush();
                /**
                 * 获取响应码 200=成功
                 * 当响应成功，获取响应的流
                 */
                int res = conn.getResponseCode();
                Log.i("allen", "response code:" + res);
                if (res == 200) {
                    InputStream input = conn.getInputStream();
                    StringBuffer sb1 = new StringBuffer();
                    int ss;
                    while ((ss = input.read()) != -1) {
                        sb1.append((char) ss);
                    }
                    result = sb1.toString();
                    Log.i("allen", "result : " + result);
                    JSONObject jsonObject = new JSONObject(result);
                    String dir = jsonObject.getString("dir");
                    String filename = jsonObject.getString("filename");
                    String imgName = dir + "/" + filename + ".jpg";
                    JSONObject callbackJsonObject = new JSONObject();
//                    {code: 成功1，失败0, msg: 描述, imgUrl: [2017-03/12345.jpg,2017-03/12346.jpg]}
                    JSONArray array = new JSONArray();
                    array.put(imgName);
                    callbackJsonObject.put("code", 1);
                    callbackJsonObject.put("msg", "上传成功");
                    callbackJsonObject.put("imgUrl", array);
                    mCallbackContext.success(callbackJsonObject);
                    Log.i("allen", "callbackMessage : " + callbackJsonObject.toString());
                    return SUCCESS;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject errorObject = null;
        try {
            errorObject = new JSONObject();
            errorObject.put("msg", "上传失败");
            errorObject.put("code", 0);
            mCallbackContext.error(errorObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return FAILURE;
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //权限申请成功
                    takePhoto();
                } else {
                    //权限申请失败
                }
            }
            break;
            case MY_PERMISSIONS_REQUEST_STORAGE_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //权限申请成功
                    setPicToView(decodeUriAsBitmap(imageUri));//保存在SD卡中
                    uploadPic(mPath + mFilename);
                } else {
                    //权限申请失败
                }
            }
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
    private void cropPhoto(Uri uri,int requestCode){
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 800);
        intent.putExtra("outputY", 800);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        mPlugin.cordova.startActivityForResult(mPlugin, intent, requestCode);
    }

    /**
     * 保存图片到sd卡
     */
    private void setPicToView(Bitmap mBitmap) {
        String sdStatus = Environment.getExternalStorageState();
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
            return;
        }
        FileOutputStream b = null;
        File file = new File(mPath);
        file.mkdirs();// 创建文件夹
        String fileName = mPath + mFilename;//图片名字
        try {
            b = new FileOutputStream(fileName);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                //关闭流
                b.flush();
                b.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 拼接URL
     *
     * @param baseUrl 接口请求地址
     * @param params  请求参数
     * @return 拼接的url
     */
    private static String getUrl(String baseUrl, HashMap<String, String> params) {
        String url = baseUrl;
        // 添加url参数
        if (params != null) {
            Iterator<String> it = params.keySet().iterator();
            StringBuffer sb = null;
            while (it.hasNext()) {
                String key = it.next();
                String value = params.get(key);
                if (sb == null) {
                    sb = new StringBuffer();
                    sb.append("?");
                } else {
                    sb.append("&");
                }
                sb.append(key);
                sb.append("=");
                sb.append(value);
            }
            url += sb.toString();
        }
        return url;
    }

    private Bitmap decodeUriAsBitmap(Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(mPlugin.cordova.getActivity().getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }

}
