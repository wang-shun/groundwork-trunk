package CollageQuery;

use 5.0;
use strict;
use warnings;

require Exporter;
use DBI;
use Carp;

our @ISA = qw(Exporter);

# Items to export into caller's namespace by default.  Note: do not export
# names by default without a very good reason.  Use EXPORT_OK instead.
# Do not simply export all your public functions/methods/constants.

# NOTE:  Routines with "Sync" in their names are specially designed for use
# with Sync operations, and are subject to evolution in concert with Sync
# code elsewhere without regard to any kind of backward compatibility.
# Do not use them in your own code.

our @EXPORT_OK = qw(
	readGroundworkDBConfig
	new
	destroy
	getServicesForHostGroup
	getHostsForHostGroup
	getHostGroups
	getHostGroupsByType
	getHostGroupsForHost
	getHostGroup
	getServicesForHost
	getHosts
	getHostsByType
	getHostTypes
	getHostServiceTypes
	getHostStatusForHost
	getDeviceForHost
	getServiceNamesForHost
	getDevicesForHosts
	getEventsbyDate
	getEventsbyDate_TEST
	getEventsForDevice
	getEventsForService
	getEventsForHost
	getHostServices
	getService
	getServices
	getMonitorServers
	getHostsForMonitorServer
	getHostGroupsForMonitorServer
	getServiceGroups
	getServiceGroupsForService
	getHostsForServiceGroup
	getHostServicesForServiceGroup
	getHostParents
	getHostAlias
	getServiceProperties
	getHostCount
	getServiceCount
);
our $VERSION = '0.8.2';

# 0.8.2 DN 10/30/15 getServiceGroups() updated for GWMON-12290 - see getServiceGroups() for details

##########################################################
#
#	Constructor methods
#
##########################################################

sub readGroundworkDBConfig {
	my $type     = shift;
	my $database = undef;
	my $dbhost   = undef;
	my $username = undef;
	my $password = undef;
	my $dbtype   = 'mysql';
	my $gwconfigfile =  "/usr/local/groundwork/config/db.properties";
	if ($type !~ /^(collage|insightreports|monarch|sv)$/) { return "ERROR: Invalid database type."; }
	if (!open(CONFIG,"$gwconfigfile")) {
		return "ERROR: Unable to find configuration file $gwconfigfile";
	}
	while (my $line=<CONFIG>) {
		chomp $line;
		if ($line =~ /\s*$type\.(\S+)\s*=\s*(\S*)\s*/) {
			if (($1 eq "username") or ($1 eq "dbusername")) {
				$username = $2;
			} elsif (($1 eq "password") or ($1 eq "dbpassword")) {
				$password = $2;
			} elsif (($1 eq "database") or ($1 eq "dbdatabase")) {
				$database = $2;
			} elsif ($1 eq "dbhost") {
				$dbhost = $2;
			}
		}
		if ( $line =~ /^\s*global\.db\.type\s*=\s*(\S+)/ ) { $dbtype = $1 }
	}
	close CONFIG;
	return ($database,$dbhost,$username,$password,$dbtype);
}

# Important:  Be prepared to die here.  That is, you should ordinarily
# call this from within an eval{}, and handle exceptions accordingly.
sub new {
	my $self = {};
	# Read properties file to get database properties
	my ($database,$host,$username,$password,$dbtype) = readGroundworkDBConfig('collage');
	# Connect to database here
	my $dsn = '';
	if ( defined($dbtype) && $dbtype eq 'postgresql' ) {
	    $dsn = "DBI:Pg:dbname=$database;host=$host";
	}
	else {
	    $dsn = "DBI:mysql:database=$database;host=$host";
	}
	$self->{dbh} = DBI->connect($dsn, $username,$password, { 'AutoCommit' => 1 })
		or croak "Can't connect to database $database. Error: ".$DBI::errstr;
	#print "Connect to database $database\n";
	# Create new object
	bless ($self);
	return $self;
}

sub destroy {
	my $self = shift;
	# Close connection to database here.
	if ($self->{dbh}) {
		$self->{dbh}->disconnect();
		delete $self->{dbh};
	}
	return;
}

# Perl-standard destructor.  Useful to have in place for when programs forget to call destroy().
sub DESTROY {
	my $self = shift;
	destroy ($self);
}

##########################################################
#
#	CollageHostGroupQuery class methods
#
##########################################################

# return a reference to a hash host-service-attributes for a designated host group
sub getServicesForHostGroup {
	my $self = shift;
	my $hostgroup = shift;
	my $service_ref = undef;
	# print "Looking for host group $hostgroup \n";
	my $quoted_hg = $self->{dbh}->quote($hostgroup);
	my $sql =
		"select
			s.ServiceStatusID	as \"ServiceStatusID\",
			s.ApplicationTypeID	as \"ApplicationTypeID\",
			s.ServiceDescription	as \"ServiceDescription\",
			s.HostID		as \"HostID\",
			s.MonitorStatusID	as \"MonitorStatusID\",
			s.LastCheckTime		as \"LastCheckTime\",
			s.NextCheckTime		as \"NextCheckTime\",
			s.LastStateChange	as \"LastStateChange\",
			s.LastHardStateID	as \"LastHardStateID\",
			s.StateTypeID		as \"StateTypeID\",
			s.CheckTypeID		as \"CheckTypeID\",
			s.MetricType		as \"MetricType\",
			s.Domain		as \"Domain\",
			h.HostName		as \"HostName\",
			s.ServiceDescription	as \"ServiceDescription\",
			st.Name			as \"StateType\",
			ct.Name			as \"CheckType\",
			ms.Name			as \"MonitorStatus\",
			lhs.Name		as \"LastHardState\"
		from 	ServiceStatus		as s,
			Host			as h,
			HostGroup		as hg,
			HostGroupCollection	as hgc,
			StateType		as st,
			CheckType		as ct,
			MonitorStatus		as ms,
			MonitorStatus		as lhs
		where	hg.Name = $quoted_hg
		and	hgc.HostGroupID = hg.HostGroupID
		and	h.HostID = hgc.HostID
		and	s.HostID = h.HostID
		and	st.StateTypeID = s.StateTypeID
		and	ct.CheckTypeID = s.CheckTypeID
		and	lhs.MonitorStatusID = s.LastHardStateID
		and	ms.MonitorStatusID = s.MonitorStatusID;
		";
	my $sth = $self->{dbh}->prepare($sql);
	$sth->execute();
	while (my $hashref = $sth->fetchrow_hashref()) {
		# $service_ref->{"Host_A"}->{"Service_2"}->{"attribute"} = "attribute_value";
		foreach my $key (keys %{$hashref}) {
			## If key ends in ID, then it's a Primary Key so don't assign
			if (($key eq "HostName") or ($key eq "ServiceDescription") or ($key =~ /ID$/)) { next }
			$service_ref->{$hashref->{HostName}}->{$hashref->{ServiceDescription}}->{$key} = $hashref->{$key};
		}
	}
	$sth->finish;
	if (!defined($service_ref)) {
		return '';
	}
	return \%{$service_ref};
}

#	return a reference to a hash of all host names, device name for a designated host group
sub getHostsForHostGroup {
	my $self = shift;
	my $hostgroup = shift;
	my $host_ref = undef;
	# print "Looking for host group $hostgroup \n";
	my $quoted_hg = $self->{dbh}->quote($hostgroup);
	my $sql =
		"select
			h.HostName		as \"HostName\",
			h.Description		as \"HostDescription\",
			d.Identification	as \"DeviceIdentification\",
			d.Description		as \"DeviceDescription\"
		from
		HostGroup		as hg,
		HostGroupCollection	as hgc,
		Host			as h,
		Device			as d
		where	hg.Name = $quoted_hg
		and	hgc.HostGroupID = hg.HostGroupID
		and	h.HostID = hgc.HostID
		and	d.DeviceID = h.DeviceID
		;
		";
	my $sth = $self->{dbh}->prepare($sql);
	$sth->execute();
	while (my $hashref = $sth->fetchrow_hashref()) {
		## $host_ref->{"Host_A"}->{HostDescription}="Host A description";
		foreach my $key (keys %{$hashref}) {
			if (($key eq "HostName") or ($key =~ /ID$/)) { next }
			$host_ref->{$hashref->{HostName}}->{$key} = $hashref->{$key};
		}
	}
	$sth->finish;
	if (!defined($host_ref)) {
		return '';
	}
	return \%{$host_ref};
}

# FIX LATER:  This approximates what we would need to implement to pull all service groups, along
# with all of their host and service details, in one query.  The main thing we would also need is
# a left join somewhere in the middle of this, to also retrieve the names of empty servicegroups
# (those with no associated hosts/services assigned).  We will create a routine for that in a
# future version.

my $full_servicegroup_join_sql =
		"select DISTINCT
			c.Name			as \"Service Group\",
			h.HostName		as \"HostName\",
			ss.ServiceDescription	as \"ServiceDescription\"
		from	Host h, ServiceStatus ss, CategoryEntity ce, Category c, EntityType et
		where	et.Name = 'SERVICE_GROUP'
		and	c.EntityTypeID = et.EntityTypeID
		and	ce.CategoryID = c.CategoryID
		and	ss.ServiceStatusID = ce.ObjectID
		and	h.HostID = ss.HostID
		;";

#       return a reference to a hash of all hosts for a designated service group
sub getHostsForServiceGroup {
	my $self = shift;
	my $servicegroup = shift;
	my $host_ref = undef;
	my $sql =
		"select DISTINCT h.HostName as \"HostName\"
		from	Host h, ServiceStatus ss, CategoryEntity ce, Category c, EntityType et
		where	et.Name = 'SERVICE_GROUP'
		and	c.EntityTypeID = et.EntityTypeID
		and	c.Name = '$servicegroup'
		and	ce.CategoryID = c.CategoryID
		and	ss.ServiceStatusID = ce.ObjectID
		and	h.HostID = ss.HostID
		;";
	my $sth = $self->{dbh}->prepare($sql);
	$sth->execute();
	while (my $hashref = $sth->fetchrow_hashref()) {
		$host_ref->{$hashref->{HostName}} = 1;
	}
	$sth->finish;
	if (!defined($host_ref)) {
		return '';
	}
	return \%{$host_ref};
}

#       return a reference to a hash of all service names for a designated service group and host
sub getHostServicesForServiceGroup {
	my $self = shift;
	my $servicegroup = shift;
	my $sghost = shift;
	my $hostservice_ref = undef;
	my $sql =
		"select
			h.HostName		as \"HostName\",
			ss.ServiceDescription	as \"ServiceDescription\"
		from	Host h, ServiceStatus ss, CategoryEntity ce, Category c, EntityType et
		where	et.Name = 'SERVICE_GROUP'
		and	c.EntityTypeID = et.EntityTypeID
		and	c.Name = '$servicegroup'
		and	ce.CategoryID = c.CategoryID
		and	ss.ServiceStatusID = ce.ObjectID
		and	h.HostID = ss.HostID
		and	h.HostName = '$sghost'
		;";
	my $sth = $self->{dbh}->prepare($sql);
	$sth->execute();
	while (my $hashref = $sth->fetchrow_hashref()) {
		$hostservice_ref->{$hashref->{ServiceDescription}} = 1;
	}
	$sth->finish;
	if (!defined($hostservice_ref)) {
		return '';
	}
	return \%{$hostservice_ref};
}

#       return a string of parents for a designated host
sub getHostParents {
	my $self = shift;
	my $host = shift;
	my $host_parents = undef;
	my $sql =
		"select hsp.ValueString as \"ValueString\"
		from	Host h, HostStatusProperty hsp, PropertyType pt
		where	h.HostName = '$host'
		and	hsp.HostStatusID = h.HostID
		and	pt.PropertyTypeID = hsp.PropertyTypeID
		and	pt.Name = 'Parent';";
	my $sth = $self->{dbh}->prepare($sql);
	$sth->execute();
	while (my $hashref = $sth->fetchrow_hashref()) {
		$host_parents = $hashref->{'ValueString'};
	}
	$sth->finish;
	if (!defined($host_parents)) {
		return '';
	}
	return $host_parents;
}

##########################################################
#
#	CollagePropertiesQuery class methods
#
##########################################################

