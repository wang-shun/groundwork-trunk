#!/usr/bin/perl

# COPYRIGHT:
#
# This software is Copyright (c) 2007-2009 NETWAYS GmbH, Christian Doebler
#                 some parts (c) 2009      NETWAYS GmbH, William Preston
#                                <support@netways.de>
#
# (Except where explicitly superseded by other copyright notices)
#
#
# LICENSE:GPL2
# see noma_daemon.pl in parent directory for full details.
# Please do not distribute without the above file!
use time_frames;
use datetime;
use groupnames;
use Data::Dumper;
$Data::Dumper::Sortkeys = 1;


##############################################################################
# NOTIFICATION- AND CONTACT-FILTERING FUNCTIONS
##############################################################################

# generate a list of contacts from an id, counter and status
sub getContacts
{

    my ($ids, $notificationCounter, $status, $notification_type, $incident_id, $counterHasRolledOver) = @_;
    debug('trying to getUsersAndMethods', 2);
    my %contacts_c =
    getUsersAndMethods( $ids, $notificationCounter, $notification_type,$status, $counterHasRolledOver );
    debug("Users from rules: ". debugHashUsers(%contacts_c), 2);
    my %contacts_cg =
    getUsersAndMethodsFromGroups( $ids, $notificationCounter, $notification_type,
        $status, $counterHasRolledOver );
    debug( 'Users from groups: '.debugHashUsers(%contacts_cg), 2);

    # merge contact hashes
    my %contacts = mergeHashes( \%contacts_c, \%contacts_cg );
    %contacts = removeHashEntryDuplicates( \%contacts );
    debug("Removing duplicates. Users remaining: ". debugHashUsers(%contacts), 2);

    # get current time
    my $now = time();

    # check contacts for holidays
    %contacts = checkHolidays(%contacts);
    debug("Holidays. Users remaining: ". debugHashUsers(%contacts), 2);

    # check contacts for working hours
    %contacts = checkContactWorkingHours(%contacts);
    debug("Working hours. Users remaining: ". debugHashUsers(%contacts), 2);

    # check contacts for multiple alert suppression
    # --> only sends one alert if the incident_id is the same - be careful
    %contacts = checkSuppressionFlag($incident_id, %contacts) if ( $status ne 'OK' && $status ne 'UP');
    debug("Suppression. Users remaining: ". debugHashUsers(%contacts), 2);

    # convert contact hash into array
    my @contactsArr = hash2arr(%contacts);
    return @contactsArr;
}

