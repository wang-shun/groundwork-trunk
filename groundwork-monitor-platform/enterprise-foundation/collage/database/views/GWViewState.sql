-- $id:$
-- Copyright 2004-2007 GroundWork OpenSource Solutions
-- 

-- Note: Views are supported starting in MySQL 5.0. Any older version doesn't support views

use GWCollageDB;

CREATE VIEW vServiceStatus AS SELECT * FROM Monitor m, MonitorType mt  WHERE m.MonitorTypeID = mt.MonitorTypeID AND mt.Name='SERVICE'

CREATE VIEW vHostStatus  AS SELECT * FROM Monitor m, MonitorType mt  WHERE m.MonitorTypeID = mt.MonitorTypeID AND mt.Name='HOST'

CREATE VIEW vMonitorServer AS SELECT * FROM MonitorServer

CREATE VIEW vHostGroups AS SELECT FROM HostGroup