#       return the alias for a designated host
sub getHostAlias {
	my $self = shift;
	my $host = shift;
	my $alias = undef;
	my $sql =
		"select hsp.ValueString as \"ValueString\"
		from Host h, HostStatusProperty hsp, PropertyType pt
		where	h.HostName='$host'
		and	hsp.HostStatusID = h.HostID
		and	pt.PropertyTypeID = hsp.PropertyTypeID
		and	pt.Name='Alias';";
	my $sth = $self->{dbh}->prepare($sql);
	$sth->execute();
	while (my $hashref = $sth->fetchrow_hashref()) {
		$alias = $hashref->{'ValueString'};
	}
	$sth->finish;
	if (!defined($alias)) {
		return '';
	}
	return $alias;
}

#       return the value of a specific host/service property
sub getServiceProperties {
	my $self = shift;
	my $host = shift;
	my $service = shift;
	my $propname = shift;
	my $properties = undef;
	my $sql =
		"select ssp.ValueString as \"ValueString\"
		from Host h, ServiceStatus ss, ServiceStatusProperty ssp, PropertyType pt
		where	h.HostName='$host'
		and	ss.HostID = h.HostID
		and	ss.ServiceDescription = '$service'
		and	ssp.ServiceStatusID = ss.ServiceStatusID
		and	pt.PropertyTypeID = ssp.PropertyTypeID
		and	pt.Name = '$propname';
		";
	my $sth = $self->{dbh}->prepare($sql);
	$sth->execute();
	while (my $hashref = $sth->fetchrow_hashref()) {
		$properties = $hashref->{'ValueString'};
	}
	$sth->finish;
	if (!defined($properties)) {
		return '';
	}
	return $properties;
}

#	This routine is supported for backward compatibility.
#	Return a reference to all hostgroup names, descriptions, and aliases, or an empty string.
sub getHostGroups {
	my $self = shift;
	return $self->getHostGroupsByType('NAGIOS');
}
#	return a reference to all hostgroup names, descriptions, and aliases, or an empty string
sub getHostGroupsByType {
	my $self            = shift;
	my $applicationType = shift;
	my $hostgroup_ref   = undef;
	my $app_type_condition;
	if ($applicationType) {
	    my $quoted_appType  = $self->{dbh}->quote($applicationType);
	    $app_type_condition = "where at.Name = $quoted_appType";
	}
	else {
	    $app_type_condition = '';
	}
	my $sql = "select
		hg.HostGroupID		as \"HostGroupID\",
		hg.Name			as \"Name\",
		hg.Description		as \"Description\",
		hg.ApplicationTypeID	as \"ApplicationTypeID\",
		hg.Alias		as \"Alias\"
		from	ApplicationType at join HostGroup hg using (ApplicationTypeID)
		$app_type_condition;";
	my $sth = $self->{dbh}->prepare($sql);
	$sth->execute();
	# $hostgroup_ref->{"Hostgroup1"}->{Description} = "Hostgroup1 Description";
	while (my $hashref = $sth->fetchrow_hashref()) {
		foreach my $key (keys %{$hashref}) {
			if (($key eq "Name") or ($key =~ /ID$/)) { next }
			$hostgroup_ref->{$hashref->{Name}}->{$key} = $hashref->{$key};
		}
	}
	$sth->finish;
	if (!defined($hostgroup_ref)) {
		return '';
	}
	return \%{$hostgroup_ref};
}

#	return a reference to an array with the names of all hostgroups to which a host belongs
sub getHostGroupsForHost {
	my $self = shift;
	my $host = shift;
	my @hostgroup_names = ();
	my $quoted_host = $self->{dbh}->quote($host);
	my $sql =
		"select hg.name as \"hostgroup\"
		from host h
		    left join hostgroupcollection hgc on hgc.hostid = h.hostid
		    left join hostgroup hg on hg.hostgroupid = hgc.hostgroupid
		where h.hostname = $quoted_host;
		";
	my $sth = $self->{dbh}->prepare($sql);
	$sth->execute();
	while (my @values = $sth->fetchrow_array()) {
	    push(@hostgroup_names, $values[0]);
	}
	$sth->finish;
	return \@hostgroup_names;
}

#	return a reference to all servicegroup names and descriptions
sub getServiceGroups {
	my $self = shift;
	my $servicegroup_ref = undef;
	#my $sql =
	#	"select c.Name as \"Service Group\", c.Description as \"Description\"
	#	from	Category c, EntityType et
	#	where	et.Name = 'SERVICE_GROUP'
	#	and	c.EntityTypeID = et.EntityTypeID
	#	;";
	# DN GWMON-12290 Update 10/30/15: service groups that are created via a monarch sync using the GW XML api currently end up with applicationtypeid = null.
	# Also, we later want to be able to filter down to only various application types. 
	# So, this query update adds the ApplicationType (eg NAGIOS, SYSTEM etc), and if the apptypeid in category row is null, assumes it's nagios
	my $sql = 
		"SELECT c.Name as \"Service Group\", c.Description as \"Description\", at.name as \"ApplicationType\"
                 FROM   Category c, EntityType et, ApplicationType at
                 WHERE  et.Name = 'SERVICE_GROUP'
                 AND    c.EntityTypeID = et.EntityTypeID
                 AND    COALESCE( c.applicationtypeid, (SELECT applicationtypeid FROM applicationtype WHERE name = 'NAGIOS' ) )  = at.applicationtypeid
		;";

	my $sth = $self->{dbh}->prepare($sql);
	$sth->execute();
	while (my $hashref = $sth->fetchrow_hashref()) {
		foreach my $key (keys %{$hashref}) {
			$servicegroup_ref->{$hashref->{'Service Group'}}->{$key} = $hashref->{$key};
		}
	}
	$sth->finish;
	if (!defined($servicegroup_ref)) {
		return '';
	}
	return \%{$servicegroup_ref};
}

#	return a reference to an array with the names of all servicegroups to which a host service belongs
sub getServiceGroupsForService {
	my $self = shift;
	my $host = shift;
	my $service = shift;
	my @servicegroup_names = ();
	my $quoted_host = $self->{dbh}->quote($host);
	my $quoted_service = $self->{dbh}->quote($service);
	my $sql =
		"select distinct c.name as \"servicegroup\"
		from
		    host h, 
		    servicestatus ss,
		    categoryentity ce,
		    category c,
		    entitytype et
		where
		    h.hostname = $quoted_host
		and ss.hostid = h.hostid
		and ss.servicedescription = $quoted_service
		and ce.objectid = ss.servicestatusid
		and c.categoryid = ce.categoryid
		and et.EntityTypeID = c.entitytypeid
		and et.name = 'SERVICE_GROUP';
		";
	my $sth = $self->{dbh}->prepare($sql);
	$sth->execute();
	while (my @values = $sth->fetchrow_array()) {
	    push(@servicegroup_names, $values[0]);
	}
	$sth->finish;
	return \@servicegroup_names;
}

#	return a hash containing the attributes for a host group
sub getHostGroup {
	my $self = shift;
	my $hgName = shift;
	my $hostgroup_ref = undef;
#	print "Looking for host group $hgName \n";
#	$hostgroup_ref->{"Description"} = "Hostgroup Description";

#	May need to adapt to get Entity Properties for HostGroup
# Test this SQL
#	select pt.Name, ep.ValueDate, ep.ValueString, ep.ValueDouble, ep.ValueBoolean, hg.Name, hg.Description
#	from PropertyType as pt, EntityType as et, EntityProperty as ep, HostGroup as hg where et.Name="HOST_GROUP" and
#	et.PropertyTypeID=pt.PropertyTypeID and hg.Name="Application_1"	;
#
	my $quoted_hg = $self->{dbh}->quote($hgName);
	my $sql = "select
		HostGroupID		as \"HostGroupID\",
		Name			as \"Name\",
		Description		as \"Description\",
		ApplicationTypeID	as \"ApplicationTypeID\",
		Alias			as \"Alias\"
		from HostGroup where Name=$quoted_hg;";
	my $sth = $self->{dbh}->prepare($sql);
	$sth->execute();
	# $hostgroup_ref->{"Hostgroup1"}->{Description} = "Hostgroup1 Description";
	while (my $hashref = $sth->fetchrow_hashref()) {
		foreach my $key (keys %{$hashref}) {
			if ($key =~ /ID$/) { next }
			$hostgroup_ref->{$key} = $hashref->{$key};
		}
	}
	$sth->finish;
	if (!defined($hostgroup_ref)) {
		return '';
	}
	return %{$hostgroup_ref};	# return hash
}

##########################################################
#
# CollageHostQuery class methods
#
##########################################################

#	return an reference to a hash of all services-attributes for a host
sub getServicesForHost {
	my $self = shift;
	my $host = shift;
	my $service_ref=undef;
	my $quoted_host = $self->{dbh}->quote($host);
	my $sql =
		"select
		h.HostName		as \"HostName\",
		ss.ServiceStatusID	as \"ServiceStatusID\",
		ss.ServiceDescription	as \"ServiceDescription\",
		st.Name			as \"StateType\",
		ct.Name			as \"CheckType\",
		ms.Name			as \"MonitorStatus\",
		pt.Name			as \"PropertyType\",
		pt.Description		as \"PropertyDescription\",
		pt.isInteger		as \"PropertyisInteger\",
		pt.isLong		as \"PropertyisLong\",
		pt.isBoolean		as \"PropertyisBoolean\",
		pt.isDate		as \"PropertyisDate\",
		pt.isString		as \"PropertyisString\",
		pt.isDouble		as \"PropertyisDouble\",
		ssp.ValueDate		as \"ServiceStatusDate\",
		ssp.ValueBoolean	as \"ServiceStatusBoolean\",
		ssp.ValueString		as \"ServiceStatusString\",
		ssp.ValueInteger	as \"ServiceStatusInteger\",
		ssp.ValueLong		as \"ServiceStatusLong\",
		ssp.ValueDouble		as \"ServiceStatusDouble\"
		from
		ServiceStatus			as ss,
		ServiceStatusProperty		as ssp,
		EntityType			as et,
		Host				as h,
		StateType			as st,
		CheckType			as ct,
		MonitorStatus			as ms,
		PropertyType			as pt,
		ApplicationType			as at,
		ApplicationEntityProperty	as aep
		where	h.HostName = $quoted_host
		and	ss.HostID = h.HostID
		and	at.ApplicationTypeID = ss.ApplicationTypeID
		and	at.Name = 'NAGIOS'
		and	aep.ApplicationTypeID = at.ApplicationTypeID
		and	et.EntityTypeID = aep.EntityTypeID
		and	et.Name = 'SERVICE_STATUS'
		and	ssp.ServiceStatusID = ss.ServiceStatusID
		and	pt.PropertyTypeID = ssp.PropertyTypeID
		and	pt.PropertyTypeID = aep.PropertyTypeID
		and	st.StateTypeID = ss.StateTypeID
		and	ct.CheckTypeID = ss.CheckTypeID
		and	ms.MonitorStatusID = ss.MonitorStatusID
		;";

	my $sth = $self->{dbh}->prepare($sql);
	$sth->execute();
	# $service_ref->{"Service_1"}->{"attribute"} = "attribute_value";
	while (my $hashref = $sth->fetchrow_hashref()) {
		foreach my $key (keys %{$hashref}) {
			if (($key eq "ServiceDescription") or ($key =~ /ID$/) or ($key eq "PropertyType") or ($key =~/^Propertyis/)) { next }
			if ($key eq "ServiceStatusBoolean") {
				if ($hashref->{PropertyisBoolean}) {
					$service_ref->{$hashref->{ServiceDescription}}->{$hashref->{PropertyType}} = $hashref->{ServiceStatusBoolean};
				}
			} elsif ($key eq "ServiceStatusDate") {
				if ($hashref->{PropertyisDate}) {
					$service_ref->{$hashref->{ServiceDescription}}->{$hashref->{PropertyType}} = $hashref->{ServiceStatusDate};
				}
			} elsif ($key eq "ServiceStatusString") {
				if ($hashref->{PropertyisString}) {
					$service_ref->{$hashref->{ServiceDescription}}->{$hashref->{PropertyType}} = $hashref->{ServiceStatusString};
				}
			} elsif  ($key eq "ServiceStatusDouble") {
				if ($hashref->{PropertyisDouble}) {
					$service_ref->{$hashref->{ServiceDescription}}->{$hashref->{PropertyType}} = $hashref->{ServiceStatusDouble};
				}
			} elsif ($key eq "ServiceStatusInteger") {
				if ($hashref->{PropertyisInteger}) {
					$service_ref->{$hashref->{ServiceDescription}}->{$hashref->{PropertyType}} = $hashref->{ServiceStatusInteger};
				}
			} elsif ($key eq "ServiceStatusLong")  {
				if ($hashref->{PropertyisLong}) {
					$service_ref->{$hashref->{ServiceDescription}}->{$hashref->{PropertyType}} = $hashref->{ServiceStatusLong};
				}
			} else {
				$service_ref->{$hashref->{ServiceDescription}}->{$key} = $hashref->{$key};
			}
		}
	}
	$sth->finish;
	if (!defined($service_ref)) {
		return '';
	}
	return \%{$service_ref};
}

