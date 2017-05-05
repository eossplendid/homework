//package com.unisound.uploaduserinfo.outer;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.util.HashMap;
//
//public class DefaultHttpRequest {
//
//	public String mUrl = null;
//	public static final String ENCODE = "utf-8";
//	private static final int mTimeout = 10000;
//	public static HashMap<String, String> defaultHostMap = new HashMap<String, String>();
//
//	public static void addDefaultHost(HashMap<String, String> hostMap) {
//		defaultHostMap = hostMap;
//	}
//
//	public static String getHttpResponse(String uri, String method, String params) {
//
//		HttpURLConnection mHttpURLConnection = null;
//		PrintWriter out = null;
//		BufferedReader in = null;
//		StringBuilder result = new StringBuilder();
//		try {
//			URL url = new URL(uri);
//			mHttpURLConnection = (HttpURLConnection) url.openConnection();
//			// 设置通用的请求属性
//			mHttpURLConnection.setRequestProperty("accept", "*/*");
//			mHttpURLConnection.setRequestProperty("connection", "Keep-Alive");
//			mHttpURLConnection.setRequestProperty("user-agent",
//					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
//			mHttpURLConnection.setConnectTimeout(mTimeout);
//			mHttpURLConnection.setRequestMethod(method);
//			// 发送POST请求必须设置如下两行
//			mHttpURLConnection.setDoOutput(true);
//			mHttpURLConnection.setDoInput(true);
//			if (method.equals("POST")) {
//				// 获取URLConnection对象对应的输出流
//				out = new PrintWriter(mHttpURLConnection.getOutputStream());
//				// 发送请求参数
//				out.print(params);
//				// flush输出流的缓冲
//				out.flush();
//				// 定义BufferedReader输入流来读取URL的响应
//				in = new BufferedReader(new InputStreamReader(mHttpURLConnection.getInputStream(), "UTF-8"));
//				String line;
//				while ((line = in.readLine()) != null) {
//					result.append(line);
//				}
//			} else {
//				// 建立实际的连接
//				mHttpURLConnection.connect();
//
//				// 定义 BufferedReader输入流来读取URL的响应
//				in = new BufferedReader(new InputStreamReader(mHttpURLConnection.getInputStream(), "UTF-8"));
//				String line;
//				while ((line = in.readLine()) != null) {
//					result.append(line);
//				}
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				if (out != null) {
//					out.close();
//					out = null;
//				}
//				if (in != null) {
//					in.close();
//					in = null;
//				}
//			} catch (Exception e2) {
//				e2.printStackTrace();
//			}
//
//			if (mHttpURLConnection != null) {
//				mHttpURLConnection.disconnect();
//				mHttpURLConnection = null;
//			}
//		}
//		return result.toString();
//	}
//}
