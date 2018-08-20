#!/usr/bin/perl
package GWNMSInstaller::AL::ntopPackage;
@ISA = qw(GWInstaller::AL::Software);
 
 
 sub set_ntop{
 	my ($self,$value) = @_;
 	$self->{ntop} = $value;	
 }
 
 sub get_ntop{
 	my $self = shift;
 	return $self->{ntop};
 }
 
 1;