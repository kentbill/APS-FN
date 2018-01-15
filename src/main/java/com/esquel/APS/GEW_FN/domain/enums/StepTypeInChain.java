package com.esquel.APS.GEW_FN.domain.enums;

/**
 * 步骤类型，在OptaPlanner构成的链中，每个机台计划会形成一个链，链由一个MachinePlan对象开头（称作anchor),后面跟随的是分配到该机台上的任务，称作Eneity.
 * 
 * @author ZhangKent
 *
 */
public enum StepTypeInChain {
	/**
	 * 机台计划,即链中的Anchor
	 */
	MACHINE_PLAN,
	/**
	 * 任务，即链中的Entity
	 */
	TASK
}
