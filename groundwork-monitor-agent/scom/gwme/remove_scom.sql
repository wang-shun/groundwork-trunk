-- revoke scom user privs
REVOKE ALL PRIVILEGES ON DATABASE scom FROM scomuser;

-- remove the scom database
DROP DATABASE scom;

-- remove the user
DROP USER scomuser;
