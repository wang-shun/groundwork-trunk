#!/usr/bin/perl
package GWNMSInstaller::UI::HostListBox;

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
		listbox=>$listbox
	};

	bless($self,$class);
 	$self->init();
 	
	return $self;		
}
sub init{
	
	my $self = shift;	
 
	$self->{nmscui} = $parent->{nmscui};
	$self->{cui} = $self->{nmscui}->{cui};
	
	my $cui = $self->{cui};
	my $nmscui = $self->{nmscui};
	 
	my $hostWin = $self->{win};
	
	
		my $nui = $self->{nmscui};
	my $installer = $nui->{installer};
	$self->{cui} = $installer->{cui};

	$self->add_listbox();
	
}

sub add_listbox{ 
	$self = shift;
	
	my $listbox = $hostWin->add(
        'mylistbox', 'Listbox',
        -radio     => 0,
        -x =>6,
        -y => 4,
        -width =>60,
        -onchange=> sub { 
        	
        	$listbox->labels({}); #clear listbox
 			$listbox->draw();
 			@toggled_components = ();
 			$toggleditem = $listbox->get();
 			($item,$status) = split(/\.+/,$toggleditem);
 	
 			if($item eq "groundwork-nms-core" && $status eq "INSTALLED"){
 				$confirm = $nmscui->yes_or_no_dialog("WARNING!!","All NMS packages are dependent on groundwork-nms-core.\nRemoving this package will cause all others to be removed\nfrom this system");
 				if($confirm){
 					@components = $self->set_listbox_uninstall(@components);#set all to to be uninstalled
 					$self->{corelast} = 1;
 				}
				else{
					$listbox->clear_selection();
					return;
				}
 			}
# 			else{
#				if(core) is not installed or to be installed
# 				$confirm = $self->yes_or_no_dialog("Notice! This package reqr")
# 			}
 			
 			$toggleditem = $self->toggle_status($toggleditem);
 			
 			
 		 
 			
			foreach $cp (@components){
				($item,$status) = split(/\.+/,$cp);
				($newitem,$newstatus) = split(/\.+/,$toggleditem);
				if($item eq $newitem){
					push(@toggled_components,$toggleditem);
				}
				else{
					push(@toggled_components,$cp);
				}
			}#end foreach
			@components = @toggled_components;
			$listbox->insert_at(1,\@toggled_components);
        },
        -border=>1,
        -height=>6
        
    );	
    
 
    $listbox->insert_at(1,\@components);
    
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
sub populate_host_listbox{
	my ($self,$hostlistbox,$collection) = @_;
	
	foreach $host ($collection){
		
		$hostlistbox->insert_at(1,$host->{hostname});
	}
	
	
}
1;
