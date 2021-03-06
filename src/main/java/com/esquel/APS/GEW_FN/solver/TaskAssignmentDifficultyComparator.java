package com.esquel.APS.GEW_FN.solver;

import java.util.Comparator;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionSorterWeightFactory;

import com.esquel.APS.GEW_FN.domain.Task;

public class TaskAssignmentDifficultyComparator implements Comparator<Task>{

	@Override
	public int compare(Task t1, Task t2) {
		 return new CompareToBuilder()
	                .append(t1.getDuration(), t2.getDuration())
	                .append(t1.getDeliveryTimeL(), t2.getDeliveryTimeL())
	                .append(t1.getId(), t2.getId())
	                .toComparison();
	}
}
