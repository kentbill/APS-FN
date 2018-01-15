package com.esquel.APS.GEW_FN.solver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.optaplanner.core.api.domain.valuerange.CountableValueRange;
import org.optaplanner.core.api.domain.valuerange.ValueRangeFactory;

import com.esquel.APS.GEW_FN.domain.MachinePlan;
import com.esquel.APS.GEW_FN.domain.Step;
import com.esquel.APS.GEW_FN.domain.Task;
import com.esquel.APS.GEW_FN.domain.enums.TaskType_Location_In_Lot;
import com.esquel.APS.Helpers.Common;

/**
 * a tools to verify the planning result
 * 
 * @author ZhangKent
 *
 */
public class PlanningVerification {

	private static final int WHITE_TYPE_CLEAR_MACHINE_ID = 28; // WHITE_TYPE转换洗机的非生产任务ID为28
	private static final int DOSAGE_CLEAR_MACHINE_ID = 15; // 当量转换洗机的非生产任务ID为15
	private static final int TQOR_CLEAR_MACHINE_ID = 29; // 特殊色E转换洗机的非生产任务ID为29
	private static final int CROW_YARN_CLEAR_MACHINE_ID = 27; // 牛仔纱与普通转换洗机的非生产任务ID为27
	private static final int NATURAL_DYES_CLEAR_MACHINE_ID = 30; // 天然染料转换洗机的非生产任务ID为30
	private static final int SELVAGE_GUIDE_MACHINE_SPEED_ID = 17; // 两卡车速相差超过10，接导布
	private static final int SELVAGE_GUIDE_FORMULA_CHANGE_ID = 24; // 两卡配方不同，接导布
	private static final int SELVAGE_GUIDE_COLLECT_ID = 23; // 两卡落布类型不同，接导布
	
	
	private static final String WHITE_TYPE_CLEAR_MACHINE_NAME = "特白转漂白清洁机台"; // WHITE_TYPE转换洗机的非生产任务ID为28
	private static final String DOSAGE_CLEAR_MACHINE_NAME = "大当量段转小当量段清洁机台"; // 当量转换洗机的非生产任务ID为15, 大当量段转小当量段清洁机台,大当量段转小当量段清洁机台
	private static final String TQOR_CLEAR_MACHINE_NAME = "特殊色转换"; // 特殊色E转换洗机的非生产任务ID为29
	private static final String CROW_YARN_CLEAR_MACHINE_NAME = "牛仔纱与普通转换洗机"; // 牛仔纱与普通转换洗机的非生产任务ID为27
	private static final String NATURAL_DYES_CLEAR_MACHINE_NAME = "天然染料与普通转换洗机"; // 天然染料转换洗机的非生产任务ID为30
	private static final String SELVAGE_GUIDE_MACHINE_SPEED_NAME = "接导布"; // 两卡车速相差超过10，接导布
	private static final String SELVAGE_GUIDE_FORMULA_CHANGE_NAME = "接导布"; // 两卡配方不同，接导布
	private static final String SELVAGE_GUIDE_COLLECT_NAME = "落布类型(干湿落布要求)不同接导布改穿布路线"; // 两卡落布类型不同，接导布

	private static final String WHITE_TYPE_CLEAR_MACHINE_MSG = "特白转漂白清洁机台的非生产任务"; // WHITE_TYPE转换洗机的非生产任务ID为28
	private static final String DOSAGE_CLEAR_MACHINE_MSG = "当量转换洗机的非生产任务"; // 当量转换洗机的非生产任务ID为15
	private static final String TQOR_CLEAR_MACHINE_MSG = "特殊色转换洗机的非生产任"; // 特殊色E转换洗机的非生产任务ID为29
	private static final String CROW_YARN_CLEAR_MACHINE_MSG = "牛仔纱与普通转换洗机的非生产任务"; // 牛仔纱与普通转换洗机的非生产任务ID为27
	private static final String NATURAL_DYES_CLEAR_MACHINE_MSG = "天然染料转换洗机的非生产任务"; // 天然染料转换洗机的非生产任务ID为30
	private static final String SELVAGE_GUIDE_MACHINE_SPEED_MSG = "两卡车速相差超过10，接导布"; // 两卡车速相差超过10，接导布
	private static final String SELVAGE_GUIDE_FORMULA_CHANGE_MSG = "两卡配方不同，接导布"; // 两卡配方不同，接导布
	private static final String SELVAGE_GUIDE_COLLECT_MSG = "两卡落布类型不同，接导布"; // 两卡落布类型不同，接导布
	