##########################################################
#
# CollageHostQuery class methods
#
##########################################################

#	return a reference to an array with all services names for a host
sub getServiceNamesForHost {
	my $self = shift;
	my $host = shift;
	my @service_names = ();
	my $quoted_host = $self->{dbh}->quote($host);
	my $sql =
		"select ServiceDescription
		from	Host h, ServiceStatus ss
		where	h.HostName = $quoted_host
		and	ss.HostID = h.HostID;
		";
	my $sth = $self->{dbh}->prepare($sql);
	$sth->execute();
	while (my @values = $sth->fetchrow_array()) {
	    push(@service_names, $values[0]);
	}
	$sth->finish;
	return \@service_names;
}

#	This routine is supported for backward compatibility.
#	Return a reference to a hash of all host-attributes, or an empty string.
sub getHosts {
	my $self = shift;
	return $self->getHostsByType('NAGIOS');
}

#	Return a reference to a hash of all host-attributes, or an empty string.
sub getHostsByType {
	my $self            = shift;
	my $applicationType = shift;
	my $host_ref        = undef;
	my $app_type_condition;
	if ($applicationType) {
	    my $quoted_appType  = $self->{dbh}->quote($applicationType);
	    $app_type_condition = "and at.Name = $quoted_appType";
	}
	else {
	    $app_type_condition = '';
	}
	# There is an implicit logical join here to the EntityType table as well:
	#   EntityType as et,
	#   and et.EntityTypeID = aep.EntityTypeID
	#   and et.Name = 'HOST_STATUS'
	# but actually doing so would be redundant, considering we already have
	# the join to the HostStatusProperty table hardcoded.
	my $sql =
		"select
		h.HostName		as \"HostName\",
		at.Name			as \"ApplicationType\",
		ms.Name			as \"MonitorStatus\",
		hs.LastCheckTime	as \"LastCheckTime\",
		hs.NextCheckTime	as \"NextCheckTime\",
		st.Name			as \"StateType\",
		pt.Name			as \"PropertyType\",
		pt.Description		as \"PropertyDescription\",
		pt.isInteger		as \"PropertyisInteger\",
		pt.isLong		as \"PropertyisLong\",
		pt.isBoolean		as \"PropertyisBoolean\",
		pt.isDate		as \"PropertyisDate\",
		pt.isString		as \"PropertyisString\",
		pt.isDouble		as \"PropertyisDouble\",
		hsp.ValueInteger	as \"HostStatusInteger\",
		hsp.ValueLong		as \"HostStatusLong\",
		hsp.ValueBoolean	as \"HostStatusBoolean\",
		hsp.ValueDate		as \"HostStatusDate\",
		hsp.ValueString		as \"HostStatusString\",
		hsp.ValueDouble		as \"HostStatusDouble\"
		from
		PropertyType			as pt,
		ApplicationType			as at,
		ApplicationEntityProperty	as aep,
		HostStatusProperty		as hsp,
		HostStatus			as hs,
		Host				as h,
		MonitorStatus			as ms,
		StateType			as st
		where	hs.HostStatusID = h.HostID
		and	hsp.HostStatusID = hs.HostStatusID
		and	pt.PropertyTypeID = hsp.PropertyTypeID
		and	aep.PropertyTypeID = pt.PropertyTypeID
		and	at.ApplicationTypeID = aep.ApplicationTypeID
		$app_type_condition
		and	st.StateTypeID = hs.StateTypeID
		and	ms.MonitorStatusID = hs.MonitorStatusID;
		";
	my $sth = $self->{dbh}->prepare($sql);
	$sth->execute();
	if (!$main::shutdown_requested) {
		while (my $hashref = $sth->fetchrow_hashref()) {
			if ($main::shutdown_requested) {
				$host_ref = undef;
				last;
			}
			foreach my $key (keys %{$hashref}) {
				if (($key eq "HostName") or ($key =~ /ID$/) or ($key eq "PropertyType") or ($key =~/^Propertyis/)) { next }
				if ($key eq "HostStatusBoolean") {
					if ($hashref->{PropertyisBoolean}) {
						$host_ref->{$hashref->{HostName}}->{$hashref->{PropertyType}} = $hashref->{HostStatusBoolean};
					}
				} elsif ($key eq "HostStatusDate") {
					if ($hashref->{PropertyisDate}) {
						$host_ref->{$hashref->{HostName}}->{$hashref->{PropertyType}} = $hashref->{HostStatusDate};
					}
				} elsif ($key eq "HostStatusString") {
					if ($hashref->{PropertyisString}) {
						$host_ref->{$hashref->{HostName}}->{$hashref->{PropertyType}} = $hashref->{HostStatusString};
					}
				} elsif ($key eq "HostStatusDouble") {
					if ($hashref->{PropertyisDouble}) {
						$host_ref->{$hashref->{HostName}}->{$hashref->{PropertyType}} = $hashref->{HostStatusDouble};
					}
				} elsif ($key eq "HostStatusInteger") {
					if ($hashref->{PropertyisInteger}) {
						$host_ref->{$hashref->{HostName}}->{$hashref->{PropertyType}} = $hashref->{HostStatusInteger};
					}
				} elsif ($key eq "HostStatusLong")  {
					if ($hashref->{PropertyisLong}) {
						$host_ref->{$hashref->{HostName}}->{$hashref->{PropertyType}} = $hashref->{HostStatusLong};
					}
				} else {
					$host_ref->{$hashref->{HostName}}->{$key} = $hashref->{$key};
				}
			}
		}
	}
	$sth->finish;
	if (!defined($host_ref)) {
		return '';
	}
	return $host_ref;
}

#	Return a reference to a hash of all hosts of a given application type,
#	or an undefined value if the retrieval was aborted.
sub getHostTypes {
	my $self            = shift;
	my $applicationType = shift;
	my $host_ref        = {};
	## $host_ref->{"Host_A"} = "application_type";
	my $app_type_condition;
	if ($applicationType) {
	    my $quoted_appType  = $self->{dbh}->quote($applicationType);
	    $app_type_condition = "and at.Name = $quoted_appType";
	}
	else {
	    $app_type_condition = '';
	}
	my $sql =
		"select
		h.HostName	as \"HostName\",
		at.Name		as \"ApplicationType\"
		from
		Host		as h,
		ApplicationType	as at
		where	at.ApplicationTypeID = h.ApplicationTypeID
		$app_type_condition;
		";
	my $sth = $self->{dbh}->prepare($sql);
	$sth->execute();
	if ($main::shutdown_requested) {
		$host_ref = undef;
	}
	else {
		my @values = ();
		while (@values = $sth->fetchrow_array()) {
			if ($main::shutdown_requested) {
				$host_ref = undef;
				last;
			}
			$host_ref->{$values[0]} = $values[1];
		}
	}
	$sth->finish;
	return $host_ref;
}

#	Return a reference to a hash of all host services of a given application type,
#	or an undefined value if the retrieval was aborted.
sub getHostServiceTypes {
	my $self            = shift;
	my $applicationType = shift;
	my $service_ref     = {};
	## $service_ref->{"Host_A"}->{"Service_2"} = "application_type";
	my $app_type_condition;
	if ($applicationType) {
	    my $quoted_appType  = $self->{dbh}->quote($applicationType);
	    $app_type_condition = "and at.Name = $quoted_appType";
	}
	else {
	    $app_type_condition = '';
	}
	my $sql =
		"select
		h.HostName		as \"HostName\",
		ss.ServiceDescription	as \"ServiceDescription\",
		at.Name			as \"ApplicationType\"
		from
		Host			as h,
		ServiceStatus		as ss,
		ApplicationType		as at
		where	ss.HostID = h.HostID
		and	at.ApplicationTypeID = ss.ApplicationTypeID
		$app_type_condition;
		";
	my $sth = $self->{dbh}->prepare($sql);

	# Note:  $sth->execute() can take a long time to run, but it is effectively
	# impossible to cleanly interrupt and cut short that time, even with a signal
	# handler in place.  See the DBI documentation for details.  The DBD::mysql
	# driver just continues on after receiving EINTR on one of the system calls it
	# makes, rather than recognizing it as a valid attempt to abort the operation.
	# And the driver does not implement $sth->cancel().  Thus, trying to wrap the
	# call to $sth->execute() in an eval{} and having the signal handler die()
	# from within the eval{}, or call $sth->cancel(), just does not work.  An
	# incoming signal is not recognized until sth->execute() has finished running,
	# which defeats the purpose of trying to use eval{} to abort early.
	$sth->execute();
	if ($main::shutdown_requested) {
		$service_ref = undef;
	}
	else {
		my @values = ();
		while (@values = $sth->fetchrow_array()) {
			if ($main::shutdown_requested) {
				$service_ref = undef;
				last;
			}
			$service_ref->{$values[0]}->{$values[1]} = $values[2];
		}
	}
	$sth->finish;
	return $service_ref;
}

# Unstable interface, subject to change across releases.
#	return a reference to a hash of all hosts, or an empty string
sub getSyncHosts {
    my $self     = shift;
    my $host_ref = undef;
    my @values   = undef;
    my @errors   = ();
    my $sql;
    my $sth;

    # Because we might have a Monarch-managed host with no associated services that is owned in Foundation by
    # some non-NAGIOS application type, we cannot accomplish all the filtering we might want to at this level.
    # We must instead return data for all hosts to the caller, and allow it to apply appropriate filtering.
    # Services, on the other hand, will always be owned in Foundation by the NAGIOS application type if they
    # are managed by Monarch, so we can apply that filtering here.

    # FIX LATER:  what are the rules for join associativity and precedence?
    $sql = "select h.HostName, d.Identification, at.Name
	    from ApplicationType at join Host h using (ApplicationTypeID) left join Device d using (DeviceID);";
    $sth = $self->{dbh}->prepare($sql);
    if ( !$sth->execute() ) {
	push @errors, $sth->errstr;
    }
    else {
	while ( @values = $sth->fetchrow_array() ) {
	    $host_ref->{ $values[0] }{'Identification'}  = $values[1];
	    $host_ref->{ $values[0] }{'ApplicationType'} = $values[2];
	}
    }
    $sth->finish;

    my %OutsideName = (
	Alias  => 'Alias',
	Notes  => 'HostNotes',
	Parent => 'Parents'
    );

    foreach my $ptname (keys %OutsideName) {
	unless (@errors) {
	    $sql = "select h.HostName, hsp.ValueString
		    from PropertyType pt, HostStatusProperty hsp, Host h
		    where	pt.Name = '$ptname'
		    and		hsp.PropertyTypeID = pt.PropertyTypeID
		    and		h.HostID = hsp.HostStatusID;";
	    $sth = $self->{dbh}->prepare($sql);
	    if ( !$sth->execute() ) {
		push @errors, $sth->errstr;
	    }
	    else {
		while ( @values = $sth->fetchrow_array() ) {
		    $host_ref->{ $values[0] }{ $OutsideName{$ptname} } = $values[1];
		}
	    }
	    $sth->finish;
	}
    }

    unless (@errors) {
	$sql = "select h.HostName, ss.ServiceDescription
		from ApplicationType at, ServiceStatus ss, Host h
		where	at.Name = 'NAGIOS'
		and	ss.ApplicationTypeID = at.ApplicationTypeID
		and	h.HostID = ss.HostID;";
	$sth = $self->{dbh}->prepare($sql);
	if ( !$sth->execute() ) {
	    push @errors, $sth->errstr;
	}
	else {
	    while ( @values = $sth->fetchrow_array() ) {
		push @{ $host_ref->{ $values[0] }{'Services'} }, $values[1];
	    }
	}
	$sth->finish;
    }

    unless (@errors) {
	$sql = "select h.HostName, ss.ServiceDescription, ssp.ValueString
		from ApplicationType at, ServiceStatus ss, Host h, ServiceStatusProperty ssp, PropertyType pt
		where	at.Name = 'NAGIOS'
		and	ss.ApplicationTypeID = at.ApplicationTypeID
		and	h.HostID = ss.HostID
		and	ssp.ServiceStatusID = ss.ServiceStatusID
		and	pt.PropertyTypeID = ssp.PropertyTypeID
		and	pt.Name='Notes';";
	$sth = $self->{dbh}->prepare($sql);
	if ( !$sth->execute() ) {
	    push @errors, $sth->errstr;
	}
	else {
	    while ( @values = $sth->fetchrow_array() ) {
		$host_ref->{ $values[0] }{'ServiceNotes'}{ $values[1] } = $values[2] if defined $values[2];
	    }
	}
	$sth->finish;
    }

    if (!defined($host_ref)) {
	return \@errors, '';
    }
    return \@errors, $host_ref;
}

