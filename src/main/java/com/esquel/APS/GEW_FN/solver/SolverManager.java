package com.esquel.APS.GEW_FN.solver;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import org.optaplanner.core.api.solver.event.SolverEventListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.score.director.ScoreDirectorFactory;
import com.esquel.APS.GEW_FN.domain.MachinePlan;
import com.esquel.APS.GEW_FN.domain.NonproductionTaskSummary;
import com.esquel.APS.GEW_FN.domain.ScoreDrl;
import com.esquel.APS.GEW_FN.domain.ScoreSummary;
import com.esquel.APS.GEW_FN.domain.Task;
import com.esquel.APS.GEW_FN.domain.TaskAssignmentSolution;
import com.esquel.APS.GEW_FN.domain.enums.EntityCategory;
import com.esquel.APS.Helpers.APSException;
import com.esquel.APS.Helpers.Common;
import com.esquel.APS.Helpers.XMLHelper;


public class SolverManager {
	private static SolverManager _instance;
	
	private ScoreDirector<TaskAssignmentSolution> scoreDirector = null;	//用于
	private TaskAssignmentSolution bestAssignmentSolution = null;
	private TaskAssignmentSolution solvedTaskAssignment = null;
	private List<Task> taskListPendingPlan = null;
	private List<MachinePlan> machinePlanListPendingPlan = null;
	private Solver<TaskAssignmentSolution> solver = null;
	private HashMap<Long, NonproductionTaskSummary> nonproductionTaskSummaries;
	private List<ScoreSummary> socreSummaries = new ArrayList<ScoreSummary>();
	private Long scoreSummarySeq = 0L;
	private Long minutesSpent;
	private TaskAssignmentSolution pendingTaskAssignment;

	
	public List<Task> getTaskListPendingPlan() {
		return taskListPendingPlan;
	}

	public void setTaskListPendingPlan(List<Task> taskListPendingPlan) {
		this.taskListPendingPlan = taskListPendingPlan;
	}

	public List<MachinePlan> getMachinePlanListPendingPlan() {
		return machinePlanListPendingPlan;
	}

	public void setMachinePlanListPendingPlan(List<MachinePlan> machinePlanListPendingPlan) {
		this.machinePlanListPendingPlan = machinePlanListPendingPlan;
	}

	public HashMap<Long, NonproductionTaskSummary> getNonproductionTaskSummaries() {
		return nonproductionTaskSummaries;
	}

	public void setNonproductionTaskSummaries(HashMap<Long, NonproductionTaskSummary> nonproductionTaskSummaries) {
		this.nonproductionTaskSummaries = nonproductionTaskSummaries;
	}

	public TaskAssignmentSolution getSolvedTaskAssignment() {
		return solvedTaskAssignment;
	}

	public TaskAssignmentSolution getPendingTaskAssignment() {
		return pendingTaskAssignment;
	}

	public ScoreDirector<TaskAssignmentSolution> getScoreDirector() {
		return scoreDirector;
	}

	public List<ScoreSummary> getSocreSummaries() {
		return socreSummaries;
	}

	public Long getMinutesSpent() {
		return minutesSpent;
	}

	private SolverManager(){}
	
	public static synchronized SolverManager getInstance(DataManager dataManager) throws APSException{
		if(_instance == null || dataManager == null){
			_instance = new SolverManager();
			_instance.taskListPendingPlan = dataManager.getTaskList();
			_instance.machinePlanListPendingPlan=dataManager.getMachinePlanList();
			_instance.nonproductionTaskSummaries = dataManager.getNonproductionTaskSummaries();
			_instance.initSolver(dataManager.getPlanningTarget());
			_instance.pendingTaskAssignment = _instance.generateProblem();
		}
		
		return _instance;
		
	}
	
	/**
	 * 初始化Solve对象
	 * 
	 * @throws APSException
	 */
	private void initSolver(String solveKey) throws APSException {
		// Configuration file
		String fileName = Common.getXMLPath(EntityCategory.SOLVER_CONFIGURATION, false);
		File fSolver = new File(fileName);
		if (fSolver.exists() == false) {
			throw new APSException("Planning配置文件[" + fileName + "]不存在!");
		}

		SolverFactory<TaskAssignmentSolution> solverFactory = SolverFactory.createFromXmlFile(fSolver);

		// 根据用户选择的维度，选择Score组合，以确定方案的偏向性（例如成本，交期，效率等）。
		// 先取得全局的计划规则global.drl
		List<String> scoreDrlList = solverFactory.getSolverConfig().getScoreDirectorFactoryConfig().getScoreDrlList();

		// 获得Plan需要运行的分钟数
		_instance.minutesSpent = solverFactory.getSolverConfig().getTerminationConfig().getMinutesSpentLimit();
		
		if(scoreDrlList == null){
			scoreDrlList = new ArrayList<String>();
		}

		// 获得用户指定的排产偏向规则
		List<String> solveKeyList = new ArrayList<String>();
		solveKeyList.add(solveKey);
		if (getScoreDrlList(solveKeyList, scoreDrlList) == false) {
			System.out.println("Failue to get the calculate score DRL file.");
			return;
		}

		// 将添加了偏向性计算规则的ScoreDrl添加到SolverFactory.
		solverFactory.getSolverConfig().getScoreDirectorFactoryConfig().setScoreDrlList(scoreDrlList);

		// 创建Solver并返回
		_instance.solver = solverFactory.buildSolver();

		ScoreDirectorFactory<TaskAssignmentSolution> scoreDirectorFactory = _instance.solver.getScoreDirectorFactory();
		_instance.scoreDirector = scoreDirectorFactory.buildScoreDirector();

		_instance.solver.addEventListener(new SolverEventListener<TaskAssignmentSolution>() {
			public void bestSolutionChanged(BestSolutionChangedEvent<TaskAssignmentSolution> event) {
				// 显示best solution信息
				_instance.showBestSolutionInfo(event);
					
				// 收集新分数
				_instance.collectScore(event);

			}
		});
	}
	
