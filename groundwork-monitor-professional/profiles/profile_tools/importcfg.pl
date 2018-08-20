#!/usr/local/groundwork/perl/bin/perl --
#
#
#	importcfg.pl	v4.3	2013-08-21
#
# Copyright (C) 2008-2013 GroundWork Open Source, Inc. ("GroundWork")
# All rights reserved. Use is subject to GroundWork commercial license terms.
#
use strict;
use Time::Local;
use DBI;
use CollageQuery;
use lib "/usr/local/groundwork/core/monarch/lib";
use MonarchImport;
use Time::HiRes;
use XML::LibXML;

my $stylesheethtmlref = "";
my $thisprogram       = "importcfg.pl";

# my $csvdirectory = "/home/test/import";
# my $csvdirectory = "/home/dev/import";
# my $csvdirectory = "/home/peter/import";
my $csvdirectory = "/usr/local/groundwork/tools/discover_import";
my $debug        = 0;

print "Content-type: text/html \n\n";
my $request_method = $ENV{'REQUEST_METHOD'};
my $form_info;
if ( $request_method eq "GET" ) {
    $form_info = $ENV{'QUERY_STRING'};
    ## $form_info =~ s/%([\dA-Fa-f][\dA-Fa-f])/pack("C",hex($1))/eg;
}
elsif ( $request_method eq "POST" ) {
    my $size_of_form_info = $ENV{'CONTENT_LENGTH'};
    read( STDIN, $form_info, $size_of_form_info );
}
else {
    print "500 Server Error. Server uses unsupported method";
    $ENV{'REQUEST_METHOD'} = "GET";
    $ENV{'QUERY_STRING'}   = $ARGV[0];
    $form_info             = $ARGV[0];
}
my %FORM_DATA;
my ( $key, $value );
foreach my $key_value ( split( /&/, $form_info ) ) {
    ( $key, $value ) = split( /=/, $key_value );
    $value =~ tr/+/ /;
    $value =~ s/%([\dA-Fa-f][\dA-Fa-f])/pack("C",hex($1))/eg;
    if ( defined( $FORM_DATA{$key} ) ) {
	$FORM_DATA{$key} = join( "\0", $FORM_DATA{$key}, $value );
    }
    else {
	$FORM_DATA{$key} = $value;
    }
}

print "
	<HTML>
	<HEAD>
	<META HTTP-EQUIV='Cache-Control' CONTENT='no-cache'>
	<META HTTP-EQUIV='Pragma' CONTENT='no-cache'>
	<META HTTP-EQUIV='Expires' CONTENT='0'>
	<TITLE>Groundwork Configuration Data Import Tool</TITLE>
	<link rel='stylesheet' type='text/css' href='$stylesheethtmlref'>
";
printstyles();

print qq(
	<SCRIPT language="JavaScript">
	function changePage (page) {
		if (page.length) {
			location.href=page
		}
	}
	function updatePage (attrName,attrValue) {
		page="$thisprogram?$form_info&"+attrName+"="+attrValue
		if (page.length) {
			location.href=page
		}
	}
	</SCRIPT>
);

print '
	</HEAD>
	<BODY class=insight>
	<DIV id=container>
';

if ( !$FORM_DATA{Portal} ) {    # Don't print header if invoked from the portal
    ##	print '
    ##		<DIV id=logo></DIV>
    ##		<DIV id=pagetitle>
    ##		<H1 class=insight>Configuration Data Import Tool</H1>
    ##		</DIV>
    ##	';
}
my ( $sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $isdst ) = localtime(time);
$year += 1900;
my $month = qw(January February March April May June July August September October November December) [$mon];
my $timestring = sprintf "%02d:%02d:%02d", $hour, $min, $sec;
my $thisday = qw(Sunday Monday Tuesday Wednesday Thursday Friday Saturday) [$wday];

print "<FORM name=selectForm class=formspace action=$thisprogram method=get>";
print "<table class=insightcontrolpanel cellspacing=0><TBODY><tr class=insightgray-bg>";
print "<TH class=insight colSpan=2>$thisday, $month $mday, $year. $timestring</TH></TR>";
print "</TABLE>";

my ( $dbname, $dbhost, $dbuser, $dbpass, $dbtype ) = CollageQuery::readGroundworkDBConfig('monarch');
my $dsn = '';
if ( defined($dbtype) && $dbtype eq 'postgresql' ) {
    $dsn = "DBI:Pg:dbname=$dbname;host=$dbhost";
}
else {
    $dsn = "DBI:mysql:database=$dbname;host=$dbhost";
}
my $dbh = DBI->connect( $dsn, $dbuser, $dbpass, { 'AutoCommit' => 1 } )
  or die "Can't connect to database $dbname. Error: " . $DBI::errstr;

if ( $FORM_DATA{cmd} eq "FormEntry" or !$FORM_DATA{cmd} ) {
    FormEntry();
}
elsif ( $FORM_DATA{cmd} eq "Show Services" ) {
    ShowServices();
}
elsif ( $FORM_DATA{cmd} eq "Test Next Record" ) {
    ShowServices();
    TestStep();
}
elsif ( $FORM_DATA{cmd} eq "Create All Definitions" ) {
    ShowServices();
    CreateAll();
}
elsif ( $FORM_DATA{cmd} eq "Show Created Definitions" ) {
    ShowCreated();
}
elsif ( $FORM_DATA{cmd} eq "Modify Selected Hosts/Services" ) {
    Modify();
}
elsif ( $FORM_DATA{cmd} eq "Implement Modifications" ) {
    ImplementMods();
}
elsif ( $FORM_DATA{cmd} eq "Import Definitions" ) {
    ImportForm();
}
elsif ( $FORM_DATA{cmd} eq "Commit" ) {
    Commit();
}
elsif ( $FORM_DATA{cmd} eq "Export Current Match Criteria" ) {
    ShowServices();
    ExportMatch();
}
print "</FORM>";
$dbh->disconnect();
exit;

#
#	Initial Form
#
sub FormEntry {
    my %checked;
    print "<table class=insightcontrolpanel cellspacing=0>";
    my $query = "SELECT * FROM profiles_host";
    print "<tr><td class=insight><b>Select Host Profile:</b> (required)</td><td class=insight>
			<select name=hostprofileid class=insight>
			<option class=insight $checked{''} value=''>
	";
    my $sth = $dbh->prepare($query);
    $sth->execute() or die $@;
    while ( my $row = $sth->fetchrow_hashref() ) {
	my $option = $$row{hostprofile_id};
	print "<option class=insight $checked{$option} value='$option'>$$row{name}</option>";
    }
    $sth->finish;
    print "</select>";
    print "</td></tr>";

    #	my $query = "SELECT * FROM profiles_service";
    #	print "<tr><td class=insight><b>Select Service Profile:</b></td><td class=insight>
    #			<select name=serviceprofileid class=insight>
    #			<option class=insight $checked{''} value=''>
    #	";
    #	my $sth = $dbh->prepare($query);
    #	$sth->execute() or die  $@;
    #	while (my $row=$sth->fetchrow_hashref()) {
    #		my $option = $$row{serviceprofile_id};
    #		print "<option class=insight $checked{$option} value='$option'>$$row{name}</option>";
    #	}
    #	print "</select>";
    #	print "</td></tr>";

    print "<tr><td class=insight><b>Select CSV file to import:</b> (required, in $csvdirectory)</td><td class=insight>
	    <select name=file class=insight>
	    <option class=insight $checked{''} value=''>
	";
    opendir( DIR, $csvdirectory ) or print "ERROR. Can't open directory $csvdirectory:$! \n";
    while ( defined( my $file = readdir(DIR) ) ) {
	if ( $file =~ /\w+\.csv$/i ) {
	    print "<option class=insight $checked{$file} value='$file'>$file</option>";
	}
    }
    closedir(DIR);
    print "</select>";
    print "</td></tr>";

    print "<tr><td class=insight><b>Select match criteria file to import:</b> (optional, in $csvdirectory)</td><td class=insight>
	    <select name=matchfile class=insight>
	    <option class=insight $checked{''} value=''>
	";
    opendir( DIR, $csvdirectory ) or print "ERROR. Can't open directory $csvdirectory:$! \n";
    while ( defined( my $file = readdir(DIR) ) ) {
	if ( $file =~ /\w+\.xml$/i ) {
	    print "<option class=insight $checked{$file} value='$file'>$file</option>";
	}
    }
    closedir(DIR);
    print "</select>";
    print "</td></tr>";
    print "</table>";
    print "<INPUT class=graybutton type=button value='Reset' onClick='changePage(\"$thisprogram?cmd=FormEntry\")'>
	    <INPUT class=orangebutton type=submit value='Show Services' name=cmd>
	";
    print "<br>";
}

