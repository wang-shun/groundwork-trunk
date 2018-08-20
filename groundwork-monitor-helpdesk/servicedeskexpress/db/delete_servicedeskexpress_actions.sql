-- Copyright 2013 GroundWork Open Source, Inc. ("GroundWork").
-- All rights reserved.  Use is subject to commercial license terms.

-- ==============================================================================
-- Delete Service Desk Express Actions
-- This script deletes critical rows from the Foundation database (GWCollageDB)
-- that support creating and managing helpdesk tickets, so resources from the
-- integration package are not referenced after the package is removed.
-- ==============================================================================

CREATE OR REPLACE FUNCTION delete_helpdesk_actions(ActionMenuString varchar)
RETURNS VOID
AS
$$
BEGIN

    -- This is the critical deletion.
    DELETE FROM Action WHERE Name = ActionMenuString;

    -- All the other table insertions we did upon installation were to tables
    -- with an ON DELETE CASCADE clause on the Action.ActionID field, so there
    -- is no reason to carry out any additional deletions here.

    -- Output a simple completion message.
    --
    -- Because we cannot do conditional logic outside of this function, and we cannot
    -- end the current transaction inside this function, we need to emit this message 
    -- before the COMMIT that will actually update the database.  But we would not
    -- have gotten here had the function failed earlier (we would have ended up in the
    -- EXCEPTION clause below), so emitting the message now is a reasonable compromise.
    RAISE INFO '';
    RAISE INFO 'gwcollagedb has been updated to delete helpdesk actions.';
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
        RAISE WARNING 'Deleting the helpdesk action changes failed.';
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
SELECT delete_helpdesk_actions( 'Create a HelpDesk Ticket' );

-- Commit All Changes
-- If an error occurred while running the function, the following COMMIT will
-- actually ROLLBACK instead (and be printed that way when this executes).
COMMIT;

-- Remove Stored Procedure
DROP FUNCTION IF EXISTS delete_helpdesk_actions(varchar);

