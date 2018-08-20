#
# Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")  
# All rights reserved. Use is subject to GroundWork commercial license terms.
#
-----------------------------------------------
-- Add Event Broker entries to Monarch database
------------------------------------------------
use monarch;

REPLACE INTO setup (name, type, value) VALUES ('broker_module','nagios','/usr/local/groundwork/common/lib/libbronx.so');
REPLACE INTO setup (name, type, value) VALUES ('event_broker_options','nagios','-1');
