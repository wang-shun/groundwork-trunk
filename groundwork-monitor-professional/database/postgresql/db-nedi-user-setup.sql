--
-- GroundWork Monitor GWMEE inital database setup and configuration
-- Created: March 7, 2016
-- Updated: May 22, 2018

--
-- Make sure databases are dropped before executing the commands
--
DROP DATABASE IF EXISTS nedi;
DROP DATABASE IF EXISTS nedi_nodes;
DROP USER IF EXISTS nedi;

--
-- Create default users
--
CREATE USER nedi WITH PASSWORD 'dbpa55';

--
-- Create default databases
--
CREATE DATABASE nedi       ENCODING='LATIN1' OWNER=nedi;
CREATE DATABASE nedi_nodes ENCODING='LATIN1' OWNER=nedi;


--
-- Initial privileges for the databases
--
GRANT ALL PRIVILEGES ON DATABASE nedi       to nedi;
GRANT ALL PRIVILEGES ON DATABASE nedi_nodes to nedi;
