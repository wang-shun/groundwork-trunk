<?php

/*
 * Copyright (c) 1999-2005 The SquirrelMail Project Team (http://squirrelmail.org)
 * Licensed under the GNU GPL. For full terms see the file COPYING.
 */

global $plugin_hooks;
$plugin_hooks = array();

function use_plugin ($name) {
    global $config;
    if (file_exists($config['base_path'] . "/plugins/$name/setup.php")) {
        include_once($config['base_path'] . "/plugins/$name/setup.php");
        $function = "plugin_init_$name";
        if (function_exists($function)) {
            $function();
        }
    }
}

/**
 * This function executes a hook.
 * @param string $name Name of hook to fire
 * @return mixed $data
 */

/* On startup, register all plugins configured for use. */
if (isset($plugins) && is_array($plugins)) {
    foreach ($plugins as $name) {
        use_plugin($name);
    }
}

if (isset($_SERVER['DOCUMENT_ROOT']) && isset($_SERVER['REMOTE_ADDR'])) {
	$config['url_path'] = substr(__FILE__, strlen($_SERVER['DOCUMENT_ROOT']), strlen(__FILE__) - strlen($_SERVER['DOCUMENT_ROOT']) - strlen('include/plugins.php'));
	db_execute("REPLACE INTO settings (name, value) VALUES ('url_path', '" . $config['url_path'] . "')");
} else {
	$config['url_path'] = db_fetch_cell("SELECT value FROM settings WHERE name = 'url_path'");
}

$config['url_path'] = '/cacti/';
define('URL_PATH', $config['url_path']);

