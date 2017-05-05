//package com.unisound.uploaduserinfo;
//
//import android.app.Activity;
//import android.os.Bundle;
//import android.util.Log;
//
//import com.unisound.uscactivate.client.UniActivator;
//import com.unisound.uscactivate.client.UniActivatorConstant;
//import com.unisound.uscactivate.client.UniActivatorListener;
//
//public class MainActivity extends Activity {
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
//		
//		// 实例化激活对象
////		ActivateCenter center = new ActivateCenter(this);
////		/**
////		 * http://activate.hivoice.cn/security/security？
////		 * vendor={0}&type={1}&pkgname={2}&version={3}&udid={4}&distributors={5}&appkey={6}&mac={7}&extra={8}
////		 * @return
////		 */
////		// 设置参数
////		center.setVendor("vendor");// 销售商
////		center.setType("vui_assistant");
////		center.setPackage(DeviceTool.getAppPackageName(this));
////		center.setVersion(DeviceTool.getAppVersionName(this));
////		center.setUdid(DeviceTool.getUDID(this));
////		center.setDistributors("pinwang");// 分售商
////		center.setAppKey(Config.appKey);
////		center.setMac(DeviceTool.getMac(this));
////		center.setExtra("beijing");
////		center.activate();
//		
//		
////		private String mAppKey = "";
////		private String mDeviceCode = "";// 设备唯一标示
////		private String mPackageName = "";
////		private String mAppVersion = "";
////		private String mFeature = "";// 特征码，随机字符串
////		private String mMacAddress = "";// 设备网卡MAC地址
////		private String mWifiSsid = "";// 设备WIFI的SSID
////		private String mTelecomOperator = "";// 电信运营商
////		private String mPassportId = "";// 用户通行证Passport的ID
////		private String mPassportToken = "";// 用户通行证Token
////		private String mSignature = "";// 签名
////		private String mMemo = "";// 备注字段（字符串，最大长度1024字节）
//		
//		String jsonStr = "{'appkey':'1','secret':'2','passPortId':'3','passPortToken':'4',"
//				+ "'deviceCode':'5','imei':'66','packageName':'6','appVersion':'7','feature':'8','macAddress':'9',"
//				+ "'wifiSsid':'10','telecomOperator':'11','memo':'12'}";
//		
//		UniActivator activator = UniActivator.getActivator(this, jsonStr);
//		activator.setListener(new UniActivatorListener() {
//			@Override
//			public void onEvent(String jsonStr) {
//				Log.e("yi", "jsonStr= " + jsonStr);
//			}
//		});
//		// 非必要参数设置
////		activator.setPassportId(""+ 1201);
////		activator.setPassportToken("KCKr9QoUb0v/k6awF4aAqyh5bCWlfeCvVKWiDb/wZm1n2iHU9fdJlBU0q4KAPx7KHSr1HA/hnCp7OXk6rOQod/hKHXJlPekx");
//		activator.setMemo("memo");
//		
//		Log.e("yi", "before activate ");
//		activator.activate(UniActivatorConstant.RESET_ACTIVATE);
//	}
//
//}
