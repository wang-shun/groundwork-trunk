################################################################################
#
#   GDMAUtils.pm
#   Originally created on 28th August 2009, by Hrisheekesh Kale.
#
#   This library contains utility subroutines for GDMA. Both poller
#   and spool processor use these subroutines, so double check before
#   making a change.
#
#   Copyright 2003-2016 GroundWork Open Source, Inc.
#   http://www.groundworkopensource.com
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
#   implied. See the License for the specific language governing
#   permissions and limitations under the License.
#
################################################################################

package GDMAUtils;

use Fcntl qw(:DEFAULT :flock);
use Sys::Hostname;
use Net::Domain qw(hostfqdn);

# Use the exporter module to make a bunch of functions available to the program
# linking to this library.
use Exporter;
@ISA    = ('Exporter');
@EXPORT = qw(&read_config &read_multiple_config &validate_config &spool_results
  &get_spool_filename &try_lock &get_lock &get_current_time_str
  &load_hires_timer $utilsdebug);

use strict;

################################################################################
#   gdma_debug()
#
#   Local debug function. Set $debug to 1 to enable debugging in this
#   library.
#
################################################################################
sub gdma_debug {
    print "@_" if ($GDMAUtils::utilsdebug);
}

################################################################################
#   read_config()
#
#   Reads configuration file into the %config hash passed.
#   Returns 1 on success and 0 on failure.
#
#   Arguments:
#   $configfile - Full pathname for the config file.
#   $config - A reference to config structure to be loaded.
#   $errstr - A reference to an error string variable. This is an output
#             parameter and is be updated according to error encountered,
#             if any, indicated by a "0" return.
#   $optional - A flag to tell whether this config file might not exist.
#
################################################################################
sub read_config {
    my $configfile   = shift;
    my $config       = shift;
    my $errstr       = shift;
    my $optional     = shift;
    my $checkname    = ();

    $$errstr = "Ok";

    if ( $configfile !~ m{[/\\](gdma_auto\.conf|gdma_override\.conf|gwmon_[^/\\]+\.cfg)$} ) {
	$$errstr = "Failed to parse the config filepath ($configfile)";
	return 0;
    }

    # Open the config file for reading.
    if ( !open( CONFIG, '<', "$configfile" ) ) {
	$$errstr = "Cannot open configuration file $configfile ($!).";
	return $optional ? 1 : 0;
    }
    while ( my $line = <CONFIG> ) {
	## Discard comment and invalid lines
	## Comment if line starts with #
	if ( $line =~ /^\s*#/ ) { next }
	if ( $line =~ /^\s*(\S+)\s*=\s*"(.*?)"/ ) {
	    my $parameter = $1;
	    my $value     = $2;

	    # Sample line:
	    # Check_Disk[1]_Parm_--warning = "10%"
	    if ( $parameter =~ /^(.*?)\[(\d+)\]_(.*)/ ) {
		$config->{$1}->[$2]->{$3} = $value;
	    }
	    elsif ( $parameter =~ /^(.*?)\[(\d+)\]$/ ) {
		$config->{$1}->[$2] = $value;
	    }
	    else {
		$config->{$parameter} = $value;
	    }
	}
	elsif ( $line =~ /^\s*(\S+)\s*=\s*(\S+)/ ) {
	    ## If parameter value starts with # then it is not discarded.
	    ## That is, value is not discarded if # is present after =.

	    # Two cases if No quotes after "=" character:
	    #
	    # 1.Set to another parameter that has already been defined.
	    # Example:
	    # Monitor_Server[1]= "groundwork.company.com"
	    # Check_Response_Servlet[1]_Parm_-n = Monitor_Server[1]
	    # Also support multiples, i.e.:
	    # Check_Response_Servlet[1]_Parm_-n = Monitor_Server[1],Monitor_Server[2]
	    #
	    # 2.If parameter is not defined then it is value without quotes.
	    # Example:
	    # gw.company.com is not defined as parameter in the config file
	    # Check_Response_Servlet[1]_Parm_-n = gw.company.com
	    # Also support multiples, i.e.:
	    # Check_Response_Servlet[1]_Parm_-n = gw1.company.com,gw2.company.com
	    #
	    my $parameter = $1;
	    my $target    = $2;

	    # There may be multiple target parameters. Split them into an array.
	    my @targetparameters = split /,/, $target;
	    gdma_debug("target = $2\n");

	    my $value = "";
	    foreach my $targetparameter (@targetparameters) {
		gdma_debug("Checking targetparameter=$targetparameter\n");
		if ( $targetparameter =~ /^(.*?)\[(\d+)\]_(.*)/ ) {
		    ## Target parameter of the format Check_Response_Servlet[1]_Parm
		    ## We stored it as an array of hashes.
		    $value .= $config->{$1}->[$2]->{$3} . ",";
		}
		elsif ( $targetparameter =~ /^(.*?)\[(\d+)\]$/ ) {
		    ## Target parameter of the format Monitor_Server[1].
		    $value .= $config->{$1}->[$2] . ",";
		}
		elsif ( defined $config->{$targetparameter} ) {
		    ## Target parameter of the format Monitor_Server.
		    $value .= $config->{$targetparameter} . ",";
		}
		else {
		    ## Target parameter is not defined in the config file;
		    ## in this case, the Target parameter is value.
		    $value .= $targetparameter . ",";
		}
	    }
	    gdma_debug("value = $value\n");
	    if ($value) {
		## Get rid of trailing comma.
		$value =~ s/,$//;

		# Append the value to the existing one.
		# This is neccessary because of multiple target parameters.
		# Multiple values will be comma separated.
		if ( $parameter =~ /^(.*?)\[(\d+)\]_(.*)/ ) {
		    ## Parameter of the format Check_Response_Servlet[1]_Parm.
		    $config->{$1}->[$2]->{$3} = $value;
		}
		elsif ( $parameter =~ /^(.*?)\[(\d+)\]$/ ) {
		    ## Parameter of the format Monitor_Server[1].
		    $config->{$1}->[$2] = $value;
		}
		else {
		    ## Parameter of the format Monitor_Server.
		    $config->{$parameter} = $value;
		}
		gdma_debug("setting parameter $parameter \n");
	    }
	}
	elsif ( $line =~ /^\s*(\S+)\s*#?/ ) {
	    my $parameter = $1;

	    # Sample line:
	    # Check_Disk[1]_Parm_--errors-only
	    if ( $parameter =~ /^(.*?)\[(\d+)\]_(.*)/ ) {
		$config->{$1}->[$2]->{$3} = "";
	    }
	    elsif ( $parameter =~ /^(.*?)\[(\d+)\]$/ ) {
		$config->{$1}->[$2] = "";
	    }
	    else {
		$config->{$parameter} = "";
	    }
	}
    }
    close CONFIG;

    # We got up to here. Return success.
    return 1;
}

################################################################################
#   read_multiple_config()
#
#   Reads configuration files into the %config hash passed.
#   First read the host config file for localhost using read_config()
#   Next read out only service specific stuff from remaining config files.
#   Returns 1 on success and 0 on failure.
#
#   Arguments:
#   $configfile - Full pathname for the config file.
#   $config - A reference to config structure to be loaded.
#   $errstr - A reference to an error string variable. This is an output
#             parameter and is to be updated according to error encountered,
#             if any, indicated by a "0" return.
#
################################################################################
sub read_multiple_config {
    my $configfile        = shift;
    my $config            = shift;
    my $errstr            = shift;
    my $checkname         = ();
    my $ret_val           = 1;

    # First read the host config file as normal -> read_config().
    $$errstr = "Ok";
    if ( GDMAUtils::read_config( $configfile, $config, $errstr, 0 ) ) {
	my $config_dir;
	my $DIR;
	my $CONFIG;
	my $file;
	my $temp;
	my $hostname = my_hostname( $config->{Use_Long_Hostname}, $config->{Forced_Hostname}, $config->{Use_Lowercase_Hostname} );
	my $hostname_cfg = "gwmon_$hostname.cfg";
	my $file_path;
	my $current_host;
	my $line;

	# Parse out the config directory path, we need to read other config files as well.
	$config_dir = $configfile;
	$config_dir =~ s/$hostname_cfg/""/e;

	# Build the $config hash from set of downloaded files in $config_dir.
	# We have already read the contents of main host config file in
	# $config(reference to hash).
	# Get the directory contents of $config_dir and read the other "*.cfg"
	# files for service related parameters and append to $config.
	if ( opendir( $DIR, $config_dir ) ) {
	    my @all_the_files = readdir($DIR);
	    closedir($DIR);

	    # Read all the config files other than local host config file.
	    foreach $file (@all_the_files) {
		## Read host-config files ending with .cfg and not the local host config.  Be sure
		## to skip the send_nsca.cfg file, which also normally lives in this directory, by
		## restricting the filter to only process files that look like host-config files.
		if ( ( $file =~ /^gwmon_.*\.cfg$/ ) and ( $file ne $hostname_cfg ) ) {
		    $file_path    = "$config_dir/$file";
		    $current_host = $file;
		    $current_host =~ s/gwmon_//;
		    $current_host =~ s/\.cfg//;
		    if ( !open( $CONFIG, '<', "$file_path" ) ) {
			$$errstr = "Cannot open configuration file $file_path ($!).";
			return 0;
		    }
		    while ( $line = <$CONFIG> ) {
			## Discard comment and invalid lines
			## Comment if line starts with #
			if ( $line =~ /^\s*#/ ) { next }
			if ( $line =~ /^\s*(\S+)\s*=\s*"(.*?)"/ ) {
			    my $parameter = $1;
			    my $value     = $2;

			    # Global parameters are read from main host config.
			    # Ignore global parameters from other configs and
			    # read only the service check related parameters.
			    if ( $parameter =~ /^(.*?)\[(\d+)\]_(.*)/ ) {
				## The service checks read from multiple host config
				## files are inserted into the g_config hash.  If two
				## files contain the same service checks, previously
				## read ones are overwritten.  So prepend hostname while
				## inserting so that it becomes unique.  For example,
				## Check_gdma_wmi_cpu_get_cpu would be inserted as
				## lysithea_Check_gdma_wmi_cpu_get_cpu for host lysithea.
				$checkname = join( "_", $current_host, $1 );
				$config->{$checkname}->[$2]->{$3} = $value;
			    }
			    elsif ( $parameter =~ /^(.*?)\[(\d+)\]$/ ) {
				## FIX LATER:  Apparently, we don't consider a setting
				## like this to represent a service-check related parameter.
				## Is that true?
				## $checkname = join( "_", $current_host, $1 );
				## $config->{$checkname}->[$2] = $value;
			    }
			}
			# ===========================================================
			elsif ( $line =~ /^\s*(\S+)\s*=\s*(\S+)/ ) {
			    ## See read_config() for details of the processing here.
			    ## However, the processing here is adjusted to account for
			    ## the fact that the setup for each host should only refer
			    ## to its own configuration parameters, plus any global
			    ## configuration parameters.

			    # Two cases if No quotes after "=" character:
			    #
			    # 1.Set to another parameter that has already been defined.
			    # Example:
			    # Monitor_Server[1]= "groundwork.company.com"
			    # Check_Response_Servlet[1]_Parm_-n = Monitor_Server[1]
			    # Also support multiples, i.e.:
			    # Check_Response_Servlet[1]_Parm_-n = Monitor_Server[1],Monitor_Server[2]
			    #
			    # 2.If parameter is not defined then it is value without quotes.
			    # Example:
			    # gw.company.com is not defined as parameter in the config file
			    # Check_Response_Servlet[1]_Parm_-n = gw.company.com
			    # Also support multiples, i.e.:
			    # Check_Response_Servlet[1]_Parm_-n = gw1.company.com,gw2.company.com
			    #
			    my $parameter = $1;
			    my $target    = $2;

			    # There may be multiple target parameters. Split them into an array.
			    my @targetparameters = split /,/, $target;
			    gdma_debug("target = $2\n");

			    my $value = "";
			    foreach my $targetparameter (@targetparameters) {
				gdma_debug("Checking targetparameter=$targetparameter\n");
				if ( $targetparameter =~ /^(.*?)\[(\d+)\]_(.*)/ ) {
				    ## Target parameter of the format Check_Response_Servlet[1]_Parm
				    ## We stored it earlier as an array of hashes.
				    $checkname = join( "_", $current_host, $1 );
				    $value .= $config->{$checkname}->[$2]->{$3} . ",";
				}
				elsif ( $targetparameter =~ /^(.*?)\[(\d+)\]$/ ) {
				    ## Target parameter of the format Monitor_Server[1].
				    $checkname = join( "_", $current_host, $1 );
				    $value .= $config->{$checkname}->[$2] . ",";
				}
				elsif ( defined $config->{$targetparameter} ) {
				    ## Target parameter of the format Monitor_Server.
				    $value .= $config->{$targetparameter} . ",";
				}
				else {
				    ## Target parameter is not defined in the config file;
				    ## in this case, the Target parameter is value.
				    $value .= $targetparameter . ",";
				}
			    }
			    gdma_debug("value = $value\n");
			    if ($value) {
				## Get rid of trailing comma.
				$value =~ s/,$//;

				# Append the value to the existing one.
				# This is neccessary because of multiple target parameters.
				# Multiple values will be comma separated.
				if ( $parameter =~ /^(.*?)\[(\d+)\]_(.*)/ ) {
				    ## Parameter of the format Check_Response_Servlet[1]_Parm.
				    $checkname = join( "_", $current_host, $1 );
				    $config->{$checkname}->[$2]->{$3} = $value;
				}
				elsif ( $parameter =~ /^(.*?)\[(\d+)\]$/ ) {
				    ## Parameter of the format Monitor_Server[1].
				    $checkname = join( "_", $current_host, $1 );
				    $config->{$checkname}->[$2] = $value;
				}
				else {
				    ## Parameter of the format Monitor_Server.
				    $config->{$parameter} = $value;
				}
				gdma_debug("setting parameter $parameter \n");
			    }
			}
			elsif ( $line =~ /^\s*(\S+)\s*#?/ ) {
			    my $parameter = $1;

			    # Sample line:
			    # Check_Disk[1]_Parm_--errors-only
			    if ( $parameter =~ /^(.*?)\[(\d+)\]_(.*)/ ) {
				$checkname = join( "_", $current_host, $1 );
				$config->{$checkname}->[$2]->{$3} = "";
			    }
			    elsif ( $parameter =~ /^(.*?)\[(\d+)\]$/ ) {
				$checkname = join( "_", $current_host, $1 );
				$config->{$checkname}->[$2] = "";
			    }
			    else {
				$config->{$parameter} = "";
			    }
			}
			# ===========================================================
		    }
		    close $CONFIG;
		}
	    }
	}
	else {
	    $$errstr = "Failed to open the config directory $config_dir ($!)";
	    $ret_val = 0;
	}
    }
    else {
	## read_config() failed. Report error.
	gdma_debug "read_multiple_config:  Failed to read host config file:\n$$errstr\n";
	$ret_val = 0;
    }
    return $ret_val;
}

################################################################################
#   validate_config()
#
#   Syntax checks and validates values of a read configuration file.
#   Returns 1 on success and 0 on failure.
#
#   Arguments:
#   $config - A reference to config structure to be loaded.
#   $errstr - A reference to an error string variable. This is an output
#             parameter and is to be updated according to error encountered,
#             if any, indicated by a "0" return.
#
#   Author:  DSN 9/09
#
################################################################################
sub validate_config {

    # TBD :
    #  - check for duplicate directive definitions ?
    #  - add directive dependency element sets ?

    my ( $ref_config, $ref_errstr ) = @_;

    my (
	%required_directives,        $required_directive,             @missing_directives,             %syntax,
	$directive,                  @invalid_directives,             @directives_without_regexes,     @invalid_value_structures,
	@invalid_directive_ranges,   @all_validation_errors,          $defined_check_iteration,        $defined_check_iteration_command,
	@invalid_service_directives, @required_service_subdirectives, @optional_service_subdirectives, $required_service_subdirectives,
	$all_service_subdirectives,  $required_service_subdirective,  @missing_service_subdirectives,
    );

    # The following syntax hash is comprised of the following structured elements :
    #
    #    <Directive> => {
    #        'requirement' => <optional | required>,
    #        'regex'       => <regex>,
    #        'message'     => <message string>
    #    }
    #
    #    Where :
    #
    #        <Directive> is a valid GDMA configuration directive. The directives herein are not service check directives (Check_*).
    #        These will be checked seperately since we don't know their names in advance.
    #
    #        <Directive> -> {requirement} is either 'optional' or 'required'. This is used when checking the cfg
    #        to ensure all required directives are presently defined.  Omitting this from the definition will effectively
    #        make the directive an optional directive.
    #
    #        <Directive> -> {regex} is a regular expression which is used for checking the structure of the directives's value.
    #        Omitting this from the definition here will result in an internal error being thrown.
    #
    #        <Directive> -> {message} is an informative string that is used to indicate what is expected of the value, and is
    #        used in the case when the regex is not matched.
    #
    #        (Other fields might be added as needed, such as moving the min/max ranges in from lower code sections etc)

    # Algorithm overview
    # 1. check that all required non-service-related directives are present
    # 2. check that all non-service directives defined in the config are part of the GDMA syntax
    # 3. check structural validity of non-service-related directives values (defaults have been set where necessary)
    # 4. check range validity of non-service-related directives values (defaults have been set where necessary)
    # 5. check validity of service related directives values. This includes :
    #    5.1 checking for required service sub-directives for each service block
    #    5.2 checking syntax of the syntax of the sub directive
    #    5.3 checking validity of the value of the sub directive

    # FIX LATER:  Why aren't more of these required directives, instead of optional?  Where are default values set?

    %syntax = (
	"Auto_Register_User" => {
	    'requirement' => 'optional',
	    'regex'       => '\\S*',	# For now, just match anything -- might want to improve this, tho
	    'message' => "Auto_Register_User must be a non-space-separated string",
	},
	"Auto_Register_Pass" => {
	    'requirement' => 'optional',
	    'regex'       => '.*',	# For now, just match anything -- might want to improve this, tho
	    'message' => "Auto_Register_Pass should be a hard-to-guess string",
	},
	"Auto_Register_Host_Profile" => {
	    'requirement' => 'optional',
	    'regex'       => '[^\\s`()\'"]*',	# For now, match almost anything -- might want to improve this, tho
	    'message' => "Auto_Register_Host_Profile must be a non-space-separated string",
	},
	"Auto_Register_Service_Profile" => {
	    'requirement' => 'optional',
	    'regex'       => '[^\\s`()\'"]*',	# For now, match almost anything -- might want to improve this, tho
	    'message' => "Auto_Register_Service_Profile must be a non-space-separated string",
	},
	"Auto_Register_Attempts" => {
	    'requirement' => 'optional',
	    'regex'       => '(never|once|arithmetic|exponential|fibonacci|periodic)',
	    'message' => "Auto_Register_Attempts must be one of:  never once arithmetic exponential fibonacci periodic",
	},
	"Auto_Register_Max_Interval" => {
	    'requirement' => 'optional',
	    'regex'       => '[1-9][0-9]*',
	    'message' => "Auto_Register_Max_Interval must be an unsigned non-zero positive integer",
	},
	"Enable_Auto" => {
	    'requirement' => 'optional',
	    'regex'       => '(ON|On|on|oN|off)',
	    'message'     => "Enable_Auto must be ON, On, on, oN, or off",
	},
	"Spooler_Status" => {
	    'requirement' => 'optional',
	    'regex'       => '(ON|On|on|oN|off|Off|updates|Updates)',
	    'message'     => "Spooler_Status must be ON, On, on, oN, off, Off, updates, or Updates",
	},

	"Poller_Status" =>
	  { 'requirement' => 'optional', 'regex' => '(ON|On|on|oN|off|Off)', 'message' => "Poller_Status must be ON, On, on, oN, off, Off", },

	"Use_Long_Hostname" =>
	  { 'requirement' => 'optional', 'regex' => '(ON|On|on|off|Off)', 'message' => "Use_Long_Hostname must be ON, On, on, off, Off", },

	"Use_Lowercase_Hostname" =>
	  { 'requirement' => 'optional', 'regex' => '(ON|On|on|off|Off)', 'message' => "Use_Lowercase_Hostname must be ON, On, on, off, Off", },

	# See http://en.wikipedia.org/wiki/Hostname for hostname validation rules.
	"Forced_Hostname" => {
	    'requirement' => 'optional',
	    'regex'       => '[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(\\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*',
	    'message'     => "Forced_Hostname must be either omitted, or a valid FQDN",
	},
	"Spooler_Batch_Size" => {
	    'requirement' => 'optional',
	    'regex'       => '[1-9][0-9]*',
	    'message'     => "Spooler_Batch_Size must be an unsigned non-zero positive integer",
	},

	# --------------------------------------------------------
	# Service-related directives will be checked separately
	# "Check_gdma_wmi_cpu_get_cpu" =>
	#   { 'requirement' => 'optional', 'regex' => "", },
	# --------------------------------------------------------

	"ConfigFile_Pull_Cycle" =>
	  { 'requirement' => 'optional', 'regex' => '[0-9]+', 'message' => "ConfigFile_Pull_Cycle must be an unsigned integer" },

	"ConfigPull_Timeout" =>
	  { 'requirement' => 'optional', 'regex' => '[0-9]+', 'message' => "ConfigPull_Timeout must be an unsigned integer" },

	"PluginPull_Timeout" =>
	  { 'requirement' => 'optional', 'regex' => '[0-9]+', 'message' => "PluginPull_Timeout must be an unsigned integer" },

	"Poller_Plugin_Timeout" => {
	    'requirement' => 'optional',
	    'regex'       => '[1-9][0-9]*',
	    'message'     => "Poller_Plugin_Timeout must be an unsigned non-zero positive integer",
	},
	"GDMA_Auto_Host" => {
	    'requirement' => 'optional',
	    'regex'       => '\\S+',	# For now, just match anything -- might want to improve this, tho
	    'message' => "GDMA_Auto_Host must be a non-empty, non-space-separated string",
	},
	"GDMA_Auto_Service" => {
	    'requirement' => 'optional',
	    'regex'       => '\\S+',	# For now, just match any non-empty, non-whitespace string -- might want to improve this, tho
	    'message' => "GDMA_Auto_Service must be a non-empty, non-space-separated string",
	},

	"HostSequenceNumber" =>            # this is currently not used, but present in cfg's so put in check for future
	  { 'requirement' => 'optional', 'regex' => '[0-9]+', 'message' => "HostSequenceNumber must be an unsigned integer" },

	"Enable_Local_Logging" =>
	  { 'requirement' => 'optional', 'regex' => "(on|off)", 'message' => "Enable_Local_Logging must be either on or off", },

	"Max_Server_Redirects" =>
	  { 'requirement' => 'optional', 'regex' => '[0-9]+', 'message' => "Max_Server_Redirects must be an unsigned integer" },

	"Poller_Proc_Interval" => {
	    'requirement' => 'optional',
	    'regex'       => '[1-9][0-9]*',
	    'message'     => "Poller_Proc_Interval must be an unsigned non-zero positive integer",
	},
	"Spooler_Max_Retries" => {
	    'requirement' => 'optional',
	    'regex'       => '[1-9][0-9]*',
	    'message'     => "Spooler_Max_Retries must be an unsigned non-zero positive integer",
	},
	"Monitor_Host_Type" => {
	    'requirement' => 'optional',
	    'regex'   => '(hostname_command|long_hostname_command|short_hostname_command|config_file_hostname|Monitor_Host)',
	    'message' => "Monitor_Host_Type must be hostname_command, long_hostname_command, short_hostname_command, config_file_hostname, or Monitor_Host",
	},
	"Monitor_Host" => {
	    'requirement' => 'optional',
	    ## This character-set restriction prohibits shell metacharacters for safety, but is not a full validation as a hostname.
	    ## We allow underscore only in case Windows machines might require it, though it's not RFC-compliant.
	    'regex'       => '[a-zA-Z0-9_](?:[-a-zA-Z0-9._]*[a-zA-Z0-9_])?',
	    'message' => "Monitor_Host must be a valid hostname",
	},
	"Spooler_NSCA_Port" => {
	    'requirement' => 'optional',
	    'regex'       => '[1-9][0-9]*',
	    'message'     => "Spooler_NSCA_Port must be an unsigned non-zero positive integer"
	},
	"Spooler_NSCA_Program" => {
	    'requirement' => 'optional',
	    'regex'       => '.+',	# For now, just match any non-empty string -- might want to improve this, tho
	    'message' => "Spooler_NSCA_Program must be a non-empty string",
	},
	"Spooler_NSCA_Config" => {
	    'requirement' => 'optional',
	    'regex'       => '.+',	# For now, just match any non-empty string -- might want to improve this, tho
	    'message' => "Spooler_NSCA_Config must be a non-empty string",
	},

	"NumHostsInInstallation" =>        # this is currently not used, but present in cfg's so put in check for future
	  { 'requirement' => 'optional', 'regex' => '[0-9]+', 'message' => "HostSequenceNumber must be an unsigned integer" },

	"Logdir" => { 'requirement' => 'optional', 'regex' => '.+', 'message' => "Logdir must be a non-empty string", },

	"Poller_Plugin_Directory" =>
	  { 'requirement' => 'optional', 'regex' => '.+', 'message' => "Poller_Plugin_Directory must be a non-empty string", },

	"Poller_Service" => { 'requirement' => 'optional', 'regex' => '.+', 'message' => "Poller_Service must be a non-empty string", },

	"Spooler_Service" => { 'requirement' => 'optional', 'regex' => '.+', 'message' => "Spooler_Service must be a non-empty string", },

	"Poller_Pull_Failure_Interval" => {
	    'requirement' => 'optional',
	    'regex'       => '[0-9]+',
	    'message'     => "Poller_Pull_Failure_Interval must be an unsigned integer",
	},
	"Spooler_Retention_Time" => {
	    'requirement' => 'optional',
	    'regex'       => '[1-9][0-9]*',
	    'message'     => "Spooler_Retention_Time must be an unsigned non-zero positive integer",
	},
	"Spooler_NSCA_Timeout" => {
	    'requirement' => 'required',
	    'regex'       => '[0-9]+',                                               # allows 0 -- might need to change that -- unclear
	    'message'     => "Spooler_NSCA_Timeout must be an unsigned integer"    # change this if you change the regex
	},
	"Spooler_Proc_Interval" => {
	    'requirement' => 'optional',
	    'regex'       => '[1-9][0-9]*',
	    'message'     => "Spooler_Proc_Interval must be an unsigned non-zero positive integer",
	},
	"Target_Server" => {
	    'requirement' => 'required',
	    'regex'       => '.+',	# For now, just match any non-empty, non-whitespace string -- might want to improve this, tho
	    'message' => "Target_Server must be a non-empty string",
	},
	"Target_Server_Secondary" => {
	    'requirement' => 'optional',
	    'regex'       => '.+',                                                     # just match any non-empty, non-whitespace string
	    'message'     => "Target_Server_Secondary must be a non-empty string",
	},
	"Host_Name_Prefix" => {
	    'requirement' => 'optional',
	    'regex'       => '.+',                                                     # just match any non-empty, non-whitespace string
	    'message'     => "Host Name Prefix must be a non-empty string",
	},
	"GDMAConfigDir"        => { 'requirement' => 'optional', 'regex' => '\\S+', 'message' => "GDMAConfigDir must be a non-empty string", },
	"Max_Concurrent_Hosts" => {
	    'requirement' => 'optional',
	    'regex'       => '[1-9][0-9]*',
	    'message'     => "Max_Concurrent_Hosts must be an unsigned non-zero positive integer",
	},
	"GDMA_Multihost" => { 'requirement' => 'optional', 'regex' => '(on|off)', 'message' => "GDMA_Multihost must be either on or off", },
	"Critical_Threshold" => {
	    'requirement' => 'optional',
	    'regex'       => '[1-9][0-9]*',
	    'message'     => "Critical_Threshold must be an unsigned non-zero positive integer",
	},
	"Warning_Threshold" => {
	    'requirement' => 'optional',
	    'regex'       => '[1-9][0-9]*',
	    'message'     => "Warning_Threshold must be an unsigned non-zero positive integer",
	},
	## FIX LATER:  Notice that the Sleep_Interval regex doesn't enforce being non-zero.
	"Sleep_Interval" => {
	    'requirement' => 'optional',
	    'regex'       => '[0-9]+([.][0-9]*)?',
	    'message'     => "Sleep_Interval must be an unsigned positive float",
	},
	"Enable_Poller_Plugins_Upgrade" => {
	    'requirement' => 'optional',
	    'regex'       => '(On|on|Off|off)',
	    'message'     => "Enable_Poller_Plugins_Upgrade must be either On, on, Off or off"
	},
	"Poller_Plugins_Upgrade_URL" =>
	  { 'requirement' => 'optional', 'regex' => '\\S+', 'message' => "Poller_Plugins_Upgrade_URL must be a non-empty string", },
    );    # end of %syntax definition

    # use Data::Dumper; $Data::Dumper::Useqq=1; print Dumper($ref_config);

    $$ref_errstr = "Ok";    # assume all ok and disprove

    # ----------------------------------------------------------------
    # check presence of required directives and bail if not all found
    # (we'll validate right hand sides later)
    # ----------------------------------------------------------------

    gdma_debug("Config validation:  Checking for required directives.\n");

    # first select the required directives from %syntax
    foreach $directive ( keys %syntax ) {
	$required_directives{$directive} = 1 if $syntax{$directive}{'requirement'} eq 'required';
    }

    # go through all of the required directives and see if it was defined
    foreach $required_directive ( sort keys %required_directives ) {
	gdma_debug("\tChecking for directive '$required_directive'");

	# if the directive was not defined, record it in @missing_directives, and carry on checking for other missing ones
	if ( not defined $$ref_config{$required_directive} ) {
	    gdma_debug(" (MISSING)\n");
	    push( @missing_directives, $required_directive );
	}
	else {
	    gdma_debug(" (found)\n");
	}
    }

    # If missing directives were recorded, return them in an error string and stop checking.
    # Why stop checking? Missing required directives are showstoppers and need resolving before getting
    # into details of other problems with the config.
    # Might change this later to dump all errors out in one go.
    if ( $#missing_directives > -1 ) {
	$$ref_errstr = "Config validation:  The following required directives were not defined:  " . join( ", ", @missing_directives ) . " ";
	return 0;
    }

    # ----------------------------------------------------------------
    # check for invalid directives meaning check for the existence of
    # any directives that are not part of the GDMA syntax.  This is
    # different from just ignoring them as in previous versions.
    # ----------------------------------------------------------------

    gdma_debug("Config validation:  Checking for non-service-related directives that are not part of the GDMA syntax set.\n");

    # Check each directive found in the config against a complete list of known valid directives.
    foreach $directive ( sort keys %$ref_config ) {

	next if ( $directive =~ /Check_/ );

	gdma_debug("\tChecking config file directive '$directive' is a valid directive");

	if ( not $syntax{$directive} ) {
	    gdma_debug(" (INVALID)\n");
	    push( @invalid_directives, $directive );
	}
	else {
	    gdma_debug(" (valid)\n");
	}

    }

    # If invalid directives were recorded, return them in an error string and stop checking.
    # Why stop checking here ? Invalid directives are show stoppers at this point and need resolving before getting
    # into details of other problems with the config.
    # Might change this later to dump all errors out in one go.
    if ( $#invalid_directives > -1 ) {
	$$ref_errstr = "Config validation:  The following directives are not valid:  " . join( ", ", @invalid_directives ) . " ";
	return 0;
    }

    # ----------------------------------------------------------------
    # check each non-service directive for validity
    # FIX LATER:  excessive code duplication; abstract and collapse
    # ----------------------------------------------------------------

    gdma_debug(
	"Config validation:  Checking structural validity of non-service related directives values (defaults have been set where necessary).\n");

    # validate directives value structure found in the config against a regex definition in %syntax
    foreach $directive ( sort keys %$ref_config ) {

	next if $directive =~ /check/i;    # We'll check service-related externals later

	gdma_debug("\tValidating config file directive '$directive' value '$$ref_config{$directive}'");

	# First check if there is a regex defined for the directive.
	# If there isn't, then it's effectively an internal error, so record that for later.
	if ( not defined $syntax{$directive}{'regex'} ) {
	    gdma_debug(" (NO REGEX DEFINED)\n");
	    push( @directives_without_regexes, " No regex is defined for validating the value structure of '$directive'" );
	}

	# A regex was found, so proceed to check the structure of the directive value against it.
	else {
	    if ( $$ref_config{$directive} !~ /^$syntax{$directive}{'regex'}$/ ) {
		gdma_debug(" (INVALID)\n");
		push( @invalid_value_structures,
" $directive value '$$ref_config{$directive}' is invalid:  $syntax{$directive}{'message'} [ regex:  $syntax{$directive}{'regex'} ] "
		);
	    }
	    else {
		gdma_debug(" (valid)\n");
	    }
	}
    }    # end directive value structure validation looping over all directives defined in config hash

    #validation -- dependency check
    gdma_debug("Config validation:  Checking dependency of non-service related directives.\n");

    gdma_debug("\tChecking dependancy of Enable_Poller_Plugins_Upgrade:\n");

    # Enable_Poller_Plugins_Upgrade's On/on value depends on Poller_Plugins_Upgrade_URL
    if (   defined $$ref_config{Enable_Poller_Plugins_Upgrade}
	&& $$ref_config{Enable_Poller_Plugins_Upgrade} =~ /^[O|o]n$/
	&& !defined $$ref_config{Poller_Plugins_Upgrade_URL} )
    {
	gdma_debug("Poller_Plugins_Upgrade_URL must be defined if Enable_Poller_Plugins_Upgrade is On or on: ");
	gdma_debug(" (failed)\n");
	push( @invalid_value_structures, " Poller_Plugins_Upgrade_URL must be defined if Enable_Poller_Plugins_Upgrade is On or on" );
    }
    else {
	gdma_debug("Enable_Poller_Plugins_Upgrade is On and Poller_Plugins_Upgrade_URL is defined: ");
	gdma_debug(" (passed)\n");
    }

    # validation -- range checks
    gdma_debug(
	"Config validation:  Checking range validity of non-service related directives values (defaults have been set where necessary).\n");

    # Theoretically, we should validate the top-level domain here, too.
    if ( defined $$ref_config{Forced_Hostname} ) {
	gdma_debug("\tRange checking value of Forced_Hostname (value $$ref_config{Forced_Hostname}):  must be no longer than 255 characters");
	if ( length($$ref_config{Forced_Hostname}) > 255 ) {
	    gdma_debug(" (failed)\n");
	    push( @invalid_directive_ranges, " Forced_Hostname cannot be longer than 255 characters; found '$$ref_config{Forced_Hostname}'" );
	}
	else {
	    gdma_debug(" (passed)\n");
	}
    }

    # Spooler_Batch_Size:  valid range is [5..infinity]
    if ( defined $$ref_config{Spooler_Batch_Size} ) {
	gdma_debug("\tRange checking value of Spooler_Batch_Size (value $$ref_config{Spooler_Batch_Size}):  must be at least 5");
	if ( $$ref_config{Spooler_Batch_Size} < 5 ) {
	    gdma_debug(" (failed)\n");
	    push( @invalid_directive_ranges, " Spooler_Batch_Size must be at least 5, found '$$ref_config{Spooler_Batch_Size}'" );
	}
	else {
	    gdma_debug(" (passed)\n");
	}
    }

    # Spooler_Max_Retries:  range is [1..100]
    if ( defined $$ref_config{Spooler_Max_Retries} ) {
	gdma_debug("\tRange checking value of Spooler_Max_Retries (value $$ref_config{Spooler_Max_Retries}):  must be between 1 and 100, inclusive");
	if ( ( $$ref_config{Spooler_Max_Retries} < 1 ) or ( $$ref_config{Spooler_Max_Retries} > 100 ) ) {
	    gdma_debug(" (failed)\n");
	    push( @invalid_directive_ranges, " Spooler_Max_Retries must be between 1 and 100, found '$$ref_config{Spooler_Max_Retries}'" );
	}
	else {
	    gdma_debug(" (passed)\n");
	}
    }

# Target_Server:  details unknown at this time TBD
# if ( defined $$ref_config{Target_Server} )
# {
#    gdma_debug("\tRange checking value of Target_Server (value $$ref_config{Target_Server}):  must meet some special critera to be defined");
#    # if (  $$ref_config{Target_Server} doesn't pass some criteria yet to be defined )
#    # {
#    #    gdma_debug(" (failed)\n" );
#        push( @invalid_directive_ranges, "\tTarget_Server didn't pass validation, found '$$ref_config{Max_Retries}'" );;
#    # }
#    # else
#    # {
#    #    gdma_debug(" (passed)\n");
#    # }
# }

    # Spooler_Retention_Time:  range is [0..900]
    if ( defined $$ref_config{Spooler_Retention_Time} ) {
	gdma_debug(
	    "\tRange checking value of Spooler_Retention_Time (value $$ref_config{Spooler_Retention_Time}):  must be between 0 and 900, inclusive");
	if ( ( $$ref_config{Spooler_Retention_Time} < 0 ) or ( $$ref_config{Spooler_Retention_Time} > 900 ) ) {
	    gdma_debug(" (FAILED)\n");
	    push( @invalid_directive_ranges,
		" Spooler_Retention_Time must be between 0 and 900, found '$$ref_config{Spooler_Retention_Time}'" );
	}
	else {
	    gdma_debug(" (passed)\n");
	}
    }

    # Poller_Proc_Interval:  range is [10..3600]
    if ( defined $$ref_config{Poller_Proc_Interval} ) {
	gdma_debug(
	    "\tRange checking value of Poller_Proc_Interval (value $$ref_config{Poller_Proc_Interval}):  must be between 10 and 3600, inclusive");
	if ( ( $$ref_config{Poller_Proc_Interval} < 10 ) or ( $$ref_config{Poller_Proc_Interval} > 3600 ) ) {
	    gdma_debug(" (FAILED)\n");
	    push( @invalid_directive_ranges,
		" Poller_Proc_Interval must be between 10 and 3600, found '$$ref_config{Poller_Proc_Interval}'" );
	}
	else {
	    gdma_debug(" (passed)\n");
	}
    }

    # Spooler_Proc_Interval:  range is [10..3600]
    if ( defined $$ref_config{Spooler_Proc_Interval} ) {
	gdma_debug(
	    "\tRange checking value of Spooler_Proc_Interval (value $$ref_config{Spooler_Proc_Interval}):  must be between 10 and 3600, inclusive");
	if ( ( $$ref_config{Spooler_Proc_Interval} < 10 ) or ( $$ref_config{Spooler_Proc_Interval} > 3600 ) ) {
	    gdma_debug(" (FAILED)\n");
	    push( @invalid_directive_ranges,
		" Spooler_Proc_Interval must be between 10 and 3600, found '$$ref_config{Spooler_Proc_Interval}'" );
	}
	else {
	    gdma_debug(" (passed)\n");
	}
    }

    # Poller_Pull_Failure_Interval:  range is [0..2592000]
    if ( defined $$ref_config{Poller_Pull_Failure_Interval} ) {
	gdma_debug(
"\tRange checking value of Poller_Pull_Failure_Interval (value $$ref_config{Poller_Pull_Failure_Interval}):  must be between 0 and 2592000, inclusive"
	);
	if ( ( $$ref_config{Poller_Pull_Failure_Interval} < 0 ) or ( $$ref_config{Poller_Pull_Failure_Interval} > 2592000 ) ) {
	    gdma_debug(" (FAILED)\n");
	    push( @invalid_directive_ranges,
		" Poller_Pull_Failure_Interval must be between 0 and 2592000, found '$$ref_config{Poller_Pull_Failure_Interval}'" );
	}
	else {
	    gdma_debug(" (passed)\n");
	}
    }

    # ConfigFile_Pull_Cycle:  range is [1..10]
    if ( defined $$ref_config{ConfigFile_Pull_Cycle} ) {
	gdma_debug("\tRange checking value of ConfigFile_Pull_Cycle (value $$ref_config{ConfigFile_Pull_Cycle}):  must be between 1 and 10, inclusive");
	if ( ( $$ref_config{ConfigFile_Pull_Cycle} < 1 ) or ( $$ref_config{ConfigFile_Pull_Cycle} > 10 ) ) {
	    gdma_debug(" (FAILED)\n");
	    push( @invalid_directive_ranges, " ConfigFile_Pull_Cycle must be between 1 and 10, found '$$ref_config{ConfigFile_Pull_Cycle}'" );
	}
	else {
	    gdma_debug(" (passed)\n");
	}
    }

    # ConfigPull_Timeout:  range is [1..100]
    if ( defined $$ref_config{ConfigPull_Timeout} ) {
	gdma_debug("\tRange checking value of ConfigPull_Timeout(value $$ref_config{ConfigPull_Timeout}):  must be between 1 and 100, inclusive");
	if ( ( $$ref_config{ConfigPull_Timeout} < 1 ) or ( $$ref_config{ConfigPull_Timeout} > 100 ) ) {
	    gdma_debug(" (FAILED)\n");
	    push( @invalid_directive_ranges, " ConfigPull_Timeout must be between 1 and 100, found '$$ref_config{ConfigPull_Timeout}'" );
	}
	else {
	    gdma_debug(" (passed)\n");
	}
    }

    # PluginPull_Timeout:  range is [1..100]
    if ( defined $$ref_config{PluginPull_Timeout} ) {
	gdma_debug("\tRange checking value of PluginPull_Timeout(value $$ref_config{PluginPull_Timeout}):  must be between 1 and 100, inclusive");
	if ( ( $$ref_config{PluginPull_Timeout} < 1 ) or ( $$ref_config{PluginPull_Timeout} > 100 ) ) {
	    gdma_debug(" (FAILED)\n");
	    push( @invalid_directive_ranges, " PluginPull_Timeout must be between 1 and 100, found '$$ref_config{PluginPull_Timeout}'" );
	}
	else {
	    gdma_debug(" (passed)\n");
	}
    }

    # Poller_Plugin_Timeout:  range is [1..900]
    if ( defined $$ref_config{Poller_Plugin_Timeout} ) {
	gdma_debug(
	    "\tRange checking value of Poller_Plugin_Timeout (value $$ref_config{Poller_Plugin_Timeout}):  must be between 1 and 900, inclusive");
	if ( ( $$ref_config{Poller_Plugin_Timeout} < 1 ) or ( $$ref_config{Poller_Plugin_Timeout} > 900 ) ) {
	    gdma_debug(" (FAILED)\n");
	    push( @invalid_directive_ranges,
		" Poller_Plugin_Timeout must be between 1 and 900, found '$$ref_config{Poller_Plugin_Timeout}'" );
	}
	else {
	    gdma_debug(" (passed)\n");
	}
    }

    # Spooler_NSCA_Timeout:  range is [1..30]
    if ( defined $$ref_config{Spooler_NSCA_Timeout} ) {
	gdma_debug("\tRange checking value of Spooler_NSCA_Timeout (value $$ref_config{Spooler_NSCA_Timeout}):  must be between 1 and 30, inclusive");
	if ( ( $$ref_config{Spooler_NSCA_Timeout} < 1 ) or ( $$ref_config{Spooler_NSCA_Timeout} > 30 ) ) {
	    gdma_debug(" (FAILED)\n");
	    push( @invalid_directive_ranges, " Spooler_NSCA_Timeout must be between 1 and 30, found '$$ref_config{Spooler_NSCA_Timeout}'" );
	}
	else {
	    gdma_debug(" (passed)\n");
	}
    }

    # < Add more range checks in here. >

    # ----------------------------------------------------------------
    # Check each service directive value for validity.
    # ----------------------------------------------------------------

    gdma_debug("Config validation:  Checking validity of service related directives values.\n");

    # Two lists of service sub directives -- one set of required ones, one set of optional ones.
    # Note if you add another sub directive to either of these lists, make sure you add
    # a validation block lower down or you'll throw an internal error.
    @required_service_subdirectives = ( "Enable", "Service", "Command" );
    @optional_service_subdirectives = ( "Check_Interval", "Timeout" );
    $required_service_subdirectives = join( "|", @required_service_subdirectives );
    $all_service_subdirectives = join( "|", @required_service_subdirectives, @optional_service_subdirectives );

    foreach $directive ( sort keys %$ref_config ) {

	next if ( $directive !~ /Check_/ );

	# loop over each defined iteration of the defined service Check_xyz[1], Check_xyz[2] ...
	foreach ( $defined_check_iteration = 1 ; $defined_check_iteration <= $#{ $ref_config->{$directive} } ; $defined_check_iteration++ ) {

	    # first check that the required service sub directives are all present for this service definition
	    foreach $required_service_subdirective (@required_service_subdirectives) {
		gdma_debug(
		    "\tChecking for required service sub directive $directive\[$defined_check_iteration\]_$required_service_subdirective");
		if ( not defined $ref_config->{$directive}[$defined_check_iteration]{$required_service_subdirective} ) {
		    push( @missing_service_subdirectives,
			" Missing service sub directive $directive\[$defined_check_iteration\]_$required_service_subdirective" );
		    gdma_debug(" (MISSING)\n");
		}
		else {
		    gdma_debug(" (found)\n");
		}
	    }

	    # loop over each of the sets of sub directives (_Enable, _Service etc) in each defined service iteration block
	    foreach $defined_check_iteration_command ( keys %{ $ref_config->{$directive}->[$defined_check_iteration] } ) {
		gdma_debug("\tChecking service directive ${directive}\[$defined_check_iteration\]_${defined_check_iteration_command}");

		# If the sub command is not one that's valid, record that error.
		# if ( $defined_check_iteration_command !~ /^(Enable|Service|Command|Check_Interval)$/ )
		if ( $defined_check_iteration_command !~ /^($all_service_subdirectives)$/ ) {
		    gdma_debug(" (INVALID)\n");
		    push( @invalid_service_directives,
			" ${directive}\[$defined_check_iteration\]_${defined_check_iteration_command} is not a valid service directive" );
		}
		else {
		    gdma_debug(" (syntax:  valid,");

		    # If the directive name is ok, let's check that the value is valid.
		    # It's most efficient to do this here, as otherwise we've got to run
		    # through this awfully confusing crummy loop again somewhere else.

		    # Check service _Enable directive -- value must be on or off
		    # -------------------------------
		    if ( $defined_check_iteration_command =~ /^Enable$/ ) {
			if ( $ref_config->{$directive}[$defined_check_iteration]{Enable} !~ /^(on|off)$/i ) {
			    gdma_debug(" value:  INVALID)\n");
			    push( @invalid_service_directives,
" ${directive}\[$defined_check_iteration\]_${defined_check_iteration_command} value '$ref_config->{$directive}[$defined_check_iteration]{Enable}' is not valid -- must be on/off "
			    );
			}
			else {
			    gdma_debug(" value:  valid)\n");
			}
		    }

		    # Check service _Service or _Command directives -- value must be non-empty string -- might want to tighten that up later
		    # -------------------------------
		    elsif ( $defined_check_iteration_command =~ /^(Service|Command)$/ ) {
			if ( $ref_config->{$directive}[$defined_check_iteration]{$defined_check_iteration_command} !~ /^.+$/i ) {
			    gdma_debug(" value:  INVALID)\n");
			    push( @invalid_service_directives,
" ${directive}\[$defined_check_iteration\]_${defined_check_iteration_command} value is not valid -- must be a non-empty string "
			    );
			}
			else {
			    gdma_debug(" value:  valid)\n");
			}
		    }

		    # Check service _Check_Interval -- value must be an unsigned non-zero positive integer
		    # -------------------------------
		    elsif ( $defined_check_iteration_command =~ /^Check_Interval$/ ) {
			if ( $ref_config->{$directive}[$defined_check_iteration]{Check_Interval} !~ /^[1-9][0-9]*$/ ) {
			    gdma_debug(" value:  INVALID)\n");
			    push( @invalid_service_directives,
" ${directive}\[$defined_check_iteration\]_${defined_check_iteration_command} value '$ref_config->{$directive}[$defined_check_iteration]{$defined_check_iteration_command}' is not valid -- must be an unsigned non-zero positive integer "
			    );
			}
			else {
			    gdma_debug(" value:  valid)\n");
			}
		    }

		    # Check service _Timeout -- value must be an unsigned non-zero positive integer
		    # -------------------------------
		    elsif ( $defined_check_iteration_command =~ /^Timeout$/ ) {
			if ( $ref_config->{$directive}[$defined_check_iteration]{Timeout} !~ /^[1-9][0-9]*$/ ) {
			    gdma_debug(" value:  INVALID)\n");
			    push( @invalid_service_directives,
" ${directive}\[$defined_check_iteration\]_${defined_check_iteration_command} value '$ref_config->{$directive}[$defined_check_iteration]{$defined_check_iteration_command}' is not valid -- must be an unsigned non-zero positive integer "
			    );
			}
			else {
			    gdma_debug(" value:  valid)\n");
			}
		    }

		    # Add new service sub directive validation checks here
		    # ----------------------------------------------------
		    # <new service sub directive validation code block>

		    # unrecognized sub directive catchall
		    # -----------------------------------
		    else {
			push( @invalid_service_directives,
" Internal error -- ${directive}\[$defined_check_iteration\]_${defined_check_iteration_command} does not have any validation check defined "
			);
		    }
		}
	    }    # end looping over set of sub directives in the iteration set
	}    # end looping over the service check iteration as a whole
    }    # end looping over the directives in the config

    # put any internal errors into the error stack
    if ( $#directives_without_regexes > -1 ) {
	@all_validation_errors = ( @all_validation_errors, "   The following internal errors were detected:  ", @directives_without_regexes );
    }

    # put any invalid directive value structures into the error stack
    if ( $#invalid_value_structures > -1 ) {
	@all_validation_errors = (
	    @all_validation_errors, "   The following invalid non-service-related directive value structures were detected:  ",
	    @invalid_value_structures
	);
    }

    # put any invalid range values into the error stack
    if ( $#invalid_directive_ranges > -1 ) {
	@all_validation_errors = (
	    @all_validation_errors, "   The following non-service-related directive values were detected to be out of range:  ",
	    @invalid_directive_ranges
	);
    }

    # put any service related problems in the error stack
    if ( $#invalid_service_directives > -1 ) {
	@all_validation_errors =
	  ( @all_validation_errors, "  The following service related directive issues were detected:  ", @invalid_service_directives );
    }
    if ( $#missing_service_subdirectives > -1 ) {
	@all_validation_errors =
	  ( @all_validation_errors, "  The following required service sub directives were not defined:  ", @missing_service_subdirectives );
    }

    # report back with all of the errors found
    if ( $#all_validation_errors > -1 ) {
	$$ref_errstr = "Config validation:  Errors occurred during validation:  @all_validation_errors";
	return 0;
    }

    return 1;    # If here, then all went ok
}

################################################################################
#   spool_results()
#
#   Writes the data in buffer passed, to spool file.  Each line in the resultant
#   spool file is assumed to be a result.
#
#   The write operation is wrapped with a file lock, the obtaining of which is
#   blocking if the $blocking argument is 1.  If the lock cannot be obtained,
#   the spooling does not take place, in which case it is up to the caller to
#   decide what to do with the unspooled data.  If writing to the spool file is
#   completely successful, the buffer is purged.
#
#   Returns 1 on success, 0 otherwise.
#
#   Arguments:
#   $spool_filename - Spool file name.
#   $buf - A reference to the buffer containing data to be spooled.
#   $blocking - A flag indicating whether write to spool file should be
#               blocking.
#   $num_results - A reference to a variable where the number of results
#                  successfully spooled will be written.
#   $errstr - A reference to an error string variable.  This will be
#             updated according to error encountered, if any, indicated by "0"
#             return value.
#
################################################################################
# FIX LATER:  We should provide a reasonable timeout on obtaining the lock,
# rather than just a "blocking" flag.
sub spool_results {
    my ( $spool_filename, $buf, $blocking, $num_results, $errstr ) = @_;
    my $spool_fh;
    my $ret_val = 1;

    $$errstr      = "OK";
    $$num_results = 0;

    if ( scalar(@$buf) == 0 ) {
	$$errstr = "Nothing to be spooled";

	# Make the caller take a note of it, even though it is not a
	# catastrophic condition.
	return 0;
    }

    # Open the spool file in append mode.
    if ( open( $spool_fh, '>>', $spool_filename ) ) {
	my $spooled = undef;
	## Check if we are supposed to get a blocking lock.
	if ($blocking) {
	    $ret_val = get_lock($spool_fh);
	}
	else {
	    $ret_val = try_lock($spool_fh);
	}
	if ($ret_val) {
	    ## We got the lock.
	    if ( print $spool_fh @$buf ) {
		## This setting is provisional.  In fact, possibly no actual
		## file writes have occurred yet; we may have only copied the
		## data to the Perl i/o buffer, which will be flushed during
		## a subsequent close() operation.  Or maybe a write to the
		## kernel has successfully occurred, but a later filesystem or
		## media problem may invalidate the write to the filesystem.
		$spooled = 1;
	    }
	    else {
		$errstr  = "Write to spoolfile $spool_filename failed ($!).";
		$ret_val = 0;
		$spooled = 0;
	    }

	    # Relinquish the lock.
	    if ( !release_lock( $spool_fh ) ) {
		$$errstr = "Could not relinquish the spool file lock:  $!.";
		$ret_val = 0;
	    }
	}
	else {
	    ## Log error and move on.
	    $$errstr = "Could not acquire the spool file lock.";
	    $ret_val = 0;
	}

	# Close the spool file.  This may do some writing from the Perl i/o buffers,
	# so we have to check its return value to see if everything succeeded.
	if (not close $spool_fh) {
	    $spooled = 0;
	}
	if ($spooled) {
	    ## Record the number of checks processed.
	    $$num_results = scalar(@$buf);

	    # We successfully wrote the results to the spool file; purge the buffer.
	    @$buf = ();
	    gdma_debug("Flushed $$num_results result".($$num_results == 1 ? '' : 's')." to the spool file.\n");
	}
    }
    else {
	## Log error and move on.
	$$errstr = "Could not open the spool file:  $!.";
	$ret_val = 0;
    }
    return $ret_val;
}

################################################################################
#   get_spool_filename()
#
#   Returns the spool file name based on the name of the OS we're running under.
#   Arguments:
#   $my_headpath - Head path for the set-up.
#   $priority - Optional parameter:  if true, return the path to the
#               priority-queue file instead of the path to the normal-queue file.
#
################################################################################
sub get_spool_filename {
    my $my_headpath = shift;
    my $priority    = shift;
    my $spool_file  = "NULL";
    my $osname      = $^O;

    my $priority_extension = $priority ? '_priority' : '';

    if ( ( $osname eq "linux" ) or ( $osname eq "solaris" ) or ( $osname eq "aix" ) or ( $osname eq "hpux" ) ) {
	$spool_file = "$my_headpath/spool/gdma$priority_extension.spool";
    }
    elsif ( $osname eq "MSWin32" ) {
	$spool_file = "$my_headpath\\spool\\gdma$priority_extension.spool";
    }
    return $spool_file;
}

################################################################################
#   try_lock()
#
#   Tries to get a non-blocking lock on the filehandle passed. If it fails,
#   tries it once more after sleeping for a second. Returns true on success,
#   false on failure.
#
#   Arguments:
#   $fh - The file handle to attempt a lock on.
#
################################################################################
sub try_lock {
    my $fh        = shift;
    my $ret_value = 1;

    # Try to get an exclusive lock, without blocking if it is not immediately available.
    # FIX LATER:  Compare to POSIX locks.
    $ret_value = flock( $fh, LOCK_EX | LOCK_NB );

    if ( !$ret_value ) {
	## Wait for a while and try again.
	sleep 1;
	$ret_value = flock( $fh, LOCK_EX | LOCK_NB );
    }

    # Whether or not we were successful.
    return $ret_value;
}

################################################################################
#   get_lock()
#
#   Blocking attempt to get a lock on the filehandle passed.
#   Returns true on success, false on failure.
#
#   Arguments:
#   $fh - The file handle to attempt a lock on.
#
################################################################################
sub get_lock {
    my $fh = shift;

    # Get an exclusive lock.  Block until you get it (or fail attempting to do so).
    return flock( $fh, LOCK_EX );
}

################################################################################
#   release_lock()
#
#   Releases a blocking lock on the filehandle passed.
#   Returns true on success, false on failure.
#
#   Arguments:
#   $fh - The file handle to release a lock on.
#
################################################################################
sub release_lock {
    my $fh = shift;

    # Try to release an existing lock.
    return flock( $fh, LOCK_UN );
}

################################################################################
#   get_current_time_str()
#
#   Returns the current time in human readable format.
#   Uses perl function localtime().
#
################################################################################
sub get_current_time_str {
    my ( $sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $isdst ) = localtime(time);
    $year += 1900;
    my $month = qw(January February March April May June July August September October November December) [$mon];
    my $thisday = qw(Sunday Monday Tuesday Wednesday Thursday Friday Saturday) [$wday];

    my $timestring = sprintf "%s %s %d %02d:%02d:%02d %d", $thisday, $month, $mday, $hour, $min, $sec, $year;
    return $timestring;
}

################################################################################
#   load_hires_timer()
#
#   Checks if Time::HiRes is available. It may not be installed on all
#   the platforms. So we build a portable mechanism for finding time
#   to whatever resolution is available. Returns time function and
#   format.
#
################################################################################
sub load_hires_timer {
    my ( $hires_time, $hires_time_format );
    eval { require Time::HiRes; import Time::HiRes; };
    if ($@) {
	## 'require' died; Time::HiRes is not available.
	$hires_time = sub { return time; };
	$hires_time_format = "%0.0f";
    }
    else {
	## 'require' succeeded; Time::HiRes was loaded.
	$hires_time = sub { return Time::HiRes::time(); };
	$hires_time_format = "%0.3f";
    }

    return ( $hires_time, $hires_time_format );
}

################################################################################
#   get_config_lock_filename()
#
#   Returns the full pathname of the configuration update lock file,
#   based on the platform we are running on.
#
#   $head_path - Installation directory for the agent.
#
################################################################################
sub get_config_lock_filename {
    my $head_path        = shift;
    my $config_lock_file = "NULL";
    my $osname           = $^O;

    if ( ( $osname eq "linux" ) or ( $osname eq "solaris" ) or ( $osname eq "aix" ) or ( $osname eq "hpux" ) ) {
	$config_lock_file = "$head_path/tmp/config.lock";
    }
    elsif ( $osname eq "MSWin32" ) {
	$config_lock_file = "$head_path\\tmp\\config.lock";
    }

    return $config_lock_file;
}

################################################################################
#   my_hostname()
#
#   Finds the form of hostname we should use.
#
#   $long_name_option - The configured Use_Long_Hostname value, if any.
#   $forced_hostname  - The configured Forced_Hostname value, if any.
#   $lowercase_option - The configured Use_Lowercase_Hostname value, if any.
#
################################################################################
sub my_hostname {
    my $long_name_option = shift;
    my $forced_hostname  = shift;
    my $lowercase_option = shift;
    my $hostname;

    # The Forced_Hostname option, if specified, overrides all dynamic determination
    # of the hostname by the program.  To be used sparingly, only in exceptional
    # conditions when the local configuration simply cannot be set up to correctly
    # determine the hostname by ordinary means.
    if (defined($forced_hostname) && $forced_hostname ne '') {
	$hostname = $forced_hostname;
    }
    ## Use FQDN name if user has configured 'Use_Long_Hostname'.
    elsif (defined($long_name_option) && $long_name_option =~ /on/i) {
	$hostname = hostfqdn();
    }
    else {
	$hostname = hostname();
	$hostname =~ s/\..*//;  # force into shortname form
    }
    # Historically, the GWM application assigned only lowercase names for hosts.
    # That is no longer forced, so it is optional here.
    return ( defined($lowercase_option) && $lowercase_option =~ /on/i ) ? lc($hostname) : $hostname;
}

################################################################################
# Routines to convert between an arbitrary forced hostname and a shell-safe
# form of such a hostname.
#
# The "safe" characters are exactly those that are allowed in a valid 
# hostname, plus underscore (for simple clarity, because it is known to
# be safe [is acceptable on both UNIX and Windows platforms, and is not a
# shell metacharacter] and because it can make displaynames more readable).
#
# We would like to completely lowercase the safename, but it would then not
# be possible in the general case to reverse that transform to recover the
# original unsafename from the safename.
################################################################################

sub to_safename {
    my $name = shift;
    utf8::encode $name;
    $name =~ s/([^-._A-Za-z0-9])/ sprintf( "+%02X", ord($1) ) /eg;
    return $name;
}

sub from_safename {
    my $name = shift;
    $name =~ s/\+([0-9A-F]{2})/ chr(hex($1)) /eg;
    utf8::decode $name;
    return $name;
}

1;