sub ShowServices {
    my %checked;
    print "<input type=hidden name=hostprofileid value=$FORM_DATA{hostprofileid}>";
    ## print "<input type=hidden name=serviceprofileid value=$FORM_DATA{serviceprofileid}>";
    print "<input type=hidden name=file value=$FORM_DATA{file}>";
    print "<input type=hidden name=matchfile value=$FORM_DATA{matchfile}>";

    if ( !$FORM_DATA{hostprofileid} ) {
	print "<p>Error: No host profile specified.</p>";
	return;
    }
    my $hostprofilename = undef;
    my $query           = "SELECT ph.name,phps.serviceprofile_id FROM profiles_host as ph,profile_host_profile_service as phps "
      . "WHERE ph.hostprofile_id=$FORM_DATA{hostprofileid} and ph.hostprofile_id=phps.hostprofile_id";
    my $sth = $dbh->prepare($query);
    $sth->execute() or die $@;
    my @serviceprofile_ids = ();
    while ( my $row = $sth->fetchrow_hashref() ) {
	$hostprofilename = $$row{name};
	push @serviceprofile_ids, $$row{serviceprofile_id};
    }
    $sth->finish;
    if ( $#serviceprofile_ids < 0 ) {
	print "	<INPUT class=orangebutton type=button value='Reset' onClick='changePage(\"$thisprogram?cmd=FormEntry\")'> ";
	print "<p>No service profiles are assigned to this host profile.</p>\n";
	return;
    }

    my $service = undef;
    foreach my $serviceprofileid (@serviceprofile_ids) {
	print "<input type=hidden name=serviceprofileid value=$serviceprofileid>";
	my $query = "SELECT * FROM serviceprofile where serviceprofile_id=$serviceprofileid";
	my $sth   = $dbh->prepare($query);
	$sth->execute() or die $@;
	my @servicename_ids = ();
	while ( my $row = $sth->fetchrow_hashref() ) {
	    push @servicename_ids, $$row{servicename_id};
	}
	$sth->finish;
	foreach my $id (@servicename_ids) {
	    my $query = "SELECT * FROM service_names where servicename_id=$id";
	    my $sth   = $dbh->prepare($query);
	    $sth->execute() or die $@;
	    while ( my $row = $sth->fetchrow_hashref() ) {
		$service->{$id}->{name}          = $$row{name};
		$service->{$id}->{description}   = $$row{description};
		$service->{$id}->{command_line}  = $$row{command_line};
		$service->{$id}->{check_command} = $$row{check_command};
	    }
	    $sth->finish;
	}
    }
    my @filefields = ();
    if ( $FORM_DATA{file} ) {
	open( IMPORTFILE, "$csvdirectory/$FORM_DATA{file}" ) or die $!;
	while ( my $line = <IMPORTFILE> ) {
	    chomp $line;
	    if ( $line =~ /^\s*#/ ) {
		next;
	    }
	    @filefields = split /,/, $line;
	    last;
	}
	close IMPORTFILE;
    }

    print "<table class=insightcontrolpanel cellspacing=0>";
    print "<tr><td class=insight>Using Host Profile:</td><td class=insight>	";
    if ( $FORM_DATA{hostprofileid} ) {
	print "$hostprofilename";
    }
    else {
	print " ERROR: Unknown host profile";
    }
    print "</td></tr>";

    print "<tr><td class=insight>Using Service Profile:</td><td class=insight>	";
    my $tmpstring = "";
    foreach my $id (@serviceprofile_ids) {
	$tmpstring .= "$id,";
    }
    $tmpstring =~ s/,$//;
    my $query = "SELECT * FROM profiles_service where serviceprofile_id IN ($tmpstring) ";
    my $sth   = $dbh->prepare($query);
    $sth->execute() or die $@;
    while ( my $row = $sth->fetchrow_hashref() ) {
	print $$row{name} . " - " . $$row{description} . "<br>";
    }
    $sth->finish;
    print "</td></tr>";

    print "<tr><td class=insight>CSV file imported:(in $csvdirectory)</td><td class=insight>$FORM_DATA{file}";
    print "</td></tr>";

    my $xmlstring = undef;
    my $xml_ref   = undef;
    if ( $FORM_DATA{matchfile} ) {
	open( IMPORTFILE, "$csvdirectory/$FORM_DATA{matchfile}" ) or die $!;
	while ( my $line = <IMPORTFILE> ) {
	    chomp $line;
	    $xmlstring .= $line;
	}
	close IMPORTFILE;
	$xml_ref = parse_xml($xmlstring);
    }
    print "<tr><td class=insight>Match file imported: (in $csvdirectory)</td><td class=insight>$FORM_DATA{matchfile}";
    print "</td></tr>";

    print "<tr><td class=insight>Map Host Name to field:</td><td class=insight>";

    my $i            = 0;
    my %xmlfieldname = ();
    if ( defined $xml_ref ) {
	foreach my $key ( sort keys %{ $xml_ref->{field_name} } ) {
	    $xmlfieldname{ $xml_ref->{field_name}->{$key} } = $i;
	    ## print "<br>Setting $xml_ref->{field_name}->{$key} to index $i";
	    $i++;
	}

	my %checked = ();
	if ( defined( $FORM_DATA{hostnamefield} ) ) {
	    $checked{ $FORM_DATA{hostnamefield} } = "SELECTED";
	}
	else {
	    ## print "<br>Using host_name_field index ".$xmlfieldname{$xml_ref->{host_name_field}};
	    $checked{ $xmlfieldname{ $xml_ref->{host_name_field} } } = "SELECTED";
	}
	print "	<select name=hostnamefield class=insight>";
	for ( my $i = 0 ; $i <= $#filefields ; $i++ ) {
	    print "<option class=insight $checked{$i} value='$i'>$filefields[$i]</option>";
	}
	print "</select>";
	print "</td></tr>";
	print "<tr><td class=insight>Map Host Alias to field:</td><td class=insight>";
	my %checked = ();
	if ( defined( $FORM_DATA{hostaliasfield} ) ) {
	    $checked{ $FORM_DATA{hostaliasfield} } = "SELECTED";
	}
	else {
	    ## print "<br>Using host_alias_field index ".$xmlfieldname{$xml_ref->{host_alias_field}};
	    $checked{ $xmlfieldname{ $xml_ref->{host_alias_field} } } = "SELECTED";
	}
	print "	<select name=hostaliasfield class=insight>";
	for ( my $i = 0 ; $i <= $#filefields ; $i++ ) {
	    print "<option class=insight $checked{$i} value='$i'>$filefields[$i]</option>";
	}
	print "</select>";
	print "</td></tr>";
	my %checked = ();
	print "<tr><td class=insight>Map Host IP Address to field:</td><td class=insight>";
	if ( defined( $FORM_DATA{hostipaddressfield} ) ) {
	    $checked{ $FORM_DATA{hostipaddressfield} } = "SELECTED";
	}
	else {
	    ## print "<br>Using host_ipaddress_field index ".$xmlfieldname{$xml_ref->{host_ipaddress_field}};
	    $checked{ $xmlfieldname{ $xml_ref->{host_ipaddress_field} } } = "SELECTED";
	}
	print "<select name=hostipaddressfield class=insight>";
	for ( my $i = 0 ; $i <= $#filefields ; $i++ ) {
	    print "<option class=insight $checked{$i} value='$i'>$filefields[$i]</option>";
	}
	print "</select>";
	print "</td></tr>";

	print "</table>";
	print "<br><br>";
	print "<table class=insightcontrolpanel cellspacing=0>";
	print "<tr><th class=insight>Service Name</th>";

	# sparris modified 2006-3-10
	#	print "<th class=insight>Service Description</th>";
	print "<th class=insight >Create Service Condition:</th>";
	print "<th class=insight >New Service Description</th>";
	print "<th class=insight >Command</th>";
	print "<th class=insight >Command Argument Mapping</th>";
	foreach my $id ( keys %{$service} ) {
	    print "<tr><td class=insight>" . $service->{$id}->{name} . "</td>";

	    # sparris modified 2006-3-10
	    #	print "<td class=insight>".$service->{$id}->{description}."</td>";
	    print "<td class=insight>";

	    my %buttonchecked = ();
	    if ( defined( $FORM_DATA{"createservice_$id"} ) ) {
		$buttonchecked{ $FORM_DATA{"createservice_$id"} } = "CHECKED";
	    }
	    elsif (
		$xml_ref->{service}->{service_name}->{ $service->{$id}->{name} }->{create_service_condition}->{on_field_match}->{enable} == 1 )
	    {

		## print "<br>match=".$xml_ref->{service}->{service_name}->{$service->{$id}->{name}}->{create_service_condition}->{on_field_match}->{enable};
		$buttonchecked{match} = "CHECKED";
	    }
	    else {
		$buttonchecked{host} = "CHECKED";
	    }
	    %checked = ();
	    if ( defined( $FORM_DATA{"createservice_match_$id"} ) ) {
		$checked{ $FORM_DATA{"createservice_match_$id"} } = "SELECTED";
	    }
	    else {

# print "<br>Using create_service_match_$id index ".$xmlfieldname{$xml_ref->{service}->{service_name}->{$service->{$id}->{name}}->{create_service_condition}->{on_field_match}->{field}};
		$checked{
		    $xmlfieldname{
			$xml_ref->{service}->{service_name}->{ $service->{$id}->{name} }->{create_service_condition}->{on_field_match}->{field}
		      }
		  }
		  = "SELECTED";
	    }
	    print "<input class='insightradio' type='radio' name='createservice_$id' value='host' $buttonchecked{host}>For each Host
		    <br><input type='radio' class=insightradio name='createservice_$id' value='match' $buttonchecked{match}>When field
		    <select name=createservice_match_$id class=insight>";
	    for ( my $i = 0 ; $i <= $#filefields ; $i++ ) {
		print "<option class=insight $checked{$i} value='$i'>$filefields[$i]</option>";
	    }
	    print "</select>";
	    if ( !defined( $FORM_DATA{"createservice_regx_$id"} ) ) {
		$FORM_DATA{"createservice_regx_$id"} =
		  $xml_ref->{service}->{service_name}->{ $service->{$id}->{name} }->{create_service_condition}->{on_field_match}
		  ->{field_match_value};
	    }
	    print "<br>matches <input class=insighttext type='text' name='createservice_regx_$id' value='"
	      . $FORM_DATA{"createservice_regx_$id"} . "'>";

	    #print "<td class=insight>".$service->{$id}->{command_line}."</td>";
	    my @args = split /!/, $service->{$id}->{command_line};
	    if ( !defined( $FORM_DATA{"createservice_desc_$id"} ) ) {
		$FORM_DATA{"createservice_desc_$id"} =
		  $xml_ref->{service}->{service_name}->{ $service->{$id}->{name} }->{new_service_description};
	    }
	    if ( $FORM_DATA{"createservice_desc_$id"} ) {
		print "<td class=insight><input class=insighttext type='text' name='createservice_desc_$id' size=30 value='"
		  . $FORM_DATA{"createservice_desc_$id"}
		  . "'></td>";
	    }
	    else {
## print "<td class=insight><input class=insighttext type='text' name='createservice_desc_$id' value='".$service->{$id}->{description}."'></td>";
		## Use the service name as the default. Can also use description if necessary.
		print "<td class=insight><input class=insighttext type='text' name='createservice_desc_$id' size=30 value='"
		  . $service->{$id}->{name}
		  . "'></td>";
	    }

	    my $print_command_line;
	    if ( $service->{$id}->{command_line} ) {
		$print_command_line = $service->{$id}->{command_line};
	    }
	    else {
		my $query = "SELECT name FROM commands where command_id=" . $service->{$id}->{check_command};
		my $sth   = $dbh->prepare($query);
		$sth->execute() or die $@;
		while ( my $row = $sth->fetchrow_hashref() ) {
		    $print_command_line = $$row{name};
		}
		$sth->finish;
	    }
	    print "<input type='hidden' name='createservice_chkid_$id' value='" . $service->{$id}->{check_command} . "'></td>";
	    if ( $#args == 0 ) {
		print "<td class=insight>" . $print_command_line . "</td>";
		print "<td class=insight>No arguments</td>";
	    }
	    else {
		my $cmdname = $args[0];
		## print "<td class=insight>".$cmdname."</td>";
		print "<td class=insight>" . $print_command_line . "</td>";
		print "<td class=insight>";
		for ( my $i = 1 ; $i <= $#args ; $i++ ) {
		    print "Map <b>$args[$i]</b> to field ";
		    print "<select name='matcharg_$id\_$i' class=insight>";
		    print "<option class=insight $checked{''} value=''>No change</option>";
		    %checked = ();
		    if ( defined( $FORM_DATA{"matcharg_$id\_$i"} ) ) {
			$checked{ $FORM_DATA{"matcharg_$id\_$i"} } = "SELECTED";
		    }
		    else {
## print "<br>Using create_service_match_$id index ".$xmlfieldname{$xml_ref->{service}->{service_name}->{$service->{$id}->{name}}->{create_service_condition}->{on_field_match}->{field}};
			$checked{
			    $xmlfieldname{
				$xml_ref->{service}->{service_name}->{ $service->{$id}->{name} }->{command_arg_map}->{argument_file_map}->{$i}
			      }
			  }
			  = "SELECTED";
		    }
		    for ( my $j = 0 ; $j <= $#filefields ; $j++ ) {
			print "<option class=insight $checked{$j} value='$j'>$filefields[$j]</option>";
		    }
		    print "</select><br>";
		}
		print "</td>";
	    }
	    print "</tr>";
	}
	print "</table>";
    }
    print "<br>";
    print "	<INPUT class=graybutton type=button value='Reset' onClick='changePage(\"$thisprogram?cmd=FormEntry\")'> ";
    if ( $FORM_DATA{cmd} eq "Create All Definitions" ) {
	print "<INPUT class=graybutton type=submit value='Test Next Record'  name='cmd'>&nbsp;
	    <INPUT class=graybutton type=submit value='Create All Definitions' name='cmd'>&nbsp;
	    <INPUT class=orangebutton type=submit value='Show Created Definitions' name='cmd'>
	    <INPUT class=orangebutton type=submit value='Export Current Match Criteria' name='cmd'>
	";
    }
    else {
	print "<INPUT class=orangebutton type=submit value='Test Next Record'  name='cmd'>
	    <INPUT class=orangebutton type=submit value='Create All Definitions' name='cmd'>
	    <INPUT class=orangebutton type=submit value='Export Current Match Criteria' name='cmd'>
	";
    }
    print "<br>";
}

