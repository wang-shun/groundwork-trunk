#!/usr/bin/perl
package GWNMSInstaller::AL::automationPackage;
@ISA = qw(GWInstaller::AL::Software);
 
 
 sub set_cacti{
 	my ($self,$value) = @_;
 	$self->{cacti} = $value;	
 }
 
 sub get_cacti{
 	my $self = shift;
 	return $self->{cacti};
 }
  sub set_nedi{
 	my ($self,$value) = @_;
 	$self->{nedi} = $value;	
 }
 
 sub get_nedi{
 	my $self = shift;
 	return $self->{nedi};
 }
 1;