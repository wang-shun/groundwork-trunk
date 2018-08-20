<?php
/*
Copyright 2005 GroundWork Open Source Solutions, Inc. ("GroundWork")  
All rights reserved. Use is subject to GroundWork commercial license

	Version:	1.0

	Output Library for Sample App
	Author: Taylor Dondich (tdondich@itgroundwork.com)
	Description:
		This is the support output library for the Event Viewer application.
	Changelog:
		2005-05-11:	Started work
*/

function print_window_header($header, $width, $alignment = "center") {
	global $output_config;
	global $path_config;
	?>
	<table <?php if($width != NULL) { ?>width="<?=$width;?>"<?php }?> cellspacing="0" cellpadding="0" align="<?=$alignment;?>">
	<tr>
		<td width="1" bgcolor="#CCCCCC"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
		<td height="1" bgcolor="#CCCCCC"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
		<td width="1" bgcolor="#000000"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
	</tr>
	<tr>
		<td width="1" bgcolor="#CCCCCC"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
		<td class="navbar" bgcolor="#FF9999">
		<table cellpadding="2" border="0">
		<tr>
		<td class="windowtitlebar"><?=$header;?></td>
		</tr>
		</table>
		</td>
		<td width="1" bgcolor="#000000"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
	</tr>
	<tr>
		<td width="1" bgcolor="#000000"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
		<td height="1" bgcolor="#000000"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
		<td width="1" bgcolor="#000000"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
	</tr>
	<tr>
		<td width="1" bgcolor="#CCCCCC"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
		<td bgcolor="#f9f9f9">
			<table width="100%" border="0">
			<tr>
				<td class="description">
	<?php
}

function print_window_footer() {
	global $path_config;
	?>
				</td>
			</tr>
			</table>
		</td>
		<td width="1" bgcolor="#000000"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
	</tr>
	<tr>
		<td width="1" bgcolor="#CCCCCC"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
		<td height="1" bgcolor="#000000"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
		<td width="1" bgcolor="#000000"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
	</tr>	
	</table>
	<?php
}

function print_header($header, $refreshRate = 0) {
	global $output_config;
	global $path_config;
	global $sys_config;
	?>
	<html>
	<head>
	<title><?=$sys_config['name'];?><?php if($header) print(" - " . $header);?></title>
	<link rel="stylesheet" type="text/css" href="style/style.css">
	<?php
	if($refreshRate > 0) {
		?>
		<meta http-equiv="refresh" content="<?=$refreshRate;?>">
		<?php
	}
	?>
	</head>
	
	<body bgcolor="#ffffff" marginheight="0" marginwidth="0" leftmargin="0" topmargin="0">
	<table height="100%" width="100%" cellspacing="0" cellpadding="0" align="center">
	<tr>
		<td height="1" width="1" bgcolor="#000000"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
		<td class="headerBar" bgcolor="#666666;">
		<table cellpadding="2" border="0">
		<tr>
		<td height="40" class="titlebar"><?=$header;?></td>
		</tr>
		</table>
		</td>
	</tr>
	<tr>
		<td height="1" width="1" bgcolor="#000000"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
		<td height="1" bgcolor="#aaaaaa"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
	</tr>
	<tr>
		<td width="1" bgcolor="#000000"><img src="<?=$path_config['image_root'];?>dotclear.gif" height="1" width="1" /></td>
		<td valign="top" bgcolor="#ffffff">
			<table border="0" width="100%">
			<tr>
				<td valign="top" class="description">
	<?php
}

function print_footer() {
	global $output_config;
	global $path_config;
	?>
				</td>
			</tr>
			</table>
		</td>
	</tr>
	</table>
	</body>
	</html>
	<?php
}

function print_select($name, $list, $index, $index_desc, $selected = NULL, $enabled = 1) {
	$numOfElements = count($list);
	?>
	<select name="<?=$name;?>" <? if(!$enabled) print("DISABLED");?>>
		<?php
		for($counter = 0; $counter < $numOfElements; $counter++) {
			?>
			<option <?php if($selected == $list[$counter][$index]) print("SELECTED");?> value="<?=$list[$counter][$index];?>"><?=$list[$counter][$index_desc];?></option>
			<?php
		}
		?>
	</select>
	<?php
}

function print_list($listItems, $listKeys, $sortBy, $width = "100%") {
	$numOfItems = $listItems;
	?>
	<table width="<?=$width;?>" cellspacing="0" cellpadding="0" border="0">
	<?php
	for($counter = 0; $counter < $numOfItems; $counter++) {
		if($counter % 2) {
			?>
			<tr bgcolor="#cccccc">
			<?php
		}
		else {
			?>
			<tr bgcolor="#f0f0f0">
			<?php
		}
		?>
		<td><?=$listItems[$counter][$listKeys[0]['key_name']]?></td>
		</tr>
		<?php
	}
	?>
	</table>
	<?php
}
