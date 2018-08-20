<?
# Program: missing_plugin.php

require_once ("inc/libmisc.php");

$_GET = sanitize($_GET);
$type = htmlspecialchars(isset($_GET['type']) ? $_GET['type'] : "");

echo "You are missing a plugin to handle \"$type\" files.
See your browser setup and the browser-vendor
website to correct this situation, if desired.";
?>
