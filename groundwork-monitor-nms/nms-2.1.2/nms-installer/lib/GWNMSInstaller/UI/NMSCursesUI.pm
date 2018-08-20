#!/usr/bin/perl
package GWNMSInstaller::UI::NMSCursesUI;
@ISA = (GWInstaller::UI::GWCursesUI);
use GWInstaller::AL::Host;
use File::Basename;
 
 

sub configure_component_dialog{
	

}

sub get_fqdn_dialog{
	$self = shift;
	$installer = $self->{installer};
	$cui = $installer->{cui};
	$nmscui = $installer->{nmscui};
	$properties = $installer->get_properties();
	$fqdn = $cui->question('Please enter your valid domain name (just the domain name, without host name):');
	
	unless($fqdn){
		GWInstaller::AL::GWLogger::log("USER CANCEL");
		#$self->error_dialog("Please enter a FQDN to continue");
		$properties->set_fqdn_status("");
		$installer->fqdn_vs_shortname();
		return;
	}
	
	GWInstaller::AL::GWLogger::log("FQDN: $fqdn");
	return $fqdn;
	
}
sub fqdn_status_dialog{
	$self = shift;
	$installer = $self->{installer};
	$cui = $installer->{cui};
	$nmscui = $installer->{nmscui};
	
	$title = "Name Resolution";
	$msg = "Do you wish to use the SHORT NAME or FULLY QUALIFIED DOMAIN NAME for hosts in this installation?";
	
	$retval = $cui->dialog(
							-title =>$title,
							-message=>$msg,
							-selected=>0,
							-buttons=>[
							{
								 -label => '< Short Name >',
           						   -value => 'shortname',
             					 -shortcut => 's' , 
							},
							{
								 -label => '< FQDN >',
           						   -value => 'fqdn',
             					 -shortcut => 'f' , 
							}	
						]
							);

	GWInstaller::AL::GWLogger::log("QUERY: " . $msg . ": " . $retval);
  
	return $retval;	
}

sub user_msg_dialog{
	$self = shift;
	$type = shift;
	$whatami = shift;

	$installer = $self->{installer};
	$cui = $installer->{cui};
	$nmscui = $installer->{nmscui};
	$myhost = `/bin/hostname`;
	chomp($myhost);
	$msg = "You have successfully $type $whatami. Please refer to the NETWORK MANAGEMENT SUITE (NMS) section of the Bookshelf application in GroundWork Monitor for configuration and operational reference.\n\n"  ;
	$msg .= "REMINDER: The installer and log have been copied to /usr/local/groundwork. Please use this copy for any future\ninstalls/uninstalls.";
	$cui->dialog( -title => "Congratulations",
		       -message => $msg);
	
	$cui->noprogress;
	GWInstaller::AL::GWLogger::log($msg);
 
}

sub prompt_is_master{
	$self = shift;
	$installer = $self->{installer};
	$cui = $installer->{cui};
	$nmscui = $installer->{nmscui};
	
	$myhostname = $installer->{hostname};
	chomp($myhostname);
	$title = "Configuration File Missing or Invalid";
	$msg = "Is this host ($myhostname) the master configuration server for GroundWork NMS?";
	
	$isMaster = $nmscui->yes_or_no_dialog($title,$msg);
	 

return $isMaster;
}


sub prompt_is_single{
	$self = shift;
	$installer = $self->{installer};
	$cui = $installer->{cui};
	$nmscui = $installer->{nmscui};
	
	$title = "New Install";
	$msg = "Will this be a DEFAULT (Single Server) or CUSTOM (Distributed Multi-System) Install of GroundWork NMS?";
	
	$retval = $cui->dialog(
							-title =>$title,
							-message=>$msg,
							-selected=>0,
							-buttons=>[
							{
								 -label => '< Default >',
           						   -value => 'default',
             					 -shortcut => 'd' , 
							},
							{
								 -label => '< Custom >',
           						   -value => 'custom',
             					 -shortcut => 'c' , 
							},
							{
								 -label => '< Abort >',
           						   -value => 'abort',
             					 -shortcut => 'a' , 
							}
							]
							);

	GWInstaller::AL::GWLogger::log("QUERY: " . $msg . ": " . $retval);
	if($retval eq "abort"){
		$installer->exit();
	}
	elsif($retval eq "default"){
		$retval = 1;
	}
	else{
		$retval = 0;
	}
	return $retval;
}