	/**
	 * create a score summary and save in the list
	 * @param event
	 */
	private void collectScore(BestSolutionChangedEvent<TaskAssignmentSolution> event){
		
		Number[] scores = event.getNewBestScore().toLevelNumbers();
		Date now = new Date();
		Long timePont = Common.getTime_Minute(now.getTime());
		timePont -= Common.get_planningStartTimeL(); // 取得当前过程距离采样过程的分钟数,用采样时的分钟数减去开始排产时的分钟数
		ScoreSummary scoreSumm = new ScoreSummary(this.scoreSummarySeq, timePont, scores[0].longValue(), scores[1].longValue(), scores[2].longValue());
		
		//Long id, Long timeStamp, Long hardScore,Long mediumScore, Long softScore
		this.scoreSummarySeq++;
		
		socreSummaries.add(scoreSumm);
	}
	
	/**
	 * Show the best solution information in console device.
	 * @param event
	 */
	private void showBestSolutionInfo(BestSolutionChangedEvent<TaskAssignmentSolution> event){
		if (event.getNewBestSolution().getScore().isSolutionInitialized()) {
			_instance.bestAssignmentSolution = event.getNewBestSolution();
		}

		if (_instance.bestAssignmentSolution != null) {

			System.out.println("A best solution appear!");

			System.out.println("isFeasible: " + event.getNewBestSolution().getScore().isFeasible()
					+ "-> Score: [ " + event.getNewBestSolution().getScore().getHardScore() + " Hard/"
					+ event.getNewBestSolution().getScore().getMediumScore() + " Medium / "
					+ event.getNewBestSolution().getScore().getSoftScore() + " Soft ]");

		} else {
			
			System.out.println("Not all the tasks was initialized.");
			System.out.println("The score is: [" +  event.getNewBestSolution().getScore().getHardScore()	+ " Hard / " + 
													event.getNewBestSolution().getScore().getMediumScore() + " Medium / " + 
													event.getNewBestSolution().getScore().getSoftScore() + " Soft ]");

			List<Task> unassignedTasks = event.getNewBestSolution().getTaskList().stream()
					.filter(x -> x.getMachinePlan() == null).collect(Collectors.toList());
			List<Task> assignedTasks = event.getNewBestSolution().getTaskList().stream()
					.filter(x -> x.getMachinePlan() != null).collect(Collectors.toList());
			System.out.println("Task initialization, Assigned / Unassigned: " + assignedTasks.size() + " / "
					+ unassignedTasks.size());
		}
	}
	
	
	
	/**
	 * 启动排产
	 * @throws APSException 
	 */
	public void startSolve() throws APSException {

		String checkingMsg = checkSolverConfiguration();
		if(checkingMsg != ""){
			throw new APSException(checkingMsg);
		}

		//TaskAssignmentSolution pendingTaskAssignment = generateProblem();
		
		this.solvedTaskAssignment = solver.solve(this.pendingTaskAssignment);
	
		//return solvedTaskAssignment;
	}
	
	
	/**
	 * 对Solver所需的参数进行检查
	 * @return
	 */
	private String checkSolverConfiguration(){
		String exceptionMsg = "";
		if (solver == null) {
			exceptionMsg = "Solver initialize failue. Pleas check the configuration.";
			System.out.println(exceptionMsg);
			return exceptionMsg;
		}
		
		if (machinePlanListPendingPlan == null || machinePlanListPendingPlan.isEmpty()) {
			exceptionMsg = "MachinePlan list initialize failue. Pleas check the configuration.";
			System.out.println(exceptionMsg);
			return exceptionMsg;
		}
		
		if (taskListPendingPlan == null || taskListPendingPlan.isEmpty()) {
			exceptionMsg = "Task list initialize failue. Pleas check the configuration.";
			System.out.println(exceptionMsg);
			return exceptionMsg;
		}
		
		if (nonproductionTaskSummaries == null || nonproductionTaskSummaries.isEmpty()) {
			exceptionMsg = "NonproductionTask mapping list initialize failue. Pleas check the configuration.";
			System.out.println(exceptionMsg);
			return exceptionMsg;
		}
		
		return exceptionMsg;
	}
	
	/**
	 * 根据Key(用户选择的)获得对应的评分DRL文件
	 * @param solveKey
	 * @param orignalScoreDrlList
	 * @return
	 */
	private boolean getScoreDrlList(List<String> solveKeys, List<String> orignalScoreDrlList) {
		
		// 加入全局约束global
		if(solveKeys.isEmpty() || (!solveKeys.contains("global"))){
			solveKeys.add("global");
		}
		
		
		List<String> scoreDrls = XMLHelper.getScoreDrlList().stream().filter(x -> solveKeys.contains(x.getKey())).map(ScoreDrl::getDrlFileName).collect(Collectors.toList());
		
		if (scoreDrls == null || scoreDrls.isEmpty()) {
			return false;
		} else {
			orignalScoreDrlList.addAll(scoreDrls);
			return true;
		}
	}

	/**
	 * 生成待排产的Solution
	 * @return
	 */
	private TaskAssignmentSolution generateProblem() {

		TaskAssignmentSolution taskAssignment = new TaskAssignmentSolution();
		taskAssignment.setMachinePlanList(_instance.machinePlanListPendingPlan);
		taskAssignment.setTaskList(_instance.taskListPendingPlan);
		taskAssignment.setNonproductionMappingList(_instance.nonproductionTaskSummaries);
		
		return taskAssignment;
	}
}