sub generateNotificationList
{

    my ( $check_type, $notificationRecipients, $notificationHost, $notificationService, $notificationHostgroups, $notificationServicegroups, %dbResult ) = @_;

    debug(' notificationRecipients: '. $notificationRecipients . ' notificationHost: '.$notificationHost.' notificationService: '.$notificationService.' notificationHostgroups: '.$notificationHostgroups.' notificationServicegroups: '.$notificationServicegroups, 2);

	# debugging for test suite
	debug('Testdata: '.Dumper({checktype => $check_type, recipients => $notificationRecipients, host => $notificationHost, svc => $notificationService, hgs => $notificationHostgroups, sgs => $notificationServicegroups, dbresult => \%dbResult}), 3);

    my $cnt = 0;
    my %notifyList;
    my @recipients = split(",",$notificationRecipients);
    my @hostgroups = split(",",$notificationHostgroups);
    my @servicegroups = split(",",$notificationServicegroups);
    # ========================================================================
    # GWMON-13076:  Retrieve hostgroup/servicegroup associations from an
    # outside source.
    # ========================================================================
    # We could conceivably retrieve the hostgroups and servicegroups here
    # only if they were not supplied in the incoming alert.  If both are
    # empty, we cannot be sure whether that's because there are no hostgroups
    # or servicegroups associated with this host or service, or whether the
    # upstream alerting source simply didn't bother to pass along that data.
    # In either case, try to fetch the data now.
    # if ( !@hostgroups && !@servicegroups ) { ... }

    # Alternatively, we could base this decision to override the incoming
    # hostgroup and servicegroup data for this particular alert on some
    # config-file setting, perhaps in conjunction with whether the incoming
    # group data is empty.  We choose not to do that for the time being,
    # mostly because we know that our chosen external source should have
    # comprehensive knowledge of all hostgroups and servicegroups maintained
    # by all possible alerting sources, so we shouldn't lose anything by doing
    # unconditional lookups here, and this will provide complete consistency
    # in the values available for testing against notification rules.
    #
    if (1) {
	my ( $hostgroups, $servicegroups ) = getHostGroupAndServiceGroupNames( $notificationHost, $notificationService );
	@hostgroups    = @$hostgroups    if defined $hostgroups;
	@servicegroups = @$servicegroups if defined $servicegroups;
	debug( "Full set of    hostgroups for this alert:  " . join( ', ', map { '"' . $_ . '"' } @hostgroups ),    2 );
	debug( "Full set of servicegroups for this alert:  " . join( ', ', map { '"' . $_ . '"' } @servicegroups ), 2 );
    }
    # ========================================================================
    my $rCount = @recipients;
    if ($rCount < 1) {
        $recipients[0] = "__NONE";
    }
    my $hgCount = @hostgroups;
    if($hgCount < 1) {
        $hostgroups[0] = "__NONE";
    }
    my $sgCount = @servicegroups;
    if($sgCount < 1) {
        $servicegroups[0] = "__NONE";
    }
    # BEGIN - generate include and exclude lists for hosts and services

    while ( my $res = $dbResult{$cnt++})
    {

	my $matched;

	# Implicit include means that we match if the include field is blank
	$res->{recipients_include} = '*' if !$res->{recipients_include};
	$res->{servicegroups_include} = '*' if !$res->{servicegroups_include};
	$res->{services_include} = '*' if !$res->{services_include};
	$res->{hostgroups_include} = '*' if !$res->{hostgroups_include};
	$res->{hosts_include} = '*' if !$res->{hosts_include};
	$res->{recipients_exclude} = '' if !$res->{recipients_exclude};
	$res->{servicegroups_exclude} = '' if !$res->{servicegroups_exclude};
	$res->{services_exclude} = '' if !$res->{services_exclude};
	$res->{hostgroups_exclude} = '' if !$res->{hostgroups_exclude};
	$res->{hosts_exclude} = '' if !$res->{hosts_exclude};

	# generate recipients list
	foreach my $recipient(@recipients) {
            if (!$recipient
				or (matchString($res->{recipients_include}, $recipient)
					and !matchString($res->{recipients_exclude}, $recipient)))
            {
                debug( "Step1: RecipientIncl: $recipient\t" . $res->{id}, 2);
                $matched = 1;
            }

	}
	next unless $matched;

	# ========================================================================
	# GWMON-13076:  Modify the hostgroup/servicegroup processing model
	# from what a stock NoMa distribution uses, to make it practical.
	# ========================================================================
	# Verify servicegroup restrictions.
	if ( $check_type eq 's' )
	{
		(my $clean_include_patterns = $res->{servicegroups_include}) =~ s/(^\s+|\s+$)//g;
		(my $clean_exclude_patterns = $res->{servicegroups_exclude}) =~ s/(^\s+|\s+$)//g;
		$clean_include_patterns =~ s/(\s*,\s*)/,/g;
		$clean_exclude_patterns =~ s/(\s*,\s*)/,/g;
		next unless
			( $clean_include_patterns =~ /(^|,)\*+(,|$)/ or groupsMatch( \@servicegroups, $clean_include_patterns ) )
		      and
			( $clean_exclude_patterns =~ /^,*$/ or not groupsMatch( \@servicegroups, $clean_exclude_patterns ) );
		debug( "Step1: SvcGrp: passed all servicegroups conditions\t" . $res->{id}, 2);
	}

	# Verify hostgroup restrictions.
	(my $clean_include_patterns = $res->{hostgroups_include}) =~ s/(^\s+|\s+$)//g;
	(my $clean_exclude_patterns = $res->{hostgroups_exclude}) =~ s/(^\s+|\s+$)//g;
	$clean_include_patterns =~ s/(\s*,\s*)/,/g;
	$clean_exclude_patterns =~ s/(\s*,\s*)/,/g;
	next unless
		( $clean_include_patterns =~ /(^|,)\*+(,|$)/ or groupsMatch( \@hostgroups, $clean_include_patterns ) )
	      and
		( $clean_exclude_patterns =~ /^,*$/ or not groupsMatch( \@hostgroups, $clean_exclude_patterns ) );
	debug( "Step1: HostGrp: passed all hostgroups conditions\t" . $res->{id}, 2);
	# ========================================================================

		$matched = 0;
        # generate host list
        if (!$notificationHost
			or (matchString($res->{hosts_include}, $notificationHost)
				and !matchString($res->{hosts_exclude}, $notificationHost)))
        {
            debug( "Step1: Host: $notificationHost\t" . $res->{id}, 2);
            $matched = 1;
        }

		next unless $matched;

        if ( $check_type eq 's' )
        {
			$matched = 0;
            # generate service list
            if (!$notificationService
				or (matchString($res->{services_include}, $notificationService)
					and !matchString($res->{services_exclude}, $notificationService)))
            {
                debug( "Step1: Service: $notificationService\t" . $res->{id}, 2);
                $matched = 1;
            }


        }

		next unless $matched;

		$notifyList{ $res->{id} } = 1;

    }

    # END - (of generate include and exclude lists for hosts and services)

    # BEGIN - collect all IDs to notify
    my %idList;
    my @ids;
    while ( my ($notifyIncl) = each(%notifyList) )
    {
            if ( defined( $notifyList{$notifyIncl}) )
            {
		# Verify that the notification is within the time frame.
		if (notificationInTimeFrame($notifyIncl) == '1'){
                $idList{$notifyIncl} = 1;
                debug("Step2: notifyIncl: $notifyIncl", 2);
		}
            }
    }
    while ( my ($id) = each(%idList) )
    {
        push( @ids, $id );
    }

    # END   - collect all IDs to notify

    return @ids;

}

