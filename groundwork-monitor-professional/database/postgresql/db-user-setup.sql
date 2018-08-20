--
-- GroundWork Monitor GWMEE inital database setup and configuration
-- Created: October 18, 2011
-- Updated: June 11, 2018

--
-- Create default users
--

CREATE USER monarch   WITH PASSWORD 'gwrk';
CREATE USER cactiuser WITH PASSWORD 'cactiuser';
CREATE USER nedi      WITH PASSWORD 'dbpa55';

--
-- Create default databases
--

CREATE DATABASE monarch    ENCODING='LATIN1' OWNER=monarch;
CREATE DATABASE cacti      ENCODING='LATIN1' OWNER=cactiuser;
CREATE DATABASE nedi       ENCODING='LATIN1' OWNER=nedi;
CREATE DATABASE nedi_nodes ENCODING='LATIN1' OWNER=nedi;


--
-- Initial privileges for the databases
--

GRANT ALL PRIVILEGES ON DATABASE monarch    to monarch;
GRANT ALL PRIVILEGES ON DATABASE cacti      to cactiuser;
GRANT ALL PRIVILEGES ON DATABASE nedi       to nedi;
GRANT ALL PRIVILEGES ON DATABASE nedi_nodes to nedi;

