package com.esquel.APS.GEW_FN.domain.enums;

/**
 * 自定义异常的级别，分别用于标识异常的严重程序
 * @author ZhangKent
 *
 */
public enum ExceptionLevel {
	INFO,		// 非常异常情况，仅仅作出提示
	WARNING, 	// 警告，完成日志后，程序将继续
	ERROR, 		// 此类异常将会令程序错误，或会导致错误的执行结果，需要中断程序执行。
}