	public static List<String> checkMachinePlanMatching(List<Task> tasks){
		List<String> errMessage = new ArrayList<String>();
		if (tasks == null || tasks.isEmpty()) {
			return errMessage;
		}
		
		
		for(Task task : tasks){
			if(task.getMachinePlan() == null){
				String msg = "任务[" + task + "]未被分配到任何机台";
				errMessage.add(msg);
			}
			
			if(!task.getUsableMachinePlan().contains(task.getMachinePlan())){
				String msg = "任务[" + task + "]被分配到错误的机台[" + task.getMachinePlan() + "]";
				errMessage.add(msg);
			}
		}
		return errMessage;
		
	}

	/**
	 * Checks if the planned start time is earlier than arrival time in a task.
	 * 
	 * @param tasks
	 * @return
	 */
	public static List<String> checkArriavleTimeAndPlannedStartTime(List<Task> tasks) {
		List<String> errMessage = new ArrayList<String>();
		
		if (tasks == null || tasks.isEmpty()) {
			return errMessage;
		}

		List<Task> invalidTasks = tasks.stream().filter(
				x ->(x.getArrivalTimeL() != null && 
					x.getArrivalTimeL() != 0L && 
					x.getPlanStartTimeL() != null && 
					x.getPlanStartTimeL() != 0L && 
					x.getPlanStartTimeL() < x.getArrivalTimeL())).collect(Collectors.toList());
		
		if(invalidTasks == null || invalidTasks.isEmpty()){
			return errMessage;
		}
		
		for(Task task : invalidTasks){
			String msg = "任务[" + task + "]的计划开始时间比预计到达时间早, 预计到达时间[" + Common.getTimeByMinuteLong(task.getArrivalTimeL()) + "], 开始时间[" + Common.getTimeByMinuteLong(task.getPlanStartTimeL()) + "]";
			errMessage.add(msg);
		}

		return errMessage;
	}
	
	/**
	 * 检查每个机台中，是否存在时间上有重叠的任务
	 * @param tasks
	 * @return
	 */
	public static List<String> checkTaskTimeOverlap(List<Task> tasks, List<MachinePlan> machinePlans){
		List<String> errMessage = new ArrayList<String>();
		
		for(MachinePlan machinePlan : machinePlans){
			if(machinePlan.getTasks() == null || machinePlan.getTasks().isEmpty()){
				continue;
			}
			
			// 检查每个任务是否跟其处于相同机台下其它的任务存在时间重叠
			if(CommonFunctions.isOverlap(machinePlan.getTasks())){
				String msg = "存在同一机台[" + machinePlan + "]上的任务有时间重叠的情况.";
				errMessage.add(msg);
			}
			
			/*
			// 检查每个任务是否跟其处于相同机台下其它的任务存在时间重叠
			for (Task task : machinePlan.getTasks()) {
				Task overLapTask = machinePlan.getTasks().stream().filter(x -> (!x.equals(task)) && CommonFunctions.isOverlap(x, task))
						.findAny().orElse(null);
				if (overLapTask != null) {
					String msg = "被分配到同一机台[" + machinePlan + "]的任务[" + task + "]与任务[" + overLapTask + "]，存在时间重叠";
					errMessage.add(msg);
				}
			}
			*/
		}
		
		return errMessage;
		
	}
	
