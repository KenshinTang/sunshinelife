package com.plugin.myPlugin.factory;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.yunlinker.ygsh.R;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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

    @Override
    public void doAction(final CordovaPlugin plugin, JSONObject jsonObject, CallbackContext callbackContext) {
        LayoutInflater inflater = LayoutInflater.from(plugin.cordova.getActivity());
        View view = inflater.inflate(R.layout.popup_take_photo_layout, null);
        this.mPlugin = plugin;
        mPopupWindow = new PopupWindow(view,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        mPopupWindow.setBackgroundDrawable(dw);
        mPopupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
        mPopupWindow.showAtLocation(plugin.cordova.getActivity().getCurrentFocus(),
                Gravity.BOTTOM, 0, 0);

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
                if (resultCode == -1) {
                    File temp = new File(Environment.getExternalStorageDirectory()
                            + "/sunshinelife.jpg");
                    cropPhoto(Uri.fromFile(temp));//裁剪图片
                }
                break;
            case ALBUM_TYPE:
                if (resultCode == -1) {
                    cropPhoto(intent.getData());//裁剪图片
                }
                break;
            case CROP_TYPE:
                if (intent != null) {
                    Bundle extras = intent.getExtras();
                    Bitmap head = extras.getParcelable("data");
                    if (head != null) {
                        //Android 6.0 需要检查权限 ，对于没有权限的需要先申请权限
                        if (ContextCompat.checkSelfPermission(mPlugin.cordova.getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            //申请拍照权限
                            ActivityCompat.requestPermissions(mPlugin.cordova.getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_STORAGE_CODE);
                        } else {
                            setPicToView(head);//保存在SD卡中
                        }
//                        uploadPrint(filePath);
//            userImg.setImageBitmap(head);
                    }
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {

    }

    /**
     * 拍照
     */
    private void takePhoto() {
        mPopupWindow.dismiss();
        Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
                "sunshinelife.jpg")));
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
     * 调用系统的裁剪
     */
    public void cropPhoto(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        mPlugin.cordova.startActivityForResult(mPlugin, intent, 3);
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
        String path = "/sdcard/myHead/";
        File file = new File(path);
        file.mkdirs();// 创建文件夹
        String fileName = path + "sunshinelife.jpg";//图片名字
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
}
