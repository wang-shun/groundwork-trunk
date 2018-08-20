--gw_host_availability_1
	SELECT *
	FROM (SELECT h.host_name, h.datestamp, '% total time up' AS state_name, h1.percent_total_time_up AS time FROM host_availability h
	        INNER JOIN (SELECT host_name, percent_total_time_up, datestamp FROM host_availability) h1
	          ON h1.host_name = h.host_name AND h1.datestamp = h.datestamp
	      WHERE h.host_name = 'localhost' AND h.datestamp >= '2010-01-01' AND h.datestamp <= '2019-01-01'
	UNION
	      SELECT h.host_name, h.datestamp, '% time down unscheduled' AS state_name, h1.percent_time_down_unscheduled AS time FROM host_availability h
	        INNER JOIN (SELECT host_name, percent_time_down_unscheduled, datestamp FROM host_availability) h1
	          ON h1.host_name = h.host_name AND h1.datestamp = h.datestamp
	      WHERE h.host_name = 'localhost' AND h.datestamp >= '2010-01-01' AND h.datestamp <= '2019-01-01'
	UNION
	      SELECT h.host_name, h.datestamp, '% time down scheduled' AS state_name, h1.percent_time_down_scheduled AS time FROM host_availability h
	        INNER JOIN (SELECT host_name, percent_time_down_scheduled, datestamp FROM host_availability) h1
	          ON h1.host_name = h.host_name AND h1.datestamp = h.datestamp
	      WHERE h.host_name = 'localhost' AND h.datestamp >= '2010-01-01' AND h.datestamp <= '2019-01-01'
	UNION
	      SELECT h.host_name, h.datestamp, '% time other', (100.0 - (h1.percent_total_time_up + h1.percent_time_down_unscheduled + h1.percent_time_down_scheduled)) AS time FROM host_availability h
	        INNER JOIN (SELECT host_name, percent_total_time_up, percent_time_down_unscheduled, percent_time_down_scheduled, datestamp FROM host_availability) h1
	          ON h1.host_name = h.host_name AND h1.datestamp = h.datestamp
	      WHERE h.host_name = 'localhost' AND h.datestamp >= '2010-01-01' AND h.datestamp <= '2019-01-01'
	     ) AS ha
	ORDER BY ha.datestamp
	
	--??????????
		select *, (100.0 - (PERCENT_TOTAL_TIME_OK +
				  PERCENT_KNOWN_TIME_CRITICAL_SCHEDULED +
				  PERCENT_KNOWN_TIME_CRITICAL_UNSCHEDULED +
				  PERCENT_KNOWN_TIME_WARNING_SCHEDULED +
				  PERCENT_KNOWN_TIME_WARNING_UNSCHEDULED)) AS PERCENT_OTHER
		FROM service_availability
		where service_availability.HOST_NAME = 'localhost'
		and service_availability.DATESTAMP >= '2017-11-25'
		and service_availability.DATESTAMP <= '2017-12-17'
		ORDER BY service_availability.DATESTAMP, service_availability.SERVICE_NAME
		
	--??????????
		select *, (100.0 -(PERCENT_TOTAL_TIME_UP +
			   PERCENT_TIME_DOWN_SCHEDULED +
			   PERCENT_TIME_DOWN_UNSCHEDULED)) AS PERCENT_OTHER
		from host_availability ha
		WHERE ha.HOST_NAME='localhost' AND ha.DATESTAMP>='2017-11-25' AND ha.DATESTAMP<='2017-12-17'
		ORDER BY ha.HOST_NAME
		
	--??????????
		select * from (
		
		select h.host_name, h.service_name, h.datestamp, '% total time ok' as state_name, h1.percent_total_time_ok as time
		from service_availability h
		inner join ( select host_name, service_name, percent_total_time_ok, datestamp from service_availability) h1
			on h1.host_name = h.host_name and h1.service_name = h.service_name and h1.datestamp = h.datestamp
		where h.host_name='localhost' and h.datestamp >='2017-11-25' and h.datestamp <='2017-12-17'
		
		union
		
		select h.host_name, h.service_name, h.datestamp, '% known time critical scheduled' as state_name, h1.percent_known_time_critical_scheduled as time
		from service_availability h
		inner join ( select host_name, service_name, percent_known_time_critical_scheduled, datestamp from service_availability) h1
			on h1.host_name = h.host_name and h1.service_name = h.service_name and h1.datestamp = h.datestamp
		where h.host_name='localhost' and h.datestamp >='2017-11-25' and h.datestamp <='2017-12-17'
		
		union
		
		select h.host_name, h.service_name, h.datestamp, '% known time critical unscheduled' as state_name, h1.percent_known_time_critical_unscheduled as time
		from service_availability h
		inner join ( select host_name, service_name, percent_known_time_critical_unscheduled, datestamp from service_availability) h1
			on h1.host_name = h.host_name and h1.service_name = h.service_name and h1.datestamp = h.datestamp
		where h.host_name='localhost' and h.datestamp >='2017-11-25' and h.datestamp <='2017-12-17'
		
		union
		
		select h.host_name, h.service_name, h.datestamp, '% known time warning scheduled' as state_name, h1.percent_known_time_warning_scheduled as time
		from service_availability h
		inner join ( select host_name, service_name, percent_known_time_warning_scheduled, datestamp from service_availability) h1
			on h1.host_name = h.host_name and h1.service_name = h.service_name and h1.datestamp = h.datestamp
		where h.host_name='localhost' and h.datestamp >='2017-11-25' and h.datestamp <='2017-12-17'
		
		union
		
		select h.host_name, h.service_name, h.datestamp, '% known time warning unscheduled' as state_name, h1.percent_known_time_warning_unscheduled as time
		from service_availability h
		inner join ( select host_name, service_name, percent_known_time_warning_unscheduled, datestamp from service_availability) h1
			on h1.host_name = h.host_name and h1.service_name = h.service_name and h1.datestamp = h.datestamp
		where h.host_name='localhost' and h.datestamp >='2017-11-25' and h.datestamp <='2017-12-17'
		
		union
		
		select h.host_name, h.service_name, h.datestamp, '% time other' as state_name,
		(100.0 - (h1.percent_total_time_ok + h1.percent_known_time_critical_scheduled + h1.percent_known_time_critical_unscheduled + h1.percent_known_time_warning_scheduled + h1.percent_known_time_warning_unscheduled))  as time
		from service_availability h
		inner join ( select host_name, service_name,
					percent_total_time_ok,
					percent_known_time_critical_scheduled,
					percent_known_time_critical_unscheduled,
					percent_known_time_warning_scheduled,
					percent_known_time_warning_unscheduled,
					datestamp from service_availability) h1
			on h1.host_name = h.host_name and h1.service_name = h.service_name and h1.datestamp = h.datestamp
		where h.host_name='localhost' and h.datestamp >='2017-11-25' and h.datestamp <='2017-12-17'
		
			) as ha order by ha.datestamp, ha.service_name

	--??????????		
		select distinct hostname from host [,hostgroup,hostgroupcollection 
		where host.hostid = hostgroupcollection.hostid and hostgroup.hostgroupid = hostgroupcollection.hostgroupid and hostgroup.name IN (<list>)] 
		order by hostname
		