	/*
	private static boolean isOverlap(Task taskLeft, Task taskRight){
		CountableValueRange<Long> range1 = ValueRangeFactory.createLongValueRange(taskLeft.getPlanStartTimeL(), taskLeft.getPlanEndTimeL());
		CountableValueRange<Long> range2 = ValueRangeFactory.createLongValueRange(taskRight.getPlanStartTimeL(), taskRight.getPlanEndTimeL());
		
		List<Long> list1 = getLongList(range1);
		List<Long> list2 = getLongList(range2);
		
		return list1.stream().filter(x -> list2.contains(x)).findAny().orElse(null) != null;
		
	}
	
	private static List<Long> getLongList(CountableValueRange<Long> range) {

		List<Long> longList = new ArrayList<Long>();
		Iterator<Long> itrTG = range.createOriginalIterator();
		
		
		while (itrTG.hasNext()) {
			Long lTG = itrTG.next();

			if (!longList.contains(lTG)) {
				longList.add(lTG);
			}
		}

		return longList;

	}
	*/
	
	
	/**
	 * 检查同一批次的任务，是否出现后任务的开始时间比前任务的结束时间早的情况
	 * @param tasks
	 * @return
	 */
	public static List<String> checkTaskSequenceInLot(List<Task> tasks) {
		List<String> errMessage = new ArrayList<String>();
		for (Task task : tasks) {
			if (task.getTaskLocation() == TaskType_Location_In_Lot.SOURCE
					|| task.getTaskLocation() == TaskType_Location_In_Lot.ONLY_ONE || task.getPlanStartTimeL() == null
					|| task.getPlanStartTimeL() == 0L || task.getPreviousTaskInSameLot() == null) {
				continue;
			}

			if (task.getPlanStartTimeL() < task.getPreviousTaskInSameLot().getPlanEndTimeL()) {
				String msg = "任务[" + task + "] 的开始时间较其前置任务[" + task.getPreviousTaskInSameLot() + "]的结束时间早";
				errMessage.add(msg);

			}
		}

		return errMessage;
	}
	
	/**
	 * 检查任务的结束时间是否超出它所在机台的结束时间
	 * @param tasks
	 * @return
	 */
	public static List<String> checkIfTaskEndTimeLaterThanMachinePlanEndTime(List<Task> tasks){
		List<String> errMessage = new ArrayList<String>();
		
		for(Task task : tasks){
			if(task == null || task.getMachinePlan() == null || task.getPlanEndTimeL() == null || task.getPlanEndTimeL() == 0){
				continue;
			}
			
			if(task.getPlanEndTimeL() > task.getMachinePlan().getEndTimeL()){
				String msg = "任务[" + task + "] 的结束时间比其所在机台[" + task.getMachinePlan() + "]的结束时间晚";
				errMessage.add(msg);
			}
		}
		
		return errMessage;
	}

	/**
	 * Checks if the non-production should create but not appear in a task.
	 * 
	 * @param tasks
	 * @return
	 */
	public static List<String> checkShaoMaoNonproductionTask(List<MachinePlan> machinePlans) {
		// 获得烧毛工序的machinePlan列表 
		List<String> shaoMaoProcess = new ArrayList<String>();
		shaoMaoProcess.add("023");
		shaoMaoProcess.add("111");
		shaoMaoProcess.add("114");
		List<MachinePlan> shaoMaoMachinePlans = getMachinePlanByProcesses(machinePlans, shaoMaoProcess);
		List<String> errMessages = getNonproductionCheckingResult(shaoMaoMachinePlans);
		
		return errMessages;

	}
	