sub TestStep {
    my $teststep;
    if ( !$FORM_DATA{teststep} ) {
	$teststep = 1;
    }
    else {
	$teststep = $FORM_DATA{teststep} + 1;
    }
    print "<input type=hidden name=teststep value=$teststep>";
    my @serviceprofileids = split /\0/, $FORM_DATA{serviceprofileid};
    my $service = undef;
    foreach my $spid (@serviceprofileids) {
	my $query = "SELECT * FROM serviceprofile where serviceprofile_id=$spid";
	my $sth   = $dbh->prepare($query);
	$sth->execute() or die $@;
	my @servicename_ids = ();
	while ( my $row = $sth->fetchrow_hashref() ) {
	    push @servicename_ids, $$row{servicename_id};
	}
	$sth->finish;
	foreach my $id (@servicename_ids) {
	    $service->{$id}->{serviceprofile_id} = $spid;
	    my $query = "SELECT * FROM service_names where servicename_id=$id";
	    my $sth   = $dbh->prepare($query);
	    $sth->execute() or die $@;
	    while ( my $row = $sth->fetchrow_hashref() ) {
		$service->{$id}->{name}          = $$row{name};
		$service->{$id}->{description}   = $$row{description};
		$service->{$id}->{command_line}  = $$row{command_line};
		$service->{$id}->{check_command} = $$row{check_command};
	    }
	    $sth->finish;
	}
    }
    my $line;
    my $hostref    = undef;
    my @filefields = ();
    if ( $FORM_DATA{file} ) {
	open( IMPORTFILE, "$csvdirectory/$FORM_DATA{file}" ) or die $!;
	while ( $line = <IMPORTFILE> ) {
	    chomp $line;
	    if ( $line =~ /^\s*#/ ) {
		next;
	    }
	    @filefields = split /,/, $line;
	    last;
	}
	my $reccount = 0;
	while ( $line = <IMPORTFILE> ) {
	    chomp $line;
	    $reccount++;
	    if ( $line =~ /^\s*#/ ) {
		next;
	    }
	    if ( $reccount < $teststep ) {
		## print "<br>File line $reccount: $line";
		next;
	    }

	    # print "<br>File line $reccount: $line";
	    last;
	}
    }
    my @filevalues = split /,/, $line;
    $hostref = process_line( $hostref, $service, $line, \@filefields, \@filevalues );
    print "<p>Test Step";
    print "<table class=insightcontrolpanel cellspacing=0 bordercolor='#FFFFFF'>";
    print "<tr><th class=insight>Test File Record #</th>";
    for ( my $i = 1 ; $i <= $#filefields ; $i++ ) {
	print "<th class=insight>$filefields[$i]</th>";
    }
    print "</tr><tr>";
    print "<td class=insight>$teststep</td>";
    for ( my $i = 1 ; $i <= $#filevalues ; $i++ ) {
	print "<td class=insight>$filevalues[$i]</th>";
    }
    print "</tr></table></p>";

    foreach my $host ( keys %{$hostref} ) {
	print "<p>Host definitions created for this record";
	print "<table class=insightcontrolpanel cellspacing=0>";
	print "<tr><th class=insight>Host Name</th>";
	print "<th class=insight>Host Alias</th>";
	print "<th class=insight>Host IP Address</th>";
	print "<tr>";
	print "<td class=insight>$host</td>";
	print "<td class=insight>" . $hostref->{$host}->{alias} . "</td>";
	print "<td class=insight>" . $hostref->{$host}->{ipaddress} . "</td>";
	print "</tr></table></p>";
	print "<p>Service definitions created for this record";
	print "<table class=insightcontrolpanel cellspacing=0>";
	print "<tr><th class=insight>Host Name</th>";
	print "<th class=insight>Service Description</th>";
	print "<th class=insight>Service Command</th>";
	print "<th class=insight>Service Command Line - Translated</th>";
	print "</tr>";

	foreach my $service ( keys %{ $hostref->{$host}->{services} } ) {
	    print "<tr>";
	    print "<td class=insight>$host</td>";
	    print "<td class=insight>$service</td>";
	    print "<td class=insight>" . $hostref->{$host}->{services}->{$service}->{command_line} . "</td>";
	    print "<td class=insight>" . $hostref->{$host}->{services}->{$service}->{trans_command_line} . "</td>";
	    print "</tr>";
	}
	print "</tr></table></p>";
    }
    return;
}

sub process_line {
    my $hostref    = shift;
    my $service    = shift;
    my $line       = shift;
    my $tmp_ref    = shift;
    my @filefields = @$tmp_ref;
    $tmp_ref = shift;
    my @filevalues = @$tmp_ref;
    $hostref->{ $filevalues[ $FORM_DATA{hostnamefield} ] }->{alias}         = $filevalues[ $FORM_DATA{hostaliasfield} ];
    $hostref->{ $filevalues[ $FORM_DATA{hostnamefield} ] }->{ipaddress}     = $filevalues[ $FORM_DATA{hostipaddressfield} ];
    $hostref->{ $filevalues[ $FORM_DATA{hostnamefield} ] }->{hostprofileid} = $FORM_DATA{hostprofileid};
    my $query = "SELECT * FROM setup WHERE name LIKE 'user%'";
    my $sth   = $dbh->prepare($query);
    $sth->execute() or die $@;
    my %user = ();

    while ( my $row = $sth->fetchrow_hashref() ) {
	if ( $$row{name} =~ /^user/i ) {
	    $user{ $$row{name} } = $$row{value};
	}
    }
    $sth->finish;
    foreach my $id ( keys %{$service} ) {
	if ( !$id ) { next }
	my $tmpregx      = qr/$FORM_DATA{"createservice_regx_$id"}/;
	my $new_servdesc = $FORM_DATA{"createservice_desc_$id"};
	for ( my $i = 0 ; $i <= $#filefields ; $i++ ) {
	    $new_servdesc =~ s/\$$filefields[$i]\$/$filevalues[$i]/g;
	}
	my @args = split /!/, $service->{$id}->{command_line};
	my $new_servcmd = $args[0];
	for ( my $i = 1 ; $i <= $#args ; $i++ ) {
	    if ( $FORM_DATA{"matcharg_$id\_$i"} == 0 ) {    # If first maping option, don't change. Keep argument value
		$new_servcmd .= "!" . $args[$i];
	    }
	    else {                                          # Replace with field value
		$new_servcmd .= "!" . $filevalues[ $FORM_DATA{"matcharg_$id\_$i"} ];
	    }
	}
	my $query = "SELECT * FROM commands where command_id=" . $service->{$id}->{check_command};

	# print "<br>SQL=$query\n";

	my $sth = $dbh->prepare($query);
	$sth->execute() or die $@;
	while ( my $row = $sth->fetchrow_hashref() ) {
	    $service->{$id}->{chk_command_line} = $$row{data};
	}
	$sth->finish;
	my $new_servcmdline = $service->{$id}->{chk_command_line};
	if ( $service->{$id}->{chk_command_line} =~ /\[CDATA\[(.*?)\]]/ ) {
	    $new_servcmdline = $1;
	}

	my $tmp = $filevalues[ $FORM_DATA{hostaddressfield} ];
	$new_servcmdline =~ s/\$HOSTADDRESS\$/$tmp/gi;
	my $tmp = $filevalues[ $FORM_DATA{hostnamefield} ];
	$new_servcmdline =~ s/\$HOSTNAME\$/$tmp/gi;
	my $tmp = $filevalues[ $FORM_DATA{hostaliasfield} ];
	$new_servcmdline =~ s/\$HOSTALIAS\$/$tmp/gi;
	foreach my $key ( keys %user ) {
	    $new_servcmdline =~ s/\$$key\$/$user{$key}/gi;
	}
	for ( my $i = 1 ; $i <= $#args ; $i++ ) {
	    if ( $FORM_DATA{"matcharg_$id\_$i"} != 0 ) {    # If first maping option, don't change. Keep argument value
		$new_servcmdline =~ s/\$ARG$i\$/$filevalues[$FORM_DATA{"matcharg_$id\_$i"}]/g;
	    }
	    else {
		$new_servcmdline =~ s/\$ARG$i\$/$args[$i]/g;
	    }
	}
	if ( ( $FORM_DATA{"createservice_$id"} eq "host" ) or ( $filevalues[ $FORM_DATA{"createservice_match_$id"} ] =~ /$tmpregx/ ) )
	{                                                   #	Create service only if host or match condition
	    $hostref->{ $filevalues[ $FORM_DATA{hostnamefield} ] }->{services}->{$new_servdesc}->{command_line}       = $new_servcmd;
	    $hostref->{ $filevalues[ $FORM_DATA{hostnamefield} ] }->{services}->{$new_servdesc}->{trans_command_line} = $new_servcmdline;
	    $hostref->{ $filevalues[ $FORM_DATA{hostnamefield} ] }->{services}->{$new_servdesc}->{check_command} =
	      $service->{$id}->{check_command};
	    $hostref->{ $filevalues[ $FORM_DATA{hostnamefield} ] }->{services}->{$new_servdesc}->{service_id} = $id;
	    $hostref->{ $filevalues[ $FORM_DATA{hostnamefield} ] }->{services}->{$new_servdesc}->{serviceprofile_id} =
	      $service->{$id}->{serviceprofile_id};
	}
    }
    return $hostref;
}

