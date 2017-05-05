package com.unisound.uscactivate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Message;

import com.unisound.uscactivate.client.UniActivatorConstant;
import com.unisound.uscactivate.client.UniActivatorListener;

public class ActivatorInterface {
	
	//***有deviceSn情况的***
	//1.激活URL：
//	private String mRegisterUrl_Sn = "http://10.30.10.21:8180/rest/v2/device/activate"; //测试URL
	private String mRegisterUrl_Sn = "http://dc.hivoice.cn/rest/v2/device/activate";
	//2.刷新tokenURL：
//	private String mRefreshUrl_Sn = "http://10.30.10.21:8180/rest/v2/token/refresh"; //测试URL
	private String mRefreshUrl_Sn = "http://dc.hivoice.cn/rest/v2/token/refresh";
	private String mServer;
	
	// 需要用到的参数
	private String mUdid = "";// 设备唯一标示udid,根据deviceSn生成 (注：在Sn情况下叫Udid，正常情况下为DeviceCode)
	private String mDeviceSn = ""; //必须需要外界设置 （Sn情况下必需）
	private String mAppKey = ""; //（必需）
	private String mTimeStamp = ""; // （必需）
	private String mAppVersion = "";
	private String mPackageName = "";
	private String mImei = "";// 设备imei
	private String mMacAddress = "";// 设备网卡MAC地址
	private String mWifiSsid = "";// 设备WIFI的SSID
	private String mTelecomOperator = "";// 电信运营商
	private String mBssid = "";// bssId , 当前的接入点MAC地址, 字符串（100）
	private String mProductName = "";// productName , 设备名称, 字符串（100）
	private String mProductModel = "";// productModel , 型号名称, 字符串（100）
	private String mProductMfr = "";// productMfr , 制造商名称 , 字符串（100）
	private String mProductOs = "Android";// productOs , 操作系统 , Android系统固定为“Android”,字符串（20）
	private String mProductOsVersion = "";// productOsVersion , 操作系统版本号, 字符串（100）
	private String mHardwareSn = "";// hardwareSn , 厂商硬件序列号 , 字符串（100）//TODO, 约定一个固定的文件
	private String mMemo = "";// 备注字段（字符串，最大长度1024字节）
	private String mSignature_activate_Sn = ""; // Sn情况下激活需要的签名
	private String mSignature_refresh_Sn = ""; // Sn情况下刷新需要的签名
	private String mToken = "";
	
	private String mSecret = "";
	private String jsonStr = "";

	private Context mContext = null;
	private ActivatorHandler mHandler;
	
	private static final String RESPONESE_FILE_NAME = "systemResponese" ;

	enum ActivateStatus {
		RUNNING, FINISH
	}

	private ActivateStatus mStatus = ActivateStatus.FINISH;

	public ActivatorInterface(Context context, String jsonStr) {
		this.mContext = context;
		mHandler = new ActivatorHandler(context.getMainLooper());
		this.jsonStr = jsonStr;
	}

	public void setListener(UniActivatorListener listener) {
		mHandler.setListener(listener);
	}

