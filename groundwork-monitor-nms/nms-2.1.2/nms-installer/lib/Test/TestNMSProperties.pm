package TestNMSProperties;
use lib qw(../);
use GWNMSInstaller::AL::NMSProperties;
use GWTest::GWTest;

@ISA = qw(GWTest);

sub test_init{
	$pass = 0;
	$prop = GWNMSInstaller::AL::NMSProperties->new("/tmp/enterprise.properties");
	$prop->isa(GWNMSInstaller::AL::NMSProperties)?($pass = 1):($pass = 0);
	return $pass;
}

sub test_write_properties{
	$retVal = 0 ;
	$orig = 3433;
   	$prop = GWInstaller::AL::Properties->new("/tmp/dp.properties");
   	$prop->set_property("system.database.mysql_parent.port", $orig);
   	$return = $prop->write_properties();
   
  	$otherprop = GWInstaller::AL::Properties->new("/tmp/dp.properties");
    $val = $otherprop->get_property("system.database.mysql_parent.port");
    if( ($val == $orig) && ($return == 1)){
    	$retVal = 1;
    }
    
    return $retVal;
}


sub test_is_valid{
	$pass = 0;
	$prop = GWNMSInstaller::AL::NMSProperties->new("/tmp/enterprise.properties");
	$prop->set_property("testproperty","testvalue");
	$prop->write_properties();
	$validity = $prop->is_valid();
	if($validity == 0 || $validity == 1){
		$pass = 1;
	}
	return $pass
}
1;