#	return a hash of all the status attributes for a host
sub getHostStatusForHost {
	my $self = shift;
	my $host = shift;
	my $host_ref = undef;
	# $host_ref->{"attribute"} = "attribute_value";
	my $quoted_host = $self->{dbh}->quote($host);
	my $sql =
		"select
		h.HostName		as \"HostName\",
		ms.Name			as \"MonitorStatus\",
		hs.LastCheckTime	as \"LastCheckTime\",
		hs.NextCheckTime	as \"NextCheckTime\",
		st.Name			as \"StateType\",
		pt.Name			as \"PropertyType\",
		pt.Description		as \"PropertyDescription\",
		pt.isInteger		as \"PropertyisInteger\",
		pt.isLong		as \"PropertyisLong\",
		pt.isBoolean		as \"PropertyisBoolean\",
		pt.isDate		as \"PropertyisDate\",
		pt.isString		as \"PropertyisString\",
		pt.isDouble		as \"PropertyisDouble\",
		hsp.ValueDate		as \"HostStatusDate\",
		hsp.ValueBoolean	as \"HostStatusBoolean\",
		hsp.ValueString		as \"HostStatusString\",
		hsp.ValueInteger	as \"HostStatusInteger\",
		hsp.ValueLong		as \"HostStatusLong\",
		hsp.ValueDouble		as \"HostStatusDouble\"
		from
		PropertyType			as pt,
		HostStatusProperty		as hsp,
		HostStatus			as hs,
		Host				as h,
		MonitorStatus			as ms,
		StateType			as st
		where	h.HostName = $quoted_host
		and	hs.HostStatusID = h.HostID
		and	ms.MonitorStatusID = hs.MonitorStatusID
		and	st.StateTypeID = hs.StateTypeID
		and	hsp.HostStatusID = hs.HostStatusID
		and	pt.PropertyTypeID = hsp.PropertyTypeID;
		";
	my $sth = $self->{dbh}->prepare($sql);
	$sth->execute();
	while (my $hashref = $sth->fetchrow_hashref()) {
		foreach my $key (keys %{$hashref}) {
			if (($key =~ /ID$/) or ($key eq "PropertyType")  or ($key =~/^Propertyis/)) { next }
			if ($key eq "HostStatusBoolean") {
				if ($hashref->{PropertyisBoolean}) {
					$host_ref->{$hashref->{PropertyType}} = $hashref->{HostStatusBoolean};
				}
			} elsif ($key eq "HostStatusDate") {
				if ($hashref->{PropertyisDate}) {
					$host_ref->{$hashref->{PropertyType}} = $hashref->{HostStatusDate};
				}
			} elsif ($key eq "HostStatusString") {
				if ($hashref->{PropertyisString}) {
					$host_ref->{$hashref->{PropertyType}} = $hashref->{HostStatusString};
				}
			} elsif ($key eq "HostStatusDouble") {
				if ($hashref->{PropertyisDouble}) {
					$host_ref->{$hashref->{PropertyType}} = $hashref->{HostStatusDouble};
				}
			} elsif ($key eq "HostStatusInteger") {
				if ($hashref->{PropertyisInteger}) {
					$host_ref->{$hashref->{PropertyType}} = $hashref->{HostStatusInteger};
				}
			} elsif ($key eq "HostStatusLong")  {
				if ($hashref->{PropertyisLong}) {
					$host_ref->{$hashref->{PropertyType}} = $hashref->{HostStatusLong};
				}
			} else {
				$host_ref->{$key} = $hashref->{$key};
			}
		}
	}
	$sth->finish;
	if (!defined($host_ref)) {
		return '';
	}
	return %{$host_ref};
}

# return a hash of the device attributes for a host, except for the DeviceID field
sub getDeviceForHost {
	my $self = shift;
	my $host = shift;
	my $device_ref = undef;
	# $device_ref->{DisplayName}    = "Display Name";
	# $device_ref->{Identification} = "Identification";
	# $device_ref->{Description}    = "Description";
	my $quoted_host = $self->{dbh}->quote($host);
	my $sql = "select
		d.DisplayName		as \"DisplayName\",
		d.Identification	as \"Identification\",
		d.Description		as \"Description\"
		from Host as h, Device as d
		where h.HostName = $quoted_host and d.DeviceID = h.DeviceID;";
	my $sth = $self->{dbh}->prepare($sql);
	$sth->execute();
	while (my $hashref = $sth->fetchrow_hashref()) {
		foreach my $key (keys %{$hashref}) {
			$device_ref->{$key} = $hashref->{$key};
		}
	}
	$sth->finish;
	if (!defined($device_ref)) {
		return '';
	}
	return %{$device_ref};
}

# return a hashref to the device attributes (except for the DeviceID fields)
# for all hosts, or undef if there are no hosts
sub getDevicesForHosts {
	my $self = shift;
	my $device_ref = undef;
	# $device_ref->{$hostname}{DisplayName}    = "Display Name";
	# $device_ref->{$hostname}{Identification} = "Identification";
	# $device_ref->{$hostname}{Description}    = "Description";
	my $sql = "select
		h.HostName		as \"HostName\",
		d.DisplayName		as \"DisplayName\",
		d.Identification	as \"Identification\",
		d.Description		as \"Description\"
		from Host as h, Device as d
		where d.DeviceID = h.DeviceID;";
	my $sth = $self->{dbh}->prepare($sql);
	$sth->execute();
	my $hostname;
	while (my $hashref = $sth->fetchrow_hashref()) {
		$hostname = $hashref->{HostName};
		foreach my $key (keys %{$hashref}) {
			next if $key eq 'HostName';
			$device_ref->{$hostname}{$key} = $hashref->{$key};
		}
	}
	$sth->finish;
	return $device_ref;
}

##########################################################
#
# CollageEventQuery class methods
#		Note: timeField (String) can be FirstInsertDate or LastInsertDate if it's null no range will be applied
#		Date in DATE format: YYYY-MM-DD hh:mm:ss ??????
#
##########################################################

#	Return a reference to a hash of events. The event ID is the primary key
sub getEventsbyDate {
	my $self = shift;
	my $timeField = shift;
	my $fromDate = shift;
	my $toDate = shift;
	my $applicationType=shift;
	my $event_ref = undef;
	# $event_ref->{id}->{Severity} = '';
	if (!$applicationType) {
		$applicationType = 'NAGIOS';
	}
	if ($timeField !~ /(LastInsertDate|FirstInsertDate|ReportDate)/) {
		return "Invalid time field $timeField";
	}
	if ($fromDate !~ /\d\d\d\d-\d\d-\d\d \d\d:\d\d:\d\d/) {
		return "Invalid from-date field $fromDate";
	}
	if ($toDate !~ /\d\d\d\d-\d\d-\d\d \d\d:\d\d:\d\d/) {
		return "Invalid to-date field $toDate";
	}
	my $quoted_fromDate = $self->{dbh}->quote($fromDate);
	my $quoted_toDate   = $self->{dbh}->quote($toDate);
	my $sql =
		"select
		lm.LogMessageID		as \"LogMessageID\",
		lm.TextMessage		as \"TextMessage\",
		lm.MsgCount		as \"MsgCount\",
		lm.FirstInsertDate	as \"FirstInsertDate\",
		lm.LastInsertDate	as \"LastInsertDate\",
		lm.ReportDate		as \"ReportDate\",
		h.HostName		as \"HostName\",
		ms.Name			as \"MonitorStatus\",
		sev.Name		as \"Severity\",
		at.Name			as \"ApplicationType\",
		c.Name			as \"Component\",
		d.Identification	as \"DeviceIdentification\",
		t.Name			as \"TypeRule\",
		p.Name			as \"Priority\",
		os.Name			as \"OperationStatus\"
		from
		LogMessage	as lm,
		ApplicationType	as at,
		Severity	as sev,
		Component	as c,
		Device		as d,
		MonitorStatus	as ms,
		Host		as h,
		OperationStatus	as os,
		Priority	as p,
		TypeRule	as t
		where
		lm.$timeField >= $quoted_fromDate and
		lm.$timeField <= $quoted_toDate and
		lm.SeverityID = sev.SeverityID and
		lm.PriorityID = p.PriorityID and
		lm.ComponentID = c.ComponentID and
		lm.ApplicationTypeID = at.ApplicationTypeID and
		lm.DeviceID = d.DeviceID and
		lm.MonitorStatusID = ms.MonitorStatusID and
		lm.OperationStatusID = os.OperationStatusID and
		lm.TypeRuleID = t.TypeRuleID;
		";

	my $sth = $self->{dbh}->prepare($sql);
	$sth->execute();
	while (my $hashref = $sth->fetchrow_hashref()) {
		foreach my $key (keys %{$hashref}) {
			$event_ref->{$hashref->{LogMessageID}}->{$key} = $hashref->{$key};
		}
	}
	$sth->finish;
	if (!defined($event_ref)) {
		return '';
	}
	return \%{$event_ref};
}