	private void initJsonStr(String jsonStr) {
		JSONObject _json = JsonUtil.parseToJSONObject(jsonStr);

		//添加deviceSn字段
		String _deviceSn = JsonUtil.getJsonValue(_json, "deviceSn");
		if (_deviceSn.equals("")) {
			LogUtil.i("ActivatorInterface _deviceSn= null");
		} else {
			setDeviceSn(_deviceSn);
			LogUtil.i("ActivatorInterface _deviceSn= " + _deviceSn);
		}

		String _appkey = JsonUtil.getJsonValue(_json, "appKey");
		if (_appkey.equals("")) {
			LogUtil.e("ActivatorInterface _appkey= null");
		} else {
			setAppKey(_appkey);
			LogUtil.i("ActivatorInterface _appkey= " + _appkey);
		}

		String _secret = JsonUtil.getJsonValue(_json, "appSecret");
		if (_secret.equals("")) {
			LogUtil.e("ActivatorInterface _secret= null");
		} else {
			setSecret(_secret);
			LogUtil.i("ActivatorInterface _secret= " + _secret);
		}

		String _appVersion = JsonUtil.getJsonValue(_json, "appVersion");
		if (_appVersion.equals("")) {
			LogUtil.i("ActivatorInterface _appVersion= null");
		} else {
			setAppVersion(_appVersion);
			LogUtil.i("ActivatorInterface _appVersion= " + _appVersion);
		}

		String _packageName = JsonUtil.getJsonValue(_json, "packageName");
		if (_packageName.equals("")) {
			LogUtil.i("ActivatorInterface _packageName= null");
		} else {
			setPackageName(_packageName);
			LogUtil.i("ActivatorInterface _packageName= " + _packageName);
		}

		String _imei = JsonUtil.getJsonValue(_json, "imei");
		if (_imei.equals("")) {
			LogUtil.i("ActivatorInterface _imei= null");
		} else {
			setImei(_imei);
			LogUtil.i("ActivatorInterface _imei= " + _imei);
		}

		String _macAddress = JsonUtil.getJsonValue(_json, "macAddress");
		if (_macAddress.equals("")) {
			LogUtil.i("ActivatorInterface _macAddress= null");
		} else {
			setMacAddress(_macAddress);
			LogUtil.i("ActivatorInterface _macAddress= " + _macAddress);
		}

		String _wifiSsid = JsonUtil.getJsonValue(_json, "wifiSsid");
		if (_wifiSsid.equals("")) {
			LogUtil.i("ActivatorInterface _wifiSsid= null");
		} else {
			setWifiSsid(_wifiSsid);
			LogUtil.i("ActivatorInterface _wifiSsid= " + _wifiSsid);
		}

		String _telecomOperator = JsonUtil.getJsonValue(_json, "telecomOperator");
		if (_telecomOperator.equals("")) {
			LogUtil.i("ActivatorInterface _telecomOperator= null");
		} else {
			setTelecomOperator(_telecomOperator);
			LogUtil.i("ActivatorInterface _telecomOperator= " + _telecomOperator);
		}

		String _memo = JsonUtil.getJsonValue(_json, "memo");
		if (_memo.equals("")) {
			LogUtil.i("ActivatorInterface _memo= null");
		} else {
			setMemo(_memo);
			LogUtil.i("ActivatorInterface _memo= " + _memo);
		}

		//添加Sn刷新需要token
		String _token = JsonUtil.getJsonValue(_json, "token");
		if (_token.equals("")) {
			LogUtil.i("ActivatorInterface _token= null");
		} else {
			setToken(_token);
			LogUtil.i("ActivatorInterface __token= " + _token);
		}
		
		boolean _debug = JsonUtil.getJsonValue(_json, "debug", false);
		if (_debug) {
			setDebug(_debug);
			LogUtil.i("ActivatorInterface _debug= " + _debug);
		}
	}

	private void init() {
		if (mAppVersion == "") {
			setAppVersion();
		}
		if (mImei == "") {
			setImei();
		}
		if (mPackageName == "") {
			setPackageName();
		}

		if (mMacAddress == "") {
			setMacAddress();
		}
		if (mWifiSsid == "") {
			setWifiSsid();
		}
		if (mTelecomOperator == "") {
			setTelecomOperator();
		}
		
		createDeviceInfo();
	}

	public void activate(int type) {
		if (mStatus == ActivateStatus.RUNNING) {
			// 返回一个提示
			mHandler.sendEmptyMessage(HandlerValue.ACTIVATOR_STATUS_ERROR_MESSAGE);
			return;
		}

		mStatus = ActivateStatus.RUNNING;

		switch (type) {
		case UniActivatorConstant.REGISTER_ACTIVATE:
			break;
		case UniActivatorConstant.RESET_ACTIVATE:
			break;
			default:
				mHandler.sendEmptyMessage(HandlerValue.INVALID_URL_TYPE_MESSAGE);
				mStatus = ActivateStatus.FINISH;
				return;
		}

		initJsonStr(jsonStr);

		//用deviceSn方式注册
		new HttpThreadSn(type).start();

	}

