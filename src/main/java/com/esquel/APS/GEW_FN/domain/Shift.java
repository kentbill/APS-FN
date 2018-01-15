package com.esquel.APS.GEW_FN.domain;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.esquel.APS.Helpers.Common;

public class Shift extends AbstractPersistable{

	private Long startTime;// 班次的开始时间，若当前班次横跨了两个机台计划，则对于后一个机台计划来说，这个班次是这个机台计划的首个班次，而机台计划的开始时间，作为该班次的开始时间
	private Long endTime; // 班次的结束时间，若当前班次横跨了两个机台计划，则对于前一个机台计划来说，这个班次是这个机台计划的最后一个班次，而机台计划的结束时间，作为该班次的结束时间
	private String name; // 班次名称，格式为: 2018-01-05 早班
	
	public Long getStartTime() {
		return startTime;
	}
	public Long getEndTime() {
		return endTime;
	}
	public String getName() {
		return name;
	}
	public Shift(Long id, Long startTime, Long endTime) {
		super(id);
		this.startTime = startTime;
		this.endTime = endTime;
		initName();
	}
	
	/**
	 * 根据时间初始化班次名
	 */
	private void initName(){
		if(this.startTime == null || this.startTime == 0L){
			return;
		}
		
		// 根据strat time 来判断班次属于哪个日期时间
		Date startTimeDate = Common.getTimeByMinuteLong(this.startTime);
		Calendar startTimeCalendar = new GregorianCalendar();
		startTimeCalendar.setTime(startTimeDate);
		
		int hour = startTimeCalendar.get(Calendar.HOUR_OF_DAY);
		// 每天7点前开始的班次，应该为前一天的晚班,
		// 因此，需要检查这个开始时间是否在00:00和07:00之间，若是，则日期退要退回1天
		String shiftType = "";
		if(hour < 7){
			startTimeCalendar.add(startTimeCalendar.DATE, -1);
			shiftType = " 晚班";
		}else if(hour >= 7 && hour < 15){
			shiftType = " 早班";
		}else if(hour >= 15 && hour < 23){
			shiftType = " 中班";
		}else{
			shiftType = " 晚班";
		}
		
		int year = startTimeCalendar.get(Calendar.YEAR);
		int month = startTimeCalendar.get(Calendar.MONTH);
		month++; //获取的月份是以0开始的
		int date = startTimeCalendar.get(Calendar.DAY_OF_MONTH);
		this.name = year + "-" + month + "-" + date + shiftType;
	
	}
	
	
	 @Override
	    public String toString() {
	        return getClass().getName().replaceAll(".*\\.", "")  +  id + "[" + name + "]: StartTime:" +  Common.getTimeByMinuteLong(this.startTime)  + ", entTime:" + Common.getTimeByMinuteLong(this.endTime);
	    }
	
}
