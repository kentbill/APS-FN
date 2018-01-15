package com.esquel.APS.GEW_FN.domain.enums;
/**
 * the task location in a lot 
 * @author ZhangKent
 *
 */
public enum TaskType_Location_In_Lot {
	/**
	 * the first task in a lot.
	 */
	SOURCE,
	/**
	 * the middle task in a lot.
	 */
	NORMAL,
	/**
	 * the last task in a lot.
	 */
	SINK,
	/**
	 * the only one task in a lot, it means the task is the SOURCE, MIDDLE and SINK task in a lot.
	 */
	ONLY_ONE
}
