package TestDBCursesUI;
use lib qw(../);
use GWInstaller::UI::DBCursesUI;
use GWTest::GWTest;
 
@ISA = qw(GWTest);

 


sub test_configure_db_dialog{
	return GWInstaller::UI::DBCursesUI::configure_db_dialog();
}

 
1;