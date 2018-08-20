#!/usr/bin/perl
package GWNMSInstaller::AL::NeDiPackage;
@ISA = qw(GWInstaller::AL::Software);
 
 
 sub set_nedi{
 	my ($self,$value) = @_;
 	$self->{nedi} = $value;	
 }
 
 sub get_nedi{
 	my $self = shift;
 	return $self->{nedi};
 }
 
 1;