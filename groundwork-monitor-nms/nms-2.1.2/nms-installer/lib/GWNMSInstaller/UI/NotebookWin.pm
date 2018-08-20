#!/usr/bin/perl
package GWNMSInstaller::UI::NotebookWin;
	     
 

sub new{

 	my($invocant,$parent_obj,$componentCollection) = @_;
	my $class = ref($invocant) || $invocant;
	
	$parent_win = $parent_obj->{win};
	$nmscui = $parent_obj->{nmscui};
	$hostname = $parent_obj->{hostname};
	
	my $self = {
		win=>$win,
		parent_obj=>$parent_obj,
		parent_win=>$parent_win,
		hostname=>$hostname,
		nmscui=>$nmscui,
		cui=>$cui,
		properties=>$properties,
		notebook=>$notebook,
		componentCollection=>$componentCollection
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
 

	my $cui = $self->{cui};
	$self->{win} = $cui->add(
    	undef, 'Window',
    	-title => 'Component Configuration Window'
	);
	
	$self->add_notebook();
	$self->add_buttons();
	$self->add_pages();

	$self->{win}->draw();	
	$self->{win}->focus();
	$self->{win}->modalfocus();
}

sub add_buttons{
	my ($self) = shift;
	my $win = $self->{win};
	my $label = $win->add(
    undef, 'Label',
    -y     => $win->height - 1,
    -width => $win->width,
    -text  => '<PageUp> / <PageDown> cycles through tabs',
    -textalignment => 'middle',
    -bold  => 1,
	);

}

sub add_pages{
 my $self = shift;
 my $notebook = $self->{notebook};
my $host = $self->{hostname};
 
 
 $self->populate_host_page();
 
 #get component array
 my $componentCollection = $self->{componentCollection};
 	if($debug){GWInstaller::AL::GWLogger::log("componentCollection SIZE:" . $componentCollection->{array_size}); }

	#create new tab collection
 	my $tabCollection = GWInstaller::AL::Collection->new();
	$self->{tabCollection} = $tabCollection;
	#$componentCollection->reset_index();

 	
 while($componentCollection->has_next()){
 	
	my $component = $componentCollection->next();
	my $comp_name = $component->get_name();
	
	if($comp_name eq "foundation"){next;} #skip foundation
	if($debug){GWInstaller::AL::GWLogger::log("addpage: $comp_name");}
	
	my $method_name = "populate_" . $comp_name;
	if($method_name =~ /package/){ next;}
	
	eval{ 
	 	my $newtab = $notebook->add_page($comp_name);
	 #	$tabCollection->add($newtab);
		 
  $self->$method_name($newtab,$component);
	};
	GWInstaller::AL::GWLogger::log("method missing: $method_name: $!") if $@; 	
 }
 
 
 
#GWInstaller::AL::GWLogger::log("Array of objects: $aoc");
# foreach $compPage (@arrayofobjs){
# 	
# 	$pagenomen  = $compPage;
#	$pagenomen =~ s/groundwork-nms-//;
#	if($pagenomen eq "core"){next;}
#	#	GWInstaller::AL::GWLogger::log("Adding page for: " . $pagenomen);
#
#	$formatted_name =$pagenomen;	 
#	 if($pagenomen eq "cacti"){
#	 	$formatted_name = "Cacti";
#	 }
#	 elsif($pagenomen eq "nedi"){
#	 	$formatted_name = "NeDi";
#	 }
#	 elsif($pagenomen eq "weathermap"){
#	 	$formatted_name = "Weathermap"
#	 }
#	 my $myPage =   $notebook->add_page($formatted_name);
#	 $self->populate_config_page($myPage,$formatted_name);
#	
# 	  $nbCollection{$pagenomen} = $myPage;
# }
 
  $nbCollection{$nb_hostname}->add(
    	undef,'Label',
    	-text=>'Component Configuration -- Host: ' . $nb_hostname,
    	-width=>$hostWin->width,
     	-textalignment=>'middle',
    	-y => 0,);
    	
	$nbCollection{$nb_hostname}->focus();	
}
sub add_notebook{
	$self = shift;
	$nb_hostname = $self->{hostname};
	my $nui = $self->{nmscui};
	my $installer = $nui->{installer};	
	$cui = $self->{cui};
 	$hostWin = $installer->{win};	
	$nbCollection{$nb_hostname}  = $self->{win};

  
# Create notebook and a couple of pages.
my $notebook = $self->{win}->add(
    undef, 'Notebook',
    -y=>1,
    -height => $hostWin->height -2,
    -width => $hostWin->width  -1,
);

  $self->{notebook} = $notebook;

}

sub populate_host_page{
 my $self = shift;
 my $notebook = $self->{notebook};
 my $nb_hostname = $self->{hostname};
 my $properties = $self->{properties};
 my  $hostPage = $notebook->add_page($nb_hostname);
 my $nmscui = $self->{nmscui};
 my $installer = $nmscui->{installer};
 my 	$cui = $self->{cui};
 
 my $msg = "\n\nThis is a placeholder for lots of explanatory text.This is a placeholder for lots of explanatory text.This is a placeholder for lots of explanatory text.This is a placeholder for lots of explanatory text.This is a placeholder for lots of explanatory text.This is a placeholder for lots of explanatory text.This is a placeholder for lots of explanatory text.\n\n";
 $msg .= "This is a placeholder for lots of explanatory text. This is a placeholder for lots of explanatory text.";
    my $textviewer = $hostPage->add( 
        undef, 'TextEditor',
    	-text => $msg,
    	-wrapping =>1,
    	-width=>120,
    	-readonly=>1
    );
  
 
 # modify save button label if master
   $savelabel;
  if($properties->get_master() eq $self->{hostname}){ 
  	$savelabel = "< Save & Continue... >";
  }
  else{
  	$savelabel = "< Save >";
  }
    
  
 $navButtons = 	$hostPage->add('navButtons', 'Buttonbox',
        -x=>55,-y=>19,-buttons   => [
  			{ -label => '< Back... >',
        -value => 'back',
        -shortcut => 'b',
        -onpress => sub {  
				$self->{win}->loose_focus();      		
				$self->{win}->hide(); 		
        		$self->{parent_win}->modalfocus(); 
        		$self->{parent_win}->{mainbuttons}->focus();
			    
         	}#endsub
            },
            { -label => $savelabel,
        -value => 'save',
        -shortcut => 's',
        -onpress => sub {   
        		GWInstaller::AL::GWLogger::log("enter sub");
        	
        		#get host collection
        		my $hostCollection = $properties->get_collection("host");
        		#get host object
        		my $hostobj = $hostCollection->get_by_identifier($nb_hostname);
        		GWInstaller::AL::GWLogger::log("copy values for $nb_hostname ... ref:" . ref $hostobj);
        		
        		#copy UI values to components
        		$hostobj->set_properties($properties);
        		GWInstaller::AL::GWLogger::log("properties set for ref:" . ref $properties);
        		
				$hostobj->save_values();        	
				 GWInstaller::AL::GWLogger::log("lose focus");
					
				$self->{win}->loose_focus();     
				$self->hide(); 	

				#lose focus on parent
				$self->{parent_win}->loose_focus();
				$self->{parent_win}->hide();
				
				# NEXT WINDOW TO FOCUS DEPENDS ON IF MASTER/NOT
				GWInstaller::AL::GWLogger::log("parent focus lost");
				#if master, show dialog offering close or execute
			eval{ 
				if($properties->get_master() eq $self->{hostname}){ 
				GWInstaller::AL::GWLogger::log($self->{hostname} . "is master");
	
 					$do_changes = $nmscui->yes_or_no_dialog("Execute changes?","Would you like to execute any changes you have made to the configuration of this host?");
					if($do_changes){
	       				GWInstaller::UI::GWCursesUI::status_msg("Scanning for Work to Perform...",$cui); 
	       				$properties->write_properties();
		       			$installer->install_components($self->{hostname});
					}#end do changes
				}		
				else{ 
					#focus main window
					my $nmscui = $self->{nmscui};
					my $installer = $nmscui->{installer};
					$installer->{win}->modalfocus();
					$nmscui->{main_buttons}->focus();
				}
			};
			if($@){				GWInstaller::AL::GWLogger::log("SOMETIHNG WENT REALLY WRONG: $@");}
				$properties->write_properties();

         	}#endsub
            }#end save button

            ]);
           
	$hostPage->draw();
	$navButtons->draw();
$navButtons->focus();

}




 

sub populate_gwm{
	($self,$comptab) = @_;
 
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
 
	my ($self,$comptab,$cacti_obj) = @_;
	
 	my $properties = $self->{properties};
 	my $dbname = $cacti_obj->get_database_name();
	my $dbusername = $cacti_obj->get_database_user();
	my $dbpassword = $cacti_obj->get_database_password();
 	my $cacti_id = $cacti_obj->get_identifier();
 	my $selected_gwm = $cacti_obj->get_GWM_host();
	
	
############################
 $comptab->add(undef,'Label',
 		-y => 1,
 		-x => 2,
 		-text => 'GW Monitor Server:',
 		-bold => 1);

my $gwm_collection = $properties->get_collection("gwm");
my $cacti_gwm_dropdown = GWInstaller::UI::DropdownMenu->new($self,$comptab,$gwm_collection,20,1,$selected_gwm);
$cacti_gwm_dropdown->set_identifier("nedi_gwm_dropdown");
$cacti_gwm_dropdown->draw();
 
# MySQL Server
###############
 $comptab->add(undef,'Label',
 		-y => 4,
 		-x => 4,
 		-text => 'MySQL Server:',
 		-bold => 1);

  	my $selected_database = $cacti_obj->get_database();
 	 

	my $database_collection = $properties->get_collection("database");
	my $cacti_database_dropdown = GWInstaller::UI::DropdownMenu->new($self,$comptab,$database_collection,20,4,$selected_database);
	$cacti_database_dropdown->set_identifier("cacti_database_dropdown");
	$cacti_database_dropdown->draw();
 		
# DB Name
#############
 $comptab->add(undef,'Label',
 		-y => 7,
 		-x => 4,
 		-text => 'Database Name:',
 		-bold => 1);

$dbname_textentry = $comptab->add(undef, 'TextEntry',
			 	-x => 20,
			 	-y => 6,
			 	-width=>15,
			 	-border=>1,
			 	-text=>$dbname
 			 	);	
 		
# DB USER 
########
 $comptab->add(undef,'Label',
 		-y => 10,
 		-x => 2,
 		-text => 'Database username:',
 		-bold => 1);
 		
$dbuser_textentry =  $comptab->add(undef, 'TextEntry',
			 	-x => 20,
			 	-y => 9,
			 	-width=>15,
			 	-border=>1,
			 	-text=>$dbusername
			 	);	
			 	
			 	
# DB PASSWORD
##############
 $comptab->add(undef,'Label',
 		-y => 13,
 		-x => 2,
 		-text => 'Database password:',
 		-bold => 1);
 		
$dbpassword_textentry =  $comptab->add(undef, 'PasswordEntry',
			 	-x => 20,
			 	-y => 12,
			 	-width=>15,
			 	-border=>1,
			 	-text=>$dbpassword
			 	);	
			 	
			 	
			 	
#add items to cactiUICollection
my $UI_collection = GWInstaller::AL::Collection->new();
$UI_collection->add($cacti_gwm_dropdown);
$UI_collection->add($cacti_database_dropdown);
$UI_collection->add($dbname_textentry);
$UI_collection->add($dbuser_textentry);
$UI_collection->add($dbpassword_textentry);

$cacti_obj->{UICollection} = $UI_collection;
	
}

sub populate_nedi{
	my ($self,$comptab,$nedi_obj) = @_;
	
 	my $dbname = $nedi_obj->get_database_name();
	my $dbusername = $nedi_obj->get_database_user();
	my $dbpassword = $nedi_obj->get_database_password();
	my $properties = $self->{properties};
 	my $nedi_id = $nedi_obj->get_identifier();
 	my $selected_gwm = $nedi_obj->get_GWM_host();
 	
 GWInstaller::AL::GWLogger::log("Nedi vals: $nedi_id ; $selected_gwm");
	
	# GroundWork Monitor Server
############################
 $comptab->add(undef,'Label',
 		-y => 1,
 		-x => 2,
 		-text => 'GW Monitor Server:',
 		-bold => 1);
 		
 		


my $gwm_collection = $properties->get_collection("gwm");
my $nedi_gwm_dropdown = GWInstaller::UI::DropdownMenu->new($self,$comptab,$gwm_collection,20,1,$selected_gwm);
$nedi_gwm_dropdown->set_identifier("nedi_gwm_dropdown");
$nedi_gwm_dropdown->draw();

 
# MySQL Server
###############
 $comptab->add(undef,'Label',
 		-y => 4,
 		-x => 4,
 		-text => 'MySQL Server:',
 		-bold => 1);
 		
 	my $selected_database = $nedi_obj->get_database();
 	 

	my $database_collection = $properties->get_collection("database");
	my $nedi_database_dropdown = GWInstaller::UI::DropdownMenu->new($self,$comptab,$database_collection,20,4,$selected_database);
	$nedi_database_dropdown->set_identifier("nedi_database_dropdown");
	$nedi_database_dropdown->draw();
 		

# DB Name
#############
 $comptab->add(undef,'Label',
 		-y => 7,
 		-x => 4,
 		-text => 'Database Name:',
 		-bold => 1);

$dbname_textentry = $comptab->add(undef, 'TextEntry',
			 	-x => 20,
			 	-y => 6,
			 	-width=>15,
			 	-border=>1,
			 	-text=>$dbname
 			 	);	
 		
# DB USER 
########
 $comptab->add(undef,'Label',
 		-y => 10,
 		-x => 2,
 		-text => 'Database username:',
 		-bold => 1);
 		
$dbuser_textentry =  $comptab->add(undef, 'TextEntry',
			 	-x => 20,
			 	-y => 9,
			 	-width=>15,
			 	-border=>1,
			 	-text=>$dbusername
			 	);	
			 	
			 	
# DB PASSWORD
##############
 $comptab->add(undef,'Label',
 		-y => 13,
 		-x => 2,
 		-text => 'Database password:',
 		-bold => 1);
 		
 $dbpassword_textentry = $comptab->add(undef, 'PasswordEntry',
			 	-x => 20,
			 	-y => 12,
			 	-width=>15,
			 	-border=>1,
			 	-text=>$dbpassword
			 	);	
#add items to nediUICollection
my $UI_collection = GWInstaller::AL::Collection->new();

$UI_collection->add($nedi_gwm_dropdown);
$UI_collection->add($nedi_database_dropdown);
$UI_collection->add($dbname_textentry);
$UI_collection->add($dbuser_textentry);
$UI_collection->add($dbpassword_textentry);
$nedi_obj->{UICollection} = $UI_collection;			 	
	

}

sub populate_weathermap{
	my ($self,$comptab,$weathermap_obj) = @_;
	my $weatherhost = $weathermap_obj->get_host(); #same as gwm
 	my $properties = $self->{properties}; 

# GroundWork Monitor Server
############################
 $comptab->add(undef,'Label',
 		-y => 1,
 		-x => 2,
 		-text => 'GW Monitor Server:',
 		-bold => 1);


 my $gwm_collection = $properties->get_collection("gwm");
my $wm_gwm_dropdown = GWInstaller::UI::DropdownMenu->new($self,$comptab,$gwm_collection,20,1,$weatherhost);
$wm_gwm_dropdown->set_identifier("wm_gwm_dropdown");
$wm_gwm_dropdown->draw();
 	 
 
 #add items to nediUICollection
my $UI_collection = GWInstaller::AL::Collection->new();
$UI_collection->add($wm_gwm_dropdown); 

$weathermap_obj->{UICollection} = $UI_collection;			 
			 	
	

}

sub populate_foundation{
	my ($self,$comptab,$foundation_obj) = @_;
	
 	my $port = $foundation_obj->get_port();

# Foundation Port
##################
 $comptab->add(undef,'Label',
 		-y => 6,
 		-x => 4,
 		-text => 'Foundation Port:',
 		-bold => 1);

 $port_textentry = $comptab->add(undef, 'TextEntry',
			 	-x => 20,
			 	-y => 5,
			 	-width=>10,
			 	-border=>1,
			 	-text=>$port
			 	);		
			 	
#add items to nediUICollection
my $UI_collection = GWInstaller::AL::Collection->new();
$UI_collection->add($port_textentry);

$foundation_obj->{UICollection} = $UI_collection;	
}


sub populate_ntop{
	my ($self,$comptab,$ntop_obj) = @_;
	my $properties = $self->{properties};
 	my $port = $ntop_obj->get_port();
 	my $ntop_id = $ntop_obj->get_identifier();
 	my $selected_gwm = $ntop_obj->get_identifier();

    
    
# GroundWork Monitor Server
############################
 $comptab->add(undef,'Label',
 		-y => 1,
 		-x => 2,
 		-text => 'GW Monitor Server:',
 		-bold => 1);

my $gwm_collection = $properties->get_collection("gwm");
my $ntop_gwm_dropdown = GWInstaller::UI::DropdownMenu->new($self,$comptab,$gwm_collection,20,1,$selected_gwm);
$ntop_gwm_dropdown->set_identifier("ntop_gwm_dropdown");
$ntop_gwm_dropdown->draw();
 	 
# Port
################
 $comptab->add(undef,'Label',
 		-y => 6,
 		-x => 2,
 		-text => 'ntop Port:',
 		-bold => 1);


my $port_textentry =  $comptab->add(undef, 'TextEntry',
			 	-x => 20,
			 	-y => 5,
			 	-width=>10,
			 	-border=>1,
			 	-text=>$port
			 	);	
 	 
			 	
	
#add items to nediUICollection
my $UI_collection = GWInstaller::AL::Collection->new();
$UI_collection->add($ntop_gwm_dropdown);
$UI_collection->add($port_textentry);

$ntop_obj->{UICollection} = $UI_collection;			
	
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
