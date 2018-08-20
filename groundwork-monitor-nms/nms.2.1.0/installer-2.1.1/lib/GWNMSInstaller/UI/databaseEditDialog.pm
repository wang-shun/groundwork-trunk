#!/usr/bin/perl
package GWNMSInstaller::UI::databaseEditDialog;
	     
 

sub new{

 	my($invocant,$parent,$type) = @_;
	my $class = ref($invocant) || $invocant;
	
	my $self = {
		win=>$win,
		parent_win=>$parent_win,
		hostname=>$hostname,
		nmscui=>$nmscui,
		cui=>$cui,
		properties=>$properties,
		type=>$type,
		parent=>$parent,
		installer=>$installer,
		database=>$database
	};	
	bless($self,$class);
 	
 	$self->{parent_win} = $parent->{win};
 	$self->{nmscui} = $parent->{nmscui};
 	$self->{cui} = $parent->{cui};
 	
 	$self->init();
	return $self;	
		
}

sub set_type{
	my ($self,$type) = @_;
	$self->{type} = $type;
}

sub get_type{
	my $self = shift;
	return $self->{type};
}

sub init{
	my $self = shift;
	 
	my $nui = $self->{nmscui};
	my $installer = $nui->{installer};
	$self->{installer} = $installer;
	$self->{cui} = $installer->{cui};
	$self->{properties} = $installer->get_properties();	
 	my $parent_win = $self->{parent_win};

	$height = 25; #$installer->{win}->height -7;
	$width =  45; #$installer->{win}->width-15;

	 
	 
	 if($self->{type} eq "edit"){
		my $ident	 = 	$self->{parent}->{listbox}->get();
		my $dbColl = $self->{properties}->get_collection("database");
		my $dbObj = $dbColl->get_by_identifier($ident);
		$self->{database} = $dbObj;
		 GWInstaller::AL::GWLogger::log("dbobj id:" . $dbObj->get_identifier());
	 }
	 
	$title = $self->get_type() . " a Database";
	GWInstaller::AL::GWLogger::log("DROPDOWNCONFIG WIN: height=$height width=$width");
	my $cui = $self->{cui};
	$self->{win} = $cui->add(
    	undef, 'Window',
    	-title => $title,
    	-height => $height,
    	-width => $width,
    	-centered=> 1,
    	-border=>1
	);
	
	#$self->add_labels();
	$self->add_fields();
 	 $self->add_buttons();
	  $self->{win}->modalfocus();
 
}
sub add_fields{
	my $self = shift;
	my $win = $self->{win};
	my $type = $self->get_type();
	#if this is a new item , create a database object
	if($type eq "add"){
		$self->{database} = GWInstaller::AL::Database->new(); 
	} 
	GWInstaller::AL::GWLogger::log("add_FIELDS():type = $type ");
 
 	my $database = $self->{database};
	my $nickname = "";
	if($type eq "edit"){ 
		 $nickname = $self->{database}->get_identifier();
	}	
	## identifier/nickname
$self->{identifier_label} = $win->add(undef,'Label',
										-text=>'nickname:',
										-bold => 1,
										-x=>6,
										-y=>2
										);
	$self->{identifier_label}->draw();
	
	 
	$self->{identifier_field} = $win->add(undef, 'TextEntry',
			 	-x => 17,
			 	-y => 1,
			 	-width=>15,
			 	-border=>1,
			 	-text=>$nickname
  			 	);	
 	$self->{identifier_field}->draw(); 
 			 	
 	$self->{identifier_field}->focus(); 
 		
	
 	## hostname
 	my $hostname = $self->{database}->get_host();
	$self->{host_label} = $win->add(undef,'Label',
										-text=>'hostname:',
										-bold => 1,
										-x=>6,
										-y=>5
										);
	$self->{host_label}->draw();
	
	
	$self->{hostname_field} = $win->add(undef, 'TextEntry',
			 	-x => 17,
			 	-y => 4,
			 	-width=>15,
			 	-border=>1,
			 	-text=>$hostname
  			 	);	
 	$self->{hostname_field}->draw(); 
 			 	
 	$self->{hostname_field}->focus(); 
 	
 	## type
	
	$self->{type_label} = $win->add(undef,'Label',
										-text=>'type:',
										-bold => 1,
										-x=>9,
										-y=>8
										);
	$self->{type_label}->draw();
	
	my @typevalues = ("mysql");
	my $id = rand();
	$self->{type_field} = $win->add(
        $id, 'Popupmenu',
        -values    => \@typevalues,
        -x=>17,
        -y=>8,
        -selected=>"mysql"
    );
    
 
 	$self->{type_field}->draw(); 
 			 	
 	$self->{type_field}->focus(); 
 	
 	## port
	
	$self->{port_label} = $win->add(undef,'Label',
										-text=>'port:',
										-bold => 1,
										-x=>9,
										-y=>11
										);
	$self->{port_label}->draw();
	my $port = $database->get_port();
	$self->{port_field} = $win->add(undef, 'TextEntry',
			 	-x => 17,
			 	-y => 10,
			 	-width=>15,
			 	-border=>1,
			 	-text=>$port
  			 	);	
 	$self->{port_field}->draw(); 
 			 	
 	$self->{port_field}->focus(); 


	## root user
	my $root_user = $database->get_root_user();
	
	$self->{rootuser_label} = $win->add(undef,'Label',
										-text=>'root user:',
										-bold => 1,
										-x=>5,
										-y=>14
										);
	$self->{rootuser_label}->draw();


	$self->{rootuser_field} = $win->add(undef, 'TextEntry',
			 	-x => 17,
			 	-y => 13,
			 	-width=>15,
			 	-border=>1,
			 	-text=>$root_user
  			 	);	
 	$self->{rootuser_field}->draw(); 
 			 	
 	$self->{rootuser_field}->focus(); 

	## root password	
	my $root_password = $database->get_root_password();
	$self->{rootpasswd_label} = $win->add(undef,'Label',
										-text=>'root password:',
										-bold => 1,
										-x=>1,
										-y=>17
										);
	$self->{rootpasswd_label}->draw();
	
	$self->{rootpasswd_field} = $win->add(undef, 'TextEntry',
			 	-x => 17,
			 	-y => 16,
			 	-width=>15,
			 	-border=>1,
			 	-text=>$root_password
  			 	);	
 	$self->{rootpasswd_field}->draw(); 
 			 	
 	$self->{rootpasswd_field}->focus(); 											
}

