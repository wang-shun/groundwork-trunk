package GW::HelpDeskUtils;

use vars qw($VERSION); $VERSION = '1.0';

use strict;
use warnings;
use DBI;
use Data::Dumper;

sub new {
	my $packageName = shift;

	my $self = {
	};

	# Bless the Hash
	bless $self, $packageName;

	# Pass the Reference
	return $self;
}

#------------------------------------------------------------------------------
# Take a string of comma separated LogMessageIDs: 8,22,678 and convert them
# into a string suitable for dropping into a membership SQL search
# query: ('8','22','678').
#
# For example:
#   SELECT * FROM LogMessage WHERE LogMessageID IN ('8', '22', '678');
#------------------------------------------------------------------------------
sub generateQuerySet {
	my $idStr = shift(@_);
	my $idSet = "(";

	my @fields = split(/,/, $idStr);

	foreach my $field (@fields) {
		$idSet .= "'$field',";
	}

	# Remove the trailing ','
	chop($idSet);

	$idSet .= ")";

	return $idSet;
}

#------------------------------------------------------------------------------
# Take a string of comma separated LogMessageIDs: 8,22,678 and convert them
# into a string suitable for dropping into an SQL insert
# statement: ('8'),('22'),('678').  This is usually used for inserts into
# a ConcurrencyTable like the HelpDeskConcurrencyTable.
#------------------------------------------------------------------------------
sub generateInsertSet {
	my $idStr     = shift(@_);
	my $insertSet = "";

	my @fields = split(/,/, $idStr);

	foreach my $field (@fields) {
		$insertSet .= "('$field'),";
	}

	# Remove the trailing ','
	chop($insertSet);

	return $insertSet;
}

1;
__END__