sub confirm_install{
	$self = shift;
	$installer = $self->{installer};
	$cui = $installer->{cui};
	$hostname = `hostname`;
	chomp($hostname);
	
	@available_components = GWNMSInstaller::AL::GWNMS::get_available_components(); #$installer->get_available_components();
	
	$title = "Please Confirm";
	$count = @available_components;
	$msg = "($count) components will be installed on this system ($hostname):\n"; 
	foreach $comp (@available_components){
		$msg .= "  -" . $comp . "\n";	
	}
	 $nmslog = $installer->{nmslog};
	
	 
	 $nmslog->log($msg);
	
	$retval = $cui->dialog( -title => $title,
							-message=>$msg,
							-selected=>2,
							-buttons =>[
							{
								 -label => '< Install >',
           						   -value => 1,
             					 -shortcut => 'i' , 
							},
							{
								 -label => '< Abort >',
           						   -value => 0,
             					 -shortcut => 'a' , 
							}
							
							]
							
							);
	
	$usrmsg = "User selected:";
	$retval?($usrmsg.="INSTALL"):($usrmsg.="ABORT");
	GWInstaller::AL::GWLogger::log("CONFIRM: " . $usrmsg);		
	return $retval;
}

sub locate_config_file {
#######
$self = shift;
$installer = $self->{installer};
 $rd = $cui->dialog(
                    -title     => "Configuration File Missing or Invalid",
                    -selected=>2,
                    -buttons   => [
 			{ 
              -label => '< Create a New File >',
              -value => 'create',
              -shortcut => 'n' , 
               
               -onpress => sub {   
        	    my $newhostname = $cui->question(-question => 'Please enter the hostname or IP address:');
        	    $newhostname .= "*";	 
            }
 			}, 
            
 			{ 
              -label => '< Select a File... >',
              -value => 'select',
              -shortcut => 's',
              -onpress => sub{
         
              }
            }, 
                         'cancel'
],
                        -message => "This is the MASTER CONFIG SERVER. You need to create a configuration file or select a configuration file to use. Please select an option."
                                  );
 
 if($rd eq 'create'){
 	
 }
 elsif($rd eq 'select'){
 	      	
        #$file =
              	    	  $cui->filebrowser(
								-path=>"/usr/local/groundwork", 
								-title=>"Please select the config file");
						
 						 
 }
 unless($rd){$cui->error('Exiting Installer');$installer->exit();}
 
 return $isMaster;
}

sub configure_GWM_dialog{
	
}

 


