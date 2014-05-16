package org.cnc.mombot.resource;

import java.util.Date;

public class TaskTime {
	public Date start, end;
	public String desc;

	public TaskTime(Date start, Date end, String desc) {
		this.start = start;
		this.end = end;
		this.desc = desc;
	}
}