	/**
	 * 检查非生产任务
	 * @param machinePlans
	 * @return
	 */
	private static List<String> getNonproductionCheckingResult(List<MachinePlan> machinePlans){
		
		List<String> errMessages = new ArrayList<String>();

		for (MachinePlan machinePlan : machinePlans) {
			for (Task task : machinePlan.getTasks()) {
				

				Step preStep = task.getPreviousStep();

				// 仅任务之间有非生产任务
				if (preStep == null || preStep instanceof MachinePlan) {
					continue;
				}

				Task preTask = (Task) preStep;
				// 仅大货之间有非生产任务
				if (!preTask.getTaskOrderType().equals("Bulk") || !task.getTaskOrderType().equals("Bulk")) {
					continue;
				}

				// WHITE_TYPE(本白、漂白) ###############################################
				/*
				String s = "";
				if(preTask.getId() == 440 && task.getId() == 966){
					s = "s";
				}
				*/
				String msg = checkWHITE_TYPE(preTask, task);
				if (msg != "") {
					errMessages.add(msg);
				}
				// ######################################################################

				// 当量 #################################################################
				// 烧毛没有当量转换虚拟任务
				/*
				msg = checkDangLiang(preTask, task);
				if (msg != "") {
					errMessages.add(msg);
				}
				// ######################################################################
				*/
				
				// 特殊色转换############################################################
				msg = checkTeShuShe(preTask, task);
				if (msg != "") {
					errMessages.add(msg);
				}
				// ######################################################################
				
				
				// 牛仔纱与普通转换洗机##################################################
				msg = checkNiuZaiSha(preTask, task);
				if (msg != "") {
					errMessages.add(msg);
				}
				// ######################################################################

				// 天然染料与普通转换洗机################################################
				msg = checkTianYanRanLiao(preTask, task);
				if (msg != "") {
					errMessages.add(msg);
				}
				// ######################################################################
				

				// 工序转换##############################################################
				/*
				msg = checkGongXuZhuanHuan(preTask, task);
				if (msg != "") {
					errMessages.add(msg);
				}
				*/
				// ######################################################################
				

				// 退浆配方转换 #########################################################
				// 未实现?????????????????????

				// 丝光碱浓转换
				// 未实现?????????????????????

				// 接导布 - 车速相差超过10码/秒, 或配方不同
				
				msg = checkJieDaoBu(preTask, task);
				if (msg != "") {
					errMessages.add(msg);
				}

				
				// 接导布 - 落布方式不同
				// 去除 !!!!!!!!!!!!!!!!!!!!!!!!
				
				
			}
		}
		
		return errMessages;
	}
	
	/**
	 * 检查WHITE_TYPE非生产任务
	 * @param preTask
	 * @param currentTask
	 * @return
	 */
	private static String checkWHITE_TYPE(Task preTask, Task currentTask){
		String msg = "";
		
		String preTaskOrderType = preTask.getTaskOrderType();
		String currentTaskOrderType = currentTask.getTaskOrderType();
		
		boolean shouldExistNonPro = true;
		
		shouldExistNonPro = preTaskOrderType.equals("Bulk") && currentTaskOrderType.equals("Bulk");
		
		if(shouldExistNonPro){
			// 特白转漂白清洁机台:特白、加白、少荧转换为漂白、本白时插入任务
			shouldExistNonPro = (((preTask.getAttributeValue("WHITE_TYPE").equals("漂白") || preTask.getAttributeValue("WHITE_TYPE").equals("本白")) 
				&& (currentTask.getAttributeValue("WHITE_TYPE").equals("特白")|| currentTask.getAttributeValue("WHITE_TYPE").equals("加白") || currentTask.getAttributeValue("WHITE_TYPE").equals("少荧")))) ;
		}
		
		msg = checkIfNonproductionExist(preTask, currentTask, WHITE_TYPE_CLEAR_MACHINE_NAME, WHITE_TYPE_CLEAR_MACHINE_MSG, shouldExistNonPro);

		return msg;
	}
	
