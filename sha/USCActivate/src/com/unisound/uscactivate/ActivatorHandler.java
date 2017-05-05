package com.unisound.uscactivate;

import org.json.JSONObject;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.unisound.uscactivate.client.UniActivatorConstant;
import com.unisound.uscactivate.client.UniActivatorListener;

public class ActivatorHandler extends Handler {
	
	public ActivatorHandler(Looper looper) {
		super(looper);
	}
	
	private UniActivatorListener listener;

	public void setListener(UniActivatorListener listener) {
		this.listener = listener;
	}

	@Override
	public void handleMessage(Message msg) {
		if (listener == null) {
			LogUtil.w("ActivatorHandler listener == null");
			return;
		}
		switch (msg.what) {
			case HandlerValue.NO_NETWORK_MESSAGE:
				LogUtil.e("ActivatorHandler NO_NETWORK_ERROR");
				listener.onEvent(toJsonString(UniActivatorConstant.NO_NETWORK_ERROR));
				break;
			case HandlerValue.EXCEPTION_MESSAGE:
				LogUtil.e("ActivatorHandler EXCEPTION_ERROR");
				listener.onEvent(toJsonString(UniActivatorConstant.EXCEPTION_ERROR));
				break;
			case HandlerValue.RESPONSE_IS_NULL_MESSAGE:
				LogUtil.e("ActivatorHandler RESPONSE_IS_NULL_ERROR");
				listener.onEvent(toJsonString(UniActivatorConstant.RESPONSE_IS_NULL_ERROR));
				break;
			case HandlerValue.INVALID_URL_TYPE_MESSAGE:
				LogUtil.e("ActivatorHandler INVALID_URL_TYPE_ERROR");
				listener.onEvent(toJsonString(UniActivatorConstant.INVALID_URL_TYPE_ERROR));
				break;
			case HandlerValue.ACTIVATOR_STATUS_ERROR_MESSAGE:
				LogUtil.e("ActivatorHandler ACTIVATOR_STATUS_ERROR_MESSAGE");
				listener.onEvent(toJsonString(UniActivatorConstant.ACTIVATOR_STATUS_ERROR));
				break;
			case HandlerValue.GET_RESULT_MESSAGE:
				LogUtil.i("ActivatorHandler GET_RESULT");
				listener.onEvent((String) msg.obj);
				break;
			default:
				break;
		}
	}

	/**
	 * {
	 * "errorCode": "1000",
	 * "message": "设备激活注册成功",
	 * "registerCode": "29CCCB713A06A3C8F6DB0D23642C781157A0FDA7"
	 * }
	 * @param code
	 */
	private String toJsonString(int code) {

		JSONObject _JSON = new JSONObject();
		try {
			_JSON.put("errorCode", code);
			_JSON.put("registerCode", "");
			switch (code) {
				case UniActivatorConstant.NO_NETWORK_ERROR:
					_JSON.put("message", "没有网络连接错误");
					break;
				case UniActivatorConstant.EXCEPTION_ERROR:
					_JSON.put("message", "异常错误");
					break;
				case UniActivatorConstant.RESPONSE_IS_NULL_ERROR:
					_JSON.put("message", "返回结果为空错误");
					break;
				case UniActivatorConstant.INVALID_URL_TYPE_ERROR:
					_JSON.put("message", "无效激活类型");
					break;
				case UniActivatorConstant.ACTIVATOR_STATUS_ERROR:
					_JSON.put("message", "激活状态错误，已经有激活操作正在执行");
					break;
				default:
					_JSON.put("message", "未知错误");
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return _JSON.toString();
	}

}
