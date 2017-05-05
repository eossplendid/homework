///**
// * Copyright (c) 2012-2014 YunZhiSheng(Shanghai) Co.Ltd. All right reserved.
// * @FileName : DeviceTool.java
// * @ProjectName : vui_common
// * @PakageName : cn.yunzhisheng.common
// * @Author : Dancindream
// * @CreateDate : 2014-2-25
// */
//package com.unisound.uploaduserinfo.outer;
//
//import android.content.Context;
//import android.content.pm.PackageInfo;
//import android.net.wifi.WifiInfo;
//import android.net.wifi.WifiManager;
//import android.provider.Settings.Secure;
//import android.telephony.TelephonyManager;
//
///**
// * @Module : 隶属模块名
// * @Comments : 描述
// * @Author : Dancindream
// * @CreateDate : 2014-2-25
// * @ModifiedBy : Dancindream
// * @ModifiedDate: 2014-2-25
// * @Modified:
// *            2014-2-25: 实现基本功能
// */
//public class DeviceTool {
//	public static final String TAG = "DeviceTool";
//
//	private static final String INVALID_IMEI = "000000000000000";
//
//	public static String getDeviceId(Context context) {
//		String deviceId = getIMEI(context);
//		return (deviceId == null || deviceId.equals("")) ? INVALID_IMEI : deviceId;
//	}
//
//	// udid 一定要用imei_mac （imei +"_"+ mac）
//	public static String getUDID(Context context) {
//		return getIMEI(context) + "_" + getMac(context);
//	}
//
//	public static String getIMEI(Context context) {
//		String imei = "";
//		imei = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
//		if (imei != null && !"".equals(imei) && !imei.equals(INVALID_IMEI)) {
//			return imei;
//		}
//
//		imei = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
//		if (imei != null && !"".equals(imei) && !imei.equals(INVALID_IMEI)) {
//			return imei;
//		}
//		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//		WifiInfo info = wifi.getConnectionInfo();
//		if (info != null) {
//			return info.getMacAddress();
//		}
//		return INVALID_IMEI;
//
//	}
//
//	public static String getMac(Context context) {
//		if (context == null) {
//			return INVALID_IMEI;
//		}
//		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//		WifiInfo info = wifi.getConnectionInfo();
//		if (info != null) {
//			return info.getMacAddress();
//		}
//		return INVALID_IMEI;
//	}
//
//	public static String getAppVersionName(Context context) {
//		String versionName = "";
//
//		try {
//			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
//
//			versionName = packageInfo.versionName;
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return versionName;
//	}
//
//	public static String getAppPackageName(Context context) {
//		String packageName = "";
//
//		try {
//			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
//
//			packageName = packageInfo.packageName;
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return packageName;
//	}
//}