sub getEventsbyDate_TEST {
	my $self = shift;
	my $timeField = shift;
	my $fromDate = shift;
	my $toDate = shift;
	my $applicationType=shift;
	my $event_ref = undef;
	# $event_ref->{id}->{Severity} = '';
	if (!$applicationType) {
		$applicationType = 'NAGIOS';
	}
	if ($timeField !~ /(LastInsertDate|FirstInsertDate|ReportDate)/) {
		return "Invalid time field $timeField";
	}
	if ($fromDate !~ /\d\d\d\d-\d\d-\d\d \d\d:\d\d:\d\d/) {
		return "Invalid from-date field $fromDate";
	}
	if ($toDate !~ /\d\d\d\d-\d\d-\d\d \d\d:\d\d:\d\d/) {
		return "Invalid to-date field $toDate";
	}
	my $quoted_appType  = $self->{dbh}->quote($applicationType);
	my $quoted_fromDate = $self->{dbh}->quote($fromDate);
	my $quoted_toDate   = $self->{dbh}->quote($toDate);
	my $sql =
		"select
		lm.LogMessageID			as \"LogMessageID\",
		lm.ApplicationTypeID		as \"ApplicationTypeID\",
		lm.DeviceID			as \"DeviceID\",
		lm.HostStatusID			as \"HostStatusID\",
		lm.ServiceStatusID		as \"ServiceStatusID\",
		lm.TextMessage			as \"TextMessage\",
		lm.MsgCount			as \"MsgCount\",
		lm.FirstInsertDate		as \"FirstInsertDate\",
		lm.LastInsertDate		as \"LastInsertDate\",
		lm.ReportDate			as \"ReportDate\",
		lm.MonitorStatusID		as \"MonitorStatusID\",
		lm.SeverityID			as \"SeverityID\",
		lm.ApplicationSeverityID	as \"ApplicationSeverityID\",
		lm.PriorityID			as \"PriorityID\",
		lm.TypeRuleID			as \"TypeRuleID\",
		lm.ComponentID			as \"ComponentID\",
		lm.OperationStatusID		as \"OperationStatusID\",
		lm.isStateChanged		as \"isStateChanged\",
		lm.ConsolidationHash		as \"ConsolidationHash\",
		lm.StatelessHash		as \"StatelessHash\",
		lm.StateTransitionHash		as \"StateTransitionHash\",
		at.Name				as \"ApplicationTypeName\",
		et.Name				as \"EntityName\",
		pt.Name				as \"PropertyType\",
		pt.isInteger			as \"PropertyisInteger\",
		pt.isLong			as \"PropertyisLong\",
		pt.isBoolean			as \"PropertyisBoolean\",
		pt.isDate			as \"PropertyisDate\",
		pt.isString			as \"PropertyisString\",
		pt.isDouble			as \"PropertyisDouble\",
		lmp.ValueDate			as \"LogMessageDate\",
		lmp.ValueBoolean		as \"LogMessageBoolean\",
		lmp.ValueString			as \"LogMessageString\",
		lmp.ValueInteger		as \"LogMessageInteger\",
		lmp.ValueLong			as \"LogMessageLong\",
		lmp.ValueDouble			as \"LogMessageDouble\",
		ms.Name				as \"MonitorStatus\",
		sev.Name			as \"Severity\",
		appsev.Name			as \"ApplicationSeverity\",
		c.Name				as \"Component\",
		d.Identification		as \"DeviceIdentification\",
		os.Name				as \"OperationStatus\",
		h.HostName			as \"HostName\"
		from
		LogMessage			as lm,
		EntityType			as et,
		ApplicationEntityProperty	as aep,
		ApplicationType			as at,
		PropertyType			as pt,
		LogMessageProperty		as lmp,
		Severity			as sev,
		Severity			as appsev,
		Priority			as pri,
		Component			as c,
		Device				as d,
		MonitorStatus			as ms,
		ServiceStatus			as s,
		Host				as h,
		HostStatus			as hs,
		OperationStatus			as os,
		TypeRule			as t
		where
		at.Name=$quoted_appType and
		et.Name='LOG_MESSAGE' and
		aep.EntityTypeID=et.EntityTypeID and
		aep.ApplicationTypeID=at.ApplicationTypeID and
		at.ApplicationTypeID=lm.ApplicationTypeID and
		pt.PropertyTypeID=aep.PropertyTypeID and
		pt.PropertyTypeID=lmp.PropertyTypeID and
		lmp.LogMessageID=lm.logMessageID and
		lm.$timeField>=$quoted_fromDate and
		lm.$timeField<=$quoted_toDate and
		lm.SeverityID=sev.SeverityID and
		lm.ApplicationSeverityID=appsev.SeverityID and
		lm.PriorityID=pri.PriorityID and
		lm.ComponentID=c.ComponentID and
		lm.DeviceID=d.DeviceID and
		lm.MonitorStatusID=ms.MonitorStatusID and
		lm.OperationStatusID=os.OperationStatusID and
		lm.TypeRuleID=t.TypeRuleID and
		lm.HostStatusID=hs.HostStatusID and
		hs.HostStatusID=h.HostID;
		";

	my $sth = $self->{dbh}->prepare($sql);
	$sth->execute();
	while (my $hashref = $sth->fetchrow_hashref()) {
		foreach my $key (keys %{$hashref}) {
			if (($key =~ /ID$/) or ($key eq "PropertyType")  or ($key =~/^Propertyis/))  { next }
			if ($key eq "LogMessageBoolean") {
				if ($hashref->{PropertyisBoolean}) {
					$event_ref->{$hashref->{LogMessageID}}->{$hashref->{PropertyType}} = $hashref->{LogMessageBoolean};
				}
			} elsif ($key eq "LogMessageDate") {
				if ($hashref->{PropertyisDate}) {
					$event_ref->{$hashref->{LogMessageID}}->{$hashref->{PropertyType}} = $hashref->{LogMessageDate};
				}
			} elsif ($key eq "LogMessageString") {
				if ($hashref->{PropertyisString}) {
					$event_ref->{$hashref->{LogMessageID}}->{$hashref->{PropertyType}} = $hashref->{LogMessageString};
				}
			} elsif  ($key eq "LogMessageDouble") {
				if ($hashref->{PropertyisDouble}) {
					$event_ref->{$hashref->{LogMessageID}}->{$hashref->{PropertyType}} = $hashref->{LogMessageDouble};
				}
			} elsif ($key eq "LogMessageInteger") {
				if ($hashref->{PropertyisInteger}) {
					$event_ref->{$hashref->{LogMessageID}}->{$hashref->{PropertyType}} = $hashref->{LogMessageInteger};
				}
			} elsif ($key eq "LogMessageLong")  {
				if ($hashref->{PropertyisLong}) {
					$event_ref->{$hashref->{LogMessageID}}->{$hashref->{PropertyType}} = $hashref->{LogMessageLong};
				}
			} else {
				$event_ref->{$hashref->{LogMessageID}}->{$key} = $hashref->{$key};
			}
		}
	}
	$sth->finish;
	if (!defined($event_ref)) {
		return '';
	}
	return \%{$event_ref};
}

sub getEventsForDevice {
	my $self = shift;
	my $identification = shift;
	my $timeField = shift;
	my $fromDate = shift;
	my $toDate = shift;
	my $applicationType = shift;
	my $event_ref = undef;
	# $event_ref->{id}->{Severity} = '';
	if (!$applicationType) {
		$applicationType = 'NAGIOS';
	}
	if ($timeField !~ /(LastInsertDate|FirstInsertDate|ReportDate)/) {
		return "Invalid time field $timeField";
	}
	if ($fromDate !~ /\d\d\d\d-\d\d-\d\d \d\d:\d\d:\d\d/) {
		return "Invalid from-date field $fromDate";
	}
	if ($toDate !~ /\d\d\d\d-\d\d-\d\d \d\d:\d\d:\d\d/) {
		return "Invalid to-date field $toDate";
	}
	my $quoted_ident    = $self->{dbh}->quote($identification);
	my $quoted_appType  = $self->{dbh}->quote($applicationType);
	my $quoted_fromDate = $self->{dbh}->quote($fromDate);
	my $quoted_toDate   = $self->{dbh}->quote($toDate);
	my $sql =
		"select
		lm.LogMessageID			as \"LogMessageID\",
		lm.ApplicationTypeID		as \"ApplicationTypeID\",
		lm.DeviceID			as \"DeviceID\",
		lm.HostStatusID			as \"HostStatusID\",
		lm.ServiceStatusID		as \"ServiceStatusID\",
		lm.TextMessage			as \"TextMessage\",
		lm.MsgCount			as \"MsgCount\",
		lm.FirstInsertDate		as \"FirstInsertDate\",
		lm.LastInsertDate		as \"LastInsertDate\",
		lm.ReportDate			as \"ReportDate\",
		lm.MonitorStatusID		as \"MonitorStatusID\",
		lm.SeverityID			as \"SeverityID\",
		lm.ApplicationSeverityID	as \"ApplicationSeverityID\",
		lm.PriorityID			as \"PriorityID\",
		lm.TypeRuleID			as \"TypeRuleID\",
		lm.ComponentID			as \"ComponentID\",
		lm.OperationStatusID		as \"OperationStatusID\",
		lm.isStateChanged		as \"isStateChanged\",
		lm.ConsolidationHash		as \"ConsolidationHash\",
		lm.StatelessHash		as \"StatelessHash\",
		lm.StateTransitionHash		as \"StateTransitionHash\",
		at.Name				as \"ApplicationTypeName\",
		et.Name				as \"EntityName\",
		pt.Name				as \"PropertyType\",
		pt.isInteger			as \"PropertyisInteger\",
		pt.isLong			as \"PropertyisLong\",
		pt.isBoolean			as \"PropertyisBoolean\",
		pt.isDate			as \"PropertyisDate\",
		pt.isString			as \"PropertyisString\",
		pt.isDouble			as \"PropertyisDouble\",
		lmp.ValueDate			as \"LogMessageDate\",
		lmp.ValueBoolean		as \"LogMessageBoolean\",
		lmp.ValueString			as \"LogMessageString\",
		lmp.ValueInteger		as \"LogMessageInteger\",
		lmp.ValueLong			as \"LogMessageLong\",
		lmp.ValueDouble			as \"LogMessageDouble\",
		ms.Name				as \"MonitorStatus\",
		sev.Name			as \"Severity\",
		appsev.Name			as \"ApplicationSeverity\",
		c.Name				as \"Component\",
		d.Identification		as \"DeviceIdentification\",
		os.Name				as \"OperationStatus\"
		from
		LogMessage			as lm,
		EntityType			as et,
		ApplicationEntityProperty	as aep,
		ApplicationType			as at,
		PropertyType			as pt,
		LogMessageProperty		as lmp,
		Severity			as sev,
		Severity			as appsev,
		Priority			as pri,
		Component			as c,
		Device				as d,
		MonitorStatus			as ms,
		OperationStatus			as os,
		TypeRule			as t
		where
		d.Identification=$quoted_ident and
		at.Name=$quoted_appType and
		et.Name='LOG_MESSAGE' and
		aep.EntityTypeID=et.EntityTypeID and
		aep.ApplicationTypeID=at.ApplicationTypeID and
		at.ApplicationTypeID=lm.ApplicationTypeID and
		pt.PropertyTypeID=aep.PropertyTypeID and
		pt.PropertyTypeID=lmp.PropertyTypeID and
		lmp.LogMessageID=lm.logMessageID and
		lm.$timeField>=$quoted_fromDate and
		lm.$timeField<=$quoted_toDate and
		lm.SeverityID=sev.SeverityID and
		lm.ApplicationSeverityID=appsev.SeverityID and
		lm.PriorityID=pri.PriorityID and
		lm.ComponentID=c.ComponentID and
		lm.DeviceID=d.DeviceID and
		lm.MonitorStatusID=ms.MonitorStatusID and
		lm.OperationStatusID=os.OperationStatusID and
		lm.TypeRuleID=t.TypeRuleID
		";

#	No host relationship for devices
# 		ServiceStatus as s,
#		Host as h,
#		HostStatus as hs,
#		lm.HostStatusID=hs.HostStatusID and
#		hs.HostStatusID=h.HostID;
#		h.HostName,
#		s.ServiceDescription,
#		pt.Description as PropertyDescription,
#		lm.ServiceStatusID=s.ServiceStatusID and
#		lm.HostStatusID=hs.HostStatusID and

	my $sth = $self->{dbh}->prepare($sql);
	$sth->execute();
	while (my $hashref = $sth->fetchrow_hashref()) {
		foreach my $key (keys %{$hashref}) {
			if (($key =~ /ID$/) or ($key eq "PropertyType")  or ($key =~/^Propertyis/))  { next }
			if ($key eq "LogMessageBoolean") {
				if ($hashref->{PropertyisBoolean}) {
					$event_ref->{$hashref->{LogMessageID}}->{$hashref->{PropertyType}} = $hashref->{LogMessageBoolean};
				}
			} elsif ($key eq "LogMessageDate") {
				if ($hashref->{PropertyisDate}) {
					$event_ref->{$hashref->{LogMessageID}}->{$hashref->{PropertyType}} = $hashref->{LogMessageDate};
				}
			} elsif ($key eq "LogMessageString") {
				if ($hashref->{PropertyisString}) {
					$event_ref->{$hashref->{LogMessageID}}->{$hashref->{PropertyType}} = $hashref->{LogMessageString};
				}
			} elsif  ($key eq "LogMessageDouble") {
				if ($hashref->{PropertyisDouble}) {
					$event_ref->{$hashref->{LogMessageID}}->{$hashref->{PropertyType}} = $hashref->{LogMessageDouble};
				}
			} elsif ($key eq "LogMessageInteger") {
				if ($hashref->{PropertyisInteger}) {
					$event_ref->{$hashref->{LogMessageID}}->{$hashref->{PropertyType}} = $hashref->{LogMessageInteger};
				}
			} elsif ($key eq "LogMessageLong")  {
				if ($hashref->{PropertyisLong}) {
					$event_ref->{$hashref->{LogMessageID}}->{$hashref->{PropertyType}} = $hashref->{LogMessageLong};
				}
			} else {
				$event_ref->{$hashref->{LogMessageID}}->{$key} = $hashref->{$key};
			}
		}
	}
	$sth->finish;
	if (!defined($event_ref)) {
		return '';
	}
	return \%{$event_ref};
}