	protected void setServer(String server){
		this.mServer = server;
		mRegisterUrl_Sn = "http://"+ server + "/rest/v1/device/activate";
		mRefreshUrl_Sn = "http://" + server + "/rest/v1/token/refresh";
	}

	private void createDeviceInfo(){
		mBssid = DeviceUtil.getBSSID(mContext);
		mProductName = DeviceUtil.getProductName();
		mProductModel = DeviceUtil.getProductModel();
		mProductMfr = DeviceUtil.getProductMfr();
		mProductOsVersion = DeviceUtil.getProductOsVersion();
		mHardwareSn = DeviceUtil.readSN();
	}

	protected void setDebug(boolean debug) {
		LogUtil.DEBUG = debug;
	}

	protected Context getContext() {
		return mContext;
	}

	protected void setContext(Context mContext) {
		this.mContext = mContext;
	}

	protected String getAppKey() {
		return mAppKey;
	}

	protected void setAppKey(String mAppKey) {
		this.mAppKey = mAppKey;
	}

	protected void setSecret(String mSecret) {
		this.mSecret = mSecret;
	}

	protected String getSecret() {
		return this.mSecret;
	}

	protected String getImei() {
		return mImei;
	}

	protected void setImei() {
		this.mImei = DeviceUtil.getIMEI(mContext);
	}

	protected void setImei(String imei) {
		this.mImei = imei;
	}

	protected String getAppVersion() {
		return mAppVersion;
	}

	protected void setAppVersion() {
		this.mAppVersion = DeviceUtil.getAppVersion(mContext);
	}

	protected void setAppVersion(String appVersion) {
		this.mAppVersion = appVersion;
	}

	protected String getPackageName() {
		return mPackageName;
	}

	protected void setPackageName() {
		this.mPackageName = DeviceUtil.getAppPackageName(mContext);
	}

	protected void setPackageName(String packageName) {
		this.mPackageName = packageName;
	}

	protected String getMacAddress() {
		return mMacAddress;
	}

	protected void setMacAddress() {
		this.mMacAddress = DeviceUtil.getMac(mContext);
	}

	protected void setMacAddress(String macAddress) {
		this.mMacAddress = macAddress;
	}

	protected String getWifiSsid() {
		return mWifiSsid;
	}

	protected void setWifiSsid() {
		this.mWifiSsid = DeviceUtil.getWifiSSID(mContext);
	}

	protected void setWifiSsid(String wifiSsid) {
		this.mWifiSsid = wifiSsid;
	}

	protected String getTelecomOperator() {
		return mTelecomOperator;
	}

	protected void setTelecomOperator() {
		this.mTelecomOperator = DeviceUtil.getTelecomOperator(mContext);
	}

	protected void setTelecomOperator(String telecomOperator) {
		this.mTelecomOperator = telecomOperator;
	}

	protected String getMemo() {
		return mMemo;
	}

	protected void setMemo(String mMemo) {
		this.mMemo = mMemo;
	}

	protected String getVersion() {
		return ActivatorVersion.getVersion();
	}
	
	public String getDeviceSn() {
		return mDeviceSn;
	}

	public void setDeviceSn(String deviceSn) {
		this.mDeviceSn = deviceSn;
	}

	public String getmTimeStamp() {
		return mTimeStamp;
	}

	public void setmTimeStamp(String mTimeStamp) {
		this.mTimeStamp = mTimeStamp;
	}

	protected String getUdid() {
		return mUdid;
	}

	protected void setUdid(String mUdid) {
		this.mUdid = mUdid;
	}

	public String getSignatureActivateSn() {
		return mSignature_activate_Sn;
	}

	protected void setSignatureActivateSn(String mSignatureActivateSn) {
		this.mSignature_activate_Sn = mSignatureActivateSn;
	}

	public String getSignatureRefreshSn() {
		return mSignature_refresh_Sn;
	}