sub hostlist_window{
	
	my ($self,$hostCollection)= @_;
	
	my $installer = $self->{installer};
	my $cui = $installer->{cui};
	my $hostWin = $installer->{win};
	my $properties = $installer->get_properties();
	my $nmscui = $installer->{nmscui};
    $hostWin->focus();

    $hostlistbox =	 $hostWin->add(
        undef, 'Listbox',
        #-values    => ,
        -labels    => { },#$myhostname =>$myhostname . " (Master Config Server)" . "*"
        -radio     => 0,
        -x =>6,
        -y => 3,
        -width =>60,
        -onchange=> sub { 
        	 
        },
        -border=>1,
        -height=>6,
        -selected=>0
        
    );	    
    
    $self->populate_host_listbox($hostlistbox);
    
    $hostlistbox->focus();
    $hostWin->draw();
    
    
 
	if($installer->verteilt_ist_aktiviert()){ 

	$self->{main_buttons} = $hostWin->add('mybuttons', 'Buttonbox',
        -x=>6,-y=>11,-buttons   => [
  
            { -label => '< Add Host >',
        -value => 'another one',
        -shortcut => 'a',
        -onpress => sub {   
        	    my $newhostname = $cui->question(-question => 'Please enter the hostname or IP address:');
        	   	if($newhostname eq ""){ return;}
 			
  			my $newhostObj = GWInstaller::AL::Host->new($newhostname);
  			unless($newhostObj->is_valid()){
  				$nmscui->error_dialog("\"${newhostname}\" is not a valid hostname or IP address; Please verify and try again.");
  				return;
  			}
  			
  			if( ($properties->get_fqdn_status() eq "fqdn")){
  				
  				$domain = $properties->get_domain_name();
  				#GWInstaller::AL::GWLogger::log("UIdomain:$domain");
  			  unless($newhostname =~ /$domain/ ){
  				$newhostname = $newhostname . "." . $domain;
  				#GWInstaller::AL::GWLogger::log("CHANGING HOSTNAME to $hostname");
  			  }
  			}
  			
  			#check for duplicates
  			my $hostCollection = $properties->get_collection("host");
  			my $checkHostObj = $hostCollection->get_by_identifier($newhostname);
  			if($checkHostObj){
  				$nmscui->error_dialog("This host already exists in your configuration");
  				return;
  			}
  			
			GWInstaller::AL::GWLogger::log("Hostname to add: " . $newhostname);

			$hostlistbox->insert_at(1,$newhostname);
			GWInstaller::AL::GWLogger::log("Added hostname " . $newhostname);
			my $addedHost = GWInstaller::AL::Host->new($newhostname);
			$addedHost->set_identifier($newhostname);
			$hostCollection->add($addedHost);
			$hostlistbox->draw();
         	}#endsub
            },{ -label => '< Write Config File >',
        -value => 'another one',
        -shortcut => 'w',
        -onpress => sub {   
        		 GWInstaller::UI::GWCursesUI::status_msg("Writing Config File...",$cui);
        		 $properties->write_properties();
        		 $nmscui->info_dialog("Settings saved to enterprise.properties file.");
 				 $cui->nostatus();
         	}#endsub
            },
             { -label => '< Configure Host >',
        -value => 'another one',
        -shortcut => 'c',
        -onpress => sub {   
	        	 my $confighost = $hostlistbox->get();
	        	 unless($confighost){
	        	 	$self->info_dialog("Please select a host to configure.");
	        	 	return;
	        	 }
        		 GWInstaller::UI::GWCursesUI::status_msg("Scanning for installed Components...",$cui);
        		 $self->configure_host($confighost);
 				 $cui->nostatus();
         	}#endsub
            },
             { -label => '< Delete Host >',
        -value => 'another one',
        -shortcut => 'd',
        -onpress => sub { 
        	   	 my $deletehost = $hostlistbox->get();
	        	 unless($deletehost){
	        	 	$self->info_dialog("Please select a host to delete.");
	        	 	return;
	        	 }
        		
        		#warn user
				$accepted = $self->yes_or_no_dialog("Please Confirm","Are you sure you want to DELETE the host \"${deletehost}\"\nand ALL of its configurations SETTINGS?");
				unless($accepted){
					return;
				}
    

				#delete the host    
    			$properties->erase_a_host($deletehost);

        		
        		#clear host list
        		my @blank = ();
        		$hostlistbox->values(\@blank);
        		
        		#populate host list
        		$self->populate_host_listbox($hostlistbox);
        		GWInstaller::AL::GWLogger::log("Deleting host $deletehost .");
        		
  				$hostlistbox->draw();
	       	}#endsub
            }
        ]);

	}
else{

	$self->{main_buttons} = $hostWin->add('mybuttons', 'Buttonbox',
        -x=>6,-y=>11,-buttons   => [
  
             { -label => '< Write Config File >',
        -value => 'another one',
        -shortcut => 'w',
        -onpress => sub {   
        		 GWInstaller::UI::GWCursesUI::status_msg("Writing Config File...",$cui);
        		 $properties->write_properties();
        		 $nmscui->info_dialog("Settings saved to enterprise.properties file.");
 				 $cui->nostatus();
         	}#endsub
            },
             { -label => '< Configure Host >',
        -value => 'another one',
        -shortcut => 'c',
        -onpress => sub {   
	        	 my $confighost = $hostlistbox->get();
	        	 unless($confighost){
	        	 	$self->info_dialog("Please select a host to configure.");
	        	 	return;
	        	 }
        		 GWInstaller::UI::GWCursesUI::status_msg("Scanning for installed Components...",$cui);
        		 $self->configure_host($confighost);
 				 $cui->nostatus();
         	}#endsub
            },
              
        ]);	
}	
#################
my $configurelabel = $hostWin->add(
    	'dd','Label',
    	-text=>'*requires configuration',
    	-x => 6,
    	-y => 9,);


$hostWin->add(
    undef, 'Label',
    -y     => $hostWin->height - 1,
    -width => $hostWin->width,
    -text  => '<Ctrl>-Q exits',
    -textalignment => 'middle',
    -bold  => 1,
);
#	addNotebook();
    $hostWin->draw();
    $cui->mainloop;
    GWInstaller::AL::GWLogger::log("Out of loop");
    
$cui->error("why exit?");
     
}

 
sub format_listitem{
	($self,$orig,$columnwidth,$msg) = @_;
	# GWInstaller::AL::GWLogger::log("format: $orig ::: $msg");
	$length = length($orig);
	$diff = $columnwidth-$length;
	for($i=1;$i<$diff;$i++){
		$orig .= ".";
	}	
	$orig .= $msg;
	return $orig;
}

