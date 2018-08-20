-- HelpDesk Dynamic Property Types File
--
-- This script populates the Foundation database (gwcollagedb) with data
-- to support creating and managing helpdesk tickets.
--
-- Copyright 2013 GroundWork Open Source, Inc. ("GroundWork").
-- All rights reserved.  Use is subject to commercial license terms.
--
-- We want this script to operate idempotently, which mainly means dealing cleanly with
-- possible duplicate rows.  Generally, if an SQL error occurs, such as trying to insert
-- a duplicate row, that will kill the execution of the entire procedure at that point.
-- In MySQL, we could sidestep that either by using "INSERT ... ON DUPLICATE KEY UPDATE"
-- (a MySQL extension) or by wrapping the insertions within logic that only performs an
-- insertion if the row does not already exist.  To keep the code more portable between
-- databases, we choose to use procedural logic rather than a non-portable SQL extension.
-- (Regardless, this script is no longer portable between databases, simply because the
-- stored-procedure syntax is different between MySQL and PostgreSQL.)

CREATE OR REPLACE FUNCTION install_helpdesk_property_types()
RETURNS VOID
AS
$$
BEGIN

    RAISE INFO '';

    --
    -- Add Dynamic Property Types to Foundation
    --
    -- Note with respect to idempotency when running this script:  insertion into
    -- PropertyType will fail if we attempt to create a duplicate row.
    --

    RAISE INFO 'Adding PropertyType rows.';

    IF NOT EXISTS (
	SELECT NULL FROM PropertyType WHERE Name = 'TicketNo'
    ) THEN
	INSERT INTO PropertyType VALUES (DEFAULT, 'TicketNo', 'HelpDesk Ticket Number',          0, 0, 1, 0, 0, 0, 1);
    END IF;

    IF NOT EXISTS (
	SELECT NULL FROM PropertyType WHERE Name = 'Operator'
    ) THEN
	INSERT INTO PropertyType VALUES (DEFAULT, 'Operator', 'ID Of Operator Who Filed Ticket', 0, 0, 1, 0, 0, 0, 1);
    END IF;

    --
    -- Associate all four Application Types with new Dynamic Property Types
    --
    -- Note with respect to idempotency when running this script:  insertion into
    -- ApplicationEntityProperty will fail if we attempt to create a duplicate row.
    --

    RAISE INFO 'Adding ApplicationEntityProperty rows.';

    IF NOT EXISTS (
	SELECT NULL FROM ApplicationEntityProperty
	WHERE ApplicationTypeID =
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'SYSTEM')
	AND EntityTypeID =
	    (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE')
	AND PropertyTypeID =
	    (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'TicketNo')
    ) THEN
	INSERT INTO ApplicationEntityProperty VALUES (
	    DEFAULT,
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'SYSTEM'),
	    (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE'),
	    (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'TicketNo'),
	    999
	);
    END IF;

    IF NOT EXISTS (
	SELECT NULL FROM ApplicationEntityProperty
	WHERE ApplicationTypeID =
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'NAGIOS')
	AND EntityTypeID =
	    (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE')
	AND PropertyTypeID =
	    (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'TicketNo')
    ) THEN
	INSERT INTO ApplicationEntityProperty VALUES (
	    DEFAULT,
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'NAGIOS'),
	    (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE'),
	    (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'TicketNo'),
	    999
	);
    END IF;

    IF NOT EXISTS (
	SELECT NULL FROM ApplicationEntityProperty
	WHERE ApplicationTypeID =
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'SNMPTRAP')
	AND EntityTypeID =
	    (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE')
	AND PropertyTypeID =
	    (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'TicketNo')
    ) THEN
	INSERT INTO ApplicationEntityProperty VALUES (
	    DEFAULT,
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'SNMPTRAP'),
	    (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE'),
	    (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'TicketNo'),
	    999
	);
    END IF;

    IF NOT EXISTS (
	SELECT NULL FROM ApplicationEntityProperty
	WHERE ApplicationTypeID =
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'SYSLOG')
	AND EntityTypeID =
	    (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE')
	AND PropertyTypeID =
	    (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'TicketNo')
    ) THEN
	INSERT INTO ApplicationEntityProperty VALUES (
	    DEFAULT,
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'SYSLOG'),
	    (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE'),
	    (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'TicketNo'),
	    999
	);
    END IF;

    IF NOT EXISTS (
	SELECT NULL FROM ApplicationEntityProperty
	WHERE ApplicationTypeID =
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'GDMA')
	AND EntityTypeID =
	    (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE')
	AND PropertyTypeID =
	    (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'TicketNo')
    ) THEN
	INSERT INTO ApplicationEntityProperty VALUES (
	    DEFAULT,
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'GDMA'),
	    (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE'),
	    (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'TicketNo'),
	    999
	);
    END IF;

    IF NOT EXISTS (
	SELECT NULL FROM ApplicationEntityProperty
	WHERE ApplicationTypeID =
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'NOMA')
	AND EntityTypeID =
	    (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE')
	AND PropertyTypeID =
	    (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'TicketNo')
    ) THEN
	INSERT INTO ApplicationEntityProperty VALUES (
	    DEFAULT,
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'NOMA'),
	    (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE'),
	    (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'TicketNo'),
	    999
	);
    END IF;

    IF NOT EXISTS (
	SELECT NULL FROM ApplicationEntityProperty
	WHERE ApplicationTypeID =
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'VEMA')
	AND EntityTypeID =
	    (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE')
	AND PropertyTypeID =
	    (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'TicketNo')
    ) THEN
	INSERT INTO ApplicationEntityProperty VALUES (
	    DEFAULT,
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'VEMA'),
	    (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE'),
	    (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'TicketNo'),
	    999
	);
    END IF;

    IF NOT EXISTS (
	SELECT NULL FROM ApplicationEntityProperty
	WHERE ApplicationTypeID =
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'SYSTEM')
	AND EntityTypeID =
	    (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE')
	AND PropertyTypeID =
	    (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'Operator')
    ) THEN
	INSERT INTO ApplicationEntityProperty VALUES (
	    DEFAULT,
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'SYSTEM'),
	    (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE'),
	    (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'Operator'),
	    999
	);
    END IF;

    IF NOT EXISTS (
	SELECT NULL FROM ApplicationEntityProperty
	WHERE ApplicationTypeID =
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'NAGIOS')
	AND EntityTypeID =
	    (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE')
	AND PropertyTypeID =
	    (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'Operator')
    ) THEN
	INSERT INTO ApplicationEntityProperty VALUES (
	    DEFAULT,
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'NAGIOS'),
	    (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE'),
	    (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'Operator'),
	    999
	);
    END IF;

    IF NOT EXISTS (
	SELECT NULL FROM ApplicationEntityProperty
	WHERE ApplicationTypeID =
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'SNMPTRAP')
	AND EntityTypeID =
	    (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE')
	AND PropertyTypeID =
	    (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'Operator')
    ) THEN
	INSERT INTO ApplicationEntityProperty VALUES (
	    DEFAULT,
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'SNMPTRAP'),
	    (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE'),
	    (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'Operator'),
	    999
	);
    END IF;

    IF NOT EXISTS (
	SELECT NULL FROM ApplicationEntityProperty
	WHERE ApplicationTypeID =
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'SYSLOG')
	AND EntityTypeID =
	    (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE')
	AND PropertyTypeID =
	    (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'Operator')
    ) THEN
	INSERT INTO ApplicationEntityProperty VALUES (
	    DEFAULT,
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'SYSLOG'),
	    (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE'),
	    (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'Operator'),
	    999
	);
    END IF;

    IF NOT EXISTS (
	SELECT NULL FROM ApplicationEntityProperty
	WHERE ApplicationTypeID =
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'GDMA')
	AND EntityTypeID =
	    (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE')
	AND PropertyTypeID =
	    (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'Operator')
    ) THEN
	INSERT INTO ApplicationEntityProperty VALUES (
	    DEFAULT,
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'GDMA'),
	    (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE'),
	    (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'Operator'),
	    999
	);
    END IF;

    IF NOT EXISTS (
	SELECT NULL FROM ApplicationEntityProperty
	WHERE ApplicationTypeID =
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'NOMA')
	AND EntityTypeID =
	    (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE')
	AND PropertyTypeID =
	    (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'Operator')
    ) THEN
	INSERT INTO ApplicationEntityProperty VALUES (
	    DEFAULT,
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'NOMA'),
	    (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE'),
	    (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'Operator'),
	    999
	);
    END IF;

    IF NOT EXISTS (
	SELECT NULL FROM ApplicationEntityProperty
	WHERE ApplicationTypeID =
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'VEMA')
	AND EntityTypeID =
	    (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE')
	AND PropertyTypeID =
	    (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'Operator')
    ) THEN
	INSERT INTO ApplicationEntityProperty VALUES (
	    DEFAULT,
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'VEMA'),
	    (SELECT EntityTypeID      FROM EntityType      WHERE Name = 'LOG_MESSAGE'),
	    (SELECT PropertyTypeID    FROM PropertyType    WHERE Name = 'Operator'),
	    999
	);
    END IF;

    -- Output a simple completion message.
    --
    -- Because we cannot do conditional logic outside of this function, and we cannot
    -- end the current transaction inside this function, we need to emit this message
    -- before the COMMIT that will actually update the database.  But we would not
    -- have gotten here had the function failed earlier (we would have ended up in the
    -- EXCEPTION clause below), so emitting the message now is a reasonable compromise.
    RAISE INFO '';
    RAISE INFO 'gwcollagedb has been updated to include helpdesk dynamic property types.';
    RAISE INFO '';
    RAISE INFO 'Committing all helpdesk dynamic property type changes.';
    RAISE INFO '';

    RETURN;

EXCEPTION
    WHEN OTHERS THEN
	-- There is no need to explicitly roll back what we did here within this BEGIN block;
	-- that is implicit in the exception handling that got us here.  But we do want to
	-- report the failure to the user running this script.
	RAISE INFO '';
	RAISE WARNING '';
	RAISE WARNING 'Adding the helpdesk dynamic property type changes failed.';
	RAISE WARNING '';
	-- Re-throw the exception, so the details of the specific failure will be printed
	-- where the administrator can see them.
	RAISE;

END
$$
LANGUAGE PLPGSQL;

-- We turn off display of output headers and footers and such, because the main SELECT
-- below doesn't produce anything interesting, and such decoration is just confusing.
\pset tuples_only on

-- Begin Transaction
START TRANSACTION ISOLATION LEVEL REPEATABLE READ;

-- Execute Stored Procedure
SELECT install_helpdesk_property_types();

-- Commit All Changes
-- If an error occurred while running the function, the following COMMIT will
-- actually ROLLBACK instead (and be printed that way when this executes).
COMMIT;

-- Remove Stored Procedure
DROP FUNCTION IF EXISTS install_helpdesk_property_types();

