package com.esquel.APS.GEW_FN.domain.enums;

/**
 * 两个时间的比较结果，分别有： EARLIER, LATER, EQUALS, INVALID_INPUT * @author ZhangKent
 *
 */
public enum TimeComparisonResultType {
	/**
	 * Time1比Time2早
	 */
	EARLIER,
	/**
	 * Time1比Time2晚
	 */
	LATER,
	/**
	 * 两个时间相等
	 */
	EQUALS,
	/**
	 * 输入的两个时间不合法
	 */
	INVALID_INPUT

}