sub matchString
{
	# test a string against a comma separated list and return 1 if it matches
	my ($matchList, $match) = @_;

        my @items = split( ',', $matchList );
        @items = map( { lc($_) } @items );

        for my $item (@items)
        {
            # remove leading/trailing whitespace
            $item =~ s/^\s+|\s+$//g;

            if ( $item ne '' )
            {

		# Use * and ? as wildcards
                $item =~ s/\*/.*/g;
                $item =~ s/\./\./g;
                $item =~ s/\?/./g;

                # only add item to list if it matches the passed one
                return 1 if ( $match =~ m/^$item$/i );

            }

        }
	return 0;
}

sub groupsMatch {
    my ( $groups, $patterns ) = @_;
    my @groups = @$groups;
    my @patterns = split( ',', $patterns );
    foreach my $pattern (@patterns) {
	## Here we assume the incoming patterns have been cleaned up by the caller,
	## so each individual pattern is already trimmed.
	# $pattern =~ s/^\s+|\s+$//g;
	if ( $pattern ne '' ) {
	    ## Use * and ? as wildcards
	    $pattern =~ s/\*/.*/g;
	    $pattern =~ s/\./\./g;
	    $pattern =~ s/\?/./g;
	    foreach my $member (@groups) {
		## Notice that we do not ignore leading or trailing spaces within $member;
		## it is up to the original upstream code to have submitted clean alert data.
		return 1 if ( $member =~ m/^$pattern$/i );
	    }
	}
    }
    return 0;
}

