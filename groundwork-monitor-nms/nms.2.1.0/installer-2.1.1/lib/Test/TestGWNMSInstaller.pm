package TestGWNMSInstaller;
use lib qw(../);
use GWNMSInstaller::GWNMSInstaller;
use GWTest::GWTest;

@ISA = qw(GWTest);


sub test_init{
	#return -1;
	$pass = 0;
	$prop = GWNMSInstaller::GWNMSInstaller->new();
	$prop->isa(GWNMSInstaller::GWNMSInstaller)?($pass = 1):($pass = 0);
	return $pass;
}



sub test_scan_for_showstoppers{
	$pass = 0;
	$installer = GWNMSInstaller::GWNMSInstaller->new();
	if(	$installer->isa(GWNMSInstaller::GWNMSInstaller)){ 
		$pass = $installer->scan_for_showstoppers();
	}
	return $pass;

}

sub test_verify_environment{
	$pass = 0;
	$installer = GWNMSInstaller::GWNMSInstaller->new();
	if(	$installer->isa(GWNMSInstaller::GWNMSInstaller)){ 
		$pass = $installer->verify_environment();
	}
	return $pass;
}

sub test_verify_install{
 	$pass = 0;
	$nms =  GWNMSInstaller::GWNMSInstaller->new();
	if(	$nms->isa(GWNMSInstaller::GWNMSInstaller)){ 
		$nms->init();
		$val = $nms->verify_install();
		if($val == 0 || $val == 1){
		 	$pass = 1;
		 }
	}
	 return $pass;
}

1;