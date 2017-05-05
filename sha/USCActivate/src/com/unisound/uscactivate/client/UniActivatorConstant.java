package com.unisound.uscactivate.client;

public final class UniActivatorConstant {

	/**
	 * 没有网络错误
	 */
	public static final int NO_NETWORK_ERROR = 1011;

	/**
	 * 异常错误
	 */
	public static final int EXCEPTION_ERROR = 1012;

	/**
	 * 返回结果为空错误
	 */
	public static final int RESPONSE_IS_NULL_ERROR = 1013;

	/**
	 * 无效激活类型
	 */
	public static final int INVALID_URL_TYPE_ERROR = 1014;

	/**
	 * 激活状态错误，已经有激活操作正在执行
	 */
	public static final int ACTIVATOR_STATUS_ERROR = 1015;

	/**
	 * 执行激活类型，一个是register，一个是reset
	 */
	public static final int REGISTER_ACTIVATE = 0;
	public static final int RESET_ACTIVATE = 1;

}