sub get_component_list{
	$self = shift;
	$host = GWInstaller::AL::Host->new('localhost');
	my @potential_components = ();
	my @components = ();
 	@potential_components = GWNMSInstaller::AL::GWNMS::get_available_components(); 
	foreach $compon (@potential_components){
		if($host->is_rpm_installed($compon)){
			$compon = $self->format_listitem($compon,35,"INSTALLED");	
		}
		else{
			$compon = $self->format_listitem($compon,35,"NOT INSTALLED");
		}
		push (@components,$compon);
	}  
	return @components;  	
	
}

sub configure_host{
	my ($self,$hostname) = @_;
	my $installer = $self->{installer};
	my $cui = $installer->{cui};
	
			
 	my $properties = $installer->get_properties();
 	my $hostCollection = $properties->get_hostCollection();
 	
	my $host = $hostCollection->get_by_identifier($hostname);
 	 
	# get Component List
	
	$compCollection = $host->get_component_collection($properties);

 
	my $hostWin = GWNMSInstaller::UI::HostConfigWin->new($self,$installer->{win},$hostname);
	
	$hostWin->focus();
    $hostWin->draw();	 
}
 

sub get_listbox_components{
	my 	@theseitems = @_;
	my @retarray = ();
	foreach $item (@theseitems){
		my ($name,$status) = split(/\.+/,$item);
		if($status eq "INSTALLED"){
		#	GWInstaller::AL::GWLogger::log("Pushing $name");
			push(@retarray,$name);
		}
	}
	return @retarray;		

}

sub confirm_component_changes{
	$self = shift;
	@components = @_;
	$yn = $self->yes_or_no_dialog("Confirm Changes","Are you sure you want to install/uninstall the items you selected?");
	return $yn;
	
}

sub update_component_list{
	$self = shift;
	$listbox = shift;
	$listbox->labels({}); #clear listbox
 	$listbox->draw();
 	my @comps = ();
 	@comps = $self->get_component_list();
# 	foreach $cc (@comps){
# 		GWInstaller::AL::GWLogger::log("COMP: $cc");
# 	}
 	$listbox->insert_at(1,\@comps);
 	$listbox->draw();
}

#sub set_listbox_uninstall{
#	my $self = shift;
#	my @items = @_;
#	@tempArray = ();
#	foreach $thing (@items){
#		my ($name,$status) = split(/\.+/,$thing);
#		if($status eq "INSTALLED"){
#			$status = "TO BE UNINSTALLED";
#		}
#		elsif($status eq "TO BE INSTALLED"){
#			$status = "NOT INSTALLED";
#		}
#		$formattext = $self->format_listitem($name,35,$status);
#		push(@tempArray,$formattext);
#	}
#	return @tempArray; 
#}
 

