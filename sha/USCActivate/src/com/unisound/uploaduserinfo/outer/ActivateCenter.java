//package com.unisound.uploaduserinfo.outer;
//
//import java.text.MessageFormat;
//import java.util.HashMap;
//import org.json.JSONObject;
//
//import com.unisound.uscactivate.Network;
//
//
//import android.content.Context;
//import android.text.TextUtils;
//import android.util.Log;
//
///**
// * @Module : 隶属模块名
// * @Comments : 激活
// * @Author : Alieen
// * @CreateDate : 2015-07-17
// * @ModifiedBy : Alieen
// * @ModifiedDate: 2015-07-17
// * @Modified: 2015-07-17: 实现基本功能
// */
//public class ActivateCenter {
//	public static final String TAG = "ActivateCenter";
//	
//	private Context mContext = null;
////	private String mDomain = "http://activate.hivoice.cn/security/security";
//	private String mDomain = "10.30.10.21:8080";
//
//	private String PARAMS_FORMAT = "vendor={0}&type={1}&pkgname={2}&version={3}&udid={4}&distributors={5}&appkey={6}&mac={7}&extra={8}";
//	
//	private String mVendor = "";// 供应商
//	private String mType = "";// 供应商类型
//	private String mPackageName = "";
//	private String mVersion = "";
//	private String mUdid = "";// 设备唯一标示
//	private String mDistributors = "";// 分销商
//	private String mAppKey = "";
//	private String mMac = "";
//	private String mExtra = "";// 预留字段
//	
//	public ActivateCenter(Context context){
//		mContext = context;
//		HashMap<String, String> defaultHostMap = new HashMap<String, String>();
////		defaultHostMap.put("activate.hivoice.cn", "117.121.49.33");
////		defaultHostMap.put("scv2.hivoice.cn", "117.121.49.45");
//		
//		defaultHostMap.put("10.30.10.21:8080", "10.30.10.21:8080");
//
//		DefaultHttpRequest.addDefaultHost(defaultHostMap);
//	}
//
//	public void activate() {
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
////				String url = mDomain;
//				String url = "domain";
//				String result = "";
//				if (!TextUtils.isEmpty(url)) {
//					// 检查网络是否可用
//					Network.checkNetworkConnected(mContext);
//					if (Network.hasNetWorkConnect()) {
//						try {
//							result = DefaultHttpRequest.getHttpResponse(mDomain, "POST", getParams());
//							Log.e("yi", "result = " + result);
////							LogOut.d(TAG, "activate HttpResponse result : " + result);
//							JSONObject obj = JsonTool.parseToJSONObject(result);
//							if (obj != null) {
//								int rc = JsonTool.getJsonValue(obj, "rc", -1);
//								if (rc == 0) {
//									// 服务器反馈已激活
//									String activateFlag = JsonTool.getJsonValue(obj, "activate_flag", "");
//									if("0".equals(activateFlag)){
//										Log.e(TAG, "activate success");
//									} else {
//										Log.e(TAG, "activate activateFlag : " + activateFlag);
//									}
//								}
//							} else {
////								LogOut.d(TAG, "activate HttpResponse result is null");
//							}
//						} catch (Exception e) {
//							e.printStackTrace();
////							LogOut.e(TAG, "activate Exception exception :" + e.getMessage());
//							return;
//						}
//					} else {
////						LogOut.e(TAG, "activate No Network");
//					}
//				}
//			}
//		}).start();
//	}
//
//	/**
//	 * 设置domain
//	 * @param domain
//	 */
//	public void setDomain(String domain) {
//		this.mDomain = domain;
//	}
//
//	/**
//	 * 设置vendor
//	 * @param vendor
//	 */
//	public void setVendor(String vendor) {
//		this.mVendor = vendor;
//	}
//
//	/**
//	 * 设置type
//	 * @param type
//	 */
//	public void setType(String type) {
//		this.mType = type;
//	}
//
//	/**
//	 * 设置packageName
//	 * @param packageName
//	 */
//	public void setPackage(String packageName) {
//		this.mPackageName = packageName;
//	}
//
//	/**
//	 * 设置version
//	 * @param version
//	 */
//	public void setVersion(String version) {
//		this.mVersion = version;
//	}
//
//	/**
//	 * 设置udid
//	 * @param udid
//	 */
//	public void setUdid(String udid) {
//		this.mUdid = udid;
//	}
//
//	/**
//	 * 设置distributors
//	 * @param distributors
//	 */
//	public void setDistributors(String distributors) {
//		this.mDistributors = distributors;
//	}
//
//	/**
//	 * 设置appKey
//	 * @param appKey
//	 */
//	public void setAppKey(String appKey) {
//		this.mAppKey = appKey;
//	}
//
//	/**
//	 * 设置mac
//	 * @param mac
//	 */
//	public void setMac(String mac) {
//		this.mMac = mac;
//	}
//
//	/**
//	 * 设置extra
//	 * @param extra
//	 */
//	public void setExtra(String extra) {
//		this.mExtra = extra;
//	}
//
//	/**
//	 * http://activate.hivoice.cn/security/security？
//	 * vendor={0}&type={1}&pkgname={2}&version={3}&udid={4}&distributors={5}&appkey={6}&mac={7}&extra={8}
//	 * @return
//	 */
//	private String getParams() {
//		return MessageFormat.format(PARAMS_FORMAT, mVendor, mType, mPackageName,mVersion, mUdid, mDistributors,mAppKey,mMac,mExtra);
//	}
//}
