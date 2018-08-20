#
# Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")  
# All rights reserved. Use is subject to GroundWork commercial license terms.
#
-----------------------------------------------
-- Remove Event Broker entries to Monarch database
------------------------------------------------
use monarch;

DELETE FROM setup WHERE name='broker_module' AND type='nagios';
DELETE FROM setup WHERE name='event_broker_options'AND type='nagios';
