package TestSoftwareCursesUI;
use lib qw(../);
use GWInstaller::UI::SoftwareCursesUI;
use GWTest::GWTest;

@ISA = qw(GWTest);

 


sub test_available_packages_dialog{
	return GWInstaller::UI::SoftwareCursesUI::available_packages_dialog();
}

 
1;