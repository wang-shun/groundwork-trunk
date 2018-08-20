#
################################################################################
#Cacti Database Migration Script for plugins architecture
###############################################################################
#

use cacti;

INSERT IGNORE INTO `plugin_realms` SET id=1,plugin='internal',file='plugins.php',display='Plugin Management' ;

INSERT IGNORE INTO `plugin_hooks` SET id=1,name='internal',hook='config_arrays',file='',function='plugin_config_arrays',status=1 ;
INSERT IGNORE INTO `plugin_hooks` SET id=2,name='internal',hook='draw_navigation_text',file='',function='plugin_draw_navigation_text',status=1 ;