	/**
	 * 检查当量非生产任务
	 * @param machinePlans
	 * @return
	 */
	/*
	private static String checkDangLiang(Task preTask, Task currentTask){
		String msg = "";
		
		String preTaskOrderType = preTask.getTaskOrderType();
		String currentTaskOrderType = currentTask.getTaskOrderType();
		
		boolean shouldExistNonPro = true;
		
		shouldExistNonPro = preTaskOrderType.equals("Bulk") && currentTaskOrderType.equals("Bulk");
		
		if(shouldExistNonPro){
		
			String preTaskDangLiangStr = preTask.getAttributeValue("当量段");
			String currentTaskDangLiangStr = currentTask.getAttributeValue("当量段");
			
			if(preTaskDangLiangStr == null || currentTaskDangLiangStr == ""){
				return msg;
			}
			
			int preTaskDangLiang =  preTaskDangLiangStr == "" ? 0 : Integer.parseInt(preTaskDangLiangStr);
			int currentTaskDangLiang = currentTaskDangLiangStr == "" ? 0 : Integer.parseInt(currentTaskDangLiangStr);
			
			// 
			
			shouldExistNonPro = preTaskDangLiang > currentTaskDangLiang;
		}
		
		msg = checkIfNonproductionExist(preTask, currentTask, DOSAGE_CLEAR_MACHINE_NAME, DOSAGE_CLEAR_MACHINE_MSG, shouldExistNonPro);
		
		return msg;
	}
	*/
	
	/**
	 * 检查特殊色非生产任务
	 * @param preTask
	 * @param currentTask
	 * @return
	 */
	private static String checkTeShuShe(Task preTask, Task currentTask){
		String msg = "";
		
		String preTaskOrderType = preTask.getTaskOrderType();
		String currentTaskOrderType = currentTask.getTaskOrderType();
		
		boolean shouldExistNonPro = true;
		
		shouldExistNonPro = preTaskOrderType.equals("Bulk") && currentTaskOrderType.equals("Bulk");
		
		if(shouldExistNonPro){
			
			String preTaskStr = preTask.getAttributeValue("TQOR");
			String currentTaskStr = currentTask.getAttributeValue("TQOR");
			
			shouldExistNonPro = ((!preTaskStr.equals("") || !currentTaskStr.equals("")) && (!preTaskStr.equals(currentTaskStr)));
		}
		
		msg = checkIfNonproductionExist(preTask, currentTask, TQOR_CLEAR_MACHINE_NAME, TQOR_CLEAR_MACHINE_MSG, shouldExistNonPro);
		return msg;
	}

	/**
	 * 检查牛仔纱非生产任务
	 * @param preTask
	 * @param currentTask
	 * @return
	 */
	private static String checkNiuZaiSha(Task preTask, Task currentTask){
		String msg = "";
		
		String preTaskOrderType = preTask.getTaskOrderType();
		String currentTaskOrderType = currentTask.getTaskOrderType();
		
		boolean shouldExistNonPro = true;
		
		shouldExistNonPro = preTaskOrderType.equals("Bulk") && currentTaskOrderType.equals("Bulk");
		
		if(shouldExistNonPro){
		
			String preTaskStr = preTask.getAttributeValue("牛仔纱");
			String currentTaskStr = currentTask.getAttributeValue("牛仔纱");
			
			shouldExistNonPro = ((!preTaskStr.equals("") || !currentTaskStr.equals("")) && (!preTaskStr.equals(currentTaskStr)));
		}
		
		msg = checkIfNonproductionExist(preTask, currentTask, CROW_YARN_CLEAR_MACHINE_NAME, CROW_YARN_CLEAR_MACHINE_MSG, shouldExistNonPro);
		return msg;
	}
	


