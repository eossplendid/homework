package com.unisound.uscactivate;

import android.util.Log;

public class LogUtil {

	private final static String TAG = "USCActivate";
	public static boolean DEBUG = false;

	public static void e(String msg) {
		if (DEBUG) {
			Log.e(TAG, msg);
		}
	}

	public static void e(String tag, String msg) {
		if (DEBUG) {
			Log.e(tag, msg);
		}
	}

	public static void w(String msg) {
		if (DEBUG) {
			Log.w(TAG, msg);
		}
	}

	public static void w(String tag, String msg) {
		if (DEBUG) {
			Log.w(tag, msg);
		}
	}

	public static void i(String msg) {
		if (DEBUG) {
			Log.i(TAG, msg);
		}
	}

	public static void i(String tag, String msg) {
		if (DEBUG) {
			Log.i(tag, msg);
		}
	}

	public static void d(String msg) {
		if (DEBUG) {
			Log.d(TAG, msg);
		}
	}

	public static void d(String tag, String msg) {
		if (DEBUG) {
			Log.d(tag, msg);
		}
	}

}