sub CreateAll {
    my @serviceprofileids = split /\0/, $FORM_DATA{serviceprofileid};
    my $service = undef;
    foreach my $spid (@serviceprofileids) {
	my $query = "SELECT * FROM serviceprofile where serviceprofile_id=$spid";
	my $sth   = $dbh->prepare($query);
	$sth->execute() or die $@;
	my @servicename_ids = ();
	while ( my $row = $sth->fetchrow_hashref() ) {
	    push @servicename_ids, $$row{servicename_id};
	}
	$sth->finish;
	foreach my $id (@servicename_ids) {
	    $service->{$id}->{serviceprofile_id} = $spid;
	    my $query = "SELECT * FROM service_names where servicename_id=$id";
	    my $sth   = $dbh->prepare($query);
	    $sth->execute() or die $@;
	    while ( my $row = $sth->fetchrow_hashref() ) {
		$service->{$id}->{name}          = $$row{name};
		$service->{$id}->{description}   = $$row{description};
		$service->{$id}->{command_line}  = $$row{command_line};
		$service->{$id}->{check_command} = $$row{check_command};
	    }
	    $sth->finish;
	}
    }
    my $line;
    my $hostref    = undef;
    my @filefields = ();
    if ( !$FORM_DATA{file} ) {
	print "<br>ERROR: No import file defined.";
	return;
    }
    open( IMPORTFILE, "$csvdirectory/$FORM_DATA{file}" ) or die $!;
    while ( $line = <IMPORTFILE> ) {
	chomp $line;
	if ( $line =~ /^\s*#/ ) {
	    next;
	}
	@filefields = split /,/, $line;
	last;
    }
    my $reccount = 0;
    while ( $line = <IMPORTFILE> ) {
	chomp $line;
	$reccount++;
	if ( $line =~ /^\s*#/ ) {
	    next;
	}

	# print "<br>File line $reccount: $line";
	my @filevalues = split /,/, $line;
	$hostref = process_line( $hostref, $service, $line, \@filefields, \@filevalues );
    }
    my $query = "DELETE FROM import_hosts";    # clear import_hosts table
    my $sth   = $dbh->prepare($query);
    $sth->execute() or die $@;
    $sth->finish;
    my $query = "DELETE FROM import_services";    # clear import_hosts table
    my $sth   = $dbh->prepare($query);
    $sth->execute() or die $@;
    $sth->finish;
    my $hostcount = 0;

    foreach my $host ( keys %{$hostref} ) {
	my $valuestring =
	    "'$host','"
	  . $hostref->{$host}->{alias} . "','"
	  . $hostref->{$host}->{ipaddress} . "',"
	  . $hostref->{$host}->{hostprofileid};    # need to add hostprofile_id
	my $query = "INSERT INTO import_hosts (name,alias,address,hostprofile_id) VALUES ($valuestring) ";

	# print "<br>$query\n";
	my $sth = $dbh->prepare($query);
	$sth->execute() or die $@;
	$sth->finish;
	$hostcount++;
    }

    my $servicecount = 0;
    foreach my $host ( keys %{$hostref} ) {
	foreach my $service ( keys %{ $hostref->{$host}->{services} } ) {
	    my $valuestring = "(SELECT import_hosts_id FROM import_hosts WHERE name='$host') ,";
	    $valuestring .= $dbh->quote($service) . ",";
	    $valuestring .= $dbh->quote( $hostref->{$host}->{services}->{$service}->{check_command} ) . ",";
	    $valuestring .= $dbh->quote( $hostref->{$host}->{services}->{$service}->{command_line} ) . ",";
	    $valuestring .= $dbh->quote( $hostref->{$host}->{services}->{$service}->{trans_command_line} ) . ",";
	    $valuestring .= $dbh->quote( $hostref->{$host}->{services}->{$service}->{service_id} ) . ",";
	    $valuestring .= $dbh->quote( $hostref->{$host}->{services}->{$service}->{serviceprofile_id} );
	    my $query =
"INSERT INTO import_services (import_hosts_id,description,check_command_id,command_line,command_line_trans,servicename_id,serviceprofile_id) "
	      . "VALUES ( $valuestring) ";

	    #print "<br>$query";
	    my $sth = $dbh->prepare($query);
	    $sth->execute() or print "<p>Error: $@<br>$query</p>";
	    $sth->finish;
	    $servicecount++;
	}
    }
    print "<p>$hostcount hosts created.\n";
    print "<br>$servicecount services created.\n</p>";

    # print "<INPUT class=orangebutton type=submit value='Show Created Definitions' name='cmd'>";
    return;
}

sub ShowCreated {

    #	foreach my $key (keys %FORM_DATA) {
    #		if (($key ne "cmd") and ($key ne "hostname") and ($key ne "hostalias") and ($key ne "hostaddress") and ($key !~ /^mod/)
    #			and ($key !~ /^del[sh]$/) and ($key !~ /^sel[sh]$/) ) {
    #			my @tmps=split /\0/,$FORM_DATA{$key};
    #			my %seen=();
    #			foreach my $tmp (@tmps) {
    #				print "<input type=hidden name='$key' value='".$tmp."'>\n" unless ($seen{$tmp}++);
    #			}
    #		}
    #	}

    print "<table class=insightcontrolpanel cellspacing=0>";
    print "<tr><td class=insight>Imported File</td>";
    print "<td class=insight>$csvdirectory/$FORM_DATA{file}</td>";
    print "<input type=hidden name=file value=$FORM_DATA{file}>";
    print "</tr></table";
    print "<br>";
    print "<INPUT class=graybutton type=button value='Reset' onClick='changePage(\"$thisprogram?cmd=FormEntry\")'>
	    <INPUT class=graybutton type=submit value='Test Next Record'  name='cmd'>
	    <INPUT class=orangebutton type=submit value='Modify Selected Hosts/Services'  name='cmd'>
	    <INPUT class=orangebutton type=submit value='Import Definitions' name='cmd'>
	";
    print "<br>\n";
    print "<p>Host definitions created\n";
    print "<table class=insightcontrolpanel cellspacing=0>\n";
    print "<tr><th class=insight>Edit</th>";
    print "<th class=insight>Delete</th>";
    print "<th class=insight>Host Name</th>";
    print "<th class=insight>Host Alias</th>";
    print "<th class=insight>Host IP Address</th>\n";
    my $query = "SELECT * FROM import_hosts";    # clear import_hosts table
    my $sth   = $dbh->prepare($query);
    $sth->execute() or die $@;

    while ( my $row = $sth->fetchrow_hashref() ) {
	print "<tr>\n";
	## print "<td class=insight>".$$row{import_hosts_id}."</td>";
	print "<td class=insight><input type=CHECKBOX NAME=selh value='" . $$row{import_hosts_id} . "'></td>\n";
	print "<td class=insight><input type=CHECKBOX NAME=delh value='" . $$row{import_hosts_id} . "'></td>\n";
	print "<td class=insight>" . $$row{name} . "</td>\n";
	print "<td class=insight>" . $$row{alias} . "</td>\n";
	print "<td class=insight>" . $$row{address} . "</td>\n";
    }
    $sth->finish;
    print "</tr></table>\n";
    print "</p>";
    print "<p>Service definitions created\n";
    print "<table class=insightcontrolpanel cellspacing=0>\n";
    print "<tr><th class=insight>Edit</th>\n";
    print "<th class=insight>Delete</th>\n";
    print "<th class=insight>Host Name</th>\n";
    print "<th class=insight>Service Description</th>\n";
    print "<th class=insight>Service Command</th>\n";
    print "<th class=insight>Service Command Line - Translated</th>\n";
    print "</tr>";
    my $query =
        "SELECT s.import_services_id,h.name,s.description,s.command_line,s.command_line_trans FROM import_services as s, import_hosts as h "
      . "WHERE h.import_hosts_id=s.import_hosts_id order by h.name,s.description";
    my $sth = $dbh->prepare($query);
    $sth->execute() or die $@;

    while ( my $row = $sth->fetchrow_hashref() ) {
	print "<tr>\n";
	## print "<td class=insight>".$$row{import_services_id}."</td>";
	print "<td class=insight><input type=CHECKBOX NAME=sels value='" . $$row{import_services_id} . "'></td>\n";
	print "<td class=insight><input type=CHECKBOX NAME=dels value='" . $$row{import_services_id} . "'></td>\n";

	print "<td class=insight>" . $$row{name} . "</td>\n";
	print "<td class=insight>" . $$row{description} . "</td>\n";
	print "<td class=insight>" . $$row{command_line} . "</td>\n";
	print "<td class=insight>" . $$row{command_line_trans} . "</td>\n";

	## print "<td class=insight>".$hostref->{$$row{name}}->{services}->{$$row{description}}->{command_line}."</td>\n";
	print "</tr>\n";
    }
    $sth->finish;
    print "</tr></table>\n";
    print "</p>";

    return;
}

