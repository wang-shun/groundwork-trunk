#!/usr/bin/perl 
#
# Addresses GWMON-4880
#
$user = `whoami`;
chomp($user);
unless($user eq "root"){
	print "You must be root to run this script.\n";
    exit(1);
}
  






#write out patch
open(PATCH,">installer-patch-5.2.0");
while(<DATA>){
	print PATCH $_;
}
close(PATCH);

#execute patch
print `patch -p1 < installer-patch-5.2.0`;

__DATA__
diff -Naur groundwork-installer/conf/installer.properties groundwork-installer.new/conf/installer.properties
--- groundwork-installer/conf/installer.properties	2008-03-18 16:17:18.000000000 -0700
+++ groundwork-installer.new/conf/installer.properties	2008-04-15 10:10:19.000000000 -0700
@@ -34,7 +34,7 @@
 Section "Prerequisite"
 	name=Sun Java JDK
 	rpm_name=jdk
-	valid_version=1.5.0_06
+	valid_version=1.5
 	version_command=java -version 2>&1 | head -1 | sed s/'java version '// | sed s/\"//g | grep -v 'command not found'
 EndSection
 
diff -Naur groundwork-installer/lib/GWInstaller/Dialogs.pm groundwork-installer.new/lib/GWInstaller/Dialogs.pm
--- groundwork-installer/lib/GWInstaller/Dialogs.pm	2008-03-18 16:17:19.000000000 -0700
+++ groundwork-installer.new/lib/GWInstaller/Dialogs.pm	2008-04-16 10:34:34.000000000 -0700
@@ -1,6 +1,6 @@
 #!/usr/bin/perl
 #
-#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
+#Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")
 #All rights reserved. Use is subject to GroundWork commercial license terms. 
 #
 
@@ -198,7 +198,7 @@
 	     	prereq_dialog($pk,$cui);
 	     }
 	     GWLogger::log("Package is correct");
