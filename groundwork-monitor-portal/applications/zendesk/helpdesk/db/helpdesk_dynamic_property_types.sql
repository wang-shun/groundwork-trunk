--
-- Add Dynamic Property Types to Foundation
--
INSERT INTO PropertyType VALUES (NULL, 'TicketNo', 'HelpDesk Ticket Number',          0, 0, 1, 0, 0, 0, 1);
INSERT INTO PropertyType VALUES (NULL, 'Operator', 'ID Of Operator Who Filed Ticket', 0, 0, 1, 0, 0, 0, 1);

--
-- Associate all four Application Types with new Dynamic Property Types
--
INSERT INTO ApplicationEntityProperty VALUES 
	(NULL,  
	 (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'SYSTEM'),
	 (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE'),
	 (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'TicketNo'),
	 999);

INSERT INTO ApplicationEntityProperty VALUES 
	(NULL,  
	 (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'NAGIOS'),
	 (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE'),
	 (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'TicketNo'),
	 999);

INSERT INTO ApplicationEntityProperty VALUES 
	(NULL,  
	 (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'SNMPTRAP'),
	 (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE'),
	 (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'TicketNo'),
	 999);

INSERT INTO ApplicationEntityProperty VALUES 
	(NULL,  
	 (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'SYSLOG'),
	 (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE'),
	 (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'TicketNo'),
	 999);

INSERT INTO ApplicationEntityProperty VALUES 
	(NULL,  
	 (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'SYSTEM'),
	 (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE'),
	 (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'Operator'),
	 999);

INSERT INTO ApplicationEntityProperty VALUES 
	(NULL,  
	 (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'NAGIOS'),
	 (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE'),
	 (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'Operator'),
	 999);

INSERT INTO ApplicationEntityProperty VALUES 
	(NULL,  
	 (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'SNMPTRAP'),
	 (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE'),
	 (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'Operator'),
	 999);

INSERT INTO ApplicationEntityProperty VALUES 
	(NULL,  
	 (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'SYSLOG'),
	 (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE'),
	 (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'Operator'),
	 999);

