package com.groundwork.collage.model;

import java.util.Date;

public interface StateTransition {

	public String getHostName();

	public MonitorStatus getFromStatus();

	public Date getFromTransitionDate();

	public MonitorStatus getToStatus();

	public Date getToTransitionDate();

	public Long getDurationInState();

	public Date getEndTransitionDate();

	public void setEndTransitionDate(Date endDate);

	

}
