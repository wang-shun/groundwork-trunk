#!/usr/local/groundwork/perl/bin/perl -w

use DBI;
use XML::Simple;
use Data::Dumper;

# turn debugging on/off
my $debug = 0;

# config file location
my $jpp_config = "/usr/local/groundwork/foundation/container/jpp/standalone/configuration/standalone.xml";

# default DB config
my $db_user = "postgres";
my $db_pass = "groundwork";
my $db_host = "localhost";
my $db_port = 5432;
my $old_dbname = "jbossportal";
my $new_dbname = "jboss-idm";

# load connection options
if( -e $jpp_config ){
	my $server = XMLin($jpp_config);
		
	foreach my $subsystem ( @{$server->{'profile'}->{'subsystem'}} ){
		if( $subsystem->{'xmlns'} eq "urn:jboss:domain:datasources:1.1" ){
			foreach my $datasource ( @{$subsystem->{'datasources'}->{'datasource'}} ){
				if( $datasource->{'pool-name'} eq "IDMPortalDS" ){
					
					$db_user = $datasource->{'security'}->{'user-name'} if exists $datasource->{'security'}->{'user-name'};
					$db_pass = $datasource->{'security'}->{'password'} if exists $datasource->{'security'}->{'password'};
					
					if( $datasource->{'connection-url'} =~ /jdbc:postgresql:\/\/([\w\.\-\_]+):(\d+)\/([\w\.\-\_]+)/ ){
						$db_host = $1 if $1 ne "";
						$db_port = $2 if $2 ne "";
						$new_dbname = $3 if $3 ne "";
					}
				}
			}
		}
	}
}

print "------------------------\n" if $debug;

# connect old database
my $old_dbh = DBI->connect("dbi:Pg:dbname=$old_dbname;host=$db_host;port=$db_port;", $db_user, $db_pass, { AutoCommit => 1 }) or die $DBI::errstr;

# test if old NagVis permissions are present
my $rows = $old_dbh->do("SELECT table_name FROM information_schema.tables WHERE table_name LIKE 'jbp_nagvis_perm%';");
exit if $rows == 0;
$rows = $old_dbh->do("SELECT * FROM jbp_nagvis_perm_membership;");
exit if $rows == 0;
print "Old NagVis permissions are present\n" if $debug;

# connect new database
my $new_dbh = DBI->connect("dbi:Pg:dbname=$new_dbname;host=$db_host;port=$db_port;", $db_user, $db_pass, { PrintError => 0, AutoCommit => 1 } ) or die $DBI::errstr;

# test if new NagVis permissions are present and create or truncate tables
$rows = $new_dbh->do("SELECT table_name FROM information_schema.tables WHERE table_name = 'nagvis_perms';");
if($rows == 0){
	print "Creating table nagvis_perms\n" if $debug;
	$new_dbh->do("CREATE TABLE IF NOT EXISTS nagvis_perms (
	nv_pid SERIAL,
	nv_mod VARCHAR(100) NULL ,
	nv_act VARCHAR(100) NULL ,
	nv_obj VARCHAR(100) NULL ,
	PRIMARY KEY (nv_pid) ,
	UNIQUE (nv_mod , nv_act , nv_obj )
);") or die $new_dbh->errstr;
} else {
	print "Truncating table nagvis_perms\n" if $debug;
	$new_dbh->do("TRUNCATE TABLE nagvis_perms CASCADE;");
}
$rows = $new_dbh->do("SELECT table_name FROM information_schema.tables WHERE table_name = 'nagvis_perm_membership';");
if($rows == 0){
	print "Creating table nagvis_perm_membership\n" if $debug;
	$new_dbh->do("CREATE TABLE IF NOT EXISTS nagvis_perm_membership (
	jbp_name VARCHAR(255) NOT NULL ,
	nv_pid INT NOT NULL ,
	PRIMARY KEY (jbp_name, nv_pid) ,
	CONSTRAINT role
		FOREIGN KEY (jbp_name)
		REFERENCES gw_ext_role_attributes (jbp_name)
		ON DELETE CASCADE
		ON UPDATE CASCADE,
	CONSTRAINT perm
		FOREIGN KEY (nv_pid)
		REFERENCES nagvis_perms (nv_pid)
		ON DELETE CASCADE
		ON UPDATE CASCADE
);") or die $new_dbh->errstr;
} else {
	print "Truncating table nagvis_perm_membership\n" if $debug;
	$new_dbh->do("TRUNCATE TABLE nagvis_perm_membership CASCADE;");
}

print "------------------------\n" if $debug;

# copy all permissions
my $perms_sth = $old_dbh->prepare("SELECT nv_mod, nv_act, nv_obj FROM jbp_nagvis_perms WHERE nv_mod IN('*','Map','Rotation','ManageBackgrounds','ManageShapes','RoleMgmt') ORDER BY nv_mod, nv_obj;");
$rows = $perms_sth->execute() or die $perms_sth->errstr;
my $new_perm_sth = $new_dbh->prepare("INSERT INTO nagvis_perms (nv_mod,nv_act,nv_obj) VALUES (?,?,?);");

while( my @perm = $perms_sth->fetchrow_array() ){
	if( $new_perm_sth->execute(@perm) > 0 && $debug ){
		printf "Added perm MOD: %-10s ACT: %-10s OBJ: %s\n", @perm;
	}
}

print "------------------------\n" if $debug;

# add permissions to roles
my $perm_membership_sth = $old_dbh->prepare("SELECT
  r.jbp_name,
  p.nv_mod,
  p.nv_act,
  p.nv_obj
FROM
  jbp_nagvis_perms AS p
JOIN jbp_nagvis_perm_membership AS r2p
  USING(nv_pid)
JOIN jbp_roles AS r
  USING(jbp_rid)
GROUP BY jbp_name, nv_mod, nv_act, nv_obj
ORDER BY jbp_name, nv_mod;");
$perm_membership_sth->execute() or die $perm_membership_sth->errstr;
my $new_perm_memb_sth = $new_dbh->prepare("INSERT INTO nagvis_perm_membership (jbp_name, nv_pid) VALUES (
  ?, (SELECT nv_pid FROM nagvis_perms WHERE nv_mod = ? AND nv_act = ? AND nv_obj = ? ) 
);");

while( my @perm_membership = $perm_membership_sth->fetchrow_array() ){
	if( $new_perm_memb_sth->execute(@perm_membership) && $debug ){
		printf "Linked role %-10s to permission MOD: %-10s ACT: %-10s OBJ: %s\n", @perm_membership;
	}
}

print "------------------------\n" if $debug;
print "Done\n" if $debug;