-	     if( ($rpm_ver ne $pk->{'valid_version'}) && ($pk->{'valid_version'} ne "ANY")){
+	     if( !($rpm_ver =~ $pk->{'valid_version'}) && ($pk->{'valid_version'} ne "ANY")){
 	     		$msg = "The package $justrpm is version $rpm_ver. You need to select a package containing version $pk->{'valid_version'} of $pk->{'rpm_name'} ";
 	     		GWCursesUI::error_msg($msg,$cui);
 	     		prereq_dialog($pk,$cui);
diff -Naur groundwork-installer/lib/GWInstaller/GWMonitor.pm groundwork-installer.new/lib/GWInstaller/GWMonitor.pm
--- groundwork-installer/lib/GWInstaller/GWMonitor.pm	2008-03-18 16:17:19.000000000 -0700
+++ groundwork-installer.new/lib/GWInstaller/GWMonitor.pm	2008-04-16 10:34:34.000000000 -0700
@@ -1,6 +1,6 @@
 #!/usr/bin/perl
 #
-#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
+#Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")
 #All rights reserved. Use is subject to GroundWork commercial license terms. 
 #
 
@@ -8,7 +8,8 @@
  
 use GWLogger;
 use GWInstaller::Host;
- 
+#use GWDB;
+
 sub new{
 	 
 	my ($invocant,$software_class,$version) = @_;
@@ -31,6 +32,28 @@
 	
 }
 
+sub update_guava_menu{
+  	GWDB::init("guava");
+  	$selectViewQuery = "SELECT view_id " . 
+  					   "FROM guava_views " .
+  					   "ORDER BY viewname";
+  					    
+  	$sth = GWDB::executeQuery($selectViewQuery);
+    $sth->bind_col(1,\$view_id);
+    $cnt = 1;
+    while ($sth->fetch()){
+	
+	    $updateRoleViewQuery =  "UPDATE guava_roleviews" . 
+    							"SET vieworder=$cnt" . 
+    							"WHERE view_id=$view_id";
+		$cnt++;
+		$sth = GWDB::executeQuery($updateRoleViewQuery);
+		$sth->finish();
+		
+	    }
+    $sth->finish();	
+}
+
 sub get_os_status{
   	$prefs = GWInstaller::Prefs->new();
 	$prefs->load_software_prefs();
@@ -171,16 +194,20 @@
 	   (GWInstaller::Host::is_rpm_installed("groundwork-monitor-core")) &&
 	   (GWInstaller::Host::is_rpm_installed("groundwork-monitor-pro"))
 	   ){
-	   	$self->{version} = "Pro";
+	   	$self->{software_class} = "Pro";
 	    $retval = 1;
 	   }
 	elsif((GWInstaller::Host::is_rpm_installed("groundwork-foundation-pro")) && 
 	      (GWInstaller::Host::is_rpm_installed("groundwork-monitor-core"))
 	      ){
-	      	$self->{version} = "Open Source";
+	      	$self->{software_class} = "Open Source";
 	      	$retval = 1;
 	      }
 	     
+	 if($retval){
+	 	my	$version = `rpm -qi groundwork-monitor-core | grep Version | sed s/' '//g | sed s/Version:// | sed s/Vendor.*//`;
+		$self->{version} = $version;
+	}
 	return $retval;
 	
 }
diff -Naur groundwork-installer/lib/GWInstaller/Host.pm groundwork-installer.new/lib/GWInstaller/Host.pm
--- groundwork-installer/lib/GWInstaller/Host.pm	2008-03-18 16:17:19.000000000 -0700
+++ groundwork-installer.new/lib/GWInstaller/Host.pm	2008-04-16 10:34:34.000000000 -0700
@@ -1,6 +1,6 @@
 #!/usr/bin/perl
 #
-#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
+#Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")
 #All rights reserved. Use is subject to GroundWork commercial license terms. 
 #
 
@@ -8,6 +8,7 @@
 
 use GWInstaller::FileSystem;
 use GWCursesUI;
+use GWInstaller::Prefs;
 
 sub new{
 	 
@@ -246,17 +247,17 @@
 	open(HOSTS,"/etc/hosts");
 	open(HOST_TEMP,">/tmp/gw.hosttemp");
 	
-	#replace existing loopback entry with compliant entry
-	while(<HOSTS>){
-		$line = $_;
-		if($line =~ '127.0.0.1'){
-			print HOST_TEMP "127.0.0.1\tlocalhost\n";
-		}
-		else{
-			print HOST_TEMP $line;
-		}
-	}
-	
+#	#replace existing loopback entry with compliant entry
+#	while(<HOSTS>){
+#		$line = $_;
+#		if($line =~ '127.0.0.1'){
+#			print HOST_TEMP "127.0.0.1\tlocalhost\n";
+#		}
+#		else{
+#			print HOST_TEMP $line;
+#		}
+#	}
+#	
  
 	#if loopback missing, add an entry
 	$hasLoopback = `grep -c 127.0.0.1 /tmp/gw.hosttemp`;
@@ -282,6 +283,8 @@
 }
 
 sub fix_java_config{
+ 	$prefs = GWInstaller::Prefs->new();
+	$prefs->load_software_prefs();
 	$fixRef = shift;
 #	@to_fix = shift;
 	@to_fix = @{$fixRef};
@@ -293,14 +296,14 @@
 		if($fix eq "profile"){
 		
 			open(PROFILE,">>/etc/profile");
-			print PROFILE "JAVA_HOME=/usr/java/jdk1.5.0_06\n";
+			print PROFILE "JAVA_HOME=$prefs->{java_home}\n";
 			print PROFILE "export JAVA_HOME\n";
 			close(PROFILE);
 			
 			}
 		elsif($fix eq "bashrc"){
 			open(BASHRC,">>/etc/bash.bashrc.local");
-			print BASHRC "JAVA_HOME=/usr/java/jdk1.5.0_06\n";
+			print BASHRC "JAVA_HOME=$prefs->{java_home}\n";
 			print BASHRC "export JAVA_HOME\n";
 			close(BASHRC);
 			}
@@ -309,14 +312,15 @@
 			print `ln -s /etc/alternatives/java /usr/bin/java 2>/dev/null`;
 			} 
 		elsif($fix eq "env"){
-			$ENV{'JAVA_HOME'} = "/usr/java/jdk1.5.0_06";
+			$ENV{'JAVA_HOME'} = "$prefs->{java_home}";
 			print `source /etc/profile`;
 			print `source /etc/bash.bashrc.local`;
 			}
 		
 		elsif($fix eq "alternatives"){
 			print `rm -rf /etc/alternatives/java 2>/dev/null`;
-			print `ln -s /usr/java/jdk1.5.0_06/bin/java /etc/alternatives/java 2>/dev/null`;
+			$linkCommand = "ln -s " .  $prefs->{java_home} . "/bin/java /etc/alternatives/java 2>/dev/null";
+			print `$linkCommand`;
 			}
 			
 		} #end foreach
