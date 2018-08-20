<?php
class MonarchMainView extends View {
	private $context;
	
	function __construct() {
		$this->addMenuItem("Services", "services");
		$this->addMenuItem("Profiles", "profiles");
		$this->addMenuItem("Hosts", "hosts");
		$this->addMenuItem("Contacts", "contacts");
		$this->addMenuItem("Escalations", "escalations");
		$this->addMenuItem("Commands", "commands");
		$this->addMenuItem("Time Periods", "time_periods");
		$this->addMenuItem("Control", "control");
		$this->addMenuItem("Tools", "tools");
	//	$this->addMenuItem("Help", "help");
		$this->context = "hosts";
		parent::__construct("Monarch");
	}
	
	public function menuCommand($command) {
		switch($command) {
			case 'time_periods':
				$this->context = 'time_periods';
				break;
			case 'commands':
				$this->context = 'commands';
				break;
			case 'contacts':
				$this->context = 'contacts';
				break;
			case 'escalations':
				$this->context = 'escalations';
				break;
			case 'hosts':
				$this->context = 'hosts';
				break;
			case 'services':
				$this->context = 'services';
				break;
			case 'profiles':
				$this->context = 'profiles';
				break;
			case 'tools':
				$this->context = 'tools';
				break;
			case 'control':
				$this->context = 'control';
				break;
			case 'help':
				$this->context = 'help';
				break;
			
		}
	}
	
	public function init() {
  		global $guava;
                // Let's get our single sign-on configuration
                $ssouser = $guava->getpreference('monarch', 'ssouser');
                $ssoenable = $guava->getpreference('monarch', 'ssoenable');
                if($ssoenable) {
                        // Create our auth ticket
                        $guava->SSO_createAuthTicket("monarch_auth_tkt", "/monarch", $ssouser);
		}
	}
	
	public function close() {
		// No need to do anything
	}
	
	function render() {
		switch($this->context) {
			case 'time_periods':
			case 'commands':
			case 'contacts':
			case 'escalations':
			case 'services':
			case 'hosts':
			case 'profiles':
			case 'control':
			case 'tools':
				header("Location: /monarch/cgi-bin/monarch.cgi?user_acct=super_user&top_menu=".$this->context);
				break;
			case 'help':
				header("Location: /monarch/doc/index.html");
				break;
		}
	}
	
	public function refresh() {}
	
	public function sideNavValue($command) {}
}
?>
