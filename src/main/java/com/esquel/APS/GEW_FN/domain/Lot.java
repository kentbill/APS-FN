package com.esquel.APS.GEW_FN.domain;

import java.util.Date;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.stream.Collectors;

import com.esquel.APS.GEW_FN.domain.enums.ExceptionLevel;
import com.esquel.APS.GEW_FN.domain.enums.Lot_Break_Type;
import com.esquel.APS.GEW_FN.domain.enums.TaskMachinePlanBrokenType;
import com.esquel.APS.GEW_FN.domain.enums.TaskType_Location_In_Lot;
import com.esquel.APS.Helpers.APSException;
import com.esquel.APS.Helpers.Common;

/**
 * A lot indicates one or more cards what have the same process routing and act attributes
 * @author ZhangKent
 *
 */
public class Lot extends AbstractPersistable{
	private String cardNos;
//	private Date orderDeliveryTime;
	private List<Task> tasks;
	private Task sourceTask;
	private Task sinkTask;
	private Lot_Break_Type breakType;
//	private Long arrivalTime;
	private Long fn_DeliveryTime; // 后整的交期
	
	private Long orderDeliveryTimeL; // 订单的交期
	
	private boolean isValid;
	
	private APSException exception = null;
	
	
	public String getCardNos() {
		return cardNos;
	}
	
	public void setCardNos(String cardNos) {
		this.cardNos = cardNos;
	}

	public Long getFn_DeliveryTime() {
		return fn_DeliveryTime;
	}

	public void setFn_DeliveryTime(Long fn_DeliveryTime) {
		this.fn_DeliveryTime = fn_DeliveryTime;
	}

	public APSException getException() {
		return exception;
	}

	public void setException(APSException exception) {
		this.exception = exception;
	}

	public Long getArrivalTime(){
		if(this.tasks == null || this.tasks.isEmpty()){
			return 0L;
		}
		
		List<Long> arrivaleTimes = this.tasks.stream().map(Task::getArrivalTimeL).filter(x -> x != null && x != 0L).distinct().collect(Collectors.toList());
		if(arrivaleTimes == null || arrivaleTimes.isEmpty()){
			return 0L;
		}
		
		LongSummaryStatistics stats = arrivaleTimes.stream().mapToLong((x) -> x).summaryStatistics();
		Long maxTime = stats.getMax();

		return maxTime;
	}
	
	/**
	 * 获得当前批次的首任务
	 * 
	 * @return
	 */
	public Task getSourceTask() {
		return this.sourceTask;
	}

	public void setSourceTask(Task sourceTask) {
		this.sourceTask = sourceTask;
	}

	/**
	 * 获得当前任务所在批次的尾任务
	 * @return
	 */
	public Task getSinkTask() {
		if (this.sinkTask == null) {
			Task sinkTask = tasks.stream().filter(x -> x.getTaskLocation() == TaskType_Location_In_Lot.SINK).findAny()
					.orElse(null);

			if (sinkTask == null) {
				sinkTask = tasks.stream().filter(x -> x.getNextTaskID() == 0L).findAny().orElse(null);
				if (sinkTask != null) {
					sinkTask.setTaskLocation(TaskType_Location_In_Lot.SINK);
					this.sinkTask = sinkTask;
				}
			} else {
				this.sinkTask = sinkTask;
			}
		}
		return this.sinkTask;
	}

	public void setSinkTask(Task sinkTask) {
		this.sinkTask = sinkTask;
	}

	public Lot_Break_Type getBreakType() {
		return breakType;
	}

	public void setBreakType(Lot_Break_Type breakType) {
		this.breakType = breakType;
	}

	public Long getOrderDeliveryTimeL() {
		return orderDeliveryTimeL;
	}

	public void setOrderDeliveryTimeL(Long orderDeliveryTimeL) {
		this.orderDeliveryTimeL = orderDeliveryTimeL;
	}

	public Lot(Long id, List<Task> tasks) throws APSException {
		super(id);
		if (tasks != null && (!tasks.isEmpty())) {
			this.cardNos = tasks.get(0).getCardNos();
			this.fn_DeliveryTime = tasks.get(0).getFn_DeliveryTime() == null ? 0L : Common.getTime_Minute(tasks.get(0).getFn_DeliveryTime().getTime());
			this.orderDeliveryTimeL = tasks.get(0).getDeliveryTime() == null ? 0L : Common.getTime_Minute(tasks.get(0).getDeliveryTime().getTime());
			this.tasks = tasks;
			
			for(Task task : tasks){
				task.setLot(this);
			}
			
			// 设置当前lot中的任务位置
			initTaskLocation();
			
			// 设置当前Lot中任务的连接关系
			createTaskChainInLot();
		}
	}
	
