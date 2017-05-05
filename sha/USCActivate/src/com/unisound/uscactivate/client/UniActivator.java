package com.unisound.uscactivate.client;

import android.content.Context;

import com.unisound.uscactivate.ActivatorInterface;

public class UniActivator extends ActivatorInterface {

	/**
	 * 设置参数appkey，appSecret，APPVersion，deviceCode，imei, pkgName，feature，signature，
	 * passPortId和passPortToken10个必要设置的参数
	 * @param context
	 * @param appkey
	 * @param appSecret
	 */
	private UniActivator(Context context, String jsonStr) {
		super(context, jsonStr);
	}

	private static UniActivator INSTANCE = null;

	public static UniActivator getActivator(Context context, String jsonStr) {
		if (INSTANCE == null) {
			INSTANCE = new UniActivator(context, jsonStr);
		}
		return INSTANCE;
	}

	/**
	 * 激活（注册）方法
	 */
	public void activate(int type) {
		/*E-ACTIVATE-1*/
		super.activate(type);
		/*E-ACTIVATE-1*/
	}

	public void setDeviceSn(String deviceSn) {
		super.setDeviceSn(deviceSn);
	}
	
	public String getDeviceSn() {
		return super.getDeviceSn();
	}
	
	public void setToken(String token) {
		super.setToken(token);
	}
	
	public String getToken() {
		return super.getToken();
	}
	
	public String getAppKey() {
		return super.getAppKey();
	}

	public void setAppKey(String mAppKey) {
		super.setAppKey(mAppKey);
	}

	public void setSecret(String mSecret) {
		super.setSecret(mSecret);
	}

	public String getSecret() {
		return super.getSecret();
	}

	public String getImei() {
		return super.getImei();
	}

	public void setImei(String imei) {
		super.setImei(imei);
	}

	public String getAppVersion() {
		return super.getAppVersion();
	}

	public void setAppVersion(String appVersion) {
		super.setAppVersion(appVersion);
	}

	public String getPackageName() {
		return super.getPackageName();
	}

	public void setPackageName(String packageName) {
		super.setPackageName(packageName);
	}

	public String getMacAddress() {
		return super.getMacAddress();
	}

	/**
	 * 设置mac地址，非必要参数
	 */
	public void setMacAddress(String macAddress) {
		super.setMacAddress(macAddress);
	}

	public String getWifiSsid() {
		return super.getWifiSsid();
	}

	/**
	 * 设置wifiSSID，非必要参数
	 */
	public void setWifiSsid(String wifiSsid) {
		super.setWifiSsid(wifiSsid);
	}

	public String getTelecomOperator() {
		return super.getTelecomOperator();
	}

	public void setTelecomOperator(String telecomOperator) {
		super.setTelecomOperator(telecomOperator);
	}

	public String getMemo() {
		return super.getMemo();
	}

	/**
	 * 设置备用信息
	 */
	public void setMemo(String mMemo) {
		super.setMemo(mMemo);
	}

	public void setDebug(boolean debug) {
		super.setDebug(debug);
	}

	public String getVersion() {
		return super.getVersion();
	}

	/**
	 * 设置服务器地址
	 */
	public void setServer(String server){
		super.setServer(server);
	}
}