	/**
	 * 检查天然染料非生产任务
	 * @param preTask
	 * @param currentTask
	 * @return
	 */
	private static String checkTianYanRanLiao(Task preTask, Task currentTask){
		
		String msg = "";
		
		String preTaskOrderType = preTask.getTaskOrderType();
		String currentTaskOrderType = currentTask.getTaskOrderType();
		
		boolean shouldExistNonPro = true;
		
		shouldExistNonPro = preTaskOrderType.equals("Bulk") && currentTaskOrderType.equals("Bulk");
		
		if(shouldExistNonPro){

			// 两工序间是否满足天然染料转换条件
			boolean isTRYL = isTRYLSwit(preTask, currentTask);
			
			// 两个工序同时天然染料
			boolean isBothTRYL = isBothTRYL(preTask, currentTask);
			
			// 同种天然染料，但品名不同
			boolean isNotSameGF = preTask.getGf_id().equals(currentTask.getGf_id());
			
			// 天然染料与非天然染料之间需要洗机； 或两卡同是天然染料，但两个卡属于不同的GF，需要洗机。
			shouldExistNonPro = isTRYL || (isBothTRYL && isNotSameGF);
			
		}
		msg = checkIfNonproductionExist(preTask, currentTask, NATURAL_DYES_CLEAR_MACHINE_NAME, NATURAL_DYES_CLEAR_MACHINE_MSG, shouldExistNonPro);
		return msg;
	}
	
	/**
	 * 检查是否符合天然染料条件
	 * 
	 * @param preTask
	 * @param currentTask
	 * @return
	 */
	private static boolean isTRYLSwit(Task preTask, Task currentTask){
		String preTaskStr = preTask.getAttributeValue("天然染料");
		String currentTaskStr = currentTask.getAttributeValue("天然染料");
		
		// 不是前后任务均为普通染料的，即符合天然染料。
		return (!(preTaskStr.equals("") && currentTaskStr.equals("")));
		
	}
	
	private static boolean isBothTRYL(Task preTask, Task currentTask){
		String preTaskStr = preTask.getAttributeValue("天然染料");
		String currentTaskStr = currentTask.getAttributeValue("天然染料");
		
		return (preTaskStr.equals("Y") && currentTaskStr.equals("Y"));
	}
	
	/**
	 * 工序转换非生产任务
	 * @param preTask
	 * @param currentTask
	 * @return
	 */
	/*
	private static String checkGongXuZhuanHuan(Task preTask, Task currentTask){
		String msg = "";
		
		String preTaskProcCode = preTask.getProcessCode();
		String currentTaskProcCode = currentTask.getProcessCode();
		
		if(preTaskProcCode == null || preTaskProcCode == ""){
			return msg;
		}
	
		// 工序不同，需要添加工序转换非生产任务
		boolean shouldExistNonPro = !preTaskProcCode.equals(currentTaskProcCode);
		msg = checkIfNonproductionExist(preTask, currentTask, DOSAGE_CLEAR_MACHINE_ID, DOSAGE_CLEAR_MACHINE_MSG, shouldExistNonPro);
		
		return msg;
	}
	*/
	
