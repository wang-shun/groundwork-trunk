--gw_host_status_1

	select hostname, lastchecktime, name as status from host 
	left JOIN hoststatus h2 ON host.hostid = h2.hoststatusid 
	LEFT JOIN monitorstatus m2 ON h2.monitorstatusid = m2.monitorstatusid
	where hostname = 'centos6-linbit-1'
	order by hostname

	--PieChart
		select concat('(', count(m2.name), ') ', m2.name) as status, count(m2.name) as statuscount from host
		LEFT JOIN servicestatus s2 on host.hostid = s2.hostid
		LEFT JOIN monitorstatus m2 ON s2.monitorstatusid = m2.monitorstatusid
		where hostname = 'centos6-linbit-1'
	  	group by m2.name

	--??????????
		select distinct HostName from Host [,HostGroup,HostGroupCollection 
		where Host.HostID = HostGroupCollection.HostID and HostGroup.HostGroupID = HostGroupCollection.HostGroupID and HostGroup.Name IN (<list>)] 
		order by HostName

--gw_host_status_2

	select servicedescription, m2.name as status, lastchecktime, nextchecktime, laststatechange from host
	LEFT JOIN servicestatus s2 on host.hostid = s2.hostid
	LEFT JOIN monitorstatus m2 ON s2.monitorstatusid = m2.monitorstatusid
	where hostname = 'localhost'
	order by hostname

	--??????????
		select Name from HostGroup [where Name IN (<list>)] order by Name


--gw_hostgroup_status_1

	select DISTINCT hostgroup.description from hostgroup
	where hostgroup.description = 'ESXi hypervisor'

	--PieChart - HostGroupService
		select hostgroup.description, concat('(', count(m2.name), ') ', m2.name) as status, count(m2.name) as statuscount from hostgroup
		left join hostgroupcollection on hostgroupcollection.hostgroupid = hostgroup.hostgroupid
		left join host on host.hostid = hostgroupcollection.hostid
		LEFT JOIN servicestatus s2 on host.hostid = s2.hostid
		LEFT JOIN monitorstatus m2 ON s2.monitorstatusid = m2.monitorstatusid
		where hostgroup.description = 'ESXi hypervisor'
		GROUP BY hostgroup.description, m2.name
		ORDER BY hostgroup.description
	
	--PieChart - HostGroupHost	
		select hostgroup.description, concat('(', count(m2.name), ') ', m2.name) as status, count(m2.name) as statuscount from hostgroup
		left join hostgroupcollection on hostgroupcollection.hostgroupid = hostgroup.hostgroupid
		left join host on host.hostid = hostgroupcollection.hostid
		LEFT JOIN servicestatus s2 on host.hostid = s2.hostid
		LEFT JOIN monitorstatus m2 ON s2.monitorstatusid = m2.monitorstatusid
		where hostgroup.description = 'ESXi hypervisor'
		GROUP BY hostgroup.description, m2.name
		ORDER BY hostgroup.description

--gw_hostgroup_status_2
	select hostgroup.name as hostgroupname, host.hostname as hostname from hostgroup 
	left join hostgroupcollection on hostgroupcollection.hostgroupid = hostgroup.hostgroupid 
	left join host on host.hostid = hostgroupcollection.hostid 
	order by hostgroupname, hostname
