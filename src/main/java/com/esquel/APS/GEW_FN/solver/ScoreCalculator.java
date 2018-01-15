package com.esquel.APS.GEW_FN.solver;

import java.util.ArrayList;
//import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.stream.Collectors;

import org.optaplanner.core.api.domain.valuerange.CountableValueRange;
import org.optaplanner.core.api.domain.valuerange.ValueRangeFactory;

import com.esquel.APS.GEW_FN.domain.MachinePlan;
import com.esquel.APS.GEW_FN.domain.NonproductionTask;
import com.esquel.APS.GEW_FN.domain.Step;
import com.esquel.APS.GEW_FN.domain.Task;
import com.esquel.APS.GEW_FN.domain.enums.TimeComparisonResultType;
import com.esquel.APS.Helpers.Common;

/**
 * ר�������Ѽ�����ֹ�������Ĺ�����
 * @author ZhangKent
 *
 */
public class ScoreCalculator {
	
	// ��������Ƿ���ѭ��ʱ�����ڼ�¼�������
	//private static Task checkingTask = null;
	private static List<Long> checkingTaskIds = new ArrayList<Long>();
	
	/**
	 * ����һ��������Լ���ķ���
	 * 
	 * @param task
	 *            - ��Ҫ������������
	 * @return
	 * @throws ParseException 
	 */
	public static Long getDeliveryTimeSocre(Task task) {
			
		Long score = 0L;

		if (task == null || task.getMachinePlan() == null || task.getPlanStartTimeL() == null || task.getPlanStartTimeL() == 0L)
			return score;

		/*
		 * ͨ����ǰ���ڼ�ȥ�������õ���������ΪԼ�������Ļ�׼�� ���� 
		 * 1. �������ڣ������û�׼��Ϊ��������������Խ�࣬��������ֵԽ�󣬴Ӷ��ó�Խ�����ı�ʶ�� 
		 * 1.1. ��Խ������Խ��ʱ�������������������������ֵ�ᰴ������������ֹ��Ƚ�������Լ��������������˵�Լ�������������⣬
		 * 		��ˣ���������Խ���������������ӷ���Ӧ��ԽС.ͨ����������ʵ�ִ˱仯. 
		 * 2. * ������δ���ڣ������û�׼��Ϊ���������������뵱ǰʱ��Խ��ʱ�����߲���Խ��,���÷���Խ�࣬����������Ϲ���
		 * 
		 */
		Long planStartTime = task.getPlanStartTimeL();
		Long delDate = task.getDeliveryTimeL();
		
		// ������������
		Long days_Overdue = delDate - planStartTime; //Common.getTimeGap(delDate, planStartTime, Calendar.DATE);
		
		// û��ÿ��ǰһ���1��
		if(days_Overdue > 0L){
			score = days_Overdue;
			return score;
		}
		
		days_Overdue = Math.abs(days_Overdue);

		// ��������������Ln(x-1)�׶�������ֵ���ٳ���5��Ϊ����, �۷ֱȼ����1��
		score = (long) (Math.log1p(days_Overdue) * 2L);
	
		// ���Ʒ�����߿�2000
		score *= 100L;
		if(score > 2000L)
			score = 2000L;

		return score;

	}

	/**
	 * 
	 * ����һ���������һ����Ľ���ʱ�䵽��ǰ����Ľ���ʱ�䣬�Ƿ񳬹������ȴ�ʱ��
	 * ȷ���ﵽ��������ȴ�ʱ�䣬���磺ʪ�䲼�����ȴ�ʱ��Ϊ16Сʱ����һ��ʪ�䲼�����������Ҫ��16Сʱ����ɶ���Щ���Ĵ������������������⡣
	 * 
	 * @param task
	 * @return
	 */
	public static Long getMaxWaitingTimeSocre(Task task) {
	
		if (task == null || task.getMaxWaitingTime() <= 0L || task.getMachinePlan() == null
				|| task.getPreviousStep() == null || task.getArrivalTimeL() == null || task.getArrivalTimeL() == 0L)

			return 0L;

		// ����Ϲ�������ʱ��
		Long preEndTime = 0L;
		Step lastStep = task.getPreviousStep();

		if (lastStep instanceof MachinePlan) {
			preEndTime = ((MachinePlan) lastStep).getEndTimeL();
		}

		if (lastStep instanceof Task) {
			preEndTime = ((Task) lastStep).getPlanEndTimeL();
		}

		// ��ñ���������ʱ��
		Long taskEndTime = task.getPlanEndTimeL();

		// ����Ϲ�������ʱ���뱾��������ʱ��֮ǰ
		Long endStartTimeSpan =  Math.abs(preEndTime - taskEndTime); // Math.abs(Common.getTimeGap(preEndTime, taskEndTime, Calendar.MINUTE));
		Long maxWaitingTime = task.getMaxWaitingTime();

		// �Ϲ���Ľ���ʱ���뱾����Ľ���ʱ���ȱ����ȴ�ʱ����򷵻ز��죨���ӣ�,ÿ100���ӿ�1��
		if (endStartTimeSpan > maxWaitingTime) {
			return Math.abs((endStartTimeSpan - maxWaitingTime) / 100L);
		} else {
			return 0L;
		}
	}
	
