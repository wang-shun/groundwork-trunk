#!/usr/bin/perl
package GWNMSInstaller::UI::gwmEditDialog;
	     
 

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
		 $nui->info_dialog("You can't edit this type of resource");
		 return;
	 }

 	$self->add_host_dialog();	 
  
}

sub add_host_dialog{
	my $self = shift;
	my $cui = $self->{cui};
	my $nmscui = $self->{nmscui};
	my $properties = $self->{properties};
	my $hostlistbox = $self->{parent}->{listbox};
	
	  my $newhostname = $cui->question(-question => 'Please enter the hostname or IP address:');
        	   	if($newhostname eq ""){ return;}
 			
  			my $newhostObj = GWInstaller::AL::GWMonitor->new();
  			$newhostObj->set_host($newhostname);
  			$newhostObj->set_identifier($newhostname);
  			$newhostObj->set_port(80); #always 80
  			unless($newhostObj->is_valid()){
  				$nmscui->error_dialog("\"${newhostname}\" is not a valid hostname or IP address; Please verify and try again.");
  				return;
  			}
  			
  			#check for duplicates
  			my $gwmCollection = $properties->get_collection("gwm");
  			my $checkHostObj = $gwmCollection->get_by_identifier($newhostname);
  			if($checkHostObj){
  				$nmscui->error_dialog("This host already exists in your configuration");
  				return;
  			}
  			
			GWInstaller::AL::GWLogger::log("Hostname to add: " . $newhostname);

			$hostlistbox->insert_at(1,$newhostname);
   			$gwmCollection->add($newhostObj);
	
			$hostlistbox->draw();
	
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
	return;
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
