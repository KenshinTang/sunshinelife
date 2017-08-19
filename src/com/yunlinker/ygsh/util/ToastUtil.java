package com.yunlinker.ygsh.util;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {

	private static Toast sT;
	public static void show(Context context, String msg){
		if(sT == null){
			sT = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
		}else{
			sT.setText(msg);
			sT.setDuration(Toast.LENGTH_SHORT);
		}
		sT.show();
	}
}
