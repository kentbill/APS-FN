package com.esquel.APS.Helpers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import com.esquel.APS.GEW_FN.domain.NonproductionTask;
import com.esquel.APS.GEW_FN.domain.Task;
@Deprecated
public final class DBHelper {
	@Deprecated
	public static void SaveTask(List<Task> tasks) {
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		TimeZone tz = TimeZone.getTimeZone("GTM+8");
		sdf.setTimeZone(tz);

		String sqlCommandString = "";

		try {
			try {
				Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String url = "jdbc:sqlserver://GEW-MIS01uat:1434;databasename=FNMDB";
			Connection conn = DriverManager.getConnection(url, "test", "ittest");

			PreparedStatement psStatement = null;//

			// ��������������
			sqlCommandString = "delete from APSPlanningResult1";
			psStatement = conn.prepareStatement(sqlCommandString);
			psStatement.executeUpdate();

			sqlCommandString = "delete from APSNonProductionTaskID";
			psStatement = conn.prepareStatement(sqlCommandString);
			psStatement.executeUpdate();

			String planStartTime = "null";
			String planEndTime = "null";

			// tasks.stream().filter(x -> x.getMachinePlan() == null).to

			for (Task task : tasks) {
				if (task == null)
					continue;
				/*
				 * if (task == null || task.getMachinePlan() == null ||
				 * task.getPlanStartTime() == null || task.getPlanEndTime() ==
				 * null) { continue; }
				 */

				planStartTime = task.getPlanStartTime() == null ? "null"
						: "'" + sdf.format(task.getPlanStartTime()) + "'";
				planEndTime = task.getPlanEndTime() == null ? "null" : "'" + sdf.format(task.getPlanEndTime()) + "'";

				String machinePlanID = task.getMachinePlan() == null ? "null"
						: String.valueOf(task.getMachinePlan().getId());

				sqlCommandString = "insert into APSPlanningResult1(IDEN, TaskID, MachinePlanID, PlannedStartTime, PlannedEndTime, CreateTime) values("
						+ "newid()," + task.getId() + "," + machinePlanID + "," + planStartTime + "," + planEndTime
						+ ", getdate())";

				psStatement = conn.prepareStatement(sqlCommandString);
				psStatement.executeUpdate();

				if (task.getNonProductionTaskList() != null && !task.getNonProductionTaskList().isEmpty()) {
					for (NonproductionTask npTask : task.getNonProductionTaskList()) {
						planStartTime = npTask.getPlanStartTime() == null ? "null"
								: "'" + sdf.format(npTask.getPlanStartTime()) + "'";
						planEndTime = npTask.getPlanEndTime() == null ? "null"
								: "'" + sdf.format(npTask.getPlanEndTime()) + "'";

						sqlCommandString = "insert into APSNonProductionTaskID(IDEN, TaskID, NonProductionTaskID, PlannedStartTime, PlannedEndTime) values("
								+ "newid()," + task.getId() + "," + npTask.getId() + "," + planStartTime + ","
								+ planEndTime + ")";
						psStatement = conn.prepareStatement(sqlCommandString);
						psStatement.executeUpdate();
					}
				}
			}

			if (psStatement != null) {
				close(psStatement);
			}

			if (conn != null) {
				close(conn);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			// System.out.println("\n" + sqlCommandString);
		}
	}
	@Deprecated
	public static void close(Connection conn) {
		try {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	@Deprecated
	public static void close(PreparedStatement stmt) {
		try {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	@Deprecated
	public static void close(ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
				rs = null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