# given a list of rules, the notification counter, and the type
# it returns an array of contacts to notify
#
sub getUsersAndMethods
{

	my ( $ids, $notificationCounter, $notification_type, $status, $counterHasRolledOver ) = @_;

	my %dbResult;
	my @dbResult_arr;
	my @dbResult_not_arr;
	my @dbResult_esc_arr;
	my @dbResult_tmp_arr;
	my $where = '';
	my $query;

	# standard query
	my $ids_cnt = scalar(@$ids);

	if ($ids_cnt)
	{

		if ( $ids_cnt == 1 )
		{
			$where = $ids->[0];
		} else
		{
			$where = join( '\' or n.id=\'', @$ids );
		}

		$query =
			'select distinct c.username, c.phone, c.mobile, c.growladdress, c.email, tz.timezone, m.id mid, m.method, m.command, m.contact_field, m.sender, m.on_fail, m.ack_able, n.notify_after_tries, n.let_notifier_handle, n.id as rule from notifications n
			left join notifications_to_methods nm on n.id=nm.notification_id
			left join notification_methods m on m.id=nm.method_id
			left join notifications_to_contacts nc on n.id=nc.notification_id
			left join contacts c on c.id=nc.contact_id
			left join timezones tz on c.timezone_id=tz.id
			where n.active=\'1\' and (n.id=\'' . $where . '\')';

		@dbResult_not_arr = queryDB( $query, 1 );

		$where =~ s/n\.id/ec\.notification_id/g;

		$query =
			'select distinct c.username, c.phone, c.mobile, c.growladdress, c.email, tz.timezone, m.id mid, m.method, m.command, m.contact_field, m.sender, m.on_fail, m.ack_able, ec.notify_after_tries, n.let_notifier_handle, n.id as rule from escalations_contacts ec
			left join escalations_contacts_to_contacts ecc on ec.id=ecc.escalation_contacts_id
			left join contacts c on c.id=ecc.contacts_id
			left join escalations_contacts_to_methods ecm on ec.id=ecm.escalation_contacts_id
			left join notification_methods m on ecm.method_id=m.id
			left join timezones tz on c.timezone_id=tz.id
			left join notifications n on ec.notification_id=n.id
			where n.active=\'1\' and (ec.notification_id=\'' . $where . '\')';

		@dbResult_esc_arr = queryDB( $query, 1 );

		@dbResult_tmp_arr = ( @dbResult_not_arr, @dbResult_esc_arr );
		@dbResult_arr =
		filterNotificationsByEscalation( \@dbResult_tmp_arr, $notificationCounter, $notification_type, $status, $counterHasRolledOver );


		debug("To be notified: ".Dumper(@dbResult_arr), 3);

		%dbResult = arrayToHash( \@dbResult_arr );

		%dbResult = () unless ( defined( $dbResult{0}->{username} ) );

	}

	return %dbResult;

}