	protected void setSignatureRefreshSn(String mSignatureRefreshSn) {
		this.mSignature_refresh_Sn = mSignatureRefreshSn;
	}

	public void setToken(String token) {
		this.mToken = token;
	}

	public String getToken() {
		return mToken;
	}

	private class HttpThreadSn extends Thread {

		private int mode = UniActivatorConstant.REGISTER_ACTIVATE;

		public HttpThreadSn(int type) {
			this.mode = type;
		}

		@Override
		public void run() {
			LogUtil.d("ActivatorInterface HttpThreadSn start jsonStr= " + jsonStr);

//			initJsonStr(jsonStr);
			init();
			
			setUdid(DeviceUtil.getDeviceId(mContext, mDeviceSn));
//			buildUdidFromDeviceSn(mDeviceSn);

			LogUtil.i("ActivatorInterface$HttpThreadSn init over");
			String result = "";
			ResponseInfo responseInfo ;

			// 检查网络是否可用
			Network.checkNetworkConnected(mContext);
			if (Network.hasNetWorkConnect()) {
				try {
					String url = "";
					Map<String,String> param = null;
					if (mode == UniActivatorConstant.REGISTER_ACTIVATE) {
						url = mRegisterUrl_Sn;
						param = getSnActivateParams();
					} else if (mode == UniActivatorConstant.RESET_ACTIVATE) {
						url = mRefreshUrl_Sn;
						param = getSnRefreshParams();
					}

					LogUtil.d("ActivatorInterface","url = " + url);
					responseInfo = DefaultHttpRequest.submitPostData(url, param,5000);
					result = responseInfo.getResponse() ;
					if (responseInfo.getResponseHeaderTime()!=0) {
						saveTimeDelay(responseInfo.getResponseHeaderTime()-responseInfo.getResponseCurrentSystemTime());
					}
					LogUtil.d("result = " + result);
					if ((!result.equals(""))&&(!result.equals("{}"))) {
						Message msg = new Message();
						msg.obj = result;
						msg.what = HandlerValue.GET_RESULT_MESSAGE;
						mHandler.sendMessage(msg);
					} else {
						mHandler.sendEmptyMessage(HandlerValue.RESPONSE_IS_NULL_MESSAGE);
						LogUtil.d("activate HttpResponse result is null");
					}
				} catch (Exception e) {
					mStatus = ActivateStatus.FINISH;
					e.printStackTrace();
					mHandler.sendEmptyMessage(HandlerValue.EXCEPTION_MESSAGE);
					LogUtil.e("activate Exception exception :" + e.getMessage());
					return;
				}
			} else {
				mHandler.sendEmptyMessage(HandlerValue.NO_NETWORK_MESSAGE);
				LogUtil.e("activate No Network");
			}
			mStatus = ActivateStatus.FINISH;
		}
	}

	/*
	 * 获取激活的参数
	 */
	private Map<String,String> getSnActivateParams() throws Exception {
		HashMap<String, String> activateParams = new HashMap<String, String>();
		activateParams.put("udid",(mUdid != null)?mUdid:"");
		activateParams.put("deviceSn",(mDeviceSn != null)?mDeviceSn:"");
		activateParams.put("appKey",(mAppKey != null)?mAppKey:"");
		activateParams.put("timestamp", getCurrentUnixTimestamp());
		activateParams.put("appVersion",(mAppVersion != null)?mAppVersion:"");
		activateParams.put("pkgName",(mPackageName != null)?mPackageName:"");
		activateParams.put("imei",(mImei != null)?mImei:"");
		activateParams.put("macAddress",(mMacAddress != null)?mMacAddress:"");
		activateParams.put("wifiSsid",(mWifiSsid != null)?mWifiSsid:"");
		activateParams.put("telecomOperator",(mTelecomOperator != null)?mTelecomOperator:"");
		activateParams.put("bssId",(mBssid != null)?mBssid:"");
		activateParams.put("productName",(mProductName != null)?mProductName:"");
		activateParams.put("productModel",(mProductModel != null)?mProductModel:"");
		activateParams.put("productMfr",(mProductMfr != null)?mProductMfr:"");
		activateParams.put("productOs",(mProductOs != null)?mProductOs:"");
		activateParams.put("productOsVersion",(mProductOsVersion != null)?mProductOsVersion:"");
		activateParams.put("hardwareSn",(mHardwareSn != null)?mHardwareSn:"");
		activateParams.put("memo",(mMemo != null)?mMemo:"");
		activateParams.put("signature",buildSnActivateSignature());
		return activateParams;
	}

