package com.unisound.uscactivate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Xml;

public class DeviceUtil {

	private static final String INVALID_IMEI = "000000000000000";
	public static String UUID_FILE_NAME = "";

	// private static String udid = "";

	/**
	 * appKey：应用KEY（字符串，最大长度50字节）
	 * 直接在应用中获取
	 */

	/**
	 * deviceCode：设备的IEMI（字符串，最大长度200字节）
	 * <uses-permission android:name="android.permission.READ_PHONE_STATE" />
	 */
	public static String getDeviceId(Context context, String deviceSn) {
		String udid = "";
		getUDIDFileName(context);
		SharedPreferences sp = context.getSharedPreferences(UUID_FILE_NAME, Context.MODE_PRIVATE);

		udid = getUDIDFromSdcard();

		if (udid != null && !udid.equals("")) {
			LogUtil.i("DeviceInfoUtil getUDID from sdcard= " + udid);
			if (!TextUtils.isEmpty(deviceSn) && !isUdidMatchDeviceSn(udid,deviceSn)) {
				udid = createNewUDID(context,deviceSn);
			} else {
				Editor editor = sp.edit();
				editor.putString("UDID", udid);
				editor.commit();
			}
			return udid;
		}

		udid = sp.getString("UDID", "");
		if (udid != null && !udid.equals("")) {
			LogUtil.i("DeviceInfoUtil getUDID from sharedPreferences= " + udid);
			if (!TextUtils.isEmpty(deviceSn) && !isUdidMatchDeviceSn(udid,deviceSn)) {
				udid = createNewUDID(context,deviceSn);
			} else {
				setUDIDToSdcard(UUID_FILE_NAME,udid);
			}
			return udid;
		}

		udid = createNewUDID(context,deviceSn);

		return udid;
	}
	
	private static boolean isUdidMatchDeviceSn(String udid, String deviceSn) {
		boolean isMatched = udid.equals(buildUdidFromDeviceSn(deviceSn));
		LogUtil.i("DeviceInfoUtil isUdidMatchDeviceSn = " + isMatched);
		return isMatched;
	}
	
	public static String createNewUDID(Context context, String deviceSn){
		SharedPreferences sp = context.getSharedPreferences(UUID_FILE_NAME, Context.MODE_PRIVATE);
		String udid = "";
		if (!TextUtils.isEmpty(deviceSn)) {
			udid = buildUdidFromDeviceSn(deviceSn);
		} else {
			udid = UUID.randomUUID().toString();
		}
		Editor editor = sp.edit();
		editor.putString("UDID", udid);
		editor.commit();
		
		setUDIDToSdcard(UUID_FILE_NAME,udid);
		LogUtil.i("DeviceInfoUtil createNewUDID UDID= " + udid);
		return udid;
	}