sub getUsersAndMethodsFromGroups
{

	my ( $ids, $notificationCounter, $notification_type, $status, $counterHasRolledOver ) = @_;

	my %dbResult;
	my @dbResult_arr;
	my @dbResult_not_arr;
	my @dbResult_esc_arr;
	my @dbResult_tmp_arr;
	my $where = '';
	my @ignoreCGs;
	my $query;

	# get count of notification id's
	my $ids_cnt = @$ids;

	if ($ids_cnt)
	{

		if ( $ids_cnt == 1 )
		{

			my $query_temp = 'select ncg.contactgroup_id from notifications_to_contactgroups ncg where ncg.notification_id=\''.$ids->[0].'\'';
			my %dbResult_temp = queryDB($query_temp);
			foreach my $cg (keys %dbResult_temp )
			{
				if(contactgroupInTimeFrame($dbResult_temp{$cg}->{contactgroup_id}) eq 0)
				{
					debug(' Contactgroup ID to exclude from queries: '.$dbResult_temp{$cg}->{contactgroup_id},2);
					push(@ignoreCGs, $dbResult_temp{$cg}->{contactgroup_id});
				}
			}
			$query_temp = 'select eccg.contactgroup_id from escalations_contacts ec left join escalations_contacts_to_contactgroups eccg on ec.id=eccg.escalation_contacts_id where ec.notification_id=\''.$ids->[0].'\'';
			%dbResult_temp = queryDB($query_temp);
			foreach my $cg (keys %dbResult_temp )
			{
				if(contactgroupInTimeFrame($dbResult_temp{$cg}->{contactgroup_id}) eq 0)
				{
					debug(' Contactgroup ID to exclude from queries: '.$dbResult_temp{$cg}->{contactgroup_id},2);
					push(@ignoreCGs, $dbResult_temp{$cg}->{contactgroup_id});
				}
			}

			$where = $ids->[0];
		}
		else
		{
			my $query_temp = 'select ncg.contactgroup_id from notifications_to_contactgroups ncg where ncg.notification_id in (' . join( ',', @$ids ) . ')';
			my %dbResult_temp = queryDB($query_temp);
			foreach my $cg (keys %dbResult_temp )
			{
				if(contactgroupInTimeFrame($dbResult_temp{$cg}->{contactgroup_id}) eq 0)
				{
					debug(' Contactgroup ID to exclude from queries: '.$dbResult_temp{$cg}->{contactgroup_id},2);
					push(@ignoreCGs, $dbResult_temp{$cg}->{contactgroup_id});
				}
			}
			$query_temp = 'select eccg.contactgroup_id from escalations_contacts ec left join escalations_contacts_to_contactgroups eccg on ec.id=eccg.escalation_contacts_id where ec.notification_id in (' . join( ',', @$ids ) . ')';
			%dbResult_temp = queryDB($query_temp);
			foreach my $cg (keys %dbResult_temp )
			{
				if(contactgroupInTimeFrame($dbResult_temp{$cg}->{contactgroup_id}) eq 0)
				{
					debug(' Contactgroup ID to exclude from queries: '.$dbResult_temp{$cg}->{contactgroup_id},2);
					push(@ignoreCGs, $dbResult_temp{$cg}->{contactgroup_id});
				}
			}
			$where = join( '\' or n.id=\'', @$ids );
		}

		# get contactgroups of ID's
		# figure out what ID's NOT to select.

		# query db for contacts
		$query =
			'select distinct c.username, c.phone, c.mobile, c.growladdress, c.email, tz.timezone, m.id mid, m.method, m.command, m.contact_field, m.sender, m.on_fail, m.ack_able, n.notify_after_tries, n.let_notifier_handle, n.id as rule from notifications n
			left join notifications_to_methods nm on n.id=nm.notification_id
			left join notification_methods m on m.id=nm.method_id
			left join notifications_to_contactgroups ncg on n.id=ncg.notification_id
			left join contactgroups cg on ncg.contactgroup_id=cg.id
			left join contactgroups_to_contacts cgc on cgc.contactgroup_id=cg.id
			left join contacts c on c.id=cgc.contact_id
			left join timezones tz on c.timezone_id=tz.id
			where cg.view_only=\'0\' and n.active=\'1\' and (n.id=\'' . $where . '\')'
			. ( @ignoreCGs ? ' and ncg.contactgroup_id not in (' . join( ',', @ignoreCGs ) . ')' : '' );

		@dbResult_not_arr = queryDB( $query, 1 );

		$where =~ s/n\.id/ec\.notification_id/g;

		$query =
			'select distinct c.username, c.phone, c.mobile, c.growladdress, c.email, tz.timezone, m.id mid, m.method, m.command, m.contact_field, m.sender, m.on_fail, m.ack_able, ec.notify_after_tries, n.let_notifier_handle, n.id as rule from escalations_contacts ec
			left join escalations_contacts_to_contactgroups eccg on ec.id=eccg.escalation_contacts_id
			left join contactgroups_to_contacts cgc on eccg.contactgroup_id=cgc.contactgroup_id
			left join contacts c on cgc.contact_id=c.id
			left join escalations_contacts_to_methods ecm on ec.id=ecm.escalation_contacts_id
			left join notification_methods m on m.id=ecm.method_id
			left join timezones tz on c.timezone_id=tz.id
			left join notifications n on ec.notification_id=n.id
			left join contactgroups cg on cgc.contactgroup_id=cg.id
			where cg.view_only=\'0\' and n.active=\'1\' and (ec.notification_id=\'' . $where . '\')'
			. ( @ignoreCGs ? ' and eccg.contactgroup_id not in (' . join( ',', @ignoreCGs ) . ')' : '' );

		@dbResult_esc_arr = queryDB( $query, 1 );
		@dbResult_tmp_arr = ( @dbResult_not_arr, @dbResult_esc_arr );

		@dbResult_arr =
			filterNotificationsByEscalation( \@dbResult_tmp_arr, $notificationCounter, $notification_type, $status, $counterHasRolledOver );


		debug("To be notified: ".Dumper(@dbResult_arr), 3);

		%dbResult = arrayToHash( \@dbResult_arr );

		%dbResult = () unless ( defined( $dbResult{0}->{username} ) );

	}

	return %dbResult;

}

