<?php
/*
 +-------------------------------------------------------------------------+
 | Copyright (C) 2004-2008 The Cacti Group                                 |
 |                                                                         |
 | This program is free software; you can redistribute it and/or           |
 | modify it under the terms of the GNU General Public License             |
 | as published by the Free Software Foundation; either version 2          |
 | of the License, or (at your option) any later version.                  |
 |                                                                         |
 | This program is distributed in the hope that it will be useful,         |
 | but WITHOUT ANY WARRANTY; without even the implied warranty of          |
 | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           |
 | GNU General Public License for more details.                            |
 +-------------------------------------------------------------------------+
 | Cacti: The Complete RRDTool-based Graphing Solution                     |
 +-------------------------------------------------------------------------+
 | This code is designed, written, and maintained by the Cacti Group. See  |
 | about.php and/or the AUTHORS file for specific developer information.   |
 +-------------------------------------------------------------------------+
 | http://www.cacti.net/                                                   |
 +-------------------------------------------------------------------------+
*/

include("./include/global.php");

/* check to see if this is a new installation */
if (db_fetch_cell("select cacti from version") != $config["cacti_version"]) {
	header ("Location: install/");
	exit;
}

if (read_config_option("auth_method") != 0) {
        /* handle alternate authentication realms */
        do_hook_function('auth_alternate_realms');

	/* handle change password dialog */
	if ((isset($_SESSION['sess_change_password'])) && (read_config_option("webbasic_enabled") != "on")) {
		header ("Location: auth_changepassword.php?ref=" . (isset($_SERVER["HTTP_REFERER"]) ? $_SERVER["HTTP_REFERER"] : "index.php"));
		exit;
	}

	/* don't even bother with the guest code if we're already logged in */
	if ((isset($guest_account)) && (empty($_SESSION["sess_user_id"]))) {
		$guest_user_id = db_fetch_cell("select id from user_auth where username='" . read_config_option("guest_user") . "' and realm = 0 and enabled = 'on'");

		/* cannot find guest user */
		if (!empty($guest_user_id)) {
			$_SESSION["sess_user_id"] = $guest_user_id;
		}
	}

/* START PATCH AUTH{ */
/* We make it single sign-on for groundwork */
        if (strpos($_SERVER['QUERY_STRING'],'gwuid=')=== false ) {
	/*	echo("User already set: ");echo($SESSION['GWRK_USER']);*/
        }
        else
        {
	   /* Store the user in a session and keep it around for the other pages */
           $_SESSION['GWRK_USER'] = substr($_SERVER['QUERY_STRING'], strpos($_SERVER['QUERY_STRING'],"gwuid=")+6, 20);
        }
	$remote_user = $_SESSION['GWRK_USER'];
	$_SESSION["sess_user_id"] =     db_fetch_cell("select id from user_auth where username='" . $remote_user . "'");
        if (empty($_SESSION["sess_user_id"])) {
                $password = md5($remote_user);
                db_execute("insert into user_auth values('','" . $remote_user . "','" . $password . "','0','" . $remote_user . "','','on','on','on','on','1','1','1','1','1','on')");
                $_SESSION["sess_user_id"] =     db_fetch_cell("select id from user_auth where username='" . $remote_user . "'");
                for ( $counter = 1; $counter <= 11; $counter += 1) {
                        db_execute("replace into user_auth_realm (user_id,realm_id) values(" . $_SESSION["sess_user_id"] . "," . $counter . ")");
                }

        }

/* }AUTH END PATCH */
	/* if we are a guest user in a non-guest area, wipe credentials */
	if (!empty($_SESSION["sess_user_id"])) {
		if ((!isset($guest_account)) && (db_fetch_cell("select id from user_auth where username='" . read_config_option("guest_user") . "'") == $_SESSION["sess_user_id"])) {
			kill_session_var("sess_user_id");
		}
	}

	if (empty($_SESSION["sess_user_id"])) {
		include("./auth_login.php");
		exit;
	}elseif (!empty($_SESSION["sess_user_id"])) {
		$realm_id = 0;

		if (isset($user_auth_realm_filenames{basename($_SERVER["PHP_SELF"])})) {
			$realm_id = $user_auth_realm_filenames{basename($_SERVER["PHP_SELF"])};
		}

		if ((!db_fetch_assoc("select
			user_auth_realm.realm_id
			from
			user_auth_realm
			where user_auth_realm.user_id='" . $_SESSION["sess_user_id"] . "'
			and user_auth_realm.realm_id='$realm_id'")) || (empty($realm_id))) {

			?>
			<html>
			<head>
				<title>Cacti</title>
				<link href="<?php echo $config['url_path']; ?>include/main.css" rel="stylesheet">
			</style>
			</head>

			<br><br>

			<table width="450" align='center'>
				<tr>
					<td colspan='2'><img src='<?php echo $config['url_path']; ?>images/auth_deny.gif' border='0' alt='Access Denied'></td>
				</tr>
				<tr height='10'><td></td></tr>
				<tr>
					<td class='textArea' colspan='2'>You are not permitted to access this section of Cacti. If you feel that you
					need access to this particular section, please contact the Cacti administrator.</td>
				</tr>
				<tr>
					<td class='textArea' colspan='2' align='center'>( <a href='' onclick='javascript: history.back();'>Return</a> | <a href='<?php echo $config['url_path']; ?>index.php'>Login</a> )</td>
				</tr>
			</table>

			</body>
			</html>
			<?php
			exit;
		}
	}
}

?>