@@ -429,7 +433,7 @@
 
 #todo verifying software configs should be loadable modules
 sub verify_java_config{
-	
+	$my_java_home = GWInstaller::Prefs::get_java_home();
 	$is_redhat = (-e "/etc/redhat-release");
 	$is_suse = (-e "/etc/SuSE-release");
 	
@@ -450,7 +454,7 @@
 	#verify /etc/alternatives/java link
 	if($debug){GWLogger::log("\tverifying /etc/alternatives/java link");}
 	$altlinkval = readlink("/etc/alternatives/java");
-	$java_bin = $prefs->{java_home} . "/bin/java";
+	$java_bin = $my_java_home . "/bin/java";
 	#GWLogger::log("alt: $altlinkval bin: $java_bin");
 	unless($altlinkval eq $java_bin){
 		if($debug){GWLogger::log("\t>>broken alternatives<<");}
@@ -476,7 +480,7 @@
 	chomp($BASHRC_JAVA_HOME);
 #	GWLogger::log("bashrc: $BASHRC_JAVA_HOME");
 	
-	unless( ($PROFILE_LOCAL_JAVA_HOME eq $prefs->{java_home}) || ($PROFILE_JAVA_HOME eq $prefs->{java_home}) || ($BASHRC_JAVA_HOME eq $prefs->{java_home}) ) {
+	unless( ($PROFILE_LOCAL_JAVA_HOME eq $my_java_home) || ($PROFILE_JAVA_HOME eq $my_java_home) || ($BASHRC_JAVA_HOME eq $my_java_home) ) {
 		if($debug){GWLogger::log("\t>>broken profile<<");}
 		push(@to_fix,"profile");
 	}
@@ -486,7 +490,7 @@
 	#verify that JAVA_HOME is set in ENVIRONMENT
 	if($debug){GWLogger::log("\tverifying JAVA_HOME");}
  
-	unless($ENV{'JAVA_HOME'} eq $prefs->{java_home}){
+	unless($ENV{'JAVA_HOME'} eq $my_java_home){
 		if($debug){GWLogger::log("\t>>broken env<<");} 
 		push(@to_fix,"env");
 	}
@@ -657,9 +661,10 @@
 
 
      unless($pkg eq ""){
-        GWLogger::log("checking if $pkg installed $command");
-        $is_installed = `rpm -qa $pkg 2> /dev/null | wc -l`;
-	    chomp($is_installed);
+        	$is_installed = `rpm -qa $pkg 2> /dev/null | wc -l`;
+	    	chomp($is_installed);
+        	if($is_installed){GWLogger::log("$pkg is INSTALLED.");}
+        	else{GWLogger::log("$pkg is NOT installed")}
          }
     return $is_installed;
 }
diff -Naur groundwork-installer/lib/GWInstaller/Prefs.pm groundwork-installer.new/lib/GWInstaller/Prefs.pm
--- groundwork-installer/lib/GWInstaller/Prefs.pm	2008-03-18 16:17:19.000000000 -0700
+++ groundwork-installer.new/lib/GWInstaller/Prefs.pm	2008-04-16 10:34:34.000000000 -0700
@@ -1,6 +1,6 @@
 #!/usr/bin/perl
 #
-#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
+#Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")
 #All rights reserved. Use is subject to GroundWork commercial license terms. 
 #
 #TODO install logging
@@ -78,8 +78,15 @@
 }
 
 sub get_java_home{
-	$home = "/usr/java/jdk1.5.0_06";
-#	$home_cmd = "find / -name ".  $self->{'java_rpm'} . " | head -1";
+	$home_cmd = "find / -type d -name jdk* | head -1";
+	$home = `$home_cmd`;
+	chomp($home);
+	
+	unless($home){
+		$home = $ENV{JAVA_HOME} if($ENV{JAVA_HOME});
+	}
+	
+	
 	return $home;	
 }
 
diff -Naur groundwork-installer/lib/GWInstaller/Software.pm groundwork-installer.new/lib/GWInstaller/Software.pm
--- groundwork-installer/lib/GWInstaller/Software.pm	2008-03-18 16:17:19.000000000 -0700
+++ groundwork-installer.new/lib/GWInstaller/Software.pm	2008-04-16 10:34:34.000000000 -0700
@@ -1,6 +1,6 @@
 #!/usr/bin/perl
 #
-#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
+#Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")
 #All rights reserved. Use is subject to GroundWork commercial license terms. 
 #
 
@@ -37,7 +37,7 @@
      }
      $other = `$command`;
      chomp($other);
-     if(($other ne "") && ($self->{'valid_version'} ne "") &&  ($other =~ /$self->{'valid_version'}/) ){
+     if(($other ne "") && ($self->{'valid_version'} ne "") &&  (($other =~ /$self->{'valid_version'}/) || ($self->{'valid_version'} eq "ANY" ))){
      	$other_installed = 1;
      }
      
__DATA__
