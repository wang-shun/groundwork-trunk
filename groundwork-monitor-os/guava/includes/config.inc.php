<?php
/**
 * This is the configuration file for Guava.
 * Change the define statements to properly represent your configuration.
 *
  * @package guava
 */

/**
 * The following define statements define the web behavior.
 * GUAVA_SYS_NAME		The system name.  This will be shown in the titlebar.
 * GUAVA_WS_ROOT 		The url path to the guava installation.
 * GUAVA_FS_ROOT		Where the files for guava are located on your filesystem.
 * GUAVA_SESSION_SECRET	A secret word to use when initializing session data.
 *
 */
define('GUAVA_SYS_NAME', 'GroundWork Community Edition');
define('GUAVA_WS_ROOT', '/monitor/');
define('GUAVA_FS_ROOT', '/usr/local/groundwork/guava/');
define('GUAVA_THEME_ROOT', GUAVA_FS_ROOT . 'themes/');
define('GUAVA_SESSION_SECRET', 'groundworkmonitoropensource');

/**
 * The following define statements is the configuration to access the guava 
 * database via mysql.
 * GUAVA_DB_ADDRESS		A fqdn or ip address of your mysql server
 * GUAVA_DB_USERNAME	The username to connect as
 * GUAVA_DB_PASSWORD	What password to use to connect
 * GUAVA_DB_DATABASE	What database to connect to
 *
 */
define('GUAVA_DB_TYPE','mysql');
define('GUAVA_DB_ADDRESS', 'localhost');
define('GUAVA_DB_USERNAME', 'guava');
define('GUAVA_DB_PASSWORD', 'gwrk');
define('GUAVA_DB_DATABASE', 'guava');

/**
 * Single Sign-On Support With mod_auth_tkt:
 * The below definitions define if single-sign-on resources should be enabled for 
 * guava applications.  And if so, what the secret key is.  This should match your 
 * TKTAuthSecret string in your apache's configuration file.
 */
define('GUAVA_SSO_ENABLE', true);
define('GUAVA_SSO_SECRET', 'changethistosomethingunique');

/**
 * Database Session Handling Support:
 * The below definitions define if you want to support session storage via 
 * ADODB.  This is faster than using the default php session file storage system.
 * 
 * This is disabled by default.  Uncomment all the lines to enable this functionality.
 */
//$ADODB_SESSION_DRIVER = 'mysql';
//$ADODB_SESSION_CONNECT = 'localhost';
//$ADODB_SESSION_USER = 'root';
//$ADODB_SESSION_PWD = '';
//$ADODB_SESSION_DB = 'guava';
//require_once(GUAVA_FS_ROOT . 'adodb/session/adodb-session.php');



/**
 * Do not modify these require statements.  These include all the necessary classes 
 * to have the framework function properly.
 */

// Database Abstraction Library (ADOdb)
require_once(GUAVA_FS_ROOT . 'adodb/adodb.inc.php');
require_once(GUAVA_FS_ROOT . 'adodb/adodb-exceptions.inc.php');


// Core Objects & Event and Messaging System Includes 
require_once(GUAVA_FS_ROOT . 'includes/ActionEvent.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/ActionListener.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/guavamessageparameter.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/guavamessage.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/guavamessagequeue.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/guavamessagehandler.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/guavamessageprocessor.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/guavaexception.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/guavaobject.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/module.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/systemmodule.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/guava.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/guavatimer.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/guavascheduler.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/view.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/output.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/Launcher.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/GuavaDesktop.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/GuavaLogin.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/GuavaFileCorruptionException.inc.php');
require_once(GUAVA_FS_ROOT . 'lib/PHPTAL/PHPTAL.php');
require_once(GUAVA_FS_ROOT . 'lib/PHPTAL/PHPTAL/PhpTransformer.php');



// UI GuavaObjects
require_once(GUAVA_FS_ROOT . 'includes/codediv.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/form.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/checkbox.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/submitbutton.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/inputselect.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/select.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/button.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/IFrame.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/image.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/textlink.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/inputtext.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/InputTextSuggestControl.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/inputdatetime.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/inputcheckbox.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/component.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/container.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/navnode.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/ScrollBuffer.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/TabContainer.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/TabPane.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/Dialog.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/ErrorDialog.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/InfoDialog.inc.php');
require_once(GUAVA_FS_ROOT . 'includes/SearchButton.inc.php');

// Drag and Drop interfaces
require_once(GUAVA_FS_ROOT . 'includes/DropTarget.inc.php');

require_once(GUAVA_FS_ROOT . 'includes/runtime.inc.php');

// Must be last to be included.  Will start/re-start the session.
require_once(GUAVA_FS_ROOT . 'includes/sessions.inc.php');


?>