--gw_hostgroup_availability_1

	--??????????	
		select hgh.DATESTAMP, hgh.HOSTGROUP_NAME, hgh.PERCENT_TOTAL_TIME_UP, hgh.PERCENT_TIME_DOWN_SCHEDULED, hgh.PERCENT_TIME_DOWN_UNSCHEDULED,
		(100.0 - (PERCENT_TOTAL_TIME_UP + PERCENT_TIME_DOWN_SCHEDULED + PERCENT_TIME_DOWN_UNSCHEDULED)) as PERCENT_OTHER from hostgroup_host_availability hgh
		WHERE hgh.HOSTGROUP_NAME=replace('Linux+Servers',' ', '+') AND hgh.DATESTAMP >= '2017-11-25' AND hgh.DATESTAMP <= '2017-12-17'
		ORDER BY hgh.DATESTAMP
		
	--??????????		
		select hgs.DATESTAMP, hgs.HOSTGROUP_NAME,
		case when (hgs.PERCENT_TOTAL_TIME_OK <=0) then 0 else (trunc(cast(hgs.PERCENT_TOTAL_TIME_OK as numeric),2)) end as PERCENT_TOTAL_TIME_OK,
		case when (hgs.PERCENT_KNOWN_TIME_CRITICAL_SCHEDULED <=0) then 0 else (trunc(cast(hgs.PERCENT_KNOWN_TIME_CRITICAL_SCHEDULED as numeric),2)) end AS PERCENT_KNOWN_TIME_CRITICAL_SCHEDULED,
		case when (hgs.PERCENT_KNOWN_TIME_CRITICAL_UNSCHEDULED <=0) then 0 else (trunc(cast(hgs.PERCENT_KNOWN_TIME_CRITICAL_UNSCHEDULED as numeric),2)) end AS PERCENT_KNOWN_TIME_CRITICAL_UNSCHEDULED,
		case when (hgs.PERCENT_KNOWN_TIME_WARNING_SCHEDULED <=0)then 0 else (trunc(cast(hgs.PERCENT_KNOWN_TIME_WARNING_SCHEDULED as numeric),2)) end AS PERCENT_KNOWN_TIME_WARNING_SCHEDULED,
		case when (hgs.PERCENT_KNOWN_TIME_WARNING_UNSCHEDULED  <=0)then 0 else  trunc(cast(hgs.PERCENT_KNOWN_TIME_WARNING_UNSCHEDULED as numeric),2) end AS PERCENT_KNOWN_TIME_WARNING_UNSCHEDULED,
		case when ((100.0 - (PERCENT_TOTAL_TIME_OK + PERCENT_KNOWN_TIME_CRITICAL_SCHEDULED
				  + PERCENT_KNOWN_TIME_CRITICAL_UNSCHEDULED + PERCENT_KNOWN_TIME_WARNING_SCHEDULED
				  + PERCENT_KNOWN_TIME_WARNING_UNSCHEDULED)) <=0 )then 0 else trunc(cast(100.0 - (PERCENT_TOTAL_TIME_OK + PERCENT_KNOWN_TIME_CRITICAL_SCHEDULED
				  + PERCENT_KNOWN_TIME_CRITICAL_UNSCHEDULED + PERCENT_KNOWN_TIME_WARNING_SCHEDULED
				  + PERCENT_KNOWN_TIME_WARNING_UNSCHEDULED) as numeric),2) end AS PERCENT_OTHER
		from hostgroup_service_availability hgs
		WHERE hgs.HOSTGROUP_NAME=replace('Linux+Servers',' ', '+') AND hgs.DATESTAMP >= '2017-11-25' AND hgs.DATESTAMP <= '2017-12-17'
		ORDER BY hgs.DATESTAMP
		
	--??????????
		SELECT *
		FROM (
			SELECT h.HOSTGROUP_NAME, h.DATESTAMP, '% TOTAL TIME UP' as STATE_NAME, h1.PERCENT_TOTAL_TIME_UP AS Time
			FROM hostgroup_host_availability h
			INNER JOIN (
					SELECT HOSTGROUP_NAME, PERCENT_TOTAL_TIME_UP, DATESTAMP
					FROM hostgroup_host_availability
					WHERE HOSTGROUP_NAME=replace('Linux+Servers',' ', '+') AND DATESTAMP >= '2017-11-25' AND DATESTAMP <= '2017-12-17'
					) h1
				ON h1.HOSTGROUP_NAME = h.HOSTGROUP_NAME AND h1.DATESTAMP = h.DATESTAMP
		
			UNION
		
			SELECT h.HOSTGROUP_NAME, h.DATESTAMP, '% TIME DOWN - UNSCHEDULED' as STATE_NAME, h1.PERCENT_TIME_DOWN_UNSCHEDULED AS time
			FROM hostgroup_host_availability h
			INNER JOIN (
				SELECT HOSTGROUP_NAME, PERCENT_TIME_DOWN_UNSCHEDULED, DATESTAMP
				FROM hostgroup_host_availability
				WHERE HOSTGROUP_NAME=replace('Linux+Servers',' ', '+') AND DATESTAMP >= '2017-11-25' AND DATESTAMP <= '2017-12-17'
				) h1
			ON h1.HOSTGROUP_NAME = h.HOSTGROUP_NAME AND h1.DATESTAMP = h.DATESTAMP
		
			UNION
		
			SELECT h.HOSTGROUP_NAME, h.DATESTAMP, '% TIME DOWN - SCHEDULED' as STATE_NAME, h1.PERCENT_TIME_DOWN_SCHEDULED AS Time
			FROM hostgroup_host_availability h
			INNER JOIN (
				SELECT HOSTGROUP_NAME, PERCENT_TIME_DOWN_SCHEDULED, DATESTAMP
				FROM hostgroup_host_availability
				WHERE HOSTGROUP_NAME=replace('Linux+Servers',' ', '+') AND DATESTAMP >= '2017-11-25' AND DATESTAMP <= '2017-12-17'
				) h1
			ON h1.HOSTGROUP_NAME = h.HOSTGROUP_NAME AND h1.DATESTAMP = h.DATESTAMP
		
			UNION
		
			SELECT h.HOSTGROUP_NAME, h.DATESTAMP,
				   '% TIME OTHER',
			   	   (100.0 - (h1.PERCENT_TOTAL_TIME_UP + h1.PERCENT_TIME_DOWN_UNSCHEDULED + h1.PERCENT_TIME_DOWN_SCHEDULED))  AS Time
		
			FROM hostgroup_host_availability h
			INNER JOIN (
				SELECT HOSTGROUP_NAME,
					PERCENT_TOTAL_TIME_UP,
					PERCENT_TIME_DOWN_UNSCHEDULED,
					PERCENT_TIME_DOWN_SCHEDULED,
					DATESTAMP
				FROM hostgroup_host_availability
				WHERE HOSTGROUP_NAME=replace('Linux+Servers',' ', '+') AND DATESTAMP >= '2017-11-25' AND DATESTAMP <= '2017-12-17'
				) h1
			ON h1.HOSTGROUP_NAME = h.HOSTGROUP_NAME AND h1.DATESTAMP = h.DATESTAMP
		
		)as ha ORDER BY ha.DATESTAMP
		
	--??????????
		SELECT * FROM (SELECT h.HOSTGROUP_NAME, h.DATESTAMP, '% TOTAL TIME OK' as STATE_NAME, h1.PERCENT_TOTAL_TIME_OK AS Time
		FROM hostgroup_service_availability h
		INNER JOIN (SELECT HOSTGROUP_NAME, PERCENT_TOTAL_TIME_OK, DATESTAMP
					FROM hostgroup_service_availability
				    WHERE HOSTGROUP_NAME=replace('Linux+Servers',' ', '+') AND DATESTAMP >= '2017-11-25' AND DATESTAMP <= '2017-12-17') h1
			ON h1.HOSTGROUP_NAME= h.HOSTGROUP_NAME AND h1.DATESTAMP = h.DATESTAMP
		
		UNION
		SELECT h.HOSTGROUP_NAME, h.DATESTAMP, '% KNOWN TIME CRITICAL - SCHEDULED' as STATE_NAME, h1.PERCENT_KNOWN_TIME_CRITICAL_SCHEDULED AS time
		FROM hostgroup_service_availability h
		INNER JOIN (SELECT HOSTGROUP_NAME, PERCENT_KNOWN_TIME_CRITICAL_SCHEDULED, DATESTAMP
					FROM hostgroup_service_availability
					WHERE HOSTGROUP_NAME=replace('Linux+Servers',' ', '+') AND DATESTAMP >= '2017-11-25' AND DATESTAMP <= '2017-12-17') h1
			ON h1.HOSTGROUP_NAME = h.HOSTGROUP_NAME AND h1.DATESTAMP = h.DATESTAMP
		
		UNION
		SELECT h.HOSTGROUP_NAME, h.DATESTAMP, '% KNOWN TIME CRITICAL - UNSCHEDULED' as STATE_NAME, h1.PERCENT_KNOWN_TIME_CRITICAL_UNSCHEDULED AS Time
		FROM hostgroup_service_availability h
		INNER JOIN (SELECT HOSTGROUP_NAME, PERCENT_KNOWN_TIME_CRITICAL_UNSCHEDULED, DATESTAMP
					FROM hostgroup_service_availability
					WHERE HOSTGROUP_NAME=replace('Linux+Servers',' ', '+') AND DATESTAMP >= '2017-11-25' AND DATESTAMP <= '2017-12-17') h1
			ON h1.HOSTGROUP_NAME = h.HOSTGROUP_NAME AND h1.DATESTAMP = h.DATESTAMP
		
		UNION
		SELECT h.HOSTGROUP_NAME, h.DATESTAMP, '% KNOWN TIME WARNING - SCHEDULED' as STATE_NAME, h1.PERCENT_KNOWN_TIME_WARNING_SCHEDULED AS Time
		FROM hostgroup_service_availability h
		INNER JOIN (SELECT HOSTGROUP_NAME, PERCENT_KNOWN_TIME_WARNING_SCHEDULED, DATESTAMP
					FROM hostgroup_service_availability
					WHERE HOSTGROUP_NAME=replace('Linux+Servers',' ', '+') AND DATESTAMP >= '2017-11-25' AND DATESTAMP <= '2017-12-17') h1
			ON h1.HOSTGROUP_NAME = h.HOSTGROUP_NAME AND h1.DATESTAMP = h.DATESTAMP
		
		UNION
		SELECT h.HOSTGROUP_NAME, h.DATESTAMP, '% KNOWN TIME WARNING - UNSCHEDULED' as STATE_NAME, h1.PERCENT_KNOWN_TIME_WARNING_UNSCHEDULED AS Time
		FROM hostgroup_service_availability h
		INNER JOIN (SELECT HOSTGROUP_NAME, PERCENT_KNOWN_TIME_WARNING_UNSCHEDULED, DATESTAMP
					FROM hostgroup_service_availability
					WHERE HOSTGROUP_NAME=replace('Linux+Servers',' ', '+') AND DATESTAMP >= '2017-11-25' AND DATESTAMP <= '2017-12-17') h1
			ON h1.HOSTGROUP_NAME = h.HOSTGROUP_NAME AND h1.DATESTAMP = h.DATESTAMP
		
		UNION
		SELECT h.HOSTGROUP_NAME, h.DATESTAMP, '% TIME OTHER' as STATE_NAME,
		(100.0 - (h1.PERCENT_TOTAL_TIME_OK + h1.PERCENT_KNOWN_TIME_CRITICAL_SCHEDULED + h1.PERCENT_KNOWN_TIME_CRITICAL_UNSCHEDULED + h1.PERCENT_KNOWN_TIME_WARNING_SCHEDULED + h1.PERCENT_KNOWN_TIME_WARNING_UNSCHEDULED))  AS Time
		FROM hostgroup_service_availability h
		INNER JOIN (SELECT HOSTGROUP_NAME,
					PERCENT_TOTAL_TIME_OK,
					PERCENT_KNOWN_TIME_CRITICAL_SCHEDULED,
					PERCENT_KNOWN_TIME_CRITICAL_UNSCHEDULED,
					PERCENT_KNOWN_TIME_WARNING_SCHEDULED,
					PERCENT_KNOWN_TIME_WARNING_UNSCHEDULED, DATESTAMP
					FROM hostgroup_service_availability
					WHERE HOSTGROUP_NAME=replace('Linux+Servers',' ', '+') AND DATESTAMP >= '2017-11-25' AND DATESTAMP <= '2017-12-17') h1
			ON h1.HOSTGROUP_NAME = h.HOSTGROUP_NAME AND h1.DATESTAMP = h.DATESTAMP
		
		) as ha ORDER BY ha.DATESTAMP

	--??????????	
		select Name from HostGroup [where Name IN (<list>)] order by Name
	
--gw_host_state_transitions_1
	
	select distinct hostname from host [,hostgroup,hostgroupcollection
	where host.hostid = hostgroupcollection.hostid and hostgroup.hostgroupid = hostgroupcollection.hostgroupid and hostgroup.name IN (<list>)]
	order by hostname
	
--gw_service_state1

	select distinct HostName from Host [,HostGroup,HostGroupCollection
	where Host.HostID = HostGroupCollection.HostID and HostGroup.HostGroupID = HostGroupCollection.HostGroupID and HostGroup.Name IN (<list>)]
	order by HostName
	
	select ServiceStatus.ServiceDescription from ServiceStatus, Host
	where Host.HostID=ServiceStatus.HostID and Host.HostName='localhost';
	
	select ServiceStatus.ServiceDescription, MonitorStatus.Name from MonitorStatus, ServiceStatus, Host
	where ServiceStatus.MonitorStatusID=MonitorStatus.MonitorStatusID and ServiceStatus.ServiceDescription = 'snapshots.count'
	and ServiceStatus.HostID=Host.HostID
	and Host.HostName='localhost'
		