sub Modify {
    foreach my $key ( keys %FORM_DATA ) {
	if (    ( $key ne "cmd" )
	    and ( $key ne "hostname" )
	    and ( $key ne "hostalias" )
	    and ( $key ne "hostaddress" )
	    and ( $key !~ /^mod/ )
	    and ( $key !~ /^del[sh]$/ ) )
	{
	    my @tmps = split /\0/, $FORM_DATA{$key};
	    my %seen = ();
	    foreach my $tmp (@tmps) {
		print "<input type=hidden name='$key' value='" . $tmp . "'>\n" unless ( $seen{$tmp}++ );
	    }
	}
    }
    print "<table class=insightcontrolpanel cellspacing=0>";
    print "<tr><td class=insight>Imported File</td>";
    print "<td class=insight>$csvdirectory/$FORM_DATA{file}</td>";
    print "<input type=hidden name=file value=$FORM_DATA{file}>";
    print "</tr></table";
    print "<br>";
    print "	<INPUT class=graybutton type=button value='Reset' onClick='changePage(\"$thisprogram?cmd=FormEntry\")'>
		<INPUT class=orangebutton type=submit value='Show Created Definitions' name='cmd'>
	";

    if ( $FORM_DATA{delh} or $FORM_DATA{dels} or $FORM_DATA{selh} or $FORM_DATA{sels} ) {
	print "<INPUT class=orangebutton type=submit value='Implement Modifications'  name='cmd'>";
    }

    # Process selected deletes
    my @hostids = split /\0/, $FORM_DATA{delh};    # Get selected host ids
    my @servids = split /\0/, $FORM_DATA{dels};    # Get selected host ids
    print "<br>";

    #	print "<br>selh=".$FORM_DATA{selh};
    #	print "<br>sels=".$FORM_DATA{sels};
    #	print "<br>delh=".$FORM_DATA{delh};
    #	print "<br>dels=".$FORM_DATA{dels};
    foreach my $id (@hostids) {
	my $query = "delete from import_services WHERE import_hosts_id=$id ";
	my $sth   = $dbh->prepare($query);
	$sth->execute() or die $@;
	$sth->finish;
	my $query = "delete from import_hosts WHERE import_hosts_id=$id ";
	my $sth   = $dbh->prepare($query);
	$sth->execute() or die $@;
	$sth->finish;
    }
    if ( $#hostids >= 0 ) {
	print "<p>Deleted " . ( $#hostids + 1 ) . " hosts.</p>";
    }
    foreach my $id (@servids) {
	my $query = "delete from import_services WHERE import_services_id=$id ";
	my $sth   = $dbh->prepare($query);
	$sth->execute() or die $@;
	$sth->finish;
    }
    if ( $#servids >= 0 ) {
	print "<p>Deleted " . ( $#servids + 1 ) . " services.</p>";
    }

    # Process selected updates
    if ( !$FORM_DATA{selh} and !$FORM_DATA{sels} ) {
	return;
    }
    my @hostids = split /\0/, $FORM_DATA{selh};    # Get selected host ids
    my @servids = split /\0/, $FORM_DATA{sels};    # Get selected host ids
    if ( $#hostids >= 0 ) {
	print "<P>Host definitions";
	print "<table class=insightcontrolpanel cellspacing=0>";
	print "<tr><th class=insight>ID</th>";
	print "<th class=insight>Host Name</th>";
	print "<th class=insight>Host Alias</th>";
	print "<th class=insight>Host IP Address</th>";
	my $instring = undef;
	foreach my $id (@hostids) {
	    $instring .= "$id,";
	}
	$instring =~ s/,$//;                       # Get rid of trailing ,
	my $query = "SELECT * FROM import_hosts WHERE import_hosts_id IN ($instring)";

	# print "<tr><td>$query</td></tr>";
	my $sth = $dbh->prepare($query);
	$sth->execute() or die $@;
	while ( my $row = $sth->fetchrow_hashref() ) {
	    my $id = $$row{import_hosts_id};
	    print "<tr>";
	    print "<td class=insight>$id</td>";
	    print "<td class=insight><input class=insighttext type=text size=50 maxlength=255 name=hostname_$id value='"
	      . $$row{name}
	      . "'></td>";
	    print "<td class=insight><input class=insighttext type=text size=50 maxlength=50 name=hostalias_$id value='"
	      . $$row{alias}
	      . "'></td>";
	    print "<td class=insight><input class=insighttext type=text size=50 maxlength=50 name=hostaddress_$id value='"
	      . $$row{address}
	      . "'></td>";
	}
	$sth->finish;
	print "</tr></table></p>";
    }

    #	print "<br>";
    if ( $#servids >= 0 ) {
	print "<p>Service definitions created";
	print "<table class=insightcontrolpanel cellspacing=0>";
	print "<tr><th class=insight>ID</th>";
	print "<th class=insight>Host Name</th>";
	print "<th class=insight>Service Description</th>";
	print "<th class=insight>Service Command</th>";
	print "</tr>";
	my $instring = undef;

	foreach my $id (@servids) {
	    $instring .= "$id,";
	}
	$instring =~ s/,$//;    # Get rid of trailing ,
	my $query = "SELECT import_hosts_id,name from import_hosts ";
	my $sth   = $dbh->prepare($query);
	$sth->execute() or die $@;
	my %hostslist = ();
	while ( my $row = $sth->fetchrow_hashref() ) {
	    $hostslist{ $$row{import_hosts_id} } = $$row{name};
	}
	$sth->finish;
	my $query = "SELECT distinct(check_command_id),command_line,servicename_id from import_services ";
	my $sth   = $dbh->prepare($query);
	$sth->execute() or die $@;
	my %chkcmdlist     = ();
	my %srvid_chkcmdid = ();
	while ( my $row = $sth->fetchrow_hashref() ) {
	    $chkcmdlist{ $$row{check_command_id} } = $$row{command_line};
	    ## $srvid_chkcmdid{$$row{servicename_id}} = $$row{check_command_id};
	}
	$sth->finish;
	my $query =
	    "SELECT s.import_services_id,h.name,s.description,s.command_line,s.check_command_id FROM import_services as s, import_hosts as h "
	  . "WHERE h.import_hosts_id=s.import_hosts_id AND "
	  . "import_services_id IN ($instring) "
	  . "order by h.name,s.description";

	# print "<tr><td>$query</td></tr>";
	my $sth = $dbh->prepare($query);
	$sth->execute() or die $@;
	while ( my $row = $sth->fetchrow_hashref() ) {
	    my $id = $$row{import_services_id};
	    print "<tr>";
	    print "<td class=insight>$id</td>";
	    print "<td class=insight><select class=insight name=modservhost_$id>";
	    my %checked = ();
	    $checked{$id} = "SELECTED";
	    foreach my $key ( keys %hostslist ) {
		print "<option class=insight $checked{$key} value='$key'>$hostslist{$key}</option>";
	    }
	    print "</select>";
	    print "</td>";
	    print "<td class=insight><input class=insighttext type=text size=50 name=modservdesc_$id value='" . $$row{description} . "'></td>";

	    ## print "<input type=hidden name=modservcmdline_$id value='".$$row{command_line}."'>";
	    print "<td class=insight><select class=insight name=modservcmd_$id>";
	    my %checked = ();
	    my @args;
	    $checked{ $$row{check_command_id} } = "SELECTED";
	    @args = split /!/, $$row{command_line};

	    ## print "<input class=insight  type=text size=50 name=servcmd_$id value='".$$row{command_line}."'>";
	    foreach my $key ( keys %chkcmdlist ) {
		my @args2 = split /!/, $chkcmdlist{$key};
		print "<option class=insight $checked{$key} value='$key'>" . $args2[0] . "</option>";
	    }
	    print "</select>";
	    my @args = split /!/, $$row{command_line};
	    my $tmpstring = "";
	    for ( my $i = 1 ; $i <= $#args ; $i++ ) {
		$tmpstring .= "$args[$i]!";
	    }
	    $tmpstring =~ s/!$//;
	    print "&nbsp;&nbsp;Args:<input class=insighttext type=text size=50 maxsize=255 name=modservarg_$id value='$tmpstring'>";

	    #	for (my $i=1;$i<=$#args;$i++) {
	    #		print "<br>Arg$i: <input class=insighttext type=text size=20 name=modservcmd_$id\_arg_$i value='$args[$i]'> ";
	    #	}
	    print "</td>";
	    print "</tr>";
	}
	$sth->finish;
	print "</tr></table></p>";
    }
    return;
}

sub ImplementMods {
    foreach my $key ( keys %FORM_DATA ) {
	if (    ( $key ne "cmd" )
	    and ( $key ne "hostname" )
	    and ( $key ne "hostalias" )
	    and ( $key ne "hostaddress" )
	    and ( $key !~ /^mod/ )
	    and ( $key !~ /^sel[sh]$/ )
	    and ( $key !~ /^del[sh]$/ ) )
	{
	    my @tmps = split /\0/, $FORM_DATA{$key};
	    my %seen = ();
	    foreach my $tmp (@tmps) {
		print "<input type=hidden name='$key' value='" . $tmp . "'>\n" unless ( $seen{$tmp}++ );
	    }
	}
    }

    my @hostids = split /\0/, $FORM_DATA{selh};    # Get selected host ids
    my @servids = split /\0/, $FORM_DATA{sels};    # Get selected host ids
    foreach my $id (@hostids) {
	my $query =
	    "UPDATE import_hosts SET " 
	  . "name='"
	  . $FORM_DATA{"hostname_$id"}
	  . "',alias='"
	  . $FORM_DATA{"hostalias_$id"}
	  . "',address='"
	  . $FORM_DATA{"hostaddress_$id"}
	  . "' WHERE import_hosts_id=$id ";
	print "<br>query=$query" if $debug;
	my $sth = $dbh->prepare($query);
	$sth->execute() or die $@;
	$sth->finish;
    }
    print "<br>sels=" . $FORM_DATA{sels} if $debug;
    foreach my $id (@servids) {
	print "<br>checking selsid=$id" if $debug;
	my $chk_command_name = undef;
	my $chk_command_line = undef;
	my $query            = "SELECT * FROM commands where command_id=" . $FORM_DATA{"modservcmd_$id"};
	my $sth              = $dbh->prepare($query);
	$sth->execute() or die $@;
	while ( my $row = $sth->fetchrow_hashref() ) {
	    $chk_command_name = $$row{name};
	    $chk_command_line = $$row{data};
	}
	$sth->finish;
	my $new_servcmdline = $chk_command_line;
	if ( $chk_command_line =~ /\[CDATA\[(.*?)\]]/ ) {
	    $new_servcmdline = $1;
	}
	$chk_command_name .= "!" . $FORM_DATA{"modservarg_$id"};
	my @args = split /!/, $FORM_DATA{"modservarg_$id"};
	for ( my $i = 0 ; $i <= $#args ; $i++ ) {
	    my $j = $i + 1;
	    $new_servcmdline =~ s/\$ARG$j\$/$args[$i]/g;
	}

	my $query =
	    "UPDATE import_services SET "
	  . "import_hosts_id="
	  . $dbh->quote( $FORM_DATA{"modservhost_$id"} )
	  . ",description="
	  . $dbh->quote( $FORM_DATA{"modservdesc_$id"} )
	  . ",check_command_id="
	  . $dbh->quote( $FORM_DATA{"modservcmd_$id"} )
	  . ",command_line="
	  . $dbh->quote($chk_command_name)
	  . ",command_line_trans="
	  . $dbh->quote($new_servcmdline)
	  . " WHERE import_services_id='$id'";
	print "<br>query=$query" if $debug;
	my $sth = $dbh->prepare($query);
	$sth->execute() or die $@;
	$sth->finish;
    }
    print "<br>" . ( $#hostids + 1 ) . " hosts and " . ( $#servids + 1 ) . " services updated.";
    print "<br><INPUT class=orangebutton type=submit value='Show Created Definitions' name='cmd'>";
    return;
}

sub ImportForm {
    foreach my $key ( keys %FORM_DATA ) {
	if (    ( $key ne "cmd" )
	    and ( $key ne "hostname" )
	    and ( $key ne "hostalias" )
	    and ( $key ne "hostaddress" )
	    and ( $key !~ /^mod/ )
	    and ( $key !~ /^del[sh]$/ )
	    and ( $key !~ /^sel[sh]$/ ) )
	{
	    my @tmps = split /\0/, $FORM_DATA{$key};
	    my %seen = ();
	    foreach my $tmp (@tmps) {
		print "<input type=hidden name='$key' value='" . $tmp . "'>\n" unless ( $seen{$tmp}++ );
	    }
	}
    }

    print "<table class=insightcontrolpanel cellspacing=0>";
    print "<tr><td class=insight>Imported File</td>";
    print "<td class=insight>$csvdirectory/$FORM_DATA{file}</td>";
    print "<input type=hidden name=file value=$FORM_DATA{file}>";
    print "</tr></table";
    print "<br>";
    print "
	<INPUT class=graybutton type=button value='Reset' onClick='changePage(\"$thisprogram?cmd=FormEntry\")'>
	<INPUT class=graybutton type=submit value='Show Created Definitions'  name='cmd'>
	<INPUT class=orangebutton type=submit value='Commit' name='cmd'>";
    print "<br><br>\n";
    print "<table class=insightcontrolpanel cellspacing=0>\n";
    print "<tr><th class=insight>Host Import Options</th>";
    my %checked = ();

    if ( !$FORM_DATA{importhostoptions} ) {
	$checked{merge} = "CHECKED";
    }
    else {
	$checked{ $FORM_DATA{importhostoptions} } = "CHECKED";
    }
    print "<tr><td class=insight><input type=radio name=importhostoptions value=merge "
      . $checked{merge}
      . ">Don't import if host definition exists</td>";
    print "<tr><td class=insight><input type=radio name=importhostoptions value=overwrite "
      . $checked{overwrite}
      . "> Overwrite duplicate host definition</td>";
    print "</tr></table>\n";
    print "<br><br>";
    my %checked = ();
    if ( !$FORM_DATA{importserviceoptions} ) {
	$checked{merge} = "CHECKED";
    }
    else {
	$checked{ $FORM_DATA{importserviceoptions} } = "CHECKED";
    }
    print "<table class=insightcontrolpanel cellspacing=0>\n";
    print "<tr><th class=insight>Service Import Options</th>";
    print "<tr><td class=insight><input type=radio name=importserviceoptions value=merge "
      . $checked{merge}
      . ">Don't import if service definition exists</td>";
    print "<tr><td class=insight><input type=radio name=importserviceoptions value=overwrite "
      . $checked{overwrite}
      . "> Overwrite duplicate service definition</td>";
    print "</tr></table>\n";
    return;
}

sub Commit {
    foreach my $key ( keys %FORM_DATA ) {
	if (    ( $key ne "cmd" )
	    and ( $key ne "hostname" )
	    and ( $key ne "hostalias" )
	    and ( $key ne "hostaddress" )
	    and ( $key !~ /^mod/ )
	    and ( $key !~ /^del[sh]$/ )
	    and ( $key !~ /^sel[sh]$/ ) )
	{
	    my @tmps = split /\0/, $FORM_DATA{$key};
	    my %seen = ();
	    foreach my $tmp (@tmps) {
		print "<input type=hidden name='$key' value='" . $tmp . "'>\n" unless ( $seen{$tmp}++ );
	    }
	}
    }

    print "<br>Starting Commit module. Cmd=$FORM_DATA{cmd}<br>\n" if $debug;

    #	print STDERR "Form data=$form_info\n";
    #	print STDERR "Referer=$ENV{HTTP_REFERER}\n";

    # The following 4 lines are used to get around strange Firefox browser bug where cgi is submitted twice.
    # The second time, the referer is from the URL with cmd=Commit, so we will skip.  This is not a problem with IE.
    #	if ($ENV{HTTP_REFERER} =~ /cmd=Commit/) {
    #		print STDERR "Leaving Commit module\n";
    #		return;
    #	}

    print "<table class=insightcontrolpanel cellspacing=0>";
    print "<tr><td class=insight>Imported File</td>";
    print "<td class=insight>$csvdirectory/$FORM_DATA{file}</td>";
    print "</tr></table>";
    print "<br>";
    if ($debug) {
	foreach my $n ( keys %FORM_DATA ) {
	    print "<br>$n $FORM_DATA{$n}<br>";
	}
    }
    print "<INPUT class=orangebutton type=button value='Reset' onClick='changePage(\"$thisprogram?cmd=FormEntry\")'>
	    <INPUT class=graybutton type=submit value='Show Created Definitions'  name='cmd'>
	";
    print "<br>\n";
    print "<p>\n";
    my $hostref = undef;
    my $query   = "SELECT * FROM import_hosts";
    my $sth     = $dbh->prepare($query);
    $sth->execute() or die $@;
    my $hostupdate = 0;

    if ( $FORM_DATA{importhostoptions} eq "overwrite" ) {
	$hostupdate = 1;
    }
    my $serviceupdate = 0;
    if ( $FORM_DATA{importserviceoptions} eq "overwrite" ) {
	$serviceupdate = 1;
    }
    my %host_ids = ();
    my $host_ref = undef;
    while ( my $row = $sth->fetchrow_hashref() ) {
	$host_ref->{ $$row{'name'} }->{alias}           = $$row{'alias'};
	$host_ref->{ $$row{'name'} }->{address}         = $$row{'address'};
	$host_ref->{ $$row{'name'} }->{hostprofile_id}  = $$row{'hostprofile_id'};
	$host_ref->{ $$row{'name'} }->{import_hosts_id} = $$row{'import_hosts_id'};
    }
    $sth->finish;
    print "<br>update host? $hostupdate<br>" if $debug;
    foreach my $key ( keys %{$host_ref} ) {
	print "<br>Host $key<br" if $debug;
	my ( $host_id, $messages ) = Import->import_host(
	    $key,
	    $host_ref->{$key}->{alias},
	    $host_ref->{$key}->{address},
	    $host_ref->{$key}->{hostprofile_id}, $hostupdate
	);
	$host_ids{ $host_ref->{$key}->{import_hosts_id} } = $host_id;
	foreach my $line ( @{$messages} ) {
	    print "<br>Hostid $host_id, Msg: $line";
	    ## print STDERR "Hostid $host_id, Msg: $line\n";
	}
    }

    my $sth = $dbh->prepare($query);
    $sth->execute() or print "<br>Error: $@<br>";
    $sth->finish;

    print "<br>update service? $serviceupdate<br>" if $debug;

    my $query = "SELECT * FROM import_services as s, import_hosts as h WHERE h.import_hosts_id=s.import_hosts_id";
    my $sth   = $dbh->prepare($query);
    $sth->execute() or print "<br>Error: $@<br>";
    while ( my $row = $sth->fetchrow_hashref() ) {
	## print "<br>Importing hostid ".$host_ids{$$row{'import_hosts_id'}}.": service $$row{'servicename_id'},$$row{serviceprofile_id},$$row{check_command_id},$$row{command_line}";
	## print STDERR "<br>Importing hostid ".$host_ids{$$row{'import_hosts_id'}}.": service $$row{'servicename_id'},$$row{serviceprofile_id},$$row{check_command_id},$$row{command_line}\n\n";
	print "\n<br>Host $host_ids{$$row{'import_hosts_id'}}<br>" if $debug;

	if ( $host_ids{ $$row{'import_hosts_id'} } ) {
	    my @messages = Import->import_service(
		$host_ids{ $$row{'import_hosts_id'} },
		$$row{'servicename_id'},
		$$row{'serviceprofile_id'},
		$$row{'check_command_id'},
		$$row{'command_line'}, $serviceupdate, $$row{'description'}
	    );    #       Add service name as 7th argument if necessary
	    foreach my $line (@messages) {
		print "<br>$line";
		## print STDERR "$line";
	    }
	}
    }
    $sth->finish;
    print "</p>";
    exit;
    return;
}

sub ExportMatch {
    if ( !$FORM_DATA{hostprofileid} ) {
	print "<p>Error: No host profile specified.</p>";
	return;
    }
    my $hostprofilename = undef;
    my $query           = "SELECT ph.name,phps.serviceprofile_id FROM profiles_host as ph,profile_host_profile_service as phps "
      . "WHERE ph.hostprofile_id=$FORM_DATA{hostprofileid} and ph.hostprofile_id=phps.hostprofile_id";
    my $sth = $dbh->prepare($query);
    $sth->execute() or die $@;
    my @serviceprofile_ids = ();
    while ( my $row = $sth->fetchrow_hashref() ) {
	$hostprofilename = $$row{name};
	push @serviceprofile_ids, $$row{serviceprofile_id};
    }
    $sth->finish;
    my @serviceprofileids = split /\0/, $FORM_DATA{serviceprofileid};
    my $service = undef;
    foreach my $spid (@serviceprofileids) {
	my $query = "SELECT * FROM serviceprofile where serviceprofile_id=$spid";
	my $sth   = $dbh->prepare($query);
	$sth->execute() or die $@;
	my @servicename_ids = ();
	while ( my $row = $sth->fetchrow_hashref() ) {
	    push @servicename_ids, $$row{servicename_id};
	}
	$sth->finish;
	foreach my $id (@servicename_ids) {
	    $service->{$id}->{serviceprofile_id} = $spid;
	    my $query = "SELECT * FROM service_names where servicename_id=$id";
	    my $sth   = $dbh->prepare($query);
	    $sth->execute() or die $@;
	    while ( my $row = $sth->fetchrow_hashref() ) {
		$service->{$id}->{name}          = $$row{name};
		$service->{$id}->{description}   = $$row{description};
		$service->{$id}->{command_line}  = $$row{command_line};
		$service->{$id}->{check_command} = $$row{check_command};
	    }
	    $sth->finish;
	}
    }

    my @filefields = ();
    if ( $FORM_DATA{file} ) {
	open( IMPORTFILE, "$csvdirectory/$FORM_DATA{file}" ) or die $!;
	while ( my $line = <IMPORTFILE> ) {
	    chomp $line;
	    if ( $line =~ /^\s*#/ ) {
		next;
	    }
	    @filefields = split /,/, $line;
	    last;
	}
    }
    my $xmlstring = "<groundwork_import_match_configuration>\n";
    $xmlstring .= "<host_profile name=\"$hostprofilename\">\n";
    my $i = 1;
    foreach my $field_name (@filefields) {
	$xmlstring .= "<field_name order=\"$i\">$field_name</field_name>\n";
	$i++;
    }

    $xmlstring .= "<host_name_field><![CDATA[" . $filefields[ $FORM_DATA{hostnamefield} ] . "]]></host_name_field>\n";
    $xmlstring .= "<host_alias_field><![CDATA[" . $filefields[ $FORM_DATA{hostaliasfield} ] . "]]></host_alias_field>\n";
    $xmlstring .= "<host_ipaddress_field>" . $filefields[ $FORM_DATA{hostipaddressfield} ] . "</host_ipaddress_field>\n";
    foreach my $id ( keys %{$service} ) {
	if ( !$id ) { next }
	$xmlstring .= "<service>\n";
	$xmlstring .= "<service_name><![CDATA[" . $service->{$id}->{name} . "]]></service_name>\n";
	$xmlstring .= "<create_service_condition>\n";
	if ( $FORM_DATA{"createservice_$id"} eq "host" ) {
	    $xmlstring .= "<on_every_host>1</on_every_host>\n";
	}
	else {
	    $xmlstring .= "<on_every_host></on_every_host>\n";
	    $xmlstring .= "<on_field_match>\n";
	    $xmlstring .= "<enable>1</enable>\n";
	    $xmlstring .= "<field><![CDATA[" . $filefields[ $FORM_DATA{"createservice_match_$id"} ] . "]]></field>\n";
	    $xmlstring .= "<field_match_value><![CDATA[" . $FORM_DATA{"createservice_regx_$id"} . "]]></field_match_value>\n";
	    $xmlstring .= "</on_field_match>\n";
	}
	$xmlstring .= "</create_service_condition>\n";
	$xmlstring .= "<new_service_description><![CDATA[" . $FORM_DATA{"createservice_desc_$id"} . "]]></new_service_description>\n";
	$xmlstring .= "<command_arg_map>\n";

	my @args = split /!/, $service->{$id}->{command_line};
	my $new_servcmd = $args[0];
	for ( my $i = 1 ; $i <= $#args ; $i++ ) {
	    if ( $FORM_DATA{"matcharg_$id\_$i"} == 0 ) {    # If first maping option, don't change. Keep argument value
		$xmlstring .= "<argument_file_map number=\"$i\"></argument_file_map>\n";
	    }
	    else {                                          # Replace with field value
		$xmlstring .=
		  "<argument_file_map number=\"$i\"><![CDATA[" . $filefields[ $FORM_DATA{"matcharg_$id\_$i"} ] . "]]></argument_file_map>\n";
	    }
	}
	$xmlstring .= "</command_arg_map>\n";
	$xmlstring .= "</service>\n";
    }
    $xmlstring .= "</host_profile>\n";
    $xmlstring .= "</groundwork_import_match_configuration>\n";

    open( OUT, ">$csvdirectory/csvmatch_$hostprofilename.xml" ) or die "ERROR Can't open file $csvdirectory/csvmatch_$hostprofilename.xml";
    print OUT $xmlstring;
    close OUT;
    $xmlstring =~ s/</&lt;/g;
    print "<p>The following XML string was written to directory $csvdirectory in file csvmatch_$hostprofilename.xml.</p>";
    print "<p><PRE>$xmlstring</PRE>";
    return;
}

sub parse_xml {
    my $data      = shift;
    my $xml_ref   = undef;
    my $msgstring = undef;
    if ($data) {
	eval {
	    my $parser = XML::LibXML->new(
		ext_ent_handler => sub { die "INVALID FORMAT: external entity references are not allowed in XML documents.\n" },
		no_network      => 1
	    );
	    my $doc = undef;
	    eval { $doc = $parser->parse_string($data); };
	    if ($@) {
		my ( $package, $file, $line ) = caller;
		print STDERR $@, " called from $file line $line.";
		## FIX LATER:  HTMLifying here, along with embedded markup in print() output, is something of a hack,
		## as it presumes a context not in evidence.  But it's necessary in the browser context.
		$@ = HTML::Entities::encode($@);
		$@ =~ s/\n/<br>/g;
		if ( $@ =~ s/external entity callback died: // || $@ =~ /external entity references are not allowed/ ) {
		    ## First undo the effect of the croak() call in XML::LibXML.
		    $@ =~ s/ at \S+ line \d+<br>//;
		    print "<br>Bad XML string (parse_xml):<br>$@";
		}
		elsif ( $@ =~ /Attempt to load network entity/ ) {
		    print
"<br>Bad XML string (parse_xml):<br>INVALID FORMAT: non-local entity references are not allowed in XML documents.<pre>$@</pre>";
		}
		else {
		    print "<br>Bad XML string (parse_xml):<br>$@ called from $file line $line.";
		}
	    }
	    else {
		my @nodes = $doc->findnodes("groundwork_import_match_configuration");
		foreach my $node (@nodes) {
		    foreach my $hostprof ( $node->getChildnodes ) {
			if ( $hostprof->hasAttributes() ) {
			    $xml_ref->{host_profile_name} = $hostprof->getAttribute('name');
			    $msgstring .= "<br>Processing host profile name=\"" . $xml_ref->{host_profile_name} . "\"\n";
			}
			else {
			    $msgstring .= "<br>Processing host profile with no name attribute\n";
			}
			foreach my $childnode ( $hostprof->findnodes("field_name") ) {
			    if ( $childnode->hasAttributes() ) {
				$xml_ref->{field_name}->{ $childnode->getAttribute('order') } = $childnode->textContent;
			    }
			}
			foreach my $childnode ( $hostprof->findnodes("host_name_field") ) {
			    $xml_ref->{host_name_field} = $childnode->textContent;
			    $msgstring .= "<br>Setting host name=\"" . $xml_ref->{host_name_field} . "\"\n";
			}
			foreach my $childnode ( $hostprof->findnodes("host_alias_field") ) {
			    $xml_ref->{host_alias_field} = $childnode->textContent;
			    $msgstring .= "<br>Setting host name=\"" . $xml_ref->{host_alias_field} . "\"\n";
			}
			foreach my $childnode ( $hostprof->findnodes("host_ipaddress_field") ) {
			    $xml_ref->{host_ipaddress_field} = $childnode->textContent;
			    $msgstring .= "<br>Setting host name=\"" . $xml_ref->{host_ipaddress_field} . "\"\n";
			}
			foreach my $childnode ( $hostprof->findnodes("service") ) {
			    my $service_name;
			    foreach my $childnode2 ( $childnode->findnodes("service_name") ) {
				$service_name = $childnode2->textContent;
				$msgstring .= "<br>Setting service name=\"" . $service_name . "\"\n";
			    }
			    foreach my $childnode2 ( $childnode->findnodes("create_service_condition") ) {
				foreach my $childnode3 ( $childnode2->findnodes("on_every_host") ) {
				    $xml_ref->{service}->{service_name}->{$service_name}->{create_service_condition}->{on_every_host} =
				      $childnode3->textContent;
				    $msgstring .=
				      "<br>Setting service name=\"" . $service_name . "\", on_every_host=" . $childnode3->textContent . "\n";
				}
				foreach my $childnode3 ( $childnode2->findnodes("on_field_match") ) {
				    foreach my $childnode4 ( $childnode3->findnodes("enable") ) {
					$xml_ref->{service}->{service_name}->{$service_name}->{create_service_condition}->{on_field_match}
					  ->{enable} = $childnode4->textContent;
					$msgstring .=
					    "<br>Setting service name=\""
					  . $service_name
					  . "\", on_field_match="
					  . $childnode4->textContent . "\n";
				    }
				    foreach my $childnode4 ( $childnode3->findnodes("field") ) {
					$xml_ref->{service}->{service_name}->{$service_name}->{create_service_condition}->{on_field_match}
					  ->{field} = $childnode4->textContent;
					$msgstring .=
					  "<br>Setting service name=\"" . $service_name . "\", on_field=" . $childnode4->textContent . "\n";
				    }
				    foreach my $childnode4 ( $childnode3->findnodes("field_match_value") ) {
					$xml_ref->{service}->{service_name}->{$service_name}->{create_service_condition}->{on_field_match}
					  ->{field_match_value} = $childnode4->textContent;
					$msgstring .=
					    "<br>Setting service name=\""
					  . $service_name
					  . "\", on_field_value="
					  . $childnode4->textContent . "\n";
				    }
				}
			    }    # End create_service_condition
			    foreach my $childnode2 ( $childnode->findnodes("new_service_description") ) {
				$xml_ref->{service}->{service_name}->{$service_name}->{new_service_description} = $childnode2->textContent;
			    }

			    foreach my $childnode2 ( $childnode->findnodes("command_arg_map") ) {
				foreach my $childnode3 ( $childnode2->findnodes("argument_file_map") ) {
				    if ( $childnode3->hasAttributes() ) {
					$xml_ref->{service}->{service_name}->{$service_name}->{command_arg_map}->{argument_file_map}
					  ->{ $childnode3->getAttribute('number') } = $childnode3->textContent;
					$msgstring .=
					    "<br>Setting service name=\""
					  . $service_name
					  . "\", command arg "
					  . $childnode3->getAttribute('number')
					  . "field="
					  . $childnode3->textContent . "\n";
				    }
				}
			    }    # End command_arg_map
			}
		    }
		}
	    }
	};

	# print $msgstring;
	if ($@) {
	    print "<br>Error parsing: $@\n$data\n";
	}
    }
    else {
	print "<br>Error parsing: data is empty.\n";
    }

    # $xml_ref may be undef, upon error.
    return $xml_ref;
}

sub printstyles {
    print qq(
<style>

body.insight {
	background-color: #F0F0F0;
	scrollbar-face-color: #dcdcdc;
	scrollbar-shadow-color: #000099;
	scrollbar-highlight-color: #dcdcdc;
	scrollbar-3dlight-color: #000099;
	scrollbar-darkshadow-color: #dcdcdc;
	scrollbar-track-color: #dcdcdc;
	scrollbar-arrow-color: #dcdcdc;
}

table.insight {
	width: 100%;
	background-color: #F0F0F0; /* GroundWork Portal Interface: Background */
	border: 1px solid #666666; /* GroundWork Portal Interface: Gray (Table Fill 1px Outlines) */
	text-align: center;
}
table.insightcontrolpanel {
	width: 100%;
	text-align: left;
	background-color: #F0F0F0; /* GroundWork Portal Interface: Background */
	border: 1px solid #FFFFFF; /* GroundWork Portal Interface: Gray (Table Fill 1px Outlines) */
	border-spacing: 0px;
	empty-cells: show;
}
table.insighttoplist {
	width: 100%;
	background-color: #F0F0F0; /* GroundWork Portal Interface: Background */
	border: 0px solid #666666; /* GroundWork Portal Interface: Gray (Table Fill 1px Outlines) */
}

th.insight {
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 8pt;
	font-style: normal;
	font-variant: normal;
	font-weight: bold;
	text-decoration: none;
	text-align: center;
	color: #FFFFFF; /* GroundWork Portal Interface: White */
	padding: 2;
	background-color: #55609A; /* GroundWork Portal Interface: Table Fill #1 */
	border: 1px solid #666666; /* GroundWork Portal Interface: Gray (Table Fill 1px Outlines) */
	border-spacing: 0;
}
th.insightrow2 {
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 8pt;
	font-style: normal;
	font-variant: normal;
	font-weight: bold;
	text-decoration: none;
	text-align: center;
	color: #FFFFFF; /* GroundWork Portal Interface: White */
	padding:0;
	spacing:0;
	background-color: #A0A0A0; /* GroundWork Portal Interface: Table Fill #1 */
	border: 0px solid #666666; /* GroundWork Portal Interface: Gray (Table Fill 1px Outlines) */
}

table.insightform {background-color: #bfbfbf;}
td.insight {
	color: #000000; font-family: verdana, helvetica, arial, sans-serif; font-size: 10; vertical-align: top;
	border: 1px solid #666666;
	background-color: #D9D9D9;
	padding:2;
	spacing:2;
}
td.insightleft {color: #000000; font-family: verdana, helvetica, arial, sans-serif; font-size: 10; vertical-align: top; text-align: left;}
td.insightcenter {color: #000000; font-family: verdana, helvetica, arial, sans-serif; font-size: 10; vertical-align: top;  text-align: center;}
tr.insight {color: #000000; font-family: verdana, helvetica, arial, sans-serif; font-size: 10;}
tr.insightdkgray-bg td {
	background-color:#999;
	color:#fff;
	font-size:11px;
}
tr.insightsublist td {
	color:#475181;
	font-size:10px;
	padding-left:12px !important;
}

tr.insightsublist-graybg td {
	background-color:#efefef;
	color:#475181;
	font-size:10px;
	padding-left:12px !important;
}

td.insighttitle {color: #000000; font-family: verdana, helvetica, arial, sans-serif; font-size: 18;font-weight: bold; color: #FA840F;}
td.insighthead {background-color: #55609A; font-family: verdana, helvetica, arial, sans-serif; font-size: 10; font-weight: bold; color: #ffffff;}
td.insightsubhead {background-color: #8089b9; font-family: verdana, helvetica, arial, sans-serif; font-size: 10; font-weight: bold; color: #ffffff;}
td.insightselected {background-color: #898787; font-family: verdana, helvetica, arial, sans-serif; font-size: 10; font-weight: bold; color: #ffffff;}
td.insightrow1 {background-color: #dcdcdc; font-family: verdana, helvetica, arial, sans-serif; font-size: 10;font-weight: bold;}
td.insightrow2 {background-color: #bfbfbf; font-family: verdana, helvetica, arial, sans-serif; font-size: 10;font-weight: bold;}
td.insightrow_lt {background-color: #f4f4f4; font-family: verdana, helvetica, arial, sans-serif; font-size: 10;font-weight: bold;}
td.insightrow_dk {background-color: #e2e2e2; font-family: verdana, helvetica, arial, sans-serif; font-size: 10;font-weight: bold;}
td.insighterror {background-color: #dcdcdc; font-family: verdana, helvetica, arial, sans-serif; font-size: 10;font-weight: bold; color:cc0000}
#input, textarea, select {border: 0px solid #000099; font-family: verdana, helvetica, arial, sans-serif; font-size: 9px; font-weight: bold; background-color: #ffffff; color: #000000;}
input.insight, textarea.insight, select.insight {border: 0px solid #000099; font-family: verdana, helvetica, arial, sans-serif; font-size: 9px; font-weight: bold; color: #000000;}
input.insighttext {border: 1px solid #000099; font-family: verdana, helvetica, arial, sans-serif; font-size: 9px; font-weight: bold; color: #000000;}
input.insightradio {border: 0px; background-color: #dcdcdc;}
input.insightcheckbox {border: 0px; background-color: #dcdcdc;}

#input.button {
#	border: 1px solid #000000;
#	border-style: solid;
#	border-top-width: auto;
#	border-right-width: auto;
#	border-bottom-width: auto;
#	border-left-width: auto:
#	font-family: verdana, helvetica, arial, sans-serif; font-size: 11px; font-weight: bold; background-color: #898787; color: #ffffff;
#}

input.insightbutton {
	font: normal 10px/normal verdana, helvetica, arial, sans-serif;
	text-transform:uppercase !important;
	border-color: #a0a6c6 #333 #333 #a0a6c6;
	border-width: 2px;
	border-style: solid;
	background:#666;
	color:#fff;
	padding:0;
}

/* for orange buttons */
input.orangebutton {background-color:#FA840F; color:#ffffff; font-size:8pt; border:1px solid #000000; margin-top:10px;}

input.graybutton {background-color:gray; color:#ffffff; font-size:8pt; border:1px solid #000000; margin-top:10px;}

input.insightbox {border: 0px;}

a.insighttop:link    {
	color:#ffffff;
	font-size: 12px;
	font-family: verdana, helvetica, arial, sans-serif;
	text-decoration: none;
	font-weight: bold;
}
a.insighttop:visited {
	color:#ffffff;
	font-size: 12px;
	font-family: verdana, helvetica, arial, sans-serif;
	text-decoration: none;
	font-weight: bold;
}
a.insighttop:active  {
	color:#ffffff;
	font-size: 12px;
	font-family: verdana, helvetica, arial, sans-serif;
	text-decoration: none;
	font-weight: bold;
}
a.insighttop:hover   {
	color:#ffffff;
	font-size: 12px;
	font-family: verdana, helvetica, arial, sans-serif;
	text-decoration: none;
	font-weight: bold;
}
a.insight:link    {
	color:#414141;
	font-size: 12px;
	font-family: verdana, helvetica, arial, sans-serif;
	text-decoration: none;
	font-weight: bold;
}
a.insight:visited {
	color:#414141;
	font-size: 12px;
	font-family: verdana, helvetica, arial, sans-serif;
	text-decoration: none;
	font-weight: bold;
}
a.insight:active  {
	color:#919191;
	font-size: 12px;
	font-family: verdana, helvetica, arial, sans-serif;
	text-decoration: none;
	font-weight: bold;
}
a.insight:hover   {
	color:#919191;
	font-size: 12px;
	font-family: verdana, helvetica, arial, sans-serif;
	text-decoration: none;
	font-weight: bold;
}
a.insightorange:link    {
	color:#FA840F;
	font-size: 12px;
	font-family: verdana, helvetica, arial, sans-serif;
	text-decoration: none;
	font-weight: bold;
}
a.insightorange:visited {
	color:#FA840F;
	font-size: 12px;
	font-family: verdana, helvetica, arial, sans-serif;
	text-decoration: none;
	font-weight: bold;
}
a.insightorange:active  {
	color:#FA840F;
	font-size: 12px;
	font-family: verdana, helvetica, arial, sans-serif;
	text-decoration: none;
	font-weight: bold;
}
a.insightorange:hover   {
	color:#FA840F;
	font-size: 12px;
	font-family: verdana, helvetica, arial, sans-serif;
	text-decoration: none;
	font-weight: bold;
}

/*Center paragraph*/
p.insightcenter {
	color:#000;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	font-weight: normal;
}

p {
	color:#000;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	font-weight: normal;
}

h1.insight {
	color:#FA840F;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 18px;
	font-weight: 600;
}

h2.insight {
	color:#55609A;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 14px;
	font-weight: bold;
}

h3.insight {
	color:#000;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	font-weight: normal;
}

h4.insight {
	color:#FFFFFF;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 12px;
	font-weight: bold;
}

h5.insight {
	color:#000;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 16px;
	font-style: italic;
	font-weight: normal;
}

h6.insight {
	color:#000;
	font-family: verdana, helvetica, arial, sans-serif;
	font-size: 18px;
	font-weight: bold;
}

</style>
    );
}

