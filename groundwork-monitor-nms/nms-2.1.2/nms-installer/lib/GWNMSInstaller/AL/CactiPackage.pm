#!/usr/bin/perl
package GWNMSInstaller::AL::CactiPackage;
@ISA = qw(GWInstaller::AL::Software);
 
 
 sub set_cacti{
 	my ($self,$value) = @_;
 	$self->{cacti} = $value;	
 }
 
 sub get_cacti{
 	my $self = shift;
 	return $self->{cacti};
 }
 1;