	/**
	 * Initialize the location of the tasks,
	 * SOURCE -  the first task in a lot;
	 * NORMAL - the middle task in a lot;
	 * SINK	  - the last task in a lot;
	 * ONLY_ONE - the only one task in a lot;
	 * @throws APSException 
	 */
	private void initTaskLocation() throws APSException {
	/*	
		if(this.getId() == 104884){
			System.out.println("test");
		}
*/
		if (this.tasks == null || this.tasks.isEmpty()) {
			throw new APSException("每个批次至少存在一个Task, 但批次[" + this + "]不存在Task.");
		}

		if (this.tasks.size() == 1) {
			this.tasks.get(0).setTaskLocation(TaskType_Location_In_Lot.ONLY_ONE);
			this.sourceTask = this.tasks.get(0);
			this.sinkTask = this.tasks.get(0);
			return;
		}

		// 获得NextTaskID字段列表, 0除外。
		List<Long> nextTaskIDs = this.tasks.stream().map(Task::getNextTaskID).filter(x -> x != 0L)
				.collect(Collectors.toList());
		if (nextTaskIDs == null || nextTaskIDs.isEmpty()) {
			this.setTaskInvalid(this.tasks, "批次[" + this + "]中的任务没有形成链.");
			return;
			//throw new APSException("批次[" + this + "]中的任务没有形成链.");
		}

		// 首任务：同批次中，没有任务一个任务指定它的，则定义为首任务
		List<Task> tasks = this.tasks.stream().filter(x -> !nextTaskIDs.contains(x.getId()))
				.collect(Collectors.toList());
		if (tasks == null || tasks.isEmpty()) {
			this.setTaskInvalid(this.tasks, "批次[" + this + "]不存在首任务(同批任务中，没有被其它任务指向).");
			return;
			//throw new APSException("批次[" + this + "]不存在首任务(同批任务中，没有被其它任务指向).");
		}

		if (tasks.size() > 1) {
			this.setTaskInvalid(this.tasks, "批次[" + this + "]存在多个首任务(同批任务中，没有被其它任务指向).");
			return;
			//throw new APSException("批次[" + this + "]存在多个首任务(同批任务中，没有被其它任务指向).");
		}

		Task sourceTask = tasks.get(0);
		sourceTask.setTaskLocation(TaskType_Location_In_Lot.SOURCE);
		this.sourceTask = sourceTask;

		// 末任务:同批次中，NextTaskID为空的，或为0L的，则为末任务。
		tasks = this.tasks.stream().filter(x -> x.getNextTaskID() == 0L).collect(Collectors.toList());
		if (tasks == null || tasks.isEmpty()) {
			this.setTaskInvalid(this.tasks, "批次[" + this + "]不存在尾任务(NextTaskID为0的任务作为尾任务).");
			return;
			//throw new APSException("批次[" + this + "]不存在尾任务(NextTaskID为0的任务作为尾任务).");
		}

		if (tasks.size() > 1) {
			this.setTaskInvalid(this.tasks, "批次[" + this + "]存在多个首任务(NextTaskID为0的任务作为尾任务).");
			return;
			//throw new APSException("批次[" + this + "]存在多个首任务(NextTaskID为0的任务作为尾任务).");
		}

		Task sinkTask = tasks.get(0);
		sinkTask.setTaskLocation(TaskType_Location_In_Lot.SINK);
		this.sinkTask = sinkTask;

		// 其它任务
		if (this.tasks.size() > 2) {
			tasks = this.tasks.stream().filter(x -> (!x.equals(sourceTask) && (!x.equals(sinkTask))))
					.collect(Collectors.toList());
			if (tasks != null && (!tasks.isEmpty())) {
				for (Task task : tasks) {
					task.setTaskLocation(TaskType_Location_In_Lot.NORMAL);
				}
			}
		}

	}
	
	/**
	 * set the tasks in a list to be invalid(Broke_For_Invalid_Information_No_Perfect).
	 * @param tasks
	 */
	private void setTaskInvalid(List<Task> tasks, String message){
		for(Task task : tasks){
			task.setException(new APSException(task, task.getId(), ExceptionLevel.WARNING, message));
			//task.setTaskMachinePlanBroken(TaskMachinePlanBrokenType.Broke_For_Invalid_Information_No_Perfect);
		}
	}

	/**
	 * Create a chain for the tasks in the same lot, by the field nextTaskID
	 */
	private void createTaskChainInLot() {

		// if there is less than 2 tasks, needn't a chain.
		if (this.tasks == null || this.tasks.isEmpty() || this.tasks.size() <= 1){
			return;
		}

		for (Task task : this.tasks) {

			if (task == null){
				continue;
			}
			
			// this the nextTaskID is 0, determines that it's the last task of the lot, set its nextTaskInSameLot to null.
			if (task.getTaskLocation() == TaskType_Location_In_Lot.SINK) {
				task.setNextTaskInSameLot(null);
				continue;
			}

			Long nextTaskID = task.getNextTaskID();

			// get the nextTask object by id
			Task nextTaskInLot = this.tasks.stream().filter(x -> (x.getId().equals(nextTaskID))).findAny()
					.orElse(null);

			// set the nextTask
			task.setNextTaskInSameLot(nextTaskInLot);

			// Inversely, set the previous task as current task for next task object.
			if (nextTaskInLot != null) {
				nextTaskInLot.setPrivousTaskInSameLot(task);
			}
		}
	}
	
	
	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}
	
	public boolean isValid() {
		return isValid;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Lot) {
			Lot l = (Lot) obj;
			return (l.getId().equals(this.id));
		} else {
			return super.equals(obj);
		}
	}
}
