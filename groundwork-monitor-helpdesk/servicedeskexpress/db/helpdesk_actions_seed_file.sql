-- HelpDesk Actions Seed File
--
-- This script populates the Foundation database (gwcollagedb) with data
-- to support creating and managing helpdesk tickets.
--
-- Note that you will need to customize the string given in the install_helpdesk_actions()
-- function call at the end of the script, to reflect what the customer wants to see in
-- their action menu.
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

CREATE OR REPLACE FUNCTION install_helpdesk_actions (ActionMenuString varchar)
RETURNS VOID
AS
$$
BEGIN
    RAISE INFO '';

    RAISE INFO 'Adding Action row.';

    -- This insertion would normally fail if the Name field already contains this value.
    IF NOT EXISTS ( SELECT NULL FROM Action WHERE Name = ActionMenuString ) THEN
	INSERT INTO Action (ActionTypeID,Name,Description) VALUES(
	    (SELECT ActionTypeID FROM ActionType WHERE Name = 'SCRIPT_ACTION'),
	    ActionMenuString, concat(ActionMenuString, ' for the selected items.')
	);
    END IF;

    RAISE INFO 'Adding ActionProperty row.';

    -- This insertion would normally fail if there is already a {ActionID, Name}
    -- pair matching this specification in the ActionProperty table.
    IF NOT EXISTS (
	SELECT NULL FROM ActionProperty
	WHERE ActionID =
	    (SELECT ActionID FROM Action WHERE Name = ActionMenuString)
	AND Name = 'Script'
    ) THEN
	INSERT INTO ActionProperty (ActionID, Name, Value) VALUES(
	    (SELECT ActionID FROM Action WHERE Name = ActionMenuString),
	    'Script', '/usr/local/groundwork/servicedeskexpress/bin/oneway_helpdesk.pl'
	);
    END IF;

    RAISE INFO 'Adding ApplicationAction rows.';

    -- This insertion would normally fail if we attempted to create a duplicate row.
    IF NOT EXISTS (
	SELECT NULL FROM ApplicationAction
	WHERE ApplicationTypeID =
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'NAGIOS')
	AND ActionID =
	    (SELECT ActionID FROM Action WHERE Name = ActionMenuString)
    ) THEN
	INSERT INTO ApplicationAction (ApplicationTypeID,ActionID) VALUES (
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'NAGIOS'),
	    (SELECT ActionID FROM Action WHERE Name = ActionMenuString)
	);
    END IF;

    -- This insertion would normally fail if we attempted to create a duplicate row.
    IF NOT EXISTS (
	SELECT NULL FROM ApplicationAction
	WHERE ApplicationTypeID =
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'SNMPTRAP')
	AND ActionID =
	    (SELECT ActionID FROM Action WHERE Name = ActionMenuString)
    ) THEN
	INSERT INTO ApplicationAction (ApplicationTypeID,ActionID) VALUES (
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'SNMPTRAP'),
	    (SELECT ActionID FROM Action WHERE Name = ActionMenuString)
	);
    END IF;

    -- This insertion would normally fail if we attempted to create a duplicate row.
    IF NOT EXISTS (
	SELECT NULL FROM ApplicationAction
	WHERE ApplicationTypeID =
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'SYSLOG')
	AND ActionID =
	    (SELECT ActionID FROM Action WHERE Name = ActionMenuString)
    ) THEN
	INSERT INTO ApplicationAction (ApplicationTypeID,ActionID) VALUES (
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'SYSLOG'),
	    (SELECT ActionID FROM Action WHERE Name = ActionMenuString)
	);
    END IF;

    -- This insertion would normally fail if we attempted to create a duplicate row.
    IF NOT EXISTS (
	SELECT NULL FROM ApplicationAction
	WHERE ApplicationTypeID =
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'SYSTEM')
	AND ActionID =
	    (SELECT ActionID FROM Action WHERE Name = ActionMenuString)
    ) THEN
	INSERT INTO ApplicationAction (ApplicationTypeID,ActionID) VALUES (
	    (SELECT ApplicationTypeID FROM ApplicationType WHERE Name = 'SYSTEM'),
	    (SELECT ActionID FROM Action WHERE Name = ActionMenuString)
	);
    END IF;

    RAISE INFO 'Adding ActionParameter rows.';

    -- Script for Action parameters
    -- This insertion would normally fail if we attempted to create a duplicate row.
    IF NOT EXISTS (
	SELECT NULL FROM ActionParameter
	WHERE ActionID =
	    (SELECT ActionID FROM Action WHERE Name = ActionMenuString)
	AND Name = 'UserName'
    ) THEN
	INSERT INTO ActionParameter (ActionID, Name, Value) VALUES (
	    (SELECT ActionID FROM Action WHERE Name = ActionMenuString),'UserName','UserName'
	);
    END IF;

    -- Script for Action parameters
    -- This insertion would normally fail if we attempted to create a duplicate row.
    IF NOT EXISTS (
	SELECT NULL FROM ActionParameter
	WHERE ActionID =
	    (SELECT ActionID FROM Action WHERE Name = ActionMenuString)
	AND Name = 'LogMessageIds'
    ) THEN
	INSERT INTO ActionParameter (ActionID, Name, Value) VALUES (
	    (SELECT ActionID FROM Action WHERE Name = ActionMenuString),'LogMessageIds','LogMessageIds'
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
    RAISE INFO 'gwcollagedb has been updated to include helpdesk actions.';
    RAISE INFO '';
    RAISE INFO 'Committing all helpdesk action changes.';
    RAISE INFO '';

    RETURN;

EXCEPTION
    WHEN OTHERS THEN
	-- There is no need to explicitly roll back what we did here within this BEGIN block;
	-- that is implicit in the exception handling that got us here.  But we do want to
	-- report the failure to the user running this script.
	RAISE INFO '';
	RAISE WARNING '';
	RAISE WARNING 'Adding the helpdesk action changes failed.';
	RAISE WARNING '';
	-- Re-throw the exception, so the details of the specific failure will be printed
	-- where the administrator can see them.
	RAISE;

END;
$$
LANGUAGE PLPGSQL;

-- We turn off display of output headers and footers and such, because the main SELECT
-- below doesn't produce anything interesting, and such decoration is just confusing.
\pset tuples_only on

-- Begin Transaction
START TRANSACTION ISOLATION LEVEL REPEATABLE READ;

-- Execute Stored Procedure
-- Customize the function argument string (the action menu string, which will
-- appear in the Event Console action menu) as needed for your integration.
-- FIX LATER:  we ought to obtain this string from some external source,
-- so we don't have to modify this script for every distinct integration.
SELECT install_helpdesk_actions( 'Create a HelpDesk Ticket' );

-- Commit All Changes
-- If an error occurred while running the function, the following COMMIT will
-- actually ROLLBACK instead (and be printed that way when this executes).
COMMIT;

-- Remove Stored Procedure
DROP FUNCTION IF EXISTS install_helpdesk_actions(varchar);

