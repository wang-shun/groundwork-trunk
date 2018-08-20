#!/usr/bin/perl
#
#Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

package GWInstaller::AL::InstallerPackage;


# returns a list of components already installed on the system (An Array of GWInstaller::AL::Software objects) 
sub get_installed_components{
	
}

# returns list of components available for install in the packages dir (An Array of GWInstaller::AL::Software objects) 
sub get_available_components{
	
}

# returns list of all potential software components (An Array of GWInstaller::AL::Software objects) 
sub get_components{
	
}

# returns list of all conflicting software packages (An Array of GWInstaller::AL::Software objects) 
sub get_conflicts{
	
}

# returns list of conflicting packages (An Array of GWInstaller::AL::Software objects) found to be installed on the host system
sub get_installed_conflicts{
}


#returns a list of all prerequisite software packages (An Array of GWInstaller::AL::Software objects) 
sub get_prerequisites{
}

# returns list of prequisite packages which have not been installed (An Array of GWInstaller::AL::Software objects) 
sub get_missing_prerequisites{
	
}

# gets the list of installed and available components and returns a merged list with statuses
# Params: none
# Return: an array of software objects that have their status values set.
 sub get_customer_components{
 	
 	
 }

