package com.unisound.uscactivate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class DefaultHttpRequest {

	public String mUrl = null;
	public static final String ENCODE = "utf-8";
	private static final int mTimeout = 10000;
	public static HashMap<String, String> defaultHostMap = new HashMap<String, String>();

	public static void addDefaultHost(HashMap<String, String> hostMap) {
		defaultHostMap = hostMap;
	}

	public static String getHttpResponse(String uri, String method, String params) {

		HttpURLConnection mHttpURLConnection = null;
		PrintWriter out = null;
		BufferedReader in = null;
		StringBuilder result = new StringBuilder();
		try {
			URL url = new URL(uri);
			mHttpURLConnection = (HttpURLConnection) url.openConnection();
			// 设置通用的请求属性
			mHttpURLConnection.setRequestProperty("accept", "*/*");
			mHttpURLConnection.setRequestProperty("connection", "Keep-Alive");
			mHttpURLConnection.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			mHttpURLConnection.setConnectTimeout(mTimeout);
			mHttpURLConnection.setRequestMethod(method);
			// 发送POST请求必须设置如下两行
			mHttpURLConnection.setDoOutput(true);
			mHttpURLConnection.setDoInput(true);
			if (method.equals("POST")) {
				// 获取URLConnection对象对应的输出流
				out = new PrintWriter(mHttpURLConnection.getOutputStream());
				// 发送请求参数
				out.print(params);
				// flush输出流的缓冲
				out.flush();
				// 定义BufferedReader输入流来读取URL的响应
				in = new BufferedReader(new InputStreamReader(mHttpURLConnection.getInputStream(), "UTF-8"));
				String line;
				while ((line = in.readLine()) != null) {
					result.append(line);
				}
			} else {
				// 建立实际的连接
				mHttpURLConnection.connect();

				// 定义 BufferedReader输入流来读取URL的响应
				in = new BufferedReader(new InputStreamReader(mHttpURLConnection.getInputStream(), "UTF-8"));
				String line;
				while ((line = in.readLine()) != null) {
					result.append(line);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
					out = null;
				}
				if (in != null) {
					in.close();
					in = null;
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}

			if (mHttpURLConnection != null) {
				mHttpURLConnection.disconnect();
				mHttpURLConnection = null;
			}
		}
		return result.toString();
	}

	 /*
     * Function  :   发送Post请求到服务器
     * Param     :   params请求体内容
     */
    public static ResponseInfo submitPostData(String strUrlPath,Map<String, String> params,int timeoutMillis) {

        byte[] data = getRequestData(params).toString().getBytes();//获得请求体

        OutputStream outputStream = null;
        InputStream inputStream = null;
        BufferedReader br = null;
        HttpURLConnection httpURLConnection = null;
        String response = "";
        ResponseInfo responseInfo = new ResponseInfo();

        try {
            URL url = new URL(strUrlPath);
            httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setConnectTimeout(timeoutMillis);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("content-type", "application/x-www-form-urlencoded");
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));

            outputStream = httpURLConnection.getOutputStream();
            outputStream.write(data);

            int status = httpURLConnection.getResponseCode();
            responseInfo.setResponseHeaderTime(Network.getServerTimeDetal(httpURLConnection.getHeaderField("Date")));
            responseInfo.setResponseCurrentSystemTime(System.currentTimeMillis());

            if(status == HttpURLConnection.HTTP_OK) {
                inputStream = httpURLConnection.getInputStream();
                br = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
                String line;
                while ((line = br.readLine()) != null) {
                	response += line;
                }
            } else {
            	response =  "{}";
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        	try {
        		httpURLConnection = null;
        		if (outputStream != null) {
        			outputStream.close();
        		}
        		if (inputStream != null) {
        			inputStream.close();
        		}
        		if (br != null) {
        			br.close();
        		}
        	} catch (IOException e) {
        		e.printStackTrace();
        	}
        }
        responseInfo.setResponse(response);
        return responseInfo;
    }
    
	/*
	* Function  :   封装请求体信息
	* Param     :   params请求体属性内容
	*/
	private static String getRequestData(Map<String, String> params) {
        StringBuilder requestUrl = new StringBuilder();
        String joinChar = "";
        for (Map.Entry<String, String> entry : params.entrySet()) {
            requestUrl.append(joinChar);
            requestUrl.append(entry.getKey());
            requestUrl.append("=");
            String value;
            try {
                 value = URLEncoder.encode(entry.getValue(), "UTF-8");
             } catch (Exception e) {
                e.printStackTrace();
                LogUtil.w("encode error, key = " + entry.getKey());
                value = "";
             }
            requestUrl.append(value);
            joinChar = "&";
        }
        LogUtil.d("requestData : POST params is " + requestUrl.toString());
        return requestUrl.toString();
    }
}
