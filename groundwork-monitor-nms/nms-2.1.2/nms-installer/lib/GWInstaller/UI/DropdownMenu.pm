#!/usr/bin/perl

package GWInstaller::UI::DropdownMenu;

sub new{
	 
 	my ($invocant,$parent_obj,$env,$collection,$xval,$yval,$selected) = @_;
	$cui = $installer->{cui};
	my $class = ref($invocant) || $invocant; 
	$cui = $parent_obj->{cui};
	$nmscui = $parent_obj->{nmscui};
	$installer = $nmscui->{installer};
	$win = $parent_obj->{win};
	$debug=0;
	if($debug){ 
	GWInstaller::AL::GWLogger::log("DropdownMenu Class");	
	GWInstaller::AL::GWLogger::log("nmscui:" . ref $nmscui);
	
	GWInstaller::AL::GWLogger::log("PRE Collection class:" . ref $collection);
	GWInstaller::AL::GWLogger::log("PRE Collection size: $collection->{array_size}");
	GWInstaller::AL::GWLogger::log("parent class" .  ref $parent_obj);
	GWInstaller::AL::GWLogger::log("env class" . ref $env);
	GWInstaller::AL::GWLogger::log("xval: $xval");
	GWInstaller::AL::GWLogger::log("yval: $yval");
	}
		
			
 
	my $self =  {
		installer => $installer,
		cui => $cui,
		parent_obj=>$parent_obj,
		collection=>$collection,
		win=>$win,
		env=>$env,
		popupbox=>$popupbox,
		value_array=>\@value_array,
		label_hash=>\%label_hash,
		xval=>$xval,
		yval=>$yval,
		selected=>$selected,
		identifier=>$identifier,
		nmscui=>$nmscui
	};
	
 
	
	bless($self,$class);

	$self->init();
	
	return $self;		
}

sub set_identifier{
	my($self,$ident) = @_;
	$self->{identifier} = $ident;
}

sub get_identifier{
	my $self = shift;
	return $self->{identifier};	
}

sub refresh {
	my $self = shift;
	my $collect = $self->{collection};
	my %label_hash;
	my @myvals;
	$debug =1;
	while($collect->has_next()){
		$obj = $collect->next();
		my $id = $obj->get_identifier();
		if($debug){GWInstaller::AL::GWLogger::log("refresh(): Adding $id to hash and array");}
		$label_hash->{$id} = $id;
		push(@myvals,$id);
	}
	my $installer = $self->{installer};
	
	if($installer->verteilt_ist_aktiviert()){ 
		$label_hash->{"edit"} = "Edit this Menu...";
		push(@myvals,"Edit this Menu...");
	}
	$arraysize = @myvals;

	#set selected?
	for($i=0;$i<$arraysize;$i++){
		if($myvals[$i] eq $selected){
			$selected =$i;
			last;
		}
	}	

    
	$self->{value_array} = \@myvals;
	$self->{label_hash} = \%label_hash;

eval{ 
	$self->{popupbox}->values(\@myvals);
};
if($@){GWInstaller::AL::GWLogger::log("ERROR: $@");}
	$self->{popupbox}->draw();
	
}

sub init{
	my $self = shift;
    my $cui = $self->{cui};
	my $env = $self->{env};
	my $xval = $self->{xval};
	my $yval = $self->{yval};
	my $selected = $self->{selected};
	
	my $collect = $self->{collection};
	my %label_hash;
	my @myvals;
	while($collect->has_next()){
		$obj = $collect->next();
		my $id = $obj->get_identifier();
		if($debug){GWInstaller::AL::GWLogger::log("Adding $id to hash and array");}
		$label_hash->{$id} = $id;
		push(@myvals,$id);
	}
	if($installer->verteilt_ist_aktiviert()){ 		
		$label_hash->{"edit"} = "Edit this Menu...";
		push(@myvals,"Edit this Menu...");
	}
	$arraysize = @myvals;
	for($i=0;$i<$arraysize;$i++){
		if($myvals[$i] eq $selected){
			$selected =$i;
			last;
		}
	}
#		GWInstaller::AL::GWLogger::log("Selecting $selected");	
     $self->{popupbox} = $env->add(
        undef, 'Popupmenu',
        -values    => \@myvals,
        -labels    => \%label_hash,
        -x=>$xval,
        -y=>$yval,
        -onchange=>sub{
        		my $newval = $self->{popupbox}->get();
				
        		if($newval eq "Edit this Menu..."){ 
        			eval{  
        			GWInstaller::AL::GWLogger::log("edit menu ". $self->get_identifier());
	        		 $self->make_new($self->get_identifier());
	        		 $self->refresh($self->{collection});
        			};
        			if($@){GWInstaller::AL::GWLogger::log("ERROR: $@");}
        		}
        		else{
           			GWInstaller::AL::GWLogger::log("other");        			
        		}
 
        },
        -selected=>$selected
    );
    
	$self->{value_array} = \@myvals;
	$self->{label_hash} = \%label_hash;
	
     $self->{popupbox}->focus();
}

sub make_new{
	my ($self,$type) = @_;
	my $nmscui = $self->{nmscui};
	my $parent_win = $self->{parent_obj};
	my $hostname = $parent_win->{hostname};
	if($type =~ /database/){$type = "database";}
	elsif($type =~ /gwm/){$type = "gwm";}
	my $dialog = GWNMSInstaller::UI::DropdownConfigDialog->new($self,$type);
}
sub set_selected{
	my($self,$itemname) = @_;
	my $popupbox = $self->{popupbox};
	my $array_ref = $self->{value_array};
	my @myvals = @$array_ref;
	my $selected = $self->{selected};
	
	$arraysize = @myvals;
	for($i=0;$i<$arraysize;$i++){
		if($myvals[$i] eq $selected){
			$selected =$i;
			last;
		}
	}
	
	$popupbox->{selected} = $selected;
	$popupbox->draw();	
	
	
}

sub focus{
	my $self = shift;
	my $popupbox = $self->{popupbox};
	$popupbox->focus();
}

sub get{
	my $self = shift;
	my $popupbox = $self->{popupbox};
	return $popupbox->get();
}

sub add{
	my ($self,$newvalue) = @_;
	
	my $label_hash_ref = $self->{label_hash};
	my %label_hash = %$label_hash_ref;

	my $value_array_ref = $self->{value_array};
	my @value_array = @$value_array_ref;
	
	$label_hash->{$newvalue} = $newvalue;
	push(@value_array,$newvalue);
	
	$self->{label_hash} = \$label_hash;
	$self->{value_array} = \@value_array;
	
}

sub delete{
	my ($self,$delvalue) = @_;
	
	my $label_hash_ref = $self->{label_hash};
	my %label_hash = %$label_hash_ref;

	my $value_array_ref = $self->{value_array};
	my @value_array = @$value_array_ref;
	
	$label_hash->{$newvalue} = $newvalue;
	push(@value_array,$newvalue);
	
	$self->{label_hash} = \$label_hash;
	$self->{value_array} = \@value_array;
	
}

sub draw{
	my $self = shift;
	my $popupbox = $self->{popupbox};
	$popupbox->draw();
}

1;
