#!/usr/bin/perl

package TestProperties;
use lib qw(../);
use GWInstaller::AL::Properties;
use GWTest::GWTest;

@ISA = qw(GWTest);

sub test_init{
        $pass = 0;
        $prop = GWInstaller::AL::Properties->new("/tmp/enterprise.properties");
        $prop->isa(GWInstaller::AL::Properties)?($pass = 1):($pass = 0);
        return $pass;
}

sub test_get_property{
   $prop = GWInstaller::AL::Properties->new("/tmp/enterprise.properties");
   $prop->set_property("system.database.mysql_parent.type","mysql");
   $result = $prop->get_property("system.database.mysql_parent.type");
   ($result eq "mysql")?($pass = 1):($pass = 0);
   return $pass;
}

sub test_set_property{
   $prop = GWInstaller::AL::Properties->new("/tmp/enterprise.properties");
   $prop->read_properties();
   ($prop->set_property("system.database.mysql_parent.port", 3433))?($pass = 1):($pass = 0);
   return $pass;
}

sub test_read_properties{
	$retVal = 0 ;
	$orig = 3433;
   	$prop = GWInstaller::AL::Properties->new("/tmp/dp.properties");
   	$prop->set_property("system.database.mysql_parent.port", $orig);
   	$prop->write_properties();
   
  	$otherprop = GWInstaller::AL::Properties->new("/tmp/dp.properties");
    $val = $otherprop->get_property("system.database.mysql_parent.port");
    if($val == $orig){
    	$retVal = 1;
    }
    
    return $retVal;
}

sub test_write_properties{
	$retVal = 0 ;
	$orig = 3433;
   	$prop = GWInstaller::AL::Properties->new("/tmp/dp.properties");
   	$prop->set_property("system.database.mysql_parent.port", $orig);
   	$prop->write_properties();
   
  	$otherprop = GWInstaller::AL::Properties->new("/tmp/dp.properties");
    $val = $otherprop->get_property("system.database.mysql_parent.port");
    if($val == $orig){
    	$retVal = 1;
    }
    
    return $retVal;
    
}

sub test_update_property{
	$retVal = 0;
	$original = "one";
	$prop = GWInstaller::AL::Properties->new("/tmp/enterprise.properties");
	$prop->set_property("click",$original);
	$prop->update_property("click","clack");
	$result = $prop->get_property("clack");
	if($result eq $original){
		$retVal = 1;
	}
	return $retVal;
}
sub test_set_JDBCURL{
	
	$retVal = 0;
	
	$dbDriverName = "mysql";
	$hostName = "localhost";
	$dbName = "GWCollageDB";
	$propName= "jdbcURL";
	@myArgs = ($propName,$dbDriverName,$hostName,$dbName);
	$expected = "jdbc:". $dbDriverName ."://". $hostName ."/". $dbName;
	$prop = GWInstaller::AL::Properties->new("/tmp/enterprise.properties");
	$valid = $prop->set_JDBCURL(@myArgs);
	
	$propRef = $prop->{properties};
	$properties = %$propRef;
	$result = $properties->{jdbcURL};

	if( ($expected eq $result) && ($valid)){
		$retVal = 1;
	}
	
	return $retVal;
		

}

1;