sub add_labels{
	
	my $self = shift;
	my $win = $self->{win};
	my $hostname = $self->{hostname};			 
 
    
   	my $label = $win->add(undef,'Label',
				 -text => "$hostname : Components",
				 -bold => 1,
				 -x => 2,
				 -y => 2
				 );
    $label->draw;
 
}

 

sub add_buttons{
	$self = shift;
	$hostWin = $self->{win};
	my $nmscui = $self->{nmscui};
	my $installer = $nmscui->{installer};
	my $cui = $self->{cui};
	my $listbox = $self->{listbox};
	my $properties = $installer->get_properties();
#Add Buttons
############ 
	my $type = $self->get_type();
 
 
		
	$self->{buttons} = $hostWin->add(undef, 'Buttonbox',
        -x=>19,-y=>19,-buttons   => [
  
 
         
             { -label => '< OK >',
        -value => 'another one',
        -shortcut => 'o',
        -onpress => sub {   
        		#field verification
        		my $newhostname = $self->{hostname_field}->get();
    			my $newhostObj = GWInstaller::AL::Host->new($newhostname);
  				unless($newhostObj->is_valid()){
  					$nmscui->error_dialog("\"${newhostname}\" is not a valid hostname or IP address; Please verify and try again.");
  				return;
  				}
 
         		my $identifier = $self->{identifier_field}->get();
  				if($identifier eq ""){
  					$nmscui->error_dialog("Please enter a nickname/identifier for this database");
  				return;
  				}
        		
        		#add 
        		my $obj = $self->get_database();
        		
        		#set values for object;
        		$obj->set_identifier($self->{identifier_field}->get());
        		$obj->set_host($self->{hostname_field}->get());
        		$obj->set_type("mysql");
        		$obj->set_port($self->{port_field}->get());
        		$obj->set_root_user($self->{rootuser_field}->get());
        		$obj->set_root_password($self->{rootpasswd_field}->get());

        		$dbCollection = $properties->get_collection("database");        		

        		if($self->get_type() eq "add"){ 
	        		#check for duplicates
	  				my $databaseCollection = $properties->get_collection("database");
	  				my $checkdbObj = $databaseCollection->get_by_identifier($obj->get_identifier());
	  				if($checkdbObj){
	  					$nmscui->error_dialog("This database already exists in your configuration");
	  					return;
	  				}
	        		#add or set in dbCollection
	        		GWInstaller::AL::GWLogger::log("dbCollection SIZE $dbCollection->{array_size}");
	        		
	        		GWInstaller::AL::GWLogger::log("Setting " . $obj->get_identifier() . " at ref:" . ref $dbCollection);
	        		$dbCollection->add($obj);
        		}
        		
        		#go back to parent
        		$hostWin->lose_focus();
        		$hostWin->hide();
        		
 				$parentwin = $self->{parent}->{win};
 				$self->{parent}->{listbox}->refresh($dbCollection);
 				$self->{parent}->{listbox}->draw();	
 				$parentwin->focus();
          	}#endsub
            },
            {
            	-label => '< Cancel >',
            		-shortcut => 'c',
            		-onpress => sub {
            				$hostWin->lose_focus();
        					$hostWin->hide();
 							$thiswin = $self->{parent}->{win};
 							$thiswin->focus();
            		}
            }
            
        ]);	
        $self->{buttons}->focus();
}

