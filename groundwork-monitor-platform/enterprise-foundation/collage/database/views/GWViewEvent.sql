-- $id:$
-- Copyright 2004-2007 GroundWork OpenSource Solutions
-- 

-- Note: Views are supported starting in MySQL 5.0. Any older version doesn't support views

use GWCollageDB;

CREATE VIEW vConsole (LogMessageID, OpStatus, TextMessage , ApplicationName, LoggerName, FirstInsertDate, LastInsertDate, MsgCount, Severity, Priority, Type , Component, ServerIP) AS SELECT l.LogMessageID, ops.Name OpStatus, CONCAT(SUBSTRING(l.TextMessage,1,50), '...') TextMessage , l.ApplicationName, l.LoggerName, l.FirstInsertDate, l.LastInsertDate, l.MsgCount, s.Name Severity, p.Name Priority, t.Name Type , c.Name Component, srv.IP ServerIP)
AS SELECT FROM LogMessage l, OperationStatus ops, Severity s, Priority p, TypeRule t, Component c, Server srv
WHERE l.SeverityID = s.SeverityID AND l.PriorityID=p.PriorityID AND l.TypeID=t.TypeID AND l.ComponentID=c.ComponentID AND l.ServerID = srv.ServerID AND ops.OperationStatusID = l.OperationStatusID
ORDER BY Priority DESC