	/**
	 * get the non-production score
	 * @param task
	 * @return
	 */
	public static Long getTotalNonproductionScore(Task task){
		Long totalScore = 0L;

		if ((task.getNonProductionTaskList() == null) || (task.getNonProductionTaskList().isEmpty())) {
			return totalScore;
		}

		List<Long> scores = task.getNonProductionTaskList().stream().map(NonproductionTask::getScore).collect(Collectors.toList());
		
		LongSummaryStatistics stats = scores.stream().mapToLong((x) -> x).summaryStatistics();
		totalScore = stats.getSum();
	
		return totalScore * 100;
	}

	/**
	 * ���һ�������Ƿ�Υ���ӹ�����ķ�ֵ����Լ������ӲԼ�����̶���ֵΪ500�֣���Υ����Ϊ0��
	 * 
	 * @param task
	 * @return
	 */
	/*
	public static int getTaskSequenceScore(Task task) {
		// ��������Ϊ�գ������ж�
		if (task == null)
			return 0;

		// ����ļƻ���ʼʱ���δ�л�̨�ƻ��������ж�
		if (task.getPlanStartTime() == null || task.getPlanStartTime().equals(Common.getNullDate())
				|| task.getMachinePlan() == null)
			return 0;

		// ������û��ǰ�����񣬲����ж�
		if (task.getPreviousStep() == null)
			return 0;

		// ������һ��������Ӧ��ʱ��
		// ��һ�������������Ҳ�����ǻ�̨�ƻ�������������ȡ��ƻ�����ʱ�䣬���ǻ�̨�ƻ�����ȡ�俪ʼʱ��
		Date preTime = null;
		if (task.getPreviousStep() instanceof Task) {
			preTime = ((Task) task.getPreviousStep()).getPlanEndTime();
		} else if (task.getPreviousStep() instanceof MachinePlan) {
			preTime = ((MachinePlan) task.getPreviousStep()).getStartTime();
		}

		// ��ñ�����Ŀ�ʼʱ��
		Date startTime = task.getPlanStartTime();

		if (startTime.getTime() < preTime.getTime())
			return _taskSequenceScoreValue;

		return 0;

	}
	*/
	
	/**
	 * ������Ľ���ʱ��ϻ�̨�ƻ��Ľ���ʱ���������Ӧ�ķ�����
	 * @param task
	 * @return
	 */
	public static Long getTaskEndTimeAtMachinePlanScore(Task task){
		
		Long scoreL = 0L;
		if(task.getPlanEndTimeL() == null || task.getPlanEndTimeL() == 0L || task.getMachinePlan() == null)
			return scoreL;
		
		//long gap = Common.getTimeGap(task.getMachinePlan().getEndTime(), task.getPlanEndTime(), Calendar.HOUR);
		Long gap = (task.getMachinePlan().getEndTimeL() - task.getPlanEndTimeL()) / 60L;
		// �����֣���ÿСʱ��1�֣������֣�ÿСʱ��10��
		scoreL = (gap > 0L ? gap : (gap * 10L));
		
		if(scoreL > 0L){
			scoreL = 0L;
			return scoreL;
		}
		
					
		if(scoreL <= -200L)
			scoreL = -200L;
			
		return  scoreL;
		
	}
	