	/**
	 * 检查接导布：
	 * 1. 两卡车速开差超过10m/s, 或
	 * 2. 两卡配方不同
	 * @param preTask
	 * @param currentTask
	 * @return
	 */
	private static String checkJieDaoBu(Task preTask, Task currentTask){
		String msg = "";
		
		String preTaskOrderType = preTask.getTaskOrderType();
		String currentTaskOrderType = currentTask.getTaskOrderType();
		
		boolean shouldExistNonPro = true;
		
		shouldExistNonPro = preTaskOrderType.equals("Bulk") && currentTaskOrderType.equals("Bulk");
		
		if(shouldExistNonPro){
		
			
			
			String preTaskMachineSpeedStr = preTask.getAttributeValue("车速");
			String currentTaskMachineSpeedStr = currentTask.getAttributeValue("车速");
			
			if(preTaskMachineSpeedStr == null || currentTaskMachineSpeedStr == ""){
				return msg;
			}
			
			int preTaskMachineSpeed = preTaskMachineSpeedStr == "" ? 0 : Integer.parseInt(preTaskMachineSpeedStr);
			int currentTaskMachineSpeed = currentTaskMachineSpeedStr == "" ? 0 : Integer.parseInt(currentTaskMachineSpeedStr);
			
			int speedDifference = Math.abs(currentTaskMachineSpeed - preTaskMachineSpeed);
			
			boolean cheShu_Diff10 = speedDifference > 10;
			
			String preTaskStr = preTask.getAttributeValue("配方");
			String currentTaskStr = currentTask.getAttributeValue("配方");
			
			boolean peiFang_Diff = (!preTaskStr.equals("") || !currentTaskStr.equals("")) && (!preTaskStr.equals(currentTaskStr));
			
			shouldExistNonPro = cheShu_Diff10 || peiFang_Diff;	
			
		}
		
		msg = checkIfNonproductionExist(preTask, currentTask, SELVAGE_GUIDE_MACHINE_SPEED_NAME, SELVAGE_GUIDE_MACHINE_SPEED_MSG, shouldExistNonPro);
		
		return msg;
	}

	/**
	 * 检查两个工序指定属性值是否相同（字符串）
	 * @param preTask
	 * @param currentTask
	 * @param attributeName
	 * @return
	 */
	private static boolean checkStringEquals(Task preTask, Task currentTask, String attributeName){
	
		String preTaskTeShuSheStr = preTask.getAttributeValue(attributeName);
		String currentTaskTeShuSheStr = currentTask.getAttributeValue(attributeName);
		
		boolean isEqual = (!preTaskTeShuSheStr.equals("") && !currentTaskTeShuSheStr.equals("") &&  preTaskTeShuSheStr.equals(currentTaskTeShuSheStr));
		return isEqual;
	}
	
	
	
	/**
	 * 根据指定的工序编号，获得指定可处理的机台
	 * @param machinePlans
	 * @param processCodes
	 * @return
	 */
	private static List<MachinePlan> getMachinePlanByProcesses(List<MachinePlan> machinePlans,
			List<String> processCodes) {
		List<MachinePlan> selectedMachinePlan = new ArrayList<MachinePlan>();
		for (MachinePlan machinePlan : machinePlans) {
			if (machinePlan.getProcesses().stream().filter(x -> processCodes.contains(x.getCode())).findAny()
					.orElse(null) != null) {
				selectedMachinePlan.add(machinePlan);
			}
		}

		return selectedMachinePlan;
	}
	
	/**
	 * 检查两个任务之间是否应该存在指定非生产任务
	 * @param preTask
	 * @param currentTask
	 * @param nonproductionID
	 * @param nonproductionName
	 * @param shouldExist
	 * @return
	 */
	private static String checkIfNonproductionExist(Task preTask, Task currentTask,
			String nonproductionName, String nonproductionMsg, boolean shouldExist) {
		String msg = "";

		if (shouldExist) {
			if (currentTask.getNonProductionTaskList() == null || currentTask.getNonProductionTaskList().isEmpty()
					|| currentTask.getNonProductionTaskList().stream().filter(x -> x.getName().equals(nonproductionName))
							.findAny().orElse(null) == null) {
				msg = "前任务：[" + preTask + "], 后任务:[" + currentTask + "], 满足【" + nonproductionMsg + "】的条件，但没有产生";

			}
		} else {
			// 本应不该产生非生产任务，却存在
			if (currentTask.getNonProductionTaskList() != null && (!currentTask.getNonProductionTaskList().isEmpty())
					&& currentTask.getNonProductionTaskList().stream().filter(x -> x.getName().equals(nonproductionName))
							.findAny().orElse(null) != null) {
				msg = "前任务：[" + preTask + "], 后任务:[" + currentTask + "], 不满足【" + nonproductionMsg + "】的条件，却产生该非生任务";
			}
		}

		return msg;
	}

}