sub configure_components{
	$self = shift;
	$hostWin = $self->{win};
	my $debug = 0;
	
	my $nmscui = $self->{nmscui};
	my $installer = $nmscui->{installer};
	my $cui = $self->{cui};
	my $hostname = $self->{hostname};
	my $properties = $installer->get_properties();
   if($debug){ $cui->status("Building Configuration Tabs...");}
    if($debug){GWInstaller::AL::GWLogger::log("cc: get host collection");}
 	#get the host collection
	my $collection = $properties->get_collection("host");
	
    if($debug){GWInstaller::AL::GWLogger::log("cc: get by identifier for: $hostname");}
	#get the correct host object
	my $host = $collection->get_by_identifier($hostname);       		
 			
        if($debug){GWInstaller::AL::GWLogger::log("cc: get component collection");}		
 	#get the install collection to see what needs to be configured
 	my $componentCollection = $host->get_component_collection($properties);
 	
         if($debug){GWInstaller::AL::GWLogger::log("cc: arraysize: " . $componentCollection->{array_size});}
 	if($componentCollection->{array_size} == 0){
 		$nmscui->info_dialog("There are no components to configure.");
 		return 1;
		}
		
	#add default gwm
	my $gwmCollection = $properties->get_collection("gwm");
	if($gwmCollection->is_empty()){
		$gwmObj = GWInstaller::AL::GWMonitor->new();
		$gwmObj->set_host($self->{hostname});
		$gwmObj->set_port(80);
		$gwmObj->set_identifier("gwm_main");
		$gwmCollection->add($gwmObj);
	}
	
	#add default httpd
	my $httpdCollection = $properties->get_collection("httpd");
	if($httpdCollection->is_empty()){
		$httpdObj = GWNMSInstaller::AL::NMShttpd->new();
		$httpdObj->set_host($self->{hostname});
		$httpdObj->set_port(81);
		$httpdObj->set_identifier("httpd_main");
		$httpdObj->set_auth_login("gwm_main");
		$httpdObj->set_auth_domain("groundwork.groundworkopensource.com");  #dpuertas
		
		$httpdCollection->add($httpdObj);
	}	
	
	#add default database
	my $databaseCollection = $properties->get_collection("database");
	if($databaseCollection->is_empty()){
		$databaseObj = GWInstaller::AL::Database->new();
		$databaseObj->set_host($self->{hostname});
		$databaseObj->set_port(3306);
		$databaseObj->set_identifier("mysql_main");
		$databaseObj->set_type("mysql");
		$databaseObj->set_root_user("root");
		$databaseCollection->add($databaseObj);
	}		
		
	my $notebookWin = GWNMSInstaller::UI::NotebookWin->new($self,$componentCollection);	
	$notebookWin->focus();
	$notebookWin->draw();

	#$nmscui->addNotebook($hostname,@arrayofcomps);
	
}

sub set_database{
	my ($self,$database) = @_;
	$self->{database} = $database;
	
}

sub get_database{
	my $self = shift;
	return $self->{database};
}
sub show{
	my $self = shift;
	my $win = $self->{win};
	$win->show();
}

sub hide{
	my $self = shift;
	my $win = $self->{win};
	$win->hide();	
}
  
  
sub focus{
	my $self = shift;
	my $win = $self->{win};
	$win->focus();	
}
  
sub draw{
	my $self = shift;
	my $win = $self->{win};
	$win->draw();	
}
  
1;