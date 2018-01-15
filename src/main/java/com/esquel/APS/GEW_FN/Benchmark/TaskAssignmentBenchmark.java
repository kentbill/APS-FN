package com.esquel.APS.GEW_FN.Benchmark;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.optaplanner.benchmark.api.PlannerBenchmark;
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;
import org.optaplanner.core.api.solver.SolverFactory;

import com.esquel.APS.GEW_FN.domain.TaskAssignmentSolution;
import com.esquel.APS.GEW_FN.domain.enums.EntityCategory;
import com.esquel.APS.GEW_FN.solver.DataManager;
import com.esquel.APS.GEW_FN.solver.SolverManager;
import com.esquel.APS.Helpers.APSException;
import com.esquel.APS.Helpers.Common;

public class TaskAssignmentBenchmark {

	public static void main(String[] args) {
	
		File benchmarkFile = new File("src\\main\\resources\\Benchmark\\TaskAssignmentBenchmarkConfig.xml");
   		SolverFactory<TaskAssignmentSolution> solverFactory = SolverFactory.createFromXmlFile(benchmarkFile);
		PlannerBenchmarkFactory benchmarkFactory = PlannerBenchmarkFactory.createFromSolverFactory(solverFactory);
				
		List<TaskAssignmentSolution> solutions = new ArrayList<TaskAssignmentSolution>();
		solutions.add(getSolution("small"));
	//	solutions.add(getSolution("medium"));
	//	solutions.add(getSolution("large"));
	
		PlannerBenchmark plannerBenchmark = benchmarkFactory.buildPlannerBenchmark(solutions);
		plannerBenchmark.benchmark();

	}
	
	private static TaskAssignmentSolution getSolution(String problemKey){
		try {
			DataManager dataManager = DataManager.getInstance("delivery");
			SolverManager solverManager = SolverManager.getInstance(dataManager);
			TaskAssignmentSolution taskAssignment = solverManager.getPendingTaskAssignment();
			return taskAssignment;
		} catch (APSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	
	

}
