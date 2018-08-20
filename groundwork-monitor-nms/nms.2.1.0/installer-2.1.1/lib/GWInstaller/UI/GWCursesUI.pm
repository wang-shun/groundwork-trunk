#!/usr/local/groundwork/perl/bin/perl
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

package GWInstaller::UI::GWCursesUI;
use File::Basename;

sub new{
	 
	my ($invocant,$installer) = @_;
	my $class = ref($invocant) || $invocant;
	 
   	$pRefr = {};
	my $self = {
		installer=>$installer,
		corelast=>$corelast
	};
 
  
	bless($self,$class);
 
 	
	return $self;		
}

sub yes_or_no_dialog{
		($self,$title,$msg) = @_;
 		
		$installer = $self->{installer};
		$cui = $installer->{cui};		
        my $return = $cui->dialog(
                        -title     => "$title",
                        -selected	=> 2,
                        -buttons   => ['yes', 'no'],
                        -message => $msg
				  );
		$yn = "";
	 	if($return){
	 		$yn = "YES";
	 	}
	 	else{
	 		$yn = "NO";
	 	}
	  GWInstaller::AL::GWLogger::log("QUERY: " . $msg . " $yn");
	  return $return;
}

sub error_msg{
	$msg = shift;
	$cui = shift;
	$cui->error($msg);
	GWInstaller::AL::GWLogger::log("ERROR: " . $msg);
}

# error_dialog
##############
sub error_dialog{
	my($self,$msg) = @_;
	$installer = $self->{installer};
	$cui = $installer->{cui};	
	$cui->error($msg);
	GWInstaller::AL::GWLogger::log("ERROR: " . $msg);	
}

# info_dialog
#############
sub info_dialog{
	my ($self,$msg) = @_;
	$installer = $self->{installer};
	$cui = $installer->{cui};
	
	$cui->dialog(
			-title => "Info",
			-buttons =>['ok'],
			-message =>$msg
			);
	GWInstaller::AL::GWLogger::log("INFO:" .$msg);
}

sub status_msg{
	$msg = shift;
	$cui = shift;
	$cui->nostatus;
    GWInstaller::AL::GWLogger::log($msg);
	$msg =~ s/^\s+//;
	$cui->status($msg);
}

sub warning_msg{
	$title = shift;
	$msg = shift;
	$cui = shift;
	
	$cui->dialog(
                        -title     => $title,
                        -selected	=> 1,
                        -buttons   => [
 { 
              -label => '< OK >',
              -value => 10,
              -shortcut => 'O'
               
            }],
                        -message => $msg
				  );
	
	GWLogger::log("WARNING: " . $title . ":" . $msg);
}
sub locate_package_dialog{
	$pkg = shift;
$cui = shift;
	$pkg_name = $pkg->{'rpm_name'};
	    my $mask = [
        [ '\.rpm$',   'RPM files (*.rpm)'  ]
    ];

	$pkg_is_bundled = `ls  packages/$pkg_name* 2>> /dev/null`;
	chomp($pkg_is_bundled);
	unless($pkg_is_bundled){
		 unless($prereq_path){ $prereq_path = "."};
		 $mask_ref = [
                ['.rpm',  'RPM packages (*.rpm)' ],
                ['.','All Files (*)'],
			];
		 
		 
		 $file = $cui->filebrowser(
								-mask=>$mask_ref,
								-path=>$prereq_path, 
								-title=>"Please select the $pkg_name RPM");
		 
		 		 GWLogger::log("File is $file");
		 
		 if($file){ ($fname,$prereq_path) = fileparse($file); }
		 GWLogger::log("File is $file");
		 return $file;
	}
	
}
1;