	public static Long getStartEarlyScore(Task task){
		Long score = 0L;
		
		if(task == null || task.getMachinePlan() == null || task.getPreviousStep() == null || task.getPlanStartTimeL() == null || task.getPlanStartTimeL() == 0L)
			return score;
		
		if(task.getPreviousStep() instanceof Task)
			return score;
		
		MachinePlan machinePlan = (MachinePlan)(task.getPreviousStep());
		
		//int startGap = (int)Math.abs(Common.getTimeGap(machinePlan.getStartTime(), task.getPlanStartTime(), Calendar.MINUTE));
		
		Long startGapL = Math.abs(task.getPlanStartTimeL() - machinePlan.getStartTimeL());
		
		return startGapL;
	}
	
	/**
	 * ����ǰ����Ŀ�ʼʱ�����ǰ������Ľ���ʱ���磬�����̽�����0��1��1��ʾ��ǰ�������ǽ���.
	 * @param task
	 * @return
	 */
	public static Long getEarlyPreEndTimeScore(Task task){
		Long score = 0L;
	
		if(task == null || 
				task.getPlanStartTimeL() == null ||
				task.getPlanStartTimeL() == 0L || 
				task.getMachinePlan() == null ||
				task.getPrivousTaskInSameLot() == null || 
						task.getPrivousTaskInSameLot().getPlanEndTimeL() == null ||
				task.getPrivousTaskInSameLot().getPlanEndTimeL() == 0L)
			return score;

		
		Long preEndTime = task.getPrivousTaskInSameLot().getPlanEndTimeL();
		Long currentStartTime = task.getPlanStartTimeL();
		
		score = currentStartTime < preEndTime ? 1L : 0L;

		return score;

	}
	
	
	
	/**
	 * �ж�һ���������ڵ������������������̨��)���³�����ѭ��
	 * �㷨��	�Ե�ǰ�����������������ѭ��������һ���������������г��ֶ���һ�Σ�����ʾ����������ѭ����
	 * 			�����б��������NextTask��NextTaskInSameLot��Ϊ�գ���ʾû����ѭ��.
	 * @param task
	 * @return
	 */
	public static Long getTasksDeadLoopScore(Task task){
		
		if(task == null)
			return 0L;
		
		checkingTaskIds.clear();
		checkingTaskIds.add(task.getId());
		// ������Ҫ����Task;

		if(isDeadloopInLot(task.getNextTaskInSameLot()))
			return 1L;
		
		return 0L;
		
	}
	
	/**
	 * get the score indicate that if the task was finished in a particular time (the time define by taskRequestFinishTime field),
	 * if the taskRequestFinishTime field is null, the constraint disable.
	 * @param task
	 * @return
	 */
	public static Long getTaskOnTimeScore(Task task){
		// the request finish time is null, this constraint disable.
		if(task.getTaskRequestFinishTimeL() == null || task.getTaskRequestFinishTimeL() == 0L || task.getPlanStartTimeL() == null || task.getPlanStartTimeL() == 0L){
			return 0L;
		}
		
		Long minutes_Overdue = task.getTaskRequestFinishTimeL() - task.getPlanEndTimeL(); //  Common.getTimeGap(task.getTaskRequestFinishTime(), task.getPlanEndTime(), Calendar.MINUTE);
		
		return minutes_Overdue * 10L;
	}

	/**
	 * Checks if the task was planed in a machine plan, to avoid a task locate in the unusable time span.
	 * @param task
	 * @return
	 */
	public static boolean isTaskOutOfMachinePlan(Task task){
		if(task.getPlanStartTimeL() == null || task.getPlanStartTimeL() == 0L || task.getMachinePlan() == null){
			return false;
		}
		
		// 任务的开始时间比机台开始时间早，或任务的结束时间机台的结束时间晚
		return (task.getPlanStartTimeL() <  task.getMachinePlan().getStartTimeL()) || (task.getPlanEndTimeL() > task.getMachinePlan().getEndTimeL());
		
	}
	
