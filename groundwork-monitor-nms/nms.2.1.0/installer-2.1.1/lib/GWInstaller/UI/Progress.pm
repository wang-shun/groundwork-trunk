#!/usr/bin/perl
package GWInstaller::UI::Progress;



sub new{
	 
 	my ($invocant,$installer,$max_val,$total_steps) = @_;
	$cui = $installer->{cui};
	my $class = ref($invocant) || $invocant; 
	my $self =  {
		max_val => $max_val,
		installer => $installer,
		cui => $cui,
		total_steps => $total_steps,
		step =>	$step
	};
	
	bless($self,$class);

	$self->init();
	
	return $self;		
}


sub init{
		$self = shift;
 		$self->{step} = 0;
		unless($self->{total_steps}){
			$self->{total_steps} = 100;
		}
 		
		my $cui = $self->{cui};
		
		$installer = $self->{installer};
 
		$cui = $installer->{cui};
	 
	   	$cui->progress(-max => $self->{max_val});
	   	
#	   	###
#	   	$win = $installer->{win};
#	   	 my $dialog = $win->add(
#        'undef', 'Dialog::Progress',
#    	-max       => $self->{max_val}
#    );
	   	
 }

sub reset{
	$self = shift;
	$self->{step} = 0;
}

sub set_total_steps{
	$self = shift;
	$self->{total_steps} = shift;
}

sub hide{
	$self = shift;
	my $cui = $self->{cui};
	$cui->noprogress();
}

sub increment{
	($self,$msg) = @_;
	$cui = $self->{cui};
	$installer = $self->{installer};
	$nmslog = $installer->{nmslog};
	$nmslog->log($msg);
	$eachstep = $self->{max_val}/$self->{total_steps};
	$newval = $self->{step} + $eachstep;
	for($i = $self->{step};$i<=$newval;$i++){
			$cui->setprogress($i,$msg);		
	}
	$self->{step} = $newval;
}

1;