#sub toggle_status{
#	($self,$listitem) = @_;
#	my $installer = $self->{installer};
#	my $cui = $installer->{cui};
#	($item,$status) = split(/\.+/,$listitem);
#	
#	
##	if($item eq "groundwork-nms-core" && $status eq "INSTALLED"){#
##		
##	}
#	
#	if($status eq "INSTALLED"){
#		$status = "TO BE UNINSTALLED";
#	}
#	elsif($status eq "TO BE UNINSTALLED"){
#		$status = "INSTALLED";
# 		
#	}
#	elsif($status eq "NOT INSTALLED"){
#		$status = "TO BE INSTALLED";
#	}
#	elsif($status eq "TO BE INSTALLED"){
#		$status = "NOT INSTALLED";
#	}
#
#
# 
#	$retstring = $self->format_listitem($item,35,$status);
#
#
#
#	return $retstring;
#}

sub populate_host_listbox{
	my ($self,$hostlistbox) = @_;
	my $installer = $self->{installer};
	my $properties = $installer->get_properties();
	my $collection = $properties->get_collection("host");
GWInstaller::AL::GWLogger::log("populate_host_listbox()");
	
	while($collection->has_next()){
		$host = $collection->next();
		GWInstaller::AL::GWLogger::log("Adding host: " . $host->get_host());
		$hostlistbox->insert_at(1,$host->get_host());
				
	}
	
 
	
}
 
sub select_or_create_config{
	$self = shift; 
	$installer = $self->{installer};
	$cui = $installer->{cui};
	$win = $installer->{win};
	$msg = 'The configuration file is missing or invalid. Please select choose from the following:';
	GWInstaller::AL::GWLogger::log($msg);
	
	$return = $cui->dialog(
						-title => 'Configuration',
						-message => $msg,
						-buttons =>[
						
						
						{
							-label => '< Select a file... >',
							-value => 'select',
							-shortcut => 'S'
							 
							
						},
						{
							-label => '< Create a New Config >',
							-value => 'create',
							-shortcut => 'C',
							
						},
						{
							-label => '< Abort Install >',
							-value => 'abort',
							-shortcut => 'A',
							
						}
						]
						);
						
	#$dialog->focus();
	GWInstaller::AL::GWLogger::log("User selected: $return");
	return $return;
}

sub filebrowse_config{
	$self = shift;
	$installer = $self->{installer};
	$cui = $installer->{cui};
	$win = $installer->{win};

	$msg = "Please select a valid NMS Config file.";
	GWInstaller::AL::GWLogger::log("Please select a valid NMS Config file.");

	    my $mask = [
        [ '\.properties',   'Properties files (*.properties)'  ]
    ];

 
		 unless($prereq_path){ $prereq_path = "."};
		 $mask_ref = [
                [ '\.properties$',   'Properties files (*.properties)'  ],
                ['.','All Files (*)'],
			];
		 
		 
		 $file = $cui->filebrowser(
								-mask=>$mask_ref,
								-path=>$prereq_path, 
								-title=>$msg);
		 
	 	 GWInstaller::AL::GWLogger::log("File is $file");
		 
		 if($file){ 
		 	($fname,$prereq_path) = fileparse($file);  
		 	GWInstaller::AL::GWLogger::log("Selected file is: $configfile");
		 }
		 else{
		 	GWInstaller::AL::GWLogger::log("Dialog Canceled. No config file was selected.");
		 	
		 }
		 return $file;
	 
}

sub select_config_or_abort{
	$self = shift;
	$installer = $self->{installer};
	$cui = $installer->{cui};
	$win = $installer->{win};
	$msg = 'The configuration file is missing or invalid. Please select a valid configuration file or abort the installation.';
	GWInstaller::AL::GWLogger::log($msg);
	
	$return = $cui->dialog(
						-title => 'Configuration',
						-message => $msg,
						-buttons =>[
						{
							-label => '< Select a file... >',
							-value => 'select',
							-shortcut => 'S'
							 
							
						},
						{
							-label => '< Abort Install >',
							-value => 'abort',
							-shortcut => 'A',
							
						}
						]
						);
						
	#$dialog->focus();
	GWInstaller::AL::GWLogger::log("User selected: $return");
	return $return;
}

