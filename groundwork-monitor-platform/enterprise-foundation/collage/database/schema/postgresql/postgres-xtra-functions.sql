-- This file contains a few extra functions to be added to PostgreSQL to
-- make it behave a bit more like MySQL, in ways that seem reasonable.

-- Copyright (c) 2013 GroundWork, Inc. (www.gwos.com).  All rights reserved.
-- Use is subject to GroundWork commercial license terms.

-- We encapsulate these changes in a transaction mainly so the brief extinction of
-- the (bigint AS text) cast between the DROP CAST statement and the CREATE CAST
-- statement is never visible to other clients that might already be using this.
START TRANSACTION ISOLATION LEVEL SERIALIZABLE;

-- Supply a PostgreSQL equivalent of the MySQL substring_index() function,
-- which is not provided natively by PostgreSQL.

CREATE OR REPLACE FUNCTION substring_index(str text, delim text, count int) RETURNS text AS $$
    SELECT CASE
	WHEN $3 > 0
	THEN array_to_string((string_to_array($1, $2))[1:$3], $2)
	ELSE array_to_string(ARRAY(SELECT unnest(string_to_array($1,$2)) OFFSET array_upper(string_to_array($1,$2),1) + $3), $2)
    END
$$ LANGUAGE sql;

-- Make a bigint trivially and automatically representable as text.
-- Why we only need to do this for bigint, and not also for integer or smallint,
-- remains to be investigated.  Does PostgreSQL already define those conversions?
-- Or do we just never need them?  In what context do we need this particular
-- conversion, and would it be better handled explicitly in those places?  Or
-- are we simply trying to maintain backward portability to MySQL, to support
-- third-party code originally written and maintained in the MySQL context?

CREATE OR REPLACE FUNCTION pg_catalog.text(bigint) RETURNS text STRICT IMMUTABLE LANGUAGE SQL AS 'SELECT textin(int8out($1));';

DROP CAST IF EXISTS (bigint AS text);

CREATE CAST (bigint AS text) WITH FUNCTION pg_catalog.text(bigint) AS IMPLICIT;

COMMENT ON FUNCTION pg_catalog.text(bigint) IS 'convert bigint to text';

-- Allow integers to be trivially and automatically interpreted as booleans.

-- FIX LATER:  These type conversions were put in play to ease the migration from
-- MySQL to PostgreSQL.  Now that we have moved past that point, we should strive
-- over time to identify exactly where and why these implicit casts are needed in
-- our code.  The problem with having them in play is they they happen not just
-- when you want them, but also when you don't expect them; and that can create
-- silently inappropriate query results.  If we can see that there are just a few
-- places where these implicit casts are used, it would probably be better to apply
-- explicit casts in those places, and drop these automatic type coercions.

UPDATE pg_cast SET castcontext = 'i' WHERE oid IN (
    SELECT c.oid
    FROM pg_cast c INNER JOIN pg_type src ON src.oid = c.castsource INNER JOIN pg_type tgt ON tgt.oid = c.casttarget
    WHERE src.typname = 'int4' AND tgt.typname = 'bool'
);

COMMIT;
