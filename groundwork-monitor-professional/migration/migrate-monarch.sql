-- Copyright (C) 2009 GroundWork Open Source, Inc. ("GroundWork")
-- All rights reserved. Use is subject to GroundWork commercial license terms.

use monarch;

-- Add commands here as necessary.  But first think about whether they really
-- belong instead in the migrate-monarch.pl script, where conditional logic can
-- be applied.

-- Event Broker
REPLACE INTO setup (name, type, value) VALUES ('broker_module','nagios','/usr/local/groundwork/common/lib/libbronx.so');
REPLACE INTO setup (name, type, value) VALUES ('event_broker_options','nagios','-1');