	/*
	 * 获取刷新的参数
	 */
	private Map<String,String> getSnRefreshParams() throws Exception {
		Map<String,String> refreshParams = new HashMap<String,String>();
		refreshParams.put("udid",(mUdid != null)?mUdid:"");
		refreshParams.put("appKey",(mAppKey != null)?mAppKey:"");
		refreshParams.put("token", (mToken != null)?mToken:"");
		refreshParams.put("timestamp",getCurrentUnixTimestamp());
		refreshParams.put("signature", buildSnRefreshSignature());
		return refreshParams;
	}

	/*
	 * 构建激活的签名
	 */
	private String buildSnActivateSignature() throws Exception{
		List<String> params = new ArrayList<String>();
		params.add((mUdid != null)?mUdid:"");
		params.add((mDeviceSn != null)?mDeviceSn:"");
		params.add((mAppKey != null)?mAppKey:"");
		params.add((mTimeStamp != null)?mTimeStamp:"");
		params.add((mAppVersion != null)?mAppVersion:"");
		params.add((mPackageName != null)?mPackageName:"");
		params.add((mImei != null)?mImei:"");
		params.add((mMacAddress != null)?mMacAddress:"");
		params.add((mWifiSsid != null)?mWifiSsid:"");
		params.add((mTelecomOperator != null)?mTelecomOperator:"");
		params.add((mBssid != null)?mBssid:"");
		params.add((mProductName != null)?mProductName:"");
		params.add((mProductModel != null)?mProductModel:"");
		params.add((mProductMfr != null)?mProductMfr:"");
		params.add((mProductOs != null)?mProductOs:"");
		params.add((mProductOsVersion != null)?mProductOsVersion:"");
		params.add((mHardwareSn != null)?mHardwareSn:"");
		params.add((mMemo != null)?mMemo:"");
		params.add((mSecret != null)?mSecret:"");
		return PubUtil.buildSignature(params);
	}

	/*
	 * 构建刷新的签名
	 */
	private String buildSnRefreshSignature() throws Exception {
		List<String> params = new ArrayList<String>();
		params.add((mUdid != null)?mUdid:"");
		params.add((mAppKey != null)?mAppKey:"");
		params.add((mTimeStamp != null)?mTimeStamp:"");
		params.add((mToken != null)?mToken:"");
		params.add((mSecret != null)?mSecret:"");
		return PubUtil.buildSignature(params);
		
	}

	private String getCurrentUnixTimestamp() {
		long timeDelay = getTimeDelay();
		if (timeDelay!=0) {
			mTimeStamp = String.valueOf(PubUtil.transToUnixTimestamp(System.currentTimeMillis()+timeDelay)) ;
			return mTimeStamp ;
		}else {
			mTimeStamp = String.valueOf(PubUtil.transToUnixTimestamp(System.currentTimeMillis()));
			return mTimeStamp;
		}
	}
	
	//保存服务器与本地的时间差
	private void saveTimeDelay(long timeDelay){
		SharedPreferences sp = mContext.getSharedPreferences(RESPONESE_FILE_NAME, Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putLong("timeDelay", timeDelay);
		editor.commit();
	}
	
	//取出服务器与本地的时间差
	private long getTimeDelay(){
		SharedPreferences sp = mContext.getSharedPreferences(RESPONESE_FILE_NAME, Context.MODE_PRIVATE);
		return sp.getLong("timeDelay", 0);
	}

}
