package TestNMSCursesUI;
use lib qw(../);
use GWNMSInstaller::UI::NMSCursesUI;
use GWTest::GWTest;

@ISA = qw(GWTest);

 


sub test_configure_component_dialog{
	return GWNMSInstaller::UI::NMSCursesUI::configure_component_dialog();
}

sub test_configure_GWM_dialog{
	return GWNMSInstaller::UI::NMSCursesUI::configure_GWM_dialog();
}
1;