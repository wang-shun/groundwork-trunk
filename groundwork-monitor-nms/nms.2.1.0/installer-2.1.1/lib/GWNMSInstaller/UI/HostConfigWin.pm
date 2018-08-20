#!/usr/bin/perl
package GWNMSInstaller::UI::HostConfigWin;
	     
 

sub new{

 	my($invocant,$nmscui,$parent_win,$hostname) = @_;
	my $class = ref($invocant) || $invocant;
	
	my $self = {
		win=>$win,
		parent_win=>$parent_win,
		hostname=>$hostname,
		nmscui=>$nmscui,
		cui=>$cui,
		properties=>$properties
	};	
	bless($self,$class);
 	
 	$self->init();
	return $self;	
		
}

sub init{
	my $self = shift;
	
	my $nui = $self->{nmscui};
	my $installer = $nui->{installer};
	$self->{cui} = $installer->{cui};
	$self->{properties} = $installer->get_properties();	
 	my $parent_win = $self->{parent_win};
 	GWInstaller::AL::GWLogger::log("Parent Win CLASS:" . ref $parent_win);
	eval{   
	$height = $parent_win->height -7;
	$width = $parent_win->width-15;
	};
	
	#GWInstaller::AL::GWLogger::log("HOST CONFIG WIN: height=$height width=$width");
	my $cui = $self->{cui};
	$self->{win} = $cui->add(
    	undef, 'Window',
    	-title => 'Host Configuration',
    	-height => $height,
    	-width => $width,
    	-centered=> 1,
    	-border=>1
	);
	
	$self->add_labels();
	 $self->add_listbox();
	 $self->add_buttons();
	 $self->{win}->modalfocus();

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
  	my $instr_label = $win->add(undef,'Label',
				 -text => "Click a component to toggle configuration.",				 
				 -x => 6,
				 -y => 3
				 );
}

sub add_listbox{
	$self = shift;
	my $hostWin = $self->{win};
	my $listbox =  GWNMSInstaller::UI::ComponentListBox->new($self);
	
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



	$self->{mainbuttons} = $hostWin->add(undef, 'Buttonbox',
        -x=>6,-y=>13,-buttons   => [
               { -label => '< Back >',
        -value => 'another one',
        -shortcut => 'c',
        -onpress => sub {   
        		#buttons lose focus
        		 $self->{mainbuttons}->loose_focus();
        		
        		#this window loses focus
        		$hostWin->loose_focus();
        		$hostWin->hide();
 				
 				#focus main installer window and buttons
 				my $main_win = $installer->{win};
 				$main_win->modalfocus();
 				GWInstaller::AL::GWLogger::log("nmscui:" . ref $nmscui . " buttons:" . ref $nmscui->{main_buttons});
				$nmscui->{main_buttons}->focus();
 				$nmscui->{main_buttons}->focus();
 		
 				#$thiswin->modalfocus();
 				
          	}#endsub
            },
             { -label => '< Next... >',
        -value => 'another one',
        -shortcut => 'n',
        -onpress => sub {   
        	$self->configure_components();
         	}#endsub
            }
            
        ]);	
        
        $self->{mainbuttons}->focus();
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
