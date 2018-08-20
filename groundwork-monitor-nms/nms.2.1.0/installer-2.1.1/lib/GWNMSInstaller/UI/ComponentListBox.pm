#!/usr/bin/perl
package GWNMSInstaller::UI::ComponentListBox;

sub new{
	 
	my ($invocant,$parent) = @_;
	my $class = ref($invocant) || $invocant;
	 
   	$pRefr = {};
	my $self = {
		cui=>$cui,
		win=>$win,
		nmscui=>$nmscui,
		listbox=>$listbox,
		parent=>$parent,
		listbox=>$listbox,
		host=>$host,
		properties=>$properties
	};

	bless($self,$class);
  	 $self->init();
 	
	return $self;		
}


sub init{
	
	my $self = shift;	
 
	$self->{nmscui} = $self->{parent}->{nmscui};
	

	my $nmscui = $self->{nmscui};
if($debug){GWInstaller::AL::GWLogger::log("get properties");}

	my $installer = $nmscui->{installer};
	$self->{properties} = $installer->get_properties();
    my $properties = $self->{properties};
	$self->{cui} = $installer->{cui};
	my $cui = $self->{cui};
	my $parent = $self->{parent};
	$self->{win} = $parent->{win};
	my $hostWin = $self->{win};

if($debug){ GWInstaller::AL::GWLogger::log("get collection");}
	
	my $hostCollection = $properties->get_collection("host");
	my $hostname = $parent->{hostname};
if($debug){ GWInstaller::AL::GWLogger::log("get host by id");}
	
	my $host = 	$hostCollection->get_by_identifier($hostname);
 
	
 if($debug){ GWInstaller::AL::GWLogger::log("add listbox");}
 
	$self->add_listbox();
if($debug){ 	 GWInstaller::AL::GWLogger::log("populate");}
	

	 $self->populate();
}

sub on_change{
	my($self) = shift;
	my $nmscui = $self->{nmscui};
	my $listbox = $self->{listbox};
	my $properties = $self->{properties};
	my $parent = $self->{parent};
	my $hostname = $parent->{hostname};
		
 if($debug){ GWInstaller::AL::GWLogger::log("on_change()");	}

	# get toggled item
	my $toggleditem = $listbox->get();
   if($debug){GWInstaller::AL::GWLogger::log("on_change(): get listbox item: $toggleditem");}

	my($item,$statustxt) = split(/\.+/,$toggleditem);
   if($debug){GWInstaller::AL::GWLogger::log("on_change(): get item: $item");}
 
	
   if($debug){GWInstaller::AL::GWLogger::log("get collection for ");}

	# change status #
	
	#get the host collection
	my $collection = $properties->get_collection("host");
	
	#get the correct host object
	my $host = $collection->get_by_identifier($hostname);

	#get the install collection
	my $installCollection = $host->get_install_collection($properties);


	while($installCollection->has_next()){
		$component = $installCollection->next();
		
		#when you find the toggled item, swap its value
		if($component->get_identifier() eq $item){
			 if($debug){  GWInstaller::AL::GWLogger::log("onchange(): toggle $item");}
			
			  if($debug){ GWInstaller::AL::GWLogger::log("$item value:" . $component->get_do_install());}
			$methname = "set_" . $item;
			$unique = $hostname; # `date`;
			$unique =~ s/\s+//g; #zap whitespace
		 	$unique =~ s/\./_/g;
			chomp($unique);

			#get the component collection for this type
			my $cColl = $properties->get_collection($item);
			
			
			if($component->get_do_install() == 1){
				my $confirm = $nmscui->yes_or_no_dialog("Confirm","All SETTINGS for this component (" . $component->get_identifier() . ") will be DELETED.\nAre you sure you want to remove this component?");
				unless($confirm){ return;}				
				$component->set_do_install(0);
				#set identifier in host to blank
				$host->$methname("");

				#remove object with that identifier from component collection
				$cColl->remove_by_identifier($ident);				
			}
			
			elsif($component->get_do_install() == 0){
				$component->set_do_install(1);
				
				#set identifier in host to new value
				my $newident = $item . "_" . $unique; 
				$host->$methname($newident);
				
				#add object using this identifier to the component collection
				my $newclass = $properties->get_classname($item);
				my $newobj = $newclass->new();
				$newobj->set_identifier($newident);
				$cColl->add($newobj);	
			}
			
			  if($debug){GWInstaller::AL::GWLogger::log("$item value:" . $component->get_do_install());}
				
		}		
	
	}	
	# populate
	$self->populate();
	$listbox->focus();
	$listbox->clear_selection();
}
	
sub add_listbox{ 
	$self = shift;
	my $hostWin = $self->{win};
	my @components = ();
	my $listbox;
	$listbox = $hostWin->add(
        undef, 'Listbox',
        -radio     => 0,
        -x =>6,
        -y => 4,
        -width =>60,
        -height=>10,
        -onchange=> sub { 
     		#$listbox->insert_at(1,\@components);
 			#$listbox->draw();
			$self->on_change();        	
			$listbox->clear_selection();

        }, #end onchange=>sub
        -border=>1,
        -height=>7
        
    );	
    
 
    $listbox->insert_at(1,\@components);
    
    $self->{listbox} = $listbox;
}

sub toggle_status {
	($self,$listitem) = @_;

	my $cui = $self->{cui};
	($item,$status) = split(/\.+/,$listitem);
	
	if($status eq "INSTALLED"){
		$status = "TO BE UNINSTALLED";
	}
	elsif($status eq "TO BE UNINSTALLED"){
		$status = "INSTALLED";
 		
	}
	elsif($status eq "NOT INSTALLED"){
		$status = "TO BE INSTALLED";
	}
	elsif($status eq "TO BE INSTALLED"){
		$status = "NOT INSTALLED";
	}

	$retstring = $self->format_listitem($item,35,$status);

	return $retstring;	
	
}

sub set_listbox_uninstall{
	my $self = shift;
	my @items = @_;
	@tempArray = ();
	foreach $thing (@items){
		my ($name,$status) = split(/\.+/,$thing);
		if($status eq "INSTALLED"){
			$status = "TO BE UNINSTALLED";
			}
		elsif($status eq "TO BE INSTALLED"){
			$status = "NOT INSTALLED";
			}
		$formattext = $self->format_listitem($name,35,$status);
		push(@tempArray,$formattext);
	}
	return @tempArray; 
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

sub populate{
	my ($self) = @_;
	my $properties = $self->{properties};
	my $listbox = $self->{listbox};
	my $parent = $self->{parent};
	my $hostname = $parent->{hostname};
	@components = ();
	#clear listbox
	my @blank = ();
    $listbox->values(\@blank);
    
	$listbox->draw();
	
	#get the host collection
	my $collection = $properties->get_collection("host");
	

	#grab the appropriate host object
	my $host = $collection->get_by_identifier($hostname);


	#get component collection for the host
	my $installCollection = $host->get_install_collection($properties);

	#set status according to object attribute (do_install)	
	while($installCollection->has_next()){
		$component = $installCollection->next();
		  if($component->get_do_install() == 1){ 
			$status = "INSTALL";
		  }
		  elsif($component->get_do_install() == 0){
		  	$status = "DON'T INSTALL";
		  }
		 
		 my $formattext =  $self->format_listitem($component->get_identifier,35,$status);
		 $listbox->insert_at(1,$formattext);
 
		}#end while
 
 $listbox->focus();
}

 
1;