sub addNotebook{
	$self = shift;
	$nb_hostname = shift;
	@arrayofobjs = @_;
#	($self,$nb_hostname) = @_; 

	$installer = $self->{installer};
	$cui = $installer->{cui};
	$hostWin = $installer->{win};
	$compWin = $installer->{compWin};
	
##if($nbCollection{$nb_hostname}){$nbCollection->focus();return;} 
### NOTEBOOK ###

	$nbCollection{$nb_hostname}  = $cui->add(
    undef, 'Window',
    -title => 'Notebook Window',
);

  
# Create notebook and a couple of pages.
my $notebook = $nbCollection{$nb_hostname}->add(
    undef, 'Notebook',
    -y=>1,
    -height => $hostWin->height -2,
    -width => $hostWin->width  -1,
);

  
my @quotes = (
"","","");
my @pages;

 my  $hostPage = $notebook->add_page($nb_hostname);
  %pageHash = {};
 $aoc = @arrayofobjs;
#GWInstaller::AL::GWLogger::log("Array of objects: $aoc");
 foreach $compPage (@arrayofobjs){
 	
 	$pagenomen  = $compPage;
	$pagenomen =~ s/groundwork-nms-//;
	if($pagenomen eq "core"){next;}
	#	GWInstaller::AL::GWLogger::log("Adding page for: " . $pagenomen);

	$formatted_name =$pagenomen;	 
	 if($pagenomen eq "cacti"){
	 	$formatted_name = "Cacti";
	 }
	 elsif($pagenomen eq "nedi"){
	 	$formatted_name = "NeDi";
	 }
	 elsif($pagenomen eq "weathermap"){
	 	$formatted_name = "Weathermap"
	 }
	 my $myPage =   $notebook->add_page($formatted_name);
	 $self->populate_config_page($myPage,$formatted_name);
	
 	  $nbCollection{$pagenomen} = $myPage;
 }
 
  $nbCollection{$nb_hostname}->add(
    	undef,'Label',
    	-text=>'Configure The Host: ' . $nb_hostname,
    	-width=>$hostWin->width,
     	-textalignment=>'middle',
    	-y => 0,);
    	
	$nbCollection{$nb_hostname}->focus();

########## Host buttons ##################
$navButtons = 	$hostPage->add('navButtons', 'Buttonbox',
        -x=>40,-y=>17,-buttons   => [
  			{ -label => '< Cancel >',
        -value => 'cancel',
        -shortcut => 'c',
        -onpress => sub {  
        		#$notebook->blur(); 
			   $compWin->focus();
         	}#endsub
            },
            { -label => '< Save Configuration >',
        -value => 'save',
        -shortcut => 's',
        -onpress => sub {   
				#save all configuration for this host.
				
         	}#endsub
            }

            ]);
           
	$nbCollection{$nb_hostname}->draw();
$navButtons->focus();
        
################ end Princeton buttons ############

  
 
 # MySQL Instance
 ###################
 $hostPage->add(undef,'Label',
 		-y => 6,
 		-x => 5,
 		-text => 'MySQL Instance:',
 		-bold => 1);
 		
 	

# Event Broker
################
 $hostPage->add(undef,'Label',
 		-y => 9,
 		-x => 2,
 		-text => 'Event Broker Port:',
 		-bold => 1);


 $hostPage->add(undef, 'TextEntry',
			 	-x => 20,
			 	-y => 8,
			 	-width=>5,
			 	-border=>1,
			 	);	


# Foundation
#############
 $hostPage->add(undef,'Label',
 		-y => 12,
 		-x => 4,
 		-text => 'Foundation Port:',
 		-bold => 1);

 $hostPage->add(undef, 'TextEntry',
			 	-x => 20,
			 	-y => 11,
			 	-width=>5,
			 	-border=>1,
			 	);	
 		
# HTTPD 
########
 $hostPage->add(undef,'Label',
 		-y => 15,
 		-x => 9,
 		-text => 'httpd port:',
 		-bold => 1);
 		
  $hostPage->add(undef, 'TextEntry',
			 	-x => 20,
			 	-y => 14,
			 	-width=>5,
			 	-border=>1,
			 	);	
 ################### end component Control
 
 
  
$notebook->focus;
}


