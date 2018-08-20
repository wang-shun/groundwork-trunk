#!/usr/bin/perl
package GWInstaller::AL::Collection;

#requires objects that have get_identifier() method

sub new{
	my ($invocant) = shift;
	my $class = ref($invocant) || $invocant;
	
	my @object_array = @_;
	my $array_size = 0;
	my $current_index = 0;
	
	my $class = ref($invocant) || $invocant;
	my $self = {
		current_index => $current_index,
 		object_array => \@object_array,
 		array_size => $array_size
	};
	
	 

	bless($self,$class);
	return $self;	
	
}


sub contains{
	my ($self,$obj) = @_;
	my $contains_object = 0; #assume it does not
	
	my $class = ref $obj;
	my $ident = $obj->get_identifier();
	
	while($self->has_next()){
		$tmpObj = $self->next();
		
		$tmpclass = ref $tmpObj;
		$tmpident = $tmpObj->get_identifier();
		
		#if identifier and class match, it does 
		if( ($tmpClass eq $class) && ($tmpident eq $ident) ){
			$contains_object = 1;
			last;
			}
		}#end while	
	
	return $contains_object;
		
}


sub has_next{
	$self = shift;
	$has = 0;
	#GWInstaller::AL::GWLogger::log("COLINDEX:" . $self->{current_index} . " OF " . $self->{array_size});
	$newindex = $self->{current_index} + 1;
	if($self->{current_index} < $self->{array_size}){
		$has = 1;
	}
	else{
		$self->reset_index();
	}	
	return $has;
}

sub next{
	$self = shift;
	
	my $obj = null;
 	my $arrayref = $self->{object_array};
 	my @objarray = @$arrayref;
 	my $newindex = $self->{current_index} + 1;
 	
 
 	
 	#retrieve object at that index
 	$obj = $objarray[$self->{current_index}];
 
 	#increment index by one if possible
 	if($newindex > $self->{array_size}){
		GWInstaller::AL::GWLogger::log("return null?");
 		#return null;
 	}
 	else{
 		$self->{current_index} = $newindex;
 	} 
 
	return $obj;
}

sub add{
	($self,$object) = @_;
	$ref = ref $object;
 	my $arrayref = $self->{object_array};
	my @array = @$arrayref;
	push(@array,$object);
	$self->{object_array} = \@array;
	$self->{array_size}++;
	
	if($debug){ eval{ 
		$id = $object->get_identifier();
		};
		GWInstaller::AL::GWLogger::log("+++Adding $ref of $id; ArraySize:" . $self->{array_size});
	}
	
}


sub clear{
	($self,$object) = @_;

	my @cleared_array = ();
	$self->{object_array} = \@cleared_array;
		
}

sub is_empty{
	$retval=0;
	$self = shift;
	my $arrayref = $self->{object_array};
	my @array = @$arrayref;
	
	$size = @array;
	if($size == 0){
		$retval=1;
	}
	return $retval;
}

sub remove_by_identifier{
	my($self,$identifier) = @_;
	my $arrayref = $self->{object_array};
	my @array = @$arrayref;
	$i = 0;

	foreach $obj (@array){
		
		if( ($obj->get_identifier()) eq $identifier ){
			splice(@array,$i,1);
			last;
			}
		$i++;
		}
	$self->{object_array} = \@array;
	$self->{array_size}--;
}

 

 

sub get_by_index{
	my($self,$index) = @_;
	
}

sub get_by_identifier{
	my($self,$identifier) = @_;
	my $arrayref = $self->{object_array};
	my @array = @$arrayref;
	my $obj = null;
	my $retval = 0;
	
	$mysize = @array;
	#GWInstaller::AL::GWLogger::log("SIZE:$mysize");
	unless($self->is_empty()){ 	
		foreach $obj (@array){
			$myref = ref $obj;
			#GWInstaller::AL::GWLogger::log("WHAT:$myref");
			if( ($obj->get_identifier()) eq $identifier ){
				$retval = $obj;
				last;
				}
			}
		}
		
	return $retval;;
}


sub set_by_identifier{
	my($self,$identifier,$newobj) = @_;
	$myref = ref $newobj;
	# GWInstaller::AL::GWLogger::log("set_by_identifier($identifier,$myref)");

	my $arrayref = $self->{object_array};
	my @array = @$arrayref;
	my $i = 0;	
	my $set = 0;
	
	my $myref = ref $newobj;
	unless($myref){
		GWInstaller::AL::GWLogger::log("set_by_identifier: Can't set an invalid object of type: $myref");
	}
	#if array is empty add this object
	if($self->is_empty()){
		GWInstaller::AL::GWLogger::log("Adding new object of type: $myref");
		$self->add($newobj);
		$set =1;
		$self->{array_size}++;
	}
	
	#array not empty
	else{ 
		#look for object with a matching identifier
		unless($set){ 
			foreach $obj (@array){
				if( ($obj->get_identifier()) eq $identifier ){
					$array[$i] = $newobj;
					$set = 1;
					last;
					}
				$i++;
				}
		}
	#if you still havent set the object, add it 
		unless($set){
			$self->add($newobj);
	 
		}	
	}
	$self->{object_array} = \@array;
	
	#debug
	# GWInstaller::AL::GWLogger::log("***SIZE:" . $self->{array_size});	
}

sub reset_index{
	$self = shift;
	$self->{current_index} = 0;
}

sub get_object_array{
	my $array_ref =  $self->{object_array};	
	my @array = @$array_ref;
	return @array;	
}


1;