	/**
	 * 检查当前任务的开始时间是否早于或等于其前置任务的结束时间
	 * @param task
	 * @return
	 */
	public static boolean startTimeEarlierThanPreviousEndTime(Task task){
		if(task.getPlanStartTimeL() == null || task.getPlanStartTimeL() == 0L || task.getPreviousTaskInSameLot() == null || task.getPreviousTaskInSameLot().getPlanEndTimeL() == null || task.getPreviousTaskInSameLot().getPlanEndTimeL() == 0L){
			return false;
		}
		
		return task.getPlanStartTimeL() <= task.getPreviousTaskInSameLot().getPlanEndTimeL();
		
	}
	
	/**
	 * 检查当前任务的开始时间是否早于其预计到达时间
	 * @param task
	 * @return
	 */
	public static boolean plannedStartTimeLaterThanArrivalTime(Task task){
		if(task == null || 
				task.getLot() == null || 
				task.getLot().getSourceTask() == null || 
				task.getLot().getSourceTask().getArrivalTimeL() == null || 
				task.getLot().getSourceTask().getArrivalTimeL() == 0L ||
				task.getPlanStartTimeL() == null
				){
			return false;
		}
		
		return task.getPlanStartTimeL() < task.getLot().getSourceTask().getArrivalTimeL(); 
		
	}
	
	/**
	 * 当前任务是否与在与其在同一机台上，时间重叠的任务。
	 * @param task
	 * @return
	 */
	public static boolean timeOverlap(Task task){
		if(task.getMachinePlan() == null || task.getMachinePlan().getTasks() == null || task.getMachinePlan().getTasks().isEmpty()){
			return false;
		}
		
		return CommonFunctions.timeOverlap(task);
		
	}

	private static boolean isDeadloopInLot(Task task) {
		if (task == null)
			return false;

		// ��鵱ǰtaskID�Ƿ��Ѵ����б��У������б��У���ʾǰ���Ѿ����ʹ�������ʾ������ѭ��,���򽫸�taskID���뵽���У��Ա���һ�ּ�顣
		if(checkingTaskIds.contains(task.getId())){
			return true;
		}else{
			checkingTaskIds.add(task.getId());
		}
		
		if (isDeadloopInLot(task.getNextTaskInSameLot()) == false) {
			if (task.getNextTask() != null) {
				return isDeadloopInMP(task.getNextTask());
			} else {
				return false;
			}
		} else {
			return true;
		}
	}
	
	
	private static boolean isDeadloopInMP(Task task) {
		if (task == null)
			return false;

		// ��鵱ǰtaskID�Ƿ��Ѵ����б��У������б��У���ʾǰ���Ѿ����ʹ�������ʾ������ѭ��,���򽫸�taskID���뵽���У��Ա���һ�ּ�顣
		if (checkingTaskIds.contains(task.getId())) {
			return true;
		} else {
			checkingTaskIds.add(task.getId());
		}

		if (isDeadloopInMP(task.getNextTask()) == false) {
			if (task.getNextTaskInSameLot() != null) {
				return isDeadloopInLot(task.getNextTaskInSameLot());
			} else {
				return false;
			}

		} else {
			return true;
		}
	}

	/**
	 * ��һ���������ڵ����Σ������ظ�����ʱ���ж���Щ�����ڻ�̨�ƻ����г��ֵĴ��򣬸�����������Ƿ�һ��
	 * @param task
	 * @return
	 */
	/*
	public static int getRepeatProcessScore(Task task){
		int score = 0;
		
		if(task == null ||	// ����Ϊ��
				task.getMachinePlan() == null ||	// �������ڵĻ�̨�ƻ�Ϊ�գ���δ�����̨�ƻ� 
				(task.getPrivousTaskInSameLot() == null && task.getNextTaskInSameLot() == null) ||	// ����ǰ����������������Ϊ�գ���������������ֻ����һ�� 
				(task.getPreviousStep() == null && task.getNextTaskInSameMachinePlan() == null)) // ����Ļ�̨�ƻ����У�ǰ�������������Ϊ�գ���������ڻ�̨�ƻ��У�δ������������ǰ���ϵ
			return score;
	
		// ��������Ƿ���������������Ϊ�ظ�����
		List<Task> tasksInLot = SolveMain._taskList.stream().filter(x -> x.getLot() == task.getLot()).collect(Collectors.toList());
		
		if(tasksInLot == null || tasksInLot.size() <= 1){
			return score;
		}

	}
*/


}