sub getEventsForService {
	my $self = shift;
	my $hostName = shift;
	my $serviceDescription = shift;
	my $timeField = shift;
	my $fromDate = shift;
	my $toDate = shift;
	my $applicationType = shift;
	my $event_ref = undef;
	# $event_ref->{id}->{Severity} = '';
	if (!$applicationType) {
		$applicationType = 'NAGIOS';
	}
	if ($timeField !~ /(LastInsertDate|FirstInsertDate|ReportDate)/) {
		return "Invalid time field $timeField";
	}
	if ($fromDate !~ /\d\d\d\d-\d\d-\d\d \d\d:\d\d:\d\d/) {
		return "Invalid from-date field $fromDate";
	}
	if ($toDate !~ /\d\d\d\d-\d\d-\d\d \d\d:\d\d:\d\d/) {
		return "Invalid to-date field $toDate";
	}
	my $quoted_host     = $self->{dbh}->quote($hostName);
	my $quoted_service  = $self->{dbh}->quote($serviceDescription);
	my $quoted_appType  = $self->{dbh}->quote($applicationType);
	my $quoted_fromDate = $self->{dbh}->quote($fromDate);
	my $quoted_toDate   = $self->{dbh}->quote($toDate);
	my $sql =
		"select
		lm.LogMessageID			as \"LogMessageID\",
		lm.ApplicationTypeID		as \"ApplicationTypeID\",
		lm.DeviceID			as \"DeviceID\",
		lm.HostStatusID			as \"HostStatusID\",
		lm.ServiceStatusID		as \"ServiceStatusID\",
		lm.TextMessage			as \"TextMessage\",
		lm.MsgCount			as \"MsgCount\",
		lm.FirstInsertDate		as \"FirstInsertDate\",
		lm.LastInsertDate		as \"LastInsertDate\",
		lm.ReportDate			as \"ReportDate\",
		lm.MonitorStatusID		as \"MonitorStatusID\",
		lm.SeverityID			as \"SeverityID\",
		lm.ApplicationSeverityID	as \"ApplicationSeverityID\",
		lm.PriorityID			as \"PriorityID\",
		lm.TypeRuleID			as \"TypeRuleID\",
		lm.ComponentID			as \"ComponentID\",
		lm.OperationStatusID		as \"OperationStatusID\",
		lm.isStateChanged		as \"isStateChanged\",
		lm.ConsolidationHash		as \"ConsolidationHash\",
		lm.StatelessHash		as \"StatelessHash\",
		lm.StateTransitionHash		as \"StateTransitionHash\",
		at.Name				as \"ApplicationTypeName\",
		et.Name				as \"EntityName\",
		h.HostName			as \"HostName\",
		s.ServiceDescription		as \"ServiceDescription\",
		pt.Name				as \"PropertyType\",
		pt.Description			as \"PropertyDescription\",
		pt.isInteger			as \"PropertyisInteger\",
		pt.isLong			as \"PropertyisLong\",
		pt.isBoolean			as \"PropertyisBoolean\",
		pt.isDate			as \"PropertyisDate\",
		pt.isString			as \"PropertyisString\",
		pt.isDouble			as \"PropertyisDouble\",
		lmp.ValueDate			as \"LogMessageDate\",
		lmp.ValueBoolean		as \"LogMessageBoolean\",
		lmp.ValueString			as \"LogMessageString\",
		lmp.ValueInteger		as \"LogMessageInteger\",
		lmp.ValueLong			as \"LogMessageLong\",
		lmp.ValueDouble			as \"LogMessageDouble\",
		ms.Name				as \"MonitorStatus\",
		sev.Name			as \"Severity\",
		appsev.Name			as \"ApplicationSeverity\",
		c.Name				as \"Component\",
		d.Identification		as \"DeviceIdentification\",
		os.Name				as \"OperationStatus\"
		from
		LogMessage			as lm,
		EntityType			as et,
		ApplicationEntityProperty	as aep,
		ApplicationType			as at,
		PropertyType			as pt,
		LogMessageProperty		as lmp,
		Severity			as sev,
		Severity			as appsev,
		Priority			as pri,
		Component			as c,
		Device				as d,
		MonitorStatus			as ms,
		ServiceStatus			as s,
		Host				as h,
		HostStatus			as hs,
		OperationStatus			as os,
		TypeRule			as t
		where
		h.HostName=$quoted_host and
		s.ServiceDescription=$quoted_service and
		at.Name=$quoted_appType and
		et.Name='LOG_MESSAGE' and
		aep.EntityTypeID=et.EntityTypeID and
		aep.ApplicationTypeID=at.ApplicationTypeID and
		at.ApplicationTypeID=lm.ApplicationTypeID and
		pt.PropertyTypeID=aep.PropertyTypeID and
		pt.PropertyTypeID=lmp.PropertyTypeID and
		lmp.LogMessageID=lm.logMessageID and
		lm.$timeField>=$quoted_fromDate and
		lm.$timeField<=$quoted_toDate and
		lm.SeverityID=sev.SeverityID and
		lm.ApplicationSeverityID=appsev.SeverityID and
		lm.PriorityID=pri.PriorityID and
		lm.ComponentID=c.ComponentID and
		lm.DeviceID=d.DeviceID and
		lm.MonitorStatusID=ms.MonitorStatusID and
		lm.ServiceStatusID=s.ServiceStatusID and
		lm.HostStatusID=hs.HostStatusID and
		lm.OperationStatusID=os.OperationStatusID and
		lm.TypeRuleID=t.TypeRuleID and
		hs.HostStatusID=h.HostID;
		";
	my $sth = $self->{dbh}->prepare($sql);
	$sth->execute();
	while (my $hashref = $sth->fetchrow_hashref()) {
		foreach my $key (keys %{$hashref}) {
			if (($key =~ /ID$/) or ($key eq "PropertyType")  or ($key =~/^Propertyis/))  { next }
			if ($key eq "LogMessageBoolean") {
				if ($hashref->{PropertyisBoolean}) {
					$event_ref->{$hashref->{LogMessageID}}->{$hashref->{PropertyType}} = $hashref->{LogMessageBoolean};
				}
			} elsif ($key eq "LogMessageDate") {
				if ($hashref->{PropertyisDate}) {
					$event_ref->{$hashref->{LogMessageID}}->{$hashref->{PropertyType}} = $hashref->{LogMessageDate};
				}
			} elsif ($key eq "LogMessageString") {
				if ($hashref->{PropertyisString}) {
					$event_ref->{$hashref->{LogMessageID}}->{$hashref->{PropertyType}} = $hashref->{LogMessageString};
				}
			} elsif  ($key eq "LogMessageDouble") {
				if ($hashref->{PropertyisDouble}) {
					$event_ref->{$hashref->{LogMessageID}}->{$hashref->{PropertyType}} = $hashref->{LogMessageDouble};
				}
			} elsif ($key eq "LogMessageInteger") {
				if ($hashref->{PropertyisInteger}) {
					$event_ref->{$hashref->{LogMessageID}}->{$hashref->{PropertyType}} = $hashref->{LogMessageInteger};
				}
			} elsif ($key eq "LogMessageLong")  {
				if ($hashref->{PropertyisLong}) {
					$event_ref->{$hashref->{LogMessageID}}->{$hashref->{PropertyType}} = $hashref->{LogMessageLong};
				}
			} else {
				$event_ref->{$hashref->{LogMessageID}}->{$key} = $hashref->{$key};
			}
		}
	}
	$sth->finish;
	if (!defined($event_ref)) {
		return '';
	}
	return \%{$event_ref};
}

sub getEventsForHost {
	my $self = shift;
	my $hostName = shift;
	my $timeField = shift;
	my $fromDate = shift;
	my $toDate = shift;
	my $applicationType = shift;
	my $event_ref = undef;
	# $event_ref->{id}->{Severity} = '';
	if (!$applicationType) {
		$applicationType = 'NAGIOS';
	}
	if ($timeField !~ /(LastInsertDate|FirstInsertDate|ReportDate)/) {
		return "Invalid time field $timeField";
	}
	if ($fromDate !~ /\d\d\d\d-\d\d-\d\d \d\d:\d\d:\d\d/) {
		return "Invalid from-date field $fromDate";
	}
	if ($toDate !~ /\d\d\d\d-\d\d-\d\d \d\d:\d\d:\d\d/) {
		return "Invalid to-date field $toDate";
	}
	my $quoted_host     = $self->{dbh}->quote($hostName);
	my $quoted_appType  = $self->{dbh}->quote($applicationType);
	my $quoted_fromDate = $self->{dbh}->quote($fromDate);
	my $quoted_toDate   = $self->{dbh}->quote($toDate);
	my $sql =
		"select
		lm.LogMessageID			as \"LogMessageID\",
		lm.ApplicationTypeID		as \"ApplicationTypeID\",
		lm.DeviceID			as \"DeviceID\",
		lm.HostStatusID			as \"HostStatusID\",
		lm.ServiceStatusID		as \"ServiceStatusID\",
		lm.TextMessage			as \"TextMessage\",
		lm.MsgCount			as \"MsgCount\",
		lm.FirstInsertDate		as \"FirstInsertDate\",
		lm.LastInsertDate		as \"LastInsertDate\",
		lm.ReportDate			as \"ReportDate\",
		lm.MonitorStatusID		as \"MonitorStatusID\",
		lm.SeverityID			as \"SeverityID\",
		lm.ApplicationSeverityID	as \"ApplicationSeverityID\",
		lm.PriorityID			as \"PriorityID\",
		lm.TypeRuleID			as \"TypeRuleID\",
		lm.ComponentID			as \"ComponentID\",
		lm.OperationStatusID		as \"OperationStatusID\",
		lm.isStateChanged		as \"isStateChanged\",
		lm.ConsolidationHash		as \"ConsolidationHash\",
		lm.StatelessHash		as \"StatelessHash\",
		lm.StateTransitionHash		as \"StateTransitionHash\",
		at.Name				as \"ApplicationTypeName\",
		et.Name				as \"EntityName\",
		h.HostName			as \"HostName\",
		s.ServiceDescription		as \"ServiceDescription\",
		pt.Name				as \"PropertyType\",
		pt.Description			as \"PropertyDescription\",
		pt.isInteger			as \"PropertyisInteger\",
		pt.isLong			as \"PropertyisLong\",
		pt.isBoolean			as \"PropertyisBoolean\",
		pt.isDate			as \"PropertyisDate\",
		pt.isString			as \"PropertyisString\",
		pt.isDouble			as \"PropertyisDouble\",
		lmp.ValueDate			as \"LogMessageDate\",
		lmp.ValueBoolean		as \"LogMessageBoolean\",
		lmp.ValueString			as \"LogMessageString\",
		lmp.ValueInteger		as \"LogMessageInteger\",
		lmp.ValueLong			as \"LogMessageLong\",
		lmp.ValueDouble			as \"LogMessageDouble\",
		ms.Name				as \"MonitorStatus\",
		sev.Name			as \"Severity\",
		appsev.Name			as \"ApplicationSeverity\",
		c.Name				as \"Component\",
		d.Identification		as \"DeviceIdentification\",
		os.Name				as \"OperationStatus\"
		from
		LogMessage			as lm,
		EntityType			as et,
		ApplicationEntityProperty	as aep,
		ApplicationType			as at,
		PropertyType			as pt,
		LogMessageProperty		as lmp,
		Severity			as sev,
		Severity			as appsev,
		Priority			as pri,
		Component			as c,
		Device				as d,
		MonitorStatus			as ms,
		ServiceStatus			as s,
		Host				as h,
		HostStatus			as hs,
		OperationStatus			as os,
		TypeRule			as t
		where
		h.HostName=$quoted_host and
		at.Name=$quoted_appType and
		et.Name='LOG_MESSAGE' and
		aep.EntityTypeID=et.EntityTypeID and
		aep.ApplicationTypeID=at.ApplicationTypeID and
		at.ApplicationTypeID=lm.ApplicationTypeID and
		pt.PropertyTypeID=aep.PropertyTypeID and
		pt.PropertyTypeID=lmp.PropertyTypeID and
		lmp.LogMessageID=lm.logMessageID and
		lm.$timeField>=$quoted_fromDate and
		lm.$timeField<=$quoted_toDate and
		lm.SeverityID=sev.SeverityID and
		lm.ApplicationSeverityID=appsev.SeverityID and
		lm.PriorityID=pri.PriorityID and
		lm.ComponentID=c.ComponentID and
		lm.DeviceID=d.DeviceID and
		lm.MonitorStatusID=ms.MonitorStatusID and
#		lm.ServiceStatusID=s.ServiceStatusID and
		lm.HostStatusID=hs.HostStatusID and
		lm.OperationStatusID=os.OperationStatusID and
		lm.TypeRuleID=t.TypeRuleID and
		hs.HostStatusID=h.HostID;
		";
	my $sth = $self->{dbh}->prepare($sql);
	$sth->execute();
	while (my $hashref = $sth->fetchrow_hashref()) {
		foreach my $key (keys %{$hashref}) {
			if (($key =~ /ID$/) or ($key eq "PropertyType")  or ($key =~/^Propertyis/))  { next }
			if ($key eq "LogMessageBoolean") {
				if ($hashref->{PropertyisBoolean}) {
					$event_ref->{$hashref->{LogMessageID}}->{$hashref->{PropertyType}} = $hashref->{LogMessageBoolean};
				}
			} elsif ($key eq "LogMessageDate") {
				if ($hashref->{PropertyisDate}) {
					$event_ref->{$hashref->{LogMessageID}}->{$hashref->{PropertyType}} = $hashref->{LogMessageDate};
				}
			} elsif ($key eq "LogMessageString") {
				if ($hashref->{PropertyisString}) {
					$event_ref->{$hashref->{LogMessageID}}->{$hashref->{PropertyType}} = $hashref->{LogMessageString};
				}
			} elsif  ($key eq "LogMessageDouble") {
				if ($hashref->{PropertyisDouble}) {
					$event_ref->{$hashref->{LogMessageID}}->{$hashref->{PropertyType}} = $hashref->{LogMessageDouble};
				}
			} elsif ($key eq "LogMessageInteger") {
				if ($hashref->{PropertyisInteger}) {
					$event_ref->{$hashref->{LogMessageID}}->{$hashref->{PropertyType}} = $hashref->{LogMessageInteger};
				}
			} elsif ($key eq "LogMessageLong")  {
				if ($hashref->{PropertyisLong}) {
					$event_ref->{$hashref->{LogMessageID}}->{$hashref->{PropertyType}} = $hashref->{LogMessageLong};
				}
			} else {
				$event_ref->{$hashref->{LogMessageID}}->{$key} = $hashref->{$key};
			}
		}
	}
	$sth->finish;
	if (!defined($event_ref)) {
		return '';
	}
	return \%{$event_ref};
}

