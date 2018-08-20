package TestGWNMS;
use lib qw(../);
use GWNMSInstaller::AL::GWNMS;
use GWInstaller::AL::Host;
use GWTest::GWTest;
use GWNMSInstaller::AL::ntop;
use GWNMSInstaller::AL::Weathermap;
use GWNMSInstaller::AL::Cacti;
use GWNMSInstaller::AL::NeDi;

@ISA = qw(GWTest);

sub test_init{
	$pass = 0;
	$prop = GWNMSInstaller::AL::GWNMS->new();
	$prop->isa(GWNMSInstaller::AL::GWNMS)?($pass = 1):($pass = 0);
	return $pass;
}


sub test_get_host{
	$pass = 0;
	$prop = GWNMSInstaller::AL::GWNMS->new();
	$host = $prop->get_host();
	($host->isa(GWInstaller::AL::Host))?($pass = 1):($pass = 0);
	#print "HOST: " . $host->get_hostname();
	return $pass;
	
}

sub test_get_installed_components{
	$pass = 0;
	$nms = GWNMSInstaller::AL::GWNMS->new();
	@componentArray = $nms->get_installed_components();
	if((scalar(@componentArray) == 1) || (scalar(@componentArray) == 0)){
		$pass = 1;
	}
	 return $pass;
}

sub test_get_available_components{
	$pass = 0;
	$nms = GWNMSInstaller::AL::GWNMS->new();
	@componentArray = $nms->get_available_components();
	if((scalar(@componentArray) == 1) || (scalar(@componentArray) == 0)){
		$pass = 1;
	}
	 return $pass;
	
}

1;