package com.esquel.APS.GEW_FN.domain.enums;

/**
 * The checking and planning result of task and machine plan.
 * @author ZhangKent
 *
 */
public enum TaskMachinePlanBrokenType {
	/**
	 * The assigning of the task to particular machine plan was all well
	 */
	Not_Break,
	
	/**
	 * The tasks information is invalid.
	 */
	Broke_For_Invalid_Task,
	
	/**
	 * the information of task is not perfect.
	 */
	Broke_For_Invalid_Information_No_Perfect,
	
	/**
	 * There are two or more tasks that have arrival time in the same lot.
	 */
	Broke_For_Invalid_Lot_Double_Source_Task,
	
	/**
	 * the first task of a lot have a empty arrival time
	 */
	Broke_For_Invalid_Task_Arrival_Time_Empty,
	/**
	 * the arrival time of non-source task is not empty.
	 */
	Broke_For_Invalid_Non_Source_Task_Arrival_Time_Not_Empty,
	
	/**
	 * the attribute of the task is duplicate
	 */
	Broke_For_Invalid_Task_Attribute_Duplicate,
	
	/**
	 * There are NOT available machine plans could handle particular task, check for ProcessCode
	 */
	Broke_For_MachinePlan_Process_Not_Exist,
	
	/**
	 * There are NOT machine plans could handle particular task with the conditions, check for conditions of task and machine plan.
	 */
	Broke_For_MachinePlan_Process_Condition_Not_Match,
	
	/**
	 * There are NOT machine plans belong to particular department could handle the task, check for the department field of task and machine plan.
	 */
	Broke_For_Department_Not_Exist,	
	
	/**
	 * The max waiting time in a task is less than the Min waiting time.
	 */
	Broke_For_Invalid_Idle_Time,
	
	/**
	 * 
	 */
	Broke_For_Arriavle_Time,
	
	/**
	 * The task invalid or broke a rule, but didn't know why.
	 */
	Broke_Unknow,
	
	/**
	 * The assigning of the task to particular machine plan was broke the rule - "MachinePlan-Task-Compare"
	 */
	Broke_By_Planner_Assigned_To_Wrong_MachinePlan, 
	
	/**
	 * The assigning of the tasks to machine plans violated the process sequence of a lot.
	 */
	Broke_By_Planner_Violate_Process_Sequence_In_Lot,
	
	/**
	 * a task was located at a unusable time span
	 */
	Broke_By_Planner_Task_Locate_Out_Of_MachinePlan_Time_Span,
	/**
	 * the planning result is the planned start time is later than arrival time
	 */
	Broke_By_Planner_Task_Plan_Start_Time_Later_Than_Arrival_Time,
	/**
	 * the tasks in the same machine plan, but their production time is overlap
	 */
	Broke_By_Planner_Tasks_Time_Overloap,
	/**
	 * the first task in a machine plan is not the source task of a lot, source task means the first task in a lot.
	 */
//	Broke_By_Planner_Not_First_Task_As_Source_Task,
	
	/**
	 * while the planning finished, the tasks what wasn't initialized, labeled is as Uninitialized.
	 */
	Broke_By_Planner_Uninitialize,
	
	/**
	 * 
	 */
	Semi_Broke_By_Soft_Constraint
}
