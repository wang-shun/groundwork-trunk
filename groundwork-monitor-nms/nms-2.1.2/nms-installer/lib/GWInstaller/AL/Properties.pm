#/usr/bin/perl
#
#Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms.
#

package GWInstaller::AL::Properties; 
use lib qw("./lib");
use GWInstaller::AL::GWLogger;

my $propertyFile = "";
my %prophash = ();       ## Hash to store properties file data
my @propseqarray = ();      ## Array to maintian sequence of Propertynames

sub new{
	
    my ($invocant,$propFile) = @_;
    
    unless($propFile){$propFile = "system.properties";}
    my $class = ref($invocant) || $invocant;
	$err = "";
	%properties = ();
	$propertiesRef = \%properties;
    my $self = {
    	propertyFile => $propFile,
    	properties => $propertiesRef
    };
  	bless ($self,$class);
  	
    if ( -e $propFile){
        GWInstaller::AL::GWLogger::log("Reading $self->{propertyFile}");
     	$ret = $self->read_properties();
      	unless($ret){$err = "Error reading properties.:$ret";}
    }
    else{
         ## Create file
         $err = "Configuration file is missing.";
        # GWInstaller::AL::GWLogger::log("$err");
         #$cmd = "touch $propFile 2>&1";
         #$err = `$cmd`;
         
    }
 
if($err){ 
	GWInstaller::AL::GWLogger::log("No configuration file found. Treating this as a fresh install."); 
	}
    return $self;
}


# Does the file exist?
#######################
sub exists{
	$self = shift;

	$valid = 0;
	$pFile = $self->{propertyFile};
	if( -e $pFile){
		$valid = 1;
	}
	return $valid;
}

# Get value for a particular property name
# Arguments passed: Property Name
# Returns: Property Value

sub get_property{
    
   my ($self,$propertyName) = @_;
   $pRef = $self->{properties};
	$props = %$pRef;
   return $props->{$propertyName};
  
}

# Set value for a particular property name
# Arguments passed: Property Name, Property Value
# Returns: 1 on success and 0 on failure 

sub set_property{
  $retVal = 0;
  
   my($self,$propertyName, $propertyValue) = @_;

   	$propRef = $self->{'properties'};
   	$props = %$propRef;
   	$props->{$propertyName} = $propertyValue;
  	$retVal = 1;   
    return $retVal;
}


# Reads enterprise.properties file and stores data in a hash
# Arguments passed : Nothing
# Returns: Nothing

sub read_properties{
   $retVal = 0;
   $self = shift;
   open (PROPFILE, $self->{propertyFile}) || die "Couldnt open property file: $self->{propertyFile}"; # Open properties file

   while(<PROPFILE>) {
       $line = $_;

       chomp($line);
       if ($line =~ /^#/ || $line =~ /^(\s)*$/){   # ignore comments and blank lines
       	   next;
       }	

       my ($propertyname, $propertyvalue) = split(/=/,$line);
       #print "GWInstaller::AL::GWLogger::log("$propertyname = $propertyvalue");
       push(@propseqarray, $propertyname);		
       $prophash{ $propertyname } = $propertyvalue;    # add property name, value to hash

   } # End of while

   close(PROPFILE);
   $retVal = 1;
   return $retVal;
}


# Writes data in hash to  enterprise.properties file
# Arguments passed: Nothing
# Returns: Nothing

sub write_properties{
   $self = shift;
 if(-e $self->{propertyFile}){
 	
    $propRef = $self->{properties};
    $properties = %$propRef;
     
   open(PROPFILE, ">$self->{propertyFile}") or  die "write_properties(): Couldn't write to file:" . $self->{propertyFile} . ":$!"; # Open properties file
	
	
   for my $propertyname (keys %properties){    
   
       print PROPFILE $propertyname ."=". $properties->{$propertyname} ."\n";
      
   }
   close(PROPFILE);
}
else{
	 print "FNF: $self->{propertyFile}\n";
	return 0;
}
}


# Generates JDBC URL and adds propety to enterprise.peoperties file
# Arguments passed: Property name, Databse driver name, Hostname, Database name
# Returns: Nothing

sub set_JDBCURL{
	$retVal = 0;
   	my ($self,$propertyName, $dbDriverName, $hostName, $dbName) = @_;

   	## Generate JDBC URL
   	$jdbcURL = "jdbc:". $dbDriverName ."://". $hostName ."/". $dbName;

 	$propRef = $self->{properties};
 	$properties = %$propRef;
   	$properties->{$propertyName} = $jdbcURL;
   	$self->{properties} = \%properties;
   	
   	if($jdbcURL){$retVal=1;}
   
   	return $retVal;
}


# Update an existing property in enterprise.properties file
# Aeguments passed: Old property name, New property name
# Returns: 1 on success and 0 on failure

sub update_property{
 	$retVal = 0;
   my ($self,$oldPropertyName, $newPropertyName) = @_;
   $propRef = $self->{properties};
   $properties = %$propRef;
   if(exists $properties->{$oldPropertyName} ){ 
	   $propVal = $properties->{$oldPropertyName};
	   $properties->{$newPropertyName} = $propVal;
	   delete $properties{$oldPropertyName};
	   $self->{properties} = \%properties;
   		$retVal = 1;
   }
   
   return $retVal;
}

1;

