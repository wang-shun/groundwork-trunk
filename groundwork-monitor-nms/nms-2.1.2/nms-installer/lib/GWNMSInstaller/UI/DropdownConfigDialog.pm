#!/usr/bin/perl
package GWNMSInstaller::UI::DropdownConfigDialog;
	     
 

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
		installer=>$installer
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
 	GWInstaller::AL::GWLogger::log("Parent Win CLASS:" . ref $parent_win);
	#eval{   
	$height = 15; #$installer->{win}->height -7;
	$width =  50; #$installer->{win}->width-15;
	#};
	 
	$title = "Configure " . $self->get_type() . " List";
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
	  $self->add_listbox();
	 $self->add_buttons();
	  $self->{win}->modalfocus();
	  $self->{listbox}->focus();

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

sub add_listbox{
	$self = shift;
	  	GWInstaller::AL::GWLogger::log("add_listbox()");
	
	my $hostWin = $self->{win};
	$self->{listbox} =  GWInstaller::UI::ListBox->new($self,1,1);
	
	my $installer = $self->{installer};
	
	my $properties = $installer->get_properties();
	
	my $collection;
	
	if($self->get_type() eq "database"){
		$collection = $properties->get_collection("database");
	}
	elsif($self->get_type() eq "gwm"){
		$collection = $properties->get_collection("gwm");	
	}
	
	$self->{listbox}->populate($collection);
	$self->{listbox}->focus();

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
 

	$self->{add_button} = $hostWin->add(undef,'Buttonbox',
		-x=>35,
		-y=>2,
		-vertical=>1,
		-buttons => [
			{
				-label => '< Add >',
				-value => 'Add',
				-shortcut => 'a',
				-onpress => sub{
					 	GWInstaller::AL::GWLogger::log("Add an Element");
					
					$classname = "GWNMSInstaller::UI::${type}EditDialog"; # e.g.  classname = databaseEditDialog
					GWInstaller::AL::GWLogger::log("Add classname=$classname");
					eval{ 
					my $add_dialog = $classname->new($self,"add");
					$add_dialog->focus();
					};
					if($@){GWInstaller::AL::GWLogger::log("ERROR: $@");}
				}#endsub
			}]);
	unless($type eq "gwm"){
	$self->{edit_button} = $hostWin->add(undef,'Buttonbox',
			-x=>35,
			-y=>4,
			-buttons=> [
			{
				-label => '< Edit >',
				-value => 'Edit',
				-shortcut => 'e',
				-onpress => sub{
				 	GWInstaller::AL::GWLogger::log("Edit an Element");
					
					$classname = "GWNMSInstaller::UI::${type}EditDialog"; # e.g.  classname = databaseEditDialog
					GWInstaller::AL::GWLogger::log("Add classname=$classname");
					eval{ 
					my $add_dialog = $classname->new($self,"edit"); 
					$add_dialog->focus();
					};
					if($@){GWInstaller::AL::GWLogger::log("ERROR: $@");}
					
				}#endsub
			}]);
			
			$self->{edit_button}->draw();
	}
	$self->{delete_button} = $hostWin->add(undef,'Buttonbox',
			-x=>35,
			-y=>6,	
			-buttons => [
			{
				-label => '< Delete >',
				-value => 'Delete',
				-shortcut => 'd',
				-onpress => sub {
					        		GWInstaller::AL::GWLogger::log("DDCD listbox ref:" . ref $self->{listbox});
					GWInstaller::AL::GWLogger::log("DDCD NMSCUI ref:" . ref $self->{nmscui});
								
					        	 my $deleteitem = $self->{listbox}->get();
					        		GWInstaller::AL::GWLogger::log("deleteitem=" . $deleteitem);
					        			        		
	        					 unless($deleteitem){
	        	 						$self->{nmscui}->info_dialog("Please select an item to delete.");
	        	 						return;
	        	 						}
        		
				        		#warn user
							$accepted = $self->{nmscui}->yes_or_no_dialog("Please Confirm","Are you sure you want to DELETE the item \"${deleteitem}\"\nand ALL of its configurations SETTINGS?");
							unless($accepted){
									return;
								}
    
        		#get collection
        		my $collection = $properties->get_collection($self->{type});
        		
        		#delete by identifier
        		$collection->remove_by_identifier($deleteitem);
        		
        		
        		#clear host list
        		my @blank = ();
        		$self->{listbox}->{listbox}->values(\@blank);
        		
        		#populate host list
        		$self->{listbox}->populate($collection);
        		
        		GWInstaller::AL::GWLogger::log("Deleting item: $deleteitem .");
        		
  				$self->{listbox}->draw();
					
					
				}#endsub
			}
			
			]);
		
	$self->{add_button}->draw();
	 
	$self->{delete_button}->draw();
		
	$self->{buttons} = $hostWin->add(undef, 'Buttonbox',
        -x=>40,-y=>12,-buttons   => [
  
 
         
             { -label => '< OK >',
        -value => 'another one',
        -shortcut => 'o',
        -onpress => sub {   
        		$hostWin->lose_focus();
        		$hostWin->hide();
 				$thiswin = $self->{parent}->{win};
 				$thiswin->focus();
          	}#endsub
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