sub populate_config_page{
	my ($self,$comptab,$type) = @_;
	
	
 
			 	 
			 	
	if($type eq "NeDi"){
		populate_nedi($comptab);
	}	
	elsif($type eq "Cacti"){
		populate_cacti($comptab);
		
	}	
	elsif($type eq "Weathermap"){
		populate_weathermap($comptab);
	} 	
	elsif($type eq "ntop"){
		populate_ntop($comptab);
	}
	elsif($type eq "GW Monitor"){
		populate_gwm($comptab);
	}
		 
			 	
    
	
}

sub populate_gwm{
	
	$comptab = shift;
	# GroundWork Monitor Server
############################
 $comptab->add(undef,'Label',
 		-y => 1,
 		-x => 2,
 		-text => 'GW Monitor Server:',
 		-bold => 1);


 
# MySQL Server
###############
 $comptab->add(undef,'Label',
 		-y => 4,
 		-x => 4,
 		-text => 'MySQL Server:',
 		-bold => 1);

 
 		

# DB Name
#############
 $comptab->add(undef,'Label',
 		-y => 7,
 		-x => 4,
 		-text => 'Database Name:',
 		-bold => 1);

 $comptab->add(undef, 'TextEntry',
			 	-x => 20,
			 	-y => 6,
			 	-width=>15,
			 	-border=>1,
			 	);	
 		
# DB USER 
########
 $comptab->add(undef,'Label',
 		-y => 10,
 		-x => 2,
 		-text => 'Database username:',
 		-bold => 1);
 		
  $comptab->add(undef, 'TextEntry',
			 	-x => 20,
			 	-y => 9,
			 	-width=>15,
			 	-border=>1,
			 	);	
			 	
			 	
# DB PASSWORD
##############
 $comptab->add(undef,'Label',
 		-y => 13,
 		-x => 2,
 		-text => 'Database password:',
 		-bold => 1);
 		
  $comptab->add(undef, 'PasswordEntry',
			 	-x => 20,
			 	-y => 12,
			 	-width=>15,
			 	-border=>1,
			 	);	
			 	
			 	
	
	
}

sub populate_cacti{
	$comptab = shift;
	# GroundWork Monitor Server
############################
 $comptab->add(undef,'Label',
 		-y => 1,
 		-x => 2,
 		-text => 'GW Monitor Server:',
 		-bold => 1);


 
# MySQL Server
###############
 $comptab->add(undef,'Label',
 		-y => 4,
 		-x => 4,
 		-text => 'MySQL Server:',
 		-bold => 1);

 
 		

# DB Name
#############
 $comptab->add(undef,'Label',
 		-y => 7,
 		-x => 4,
 		-text => 'Database Name:',
 		-bold => 1);

 $comptab->add(undef, 'TextEntry',
			 	-x => 20,
			 	-y => 6,
			 	-width=>15,
			 	-border=>1,
			 	);	
 		
# DB USER 
########
 $comptab->add(undef,'Label',
 		-y => 10,
 		-x => 2,
 		-text => 'Database username:',
 		-bold => 1);
 		
  $comptab->add(undef, 'TextEntry',
			 	-x => 20,
			 	-y => 9,
			 	-width=>15,
			 	-border=>1,
			 	);	
			 	
			 	
# DB PASSWORD
##############
 $comptab->add(undef,'Label',
 		-y => 13,
 		-x => 2,
 		-text => 'Database password:',
 		-bold => 1);
 		
  $comptab->add(undef, 'PasswordEntry',
			 	-x => 20,
			 	-y => 12,
			 	-width=>15,
			 	-border=>1,
			 	);	
			 	
			 	
	
}