	protected static void setUDIDToSdcard(String fileName , String udid) {
		String _sdcardPath = null;
		RandomAccessFile raf = null;
		try {
			_sdcardPath = getSdcardPath();
			File _FileFolder = new File(_sdcardPath);
			if (!_FileFolder.exists()) {
				_FileFolder.mkdirs();
			}
			
			File _File = new File(_sdcardPath + File.separator + fileName);
			if (_File.exists()) {
				_File.delete();
			}
			_File.createNewFile();
			
			raf = new RandomAccessFile(_sdcardPath + File.separator + fileName, "rw");
			String _Content ="" ;

			_Content = "UDID="  + udid ;

			raf.write(_Content.getBytes());
		} catch (Exception e) {
			// e.printStackTrace();
		} finally {
			try {
				if (raf != null) {
					raf.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static String getUDIDFromSdcard() {
		String _sdcardPath = null;
		String _udid = "";
		RandomAccessFile raf = null;
		try {
			_sdcardPath = getSdcardPath();
			raf = new RandomAccessFile(_sdcardPath + File.separator + UUID_FILE_NAME, "rw");
			String line ;
			while ((line=raf.readLine())!=null) {
				if (line.contains("UDID")) {
					String[] udids = line.split("=");
					_udid = udids[1];
				}
			}
		} catch (Exception e) {
		} finally {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return _udid;
	}

	private static String getSdcardPath() {
		String _sdcardPath = null;
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			_sdcardPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "unisound/sdk";
		} else {
			_sdcardPath = "/mnt/sdcard/unisound/sdk";
		}
		return _sdcardPath;
	}

	protected static String getUDIDFileName(Context context) {
		if (UUID_FILE_NAME.equals("")) {
			String _imei = getIMEI(context);
			String _cpuInfo = getCpuInfo();
			UUID_FILE_NAME = getSHA1Digest(_imei + _cpuInfo);

		}
		return UUID_FILE_NAME;
	}

	public static String getIMEI(Context context) {
		String imei = "";
		imei = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
		if (imei != null && !"".equals(imei) && !imei.equals(INVALID_IMEI)) {
			return imei;
		}

		imei = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
		if (imei != null && !"".equals(imei) && !imei.equals(INVALID_IMEI)) {
			return imei;
		}
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		if (info != null) {
			return info.getMacAddress();
		}
		return INVALID_IMEI;

	}

	/**
	 * pkgName：SDK包名（字符串，最大长度200字节）不需要权限
	 */
	public static String getAppPackageName(Context context) {
		String packageName = "";
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			packageName = packageInfo.packageName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (packageName == null) {
			packageName = "";
		}
		LogUtil.i("DeviceUtil getAppPackageName= " + packageName);
		return packageName;
	}

	/**
	 * app 版本号 不需要权限
	 * @param context
	 * @return
	 */
	public static String getAppVersion(Context context) {
		String versionName = "";

		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

			versionName = packageInfo.versionName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (versionName == null) {
			versionName = "";
		}
		LogUtil.i("DeviceUtil getAppVersion= " + versionName);
		return versionName;
	}

	/**
	 * feature：特征码，随机字符串，每次上传都不一样，用来校验签名及区别不同次数的注册行为（字符串，最大长度100字节）
	 * 自己生成的随机字符串
	 */
	public static String getRandomString() {
		int len = 20;
		String returnStr = "";
		char[] ch = new char[len];
		Random rd = new Random(System.currentTimeMillis());
		for (int i = 0; i < len; i++) {
			// Log.e("yi", i + "= " + (char)i );
			// Log.e("yi", "i= " + i + " rd.nextInt= " + (rd.nextInt(26) + 65));
			ch[i] = (char) (rd.nextInt(26) + 97);
		}
		returnStr = new String(ch);
		LogUtil.i("DeviceUtil featurn= " + returnStr);
		return returnStr;
	}

	/**
	 * macAddress：设备网卡MAC地址（字符串，最大长度100字节）
	 * 需要权限：<uses-permission
	 * android:name="android.permission.ACCESS_WIFI_STATE"></uses-permission>
	 */
	public static String getMac(Context context) {
		if (context == null) {
			return INVALID_IMEI;
		}
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		if (info != null) {
			return info.getMacAddress();
		}
		LogUtil.i("DeviceUtil getMac= " + INVALID_IMEI);
		return INVALID_IMEI;
	}

	/**
	 * wifiSsid：设备WIFI的SSID（字符串，最大长度200字节）
	 * 需要权限：<uses-permission
	 * android:name="android.permission.ACCESS_WIFI_STATE"></uses-permission>
	 */
	public static String getWifiSSID(Context context) {
		String wifiSSID = "";
		try {
				WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
				WifiInfo wifiInfo = wifiManager.getConnectionInfo();
				if (wifiInfo != null) {
				String _SSID = wifiInfo.getSSID();
				if (_SSID!=null&&!_SSID.equals("")) {
					wifiSSID = _SSID.substring(1, _SSID.length() - 1);
				}
			}
		}catch (java.lang.SecurityException e) {
			LogUtil.e(e.getMessage()+" add permission android.permission.ACCESS_WIFI_STATE");
		}
		if (wifiSSID == null) {
			wifiSSID = "";
		}
		LogUtil.i("DeviceUtil getWifiSSID= " + wifiSSID);
		return wifiSSID;
	}

	/**
	 * telecomOperator：电信运营商（字符串，最大长度100字节）
	 * <uses-permission android:name= "android.permission.READ_PHONE_STATE"/>
	 */
	public static String getTelecomOperator(Context context) {
		String operator = "";
		TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		// telManager.getSimOperatorName();
		String imsi = telManager.getSubscriberId();
		if (imsi != null) {
			if (imsi.startsWith("46000") || imsi.startsWith("46002")) {
				// 因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号 //中国移动
				operator = "中国移动";
			} else if (imsi.startsWith("46001")) {
				// 中国联通
				operator = "中国联通";
			} else if (imsi.startsWith("46003")) {
				// 中国电信
				operator = "中国电信";
			} else {
				operator = "未找到对应运营商";
			}
		} else {
			operator = "没有手机卡";
		}
		LogUtil.i("DeviceUtil operator= " + operator);
		return operator;
	}

	/**
	 * passportId：用户通行证Passport的ID，用户在设备上登录时云端会返回此值，默认值：0，（数字字符串）
	 */

	/**
	 * passportToken：用户通行证Token，用户在深色登录时云端会返回此值（字符串）
	 */

	/**
	 * 将字符串进行SHA1获取摘要，摘要为十六进制字符串
	 * @param data
	 * @return
	 * @throws Exception
	 */
	private static String getSHA1Digest(String data) {
		String digest = "";
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] bytes = md.digest(data.getBytes("UTF-8"));
			digest = byte2hex(bytes);
		} catch (Exception e) {
			// logger.error(e.getMessage(), e);
		}
		return digest;
	}

	/**
	 * 二进制转十六进制字符串
	 * @param bytes
	 * @return
	 */
	private static String byte2hex(byte[] bytes) {
		StringBuilder sign = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(bytes[i] & 0xFF);
			if (hex.length() == 1) {
				sign.append("0");
			}
			sign.append(hex.toUpperCase());
		}

		return sign.toString();
	}

	private static String getCpuInfo() {
		String cpuInfo = "00000000000000000000000000000000";
		Process process = null;
		InputStream inputStream = null;
		BufferedReader readBuffer = null;
		String cupSeriral = null;
		String readLine = "";
		try {
			process = Runtime.getRuntime().exec("cat /proc/cpuinfo");
			if (process != null) {
				inputStream = process.getInputStream();
				if (inputStream != null) {
					readBuffer = new BufferedReader(new InputStreamReader(inputStream));
					readLine = readBuffer.readLine();
					while (readLine != null) {
						if (readLine.indexOf("Serial") > -1) {
							cupSeriral = readLine.substring(readLine.indexOf(":") + 1, readLine.length());
							cpuInfo = cupSeriral.trim();
							break;
						} else {
							readLine = readBuffer.readLine();
						}
					}
					readBuffer.close();
					inputStream.close();
				}
			}
		} catch (IOException ioException) {
		}
		if (cpuInfo == null) {
			cpuInfo = "00000000000000000000000000000000";
		}
		return cpuInfo;
	}

	
	
	/**
	 * BSSID：设备WIFI的BSSID（字符串）
	 * 需要权限：<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"></uses-permission>
	 * bssId
	 * @param context
	 * @return
	 */
	public static String getBSSID(Context context) {
		String bssid = "";
		try {
			WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			if (wifiInfo != null) {
				String _BSSID = wifiInfo.getBSSID();
				bssid = _BSSID;
			}
		} catch (java.lang.SecurityException e) {
			LogUtil.e(e.getMessage()+" add permission android.permission.ACCESS_WIFI_STATE");
		}
		if (bssid == null) {
			bssid = "";
		}
		LogUtil.i("DeviceUtil getBSSID= " + bssid);
		return bssid;
	}
	
	/**
	 * 设备型号
	 * productModel
	 * @return model
	 */
	public static String getProductModel(){
		String model =  android.os.Build.MODEL;//手机型号
		return model;
	}
	
	/**
	 * 工业设计名称
	 * productDevice
	 * @return brand
	 */
	public static String getProductDevice(){
		String device = android.os.Build.DEVICE;// 工业设计名称
		return device ;
	}
	
	/**
	 * 制造商名称
	 * productMfr
	 * @return productMfr
	 */
	public static String getProductMfr(){
		String productMfr = android.os.Build.MANUFACTURER;// 制造商名称
		return productMfr ;
	}
	
	/**
	 * 系统版本
	 * productOsVersion
	 * @return version
	 */
	public static String getProductOsVersion(){
		String productOsVersion = android.os.Build.VERSION.RELEASE;//系统版本
		return productOsVersion;
	}
	
	/**
	 * 设备名称
	 * productName
	 * @return productName
	 */
	public static String getProductName(){
		String productName = android.os.Build.PRODUCT;// 设备名称
		return productName ;
	}
	
//	public static long getCurrentTimeStamp(){
//		long timestamp = System.currentTimeMillis();
//		return timestamp ;
//	}
	
//	
//	public static String formatDate(long timeStamp) {
//		  Date nowTime = new Date(timeStamp);
//		  SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		  String retStrFormatNowDate = sdFormatter.format(nowTime);
//		  return retStrFormatNowDate;
//	}
	
	/**
	 * create udid history ,json->
	 * [{
	 * 	"udid": "09bca678-8e3d-4bdf-9e75-2b019cd7e38c",
	 *		"timestamp": "2015-11-30 16:56:24"
	 *		},
	 *		{
	 *		"udid": "601fc01f-b6a9-4cd7-8f04-c4338c6767ae",
	 *		"timestamp": "2016-01-01 15:56:24"
	 *		}]
	 * Map -> <fileName,UDIDParam>
	 * @param udidMap
	 * @return jsonString
	 */
	public static String createUDIDHistoryJson(Map<String, UDIDParam> udidMap) {
		JSONArray udidHistoryArray = new JSONArray();
		try {
			for (String fileName : udidMap.keySet()) {
				UDIDParam udidParam = udidMap.get(fileName);
				
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("udid", udidParam.getUdid());
				jsonObject.put("timestamp",udidParam.getTimeStamp());
				
				udidHistoryArray.put(jsonObject);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return udidHistoryArray.toString() ;
	}
	
	public static String collectAndProcessUdidsInfo(Context context){
		String udidHistory = "" ;
		Map<String, UDIDParam> udidMap = new HashMap<String, UDIDParam>();
		collectAllOuterUdidsFile(udidMap);
		collectAllInnerUdidsFile(context, udidMap);
		removeDuplicate(getUDIDFileName(context), udidMap);
		saveUdidsParamToInnerAndOuter(context, udidMap);
		udidHistory = createUDIDHistoryJson(udidMap);
		return udidHistory ;
	}
	
	/**
	 * collection inner udid
	 * @param context
	 */
	public static void collectAllInnerUdidsFile(Context context , Map<String, UDIDParam> udidMap){
		File file = new File(context.getCacheDir().getParent()+"/shared_prefs/");
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				File currentFile = files[i] ; 
				if (currentFile.isFile()) {
					String fileName = DeviceUtil.filterFileName(currentFile.getName());
					if (checkFileName(currentFile)) {
						UDIDParam udidparam = readUdidParamFromInnerXML(context, currentFile,fileName);
						if (udidparam!=null) {
							udidMap.put(fileName, udidparam);
						}
					}
				}
			}
		}
	}
	
	/**
	 * collection outer udid
	 * @param context
	 */
	public static void collectAllOuterUdidsFile( Map<String, UDIDParam> udidMap){
		String path = getSdcardPath() ;
		File file = new File(path);
		if (file.exists()&&file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				File currentFile = files[i] ; 
				if (currentFile.isFile()) {
					String fileName = currentFile.getName();
					if (checkFileName(currentFile)) {
						UDIDParam udidparam = readUdidParamFromOuter(fileName);
						if (udidparam!=null) {
							udidMap.put(DeviceUtil.filterFileName(fileName), udidparam);
						}
					}
				}
			}
		}
	}
	
	
	//检查文件名长度，只有长度符合要求的才返回,UDID的文件名经过SHA1算法处理,长度为40
	/**
	 * check fileName
	 * @param file
	 * @return true or false
	 */
	private static boolean checkFileName(File file){
		int stdLength = 40;
		String fileName = file.getName() ;
		String realFileName = filterFileName(fileName);
		if (realFileName.length() == stdLength) {
			return true ;
		}
			return false ;
	}
	
