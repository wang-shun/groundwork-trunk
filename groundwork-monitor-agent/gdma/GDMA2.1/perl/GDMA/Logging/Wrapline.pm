#!/usr/bin/perl

# Copyright (c) 2017-2018 GroundWork, Inc. (www.gwos.com).  All rights reserved.
# Use of this software is subject to commercial license terms.

package GDMA::Logging::Wrapline;
use base qw(Log::Log4perl::Layout::PatternLayout);
use Text::Wrap;

###########################################
sub new {
###########################################
    my $self = Log::Log4perl::Layout::PatternLayout::new(@_);
    my $options = ref $_[1] eq 'HASH' ? $_[1] : {};
    ## We provide semi-reasonable defaults mostly just so the package does not break if
    ## they are not provided.  Almost always, you will want to provide your own values.
    $self->{max_line_length} = $options->{MaxLineLength}{value}  // 80;
    $self->{prefix_length}   = $options->{PrefixLength}{value}   // 8;
    $self->{suppress_prefix} = $options->{SuppressPrefix}{value} // 0;
    $self->{suppress_wrapping}  = 0;
    $self->{suppress_separator} = 0;
    return $self;
}

###########################################
sub render {
###########################################
    my ( $self, $message, $category, $priority, $caller_level ) = @_;

    # This is a flag intentionally reset by every message, to be used by a parent package.
    $self->{suppress_separator} = 0;

    # We can support formatting certain lines without the standard prefix.  The formulation
    # used here might depend on our knowledge of the ConversionPattern in use, so it might
    # need more work to be good enough for general use.
    local $self->{printformat} = '%2$s' . "\n" if $self->{suppress_prefix};

    my $msg;
    if ( $self->{suppress_wrapping} ) {
	$msg = $message;
    }
    else {
	local $Text::Wrap::columns  = $self->{max_line_length} - ( $self->{suppress_prefix} ? 0 : $self->{prefix_length} );
	local $Text::Wrap::unexpand = 0;
	local $Text::Wrap::huge     = 'overflow';
	$msg = wrap( '', '', $message );
    }
    if ( not $self->{suppress_prefix} ) {
	my $prefix = ' ' x $self->{prefix_length};
	$msg =~ s/\n/\n$prefix/g;
    }

    $caller_level = 0 unless defined $caller_level;

    my $result = $self->SUPER::render( $msg, $category, $priority, $caller_level + 1 );

    return $result;
}

###########################################
sub suppress_prefix {
###########################################
    my ( $self, $suppress ) = @_;
    my $old_suppression = $self->{suppress_prefix};
    $self->{suppress_prefix} = $suppress if defined $suppress;
    return $old_suppression;
}

###########################################
sub suppress_wrapping {
###########################################
    my ( $self, $suppress ) = @_;
    my $old_suppression = $self->{suppress_wrapping};
    $self->{suppress_wrapping} = $suppress if defined $suppress;
    return $old_suppression;
}

###########################################
sub suppress_separator {
###########################################
    my ( $self, $suppress ) = @_;
    my $old_suppression = $self->{suppress_separator};
    $self->{suppress_separator} = $suppress if defined $suppress;
    return $old_suppression;
}

1;

__END__

=encoding utf8

=head1 NAME

    GDMA::Logging::Wrapline

=head1 SYNOPSIS

    use GDMA::Logging::Wrapline;

    my $layout = GDMA::Logging::Wrapline->new("%d (%F:%L)> %m");

=head1 DESCRIPTION

C<GDMA::Logging::Wrapline> is a subclass of Log4perl's PatternLayout and is helpful if you
send messages with very long lines to your appenders.  This package breaks apart such long
lines on word boundaries according to a defined maximum line length, and causes them to be
formatted with extra lines indented to the level of the prefix.  So for instance:

    2007/04/04 23:59:01 This is a message with a very long line for expository purposes

might appear as

    2007/04/04 23:59:01 This is a message with
			a very long line for
			expository purposes

Multilines will be preserved as such, wrapped separately, and indented at the same level.

The overall line length, including the prefix on the first line, is specified by
the MaxLineLength parameter.  The prefix length is specified by the PrefixLength
parameter, and it should match the length of the string produced by the underlying
ConversionPattern that prints the prefix.  So for instance, with a prefix formatted
like C<"[Thu Feb 08 03:32:27 2018] ">, you could specify:

    log4perl.appender.Logfile.layout.MaxLineLength = 130
    log4perl.appender.Logfile.layout.PrefixLength  = 27
    log4perl.appender.Logfile.layout.ConversionPattern = [%d{EEE MMM dd HH:mm:ss yyyy}] %m%n

where the PrefixLength counts all characters before the C<%m> placeholder.

The constructor of the C<GDMA::Logging::Wrapline> class takes an optional hash
reference as a first argument to specify corresponding options, if you
prefer initialization that way:

    my $layout = GDMA::Logging::Wrapline->new(
	{ MaxLineLength => 130, PrefixLength => 27 },
	"[%d{EEE MMM dd HH:mm:ss yyyy}] %m%n"
    );

=head1 BUGS

Compare to the C<Log::Log4perl::Layout::PatternLayout> placeholders of C<%m{indent}> and
C<%m{indent=n}>, though they deal with multilines, not with long lines.

This package ought to be renamed to be C<Log::Log4perl::Layout::PatternLayout::Wrapline>
and publicly released.  Either that, or we ought to merge this capability directly into
C<Log::Log4perl::Layout::PatternLayout> using new placeholders of C<%m{wrap=m}>,
C<%m{wrap=m,indent}>, and C<%m{wrap=m,indent=n}> or somesuch.

=head1 LICENSE

Copyright 2018 by GroundWOrk Open Source.

This library is free software; you can redistribute it and/or modify
it under the same terms as Perl itself.