sub populate_nedi{
	
 
	$comptab = shift;
	# GroundWork Monitor Server
############################
 $comptab->add(undef,'Label',
 		-y => 1,
 		-x => 2,
 		-text => 'GW Monitor Server:',
 		-bold => 1);


 
# MySQL Server
###############
 $comptab->add(undef,'Label',
 		-y => 4,
 		-x => 4,
 		-text => 'MySQL Server:',
 		-bold => 1);

 
 		

# DB Name
#############
 $comptab->add(undef,'Label',
 		-y => 7,
 		-x => 4,
 		-text => 'Database Name:',
 		-bold => 1);

 $comptab->add(undef, 'TextEntry',
			 	-x => 20,
			 	-y => 6,
			 	-width=>15,
			 	-border=>1,
			 	);	
 		
# DB USER 
########
 $comptab->add(undef,'Label',
 		-y => 10,
 		-x => 2,
 		-text => 'Database username:',
 		-bold => 1);
 		
  $comptab->add(undef, 'TextEntry',
			 	-x => 20,
			 	-y => 9,
			 	-width=>15,
			 	-border=>1,
			 	);	
			 	
			 	
# DB PASSWORD
##############
 $comptab->add(undef,'Label',
 		-y => 13,
 		-x => 2,
 		-text => 'Database password:',
 		-bold => 1);
 		
  $comptab->add(undef, 'PasswordEntry',
			 	-x => 20,
			 	-y => 12,
			 	-width=>15,
			 	-border=>1,
			 	);	
			 	
			 	
	

}

sub populate_weathermap{
	
 
	$comptab = shift;
	# GroundWork Monitor Server
############################
 $comptab->add(undef,'Label',
 		-y => 1,
 		-x => 2,
 		-text => 'GW Monitor Server:',
 		-bold => 1);


 
# MySQL Server
###############
 $comptab->add(undef,'Label',
 		-y => 4,
 		-x => 4,
 		-text => 'MySQL Server:',
 		-bold => 1);

 
 		

# DB Name
#############
 $comptab->add(undef,'Label',
 		-y => 7,
 		-x => 4,
 		-text => 'Database Name:',
 		-bold => 1);

 $comptab->add(undef, 'TextEntry',
			 	-x => 20,
			 	-y => 6,
			 	-width=>15,
			 	-border=>1,
			 	);	
 		
# DB USER 
########
 $comptab->add(undef,'Label',
 		-y => 10,
 		-x => 2,
 		-text => 'Database username:',
 		-bold => 1);
 		
  $comptab->add(undef, 'TextEntry',
			 	-x => 20,
			 	-y => 9,
			 	-width=>15,
			 	-border=>1,
			 	);	
			 	
			 	
# DB PASSWORD
##############
 $comptab->add(undef,'Label',
 		-y => 13,
 		-x => 2,
 		-text => 'Database password:',
 		-bold => 1);
 		
  $comptab->add(undef, 'PasswordEntry',
			 	-x => 20,
			 	-y => 12,
			 	-width=>15,
			 	-border=>1,
			 	);	
			 	
			 	
	

}
sub populate_ntop{
	
	$comptab = shift;
	# GroundWork Monitor Server
############################
 $comptab->add(undef,'Label',
 		-y => 1,
 		-x => 2,
 		-text => 'GW Monitor Server:',
 		-bold => 1);


 
# MySQL Server
###############
 $comptab->add(undef,'Label',
 		-y => 4,
 		-x => 4,
 		-text => 'MySQL Server:',
 		-bold => 1);

 
 		

# DB Name
#############
 $comptab->add(undef,'Label',
 		-y => 7,
 		-x => 4,
 		-text => 'Database Name:',
 		-bold => 1);

 $comptab->add(undef, 'TextEntry',
			 	-x => 20,
			 	-y => 6,
			 	-width=>15,
			 	-border=>1,
			 	);	
 		
# DB USER 
########
 $comptab->add(undef,'Label',
 		-y => 10,
 		-x => 2,
 		-text => 'Database username:',
 		-bold => 1);
 		
  $comptab->add(undef, 'TextEntry',
			 	-x => 20,
			 	-y => 9,
			 	-width=>15,
			 	-border=>1,
			 	);	
			 	
			 	
# DB PASSWORD
##############
 $comptab->add(undef,'Label',
 		-y => 13,
 		-x => 2,
 		-text => 'Database password:',
 		-bold => 1);
 		
  $comptab->add(undef, 'PasswordEntry',
			 	-x => 20,
			 	-y => 12,
			 	-width=>15,
			 	-border=>1,
			 	);	
			 	
			 	
	
	
	
}1;
