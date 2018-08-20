#!/usr/bin/perl
package GWNMSInstaller::AL::WeathermapPackage;
@ISA = qw(GWInstaller::AL::Software);
 
 
 sub set_weathermap_editor{
 	my ($self,$value) = @_;
 	$self->{weathermap_editor} = $value;	
 }
 
 sub get_weathermap_editor{
 	my $self = shift;
 	return $self->{weathermap_editor};
 }
 
 1;