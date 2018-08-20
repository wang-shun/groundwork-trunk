package TestNeDi;
use lib qw(../);
use GWNMSInstaller::AL::NeDi;
use GWTest::GWTest;
use GWInstaller::AL::Host;

@ISA = qw(GWTest);

sub test_init{
	$pass = 0;
	$prop = GWNMSInstaller::AL::NeDi->new();
	$prop->isa(GWNMSInstaller::AL::NeDi)?($pass = 1):($pass = 0);
	return $pass;
}

sub test_configure{
	$pass = 0;
	$prop = GWNMSInstaller::AL::Cacti->new();
	$configVal = $prop->configure();
	if(($configVal == 0) || ($configVal == 1)){
		$pass = 1;
	}
	return $pass;
}

sub test_deploy{
	$cacti = GWNMSInstaller::AL::Cacti->new();
	return  $cacti->deploy();
}

sub test_get_GWM_host{
	$pass = 0;

	$cacti = GWNMSInstaller::AL::Cacti->new();
	$myHost = GWInstaller::AL::Host->new("localhost");
	$cacti->set_GWM_host($myHost);

	$host = $cacti->get_GWM_host();
	if($host){ 
	if($host->isa(GWInstaller::AL::Host)){
		$pass = 1;
		} 
	}
	return $pass;
}

sub test_set_GWM_host{
	$pass = 0;
	
	$cacti = GWNMSInstaller::AL::Cacti->new();
	$myHost = GWInstaller::AL::Host->new("localhost");
	$cacti->set_GWM_host($myHost);

	$host = $cacti->get_GWM_host();
	if($host){ 
	if($host->isa(GWInstaller::AL::Host)){
		$pass = 1;
	} 
	}
	return $pass;
}
1;