	/**
	 * fileter file name eg:  input->abc.txt  output->abc
	 * @param fileName
	 * @return
	 */
	private static String filterFileName(String fileName){
		String realFileName = "" ;
		if (fileName!=null&&!fileName.equals("")) {
			if (fileName.contains(".")) {
				realFileName = fileName.substring(0, fileName.lastIndexOf("."));
			}else {
				realFileName = fileName ;
			}
		}
		return realFileName ;
	}
	
	/**
	 * create new UDIDParam ,
	 * @param udid
	 * @param timeStamp
	 * @param fileName
	 * @return
	 */
	private static UDIDParam createUDIDParam(String udid,String timeStamp,String fileName){
		UDIDParam udidParam = null ;
		udidParam = new UDIDParam();
		udidParam.setFileName(fileName);
		udidParam.setUdid(udid);
		udidParam.setTimeStamp(timeStamp);
		return udidParam ;
	}
	
	/**
	 * read inner UDID ,SharedPreferences /data/data/package/
	 * @param context
	 * @param fileName
	 * @return
	 */
	public static UDIDParam readUdidParamFromInner(Context context,String fileName){
		
		SharedPreferences sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
		String udid = sp.getString("UDID", "");
		String timeStamp = sp.getString("TIMESTAMP", "");
		return createUDIDParam(udid, timeStamp, fileName);
	}
	
