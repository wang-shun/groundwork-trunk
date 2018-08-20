<?
/*
#============================================================================
# Program: index.php (NeDi GUI)
# Programmer: Remo Rickli
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
#============================================================================
# Visit http://nedi.sourceforge.net/ for more information.
#============================================================================
# DATE		COMMENT
# -----------------------------------------------------------
# 22/02/05	initial version.
# 17/03/06	new SQL query support
# 30/08/07	implemented CSS scheme
#============================================================================
*/

include_once ("inc/libmisc.php");
ReadConf('usr');

if($backend == 'MSQ'){
	include_once ('inc/libmsq.php');
}elseif($backend == 'CSV'){
	include_once ('inc/libcsv.php');
}else{
	print 'Backend not configured!';
	die;
}
$_POST = sanitize($_POST);

/* GWM: Check user and add if not exist */
$remote_user = substr($_SERVER['QUERY_STRING'],strpos($_SERVER['QUERY_STRING'],"gwuid=")+6, 20);
$link   = @DbConnect($dbhost,$dbuser,$dbpass,$dbname); # moved for sso from below
if (empty($_POST['user']) && $remote_user) {
        $passwd = md5( $remote_user );
        $query = sprintf("SELECT password FROM user WHERE name=%s", mysql_real_escape_string($remote_user));
        $row = DbQuery($query,$link);
        if (!$row) {
                $now = time();
                $u_query = sprintf("insert into user values('%s','" . $passwd . "','1','1','1','1','1','1','','','" . $now . "','" . $now . "','GWM User','english','default')",mysql_real_escape_string($remote_user));

                $res = DbQuery($u_query,$link);
                $_POST['user'] = $remote_user;
                $_POST['pass'] = $remote_user;
        } else {
                $_POST['user'] = $remote_user;
                $_POST['pass'] = $remote_user;
        }
}

$failed = 0;
if(isset( $_POST['user'])  ){
	$pass = md5( $_POST['pass'] );

	$link	= @DbConnect($dbhost,$dbuser,$dbpass,$dbname);
	if ( strpos($guiauth,'n') !== false ){
		$uok	= 1;
		$query	= GenQuery('user','s','*','','',array('name'),array('='),array($_POST[user]) );
		$res    = @DbQuery($query,$link);
	}elseif ( strpos($guiauth,'p') !== false && $_POST['user'] != "admin" ){		# PAM code by Owen Brotherhood & Bruberg
		if (!extension_loaded ('pam_auth')){dl("pam_auth.so");}
		$uok	= pam_auth($_POST['user'],$_POST['pass']);
		$query	= GenQuery('user','s','*','','',array('name'),array('='),array($_POST[user]) );
		$res    = @DbQuery($query,$link);
	}else{
		$pass = md5( $_POST['pass'] );
		$query	= GenQuery('user','s','*','','',array('name','password'),array('=','='),array($_POST['user'],$pass),array('AND') );
		$res    = @DbQuery($query,$link);
		$uok    = @DbNumRows($res);
	}
	if ($uok == 1) {
		$usr = @DbFetchRow($res);
		session_start(); 
		$_SESSION['user']	= $_POST['user'];
		$_SESSION['group']	= "usr,";
		if ($usr[2]) {$_SESSION['group']	.= "adm,";}
		if ($usr[3]) {$_SESSION['group']	.= "net,";}
		if ($usr[4]) {$_SESSION['group']	.= "dsk,";}
		if ($usr[5]) {$_SESSION['group']	.= "mon,";}
		if ($usr[6]) {$_SESSION['group']	.= "mgr,";}
		if ($usr[7]) {$_SESSION['group']	.= "oth,";}

		$_SESSION['lang'] = ($usr[13])?$usr[13]:"english";
		$_SESSION['theme'] = ($usr[14])?$usr[14]:"default";

		$now = time();
		$query	= GenQuery('user','u','name',$_POST['user'],'',array('lastseen'),'',array($now) );
		@DbQuery($query,$link);

	}else{
		print @DbError($link);
	}
	if(isset ($_SESSION['group'])){
		echo "<script>document.location.href='User-Profile.php';</script>\n";
		exit();
	} else {
		echo "<body bgcolor=#cc2200>";
		echo "<script>alert('Incorrect login!');";
		echo "history.go(-1);";
		echo "</script></body>";
	}
}
?>
<html>
<head><title>NeDi Login</title>
<link href="themes/default.css" type="text/css" rel="stylesheet">
<link rel="shortcut icon" href="img/favicon.ico">
</head>
<body onLoad="document.login.user.focus();">

<div align="center">
<form name="login" method="post" action="index.php">
<table class="login">
<tr class="loginbg"><th colspan=3><a href='http://www.nedi.ch'><img src="img/nedib.png"></a></th></tr>
<tr class="txta">
<th align="center" colspan=3>
<img src="img/nedie.jpg">
<p><hr>
<?=$disc?>
</th></tr>
<tr class="loginbg">
<th>User <input type="text" name="user" size="12"></th>
<th>Pass <input type="password" name="pass" size="12"></th>
<th><input type="submit" value="Login">
</th>
</tr>
</table>
</form>
</div>

</body>
