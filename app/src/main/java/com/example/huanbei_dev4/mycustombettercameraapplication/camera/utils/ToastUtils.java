package com.example.huanbei_dev4.mycustombettercameraapplication.camera.utils;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

/**
 * Toast 的utils
 * @autor timi
 * create at 2017/5/22 14:02
 */
public class ToastUtils {
	private static Toast mToast;
	private static Handler mHandler = new Handler();
	private static Runnable r = new Runnable() {

		public void run() {

			mToast.cancel();

		}

	};

	public static void showLong(Context context, Object o) {
		  if (mToast == null) {
			  mToast = Toast.makeText(context, o.toString(), Toast.LENGTH_LONG);

	        } else {
	        	mToast.setText( o.toString());
	        }
		  mToast.show();
	}

	public static void showShort(Context context, Object o) {
		  if (mToast == null) {
			  mToast = Toast.makeText(context, o.toString(), Toast.LENGTH_SHORT);

	        } else {
	        	mToast.setText( o.toString());
	        }
		  mToast.show();
	}

	public static void showToast(Context mContext, int resId, int duration) {

		showToast(mContext, mContext.getResources().getString(resId), duration);
	}

	public static void showToast(Context mContext, String text, int duration) {

		mHandler.removeCallbacks(r);

		if (mToast != null)

			mToast.setText(text);

		else

			mToast = Toast.makeText(mContext, text, duration);

		mHandler.postDelayed(r, duration);

		mToast.show();

	}
	// public static void showErrorToast(Context context, int errorCode, String
	// msg, boolean isAlter) {
	// String error = "";
	// switch (errorCode) {
	// case 100001:
	// break;
	// case 100002:
	// error = context.getString(R.string.error_100002);
	// break;
	// case 100003:
	// error = context.getString(R.string.error_100003);
	// break;
	// case 100004:
	// error = context.getString(R.string.error_100004);
	// break;
	// case 100005:
	// error = context.getString(R.string.error_100005);
	// break;
	// case 100006:
	// error = context.getString(R.string.error_100006);
	// break;
	// case 200001:
	// error = context.getString(R.string.error_200001);
	// break;
	// case 310001:
	// error = context.getString(R.string.error_310001);
	// break;
	// case 320002:
	// error = context.getString(R.string.error_320002);
	// break;
	// case 320003:
	// error = context.getString(R.string.error_320003);
	// break;
	// case 320004:
	// error = context.getString(R.string.error_320004);
	// break;
	// case 420001:
	// error = context.getString(R.string.error_420001);
	// break;
	// case 420002:
	// error = context.getString(R.string.error_420002);
	// break;
	// case 420003:
	// error = context.getString(R.string.error_420003);
	// break;
	// case 420004:
	// error = context.getString(R.string.error_420004);
	// break;
	// case 420005:
	// error = context.getString(R.string.error_420005);
	// break;
	// case 430002:
	// error = context.getString(R.string.error_430002);
	// break;
	// case 400001:
	// error = context.getString(R.string.error_400001);
	// break;
	// case 404:
	// error = context.getString(R.string.error_404);
	// break;
	// case 500001:
	// error = context.getString(R.string.error_500001);
	// break;
	// case 500002:
	// error = context.getString(R.string.error_500002);
	// break;
	// case 600001:
	// error = context.getString(R.string.error_600001);
	// break;
	// case 700002:
	// error = context.getString(R.string.error_700002);
	// break;
	// case 800001:
	// error = context.getString(R.string.error_800001);
	// break;
	// case 900001:
	// error = msg;
	// break;
	// default:
	// error = "未知" + errorCode + "异常";
	// break;
	// }
	// if (isAlter) {
	// Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
	// }
	// }
}
