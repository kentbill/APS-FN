package com.esquel.APS.GEW_FN.domain.enums;

public enum Lot_Break_Type {
	/**
	 * the tasks in the lot make a dead loop 
	 */
	DEAD_LOOP, 	
	/**
	 * the tasks in the lot do not make a chain 
	 */
	CHAIN_BROKE,			
	/**
	 * there is not the source task in the lot
	 */
	SOURCE_TASK_NOT_EXIST,
	/**
	 * there is not the sink task in the lot.
	 */
	SINK_TASK_NOT_EXIST,
	/**
	 * there are more than 1 source tasks in the lot.
	 */
	SOURCE_TASK_DUPLICATE,	
	/**
	 * there are more than 2 sink tasks in the lot.
	 */
	SINK_TASK_DUPLICATE,
	/**
	 * all the tasks in lot haven't the arrival time
	 */
	ALL_TAKS_HAS_NULL_ARRIVAL_TIME,
	/**
	 * the lot is perfect
	 */
	PERFECT
	
	
		 
}