	/**
	 * read inner UDID ,SharedPreferences /data/data/package/
	 * @param context
	 * @param fileName
	 * @return
	 */
	public static UDIDParam readUdidParamFromInnerXML(Context context,File file , String fileName){
		String udid = "";
		String timeStamp = "" ;
		try {
		InputStream is = new FileInputStream(file);
		// 由android.util.Xml创建一个XmlPullParser实例
		XmlPullParser xpp = Xml.newPullParser();
		// 设置输入流 并指明编码方式
		xpp.setInput(is,"UTF-8");
		
		int eventType = xpp.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT){
		 switch (eventType) {
		             case XmlPullParser.START_DOCUMENT:
		                 break;
		             case XmlPullParser.START_TAG:
		                 if (xpp.getName().equals("string")) {
		                	 String value = xpp.getAttributeValue(0) ;
		                	 if (value.equals("TIMESTAMP")) {
		                		 eventType = xpp.next();
		                		 timeStamp = xpp.getText();
							}else if (value.equals("UDID")) {
		                		 eventType = xpp.next();
		                		 udid = xpp.getText() ;
							}
		                 } 
		                 break;
		             case XmlPullParser.END_TAG:
		                 break;
		             }
		             eventType = xpp.next();
		         }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
			return createUDIDParam(udid, timeStamp, fileName);
	}
	
	
	/**
	 * read outer UDID
	 * @param fileName
	 * @return
	 */
	private static UDIDParam readUdidParamFromOuter(String fileName){
		String _sdcardPath = null;
		String _udid = "";
		String _timeStamp = "" ;
		try {
			_sdcardPath = getSdcardPath();
			RandomAccessFile raf = new RandomAccessFile(_sdcardPath + File.separator + fileName, "rw");
			String line ;
			while ((line=raf.readLine())!=null) {
				if (line.contains("UDID")) {
					String[] udids = line.split("=");
					_udid = udids[1];
				}else if(line.contains("TIMESTAMP")){
					String[] timeStamps = line.split("=");
					_timeStamp = timeStamps[1];
				}
			}
		} catch (Exception e) {
			//TODO 
			//log error
		}
		return createUDIDParam(_udid, _timeStamp, fileName);
	}
	
