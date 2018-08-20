INSERT INTO Action (ActionTypeID,Name,Description) VALUES((SELECT ActionTypeID FROM ActionType WHERE Name = "SCRIPT_ACTION"),"Create a HelpDesk Ticket","Create a HelpDesk Ticket for the selected items.");


INSERT INTO ActionProperty (ActionID, Name, Value)
VALUES( (SELECT ActionID FROM Action WHERE Name = "Create a HelpDesk Ticket"), "Script", "/usr/local/groundwork/helpdesk/bin/oneway_helpdesk.pl");


INSERT INTO ApplicationAction (ApplicationTypeID,ActionID)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name = "NAGIOS"), (SELECT ActionID FROM Action WHERE Name = "Create a HelpDesk Ticket"));
INSERT INTO ApplicationAction (ApplicationTypeID,ActionID)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name = "SNMPTRAP"), (SELECT ActionID FROM Action WHERE Name = "Create a HelpDesk Ticket"));
INSERT INTO ApplicationAction (ApplicationTypeID,ActionID)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name = "SYSLOG"), (SELECT ActionID FROM Action WHERE Name = "Create a HelpDesk Ticket"));
INSERT INTO ApplicationAction (ApplicationTypeID,ActionID)
VALUES ((SELECT ApplicationTypeID FROM ApplicationType WHERE Name = "SYSTEM"), (SELECT ActionID FROM Action WHERE Name = "Create a HelpDesk Ticket"));


-- Script for Action parameters
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = "Create a HelpDesk Ticket") ,"UserName","UserName");
-- Script for Action parameters
INSERT INTO ActionParameter (ActionID, Name, Value) VALUES ((SELECT ActionID FROM Action WHERE Name = "Create a HelpDesk Ticket") ,"LogMessageIds","LogMessageIds");

