package com.esquel.APS.GEW_FN.solver;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.kie.api.io.ResourceType;
//import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.AgendaFilter;

import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderError;
import org.kie.internal.builder.KnowledgeBuilderErrors;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;

import com.esquel.APS.GEW_FN.solver.interactor.NonproductionAssertionInteractor;
import com.esquel.APS.GEW_FN.solver.interactor.TaskMachinePlanAssertionInteractor;
import com.esquel.APS.GEW_FN.domain.MatchRule;
import com.esquel.APS.Helpers.NamingAgendaFilter;

/**
 * ͨ������Drools�������棬�Դ�����������ʽ���������ֵ�󣬽����жϡ� Ŀǰ���Դ���ĵ��ж���:�������̨���жϣ���������������������ж�
 * 
 * @author ZhangKent
 *
 */
public class ConditionAssertion {
	Integer i = 0;
	// ʹ�õ���ģʽ��ȷ��ֻ��һ��session
	private static ConditionAssertion instance;

	// private List<TaskMachinePlanAssertionInteractor>
	// taskMachinePlanInterators;
	// private List<NonproductionAssertionInteractor> nonproductionInterators;

	// private KieServices kServices = null;// KieServices.Factory.get();
	// private KieContainer kContainer = null;//
	// kServices.getKieClasspathContainer();
	
	private KnowledgeBuilder kBuilder = null;
	private KieSession kSession = null;
	private InternalKnowledgeBase kBase = null;
	private StringBuilder sbRulesString = null;
	private List<String> nonproductionBroke = new ArrayList<String>();
	private List<String> taskMachinePlanBroke = new ArrayList<String>();
	private List<String> ruleHeadStatements = new ArrayList<String>(Arrays.asList(
			"package com.esquel.APS.GEW_FN.configuration;\r\n\r\n", 
			"import com.esquel.APS.GEW_FN.domain.Task;\r\n",
			//"import java.lang.Integer;\r\n",
			"import com.esquel.APS.GEW_FN.domain.MachinePlan;\r\n",
			"import com.esquel.APS.GEW_FN.domain.NonproductionTask;\r\n",
			"import com.esquel.APS.GEW_FN.solver.interactor.NonproductionAssertionInteractor;\r\n",
			"import com.esquel.APS.GEW_FN.solver.interactor.TaskMachinePlanAssertionInteractor;\r\n"));

	/**
	 * ˽�л�������
	 */
	private ConditionAssertion() {
		
	}

	/**
	 * Initializes the ConditionAssertion, it's a singleton, 
	 * 
	 * @param taskMachinePlanInterators
	 * @param nonproductionInterators
	 * @return
	 */
	public static synchronized ConditionAssertion getInstance(
			List<TaskMachinePlanAssertionInteractor> taskMachinePlanInterators,
			List<NonproductionAssertionInteractor> nonproductionInterators) {

		// check if the instance haven't exist.
		if (instance == null) {
			if ((taskMachinePlanInterators == null || taskMachinePlanInterators.isEmpty())
					|| (nonproductionInterators == null || nonproductionInterators.isEmpty()))
				return null;

			instance = new ConditionAssertion();

			// create the Drools rules from interators 
			initRules(taskMachinePlanInterators, nonproductionInterators);
		}

		return instance;
	}