	/**
	 * save udid to inner /data/data/package use SharedPreferences
	 * @param fileName
	 * @param udid
	 * @param timeStamp
	 */
	public static void saveUdidParamToInner(Context context , String fileName , String udid ,String timeStamp){
		SharedPreferences sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString("UDID", udid);
		editor.putString("TIMESTAMP", timeStamp);
		editor.commit();
	}
	
	/**
	 * save udid
	 * @param udidsMap
	 */
	private static void saveUdidsParamToInnerAndOuter(Context context , Map<String, UDIDParam> udidsMap){
		for (String fileName : udidsMap.keySet()) {
			UDIDParam udidParam =  udidsMap.get(fileName);
			String udid = udidParam.getUdid() ;
			String timeStamp = udidParam.getTimeStamp();

			//saveUdidToInner
			saveUdidParamToInner(context, fileName, udid, timeStamp);
			
			//saveUdidToOuter
			setUDIDToSdcard(fileName, udid);
		}
	}
	
	/**
	 * remove Current used udid
	 */
	private static void removeDuplicate(String fileName , Map<String, UDIDParam> udidsParamMap){
		boolean isDuplicate = false ; 
		for (String key : udidsParamMap.keySet()) {
			if (key.equals(fileName)) {
				isDuplicate = true ;
			}
		}
		if (isDuplicate) {
			udidsParamMap.remove(fileName);
		}
	}
	
	private static String getSNFileName() {
		String snName = "SN" ;
//		snName = getSHA1Digest("SN");
		return snName;
	}
	
	public static String readSN(){
		String line = "";
		String filePath = getSdcardPath() + File.separator + getSNFileName() ;
		File file = new File(filePath);
		if (file.exists()) {
			try {
				RandomAccessFile raf = new RandomAccessFile(filePath , "rw");
				line = raf.readLine();
			} catch (Exception e) {
				LogUtil.e(e.getMessage()+"readSN error");
			}
		}
		return line ;
	}

	public static String buildUdidFromDeviceSn(String deviceSn) {
		String udid = "";
		if (!TextUtils.isEmpty(deviceSn)) {
			udid = Base64.encodeToString(deviceSn.getBytes(), Base64.URL_SAFE);
		}

		udid = udid.replaceAll("=", "_").replaceAll("\\s", "");
		LogUtil.d("build udid by deviceSn, udid = " + udid);
		return udid;
	}
}
