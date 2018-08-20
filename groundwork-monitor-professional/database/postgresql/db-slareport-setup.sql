-- Copyright (C) 2004-2012  GroundWork Inc.
-- SLAReport database setup. This script will be called fro clean installs and for upgrades to GWME 6.7.0

--
-- Create default users
--

CREATE USER slareport WITH PASSWORD 'slareport';

--
-- Create default databases
--

CREATE DATABASE slareport ENCODING='LATIN1' OWNER=slareport;

--
-- Initial privileges for the databases
--

GRANT ALL PRIVILEGES ON DATABASE slareport to slareport;