##########################################################
#
#	CollageServiceQuery class methods
#
##########################################################

# return a reference to a hash of host/service attributes, or an empty string
sub getHostServices {
	my $self = shift;
	my $service_ref = undef;
	# $service_ref->{"Host_A"}->{"Service_2"}->{"attribute"} = "attribute_value";
	my $sql =
		"select
		h.HostName		as \"HostName\",
		ss.ServiceDescription	as \"ServiceDescription\",
		ss.LastCheckTime	as \"LastCheckTime\",
		ss.NextCheckTime	as \"NextCheckTime\",
		ss.LastStateChange	as \"LastStateChange\",
		st.Name			as \"StateType\",
		ct.Name			as \"CheckType\",
		ms.Name			as \"MonitorStatus\",
		pt.Name			as \"PropertyType\",
		pt.isInteger		as \"PropertyisInteger\",
		pt.isLong		as \"PropertyisLong\",
		pt.isBoolean		as \"PropertyisBoolean\",
		pt.isDate		as \"PropertyisDate\",
		pt.isString		as \"PropertyisString\",
		pt.isDouble		as \"PropertyisDouble\",
		ssp.ValueDate		as \"ServiceStatusDate\",
		ssp.ValueBoolean	as \"ServiceStatusBoolean\",
		ssp.ValueString		as \"ServiceStatusString\",
		ssp.ValueInteger	as \"ServiceStatusInteger\",
		ssp.ValueLong		as \"ServiceStatusLong\",
		ssp.ValueDouble		as \"ServiceStatusDouble\"
		from
		ServiceStatus			as ss,
		ServiceStatusProperty		as ssp,
		EntityType			as et,
		Host				as h,
		StateType			as st,
		CheckType			as ct,
		MonitorStatus			as ms,
		PropertyType			as pt,
		ApplicationType			as at,
		ApplicationEntityProperty	as aep
		where	at.Name = 'NAGIOS'
		and	aep.ApplicationTypeID = at.ApplicationTypeID
		and	et.EntityTypeID = aep.EntityTypeID
		and	et.Name = 'SERVICE_STATUS'
		and	pt.PropertyTypeID = aep.PropertyTypeID
		and	ssp.PropertyTypeID = pt.PropertyTypeID
		and	ss.ServiceStatusID = ssp.ServiceStatusID
		and	ss.ApplicationTypeID = at.ApplicationTypeID
		and	h.HostID = ss.HostID
		and	st.StateTypeID = ss.StateTypeID
		and	ct.CheckTypeID = ss.CheckTypeID
		and	ms.MonitorStatusID = ss.MonitorStatusID;
		";

	my $sth = $self->{dbh}->prepare($sql);
	# Note:  $sth->execute() can take a long time to run, but it is effectively
	# impossible to cleanly interrupt and cut short that time, even with a signal
	# handler in place.  See the DBI documentation for details.  The DBD::mysql
	# driver just continues on after receiving EINTR on one of the system calls it
	# makes, rather than recognizing it as a valid attempt to abort the operation.
	# And the driver does not implement $sth->cancel().  Thus, trying to wrap the
	# call to $sth->execute() in an eval{} and having the signal handler die()
	# from within the eval{}, or call $sth->cancel(), just does not work.  An
	# incoming signal is not recognized until sth->execute() has finished running,
	# which defeats the purpose of trying to use eval{} to abort early.
	$sth->execute();
	if (!$main::shutdown_requested) {
		while (my $hashref = $sth->fetchrow_hashref()) {
			if ($main::shutdown_requested) {
				$service_ref = undef;
				last;
			}
			foreach my $key (keys %{$hashref}) {
				if (($key eq 'HostName') or ($key eq 'ServiceDescription') or ($key =~ /^Propertyis/) or ($key =~ /^ServiceStatus/)) { next }
				if ($key eq 'PropertyType') {
					if ($hashref->{PropertyisBoolean}) {
						$service_ref->{$hashref->{HostName}}->{$hashref->{ServiceDescription}}->{$hashref->{PropertyType}} = $hashref->{ServiceStatusBoolean};
					}
					elsif ($hashref->{PropertyisDate}) {
						$service_ref->{$hashref->{HostName}}->{$hashref->{ServiceDescription}}->{$hashref->{PropertyType}} = $hashref->{ServiceStatusDate};
					}
					elsif ($hashref->{PropertyisString}) {
						$service_ref->{$hashref->{HostName}}->{$hashref->{ServiceDescription}}->{$hashref->{PropertyType}} = $hashref->{ServiceStatusString};
					}
					elsif ($hashref->{PropertyisDouble}) {
						$service_ref->{$hashref->{HostName}}->{$hashref->{ServiceDescription}}->{$hashref->{PropertyType}} = $hashref->{ServiceStatusDouble};
					}
					elsif ($hashref->{PropertyisInteger}) {
						$service_ref->{$hashref->{HostName}}->{$hashref->{ServiceDescription}}->{$hashref->{PropertyType}} = $hashref->{ServiceStatusInteger};
					}
					elsif ($hashref->{PropertyisLong}) {
						$service_ref->{$hashref->{HostName}}->{$hashref->{ServiceDescription}}->{$hashref->{PropertyType}} = $hashref->{ServiceStatusLong};
					}
				}
				else {
					$service_ref->{$hashref->{HostName}}->{$hashref->{ServiceDescription}}->{$key} = $hashref->{$key};
				}
			}
		}
	}
	$sth->finish;
	if (!defined($service_ref)) {
		return '';
	}
	return $service_ref;
}

# return a hash of service attributes
sub getService {
	my $self = shift;
	my $hostname = shift;
	my $servicename = shift;
	my $service_ref = undef;
	# $service_ref->{"attribute_1"} = "attribute value";
	my $quoted_host     = $self->{dbh}->quote($hostname);
	my $quoted_service  = $self->{dbh}->quote($servicename);
	my $sql =
		"select
		h.HostName		as \"HostName\",
		ss.ServiceStatusID	as \"ServiceStatusID\",
		ss.ServiceDescription	as \"ServiceDescription\",
		st.Name			as \"StateType\",
		ct.Name			as \"CheckType\",
		ms.Name			as \"MonitorStatus\",
		pt.Name			as \"PropertyType\",
		pt.Description		as \"PropertyDescription\",
		pt.isInteger		as \"PropertyisInteger\",
		pt.isLong		as \"PropertyisLong\",
		pt.isBoolean		as \"PropertyisBoolean\",
		pt.isDate		as \"PropertyisDate\",
		pt.isString		as \"PropertyisString\",
		pt.isDouble		as \"PropertyisDouble\",
		ssp.ValueDate		as \"ServiceStatusDate\",
		ssp.ValueBoolean	as \"ServiceStatusBoolean\",
		ssp.ValueString		as \"ServiceStatusString\",
		ssp.ValueInteger	as \"ServiceStatusInteger\",
		ssp.ValueLong		as \"ServiceStatusLong\",
		ssp.ValueDouble		as \"ServiceStatusDouble\"
		from
		ServiceStatus			as ss,
		ServiceStatusProperty		as ssp,
		EntityType			as et,
		Host				as h,
		StateType			as st,
		CheckType			as ct,
		MonitorStatus			as ms,
		PropertyType			as pt,
		ApplicationType			as at,
		ApplicationEntityProperty	as aep
		where	h.HostName = $quoted_host
		and	ss.HostID = h.HostID
		and	ss.ServiceDescription = $quoted_service
		and	ssp.ServiceStatusID = ss.ServiceStatusID
		and	pt.PropertyTypeID = ssp.PropertyTypeID
		and	aep.PropertyTypeID = pt.PropertyTypeID
		and	at.ApplicationTypeID = aep.ApplicationTypeID
		and	at.ApplicationTypeID = ss.ApplicationTypeID
		and	at.Name = 'NAGIOS'
		and	et.EntityTypeID = aep.EntityTypeID
		and	et.Name = 'SERVICE_STATUS'
		and	st.StateTypeID = ss.StateTypeID
		and	ct.CheckTypeID = ss.CheckTypeID
		and	ms.MonitorStatusID = ss.MonitorStatusID;
		";

	my $sth = $self->{dbh}->prepare($sql);
	$sth->execute();
	while (my $hashref = $sth->fetchrow_hashref()) {
		foreach my $key (keys %{$hashref}) {
			if (($key =~ /ID$/) or ($key eq "PropertyType") or ($key =~ /^Propertyis/)) { next }
			if ($key eq "ServiceStatusBoolean") {
				if ($hashref->{PropertyisBoolean}) {
					$service_ref->{$hashref->{PropertyType}} = $hashref->{ServiceStatusBoolean};
				}
			} elsif ($key eq "ServiceStatusDate") {
				if ($hashref->{PropertyisDate}) {
					$service_ref->{$hashref->{PropertyType}} = $hashref->{ServiceStatusDate};
				}
			} elsif ($key eq "ServiceStatusString") {
				if ($hashref->{PropertyisString}) {
					$service_ref->{$hashref->{PropertyType}} = $hashref->{ServiceStatusString};
				}
			} elsif  ($key eq "ServiceStatusDouble") {
				if ($hashref->{PropertyisDouble}) {
					$service_ref->{$hashref->{PropertyType}} = $hashref->{ServiceStatusDouble};
				}
			} elsif ($key eq "ServiceStatusInteger") {
				if ($hashref->{PropertyisInteger}) {
					$service_ref->{$hashref->{PropertyType}} = $hashref->{ServiceStatusInteger};
				}
			} elsif ($key eq "ServiceStatusLong")  {
				if ($hashref->{PropertyisLong}) {
					$service_ref->{$hashref->{PropertyType}} = $hashref->{ServiceStatusLong};
				}
			} else {
				$service_ref->{$key} = $hashref->{$key};
			}
		}
	}
	$sth->finish;
	if (!defined($service_ref)) {
		return '';
	}
	return %{$service_ref};
}

# return a reference to a hash of host-service-attributes
sub getServices {
	my $self = shift;
	my $service_ref = undef;
	my $sql =
		"select
		h.HostName		as \"HostName\",
		ss.ServiceStatusID	as \"ServiceStatusID\",
		ss.ServiceDescription	as \"ServiceDescription\",
		st.Name			as \"StateType\",
		ct.Name			as \"CheckType\",
		ms.Name			as \"MonitorStatus\"
		from
		ServiceStatus			as ss,
		ServiceStatusProperty		as ssp,
		EntityType			as et,
		Host				as h,
		StateType			as st,
		CheckType			as ct,
		MonitorStatus			as ms,
		PropertyType			as pt,
		ApplicationType			as at,
		ApplicationEntityProperty	as aep
		where	at.Name = 'NAGIOS'
		and	ss.ApplicationTypeID = at.ApplicationTypeID
		and	ssp.ServiceStatusID = ss.ServiceStatusID
		and	pt.PropertyTypeID = ssp.PropertyTypeID
		and	aep.PropertyTypeID = pt.PropertyTypeID
		and	aep.ApplicationTypeID = at.ApplicationTypeID
		and	et.EntityTypeID = aep.EntityTypeID
		and	et.Name = 'SERVICE_STATUS'
		and	h.HostID = ss.HostID
		and	st.StateTypeID = ss.StateTypeID
		and	ct.CheckTypeID = ss.CheckTypeID
		and	ms.MonitorStatusID = ss.MonitorStatusID;
		";
	my $sth = $self->{dbh}->prepare($sql);
	$sth->execute();
	while (my $hashref = $sth->fetchrow_hashref()) {
		# $service_ref->{"Host_A"}->{"Service_2"}->{"attribute"} = "attribute_value";
		foreach my $key (keys %{$hashref}) {
			if (($key eq "HostName") or ($key eq "ServiceDescription") or ($key =~ /ID$/)) { next }	# If key ends in ID, then it's a Primary Key so don't assign
			$service_ref->{$hashref->{HostName}}->{$hashref->{ServiceDescription}}->{$key} = $hashref->{$key};
		}
	}
	$sth->finish;
	if (!defined($service_ref)) {
		return '';
	}
	return \%{$service_ref};
}

