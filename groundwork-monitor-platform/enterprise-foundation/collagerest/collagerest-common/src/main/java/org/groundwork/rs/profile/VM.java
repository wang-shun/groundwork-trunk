package org.groundwork.rs.profile;

import java.io.Serializable;

@Deprecated
public class VM  implements Serializable{
	private Metric[]  metric = null;

	public Metric[] getMetric() {
		return metric;
	}

	public void setMetric(Metric[] metric) {
		this.metric = metric;
	}
}
