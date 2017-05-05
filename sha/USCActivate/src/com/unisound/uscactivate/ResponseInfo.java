package com.unisound.uscactivate;

import java.text.SimpleDateFormat;

public class ResponseInfo {
	private String response = "" ; //response响应结果
	private long responseHeaderTime ;//response响应头中的时间
	private String responseHeaderTimeString = "" ;//response响应头中的时间String
	private long responseCurrentSystemTime ; //获取response响应时的设备系统时间
	private String responseCurrentSystemTimeString = "" ;//获取response响应时的设备系统时间String
	public String getResponse() {
		return response;
	}
	
	public void setResponse(String response) {
		this.response = response;
	}
	
	public long getResponseHeaderTime() {
		return responseHeaderTime;
	}
	
	public void setResponseHeaderTime(long responseHeaderTime) {
		this.responseHeaderTime = responseHeaderTime;
		setResponseHeaderTimeString(transLongTimeToStringTime(responseHeaderTime));
		LogUtil.d("responseHeaderTime = "+responseHeaderTime);
	}
	
	public String getResponseHeaderTimeString() {
		return responseHeaderTimeString;
	}
	
	private void setResponseHeaderTimeString(String responseHeaderTimeString) {
		this.responseHeaderTimeString = responseHeaderTimeString;
	}
	
	public long getResponseCurrentSystemTime() {
		return responseCurrentSystemTime;
	}
	
	public void setResponseCurrentSystemTime(long responseCurrentSystemTime) {
		this.responseCurrentSystemTime = responseCurrentSystemTime;
		setResponseCurrentSystemTimeString(transLongTimeToStringTime(responseCurrentSystemTime));
		LogUtil.d("responseCurrentSystemTime = "+responseCurrentSystemTime);
	}
	
	public String getResponseCurrentSystemTimeString() {
		return responseCurrentSystemTimeString;
	}
	
	private void setResponseCurrentSystemTimeString(
			String responseCurrentSystemTimeString) {
		this.responseCurrentSystemTimeString = responseCurrentSystemTimeString;
	}
	
	 private String transLongTimeToStringTime(long time) {
		    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //dd/MM/yyyy
		    String strDate = sdfDate.format(time);
		    return strDate;
	}
}