sub checkSuppressionFlag
{

    my ($id, %contacts) = @_;


    	return %contacts if not (scalar %contacts);

	# get list of users we don't want to inform
	my $query = 'select distinct c.username from contacts as c
					left join notification_logs as l on c.username='.quoteIdentifier('l.user').'
					where l.incident_id=\'' . $id . '\' and restrict_alerts=1';
					

	my @dbResult = queryDB($query, '1');

	foreach my $contact (@dbResult)
	{
		foreach my $contact_key ( keys %contacts ) {
			delete $contacts{$contact_key} if $contacts{$contact_key}{username} eq $contact->{username};
		}
	}

    return %contacts;

}

sub checkHolidays
{

    my %contacts = @_;

    # set up variables
    my $on_holiday;
    my %newContacts;

    # loop through contacts
    while ( my ($contact) = each(%contacts) )
    {

        # init on-holiday flag
        $on_holiday = 0;

        # get holiday entries for current contact
        my $query = 'select h.holiday_start,h.holiday_end from holidays h
					left join contacts c on c.id=h.contact_id
					where c.username=\'' . $contacts{$contact}->{username} . '\'';

        my @dbResult = queryDB($query, '1');

	# set timezone
	my $tz = DateTime::TimeZone->new( name =>  $contacts{$contact}->{timezone});
	my $dt = DateTime->now()->set_time_zone($tz);

        # check person's holiday data
	if (datetimeInPeriod(\@dbResult, $dt->ymd." ".$dt->hms))
        {
		debug(  "username: ".$contacts{$contact}->{username}." (GMT+".$dt->offset($dt)."s) on holiday, dropping", 2);
	        $on_holiday = 1;

        }

        # add contact to new contact list if not on holidays
        if ( !$on_holiday )
        {
            $newContacts{$contact} = $contacts{$contact};
        }

    }

    return %newContacts;

}

sub checkContactWorkingHours
{

    my %contacts = @_;

    # set up variables
    my %newContacts;

    # loop through contacts
    while ( my ($contact) = each(%contacts) )
    {

	# REPLACE WHAT IS BELOW!!! 

	my $away = 0;

	# get timeframe_id for the current contact
	my $query = 'select contacts.timeframe_id from contacts where contacts.username=\'' . $contacts{$contact}->{username} . '\'';

        my %dbResult = queryDB($query);

	    # drop contact and break loop if outside time period
	    if ( (objectInTimeFrame($dbResult{0}->{timeframe_id},'contacts') eq 0 ))
	    {
		debug( "username: ".$contacts{$contact}->{username}." outside timeframe, dropping", 2);
		$away = 1;
		next;
	    }

        # add contact to new contact list
        if ( !$away )
        {
            $newContacts{$contact} = $contacts{$contact};
        }

    }

    return %newContacts;

}