	/**
	 * create the drools rules for verification the Task-MachinePlans and
	 * Non-productions.
	 * 
	 * @param taskMachinePlanInterators
	 * @param nonproductionInteractions
	 */
	private static void initRules(List<TaskMachinePlanAssertionInteractor> taskMachinePlanInterators,
			List<NonproductionAssertionInteractor> nonproductionInteractions) {
		if (instance == null)
			return;

		instance.kBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		instance.createRulesHeader();

		// create the rule string
		for (TaskMachinePlanAssertionInteractor interator : taskMachinePlanInterators) {
			if (interator == null || interator.getRules() == null || interator.getRules().isEmpty())
				continue;

			instance.sbRulesString.append(instance.getRuleStatementFromRule_MachinePlan(interator.getRules()));
		}

		for (NonproductionAssertionInteractor interator : nonproductionInteractions) {
			if (interator == null || interator.getRules() == null || interator.getRules().isEmpty())
				continue;

			instance.sbRulesString.append(instance.getRuleStatementFromRule_NonporductionTask(interator.getRules()));
		}

//		System.out.println(instance.sbRulesString.toString());

		// add the Drools rules to KnowledgeBuilder.
		if (instance.sbRulesString != null) {
			try {
				instance.kBuilder.add(
						ResourceFactory.newByteArrayResource(instance.sbRulesString.toString().getBytes("utf-8")),
						ResourceType.DRL);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		// check if any error in rules
		KnowledgeBuilderErrors errors = instance.kBuilder.getErrors();
		for (KnowledgeBuilderError error : errors) {
			System.out.println(error);
		}

		// add the packages to KnowledgeBase
		instance.kBase = KnowledgeBaseFactory.newKnowledgeBase();
		instance.kBase.addPackages(instance.kBuilder.getKnowledgePackages());
	}

	/**
	 * Create the header of a drl file
	 */
	private void createRulesHeader() {
		sbRulesString = new StringBuilder();

		for (String headStatement : ruleHeadStatements) {
			sbRulesString.append(headStatement);
		}
		sbRulesString.append("\r\n\r\n");
	}

	/**
	 * Create the rule string for each non-production task object, to check if a task need to be add a non-production task
	 * @param rules
	 * @return
	 */
	private StringBuilder getRuleStatementFromRule_NonporductionTask(List<MatchRule> rules) {
		StringBuilder sb = new StringBuilder();

		for (MatchRule rule : rules) {
			if (rule == null || rule.getName() == null || rule.getName().equals("") || rule.getConditionString() == null
					|| rule.getConditionString().equals(""))
				continue;

			sb.append("rule \"" + rule.getName() + "\"\r\n");
			sb.append("when\r\n");
		
			sb.append("\t $interator : NonproductionAssertionInteractor(getPreviousTask() != null && getCurrentTask() != null && "
					+ rule.getConditionString() + ") \r\n");
			sb.append("then\r\n");
			sb.append("\t $interator.setAssertReuslt(true);\r\n");
			sb.append("end \r\n");
			sb.append("\r\n");
		}
		
		return sb;

	}

	/**
	 * Create the rule string for each machine plan object, to check if a task could be assign to particular machine plan
	 * @param rules
	 * @return
	 */
	private StringBuilder getRuleStatementFromRule_MachinePlan(List<MatchRule> rules) {
		StringBuilder sb = new StringBuilder();

		for (MatchRule rule : rules) {
			if (rule == null || rule.getName() == null || rule.getName().equals("") || rule.getConditionString() == null
					|| rule.getConditionString().equals(""))
				continue;

			sb.append("rule \"" + rule.getName() + "\"\r\n");
			sb.append("when\r\n");
			sb.append("\t $interator : TaskMachinePlanAssertionInteractor(getTask() != null && getMachinePlan() != null && " + rule.getConditionString() + ")\r\n");
			//sb.append("\t $interator : TaskMachinePlanAssertionInteractor(getTask() != null && " + rule.getConditionString() + ")\r\n");
			sb.append("then\r\n");
			sb.append("\t $interator.setAssertReuslt(true);\r\n");
			sb.append("end \r\n");
			sb.append("\r\n");
		}

		return sb;
	}

	/**
	 * Checks if the task could be assign to the machine plan, the task object and the machine plan object were wrap by a TaskMachinePlanAssertionInteractor object.
	 * @param interactor
	 * @return
	 */
	public boolean AssertTrue(TaskMachinePlanAssertionInteractor interactor) {
		if (interactor == null || interactor.getRules() == null || interactor.getRules().isEmpty()
				|| interactor.getMachinePlan() == null || interactor.getTask() == null)
			return false;

		for (MatchRule rule : interactor.getRules()) {
			if (rule == null || rule.getName() == null || rule.getName().equals(""))
				continue;

			NamingAgendaFilter namingFilter = new NamingAgendaFilter(rule.getName());
			kSession = instance.kBase.newKieSession();
			kSession.insert(interactor);
			kSession.fireAllRules(namingFilter);
			kSession.dispose();

			if (interactor.isAssertReuslt()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if the particular non-production should be insert into the front of a task. the 2 neighboring tasks were wrap by a NonproductionAssertionInteractor object
	 * @param interactor
	 * @return
	 */
	public boolean AssertTrue(NonproductionAssertionInteractor interactor) {
		if (interactor == null || interactor.getRules() == null || interactor.getRules().isEmpty()
				|| interactor.getPreviousTask() == null || interactor.getCurrentTask() == null)
			return false;

		for (MatchRule rule : interactor.getRules()) {
			if (rule == null || rule.getName() == null || rule.getName().equals(""))
				continue;

			NamingAgendaFilter namingFilter = new NamingAgendaFilter(rule.getName());
			kSession = instance.kBase.newKieSession();
			kSession.insert(interactor);
			kSession.fireAllRules(namingFilter);
			kSession.dispose();
			if (interactor.isAssertReuslt()) {	
				nonproductionBroke.add(interactor.getNonproductionTask().getName());
				return true;
			}

		}

		return false;
	}

}
