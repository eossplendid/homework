package com.unisound.uscactivate;

import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;

public class PubUtil {

	/**
	 * 对参数列表构造响应签名
	 * 
	 * @param params
	 * @return
	 */
	public static String buildSignature(List<String> params) throws Exception {
		if (params == null || params.isEmpty()) {
			return "";
		}

		// 升序排序参数值
		Collections.sort(params);

		StringBuilder sb = new StringBuilder();
		for (String param : params) {
			sb.append(param == null ? "" : param);
		}

		return getSHA1Digest(sb.toString());
	}

	/**
	 * 将字符串进行SHA1获取摘要，摘要为十六进制字符串
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static String getSHA1Digest(String data) throws Exception {
		String digest = "";
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] bytes = md.digest(data.getBytes("UTF-8"));
			digest = byte2hex(bytes);
		} catch (Exception e) {
			throw e;
		}

		return digest;
	}

	/**
	 * 二进制转十六进制字符串
	 *
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
	
	/**
	 * trans android time to UNIX的时间戳
	 */
	public static long transToUnixTimestamp(long androidTimeStamp) {
		return androidTimeStamp / 1000;
	}
}