# only send alerts to contacts that have the correct notification nr.
# or recoveries / OKs to all preceding
sub filterNotificationsByEscalation
{

    my ( $dbResult_arr, $filter, $notification_type, $status, $counterHasRolledOver ) = @_;
    my $ignoreFilter = 0;
    my @return_arr;

    debug('filter: '.$filter.' notification_type: '.$notification_type.' status: '.$status.' counterHasRolledOver: '.$counterHasRolledOver, 2);
    debug("dbResult_arr Array: ".Dumper($dbResult_arr), 3);

    # prepare search filter
    if ( $status eq 'OK' || $status eq 'UP' || $notification_type eq 'ACKNOWLEDGEMENT' || $notification_type eq 'CUSTOM' || $notification_type eq 'FLAPPINGSTART' || $notification_type eq 'FLAPPINGSTOP' || $notification_type eq 'FLAPPINGDISABLED' || $notification_type eq 'DOWNTIMESTART' || $notification_type eq 'DOWNTIMEEND' || $notification_type eq 'DOWNTIMECANCELLED')
    {
        if ($counterHasRolledOver) {
            ## In this case, we can assume that all contacts listed in @$dbResult_arr with some specification for when
            ## they would be sent a notification already got notified before the rollover occurred.  (This will be true
            ## provided that the notification rules have been stable over this period, which we really have no direct
            ## control over.)  So all such contacts should now be notified of the recovery condition, regardless of
            ## where their particular notification rule is now positioned within the rolled-over counter sequencing.
            $ignoreFilter = 1;
        }
        else {
            my @filter_entries;
            for ( my $x = 0 ; $x <= $filter ; $x++ ) {
                push( @filter_entries, $x );
            }
            $filter = '(' . join( '|', @filter_entries ) . ')';
        }
    }

    debug('filter2: '.($ignoreFilter ? 'notify all contacts' : $filter), 2);

    # apply filter
    foreach my $row (@$dbResult_arr)
    {
	debug('row: '.Dumper($row), 3);
        next
          if ( !defined( $row->{notify_after_tries} )
            || $row->{notify_after_tries} eq '' );
        my @notify_after_tries = getArrayOfNums( $row->{notify_after_tries} );
        if (   ( $ignoreFilter || grep( /^$filter$/, @notify_after_tries ) )
            && defined( $row->{username} )
            && $row->{username} ne '' )
        {
            push( @return_arr, $row );
        }
    }

    debug('return_arr: '.Dumper(@return_arr), 2);

    return @return_arr;

}

sub getMaxValue
{
    # given a range string, return the maximum
    # e.g. 1-4,7-8,12
	my ($range) = @_;

    my $min;
    my $max;

	$range =~ s/[^0-9,;-]//g;

    return $range unless ($range =~ /[,;-]+/);

    my $newmax = 1;
    foreach my $crange (split(/[,;]/, $range))
    {
		if ($crange =~ /-/)
		{
			debug("Expanding $crange", 3);
			$crange =~ /(\d*)-(\d*)/;

			$min = $1;
			$max = $2;

			if ((not defined($min)) or ($min < 1))
			{
				debug("Invalid minimum value in range \"$crange\" - setting to 1", 1);
				$min = 1;
			}

			if ((not defined($max)) or ($max < $min))
			{
				debug("Invalid maximum value in range \"$crange\" - setting to 99999", 1);
				$max = 99999;
			}
		} else {
            debug("Testing $crange", 3);
            $max = $crange;
        }


        $newmax = $max if ($max > $newmax);
    }

    return $newmax;
}

1;
