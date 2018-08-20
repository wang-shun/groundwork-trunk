# Makefile for testing the GW::RAPID package.

GW_RAPID_PACKAGE = /usr/local/groundwork/perl/lib/site_perl/5.8.9/GW/RAPID.pm
TEST_TYPES =			\
	overhead		\
	application_types	\
	devices			\
	hosts			\
	categories		\
	events			\
	services		\
	hostgroups		\
	notifications		\
	auditlogs		\
	license

# Note that getting the final all-clear message does NOT indicate that everything
# is okay.  It only indicates that to our test scripts found no errors.  It says
# nothing about whether our test scripts actually provide adequate coverage of
# sensible test cases.
test	: $(GW_RAPID_PACKAGE) $(TEST_TYPES) 
	echo "=== ALL CURRENT GW::RAPID UNIT TESTS PASSED ==="

overhead	:
	00.load.t
	new.t
	interface.t
	get_version.t

application_types	:
	upsert_application_types.t
	get_application_types.t
	delete_application_types.t

devices	:
	upsert_devices.t
	get_devices.t
	delete_devices.t

hosts	:
	upsert_hosts.t
	get_hosts.t
	delete_hosts.t
	hostblacklists.t

categories	:
	upsert_categories.t
	get_categories.t
	delete_categories.t

events	:
	create_events.t
	update_events.t
	get_events.t
	delete_events.t

services	:
	upsert_services.t
	get_services.t
	delete_services.t

hostgroups	:
	upsert_hostgroups.t
	get_hostgroups.t
	clear_hostgroups.t
	delete_hostgroups.t

notifications	:
	create_noma_host_notifications.t
	create_noma_service_notifications.t

auditlogs	:
	create_auditlogs.t
	get_auditlogs.t

license	:
	check_license.t

NOT_OK	:
	pod.tbd
	internals.tbd