##########################################################
#
# CollageMonitorServerQuery class methods
#
#########################################################

#	return a reference to a hash of monitorserver-attributes
sub getMonitorServers {
	my $self = shift;
	my $monitorserver_ref = undef;
	#$monitorserver_ref->{MonitorServerName}->{IP}= "1.1.1.1";
	#$monitorserver_ref->{MonitorServerName}->{Description}= "Monitor server description";
	my $sql = "select
		MonitorServerID		as \"MonitorServerID\",
		MonitorServerName	as \"MonitorServerName\",
		IP			as \"IP\",
		Description		as \"Description\"
		from MonitorServer;";
	my $sth = $self->{dbh}->prepare($sql);
	$sth->execute();
	while (my $hashref = $sth->fetchrow_hashref()) {
		foreach my $key (keys %{$hashref}) {
			if (($key eq "MonitorServerName") or ($key =~ /ID$/)) { next }
			$monitorserver_ref->{$hashref->{MonitorServerName}}->{$key} = $hashref->{$key};
		}
	}
	$sth->finish;
	if (!defined($monitorserver_ref)) {
		return '';
	}
	return \%{$monitorserver_ref};
}

#	return a reference to a hash of hosts for a designated monitorserver
sub getHostsForMonitorServer {
	my $self = shift;
	my $monitorserver = shift;
	my $host_ref = undef;
	my $quoted_server = $self->{dbh}->quote($monitorserver);
	my $sql =
		"select
		h.HostID		as \"HostID\",
		h.DeviceID		as \"DeviceID\",
		h.HostName		as \"HostName\",
		h.Description		as \"Description\",
		h.ApplicationTypeID	as \"ApplicationTypeID\"
		from Host as h, MonitorServer as m, Device as d, MonitorList as l
		where
		m.MonitorServerName=$quoted_server and
		m.MonitorServerID=l.MonitorServerID and
		l.DeviceID=d.DeviceID and
		h.DeviceID=d.DeviceID;";
	my $sth = $self->{dbh}->prepare($sql);
	$sth->execute();
	# $host_ref->{"Host_A"}->{Description} = "Host Description";
	while (my $hashref = $sth->fetchrow_hashref()) {
		foreach my $key (keys %{$hashref}) {
			if (($key eq "HostName") or ($key =~ /ID$/)) { next }
			$host_ref->{$hashref->{HostName}}->{$key} = $hashref->{$key};
		}
	}
	$sth->finish;
	if (!defined($host_ref)) {
		return '';
	}
	return \%{$host_ref};
}

# return a reference to a hash of host groups-attributes
sub getHostGroupsForMonitorServer {
	my $self = shift;
	my $monitorserver = shift;
	my $hostgroup_ref = undef;
	# $hostgroup_ref->{"Hostgroup_A"}->{Description} = "Hostgroup A description";
	my $quoted_server = $self->{dbh}->quote($monitorserver);
	my $sql =
		"select
		hg.HostGroupID		as \"HostGroupID\",
		hg.Name			as \"Name\",
		hg.Description		as \"Description\",
		hg.ApplicationTypeID	as \"ApplicationTypeID\",
		hg.Alias		as \"Alias\"
		from
		HostGroup		as hg,
		HostGroupCollection	as hgc,
		Host			as h,
		MonitorServer		as m,
		Device			as d,
		MonitorList		as ml
		where
		m.MonitorServerName=$quoted_server and
		m.MonitorServerID=ml.MonitorServerID and
		ml.DeviceID=d.DeviceID and
		h.DeviceID=d.DeviceID and
		hgc.HostID=h.HostID and
		hgc.HostGroupID=hg.HostGroupID;
		";
	my $sth = $self->{dbh}->prepare($sql);
	$sth->execute();
	# $host_ref->{"Host_A"}->{Description} = "Host Description";
	while (my $hashref = $sth->fetchrow_hashref()) {
		foreach my $key (keys %{$hashref}) {
			if (($key eq "Name") or ($key =~ /ID$/)) { next }
			$hostgroup_ref->{$hashref->{Name}}->{$key} = $hashref->{$key};
		}
	}
	$sth->finish;
	if (!defined($hostgroup_ref)) {
		return '';
	}
	return \%{$hostgroup_ref};
}

##########################################################
#
# Collage Host and Service count methods
#
#########################################################

#       return an integer count of hosts for Application type supplied
sub getHostCount {
	my $self = shift;
	my $applicationType = shift;
	my $quoted_appType  = $self->{dbh}->quote($applicationType);
	my $hostcount = undef;
	my $sql = "select count(*) FROM Host h, ApplicationType at where at.Name = $quoted_appType and h.ApplicationTypeID = at.ApplicationTypeID;";
	my $sth = $self->{dbh}->prepare($sql);
	$sth->execute();
	$hostcount = $sth->fetchrow_array;
	$sth->finish;
	if (!defined($hostcount)) {
		return 0;
	}
	return $hostcount;
}

#       return an integer count of services for Application type supplied
sub getServiceCount {
	my $self = shift;
	my $applicationType = shift;
	my $quoted_appType  = $self->{dbh}->quote($applicationType);
	my $servicecount = undef;
	my $sql = "select count(*) FROM ServiceStatus ss, ApplicationType at where at.Name = $quoted_appType and ss.ApplicationTypeID = at.ApplicationTypeID;";
	my $sth = $self->{dbh}->prepare($sql);
	$sth->execute();
	$servicecount = $sth->fetchrow_array;
	$sth->finish;
	if (!defined($servicecount)) {
		return 0;
	}
	return $servicecount;
}

1;

__END__

=head1 NAME

CollageQuery - Perl extension for GroundWork's Collage open source project

=head1 SYNOPSIS

This package allows a Perl program to access data stored in GroundWork's Collage open source package.
Collage is intended to gathers data from diverse IT managemnt systems and provide a common framework
for creating user interface applications, automation tasks and reports.  In this version, data from a Nagios
monitoring system populates the Collage data store.  This package will query the data store and return
results in Perl data structures.

The following classes of queries are available:

CollageQuery: Create and destroy CollageQuery objects.
CollageHostGroupQuery: Access Host Group information. Host groups consist of a number of hosts.
CollageHostQuery: Access Host information. Hosts consist of a number of services.
CollageEventQuery: Access event log information.  There are 4 types of log events: HOST ALERT, SERVICE ALERT,
	HOST NOTIFICATION, SERVICE NOTIFICATION.
CollageServiceQuery: Access services information. A service is always associated with a host.
CollageMonitorServerQuery:  Access monitoring server information.  A monitoring server is a Nagios server.
CollageServiceGroupQuery: ...
CollagePropertiesQuery: ...
CollageCountQuery: ...

=head1 DESCRIPTION

Sample Usage:
	use CollageQuery;
	my	$t=CollageQuery->new();
	#
	#	Get the host, services and attributes for HostGroup_A
	#
	print "\nSample getServicesForHostGroup method\n";
	my $ref = $t->getServicesForHostGroup("HostGroup_A");
	foreach my $host (keys %{$ref}) {
		print "Host=$host\n";
		foreach my $service (keys %{$ref->{$host}}) {
			print "\tService=$service\n";
			foreach my $attribute (keys %{$ref->{$host}->{$service}}) {
				print "\t\t$attribute=".$ref->{$host}->{$service}->{$attribute}."\n";
			}
		}
	}
	$t->destroy();

Sample output:
	Sample getServicesForHostGroup method
	Host=Host_A
		Service=Service_1
			attribute=attribute_value
		Service=Service_2
			attribute=attribute_value
	Host=Host_B
		Service=Service_1
			attribute=attribute_value

Methods:

CollageQuery
	new - Create the CollageQuery object. Required to use any of the following methods.
	destroy - Destroys the CollageQuery object.  Should be called when the CollageQuery object is no longer needed.

CollageHostGroupQuery
	getServicesForHostGroup(String hostGroup) -  return a reference to a hash host-service-attributes for a designated host group
	getHostsForHostGroup(String hostGroup) - return a reference to a hash of all host names, device name for a designated host group
	getHostGroups() - return a reference to all hostgroup names, descriptions,
	getHostGroupsForHost() - return a reference to an array with the names of all hostgroups to which a host belongs
	getHostGroup(String hgName) -  return a hash containing the attributes for a host group

CollageHostQuery
	getServicesForHost(String host) - return an reference to a hash of all services-attributes for a host
	getHosts() - return a reference to a hash of all host-attributes
	getHostStatusForHost(String host) - return a hash of all the status attributes for a host
	getDeviceForHost(String host) - return a hash of the device attributes for a host
	getServiceNamesForHost(String host) - return a reference to an array with all services names for a host

CollageEventQuery:   Returns a reference to a hash of events with the event ID as primary key and attributes as secondary key
	getEventsForDevice(String identification, String timeField, Date fromDate, Date toDate)
	getEventsForService(String serviceDescription, String HostName, String timeField, Date fromDate, Date toDate)
	getEventsForHost(String HostName, String timeField, Date fromDate, Date toDate)
	Note: timeField (String) can be FirstInsertDate or LastInsertDate if it's null no range will be applied

CollageServiceQuery
	getHostServices() - return a reference to a hash of host/service attributes, or an empty string
	getService(String serviceName, String hostName) - return a hash of service attributes
	getServices() - return a reference to a hash of host-service-attributes

CollageMonitorServerQuery
	getMonitorServers() - return a reference to a hash of monitorserver-attributes
	getHostsForMonitorServer(String MonitorServer) - return a reference to a hash of hosts for a designated monitorserver
	getHostGroupsForMonitorServer(String MonitorServer) - return a reference to a hash of host groups-attributes

CollageServiceGroupQuery
	getServiceGroups() - return a reference to all servicegroup names, descriptions,
	getServiceGroupsForService() - return a reference to an array with the names of all servicegroups to which a host service belongs
	getHostsForServiceGroup(String servicegroup) - return a reference to a hash of all hosts for a designated service group
	getHostServicesForServiceGroup(String servicegroup, String sghost) - return a reference to a hash of all service names for a designated service group and host

CollagePropertiesQuery
	getHostParents(String host) - return a string of parents for a designated host
	getHostAlias(String host) - return the alias for a designated host
	getServiceProperties(String host, String service, String propname) - return the value of a specific host/service property

CollageCountQuery
	getHostCount(String apptype) - return an integer count of hosts for Application type supplied
	getServiceCount(String apptype) - return an integer count of services for Application type supplied

=head2 EXPORT

None by default.  The following methods must be referenced explicitly (CollageQuery-><method>)

	new
	destroy
	getServicesForHostGroup(String hostGroup)
	getHostsForHostGroup(String hostGroup)
	getHostGroups()
	getHostGroupsForHost(String host)
	getHostGroup(String hgName)
	getServicesForHost(String host)
	getHosts()
	getHostStatusForHost(String host)
	getDeviceForHost(String host)
	getServiceNamesForHost(String host)
	getEventsForDevice(String identification, String timeField, Date fromDate, Date toDate)
	getEventsForService(String serviceDescription, String HostName, String timeField, Date fromDate, Date toDate)
	getEventsForHost(String HostName, String timeField, Date fromDate, Date toDate)
	getHostServices()
	getService(String serviceName, String hostName)
	getServices()
	getMonitorServers()
	getHostsForMonitorServer(String MonitorServer)
	getHostGroupsForMonitorServer(String MonitorServer)
	getServiceGroups()
	getServiceGroupsForService(String host, String service)
	getHostsForServiceGroup(String servicegroup)
	getHostServicesForServiceGroup(String servicegroup, String sghost)
	getHostParents(String host)
	getHostAlias(String host)
	getServiceProperties(String host, String service, String propname)
	getHostCount(String apptype)
	getServiceCount(String apptype)

=head1 SEE ALSO

More information on GroundWork products and services can be found at http://www.groundworkopensource.com

=head1 ORIGINAL AUTHOR

Peter Loh<lt>ploh@groundworkopensource.com.com<gt>

=head1 COPYRIGHT AND LICENSE

	Copyright 2003-2017 GroundWork Open Source, Inc.
	http://www.groundworkopensource.com

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
	WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
	License for the specific language governing permissions and limitations under
	the License.

=cut
