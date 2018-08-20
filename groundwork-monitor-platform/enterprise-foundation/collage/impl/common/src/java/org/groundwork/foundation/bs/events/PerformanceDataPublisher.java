package org.groundwork.foundation.bs.events;

import org.groundwork.foundation.bs.BusinessService;
import org.groundwork.foundation.bs.exception.BusinessServiceException;

public interface PerformanceDataPublisher extends BusinessService {
	public void publish(String data) throws BusinessServiceException;
}
