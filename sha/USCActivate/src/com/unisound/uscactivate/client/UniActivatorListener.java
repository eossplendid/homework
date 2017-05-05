package com.unisound.uscactivate.client;

public interface UniActivatorListener {


	/**
	 * 返回
	 * @param errorCode
	 */
	public void onEvent(String jsonStr);
}
