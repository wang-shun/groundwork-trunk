-- MySQL dump 10.10
--
-- Host: localhost    Database: guava
-- ------------------------------------------------------
-- Server version	5.0.18-pro

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `bdict`
--

DROP TABLE IF EXISTS `bdict`;
CREATE TABLE `bdict` (
  `word` varchar(255) NOT NULL default '',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `intag` longblob NOT NULL,
  KEY `word` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 MAX_ROWS=300000000 AVG_ROW_LENGTH=512;

--
-- Dumping data for table `bdict`
--


/*!40000 ALTER TABLE `bdict` DISABLE KEYS */;
LOCK TABLES `bdict` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `bdict` ENABLE KEYS */;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories` (
  `rec_id` int(11) NOT NULL auto_increment,
  `path` char(10) NOT NULL default '',
  `link` char(10) NOT NULL default '',
  `name` char(64) NOT NULL default '',
  PRIMARY KEY  (`rec_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `categories`
--


/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
LOCK TABLES `categories` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;

--
-- Table structure for table `crossdict`
--

DROP TABLE IF EXISTS `crossdict`;
CREATE TABLE `crossdict` (
  `url_id` int(11) NOT NULL default '0',
  `ref_id` int(11) NOT NULL default '0',
  `word` varchar(32) NOT NULL default '0',
  `intag` int(11) NOT NULL default '0',
  KEY `url_id` (`url_id`),
  KEY `ref_id` (`ref_id`),
  KEY `word` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `crossdict`
--


/*!40000 ALTER TABLE `crossdict` DISABLE KEYS */;
LOCK TABLES `crossdict` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `crossdict` ENABLE KEYS */;

--
-- Table structure for table `dict`
--

DROP TABLE IF EXISTS `dict`;
CREATE TABLE `dict` (
  `url_id` int(11) NOT NULL default '0',
  `word` varchar(32) NOT NULL default '',
  `intag` int(11) NOT NULL default '0',
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict`
--


/*!40000 ALTER TABLE `dict` DISABLE KEYS */;
LOCK TABLES `dict` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict` ENABLE KEYS */;

--
-- Table structure for table `dict00`
--

DROP TABLE IF EXISTS `dict00`;
CREATE TABLE `dict00` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict00`
--


/*!40000 ALTER TABLE `dict00` DISABLE KEYS */;
LOCK TABLES `dict00` WRITE;
INSERT INTO `dict00` VALUES (3872,1,'signal','ϑ'),(3723,1,'signal',''),(3743,1,'mountpoint','-&'),(3781,1,'signal','$'),(3780,1,'9999','Q'),(3780,1,'buffers','	_'),(3879,1,'setenvif','ᬏ'),(3879,1,'mountpoint','Ṡ'),(3871,1,'understand','ΖᾺ೾7ďȏʍߺȟē>Ҹ¸'),(3846,1,'mountpoint','û'),(3857,1,'signal','\Z'),(3820,1,'dormant','Þ'),(3871,1,'signal','὞'),(3871,1,'increasing','㑙ᛸ'),(3871,1,'hang','㬽'),(3871,1,'depend','⬰ʋ'),(3871,1,'bugs','ۃ'),(3862,1,'bugs','Ì'),(3870,1,'prepared','ã'),(3871,1,'05','㖓ȭ0Ųᘝ#'),(3837,1,'popuser',''),(3879,1,'valuelist','䠹'),(3879,1,'hda2','Ả'),(3879,1,'gwcollagedb','ከ勐0Z\n\Z¸'),(3878,1,'21','Ξ'),(3873,1,'vbs','rKHi'),(3880,1,'continues','ᕙ'),(3880,1,'securing','ത'),(3881,1,'agreed','⎌'),(3881,1,'designer','☫'),(3881,1,'gwcollagedb','⍌');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict00` ENABLE KEYS */;

--
-- Table structure for table `dict01`
--

DROP TABLE IF EXISTS `dict01`;
CREATE TABLE `dict01` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict01`
--


/*!40000 ALTER TABLE `dict01` DISABLE KEYS */;
LOCK TABLES `dict01` WRITE;
INSERT INTO `dict01` VALUES (3879,1,'eventhandlers','䍮»'),(3889,1,'files','ҧ'),(3877,1,'files','֥'),(3877,1,'2048','д'),(3876,1,'files',''),(3875,1,'files','ġ'),(3874,1,'files','ŉ'),(3880,1,'typed','ఁ]'),(3805,1,'files','\Zj'),(3806,1,'files',''),(3827,1,'sites','Ƅ'),(3830,1,'exist','U'),(3879,1,'spool','ᎁ'),(3879,1,'files','ሆ೴Ţ(H[\n\Z͡DÊˆݒ-ϢɢƼۼń	ZįęڢǠ¦=+ћǙٴ҄чȉ'),(3881,1,'files','લᨪ@#®՞ਝÄ'),(3879,1,'share','ൻ⬧'),(3879,1,'parents','㛤ċɐ0à3Ү'),(3780,1,'files','¬'),(3721,1,'exist','õ'),(3720,1,'files','Ĥ'),(3844,1,'share',')'),(3879,1,'valuen','䕍'),(3879,1,'synch','往̮'),(3871,1,'typed','䨯'),(3740,1,'hrswrunpath','ª'),(3877,1,'share','ɺ'),(3888,1,'share','Ō'),(3880,1,'files','໴'),(3880,1,'share','㨝UKOǎY_IJCJIƚ?BA'),(3871,1,'compensated','䯣'),(3871,1,'creation','ᒈ'),(3871,1,'files','܃\Zᢦ'),(3871,1,'gridstep','ᇕ'),(3871,1,'lfm','Ⴤ\\'),(3871,1,'behaviour','ᘉ'),(3871,1,'18446744069414584320','䴆%2'),(3870,1,'sites','G'),(3868,1,'sites','¢'),(3862,1,'files','â'),(3887,1,'share','ಬ¡'),(3885,1,'parents','ȸ'),(3886,1,'parents','\'Ȗ'),(3887,1,'parents','Ǩ'),(3885,1,'files','_?'),(3882,1,'files',')/7'),(3881,1,'typed','㍂'),(3881,1,'share','୭ᰄ'),(3881,1,'isflapdetectionenabled','ࡖ '),(3881,1,'leanest','➍'),(3890,1,'share','ٝ¥·'),(3890,1,'files','Ķ )խ<»ъܚऻ|¬'),(3778,1,'files',''),(3752,1,'spool','8'),(3752,1,'files',';'),(3751,1,'predictions','ƅĚ'),(3751,1,'exist','ƣ'),(3722,1,'sites','|'),(3892,1,'files','©'),(3888,1,'files','©ŵ\ZHeU,5o$(ѽ'),(3880,1,'sites','㧿ʽ'),(3879,1,'2048','᥸'),(3880,1,'acknowledgment','ᦦͭәŢैD\n\rϤD\r'),(3871,1,'share','߻'),(3873,1,'receiveq','βq'),(3872,1,'spool','͂5},h'),(3872,1,'files','àR'),(3872,1,'exist','ĳ'),(3872,1,'creation','ȁ'),(3871,1,'xsizexysize','ᶂ'),(3879,1,'creation','䘦\n!9ȱ'),(3871,1,'autoconfigure','ႆłĻǞ<');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict01` ENABLE KEYS */;

--
-- Table structure for table `dict02`
--

DROP TABLE IF EXISTS `dict02`;
CREATE TABLE `dict02` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict02`
--


/*!40000 ALTER TABLE `dict02` DISABLE KEYS */;
LOCK TABLES `dict02` WRITE;
INSERT INTO `dict02` VALUES (3841,1,'cisco',''),(3873,1,'net','yK'),(3824,1,'long','ó'),(3871,1,'guessed','♻'),(3871,1,'faq','健'),(3872,1,'net','ŦCϒ'),(3877,1,'long','Äˤ'),(3871,1,'speed4','㱘m'),(3829,1,'net','\"'),(3840,1,'net','̍'),(3880,1,'cisco','৞'),(3831,1,'net','ª'),(3879,1,'long','⠭'),(3871,1,'delta','ౙ:㯦:ƴ\n/|\'\nO-w\Zœd'),(3871,1,'confusion','㽖'),(3871,1,'cisco','䃜1'),(3871,1,'accommodate','ᓻ'),(3868,1,'net','Ý'),(3862,1,'reported','Æ'),(3862,1,'net','W'),(3871,1,'long','թɜ̵Ͷᚫᅻ໐Ñí'),(3871,1,'net','⡌߽⁾'),(3730,1,'r2','d'),(3862,1,'mail','P'),(3822,1,'long','í'),(3880,1,'confusion','␱'),(3878,1,'mail','ǠśɨÝ'),(3819,1,'cisco','e'),(3879,1,'integrating','Ðۛط'),(3879,1,'gwsp','䦢'),(3879,1,'cisco','ᔌ'),(3871,1,'zones','㞁'),(3837,1,'mail',')\r]3'),(3836,1,'long','P'),(3835,1,'triple','s\n'),(3871,1,'mail','ந'),(3751,1,'long','G'),(3763,1,'net','Ě'),(3766,1,'net',''),(3780,1,'long',''),(3780,1,'ofiles','¨'),(3803,1,'mail',''),(3745,1,'nocommand','o'),(3745,1,'mail',''),(3744,1,'net','Ə'),(3871,1,'python','ĬBû'),(3859,1,'cisco',' '),(3880,1,'long','᯲೵өƓࢸಗ'),(3734,1,'long',''),(3881,1,'4913','ὅѽ'),(3881,1,'integrating','ُហȃ'),(3881,1,'intends','ࠎ'),(3881,1,'long','⵾'),(3881,1,'net','఻࿑ၫͲΜد'),(3881,1,'peerport','⏎'),(3881,1,'python','Ⲯ'),(3884,1,'amplified','٬'),(3884,1,'integrating','ॗ'),(3886,1,'amplified','̔C'),(3886,1,'long','ę'),(3887,1,'amplified','̿D'),(3887,1,'long','Ł'),(3889,1,'long','ð'),(3889,1,'net','ӥ'),(3890,1,'integrating','Ş'),(3890,1,'long','๚ȎΜ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict02` ENABLE KEYS */;

--
-- Table structure for table `dict03`
--

DROP TABLE IF EXISTS `dict03`;
CREATE TABLE `dict03` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict03`
--


/*!40000 ALTER TABLE `dict03` DISABLE KEYS */;
LOCK TABLES `dict03` WRITE;
INSERT INTO `dict03` VALUES (3885,1,'send','ڥ	'),(3884,1,'send','Ҭ৶'),(3880,1,'send','Ǽβ)ḝϷ\r\rτĘ\rƢ\rƒ'),(3880,1,'mentioned','⋳'),(3880,1,'involving','䈹'),(3880,1,'fatigue','ᔦ'),(3880,1,'expired','౼'),(3880,1,'clearly','ᒧ'),(3881,1,'send','ŽɁ+ᰞëǮ۵'),(3881,1,'mentioned','⁧'),(3881,1,'internal','സ'),(3840,1,'involving','Ÿ'),(3837,1,'send','g'),(3834,1,'send',''),(3833,1,'send',',J'),(3783,1,'send','®'),(3784,1,'send',',J'),(3785,1,'send',',J'),(3804,1,'send',''),(3814,1,'send',',J'),(3818,1,'send',',J'),(3822,1,'send','N'),(3827,1,'expired','ˍ'),(3721,1,'computing','¤'),(3864,1,'send','X'),(3868,1,'send','Ò'),(3871,1,'idat1','᷒!'),(3871,1,'guess','Ԓ'),(3869,1,'transactions','Þ'),(3887,1,'send','ࣦ	'),(3879,1,'internal','ञ'),(3879,1,'mentioned','䚔'),(3879,1,'nagiosadmin','ෞ䖟\n'),(3879,1,'rrdname','䙀pġOƉ	)'),(3879,1,'s2chapter1c','开'),(3871,1,'mentioned','֝㕖त'),(3871,1,'millennium','✩'),(3872,1,'send','ୣ>`'),(3872,1,'speak',''),(3873,1,'transactions','Թ'),(3878,1,'iis','ɝӥ'),(3879,1,'avail','匒ª	Ēï'),(3854,1,'send','/Z	'),(3879,1,'send','ʝ̎)⥚>വຽ'),(3721,1,'founded','('),(3728,1,'send',')J'),(3729,1,'send','*L'),(3732,1,'send',')K'),(3737,1,'send',')J'),(3739,1,'send',')J'),(3744,1,'internal','ƿ'),(3748,1,'send',',J'),(3759,1,'procl',''),(3761,1,'send',',J'),(3762,1,'send','e'),(3763,1,'send','ą'),(3888,1,'mentioned','Т'),(3888,1,'windows2','؀O¸Ű'),(3889,1,'nagiosadmin','Ҿ'),(3889,1,'send','Ɖ'),(3890,1,'authenticating','Ψ'),(3890,1,'send','প'),(3891,1,'send','û>'),(3759,6,'procl','Ĉ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict03` ENABLE KEYS */;

--
-- Table structure for table `dict04`
--

DROP TABLE IF EXISTS `dict04`;
CREATE TABLE `dict04` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict04`
--


/*!40000 ALTER TABLE `dict04` DISABLE KEYS */;
LOCK TABLES `dict04` WRITE;
INSERT INTO `dict04` VALUES (3869,1,'popular','¬'),(3870,1,'interface','¦)'),(3871,1,'1020615600','⬽ß'),(3881,1,'getleftchild','᮰'),(3881,1,'interface','ڟԒ&ߒĝŪÚ൉ʽìĂ\r\Zɯɛҁԟ:'),(3879,1,'import','⁮,̇Ǝ¢]mƢ+#~,¤+#कХड़۬Íɉ2H!ͻ'),(3890,1,'played','ݞ &'),(3881,1,'acknowledged','᥾1'),(3881,1,'desc','᨟'),(3879,1,'characters','ॽ'),(3879,1,'central','基'),(3878,1,'interface','ēˋԁ'),(3890,1,'interface','ʌҊ˸ost႑'),(3890,1,'characters','ᤖ5ʱ!'),(3888,1,'bottom','ՀȎ-'),(3889,1,'acknowledged','͌'),(3887,1,'characters','ì'),(3886,1,'characters','Ä'),(3885,1,'import','\'ľ'),(3881,1,'popular','ᰎ'),(3881,1,'skeleton','⺅'),(3882,1,'import','ë'),(3883,1,'timeperiod','ķ'),(3872,1,'central','²Ц^+'),(3871,1,'transfered','㈦ȹऩ'),(3871,1,'setups','ҙ'),(3871,1,'interface','ĚᰟེᏟN='),(3871,1,'fridge','㊄'),(3871,1,'characters','߄ஸघ?᧬'),(3871,1,'bottom','ᖻέ'),(3721,1,'popular',''),(3723,1,'strength',''),(3744,1,'pr','Ʉ'),(3754,1,'acknowledged','ņŋ'),(3763,1,'improvements','ĩ'),(3801,1,'interface','$'),(3811,1,'ver2',''),(3819,1,'interface',''),(3820,1,'interface',' ¼f'),(3821,1,'interface','\''),(3831,1,'popular',''),(3840,1,'interface','Ⱦ'),(3849,1,'interface','*+'),(3857,1,'snmpv1',';'),(3857,1,'strength','('),(3862,1,'interface','F'),(3868,1,'improvements','í'),(3869,1,'interface','X'),(3721,1,'central','¡'),(3881,1,'distributions','∭'),(3879,1,'offerings','ѣ#'),(3876,1,'import','Ì'),(3877,1,'import','Ո'),(3875,1,'interface','OƁGÄ'),(3875,1,'import','Ūç,'),(3720,1,'improvements','µ'),(3879,1,'acknowledged','慿'),(3874,1,'import','ƚ'),(3872,1,'distributions','ſ'),(3873,1,'growth','Ӫ'),(3879,1,'interface','ำ%È®ȁഢá½ʩ˳ẫ˜ൌ๬'),(3890,1,'smarter','ᕃ'),(3881,1,'central','⟖'),(3880,1,'offerings','Ѧ#'),(3880,1,'interface','ͼԝ3ǻɖʣϹˑa@ᵊ෤dʄ'),(3880,1,'acknowledged','ᦳۍø૞Ѧ'),(3879,1,'timeperiod','⼕'),(3881,1,'offerings','ɶ#');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict04` ENABLE KEYS */;

--
-- Table structure for table `dict05`
--

DROP TABLE IF EXISTS `dict05`;
CREATE TABLE `dict05` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict05`
--


/*!40000 ALTER TABLE `dict05` DISABLE KEYS */;
LOCK TABLES `dict05` WRITE;
INSERT INTO `dict05` VALUES (3788,1,'clustered','µ'),(3827,1,'10d','ŏ'),(3827,1,'media','Ś'),(3831,1,'tool',''),(3868,1,'tool','&'),(3869,1,'capabilities','µ'),(3871,1,'12383','㟵A'),(3871,1,'4294967296','䴀\Z'),(3871,1,'assumes','ࠎ᝘⤇'),(3780,1,'vkf','µë'),(3720,1,'tool','r'),(3871,1,'rely','ႛ'),(3882,1,'tool','½¬'),(3881,1,'assumes','එ'),(3879,1,'gui','㢞'),(3722,1,'insist','A'),(3880,1,'tool','ʋM-ݨ૾ƆȊ๕'),(3881,1,'jetty','ߒᷰ\Z'),(3871,1,'tool','ǶşजᮆࢦĦౢ'),(3872,1,'assumes','Ĭ'),(3872,1,'tool','Ȑ'),(3873,1,'assumes','ɏ'),(3874,1,'tool','ƖgŒ'),(3875,1,'tool','ŦPfO'),(3876,1,'tool','ÈPl'),(3779,1,'pctcrit',','),(3751,1,'restricts','Ƅ'),(3751,1,'handled','Ȗ'),(3744,1,'snmp4nagios','\"'),(3721,1,'unites','6'),(3889,1,'tool','Z'),(3888,1,'gui','ƙ'),(3881,1,'cleanup','⽼'),(3880,1,'sanfrancisco','Ⴁ'),(3879,1,'netview','䋮'),(3881,1,'netview','޷'),(3871,1,'rra','ҽļ\"ÓɶrˆGě࠴\rৄwĝ#S)Ҋ\nWຒʀ঍ŗѼټ'),(3879,1,'assumes','搶'),(3879,1,'capabilities','ᶘ'),(3880,1,'optimize','㣑'),(3880,1,'individuals','മ'),(3880,1,'gui','␼'),(3879,1,'tool','̡M-→ϒ\'ſ֐Ĺ߷ާӷટ'),(3880,1,'capabilities','᪂'),(3879,1,'rra','䙡Ɓ+Ƴ'),(3890,1,'media','ޮ'),(3884,1,'tool','দ'),(3881,1,'tool','ߧ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict05` ENABLE KEYS */;

--
-- Table structure for table `dict06`
--

DROP TABLE IF EXISTS `dict06`;
CREATE TABLE `dict06` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict06`
--


/*!40000 ALTER TABLE `dict06` DISABLE KEYS */;
LOCK TABLES `dict06` WRITE;
INSERT INTO `dict06` VALUES (3890,1,'uid','উ'),(3879,1,'allow','ೠGా*A9*#6\rп˷χŕüϱف«Ğʺřፌ'),(3878,1,'allow','с'),(3879,1,'advance','ᨐ'),(3890,1,'storing','ข'),(3877,1,'autohome','Ɍ'),(3877,1,'allow','͑'),(3879,1,'yourserver','ᦼ'),(3877,1,'directory','ş%>YJ4Ś±(YO¬'),(3879,1,'directory','सϴ$ \"ʙÈʺœъʐbШ5\r༯၇ɑԁâʳƗɑ\'!= Ʊ`ƸX²§\n	Iľmµóʔо_ďÃ'),(3881,1,'enable','ᳮ᩹'),(3879,1,'uid','Ᲊ'),(3879,1,'ldap','Ćۛৎܸ˪\r\'/\r\Z'),(3879,1,'jump','׹'),(3879,1,'great','㾯'),(3879,1,'graphing','ᔺⷻΙ²\n'),(3879,1,'enable','ឫŁ\n\nȭãฑᰨʰ៟'),(3885,1,'directory','dƷ'),(3884,1,'enable','̭b>Ń×ΛϬƁ³$'),(3884,1,'dependent','ސ৤A+C'),(3884,1,'allow','ݝೖ'),(3876,1,'graphing','1|Ä'),(3875,1,'graphing','1ĎÊ'),(3880,1,'jump','׼'),(3892,1,'directory',''),(3892,1,'appear','Q'),(3880,1,'graphing','वÙ⪍Ș`'),(3885,1,'enable','˟3!%5÷lX'),(3881,1,'directory','⒈0,̢FϘ+(Ď2Ɔԑl®[ƫ'),(3881,1,'allow','ٷěᦈࡗޖ'),(3880,1,'ldap','ᑜ݊'),(3875,1,'directory','ʗ'),(3720,1,'announced','3'),(3878,1,'ldap','Ρšʝ'),(3878,1,'graphing','ऱ'),(3881,1,'footprint','⢚'),(3882,1,'directory','r'),(3881,1,'jump','Ќ'),(3736,1,'aix','ª'),(3736,1,'lsps','°'),(3752,1,'allow','¹'),(3753,1,'v2','h'),(3753,1,'v6','k'),(3754,1,'directory','L<'),(3756,1,'allow','ä'),(3780,1,'mrtgext','Ő'),(3799,1,'hack','ŝ'),(3804,1,'directory','p'),(3811,1,'ldap','C\r'),(3824,1,'helo','C'),(3825,1,'helo','<'),(3827,1,'allow','Š'),(3840,1,'elementary','ʱ'),(3840,1,'proved','ɽ'),(3860,1,'directory',''),(3862,1,'great','º'),(3868,1,'enable','r'),(3871,1,'12405','㠁?'),(3871,1,'3h20m','♦'),(3871,1,'aix','Ō'),(3871,1,'allow','ĿĢᵝ঴Ӄ'),(3871,1,'appear','ᇍ'),(3871,1,'dependent','๴⏘طद'),(3871,1,'directory','Ē[⚒Ֆ'),(3871,1,'graphing','\"׺಴ʋᱻ'),(3871,1,'predefined','ቱ'),(3871,1,'storing','жú'),(3879,1,'dependent','ᝑ'),(3879,1,'appear','ⱳᠶ̉ᬹ'),(3890,1,'appear','ἥ'),(3888,1,'allow','ד'),(3888,1,'enable','ϴη*'),(3888,1,'great','ٵ'),(3890,1,'allow','ݓĈೣď'),(3887,1,'enable','ԝ3!%6ùlX'),(3887,1,'directory','ದ¡'),(3887,1,'dependent','଺ԝ(9'),(3886,1,'allow','ô'),(3887,1,'allow','Ĝआ'),(3874,1,'graphing','1ńǇ'),(3872,1,'telocator',''),(3873,1,'directory','Ż'),(3872,1,'directory','ķۡ'),(3872,1,'appear','ص'),(3720,1,'directory','Ŏ'),(3811,6,'ldap','Ð'),(3892,1,'storing',''),(3890,1,'enable','Àxޭ:vʊø̥ԀQxSЉ'),(3890,1,'directory','೙Čཨ'),(3880,1,'enable','ঌ࡚ᙓƀ\n8+\n\r!+\nd\niĂ=Õ(	I+\r	ÓRìG,)ᇇ'),(3880,1,'directory','ৼ'),(3880,1,'dependent','ቯ'),(3880,1,'allow','Ⴈ޵ഃٺiѻŦƯͤ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict06` ENABLE KEYS */;

--
-- Table structure for table `dict07`
--

DROP TABLE IF EXISTS `dict07`;
CREATE TABLE `dict07` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict07`
--


/*!40000 ALTER TABLE `dict07` DISABLE KEYS */;
LOCK TABLES `dict07` WRITE;
INSERT INTO `dict07` VALUES (3759,1,'current','¿\n'),(3759,1,'warning','3:'),(3761,1,'warning','&Æ'),(3725,1,'warning','?'),(3809,1,'warning','5'),(3729,1,'warning','$`'),(3782,1,'warning','0#8-'),(3766,1,'warning','7]&'),(3776,1,'warning','t \"&'),(3778,1,'warning','0'),(3780,1,'current','o\Z'),(3780,1,'warning','2čP'),(3781,1,'warning','>\"'),(3880,1,'warning','ጚ٪:Ǌ(JoȷʢŢᓖĂpʋ0٭'),(3880,1,'usability','ۋ'),(3809,1,'current',''),(3808,1,'warning','B'),(3803,1,'warning',':	'),(3835,1,'warning',''),(3888,1,'current','òԪ'),(3857,1,'warning','@'),(3856,1,'warning','Å'),(3854,1,'warning',')ç'),(3874,1,'warning','ȩ;>6+'),(3874,1,'documented','ǃ'),(3799,1,'warning','7L	5'),(3798,1,'warning','3\'0'),(3791,1,'warning','f'),(3764,1,'warning',')'),(3763,1,'warning','(@'),(3763,1,'current','©'),(3762,1,'warning','Q<'),(3757,1,'warning','@Ú\Z'),(3891,1,'warning','ʳ˓'),(3890,1,'warning','ɴԚ೅9R'),(3890,1,'prevents','Ꮟ'),(3890,1,'current','%w˵ԓ'),(3736,1,'warning',';\r'),(3735,1,'warning','B'),(3734,1,'warning','Ô'),(3733,1,'compare','$'),(3732,1,'warning','#Ç'),(3731,1,'warning','U\n'),(3730,1,'current','M'),(3869,1,'integration','W«'),(3866,1,'warning','z'),(3859,1,'warning','.'),(3862,1,'libnet','C	'),(3863,1,'warning','Æ'),(3864,1,'warning','D\''),(3865,1,'warning','R'),(3755,1,'warning','aM'),(3872,1,'runlevel','ɝ'),(3728,1,'warning','#Æ'),(3837,1,'warning','Í\r\r'),(3840,1,'warning',''),(3843,1,'warning','4'),(3844,1,'warning','jH'),(3848,1,'warning','/N'),(3811,1,'warning',''),(3744,1,'warning','ă'),(3879,1,'current','⁓ûདੱվʀࢾ˚ᖉ'),(3880,1,'integration','ࡠ=ðחE㑮'),(3876,1,'documented','õ'),(3748,1,'warning','&Æ'),(3875,1,'administrative','Ƞ'),(3810,1,'warning',''),(3745,1,'warning','ªA'),(3871,1,'visitors','㉲'),(3871,1,'experiment','㎬'),(3871,1,'reuses','ㄤ'),(3880,1,'current','ܯொҲwդÝKΦΜ0Ȇ඼Ȣ±޺΁'),(3871,1,'compare','㋺ĩ'),(3784,1,'warning','&Æ'),(3785,1,'warning','&Æ'),(3786,1,'warning','0'),(3788,1,'warning','{'),(3737,1,'warning','#Æ'),(3765,1,'warning','8)'),(3727,1,'warning','nO&'),(3814,1,'warning','&Æ'),(3813,1,'warning','B\r'),(3834,1,'warning','d;'),(3833,1,'warning','&Æ'),(3830,1,'current','m'),(3828,1,'warning','63'),(3827,1,'warning','ƲMk8'),(3824,1,'warning',' ¦'),(3824,1,'compare',''),(3822,1,'warning',':'),(3884,1,'warning','ҹɦޠ˴H'),(3881,1,'permission','㏺'),(3881,1,'integration','ᶕɖ'),(3881,1,'current','ܹᑤ\nưΒƔ՟'),(3881,1,'compare','᪑Ô'),(3743,1,'warning','_'),(3741,1,'warning','p'),(3879,1,'completion','ᖌ丝'),(3879,1,'administrative','Ϩղ'),(3871,1,'current','ࡔ׀	ƄқɿȨ8Ĺ:ڗɸʌĖȱεˢ+Ϝ-\rࠪᆽ'),(3878,1,'integration','ô'),(3877,1,'wind','լ'),(3876,1,'warning','ō'),(3871,1,'94','䔶'),(3871,1,'5mon1w2d','⓭'),(3871,1,'30am','▜'),(3885,1,'current','ȁ	ؑ	'),(3876,1,'administrative','ƈ'),(3875,1,'documented','Ɠ'),(3874,1,'current','ɞ'),(3874,1,'administrative','͓'),(3873,1,'warning','ğ\n'),(3740,1,'warning','Ē'),(3740,1,'current','Ś'),(3739,1,'warning','#Æ'),(3721,1,'administrative','Ƀ'),(3720,1,'current','£'),(3873,1,'administrative','+'),(3872,1,'warning','਒'),(3754,1,'warning','%\ni		Ij'),(3752,1,'warning',',!'),(3751,1,'warning','pAR	=Ù'),(3884,1,'current','ƪ	ਁ\nծʜ\r'),(3881,1,'warning','ᴿոİ'),(3881,1,'documented','ṱ'),(3880,1,'administrative','䓈'),(3879,1,'warning','Ḓ῍ݫ'),(3880,1,'compare','㓽'),(3820,1,'warning','í'),(3818,1,'warning','&Æ'),(3816,1,'warning','7'),(3815,1,'warning','6	'),(3723,1,'warning','-'),(3722,1,'warning','Ü'),(3721,1,'current','đ'),(3846,1,'warning','U'),(3845,1,'warning','E'),(3872,1,'current','Ķ'),(3872,1,'6227585','ԡ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict07` ENABLE KEYS */;

--
-- Table structure for table `dict08`
--

DROP TABLE IF EXISTS `dict08`;
CREATE TABLE `dict08` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict08`
--


/*!40000 ALTER TABLE `dict08` DISABLE KEYS */;
LOCK TABLES `dict08` WRITE;
INSERT INTO `dict08` VALUES (3880,1,'amount','̷'),(3879,1,'viewer','̦pΟňॏ	\ní±⸻¡ɇבoܞ¥ಘ/ǋï$'),(3874,1,'service','h·Ëō'),(3832,6,'ftpget','X'),(3876,6,'service','ƴ'),(3887,1,'service','Ɨǉ,\r\n\n\n٧Ө'),(3887,1,'texture','೹'),(3888,1,'service','WÒJǍȬ5\"Ūt³'),(3888,1,'viewer','ƥ'),(3889,1,'continuous','ƣ'),(3889,1,'service','%\n\nQ>Ì\r4	\r0'),(3890,1,'amount','ᕑȒ'),(3890,1,'authorize','ЯJ7'),(3890,1,'ocsp','ᙩ&'),(3890,1,'service','Ï\nɅ?ȹJȮÕÅOſƼ6Bŧ\n $V	T\n!-$U!	\Z2ú%1Û\rI\n96Ög4K9X\n[U#J-\Z(ǲ1'),(3891,1,'amount','Ƚâ˗<'),(3891,1,'service','ɍ$\Zǌf$>\Z'),(3892,1,'service','Ċ$Q'),(3876,2,'service',''),(3787,6,'negate','³'),(3884,1,'service','#\r\n\n	/			<B	+#,\'8P<3-8V0\n	\n*\r\'!W\nI\n	\n	\n		\n\n			<B	+#,\'8P<3-\n	A$C	_\n\r\r'),(3884,1,'amount','ԝ਋'),(3881,1,'service','Êɵ1;#%p:w;\n#¸^\n)Ö\"sÝ\n\rãI&ֱ˕sĖ	\Z]éϟ`Ǒ9^ƶȀļǬ<'),(3881,1,'viewer','üیᲜ-#'),(3882,1,'service','GĎ'),(3883,1,'service','À'),(3875,1,'service','<6§hÏ'),(3876,1,'service',')(d'),(3877,1,'service','Ƕ'),(3878,1,'service','Çʹ\r\r<Ȁˢ%'),(3879,1,'amount','ύ崶Ů'),(3879,1,'operating','⫄'),(3879,1,'paging','α'),(3879,1,'restore','ヅ㎍ʠ'),(3879,1,'service','Ԭ1;#%஻äचïʫ÷@Q\n\rgpo\r\"\n-1Ñ/%PPŮԯ1A΀	\" \r5 *\rCE-^&\Z\'\r¿Ʉ-\r\rK¨÷	\r-úÄ~ť?¡I\'=\n\r\Z\rHe\n\Z\n	\rFĔÔ\nR@$²\r+3	Gw?Uĵƚ\n#[T**0PǳӽߚfUčnӋ '),(3880,1,'viewer','Ð>Ųpϰ ěـոǪ\'>\n$&AY\nĒI%˖/1?QϛbĥċĔnZਖÖ'),(3881,1,'operating','೭'),(3881,1,'gwservice','⌻'),(3881,1,'function','ಀºfA>B>>FBBWFO@AAA?8455!\r5\'\"\Z\Z\Z4-!X42X+©ǭ<=\nतঽ*\r79ĩĕ!\rǡϻ	t'),(3887,1,'amount','܌ą'),(3886,1,'service','ůvĴD25,P\n'),(3880,1,'paging','̛䍢'),(3880,1,'service','ԯ1;#%ƒ˵BϐFJPÑ<$ F	1\n7%às\r4ßìĢKØ1US@2}À.+.s8ǧbf%@\r#@(`Ü\nįGQ²k=pęÔȅɶ-\rO\r\r-\'\r\n\n\r	0\n\'\n		/. \'\"ͯŖ}]ĐP1CŊA)ŷ1kå(P36Q<Ľ'),(3885,1,'service',';uĈֆ?\n	\r		2$09'),(3885,1,'amount','Ӌą'),(3880,1,'operating','ޤಊܨ'),(3721,1,'amount','ɬ'),(3721,1,'seminars','ʋ'),(3724,1,'negate','V'),(3727,1,'service',''),(3744,1,'gritsch','C'),(3751,1,'amount','É$Ę'),(3752,1,'service','÷'),(3753,1,'service','\Z+'),(3754,1,'amount','ų'),(3754,1,'service','P´	}6$	\n\r4'),(3755,1,'service','\Z'),(3780,1,'service','p'),(3781,1,'ntpq',',_'),(3782,1,'paging','Ŭ'),(3782,1,'service','Ŝ'),(3787,1,'negate','3\'	'),(3788,1,'service','H%@'),(3823,1,'service',''),(3827,1,'service','\Z'),(3832,1,'ftpget',''),(3840,1,'function','ĉ'),(3840,1,'service','ʼ	'),(3841,1,'service','f-'),(3846,1,'amount',''),(3848,1,'service','\Z'),(3856,1,'service','´µ'),(3862,1,'function','x'),(3862,1,'makefile','é'),(3866,1,'service',''),(3869,1,'operating','6'),(3871,1,'amount','ȾʌĒӾÛxҚᏎ͛ࢢѝമ&ɳ҈'),(3871,1,'continuous','ࠀѠ'),(3871,1,'dst','۪Rᵽi8ʊ¿'),(3871,1,'ergens','⡆߽⁾'),(3871,1,'function','ȔɫɷĥƗν.Т©ƷʘæTȳċwɸ/ōcUð+׾ĥíᎤ'),(3871,1,'makefile','ĥ'),(3871,1,'restore','˖ᴖ\r\n	'),(3871,1,'understood','⏖'),(3871,1,'viewer','㧽ွ'),(3872,1,'contactname','ॣǄ'),(3872,1,'function','ƴ'),(3872,1,'makefile','Ǧ'),(3872,1,'paging','E*\'	&κ\Z&+ۧ'),(3872,1,'service','ȎÅ86\'%4,2		ĎïO4He$ĩ`M+\ZƵ'),(3873,1,'service','ƚ\'%§'),(3874,1,'amount','ʜå&');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict08` ENABLE KEYS */;

--
-- Table structure for table `dict09`
--

DROP TABLE IF EXISTS `dict09`;
CREATE TABLE `dict09` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict09`
--


/*!40000 ALTER TABLE `dict09` DISABLE KEYS */;
LOCK TABLES `dict09` WRITE;
INSERT INTO `dict09` VALUES (3879,1,'ul','嵍+\n¿\n'),(3871,1,'mimics','቏ι'),(3741,1,'maximum','u'),(3748,1,'maximum','5'),(3879,1,'san','ᔤ'),(3879,1,'pane','Л噕'),(3879,1,'preferable','⛭'),(3880,1,'consolidated','㙓'),(3880,1,'clearing','⊁Ɓ'),(3879,1,'maximum','䕳'),(3879,1,'letter','ⱪ'),(3879,1,'consolidated','䭛\n'),(3871,1,'775','䊿5'),(3871,1,'12411','㠅='),(3756,1,'intervals','Ú'),(3761,1,'maximum','5'),(3782,1,'letter','é'),(3782,1,'procstate','Ī'),(3732,1,'maximum','2'),(3730,1,'maximum','p'),(3729,1,'maximum','3'),(3728,1,'maximum','2'),(3720,1,'dropdownbutton','Ɲ'),(3720,1,'ability','¨Z'),(3739,1,'maximum','2'),(3871,1,'maximum','҃Ć͈ųȥ9ԯ^\nɘ୷߇¬ୈЛ̈́©ջȚ±(I~Xדm\ZAcC'),(3871,1,'letter','ชSᚁł'),(3871,1,'ability','Ǔ'),(3870,1,'ability','û'),(3854,1,'maximum','8'),(3840,1,'ability','ĳ'),(3833,1,'maximum','5'),(3827,1,'span','ţ'),(3827,1,'maximum','ǃ'),(3818,1,'maximum','5'),(3814,1,'maximum','5'),(3813,1,'maximum','G'),(3798,1,'maximum',''),(3799,1,'maximum','W'),(3785,1,'maximum','5'),(3784,1,'maximum','5'),(3871,1,'clearing','⠣'),(3871,1,'consolidated','ҋŢρ\'ಟ8'),(3871,1,'if2','ⱂ'),(3737,1,'maximum','2'),(3877,1,'whichever','ځ'),(3871,1,'span','ଜ'),(3871,1,'intervals','◇̇ᒑᄄ&'),(3880,1,'ability','ᆐిᚭˊ'),(3871,1,'building','Ī䛴'),(3751,1,'trendcritical','ƒ'),(3880,1,'intervals','㡝'),(3880,1,'maximum','ኵⷑ'),(3880,1,'pane','ܹᰨ ˝nZྷ࿰'),(3880,1,'san','৶'),(3881,1,'ability','✳ա'),(3881,1,'building','❽ɾ\Zˀ'),(3881,1,'consolidated','Ჸ'),(3881,1,'helper','⻎Ж'),(3881,1,'maximum','ឬ©'),(3881,1,'secretapp','㙚'),(3884,1,'maximum','Ԝ਋'),(3885,1,'maximum','ӊą'),(3887,1,'maximum','ˉвąي'),(3890,1,'chekcing','Æ'),(3890,1,'intervals','ाђŊଣ2'),(3890,1,'maximum','ሉŚTʦą'),(3891,1,'maximum','ȼâ˗<');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict09` ENABLE KEYS */;

--
-- Table structure for table `dict0A`
--

DROP TABLE IF EXISTS `dict0A`;
CREATE TABLE `dict0A` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict0A`
--


/*!40000 ALTER TABLE `dict0A` DISABLE KEYS */;
LOCK TABLES `dict0A` WRITE;
INSERT INTO `dict0A` VALUES (3884,2,'services',''),(3887,1,'avoid','̾D'),(3879,1,'satisfied','傋'),(3879,1,'services','ňႷÐкU4*֥ǏʂK+bæ %vƘ!#mEŃ֔D	ͳG_Fª\nTF1tù7b¢<Ȯ\ni?ßP\rÄƍ8ø٬̵%ʬʵ^ཎ%'),(3890,1,'mm','ᯖ'),(3881,1,'avoid','⶟'),(3879,1,'mm','⻛'),(3872,1,'services','࢖'),(3886,1,'manage','SŨáJ¥Ǝ'),(3879,1,'row','⍙㬍\n'),(3721,1,'manage','ɧ'),(3886,1,'avoid','̓C'),(3880,1,'poller','ᵘ\Z'),(3880,1,'services','þطࡸhÇ¢UxĈëƚ¼%&ĩĚB¼\r\nE2\n#.\n%ǝ+&4V40-#-1;)!2	ƴ#¥\"W^ëā\n\rq\n\n/+§Ɩ,ý4,\rH\Z\rČȚ¹#Ê`̌ţ Ŗ=ňŗ>\'͊5Èį'),(3884,1,'manage','Į8ٕ#̮'),(3884,1,'avoid','٫'),(3883,1,'mm','Ć'),(3882,1,'services','Ş9'),(3881,1,'unsuccessful','㥿'),(3881,1,'manage','ɧ'),(3881,1,'mm','᝷'),(3881,1,'services','ܒɀČÔf࠿ţŕlӳ-ѓ'),(3874,1,'services','GƠ'),(3875,1,'services','()ŏć7('),(3876,1,'services','#ß'),(3877,1,'avoid','â'),(3878,1,'asked','ः'),(3878,1,'manage','»'),(3878,1,'services','¡#ýǋ\r\r0NG785a4ª222Ʊ'),(3879,1,'manage','ЉͻᰄVਓేYHU(?UΔPఈ'),(3873,1,'services','ǩ'),(3728,1,'refusals',''),(3732,1,'refusals',''),(3737,1,'refusals',''),(3739,1,'refusals',''),(3740,1,'privacy','Î'),(3745,1,'refusals','Û'),(3748,1,'refusals',''),(3751,1,'services','S'),(3753,1,'rpc',''),(3754,1,'services','3Õ<Ă&'),(3755,1,'refusals',''),(3761,1,'refusals',''),(3769,1,'spcrit',''),(3782,1,'services','Č'),(3783,1,'fake','Ã'),(3784,1,'refusals',''),(3785,1,'refusals',''),(3788,1,'services',' 3D'),(3791,1,'row','a'),(3793,1,'thread',''),(3795,1,'thread','y'),(3814,1,'refusals',''),(3818,1,'refusals',''),(3819,1,'privacy','ç'),(3820,1,'privacy',''),(3827,1,'refusals','Ȇ'),(3833,1,'refusals',''),(3840,1,'services','¹!·ĝ '),(3850,1,'manage','%'),(3854,1,'refusals','Á'),(3856,1,'services','¯'),(3870,1,'services','¹'),(3871,1,'asked','㓏'),(3871,1,'avoid','ᭂ'),(3871,1,'dividing','㕫ᔑ'),(3871,1,'manage','㆞'),(3871,1,'mm','⎼c'),(3871,1,'privacy','㺼'),(3871,1,'row','ᴩϗ੗\n'),(3872,1,'rc','ɹ		~)/3M'),(3729,1,'refusals','Â'),(3892,1,'services','À'),(3891,1,'services','ˡ˗'),(3888,1,'manage','#EE)ǻĊÝ|9Ğ'),(3887,1,'jpg','౶s'),(3887,1,'services','ş¶ŕ,Vڃ'),(3888,1,'services','Q`c_Ȼ4«£zƣB\"5	'),(3890,1,'avoid','ΰ'),(3885,1,'services','Hà֫(íķ'),(3885,1,'manage','ɕ'),(3884,1,'services','hIV	_ÂɔÜ\"\n:36ě1Í\n,°ÂɔŅƇ*·'),(3880,1,'mm','⢧ӥƫ'),(3886,1,'services',';ü¾Ģ-\n	\rÅL'),(3880,1,'manage','ϘՓی⟞ഔ'),(3753,6,'rpc',''),(3890,1,'services','Ũ̽;\rg&$1ŘīTԭøˍˍå/н̲38'),(3769,6,'spcrit','1'),(3884,6,'services','ᑧ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict0A` ENABLE KEYS */;

--
-- Table structure for table `dict0B`
--

DROP TABLE IF EXISTS `dict0B`;
CREATE TABLE `dict0B` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict0B`
--


/*!40000 ALTER TABLE `dict0B` DISABLE KEYS */;
LOCK TABLES `dict0B` WRITE;
INSERT INTO `dict0B` VALUES (3752,1,'rrd','\n\r		'),(3751,1,'rrd','\nA<ð$\\'),(3751,1,'prediction','ʉ'),(3871,1,'rrd','0xǚ\Z*ï+~%Q\Zň!3\n*Ʋ(Ƹà%v!ª\r\r8yۀܦE¥þ\"	\Z+$s$\'Å.5kяPX\".	Ådû)%D)\n$\ZQG-\n	\n.ǃƊͦ\ZĮ\n\n\n\nK¢\'Ůà»đՠ =ԁ$@؉\n#'),(3879,1,'01','䵱ᣟ5'),(3873,1,'mbx','ό\''),(3879,1,'rrd','ህ゗= ˧#	+-]PzP+1'),(3879,1,'care','㾵'),(3751,1,'care','?ǃ'),(3721,1,'study','ő'),(3879,1,'usual','◯ò'),(3868,1,'study','P'),(3798,1,'avg','d.'),(3799,1,'avg','2;'),(3840,1,'locks','ȑ'),(3841,1,'01','<*'),(3862,1,'usual','Č'),(3869,1,'forms','Ó'),(3871,1,'01','ܸۋȩᆰՂ̆ĸಫ'),(3871,1,'184','䴢'),(3871,1,'care','खᱥ'),(3871,1,'december','⒏'),(3871,1,'getenv','ⴹ\n'),(3880,1,'resetting','め'),(3880,1,'study','ᖬ'),(3881,1,'care','⫕'),(3881,1,'forms','⯈'),(3881,1,'logmessages','Ԙᝨ'),(3881,1,'setfilter','ᩫÂ'),(3888,1,'care','ٶ'),(3751,6,'rrd','ʵ'),(3752,6,'rrd','ę');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict0B` ENABLE KEYS */;

--
-- Table structure for table `dict0C`
--

DROP TABLE IF EXISTS `dict0C`;
CREATE TABLE `dict0C` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict0C`
--


/*!40000 ALTER TABLE `dict0C` DISABLE KEYS */;
LOCK TABLES `dict0C` WRITE;
INSERT INTO `dict0C` VALUES (3856,1,'fork',''),(3849,1,'hear','8'),(3844,1,'generated','y'),(3820,1,'snmpv3','c'),(3781,1,'generated','J'),(3799,1,'printed','è'),(3813,1,'generated','R'),(3819,1,'generated','Õ'),(3819,1,'snmpv3','Th'),(3820,1,'generated','|'),(3879,1,'navigation','Кҕ8Ô±6lƍʦţHD᤺ƜBрঞΏࢹ¥'),(3871,1,'wrong','ඹ⧬İ!²äωౘˤ'),(3757,1,'generated','Ľ'),(3744,1,'snmpv3',' '),(3879,1,'inconvenience','ժ'),(3866,1,'generated',''),(3871,1,'generated','·ჲªภ๙'),(3874,1,'generated','ȭ&# '),(3871,1,'virtually','㰿ሗ'),(3871,1,'printed','ڳନࡽ^įQҖ࣌ጎ'),(3881,1,'installations','᱇'),(3881,1,'inconvenience','ͽ'),(3881,1,'generated','ݔ		ᜬ'),(3881,1,'contribute','ⱔ'),(3881,1,'assembly','ῆ'),(3880,1,'inconvenience','խ'),(3880,1,'navigation','Ĕ׼ҩɗ!6ޙ41ù²1	4Ħ5ࠜȑ*QT0.`L7த^`̖࿗'),(3881,1,'schemainfo','፱'),(3881,1,'tal','》\n.'),(3881,1,'navigation','⸚ৄ'),(3726,1,'clickable','\''),(3740,1,'snmpv3','f\ra('),(3879,1,'generated','ᩒ ⳤƛֆ˕Ï'),(3877,1,'generated','Ϟ¢ƾ|'),(3879,1,'printed','介'),(3879,1,'recognizes','慪'),(3871,1,'12357000','㪙'),(3870,1,'suited',''),(3723,1,'generated','1'),(3721,1,'suited','Ș'),(3880,1,'generated','໫⪽ſ\nà ̄\nđĶ\n'),(3884,1,'navigation','ޚ'),(3885,1,'generated','¡'),(3886,1,'navigation','ԣ'),(3887,1,'generated','ɫ଎'),(3888,1,'generated','ࣗ'),(3888,1,'installations','̧'),(3888,1,'navigation','ǲ'),(3888,1,'virtually','ࣛ'),(3889,1,'navigation','ˁ'),(3890,1,'allotted','ᙟ'),(3890,1,'generated','ۯ'),(3890,1,'navigation','ƃ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict0C` ENABLE KEYS */;

--
-- Table structure for table `dict0D`
--

DROP TABLE IF EXISTS `dict0D`;
CREATE TABLE `dict0D` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict0D`
--


/*!40000 ALTER TABLE `dict0D` DISABLE KEYS */;
LOCK TABLES `dict0D` WRITE;
INSERT INTO `dict0D` VALUES (3880,1,'depends','⠣'),(3879,1,'yyyymmdd','啇	'),(3880,1,'considered','ᆽñ'),(3871,1,'resolution','Շ(჉? ઐ&8z>'),(3871,1,'prefixes','㾟'),(3871,1,'cp','佤'),(3871,1,'didn','䪘״'),(3871,1,'identifier','㻕'),(3871,1,'ifinoctets','䄴'),(3871,1,'144','㘏ห;'),(3840,1,'resolution','ŷ'),(3820,1,'authnopriv','\\'),(3819,1,'authnopriv','µ'),(3799,1,'considered','f'),(3789,1,'considered','s'),(3778,1,'dba',''),(3756,1,'identifier','h'),(3871,1,'yyyymmdd','␩'),(3871,1,'considered','੫਻ᡥ؍'),(3879,1,'notification','យ࿞تǗ>>ฏᒞŸ^ൟ'),(3879,1,'identifier','奂a	'),(3879,1,'depends','⍪'),(3879,1,'considered','ⒾፏܴY'),(3879,1,'cdata','䦯\n)\n'),(3873,1,'depends','I'),(3875,1,'ifinoctets','¸*'),(3880,1,'resolution','༘ԁ'),(3880,1,'proactive','༖'),(3880,1,'notification','एੋJÖ0ԉňՐఓH\n£\n\n\r͐H\nh\n\n\rև5upȕN0?D#[bKA533¬	ʨ]'),(3751,1,'considered','Ć'),(3744,1,'considered','ö'),(3744,1,'identifier','Ì'),(3872,1,'notification','߅ńCIOÚ>'),(3872,1,'considered','̢'),(3754,1,'servhost','RÅ¯'),(3720,1,'widget','ĥ\r'),(3744,1,'authnopriv',''),(3881,1,'identifier','ⶐ'),(3881,1,'notification','ׅƦĎ'),(3884,1,'notification','ЉG/)ƹǼ֫G/)͐g'),(3884,1,'preserve','૬'),(3885,1,'notification','ӐįG:'),(3887,1,'notification','ܑįG:޵]'),(3889,1,'considered','ǨJ'),(3889,1,'notification','¯	0D+ŘeBè'),(3890,1,'considered','࢙'),(3890,1,'notification','ᝌԔ!'),(3890,1,'preserve','ັྪ'),(3891,1,'notification','ô>RCKCcūCCKc'),(3892,1,'notification','Đ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict0D` ENABLE KEYS */;

--
-- Table structure for table `dict0E`
--

DROP TABLE IF EXISTS `dict0E`;
CREATE TABLE `dict0E` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict0E`
--


/*!40000 ALTER TABLE `dict0E` DISABLE KEYS */;
LOCK TABLES `dict0E` WRITE;
INSERT INTO `dict0E` VALUES (3880,1,'public','/,Ǭ'),(3879,1,'public','/,ʐ姲'),(3879,1,'body','崑Ȭ'),(3879,1,'prefix','ᱻ'),(3879,1,'indexer','ᒋ	䉝҂'),(3871,1,'exchange','ᣌ'),(3721,1,'computer',''),(3720,1,'converted','Ɛ'),(3880,1,'clone','⚔പᐦ'),(3819,1,'prefix','Ô'),(3871,1,'public','㺰Ǌ)I'),(3871,1,'prefix','䁒'),(3877,1,'public','-,Ç>Ȅ®ØÊM'),(3873,1,'public','Х		'),(3873,1,'exchange','­Kʸ			'),(3872,1,'submitting','ׁ'),(3872,1,'public','\r'),(3871,1,'story','䫏'),(3881,1,'prefix','ⶖ'),(3879,1,'clone','⎥Qຉ'),(3878,1,'public','&,'),(3878,1,'exchange','Ǜŗ́'),(3819,1,'public','6'),(3820,1,'prefix','{'),(3820,1,'public','7'),(3827,1,'body','Ĝ'),(3829,1,'computer',')'),(3829,1,'public','o'),(3856,1,'public','Ą'),(3857,1,'public','>'),(3868,1,'exchange','²'),(3869,1,'public','!'),(3871,1,'alex','⡀ߕ$႙I྘'),(3871,1,'body','⼔\Z0L)಼'),(3871,1,'computer','㈮ᗴ'),(3751,1,'1x','ǡ'),(3721,1,'public','Ť'),(3742,1,'public','M'),(3744,1,'prefix','Ő'),(3744,1,'public','´'),(3745,1,'exchange',''),(3871,1,'ffff00','Ể'),(3881,1,'public','/,⹂ȠĤ!\rע	\rt'),(3884,1,'clone','+ૡ\n:'),(3886,1,'clone','|'),(3887,1,'clone','£'),(3888,1,'adjust','ލČ'),(3888,1,'exchange','ه'),(3888,1,'prefix','ڮ'),(3890,1,'adjust','ሡŚ'),(3720,7,'groundworkmonitoropensource','ƽ'),(3721,7,'groundworkmonitoropensource','ʞ'),(3722,7,'groundworkmonitoropensource','ť'),(3723,7,'groundworkmonitoropensource','F'),(3724,7,'groundworkmonitoropensource','i'),(3725,7,'groundworkmonitoropensource','c'),(3726,7,'groundworkmonitoropensource',''),(3727,7,'groundworkmonitoropensource','Ń'),(3728,7,'groundworkmonitoropensource','ĝ'),(3729,7,'groundworkmonitoropensource','Ö'),(3730,7,'groundworkmonitoropensource',''),(3731,7,'groundworkmonitoropensource',''),(3732,7,'groundworkmonitoropensource','Ğ'),(3733,7,'groundworkmonitoropensource',''),(3734,7,'groundworkmonitoropensource','ÿ'),(3735,7,'groundworkmonitoropensource','y'),(3736,7,'groundworkmonitoropensource','¿'),(3737,7,'groundworkmonitoropensource','ĝ'),(3738,7,'groundworkmonitoropensource',''),(3739,7,'groundworkmonitoropensource','ĝ'),(3740,7,'groundworkmonitoropensource','Ɗ'),(3741,7,'groundworkmonitoropensource','¼'),(3742,7,'groundworkmonitoropensource',''),(3743,7,'groundworkmonitoropensource',''),(3744,7,'groundworkmonitoropensource','ɞ'),(3745,7,'groundworkmonitoropensource','þ'),(3746,7,'groundworkmonitoropensource','5'),(3747,7,'groundworkmonitoropensource',']'),(3748,7,'groundworkmonitoropensource','Ġ'),(3749,7,'groundworkmonitoropensource','-'),(3750,7,'groundworkmonitoropensource','8'),(3751,7,'groundworkmonitoropensource','ʭ'),(3752,7,'groundworkmonitoropensource','đ'),(3753,7,'groundworkmonitoropensource',''),(3754,7,'groundworkmonitoropensource','̑'),(3755,7,'groundworkmonitoropensource','Á'),(3756,7,'groundworkmonitoropensource','÷'),(3757,7,'groundworkmonitoropensource','Ʃ'),(3758,7,'groundworkmonitoropensource','D'),(3759,7,'groundworkmonitoropensource','Ā'),(3760,7,'groundworkmonitoropensource','E'),(3761,7,'groundworkmonitoropensource','Ġ'),(3762,7,'groundworkmonitoropensource','á'),(3763,7,'groundworkmonitoropensource','Ļ'),(3764,7,'groundworkmonitoropensource','@'),(3765,7,'groundworkmonitoropensource','à'),(3766,7,'groundworkmonitoropensource','ó'),(3767,7,'groundworkmonitoropensource','*'),(3768,7,'groundworkmonitoropensource','+'),(3769,7,'groundworkmonitoropensource','('),(3770,7,'groundworkmonitoropensource','('),(3771,7,'groundworkmonitoropensource','('),(3772,7,'groundworkmonitoropensource','('),(3773,7,'groundworkmonitoropensource',','),(3774,7,'groundworkmonitoropensource','-'),(3775,7,'groundworkmonitoropensource','+'),(3776,7,'groundworkmonitoropensource','ĩ'),(3777,7,'groundworkmonitoropensource',','),(3778,7,'groundworkmonitoropensource','ú'),(3779,7,'groundworkmonitoropensource','6'),(3780,7,'groundworkmonitoropensource','Ʊ'),(3781,7,'groundworkmonitoropensource',''),(3782,7,'groundworkmonitoropensource','Ƙ'),(3783,7,'groundworkmonitoropensource','Ù'),(3784,7,'groundworkmonitoropensource','Ġ'),(3785,7,'groundworkmonitoropensource','Ġ'),(3786,7,'groundworkmonitoropensource','A'),(3787,7,'groundworkmonitoropensource','¬'),(3788,7,'groundworkmonitoropensource','Þ'),(3789,7,'groundworkmonitoropensource',''),(3790,7,'groundworkmonitoropensource','U'),(3791,7,'groundworkmonitoropensource','²'),(3792,7,'groundworkmonitoropensource','f'),(3793,7,'groundworkmonitoropensource','ª'),(3794,7,'groundworkmonitoropensource',')'),(3795,7,'groundworkmonitoropensource',''),(3796,7,'groundworkmonitoropensource','+'),(3797,7,'groundworkmonitoropensource','n'),(3798,7,'groundworkmonitoropensource','ò'),(3799,7,'groundworkmonitoropensource','Ƒ'),(3800,7,'groundworkmonitoropensource','G'),(3801,7,'groundworkmonitoropensource','8'),(3802,7,'groundworkmonitoropensource','>'),(3803,7,'groundworkmonitoropensource','à'),(3804,7,'groundworkmonitoropensource','·'),(3805,7,'groundworkmonitoropensource','Ï'),(3806,7,'groundworkmonitoropensource','t'),(3807,7,'groundworkmonitoropensource','C'),(3808,7,'groundworkmonitoropensource','|'),(3809,7,'groundworkmonitoropensource','f'),(3810,7,'groundworkmonitoropensource','0'),(3811,7,'groundworkmonitoropensource','È'),(3812,7,'groundworkmonitoropensource','5'),(3813,7,'groundworkmonitoropensource',''),(3814,7,'groundworkmonitoropensource','Ġ'),(3815,7,'groundworkmonitoropensource','n'),(3816,7,'groundworkmonitoropensource','T'),(3817,7,'groundworkmonitoropensource','1'),(3818,7,'groundworkmonitoropensource','Ġ'),(3819,7,'groundworkmonitoropensource','Ģ'),(3820,7,'groundworkmonitoropensource','ŏ'),(3821,7,'groundworkmonitoropensource','='),(3822,7,'groundworkmonitoropensource','ā'),(3823,7,'groundworkmonitoropensource','Î'),(3824,7,'groundworkmonitoropensource','ř'),(3825,7,'groundworkmonitoropensource','Ņ'),(3826,7,'groundworkmonitoropensource','('),(3827,7,'groundworkmonitoropensource','˗'),(3828,7,'groundworkmonitoropensource',''),(3829,7,'groundworkmonitoropensource','y'),(3830,7,'groundworkmonitoropensource',''),(3831,7,'groundworkmonitoropensource','Ó'),(3832,7,'groundworkmonitoropensource','P'),(3833,7,'groundworkmonitoropensource','Ġ'),(3834,7,'groundworkmonitoropensource','»'),(3835,7,'groundworkmonitoropensource',''),(3836,7,'groundworkmonitoropensource','e'),(3837,7,'groundworkmonitoropensource','ħ'),(3838,7,'groundworkmonitoropensource','D'),(3839,7,'groundworkmonitoropensource','6'),(3840,7,'groundworkmonitoropensource','̚'),(3841,7,'groundworkmonitoropensource','¯'),(3842,7,'groundworkmonitoropensource','?'),(3843,7,'groundworkmonitoropensource','k'),(3844,7,'groundworkmonitoropensource','×'),(3845,7,'groundworkmonitoropensource','{'),(3846,7,'groundworkmonitoropensource','ĺ'),(3847,7,'groundworkmonitoropensource','V'),(3848,7,'groundworkmonitoropensource','±'),(3849,7,'groundworkmonitoropensource','}'),(3850,7,'groundworkmonitoropensource','K'),(3851,7,'groundworkmonitoropensource',''),(3852,7,'groundworkmonitoropensource',','),(3853,7,'groundworkmonitoropensource','T'),(3854,7,'groundworkmonitoropensource','ń'),(3855,7,'groundworkmonitoropensource',';'),(3856,7,'groundworkmonitoropensource','Ƒ'),(3857,7,'groundworkmonitoropensource','`'),(3858,7,'groundworkmonitoropensource','5'),(3859,7,'groundworkmonitoropensource','C'),(3860,7,'groundworkmonitoropensource','H'),(3861,7,'groundworkmonitoropensource','3'),(3862,7,'groundworkmonitoropensource','ě'),(3863,7,'groundworkmonitoropensource','Ľ'),(3864,7,'groundworkmonitoropensource',''),(3865,7,'groundworkmonitoropensource','¢'),(3866,7,'groundworkmonitoropensource','¸'),(3867,7,'groundworkmonitoropensource','1'),(3868,7,'groundworkmonitoropensource','ė'),(3869,7,'groundworkmonitoropensource','Ě'),(3870,7,'groundworkmonitoropensource','Ĥ'),(3871,7,'groundworkmonitoropensource','僗'),(3872,7,'groundworkmonitoropensource','೰'),(3873,7,'groundworkmonitoropensource','դ'),(3874,7,'groundworkmonitoropensource','Ϯ'),(3875,7,'groundworkmonitoropensource','̈́'),(3876,7,'groundworkmonitoropensource','ƫ'),(3877,7,'groundworkmonitoropensource','ތ'),(3878,7,'groundworkmonitoropensource','ृ'),(3879,7,'groundworkmonitoropensource','朱'),(3880,7,'groundworkmonitoropensource','䠿'),(3881,7,'groundworkmonitoropensource','㦿'),(3882,7,'groundworkmonitoropensource','Ư'),(3883,7,'groundworkmonitoropensource','Ŋ'),(3884,7,'groundworkmonitoropensource','ᑣ'),(3885,7,'groundworkmonitoropensource','৚'),(3886,7,'groundworkmonitoropensource','Է'),(3887,7,'groundworkmonitoropensource','ᄑ'),(3888,7,'groundworkmonitoropensource','ऌ'),(3889,7,'groundworkmonitoropensource','Ӽ'),(3890,7,'groundworkmonitoropensource','ᾐ'),(3891,7,'groundworkmonitoropensource','ً'),(3892,7,'groundworkmonitoropensource','ǁ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict0E` ENABLE KEYS */;

--
-- Table structure for table `dict0F`
--

DROP TABLE IF EXISTS `dict0F`;
CREATE TABLE `dict0F` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict0F`
--


/*!40000 ALTER TABLE `dict0F` DISABLE KEYS */;
LOCK TABLES `dict0F` WRITE;
INSERT INTO `dict0F` VALUES (3783,1,'associate',''),(3874,1,'partition','Ȧ'),(3766,1,'processes',''),(3880,1,'good','ℏᇆ'),(3881,1,'processes','←ࢶ'),(3880,1,'formats','ќ゚ͅ૲'),(3880,1,'reconfigure','㔭'),(3791,1,'socket',''),(3879,1,'associate','⊡ଊ'),(3871,1,'ffc000','Ệ'),(3870,1,'socket','Ā'),(3740,1,'processes','l+X'),(3739,1,'socket','¸'),(3737,1,'socket','¸'),(3732,1,'socket','¹'),(3730,1,'ith','m'),(3728,1,'socket','¸'),(3880,1,'percentages','䂉'),(3874,1,'processes','˔\r£'),(3870,1,'candidate','Ĉ'),(3868,1,'release',''),(3862,1,'good','('),(3780,1,'processes','q'),(3759,1,'processes','F'),(3757,1,'processes','	H.5?\n\Z'),(3880,1,'force','⭥5қ'),(3879,1,'scheduling','儕ѐ'),(3879,1,'release','́͹\rN\r'),(3879,1,'processes','≷෎ᨐ\rᓭ\r'),(3879,1,'partition','Ṕ'),(3879,1,'good','தᒢ঻Ɯߗӳ'),(3879,1,'formats','۸姷'),(3879,1,'constructing','ݑ'),(3881,1,'good','ㆼ'),(3881,1,'associate','⃬'),(3880,1,'unavailable','᭺'),(3880,1,'scheduling','ႅ፪ϧƽ'),(3880,1,'release','ɝƞJ\rɧ๦'),(3721,1,'good',''),(3871,1,'good','ቷ⥃\r9|\rևѺ'),(3871,1,'release','Ú'),(3870,1,'release','d£'),(3741,1,'processes','b'),(3878,1,'processes','ƚ'),(3784,1,'socket','»'),(3785,1,'socket','»'),(3761,1,'socket','»'),(3813,1,'processes','+'),(3871,1,'formats','␞'),(3871,1,'force','ཧڇ'),(3854,1,'socket',' Gx'),(3846,1,'partition','Ø&'),(3833,1,'socket','»'),(3818,1,'socket','»'),(3814,1,'socket','»'),(3872,1,'force','̙¶Ë'),(3871,1,'translates','㗧Ᏸ'),(3720,1,'release',':'),(3748,1,'socket','»'),(3879,1,'committed','぀⽝'),(3881,1,'release','ࠖ̈฀'),(3881,1,'socket','⎷\rd'),(3884,1,'force','૓'),(3884,1,'formats','႗'),(3884,1,'partition','ܠ'),(3884,1,'scheduling','ɺO়O'),(3885,1,'good','Žײ'),(3886,1,'partition','Ш'),(3887,1,'formats','൪'),(3887,1,'reconfigure','ѳ'),(3888,1,'202','ࢯ'),(3888,1,'force','˿&1'),(3888,1,'good','ۄ'),(3889,1,'committed','ҡ'),(3889,1,'smallest','ƺ'),(3890,1,'good','ूࢋ'),(3890,1,'scheduling','༢<˔	ŏ	'),(3892,1,'associate','\\');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict0F` ENABLE KEYS */;

--
-- Table structure for table `dict10`
--

DROP TABLE IF EXISTS `dict10`;
CREATE TABLE `dict10` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict10`
--


/*!40000 ALTER TABLE `dict10` DISABLE KEYS */;
LOCK TABLES `dict10` WRITE;
INSERT INTO `dict10` VALUES (3880,1,'calendar','ẈHᛠ'),(3890,1,'assign','϶'),(3879,1,'assign','઼ᕋ࣐#˼t½ആ|@РӺᴉ'),(3879,1,'designing','㒖'),(3888,1,'assign','oЍJqđ$:Nì'),(3890,1,'plan','̿'),(3889,1,'assign','δm 	'),(3879,1,'2006','晏(\r	\r	\r'),(3721,1,'gpl','š'),(3879,1,'gpl','̷'),(3741,1,'consumed','°'),(3744,1,'2006','A'),(3756,1,'presents','·'),(3792,1,'blende','U'),(3856,1,'tty',''),(3863,1,'compared','į'),(3868,1,'2006','«'),(3871,1,'calculator','ᛦ㘎'),(3871,1,'equals','☇࿩'),(3886,1,'assign','ɳMƱ'),(3885,1,'assign','ȼ٢HT'),(3881,1,'scripts','Ḃܳ¨s'),(3881,1,'implements','⢡Ɯ޿Ƌµ'),(3881,1,'feedername','Ụy'),(3880,1,'gpl','ʡ'),(3880,1,'scripts','ឳ'),(3880,1,'troubleshooting','ᐖ'),(3881,1,'assign','す½7'),(3881,1,'designing','⨛'),(3878,1,'scripts','Ѱo5a7/'),(3874,1,'scripts','Aĕ\r'),(3875,1,'scripts','ĳ'),(3876,1,'scripts','£'),(3877,1,'assign','õ'),(3887,1,'plan','థ'),(3887,1,'assign','Ǫ8ഁ'),(3891,1,'assign','ϸ'),(3873,1,'scripts','Kj'),(3879,1,'scripts','ᭇ֔២વඡ0ʏ'),(3879,1,'lie','㟸΂'),(3871,1,'tune','̊╅\"7)k'),(3871,1,'scripts','s'),(3871,1,'presents','^'),(3871,1,'plan','Ʉ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict10` ENABLE KEYS */;

--
-- Table structure for table `dict11`
--

DROP TABLE IF EXISTS `dict11`;
CREATE TABLE `dict11` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict11`
--


/*!40000 ALTER TABLE `dict11` DISABLE KEYS */;
LOCK TABLES `dict11` WRITE;
INSERT INTO `dict11` VALUES (3879,1,'kind','㏫ϧ'),(3879,1,'engineer','ԭ\\;g'),(3871,1,'consolidation','ЕS\Zª\Zи೻ͭݢ≼'),(3871,1,'clear','ಖⲂ'),(3879,1,'specific','н˪̆Ļˬ৖՝чC#lᆭ\nÎûvሯ܂'),(3783,1,'specific',''),(3782,1,'specific','Ô'),(3757,1,'rszdt','Æ'),(3756,1,'invocation',''),(3752,1,'2fms','ñ'),(3720,1,'specific','à'),(3871,1,'specific','ޒ㗐'),(3720,1,'subwidgets','Ņ'),(3871,1,'lazy','ཕյᦜ\'rK'),(3871,1,'tutorial','΄ⲋ\nG!׀ʽ\ZȐډ'),(3871,1,'kind','㷍෶'),(3871,1,'circumstances','亢'),(3870,1,'tutorial',';'),(3856,1,'invocation','ħ'),(3846,1,'clear','µ'),(3837,1,'specific',''),(3829,1,'printer',''),(3828,1,'ux','$'),(3799,1,'kind','ć('),(3795,1,'clear','g'),(3881,1,'consolidation','Øзqર௿=\n۞'),(3871,1,'mibs','䆓'),(3871,1,'printer','㷚'),(3793,1,'clear','t'),(3879,1,'distinguised','ᴆ'),(3879,1,'clear','旷'),(3879,1,'consolidation','䨹āB	A'),(3879,1,'circumstances','⾚.>Ű൘;'),(3872,1,'specific','Ծ'),(3872,1,'circumstances','گʲǄ.>'),(3872,1,'clear','ʠ'),(3871,1,'value2','ế'),(3880,1,'specific','ঐࢥϗ\ZɖЁ࣐ʟֿѝᇇ'),(3880,1,'engineer','԰\\;g'),(3880,1,'duplicated','䞩'),(3880,1,'circumstances','ᅟ'),(3879,1,'value2','䗈'),(3744,1,'specific','>'),(3744,1,'mibs','8¡'),(3742,1,'printer','	\''),(3881,1,'engineer','̀\\;g'),(3881,1,'entitytype','⃱'),(3881,1,'gethostgroup','हĉ৙'),(3881,1,'kind','⎢'),(3881,1,'piece','⭲'),(3881,1,'specific','ȽӠ஄7çĝŪÚܫǠ΁ኀ'),(3881,1,'timeok','࡭'),(3881,1,'tutorial','῔'),(3882,1,'specific','Ā'),(3884,1,'specific','۟ƅj'),(3885,1,'specific','Ç,'),(3888,1,'circumstances','Ӧ'),(3888,1,'unlimited','ࣜ'),(3890,1,'circumstances','ᬗ'),(3890,1,'clear','ςᩲ'),(3890,1,'specific','ን'),(3891,1,'circumstances','Ø.>'),(3742,6,'printer','');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict11` ENABLE KEYS */;

--
-- Table structure for table `dict12`
--

DROP TABLE IF EXISTS `dict12`;
CREATE TABLE `dict12` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict12`
--


/*!40000 ALTER TABLE `dict12` DISABLE KEYS */;
LOCK TABLES `dict12` WRITE;
INSERT INTO `dict12` VALUES (3871,1,'works','ӖΙ൹ᒙؾwƯöʾ½̬Ǎȹॶǁһ'),(3873,1,'works','þ'),(3877,1,'content','֟'),(3840,1,'works','ǡ'),(3880,1,'content','๜ڀ㊂'),(3880,1,'bug','ш'),(3879,1,'x509','᥼'),(3879,1,'works','ᇩ̤ॢࢌ'),(3879,1,'week','⻀'),(3879,1,'titles','⨘Ɯ'),(3879,1,'standards','䏝ō'),(3879,1,'performs','↎'),(3879,1,'perform','ᥙ-ヘٙມְ#'),(3799,1,'works','Ī'),(3827,1,'content','ĈKñ'),(3827,1,'x509','ɑ'),(3720,1,'works','Ż'),(3871,1,'perform','ᤢ\''),(3871,1,'moves','ヾ'),(3871,1,'huge','䲌'),(3871,1,'exactly','ϖƪ˰ᵡҼᢥ୕P'),(3880,1,'works','য়'),(3880,1,'week','䁻'),(3871,1,'communication','ޓ'),(3880,1,'titles','䛸'),(3872,1,'identify','޵²б'),(3792,1,'works',''),(3792,1,'village','X'),(3750,1,'typ',')'),(3747,1,'works','8'),(3744,1,'communication','±'),(3722,1,'works',''),(3721,1,'works','ȡ'),(3877,1,'works','ȑ'),(3872,1,'communication','Ջ'),(3873,1,'exactly','ȴ'),(3872,1,'content','ҷ'),(3880,1,'identify','ण࣓◗'),(3879,1,'identify','⟿3ҫƩ͎૲©ᓤ'),(3879,1,'huge','ό'),(3879,1,'bug','ۤ'),(3879,1,'content','嘽ׯ	/'),(3871,1,'week','ᄯዖℷ'),(3871,1,'titles','⸷'),(3880,1,'huge','̶'),(3872,1,'works','å'),(3862,1,'works','´'),(3846,1,'works','è'),(3863,1,'perform','R4'),(3871,1,'bug','䵺'),(3871,1,'4am','▲'),(3870,1,'works','7'),(3869,1,'works','f'),(3881,1,'communication','᯺ཱྀ'),(3881,1,'content','⠇ŸÖƯþ'),(3881,1,'perform','௞ƒ)WN◸Ý'),(3881,1,'performs','ലፐྮ'),(3881,1,'serviceid','ᒟ'),(3881,1,'standards','ߚ'),(3881,1,'works','⻓؃'),(3883,1,'identify','Î'),(3883,1,'week','ë'),(3884,1,'identify','ᐩ'),(3884,1,'performs','ჭ'),(3886,1,'identify','ë3'),(3886,1,'perform','Ї'),(3886,1,'performs','Ђ'),(3886,1,'works','ю'),(3887,1,'identify','ē3ࣄظ'),(3888,1,'standards','Â΅'),(3890,1,'audio','ݏ\n<'),(3890,1,'works','̲'),(3891,1,'identify','ϲ'),(3891,1,'works','ƼÊɉC'),(3892,1,'performs','Ɩ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict12` ENABLE KEYS */;

--
-- Table structure for table `dict13`
--

DROP TABLE IF EXISTS `dict13`;
CREATE TABLE `dict13` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict13`
--


/*!40000 ALTER TABLE `dict13` DISABLE KEYS */;
LOCK TABLES `dict13` WRITE;
INSERT INTO `dict13` VALUES (3880,1,'windows','ީႛ᩶ø ؚʽ'),(3881,1,'represent','ᦅ'),(3881,1,'urgency','Ϭ'),(3888,1,'separately','ı'),(3869,1,'windows','A'),(3871,1,'bogaerdt','⡃ߙ$⁾'),(3871,1,'feed','ީ⇋थבᗳ'),(3871,1,'gifs','࿍Хᯞ\'ನJ'),(3871,1,'poll','|'),(3871,1,'xx','⬊'),(3872,1,'pid','ͅ5}$B'),(3873,1,'windows','gKª'),(3877,1,'separately','ݘ'),(3878,1,'windows','Ǆ\r\r\r\n\Z\nʜ\"'),(3763,6,'pgsql','Ń'),(3819,1,'bulk','N'),(3809,1,'wload1','!'),(3782,1,'windows',' ĝ'),(3763,1,'pgsql',''),(3880,1,'urgency','ל'),(3880,1,'represent','᥶3ઢᓿ'),(3880,1,'mouse','᧵ן͓Ѳ'),(3890,1,'mouse','᳡'),(3888,1,'windows','׷æ*;è\n.5	'),(3879,1,'urgency','י'),(3844,1,'windows',''),(3727,1,'networkupstools','$Ē'),(3879,1,'represent','墎Ś'),(3879,1,'feed','憚'),(3881,1,'getserviceidsforhost','ᘠ'),(3881,1,'feed','ٟᤡİƮ'),(3890,1,'xx','ᇿŚ'),(3890,1,'pid','ล'),(3820,1,'bulk','O'),(3841,1,'xx','h');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict13` ENABLE KEYS */;

--
-- Table structure for table `dict14`
--

DROP TABLE IF EXISTS `dict14`;
CREATE TABLE `dict14` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict14`
--


/*!40000 ALTER TABLE `dict14` DISABLE KEYS */;
LOCK TABLES `dict14` WRITE;
INSERT INTO `dict14` VALUES (3819,1,'23',''),(3721,1,'parts','ª'),(3875,1,'accessible','ȝ'),(3874,1,'user22','±\" '),(3883,1,'23','Ħ'),(3881,1,'parts','⥒'),(3876,1,'accessible','ƅ'),(3880,1,'accessible','ಜRୄk8ƫ8࠮ᕳ'),(3879,1,'generating','㢔'),(3879,1,'form','☰ࢨყࠤO'),(3871,1,'form','ةkᷞL੪,ज़шǳഞ'),(3879,1,'accessible','୹̄t'),(3875,1,'generating',''),(3875,1,'discards','Ȭ'),(3871,1,'difficult','㻦'),(3871,1,'23','຤ᡥ⋕'),(3871,1,'colors','ᖝ4⏖1ȑ'),(3883,1,'form','ă'),(3885,1,'form','Ż'),(3880,1,'parts','ᤃ'),(3872,1,'1b','य'),(3892,1,'escape','ů'),(3881,1,'form','ૹ₵'),(3881,1,'difficult','㐖'),(3881,1,'accessible','ୢ'),(3878,1,'23','ι'),(3876,1,'generating','o'),(3874,1,'generating',''),(3874,1,'accessible','͐'),(3871,1,'rrdxport','⧇'),(3871,1,'parts','ቛᄙᳰ'),(3880,1,'form','ॊ׳'),(3880,1,'generating','Ī&㊜̴ǶȊÙ͏ëŢĨ'),(3871,1,'moment','⎉ÙRຟɕ'),(3871,1,'idea','ქ؇〓'),(3879,1,'23','⻻⚻E'),(3870,1,'idea','3'),(3854,1,'escape',''),(3827,1,'form','Ň'),(3890,1,'idea','̹@'),(3888,1,'idea','ۅ'),(3887,1,'generating','ජ'),(3885,1,'idea','ݰ'),(3793,1,'form','m'),(3871,1,'generating','ྺᧅ'),(3791,1,'form','£'),(3782,1,'service3','Ę'),(3804,1,'form','R'),(3795,1,'form','`');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict14` ENABLE KEYS */;

--
-- Table structure for table `dict15`
--

DROP TABLE IF EXISTS `dict15`;
CREATE TABLE `dict15` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict15`
--


/*!40000 ALTER TABLE `dict15` DISABLE KEYS */;
LOCK TABLES `dict15` WRITE;
INSERT INTO `dict15` VALUES (3871,1,'oid','㺌E!!'),(3856,1,'plugin','\r\n'),(3849,1,'plugin','\r	'),(3757,1,'plugin','\r	'),(3758,1,'plugin','\r\n'),(3759,1,'plugin','\r	'),(3812,1,'plugin','\r\n'),(3787,1,'plugin','\r\n\Z'),(3786,1,'plugin','\r'),(3841,1,'plugin','\r'),(3819,1,'plugin','\r\nõ'),(3818,1,'plugin','\r	'),(3830,1,'plugin','\r+\r'),(3831,1,'plugin','\r	u>'),(3832,1,'plugin','\r'),(3833,1,'plugin','\r	'),(3834,1,'plugin','\r	'),(3835,1,'plugin','\r	.\''),(3836,1,'plugin','\r'),(3837,1,'plugin','\r'),(3815,1,'plugin','\r'),(3811,1,'plugin','\r	'),(3875,1,'ifoutdiscards','Ä'),(3797,1,'plugin','\r'),(3796,1,'plugin','\r'),(3796,1,'mssqlserver',''),(3871,1,'architecture','ῆ'),(3871,1,'7th','㛻'),(3871,1,'365','┯'),(3871,1,'22','ແ'),(3868,1,'plugin','­<'),(3867,1,'plugin','\r'),(3866,1,'plugin','\r\n'),(3865,1,'plugin','\r'),(3864,1,'plugin','\r'),(3863,1,'plugin','\r	­C'),(3862,1,'testing','z'),(3874,1,'22','̶§'),(3873,1,'plugin','Š'),(3873,1,'cd','Ɗ'),(3873,1,'arg4','̌Ȁ(%'),(3872,1,'plugin','ޏ'),(3872,1,'period','ऊ]'),(3773,1,'plugin','\r'),(3772,1,'plugin','\r'),(3771,1,'plugin','\r'),(3770,1,'plugin','\r'),(3769,1,'plugin','\r'),(3768,1,'plugin','\r'),(3760,1,'plugin','\r'),(3761,1,'plugin','\r	'),(3762,1,'plugin','\ra3'),(3762,1,'probe','§'),(3763,1,'plugin','\r09'),(3764,1,'plugin','\r	'),(3765,1,'plugin','\r'),(3766,1,'plugin','\r	µ'),(3767,1,'plugin','\r'),(3840,1,'plugin','\r	ß\rŠ'),(3838,1,'plugin','\r	'),(3839,1,'plugin','\r'),(3829,1,'plugin','\r	\''),(3828,1,'plugin','\r'),(3827,1,'plugin','\r	ǠD'),(3826,1,'plugin','\r\n'),(3825,1,'plugin','\r'),(3824,1,'plugin','\r'),(3823,1,'xp','F '),(3823,1,'plugin','\r'),(3822,1,'plugin','\r	\n'),(3876,1,'plugin','Y'),(3875,1,'plugin',''),(3862,1,'plugin','\r\n'),(3861,1,'plugin','\r\n'),(3860,1,'plugin','\r'),(3858,1,'plugin','\r'),(3853,1,'plugin','\r'),(3777,1,'plugin','\r'),(3778,1,'plugin','\r	'),(3779,1,'plugin','\r'),(3780,1,'plugin','\r	ő'),(3781,1,'plugin','\r'),(3782,1,'plugin','\r	'),(3782,1,'xp','#'),(3783,1,'plugin','\r	f'),(3784,1,'plugin','\r	'),(3785,1,'plugin','\r	'),(3748,1,'plugin','\r	'),(3846,1,'plugin','\r	'),(3753,1,'plugin','\r'),(3754,1,'plugin','\rŧ'),(3755,1,'plugin','\r	x'),(3756,1,'plugin','\r	À'),(3775,1,'plugin','\r'),(3774,1,'plugin','\r'),(3726,1,'plugin',''),(3725,1,'plugin',''),(3842,1,'plugin','\r'),(3794,1,'plugin','\r'),(3793,1,'plugin','\r'),(3859,1,'plugin','\r\n'),(3855,1,'plugin','\r'),(3854,1,'plugin','\r	'),(3848,1,'plugin','\r	'),(3847,1,'plugin','\r\n'),(3850,1,'plugin','\r'),(3820,1,'plugin','\ræ'),(3727,1,'plugin','\Zn\Z='),(3728,1,'plugin',''),(3729,1,'plugin',''),(3730,1,'plugin',''),(3731,1,'plugin',''),(3732,1,'plugin',''),(3733,1,'plugin','\n'),(3734,1,'freespace','K'),(3734,1,'plugin','\r\n'),(3735,1,'plugin','W'),(3736,1,'plugin',''),(3737,1,'plugin',''),(3738,1,'22','F'),(3738,1,'plugin',''),(3739,1,'plugin',''),(3740,1,'oid','¤'),(3740,1,'plugin','á'),(3741,1,'plugin',''),(3742,1,'plugin','\rd'),(3743,1,'plugin',''),(3744,1,'oid','K}_\''),(3744,1,'plugin','ł2)'),(3745,1,'addr','+'),(3745,1,'plugin',''),(3746,1,'plugin','\r\n'),(3747,1,'plugin','\r'),(3723,1,'plugin','	'),(3724,1,'plugin','	'),(3817,1,'plugin','\r'),(3798,1,'plugin','\r	»'),(3799,1,'plugin','\r	Ô-.'),(3800,1,'plugin','\r\n'),(3801,1,'plugin','\r'),(3802,1,'plugin','\r'),(3803,1,'plugin','\r\n\\=$'),(3795,1,'plugin','\r'),(3841,1,'arg4','¤'),(3814,1,'plugin','\r	'),(3871,1,'areas','ഊ'),(3871,1,'period','ǢĞࡑ㘻д'),(3874,1,'plugin','}Ê'),(3872,1,'cd','ǃ'),(3860,1,'period','%'),(3788,1,'plugin','\r­'),(3789,1,'plugin','\r	'),(3790,1,'plugin','\r9'),(3791,1,'plugin','\r'),(3792,1,'mssqlserver',''),(3752,1,'plugin','\r'),(3750,1,'plugin','\r\n'),(3751,1,'plugin','\ræ4.Ğ'),(3816,1,'plugin','\r'),(3813,1,'plugin','\r'),(3804,1,'plugin','\r'),(3805,1,'plugin','\r'),(3806,1,'plugin','\r\n'),(3807,1,'plugin','\r'),(3808,1,'plugin','\rX'),(3809,1,'plugin','\r	'),(3810,1,'plugin','\r'),(3821,1,'plugin','\r'),(3871,1,'mktime','╷'),(3871,1,'factor','ৄࠂ'),(3749,1,'plugin','\r\n'),(3871,1,'grprint','ᬌ'),(3843,1,'plugin','\rG'),(3845,1,'plugin','\rW'),(3859,1,'oid','*'),(3857,1,'plugin','\r\n'),(3851,1,'plugin','\rw'),(3852,1,'plugin','\r\n'),(3722,1,'plugin',''),(3844,1,'plugin','\r'),(3792,1,'plugin','\r'),(3776,1,'plugin','\r\nÿ'),(3877,1,'architecture','ԯ'),(3877,1,'cd','ǋå'),(3877,1,'plugin','Ժ/'),(3878,1,'22','Ϋ'),(3878,1,'plugin','ǉ \"  \"\"̄33522220030%/'),(3879,1,'365','ӽ'),(3879,1,'architecture','˦'),(3879,1,'areas','ⷪ'),(3879,1,'assignment','ⷳ'),(3879,1,'cd','搆ǔ'),(3879,1,'deployments','᧭'),(3879,1,'period','Ⓣউgl࿕?'),(3879,1,'plugin','͌ᨁ V\r¼♵žǺϓ'),(3879,1,'pst','Ӱ'),(3879,1,'testing','仕'),(3880,1,'365','Ԁ'),(3880,1,'architecture','Ʌഓ '),(3880,1,'areas','᪱࠳ᖼ'),(3880,1,'icons','๳ଂ3ஷฑ'),(3880,1,'period','ᆱ۠͢˂ɱᔍ\ZňƯʻг˨'),(3880,1,'plugin','ʶ㇗'),(3880,1,'pst','ӳ'),(3881,1,'365','̐'),(3881,1,'architecture','Ʒ˒´ڂᆯb฼'),(3881,1,'areas','᫹'),(3881,1,'plugin','ۚ?'),(3881,1,'pst','̃'),(3881,1,'testing','㏎֙'),(3883,1,'period','\'\r\n'),(3884,1,'period','ȝȲޞȲ'),(3884,1,'plugin','ܕ'),(3885,1,'period','ٌ$'),(3887,1,'icons','ʢ஑'),(3887,1,'period','ࢍ$'),(3887,1,'worry','˅஑'),(3888,1,'assignment','7&Τ5!ıIđĝ'),(3889,1,'period','Ǆ˸'),(3889,1,'xodtemplate','ӭ'),(3890,1,'factor','ቔ<'),(3890,1,'interleaving','ቝ'),(3890,1,'plugin','ʴ'),(3890,1,'testing','ᇕ'),(3891,1,'period','ƅ¼Ȼ5'),(3892,1,'plugin','Z[');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict15` ENABLE KEYS */;

--
-- Table structure for table `dict16`
--

DROP TABLE IF EXISTS `dict16`;
CREATE TABLE `dict16` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict16`
--


/*!40000 ALTER TABLE `dict16` DISABLE KEYS */;
LOCK TABLES `dict16` WRITE;
INSERT INTO `dict16` VALUES (3856,1,'hostname','E'),(3763,1,'integer','@?'),(3743,1,'integer','`\r'),(3744,1,'hostname',''),(3744,1,'integer','u^ã'),(3745,1,'hostname','F'),(3745,1,'integer','OH*'),(3748,1,'hostname','['),(3748,1,'integer','dZ\r*'),(3754,1,'integer','\"\"		\n'),(3755,1,'hostname','<'),(3755,1,'integer','E3'),(3845,1,'hostname','*'),(3844,1,'integer','k'),(3861,1,'lpt',''),(3859,1,'hostname',','),(3858,1,'hostname','+'),(3857,1,'integer','A'),(3857,1,'hostname','.'),(3856,1,'integer','N'),(3763,1,'hostname','7'),(3762,1,'integer','^'),(3762,1,'hostname','K'),(3761,1,'integer','dZ\r*'),(3844,1,'hostname','8'),(3843,1,'hostname','.'),(3848,1,'hostname','H'),(3846,1,'integer','V8'),(3854,1,'hostname','_'),(3814,1,'hostname','['),(3813,1,'integer','C'),(3813,1,'aid',':'),(3785,1,'integer','dZ\r*'),(3785,1,'hostname','['),(3849,1,'integer','K'),(3761,1,'hostname','['),(3820,1,'ifoperstatus',''),(3791,1,'hostname','x'),(3790,1,'hostname',')'),(3765,1,'hostname','\''),(3764,1,'hostname','&'),(3863,1,'integer','F'),(3864,1,'hostname','>'),(3833,1,'integer','dZ\r*'),(3833,1,'hostname','['),(3793,1,'integer','K'),(3793,1,'hostname','B'),(3784,1,'hostname','['),(3782,1,'integer','E'),(3784,1,'integer','dZ\r*'),(3834,1,'hostname','Q'),(3831,1,'integer',''),(3864,1,'integer','Q'),(3778,1,'hostname','!a'),(3838,1,'integer','+'),(3828,1,'hostname','R'),(3811,1,'integer','N]'),(3811,1,'hostname','E'),(3853,1,'hostname','2'),(3815,1,'hostname','+'),(3814,1,'integer','dZ\r*'),(3789,1,'integer','l'),(3854,1,'integer','os\r*'),(3848,1,'integer','QC'),(3766,1,'integer','RC'),(3766,1,'hostname','I'),(3756,1,'integer','O+'),(3756,1,'hostname','F'),(3865,1,'hostname','7'),(3824,1,'integer','Ĥ'),(3825,1,'integer','Ī'),(3827,1,'hostname',''),(3827,1,'integer','¶î#'),(3780,1,'integer','Mó'),(3780,1,'hostname','D'),(3834,1,'integer','q	'),(3830,1,'hostname',')	%'),(3820,1,'hostname','+'),(3819,1,'hostname','*'),(3787,1,'integer','h'),(3786,1,'hostname','$'),(3776,1,'integer','m\n\n\n'),(3721,1,'benchmark','ǣ'),(3722,1,'hostname','ć'),(3722,1,'integer','œ'),(3725,1,'integer','@'),(3727,1,'hostname','R'),(3727,1,'integer','[*'),(3728,1,'hostname','X'),(3728,1,'integer','aZ\r*'),(3729,1,'hostname','Z'),(3729,1,'integer','c8'),(3731,1,'hostname','>'),(3731,1,'integer','G\r'),(3732,1,'hostname','Y'),(3732,1,'integer','bZ\r*'),(3735,1,'hostname','\''),(3736,1,'integer','<'),(3737,1,'hostname','X'),(3737,1,'integer','aZ\r*'),(3738,1,'hostname','9'),(3738,1,'integer','B'),(3739,1,'hostname','X'),(3739,1,'integer','aZ\r*'),(3741,1,'hostname','@'),(3741,1,'integer','q'),(3742,1,'hostname','='),(3743,1,'hostname','<'),(3791,1,'integer',''),(3802,1,'hostname','4'),(3801,1,'hostname','.'),(3799,1,'integer','x'),(3798,1,'integer',''),(3797,1,'hostname','9'),(3795,1,'integer','>'),(3795,1,'hostname','5'),(3866,1,'hostname','`'),(3840,1,'integer','­'),(3841,1,'hostname','*'),(3862,1,'hostname','D'),(3782,1,'hostname',';'),(3840,1,'hostname','_'),(3855,1,'hostname','\"'),(3815,1,'integer','7'),(3818,1,'integer','dZ\r*'),(3818,1,'hostname','['),(3759,1,'integer','\r'),(3757,1,'integer',''),(3804,1,'integer',''),(3808,1,'hostname','%'),(3866,1,'integer','{'),(3871,1,'analyze','ƽʓᴑ'),(3871,1,'confusing','㻠'),(3871,1,'happened','࠳͜'),(3871,1,'ifentry','䃅		,'),(3871,1,'integer','ጸ'),(3871,1,'leaves','ḟ'),(3871,1,'redhat','ߺ'),(3871,1,'volumes','౫'),(3871,1,'wine','֔'),(3872,1,'cwd','ƹ'),(3872,1,'hostname','ܻ'),(3873,1,'hostname','ć'),(3875,1,'ifoperstatus','RGº'),(3878,1,'assigned','ट'),(3878,1,'ifoperstatus','ϡ'),(3879,1,'aid','⏅Ԥ҅'),(3879,1,'assigned','ܠȻ	ơಋౘƪݩ,״ӈʊL\'Ɵ5⒩'),(3879,1,'hostname','ීບ⟃Გ'),(3879,1,'ifoperstatus','倱'),(3879,1,'integer','ḓ'),(3879,1,'labellist','䠷'),(3879,1,'warnn','䕝'),(3880,1,'constructed','㠂'),(3880,1,'indicative','ᄾ'),(3880,1,'integer','ᯖ'),(3880,1,'tech','㧨ʽ'),(3881,1,'assigned','ቧŶ0෱'),(3881,1,'hostname','ংn	˝ঀŕ\'\ZƂ6'),(3881,1,'openfusion','㒥'),(3881,1,'timeup','ࡎ'),(3884,1,'assigned','ٛŸ%Ʉ'),(3884,1,'hostname','ჳ'),(3885,1,'assigned','ࢊWU+'),(3886,1,'aid','Ǟ'),(3886,1,'assigned','̡	M'),(3887,1,'aid','Ư'),(3887,1,'assigned','͌	'),(3887,1,'hostname','ౠ'),(3888,1,'assigned','¡o Hʮj3,Å=ȓ'),(3889,1,'assigned','˘EÄ'),(3890,1,'assigned','Ť'),(3890,1,'chars','᯴'),(3890,1,'chose','ᵷ'),(3892,1,'hostname','Ɯ'),(3820,6,'ifoperstatus','ŗ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict16` ENABLE KEYS */;

--
-- Table structure for table `dict17`
--

DROP TABLE IF EXISTS `dict17`;
CREATE TABLE `dict17` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict17`
--


/*!40000 ALTER TABLE `dict17` DISABLE KEYS */;
LOCK TABLES `dict17` WRITE;
INSERT INTO `dict17` VALUES (3888,1,'bear','Ӷ'),(3887,1,'statuswrl','ഁƌ'),(3865,1,'remaining','c'),(3862,1,'compile','ā'),(3844,1,'remaining','Æ'),(3871,1,'monitored','㷯'),(3871,1,'ifnumber','䂥'),(3871,1,'europe','䖋'),(3840,1,'aaa','Ȅ'),(3871,1,'datatype','௖'),(3827,1,'verisign','ɦ0	'),(3799,1,'vcl','Ê'),(3753,1,'supported','y'),(3741,1,'monitored','a'),(3742,1,'supported','P'),(3751,1,'restrict','ăƙ'),(3734,1,'checktype','5	'),(3887,1,'procedure','༂'),(3888,1,'build','ǅM_:;#$Ɨ'),(3881,1,'welcomes','є'),(3881,1,'checktype','ࡦ'),(3872,1,'compile','Ɓ'),(3871,1,'validity','׻'),(3881,1,'structures','ô⃞ʁ	܁'),(3890,1,'monitored','ࢼ'),(3889,1,'build','̊'),(3727,1,'monitored','þ'),(3721,1,'supported',''),(3880,1,'premier','Ӻ!8\n\n\nJ'),(3879,1,'monitored','ᇼมᚓᐌIᓩI'),(3879,1,'marcos','䡀'),(3881,1,'validity','ඏ'),(3881,1,'throughput','᰿'),(3881,1,'supported','೒ᑅ'),(3879,1,'build','ᙽ'),(3878,1,'procedure','ࣝ'),(3879,1,'supported','ኖ]۪⶚'),(3871,1,'throughput','ድ'),(3881,1,'build','׭ᒧᆖA'),(3871,1,'supported','ט'),(3871,1,'remaining','ื⤗˝'),(3878,1,'monitored','ЦG785a43352222003U/'),(3877,1,'remaining','΅'),(3877,1,'procedure','Ȏƅ'),(3877,1,'monitored',']0!'),(3877,1,'compile','Վ'),(3875,1,'procedure','˞'),(3874,1,'monitored','ŧï'),(3873,1,'monitored','ɛ'),(3872,1,'supported','̠'),(3881,1,'premier','̊!8\n\n\nJ'),(3881,1,'maps','₠'),(3881,1,'compile','㓫^'),(3871,1,'build','҉՟'),(3871,1,'00ff00','⿭৴ǧÄ܀֭'),(3890,1,'statuswrl','ǨӉ\r$'),(3879,1,'structures','ℋ'),(3879,1,'statuswrl','㡢'),(3890,1,'restrict','Ꮬ'),(3880,1,'monitored','ᄥ3ĉƧ̄ğûıý¸Ѝ4ᆈ¹1ѿľ'),(3880,1,'build','ཿ'),(3879,1,'procedure','਋ⱒ٣ąţLďʓňⅺe6'),(3879,1,'premier','ӷ!8\n\n\nJ'),(3881,1,'remaining','ᦘ'),(3879,1,'welcomes','ؿ'),(3869,1,'supported','5'),(3720,1,'build','A'),(3871,1,'compile','㊸഼'),(3880,1,'supported','㦼ʻ୚'),(3880,1,'welcomes','ق');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict17` ENABLE KEYS */;

--
-- Table structure for table `dict18`
--

DROP TABLE IF EXISTS `dict18`;
CREATE TABLE `dict18` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict18`
--


/*!40000 ALTER TABLE `dict18` DISABLE KEYS */;
LOCK TABLES `dict18` WRITE;
INSERT INTO `dict18` VALUES (3859,1,'level','/'),(3751,1,'multiple','R'),(3839,1,'resolves',''),(3837,1,'pophost','x'),(3822,1,'host2','0'),(3830,1,'supplied',''),(3744,1,'multiple','ŬEu'),(3865,1,'temp','*'),(3879,1,'ch','͵'),(3879,1,'starting','⇩࿳ʇǼ٣ąţL'),(3879,1,'interpreted','䢲4'),(3744,1,'interpreted','Ƞ'),(3862,1,'libs','ï'),(3862,1,'level','#'),(3862,1,'boxes','¢'),(3810,1,'temp','\Z'),(3879,1,'determining','㏥ϧ'),(3879,1,'multiple','↯Ⱥളq˹#ŮȽ˦ĺ2ƗӢټ'),(3849,1,'availability',''),(3844,1,'boxes',''),(3840,1,'supplied','Ũ'),(3856,1,'multiple','Ľ'),(3840,1,'groper','ɭ'),(3783,1,'multiple','e'),(3780,1,'vpp','¾'),(3752,1,'evaluated','h$'),(3840,1,'multiple','Ǳ'),(3879,1,'dumped','メ'),(3751,1,'level','Ŋ	%	'),(3751,1,'determining','-'),(3803,1,'multiple','\"'),(3799,1,'mrtgtraf','û'),(3799,1,'ch','ż'),(3798,1,'mrtgtraf','L'),(3798,1,'ch','»'),(3788,1,'multiple','f'),(3783,1,'supplied','u'),(3863,1,'multiple','´2('),(3879,1,'level','֖变'),(3879,1,'matt','ࡤʞ6R'),(3751,1,'accuracy','ȇ'),(3879,1,'boxes','⨠ƜⓄ'),(3879,1,'backed','敨ƃ\r'),(3879,1,'availability','ʽu᳥ㆧęAፆ\n\n'),(3879,1,'advantages','㗜'),(3878,1,'advantages','É'),(3877,1,'multiple','֡4'),(3875,1,'multiple','ɢ	\n'),(3873,1,'supplied','ʊ'),(3872,1,'starting','̇$'),(3866,1,'level','~'),(3870,1,'level','ÿ'),(3871,1,'ch','ێٽƦ႞^_èفȖ˗ί̀'),(3871,1,'level','ؐ'),(3871,1,'mind','☱'),(3871,1,'multiple','ଉ	ࠖʓജ'),(3871,1,'regenerated','⹵.'),(3871,1,'starting','ł䃓߇'),(3871,1,'supplied','ЄӵgΒ!ܡ᧴'),(3871,1,'temp','ೀ'),(3872,1,'level','Ț'),(3872,1,'multiple','ԯ̭'),(3744,1,'evaluation','āĿ'),(3740,1,'mind','ŀ'),(3727,1,'multiple','ù'),(3722,1,'supplied','¨'),(3880,1,'availability','ȜÐЉ)B!mV$\'ȫӝɑⒶm.Áদ|\Z/[Ƚ'),(3880,1,'ch','˟'),(3880,1,'level','֙ȧ><༨˨૒MTb)UA,X୒дy­áȦÕͫɥ'),(3880,1,'multiple','࠮਀≑5'),(3880,1,'starting','◚'),(3880,1,'tailor','દ⪑'),(3881,1,'advantages','Ⲍ'),(3881,1,'availability','Ǟྶ\r	\n\r	\n'),(3881,1,'interpreted','↙'),(3881,1,'level','Ωরᬐҗ'),(3881,1,'multiple','౯\nჁय़վݧØǟ'),(3881,1,'starting','ᤚ'),(3884,1,'advantages','࣓'),(3884,1,'multiple','´ֶț\"'),(3885,1,'multiple','ݔ'),(3886,1,'multiple','̘CÎb'),(3887,1,'multiple','̓D'),(3887,1,'supplied','ඇ'),(3888,1,'level','מ'),(3888,1,'mind','Ӹ'),(3888,1,'multiple','ǃ	ĶǢB'),(3888,1,'starting','ࡒ'),(3889,1,'multiple','Ƭ'),(3890,1,'interpreted','൶'),(3890,1,'level','෦'),(3890,1,'multiple','ࡽӹ'),(3890,1,'starting','๒O'),(3892,1,'determining','Ŏ'),(3798,6,'mrtgtraf','ú');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict18` ENABLE KEYS */;

--
-- Table structure for table `dict19`
--

DROP TABLE IF EXISTS `dict19`;
CREATE TABLE `dict19` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict19`
--


/*!40000 ALTER TABLE `dict19` DISABLE KEYS */;
LOCK TABLES `dict19` WRITE;
INSERT INTO `dict19` VALUES (3727,1,'download','į'),(3727,1,'voltage','ã'),(3740,1,'download','Ř'),(3744,1,'download','ƞ'),(3757,1,'100k','ƍ'),(3780,1,'logins','ġ'),(3829,1,'download','9'),(3831,1,'download','£'),(3832,1,'stops','2'),(3840,1,'fine','ǔ'),(3842,1,'download',''),(3856,1,'servicelist','3Ė'),(3862,1,'fine','µ'),(3871,1,'12000m','㗮'),(3871,1,'branch','㼏ª'),(3871,1,'completed','ڇ'),(3871,1,'dynamically','ᇻ'),(3871,1,'fine','䁠'),(3871,1,'pointer','ӷ⯑+\n'),(3871,1,'voltage','㈅'),(3872,1,'alphanumeric',''),(3872,1,'esac','ҟ'),(3874,1,'download','Ɖ'),(3875,1,'download','ř'),(3876,1,'download','»'),(3877,1,'stops','Ǿą'),(3878,1,'completed','ࣾ'),(3879,1,'accomplished','ᨩ஛'),(3879,1,'capability','Ⅶ'),(3879,1,'completed','ԩ\\9e'),(3879,1,'download','ҋ㲚'),(3879,1,'dynamically','Ⱳᝀ'),(3879,1,'hdc1','Ằ'),(3879,1,'hostextinfo','㢿5'),(3879,1,'integrate','ᓰ'),(3879,1,'logins','Ү͉ê'),(3880,1,'capability','ᑨ㊼'),(3880,1,'communications','ᅍ'),(3880,1,'completed','Ԭ\\9e'),(3880,1,'download','Ҏ'),(3880,1,'dynamically','◼0'),(3880,1,'integrate','ূ'),(3721,1,'download','Ŀ'),(3881,1,'integrate','ӯčǿᏘʻข'),(3881,1,'logins','ˁ'),(3891,1,'stops','˝˗'),(3882,1,'download','y'),(3890,1,'stops','ᖶ'),(3880,1,'logins','ұ'),(3881,1,'analyzed','ὐ'),(3881,1,'completed','̼\\9e'),(3881,1,'download','ʞ㉄'),(3881,1,'dynamically','⥼৮');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict19` ENABLE KEYS */;

--
-- Table structure for table `dict1A`
--

DROP TABLE IF EXISTS `dict1A`;
CREATE TABLE `dict1A` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict1A`
--


/*!40000 ALTER TABLE `dict1A` DISABLE KEYS */;
LOCK TABLES `dict1A` WRITE;
INSERT INTO `dict1A` VALUES (3871,1,'crash','œ#'),(3872,1,'area','֢4'),(3761,1,'delay','8'),(3762,1,'area','Ê'),(3766,1,'downloaded','Þ'),(3784,1,'delay','8'),(3785,1,'delay','8'),(3798,1,'downloaded','±'),(3799,1,'downloaded','Ų'),(3804,1,'rcv',''),(3879,1,'condition','佔'),(3879,1,'area','Ҍ'),(3879,1,'allowoverride','൧K'),(3872,1,'downloaded','ļ'),(3756,1,'predictable','Ù'),(3755,1,'incorrect','¬'),(3748,1,'delay','8'),(3879,1,'copying','ニ⨄\n'),(3805,1,'scanned','<'),(3745,1,'incorrect','é'),(3739,1,'delay','5'),(3732,1,'delay','5'),(3737,1,'delay','5'),(3728,1,'delay','5'),(3871,1,'f0f0f0','ặ'),(3871,1,'lesser','ᢍ'),(3871,1,'scanned','᏷'),(3806,1,'scanned','B'),(3871,1,'road','㯤'),(3871,1,'condition','᠚ۼ'),(3871,1,'ffffffffffffffff','䱀'),(3871,1,'area','ӯછЕࠁ!\rǮi5.Nौݚ௜.ۻ>௦#'),(3863,1,'privileges','y'),(3854,1,'delay',';´'),(3837,1,'loop','£Q'),(3833,1,'delay','8'),(3827,1,'incorrect','Ȕ'),(3818,1,'delay','8'),(3814,1,'delay','8'),(3879,1,'downloaded','ڥ㩶'),(3720,1,'area','Ř'),(3730,1,'delay','B'),(3729,1,'delay','6'),(3879,1,'hrs','ԗ?G'),(3879,1,'importing','㑺ឪ­'),(3879,1,'loop','愕'),(3879,1,'scanned','⬖'),(3880,1,'area','ҏ੘൉'),(3880,1,'delay','ⶆД'),(3880,1,'downloaded','Ѝ'),(3880,1,'hrs','Ԛ?G'),(3880,1,'speeds','ੱ'),(3881,1,'area','ʟ'),(3881,1,'downloaded','㔊'),(3881,1,'genericlog','⇬ǭ'),(3881,1,'gethostbydeviceid','ᗶ'),(3881,1,'hrs','̪?G'),(3881,1,'leftchild','᫆+'),(3881,1,'road','⛴Ԡ'),(3881,1,'sortorder','᥊Ò'),(3882,1,'downloaded','o'),(3888,1,'incorrect','͇'),(3890,1,'delay','๼̏\Z!\Z*¦ !\r*'),(3892,1,'area','Î'),(3837,6,'loop','İ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict1A` ENABLE KEYS */;

--
-- Table structure for table `dict1B`
--

DROP TABLE IF EXISTS `dict1B`;
CREATE TABLE `dict1B` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict1B`
--


/*!40000 ALTER TABLE `dict1B` DISABLE KEYS */;
LOCK TABLES `dict1B` WRITE;
INSERT INTO `dict1B` VALUES (3890,1,'named','ᦀ3'),(3887,1,'named','ǚ'),(3849,6,'dhcp',''),(3890,1,'exit','õ'),(3726,1,'example','Q\n'),(3733,1,'database',''),(3734,1,'database',',\nG	'),(3734,1,'reveal','b'),(3735,1,'identity','3\''),(3735,1,'swap',''),(3736,1,'exit','='),(3736,1,'swap','	!\n'),(3740,1,'exit','{K'),(3751,1,'example','ɂ'),(3753,1,'registered',''),(3754,1,'exit','ƫ'),(3757,1,'example','Á'),(3763,1,'database','9]>'),(3764,1,'pf',' '),(3768,1,'database',''),(3773,1,'database',''),(3774,1,'database',''),(3775,1,'database',''),(3776,1,'database','ÿ'),(3777,1,'database',''),(3778,1,'database','L O'),(3780,1,'database','ö'),(3789,1,'example',''),(3791,1,'database','@N'),(3792,1,'example','.'),(3793,1,'database','#.@'),(3793,1,'example','-'),(3795,1,'database','&'),(3799,1,'example','¯'),(3803,1,'preprocessed','®'),(3804,1,'named','$'),(3808,1,'identity','-/'),(3809,1,'exit','9'),(3816,1,'example','\''),(3822,1,'hostn','1'),(3823,1,'named','0'),(3827,1,'example',' '),(3830,1,'database','v'),(3831,1,'pf','-'),(3837,1,'example','ą'),(3841,1,'example','+'),(3843,1,'identity','L'),(3845,1,'identity','6&'),(3846,1,'exit','W'),(3849,1,'dhcp',''),(3856,1,'example','ŏ'),(3856,1,'identity',')tR\n'),(3862,1,'2002','L'),(3869,1,'2002',''),(3869,1,'database','V«'),(3870,1,'example','E'),(3870,1,'named','¾'),(3871,1,'12345000','㪚'),(3871,1,'18446744069414584318','䵛'),(3871,1,'database','7mőxǨţü&ަ ᦔ:\ZࠈsĊƵɷv-³L:LōÐŮќǦ)r¦ǇŐɣXϓ'),(3871,1,'example','ٰǱˎĿ#ǡ˄ǳǣǽ֛¸ «/âĉ+ƲͯΏ˝ˊ1K֘Õ!ŸμǘûɴýōʸʲƾʈeȖ'),(3871,1,'named','ᤵ	᷊ࠢ'),(3871,1,'overflows','࠘'),(3871,1,'reread','㎻፞'),(3872,1,'example','ȳ˖ƽǫȀū'),(3872,1,'exit','Ʒ?|RƓE'),(3872,1,'named','Ʌ'),(3872,1,'sportster','Ԕá'),(3873,1,'example','ʵ'),(3873,1,'exit','Ļ'),(3873,1,'swap','Β\r'),(3874,1,'database','Ƒ'),(3874,1,'swap','ĚEƊ'),(3875,1,'database','š'),(3876,1,'database','Ã'),(3877,1,'chmod','ѕª'),(3877,1,'example','Λ)ɋ'),(3877,1,'exit','Ԛ'),(3877,1,'named','Ğҫ'),(3878,1,'database','Ɣȋƍ'),(3878,1,'named','ե'),(3879,1,'database','Ɉėb՞æ߲~\n	+	\nR\r\n)\r\n\n\r\n౭ࠠތ-FᅬÞ$ΑȋÈ\Z8=ִ+y)\n¤Ŕñǁࡠ40Iª{\r\'\nDő/.)*E\nJ;T\n!\'6'),(3879,1,'example','চǫৼ+\rr;V3WРYSϵ£಄ωɋȸĿീQI^їЃ\Z£ѴƘŨ±ª\nȯīЙϡ'),(3879,1,'exit','ೊᅊॵ޲㓦ÊóQ'),(3879,1,'member','⢝.૿ι.Г'),(3879,1,'named','䩱ᓹ'),(3879,1,'rel','崇'),(3880,1,'database','ˉbބԝǠכ₀ભ2'),(3880,1,'developed','᝹'),(3880,1,'example','๽ȑ૳#$Ħ<Ǫèǎ:Ʒѳ೘İΫbɛf[̶'),(3880,1,'exit','ඔ㧯'),(3880,1,'member','჆'),(3880,1,'named','ၶ⥰bɛf['),(3880,1,'publication','ᗖ'),(3881,1,'database','ӑȠČЧ,\r\'\Z2[XڋƊ*͑ċ¯.Ňm`āȺeþĝöėĖ\n'),(3881,1,'developed','Ǒќ₧Å'),(3881,1,'example','ᥱ6ʧǫ%yʜ.ĥ։Ðųΐ×̗ˣ'),(3881,1,'exit','⍘'),(3881,1,'member','ᑸ2'),(3882,1,'example','Ð'),(3883,1,'example','ē'),(3883,1,'exit','ª'),(3884,1,'database','ᐾ'),(3884,1,'example','ľ ֻɾܻęƑ&'),(3884,1,'exit','Ƽਗ'),(3884,1,'member','ङ'),(3885,1,'example','Ö׵ü'),(3885,1,'exit','Ȧر'),(3885,1,'member','न1'),(3886,1,'example','И'),(3886,1,'member','̴'),(3887,1,'example','ऋߝ'),(3887,1,'exit','Ӫӹķµѻ'),(3887,1,'member','͟'),(3721,1,'developed','Ʋ'),(3721,1,'database','C(õ3KV'),(3889,1,'example','ɜºų-'),(3889,1,'database','ˎ'),(3736,6,'swap','Ç'),(3735,6,'swap',''),(3892,1,'exit','ê'),(3891,1,'exit','µ̈Æ'),(3892,1,'user32','´'),(3890,1,'database','ĺᴅ\Z'),(3888,1,'example','թwR`Ň'),(3888,1,'member','Ƒ'),(3888,1,'named','ª΅ʏ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict1B` ENABLE KEYS */;

--
-- Table structure for table `dict1C`
--

DROP TABLE IF EXISTS `dict1C`;
CREATE TABLE `dict1C` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict1C`
--


/*!40000 ALTER TABLE `dict1C` DISABLE KEYS */;
LOCK TABLES `dict1C` WRITE;
INSERT INTO `dict1C` VALUES (3879,1,'describing','䦒'),(3879,1,'main','̇ċ੡zܮʁ!ౙ᫖Sᆏ֘Ғ'),(3879,1,'monitors','˚㇣	'),(3879,1,'yesterday','啌	'),(3880,1,'cases','֝'),(3874,1,'monitors','<'),(3873,1,'monitors','qK³'),(3890,1,'main','Uݳ\r	қˎ­ҷ֏ '),(3888,1,'main','ǽU'),(3887,1,'size','ʟqআĜ~u'),(3887,1,'main','٦'),(3887,1,'emergency','్'),(3871,1,'putting','ᳪ'),(3871,1,'octets','Ɨ㱻'),(3871,1,'main','ྨᤋŁ'),(3871,1,'inout','⬌'),(3871,1,'exhibition','㉵'),(3871,1,'differ','㚉'),(3886,1,'differ','Ҥ'),(3885,1,'main','©͹'),(3884,1,'emergency','ጒ'),(3881,1,'regard','⾣'),(3871,1,'size','͆˴Òಬ˄ۢ਋঱᫨'),(3871,1,'yesterday','␏˯'),(3872,1,'main','દŎØ'),(3842,6,'size','H'),(3841,6,'dlswcircuit','·'),(3881,1,'main','Ȟѧ᝸޷࿵ϓ'),(3880,1,'monitors','ȹಝઆ൐യ'),(3880,1,'main','ɣّӨॿⵏ΁'),(3881,1,'cases','έध'),(3871,1,'replaces','ᢘ'),(3747,1,'vh','%'),(3873,1,'main','4'),(3881,1,'viewport','㥽'),(3881,1,'monitors','Ʃჶ'),(3752,1,'cases','lK'),(3757,1,'size',''),(3782,1,'differ',''),(3782,1,'size','Ü'),(3799,1,'monitors','ķ'),(3801,1,'monitors','!'),(3802,1,'monitors',' '),(3812,1,'joyd','#'),(3813,1,'main',''),(3819,1,'monitors',''),(3819,1,'size','Ā'),(3820,1,'monitors',''),(3820,1,'size','ò'),(3822,1,'hostint','Z'),(3822,1,'size','t'),(3827,1,'size','eŘ'),(3828,1,'monitors',''),(3834,1,'size','A1'),(3836,1,'size',','),(3837,1,'stderr','Ą'),(3841,1,'dlswcircuit','\r*.$'),(3842,1,'size',''),(3849,1,'requestedip','&'),(3856,1,'stderr','w'),(3858,1,'monitors',''),(3860,1,'size','2'),(3870,1,'libxml2',''),(3871,1,'cases','Šᄚᐹ᷶'),(3721,1,'putting','¿'),(3720,1,'size','¥'),(3881,1,'putting','⡝'),(3880,1,'size','⍯ᗔ'),(3880,1,'static','ẏS'),(3879,1,'cases','֚䃮'),(3875,1,'monitors','>'),(3878,1,'monitors','ž(\n  \"  \"\"\Z\r\rNI6785*77E03352222003\"3'),(3878,1,'main','Ƈ'),(3877,1,'putting','ڱ'),(3877,1,'main','ɢ'),(3877,1,'cases','ϧ'),(3876,1,'monitors','<'),(3875,1,'octets','ȩ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict1C` ENABLE KEYS */;

--
-- Table structure for table `dict1D`
--

DROP TABLE IF EXISTS `dict1D`;
CREATE TABLE `dict1D` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict1D`
--


/*!40000 ALTER TABLE `dict1D` DISABLE KEYS */;
LOCK TABLES `dict1D` WRITE;
INSERT INTO `dict1D` VALUES (3880,1,'configurable','ঊ㪈'),(3880,1,'mrtg','਋'),(3880,1,'packaged','ߤ'),(3880,1,'unreachable','ጒٰƘĴKʊযւ'),(3881,1,'classname','ⵟÄ'),(3881,1,'packaged','ڰ'),(3881,1,'plans','Ⱇ'),(3885,1,'importer','G\n`'),(3880,1,'3rd','ླ'),(3880,1,'15011','ᮊ'),(3879,1,'unreachable','⢽૿ϧГ'),(3871,1,'printf','ᐡ؊݇'),(3872,1,'unreachable','৏'),(3874,1,'importer','ƥ'),(3875,1,'importer','ŵ'),(3876,1,'importer','×'),(3878,1,'packaged','ó\n'),(3879,1,'configurable','ྮ˘⼱\n'),(3879,1,'importer','⏐ᔲ'),(3879,1,'mrtg','ᔷ'),(3887,1,'unreachable','࡜V:Yݾ'),(3891,1,'unreachable','ǰ͒'),(3890,1,'unreachable','ވ'),(3799,6,'mrtg','ƙ'),(3885,1,'unreachable','؛V:Z'),(3722,1,'broadcast','c'),(3888,1,'uniqueness','ڷ'),(3889,1,'unreachable','ïğH'),(3752,1,'printf','í'),(3754,1,'unreachable',':Ċ½'),(3780,1,'mrtg','ű'),(3782,1,'printf','ŗ'),(3798,1,'mrtg','&\Z'),(3799,1,'mrtg','\'	,\''),(3813,1,'classname','.'),(3824,1,'broadcast','B8@ '),(3825,1,'broadcast',';k;\"'),(3844,1,'gigabytes','½'),(3871,1,'configurable','؏*'),(3871,1,'creats','➃'),(3871,1,'lined','䥈'),(3871,1,'magic','Ⴍń⦩'),(3871,1,'mrtg','v\rֺࢷ̣	ᜨࠉaၐYඣ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict1D` ENABLE KEYS */;

--
-- Table structure for table `dict1E`
--

DROP TABLE IF EXISTS `dict1E`;
CREATE TABLE `dict1E` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict1E`
--


/*!40000 ALTER TABLE `dict1E` DISABLE KEYS */;
LOCK TABLES `dict1E` WRITE;
INSERT INTO `dict1E` VALUES (3879,1,'events','䬂J\Zᓎɒ'),(3886,1,'prefixed','ӟ'),(3886,1,'dns','Ķ4'),(3879,1,'guavaldapmodule','᳕'),(3884,1,'events','Ѱ਋'),(3881,1,'tktauthsecret','㗖ƿǿ'),(3879,1,'org','̾T᠎\r㯗Ռ'),(3879,1,'visibility','˒'),(3879,1,'prefixed','夫'),(3885,1,'events','ٟ'),(3879,1,'dns','⡊4ɢ'),(3880,1,'org','ʨT㛍ʽ'),(3880,1,'events','༏ʙ௠ӎͮİQᯰ'),(3881,1,'org','㏜'),(3881,1,'filtered','ᖷĉ̂'),(3881,1,'events','݌		ɇ൹¨B\"৞ࢄ'),(3881,1,'dns','ೱ'),(3880,1,'visibility','ȱٺ'),(3881,1,'visibility','Ơ'),(3720,1,'events','M'),(3721,1,'comparison','Ǚ'),(3721,1,'org','ũ'),(3727,1,'org','&Ē'),(3741,1,'nocache','1'),(3751,1,'95','ɑ+'),(3762,1,'org','Ö'),(3766,1,'org','æ'),(3781,1,'10000',''),(3782,1,'95','È'),(3783,1,'locally','Ï'),(3792,1,'org','B'),(3811,1,'org','q'),(3816,1,'inodes',''),(3817,1,'inodes',''),(3827,1,'age','mÉ'),(3827,1,'dns','²'),(3831,1,'raw','`'),(3836,1,'age','\r'),(3839,1,'dns','\n\n'),(3840,1,'dns','	*\n-4<·.#\n\n'),(3840,1,'org','ɐ'),(3844,1,'95',''),(3848,1,'dns',''),(3864,1,'aecho',' '),(3869,1,'org','ć'),(3870,1,'org',''),(3871,1,'000000','㧝ི'),(3871,1,'10000','⥤'),(3871,1,'95','侟#'),(3871,1,'9620838224e','⃱'),(3871,1,'comparison','㗋'),(3871,1,'ethernet0','䄄'),(3871,1,'org','㼬'),(3871,1,'raw','䛩'),(3871,1,'warm','䠗'),(3872,1,'org','ń୛'),(3877,1,'scp','՝1'),(3878,1,'dns','ƻǋÛ	ąÙ'),(3879,1,'95','㋟'),(3887,1,'dns','Ş4'),(3887,1,'events','ࢠ'),(3890,1,'events','ࠕफ़'),(3890,1,'periodically','᫪SK)'),(3816,6,'inodes','\\'),(3817,6,'inodes','9'),(3836,6,'age','n'),(3839,6,'dns','>'),(3840,6,'dns','̢');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict1E` ENABLE KEYS */;

--
-- Table structure for table `dict1F`
--

DROP TABLE IF EXISTS `dict1F`;
CREATE TABLE `dict1F` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict1F`
--


/*!40000 ALTER TABLE `dict1F` DISABLE KEYS */;
LOCK TABLES `dict1F` WRITE;
INSERT INTO `dict1F` VALUES (3877,1,'parameters','ǵ'),(3750,6,'check','?'),(3833,1,'check','\r'),(3739,6,'check','Ĥ'),(3761,6,'check','ħ'),(3755,6,'check','È'),(3744,6,'check','ɥ'),(3747,6,'check','d'),(3721,1,'1999','ă'),(3729,6,'check','Ý'),(3858,1,'check',''),(3839,1,'check','\r'),(3749,6,'check','4'),(3770,6,'check','/'),(3846,1,'check','!<'),(3723,1,'check',''),(3879,1,'check','᧗Άl\Z\rޛŘ­ǪƜҢƁJy	ůǼ̦̽ąîuL̈\'ĻÓǁȉ**ܡHܝৡĕ'),(3867,1,'check',''),(3868,1,'1999',''),(3868,1,'faster',' '),(3870,1,'check','>'),(3871,1,'1999','⏾ơ6\nôDྭ'),(3860,1,'check','\"'),(3781,6,'check','£'),(3884,1,'disable','̨~>ł਋³'),(3877,1,'check','ƈƏ_®Ŏĉ;'),(3786,6,'check','H'),(3795,6,'check',''),(3746,6,'check','<'),(3890,1,'faster','ᕊ'),(3723,6,'check','M'),(3877,1,'disable','͆'),(3763,6,'check','ł'),(3797,6,'check','u'),(3835,1,'check',''),(3881,1,'disparate','؋'),(3824,1,'check','j\''),(3774,6,'check','4'),(3885,1,'disable','˚3!&ƘY'),(3785,6,'check','ħ'),(3831,1,'check',''),(3843,1,'check',''),(3794,6,'check','0'),(3741,6,'check','Ã'),(3822,1,'check','\r_'),(3873,1,'check','ăËò\r\n						'),(3759,6,'check','ć'),(3884,1,'check','À?@1¬:1S\r	2˛=\n4$(˺	<Ī:1S\r	2ʔ\r\r&\n'),(3878,1,'parameters','Ͼ'),(3875,1,'check','ª%5ǝ'),(3871,1,'parameters','ᐷᑣᒊႅ'),(3871,1,'rrddump','ᾕ'),(3871,1,'val1','ẉ$V'),(3872,1,'check','ɼ\nǄͳЂ'),(3772,6,'check','/'),(3879,1,'disable','೷kI㲔Ɲ'),(3778,6,'check','ā'),(3844,1,'smb',''),(3827,1,'check','\nȃ8'),(3776,6,'check','İ'),(3727,6,'check','Ŋ'),(3885,1,'check','ȑ\rƉ=\\	ȫ'),(3784,6,'check','ħ'),(3830,1,'check',''),(3892,1,'check','@zO'),(3874,1,'check',' \r\n\n#\n­=>5//'),(3842,1,'check',''),(3793,6,'check','±'),(3740,6,'check','Ƒ'),(3821,1,'check','	'),(3872,1,'parameters','ޓ'),(3742,6,'check',''),(3832,1,'check',''),(3792,6,'check','m'),(3789,6,'check','¡'),(3765,6,'check','ç'),(3852,1,'check',''),(3853,1,'check',''),(3854,1,'check',''),(3855,1,'check','\r'),(3856,1,'check','Į\Z'),(3829,1,'check','C'),(3788,6,'check','å'),(3764,6,'check','G'),(3847,1,'check',''),(3848,1,'check',''),(3849,1,'check',''),(3850,1,'check','	'),(3851,1,'check','\n'),(3828,1,'check',''),(3881,1,'approach','ἠ'),(3758,6,'check','K'),(3882,1,'check','ēh'),(3878,1,'check','ц'),(3874,1,'parameters','-ǃ	ļ'),(3871,1,'austria','䗬'),(3871,1,'check','ȇGŝɑI̗ᚫ*ᥴɡ\Zሑ'),(3871,1,'disable','ᕫ߶ல'),(3871,1,'faster','\'Ö'),(3871,1,'networking','ゆЈǟ'),(3771,6,'check','/'),(3884,1,'parameters','Ⴙ'),(3879,1,'customization','੐䯑â'),(3880,1,'approach','܇ᵓ'),(3777,6,'check','3'),(3844,1,'check','	'),(3775,6,'check','2'),(3825,1,'check','\n¿'),(3826,1,'check','\n'),(3725,6,'check','j'),(3724,6,'check','p'),(3738,6,'check',''),(3760,6,'check','L'),(3779,6,'check','='),(3754,6,'check','̘'),(3743,6,'check',''),(3746,6,'smb','='),(3720,1,'faster',''),(3728,6,'check','Ĥ'),(3857,1,'check',''),(3838,1,'check',''),(3748,6,'check','ħ'),(3769,6,'check','/'),(3845,1,'check',''),(3721,1,'faster','ǯu'),(3722,1,'check','å'),(3861,1,'check',''),(3862,1,'check','\''),(3863,1,'check',''),(3864,1,'check',''),(3865,1,'check','.'),(3866,1,'check','@'),(3859,1,'check',''),(3780,6,'check','Ƹ'),(3875,1,'parameters','-<ŀ	P'),(3876,1,'check','|+\"'),(3876,1,'parameters','-Þ	V'),(3745,6,'check','ą'),(3890,1,'disable','̻׷²ostफ़գ*'),(3722,6,'check','Ŭ'),(3877,1,'deal','ɜ'),(3762,6,'check','è'),(3796,6,'check','2'),(3834,1,'check',''),(3881,1,'check','Ⴟ	\nΓ2ੋᠪ'),(3823,1,'check',',u'),(3773,6,'check','3'),(3737,6,'check','Ĥ'),(3879,1,'parameters','ḐӽѫǼŷ਎൝ģѷ৞*¬'),(3879,1,'faster','΍'),(3753,6,'check',''),(3880,1,'parameters','䌺'),(3880,1,'faster','˷ቮ'),(3880,1,'disable','⧞D=D.\n\r	ïgÒ+A<\rT	¬\nwþ{/'),(3880,1,'check','౓ɮ]ǼƇ\Z\r˷Ϟ\rŖ»ࡂ¢ӌn9H*)%,	\r\r\nÃǓ7@/)	\n%qƛ'),(3798,6,'check','ù'),(3766,6,'check','ú'),(3767,6,'check','1'),(3757,6,'check','ư'),(3756,6,'check','þ'),(3752,6,'check','Ę'),(3751,6,'check','ʴ'),(3841,1,'check','\r*.$'),(3840,1,'check','0©ŕ '),(3732,6,'check','ĥ'),(3733,6,'check',''),(3734,6,'check','Ć'),(3735,6,'check',''),(3736,6,'check','Æ'),(3887,1,'disable','Ԙ3!&ƛY'),(3885,1,'parameters','Ćپ'),(3886,1,'check','Ġ˘\r*&\n'),(3887,1,'check','ňԣ=\\	ߘ'),(3791,6,'check','¹'),(3783,6,'check','à'),(3782,6,'check','Ɵ'),(3731,6,'check','¤'),(3730,6,'check',''),(3881,1,'parameters','ಉ7ď5?@@>BDBCXBQ@AAA76358\r\Z7\Z\"28-!/E;&%D:§gƗ=\"ᚘٳa3'),(3768,6,'check','2'),(3891,1,'check','ś\r'),(3890,1,'spreading','ᆗ'),(3790,6,'check','\\'),(3890,1,'check','ŴÖZًϭ,\n$3Ƌ¦\n\n´Öi\",f\"Ʃ)ĸCA¬@FGȓF\r \rm'),(3888,1,'check','̙ƛ¹,l'),(3837,1,'check','£Q'),(3836,1,'check','\r'),(3820,1,'check',''),(3724,1,'check','\r'),(3725,1,'check',''),(3726,1,'check','W'),(3727,1,'check','\"­.'),(3728,1,'check','\r'),(3729,1,'check',''),(3730,1,'check','\Z'),(3731,1,'check',''),(3732,1,'check','\r'),(3733,1,'check','H'),(3734,1,'check','\n'),(3735,1,'check',''),(3736,1,'check','\r	'),(3737,1,'check','\r'),(3738,1,'check',''),(3739,1,'check','\r'),(3740,1,'check','*	H\''),(3741,1,'check',':'),(3742,1,'check',' '),(3743,1,'check','6'),(3744,1,'check','6'),(3745,1,'check',''),(3746,1,'check','\n'),(3746,1,'smb',''),(3747,1,'check','	\r'),(3748,1,'check','\r'),(3749,1,'check',''),(3750,1,'check',''),(3751,1,'check','\n:I\rò'),(3752,1,'check','>#`	!'),(3753,1,'check',''),(3754,1,'check','/%Ŭ1\Z5!'),(3755,1,'check',''),(3756,1,'check',''),(3757,1,'check',');Ï'),(3758,1,'check',''),(3759,1,'check','@'),(3760,1,'check',''),(3761,1,'check','\r'),(3762,1,'check',''),(3763,1,'check','8'),(3763,1,'parameters',''),(3764,1,'check',''),(3765,1,'check','$_'),(3766,1,'check','.'),(3767,1,'check',''),(3768,1,'check',''),(3769,1,'check','	'),(3770,1,'check','	'),(3771,1,'check','	'),(3772,1,'check','	'),(3773,1,'check','	'),(3774,1,'check','	'),(3775,1,'check','	'),(3776,1,'check','	\Z# $2'),(3777,1,'check','\n'),(3778,1,'check','	\"\n\Z'),(3779,1,'check',''),(3780,1,'check','.1M'),(3781,1,'check',''),(3782,1,'check','A&\r'),(3782,1,'parameters','Õ;3'),(3783,1,'check',''),(3784,1,'check','\r'),(3785,1,'check','\r'),(3786,1,'check',''),(3787,1,'check',''),(3788,1,'check','-3'),(3789,1,'check','#!'),(3790,1,'check',''),(3791,1,'check','#]'),(3792,1,'check','	'),(3792,1,'deblende','W'),(3793,1,'check','\r%-'),(3794,1,'check',''),(3795,1,'check',',-'),(3796,1,'check','	'),(3797,1,'check',''),(3797,1,'smb','&'),(3798,1,'check','F'),(3799,1,'check','E'),(3800,1,'check',''),(3801,1,'check','\r'),(3802,1,'check',''),(3802,1,'maxchannel','.'),(3803,1,'check',''),(3804,1,'check',''),(3805,1,'check','Q	'),(3806,1,'check',''),(3807,1,'check',''),(3808,1,'check',''),(3809,1,'check',''),(3810,1,'check',''),(3811,1,'check',''),(3812,1,'check',''),(3813,1,'check','	'),(3814,1,'check','\r'),(3815,1,'check',''),(3816,1,'check',''),(3817,1,'check',''),(3818,1,'check','\r'),(3819,1,'check',''),(3799,6,'check','Ƙ'),(3800,6,'check','N'),(3801,6,'check','?'),(3802,6,'check','E'),(3803,6,'check','ç'),(3804,6,'check','¾'),(3805,6,'check','Ö'),(3806,6,'check','{'),(3807,6,'check','J'),(3808,6,'check',''),(3809,6,'check','m'),(3810,6,'check','7'),(3811,6,'check','Ï'),(3812,6,'check','<'),(3813,6,'check',' '),(3814,6,'check','ħ'),(3815,6,'check','u'),(3816,6,'check','['),(3817,6,'check','8'),(3818,6,'check','ħ'),(3819,6,'check','ĩ'),(3820,6,'check','Ŗ'),(3821,6,'check','D'),(3822,6,'check','Ĉ'),(3823,6,'check','Õ'),(3824,6,'check','Š'),(3825,6,'check','Ō'),(3826,6,'check','/'),(3827,6,'check','˞'),(3828,6,'check','¦'),(3829,6,'check',''),(3830,6,'check',''),(3831,6,'check','Ú'),(3832,6,'check','W'),(3833,6,'check','ħ'),(3834,6,'check','Â'),(3835,6,'check',''),(3836,6,'check','l'),(3837,6,'check','Į'),(3838,6,'check','K'),(3839,6,'check','='),(3840,6,'check','̡'),(3841,6,'check','¶'),(3842,6,'check','F'),(3843,6,'check','r'),(3844,6,'check','Þ'),(3844,6,'smb','à'),(3845,6,'check',''),(3846,6,'check','Ł'),(3847,6,'check',']'),(3848,6,'check','¸'),(3849,6,'check',''),(3850,6,'check','R'),(3851,6,'check',''),(3852,6,'check','3'),(3853,6,'check','['),(3854,6,'check','ŋ'),(3855,6,'check','B'),(3856,6,'check','Ƙ'),(3857,6,'check','g'),(3858,6,'check','<'),(3859,6,'check','J'),(3860,6,'check','O'),(3861,6,'check',':'),(3862,6,'check','Ģ'),(3863,6,'check','ń'),(3864,6,'check',''),(3865,6,'check','©'),(3866,6,'check','¿'),(3867,6,'check','8');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict1F` ENABLE KEYS */;

--
-- Table structure for table `dict20`
--

DROP TABLE IF EXISTS `dict20`;
CREATE TABLE `dict20` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict20`
--


/*!40000 ALTER TABLE `dict20` DISABLE KEYS */;
LOCK TABLES `dict20` WRITE;
INSERT INTO `dict20` VALUES (3879,1,'modifying','╕ۖiෆߣȘᇤױ9'),(3877,1,'component','°'),(3879,1,'outage','凾'),(3879,1,'opening','䤨'),(3881,1,'component','òۗݙ\n\r	อú\Zϡǖ	Ŧ้'),(3881,1,'accepted','≴'),(3880,1,'trap','࡟'),(3880,1,'retried','ኧ'),(3880,1,'roles','ಳeܚ'),(3880,1,'higher','ឤ'),(3880,1,'outage','ऑ⹌7ĢΒυ6D\'A@A4ă'),(3880,1,'component','ΈѰʪӨ ࢆᳰඍɷ'),(3879,1,'roles','Êى0NGe9Ź\n$.@-Ɔ࠾̿k੍ሢ'),(3889,1,'modifying','˟6Q'),(3886,1,'drive','ЫO'),(3884,1,'retried','˥਋'),(3881,1,'servicestatusid','ᘤÑ'),(3881,1,'modifying','゙'),(3881,1,'higher','᰾'),(3871,1,'talking','ㆶೂ̙'),(3734,1,'sa','\''),(3747,1,'drive','C'),(3751,1,'higher',''),(3751,1,'mst','ƌ'),(3757,1,'higher','ç'),(3766,1,'higher','¿'),(3780,1,'osversioninclude','ŕ'),(3782,1,'drive','è'),(3792,1,'sa','4'),(3793,1,'sa','2'),(3799,1,'drive','Ĥ'),(3823,1,'srvn','¡'),(3871,1,'drive','䗩'),(3871,1,'higher','ዸǺǀ⹮ޱ'),(3871,1,'mph','㘛'),(3871,1,'mst','Ⴠ'),(3890,1,'accepted','ð'),(3890,1,'drive','ͣ'),(3890,1,'higher','ނ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict20` ENABLE KEYS */;

--
-- Table structure for table `dict21`
--

DROP TABLE IF EXISTS `dict21`;
CREATE TABLE `dict21` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict21`
--


/*!40000 ALTER TABLE `dict21` DISABLE KEYS */;
LOCK TABLES `dict21` WRITE;
INSERT INTO `dict21` VALUES (3877,1,'owner','ƕȗ»Ǚ'),(3869,1,'basic','T'),(3871,1,'955892996','₵'),(3871,1,'baarda','਋ŏ'),(3840,1,'basic','č'),(3834,1,'icmp','t	'),(3827,1,'basic','Ɔ'),(3871,1,'infinite','ᥖ	'),(3871,1,'ensures','ሂ'),(3871,1,'increased','䝙ݮ5'),(3753,1,'syslogged','`'),(3754,1,'2nd','ƃ\n'),(3762,1,'icmp','a'),(3763,1,'devel','Į'),(3872,1,'install',',åÔ'),(3871,1,'yield','᨜Ċௐ'),(3871,1,'questions','偞 *'),(3871,1,'pretend','㟔'),(3871,1,'install','Ŭ'),(3720,1,'enhancements','hĊ'),(3871,1,'basic','ޞ₷4⟔J'),(3878,1,'ensures','ƅ'),(3868,1,'questions','â'),(3868,1,'devel','ó'),(3864,1,'icmp','T'),(3862,1,'install','ć\n'),(3877,1,'xxx','ۆ'),(3877,1,'install','̢Ȯ'),(3822,1,'increased','ë'),(3876,1,'icmp','A8+G'),(3822,1,'icmp','Fk'),(3763,1,'questions','Ğ'),(3873,1,'install','Ź\Z'),(3878,1,'icmp','τ'),(3879,1,'basic','ീIК|ᬌfܨ'),(3879,1,'icmp','㓂'),(3879,1,'install','ϺವI܈ࡠ⋎Ēሠ×ϣ2v#J f'),(3880,1,'basic','α७ƍӗ㇤'),(3880,1,'corrective','㒱'),(3880,1,'install','ϋ䄏'),(3881,1,'basic','฿᭎Ͳm࣡Ǝ'),(3881,1,'inputtext','ブ6Ċ'),(3881,1,'install','ɚ♬ணÓI'),(3881,1,'questions','⾡਀'),(3890,1,'basic','α'),(3890,1,'kiddies','͑'),(3822,6,'icmp','ĉ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict21` ENABLE KEYS */;

--
-- Table structure for table `dict22`
--

DROP TABLE IF EXISTS `dict22`;
CREATE TABLE `dict22` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict22`
--


/*!40000 ALTER TABLE `dict22` DISABLE KEYS */;
LOCK TABLES `dict22` WRITE;
INSERT INTO `dict22` VALUES (3880,1,'object','ᡊ֒޴\\/İ½'),(3878,1,'customers','à'),(3880,1,'title','䒖ɬ'),(3880,1,'dell','৩'),(3873,1,'allowed','Ʊ'),(3798,1,'older','.'),(3799,1,'older','Ù'),(3799,1,'processor','£}'),(3877,1,'embedded','ã'),(3871,1,'title','ཷڙݱE·ႊ960ೣ'),(3869,1,'embedded',' B'),(3850,1,'dell','	'),(3880,1,'customers','Ҁ$῏'),(3879,1,'title','娹+SɄ&		\"\n\n\r\n\n	\n\'\n\n\r'),(3879,1,'older','䎚⊛6'),(3879,1,'customers','ѽ$'),(3879,1,'dell','ᔗ㡧'),(3879,1,'descriptive','㣧'),(3879,1,'object','↨ᕵǍ'),(3880,1,'tiers','ཛ'),(3880,1,'processor','㓜'),(3798,1,'iwl','R'),(3789,1,'older','/'),(3744,1,'object','Ë'),(3757,1,'vsz','N5\\'),(3776,1,'object','É% '),(3782,1,'object','Ŋ'),(3742,1,'rfc','1\''),(3819,1,'descriptive','j'),(3820,1,'descriptive','İ'),(3877,1,'allowed','ԑm'),(3827,1,'qualified','ȱ'),(3721,1,'embedded','Ȧ/'),(3869,1,'object','{'),(3880,1,'descriptive','㠹'),(3874,1,'allowed','ϟ'),(3871,1,'outcome','Ἓᗹᇌ'),(3871,1,'object','㻔'),(3871,1,'descriptive','䆊'),(3871,1,'allowed','㯅©ᄩ'),(3870,1,'embedded','%'),(3870,1,'object','xU'),(3871,1,'1020614700','⯯'),(3881,1,'allowed','ⶆ	ࡦ̀'),(3881,1,'customers','ʐ$'),(3881,1,'object','Ӌ˔ő\rĚçĪ+~3+ଈ5*¦§.\r0\rRłৱɺhŘE\nӎXĉ3'),(3881,1,'processor','⛤ν( '),(3884,1,'overrides','૭'),(3884,1,'processor','Θ਋'),(3887,1,'object','ച'),(3888,1,'object','Ι'),(3889,1,'object','Қ'),(3890,1,'object','ࠦ*໽¦Lȉ>śǝ¬'),(3890,1,'processor','ᜂ)'),(3850,6,'dell','S');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict22` ENABLE KEYS */;

--
-- Table structure for table `dict23`
--

DROP TABLE IF EXISTS `dict23`;
CREATE TABLE `dict23` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict23`
--


/*!40000 ALTER TABLE `dict23` DISABLE KEYS */;
LOCK TABLES `dict23` WRITE;
INSERT INTO `dict23` VALUES (3881,1,'task','൲'),(3881,1,'targets','ⱄ·͠/ʺ'),(3871,1,'manipulate','ᢷ⎐'),(3871,1,'enforce','fཹ'),(3880,1,'place','䔿'),(3872,1,'override','ெ'),(3884,1,'merge','૨'),(3884,1,'override','ǽ਋'),(3882,1,'task','Ĳ'),(3881,1,'sso','㎐@Ά	ĬÇ'),(3881,1,'place','‒ڄࡱ৤'),(3881,1,'paths','⯿'),(3886,1,'merge','Ȕ'),(3885,1,'override','ʜ '),(3871,1,'12399','㟽='),(3863,1,'override',''),(3846,1,'paths','ć\n'),(3820,1,'noauthnopriv','['),(3819,1,'noauthnopriv','´'),(3819,1,'ifstatus',''),(3782,1,'nsclient','oí\r'),(3885,1,'merge','ী'),(3887,1,'jpeg','ൕ'),(3881,1,'monitorstatus','ࡊᒮՎ&ļ'),(3881,1,'forwarded','ὒ'),(3879,1,'override','㫆ᔾ'),(3879,1,'merge','㧵Ħ'),(3888,1,'201','ޢ'),(3871,1,'place','ᇸ࡚Ʊभ¡'),(3871,1,'thinking','䳋'),(3871,1,'today','␐'),(3872,1,'hup','ϸ,'),(3871,1,'enclosed','⫓\r'),(3780,1,'sitting','æ'),(3760,1,'place','%'),(3879,1,'sso','࠹цy'),(3871,1,'override','ᖞ£'),(3879,1,'manipulate','⅝'),(3878,1,'traps','Ђ'),(3878,1,'nsclient','̵\r\n	'),(3877,1,'targets','Ѳ'),(3877,1,'place','¸ңĴ'),(3886,1,'override','Ƣ'),(3871,1,'mountains','䗴'),(3879,1,'task','▸'),(3879,1,'place','㢭ᦄ'),(3879,1,'paths','ᙿ'),(3887,1,'paths','఩'),(3871,1,'paths','　'),(3721,1,'today','Ȇ'),(3744,1,'noauthnopriv',''),(3757,1,'resident',''),(3888,1,'override','ŪȲ'),(3888,1,'place','ƜЍ'),(3891,1,'override','š'),(3892,1,'paths',''),(3819,6,'ifstatus','Ī');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict23` ENABLE KEYS */;

--
-- Table structure for table `dict24`
--

DROP TABLE IF EXISTS `dict24`;
CREATE TABLE `dict24` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict24`
--


/*!40000 ALTER TABLE `dict24` DISABLE KEYS */;
LOCK TABLES `dict24` WRITE;
INSERT INTO `dict24` VALUES (3881,1,'generic','ᶟ\ZŖĽ7oH4(L+'),(3871,1,'high','⣲ၱፊ'),(3780,1,'sap','ď	'),(3782,1,'nt','Ė'),(3799,1,'packets','°_'),(3822,1,'packets','I\Z'),(3824,1,'packets','Ī'),(3825,1,'packets','§j'),(3834,1,'packets','~'),(3841,1,'sap','0'),(3720,1,'fade',''),(3884,1,'bring','޸'),(3884,1,'alias','ᐙ'),(3881,1,'51','y'),(3873,1,'nt','Ŭ	*'),(3871,1,'4400000000e','⭢'),(3864,1,'packets','\" '),(3862,1,'packets',''),(3880,1,'imported','㡯'),(3880,1,'high','ഷਪ˨'),(3884,1,'confirming','ၵ'),(3881,1,'alias','㡲'),(3873,1,'implement','Ų'),(3879,1,'51','y䴊'),(3880,1,'generic','ᇪ'),(3880,1,'alias','ᢛ໯'),(3880,1,'51','y'),(3883,1,'alias','Â'),(3882,1,'imported','ñ'),(3881,1,'implement','㊬Ŭf'),(3740,1,'high','A0\r\r'),(3741,1,'high','\''),(3743,1,'high','&'),(3750,1,'sap','	'),(3751,1,'high','L\"\nB#V&'),(3762,1,'packets',',1'),(3720,1,'high','Ķ'),(3879,1,'nt','㊾	'),(3872,1,'referenced','ٲ˧Ǆ'),(3872,1,'generic','ˣ'),(3872,1,'alias','૧Fĭ0'),(3871,1,'referenced','⫅'),(3871,1,'leaving','ࡪ'),(3884,1,'generic','ş'),(3878,1,'51','p'),(3879,1,'s2chapter2a','帵¡D'),(3879,1,'referenced','࿙␦ઐծԟ'),(3871,1,'all1','䤞'),(3871,1,'autoconfiguration','႞'),(3881,1,'high','Ἂ'),(3879,1,'imported','⧪Ɯ¬࡞ᰯ&'),(3877,1,'51','w'),(3879,1,'alias','ഘ^᩺ļaƜÚTƻð଒ኬØ÷	Â'),(3879,1,'generic','∐ႦH௲ьЁ'),(3879,1,'implement','つዢ඗'),(3871,1,'implement',''),(3884,1,'high','ռ৤'),(3885,1,'generic','š'),(3885,1,'high','ͨ'),(3886,1,'alias','Ü'),(3886,1,'generic','ѭ'),(3887,1,'alias','Ąअ'),(3887,1,'high','֦'),(3888,1,'implement','ˎ'),(3890,1,'circular','ڬ4'),(3890,1,'confirming','Ԭᖢ'),(3890,1,'high','ः'),(3891,1,'alias','Å̞'),(3750,6,'sap','@'),(3782,6,'nt','Ơ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict24` ENABLE KEYS */;

--
-- Table structure for table `dict25`
--

DROP TABLE IF EXISTS `dict25`;
CREATE TABLE `dict25` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict25`
--


/*!40000 ALTER TABLE `dict25` DISABLE KEYS */;
LOCK TABLES `dict25` WRITE;
INSERT INTO `dict25` VALUES (3810,1,'normal','%'),(3745,1,'string','b	'),(3875,1,'string','ǧV'),(3785,1,'string','-D\"&'),(3789,1,'string','S$'),(3791,1,'string','W8'),(3793,1,'string','R'),(3795,1,'string','E'),(3797,1,'string',':'),(3798,1,'string',''),(3744,1,'string','UV(@%\rÎ'),(3755,1,'string','L'),(3756,1,'string','V	'),(3757,1,'string','Ē	'),(3758,1,'string','4'),(3761,1,'string','-D\"&'),(3763,1,'string','S'),(3766,1,'string','Y'),(3776,1,'string','K'),(3780,1,'string','TĄ'),(3748,1,'string','-D\"&'),(3732,1,'string','*E\"&'),(3859,1,'string','5'),(3871,1,'marker','ᩏ'),(3871,1,'presently','ᡛ'),(3863,1,'normal',''),(3872,1,'string','ֵ'),(3829,1,'string','i'),(3720,1,'namespace','ħ'),(3827,1,'normal','\"'),(3784,1,'string','-D\"&'),(3873,1,'string','Ē'),(3871,1,'string','ڌකؠ«Ɨ]\r	\n=̲ì౜Ⴍ'),(3754,1,'string','Z'),(3813,1,'string','x'),(3856,1,'string',''),(3881,1,'namespace','〮'),(3878,1,'listener','ƙ'),(3881,1,'listener','ᶏ	\nĎ	Łʫ࣊'),(3854,1,'string','0V\"&'),(3730,1,'string','3'),(3846,1,'string','º*'),(3844,1,'string','A\n'),(3840,1,'normal','ʈ'),(3840,1,'apparent','Þ'),(3833,1,'string','-D\"&'),(3835,1,'normal','@'),(3851,1,'string','e'),(3877,1,'paste','ֱ'),(3864,1,'responds','\Z'),(3871,1,'characteristics','⬴'),(3853,1,'string',';'),(3880,1,'informational','ࣻ'),(3849,1,'string','V'),(3848,1,'string','X\n'),(3827,1,'string','X	tA'),(3722,1,'string','Ć'),(3751,1,'nx','Ǭ'),(3876,1,'string','ƛ'),(3871,1,'noise','㉶'),(3871,1,'normal','ໝၦ'),(3727,1,'string','b'),(3728,1,'string','*D\"&'),(3729,1,'string','+9'),(3872,1,'declaration','ૅ\n'),(3881,1,'spent','⧹'),(3874,1,'string','ͩ'),(3818,1,'string','-D\"&'),(3819,1,'string','Æ'),(3820,1,'string','m'),(3822,1,'spent','Ï'),(3823,1,'string',''),(3824,1,'string','Í+\''),(3825,1,'string','î'),(3814,1,'string','-D\"&'),(3804,1,'string','@'),(3804,1,'responds','&'),(3799,1,'string','\r'),(3782,1,'string','©'),(3880,1,'collections','႔'),(3879,1,'string','䉇ͪ-) +1%ú÷ۉ'),(3879,1,'informational','勊Í6൙ķV'),(3742,1,'string','K'),(3740,1,'normal','Ĉ'),(3739,1,'string','*D\"&'),(3737,1,'string','*D\"&'),(3738,1,'string','`'),(3881,1,'string','ऋ%ˈ ³⊼!iŵ/ע'),(3884,1,'normal','ɨॼ'),(3884,1,'string','ፁ'),(3887,1,'string','಻'),(3888,1,'string','ڟ'),(3890,1,'normal','ޟܪ్'),(3890,1,'string','˙');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict25` ENABLE KEYS */;

--
-- Table structure for table `dict26`
--

DROP TABLE IF EXISTS `dict26`;
CREATE TABLE `dict26` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict26`
--


/*!40000 ALTER TABLE `dict26` DISABLE KEYS */;
LOCK TABLES `dict26` WRITE;
INSERT INTO `dict26` VALUES (3878,1,'16','̳'),(3877,1,'private','С$´Ř(-+'),(3748,1,'close','-'),(3871,1,'insensitive','⚧'),(3765,1,'inst','¦'),(3744,1,'site','$'),(3871,1,'close','㕰ခɴ'),(3871,1,'45min','⓻'),(3879,1,'close','〫'),(3871,1,'described','⺅'),(3871,1,'combine','ࢊ'),(3744,1,'snmpget','Ɗ'),(3871,1,'formatstring','ཅӑ'),(3761,1,'close','-'),(3756,1,'described','®'),(3878,1,'implied','D'),(3871,1,'1997','❡'),(3871,1,'16','⏤ᵋ1ǹ'),(3869,1,'site','	'),(3869,1,'private','#'),(3868,1,'site','º'),(3854,1,'close','¶-'),(3833,1,'close','-'),(3827,1,'insensitive','^Ĝ'),(3784,1,'close','-'),(3785,1,'close','-'),(3814,1,'close','-'),(3818,1,'close','-'),(3744,1,'insensitive','ŉ'),(3739,1,'close','-'),(3877,1,'implied','K'),(3872,1,'sourced','ɶف+ǘ'),(3871,1,'snmpget','㺂ǯ)Ɛ'),(3871,1,'ranging','ƌ'),(3871,1,'items','ᄋ௑ə'),(3879,1,'interchangeably','⁾'),(3879,1,'implied','M'),(3879,1,'described','ညፆΗ㈑'),(3879,1,'combine','⋁ዜ࡛'),(3737,1,'close','-'),(3732,1,'close','-'),(3731,1,'variance','&.'),(3730,1,'route','!'),(3728,1,'close','-'),(3726,1,'close','@'),(3721,1,'site','D'),(3721,1,'close','Ǐ'),(3720,1,'htmldragcopy','õ'),(3879,1,'16','ե'),(3877,1,'press','е'),(3879,1,'items','⍄:'),(3879,1,'private','ᥤ'),(3879,1,'site','ᖊа'),(3880,1,'16','ը'),(3880,1,'close','පiៜ0'),(3880,1,'combine','㑾'),(3880,1,'described','᫮ƆТՂᕔрɥ'),(3880,1,'implied','M'),(3880,1,'items','⒗'),(3880,1,'press','ే'),(3880,1,'site','ફ͜W⛖'),(3881,1,'16','͸⁸'),(3881,1,'close','␵੽¯Ƌ|Ö׳'),(3881,1,'described','෍K'),(3881,1,'geteventsforservice','৬ෟ'),(3881,1,'implied','M⍙'),(3881,1,'listen','㉫	'),(3881,1,'private','ゾľؙ'),(3884,1,'close','ƦਗҪ'),(3884,1,'combine','࢕'),(3885,1,'close','ǽ%؋('),(3887,1,'wasted','൱'),(3888,1,'close','õ'),(3889,1,'close','ђ'),(3890,1,'close','ò'),(3890,1,'described','੏Ĉ'),(3892,1,'items','');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict26` ENABLE KEYS */;

--
-- Table structure for table `dict27`
--

DROP TABLE IF EXISTS `dict27`;
CREATE TABLE `dict27` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict27`
--


/*!40000 ALTER TABLE `dict27` DISABLE KEYS */;
LOCK TABLES `dict27` WRITE;
INSERT INTO `dict27` VALUES (3881,1,'monitoring','Ǚϴ&	H;GZ0Ŏʑኺ'),(3866,1,'monitoring','I'),(3851,1,'idle','!+'),(3778,1,'buffer','p'),(3799,1,'monitoring','î '),(3820,1,'monitoring','T'),(3840,1,'monitoring','ä'),(3841,1,'ss','@'),(3871,1,'eq','᠍'),(3871,1,'moderately','⍢'),(3871,1,'monitoring','ι࠮㭛'),(3870,1,'extensions',''),(3871,1,'14all','­'),(3871,1,'59pm','✘'),(3879,1,'extensions','㣠'),(3878,1,'monitoring','°Óʝ!\"# *\"\"\Z'),(3877,1,'monitoring','Έɰ'),(3874,1,'monitoring','@ƍ'),(3873,1,'buffer','Ѯ'),(3872,1,'monitoring','ȝ'),(3871,1,'stack','ྠݕB\r$\r\n\r	\r	\n\rW%Ȩ+\rǼ +Nmᵊ'),(3868,1,'monitoring','K'),(3765,1,'pcp',',K'),(3871,1,'5h45min','⓹'),(3879,1,'monitoring','Đưs¥̲ଢːࠝ!L8Ŀċ¡\nƁ3gʏ7\"bøʸL¼Lѯ̯đºȥ\'K̐ɰҊìࡄ\nᔉ'),(3879,1,'fulfilled','ᩦ'),(3879,1,'59pm','嗳'),(3871,1,'months','ՂᾆDMË!	.'),(3871,1,'acceptable','੅CćÔᰉ'),(3881,1,'extensions','Ặॹ'),(3880,1,'ss','⢬өƯ'),(3879,1,'night','啸'),(3880,1,'monitoring','¸\"©&H~¥C-͘E-	%R	`&Ʉƥ\n#WK$dȚĵw¼`T4$\\\"gAEʎ %?Ʈ\r7ʜÆ\r)ąHCJɬ¨Égçംɓł!>˝ڈ2ȃ­/'),(3881,1,'stack','❝'),(3881,1,'wrappers','⣠n'),(3884,1,'monitoring','ΣԣØЉ'),(3885,1,'monitoring','ҿ'),(3887,1,'monitoring','܀'),(3888,1,'monitoring','ϝ	'),(3890,1,'monitoring','ऍĿùػʚɘJ/\"Ԋ'),(3890,1,'ss','ᯛ'),(3877,2,'monitoring',''),(3877,6,'monitoring','ޓ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict27` ENABLE KEYS */;

--
-- Table structure for table `dict28`
--

DROP TABLE IF EXISTS `dict28`;
CREATE TABLE `dict28` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict28`
--


/*!40000 ALTER TABLE `dict28` DISABLE KEYS */;
LOCK TABLES `dict28` WRITE;
INSERT INTO `dict28` VALUES (3881,1,'instances','౦\nOŘռĝŪÚ᷇'),(3881,1,'inherited','ั'),(3880,1,'window','භਙéಣ0}ūொ̂\nၣm-		$	'),(3879,1,'window','ぶᐹ᡽b´F'),(3879,1,'parse','䖞Ĥ®'),(3879,1,'minn','䕫'),(3754,1,'parse','ĥ'),(3765,1,'instances',''),(3871,1,'1hour','⌊'),(3871,1,'colon','ຒෂ'),(3871,1,'mixed','㧦'),(3871,1,'parse','ⲋ'),(3871,1,'tempted','౎'),(3879,1,'gwcollage','斩'),(3879,1,'h4','崦Ą'),(3879,1,'instances','⓱᫦'),(3881,1,'fieldsort','᥈'),(3881,1,'family','㣁'),(3881,1,'consistency','ℷ'),(3881,1,'validating','₨'),(3881,1,'window','⩵'),(3886,1,'instances','ѷg'),(3887,1,'direction','ʃஂ'),(3888,1,'dictionary','ҭX'),(3888,1,'instances','Ǆ	ĶǢ'),(3888,1,'window','މČ'),(3890,1,'wap','ܕ'),(3890,1,'window','ݢൈ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict28` ENABLE KEYS */;

--
-- Table structure for table `dict29`
--

DROP TABLE IF EXISTS `dict29`;
CREATE TABLE `dict29` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict29`
--


/*!40000 ALTER TABLE `dict29` DISABLE KEYS */;
LOCK TABLES `dict29` WRITE;
INSERT INTO `dict29` VALUES (3880,1,'assistance','ӟÊ'),(3871,1,'4294967300','䴍'),(3871,1,'32','ࡿᔘᜢ᝘Ã'),(3823,1,'xml','Á'),(3827,1,'semicolon','ƣ'),(3840,1,'authority',''),(3849,1,'eth0','_'),(3863,1,'qq','o'),(3866,1,'500','£'),(3869,1,'500',''),(3869,1,'xml','i'),(3870,1,'xml',' '),(3871,1,'12420','㠍='),(3822,1,'500','E'),(3780,1,'james','ů'),(3778,1,'sensitive','Ú'),(3890,1,'lock','ธ'),(3890,1,'global','ҺXN૰\Z/\Z'),(3757,1,'statusflags','«'),(3886,1,'arrangement','ʲ'),(3885,1,'xml','Ƞر'),(3881,1,'xml','Ḗo+\"LĀǫźN੬ŎÖZæ'),(3879,1,'workstation','⏣'),(3879,1,'xml','䦐ǝƪò'),(3879,1,'january','晛4'),(3879,1,'sensitive','ॱᅟ⧷ᒝg'),(3879,1,'hr','Տ墣(Ñ('),(3871,1,'xml','ˢ᲻I঩.'),(3871,1,'primary','ধ;M'),(3871,1,'january','ᦙ᷒'),(3884,1,'recoveries','ӄ਋'),(3885,1,'recoveries','ٳFo'),(3881,1,'hostinhostgroup','ᑬ'),(3880,1,'workstation','ု'),(3879,1,'global','ᇅઊ]ਝ⬸ຏ'),(3874,1,'xml','ŐU'),(3871,1,'wraps','䭔Ż'),(3880,1,'primary','ഡë'),(3880,1,'ez','ࡎǘʩ'),(3879,1,'ez','ĸ੤ٻഄؔK,%&YƐǇƺ}TƜöÑ_'),(3879,1,'dashboards','撘'),(3879,1,'assistance','ӜÊ'),(3878,1,'32','բ'),(3877,1,'executables','ժ'),(3876,1,'xml',':'),(3891,1,'recoveries','Ɵ^leǤC^R'),(3871,1,'calculating','᪋ᯊ፮'),(3888,1,'thei','˧'),(3890,1,'detects','ᖣ'),(3881,1,'global','ன⨢˪'),(3880,1,'dashboards','ࠜ܆É'),(3881,1,'combines','ǈ'),(3881,1,'assistance','˯Ê'),(3726,1,'wraps',''),(3871,1,'formating','᳑'),(3881,1,'semicolon','Ჩ'),(3875,1,'xml','ĨH'),(3756,1,'sensitive','ç'),(3880,1,'hr','Ւ'),(3881,1,'hr','͢'),(3890,1,'sensitive','ȩ\nٕ'),(3881,1,'primary','োⶀ'),(3872,1,'lock','ѵ'),(3872,1,'recoveries','অ5O'),(3873,1,'lock','ҿ\r'),(3887,1,'recoveries','ࢴFnë');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict29` ENABLE KEYS */;

--
-- Table structure for table `dict2A`
--

DROP TABLE IF EXISTS `dict2A`;
CREATE TABLE `dict2A` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict2A`
--


/*!40000 ALTER TABLE `dict2A` DISABLE KEYS */;
LOCK TABLES `dict2A` WRITE;
INSERT INTO `dict2A` VALUES (3761,2,'nagios',''),(3726,2,'nagios',''),(3815,1,'nagios','\r'),(3818,2,'nagios',''),(3775,1,'nagios',''),(3849,1,'nagios',''),(3730,1,'nagios',''),(3781,2,'nagios',''),(3890,1,'nagios','\'R#A%\n%88.ºA¿Ĥ¥·@!p4>\"\rO=PO6TTPS7$2G# */&%£J6ZI3}lC4wµY>H	$\'4!*1@9 ̚ S,)!O%#PD\'\Z&\r\r	'),(3745,2,'nagios',''),(3880,1,'element','ာĭࢣḗKÅ˻'),(3871,1,'leakage','Ĕ'),(3750,1,'nagios',''),(3881,1,'human','㐆'),(3853,1,'nagios',''),(3792,1,'nagios',''),(3793,1,'connection','j'),(3728,1,'nagios','Ă'),(3877,1,'connection','©Մ'),(3840,1,'nagios','ñ'),(3841,1,'nagios',''),(3842,1,'nagios',''),(3754,1,'nagios','	&Č5-!15!'),(3729,2,'nagios',''),(3881,1,'connection','ӟܭCE\Z×6'),(3881,1,'phpdocumentor','⾰'),(3863,1,'connection','I'),(3757,2,'nagios',''),(3792,2,'nagios',''),(3790,2,'nagios',''),(3879,1,'street','{'),(3859,1,'nagios',''),(3860,1,'nagios',''),(3861,1,'nagios',''),(3845,1,'nagios',''),(3822,1,'nagios',''),(3823,1,'citrix','F '),(3823,1,'nagios',''),(3824,1,'citrix','\n\"\'L\n'),(3824,1,'nagios',''),(3825,1,'citrix','\Z\'$'),(3825,1,'nagios',''),(3826,1,'nagios',''),(3827,1,'connection','1ĝ\Z'),(3827,1,'nagios','Ǡ'),(3828,1,'nagios','\n'),(3795,1,'connection',']'),(3732,1,'nagios','ă'),(3785,2,'nagios',''),(3885,1,'nagios','Ų,ʃ9>:ĳĎ'),(3755,2,'nagios',''),(3751,2,'nagios',''),(3723,2,'nagios',''),(3732,2,'nagios',''),(3722,1,'eliminated','j'),(3888,1,'monarchdeploy','ȭa'),(3785,1,'nagios','ă'),(3740,2,'nagios',''),(3880,1,'street','{'),(3764,2,'nagios',''),(3725,1,'nagios',''),(3780,2,'nagios',''),(3889,1,'nagios','Ƈ̑	4'),(3744,2,'nagios',''),(3879,1,'dojo','Ϳ៉'),(3879,1,'connection','ᇆ੧'),(3879,1,'cellspacing','崙b´F'),(3878,1,'street','r'),(3878,1,'nagios','Ɛ7\n\n\n\n\n\n\n\n\n\n\n\n\Zʪ	*	*	,	)	)	)	)	\'	\'	*	\'		&	'),(3878,1,'citrix','ؘ'),(3820,2,'nagios',''),(3880,1,'front','ʓ'),(3880,1,'frame','╊K7∑3'),(3760,2,'nagios',''),(3752,2,'nagios',''),(3742,2,'nagios',''),(3741,2,'nagios',''),(3786,1,'nagios',''),(3787,1,'connection','k'),(3787,1,'nagios','o'),(3788,1,'nagios','/'),(3789,1,'nagios','	8'),(3881,1,'frame','ᝳr'),(3778,1,'nagios',''),(3777,1,'nagios',''),(3776,1,'nagios',''),(3726,1,'nagios',''),(3813,1,'nagios','('),(3747,2,'nagios',''),(3873,1,'tasks',','),(3727,1,'connection',''),(3734,2,'nagios',''),(3735,2,'nagios',''),(3736,2,'nagios',''),(3871,1,'demo1','ຜ\r'),(3864,1,'nagios',''),(3865,1,'nagios',''),(3866,1,'nagios',''),(3867,1,'nagios',''),(3868,1,'nagios','	\n	'),(3871,1,'250','ኒ'),(3871,1,'9733333333e','⯸'),(3871,1,'connection','உဨŚᓩ'),(3863,1,'nagios',''),(3846,1,'connection','Ġ'),(3846,1,'nagios','ĝ'),(3847,1,'nagios',''),(3848,1,'connection',''),(3818,1,'nagios','ă'),(3817,1,'nagios',''),(3818,1,'connection','n!+*'),(3777,2,'nagios',''),(3776,2,'nagios',''),(3768,2,'nagios',''),(3769,2,'nagios',''),(3770,2,'nagios',''),(3771,2,'nagios',''),(3772,2,'nagios',''),(3773,2,'nagios',''),(3774,2,'nagios',''),(3775,2,'nagios',''),(3767,2,'nagios',''),(3766,2,'nagios',''),(3808,2,'nagios',''),(3807,2,'nagios',''),(3806,2,'nagios',''),(3805,2,'nagios',''),(3804,2,'nagios',''),(3798,2,'nagios',''),(3799,2,'nagios',''),(3800,2,'nagios',''),(3801,2,'nagios',''),(3802,2,'nagios',''),(3803,2,'nagios',''),(3797,2,'nagios',''),(3773,1,'nagios',''),(3772,1,'nagios',''),(3771,1,'nagios',''),(3770,1,'nagios',''),(3769,1,'nagios',''),(3768,1,'nagios',''),(3767,1,'nagios',''),(3766,1,'nagios',''),(3755,1,'connection','{\Z'),(3755,1,'nagios','x'),(3756,1,'connection',''),(3756,1,'nagios','Â'),(3757,1,'connection',''),(3757,1,'nagios',''),(3758,1,'nagios',''),(3759,1,'nagios',''),(3760,1,'nagios',''),(3761,1,'connection','n!+*'),(3761,1,'nagios','ă'),(3762,1,'connection','*/'),(3762,1,'nagios','Ä'),(3763,1,'connection','.2'),(3763,1,'nagios','_('),(3764,1,'nagios',''),(3765,1,'nagios','RY'),(3766,1,'connection','®'),(3750,1,'connection',''),(3881,1,'front','ⅱ'),(3852,1,'nagios',''),(3851,1,'nagios','5/'),(3791,1,'nagios',''),(3790,1,'nagios',''),(3811,2,'nagios',''),(3813,2,'nagios',''),(3728,1,'connection','k!+*'),(3746,2,'nagios',''),(3876,1,'nagios','aĴ'),(3875,1,'nagios','ƨ'),(3840,1,'connection','°'),(3840,1,'ccc','Ȇ'),(3748,2,'nagios',''),(3892,1,'nagios','ª}'),(3752,1,'nagios',')^'),(3753,1,'nagios',''),(3810,2,'nagios',''),(3796,2,'nagios',''),(3759,2,'nagios',''),(3807,1,'nagios',''),(3808,1,'nagios',''),(3809,1,'nagios',''),(3810,1,'nagios',''),(3811,1,'connection','XP'),(3748,1,'connection','n!+*'),(3748,1,'nagios','ă'),(3871,1,'eliminated','՛'),(3854,1,'nagios','ħ'),(3834,1,'nagios','{'),(3834,1,'suid','3'),(3835,1,'nagios',''),(3836,1,'nagios',''),(3837,1,'nagios',''),(3838,1,'nagios',''),(3873,1,'nagios','¬KgX	H1!\"!@?\'*\'*(('),(3877,1,'street','y'),(3787,2,'nagios',''),(3794,1,'nagios',''),(3784,2,'nagios',''),(3884,1,'nagios','ɅĮ[\\Ȗ˹˿Į[\\Ⱥ'),(3754,2,'nagios',''),(3750,2,'nagios',''),(3722,2,'nagios',''),(3731,2,'nagios',''),(3720,1,'element','ø'),(3888,1,'hostgroup1','׸'),(3812,1,'nagios',''),(3785,1,'connection','n!+*'),(3739,2,'nagios',''),(3763,2,'nagios',''),(3872,1,'connection','ª'),(3797,1,'nagios',''),(3724,1,'nagios',''),(3871,1,'human','ᾱ'),(3820,1,'nagios',''),(3820,1,'privpass',''),(3821,1,'duplex',' '),(3821,1,'nagios',''),(3822,1,'connection','$'),(3814,1,'nagios','ă'),(3789,2,'nagios',''),(3879,1,'ships','ⴧ'),(3793,2,'nagios',''),(3728,2,'nagios',''),(3881,1,'closing','⹬'),(3881,1,'nagios','ڂJ\r	ÚKᒷƹ&&ؿ\nº'),(3862,1,'nagios',''),(3756,2,'nagios',''),(3817,2,'nagios',''),(3774,1,'nagios',''),(3848,1,'nagios',''),(3729,1,'nagios',''),(3779,2,'nagios',''),(3888,1,'nagios','ĬZ>\"6\n\ne-\nA/LćGȀ'),(3743,2,'nagios',''),(3881,1,'element','὜ᄌ'),(3880,1,'dojo','˩䌛	'),(3871,1,'justification','ᵎ'),(3749,1,'nagios',''),(3850,1,'nagios',''),(3789,1,'stale','t'),(3812,2,'nagios',''),(3727,1,'nagios',''),(3874,1,'nagios','˞'),(3839,1,'nagios',''),(3891,1,'nagios','ˑ˗'),(3751,1,'nagios',''),(3816,1,'nagios',''),(3780,1,'connection','Š'),(3780,1,'nagios',''),(3781,1,'nagios',''),(3782,1,'connection','m'),(3782,1,'nagios',''),(3783,1,'connection','J'),(3783,1,'nagios','¸'),(3784,1,'connection','n!+*'),(3784,1,'nagios','ă'),(3734,1,'nagios',''),(3735,1,'nagios',''),(3736,1,'nagios',''),(3737,1,'connection','k!+*'),(3737,1,'nagios','Ă'),(3738,1,'connection','L'),(3738,1,'nagios','h'),(3739,1,'connection','k!+*'),(3739,1,'nagios','Ă'),(3740,1,'connection','±'),(3740,1,'nagios',''),(3741,1,'nagios',''),(3742,1,'nagios',''),(3743,1,'nagios','o'),(3744,1,'connection','Ŵ'),(3744,1,'nagios','Ř'),(3745,1,'connection','\Z?I'),(3745,1,'nagios','Ã'),(3746,1,'nagios',''),(3747,1,'nagios',''),(3729,1,'connection','k'),(3809,2,'nagios',''),(3795,2,'nagios',''),(3814,2,'nagios',''),(3758,2,'nagios',''),(3798,1,'nagios',''),(3799,1,'nagios',''),(3800,1,'nagios',''),(3801,1,'nagios',''),(3802,1,'nagios',''),(3803,1,'nagios',''),(3804,1,'nagios','q'),(3805,1,'nagios','l'),(3806,1,'nagios',''),(3734,1,'connection','F\n'),(3733,1,'nagios',''),(3879,1,'front','̩'),(3879,1,'nagios','Ęȋ\nЖÖЂ#:		!	%&.ЙS*½ফ(:	ÅDĜ\rJK*:͓Ðñĳ޲.-	ʔϧ÷\n̒ů«ö5·v\'ż­;- \n(āʁ¿ŀI[h_>UӪJ\Z,! ;;	]	\Z$		&-v\rণ1^_\n9{;4Ěʂ'),(3871,1,'element','ᣛᇼ\rך6'),(3854,1,'connection','y:+*'),(3829,1,'nagios',''),(3830,1,'nagios','P'),(3831,1,'connection',''),(3831,1,'nagios',''),(3832,1,'nagios',''),(3833,1,'connection','n!+*'),(3833,1,'nagios','ă'),(3778,2,'nagios',''),(3816,2,'nagios',''),(3880,1,'connection','ᮉ⦧'),(3879,1,'tasks','嘣â'),(3872,1,'nagios','çՆ980>\"Ƞĵ'),(3815,2,'nagios',''),(3877,1,'nagios','¯@Hʋ2ƸT\nQi	E,'),(3794,2,'nagios',''),(3786,2,'nagios',''),(3793,1,'nagios',''),(3732,1,'connection','l!+*'),(3731,1,'connection',''),(3731,1,'nagios',''),(3783,2,'nagios',''),(3881,1,'street','{'),(3753,2,'nagios',''),(3749,2,'nagios',''),(3720,2,'dojo',''),(3730,2,'nagios',''),(3720,1,'dojo',' .\rF)\r'),(3887,1,'nagios','ɋ\rͯ:@:ĳǻǋn¡'),(3812,1,'joy',''),(3811,1,'nagios','«'),(3779,1,'nagios',''),(3738,2,'nagios',''),(3737,2,'nagios',''),(3762,2,'nagios',''),(3871,1,'resize','̾␺	m'),(3796,1,'nagios',''),(3795,1,'nagios',''),(3723,1,'nagios',''),(3722,1,'nagios',''),(3782,2,'nagios',''),(3819,2,'nagios',''),(3871,1,'front','倜'),(3871,1,'frame','ᗂ'),(3819,1,'privpass','æ'),(3819,1,'nagios',''),(3880,1,'heath','″'),(3880,1,'nagios','À ®\nևró*:7&ƀȡŽ>Ç-_^N	îUXʌFFç$ѿ6aTPHTǬɂmƍDGDƩ=:9+oīDÉy(OU(uÝÎēˠ}ð৚2E\'+Pí'),(3733,2,'nagios',''),(3844,1,'nagios',''),(3843,1,'nagios',''),(3725,2,'nagios',''),(3724,2,'nagios',''),(3814,1,'connection','n!+*'),(3791,2,'nagios',''),(3788,2,'nagios',''),(3858,1,'nagios','\r'),(3857,1,'nagios',''),(3855,1,'nagios','\r'),(3856,1,'connection','X'),(3856,1,'nagios',''),(3727,2,'nagios',''),(3880,1,'verification','ᮬ'),(3765,2,'nagios',''),(3881,1,'logical','᫒»'),(3821,2,'nagios',''),(3822,2,'nagios',''),(3823,2,'nagios',''),(3824,2,'nagios',''),(3825,2,'nagios',''),(3826,2,'nagios',''),(3827,2,'nagios',''),(3828,2,'nagios',''),(3829,2,'nagios',''),(3830,2,'nagios',''),(3831,2,'nagios',''),(3832,2,'nagios',''),(3833,2,'nagios',''),(3834,2,'nagios',''),(3835,2,'nagios',''),(3836,2,'nagios',''),(3837,2,'nagios',''),(3838,2,'nagios',''),(3839,2,'nagios',''),(3840,2,'nagios',''),(3841,2,'nagios',''),(3842,2,'nagios',''),(3843,2,'nagios',''),(3844,2,'nagios',''),(3845,2,'nagios',''),(3846,2,'nagios',''),(3847,2,'nagios',''),(3848,2,'nagios',''),(3849,2,'nagios',''),(3850,2,'nagios',''),(3851,2,'nagios',''),(3852,2,'nagios',''),(3853,2,'nagios',''),(3854,2,'nagios',''),(3855,2,'nagios',''),(3856,2,'nagios',''),(3857,2,'nagios',''),(3858,2,'nagios',''),(3859,2,'nagios',''),(3860,2,'nagios',''),(3861,2,'nagios',''),(3862,2,'nagios',''),(3863,2,'nagios',''),(3864,2,'nagios',''),(3865,2,'nagios',''),(3866,2,'nagios',''),(3867,2,'nagios',''),(3868,2,'nagios',''),(3720,6,'dojo','ǂ'),(3754,6,'nagios','̚'),(3788,6,'nagios','æ'),(3789,6,'nagios','¢'),(3812,6,'joy','='),(3868,6,'nagios','Ĝ'),(3720,7,'dojo','ǁ'),(3722,7,'nagios','ũ'),(3723,7,'nagios','J'),(3724,7,'nagios','m'),(3725,7,'nagios','g'),(3726,7,'nagios',''),(3727,7,'nagios','Ň'),(3728,7,'nagios','ġ'),(3729,7,'nagios','Ú'),(3730,7,'nagios',''),(3731,7,'nagios','¡'),(3732,7,'nagios','Ģ'),(3733,7,'nagios',''),(3734,7,'nagios','ă'),(3735,7,'nagios','}'),(3736,7,'nagios','Ã'),(3737,7,'nagios','ġ'),(3738,7,'nagios',''),(3739,7,'nagios','ġ'),(3740,7,'nagios','Ǝ'),(3741,7,'nagios','À'),(3742,7,'nagios',''),(3743,7,'nagios',''),(3744,7,'nagios','ɢ'),(3745,7,'nagios','Ă'),(3746,7,'nagios','9'),(3747,7,'nagios','a'),(3748,7,'nagios','Ĥ'),(3749,7,'nagios','1'),(3750,7,'nagios','<'),(3751,7,'nagios','ʱ'),(3752,7,'nagios','ĕ'),(3753,7,'nagios',''),(3754,7,'nagios','̕'),(3755,7,'nagios','Å'),(3756,7,'nagios','û'),(3757,7,'nagios','ƭ'),(3758,7,'nagios','H'),(3759,7,'nagios','Ą'),(3760,7,'nagios','I'),(3761,7,'nagios','Ĥ'),(3762,7,'nagios','å'),(3763,7,'nagios','Ŀ'),(3764,7,'nagios','D'),(3765,7,'nagios','ä'),(3766,7,'nagios','÷'),(3767,7,'nagios','.'),(3768,7,'nagios','/'),(3769,7,'nagios',','),(3770,7,'nagios',','),(3771,7,'nagios',','),(3772,7,'nagios',','),(3773,7,'nagios','0'),(3774,7,'nagios','1'),(3775,7,'nagios','/'),(3776,7,'nagios','ĭ'),(3777,7,'nagios','0'),(3778,7,'nagios','þ'),(3779,7,'nagios',':'),(3780,7,'nagios','Ƶ'),(3781,7,'nagios',' '),(3782,7,'nagios','Ɯ'),(3783,7,'nagios','Ý'),(3784,7,'nagios','Ĥ'),(3785,7,'nagios','Ĥ'),(3786,7,'nagios','E'),(3787,7,'nagios','°'),(3788,7,'nagios','â'),(3789,7,'nagios',''),(3790,7,'nagios','Y'),(3791,7,'nagios','¶'),(3792,7,'nagios','j'),(3793,7,'nagios','®'),(3794,7,'nagios','-'),(3795,7,'nagios',''),(3796,7,'nagios','/'),(3797,7,'nagios','r'),(3798,7,'nagios','ö'),(3799,7,'nagios','ƕ'),(3800,7,'nagios','K'),(3801,7,'nagios','<'),(3802,7,'nagios','B'),(3803,7,'nagios','ä'),(3804,7,'nagios','»'),(3805,7,'nagios','Ó'),(3806,7,'nagios','x'),(3807,7,'nagios','G'),(3808,7,'nagios',''),(3809,7,'nagios','j'),(3810,7,'nagios','4'),(3811,7,'nagios','Ì'),(3812,7,'nagios','9'),(3813,7,'nagios',''),(3814,7,'nagios','Ĥ'),(3815,7,'nagios','r'),(3816,7,'nagios','X'),(3817,7,'nagios','5'),(3818,7,'nagios','Ĥ'),(3819,7,'nagios','Ħ'),(3820,7,'nagios','œ'),(3821,7,'nagios','A'),(3822,7,'nagios','ą'),(3823,7,'nagios','Ò'),(3824,7,'nagios','ŝ'),(3825,7,'nagios','ŉ'),(3826,7,'nagios',','),(3827,7,'nagios','˛'),(3828,7,'nagios','£'),(3829,7,'nagios','}'),(3830,7,'nagios',''),(3831,7,'nagios','×'),(3832,7,'nagios','T'),(3833,7,'nagios','Ĥ'),(3834,7,'nagios','¿'),(3835,7,'nagios',''),(3836,7,'nagios','i'),(3837,7,'nagios','ī'),(3838,7,'nagios','H'),(3839,7,'nagios',':'),(3840,7,'nagios','̞'),(3841,7,'nagios','³'),(3842,7,'nagios','C'),(3843,7,'nagios','o'),(3844,7,'nagios','Û'),(3845,7,'nagios',''),(3846,7,'nagios','ľ'),(3847,7,'nagios','Z'),(3848,7,'nagios','µ'),(3849,7,'nagios',''),(3850,7,'nagios','O'),(3851,7,'nagios',''),(3852,7,'nagios','0'),(3853,7,'nagios','X'),(3854,7,'nagios','ň'),(3855,7,'nagios','?'),(3856,7,'nagios','ƕ'),(3857,7,'nagios','d'),(3858,7,'nagios','9'),(3859,7,'nagios','G'),(3860,7,'nagios','L'),(3861,7,'nagios','7'),(3862,7,'nagios','ğ'),(3863,7,'nagios','Ł'),(3864,7,'nagios',''),(3865,7,'nagios','¦'),(3866,7,'nagios','¼'),(3867,7,'nagios','5'),(3868,7,'nagios','ě');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict2A` ENABLE KEYS */;

--
-- Table structure for table `dict2B`
--

DROP TABLE IF EXISTS `dict2B`;
CREATE TABLE `dict2B` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict2B`
--


/*!40000 ALTER TABLE `dict2B` DISABLE KEYS */;
LOCK TABLES `dict2B` WRITE;
INSERT INTO `dict2B` VALUES (3879,1,'2880','䙦ͷ'),(3878,1,'win','ͭ'),(3879,1,'delete','৐Ƞòᠤʐތਲ,ݳ-໡ᕋb\r\n	\n	\n'),(3870,1,'greatly','÷'),(3722,1,'nbns','T'),(3734,1,'type','Ê'),(3745,1,'suppress','p'),(3747,1,'type','A'),(3751,1,'considers','ʁ'),(3879,1,'icon','ຆ⧴ກ ƒ'),(3880,1,'knowledge','Ϊ䉍'),(3877,1,'type','ԙ'),(3872,1,'type','޸ȫO'),(3762,1,'trigger',''),(3778,1,'sid','M'),(3779,1,'sid','&'),(3780,1,'type','Ĝ'),(3799,1,'type',''),(3812,1,'joyreadbutton',''),(3827,1,'type','Ŕ'),(3831,1,'type','>'),(3834,1,'trigger','®'),(3757,1,'type','w'),(3879,1,'cacti','ᔼ'),(3871,1,'type','ւɐŒɁ\'ʖHඁҾ!ޘ¿غ\nܭᅼNĀ.ń'),(3871,1,'suppress','ᗘܬ'),(3870,1,'mysqli','¿'),(3871,1,'centered','᳃\r'),(3871,1,'delete','㤌'),(3871,1,'knowledge','㎨'),(3871,1,'meta','⫣B'),(3871,1,'nb','⇉'),(3864,1,'trigger','z'),(3880,1,'ingredients','ɴ'),(3880,1,'delete','㋯˺'),(3880,1,'icon','෥\'ମμƇH̊Ţɉูዻ7'),(3880,1,'cacti','਑ጧ\Z'),(3879,1,'win','㊷H'),(3879,1,'userid','卤'),(3879,1,'suppress','㌹׫'),(3879,1,'type','ናHႢ֤ࣗᘍƚƚᆺ'),(3879,1,'lastcheck','䠸'),(3879,1,'knowledge','⛕'),(3881,1,'type','ӢɉwӠ?ț\n	ƥ	\nU\r	\n໏rµBƃസ-Ƨ'),(3881,1,'knowledge','㒇'),(3880,1,'trigger','༑'),(3880,1,'type','௕cQόʄA૯Ò࢕ႀòȆ×ȶȺǷuϧ'),(3862,1,'type','Ċ'),(3856,1,'suppress','y'),(3848,1,'type','-2'),(3846,1,'type','î'),(3837,1,'delete','ö'),(3841,1,'ncp2','j'),(3882,1,'delete',';c\\,)'),(3883,1,'delete',''),(3884,1,'delete','Ƨਕքʨ'),(3884,1,'icon','ّೄ \Z'),(3884,1,'knowledge','঳'),(3884,1,'suppress','ݠ'),(3885,1,'delete','Ǿط'),(3885,1,'icon','ܽ'),(3886,1,'delete','g'),(3887,1,'delete','ӊӤħ Ѧ'),(3887,1,'icon','ɄzনI\ZAŅ'),(3888,1,'delete','å'),(3889,1,'delete','Ͽ'),(3890,1,'icon','ȵ'),(3890,1,'knowledge','ō'),(3890,1,'suppress','ᖮ'),(3890,1,'type','ᘿ'),(3891,1,'delete','˳²'),(3891,1,'type','ȍâɴc'),(3892,1,'type','ÃB');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict2B` ENABLE KEYS */;

--
-- Table structure for table `dict2C`
--

DROP TABLE IF EXISTS `dict2C`;
CREATE TABLE `dict2C` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict2C`
--


/*!40000 ALTER TABLE `dict2C` DISABLE KEYS */;
LOCK TABLES `dict2C` WRITE;
INSERT INTO `dict2C` VALUES (3880,1,'operation','͒ƕвಡ⿣'),(3880,1,'search','܎ɫಷɌȧ࠮ȯȱ\n\Z\"Ὡ	'),(3881,1,'view','ؐᩁଶŜ]	৞4Y¹'),(3881,1,'product','ƚë'),(3881,1,'operation','˷ଈƔ	\nεۭ=i	սa້	'),(3881,1,'creator','ᰘ'),(3888,1,'mailbox','و'),(3880,1,'177','₂'),(3890,1,'view','!;û˽-(]Ơᓈ'),(3890,1,'operation','໊'),(3887,1,'search','Î'),(3886,1,'search','§'),(3721,1,'product','ɩ'),(3880,1,'product','Ћ!6͚Ų㪁c͕'),(3740,1,'operation','ĉ'),(3740,1,'search','§'),(3744,1,'colons','ǎ'),(3757,1,'search','1'),(3763,1,'operation','ª'),(3778,1,'product','ì'),(3778,1,'search','M'),(3789,1,'expire','O'),(3789,1,'search','z'),(3798,1,'expire','0?'),(3799,1,'expire','?'),(3811,1,'search','d'),(3827,1,'search','*ń'),(3835,1,'operation','A'),(3840,1,'fatal','ē'),(3868,1,'ethan','	'),(3868,1,'operation','^'),(3871,1,'12393','㟹?'),(3871,1,'12423','㠕9'),(3871,1,'expire','⵵'),(3871,1,'mikheev','ሧ'),(3871,1,'operation','ᜂÕ'),(3871,1,'pathnames','ⴰ'),(3871,1,'search','㌓೯'),(3871,1,'view','〃ಠ2¿ثʌӍ'),(3872,1,'search','ŀ'),(3872,1,'servicedesc','ݛ'),(3873,1,'mailbox','α'),(3874,1,'product','Ŝ,'),(3875,1,'product','Ĺ'),(3876,1,'product','©'),(3878,1,'product','Ŧ'),(3879,1,'approved','ᨥ'),(3879,1,'expire','ࢃ'),(3879,1,'operation','Ӥᰀ㾔'),(3879,1,'product','Ѳȱ8எϤ䃔'),(3879,1,'search','⎧Qࡣය᳒ѶŹ'),(3879,1,'section1chapter2','嵜uD'),(3879,1,'servicedesc','䐱'),(3879,1,'view','ࠡX˘Q-;K஭\n[ë؇Ē=၎ʇǼ٣ąţLċ᭏'),(3722,1,'product','Ē'),(3884,1,'search','ޔ'),(3882,1,'search','Ľ'),(3880,1,'view','΄ޏࣆońż3\"ĳăǠĹɕS$ǣ@0/ĹsoóøYઋ61PŽ=6¬Cືǧ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict2C` ENABLE KEYS */;

--
-- Table structure for table `dict2D`
--

DROP TABLE IF EXISTS `dict2D`;
CREATE TABLE `dict2D` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict2D`
--


/*!40000 ALTER TABLE `dict2D` DISABLE KEYS */;
LOCK TABLES `dict2D` WRITE;
INSERT INTO `dict2D` VALUES (3879,1,'88','䵳'),(3843,1,'seconds','W'),(3787,1,'seconds','i'),(3781,1,'seconds','B'),(3782,1,'seconds','k'),(3783,1,'seconds','H'),(3783,1,'sends',''),(3784,1,'seconds',';('),(3785,1,'seconds',';('),(3763,1,'seconds','q'),(3740,1,'seconds','ð'),(3871,1,'seconds','ڟ3Ŧ˶QÑ\"Øæ\"đ\r0¬Ҿ̺ ۈĦ(7pÈƃõüȒu;0\r˼࡯Žˤ߇Ɨɨƚ۝$\Z`K'),(3871,1,'replaced','ⷈ U'),(3871,1,'pdp','ઋ\r&\nᗨ'),(3877,1,'supplies','٤'),(3731,1,'seconds',''),(3730,1,'seconds','r'),(3729,1,'seconds','9T'),(3722,1,'controllers','#	KD0'),(3879,1,'commit','ⅨΓNȒࢠTPాΧڰऎ&ኂ/'),(3876,1,'seconds','ő'),(3833,1,'seconds',';('),(3831,1,'seconds',''),(3831,1,'gf','\''),(3799,1,'exceeds','¼'),(3846,1,'iwarning','u'),(3765,1,'pminfo','\r'),(3734,1,'seconds','è'),(3735,1,'seconds','e'),(3727,1,'seconds','w'),(3809,1,'exceeds','@'),(3744,1,'seconds','Ų'),(3745,1,'seconds','³'),(3748,1,'seconds',';('),(3751,1,'exceeds','Ň-'),(3755,1,'seconds','j'),(3756,1,'seconds',''),(3757,1,'seconds',''),(3728,1,'seconds','8('),(3871,1,'lapsed','䝺Ǹ'),(3827,1,'seconds','ķ'),(3872,1,'easiest','ޙ'),(3871,1,'slowly','䗪'),(3837,1,'seconds',''),(3837,1,'sends',')'),(3803,1,'seconds','v'),(3737,1,'seconds','8('),(3721,1,'utilities','§'),(3741,1,'seconds','¬'),(3840,1,'seconds','\r'),(3871,1,'beware','ᱰ'),(3762,1,'seconds','v'),(3761,1,'seconds',';('),(3739,1,'seconds','8('),(3836,1,'seconds','E'),(3835,1,'seconds','H'),(3780,1,'seconds','ïo'),(3766,1,'seconds','\Z'),(3871,1,'920806200','㠯ᛧ#'),(3863,1,'seconds','G'),(3871,1,'lpr','ჃP'),(3846,1,'seconds','Ğ'),(3848,1,'seconds',''),(3844,1,'445',''),(3808,1,'seconds','h'),(3827,1,'exceeds','ʁ'),(3825,1,'sends','9'),(3824,1,'sends','_'),(3824,1,'responses',''),(3822,1,'seconds','mQ'),(3820,1,'seconds','û'),(3814,1,'seconds',';('),(3818,1,'seconds',';('),(3819,1,'seconds','ĉ'),(3811,1,'seconds',''),(3871,1,'exceeds','ણ'),(3871,1,'carry','䂷'),(3854,1,'seconds','>³('),(3840,1,'exceeds',''),(3874,1,'exceeds','ˢ'),(3849,1,'seconds','L'),(3845,1,'seconds','g'),(3733,1,'sends',''),(3732,1,'seconds','8('),(3738,1,'seconds','V'),(3871,1,'expects','ඞĮ'),(3856,1,'seconds','Î'),(3725,1,'exceeds','\''),(3879,1,'easiest','䜫'),(3879,1,'replaced','堐'),(3879,1,'seconds','愇Ů'),(3879,1,'utilities','䱗'),(3880,1,'commit','⠕'),(3880,1,'seconds','ḥFᡍ'),(3881,1,'replaced','ڻ'),(3881,1,'sends','⫆'),(3881,1,'utilities','⓽,'),(3882,1,'commit','Į'),(3884,1,'commit','୒'),(3884,1,'seconds','Ϙ਋'),(3885,1,'seconds','ѐ'),(3887,1,'seconds','ڏ'),(3888,1,'commit','ࣈ'),(3888,1,'replaced','ԩ'),(3889,1,'sends','̡'),(3890,1,'commit','ᴀƯ	\r'),(3890,1,'seconds','݆Ǜ0ϵ#ϲ¼\Zp=ƫͨ2řH'),(3890,1,'statuswml','ܙ'),(3892,1,'replaced','Ń');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict2D` ENABLE KEYS */;

--
-- Table structure for table `dict2E`
--

DROP TABLE IF EXISTS `dict2E`;
CREATE TABLE `dict2E` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict2E`
--


/*!40000 ALTER TABLE `dict2E` DISABLE KEYS */;
LOCK TABLES `dict2E` WRITE;
INSERT INTO `dict2E` VALUES (3877,1,'redistribute',' '),(3808,1,'average',')'),(3799,1,'average','W'),(3798,1,'average',''),(3782,1,'average',''),(3879,1,'redistribute','\"'),(3866,1,'average','B'),(3877,1,'average','ذ'),(3874,1,'average','ɡď'),(3880,1,'listview','ᠬҲ!\'#UȅUbş(b'),(3880,1,'graphed','㒓'),(3880,1,'functions','ೢǐ㔗'),(3880,1,'efficiency','߄㄃'),(3880,1,'decisions','㢨'),(3880,1,'average','䂈'),(3879,1,'unable','⢌śƜ'),(3780,1,'average',']'),(3766,1,'average','c'),(3762,1,'average','*'),(3751,1,'efficiency','ȉ'),(3878,1,'best','²'),(3878,1,'redistribute',''),(3879,1,'archives','呚'),(3879,1,'average','䙢ƁǞ'),(3871,1,'1m','⛬'),(3871,1,'graphed','၃ⶮ'),(3871,1,'functions','ȃͥbϞ׭k٪ґĻණ᨞'),(3740,1,'snmpversion','7¨'),(3862,1,'mailto','Q'),(3840,1,'truth','˪'),(3809,1,'average','#'),(3879,1,'li','嵐/Á'),(3879,1,'graphed','䕒	'),(3879,1,'functions','▔ƚ'),(3879,1,'efficiency','憈'),(3834,1,'average',''),(3879,1,'scheme','ᰤ'),(3879,1,'contactgroups','咋'),(3721,1,'functions','Ȏ'),(3871,1,'speaking','㘖'),(3871,1,'unable','㊶'),(3872,1,'contactgroups','బ'),(3872,1,'functions','ɵ'),(3869,1,'best','H'),(3864,1,'average','f'),(3862,1,'redistribute','c'),(3720,1,'logger','Đ'),(3879,1,'best','㗏'),(3871,1,'archives','ү\rҺΓ☙Ͻ્ɻ'),(3871,1,'fourth','വ'),(3871,1,'best','㏮'),(3871,1,'average','LеԼ@Ã±¾g෾ɔE¥ɜ¦¸5\rkߙĘˡ|FɲʒǤZţ£Słà»և>		¦5dJđIʅJؑI#'),(3880,1,'redistribute','\"'),(3881,1,'archives','◤'),(3881,1,'average','ύ'),(3881,1,'best','Ǌᵕࣲݡ'),(3881,1,'fourth','㤷'),(3881,1,'functions','ἳྜ,'),(3881,1,'getcount','ᑚƊġ'),(3881,1,'latency','ࡴ'),(3881,1,'redistribute','\"'),(3881,1,'scheme','❷įe'),(3881,1,'unable','෮'),(3884,1,'best','ࣆ'),(3886,1,'unable','Ÿ'),(3887,1,'best','ಏĜ'),(3887,1,'unable','Ơ'),(3888,1,'scheme','ڤ'),(3888,1,'windows1','׾O·9Ĺ'),(3889,1,'average','Ÿ'),(3890,1,'average','ዶ'),(3890,1,'decisions','ᕄ'),(3890,1,'functions','΂'),(3891,1,'contactgroups','͙');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict2E` ENABLE KEYS */;

--
-- Table structure for table `dict2F`
--

DROP TABLE IF EXISTS `dict2F`;
CREATE TABLE `dict2F` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict2F`
--


/*!40000 ALTER TABLE `dict2F` DISABLE KEYS */;
LOCK TABLES `dict2F` WRITE;
INSERT INTO `dict2F` VALUES (3871,1,'inputs','ṷ'),(3798,1,'exceed','>'),(3874,1,'exceed','ɳ'),(3804,1,'dump',''),(3871,1,'fetch','˰ãᵤ/tkᕛ'),(3871,1,'dump','ʾ᳐;\Zĭ'),(3871,1,'exceed','ૈᕳ'),(3725,1,'logged','\Z0'),(3871,1,'considerable','Ƚȇ'),(3871,1,'978302400','䣾'),(3871,1,'10e','፩'),(3871,1,'03','⃲ើ'),(3860,1,'greater','.'),(3844,1,'greater','Ë'),(3751,1,'exceed','ǫ'),(3730,1,'greater','\\'),(3878,1,'33','֙'),(3877,1,'03','ض'),(3871,1,'overlaid','ṽ'),(3840,1,'mis','Ó'),(3819,1,'ppp',''),(3871,1,'greater','㰊\Z'),(3809,1,'exceed','N'),(3802,1,'6000','*'),(3871,1,'out2','ⱁ	'),(3879,1,'03','䵲'),(3879,1,'dump','ガ㑸'),(3879,1,'logged','່ॵǽ៟㊅?'),(3880,1,'fetch','⋞'),(3880,1,'greater','ݮ'),(3880,1,'logged','෋0ɯᕙ'),(3881,1,'confuse','⠆'),(3881,1,'greater','ឹ©'),(3881,1,'logged','ᵁᇕਖ'),(3885,1,'impact','՟'),(3887,1,'impact','ޠ'),(3890,1,'greater','ኮ'),(3890,1,'logged','ࠗގ!\n\n\nf');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict2F` ENABLE KEYS */;

--
-- Table structure for table `dict30`
--

DROP TABLE IF EXISTS `dict30`;
CREATE TABLE `dict30` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict30`
--


/*!40000 ALTER TABLE `dict30` DISABLE KEYS */;
LOCK TABLES `dict30` WRITE;
INSERT INTO `dict30` VALUES (3871,1,'distances','䕚'),(3880,1,'standard','Ө07\n\nJיŉ֘Ⓙ'),(3869,1,'standard','Î'),(3871,1,'1020614100','⯑'),(3873,1,'actual','ȥ'),(3873,1,'standard','#'),(3874,1,'installed','CĔ'),(3875,1,'installed','Ĵ'),(3768,6,'oracle','3'),(3880,1,'solution','ȠլH-bť4'),(3880,1,'restarts','⢙ΚŏƯŨ'),(3880,1,'installed','લ㩄'),(3769,6,'oracle','0'),(3774,6,'oracle','5'),(3773,6,'oracle','4'),(3770,1,'oracle',''),(3769,1,'oracle',''),(3768,1,'oracle',''),(3767,1,'oracle',''),(3755,1,'url','K'),(3751,1,'actual','o\n=#T-'),(3744,1,'standard','7'),(3744,1,'installed','ƙ'),(3772,6,'oracle','0'),(3771,6,'oracle','0'),(3767,6,'oracle','2'),(3890,1,'url','Ǫ\n%'),(3890,1,'restarts','ࣖ٣်'),(3890,1,'actual','൹'),(3890,1,'evening','ೊ'),(3890,1,'increase','ᑨ;RZ'),(3888,1,'actual','ΐ'),(3869,1,'oracle','_'),(3866,1,'url',']9'),(3887,1,'url','Ɇত	F'),(3887,1,'restarts','Ը¶'),(3885,1,'url','ܿ'),(3884,1,'url','ٓ಩'),(3885,1,'restarts','˺¶'),(3884,1,'standard','ٳǇ'),(3880,1,'actual','၏೷ٜ'),(3871,1,'8640','≴'),(3777,6,'oracle','4'),(3776,6,'oracle','ı'),(3775,6,'oracle','3'),(3862,1,'installed','ä'),(3856,1,'proto1','`'),(3822,1,'000ms','>'),(3827,1,'url','ß+	'),(3829,1,'installed','&'),(3830,1,'installed','O,'),(3831,1,'installed',''),(3840,1,'article','è'),(3840,1,'overloaded','˥'),(3840,1,'standard','ƪ'),(3734,1,'bigbox','+'),(3820,1,'descr','©'),(3881,1,'scenario','ⷻ'),(3881,1,'installed','ୠßᦌ[༈'),(3871,1,'64','ࢁ൫㘨Ä'),(3871,1,'evolved','ㇹ'),(3871,1,'divide','㔒በ@Ƹ'),(3871,1,'actual','᲏३'),(3876,1,'installed','¤'),(3770,6,'oracle','0'),(3879,1,'installed','ၿ@5D۞ᔔގ෿ӚগԲwҕ[í'),(3879,1,'deny','൭I඀2'),(3879,1,'actual','Ỹጆ'),(3879,1,'8640','䙸ƁǶ'),(3878,1,'standard',' 1c'),(3878,1,'solution','í'),(3878,1,'oracle','ή'),(3878,1,'installed','č̞G785a7/'),(3877,1,'simpler','ο'),(3877,1,'restarts','Ȁą'),(3877,1,'quietly','ԉ'),(3877,1,'deny','͍'),(3778,6,'oracle','Ă'),(3884,1,'restarts','ץ3৘3'),(3881,1,'url','୹⬉'),(3881,1,'solution','⎁'),(3771,1,'oracle',''),(3772,1,'oracle',''),(3773,1,'oracle',''),(3774,1,'oracle',''),(3775,1,'oracle',''),(3776,1,'oracle','\Z\nÄ'),(3777,1,'oracle',''),(3778,1,'oracle','7	'),(3779,1,'oracle',''),(3793,1,'standard',''),(3726,1,'url',':'),(3721,1,'standard','é		'),(3820,1,'implies','Í'),(3881,1,'standard','˸07\n\nJᠿᥔl'),(3881,1,'actual','ԭ'),(3880,1,'url','௏4'),(3881,1,'article','㑥'),(3877,1,'actual','ɳ˙'),(3727,1,'installed','ĝ'),(3879,1,'restarts','ㄙ'),(3879,1,'scenario','㥣ᷮ'),(3879,1,'solution','ˁቈ2⑇%'),(3879,1,'src','帳'),(3879,1,'standard','ӥ07\n\nJݛÈAទဠඏ'),(3879,1,'url','෕Ȉ؅n (\r㪹'),(3871,1,'hrule','Ľ๐٨ե⁠Ä'),(3871,1,'idat2','ᷘ'),(3871,1,'implies','ᰋ'),(3871,1,'increase','䬼Ŭa'),(3871,1,'solution','ѡ'),(3871,1,'src','ᑕᩦĚ\rಲ'),(3871,1,'standard','ٞ঑ִ'),(3871,1,'url','㨆'),(3872,1,'installed','˺'),(3872,1,'overridden','՛'),(3872,1,'standard','g');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict30` ENABLE KEYS */;

--
-- Table structure for table `dict31`
--

DROP TABLE IF EXISTS `dict31`;
CREATE TABLE `dict31` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict31`
--


/*!40000 ALTER TABLE `dict31` DISABLE KEYS */;
LOCK TABLES `dict31` WRITE;
INSERT INTO `dict31` VALUES (3884,1,'command','ɋŎŪ!ƽ ɳɨtŎŪ!ŀ'),(3881,1,'templates','ⱂÝƊĎ&Îŀœ'),(3881,1,'simplest','⍧י'),(3879,1,'doctype','峪'),(3879,1,'fqdn','⠾'),(3879,1,'gathering','叺'),(3879,1,'labels','䡉'),(3879,1,'security','᧔»㢳'),(3879,1,'simplest','㾾'),(3879,1,'sync','㲏'),(3879,1,'templates','↡GƦUC	Y,ೕáÞ8ŉĄīG.$ψ)U!'),(3880,1,'02110',''),(3880,1,'addition','ۡˇn31Ëϯ۾ó໫ᩰֶ'),(3880,1,'blue','䜄x'),(3880,1,'command','຾?ˣڟ౟ƔnW¬\r?Á%H9H*.2E9\n\Z~=%\'F8o+y?#	}=&\'\Z'),(3880,1,'interactivity','ុ'),(3880,1,'security','സ⍊'),(3880,1,'unscheduled','䁵'),(3881,1,'02110',''),(3881,1,'addition','ڣĊ᥷Ňݖ'),(3881,1,'associative','ජ'),(3881,1,'blue','⊻'),(3881,1,'command','ોᡴíరҮ̊'),(3881,1,'security','㎬ɲ'),(3886,1,'sync','Ϛ'),(3890,1,'command','ɋ@&ÒíļːostmYʼc! !ԗ&-)	<Ƙ2\r\r'),(3886,1,'alternative','ѧ'),(3891,1,'templates','?ψ'),(3885,1,'templates','ě	۶'),(3885,1,'command','ФF\r< >!'),(3888,1,'stand','ʟ'),(3888,1,'command','֔1ºɎ'),(3887,1,'templates','aбșҦ'),(3887,1,'fqdn','Œ'),(3890,1,'collapsed','ڨ5'),(3890,1,'addition','ආ'),(3889,1,'templates','φ'),(3888,1,'templates','űɟ'),(3885,1,'addition','࠱'),(3884,1,'templates',']ࠐָ̄ƙ'),(3720,1,'vml','ŕ'),(3720,1,'templates','Ň'),(3888,1,'sync','ح'),(3886,1,'fqdn','Ī'),(3886,1,'command','Џu'),(3892,1,'command','#\nm/\n'),(3891,1,'command','Ƀâ˗<'),(3890,1,'templates','ṗ'),(3887,1,'command','٢G< >!'),(3721,1,'security','ȓ'),(3724,1,'command','\Z'),(3726,1,'command',''),(3727,1,'command',''),(3728,1,'command','č'),(3729,1,'command','¨'),(3730,1,'command','x'),(3732,1,'command','Ď'),(3734,1,'security','Xo'),(3737,1,'command','č'),(3738,1,'command','s'),(3739,1,'command','č'),(3741,1,'command','$3'),(3744,1,'command','ž\r'),(3745,1,'command','(J>'),(3748,1,'command','Đ'),(3751,1,'command','ƽ'),(3752,1,'command','Ò'),(3753,1,'command','%	'),(3755,1,'command',''),(3756,1,'command','Ç'),(3756,1,'security','aY'),(3757,1,'command','U{M2\Z'),(3761,1,'command','Đ'),(3762,1,'command','¥'),(3763,1,'command',''),(3763,1,'security','e'),(3765,1,'command','<'),(3782,1,'command','ř'),(3783,1,'command',',#\n\n'),(3784,1,'command','Đ'),(3785,1,'command','Đ'),(3789,1,'command','E1'),(3791,1,'security','-'),(3803,1,'command','µ'),(3811,1,'command','¸'),(3813,1,'command','8H'),(3814,1,'command','Đ'),(3818,1,'command','Đ'),(3823,1,'srv1',''),(3827,1,'command','ǭ'),(3827,1,'fqdn','ȯ'),(3831,1,'command',''),(3833,1,'command','Đ'),(3834,1,'command','l'),(3840,1,'command','ȡk'),(3841,1,'command','GS'),(3846,1,'command','Ī'),(3848,1,'command','¡'),(3849,1,'command','e'),(3854,1,'command','Ĵ'),(3856,1,'command','/Ys'),(3863,1,'command','(;'),(3863,1,'security','ė'),(3869,1,'cycle','¹'),(3870,1,'addition','Ñ'),(3871,1,'blue','㦲\"'),(3871,1,'command','îĮк+ౙů༐ޡயǇµϵႇ'),(3871,1,'interpolating','А'),(3871,1,'labels','ა\ri'),(3871,1,'rdd','ㅋ᷂'),(3871,1,'reimplementation',''),(3871,1,'security','㺾'),(3871,1,'templates','ⱱ'),(3872,1,'command','HЛƵ(*qʵ'),(3872,1,'templates','࢈'),(3873,1,'addition','ķ'),(3873,1,'command','ı y\r\\B\Z'),(3873,1,'stand','Ĕ'),(3873,1,'templates','ǧ'),(3874,1,'addition','Ǖ'),(3874,1,'command','+Fť'),(3875,1,'alternative','˘'),(3875,1,'command','+!/Ĕš'),(3876,1,'command','+\"¤A'),(3877,1,'02110','~'),(3877,1,'addition','̖'),(3877,1,'alternative','ۛ'),(3877,1,'command','˃͊Œ'),(3877,1,'simulate','؂'),(3878,1,'02110','w'),(3878,1,'command','ϛ'),(3878,1,'executor','Ǌ \"  \"\"̄33522220030%/'),(3879,1,'02110',''),(3879,1,'addition','ੀʘᐩխ•'),(3879,1,'blue','⧬Ɯ'),(3879,1,'command','፫৹ȐĠA̬Ş\Zఓ\"஡*\n7ӂǾ\\QďPƌƏ΄\n%Ջ੧ηoUC9it\n');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict31` ENABLE KEYS */;

--
-- Table structure for table `dict32`
--

DROP TABLE IF EXISTS `dict32`;
CREATE TABLE `dict32` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict32`
--


/*!40000 ALTER TABLE `dict32` DISABLE KEYS */;
LOCK TABLES `dict32` WRITE;
INSERT INTO `dict32` VALUES (3814,1,'double','í'),(3818,1,'double','í'),(3760,1,'110',';'),(3761,1,'double','í'),(3763,1,'double','i'),(3778,1,'pmon','Q'),(3782,1,'range','©'),(3784,1,'double','í'),(3785,1,'double','í'),(3791,1,'range','g'),(3798,1,'multi','ª'),(3799,1,'multi','ū'),(3811,1,'base','\"F'),(3745,1,'double','«'),(3744,1,'range','N´\rË'),(3827,1,'double','Ǌ'),(3829,1,'12694','L'),(3811,1,'double',''),(3871,1,'800','䴛'),(3856,1,'double','Æ'),(3871,1,'range','ࣵࣶN+ȳઢ*ৱ=࿢ᔃ'),(3833,1,'double','í'),(3831,1,'mf','*'),(3848,1,'double','~'),(3837,1,'statfile','­'),(3837,1,'orphaned','÷'),(3871,1,'multi','㇣'),(3871,1,'draws','ಎ'),(3871,1,'base','ݫࠅŤfеԌ⑳'),(3739,1,'double','ê'),(3737,1,'double','ê'),(3748,1,'double','í'),(3751,1,'range',''),(3755,1,'double','b'),(3757,1,'range','@		Ó'),(3759,1,'range','©'),(3877,1,'consistent','ưÞ'),(3872,1,'base','ə'),(3879,1,'desk','Ղ1t໾,'),(3879,1,'base','ᙏP#À'),(3879,1,'alternate','ⱑᏭ'),(3878,1,'range','Þ'),(3732,1,'double','ë'),(3729,1,'double',''),(3728,1,'double','ê'),(3727,1,'double','o'),(3721,1,'range','Ɉ'),(3721,1,'multi','ȵ!'),(3720,1,'animations',''),(3872,1,'800','֗E'),(3854,1,'double','đ'),(3879,1,'range','҃☱[υ-'),(3880,1,'desk','Յ1tύ.⺌ŧʽ'),(3880,1,'multi','ႇ'),(3880,1,'potential','ᆜ'),(3880,1,'range','̜҆⸌ë͢VJPǍZ\\LJBigƄ3@@A'),(3881,1,'apxs','㔜'),(3881,1,'base','ഊģ'),(3881,1,'consistent','ٯ'),(3881,1,'desk','͕1t'),(3881,1,'getservicesforhostgroup','ऊૄ'),(3881,1,'multi','චࠎĉჰħ'),(3881,1,'range','ʖ݉'),(3883,1,'range','ÿ-'),(3885,1,'base','Ŋ'),(3886,1,'range','ӕ'),(3887,1,'base','ఫ'),(3888,1,'multi','ϗ'),(3890,1,'consistent','ᑅ'),(3890,1,'orphaned','᫡'),(3891,1,'potential','ƾÊɉC');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict32` ENABLE KEYS */;

--
-- Table structure for table `dict33`
--

DROP TABLE IF EXISTS `dict33`;
CREATE TABLE `dict33` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict33`
--


/*!40000 ALTER TABLE `dict33` DISABLE KEYS */;
LOCK TABLES `dict33` WRITE;
INSERT INTO `dict33` VALUES (3873,1,'67','ĝ'),(3872,1,'67','ƥ'),(3879,1,'feel','ฯ´䣮'),(3880,1,'friendly','ᐎ'),(3880,1,'filter','ാ㧥'),(3880,1,'entering','Ṁ?H⟣'),(3880,1,'committing','⟡k'),(3880,1,'capital','ౢ'),(3879,1,'urgent','ՋL'),(3879,1,'shortname','塯)'),(3879,1,'reportserver','ᬣ'),(3871,1,'setenv','ⶓ'),(3879,1,'committing','➉ࢾ'),(3879,1,'84','ẞ'),(3871,1,'modules','ຄ'),(3871,1,'friendly','Ὺ'),(3879,1,'entering','፵Ꮠ'),(3871,1,'feel','㹂ऍ'),(3871,1,'filter','ⱶ'),(3871,1,'entering','ࡨ'),(3869,1,'modules',''),(3879,1,'granted','ጔkK'),(3880,1,'urgent','ՎL'),(3862,1,'modules','Ô'),(3721,1,'feel','ż'),(3727,1,'calibrating','³'),(3744,1,'privpasswd','xI'),(3823,1,'completing',''),(3841,1,'circuit',''),(3881,1,'adodb','ఱ'),(3881,1,'developing','㎾̣Ö'),(3881,1,'filter','࿤\r	\nխä´©u±Y)J=3ҙ'),(3881,1,'friendly','ଔ'),(3881,1,'helloworldapp','ⵠÚ5'),(3881,1,'modules','↺۰ಲ\\\n'),(3881,1,'operationstatus','ᴐՏ\r'),(3881,1,'shortname','ᏩKÃNúżᒶ@'),(3881,1,'urgent','͞L'),(3890,1,'committing','̣'),(3890,1,'smooth','ᐪ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict33` ENABLE KEYS */;

--
-- Table structure for table `dict34`
--

DROP TABLE IF EXISTS `dict34`;
CREATE TABLE `dict34` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict34`
--


/*!40000 ALTER TABLE `dict34` DISABLE KEYS */;
LOCK TABLES `dict34` WRITE;
INSERT INTO `dict34` VALUES (3871,1,'existent','ᓛ'),(3871,1,'ip','㺕'),(3878,1,'web','Ē̍¶Л'),(3871,1,'opinion','㏬'),(3877,1,'authorized','Ľћ2'),(3877,1,'account','ٜ'),(3875,1,'ip','ʈ'),(3877,1,'passwd','ƪÞ'),(3877,1,'ip','۽ 9'),(3879,1,'authorized','኱\ri\\<㻶'),(3720,1,'web','\Z'),(3721,1,'web','CɆ'),(3727,1,'ip','W'),(3728,1,'days','D'),(3728,1,'ip',']'),(3729,1,'days','E'),(3729,1,'ip','_'),(3730,1,'ip','i'),(3731,1,'ip','C'),(3732,1,'days','D'),(3732,1,'ip','^'),(3733,1,'ip','I'),(3737,1,'days','D'),(3737,1,'ip',']'),(3738,1,'ip','>'),(3739,1,'days','D'),(3739,1,'ip',']'),(3741,1,'ip','D'),(3742,1,'ip','A'),(3743,1,'ip','@'),(3744,1,'ip','HC'),(3744,1,'passwd','Å'),(3745,1,'days','4g'),(3745,1,'ip','K'),(3748,1,'days','G'),(3748,1,'ip','`'),(3752,1,'ip','$'),(3755,1,'ip','A'),(3756,1,'ip','K'),(3761,1,'days','G'),(3761,1,'ip','`'),(3763,1,'ip','<§'),(3766,1,'ip','N'),(3776,1,'passwd',')\''),(3778,1,'ip','\" '),(3779,1,'passwd',')'),(3780,1,'ip','I'),(3784,1,'days','G'),(3784,1,'ip','`'),(3785,1,'days','G'),(3785,1,'ip','`'),(3791,1,'ip','|'),(3793,1,'ip','G'),(3795,1,'ip',':'),(3795,1,'msslqserver',''),(3804,1,'ip','C;'),(3811,1,'ip','J'),(3813,1,'account',''),(3814,1,'days','G'),(3814,1,'ip','`'),(3815,1,'ip','/'),(3818,1,'days','G'),(3818,1,'ip','`'),(3823,1,'ip',''),(3827,1,'days','ÒĊQ'),(3827,1,'ip','?e'),(3827,1,'web','ɂ'),(3831,1,'ip','#%'),(3833,1,'days','G'),(3833,1,'ip','`'),(3834,1,'ip','U'),(3837,1,'account','@J'),(3837,1,'ip','z#'),(3837,1,'passwd',''),(3837,1,'smtptimeout','¤'),(3840,1,'ip','[Ȥ'),(3848,1,'ip','M'),(3849,1,'ip','0\r'),(3854,1,'days','J·'),(3854,1,'ip','c'),(3856,1,'1080933700','ŧ'),(3856,1,'authorized','¢i'),(3856,1,'days','ű'),(3856,1,'ip','J'),(3857,1,'ip','2'),(3862,1,'ip','.'),(3865,1,'ip',';'),(3866,1,'authorized','M'),(3869,1,'web','§,'),(3870,1,'web',' '),(3871,1,'account','ЎВᆎ'),(3871,1,'convenient','ρᰖ'),(3871,1,'days','ಭវ@(iᰎ\n2\nđŞC'),(3871,1,'eet','▙>'),(3879,1,'account','࡙¥}'),(3871,1,'linec','䤽'),(3877,1,'convenient','ե'),(3873,1,'ip','Ʈ\nĊ'),(3872,1,'ip','ࠒ'),(3871,1,'web','Ꮲhᠠ#ɢÉɷ޸'),(3879,1,'days','ᦃସ਌႕'),(3879,1,'deleted','㧢'),(3879,1,'ip','ዺkKࠩள$ɬ,%8Ŀ↗ČЍ൥'),(3879,1,'web','ðȮjż\Zɰ=ң͎̌ǕWпÑӖ÷¨ǚ*=/˹Ģප'),(3880,1,'account','౹'),(3880,1,'authorized','Ꮧ'),(3880,1,'categorized','ᯁ'),(3880,1,'convenient','෢'),(3880,1,'days','ᆺ⏯ 8'),(3880,1,'ip','ɲሰϻ໪'),(3880,1,'seeking','ླྀ'),(3880,1,'web','ʈjȕ\Zηî¥ŮŮʎϪ_␔੪)'),(3881,1,'ip','ࢨа'),(3881,1,'isacceptpassivechecks','ࡨ'),(3881,1,'web','Èɏ\ZŻ:vu¸Ķ͇LI&࿠-%१,+2Ñ\Z!ā$=1rˍş֘ǈª'),(3882,1,'ip','Þ'),(3883,1,'days','ò'),(3884,1,'deleted','Ꭾ2'),(3886,1,'ip','$'),(3887,1,'ip','²$'),(3888,1,'account','̣'),(3888,1,'web','̢'),(3890,1,'authorized','ӚX$%\Z\' '),(3890,1,'deleted','࣒Ԟ'),(3890,1,'web','ĥŏŅHJ7ՕostĿ࿗');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict34` ENABLE KEYS */;

--
-- Table structure for table `dict35`
--

DROP TABLE IF EXISTS `dict35`;
CREATE TABLE `dict35` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict35`
--


/*!40000 ALTER TABLE `dict35` DISABLE KEYS */;
LOCK TABLES `dict35` WRITE;
INSERT INTO `dict35` VALUES (3892,1,'configure','E'),(3762,1,'pair','U'),(3881,1,'localhost','ἈҶ'),(3892,1,'source','	'),(3871,1,'configure','ĵཚ9'),(3732,7,'source','Ġ'),(3731,7,'source',''),(3879,1,'caution','⡸ᐠ'),(3834,1,'pair','h'),(3840,1,'calls','ɞ'),(3840,1,'configure','ã'),(3841,1,'source',','),(3856,1,'localhost','Ŕ'),(3856,1,'pair','ü'),(3864,1,'pair','H'),(3798,1,'pair','j/'),(3881,1,'collagequery','Һϻ#\rĐ~'),(3884,1,'configure','E·֓ƽ࠰'),(3884,1,'caution','ͻ਋'),(3883,1,'source',''),(3882,1,'source',''),(3881,1,'rows','᜸jwjE	'),(3881,1,'source','	}^]5\Z\Z\r.\n	Ŭ2øƨ̈́ł9\"H&{ၑŗ˘XKÑܹÀ@ࡏ#w'),(3778,7,'source','ü'),(3777,7,'source','.'),(3776,7,'source','ī'),(3775,7,'source','-'),(3726,7,'source',''),(3723,7,'source','H'),(3872,1,'localhost','ؠĘ'),(3872,1,'packagedir','Ǒ	'),(3872,1,'shutting','͠'),(3763,7,'source','Ľ'),(3891,1,'configure','ò>'),(3889,1,'configure','%'),(3887,1,'source','	'),(3872,1,'configure','୚>'),(3751,1,'requirements','Ɵ'),(3724,7,'source','k'),(3881,1,'configure','ɜ⃘֔ʡउÂ'),(3754,1,'pair','ƌ'),(3887,1,'sequence','ø'),(3720,7,'source','ƿ'),(3722,7,'source','ŧ'),(3880,1,'source','	}4^@\"F\"\"@28\'4ż#2=XF/{%\Z\ZT	9	\r&\"(<\":\rZ@M1\\4\'¹˴Ơ=Ħ\neO\r\r¯łӊ!.،;࿐q¤Ǯ9±ତ	&	!;	\r	:B^Ā\"'),(3761,7,'source','Ģ'),(3885,1,'configure','Ŭ'),(3888,1,'caution','ض'),(3880,1,'requirements','Ъ'),(3879,1,'configure','ϻ̍Dॾ|ॐƜfȷĐƱ̺d৉>͞ʝūێ۶e'),(3750,7,'source',':'),(3748,7,'source','Ģ'),(3749,7,'source','/'),(3747,7,'source','_'),(3746,7,'source','7'),(3745,7,'source','Ā'),(3738,7,'source',''),(3739,7,'source','ğ'),(3740,7,'source','ƌ'),(3741,7,'source','¾'),(3742,7,'source',''),(3743,7,'source',''),(3744,7,'source','ɠ'),(3871,1,'source','ϱβ-Lʝˁ<ޡ_ุͦu!\n\r\n؋޳Њ఺'),(3871,1,'rrdres','⌘	'),(3871,1,'rows','۲ɶᶜ&1ǅxH'),(3871,1,'netware','ģ'),(3871,1,'increment','䥟'),(3871,1,'evaluating','ᛔ'),(3751,1,'source','ƹ'),(3769,7,'source','*'),(3768,7,'source','-'),(3771,7,'source','*'),(3736,7,'source','Á'),(3735,7,'source','{'),(3734,7,'source','ā'),(3733,7,'source',''),(3754,7,'source','̓'),(3753,7,'source',''),(3752,7,'source','ē'),(3751,7,'source','ʯ'),(3773,7,'source','.'),(3772,7,'source','*'),(3766,7,'source','õ'),(3765,7,'source','â'),(3886,1,'caution','Ťɿ'),(3886,1,'attach',''),(3885,1,'source','	'),(3728,7,'source','ğ'),(3759,7,'source','Ă'),(3767,7,'source',','),(3730,7,'source',''),(3876,1,'source','\r'),(3877,1,'configure','ɦ'),(3877,1,'pair','ٿ '),(3877,1,'requirements','Ã'),(3830,1,'collagequery',''),(3830,1,'requirements','J'),(3886,1,'sequence','Ð'),(3880,1,'configure','ύ๺Ⓟ්'),(3879,1,'requirements','ۊझ'),(3879,1,'source','	}l°N\'2%@2I\r|ż#-Be.¸If<q!ěÐ>ũ\n?A%!$9=&D,1HD;NA]@@7kKd*9ͥŴŒƓČ	3ü(ȔЙ¿׽Ģ٭÷Eলǻ\rď\\	Ḥ̂ʷâƹϱظj̐Só͗ʪ	%ȫ'),(3762,7,'source','ã'),(3755,7,'source','Ã'),(3887,1,'caution','ƌ'),(3881,1,'requirements','୒²Ặ>੿§ƾ'),(3758,7,'source','F'),(3874,1,'source','\r'),(3827,1,'pair','ƀ'),(3757,7,'source','ƫ'),(3890,1,'shutting','໣'),(3891,1,'source','	'),(3727,7,'source','Ņ'),(3725,7,'source','e'),(3879,2,'source',''),(3881,2,'source',''),(3720,1,'source','ê'),(3890,1,'source','\rࡓ'),(3877,1,'source','̍ȧ'),(3721,7,'source','ʠ'),(3888,1,'configure','Ɂ\r\r'),(3737,7,'source','ğ'),(3875,1,'source','\r'),(3721,1,'source','õ$'),(3879,1,'gwreportserver','ᯃ'),(3879,1,'localhost','൘IӼckK⺿༉ł൭'),(3879,1,'pair','䠍'),(3878,1,'source','ŉ'),(3760,7,'source','G'),(3887,1,'attach','¶'),(3880,1,'localhost','ᴻ\r'),(3764,7,'source','B'),(3880,2,'source',''),(3888,1,'source',''),(3890,1,'attack','͞'),(3880,1,'netware','ު'),(3729,7,'source','Ø'),(3774,7,'source','/'),(3872,1,'calls','ӑ'),(3884,1,'source','	'),(3780,1,'netware','Ŵ'),(3756,7,'source','ù'),(3886,1,'source','	'),(3887,1,'configure','+є'),(3869,1,'source','('),(3889,1,'source','	'),(3872,1,'source','ž'),(3770,7,'source','*'),(3779,7,'source','8'),(3780,7,'source','Ƴ'),(3781,7,'source',''),(3782,7,'source','ƚ'),(3783,7,'source','Û'),(3784,7,'source','Ģ'),(3785,7,'source','Ģ'),(3786,7,'source','C'),(3787,7,'source','®'),(3788,7,'source','à'),(3789,7,'source',''),(3790,7,'source','W'),(3791,7,'source','´'),(3792,7,'source','h'),(3793,7,'source','¬'),(3794,7,'source','+'),(3795,7,'source',''),(3796,7,'source','-'),(3797,7,'source','p'),(3798,7,'source','ô'),(3799,7,'source','Ɠ'),(3800,7,'source','I'),(3801,7,'source',':'),(3802,7,'source','@'),(3803,7,'source','â'),(3804,7,'source','¹'),(3805,7,'source','Ñ'),(3806,7,'source','v'),(3807,7,'source','E'),(3808,7,'source','~'),(3809,7,'source','h'),(3810,7,'source','2'),(3811,7,'source','Ê'),(3812,7,'source','7'),(3813,7,'source',''),(3814,7,'source','Ģ'),(3815,7,'source','p'),(3816,7,'source','V'),(3817,7,'source','3'),(3818,7,'source','Ģ'),(3819,7,'source','Ĥ'),(3820,7,'source','ő'),(3821,7,'source','?'),(3822,7,'source','ă'),(3823,7,'source','Ð'),(3824,7,'source','ś'),(3825,7,'source','Ň'),(3826,7,'source','*'),(3827,7,'source','˙'),(3828,7,'source','¡'),(3829,7,'source','{'),(3830,7,'source',''),(3831,7,'source','Õ'),(3832,7,'source','R'),(3833,7,'source','Ģ'),(3834,7,'source','½'),(3835,7,'source',''),(3836,7,'source','g'),(3837,7,'source','ĩ'),(3838,7,'source','F'),(3839,7,'source','8'),(3840,7,'source','̜'),(3841,7,'source','±'),(3842,7,'source','A'),(3843,7,'source','m'),(3844,7,'source','Ù'),(3845,7,'source','}'),(3846,7,'source','ļ'),(3847,7,'source','X'),(3848,7,'source','³'),(3849,7,'source',''),(3850,7,'source','M'),(3851,7,'source',''),(3852,7,'source','.'),(3853,7,'source','V'),(3854,7,'source','ņ'),(3855,7,'source','='),(3856,7,'source','Ɠ'),(3857,7,'source','b'),(3858,7,'source','7'),(3859,7,'source','E'),(3860,7,'source','J'),(3861,7,'source','5'),(3862,7,'source','ĝ'),(3863,7,'source','Ŀ'),(3864,7,'source',''),(3865,7,'source','¤'),(3866,7,'source','º'),(3867,7,'source','3'),(3868,7,'source','ę'),(3869,7,'source','Ĝ'),(3870,7,'source','Ħ'),(3871,7,'source','僙'),(3872,7,'source','ೲ'),(3873,7,'source','զ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict35` ENABLE KEYS */;

--
-- Table structure for table `dict36`
--

DROP TABLE IF EXISTS `dict36`;
CREATE TABLE `dict36` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict36`
--


/*!40000 ALTER TABLE `dict36` DISABLE KEYS */;
LOCK TABLES `dict36` WRITE;
INSERT INTO `dict36` VALUES (3881,1,'collagehostquery','࣌z˔ࢴ'),(3735,1,'percent','C'),(3736,1,'percent',' *'),(3881,1,'lastnotificationtime','ࡑ '),(3879,1,'guide','ə*ŌI\rȆু߷ᘣ᎙्྄֮'),(3879,1,'entire','⃴ดᩚᄜ]*ે'),(3879,1,'careful','৯'),(3880,1,'logs','㡭&'),(3888,1,'pre','ɢ­֮'),(3734,1,'percent','Ø'),(3720,1,'timepicker','Ƈ'),(3879,1,'agreement','Ѻ'),(3879,1,'administrators','϶̟ᅊ৫Ѱ'),(3881,2,'guide',''),(3880,2,'guide',''),(3885,1,'careful','࢏'),(3874,1,'percent','ù:GįK'),(3874,1,'guide','Ǌ'),(3881,1,'agreement','ʍ'),(3890,1,'pre','ᴣŤ'),(3891,1,'pre','Ϲ'),(3873,1,'percent','ĵ'),(3879,1,'jsp','᭞'),(3879,1,'logs','᳞㕆෹Ł'),(3879,1,'percent','ᷖM⣞3'),(3879,1,'pre','ۋ_̙ᛥĘ˜ɩ࣮ГŠҺրÊìᒃe'),(3879,1,'repository','᠇㺁ƞʿ'),(3880,1,'administrators','χടŋₖɁྻ'),(3880,1,'agreement','ѽ'),(3880,1,'entire','ޗęୃϓ'),(3880,1,'guide','Ɓaŗ,&\'ɫ͟n31˗Ⴄᝃ˘ିÎ			.'),(3881,1,'pre','⇚&'),(3881,1,'logs','⇨Ѓ'),(3881,1,'entire','ƥბ7'),(3881,1,'concurrently','↍'),(3881,1,'repository','ⓒ¦'),(3805,6,'logs','×'),(3879,6,'guide','朶'),(3880,6,'guide','䡄'),(3881,6,'guide','㧄'),(3865,1,'percent','2'),(3871,1,'300s','㗯'),(3871,1,'careful','䗱'),(3871,1,'entire','੨?¨'),(3871,1,'ethernet','䐄'),(3871,1,'pre','ᘋ⧒'),(3851,1,'percent',''),(3801,1,'maxwanstate','\r'),(3805,1,'logs',''),(3816,1,'percent','3\r'),(3817,1,'percent',''),(3822,1,'factors','¯'),(3827,1,'linespan','ş'),(3843,1,'percent','5'),(3844,1,'percent','o'),(3845,1,'percent','F'),(3846,1,'percent','f'),(3800,1,'percent','0'),(3881,1,'recommendations','ᶧΜ'),(3878,1,'pre','ë'),(3877,1,'cut','֯'),(3780,1,'percent',',1'),(3776,1,'percent','¦'),(3766,1,'percent','t'),(3756,1,'careful','Ã'),(3754,1,'logs',''),(3884,1,'careful','ࠕ'),(3884,1,'pre','ࣝ'),(3740,1,'hrswrunname','¨'),(3743,1,'percent','a\r'),(3876,1,'guide','ü'),(3875,1,'guide','ƚ'),(3881,1,'guide','ōr\"-ȕᦆ'),(3879,2,'guide',''),(3881,1,'administrators','ɖ'),(3880,1,'pre','Ыࢀ֏'),(3880,1,'objectview','ᡀĉಣ0}ఞø'),(3880,1,'percent','䁳'),(3801,6,'maxwanstate','@'),(3883,1,'entire','ĳ'),(3880,1,'navigating','஫Ȥ㙺ɏ|'),(3744,1,'commas','ȫ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict36` ENABLE KEYS */;

--
-- Table structure for table `dict37`
--

DROP TABLE IF EXISTS `dict37`;
CREATE TABLE `dict37` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict37`
--


/*!40000 ALTER TABLE `dict37` DISABLE KEYS */;
LOCK TABLES `dict37` WRITE;
INSERT INTO `dict37` VALUES (3879,1,'basis','ᰑ'),(3878,1,'30','ԃ'),(3879,1,'30','ط堘'),(3871,1,'inner','਎'),(3871,1,'reason','׃'),(3871,1,'ready','װ✀ᩦ'),(3879,1,'ready','᠚⃡ᗠƶ'),(3782,1,'valid',''),(3784,1,'valid','ã'),(3785,1,'valid','ã'),(3790,1,'replication',''),(3780,1,'valid','X'),(3766,1,'valid',']'),(3761,1,'valid','ã'),(3757,1,'valid','|'),(3754,1,'30','ƥ'),(3748,1,'valid','ã'),(3877,1,'valid','΢'),(3877,1,'ready','ͫ'),(3877,1,'red','®\r)'),(3814,1,'valid','ã'),(3818,1,'valid','ã'),(3827,1,'valid','ØŽR'),(3833,1,'valid','ã'),(3840,1,'basis','ɰ'),(3854,1,'valid','ć'),(3865,1,'30','l'),(3866,1,'30','¬'),(3871,1,'30','ဆቖᖔȳۛİ-YǊ'),(3871,1,'aggregate','ḇ'),(3871,1,'epoch','ာ<ᅵ7ĸۜ='),(3877,1,'overwriting','ק'),(3877,1,'30','ؚ'),(3872,1,'valid','ڬ̎I'),(3871,1,'valid','ᕣݏથؒጤ੄'),(3871,1,'red','὜ᨐC\"\r	ਠ'),(3881,1,'applicationseverity','≻'),(3879,1,'drilling','尖'),(3728,1,'valid','à'),(3732,1,'valid','á'),(3734,1,'30','ê'),(3737,1,'valid','à'),(3739,1,'valid','à'),(3745,1,'valid','¡'),(3871,1,'exceeding','੾'),(3880,1,'basis','ⷓҊ'),(3880,1,'30','غ'),(3879,1,'valid','ൂI᜶৷ဓ;\\ƴ'),(3879,1,'sorted','䯄'),(3881,1,'30','ъ'),(3880,1,'valid','ఞ֢'),(3880,1,'red','᥽1ऄ'),(3880,1,'reason','ഢ'),(3880,1,'drilling','ᠸ҈ĖƴȕÚďǧ'),(3881,1,'basis','⎛'),(3881,1,'red','⊶ሾ'),(3881,1,'sorted','ᨘ6'),(3881,1,'valid','ඕฅ᪮)Ȇ{'),(3883,1,'valid','ã'),(3884,1,'valid','ҝ਋̂'),(3885,1,'ready','Ǘ'),(3885,1,'valid','ڗ['),(3887,1,'valid','ࣘZˬҒ'),(3888,1,'sorted','ҮX'),(3889,1,'valid','Ŷh\rJ'),(3890,1,'aggregate','ࣣ'),(3890,1,'basis','ዠ'),(3890,1,'reason','ृ॓'),(3890,1,'valid','ڞ7'),(3891,1,'valid','ǝÊʌK'),(3892,1,'valid','ŀ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict37` ENABLE KEYS */;

--
-- Table structure for table `dict38`
--

DROP TABLE IF EXISTS `dict38`;
CREATE TABLE `dict38` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict38`
--


/*!40000 ALTER TABLE `dict38` DISABLE KEYS */;
LOCK TABLES `dict38` WRITE;
INSERT INTO `dict38` VALUES (3879,1,'spreadsheet','ₔ࡫'),(3871,1,'perlbindings','ŏ'),(3871,1,'people','߲u⟌1ŐΉ࡝D#ͣ໬!'),(3871,1,'full','܋ᴎ᭢~Ő'),(3871,1,'outoctets','Ⱟᝀ+'),(3880,1,'proprietary','ȭ'),(3880,1,'people','㟦'),(3880,1,'full','҂'),(3871,1,'aa','⬚	'),(3871,1,'cellar','֕'),(3871,1,'components','㦷9'),(3871,1,'differences','䥥'),(3754,1,'full','Ž'),(3765,1,'full','-'),(3787,1,'full',''),(3797,1,'guest','Q'),(3844,1,'guest','\\'),(3863,1,'full','ù'),(3863,1,'opts','4-'),(3868,1,'components','z'),(3870,1,'zend','r¡'),(3871,1,'18446744073709551000','䴙'),(3879,1,'people','Მp'),(3879,1,'full','ѿᇳ९'),(3879,1,'components','ǚകฬ⒄\n᜴'),(3879,1,'closest','㠇'),(3878,1,'components','ƈ'),(3873,1,'proprietary','<'),(3872,1,'dead','ѩ	'),(3871,1,'wise','ᓀ'),(3880,1,'components','ߜ+֊Ҳ'),(3879,1,'proprietary','ˎ'),(3881,1,'components','مgᕘǝ٭ĺӧ#;'),(3881,1,'full','ʒՊ⅐ѓ'),(3881,1,'gethoststatusforhost','ॢ௮'),(3881,1,'getmonitorservers','ক'),(3881,1,'people','⠅T'),(3884,1,'denote','Ȉ਋'),(3890,1,'full','ɏӐ'),(3890,1,'people','Θ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict38` ENABLE KEYS */;

--
-- Table structure for table `dict39`
--

DROP TABLE IF EXISTS `dict39`;
CREATE TABLE `dict39` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict39`
--


/*!40000 ALTER TABLE `dict39` DISABLE KEYS */;
LOCK TABLES `dict39` WRITE;
INSERT INTO `dict39` VALUES (3879,1,'spaces','⟩〡ǟ'),(3882,1,'number','ä'),(3757,1,'number','.Q'),(3756,1,'number','Q*'),(3755,1,'number','G'),(3754,1,'number',',p$$²'),(3725,1,'number',''),(3727,1,'number',']'),(3728,1,'number','c_'),(3729,1,'number','e'),(3730,1,'number','H'),(3731,1,'number','I'),(3732,1,'number','d_'),(3737,1,'number','c_'),(3738,1,'number','D'),(3739,1,'number','c_'),(3740,1,'number','DX'),(3741,1,'number','v'),(3744,1,'number','d-'),(3744,1,'spaces','ǀ'),(3722,1,'number','x'),(3793,1,'number','M'),(3871,1,'blind','ᮬ'),(3745,1,'number','QH'),(3871,1,'split','ቘ③'),(3872,1,'number','Â҇10-āѴ'),(3876,1,'number','\"'),(3879,1,'branding','๜'),(3879,1,'intel','䵟'),(3822,1,'number','J'),(3827,1,'number','¸qĘ'),(3831,1,'number','^'),(3833,1,'number','f_'),(3834,1,'number','C6'),(3848,1,'number','S'),(3851,1,'number','))'),(3854,1,'number','qx'),(3856,1,'number','P'),(3863,1,'dist',''),(3864,1,'number','R'),(3818,1,'number','f_'),(3813,1,'number','H'),(3814,1,'number','f_'),(3815,1,'number','8'),(3880,1,'split','⊘'),(3880,1,'number','տ£੆ɎࡖS# )\Z\"ƍ»ŤP0G&#˧ቹšúgVVJPŵXZ\\LJBB\'@\'ŴC@@A'),(3881,1,'split','Ӝ'),(3881,1,'spaces','⦀Є\n'),(3880,1,'alarming','㢷'),(3785,1,'number','f_'),(3787,1,'spaces','¢'),(3789,1,'number','2'),(3753,1,'number','L'),(3875,1,'number','Ę-Ǜ'),(3871,1,'unusual','૸'),(3881,1,'firstinsertdate','ࢉľඁ©ӏ'),(3872,1,'dist','Ř'),(3881,1,'number','Ώ£ä༺Ƶ&Ç¯¢<	ʞ'),(3784,1,'number','f_'),(3751,1,'number','Ǵ'),(3791,1,'number',''),(3879,1,'number','ռ£ᄧੳՋࣖȽˡȴ࣑ِÊ4	ɘ-ţᘩ'),(3874,1,'number','º])=³a\r¢'),(3866,1,'number',';'),(3795,1,'number','@'),(3803,1,'number','\Z\"'),(3805,1,'number',''),(3811,1,'number','P'),(3748,1,'number','f_'),(3871,1,'spaces','⳿'),(3871,1,'number','ƕÅȻ͛×«ȵ-ଁɆ\ZÅذʹ@Ʌå/3պš	̉jǊñȺſȰˀİ¹îlI=̭֤'),(3871,1,'dnan','û'),(3879,1,'split','ⓟ᫦'),(3879,1,'serviceperfdata','䐳'),(3761,1,'number','f_'),(3762,1,'number','_'),(3763,1,'number','B'),(3764,1,'number',''),(3766,1,'number','T)	'),(3776,1,'number','g\n\n,\n'),(3779,1,'spaces',''),(3780,1,'number','O\na	'),(3782,1,'number','H'),(3783,1,'number','F'),(3884,1,'number','Ɂ2.MĄ$ࠕ2.MĄ$'),(3885,1,'number','ӧ\Z\"'),(3887,1,'number','ܨ\Z\"'),(3889,1,'number','¼WUƥ'),(3890,1,'number','ˇત»ĚȪ £·T&࢈'),(3891,1,'number','Ę');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict39` ENABLE KEYS */;

--
-- Table structure for table `dict3A`
--

DROP TABLE IF EXISTS `dict3A`;
CREATE TABLE `dict3A` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict3A`
--


/*!40000 ALTER TABLE `dict3A` DISABLE KEYS */;
LOCK TABLES `dict3A` WRITE;
INSERT INTO `dict3A` VALUES (3871,1,'oldest','⟐'),(3871,1,'individual','Ȃň'),(3827,1,'copyright',''),(3828,1,'copyright',''),(3831,1,'copyright',''),(3817,1,'copyright',''),(3818,1,'copyright',''),(3871,1,'feeds','൰'),(3871,1,'gauge','ߨίĩᏸ!ź◲\r}º'),(3826,1,'copyright',''),(3836,1,'copyright',''),(3816,1,'copyright',''),(3815,1,'copyright',''),(3810,1,'copyright',''),(3787,1,'copyright',''),(3786,1,'copyright',''),(3785,1,'copyright',''),(3863,1,'copyright',''),(3862,1,'copyright','B'),(3861,1,'copyright',''),(3860,1,'copyright',''),(3859,1,'copyright',''),(3858,1,'copyright',''),(3857,1,'copyright',''),(3855,1,'copyright',''),(3856,1,'copyright',''),(3874,1,'copyright','	'),(3873,1,'disk','ˮ\n\r\"'),(3872,1,'individual','ఄ'),(3871,1,'translated','㝳ਕ˾'),(3871,1,'original','␱ϕ'),(3759,1,'copyright',''),(3758,1,'copyright',''),(3757,1,'copyright',''),(3756,1,'copyright',''),(3725,1,'copyright',''),(3726,1,'copyright',''),(3727,1,'copyright',''),(3728,1,'copyright',''),(3729,1,'copyright',''),(3730,1,'copyright',''),(3825,1,'copyright',''),(3824,1,'copyright',''),(3823,1,'copyright',''),(3822,1,'copyright',''),(3821,1,'copyright',''),(3832,1,'copyright',''),(3833,1,'copyright',''),(3847,1,'copyright',''),(3846,1,'copyright',''),(3766,1,'copyright',''),(3766,1,'disk','v'),(3767,1,'copyright',''),(3768,1,'copyright',''),(3769,1,'copyright',''),(3770,1,'copyright',''),(3771,1,'copyright',''),(3772,1,'copyright',''),(3773,1,'copyright',''),(3774,1,'copyright',''),(3775,1,'copyright',''),(3720,1,'copyright',''),(3841,1,'copyright',''),(3782,1,'disk','à'),(3765,1,'copyright',''),(3845,1,'disk',''),(3837,1,'copyright',''),(3854,1,'copyright',''),(3864,1,'copyright',''),(3876,1,'copyright','	'),(3875,1,'copyright','	'),(3871,1,'disk','х஽Ѕ'),(3868,1,'copyright','¢'),(3752,1,'240','ā'),(3751,1,'copyright',''),(3839,1,'copyright',''),(3811,1,'copyright',''),(3776,1,'copyright',''),(3777,1,'copyright',''),(3843,1,'disk',''),(3742,1,'jetdirect','U'),(3820,1,'copyright',''),(3807,1,'copyright',''),(3808,1,'copyright',''),(3809,1,'copyright',''),(3752,1,'copyright',''),(3753,1,'copyright',''),(3754,1,'copyright',''),(3755,1,'copyright',''),(3751,1,'240','ɓ'),(3835,1,'copyright',''),(3834,1,'copyright',''),(3764,1,'copyright',''),(3724,1,'copyright',''),(3783,1,'copyright',''),(3846,1,'disk','	-1'),(3844,1,'copyright',''),(3819,1,'copyright',''),(3731,1,'copyright',''),(3732,1,'copyright',''),(3733,1,'copyright',''),(3734,1,'copyright',''),(3735,1,'copyright',''),(3736,1,'copyright',''),(3737,1,'copyright',''),(3738,1,'copyright',''),(3739,1,'copyright',''),(3740,1,'copyright',''),(3741,1,'copyright',''),(3742,1,'copyright',''),(3722,1,'copyright',''),(3723,1,'copyright',''),(3788,1,'copyright',''),(3789,1,'copyright',''),(3790,1,'copyright',''),(3791,1,'copyright',''),(3792,1,'copyright',''),(3793,1,'copyright',''),(3794,1,'copyright',''),(3784,1,'copyright',''),(3867,1,'copyright',''),(3829,1,'copyright',''),(3829,1,'jetdirect',' '),(3813,1,'copyright',''),(3812,1,'copyright',''),(3866,1,'copyright',''),(3871,1,'collects','≾'),(3870,1,'copyright',''),(3874,1,'disk','·³ĸ'),(3845,1,'copyright',''),(3844,1,'disk','\n'),(3869,1,'copyright',''),(3838,1,'copyright',''),(3840,1,'copyright',''),(3840,1,'proper','ʤ'),(3814,1,'copyright',''),(3853,1,'copyright',''),(3877,1,'checkcommands','Ϋ'),(3760,1,'copyright',''),(3761,1,'copyright',''),(3762,1,'copyright',''),(3763,1,'copyright',''),(3743,1,'copyright',''),(3865,1,'copyright',''),(3842,1,'copyright',''),(3830,1,'copyright',''),(3795,1,'copyright',''),(3796,1,'copyright',''),(3797,1,'copyright',''),(3798,1,'copyright',''),(3799,1,'copyright',''),(3800,1,'copyright',''),(3801,1,'copyright',''),(3802,1,'copyright',''),(3803,1,'copyright',''),(3804,1,'copyright',''),(3804,1,'individual','L'),(3805,1,'copyright',''),(3806,1,'copyright',''),(3778,1,'copyright',''),(3743,1,'disk','H\r'),(3744,1,'copyright',':'),(3841,1,'240','r'),(3843,1,'copyright',''),(3848,1,'copyright',''),(3849,1,'copyright',''),(3850,1,'copyright',''),(3851,1,'copyright',''),(3852,1,'copyright',''),(3779,1,'copyright',''),(3780,1,'copyright',''),(3780,1,'original','§'),(3781,1,'copyright',''),(3782,1,'collects',''),(3782,1,'copyright',''),(3745,1,'copyright',''),(3746,1,'copyright',''),(3747,1,'copyright',''),(3747,1,'disk','\"'),(3748,1,'copyright',''),(3749,1,'copyright',''),(3750,1,'copyright',''),(3877,1,'copyright',''),(3877,1,'ordinary','ŕ'),(3878,1,'copyright',''),(3879,1,'copyright','崖'),(3879,1,'disk','ᵇ&Hϙྕ\n'),(3879,1,'gauge','䙝ƒ,ƹ'),(3879,1,'individual','ⷝ'),(3879,1,'wrappit','öڢ9ωआ4c?\r ĮËI£'),(3880,1,'copyright',''),(3880,1,'demoexchange','⏀'),(3880,1,'disk','㓙'),(3880,1,'glance','ݕႤɬǡ'),(3880,1,'individual','უᑷഽø '),(3880,1,'original','㗌'),(3880,1,'scheduler','໑'),(3880,1,'wrappit','ࡌūaʶ'),(3881,1,'copyright','⍪'),(3881,1,'feeds','ℼ'),(3881,1,'getservicesbyfilter','ᚕѬ'),(3881,1,'mycompany','㙔ȯ'),(3882,1,'copyright','\r'),(3883,1,'copyright',''),(3884,1,'copyright',''),(3884,1,'disk','¨֭১'),(3884,1,'individual','äࡋ'),(3884,1,'original','୅'),(3885,1,'copyright',''),(3885,1,'disk','ݡ'),(3885,1,'individual','ঃ'),(3886,1,'copyright',''),(3886,1,'disk','Н%*D'),(3887,1,'copyright',''),(3887,1,'individual','ྻ'),(3888,1,'cgg','ɉ'),(3888,1,'copyright',''),(3888,1,'officially','Ȏ'),(3889,1,'copyright',''),(3890,1,'copyright','	'),(3891,1,'copyright',''),(3892,1,'copyright',''),(3743,6,'disk',''),(3843,6,'disk','s'),(3844,6,'disk','ß'),(3845,6,'disk',''),(3846,6,'disk','ł');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict3A` ENABLE KEYS */;

--
-- Table structure for table `dict3B`
--

DROP TABLE IF EXISTS `dict3B`;
CREATE TABLE `dict3B` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict3B`
--


/*!40000 ALTER TABLE `dict3B` DISABLE KEYS */;
LOCK TABLES `dict3B` WRITE;
INSERT INTO `dict3B` VALUES (3881,1,'kinds','ᬛ'),(3871,1,'978303900','䤗'),(3840,1,'misconfigured','ˣ'),(3879,1,'suffix','ᚲך'),(3871,1,'chances','㞂ᔶ'),(3871,1,'value3','ể'),(3871,1,'suppose','䐶ࡎǾ'),(3871,1,'suffix','⏄'),(3871,1,'subdata','≎ck'),(3871,1,'kinds','Ǆ'),(3880,1,'activity','㠭	'),(3880,1,'executive','࠹'),(3827,1,'10m','ň'),(3820,1,'ifname','Ìe'),(3766,1,'eric','Î'),(3805,1,'suffix','°'),(3881,1,'logrotation','⏫'),(3881,1,'scheduleddowntimedepth','࡙ '),(3884,1,'manangement','ݔ'),(3886,1,'suppose','Ѡ'),(3888,1,'suffix','ڰ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict3B` ENABLE KEYS */;

--
-- Table structure for table `dict3C`
--

DROP TABLE IF EXISTS `dict3C`;
CREATE TABLE `dict3C` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict3C`
--


/*!40000 ALTER TABLE `dict3C` DISABLE KEYS */;
LOCK TABLES `dict3C` WRITE;
INSERT INTO `dict3C` VALUES (3871,1,'easier','⸾'),(3854,1,'certificate','ü'),(3833,1,'certificate','Ø'),(3761,1,'certificate','Ø'),(3720,1,'easier',''),(3871,1,'interpolate','ϫ'),(3871,1,'key','㴔'),(3871,1,'falsely','䱺'),(3780,1,'wisc','Ƈ'),(3780,1,'load5','`'),(3827,1,'certificate','6žE\n'),(3820,1,'key','v'),(3819,1,'key','Ï'),(3818,1,'certificate','Ø'),(3814,1,'certificate','Ø'),(3785,1,'certificate','Ø'),(3784,1,'certificate','Ø'),(3871,1,'8','ᶖ_\r׻ǜԏਝ႐ї'),(3856,1,'key','£b'),(3863,1,'8','\''),(3870,1,'key','k'),(3745,1,'certificate',''),(3744,1,'8','ǅ'),(3739,1,'certificate','Õ'),(3877,1,'key','İ>ȄÔ(6äÆ '),(3874,1,'8','Æǐ'),(3877,1,'8','Ȕû'),(3873,1,'typical','ù'),(3872,1,'typical','ࠋ'),(3872,1,'8','̲6ç'),(3871,1,'validation','⢽'),(3879,1,'managing','Þ՝j߂࿗Þᑨգ]I}<X#ͫǳਡ'),(3879,1,'key','ᥥڇ'),(3879,1,'easier','΋め'),(3879,1,'certificate','ᥢ+'),(3879,1,'8','żᷨṊ'),(3737,1,'certificate','Õ'),(3732,1,'certificate','Ö'),(3728,1,'certificate','Õ'),(3721,1,'structured','ar'),(3721,1,'easier','ɥ'),(3720,1,'validation','Ɖ'),(3871,1,'switches','Ȟ'),(3871,1,'typical','ૻ'),(3748,1,'certificate','Ø'),(3766,1,'load5','f'),(3871,1,'plots','౟1'),(3879,1,'switches','㟴΂'),(3879,1,'theme','ݷٲ\n#F()\rȴ'),(3879,1,'typical','䫯ᔲ'),(3879,1,'validation','垡'),(3880,1,'easier','˵໹'),(3880,1,'key','ư೪ŷ\r\n૶᱐'),(3880,1,'managing','ސë̠Ź'),(3880,1,'switches','ታ'),(3880,1,'theme','থ'),(3881,1,'dispatches','ḭ'),(3881,1,'easier','⨐'),(3881,1,'fired','㊄W'),(3881,1,'key','ৌ୭ĉΝ¾ނGዉ'),(3881,1,'managing','Ǜ'),(3881,1,'normalizing','἗'),(3884,1,'easier',''),(3886,1,'managing','ŦJI£U'),(3888,1,'managing','؟'),(3886,2,'managing','');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict3C` ENABLE KEYS */;

--
-- Table structure for table `dict3D`
--

DROP TABLE IF EXISTS `dict3D`;
CREATE TABLE `dict3D` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict3D`
--


/*!40000 ALTER TABLE `dict3D` DISABLE KEYS */;
LOCK TABLES `dict3D` WRITE;
INSERT INTO `dict3D` VALUES (3884,1,'figure','ı֍¥½§,ÈR<_҉éƅĢ'),(3879,1,'relationship','㡇'),(3879,1,'types','㖷̖'),(3880,1,'figure','ణÇ\n\nů(Ë૦ƃE51G6Ÿg4A\"FAı.#EĤ>ɾwYE]૪\\WȇĬɶ7eUKOĘ9gY_IJkgUĬB?BAԼ{y'),(3741,1,'bytes',''),(3748,1,'bytes','6'),(3799,1,'bytes','´'),(3801,1,'t1','#'),(3802,1,'maxchannels',''),(3814,1,'bytes','6'),(3818,1,'bytes','6'),(3819,1,'types','¯'),(3822,1,'bytes','q'),(3785,1,'bytes','6'),(3739,1,'bytes','3'),(3737,1,'bytes','3'),(3729,1,'bytes','4'),(3732,1,'bytes','3'),(3734,1,'secret',')'),(3734,1,'types','7'),(3736,1,'bytes',')!'),(3880,1,'types','ऌড়Dᚫm̭Ů̡۟˝ˠgĪ̽'),(3881,1,'secret','㗞mĠ\rȇ'),(3882,1,'figure','¡¬V'),(3883,1,'figure','¯'),(3871,1,'types','㸃ۗɱā'),(3842,1,'bytes','3'),(3846,1,'bytes','¼'),(3854,1,'bytes','9²'),(3857,1,'breezecom',''),(3860,1,'bytes','<'),(3871,1,'block','⫞'),(3871,1,'bytes','౰ẜ\'ì׏Ȑd9:®ࡃק'),(3871,1,'compares','᠔'),(3871,1,'figure','⹄߯a'),(3871,1,'reached','㑫'),(3798,1,'bytes','E'),(3728,1,'bytes','3'),(3881,1,'figure','ۇឹ'),(3784,1,'bytes','6'),(3757,1,'types','}'),(3841,1,'secret',']-'),(3840,1,'summary','ˆ'),(3836,1,'bytes','O'),(3834,1,'bytes','p'),(3833,1,'bytes','6'),(3827,1,'bytes','ǂ'),(3872,1,'figure','ݟǍǈŬ'),(3875,1,'bytes','Ʉ	'),(3877,1,'aliased','ٝ'),(3878,1,'types','À'),(3879,1,'figure','ࣩįXZ+¢Äǧ4tā:҉Ħ¿In1ࢾa3ҐƷGÉAÈm;Ķä¼ÝTܘ৯EŒी@Ìž,&طǐ̑>BǍ'),(3761,1,'bytes','6'),(3880,1,'summary','Ƒ३e໬\n⥑'),(3884,1,'relationship','ߩ'),(3884,1,'types','ࢮ'),(3885,1,'figure','³ĺժZAH'),(3886,1,'figure','ÑúMIîė('),(3887,1,'figure','ùØFƢ¾xӹķµѻ'),(3887,1,'relationship','Q੼)\n\r	'),(3888,1,'figure','Í1ϚAiM}_7ó\Z+>'),(3889,1,'figure','Ɋ8VF8'),(3890,1,'figure','ÿÁͱˌޕևֺǚȊ@75'),(3890,1,'types','ᙏּ'),(3891,1,'figure','º̈Æ'),(3892,1,'figure','v'),(3802,6,'maxchannels','F');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict3D` ENABLE KEYS */;

--
-- Table structure for table `dict3E`
--

DROP TABLE IF EXISTS `dict3E`;
CREATE TABLE `dict3E` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict3E`
--


/*!40000 ALTER TABLE `dict3E` DISABLE KEYS */;
LOCK TABLES `dict3E` WRITE;
INSERT INTO `dict3E` VALUES (3879,1,'cpu','≱'),(3879,1,'likelihood','㲉'),(3757,1,'cpu','P<ć'),(3757,1,'min','Ĭ'),(3759,1,'cpu','\Z1U'),(3763,1,'obsure','Ď'),(3766,1,'cpu','d'),(3780,1,'cpu','^'),(3782,1,'cpu',''),(3803,1,'min',';'),(3813,1,'min','('),(3819,1,'ianaiftype','¨'),(3823,1,'applications','6'),(3824,1,'applications','|Y'),(3827,1,'min','c'),(3828,1,'cpu','/'),(3828,1,'min','.'),(3837,1,'pendwarn','Ø'),(3851,1,'cpu','('),(3858,1,'bgp','!'),(3869,1,'cpan',''),(3870,1,'visit','ñ'),(3871,1,'920807100','㠹~'),(3871,1,'ctime','⌮'),(3871,1,'heartbeat','۫Œĳ@@$	!ဓկ!ނ}s'),(3871,1,'int','⌂9'),(3871,1,'mathematical','ᛖ'),(3871,1,'min','۬đ]p̎஽࠸!Àۇ\r'),(3871,1,'rise','䖖'),(3872,1,'cpan','ł'),(3873,1,'cpu','Ąƨ\n\r'),(3878,1,'applications','Â'),(3879,1,'applications','âǪ}Ϗ\n,1.ɡb@*w «ȸ(tg\Z\n\r^FG)&ÇȖW	ƓOƥƈ ➞࡭రंƈ'),(3879,1,'selections','ᛢ'),(3721,1,'applications','­'),(3721,1,'rich','ȉ'),(3727,1,'wv',';'),(3730,1,'hop','A'),(3741,1,'cpu','0s'),(3744,1,'min','ǒ.'),(3751,1,'cpu','Mǻ:'),(3879,1,'visit','ӗ'),(3880,1,'applications','Ȼӗ_dß	ǧáĴ?Êc3&ތਏᎌ'),(3880,1,'cpu','॑ഈɯӳ؃ᄐ,'),(3880,1,'visit','Ӛ'),(3881,1,'applications','Ķùś4Ҝ¥နਏ e!\Z!&K\Zr\n£ěøn\rêʜΏagƕö'),(3881,1,'construct','ಁǃ⁛cƿľؗ'),(3881,1,'visit','˪'),(3884,1,'selections','᎟2'),(3885,1,'cpu','ݲ'),(3886,1,'likelihood','ϔ'),(3887,1,'cpu','൲)'),(3888,1,'likelihood','ا'),(3890,1,'cpu','ऄࢯŕ'),(3851,6,'cpu','');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict3E` ENABLE KEYS */;

--
-- Table structure for table `dict3F`
--

DROP TABLE IF EXISTS `dict3F`;
CREATE TABLE `dict3F` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict3F`
--


/*!40000 ALTER TABLE `dict3F` DISABLE KEYS */;
LOCK TABLES `dict3F` WRITE;
INSERT INTO `dict3F` VALUES (3754,6,'html','̜'),(3890,1,'recommended','጑ɜ॰'),(3822,6,'html','Ċ'),(3788,6,'html','é'),(3879,1,'preface','ǔ'),(3765,6,'html','é'),(3751,6,'html','ʷ'),(3790,6,'html','^'),(3880,1,'recommended','Ԧശ'),(3814,6,'html','ĩ'),(3778,6,'html','ă'),(3758,6,'html','M'),(3752,6,'html','ě'),(3730,6,'html',''),(3881,1,'recommended','̶঱͆မ๯'),(3726,1,'html',''),(3729,1,'attempt','³'),(3745,1,'attempt',''),(3755,1,'attempt',''),(3762,1,'html','kL'),(3762,1,'wpl','\''),(3778,1,'attempt','\\'),(3780,1,'licensed','ĭ'),(3782,1,'attempt','n'),(3798,1,'html','Å'),(3799,1,'html','Ɔ'),(3808,1,'recommended',':'),(3813,1,'recommended','<'),(3827,1,'attempt','Ǹ'),(3827,1,'html','ƪ'),(3831,1,'html','Â'),(3841,1,'destmac','%'),(3846,1,'repeated','ß\Z'),(3862,1,'ksi','§'),(3864,1,'wpl','+'),(3869,1,'html','hj'),(3869,1,'licensed','*'),(3870,1,'html','\''),(3871,1,'000','休e'),(3871,1,'28','┶pO'),(3871,1,'7942099','Ṁ'),(3871,1,'environments','ᱳ'),(3871,1,'html','⼍\"(T1ಯ&Ú'),(3871,1,'police','䑆'),(3871,1,'recommended','࿒('),(3871,1,'ride','䖆'),(3878,1,'28','Ҕ'),(3878,1,'environments','ê'),(3879,1,'attempt','᳣'),(3879,1,'environments','ຟ໰'),(3879,1,'html','ͽᐓ㺳ĭ\n	5@ķ±8ǽɀ'),(3823,6,'html','Ù'),(3811,6,'html','Ñ'),(3810,6,'html','9'),(3809,6,'html','o'),(3808,6,'html',''),(3807,6,'html','L'),(3806,6,'html','}'),(3805,6,'html','Ø'),(3804,6,'html','À'),(3803,6,'html','é'),(3821,6,'html','F'),(3820,6,'html','Ř'),(3819,6,'html','ī'),(3818,6,'html','ĩ'),(3817,6,'html',':'),(3881,1,'preface','È'),(3881,1,'overwritten','ᭈ='),(3881,1,'html','⮾ѧ'),(3881,1,'hostgroupid','ᑮ\r'),(3880,1,'repeated','㑰'),(3881,1,'attempt','㜟'),(3770,6,'html','2'),(3769,6,'html','2'),(3768,6,'html','5'),(3761,6,'html','ĩ'),(3747,6,'html','f'),(3748,6,'html','ĩ'),(3749,6,'html','6'),(3750,6,'html','A'),(3722,6,'html','Ů'),(3723,6,'html','O'),(3724,6,'html','r'),(3725,6,'html','l'),(3726,6,'html',''),(3880,1,'html','˧ጆ'),(3880,1,'preface','ğ,'),(3772,6,'html','2'),(3759,6,'html','ĉ'),(3755,6,'html','Ê'),(3737,6,'html','Ħ'),(3738,6,'html',''),(3739,6,'html','Ħ'),(3740,6,'html','Ɣ'),(3741,6,'html','Ç'),(3742,6,'html',''),(3743,6,'html',''),(3744,6,'html','ɧ'),(3745,6,'html','ć'),(3746,6,'html','>'),(3720,6,'html','Ǆ'),(3880,1,'attempt','ዖ'),(3813,6,'html','¢'),(3783,6,'html','â'),(3787,6,'html','´'),(3800,6,'html','P'),(3798,6,'html','û'),(3791,6,'html','¼'),(3793,6,'html','³'),(3720,1,'html','QM'),(3784,6,'html','ĩ'),(3727,6,'html','Ō'),(3795,6,'html',''),(3779,6,'html','A'),(3802,6,'html','G'),(3762,6,'html','ê'),(3721,6,'html','ʥ'),(3721,1,'environments','Ǽ'),(3760,6,'html','N'),(3731,6,'html','¦'),(3732,6,'html','ħ'),(3733,6,'html',''),(3734,6,'html','Ĉ'),(3735,6,'html',''),(3736,6,'html','È'),(3729,6,'html','ß'),(3815,6,'html','w'),(3774,6,'html','8'),(3756,6,'html','Ā'),(3816,6,'html','^'),(3792,6,'html','p'),(3780,6,'html','ƺ'),(3782,6,'html','ơ'),(3757,6,'html','Ʋ'),(3801,6,'html','A'),(3824,6,'html','ť'),(3767,6,'html','4'),(3753,6,'html',''),(3879,1,'recommended','ԣ঍்ߥւ'),(3812,6,'html','>'),(3786,6,'html','J'),(3763,6,'html','ń'),(3773,6,'html','6'),(3766,6,'html','ü'),(3775,6,'html','5'),(3799,6,'html','ƚ'),(3796,6,'html','4'),(3888,1,'28','ޠč'),(3889,1,'html','ӯ'),(3890,1,'attempt','ࢍஓ<'),(3890,1,'html','Ǎĭ̪·॥IѠ£Já'),(3785,6,'html','ĩ'),(3771,6,'html','2'),(3781,6,'html','¥'),(3777,6,'html','6'),(3887,1,'html','ತ¡'),(3721,1,'licensed','Ɛ'),(3728,6,'html','Ħ'),(3797,6,'html','x'),(3789,6,'html','£'),(3764,6,'html','I'),(3879,1,'overwritten','⧺Ɯ'),(3776,6,'html','ĳ'),(3794,6,'html','2'),(3825,6,'html','Ő'),(3826,6,'html','1'),(3827,6,'html','ˠ'),(3828,6,'html','¨'),(3829,6,'html',''),(3830,6,'html',''),(3831,6,'html','Ü'),(3832,6,'html','Y'),(3833,6,'html','ĩ'),(3834,6,'html','Ä'),(3835,6,'html',''),(3836,6,'html','o'),(3837,6,'html','ı'),(3838,6,'html','M'),(3839,6,'html','@'),(3840,6,'html','̣'),(3841,6,'html','¸'),(3842,6,'html','I'),(3843,6,'html','u'),(3844,6,'html','á'),(3845,6,'html',''),(3846,6,'html','Ń'),(3847,6,'html','_'),(3848,6,'html','º'),(3849,6,'html',''),(3850,6,'html','U'),(3851,6,'html',''),(3852,6,'html','5'),(3853,6,'html','^'),(3854,6,'html','ō'),(3855,6,'html','D'),(3856,6,'html','ƛ'),(3857,6,'html','i'),(3858,6,'html','>'),(3859,6,'html','L'),(3860,6,'html','Q'),(3861,6,'html','<'),(3862,6,'html','Ĥ'),(3863,6,'html','ņ'),(3864,6,'html',''),(3865,6,'html','¬'),(3866,6,'html','Á'),(3867,6,'html',':'),(3868,6,'html','Ğ'),(3869,6,'html','ġ'),(3870,6,'html','ī'),(3871,6,'html','僞'),(3872,6,'html','೷'),(3873,6,'html','ի'),(3874,6,'html','Ϲ'),(3875,6,'html','͏'),(3876,6,'html','ƶ'),(3877,6,'html','ޔ'),(3878,6,'html','ो'),(3879,6,'html','朷'),(3880,6,'html','䡅'),(3881,6,'html','㧅'),(3882,6,'html','ƴ'),(3883,6,'html','ŏ'),(3884,6,'html','ᑨ'),(3885,6,'html','য়'),(3886,6,'html','Լ'),(3887,6,'html','ᄖ'),(3888,6,'html','ऑ'),(3889,6,'html','ԁ'),(3890,6,'html','ᾕ'),(3891,6,'html','ِ'),(3892,6,'html','ǆ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict3F` ENABLE KEYS */;

--
-- Table structure for table `dict40`
--

DROP TABLE IF EXISTS `dict40`;
CREATE TABLE `dict40` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict40`
--


/*!40000 ALTER TABLE `dict40` DISABLE KEYS */;
LOCK TABLES `dict40` WRITE;
INSERT INTO `dict40` VALUES (3721,1,'handling',''),(3885,1,'group','ऌ$'),(3881,1,'group','च÷ঈ$\r\r\rᾂ'),(3881,1,'guava','Ģ⍀F(	iĈ\"\n\n?(&),.*i	\n3Ac\nE;1\r\nÍÎ=,?62#a\\)»ũ\n\'MZũ\"AN?O\r	!\n:|iL'),(3724,1,'group','\"'),(3756,1,'1645','S'),(3788,1,'matched','1'),(3819,1,'empty','\"'),(3820,1,'empty','l'),(3829,1,'group','H'),(3846,1,'gb','¿'),(3869,1,'handling','Ñ'),(3870,1,'group',''),(3871,1,'empty','ᴐ⍸'),(3871,1,'fault','㋈'),(3871,1,'gb','ನ'),(3871,1,'handling','Ŋ'),(3871,1,'reality','㴗ᇸ'),(3871,1,'shadeb','ᖹ'),(3871,1,'val4','ẛB'),(3872,1,'group','Vߝ$äǄÛ#!	'),(3877,1,'group','ƙȔ'),(3879,1,'enters','ऍ'),(3879,1,'group','␣.|Ϋ\n	Ʀƍk\Zë͌Ȣ\Z$(͏\Z\n	-ϛv`)§5ƒ& ੳpݖǱI\rાÃɺ		ϗ'),(3879,1,'guava','àŦÇѩ6ɉ̷(!(Ŋ(%/%~\rÜɣſ4ŧќ▀ང\nZƟ͚ಕ$İ\n\n-<'),(3879,1,'reality','Ṿ'),(3880,1,'enters','⥯ټ'),(3880,1,'group','ডܫŐŦ֓>èā%ЊUҺZi*2\n0ċ¦)\Z \Z\r޹ø%Գ\r@7Ő I26Ő\ZE¥ )M~ĎX'),(3880,1,'guava','ɷצş'),(3881,1,'empty','⺯Ѷ/'),(3881,1,'facilitate','➦ħ'),(3720,1,'handling','ƒ'),(3887,1,'group','A̗Hו &'),(3881,1,'matched','᳗'),(3886,1,'group','ʱ|G'),(3884,1,'group','ڇĘ஽l\r'),(3888,1,'group',')*\n	*\n	BB\'J#+1,4C\ryu\r\n	 '),(3888,1,'illustrate','ߤ'),(3889,1,'group','̚ė- '),(3890,1,'empty','Ṹ'),(3890,1,'group','ঋ'),(3890,1,'matched','˝'),(3891,1,'group','3̗& \n');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict40` ENABLE KEYS */;

--
-- Table structure for table `dict41`
--

DROP TABLE IF EXISTS `dict41`;
CREATE TABLE `dict41` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict41`
--


/*!40000 ALTER TABLE `dict41` DISABLE KEYS */;
LOCK TABLES `dict41` WRITE;
INSERT INTO `dict41` VALUES (3880,1,'checked','⭛Ӛۑ'),(3879,1,'dive','ⷧ'),(3879,1,'easy','ΰⅈ'),(3879,1,'production','ℷ6'),(3879,1,'checked','䜉'),(3879,1,'displays','૾̅Φ エᇧ'),(3871,1,'797','䉺M('),(3721,1,'easy','Ƴ'),(3721,1,'production','ǻ'),(3722,1,'dcs','°'),(3744,1,'checked','ȼ'),(3871,1,'easy','Ƃ㊞ᯬ'),(3878,1,'production','é'),(3877,1,'easy','ǁß'),(3872,1,'tap','Ӡ\n`܀'),(3871,1,'reinsert','⠞'),(3884,1,'checked','Ǯ*Ĕ2K>¥FX24״Ĕ2K>¥FX24Ž'),(3871,1,'displays','㨾'),(3880,1,'layer','ཱྀY'),(3880,1,'easy','۳ТⳜୖ¯'),(3880,1,'displays','೫Ƣńڽ̅ǮRmxƄ\r¸+8ƸEwȓēnZങǐ̎yjWIQħ9l[[LKA,>*=*ħ6/A@AǪ>'),(3881,1,'displays','ׯ'),(3881,1,'dive','⟟'),(3881,1,'layer','ӨܸྭɿŏƼ'),(3881,1,'discussion','㟤'),(3880,1,'tap','䚇'),(3869,1,'easy','ă'),(3837,1,'checked',''),(3828,1,'yourhost',';'),(3754,1,'checked','ɛ'),(3780,1,'checked','ƞ'),(3793,1,'checked',' '),(3804,1,'checked','J'),(3813,1,'javaproc',''),(3827,1,'checked','â'),(3885,1,'checked','ʪ43!%póX©	J'),(3885,1,'ochp','У'),(3886,1,'checked','Ƒ'),(3887,1,'checked','Ԝ3!%róX©	I'),(3887,1,'ochp','١'),(3890,1,'checked','Ʉķ࠷sɘ9ÿ !=8ň¬ÛĥQÛ¦͟)N'),(3890,1,'ochp','ᝯ'),(3890,1,'production','ᇐ'),(3891,1,'checked','Ǫ»ɶ<'),(3813,6,'javaproc','¡');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict41` ENABLE KEYS */;

--
-- Table structure for table `dict42`
--

DROP TABLE IF EXISTS `dict42`;
CREATE TABLE `dict42` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict42`
--


/*!40000 ALTER TABLE `dict42` DISABLE KEYS */;
LOCK TABLES `dict42` WRITE;
INSERT INTO `dict42` VALUES (3820,1,'maxmsgsize','ï'),(3799,1,'utilization','·j'),(3819,1,'maxmsgsize','ý'),(3797,1,'subset',' '),(3765,1,'larger','=\n'),(3782,1,'showall','6ç'),(3874,1,'utilization','Ȣ'),(3824,1,'subset',''),(3763,1,'disconnects','´'),(3873,1,'printque','͓\r'),(3872,1,'metrocall','ԝ<¦'),(3871,1,'subset','ⲽ'),(3871,1,'separator','ຘ'),(3871,1,'prep','℅'),(3871,1,'larger','ᢏှ'),(3879,1,'utilization','ሴ'),(3879,1,'dc','Ნn'),(3878,1,'utilization','Ϧ'),(3873,1,'utilization','Ĝ'),(3870,1,'focused',''),(3875,1,'utilization','W'),(3871,1,'273','ೃ>䃟'),(3880,1,'273','₁'),(3880,1,'utilization','॒ഈɯӳᜣ*'),(3880,1,'subset','䏏'),(3880,1,'larger','⍴ᙄʻ'),(3880,1,'entity','㩓ˁ['),(3881,1,'applicationtype','‮Èĵ$¼/'),(3881,1,'larger','❊'),(3881,1,'subset','⡉'),(3884,1,'refreshes','࠸'),(3888,1,'conversely','Ԏ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict42` ENABLE KEYS */;

--
-- Table structure for table `dict43`
--

DROP TABLE IF EXISTS `dict43`;
CREATE TABLE `dict43` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict43`
--


/*!40000 ALTER TABLE `dict43` DISABLE KEYS */;
LOCK TABLES `dict43` WRITE;
INSERT INTO `dict43` VALUES (3881,1,'stores','ߵ'),(3881,1,'symbols','㘉'),(3884,1,'dev','ვ'),(3884,1,'enabled','̕\r{1ØG/Ͱσ\r{1ØG/'),(3879,1,'dev','ạ'),(3879,1,'enabled','ࣿ)൧ⵗ'),(3879,1,'graphs','ᇗ_Ŋ⺷%@Cŋ	V'),(3879,1,'indicated','֔˲'),(3879,1,'linked','嚒ƣ'),(3881,1,'linked','⋋%ɰ'),(3881,1,'isproblemacknowledged','࡫'),(3881,1,'indicated','Χፏ'),(3880,1,'stores','྿߀₊'),(3879,1,'stores','ፅ䍿'),(3879,1,'symbols','௪'),(3880,1,'analysis','ࢦⱅ'),(3880,1,'anchor','ᡮ'),(3880,1,'enabled','₿0m#T١͋=ҝ'),(3880,1,'graphs','Į֣aǱŗլٍɨ΃ō\"+״ǆว	#jEá=cಽƯ'),(3880,1,'indicated','֗Ῡ'),(3878,1,'38','ٸ'),(3877,1,'enabled','Ű'),(3892,1,'incorporated','Æ'),(3887,1,'enabled','Ԑ0\r+ŐGú'),(3879,1,'anchor','娙,$K'),(3878,1,'graphs','ࢊ'),(3884,1,'indicated','ૼ'),(3885,1,'enabled','˒0\r*ŎGû'),(3884,1,'preconfigured','ۋ'),(3875,1,'graphs','Ɗ'),(3721,1,'demanding','Ǻ'),(3876,1,'graphs','pĠ'),(3890,1,'portion','Ǳ'),(3890,1,'enabled','ৈ6e696c6=6>6ǶæӮ9RɊWo>99ɚN˶'),(3879,1,'portion','᱿'),(3721,1,'stores','¸'),(3740,1,'indicated','´v'),(3740,1,'snmpd','ŷ'),(3744,1,'indicated','Ƶ'),(3751,1,'analysis','9'),(3757,1,'indicated','Ý\r'),(3764,1,'enabled','!'),(3765,1,'dev','©\r'),(3780,1,'enabled','Ĩ'),(3793,1,'indicated','V'),(3795,1,'indicated','I'),(3812,1,'dev',')'),(3816,1,'dev','*'),(3827,1,'enabled','Ɂ'),(3828,1,'dev','f'),(3846,1,'indicated','õ'),(3871,1,'1wk','ⓢ'),(3871,1,'analysis','ǋ'),(3871,1,'discussions','㒌'),(3871,1,'doubles','థ'),(3871,1,'graphs','`T˄ృɻ˶ᜤĮ9.ęʅᎿǒ߭'),(3871,1,'hints','୪'),(3871,1,'introduced','ⴳ'),(3871,1,'line3','ᮞⶎ'),(3871,1,'portion','㾐'),(3871,1,'rea','ᾂ'),(3871,1,'stores','NՓڐßᕥ຺ᄌ3ಈ'),(3872,1,'dev','͍\\^,â'),(3874,1,'graphs','ˇ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict43` ENABLE KEYS */;

--
-- Table structure for table `dict44`
--

DROP TABLE IF EXISTS `dict44`;
CREATE TABLE `dict44` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict44`
--


/*!40000 ALTER TABLE `dict44` DISABLE KEYS */;
LOCK TABLES `dict44` WRITE;
INSERT INTO `dict44` VALUES (3871,1,'meaningless','▐'),(3871,1,'space','цଜ೓,'),(3776,1,'space','©'),(3823,1,'neighbourhood',' '),(3828,1,'plug',''),(3783,1,'space','l'),(3778,1,'space',''),(3872,1,'terminal','-బ'),(3872,1,'signalling','̝þ'),(3872,1,'reload','˲(¶)(^'),(3872,1,'plug','ڂ'),(3879,1,'databases','ȶ\Z㿽‿\Z		ţn'),(3879,1,'controls','恶'),(3766,1,'space','w'),(3744,1,'space','ƺ'),(3736,1,'space','1'),(3721,1,'databases','àč.'),(3734,1,'space','!6'),(3875,1,'space','ʊ'),(3874,1,'space','̀?J'),(3871,1,'imperialists','✠'),(3774,1,'logmode',''),(3871,1,'databases','ਣ✘Ḃ'),(3871,1,'branches','㼆'),(3799,1,'space','ĥ'),(3872,1,'controls','í\nՐ'),(3780,1,'space','±		\n'),(3871,1,'value1','ẻ'),(3779,1,'space',''),(3780,1,'puprb','ā'),(3871,1,'assuming','ḍ᫴'),(3871,1,'15min','⓽'),(3871,1,'10s','ܰ'),(3871,1,'02','㢧'),(3869,1,'databases',']'),(3862,1,'02','t'),(3846,1,'space','H '),(3844,1,'space','r3'),(3841,1,'02','W-'),(3840,1,'references','˰'),(3832,1,'space','6'),(3828,1,'space','k'),(3879,1,'terminal','揍+¬?'),(3879,1,'space','ᝌ׽T ᏐᎢ'),(3879,1,'references','䤹ڊ'),(3871,1,'reload','㳼'),(3871,1,'solved','㌩'),(3871,1,'seconds2','佧/'),(3879,1,'value1','䗆ā̰'),(3880,1,'databases','ȼ㙚'),(3880,1,'space','㌰ƪቯ'),(3881,1,'controls','╍࣠'),(3881,1,'currentnotificationnumber','ࡒ '),(3881,1,'databases','Ƭұ؞'),(3881,1,'setlabel','⧑'),(3884,1,'controls','ُ'),(3885,1,'controls','ܻ'),(3885,1,'space','ݢ'),(3887,1,'controls','ɂ'),(3887,1,'space','ʮtଝx'),(3890,1,'assuming','ೢ'),(3890,1,'controls','̐'),(3890,1,'reload','้'),(3890,1,'rotated','೜'),(3890,1,'space','ᆫ'),(3774,6,'logmode','6'),(3779,6,'space','@'),(3823,6,'neighbourhood','Ø');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict44` ENABLE KEYS */;

--
-- Table structure for table `dict45`
--

DROP TABLE IF EXISTS `dict45`;
CREATE TABLE `dict45` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict45`
--


/*!40000 ALTER TABLE `dict45` DISABLE KEYS */;
LOCK TABLES `dict45` WRITE;
INSERT INTO `dict45` VALUES (3872,1,'checkproc','̿ď*'),(3879,1,'96','Ậ'),(3879,1,'bit','㐋'),(3878,1,'output','ϥ'),(3877,1,'path','ݭ'),(3880,1,'directive','ᬫ'),(3879,1,'listed','˾֮װݹÓƓ૊	ܩƜьǨ߶࣊೼ஶó'),(3879,1,'output','Ẁᓝӱ୷š7\rǇڼ¤хƅ1'),(3779,1,'pctwarn','+'),(3720,1,'populate','¼ '),(3745,1,'output','Ô'),(3747,1,'output','/'),(3748,1,'output','¸^'),(3748,1,'sending','Ñ'),(3751,1,'path','f'),(3754,1,'output','ŷ'),(3755,1,'output',''),(3756,1,'risk','bl'),(3757,1,'output','´'),(3757,1,'path','ħ'),(3761,1,'output','¸^'),(3761,1,'sending','Ñ'),(3762,1,'output','oI'),(3763,1,'output',''),(3778,1,'path','Ã'),(3739,1,'sending','Î'),(3743,1,'output','z'),(3744,1,'output','hë\r\Z'),(3739,1,'output','µ^'),(3738,1,'output','y'),(3879,1,'stopped','䫀ᔲ'),(3737,1,'sending','Î'),(3736,1,'output',''),(3737,1,'output','µ^'),(3732,1,'output','¶^'),(3732,1,'sending','Ï'),(3733,1,'listed','+'),(3735,1,'output','o'),(3721,1,'years','ǿ'),(3879,1,'path','൴ٟˠݕJ㢇˞'),(3788,1,'sets','g'),(3811,1,'output','¾'),(3814,1,'output','¸^'),(3814,1,'sending','Ñ'),(3816,1,'output',''),(3818,1,'output','¸^'),(3818,1,'sending','Ñ'),(3824,1,'output','ŋ'),(3730,1,'output','P'),(3871,1,'years','ⓇGMé'),(3785,1,'sending','Ñ'),(3856,1,'listed','Ĉ'),(3855,1,'output','/'),(3856,1,'output','¦'),(3722,1,'output','ŗ'),(3724,1,'output',''),(3726,1,'output','\r'),(3727,1,'output','h0'),(3728,1,'output','µ^'),(3728,1,'sending','Î'),(3729,1,'output','®'),(3827,1,'output','ƨK'),(3784,1,'sending','Ñ'),(3871,1,'sets','ೞا}੄ᅼ'),(3871,1,'96','䔭'),(3785,1,'output','¸^'),(3871,1,'path','ⱳɼ੫ԓ'),(3825,1,'output','ķ'),(3824,1,'sending','@'),(3788,1,'path','~'),(3788,1,'output','.'),(3854,1,'sending','õ'),(3871,1,'pressing','㴑'),(3791,1,'path',''),(3871,1,'bit','ࢂ㮨'),(3850,1,'output','-'),(3803,1,'output',''),(3803,1,'listed',''),(3871,1,'output','࿳бCটɧণ¹>ňęźԣҊv࣒øª'),(3872,1,'listed','įબ'),(3871,1,'stopped','䊌ƷǢ'),(3797,1,'output','`'),(3879,1,'directive','ᬡ^శ\"!QѺƻð.૜'),(3868,1,'output','ą'),(3825,1,'sending','¤ '),(3871,1,'sending','භ▦'),(3845,1,'output','q'),(3843,1,'output','a'),(3840,1,'path','ƺ'),(3840,1,'listed','ğ'),(3837,1,'sending','Ã'),(3834,1,'output',''),(3833,1,'sending','Ñ'),(3833,1,'output','¸^'),(3831,1,'output','b'),(3827,1,'path','ċ'),(3880,1,'sending','ሒ'),(3880,1,'output','༅#'),(3851,1,'output','D'),(3871,1,'risk','䲁'),(3784,1,'output','¸^'),(3868,1,'listed','ZL'),(3854,1,'path','l'),(3854,1,'output','Ü^'),(3879,1,'sending','␯ᣪ'),(3879,1,'holes','᪬'),(3866,1,'path',''),(3848,1,'output','§'),(3862,1,'sending',''),(3782,1,'output','Ř'),(3783,1,'output','±'),(3808,1,'output','r'),(3805,1,'output',''),(3846,1,'output','İ'),(3872,1,'directive','ޮ$ƜIO)Ç.ē'),(3880,1,'listed','ɚӝ๱ݾʎDȈƌþ̈	๹«Ⴋ'),(3846,1,'path',':	'),(3865,1,'output','~'),(3804,1,'output',''),(3787,1,'path',''),(3787,1,'output','\'#'),(3877,1,'755','Ɛ'),(3875,1,'output','Vȸ'),(3872,1,'sending','i٭'),(3874,1,'path','ɓ'),(3880,1,'sets','ၷ␊îv'),(3881,1,'directive','⵳´}ެ̤'),(3881,1,'distribute','ᰲ'),(3881,1,'div','〉'),(3881,1,'listed','ࡅପ'),(3881,1,'output','Ỡრ'),(3881,1,'path','ౄ∗ۃ3lßɔ'),(3882,1,'listed','Ƅ'),(3883,1,'directive','Å'),(3884,1,'directive','Ǳ2;Ka\Z\"1/L;		#\"\"ק2;Ka\Z\"1/L;		#\"\"ŎȈ'),(3884,1,'listed','਌J>5/࡚2'),(3884,1,'sets','ŕ'),(3885,1,'directive','˄$\"!		\"$t!: ,:`'),(3885,1,'listed','Ȱ'),(3885,1,'sets','ʫ'),(3886,1,'directive','ß!Q'),(3886,1,'listed','Ȓ'),(3886,1,'sets','ƒ]'),(3887,1,'bit','ഞ'),(3887,1,'directive','ć!QΉ$\"!		\"$t!: ,:_ãmל'),(3887,1,'listed','ଳ'),(3887,1,'path','బ'),(3888,1,'distribute','ю'),(3888,1,'listed','ݜ'),(3888,1,'path','ʦË'),(3889,1,'directive','¹WAg\Z)'),(3889,1,'listed','ʾĜ'),(3890,1,'adversely','ཕ'),(3890,1,'bit','๧ۢ'),(3890,1,'directive','ٰƺཛྷLGGŸ>'),(3890,1,'output','ʧ9֋Ꭳ-'),(3890,1,'path','č	\r«\Z`mΚ		$Ԣၪ'),(3890,1,'populate','Ṅ'),(3890,1,'timely','᭑O'),(3891,1,'directive','È.wCK<CcîµCCKc<'),(3891,1,'sending','̼'),(3891,1,'sets','ů'),(3892,1,'directive','ĝ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict45` ENABLE KEYS */;

--
-- Table structure for table `dict46`
--

DROP TABLE IF EXISTS `dict46`;
CREATE TABLE `dict46` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict46`
--


/*!40000 ALTER TABLE `dict46` DISABLE KEYS */;
LOCK TABLES `dict46` WRITE;
INSERT INTO `dict46` VALUES (3880,1,'separate','ចǄ൐ཻY'),(3871,1,'3w1m','♠'),(3881,1,'third','שᓖၬऱˇ¨ĳ'),(3881,1,'tickets','⫟೉'),(3856,1,'option','ö'),(3862,1,'option','ð'),(3868,1,'option','ĉ'),(3869,1,'stable',''),(3869,1,'third','[5'),(3871,1,'option','٘Łȋफõ\rĘSc኶צ\''),(3871,1,'ge','᠌'),(3871,1,'finest','∳'),(3871,1,'context','☽'),(3881,1,'feederscript','⊾H'),(3880,1,'temporary','ᬽ'),(3884,1,'option','Ŕɉğ`ࡺğ`ʡ'),(3880,1,'option','Պ¥јE-ȵԳ+ѕ¸>ŏÑހŨʘȝWm9H*)n:ʓu7o)³ʩŘĘD>̈́\"dULŃ\"<h[aL@92607Ĩ6\"B?Bǈġʫ'),(3880,1,'early','㒰'),(3879,1,'outline','ۇቤ'),(3879,1,'separate','ⅼԳ⿼ƣ'),(3879,1,'temporary','懹'),(3879,1,'third','˄'),(3880,1,'collector','ཡ(@'),(3872,1,'separate','Ӱ'),(3873,1,'option','œ¥'),(3879,1,'option','ƺͽ¥ɛ\\ēs>þıƺìŞHDҧĮæiݘϐ4\"7A9\'+Ñ³R¡1ǙĬǆBztcȰ#!\n࠾%NÛĩж¨ǉƔd!µĥ׮ęʙ-	ડì۬	'),(3879,1,'execcgi','൪'),(3879,1,'context','᰽'),(3878,1,'option','ए'),(3873,1,'proc','ͱ'),(3885,1,'stalking','ۥ	'),(3880,1,'outline','Ч'),(3881,1,'context','㠚'),(3871,1,'overlay','ཏԻર'),(3871,1,'third','ലⲻӡ'),(3871,1,'timed','݋'),(3871,1,'whitespace','⚨\r$'),(3880,1,'stats','㓦'),(3727,1,'option','Ć'),(3730,1,'option','<'),(3734,1,'separate',''),(3734,1,'whitespace',''),(3741,1,'issued','}'),(3744,1,'separate','Ȩ'),(3751,1,'context','ʕ'),(3752,1,'issued',''),(3752,1,'whitespace','G'),(3756,1,'option','¶'),(3763,1,'option','ë'),(3766,1,'collector','µ'),(3766,1,'proc','{'),(3768,1,'stats',''),(3783,1,'option','t%'),(3789,1,'aging','n'),(3789,1,'option','9'),(3799,1,'option','ª'),(3805,1,'temporary','A'),(3806,1,'temporary','G'),(3819,1,'context','½'),(3819,1,'option','W'),(3820,1,'context','d'),(3824,1,'option','=|'),(3825,1,'option','2(|'),(3827,1,'option','Û'),(3721,1,'separate','»'),(3885,1,'option','ƢʔU±â'),(3881,1,'option','͚¥ㄞ'),(3882,1,'option','\nHU¬'),(3881,1,'separate','ޣd⟆'),(3880,1,'third','ȣ൷'),(3872,1,'option','اůɋO'),(3886,1,'option','¨'),(3887,1,'option','Ð̝̯U±á˪ǊŁfŸ'),(3887,1,'stalking','थ	'),(3888,1,'option','öġ\r\r\r6-Å5t̶0$'),(3889,1,'option','ж'),(3889,1,'third','ô'),(3890,1,'cautious','ഐ'),(3890,1,'context','Ȩ\n'),(3890,1,'hitting','͗'),(3890,1,'option','éãð?IƴC!7%=º7C2&\" 1##\"1\'#\'%sŃ½6(T\'!\"Iå~fI\n%\n9\']\r2S2\'%	C&877##553422>´24(uuBf&}6'),(3890,1,'third','˕'),(3891,1,'option','ȆFFȮcF<'),(3882,2,'option',''),(3890,2,'option',''),(3768,6,'stats','4');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict46` ENABLE KEYS */;

--
-- Table structure for table `dict47`
--

DROP TABLE IF EXISTS `dict47`;
CREATE TABLE `dict47` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict47`
--


/*!40000 ALTER TABLE `dict47` DISABLE KEYS */;
LOCK TABLES `dict47` WRITE;
INSERT INTO `dict47` VALUES (3871,1,'spacing','ĳ0'),(3761,1,'polling','Ô'),(3720,1,'fragment','°'),(3854,1,'polling','ø'),(3881,1,'segments','⯀'),(3872,1,'macro','۩ɱżH.>'),(3862,1,'libraries','Ö'),(3879,1,'seekfile','扇'),(3879,1,'reset','族'),(3879,1,'maintain','Ͼ᫤Ě'),(3879,1,'macro','⓹݅͟.>࿘՟Řद'),(3879,1,'libraries','ᤙ㷅ѫrº9'),(3872,1,'reset','ʼ~'),(3881,1,'getdevicechildren','ጶ'),(3881,1,'dynamic','℣ګƣʒѷ'),(3880,1,'segments','᱐'),(3748,1,'polling','Ô'),(3739,1,'polling','Ñ'),(3737,1,'polling','Ñ'),(3732,1,'polling','Ò'),(3728,1,'polling','Ñ'),(3724,1,'hagrp',''),(3721,1,'libraries','ɂ'),(3880,1,'maintain','䓞'),(3880,1,'dynamic','๖।'),(3881,1,'reset','෹┩3'),(3869,1,'libraries',''),(3871,1,'4000000000e','⭝'),(3785,1,'polling','Ô'),(3881,1,'render','㠺'),(3880,1,'reset','൷'),(3871,1,'random','€'),(3871,1,'render','ᗝ'),(3871,1,'pixels','༽ѩ▤'),(3871,1,'reset','ࡽ㿄'),(3778,1,'ratios','s'),(3784,1,'polling','Ô'),(3756,1,'exacerbated','Ð'),(3805,1,'seekfile','>'),(3839,1,'random',''),(3833,1,'polling','Ô'),(3818,1,'polling','Ô'),(3814,1,'polling','Ô'),(3806,1,'seekfile','D'),(3884,1,'macro','ჯ'),(3884,1,'maintain','ୃ'),(3886,1,'macro','ń'),(3887,1,'macro','Ŭ'),(3887,1,'pixels','ɨ@৬ĜI@'),(3888,1,'macro','eӃ\n[\ZDr\n	?9h\rDL25-'),(3888,1,'maintain','ڶ'),(3890,1,'macro','ܬŏ᎒P!&\r'),(3891,1,'macro','Û.>'),(3892,1,'macro','C\n FÇ'),(3839,6,'random','?');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict47` ENABLE KEYS */;

--
-- Table structure for table `dict48`
--

DROP TABLE IF EXISTS `dict48`;
CREATE TABLE `dict48` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict48`
--


/*!40000 ALTER TABLE `dict48` DISABLE KEYS */;
LOCK TABLES `dict48` WRITE;
INSERT INTO `dict48` VALUES (3879,1,'insure','֡'),(3871,1,'shadea','ᖵ'),(3871,1,'history','ԛ'),(3871,1,'printouts','㈈'),(3879,1,'folders','䂖'),(3879,1,'installing','ѧଥf'),(3880,1,'history','ଢ଼Ⲱ෎'),(3879,1,'sv','ጟ䳶ª'),(3840,1,'history','˿'),(3730,1,'msec','C'),(3780,1,'drews','Ű'),(3822,1,'msec',''),(3828,1,'querying','\''),(3840,1,'querying','Ƣ'),(3871,1,'air','䠘'),(3871,1,'agginput','ᷪ\Z'),(3871,1,'920808900','㡍|'),(3869,1,'duct','À'),(3866,1,'msec','¥'),(3872,1,'bd','͋'),(3875,1,'ifbandwidth','ÏëX\Z'),(3880,1,'installing','Ѫۢ'),(3880,1,'insure','֤'),(3880,1,'querying','䌁'),(3881,1,'installing','ɺᭇѝᅶā'),(3881,1,'insure','δ'),(3881,1,'sv','Ⓦ'),(3887,1,'minimize','ක'),(3888,1,'proxy2','ۼŝ\n	c'),(3890,1,'folders','-s'),(3890,1,'minimize','ᆲ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict48` ENABLE KEYS */;

--
-- Table structure for table `dict49`
--

DROP TABLE IF EXISTS `dict49`;
CREATE TABLE `dict49` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict49`
--


/*!40000 ALTER TABLE `dict49` DISABLE KEYS */;
LOCK TABLES `dict49` WRITE;
INSERT INTO `dict49` VALUES (3847,1,'centigrade','B'),(3795,1,'detailed',','),(3798,1,'detailed','x'),(3848,1,'detailed','?'),(3834,1,'detailed','H'),(3834,1,'packet','u6'),(3836,1,'detailed','T'),(3838,1,'detailed','3'),(3840,1,'detailed','V'),(3846,1,'detailed','L'),(3761,1,'detailed','R'),(3755,1,'detailed','3'),(3756,1,'detailed','='),(3871,1,'detailed','ǻyෝ%ᆈ ࠯&ड'),(3871,1,'dec','⒍'),(3869,1,'mod','ï'),(3866,1,'detailed','q'),(3864,1,'packet','w'),(3868,1,'detailed','9'),(3849,1,'detailed','l'),(3799,1,'conjunction','Ė'),(3754,1,'detailed','ƶ'),(3879,1,'directives','ᬜ	፬ϯῖ๓Ðú'),(3868,1,'directives',','),(3725,1,'detailed','6'),(3799,1,'detailed','H'),(3804,1,'packet',''),(3805,1,'lib','{'),(3807,1,'detailed','2'),(3809,1,'detailed',','),(3811,1,'detailed','<'),(3814,1,'detailed','R'),(3816,1,'filesystem','\"'),(3817,1,'filesystem','\Z'),(3818,1,'detailed','R'),(3822,1,'packet','T,'),(3824,1,'packet','Ć'),(3825,1,'packet','Ĩ'),(3827,1,'detailed',''),(3828,1,'filesystem','G'),(3829,1,'detailed','_'),(3748,1,'detailed','R'),(3871,1,'statements','⨎'),(3871,1,'2100','䤉'),(3870,1,'statements','ä'),(3866,1,'mod','$h'),(3863,1,'detailed','<'),(3856,1,'detailed','<'),(3854,1,'detailed','V'),(3853,1,'detailed','C'),(3873,1,'buff','ѻ'),(3831,1,'detailed','4'),(3833,1,'detailed','R'),(3727,1,'detailed','I'),(3728,1,'detailed','O'),(3729,1,'detailed','Q'),(3731,1,'detailed','5'),(3732,1,'detailed','P'),(3736,1,'detailed','2'),(3737,1,'detailed','O'),(3738,1,'detailed','0'),(3739,1,'detailed','O'),(3740,1,'detailed','P'),(3744,1,'detailed','}'),(3745,1,'detailed','='),(3746,1,'detailed','$'),(3871,1,'specifies','ܱ8'),(3879,1,'conjunction','ᇫ'),(3879,1,'detailed','Ч僝'),(3878,1,'detailed','ū'),(3877,1,'filesystem','ɐ'),(3877,1,'accounted','ݙ'),(3876,1,'packet','Ŕ'),(3873,1,'detailed','_'),(3793,1,'detailed','9'),(3791,1,'detailed','M'),(3789,1,'detailed','X'),(3787,1,'detailed','^'),(3785,1,'detailed','R'),(3784,1,'detailed','R'),(3780,1,'packet','þ'),(3762,1,'detailed','6'),(3762,1,'packet',''),(3763,1,'detailed','.'),(3765,1,'filesystem','È'),(3765,1,'pcpmetric','K_'),(3766,1,'detailed','@'),(3766,1,'filesystem','y'),(3778,1,'filesystem','U'),(3780,1,'detailed',';'),(3879,1,'dojotoolkit','ΐ'),(3879,1,'mod','឴'),(3879,1,'specifies','ዙ'),(3879,1,'statements','旨'),(3880,1,'detailed','·ৎࠪƙĦ&%Ź̻DʌTƵRĪ͋WzඛՕÓÀſȁɥμ'),(3880,1,'directives','໵ٻ'),(3880,1,'disregarded','ⶠЫ'),(3880,1,'dojotoolkit','˺'),(3880,1,'initiated','⅐'),(3880,1,'specifies','ᎉ'),(3881,1,'deliminated','㤾'),(3881,1,'detailed','ȭ'),(3881,1,'directives','ⷱࡱnƈ'),(3881,1,'mod','⫣ࢳ«!,\n\"/Ċɒ'),(3881,1,'portlet','⠂'),(3881,1,'specifies','ᖵĉè©Ö᳇'),(3881,1,'statements','ℒȥዻ'),(3883,1,'directives','Ù'),(3884,1,'detailed','੯'),(3884,1,'directives','½Ľਉ'),(3885,1,'directives','ʙ'),(3886,1,'directives','Ɣ'),(3887,1,'detailed','ై'),(3888,1,'lib','ʔ'),(3890,1,'balanced','ڪ5'),(3890,1,'directives','ᶗ'),(3891,1,'directives','Ş'),(3765,6,'pcpmetric','è');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict49` ENABLE KEYS */;

--
-- Table structure for table `dict4A`
--

DROP TABLE IF EXISTS `dict4A`;
CREATE TABLE `dict4A` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict4A`
--


/*!40000 ALTER TABLE `dict4A` DISABLE KEYS */;
LOCK TABLES `dict4A` WRITE;
INSERT INTO `dict4A` VALUES (3871,1,'logfile','Ϣ'),(3869,1,'macintosh',';'),(3819,1,'authengineid','Ý'),(3871,1,'explained','ᙽ⒤ۧొ'),(3820,1,'authengineid',''),(3871,1,'blocks','⫛'),(3807,1,'logfile','#'),(3806,1,'logfile',';'),(3880,1,'ranked','㨫UKOǎY_IJChgƸ?BA'),(3880,1,'inclusion','ྰ'),(3879,1,'unassigned','⪉Ɯ'),(3879,1,'gateway','⿨'),(3877,1,'separation','͈'),(3872,1,'gateway','எ'),(3871,1,'win32','Ĥ'),(3880,1,'gateway','ឰ'),(3837,1,'poptimeout',''),(3856,1,'lh','Ŗ'),(3868,1,'correspondence','ā'),(3736,1,'av','	'),(3771,1,'maxssn',''),(3780,1,'nds','ú'),(3789,1,'logfile','q'),(3799,1,'logfile','Q'),(3805,1,'logfile','5'),(3880,1,'schedules','໓'),(3881,1,'blocks','❾'),(3881,1,'tservice','ઁ'),(3881,1,'upgrading','ⷞ'),(3888,1,'necessity','߾'),(3891,1,'gateway','Ħ'),(3771,6,'maxssn','1');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict4A` ENABLE KEYS */;

--
-- Table structure for table `dict4B`
--

DROP TABLE IF EXISTS `dict4B`;
CREATE TABLE `dict4B` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict4B`
--


/*!40000 ALTER TABLE `dict4B` DISABLE KEYS */;
LOCK TABLES `dict4B` WRITE;
INSERT INTO `dict4B` VALUES (3887,1,'escalation','̲ې		\n\n'),(3886,1,'escalation','˻\r'),(3884,1,'escalation','ٟз\n'),(3883,1,'lists','Ý'),(3882,1,'lists','Ó'),(3744,1,'lists','ƽ'),(3753,1,'programs','z'),(3754,1,'hd','ŝĉ'),(3759,1,'string2','B'),(3763,1,'lists','Ę'),(3819,1,'ifxtable','c'),(3819,1,'localized','Î'),(3820,1,'ifxtable','¸}'),(3820,1,'localized','u'),(3837,1,'gibe','Ç'),(3864,1,'atalkhost','\n'),(3868,1,'lists','Ù'),(3869,1,'rapid','·'),(3871,1,'acronym','3ㅘ'),(3871,1,'clicking','㳻'),(3871,1,'granularity','䘴'),(3871,1,'lists','㌶'),(3871,1,'processed','प'),(3871,1,'programs','㽝!r'),(3871,1,'technique','ᶶጇ'),(3872,1,'escalation','ࢇ'),(3872,1,'processed','؆'),(3874,1,'programs','Ŷ'),(3875,1,'programs','ŀ'),(3876,1,'programs','®'),(3878,1,'41','ܒ'),(3878,1,'lists','Ŝ'),(3879,1,'autodiscover','❢'),(3879,1,'escalation','℘̀\Zळو˦Ԇ	\rĕ/('),(3879,1,'hostcheck','䪢'),(3879,1,'lists','ᅭᵅ௢Ӄ˲հ'),(3879,1,'nagiosevent','䰅'),(3879,1,'processed','⥦├ډ'),(3879,1,'programs','͇ᨒ║є৩.ຶ'),(3879,1,'rapid','₪'),(3879,1,'section2chapter1','嵴ĺ8'),(3880,1,'clicking','᥄⸶'),(3880,1,'lists','ߚӋōቍȄÎᛃdULĩh[aL@kfġB?Bʖ'),(3880,1,'programs','ʱ๢㇨'),(3880,1,'validate','㢿'),(3880,1,'yellow','ᦁ:ࣹ'),(3881,1,'clicking','⽰ɰŨ'),(3881,1,'lists','૫'),(3881,1,'nagiosevent','᳼\rŅ'),(3881,1,'processed','⨸'),(3881,1,'programs','ޛᙙ5ۂ'),(3881,1,'validate','ᾗƹ'),(3721,1,'programs','ɀ'),(3721,1,'1986','ñ'),(3881,1,'yellow','⊸'),(3890,1,'lists','ᷙ'),(3883,6,'timeperiods','Ŏ'),(3890,1,'processed','ࣇԯࣄðȬÒ'),(3888,1,'lists','η'),(3888,1,'processed','Է'),(3889,1,'escalation','\')\n	8\r29\n		5	\r\n\"\r/\r	\r\r\n'),(3722,1,'validate','À');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict4B` ENABLE KEYS */;

--
-- Table structure for table `dict4C`
--

DROP TABLE IF EXISTS `dict4C`;
CREATE TABLE `dict4C` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict4C`
--


/*!40000 ALTER TABLE `dict4C` DISABLE KEYS */;
LOCK TABLES `dict4C` WRITE;
INSERT INTO `dict4C` VALUES (3881,7,'developer','㧂'),(3871,1,'left','ᎈȝǧԝ⭖'),(3871,1,'method','ம⫔ᜅ'),(3744,1,'extended','Ĺ'),(3735,1,'alert','H'),(3721,1,'embed','Ƅ'),(3880,1,'storage','৷⫳'),(3873,1,'developer','Wċ'),(3872,1,'info','ȿ'),(3872,1,'alert','୦>'),(3885,1,'left','ʕ\r'),(3884,1,'info','[קњࠈ	'),(3879,1,'corrections','٦'),(3879,1,'c6','䵕'),(3874,1,'alert','Ȫ&# '),(3880,1,'developer','ϣ㐸౧Ï	'),(3880,1,'corrections','٩'),(3871,1,'info','̮ᴡ݂'),(3854,1,'absolute','k'),(3869,1,'embed','÷'),(3871,1,'1500','䣿'),(3871,1,'absolute','࢒͂eǋд`໼Ӂ⇔yÜУ'),(3871,1,'adjustments','╍'),(3871,1,'fourtytwo','ᨘ'),(3871,1,'hiways','䘱'),(3846,1,'alert','&'),(3845,1,'alert','K'),(3843,1,'alert',':'),(3837,1,'info','Ă'),(3840,1,'nslookup','Ñ-(	\n8\r'),(3880,1,'themes','ࡋ'),(3808,1,'alert','Q'),(3879,1,'storage','ᔥ'),(3881,1,'storage','⢆Ξ'),(3884,1,'alert','ɡӍԾ'),(3879,1,'alert','َᷣஏ>ഝᖄŹලү'),(3879,1,'administering','ࠫϿ㎐'),(3880,1,'alert','୚ڶㅼ	ȡ'),(3879,1,'themes','ØۛõԾ!Ý#:'),(3881,6,'developer','㧃'),(3881,1,'corrections','ѻ'),(3881,1,'alert','ֻƶø'),(3878,1,'extended','϶'),(3877,1,'method','Խ'),(3876,1,'info','f'),(3875,1,'info',''),(3875,1,'extended','g,'),(3874,1,'info',''),(3874,1,'extended',''),(3891,1,'alert','þ>Ȃ'),(3890,1,'method','چ6ןӫĽ'),(3890,1,'left','Ƃ'),(3890,1,'info','༣<˟Ś'),(3890,1,'extended','Е'),(3888,1,'preserving','Ʃ'),(3888,1,'method','ŁÐʲ'),(3888,1,'left','ǰÅ'),(3888,1,'administering','̈'),(3887,1,'left','ɷ\"୐\"'),(3887,1,'info','_Ǘच\n\Z<\n˦3m!\n'),(3757,1,'alert','Ŭ'),(3754,1,'alert','ɦ'),(3885,1,'alert','ԇ'),(3885,1,'extended','ܬ'),(3881,2,'developer',''),(3891,1,'left','Ś\r'),(3765,1,'alert','9'),(3778,1,'alert','a'),(3791,1,'absolute',''),(3791,1,'alert','m'),(3803,1,'alert','O'),(3876,1,'extended','e'),(3885,1,'info','ܮ'),(3879,1,'method','ԁ$⇅Ⱊ#'),(3879,1,'left','ЙὌ᝔ᾶ'),(3879,1,'info','⎰Sතı)˥iŦ9.J፜'),(3879,1,'extended','⎯Sඬİ(ˣkŤ3.H	ਟτƗ͑'),(3879,1,'developer','хௌ䛶MȥĊ'),(3886,1,'left','Ɲ'),(3884,1,'extended','Wתѕࠇ	;'),(3871,1,'storage','ӓ-'),(3871,1,'rras','͌ǚ%Ӵᗹ߁༧'),(3881,1,'method','̔$ދ!Ìƌ(\rD9\\:7;77>;;P>H9:::88455\n\r5\'\"\Z\Z2L\n!?42?+©	ǭ !\nถ֒ǹÒ\nY֞ '),(3880,1,'method','Ԅ$㛧˿\Z¾ʭ'),(3880,1,'left','෪Z࿮ܗJᄽ࿥'),(3881,1,'left','᮴'),(3880,1,'extended','ߧ7'),(3887,1,'extended','[Ǚक	\Z<\n,wLǷ3m!\n'),(3887,1,'alert','݈'),(3881,1,'extended','⸦É'),(3881,1,'developer','\rōɸŶA៎');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict4C` ENABLE KEYS */;

--
-- Table structure for table `dict4D`
--

DROP TABLE IF EXISTS `dict4D`;
CREATE TABLE `dict4D` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict4D`
--


/*!40000 ALTER TABLE `dict4D` DISABLE KEYS */;
LOCK TABLES `dict4D` WRITE;
INSERT INTO `dict4D` VALUES (3787,1,'times','l'),(3763,1,'times',''),(3762,1,'times','y'),(3761,1,'times','ć'),(3757,1,'times',''),(3754,1,'pattern','TÎ'),(3755,1,'times','|'),(3756,1,'times','}'),(3874,1,'refer','ǝ'),(3879,1,'going','㭬?'),(3871,1,'slight','ᬷ'),(3878,1,'design','±'),(3854,1,'times','ī'),(3846,1,'times','ġ'),(3848,1,'times',''),(3871,1,'times','ਪҟȢӚข੩߃Ĺ˵ஃ5ѩ'),(3877,1,'times','с'),(3751,1,'times','Ɠ'),(3811,1,'times','¯'),(3818,1,'times','ć'),(3780,1,'times','š'),(3871,1,'refer','ध'),(3819,1,'iftype','~'),(3731,1,'times',''),(3732,1,'times','ą'),(3737,1,'times','Ą'),(3738,1,'times','Y'),(3739,1,'times','Ą'),(3744,1,'times','ŵ'),(3745,1,'times','Å'),(3748,1,'times','ć'),(3729,1,'times',''),(3840,1,'times','±%±'),(3879,1,'design','૫͜ᐚČ[ԣ҅'),(3878,1,'refer','࣒'),(3856,1,'times','à'),(3871,1,'autoscale','༧̸?'),(3871,1,'design','Ǯ'),(3720,1,'dropdowndatepicker','ƈ'),(3814,1,'times','ć'),(3781,1,'times',''),(3727,1,'times',''),(3720,1,'sliding','Ÿ'),(3871,1,'going','ࡎռ'),(3804,1,'times',''),(3856,1,'refer','í'),(3807,1,'pattern','\Z'),(3806,1,'pattern',',\''),(3879,1,'refer','б䛸ᔲ'),(3863,1,'times','Jk2('),(3867,1,'adptraid',''),(3840,1,'going','Ŝ'),(3837,1,'supposed','['),(3833,1,'times','ć'),(3831,1,'times',''),(3827,1,'times','2Ƭ'),(3822,1,'times','é'),(3820,1,'times','ÿ'),(3819,1,'times','č'),(3766,1,'times','¯'),(3728,1,'times','Ą'),(3782,1,'times','o'),(3871,1,'rrdlast','⦑'),(3871,1,'255','ᒢ'),(3805,1,'pattern','M'),(3783,1,'times','K'),(3784,1,'times','ć'),(3785,1,'times','ć'),(3871,1,'webserver','〆'),(3879,1,'relies','ᵖ'),(3879,1,'styles','崍'),(3879,1,'supposed','㧮'),(3879,1,'times','Ⓒ\n৷Ⴀ\n'),(3879,1,'webserver','ៈ'),(3880,1,'design','ቛ'),(3880,1,'going','ᔀ'),(3880,1,'refer','ε'),(3880,1,'relies','᝶'),(3880,1,'times','क़Ҭγ\nÝࡰʴ૝#W#ҧ#ŭ&٘'),(3881,1,'design','✪ˣ'),(3881,1,'discouraged','Ἕ'),(3881,1,'going','⺁ϱ'),(3881,1,'refer','ȱㄵȝŤɌM'),(3881,1,'stacks','⢍'),(3881,1,'webserver','㑆'),(3883,1,'times','ä'),(3884,1,'going',''),(3884,1,'obsess','΂਀'),(3884,1,'times','ʣEƚࠬEƚ'),(3885,1,'obsess','А'),(3885,1,'times','өh'),(3886,1,'design','Ǡ'),(3887,1,'design','Ʊ'),(3887,1,'obsess','َ'),(3887,1,'times','ܪh'),(3889,1,'times','Ǯ'),(3890,1,'going','˱'),(3890,1,'obsess','ᙴ\n%\'\n'),(3890,1,'times','༲'),(3867,6,'adptraid','9');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict4D` ENABLE KEYS */;

--
-- Table structure for table `dict4E`
--

DROP TABLE IF EXISTS `dict4E`;
CREATE TABLE `dict4E` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict4E`
--


/*!40000 ALTER TABLE `dict4E` DISABLE KEYS */;
LOCK TABLES `dict4E` WRITE;
INSERT INTO `dict4E` VALUES (3871,1,'subject','㍱'),(3871,1,'categories','⑾'),(3871,1,'mtm','Ⴟ'),(3871,1,'offset','ᦣণēd\rJ'),(3871,1,'short','૷\rী⭤'),(3879,1,'cmd','ᎃ'),(3879,1,'filing','ԧ\\9e'),(3799,1,'expires','_'),(3879,1,'historical','Ɏ惊*Ȕ+'),(3881,1,'short','ᐙጤ'),(3720,1,'changing','Ŋ'),(3720,1,'lfx',''),(3720,1,'panes','ź'),(3734,1,'short','¢'),(3752,1,'separated','E'),(3759,1,'separated','U'),(3781,1,'offset','\''),(3783,1,'separated','i'),(3788,1,'separated','c'),(3879,1,'subject','巀	\n\n'),(3879,1,'short','ⴇ'),(3879,1,'separated','❙Ƕᱭی'),(3879,1,'loops','我'),(3883,1,'subject',''),(3872,1,'short','ޱǃ¸)ȵ'),(3874,1,'subject',''),(3875,1,'separated','ʋ'),(3875,1,'subject',''),(3876,1,'subject',''),(3877,1,'essential','ý'),(3877,1,'terms','('),(3878,1,'ftp','ǻʜ\nȍ'),(3878,1,'terms','!'),(3887,1,'40x40','ʧ৬Ĝ'),(3879,1,'categories','墐ZM'),(3871,1,'5th','❐'),(3863,1,'ubuntu','Ĝ'),(3832,1,'ftp','\r'),(3833,1,'ftp',''),(3837,1,'subject','\"'),(3842,1,'ftp',''),(3856,1,'separated','¶'),(3856,1,'short','¼'),(3862,1,'terms','l'),(3881,1,'terms','*'),(3882,1,'subject',''),(3886,1,'subject',''),(3885,1,'subject',''),(3885,1,'short','ѳĿ£Ä'),(3881,1,'separated','Ც߃'),(3827,1,'separated','ơ'),(3889,1,'5th','̱'),(3890,1,'separated','Ѓ?I7XN'),(3888,1,'subject',''),(3879,1,'binaries','ᤗ'),(3798,1,'expires',''),(3884,1,'subject',''),(3884,1,'short','Ѧॲ'),(3881,1,'au','㒩'),(3880,1,'terms','*Ȩ䓰'),(3871,1,'terms','㙮'),(3789,1,'expires','83'),(3881,1,'offset','᜺v\r\rv\rE'),(3880,1,'historical','ः⯧ʟ$\\'),(3880,1,'filing','Ԫ\\9e'),(3880,1,'expires','⢄өƯ'),(3880,1,'changing','൏'),(3887,1,'subject',''),(3887,1,'changing','྆'),(3884,1,'changing','ం'),(3881,1,'historical','׹'),(3880,1,'categories','䒦'),(3881,1,'filing','̺\\9e'),(3884,1,'separated','჎'),(3824,1,'separated','Ď\''),(3819,1,'separated','{'),(3823,1,'separated',''),(3890,1,'reliability','ᕛ'),(3890,1,'historical','࠙'),(3890,1,'cmd','ͅď'),(3890,1,'changing','ᆀ'),(3889,1,'subject',''),(3804,1,'nag',' '),(3887,1,'short','ڴĿ£ÃÛح'),(3879,1,'terms','*ˌ'),(3879,1,'changing','ง«wN'),(3890,1,'short','ऽ߉Iփ¦Lȉ>Ņ'),(3890,1,'subject',''),(3891,1,'short','Ǝ9©ƠCô<'),(3891,1,'subject',''),(3892,1,'subject',''),(3833,6,'ftp','Ĩ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict4E` ENABLE KEYS */;

--
-- Table structure for table `dict4F`
--

DROP TABLE IF EXISTS `dict4F`;
CREATE TABLE `dict4F` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict4F`
--


/*!40000 ALTER TABLE `dict4F` DISABLE KEYS */;
LOCK TABLES `dict4F` WRITE;
INSERT INTO `dict4F` VALUES (3871,1,'4581333333e','⮏'),(3720,1,'firefox','Ē'),(3879,1,'firefox','þۛᄋÇQ!!* =\r'),(3871,1,'speed2','㪽ǲ'),(3871,1,'radiation','㉭'),(3871,1,'teatime','⏣'),(3871,1,'stands','៓ࢧั'),(3879,1,'agent','ᬑ'),(3877,1,'commented','ǣþ'),(3877,1,'agent',';'),(3873,1,'agent','%>\r>©'),(3872,1,'sbin','̾5},h'),(3871,1,'unsubscribe','㎊'),(3872,1,'notifications','ڎÒ«KÕ9?ǈ'),(3879,1,'avoids','⛴'),(3871,1,'division','䭌'),(3880,1,'company','ו͇เ'),(3880,1,'avoids','ᔤ'),(3879,1,'sbin','ᒓ'),(3879,1,'meet','࿥'),(3879,1,'notifications','ᶅڮ-\"-¥Ȍ؍	Êh\n˽{υƫ˿\"Ä0}Ä¼኎'),(3880,1,'firefox','䟔'),(3880,1,'notifications','̺ࠧ΁AŎTJi=ÞBSŲ߹͹\r	i¨Ţ԰\n	\n\n	\nǫ*~p5(eĪ*G׈§̒K\n\nJ9@#.. #$$\Z$@\n\n\nȃƐ'),(3871,1,'25m','⚚'),(3862,1,'oleg','M'),(3856,1,'agent','İ'),(3840,1,'company','À'),(3827,1,'useragent','Ɖ'),(3721,1,'company','\'\rŰ'),(3721,1,'stands','Ñ'),(3741,1,'agent','U'),(3742,1,'agent','g'),(3743,1,'agent','Q'),(3783,1,'agent',''),(3798,1,'stands','¨'),(3799,1,'stands','Ũ'),(3805,1,'confile','-'),(3819,1,'agent','`'),(3820,1,'agent','µ'),(3827,1,'agent','Ɣ'),(3720,1,'onkey','O'),(3879,1,'company','ג冇Ǔ6_Ę,¨'),(3881,1,'company','ϥ'),(3881,1,'excerpts','㕷'),(3881,1,'faulty','ᾦ'),(3881,1,'isobsessoverservice','ࡻ'),(3881,1,'notifications','ݦ'),(3881,1,'sbin','㔸'),(3881,1,'wishing','➁'),(3883,1,'notifications','¾'),(3884,1,'notifications','ϯ	fƠðڙ	f͂C'),(3885,1,'notifications','ע_#	'),(3886,1,'notifications','̕?'),(3887,1,'notifications','̀@қ_#	ݸ9'),(3889,1,'notifications','Ö\\!\'Qĳ)'),(3890,1,'notifications','ড؁\nע¸ֺ'),(3890,1,'rare','ᬏ'),(3890,1,'restrictive','ࢩ'),(3891,1,'notifications','Ƭ);f)9\ZNſC);9\Z'),(3892,1,'notifications','Ĳ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict4F` ENABLE KEYS */;

--
-- Table structure for table `dict50`
--

DROP TABLE IF EXISTS `dict50`;
CREATE TABLE `dict50` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict50`
--


/*!40000 ALTER TABLE `dict50` DISABLE KEYS */;
LOCK TABLES `dict50` WRITE;
INSERT INTO `dict50` VALUES (3881,1,'center','ʣh'),(3879,1,'classes','ᆴ'),(3879,1,'coded','⧘Ɯᠹ'),(3879,1,'gathered','ቂ〱'),(3879,1,'center','Ґh奀	'),(3871,1,'center','ョ'),(3871,1,'920806500','㠵|'),(3868,1,'eases','0'),(3869,1,'pm','È'),(3871,1,'12415','㠉;'),(3848,1,'record','^'),(3730,1,'record','L'),(3730,1,'ri','f'),(3750,1,'param2','-'),(3757,1,'generates',''),(3815,1,'generates','='),(3830,1,'pm',''),(3846,1,'generates','$'),(3878,1,'trapd','ƞ'),(3871,1,'wrapup','俚'),(3871,1,'record','஡K'),(3871,1,'pm','⏉'),(3725,1,'generates','!'),(3880,1,'eases','੯ଇ'),(3880,1,'center','ғh'),(3879,1,'section2chapter2','嵽ŎC'),(3879,1,'record','䴸ļ	đ\"ሪ'),(3880,1,'record','䊹'),(3880,1,'pm','Ӱ'),(3880,1,'interact','Ͳ䉋'),(3880,1,'generates','໛ⲱПɌ'),(3880,1,'gathered','ूŗ⥪Óෳ'),(3879,1,'generates','䛪ૼ'),(3879,1,'pm','ӭ'),(3871,1,'generates','䠄'),(3881,1,'classes','ӣϟʀ¨=ǃ'),(3881,1,'gathered','ۖ'),(3881,1,'interact','⬸࠮'),(3881,1,'pm','̀'),(3881,1,'record','๭@?A>@EBCW@\"\"\"˂ʼ©Ψܺ'),(3884,1,'pm','঄5'),(3887,1,'center','̬க'),(3888,1,'generates','˞'),(3888,1,'pm','Ȯa'),(3890,1,'pm','ų\Z'),(3892,1,'usern','');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict50` ENABLE KEYS */;

--
-- Table structure for table `dict51`
--

DROP TABLE IF EXISTS `dict51`;
CREATE TABLE `dict51` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict51`
--


/*!40000 ALTER TABLE `dict51` DISABLE KEYS */;
LOCK TABLES `dict51` WRITE;
INSERT INTO `dict51` VALUES (3790,1,'password','>'),(3791,1,'password','HS'),(3795,1,'password','&.'),(3874,1,'document','ZƆ'),(3856,1,'result','Ê'),(3737,1,'clean',''),(3734,1,'password','gG'),(3732,1,'result','ï'),(3732,1,'clean',''),(3731,1,'result',']\r'),(3729,1,'result',''),(3728,1,'result','î'),(3727,1,'result','s'),(3728,1,'clean',''),(3873,1,'accessing','&'),(3873,1,'password','čŋ='),(3784,1,'clean',''),(3784,1,'result','ñ'),(3819,1,'password','Ê\r'),(3791,1,'result','	'),(3792,1,'password',','),(3724,1,'result','Z'),(3787,1,'result',''),(3790,1,'accessing','7	'),(3865,1,'result','M'),(3745,1,'result','¯5'),(3748,1,'clean',''),(3748,1,'result','ñ'),(3751,1,'result','P%'),(3753,1,'v3','i'),(3754,1,'result',' 	\n	\n	\n'),(3755,1,'result','fA'),(3756,1,'password','-/A	\"'),(3760,1,'password',','),(3761,1,'clean',''),(3761,1,'result','ñ'),(3763,1,'password','a'),(3763,1,'result','m'),(3766,1,'result',''),(3773,1,'password','\"'),(3774,1,'password','!'),(3775,1,'password','!'),(3776,1,'password','T'),(3776,1,'result','r\n\n\Z\n'),(3744,1,'password','¼'),(3740,1,'password','3'),(3721,1,'accessing','Ț'),(3721,1,'features','ǌ'),(3827,1,'result','ǎA'),(3778,1,'password','h'),(3777,1,'password','\"'),(3848,1,'result',''),(3794,1,'password',''),(3793,1,'password','+6'),(3744,1,'result','ČÁ'),(3720,1,'features','#'),(3854,1,'result','ĕ'),(3854,1,'clean','µ'),(3785,1,'clean',''),(3844,1,'password','-1'),(3827,1,'document','ġ'),(3868,1,'features',''),(3861,1,'password',')'),(3857,1,'result','J'),(3737,1,'result','î'),(3739,1,'clean',''),(3831,1,'qstat','C*'),(3797,1,'password','6'),(3796,1,'password',' '),(3820,1,'password','q\r'),(3818,1,'result','ñ'),(3818,1,'clean',''),(3814,1,'result','ñ'),(3814,1,'clean',''),(3799,1,'result',''),(3805,1,'password','Á'),(3811,1,'password','+S'),(3811,1,'result',''),(3739,1,'result','î'),(3720,1,'i18n','GĽ'),(3827,1,'password','Ƃ'),(3780,1,'result','ń'),(3833,1,'clean',''),(3833,1,'result','ñ'),(3837,1,'password',''),(3785,1,'result','ñ'),(3782,1,'result','X'),(3782,1,'password','L'),(3871,1,'spec','Ĩⳗ'),(3871,1,'result','ā݃̄ৰȻ*ÁŹ¿8ވበ׆׋ʕĻՉ'),(3871,1,'password','㺋ǻ'),(3871,1,'features','%sLי'),(3871,1,'document','〹Wēþų˧ݭǖ௅ϙÀ'),(3869,1,'features','D'),(3870,1,'accessing',''),(3870,1,'features','lb'),(3877,1,'document',' '),(3877,1,'password','ƩÎ\r͢ĭ'),(3878,1,'document','ђ6785*770'),(3879,1,'479908','ằ'),(3879,1,'70700','ả'),(3879,1,'accessing','࠻Ұ㗟႟ߪ'),(3879,1,'dictates','奖'),(3879,1,'document','ڮᜅ㦃+\"\r9©%;-ţå '),(3879,1,'features','ˣЈͦ'),(3879,1,'password','ӁΛ+\n		оӠviI㻦Ⴓy	12	'),(3880,1,'accessing','Ũ֡μ*L቎◒'),(3880,1,'document','А䃱ƞ	¶2&'),(3880,1,'features','ɂȍ˃ÞΕþ<˳)ց Ŏùࣕᛥ'),(3880,1,'password','ӄۛ \Z Xڱ'),(3880,1,'result','ⓔۜ\nҠ\nࣲ'),(3881,1,'accessing','㡖¿Y'),(3881,1,'clean','ㅵ'),(3881,1,'destructor','പ'),(3881,1,'document','Ꭽఫ'),(3881,1,'features','ƴ╭'),(3881,1,'password','˔঱'),(3881,1,'result','ૐໍ>'),(3881,1,'setoperation','᫄¥'),(3884,1,'externals','Ŷߊ#\r\n'),(3884,1,'lose','௮'),(3884,1,'result','ͮ਋'),(3885,1,'lose','࠙'),(3887,1,'result','઄˨'),(3890,1,'archived','೛'),(3890,1,'externals','Át'),(3890,1,'result','ँ'),(3890,1,'stdio','ʪ'),(3891,1,'result','ǂÊɉC');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict51` ENABLE KEYS */;

--
-- Table structure for table `dict52`
--

DROP TABLE IF EXISTS `dict52`;
CREATE TABLE `dict52` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict52`
--


/*!40000 ALTER TABLE `dict52` DISABLE KEYS */;
LOCK TABLES `dict52` WRITE;
INSERT INTO `dict52` VALUES (3892,1,'field','ĸ'),(3880,1,'warnings','ᩜḇ̒бɖ'),(3820,1,'choice','Y'),(3820,1,'sparingly','Ģ'),(3820,1,'supports','¶'),(3827,1,'tests',''),(3727,1,'tests',''),(3879,1,'supports','˨'),(3880,1,'planned','ᓪఛ'),(3819,1,'choice','²'),(3818,1,'tests',''),(3814,1,'tests',''),(3721,1,'supports','ȺC'),(3720,1,'tests',',L'),(3831,1,'tests',''),(3831,1,'field',')-'),(3879,1,'tabs','ᗕ'),(3879,1,'implemented','∞\rѸᥢ'),(3833,1,'tests',''),(3871,1,'supports','ᦲ'),(3871,1,'warnings','Ũ'),(3872,1,'supports','Ϯ,'),(3879,1,'choice','✤'),(3879,1,'arg1','㋏'),(3875,1,'arg1','¹\rà'),(3874,1,'arg1','µ\"÷;>6+'),(3755,1,'tests',''),(3756,1,'tests',''),(3759,1,'interger',''),(3761,1,'tests',''),(3763,1,'tests',''),(3784,1,'tests',''),(3785,1,'tests',''),(3793,1,'tests',''),(3803,1,'supports','!'),(3804,1,'implemented','O'),(3809,1,'tests',''),(3754,1,'warnings',''),(3751,1,'warnings','Â'),(3819,1,'supports','a'),(3880,1,'supports','ɇҫ൱ĉώ'),(3879,1,'markup','垠'),(3873,1,'arg1','˨!\"!!\'\'%%%*\'*\'(%'),(3879,1,'deletes','⃊'),(3879,1,'htdocs','䄣ᄙ'),(3880,1,'142342424fc','ᮌ'),(3880,1,'field','ⳀѦਔрɥ'),(3880,1,'implemented','ݫ'),(3849,1,'tests',''),(3854,1,'tests',''),(3862,1,'routing','4'),(3868,1,'supports','('),(3869,1,'supports','Z'),(3871,1,'0000ff','Ḋ°ᬩǠÄ܊ֳ٩#'),(3871,1,'1020613500','⮳'),(3871,1,'978303300','䤍'),(3871,1,'pushes','᝱:\Z'),(3848,1,'tests',''),(3832,1,'arg1','!'),(3882,1,'htdocs','w'),(3882,1,'choice',''),(3881,1,'targetdata','ヘİ	'),(3881,1,'timedown','ࡏ'),(3881,1,'supports','ƹ'),(3881,1,'implemented','ߡW⚴ύU'),(3881,1,'htdocs','┤'),(3881,1,'field','᧵\"4ÈȘᗞ'),(3890,1,'progress','ạ'),(3890,1,'denial','͛'),(3889,1,'field','ɣ'),(3889,1,'6th','̷'),(3887,1,'field','ჯ'),(3748,1,'tests',''),(3745,1,'repeatedly','{'),(3740,1,'securityname','õ'),(3739,1,'tests',''),(3737,1,'tests',''),(3732,1,'tests',''),(3729,1,'tests',''),(3728,1,'tests',''),(3726,1,'arg1','<'),(3723,1,'tests',''),(3885,1,'tabs','ȱ'),(3884,1,'arg1','܂'),(3884,1,'tabs','ūࡤ'),(3879,1,'field','ᱪ಻W໴่ʹǋœuÒù,DC႓'),(3884,1,'field','ᇴ'),(3871,1,'rrdrestore','Ά8'),(3841,1,'arg1','¡'),(3843,1,'arg1','%'),(3829,1,'tests',''),(3879,1,'property','ᯎ᳄'),(3892,1,'supports','«');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict52` ENABLE KEYS */;

--
-- Table structure for table `dict53`
--

DROP TABLE IF EXISTS `dict53`;
CREATE TABLE `dict53` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict53`
--


/*!40000 ALTER TABLE `dict53` DISABLE KEYS */;
LOCK TABLES `dict53` WRITE;
INSERT INTO `dict53` VALUES (3880,1,'indicators','ᴄәŢѝ'),(3877,1,'hold','ө'),(3879,1,'103','䵹'),(3880,1,'scalable','᛭'),(3792,1,'sql','2'),(3791,1,'sql','9'),(3722,1,'resolve','NS'),(3721,1,'sql','·		\n	ē'),(3720,1,'feature','ĵ'),(3841,1,'arg5','¥'),(3871,1,'compression','ዱ'),(3871,1,'alt','༞ˌP?ƙ⡕'),(3877,1,'700','Ԁ'),(3872,1,'feature','˯'),(3871,1,'precompiled','㋎'),(3871,1,'feature','ѩ÷Ö,䟪'),(3878,1,'sql','ʟº҈'),(3877,1,'feature','ɍԥ'),(3881,1,'feature','Ԑ᜻-'),(3871,1,'700','䊱='),(3862,1,'gu','U'),(3862,1,'alt','¤'),(3880,1,'feature','ۈıÕj*ƭ4̍ޢ(ƚ»գʂĦሯ̿·୽П'),(3880,1,'expands','ᜑ⾮'),(3879,1,'alt','帹'),(3879,1,'feature','ޗᷦᑹ ֎ஞ'),(3879,1,'resolve','⢎'),(3879,1,'sql','揤C4V\r`s+8Ü'),(3881,1,'gethostgroupbyid','ᑀ'),(3881,1,'scalable','௜'),(3881,1,'sql','ൽ\rI߽ĉΤڔȞ'),(3884,1,'alt','ጷ'),(3884,1,'feature','ृl'),(3886,1,'resolve','ź'),(3887,1,'alt','ಱ'),(3887,1,'cube','̰க'),(3887,1,'resolve','Ƣ'),(3890,1,'feature','ŉጏ9Râ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict53` ENABLE KEYS */;

--
-- Table structure for table `dict54`
--

DROP TABLE IF EXISTS `dict54`;
CREATE TABLE `dict54` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict54`
--


/*!40000 ALTER TABLE `dict54` DISABLE KEYS */;
LOCK TABLES `dict54` WRITE;
INSERT INTO `dict54` VALUES (3844,1,'notes','¤'),(3820,1,'notes','đ'),(3755,1,'address','='),(3873,1,'notes','Ȝ'),(3864,1,'notes','['),(3731,1,'address','!'),(3730,1,'destination',''),(3872,1,'phone','զNܖ'),(3871,1,'radio','⽲\n'),(3840,1,'address','+\Z'),(3837,1,'poph','Ċ'),(3853,1,'address','3'),(3827,1,'address','@K\Z'),(3748,1,'address','\\'),(3730,1,'address','j'),(3729,1,'notes','¯'),(3879,1,'unifying','τ'),(3871,1,'rwwright','⦽'),(3871,1,'representation','‐৫'),(3752,1,'address','%'),(3743,1,'address','A'),(3866,1,'notes',''),(3822,1,'notes','~'),(3751,1,'notes','ě'),(3816,1,'notes','0'),(3778,1,'address','# '),(3778,1,'notes','¬'),(3780,1,'abends','v'),(3871,1,'synopsis','Ȓӊٿƨႜ`ZçهÕł7ʝ'),(3785,1,'address','\\'),(3784,1,'address','\\'),(3754,1,'notes','ƾ'),(3879,1,'graphhtmlref','則'),(3849,1,'address','1\r'),(3795,1,'address','6'),(3793,1,'notes',''),(3793,1,'address','C'),(3876,1,'address','ŋ'),(3871,1,'address','㎃ଓ'),(3873,1,'address','˃'),(3879,1,'walk','ᕣ䕩'),(3740,1,'notes','Ĥ'),(3729,1,'address','['),(3823,1,'address','\\'),(3837,1,'address',',9'),(3835,1,'notes','X'),(3834,1,'notes',''),(3834,1,'address',';'),(3833,1,'address','\\'),(3831,1,'notes',''),(3831,1,'address','$%'),(3830,1,'notes','V'),(3827,1,'notes','Ǵ'),(3827,1,'uri','B'),(3856,1,'address','F'),(3854,1,'address','`'),(3874,1,'notes','7Γ'),(3872,1,'monarch','࢟'),(3744,1,'address','I>'),(3744,1,'notes','ƅ'),(3857,1,'address','3'),(3856,1,'notes','ä'),(3879,1,'monarch','ᎱచHË྿၈ఁāǡ+ໂД$ '),(3871,1,'calculations','ᤣ(᮱ֹGÿߺҧг'),(3848,1,'address','9'),(3875,1,'notes','7Ȧ'),(3868,1,'monarch','\''),(3866,1,'address','a'),(3742,1,'address','B'),(3741,1,'address','E'),(3720,1,'js','4'),(3720,1,'declare','Į'),(3865,1,'address','<'),(3752,1,'notes','`'),(3871,1,'notes','ᱎėऽŏ'),(3871,1,'missed','㊱Ĕ'),(3824,1,'address','G\'M'),(3727,1,'notes',''),(3727,1,'address','S'),(3862,1,'notes','I'),(3879,1,'customize','ϼਖ਼'),(3872,1,'address','ثâĆ˞a8'),(3799,1,'notes','¸'),(3803,1,'notes',''),(3804,1,'address','4;'),(3804,1,'notes',''),(3809,1,'notes','P'),(3811,1,'address','F'),(3812,1,'js','*'),(3814,1,'address','\\'),(3798,1,'notes','¦'),(3791,1,'address','y'),(3788,1,'notes','¥'),(3776,1,'notes','Ĕ'),(3755,1,'notes',''),(3756,1,'address','G'),(3756,1,'notes',''),(3757,1,'notes','Ĩ'),(3761,1,'address','\\'),(3762,1,'address','$'),(3762,1,'notes','}'),(3763,1,'address','8'),(3763,1,'notes',''),(3764,1,'notes','.'),(3766,1,'address','J'),(3766,1,'notes','³'),(3879,1,'address','ኜ\\hHࠩୠ.		$ºd ,C\ZdÛ̢8શክÚČЍൖ'),(3877,1,'esc','Ǫþ'),(3877,1,'address','۾j'),(3876,1,'notes','7ũ'),(3879,1,'transitional','峲\n'),(3879,1,'s2chapter1b','廁'),(3879,1,'restricting','ᨫ'),(3879,1,'phone','ᔧ'),(3879,1,'pdf','۷'),(3879,1,'notes','°ǔ~͵N'),(3879,1,'nagios2db','彫,ŵģB'),(3739,1,'address','Y'),(3738,1,'address',':'),(3737,1,'address','Y'),(3736,1,'notes',''),(3733,1,'address','J'),(3732,1,'address','Z'),(3726,1,'notes','>'),(3721,1,'shopping','l'),(3722,1,'address','ĉ'),(3783,1,'notes','z'),(3728,1,'address','Y'),(3825,1,'address','@\''),(3818,1,'address','\\'),(3871,1,'750','Ա'),(3792,1,'notes','7'),(3745,1,'address','G?'),(3745,1,'notes','Õ'),(3815,1,'address','0'),(3872,1,'destination','ظ'),(3780,1,'address','E'),(3780,1,'notes','ť'),(3782,1,'notes','ŵ'),(3783,1,'address','2'),(3840,1,'notes','¶'),(3840,1,'alternatives','˘'),(3880,1,'address','௜ࣇϻ໪'),(3880,1,'calculations','䈶'),(3880,1,'chronic','㢞'),(3880,1,'customize','䓜«'),(3880,1,'notes','ɞŤ:J'),(3880,1,'pdf','ћ'),(3880,1,'phone','৻'),(3880,1,'proactively','पࡪཀᜀ'),(3880,1,'representation','ῌ͓'),(3880,1,'unifying','̮'),(3880,1,'walk','ਰᶬ'),(3881,1,'address','೙'),(3881,1,'customize','ȕ'),(3881,1,'declare','ਛ'),(3881,1,'monarch','ߦ'),(3881,1,'notes','ɑ‱'),(3881,1,'spring','῅'),(3882,1,'address','ß'),(3882,1,'monarch','x'),(3884,1,'monarch','ŃګƧ'),(3884,1,'notes','ዻ'),(3885,1,'radio','k'),(3886,1,'address','\'J		$'),(3887,1,'address','³(J		$'),(3887,1,'notes','௩\"'),(3888,1,'monarch','ʓ'),(3888,1,'walk','ե'),(3889,1,'monarch','['),(3890,1,'address','ܱᔐ&'),(3890,1,'monarch','Ĺ'),(3890,1,'userx','ࡺ'),(3891,1,'address','ê8');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict54` ENABLE KEYS */;

--
-- Table structure for table `dict55`
--

DROP TABLE IF EXISTS `dict55`;
CREATE TABLE `dict55` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict55`
--


/*!40000 ALTER TABLE `dict55` DISABLE KEYS */;
LOCK TABLES `dict55` WRITE;
INSERT INTO `dict55` VALUES (3878,1,'feeder','Ɨ'),(3834,1,'screen','J'),(3835,1,'screen','W'),(3766,1,'screen','B'),(3776,1,'schema','Ì'),(3866,1,'screen','s'),(3869,1,'manipulation','´'),(3862,1,'master','¦'),(3787,1,'screen','`'),(3763,1,'screen','0'),(3879,1,'describe','塵'),(3871,1,'describe','⪖'),(3725,1,'screen','8'),(3788,1,'screen','¤'),(3856,1,'screen','>'),(3836,1,'screen','V'),(3838,1,'screen','5'),(3840,1,'bbb','ȅ'),(3840,1,'screen','X'),(3833,1,'screen','T'),(3872,1,'screen','ޠƨǅů'),(3854,1,'screen','X'),(3853,1,'screen','E'),(3871,1,'bounded','ᬕ'),(3846,1,'screen','N'),(3848,1,'screen','A'),(3849,1,'screen','n'),(3863,1,'screen','>'),(3879,1,'4032','䙬ͷ'),(3871,1,'3weeks','✫'),(3789,1,'screen','Z'),(3791,1,'screen','O'),(3793,1,'screen',';'),(3795,1,'screen','.'),(3798,1,'screen','z'),(3799,1,'screen','J'),(3807,1,'screen','4'),(3809,1,'cload1','%'),(3809,1,'screen','.'),(3811,1,'screen','>'),(3814,1,'screen','T'),(3818,1,'screen','T'),(3824,1,'master',')\"\'_0\''),(3825,1,'master','\n\Z\'	 		\r'),(3827,1,'screen',''),(3829,1,'screen','a'),(3831,1,'screen','6'),(3785,1,'screen','T'),(3784,1,'screen','T'),(3782,1,'screen','x'),(3780,1,'screen','='),(3778,1,'screen','¥'),(3726,1,'screen','-'),(3727,1,'screen','K'),(3728,1,'screen','Q'),(3729,1,'screen','S'),(3731,1,'screen','7'),(3732,1,'screen','R'),(3736,1,'screen','4'),(3737,1,'screen','Q'),(3738,1,'screen','2'),(3739,1,'screen','Q'),(3740,1,'screen',''),(3742,1,'screen','y'),(3744,1,'screen',''),(3745,1,'screen','?'),(3746,1,'screen','&'),(3748,1,'screen','T'),(3754,1,'screen','Ƹ'),(3755,1,'screen','5'),(3756,1,'screen','?'),(3761,1,'screen','T'),(3762,1,'screen','8'),(3879,1,'feeder','䩭Żڭ\nr\nƊਥĕJ\nGc\ne/&\n\Z'),(3879,1,'schema','⦆?'),(3879,1,'screen','ࢺý®e1éɗMڌ­¸ၜƂDƃ ƙĘ­ǁa͕֟L1}?Uባŝூޓ'),(3879,1,'timestamped','ス'),(3880,1,'descending','㧚cVKƹg[bKAjgƷA@A'),(3880,1,'describe','ᤋ໧'),(3880,1,'residing','ݳ'),(3880,1,'screen','ܘӇ2\rÀ>࡛×ūƑ»Ŋé5\'ʡǈ5vl@ѫ୸ȹ,¶:'),(3881,1,'describe','ⷅ.Ć'),(3881,1,'feeder','ۣᚨ.Ä01äO)bpƾʙ'),(3881,1,'getrightchild','ᮾ'),(3881,1,'schema','♚'),(3881,1,'timestamped','ݐ'),(3882,1,'screen','f]ª'),(3883,1,'screen','H'),(3884,1,'screen','ÏR863բ{yAʧZyѫÀ\'ų¢'),(3885,1,'screen','VÒ¾6֖$)7NOY>'),(3886,1,'screen','XūLJ¥ŵ'),(3887,1,'screen','ĥ6Ɠ$ӳŗґ'),(3888,1,'screen','41آ#'),(3889,1,'screen','z̮'),(3890,1,'screen','­$³͙˃ބն֡Ǘ,*É¾$'),(3890,1,'usernames','Ї?IЎ'),(3891,1,'screen','â¾'),(3892,1,'screen','?'),(3892,1,'usernames',''),(3825,6,'master','Ŏ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict55` ENABLE KEYS */;

--
-- Table structure for table `dict56`
--

DROP TABLE IF EXISTS `dict56`;
CREATE TABLE `dict56` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict56`
--


/*!40000 ALTER TABLE `dict56` DISABLE KEYS */;
LOCK TABLES `dict56` WRITE;
INSERT INTO `dict56` VALUES (3871,1,'real','᥻ӂᨴԼЖІζɰ'),(3755,1,'real',''),(3722,1,'connecting','C'),(3827,1,'processing','ÿ'),(3828,1,'filesystemid1',''),(3832,1,'processing','3'),(3823,1,'ms','@'),(3721,1,'situations','Ź'),(3871,1,'nan','ḩɩ0!gડ౿,'),(3871,1,'answered','偢'),(3871,1,'1h30m','⓿'),(3871,1,'1020614400','⯠'),(3870,1,'2001',''),(3869,1,'processing','é'),(3864,1,'ms','i'),(3863,1,'combined','º2'),(3834,1,'ms',''),(3862,1,'arp','^'),(3822,1,'ms','¶'),(3797,1,'ms',''),(3762,1,'ms',''),(3877,1,'differently','Ը'),(3877,1,'connecting','ͺ'),(3871,1,'situations','׋'),(3880,1,'differently','␖'),(3879,1,'real','㿲'),(3879,1,'processing','䍔࠾'),(3754,1,'freshness','cı'),(3751,1,'real','{Ě'),(3741,1,'real',''),(3741,1,'combined',''),(3736,1,'real','§'),(3722,1,'ms','=7'),(3879,1,'prerequisites','㇨pǅRǼMؖąţL'),(3880,1,'processing','┰ʋ'),(3879,1,'combined','⋟ሕ'),(3879,1,'73','ấ'),(3877,1,'propagate','ڤ'),(3871,1,'differently','䊀'),(3871,1,'combined','ⓩ'),(3880,1,'real','ࠍóࡉũᇚጅ'),(3871,1,'processing','b'),(3881,1,'combined','᭯'),(3881,1,'connecting','ಞ'),(3881,1,'getaction','㋼'),(3881,1,'processing','⨢a'),(3881,1,'real','ױ¦'),(3881,1,'tokens','㤿'),(3884,1,'freshness','α	\n৒	\n'),(3884,1,'processing','ֹ৷'),(3885,1,'freshness','Э\n	'),(3885,1,'processing','ˍ'),(3887,1,'freshness','٬\n	'),(3887,1,'processing','ԋ'),(3887,1,'real','̀க'),(3888,1,'real','׉'),(3890,1,'freshness','ᬳ\r\n	\r\Z\n'),(3890,1,'processing','̨ᚨ77'),(3755,6,'real','É'),(3797,6,'ms','v');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict56` ENABLE KEYS */;

--
-- Table structure for table `dict57`
--

DROP TABLE IF EXISTS `dict57`;
CREATE TABLE `dict57` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict57`
--


/*!40000 ALTER TABLE `dict57` DISABLE KEYS */;
LOCK TABLES `dict57` WRITE;
INSERT INTO `dict57` VALUES (3879,1,'checking','➀㫼'),(3873,1,'matter','K'),(3872,1,'serial','©'),(3872,1,'checking','Ǝʹ'),(3871,1,'sysdescr','䁼'),(3879,1,'txt','۵'),(3879,1,'released','˳'),(3879,1,'offered','̵'),(3879,1,'numeric','䖺'),(3879,1,'matter','✢'),(3757,1,'flags','½'),(3754,1,'checking','ǁé'),(3751,1,'checking','X'),(3744,1,'checking','ș'),(3722,1,'spanning','v'),(3828,1,'checking',','),(3827,1,'numeric','¬'),(3827,1,'checking','ȣ'),(3804,1,'numeric',']'),(3800,1,'warnlevel','#'),(3880,1,'checking','⬎'),(3871,1,'checking','Ἑ'),(3871,1,'flags','ĸ'),(3849,1,'offered','B'),(3871,1,'rrggbb','ཱུ\Z׹׈C'),(3763,1,'nagiosplug','ĭ'),(3871,1,'semantics','ⳋ'),(3871,1,'series','A⊀'),(3871,1,'kicks','ƶ'),(3871,1,'numeric','๿'),(3765,1,'checking',''),(3791,1,'numeric','*'),(3778,1,'checking','Ñ'),(3776,1,'extents','¿\n'),(3838,1,'numeric',''),(3847,1,'warnlevel','0'),(3871,1,'920805300','㠥ᛗ'),(3870,1,'released','W'),(3868,1,'nagiosplug','ñ'),(3866,1,'checking','¡'),(3720,1,'semantics',''),(3721,1,'released','ÿ	'),(3880,1,'offered','ʟ'),(3880,1,'pollar','ᴹ'),(3880,1,'released','ɏ'),(3880,1,'series','्അɯӴ៰ʢ'),(3880,1,'txt','љ'),(3881,1,'checking','ℸ'),(3881,1,'destroys','ࣺ'),(3881,1,'matter','⼭'),(3881,1,'packagename','ⴗ'),(3885,1,'checking','ݿ'),(3886,1,'series','Â'),(3887,1,'series','ê'),(3890,1,'attempting','܎'),(3890,1,'checking','ഫхηS£ԬI'),(3890,1,'euro','ᯜ'),(3890,1,'reduced','ቫ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict57` ENABLE KEYS */;

--
-- Table structure for table `dict58`
--

DROP TABLE IF EXISTS `dict58`;
CREATE TABLE `dict58` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict58`
--


/*!40000 ALTER TABLE `dict58` DISABLE KEYS */;
LOCK TABLES `dict58` WRITE;
INSERT INTO `dict58` VALUES (3880,1,'write','r'),(3880,1,'telephone','Ձa'),(3880,1,'sections','ᖲㄻ'),(3880,1,'response','ԗΠ܍൝'),(3880,1,'hardware','Щַ'),(3884,1,'failure','ࡦओ\"q%'),(3884,1,'fail','ᆸr'),(3884,1,'contacts','ч2৙2ο'),(3881,1,'write','rᾘᑈ=ͅ'),(3881,1,'telephone','͑a'),(3881,1,'sections','▵'),(3864,1,'percentage','u'),(3857,1,'percentage','B'),(3856,1,'response','Ç'),(3856,1,'c1','Ř'),(3855,1,'ciscotemp','\r'),(3854,1,'response','ªP'),(3850,1,'hardware',''),(3848,1,'response',''),(3881,1,'response','ζ'),(3881,1,'incremented','ᵝ'),(3880,1,'contacts','ᅏ \rᩴUБUു\'Agѵ'),(3881,1,'failure','ೲ✛'),(3881,1,'apps','ⴝEö'),(3879,1,'arrow','௩ಢ'),(3878,1,'write','i'),(3877,1,'write','pĢȠÄ'),(3877,1,'fail','Ԉ'),(3877,1,'response','ت'),(3871,1,'divided','㩈ਠ׸'),(3871,1,'eye','὚'),(3871,1,'fail','䵶'),(3868,1,'sections','V'),(3871,1,'978302700','䤃'),(3871,1,'arrow','ᗉᬛ'),(3879,1,'telephone','Ծa'),(3880,1,'eye','៛'),(3881,1,'incomplete','ᾤ'),(3879,1,'write','r⃎ㆨN|ഞéix'),(3879,1,'fail','⢆ឪ'),(3782,1,'percentage','¸&'),(3784,1,'response','P'),(3785,1,'response','P'),(3811,1,'response',''),(3814,1,'response','P'),(3818,1,'response','P'),(3823,1,'apps','h'),(3823,1,'fail','º'),(3824,1,'apps','¡'),(3824,1,'response','k­\''),(3825,1,'response','d\r'),(3826,1,'hardware',''),(3827,1,'response','ïÜ?'),(3833,1,'response','P'),(3834,1,'percentage','©'),(3840,1,'fail','Ōü'),(3840,1,'response','Ȍ«'),(3844,1,'percentage','¿'),(3844,1,'workgroup','3'),(3780,1,'percentage','	'),(3879,1,'sections','娤Ģ'),(3876,1,'response','Ɠ'),(3874,1,'response','̯'),(3874,1,'percentage','͝'),(3872,1,'contacts','õݒ#*mŵ?ûe2'),(3871,1,'lg','ᩌ'),(3871,1,'percentage','؄'),(3871,1,'write','Ϻភíൺঅ'),(3879,1,'percentage','ᷥ'),(3879,1,'response','Ԕⱟផ'),(3879,1,'contacts','ŸᷨҬ\rʪŜӈSĕɟɄϧГÕ)\n9\">'),(3879,1,'hardware','ۉๅ'),(3879,1,'failure','ᰮ'),(3866,1,'response','C'),(3720,1,'safari','ì)'),(3722,1,'fail','Y'),(3722,1,'response','0þ'),(3727,1,'response','p'),(3728,1,'response','P'),(3729,1,'response','t'),(3731,1,'response','t\r'),(3732,1,'response','P'),(3737,1,'response','P'),(3739,1,'response','P'),(3745,1,'response','k,3'),(3748,1,'response','P'),(3749,1,'hardware',''),(3752,1,'failure','Á'),(3755,1,'response','\\?'),(3756,1,'response','q'),(3757,1,'percentage',''),(3761,1,'response','P'),(3762,1,'percentage',''),(3763,1,'response','j'),(3776,1,'percentage','¯'),(3885,1,'contacts','ػ/'),(3886,1,'fail','Ų'),(3887,1,'contacts','ࡼ/ࠄ'),(3887,1,'fail','ƚ༣'),(3887,1,'failure','ၼ%'),(3888,1,'fail','͖'),(3890,1,'contacts','Ҩᦸ'),(3890,1,'failure','͇'),(3890,1,'percentage','ག'),(3890,1,'write','Ī಄୦5  '),(3891,1,'contacts','>\r˫¥\r'),(3891,2,'contacts',''),(3824,6,'apps','Ť'),(3855,6,'ciscotemp','C'),(3891,6,'contacts','ُ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict58` ENABLE KEYS */;

--
-- Table structure for table `dict59`
--

DROP TABLE IF EXISTS `dict59`;
CREATE TABLE `dict59` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict59`
--


/*!40000 ALTER TABLE `dict59` DISABLE KEYS */;
LOCK TABLES `dict59` WRITE;
INSERT INTO `dict59` VALUES (3881,1,'installation','Ժ「ȓ'),(3881,1,'monitorserver','ঝᕗ'),(3881,1,'myappview','㠖'),(3881,1,'num','෌ֻ'),(3881,1,'redirected','㚎¡ɕ'),(3881,1,'top','ލᒗɁྲ'),(3885,1,'bypass','ԧ'),(3887,1,'bypass','ݨ'),(3887,1,'top','ʉ஑'),(3888,1,'installation','ʘ\r'),(3888,1,'top','Րȏ'),(3890,1,'installation','Ģ'),(3890,1,'top','಺တǚ'),(3892,1,'top','T'),(3744,1,'miblist','kj'),(3776,1,'num','='),(3782,1,'service2','ė'),(3787,1,'9','o'),(3798,1,'webtools','¿'),(3799,1,'webtools','ƀ'),(3822,1,'unsupported','ñ'),(3827,1,'bypass','±'),(3827,1,'redirected','Ƹ'),(3837,1,'num',''),(3856,1,'tracks','Ħ'),(3862,1,'installation','Û'),(3869,1,'9','>'),(3869,1,'interpreter',']'),(3871,1,'7333333333e','⯳'),(3871,1,'9','ߌளኖ␍'),(3871,1,'canvas','ᖴ'),(3871,1,'interpreter','Ⲁ'),(3871,1,'limited','Ȿ'),(3871,1,'myspeed','㤳ŠB´'),(3871,1,'num','➌'),(3871,1,'polish','ᛛ␫'),(3871,1,'top','ᖷ˜,ʈ⥙ã'),(3872,1,'9','ɦʱ'),(3872,1,'installation','ĩÔ'),(3872,1,'num','ʱ'),(3874,1,'installation','\'ř'),(3875,1,'installation','\'ĩ'),(3876,1,'installation','\''),(3877,1,'9','Ȗ'),(3877,1,'installation','ă'),(3878,1,'installation','Ĉ'),(3879,1,'9','ƈᷨἋ'),(3879,1,'bypass','៟'),(3879,1,'installation','ڂ		પత˅WⅰĝතӼ:eє'),(3879,1,'replicated','䠈'),(3879,1,'top','ఙ˥࠾㈱ကͼń'),(3880,1,'conservative','⑙'),(3880,1,'installation','Ͽ	㿘'),(3880,1,'top','෩ᖓሎƜı\n\n¾\n[	K\nC	¬\n&\n\n\\N\rS96\nb	]	³\n\n\n\'\n\nf\n9	5\n9	Ö\nĲ΃');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict59` ENABLE KEYS */;

--
-- Table structure for table `dict5A`
--

DROP TABLE IF EXISTS `dict5A`;
CREATE TABLE `dict5A` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict5A`
--


/*!40000 ALTER TABLE `dict5A` DISABLE KEYS */;
LOCK TABLES `dict5A` WRITE;
INSERT INTO `dict5A` VALUES (3882,1,'groups','ł'),(3885,1,'groups','ȺҹǑFQ'),(3884,1,'20','ܛ\r'),(3884,1,'groups','i׍ʰƟָ̓ '),(3881,1,'symbolic','╽'),(3881,1,'systems','ƪіЫ᷈௬'),(3881,1,'feeding','àѤDz᝴'),(3877,1,'systems','ê0!ß'),(3878,1,'20','Α'),(3878,1,'systems','¹'),(3879,1,'20','䵣'),(3879,1,'groups','ƪᷨ˂Ş?N\Z_ȆĤC	μđ\"\ZΏ~ò}4	1ŇxpV	\"ɘ6Ė\r 	ø5!ªs\nĠ\r\'	õলᔲ'),(3880,1,'groups','ၯ\r0	!D´Hı֬åw¤\Zˋ	@Ր#Ĥřઠ¹\'\n¿ͲŞw\n?Ǳt\nƗʈŰ'),(3880,1,'affect','໷'),(3879,1,'systems','˛ሹଚؓ'),(3879,1,'symbolic','嚍ƣ'),(3879,1,'served','᪢'),(3881,1,'affect','ᾲ'),(3880,1,'systems','ȺիɁֳ╢'),(3881,1,'served','ⲽ'),(3881,1,'groups','׉Ļƚě੄'),(3880,1,'migration','ᕸ'),(3840,1,'affect','ı'),(3853,1,'systems',''),(3863,1,'systems',''),(3866,1,'20','«'),(3868,1,'migration','2'),(3869,1,'systems','7i'),(3870,1,'dozens','{'),(3871,1,'1000007','䮆t'),(3871,1,'20','စ⟣ȹᅝ~'),(3871,1,'999980','䮧P'),(3871,1,'affect','ዒ'),(3871,1,'attaching','٨'),(3871,1,'compensate','㮀'),(3871,1,'doors','䠎'),(3871,1,'highest','↫ġ'),(3871,1,'symbolic','ᖰ'),(3871,1,'systems','䀣'),(3871,1,'travis','ቂ'),(3872,1,'groups','ࢂ·i'),(3873,1,'20','K'),(3873,1,'systems','5'),(3874,1,'20','ğǶ'),(3831,1,'xq','´'),(3825,1,'codeferred','ç'),(3822,1,'systems','Ø'),(3765,1,'pilot',''),(3757,1,'20','Ɠ'),(3744,1,'20','ȱ'),(3721,1,'systems','Ɖ'),(3721,1,'legal','Ʀ'),(3885,1,'20','Û'),(3886,1,'groups','/ɛ='),(3887,1,'groups','Ƞ	ܟ\'\'VD'),(3887,1,'transparency','ഔ'),(3888,1,'groups','\ZAj	\nJǖi$*\rU	ÿ9Â'),(3889,1,'groups',';\n \''),(3890,1,'affect','ᰨ'),(3891,1,'groups','̰§'),(3888,2,'groups',''),(3888,6,'groups','ऐ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict5A` ENABLE KEYS */;

--
-- Table structure for table `dict5B`
--

DROP TABLE IF EXISTS `dict5B`;
CREATE TABLE `dict5B` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict5B`
--


/*!40000 ALTER TABLE `dict5B` DISABLE KEYS */;
LOCK TABLES `dict5B` WRITE;
INSERT INTO `dict5B` VALUES (3880,1,'situation','⑎'),(3872,1,'change','Ǟ'),(3872,1,'communicate','Ӂ'),(3872,1,'privilege','˵'),(3872,1,'sources','ۮ'),(3879,1,'elemental','͆'),(3879,1,'cvs','䰴ª'),(3879,1,'tktauthcookiename','൜I'),(3879,1,'routers','㟳3͏'),(3879,1,'reduce','↷ᕳ࣠'),(3879,1,'ignored','侩'),(3887,1,'change','Ӥ1ɋǟǬ'),(3887,1,'adds','Џ\n'),(3880,1,'change','๠̄ƾƙRऐ<FȦΦkČ0'),(3880,1,'aggregated','ᩗ'),(3880,1,'routers','ት'),(3880,1,'ignored','Ⱆҟ'),(3871,1,'notation','ᛜ␘'),(3871,1,'reduce','⺕'),(3871,1,'sources','ǇׁǾС \r[൴ඞࢣ¹ങ	'),(3881,1,'sources','Ӓ⏗'),(3881,1,'reduce','Ԕᝨ'),(3879,1,'w3c','垟Վ'),(3871,1,'stay','䢝'),(3880,1,'communicate','㋘'),(3880,1,'elemental','ʰ'),(3877,1,'privilege','ɯ'),(3878,1,'change','ı߂	\n\r'),(3878,1,'communicate','ǔ \"  \"\"\Zʳ33522220030%/'),(3879,1,'adds','⃇'),(3879,1,'change','ॏ	ͬIĥcWŌIિÜ0ظੴǕÙ(Ҫࢰਧϖ৬Õ઩ʐ\n'),(3886,1,'change','o'),(3877,1,'change','ǎ'),(3877,1,'245','ؖ'),(3873,1,'change','ƈ$'),(3881,1,'pull','Ⴏ'),(3881,1,'persistence','ṙŏ'),(3881,1,'guavaapplication','⸧v(iuČľ'),(3881,1,'communicate','ม᱿'),(3881,1,'change','௏ᆚೱت\\ŀĞʻ'),(3880,1,'stay','ᯯ'),(3880,1,'sources','ཽ⤄'),(3885,1,'change','Ăļ1Ɉ'),(3884,1,'change','ưĻŰ¯P/ؽİŰ¯P/ƶʩ'),(3883,1,'change','¤'),(3881,1,'tktauthcookiename','㙙Q*ƴ\\'),(3871,1,'ignored','ᙦ'),(3871,1,'hexadecimal','㏣לቸ'),(3871,1,'fetched','ⅱ'),(3871,1,'change','͂ᬠᳳ౩'),(3871,1,'adds','䱓'),(3863,1,'ignored','Ë'),(3849,1,'dhcpoffer','P'),(3837,1,'stat','¸'),(3721,1,'adds','È'),(3721,1,'change','Ŗ'),(3730,1,'routers','$'),(3734,1,'ignored',''),(3751,1,'constrain',''),(3752,1,'situation','y'),(3754,1,'change','Ⱥ'),(3756,1,'sources','³'),(3759,1,'ignored','ö'),(3803,1,'stat','¹'),(3822,1,'ignored','v'),(3888,1,'change','í޻'),(3889,1,'change','ϯ'),(3890,1,'aggregated','ࣗb\r!'),(3890,1,'change','ќ֓\Z\ZU\Z\ZY\ZZ\ZјIŤથ*'),(3891,1,'change','¯̈Æ'),(3892,1,'change','I');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict5B` ENABLE KEYS */;

--
-- Table structure for table `dict5C`
--

DROP TABLE IF EXISTS `dict5C`;
CREATE TABLE `dict5C` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict5C`
--


/*!40000 ALTER TABLE `dict5C` DISABLE KEYS */;
LOCK TABLES `dict5C` WRITE;
INSERT INTO `dict5C` VALUES (3890,1,'previous','ቼࡾ'),(3889,1,'previous','͐'),(3721,1,'freshmeat','ʑ'),(3879,1,'ticket','ҏU2e'),(3879,1,'require','ുI₣'),(3888,1,'previous','ף'),(3888,1,'centrally','Ǒ'),(3884,1,'previous','ǐ'),(3882,1,'discover','é'),(3881,1,'ticket','ʢU2e㉹ǯ'),(3881,1,'require','ᬆပଳ%Ȋ'),(3881,1,'previous','ڪҏªޮࢨ'),(3880,1,'green','ᦈF᦮'),(3880,1,'require','Ꮜй₠'),(3880,1,'ticket','ҒU2e'),(3881,1,'geteventsfordevice','ৣ്'),(3890,1,'rescheduling','ᐖX.\r3'),(3730,6,'traceroute',''),(3880,1,'extensible','ཱུ'),(3721,1,'evolving','ï'),(3881,1,'green','⊴'),(3881,1,'io','⎶'),(3722,1,'logon','[7'),(3730,1,'traceroute','M'),(3751,1,'sense','ʒ'),(3762,1,'traceroute','¼'),(3780,1,'dirty',''),(3822,1,'require','õ'),(3828,1,'mounted','H'),(3841,1,'remember',''),(3846,1,'mounted',' '),(3856,1,'logon','{'),(3869,1,'extensible',''),(3871,1,'920807700','㡁|'),(3871,1,'aligned','ൻཀ'),(3871,1,'apr','▎'),(3871,1,'cdp','℄'),(3871,1,'dirty','㲟'),(3871,1,'discover','俻'),(3871,1,'green','㦰\"'),(3871,1,'previous','ᤛ\'ųļᤆ\r'),(3871,1,'remember','᫋᭝ৼขǽ'),(3871,1,'require','ଈ'),(3871,1,'sense','䝽'),(3871,1,'ticket','䒖'),(3872,1,'remember','͕)/'),(3872,1,'require','Ȁ'),(3874,1,'require','Ơ'),(3875,1,'discover','ōġ0'),(3875,1,'require','Ű'),(3876,1,'require','Ò'),(3877,1,'remember','ڗ»'),(3878,1,'43','ݶ'),(3879,1,'43','䶁'),(3879,1,'delegation','⚴'),(3879,1,'discover','╓Ȍ̳j'),(3879,1,'mounted','ᷝ~'),(3879,1,'previous','ศ½㓀ࠪ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict5C` ENABLE KEYS */;

--
-- Table structure for table `dict5D`
--

DROP TABLE IF EXISTS `dict5D`;
CREATE TABLE `dict5D` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict5D`
--


/*!40000 ALTER TABLE `dict5D` DISABLE KEYS */;
LOCK TABLES `dict5D` WRITE;
INSERT INTO `dict5D` VALUES (3871,1,'900','⊵8\r◄ũӎL'),(3843,1,'login','F'),(3873,1,'recieveq','Ц'),(3879,1,'15am','⻯'),(3834,1,'limit','='),(3829,1,'project','B'),(3808,1,'login','+*'),(3804,1,'trouble','¦'),(3791,1,'login',''),(3778,1,'login','\'3'),(3763,1,'login','\\'),(3763,1,'determine',''),(3762,1,'crta',')'),(3879,1,'determine','⓷᫨ธХ๽'),(3879,1,'900','䙞ƒǥ'),(3879,1,'background','専'),(3871,1,'tend','ࢣ'),(3845,1,'login','4\"'),(3846,1,'limit','6\n'),(3859,1,'gi','#'),(3864,1,'crta','-'),(3871,1,'1h','⊷q߅'),(3721,1,'favorite','ɹ'),(3727,1,'determine',''),(3871,1,'stdout','ᩀ̼Ϲ'),(3871,1,'pseudo','㵩ֽ'),(3871,1,'plural','ⓕ'),(3871,1,'manner','Ʊ̒S᫂'),(3871,1,'limit','ཚր)	\Z̽ၣ♬'),(3871,1,'favorite','❣ଛݽ'),(3871,1,'determine','㹑'),(3871,1,'background','ཌԤৠ@ᒗ'),(3873,1,'project','O?\n?'),(3872,1,'determine','ɗ˽'),(3730,1,'stdout','R'),(3871,1,'6hours','✼'),(3879,1,'synchronization','抍'),(3879,1,'suffixed','ᛂ'),(3881,1,'background','⭸'),(3880,1,'serves','ฉᆿ͓'),(3880,1,'trouble','ґḒ'),(3879,1,'trouble','Ҏ'),(3880,1,'determine','ዬෂ'),(3880,1,'drill','ᘢǁ\"4¬ƃɉ[\nrǳc\nS	ŋW\nUĪȜǻ୚'),(3877,1,'login','ŗėɔȬ'),(3757,1,'limit','7'),(3754,1,'determine','ȕuu'),(3744,1,'limit','Ȝ'),(3735,1,'login','1#'),(3880,1,'pinpoint','㔞'),(3880,1,'login','ӂK׀ÈbQHHWۈFў'),(3879,1,'project','ွ'),(3879,1,'manner','䟔'),(3879,1,'login','ҿKшƇɳIĪ㐅ℂ+¬?'),(3879,1,'limit','ᝄۀ'),(3881,1,'determine','ኛÜ'),(3881,1,'filterview','޺'),(3881,1,'heterogeneous','ؒ∈'),(3881,1,'initialization','ῐཕ#Ȏ'),(3881,1,'limit','᧗'),(3881,1,'loggername','࢜'),(3881,1,'login','˒K㌺OŒĂ'),(3881,1,'manner','ٰᑽ'),(3881,1,'project','Ɱ'),(3881,1,'stdout','૒'),(3881,1,'tend','⠤'),(3881,1,'tie','⥐ņ'),(3881,1,'trouble','ʡ'),(3883,1,'15am','Ě'),(3884,1,'determine','̜a0LY|#3ܝa0LY|#3Ŵ'),(3885,1,'determine','ˈ$34$9/ĭ[£'),(3887,1,'determine','Ԇ$34$:/į[£޺'),(3889,1,'determine','ŕ¬'),(3889,1,'limit','ǔ'),(3890,1,'background','ظ'),(3890,1,'manner','ϢᝰO');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict5D` ENABLE KEYS */;

--
-- Table structure for table `dict5E`
--

DROP TABLE IF EXISTS `dict5E`;
CREATE TABLE `dict5E` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict5E`
--


/*!40000 ALTER TABLE `dict5E` DISABLE KEYS */;
LOCK TABLES `dict5E` WRITE;
INSERT INTO `dict5E` VALUES (3871,1,'speed3','㮝ĝ'),(3872,1,'17','Ƭ'),(3869,1,'critical',''),(3873,1,'critical','Ĥ'),(3789,1,'substring','x'),(3865,1,'critical','C'),(3866,1,'critical',''),(3725,1,'critical','M'),(3727,1,'critical','yU'),(3728,1,'critical','&Î'),(3729,1,'critical','\'h1'),(3731,1,'critical','c\n'),(3732,1,'critical','&Ï'),(3734,1,'critical','Ü'),(3735,1,'critical','J'),(3736,1,'critical','\\\r'),(3737,1,'critical','&Î'),(3739,1,'critical','&Î'),(3740,1,'critical','v©'),(3741,1,'critical',''),(3743,1,'critical','l'),(3744,1,'critical','Ē'),(3745,1,'critical','µ$'),(3748,1,'critical',')Î'),(3751,1,'critical','z	UZ\r	\në'),(3752,1,'critical','x=('),(3754,1,'critical','$\n	Ijí'),(3755,1,'critical','l0'),(3757,1,'critical','Jï'),(3759,1,'critical','5v'),(3761,1,'critical',')Î'),(3761,1,'pop',''),(3762,1,'critical','W8'),(3763,1,'critical','%N'),(3764,1,'critical',','),(3765,1,'critical','A'),(3766,1,'critical','9f'),(3776,1,'critical','d	'),(3778,1,'critical','/'),(3780,1,'critical','4Ė?'),(3723,1,'critical','8'),(3879,1,'handlers','ቈ଻ܙᨉӍ'),(3844,1,'critical','};	'),(3843,1,'critical','<'),(3840,1,'critical',' '),(3837,1,'critical','×\r'),(3835,1,'critical','w'),(3834,1,'critical','j7'),(3880,1,'critical','գഋƑʬȨ\nƴĤNȬʭ·«ȼᏳ'),(3874,1,'400','¤Ɨ'),(3872,1,'pager','ĈτÚ&g·π\n		'),(3872,1,'critical','ਞ'),(3787,1,'critical','7'),(3871,1,'gprint','྇୭cƼ'),(3871,1,'pop','ᡣR ՘t'),(3859,1,'critical','1'),(3880,1,'fitness','R'),(3879,1,'pager','⿐	'),(3879,1,'intermediate','㠥'),(3871,1,'decision','Ỡ'),(3857,1,'critical','L'),(3871,1,'brown','ቃ'),(3871,1,'400','Ꭻᛓầ'),(3786,1,'critical','6'),(3785,1,'critical',')Î'),(3788,1,'critical','|'),(3872,1,'handlers','ڑĪ\''),(3871,1,'regarded','ऍÌọ'),(3833,1,'critical',')Î'),(3722,1,'critical','é'),(3845,1,'critical','M'),(3863,1,'critical','õ)'),(3863,1,'upgrade','O0'),(3864,1,'critical','J#'),(3813,1,'critical','Z\r'),(3814,1,'critical',')Î'),(3815,1,'critical','D	'),(3816,1,'critical','D'),(3818,1,'critical',')Î'),(3820,1,'critical','à'),(3822,1,'critical','B'),(3824,1,'critical','R='),(3825,1,'critical','K#'),(3827,1,'critical','I\nŠ!03'),(3811,1,'critical',''),(3800,1,'critical','='),(3798,1,'critical','H$4'),(3791,1,'critical','s'),(3879,1,'critical','ՠᠥYὙݱ'),(3879,1,'correctly','䣦'),(3878,1,'fitness','I'),(3878,1,'17','͐'),(3877,1,'useradd','ƅ'),(3877,1,'fitness','P'),(3877,1,'correctly','͉'),(3876,1,'critical','ş'),(3874,1,'critical','ȾA93-'),(3879,1,'upgrade','ڷ'),(3880,1,'correctly','ఄ[ྪ'),(3879,1,'statusmap','㡡'),(3879,1,'pop','㈅'),(3879,1,'panel','␈'),(3784,1,'critical',')Î'),(3782,1,'critical','2,F-'),(3879,1,'fitness','R'),(3856,1,'critical','Ð'),(3854,1,'critical',',ï'),(3848,1,'critical','2V'),(3847,1,'critical','L'),(3846,1,'critical',''),(3828,1,'critical','84'),(3781,1,'critical','O'),(3803,1,'critical','E	'),(3803,1,'trusteduser','Ê'),(3808,1,'critical','J'),(3809,1,'critical','C'),(3810,1,'critical','!'),(3799,1,'critical','9V	5'),(3880,1,'handlers','༈ɬ?Đග>\rጔ'),(3880,1,'pager','༮'),(3880,1,'pop','ၖᒊ'),(3880,1,'upgrade','Йდఔ'),(3881,1,'changethistosomethingunique','㗗Ƣ'),(3881,1,'critical','ͳ'),(3881,1,'fitness','R'),(3884,1,'critical','Ҳɶސ̊9\\'),(3884,1,'preserves','ૺ'),(3887,1,'statusmap','೗a\') ,'),(3890,1,'critical','ݺ'),(3890,1,'handlers','బŏʇغֽ'),(3890,1,'pager','ᱣ'),(3890,1,'statusmap','Ǧё1\rÃ'),(3890,1,'upgrade','ᴥ'),(3891,1,'critical','ˁ˓'),(3891,1,'pager','Ď	'),(3892,1,'handlers','Ď\''),(3761,6,'pop','Ĩ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict5E` ENABLE KEYS */;

--
-- Table structure for table `dict5F`
--

DROP TABLE IF EXISTS `dict5F`;
CREATE TABLE `dict5F` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict5F`
--


/*!40000 ALTER TABLE `dict5F` DISABLE KEYS */;
LOCK TABLES `dict5F` WRITE;
INSERT INTO `dict5F` VALUES (3879,1,'flexibility','Ϗ〔ϧ'),(3879,1,'align','幐	'),(3821,1,'verifies',''),(3837,1,'lostwarn','Ë'),(3840,1,'admit','˯'),(3805,1,'failed','À'),(3780,1,'tsync','Ğ'),(3756,1,'failed',''),(3751,1,'trending','Ħ'),(3721,1,'smaller','ɣ'),(3721,1,'flexibility','Ë'),(3881,1,'ischecksenabled','ࡕ'),(3881,1,'flexibility','ᬖಎQ'),(3880,1,'taylor','ᗘ'),(3881,1,'failed','᎓୹'),(3880,1,'reporting','̄@ѽ^\'ª⮲ʒĨ­o/=\'/&$((+&Å$1=+/,011\'PM\Z@&Ô\"	\Z(\')-&ѹ'),(3880,1,'removes','⋘'),(3879,1,'reporting','˭­@'),(3872,1,'failed','ʦƏ'),(3871,1,'1970','ܷیȪ८۴ŏշ˔ൄ'),(3871,1,'7200','ấ'),(3871,1,'smaller','ᙟ㘷'),(3871,1,'scientist','㖾'),(3871,1,'reporting','Ÿ'),(3871,1,'heat','䠆'),(3871,1,'answering','倯'),(3879,1,'removes','⟀᥹'),(3880,1,'flexibility','̹Ѹ'),(3880,1,'failed','ᮭ'),(3880,1,'bars','Ῠ'),(3880,1,'7200','Ḥ;F'),(3881,1,'iserror','ᎌ'),(3881,1,'reporting','ƾ᪌ࣿk/$'),(3881,1,'setvalue','㈽'),(3881,1,'smaller','⢘'),(3884,1,'removes','ƨਕքʨ'),(3885,1,'removes','ǿظ'),(3888,1,'overlap','ѣ®'),(3889,1,'overlap','Ʋ'),(3890,1,'globally','᝿LGG'),(3890,1,'reporting','ၬ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict5F` ENABLE KEYS */;

--
-- Table structure for table `dict60`
--

DROP TABLE IF EXISTS `dict60`;
CREATE TABLE `dict60` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict60`
--


/*!40000 ALTER TABLE `dict60` DISABLE KEYS */;
LOCK TABLES `dict60` WRITE;
INSERT INTO `dict60` VALUES (3871,1,'undeprecate','ļ'),(3873,1,'buf','ҋ'),(3871,1,'consolidating','ס'),(3856,1,'machine',''),(3848,1,'machine','Y'),(3847,1,'machine',''),(3828,1,'machine','%'),(3871,1,'machine','G㶚'),(3868,1,'advantage',''),(3870,1,'2004','Z'),(3871,1,'bins','⾼'),(3806,1,'matches',''),(3879,1,'matches','䊶ೊ'),(3879,1,'authconfig','൨K'),(3879,1,'advantage','㿳'),(3877,1,'machine','׀'),(3880,1,'consolidating','ࢠ'),(3880,1,'advantage','ᕜ'),(3879,1,'scanning','⬨9'),(3805,1,'matches',''),(3730,1,'matches',' '),(3736,1,'machine','\Z'),(3744,1,'matches','Ľ'),(3752,1,'machine','\"·'),(3754,1,'matches','ĸ'),(3757,1,'matches','ģ'),(3781,1,'jcrit','q'),(3782,1,'machine','Ò'),(3789,1,'machine','!'),(3880,1,'machine','॓ᑪ'),(3880,1,'matches','઱㲯\n'),(3881,1,'2004','⍽'),(3881,1,'matches','ຑ>A?>DCBDX@QAAAx545E«ŝ´UچƳwĪͰᖙ'),(3884,1,'convention',''),(3890,1,'machine','᱇&');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict60` ENABLE KEYS */;

--
-- Table structure for table `dict61`
--

DROP TABLE IF EXISTS `dict61`;
CREATE TABLE `dict61` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict61`
--


/*!40000 ALTER TABLE `dict61` DISABLE KEYS */;
LOCK TABLES `dict61` WRITE;
INSERT INTO `dict61` VALUES (3879,1,'secure','ᨉ㥢'),(3871,1,'traffic','֌ȃƢ঺ʎᱜᆩ.'),(3872,1,'macros','ڞľ\r'),(3874,1,'altered','ǹ'),(3875,1,'altered','Ʋ'),(3875,1,'operational','P'),(3876,1,'altered','Ĕ'),(3877,1,'secure','Øκ'),(3878,1,'operational','Ù̆'),(3878,1,'secure','Ӕ'),(3879,1,'macros','━ᦣëi`ӵ(I]'),(3879,1,'operational','敯'),(3879,1,'remain','཮'),(3871,1,'representations','ǘූ'),(3887,1,'macros','౟'),(3881,1,'settemplate','⺣Ȥŀ'),(3890,1,'remain','਱ᑅ'),(3890,1,'secure','Λ'),(3888,1,'overlaps','Ҙ'),(3888,1,'macros','E2ƐZśŔNP¡Iĝ)\''),(3890,1,'macros','e࠿ၰ5˓\r\nZ4'),(3880,1,'communicates','࢑'),(3880,1,'bird','៙'),(3721,1,'1995',''),(3880,1,'macros','ᇤ'),(3880,1,'remain','଑ឱ'),(3734,1,'secure','1'),(3736,1,'conduct','~'),(3744,1,'quoted','ǃ'),(3791,1,'secure','¨'),(3793,1,'secure','r'),(3795,1,'secure','e'),(3798,1,'traffic','¬\"'),(3799,1,'traffic','ŭ'),(3805,1,'secure','º'),(3819,1,'operational','\Z'),(3820,1,'operational','\Z'),(3827,1,'secure','%'),(3871,1,'920809200','㢘4'),(3871,1,'aggoutput','᷷'),(3871,1,'altered','㺸'),(3871,1,'mar','▝6\n'),(3871,1,'remain','⵨'),(3720,1,'rhino','t'),(3892,1,'macros',''),(3880,1,'operational','ΕЮĪ㳪'),(3881,1,'log4j','ᶱ̎çn0Þ2'),(3881,1,'communicates','௃');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict61` ENABLE KEYS */;

--
-- Table structure for table `dict62`
--

DROP TABLE IF EXISTS `dict62`;
CREATE TABLE `dict62` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict62`
--


/*!40000 ALTER TABLE `dict62` DISABLE KEYS */;
LOCK TABLES `dict62` WRITE;
INSERT INTO `dict62` VALUES (3871,1,'tag','ᑂखྡŖ\"^\r'),(3880,1,'event','࠷ېýů? ðȋ஑>\rୈ\r\r֤\rǞ̶Îଙ'),(3871,1,'submit','⾃'),(3880,1,'corresponding','ᱦ४ྜྷ*'),(3871,1,'requires','ㄲ'),(3871,1,'pngs','࿏'),(3873,1,'waits','ҙ'),(3872,1,'startproc','̱'),(3868,1,'submit','é'),(3872,1,'event','ڐő'),(3871,1,'event','ᔎ'),(3871,1,'corresponding','ȉኣ'),(3880,1,'edited','ᓘ'),(3872,1,'requires','·'),(3720,1,'event','I'),(3721,1,'commercially','Ə'),(3804,1,'event','¤'),(3782,1,'requires','ś'),(3783,1,'requires','}'),(3862,1,'requires','Ñ'),(3840,1,'requires','Ė'),(3838,1,'corresponding',''),(3830,1,'requires','~'),(3819,1,'requires','U'),(3884,1,'event','Ӗ\n-	Ŋ࡬\n-	'),(3871,1,'waits','ٚ'),(3872,1,'distribution','Ԉ'),(3869,1,'distribution','Ï'),(3871,1,'consider','≈T'),(3780,1,'requires','Ũ'),(3780,1,'lru','å'),(3879,1,'viewable','寜'),(3879,1,'tag','䰈'),(3879,1,'event','ቇ଻ڙᡐ©Đϙñ\Zۭ	Á\Z\r\n0ؽi೾Òó\r\'˴°+'),(3879,1,'requires','Ⴣ}'),(3879,1,'edited','ঘ'),(3879,1,'distribution','®ǔϴ኱'),(3879,1,'corresponding','㻧'),(3884,1,'corresponding','Ꭸ2w'),(3881,1,'requires','ଢ଼Ë⚄Ɖ'),(3881,1,'tag','᳜'),(3881,1,'event','ֳ\nƵóļ൜Ȉη#ǫǛȉ̝\Z˴Ǆ\nؾ§'),(3881,1,'distribution','㖄!'),(3881,1,'consider','❥'),(3880,1,'submit','ⓑ ̢έ	ҡ	ˁ'),(3778,1,'requires','t'),(3766,1,'requires','Ì'),(3751,1,'consider',''),(3763,1,'submit','ĥ'),(3727,1,'requires','ď'),(3721,1,'play',''),(3884,1,'requires','঱܏'),(3884,1,'tag','ፈ'),(3885,1,'event','ֆ\r-	'),(3886,1,'event','̝C'),(3887,1,'event','͈Dл\r-	'),(3887,1,'tag','ೂ'),(3888,1,'distribution','ͅ'),(3888,1,'requires','œ'),(3890,1,'distribution','ቦ'),(3890,1,'event','ఫŏɻª\n(\nƨ;äәɻ'),(3890,1,'hourly','ಸ'),(3890,1,'requires','ŋ᱑'),(3892,1,'edited','n'),(3892,1,'event','Ĵ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict62` ENABLE KEYS */;

--
-- Table structure for table `dict63`
--

DROP TABLE IF EXISTS `dict63`;
CREATE TABLE `dict63` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict63`
--


/*!40000 ALTER TABLE `dict63` DISABLE KEYS */;
LOCK TABLES `dict63` WRITE;
INSERT INTO `dict63` VALUES (3825,1,'locates','¿'),(3881,1,'compliance','උ'),(3864,1,'rta','^'),(3871,1,'00cc00','ḅ'),(3871,1,'1256486519','䄼'),(3871,1,'ifdescr','䃆		'),(3871,1,'periods','ᬔ'),(3871,1,'recorded','ണ'),(3820,1,'ifdescr','«'),(3819,1,'excluded',''),(3799,1,'recorded','%'),(3881,1,'identification','ࢤŁൌ\Z'),(3880,1,'periods','ᆮ̺ఛ∰'),(3879,1,'periods','ƞᷨԦˍؠÁӁф֤¥ė\n'),(3798,1,'recorded','#'),(3880,1,'accurate','ᔩ'),(3822,1,'rta',''),(3752,1,'rta','ð'),(3830,1,'recorded','q'),(3834,1,'rta',''),(3762,1,'rta',''),(3879,1,'nagios2collage','䩲ň'),(3877,1,'dsa','в)Ĥ\nĩ'),(3872,1,'stopping','̌'),(3881,1,'selectquery','൐'),(3883,1,'periods','%'),(3885,1,'periods',''),(3890,1,'excluded','Ḉ'),(3890,1,'periods','ṟ'),(3891,1,'periods','ƵÊɉC'),(3883,2,'periods','');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict63` ENABLE KEYS */;

--
-- Table structure for table `dict64`
--

DROP TABLE IF EXISTS `dict64`;
CREATE TABLE `dict64` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict64`
--


/*!40000 ALTER TABLE `dict64` DISABLE KEYS */;
LOCK TABLES `dict64` WRITE;
INSERT INTO `dict64` VALUES (3880,1,'floor','}'),(3880,1,'flexible','Ʉ⚊ӧƑҔ'),(3880,1,'assist','չ'),(3878,1,'floor','t'),(3877,1,'purpose','T'),(3878,1,'14','˴'),(3872,1,'step','ݨį*ǧŎ'),(3871,1,'step','Ğ׆}ʟ3V6\r6ƈʰɗѐ7ʿ\'ݭरÀż'),(3858,1,'readcommunity','('),(3857,1,'breeze',''),(3802,1,'readcommunity','1'),(3822,1,'14','È'),(3827,1,'14','ʞ\r'),(3831,1,'leading','F'),(3841,1,'readcommunity','\''),(3801,1,'readcommunity','+'),(3759,1,'name2','*'),(3751,1,'step','ǅ'),(3879,1,'step','ಿ߶ßēÐU5ۈੀ<?Ä{4ÄȥË÷ǆʅƭ@ϻɤăšJčזƑǦථ4eÁǮV'),(3879,1,'purpose','V٪ᵭᣪ'),(3879,1,'flexible','˥᤽⎉'),(3879,1,'floor','}'),(3879,1,'os','⨔Ɯ㢨'),(3879,1,'preloaded','㖰'),(3877,1,'floor','{'),(3880,1,'os','࠯'),(3879,1,'assist','ն'),(3878,1,'step','Ę'),(3878,1,'purpose','M'),(3871,1,'purpose','४ؿᩌ£Ǫ'),(3871,1,'14','⭀'),(3869,1,'os','<'),(3870,1,'purpose',''),(3871,1,'os','ᦱᐍԃϋǥVѺʂ͒'),(3871,1,'leading','㽙	'),(3871,1,'floor','៵'),(3871,1,'flexible','*'),(3871,1,'compatible','䉆'),(3880,1,'purpose','Vϊ෰'),(3881,1,'assist','Ή⓮'),(3881,1,'flexible','ƶҖǝ'),(3881,1,'floor','}'),(3881,1,'purpose','V➿'),(3881,1,'reject','ᾣ'),(3881,1,'step','ⱟ'),(3883,1,'step','4'),(3884,1,'preloaded','ࢧ'),(3884,1,'step','tP5Rԧӛ=ҬÎƗ¨'),(3885,1,'step','ĒQٓ='),(3887,1,'step','xľFƥGZAӳŘϱ¥'),(3888,1,'step','GԀ$iT{Ǟ'),(3889,1,'step','H	ȯ]6M'),(3890,1,'bad','̸@'),(3890,1,'step','ƛͱʾީևֲ'),(3891,1,'purpose','̺'),(3891,1,'step','J́½'),(3892,1,'step','.¤'),(3857,6,'breeze','h');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict64` ENABLE KEYS */;

--
-- Table structure for table `dict65`
--

DROP TABLE IF EXISTS `dict65`;
CREATE TABLE `dict65` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict65`
--


/*!40000 ALTER TABLE `dict65` DISABLE KEYS */;
LOCK TABLES `dict65` WRITE;
INSERT INTO `dict65` VALUES (3847,1,'sensor','+'),(3854,1,'mismatch','C'),(3873,1,'case','ɟ'),(3840,1,'optionally',''),(3877,1,'case','Ħԟ'),(3879,1,'case','ջ͒ၻȿῳࢩᒝg'),(3871,1,'handles','׉䙹'),(3871,1,'integers','ㆂ'),(3871,1,'optionally','᭭ཌྷ'),(3871,1,'provide','ㅡᱜ'),(3871,1,'scaling','ĈሁkȣӠ'),(3814,1,'mismatch','@f'),(3806,1,'scan','Q'),(3805,1,'scan','5'),(3785,1,'mismatch','@f'),(3784,1,'mismatch','@f'),(3744,1,'integers','Ȟ'),(3744,1,'machines',''),(3748,1,'mismatch','@f'),(3751,1,'provide','ȸ'),(3754,1,'provide','˖'),(3757,1,'scan','­(\r'),(3761,1,'mismatch','@f'),(3762,1,'cpl','*'),(3778,1,'case','Ù'),(3782,1,'case','ğ'),(3871,1,'case','ୁ྆ɔֿόࣷॖஔțОaŧȖ'),(3871,1,'calling','ᆼ'),(3872,1,'calling','؈ƅ'),(3871,1,'value4','ệ'),(3871,1,'sensor','㊐'),(3873,1,'sendq','ϙW	'),(3872,1,'case','̜	ϘǮ'),(3827,1,'case',']Ĝ'),(3871,1,'303','亭	Y'),(3856,1,'provide','ļ'),(3864,1,'cpl','.'),(3818,1,'mismatch','@f'),(3819,1,'provide','h'),(3729,1,'mismatch','>'),(3732,1,'mismatch','=g'),(3737,1,'mismatch','=f'),(3739,1,'mismatch','=f'),(3744,1,'case','ň'),(3721,1,'provide','ɐ'),(3871,1,'traveled','㖷\"ۯ߯Ý '),(3871,1,'skipped','㨺'),(3827,1,'optionally','ɉ'),(3833,1,'mismatch','@f'),(3722,1,'case',''),(3726,1,'quoting','C'),(3728,1,'mismatch','=f'),(3879,1,'optionally','㯰'),(3879,1,'provide','ˑ͋ᆜͯւ႐ʇǼΓ٤'),(3879,1,'scan','⧐ÜP	\Z'),(3880,1,'case','վᲫ'),(3880,1,'optionally','ഽ'),(3880,1,'provide','Ȱϯ˴ጯୖᮇ'),(3880,1,'scrollable','⊬'),(3881,1,'case','Ύࢾ▀'),(3881,1,'createauthticket','㢹'),(3881,1,'lightweight','ߞ❃֛'),(3881,1,'machines','㓝'),(3881,1,'provide','ƞʑȏԹǠ٥ĝÅ¥dvႏg	\n\"BŹƚȧݜQó(Å'),(3884,1,'provide','ጆ'),(3886,1,'provide','='),(3887,1,'integers','ɢ஑'),(3887,1,'provide','°>ଋ'),(3888,1,'case','࣍'),(3888,1,'provide','ēº'),(3888,1,'sendq','ى'),(3889,1,'optionally','Р'),(3890,1,'case','ၷ'),(3890,1,'optionally','ޜय़I');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict65` ENABLE KEYS */;

--
-- Table structure for table `dict66`
--

DROP TABLE IF EXISTS `dict66`;
CREATE TABLE `dict66` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict66`
--


/*!40000 ALTER TABLE `dict66` DISABLE KEYS */;
LOCK TABLES `dict66` WRITE;
INSERT INTO `dict66` VALUES (3825,1,'options','ê'),(3728,1,'options','K'),(3790,1,'options','\''),(3871,1,'allows','ռ¤ޡڤҺ\'༼AϾɸ'),(3869,1,'unicode','q'),(3869,1,'allows','ñ'),(3865,1,'options','5'),(3866,1,'options','^'),(3743,1,'options','.'),(3742,1,'options',';'),(3727,1,'options','E'),(3765,1,'options','#'),(3766,1,'options','<{'),(3809,1,'options','('),(3811,1,'options','8'),(3813,1,'options','/'),(3814,1,'options','N'),(3732,1,'options','L'),(3822,1,'options','-¼'),(3871,1,'course','㤈ᑵ'),(3871,1,'counts','㒮ष'),(3863,1,'options','8-'),(3808,1,'options','2'),(3741,1,'options','2'),(3740,1,'options','['),(3739,1,'options','K'),(3734,1,'options',''),(3735,1,'options','4'),(3736,1,'counts','¢'),(3736,1,'options','.'),(3737,1,'options','K'),(3738,1,'options',','),(3725,1,'options','2'),(3763,1,'options','*'),(3831,1,'options','0'),(3733,1,'options','n'),(3806,1,'options','9'),(3805,1,'options','*'),(3804,1,'options','<'),(3803,1,'options','8'),(3800,1,'options','&'),(3799,1,'options','D'),(3799,1,'course','ř'),(3799,1,'allows','Ĝ'),(3798,1,'options','t'),(3797,1,'options','7'),(3795,1,'options','('),(3871,1,'datapoints','ℏ'),(3820,1,'hex','x'),(3819,1,'hex','Ñ'),(3819,1,'options','('),(3755,1,'options','/'),(3851,1,'options','N'),(3850,1,'options','3'),(3849,1,'options',','),(3848,1,'options',';'),(3847,1,'options','3'),(3846,1,'options','H'),(3843,1,'options','!'),(3844,1,'options','6'),(3845,1,'options','7'),(3841,1,'hex','6'),(3840,1,'options','Rÿ'),(3838,1,'options','/'),(3834,1,'options','D'),(3835,1,'options','+'),(3836,1,'options',';'),(3837,1,'options','E'),(3833,1,'options','N'),(3747,1,'options','&'),(3748,1,'options','N'),(3751,1,'options',''),(3731,1,'options','1'),(3807,1,'options','.'),(3857,1,'options',','),(3854,1,'options','R'),(3853,1,'options','0'),(3818,1,'options','N'),(3788,1,'options','X'),(3791,1,'options','I'),(3744,1,'securitylevel','¡'),(3730,1,'options','8'),(3823,1,'options','U'),(3824,1,'options','9T<'),(3832,1,'options','\r'),(3827,1,'options','}'),(3829,1,'options','['),(3721,1,'paying','Ŋ'),(3753,1,'options','>'),(3830,1,'options','0'),(3820,1,'options',')'),(3787,1,'options','Z'),(3745,1,'options','9'),(3724,1,'options','4'),(3722,1,'options','ă'),(3786,1,'options','#'),(3756,1,'options','9'),(3759,1,'name1','('),(3760,1,'options','.'),(3761,1,'options','N'),(3762,1,'options','2'),(3754,1,'options','r'),(3856,1,'options','8ć	'),(3815,1,'options',')'),(3776,1,'options','H'),(3780,1,'options','7'),(3781,1,'options','<'),(3782,1,'options','9'),(3783,1,'allows','º'),(3783,1,'options','/'),(3784,1,'options','N'),(3785,1,'options','N'),(3744,1,'options','y'),(3729,1,'options','M'),(3855,1,'options','!'),(3789,1,'options','T'),(3864,1,'options','<'),(3793,1,'options','5'),(3871,1,'hex','䃺'),(3871,1,'line1','ᮛɭ▇'),(3871,1,'meter','୶⨃'),(3871,1,'options','㝏ತݬ'),(3871,1,'saves','Ȼ'),(3871,1,'spot','䞟'),(3871,1,'subtract','⒫ၖ᝷'),(3871,1,'transported','வK'),(3872,1,'members','౞'),(3872,1,'options','ݴƞ\"j3ýÙ'),(3873,1,'allows','ƺ'),(3875,1,'options','̂'),(3877,1,'challenged','܇'),(3879,1,'allows','ຕൣঊܝzКභᅚ'),(3879,1,'cellpadding','崗b´F'),(3879,1,'concept','⋪ሢ'),(3879,1,'controlling','ℵϚ'),(3879,1,'course','䠕'),(3879,1,'explorer','ᩯ'),(3879,1,'members','⸐⑩'),(3879,1,'options','݂ŧдG;\"˸Ӳb/3Ń߇ȡƫÃóéƐǇƺ}ĥËöÑ_༚׏Zֳ½ÚKµMӠñĠƪч'),(3879,1,'uninstalled','ၵ7L'),(3879,1,'valign','崞ń'),(3880,1,'allows','݀ͣߵɌ˙]xÂޫ֥Ã෍ʭ'),(3880,1,'concept','Ⴖ'),(3880,1,'controlling','თ'),(3880,1,'counts','㦉'),(3880,1,'flapping','ᄦ'),(3880,1,'intermittent','ᡴ'),(3880,1,'options','࣡ȯǝħિǓ_ջɔӵஒ˲Ư˔рɥѭ'),(3881,1,'adjusting','Ԩᝨ'),(3881,1,'allows','ҼUΦȥᆝρீ'),(3881,1,'constructor','அú\rƢօĜŪÜϽᏢ*Ȉ'),(3881,1,'foreign','ᨌ6ࢌG'),(3881,1,'members','܉'),(3881,1,'options','૲ᩞ'),(3882,1,'allows','Ů'),(3882,1,'options','aX¬'),(3883,1,'options','@4'),(3884,1,'options','ęͳȋ8ɤ1­W̩ɼx¦'),(3885,1,'options','NÏթN\rÎnpĈ'),(3886,1,'options','H8İMJ¥'),(3887,1,'members','ਬ'),(3887,1,'options','%ϧ*ЏM\rN,Ĭ>͖Ð0>'),(3888,1,'allows','ļ'),(3888,1,'members','ς'),(3888,1,'options','ڇ'),(3889,1,'options','bƎMRɄ'),(3890,1,'allows','ΗʥC61XֱϪIĿoâͽ¬LMGę22>Ɓ'),(3890,1,'controlling','Ⴋֱ'),(3890,1,'flapping','ᖔ\r'),(3890,1,'options','¨3Ëӹ7S)๰ؿġ6ć3'),(3891,1,'flapping','˚˗'),(3891,1,'options','T/Ņ´®1:Ñ5'),(3892,1,'options','8');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict66` ENABLE KEYS */;

--
-- Table structure for table `dict67`
--

DROP TABLE IF EXISTS `dict67`;
CREATE TABLE `dict67` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict67`
--


/*!40000 ALTER TABLE `dict67` DISABLE KEYS */;
LOCK TABLES `dict67` WRITE;
INSERT INTO `dict67` VALUES (3880,1,'edition','̌ϙúࠚൾӎҟQᶷ'),(3880,1,'eclipse','̑'),(3881,1,'compliant','ᗑĉΤ'),(3869,1,'compliant','u'),(3888,1,'extends','֏'),(3887,1,'license',''),(3849,1,'occurs','S'),(3835,1,'license','\Z\n'),(3887,1,'extends','ɾ஑'),(3886,1,'license',''),(3885,1,'license',''),(3884,1,'license',''),(3881,1,'license','0,〈'),(3881,1,'letters','㘈'),(3881,1,'integrates','ك'),(3881,1,'extends','⬔ΈȠľ؝'),(3888,1,'license',''),(3881,1,'party','ת╂ऱˇ¨'),(3726,1,'quotes','e'),(3872,1,'compliant','ϻ'),(3883,1,'license',''),(3827,1,'occurs','ʏ'),(3831,1,'i586','À'),(3881,1,'evaluate','㚰'),(3820,1,'secname','`'),(3819,1,'secname','¹'),(3799,1,'aggregation','l'),(3798,1,'aggregation',''),(3872,1,'license',''),(3874,1,'license',''),(3875,1,'license',''),(3876,1,'license',''),(3877,1,'license','.,'),(3878,1,'edition','ह'),(3878,1,'license','\','),(3879,1,'compliant','垐'),(3880,1,'review','Բ\\;'),(3879,1,'eclipse','Χ'),(3879,1,'edition','΢䠹᏾'),(3788,1,'license',''),(3787,1,'quotes',''),(3778,1,'license','ª'),(3882,1,'license',''),(3880,1,'letters','ౣ'),(3881,1,'occurs','ݬ⑭'),(3881,1,'eclipse','߆Ṏ'),(3881,1,'container','ߔᷪ\n\n'),(3880,1,'integrates','Ȣᮉ▔'),(3880,1,'occurs','ᄨ\\ƾඣઓӚ'),(3879,1,'extends','ᇘ'),(3740,1,'license','ď'),(3880,1,'contracts','ᜓ'),(3721,1,'license','ť'),(3869,1,'license','.'),(3881,1,'targetname','お'),(3744,1,'secname','rD'),(3881,1,'review','͂\\;Ğ'),(3880,1,'aggregation','䊪)'),(3879,1,'review','ԯ\\;ƫ'),(3879,1,'party','˅'),(3879,1,'orange','⧜Ɯ'),(3879,1,'occurs','侚'),(3879,1,'license','0,'),(3889,1,'license',''),(3880,1,'eventually','ᮡ'),(3880,1,'visualize','ܭ'),(3869,1,'party','\\5'),(3871,1,'boundaries','ᕏ¸హ'),(3871,1,'eventually','䙞'),(3880,1,'license','0,'),(3880,1,'party','Ȥ൷'),(3879,1,'integrates','˃ҷ'),(3871,1,'letters','ņ'),(3720,1,'container','¹V'),(3871,1,'quotes','ⳮ-'),(3871,1,'occurs','ڧᗽ⾦'),(3871,1,'review','㋶Ꮗ'),(3890,1,'license',''),(3890,1,'occurs','ࣾಘ'),(3890,1,'quotes','ȧ'),(3891,1,'license',''),(3892,1,'license',''),(3892,1,'quotes','Ş');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict67` ENABLE KEYS */;

--
-- Table structure for table `dict68`
--

DROP TABLE IF EXISTS `dict68`;
CREATE TABLE `dict68` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict68`
--


/*!40000 ALTER TABLE `dict68` DISABLE KEYS */;
LOCK TABLES `dict68` WRITE;
INSERT INTO `dict68` VALUES (3880,1,'networknagios','ᗩ'),(3820,1,'snmpkey',''),(3827,1,'minimum','Ïï'),(3761,1,'minimum','Ú'),(3879,1,'s1chapter1a','嶿:'),(3879,1,'revisions','١'),(3879,1,'minimum','⤡᱋'),(3879,1,'feeders','Ș䀋ࡁUᑩ'),(3879,1,'env','ᬽ'),(3879,1,'complex','ⓩ᫦ଫၩӉ'),(3879,1,'classic','෿%´('),(3880,1,'minimum','䂅'),(3879,1,'special','䊄'),(3759,1,'keywords','ô'),(3748,1,'minimum','Ú'),(3745,1,'minimum',''),(3741,1,'minimum','s'),(3740,1,'minimum',')m'),(3739,1,'minimum','×'),(3737,1,'minimum','×'),(3734,1,'ax','`'),(3732,1,'minimum','Ø'),(3875,1,'ifindiscards','À'),(3878,1,'complex','Ė'),(3871,1,'special','႗௿$ٺࣀેᙐ'),(3871,1,'seperate','⾾'),(3833,1,'minimum','Ú'),(3778,1,'oratab','ç'),(3780,1,'lrum','äÃ'),(3784,1,'minimum','Ú'),(3785,1,'minimum','Ú'),(3813,1,'minimum','E'),(3814,1,'minimum','Ú'),(3818,1,'minimum','Ú'),(3819,1,'snmpkey','Ø'),(3759,1,'special','`'),(3728,1,'minimum','×'),(3871,1,'villages','䗶'),(3871,1,'minimum','҂Ďݭ$Չb්߄T⏜z-'),(3871,1,'keywords','ᄪੰ'),(3871,1,'counted','㑱'),(3871,1,'complex','ႍࢽਙᢛ'),(3860,1,'minimum','8'),(3854,1,'minimum','þ'),(3880,1,'normalizes','྽߀'),(3880,1,'revisions','٤'),(3881,1,'complex','ᩙs܄ᔨɑ'),(3881,1,'feeders','᷿,¢ʛѻg'),(3881,1,'getnumberofevents','ᤏ'),(3881,1,'getserviceavailability','ᆓ'),(3881,1,'lastinsertdate','࢈Ł඀©Ӑ'),(3881,1,'revisions','Ѷ'),(3881,1,'special','〩'),(3881,1,'technologies','ᰉଛ'),(3885,1,'minimum','Ԣ'),(3886,1,'classic','ϱ'),(3887,1,'minimum','ݣ'),(3888,1,'special','ӥǃ$'),(3890,1,'minimum','ढ़'),(3890,1,'special','ᤕ5'),(3890,1,'strict','ᯪ'),(3892,1,'surrounded','Ŝ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict68` ENABLE KEYS */;

--
-- Table structure for table `dict69`
--

DROP TABLE IF EXISTS `dict69`;
CREATE TABLE `dict69` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict69`
--


/*!40000 ALTER TABLE `dict69` DISABLE KEYS */;
LOCK TABLES `dict69` WRITE;
INSERT INTO `dict69` VALUES (3887,1,'vitals','{Ͳ'),(3885,1,'dependencies','ࢇ'),(3881,1,'driver','☙'),(3881,1,'gethostavailability','ᇔ'),(3821,1,'groundwork',''),(3822,1,'groundwork',''),(3823,1,'2003','w'),(3823,1,'groundwork',''),(3824,1,'groundwork',''),(3825,1,'groundwork',''),(3826,1,'groundwork',''),(3827,1,'groundwork',''),(3828,1,'groundwork',''),(3829,1,'groundwork',''),(3830,1,'groundwork','*)'),(3831,1,'groundwork',''),(3832,1,'groundwork',''),(3833,1,'groundwork',''),(3834,1,'groundwork',''),(3835,1,'groundwork',''),(3836,1,'groundwork',''),(3809,1,'groundwork',''),(3881,1,'cache','௶'),(3892,7,'groundwork','ǂ'),(3885,7,'groundwork','৛'),(3890,1,'dependencies','ᘎ\rࠩ'),(3849,1,'groundwork',''),(3850,1,'groundwork',''),(3851,1,'groundwork',''),(3852,1,'groundwork',''),(3853,1,'groundwork',''),(3854,1,'groundwork',''),(3855,1,'groundwork',''),(3856,1,'groundwork',''),(3857,1,'groundwork',''),(3858,1,'groundwork',''),(3859,1,'groundwork',''),(3860,1,'groundwork',''),(3861,1,'groundwork',''),(3862,1,'dependencies','Í'),(3862,1,'groundwork',''),(3863,1,'groundwork',''),(3864,1,'groundwork',''),(3865,1,'groundwork',''),(3866,1,'groundwork',''),(3867,1,'groundwork',''),(3868,1,'groundwork','#\ng'),(3871,1,'288','䇶CÀ̷'),(3871,1,'author','ۅٽƦ႞^_èفÓŃ8ʟί₲'),(3871,1,'lower','Նਗּ	㩅#'),(3871,1,'pixel','Ꭼß\rſ6E'),(3872,1,'dest','Ԧ£\"'),(3872,1,'groundwork','ēԌ'),(3873,1,'cache','ѯ\r'),(3873,1,'groundwork','T<<'),(3809,1,'cload15','\''),(3881,1,'2003','⍼'),(3891,7,'groundwork','ٌ'),(3884,7,'groundwork','ᑤ'),(3890,1,'cache','ࠧ'),(3837,1,'groundwork',''),(3838,1,'groundwork',''),(3839,1,'groundwork',''),(3840,1,'groundwork','²Ɯ'),(3840,1,'requesting','ƛ'),(3841,1,'dest','.'),(3841,1,'groundwork',''),(3842,1,'groundwork',''),(3843,1,'groundwork',''),(3844,1,'groundwork',''),(3845,1,'groundwork',''),(3846,1,'groundwork',''),(3847,1,'groundwork',''),(3848,1,'groundwork',''),(3810,1,'groundwork',''),(3811,1,'groundwork',''),(3812,1,'groundwork',''),(3813,1,'groundwork',''),(3814,1,'groundwork',''),(3815,1,'groundwork',''),(3816,1,'groundwork',''),(3817,1,'groundwork',''),(3818,1,'groundwork',''),(3819,1,'groundwork',''),(3820,1,'groundwork',''),(3876,2,'groundwork',''),(3892,1,'groundwork',''),(3874,2,'groundwork',''),(3875,2,'groundwork',''),(3876,1,'groundwork','#£'),(3878,7,'groundwork','ॄ'),(3887,1,'3d','˪\r஄'),(3883,7,'groundwork','ŋ'),(3874,1,'groundwork','5û\'#3ŭ'),(3890,1,'experimental','ᑗ9RÚ'),(3882,1,'groundwork','Z'),(3883,1,'groundwork','	'),(3884,1,'dependencies','Ųץ\'<\Zȿ\n'),(3877,1,'groundwork','-ɸ\n;9Ï¼'),(3878,1,'groundwork','\nq,\'\'Sʂ! $%&#\'%\Ẓ'),(3879,1,'dependencies','⎭෽Ć͑ʗ'),(3879,1,'groundwork','	\nul°\Z*!${  I\r	 \n!/sGH#- \"e.¸If­!ěÐ>z+En\n?A%!9:)D1H48>6\r$=7.5:,K)ed+8˘.\")O%r>Õ\r!^j\"³Ģ8Õ\'#Ȕ\rΝoʥЗĢɹϞ÷EʸԪƼƁz\r\"\ZUT\\	 <18)ƨŻà&L#!3¬č?āȡ*-(L1Í6M1(ST%		%	V#,Lj˿SÔqɗp\r|%x6ÀYVJ	(ZgŪj'),(3886,1,'groundwork',''),(3885,1,'groundwork','Tܐ'),(3875,1,'groundwork','Ġ\Z#z'),(3882,7,'groundwork','ư'),(3881,7,'groundwork','㧀'),(3891,1,'groundwork',''),(3890,1,'lower','ބᕲ'),(3890,1,'layers','ڧ'),(3890,1,'groundwork','ᴒ'),(3875,7,'groundwork','ͅ'),(3890,7,'groundwork','ᾑ'),(3879,1,'lower','㫀'),(3879,1,'vitals','㛆'),(3880,1,'dependencies','቞'),(3880,1,'groundwork','	\nu4^@5!&{  8\'$ \n!/sGH#&jJ	J6/{)\Z\ZT	\'	\r&\"	<\n\":\rZ@M.	\\4çR-\'Ⱥ\r!Ų=.|	bR4ťӊ!.،;ʮQಲ\Zq-wǮ8gSd˝ѱʀ)j\r#9	!		\"	\r		:Y^Ā\"'),(3880,7,'groundwork','䡀'),(3878,2,'groundwork',''),(3879,2,'groundwork',''),(3880,2,'groundwork',''),(3881,2,'groundwork',''),(3722,1,'groundwork',''),(3723,1,'groundwork',''),(3724,1,'groundwork',''),(3725,1,'groundwork',''),(3726,1,'groundwork',''),(3727,1,'groundwork',''),(3728,1,'groundwork',''),(3729,1,'groundwork',''),(3730,1,'groundwork',''),(3731,1,'groundwork',''),(3732,1,'groundwork',''),(3733,1,'groundwork',''),(3734,1,'groundwork',''),(3735,1,'groundwork',''),(3736,1,'groundwork',''),(3737,1,'groundwork',''),(3738,1,'groundwork',''),(3739,1,'groundwork',''),(3740,1,'groundwork',''),(3741,1,'groundwork',''),(3742,1,'groundwork',''),(3743,1,'groundwork',''),(3744,1,'groundwork',''),(3744,1,'lower','ǩ)'),(3745,1,'groundwork',''),(3746,1,'groundwork',''),(3747,1,'groundwork',''),(3748,1,'groundwork',''),(3749,1,'groundwork',''),(3750,1,'groundwork',''),(3751,1,'groundwork',''),(3751,1,'specifed','İ-'),(3752,1,'groundwork',''),(3753,1,'groundwork',''),(3754,1,'groundwork','~Ł1\Z%59%'),(3755,1,'groundwork',''),(3756,1,'groundwork',''),(3757,1,'groundwork',''),(3758,1,'groundwork',''),(3759,1,'groundwork',''),(3760,1,'groundwork',''),(3761,1,'groundwork',''),(3762,1,'groundwork',''),(3763,1,'groundwork',''),(3764,1,'groundwork',''),(3765,1,'groundwork','f'),(3766,1,'groundwork',''),(3767,1,'groundwork',''),(3768,1,'groundwork',''),(3769,1,'groundwork',''),(3770,1,'groundwork',''),(3771,1,'groundwork',''),(3772,1,'groundwork',''),(3773,1,'groundwork',''),(3773,1,'maxext',''),(3774,1,'groundwork',''),(3775,1,'groundwork',''),(3776,1,'groundwork',''),(3777,1,'groundwork',''),(3778,1,'cache','*?'),(3778,1,'groundwork',''),(3779,1,'groundwork',''),(3780,1,'cache','	'),(3780,1,'groundwork',''),(3780,1,'lower','ƕ'),(3781,1,'groundwork',''),(3782,1,'2003','$'),(3782,1,'groundwork',''),(3782,1,'lower','Ƌ'),(3783,1,'groundwork',''),(3784,1,'groundwork',''),(3785,1,'groundwork',''),(3786,1,'groundwork',''),(3787,1,'groundwork',''),(3788,1,'groundwork',''),(3789,1,'groundwork',''),(3790,1,'groundwork',''),(3791,1,'groundwork',''),(3792,1,'2003','\\'),(3792,1,'groundwork',''),(3793,1,'groundwork',''),(3794,1,'groundwork',''),(3795,1,'groundwork',''),(3796,1,'groundwork',''),(3797,1,'groundwork',''),(3798,1,'groundwork',''),(3799,1,'groundwork',''),(3800,1,'groundwork',''),(3801,1,'groundwork',''),(3802,1,'groundwork',''),(3803,1,'groundwork',''),(3804,1,'groundwork',''),(3805,1,'groundwork',''),(3806,1,'groundwork',''),(3807,1,'groundwork',''),(3808,1,'groundwork',''),(3720,1,'experimental','Ŭ'),(3879,7,'groundwork','朲'),(3889,7,'groundwork','ӽ'),(3876,7,'groundwork','Ƭ'),(3889,1,'groundwork',''),(3720,1,'refactor','R÷'),(3721,1,'2003','č'),(3888,1,'groundwork','	ĥlbĞ'),(3881,1,'groundwork','	\nu\Z<`!\Z#\r	.\n	& \n!/sGH%$p :?<ǚ\ñD7\'[ᐠÁƣ1XFxX%F>\r\r$(!+;ҙ¹L'),(3888,7,'groundwork','ऍ'),(3884,1,'groundwork','Ĳպ4ƮDÙ'),(3886,7,'groundwork','Ը'),(3881,1,'complexity','⡭'),(3773,6,'maxext','5'),(3874,7,'groundwork','ϯ'),(3887,1,'groundwork',''),(3887,1,'dependencies','mེ9'),(3887,7,'groundwork','ᄒ'),(3877,7,'groundwork','ލ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict69` ENABLE KEYS */;

--
-- Table structure for table `dict6A`
--

DROP TABLE IF EXISTS `dict6A`;
CREATE TABLE `dict6A` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict6A`
--


/*!40000 ALTER TABLE `dict6A` DISABLE KEYS */;
LOCK TABLES `dict6A` WRITE;
INSERT INTO `dict6A` VALUES (3880,1,'receive','ᎍᣭѦՕ'),(3879,1,'identical','㣜'),(3879,1,'limits','᫄'),(3720,1,'dnd','Ů'),(3871,1,'theory','䲒'),(3873,1,'fairly','K'),(3872,1,'receive','ü࣪O'),(3880,1,'numerical','ᩰǷᠡ'),(3879,1,'100','ẳ᫏Ꮨ࿔bú'),(3878,1,'software','>'),(3877,1,'software','>'),(3876,1,'100','Û'),(3875,1,'ifindex','Ǜ9'),(3874,1,'100','ĄǷ'),(3873,1,'software','='),(3879,1,'workstations','㚘'),(3880,1,'preserved','⢔өƯ'),(3879,1,'software','>ɒ᯿'),(3880,1,'dropdown','ᵺʭȇ'),(3721,1,'software','S×µE'),(3743,1,'100','w'),(3744,1,'limits','ȣ'),(3744,1,'regexi','Y'),(3752,1,'100','ì'),(3759,1,'cumulative','K-'),(3763,1,'software','ģ'),(3780,1,'receive','ÿ'),(3783,1,'receipt',''),(3815,1,'100','P'),(3819,1,'ifindex',''),(3820,1,'ifindex','¦'),(3823,1,'ica','X'),(3824,1,'ica','¡'),(3825,1,'ica','n	#	'),(3862,1,'riiki','S'),(3862,1,'software','`'),(3871,1,'limits','ԙզรޛᰄᆍ'),(3871,1,'identical','似'),(3871,1,'fairly','㊿'),(3871,1,'appears','ᥢ'),(3871,1,'100','കړŶͯ⌃J	\n=ႄ\r'),(3869,1,'software',')'),(3868,1,'theory','\\'),(3868,1,'software','ç'),(3879,1,'receive','᠐᝵'),(3879,1,'representative','Ҹ7'),(3863,1,'software',''),(3871,1,'numerical','ئচᑝ׬ݮ᭬'),(3880,1,'representative','һ7'),(3880,1,'software','>Ʊź̘½㹪'),(3880,1,'theory','ᖸ'),(3881,1,'identical','␑'),(3881,1,'numerical','ງ?@@>CCBCYAQ@AAA7635ϺW'),(3881,1,'receive','ᏂĝÝ|^ʤ'),(3881,1,'representative','ˋ7'),(3881,1,'seamless','⤟'),(3881,1,'software','>Ś⇀АQഺ'),(3887,1,'receive','ઞ!'),(3890,1,'confirms','ᷕ'),(3891,1,'receive','ȋâɴc'),(3823,6,'ica','Ö'),(3824,6,'ica','š'),(3825,6,'ica','ō');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict6A` ENABLE KEYS */;

--
-- Table structure for table `dict6B`
--

DROP TABLE IF EXISTS `dict6B`;
CREATE TABLE `dict6B` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict6B`
--


/*!40000 ALTER TABLE `dict6B` DISABLE KEYS */;
LOCK TABLES `dict6B` WRITE;
INSERT INTO `dict6B` VALUES (3890,1,'backup','ĬघĈታß(j\"'),(3888,1,'apply','ְǦí'),(3887,1,'apply','້tU'),(3879,1,'backup','ᰧՎ༖)\Z\"ᠾᨋ\"vGŁ'),(3873,1,'enterprise','/'),(3848,6,'dig','¹'),(3891,1,'unknown','ʺ˓'),(3885,1,'updated','ƒ'),(3885,1,'apply','Î#őٰRS$'),(3884,1,'updated','ᐉ'),(3884,1,'unknown','ҫਆ̉DQ'),(3880,1,'unknown','ጜ٪ȨIʲҷ'),(3871,1,'caching','ÿ'),(3879,1,'apply','๚፾ą ܣƜत-Ȭʛû '),(3871,1,'false','Ꮤø'),(3871,1,'unknown','֚5!üǚ%±KP\r\n\Z	4ǤƵ(ৄY#Ȁÿ\ZŦecRILĵ?!YڱÏἔŗ AˮǮ'),(3805,1,'unknown',''),(3797,1,'username','J'),(3796,1,'username',''),(3795,1,'username','L'),(3794,1,'username',''),(3745,1,'unknown','å'),(3753,1,'unknown',']'),(3754,1,'unknown','\'$	Dk%'),(3755,1,'unknown','¨'),(3756,1,'username','+*'),(3760,1,'username','+'),(3763,1,'effort','Ċ'),(3763,1,'encrypt','Đ'),(3774,1,'false','#'),(3778,1,'username','g'),(3790,1,'backup',''),(3790,1,'username','5'),(3791,1,'username',''),(3792,1,'username','+'),(3793,1,'username','Y'),(3871,1,'updated','ī⢄'),(3871,1,'effort','╭⇂'),(3861,1,'username','('),(3860,1,'backup','\"'),(3856,1,'username',''),(3879,1,'passenv','൱'),(3890,1,'unknown','ޑ'),(3884,1,'apply','Źޤĥ\r	\r		\n\n		>:'),(3878,1,'apply','Ľ'),(3877,1,'username','æ0qÞĖŚğÙL)'),(3877,1,'updated','ܙ'),(3877,1,'simplify','ā'),(3876,1,'updated','Í'),(3875,1,'apply','̰	'),(3875,1,'updated','ū'),(3874,1,'updated','ƛ'),(3879,1,'simplify','♬'),(3881,1,'username','಄oⰑ\"'),(3881,1,'updated','ᵤ'),(3881,1,'unknown','⊧'),(3881,1,'enterprise','ʜ㉚'),(3827,1,'unknown','Ȑ'),(3820,1,'username','a'),(3880,1,'false','ን'),(3860,6,'backup','P'),(3879,1,'updated','⃬V@⣎ۯଌ˿'),(3879,1,'unknown','㷶'),(3890,1,'username','ωֽ'),(3890,1,'updated','ࣶ'),(3880,1,'enterprise','Ҍ̯'),(3879,1,'enterprise','҉'),(3880,1,'apply','⣴өƓ'),(3879,1,'username','ࡘਲ਼kK࠙'),(3819,1,'username','º'),(3734,1,'username','eB'),(3848,1,'dig','\r'),(3881,1,'false','്ܺ2'),(3873,1,'username','ċŽ'),(3735,1,'username','U'),(3880,1,'updated','Ṭᘈ'),(3880,1,'username','ాߟ'),(3881,1,'apply','ḿī'),(3872,1,'unknown','ਘ'),(3872,1,'simplify','ࢬ'),(3840,1,'dig','íŝ	'),(3843,1,'username','G'),(3844,1,'username','T'),(3845,1,'username','W'),(3871,1,'automobile','㕆'),(3890,1,'caching','ࡖ'),(3827,1,'username','Ɓ'),(3740,1,'username','1Ã'),(3744,1,'username','·'),(3880,1,'benefits','㞊č'),(3837,1,'username',''),(3729,1,'unknown','Ì');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict6B` ENABLE KEYS */;

--
-- Table structure for table `dict6C`
--

DROP TABLE IF EXISTS `dict6C`;
CREATE TABLE `dict6C` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict6C`
--


/*!40000 ALTER TABLE `dict6C` DISABLE KEYS */;
LOCK TABLES `dict6C` WRITE;
INSERT INTO `dict6C` VALUES (3879,1,'provided','ҲϢ๔Iԟ়Řӭᖻ'),(3765,1,'help','O'),(3782,1,'help','t'),(3866,1,'provided','\'\n'),(3730,1,'wait','t'),(3731,1,'help','3'),(3732,1,'help','N'),(3732,1,'wait','Í'),(3785,1,'wait','Ï'),(3747,1,'help','P'),(3746,1,'help','\"'),(3881,1,'achieve','⟪'),(3881,1,'gettyperule','ໟ'),(3848,1,'help','='),(3846,1,'help','J'),(3725,1,'help','4'),(3870,1,'jul','Y'),(3789,1,'help','V'),(3776,1,'help','G'),(3730,1,'help','<'),(3818,1,'wait','Ï'),(3818,1,'help','P'),(3814,1,'wait','Ï'),(3814,1,'help','P'),(3840,1,'help','T'),(3820,1,'help','Ĉ'),(3819,1,'help','Ė'),(3834,1,'help','F'),(3833,1,'wait','Ï'),(3830,1,'help','+'),(3743,1,'help','6'),(3721,1,'large','Ŕ'),(3754,1,'location','w'),(3870,1,'operations','ā'),(3849,1,'help','j'),(3850,1,'help','0\n'),(3788,1,'help','RN'),(3724,1,'help','3'),(3804,1,'wait',''),(3827,1,'wait','ğ'),(3863,1,'provided','[9'),(3863,1,'help',':'),(3880,1,'provided','ҵⷵᐙ'),(3866,1,'location',''),(3782,1,'float','ŝ'),(3780,1,'threads','z'),(3804,1,'help','«'),(3881,1,'selection','૪'),(3881,1,'provided','˅̊Śۙ02105334I1C222%\r\r\r(\r&\'Eξ9i9[9עબɌਮ'),(3881,1,'pkg','⴯ƀ'),(3879,1,'operations','п'),(3809,1,'help','*'),(3808,1,'help','l'),(3879,1,'help','ᔃ,⫚ᓡ'),(3833,1,'help','P'),(3831,1,'help','2'),(3780,1,'load1','['),(3766,1,'load1','`'),(3881,1,'location','㠼'),(3881,1,'large','᱆ӿ'),(3762,1,'help','4'),(3761,1,'wait','Ï'),(3875,1,'commercial',''),(3874,1,'commercial',''),(3873,1,'zip','Ũ'),(3873,1,'wait','Ӏ\r'),(3873,1,'help','Ō§'),(3872,1,'wireless','n'),(3872,1,'1c','૷'),(3871,1,'wait','䍥'),(3871,1,'settle','㷉'),(3871,1,'selection','⽪'),(3880,1,'location','໠ƻЍ'),(3880,1,'large','㡑'),(3880,1,'help','ढ³.ⷉȪʽ'),(3880,1,'departments','ബ'),(3880,1,'corner','ච'),(3880,1,'commercial','ɘൄ'),(3880,1,'achieve','޺'),(3741,1,'help',':'),(3740,1,'help','NA'),(3739,1,'wait','Ì'),(3739,1,'help','M'),(3738,1,'help','.'),(3738,1,'46','#'),(3737,1,'wait','Ì'),(3737,1,'help','M'),(3736,1,'help','0'),(3734,1,'sybase','		\n'),(3735,1,'help','i'),(3734,1,'help','ì'),(3733,1,'help','m'),(3722,1,'help','ř'),(3723,1,'wireless',''),(3879,1,'selection','䴚'),(3881,1,'goal','⟢'),(3871,1,'operations','ᣧ'),(3871,1,'normalization','▖'),(3878,1,'provided','Ğ'),(3878,1,'46','ࠈ'),(3877,1,'location','۹'),(3876,1,'commercial',''),(3811,1,'help',':'),(3827,1,'help',''),(3866,1,'help','o'),(3857,1,'wireless',''),(3766,1,'help','>'),(3854,1,'help','T'),(3754,1,'provided','Ė&'),(3879,1,'location','ᒍڎ\r⪄Ⴖ଍'),(3871,1,'location','㊀È'),(3780,1,'help','9'),(3778,1,'help',';f'),(3854,1,'wait','ó'),(3871,1,'provided','せ৩௓ݠʛ'),(3836,1,'help','5'),(3835,1,'help','\','),(3880,1,'logout','඙'),(3871,1,'hopefully','㦝܊ܚȏ'),(3742,1,'help','8?'),(3829,1,'help',']'),(3727,1,'help','G'),(3879,1,'groupings','㸽'),(3879,1,'commercial','˼'),(3843,1,'help','['),(3787,1,'help','\\'),(3786,1,'operations','-'),(3823,1,'help','¿'),(3822,1,'help','{'),(3821,1,'help','/'),(3803,1,'help',''),(3799,1,'help','F'),(3798,1,'help','v'),(3797,1,'help','b'),(3795,1,'help','*'),(3795,1,'3306','B'),(3793,1,'help','7'),(3793,1,'3306','ON'),(3791,1,'help','K'),(3791,1,'3306',''),(3790,1,'help','I'),(3871,1,'large','䁆ణ'),(3870,1,'simplexml',''),(3869,1,'sybase','`'),(3745,1,'help',';'),(3744,1,'help','{'),(3840,1,'maintainers','ȩ'),(3879,1,'wait','愍	Ť'),(3879,1,'slash','ᙷ䊭'),(3825,1,'wait','ĭ'),(3825,1,'help','Ĺ'),(3883,1,'commercial',''),(3882,1,'commercial',''),(3881,1,'zip','♍'),(3880,1,'operations','㧴ʽ'),(3753,1,'help',':'),(3761,1,'help','P'),(3759,1,'help','<'),(3754,1,'wait','ɕ'),(3755,1,'help','1'),(3756,1,'help',';'),(3758,1,'help','/'),(3729,1,'help','O'),(3881,1,'normalization','äѢF៬Ëï9¥sĄE'),(3871,1,'help','Β׃¿☸̤Ʋూ೽ț'),(3871,1,'corner','ş'),(3849,1,'wait','N'),(3807,1,'help','*'),(3806,1,'help','8'),(3805,1,'help',')'),(3879,1,'pkg','ၛ䠂! \"ƍw'),(3879,1,'1095','ᦄ'),(3824,1,'wait','ħ'),(3824,1,'help','ō'),(3785,1,'help','P'),(3790,1,'3306','2'),(3879,1,'large','✃'),(3748,1,'wait','Ï'),(3748,1,'help','P'),(3728,1,'wait','Ì'),(3728,1,'help','M'),(3881,1,'operations','Ƀⷮ'),(3838,1,'help','1'),(3845,1,'help','k'),(3853,1,'help','A'),(3851,1,'help','K1'),(3856,1,'help',':'),(3720,1,'normalization','J'),(3754,1,'help','kŉ'),(3864,1,'help','7'),(3784,1,'wait','Ï'),(3784,1,'help','P'),(3751,1,'help','y'),(3721,1,'commercial','&ţ'),(3783,1,'provided','n'),(3765,1,'46','´'),(3763,1,'help',','),(3884,1,'commercial',''),(3884,1,'provided','ᐯ'),(3884,1,'selection','Ⴐ'),(3884,1,'wait','ɸOő࡫Oő'),(3885,1,'commercial',''),(3885,1,'wait','؎'),(3886,1,'commercial',''),(3886,1,'provided','ð'),(3887,1,'achieve','ઁ'),(3887,1,'commercial',''),(3887,1,'corner','ɹA୐A'),(3887,1,'provided','Ęआ'),(3887,1,'wait','ࡏ'),(3888,1,'commercial',''),(3888,1,'location','Α'),(3888,1,'proxies','ճ'),(3889,1,'commercial',''),(3890,1,'commercial',''),(3890,1,'help','Ȫ\nᇩ'),(3890,1,'large','༿'),(3890,1,'location','Ǻ᭛'),(3890,1,'provided','В'),(3891,1,'commercial',''),(3892,1,'commercial',''),(3734,6,'sybase','ć');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict6C` ENABLE KEYS */;

--
-- Table structure for table `dict6D`
--

DROP TABLE IF EXISTS `dict6D`;
CREATE TABLE `dict6D` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict6D`
--


/*!40000 ALTER TABLE `dict6D` DISABLE KEYS */;
LOCK TABLES `dict6D` WRITE;
INSERT INTO `dict6D` VALUES (3884,1,'default','×iŗ\\XÐࢇ\\XÐ'),(3879,1,'outages1','凷'),(3875,1,'default','ǆ'),(3871,1,'default','ܮ8شʄ *ł§w|, ph<wஃ(ɞֲ &&౴ޗÄ'),(3885,1,'default','ս©'),(3881,1,'don','ˎ☋'),(3886,1,'default','ǲ'),(3856,1,'default','Q'),(3857,1,'default','='),(3863,1,'default','L4\"$'),(3864,1,'default','Y'),(3824,1,'default','ī'),(3825,1,'default','ı'),(3879,1,'sunday','⺧$'),(3879,1,'don','һ▒Ɯ'),(3881,1,'yyy','ឍ©'),(3881,1,'writing','ஜᙝƖࡺ'),(3844,1,'default','z)'),(3823,1,'default','u'),(3879,1,'typing','ⱞ'),(3848,1,'default','T5'),(3879,1,'level02','嶻Ý'),(3846,1,'default','Áb'),(3845,1,'default','h'),(3722,1,'don','L'),(3721,1,'threaded','ȶ!'),(3871,1,'writing','ᨇ᳸ᔈ'),(3865,1,'default','N'),(3871,1,'abuse','⠐'),(3880,1,'don','Ҿ'),(3883,1,'sunday','Ò$'),(3871,1,'don','ËिĆIԸῷØȾƚÌӻِGࣵ'),(3854,1,'default','r:V'),(3879,1,'expiration','࢈'),(3871,1,'sunday','⒒Ė'),(3871,1,'happen','֪㏧	ዶ&Ì'),(3880,1,'default','ભÞû,܇੖:F'),(3881,1,'timefield','৓	ഷ%#=2#<%#'),(3881,1,'default','ᳪ̮vIԧP'),(3827,1,'default','¹7!Õ'),(3822,1,'default','¢'),(3872,1,'default','ɉ'),(3879,1,'default','गжIŹŬʸL,?<ዿټʄƫௌЃÑԄú³ŋتŭĖN(S	੽ ĕ'),(3877,1,'default','ƥԣ'),(3876,1,'default','Ĩ'),(3875,1,'ifspeed','ê'),(3880,1,'writing','࿘'),(3880,1,'relationships','๎Ь'),(3843,1,'default','X'),(3827,1,'expiration','7'),(3829,1,'default','n'),(3829,1,'don','/'),(3831,1,'default',''),(3831,1,'don',''),(3833,1,'default','g<V'),(3834,1,'default','v'),(3835,1,'default','I'),(3837,1,'default','ï'),(3840,1,'default','6g\n'),(3840,1,'don','Û'),(3827,1,'don','ĝ'),(3722,1,'wins','$1>'),(3851,1,'default','S'),(3879,1,'relationships','㭥'),(3874,1,'default','ȍ,\n  \Z'),(3884,1,'don','Ϋ਋'),(3727,1,'default','^-'),(3728,1,'default','d<V'),(3729,1,'default','f;'),(3731,1,'default','JH'),(3732,1,'default','e<V'),(3734,1,'default','L'),(3735,1,'default','f'),(3737,1,'default','d<V'),(3738,1,'default','E'),(3739,1,'default','d<V'),(3740,1,'default','!	'),(3740,1,'don','ľ'),(3741,1,'default','~'),(3742,1,'default','L\n	'),(3743,1,'default','i\r'),(3744,1,'default',' +m!'),(3744,1,'don','Ɣ'),(3745,1,'default','R\Z['),(3748,1,'default','g<V'),(3751,1,'don','Ǿ'),(3752,1,'default','6(;'),(3752,1,'don','Ç'),(3754,1,'default','¸$$*Èo'),(3755,1,'default','H!'),(3756,1,'default','R9'),(3757,1,'default','\Z'),(3759,1,'don','ç'),(3761,1,'default','g<V'),(3762,1,'default','f'),(3763,1,'default','C.'),(3764,1,'default','/'),(3766,1,'default','U\\'),(3778,1,'default','á'),(3780,1,'default','Pē'),(3782,1,'default','I('),(3783,1,'5666','D'),(3783,1,'default','C\n'),(3784,1,'default','g<V'),(3785,1,'default','g<V'),(3787,1,'default','n'),(3790,1,'default','1		'),(3791,1,'default',''),(3793,1,'default','NC'),(3795,1,'default','A'),(3797,1,'default','P'),(3803,1,'default','w'),(3804,1,'default',''),(3808,1,'default','i'),(3811,1,'default','Q*\"'),(3814,1,'default','g<V'),(3815,1,'default','A'),(3818,1,'default','g<V'),(3819,1,'default','D+#L'),(3819,1,'don','q'),(3820,1,'default','E%G.\r'),(3820,1,'don','¾'),(3886,1,'relationships','ɥ'),(3886,1,'typing','¼'),(3887,1,'default','Ϻτ©'),(3887,1,'don','˃஑'),(3887,1,'relationships','ǳ'),(3887,1,'typing','ä'),(3888,1,'default','ĕ˻'),(3888,1,'don','ѡ'),(3889,1,'default','ţ˅'),(3890,1,'default','΄;SJ5Ǡ\n\nص*&ȟȒŏbɾÛ˟3рk'),(3890,1,'don','ɭ੅ʴZi6ģŌϹ'),(3890,1,'happen','ᬔ'),(3890,1,'writing','ͦ'),(3722,6,'wins','ŭ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict6D` ENABLE KEYS */;

--
-- Table structure for table `dict6E`
--

DROP TABLE IF EXISTS `dict6E`;
CREATE TABLE `dict6E` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict6E`
--


/*!40000 ALTER TABLE `dict6E` DISABLE KEYS */;
LOCK TABLES `dict6E` WRITE;
INSERT INTO `dict6E` VALUES (3879,1,'unchanged','㬤'),(3879,1,'discussed','嬞'),(3879,1,'disregard','᧮'),(3879,1,'https','ᦻ'),(3879,1,'rearrange','௬'),(3879,1,'text','ജዶ\nՑ᷒¹g	 ,ñ·Ĵۗඇù\Z@\Z'),(3891,1,'commands','õ>àÄʹ'),(3881,1,'https','㙒ȯ'),(3890,1,'text','ᤣ5'),(3880,1,'geographical','㧹ʽ'),(3880,1,'discussed','ᝉΙ'),(3881,1,'text','Ꮷ0Ã-!í\rů\rħ;Ŝ\n	*ใ	ű׌'),(3795,1,'text','h'),(3799,1,'commands','Ő'),(3805,1,'reg','\r\Z'),(3814,1,'jabber',''),(3827,1,'https','&'),(3837,1,'text','c'),(3838,1,'text','\''),(3880,1,'text','ᒎ৵H⢶'),(3880,1,'commands','ႉðN#˝Ũƿ֐ʭ¥ŢƶŉȃI1ʞғ;'),(3880,1,'intelligent','㢧'),(3871,1,'germany','䗞'),(3871,1,'impossible','乗'),(3871,1,'magnitudes','&'),(3871,1,'mmdd','␼'),(3871,1,'text','༺RƐǪɾՃı	⑎'),(3872,1,'commands','˝Ο	êSĜ#ĕ\r\rÚ>'),(3872,1,'text','૨'),(3869,1,'text','³'),(3840,1,'translating','ʝ'),(3841,1,'commands',''),(3856,1,'commands','ę'),(3890,1,'commands','ʈĩJT\rEeۥ6$3/˄ֿׂ1!Ǜ'),(3888,1,'commands','K԰'),(3885,1,'commands','ۻ'),(3885,1,'unchanged','ৌ'),(3886,1,'unchanged','ȝ'),(3887,1,'text','ʩtଝx'),(3793,1,'text','u'),(3787,1,'unchanged','Q'),(3787,1,'invert',',['),(3747,1,'text','\''),(3781,1,'dispersion','!b'),(3884,1,'commands','ڳH'),(3879,1,'commands','ƐయָఁԎōড়>ɶµୟ\Z?ƅֻZɝᭈ×'),(3892,1,'commands','\"\rÚ'),(3892,2,'commands',''),(3726,1,'text',''),(3814,6,'jabber','Ĩ'),(3881,1,'unchanged','ᵬ'),(3881,1,'diverse','❭'),(3871,1,'deltas','䥾Ŝ;&'),(3871,1,'discussed','㝒'),(3878,1,'https','Ƚǡ«ȼ'),(3877,1,'text','ϋǪ'),(3877,1,'policy','Ƴß'),(3873,1,'commands','ǏWĪ?m%%'),(3892,6,'commands','ǅ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict6E` ENABLE KEYS */;

--
-- Table structure for table `dict6F`
--

DROP TABLE IF EXISTS `dict6F`;
CREATE TABLE `dict6F` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict6F`
--


/*!40000 ALTER TABLE `dict6F` DISABLE KEYS */;
LOCK TABLES `dict6F` WRITE;
INSERT INTO `dict6F` VALUES (3759,1,'generate',' '),(3765,1,'filesys','-'),(3765,1,'performance',''),(3766,1,'filesys','s'),(3782,1,'performance','ĺ'),(3803,1,'generate','B'),(3808,1,'generate','G'),(3820,1,'intensive','ğ'),(3827,1,'redirects',')'),(3843,1,'generate','8'),(3845,1,'generate','I'),(3851,1,'performance','F,'),(3866,1,'performance','A,'),(3868,1,'performance','!'),(3871,1,'generate','ǖэদAĐ̥ۑ'),(3871,1,'performance','٦'),(3871,1,'started','ڣ⭅ԒᎯ'),(3872,1,'performance','ߌ'),(3872,1,'started','Ȗ'),(3873,1,'performance','K'),(3874,1,'generate','̈́'),(3874,1,'performance','/Ņ:ƍ\n'),(3875,1,'generate','ȑ'),(3875,1,'performance','/ď@\n'),(3876,1,'generate','Ź'),(3876,1,'performance','/}4\n'),(3877,1,'generate','ڛ'),(3878,1,'performance','र'),(3879,1,'birtviewer','ᮒ'),(3879,1,'generate','಼ᷡHෝবࢰŷᎻ'),(3879,1,'mapping','倁'),(3879,1,'performance','ǢÝ຃:ķ\\حؤ↎\';\Z9&C	\rJ	;eK<	»XR?4q\r%r'),(3879,1,'realm','卟'),(3879,1,'started','ļᷨؒBX='),(3880,1,'generate','Ίֳŗ⥨ÓʆϏдəā&˙'),(3880,1,'guis','Ꮑ²'),(3880,1,'performance','ĬâÐ͍¦,<V$\'ŋ\'ȅ̮եèŗđӐ\"8Dխǆŷರ>%3W:Y.෾'),(3890,1,'performance','ᑥ;RźĢ\n$($!!\n)\n))'),(3888,1,'generate','șcĊҚ'),(3887,1,'performance','ԍʃ'),(3892,1,'performance','ė'),(3735,1,'generate','F'),(3721,1,'performance','ǘ'),(3890,1,'started','ࡇ'),(3881,1,'generate','Ḇ'),(3880,1,'started','²ù&࡛łओ'),(3884,1,'performance','ͱɊޱɊ'),(3884,1,'generate','ɟ݀ˋ'),(3757,1,'generate','^'),(3743,1,'generate','f\r'),(3885,1,'generate','ԅ'),(3881,1,'started','♬'),(3881,1,'servicename','ঀ'),(3887,1,'generate','݆'),(3885,1,'performance','ˀʀ'),(3881,1,'redirects','⥃'),(3881,1,'preferences','㤣'),(3881,1,'performance','Ǡਆҍ\rஞͰ'),(3881,1,'intensive','❍');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict6F` ENABLE KEYS */;

--
-- Table structure for table `dict70`
--

DROP TABLE IF EXISTS `dict70`;
CREATE TABLE `dict70` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict70`
--


/*!40000 ALTER TABLE `dict70` DISABLE KEYS */;
LOCK TABLES `dict70` WRITE;
INSERT INTO `dict70` VALUES (3879,1,'knowledgebase','Ҋ'),(3879,1,'exception','䮠'),(3871,1,'rrdtutorial','ήⱪ'),(3890,1,'won','ʁ'),(3878,1,'31','Ը'),(3876,1,'passed','V'),(3875,1,'passed',''),(3874,1,'passed','z'),(3873,1,'passed','įĖ'),(3745,1,'starttls','£'),(3752,1,'admins','Ā'),(3759,1,'watch','v'),(3766,1,'requested','*'),(3780,1,'requested','%'),(3783,1,'passed','a'),(3798,1,'won','Ó'),(3809,1,'cload5','&'),(3827,1,'posting','ŝ'),(3837,1,'abbreviated','G'),(3841,1,'admins','q'),(3871,1,'31','┸Tż\n'),(3871,1,'6h','⓼Ƀ'),(3871,1,'abbreviated','⒉=᪠'),(3871,1,'learned','㷶ऒŎ߶'),(3871,1,'nest','ⳃ'),(3871,1,'passed','ƚ᫫᫤'),(3871,1,'posting','偓'),(3871,1,'requested','ᮋף'),(3881,1,'subscription','ʌ$'),(3881,1,'passed','ஂĈॹŃ¨Ôᴞ'),(3881,1,'knowledgebase','ʝ'),(3881,1,'internationalization','Ⱏ'),(3881,1,'applicable','࠿ᭊ'),(3880,1,'watch','ᥙ൐യÒ'),(3880,1,'tooltip','῜͓'),(3881,1,'won','㇛'),(3890,1,'revert','ḕk'),(3888,1,'repeat','ࡉ'),(3879,1,'subscription','ѹ$'),(3880,1,'knowledgebase','ҍϔ'),(3884,1,'revert','˱਋'),(3881,1,'rightchild','᫇1'),(3882,1,'learned','î'),(3879,1,'repeat','ಽ'),(3720,1,'won',';'),(3880,1,'passed','ፇJ'),(3887,1,'applicable','੨&'),(3880,1,'subscription','Ѽ$'),(3872,1,'zxvf','ǆ'),(3882,1,'abbreviated','É');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict70` ENABLE KEYS */;

--
-- Table structure for table `dict71`
--

DROP TABLE IF EXISTS `dict71`;
CREATE TABLE `dict71` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict71`
--


/*!40000 ALTER TABLE `dict71` DISABLE KEYS */;
LOCK TABLES `dict71` WRITE;
INSERT INTO `dict71` VALUES (3881,1,'business','ϵV៕'),(3879,1,'discard','⩄Ɯ'),(3872,1,'properties','ޤƣǀŪ'),(3879,1,'finish','࣒ǃ૜'),(3879,1,'directories','᭴⓶ᄁՄǜ'),(3871,1,'reads','‍ᖣʠ'),(3879,1,'procedures','䉟۬ตÂĤ\\M-ĳ'),(3873,1,'directories','Ɖ'),(3878,1,'discard','ϭ'),(3878,1,'business','Ő'),(3877,1,'procedures','ŋ'),(3877,1,'directories','ɕë'),(3875,1,'procedures','ˊ'),(3875,1,'discard','^'),(3879,1,'business','עV'),(3871,1,'finish','㬺'),(3881,1,'properties','ዦિʇe!	\n8İೞƳu'),(3879,1,'alerts','㷩᏿'),(3879,1,'apache2','ം+఍.\"ō☵ၱ¨Ƴ'),(3871,1,'properties','ޟƳЭ᫗'),(3871,1,'keyword','ᕇୃ'),(3881,1,'apache2','┕'),(3881,1,'alerts','ݝ'),(3880,1,'procedures','ও㪉'),(3880,1,'keyword','䜭 '),(3880,1,'directories','䒏'),(3880,1,'cloned','ᡆ⽡'),(3880,1,'business','ץV'),(3879,1,'reads','⁙⨜ॱ୙,'),(3880,1,'alerts','⨆ʽş̆ؔ'),(3879,1,'properties','࣌ዸץ\'\nוԒĨ¡>ǤӈĀ%\n̏H৴൪ຏ'),(3881,1,'logmessagedaoimpl','⏹'),(3881,1,'hash','ऒ\n\n\r	'),(3881,1,'errortype','ࢋ᭏'),(3871,1,'elaborate','⼷'),(3871,1,'directories','⻎ñ9'),(3871,1,'compute','ባ'),(3854,1,'quit','5V\r'),(3833,1,'quit','2V'),(3818,1,'quit','2V'),(3814,1,'quit','2V'),(3816,1,'75','/'),(3785,1,'quit','2V'),(3720,1,'finish','d'),(3721,1,'business','?'),(3728,1,'quit','/V'),(3729,1,'quit','0'),(3732,1,'quit','/W'),(3737,1,'quit','/V'),(3739,1,'quit','/V'),(3747,1,'ide',':'),(3748,1,'quit','2V'),(3756,1,'radius',''),(3759,1,'keyword','a'),(3759,1,'string1','A'),(3761,1,'quit','2V'),(3778,1,'tns','#'),(3784,1,'quit','2V'),(3881,1,'reads','Ἢ'),(3882,1,'apache2','v'),(3883,1,'properties','834'),(3884,1,'cloned','଼'),(3884,1,'properties','Ĥhई2ҷÏƚ¢'),(3885,1,'properties','ī·׭(cvT'),(3886,1,'properties','[9ĲLJ'),(3887,1,'discard','ϑ '),(3887,1,'properties','\"þ(ƏÛ\"0҄$4ã8232̅®%5M'),(3888,1,'properties','ÙîĤ$ʍ'),(3888,1,'reads','࣏'),(3889,1,'elaborate','̒'),(3889,1,'properties','y$'),(3890,1,'alerts','ݐ'),(3890,1,'finish','Ặ'),(3890,1,'netalarm','ྮ'),(3890,1,'properties','Ʊͪ˓ޕևֲ'),(3890,1,'reads','ằ'),(3891,1,'properties','M)/ʐ+4C34'),(3892,1,'properties','Ý'),(3756,6,'radius','ÿ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict71` ENABLE KEYS */;

--
-- Table structure for table `dict72`
--

DROP TABLE IF EXISTS `dict72`;
CREATE TABLE `dict72` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict72`
--


/*!40000 ALTER TABLE `dict72` DISABLE KEYS */;
LOCK TABLES `dict72` WRITE;
INSERT INTO `dict72` VALUES (3765,1,'2',']A'),(3880,1,'scripting','ស'),(3880,1,'offers','ѡʢÊĪ'),(3879,1,'2','\"n\Zf²T.»¶TÑ<^¯ظȳŮ̞ȍΝҳؖȜȶ?Ť9θ܁²ˡΑҟ˪ǭŲƾ7ăȗҞΞ.ȆՉTHA?2GŐALȹĶ'),(3880,1,'2','\"n\"^a\"¸¶n&Ķ<^̓m20˖ׁǿ8)Ξϵ;F ᒽħĉbÅ§૮PÍ/'),(3727,1,'3493','_'),(3880,1,'scrolling','݃ᛐՓ'),(3880,1,'restarted','㋵'),(3880,1,'launcher','ඒR3✬'),(3782,1,'90','Ç'),(3781,1,'milliseconds','d'),(3765,1,'90','Ö'),(3726,1,'pay','?'),(3721,1,'offers','ȇ'),(3799,1,'2',''),(3871,1,'cf','ҀûŴɶN׋ۣ?ͦÂ؄JNࡖÅ'),(3871,1,'offers','џ'),(3877,1,'locked','ш'),(3874,1,'90','äǧ'),(3875,1,'2','Ďȅ'),(3877,1,'2','0'),(3873,1,'2','Ń'),(3872,1,'cf','ìŁʈV'),(3872,1,'restarted','ӫ'),(3872,1,'2','˧hº,;ع'),(3871,1,'tm','ჰ2'),(3871,1,'speeding','䒚'),(3871,1,'popping','ਟ'),(3880,1,'vendor','ਅ'),(3879,1,'vendor','ᔱ'),(3879,1,'offers','ў'),(3879,1,'launcher','࢞ē±ƚ7ƽ²ڑR/Ǽⰵࡵ'),(3878,1,'2',')'),(3878,1,'39','ګ'),(3805,1,'2',''),(3811,1,'2','4M'),(3819,1,'2','E'),(3819,1,'seclevel','±'),(3820,1,'2','F'),(3820,1,'seclevel','X'),(3822,1,'milliseconds','·'),(3831,1,'2','º'),(3835,1,'2',''),(3837,1,'2','ĝ'),(3840,1,'vendor','ŧ'),(3841,1,'2','_-'),(3856,1,'2','gĂ'),(3868,1,'2','&'),(3870,1,'scripting',''),(3871,1,'10e6','ጰ'),(3871,1,'2','Þэ঍Ôʈԟ	ɔKŪȡ̫̥LƞΛǊóĦǕÉԡȠȦב`4#Ù\nJ	\r֮ê'),(3871,1,'90','≢Ꮈ'),(3871,1,'cdef','Żค݊âĂk%ÅȫŐW\r\".<૟ÈhĮຆ3­¯	ጪ#'),(3757,1,'2','Ŋ'),(3762,1,'milliseconds','³'),(3753,1,'2','c'),(3751,1,'holt',')'),(3751,1,'2','ɱ'),(3744,1,'vendor','='),(3744,1,'seclevel','p,'),(3881,1,'2','\"nL0*NÞ<^Ú͍ᯬLǉ«YƳ˄ࠜЌ'),(3881,1,'isfailurepredictionenabled','࡚'),(3881,1,'launcher','⮐'),(3881,1,'offers','ɱ'),(3881,1,'restarted','㜞'),(3881,1,'timeframe','ᝤ©'),(3884,1,'2','Åਿ'),(3884,1,'parallelize','͏਋'),(3885,1,'2','Ťڐ'),(3885,1,'checkes','ϳ'),(3887,1,'2','Ʒ1ࡕ'),(3888,1,'2','Oñў˃l'),(3889,1,'2','˞Ǻ'),(3890,1,'2','ԍ\nਸඊk'),(3890,1,'restarted','ຩ'),(3890,1,'timeframe','ስŚ'),(3891,1,'2','˒˗'),(3892,1,'2','Ó'),(3879,2,'2',''),(3880,2,'2',''),(3881,2,'2','');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict72` ENABLE KEYS */;

--
-- Table structure for table `dict73`
--

DROP TABLE IF EXISTS `dict73`;
CREATE TABLE `dict73` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict73`
--


/*!40000 ALTER TABLE `dict73` DISABLE KEYS */;
LOCK TABLES `dict73` WRITE;
INSERT INTO `dict73` VALUES (3751,1,'ds','ɡ'),(3871,1,'ee','ۊٽƦ႞^_èفȖ˗ί̀'),(3879,1,'places','㺘'),(3880,1,'trend','㑐ϗǚWIQľ[[LKAjg£ēA@AüL'),(3880,1,'launch','ᡠ⼒'),(3880,1,'configured','ᄉT̚ॴڒB'),(3879,1,'variety','Ѡ'),(3879,1,'techniques','Ēᰔ'),(3871,1,'systematic','ư'),(3879,1,'launch','ᖒ'),(3879,1,'notifications1','凬'),(3879,1,'featured','ῢ'),(3879,1,'covers','ᴩƘ'),(3879,1,'ds','䙛ƒ+ƺ'),(3871,1,'rates','ઈƪK'),(3871,1,'graphics','ᰰᳵèȪŠٮʰक़'),(3720,1,'graphics','\''),(3733,1,'logfiles','\"'),(3871,1,'ds','ŀȪ˵űS̸¢2¦Ŀۮੌşؐp\Z´Åట\Z#ௐ׫ټ'),(3798,1,'rates',' '),(3798,1,'ee','µ'),(3783,1,'configured',''),(3780,1,'ds','õ'),(3778,1,'configured','Ï'),(3765,1,'45','¬'),(3752,1,'ds','_'),(3751,1,'trend','/	;+#]-r8)'),(3879,1,'configured','ᄼ࣠¤୔fѢɤᒗ¤ŉይ'),(3879,1,'45','⻽'),(3878,1,'45','ߘ'),(3876,1,'3000',''),(3873,1,'places','ȹ'),(3871,1,'drawn','ᘂ⋈'),(3871,1,'points','৤MȬ─'),(3871,1,'placing','ᴦ'),(3871,1,'places','ボ@ᙻ'),(3799,1,'ee','Ŷ'),(3871,1,'divides','ᝧ'),(3871,1,'configured','ယ'),(3871,1,'45','❎႞'),(3871,1,'3days','ⓡ'),(3871,1,'3am','▱'),(3871,1,'3000','䤘'),(3840,1,'configured','ÔG'),(3841,1,'45','>%'),(3827,1,'onredirect','ư'),(3872,1,'preinstalled','ę'),(3872,1,'places','ٻ'),(3872,1,'configured','˿Ȼď'),(3880,1,'variety','ѣ㻇'),(3881,1,'configured','ై⫢ť'),(3881,1,'covers','Ⳕ'),(3881,1,'credentials','㗰'),(3881,1,'featured','ߝ'),(3881,1,'points','ౙǾጨ'),(3881,1,'rpms','㓾'),(3881,1,'variety','ɳ'),(3883,1,'45','Ĩ'),(3886,1,'configured','П'),(3887,1,'drawn','ʣTଟX'),(3888,1,'beneficial','߬'),(3888,1,'configured','ϳ'),(3888,1,'points','ŗ'),(3890,1,'configured','Ყ'),(3890,1,'graphics','Ȑ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict73` ENABLE KEYS */;

--
-- Table structure for table `dict74`
--

DROP TABLE IF EXISTS `dict74`;
CREATE TABLE `dict74` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict74`
--


/*!40000 ALTER TABLE `dict74` DISABLE KEYS */;
LOCK TABLES `dict74` WRITE;
INSERT INTO `dict74` VALUES (3872,2,'wmi',''),(3873,7,'wmi','ը'),(3887,1,'negatively','ޟ'),(3887,1,'settings','ϻ'),(3888,1,'wmi','մÒ*;\ZÎ\n	%5	'),(3871,1,'bound','ᔮ'),(3875,1,'settings','Ʃ'),(3871,1,'skip','ᦓ᛹سণ'),(3873,1,'wmi','C%%ŗ\n\n\n\n\n			\r\r'),(3873,6,'wmi','թ'),(3887,1,'demand','؟Ŭ'),(3871,1,'lu','ᑙᩣę'),(3871,1,'978302100','䣹'),(3871,1,'12422','㠑;'),(3863,1,'debug','k'),(3851,1,'debug','I'),(3850,1,'debug',',	'),(3730,1,'mandatory','_'),(3734,1,'mandatory',''),(3744,1,'bound','Ǫ'),(3744,1,'skip','Ș'),(3744,1,'unsigned','ɓ'),(3751,1,'debug','u'),(3797,1,'debug','^'),(3803,1,'mailq',''),(3804,1,'debug',''),(3805,1,'skip','`'),(3806,1,'skip','f'),(3823,1,'debug','Â'),(3830,1,'debug','/'),(3837,1,'debug','þ'),(3840,1,'debug','ƌ'),(3846,1,'mb','¾'),(3881,1,'integrator','⏮'),(3881,1,'express','⎤'),(3881,1,'debug','⎹q'),(3879,1,'skip','傫'),(3879,1,'tandem','娗'),(3880,1,'fixed','⢱/T/Ҥ/Ɠ'),(3880,1,'manageability','ᛸ'),(3880,1,'settings','ቁึ'),(3880,1,'wmi','⎽⊽'),(3871,1,'fixed','İ7⽒'),(3871,1,'ffffff','㧕'),(3878,1,'wmi','ؗ\r\r\r'),(3876,1,'settings','ċ'),(3877,1,'skip','Ϫ'),(3881,1,'programmers','מ'),(3881,1,'pressed','⩱'),(3730,1,'debug',':'),(3879,1,'debug','䉕؀*C3৓\r\n:\n\nT\n\nē{୮\n	J	6\n	\r'),(3879,1,'mount','᷶'),(3879,1,'paged','㏨ϧ'),(3879,1,'referer','ᨰ\'!Ç\Z4'),(3879,1,'settings','ུȍૐQ@$ԵͪڌࡐకġೆóȕʉñႳ'),(3890,1,'demand','ଦ'),(3890,1,'aggressively','ᕾ'),(3803,6,'mailq','è'),(3873,2,'wmi',''),(3886,1,'settings','җ'),(3881,1,'unidirectional','ẋ'),(3885,1,'demand','ϡũ'),(3885,1,'negatively','՞'),(3885,1,'settings','È,¢Ê'),(3890,1,'settings','9ňحౄ'),(3871,1,'demand','¹'),(3871,1,'cluttered','ሺ'),(3720,1,'debug','ä)\r'),(3874,1,'settings','ǰ'),(3720,1,'fixed','Ŷ'),(3722,1,'debug','ŕ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict74` ENABLE KEYS */;

--
-- Table structure for table `dict75`
--

DROP TABLE IF EXISTS `dict75`;
CREATE TABLE `dict75` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict75`
--


/*!40000 ALTER TABLE `dict75` DISABLE KEYS */;
LOCK TABLES `dict75` WRITE;
INSERT INTO `dict75` VALUES (3765,1,'usr','g'),(3776,1,'free','â'),(3778,1,'free',''),(3780,1,'free','°	'),(3787,1,'usr','{'),(3789,1,'usr',''),(3792,1,'microsoft','\Z'),(3800,1,'free',')	'),(3822,1,'40','?'),(3830,1,'module',''),(3840,1,'infrastructure','Ã'),(3840,1,'usr','ʐ'),(3841,1,'40',':%'),(3846,1,'free','(;'),(3862,1,'free','_'),(3862,1,'module','v9'),(3865,1,'40','^'),(3720,1,'infrastructure','HÞ'),(3879,1,'s2chapter2b','廞J'),(3879,1,'exposed','᫔'),(3879,1,'free','>ᶫO፨'),(3879,1,'infrastructure','˘ÿ᳹'),(3879,1,'module','ᆱ'),(3871,1,'free','䵻'),(3879,1,'ranges','⺵'),(3878,1,'free','>'),(3871,1,'ranges','ቴ⿺'),(3878,1,'40','۠'),(3871,1,'module','Į⥭'),(3871,1,'justified','Ჷ'),(3879,1,'usr','೿+E˗Âȷo¥ү.\"ōÕᓽߒࡧɋR18Ǳɛ²©ʹāɸ(L1Í6M1(Sy		%	yƣܬĞx6ÀYVägǔ'),(3877,1,'usr','ʵЇ'),(3874,1,'free','ȳ'),(3873,1,'microsoft','N#K'),(3872,1,'usr','ɫƘ,ǬĘ'),(3871,1,'rrdresize','❻'),(3878,1,'microsoft','ʞº҈'),(3877,1,'owned','ۗ'),(3877,1,'mkdir','Ӭ'),(3877,1,'free','>'),(3875,1,'usr','ʛ'),(3865,1,'module','%'),(3869,1,'module','É'),(3871,1,'40','ကਚᯗȇȭD2'),(3765,1,'35514044640914','­'),(3879,1,'serviceoutput','䐲'),(3757,1,'usr','ť'),(3757,1,'owned','Ź'),(3757,1,'ranges',')Ā'),(3734,1,'free',' 6'),(3736,1,'free','!'),(3742,1,'module','$'),(3744,1,'ranges','Ǉb'),(3754,1,'usr','Ł1\Z%59%'),(3756,1,'compromise','ë'),(3880,1,'free','>'),(3880,1,'infrastructure','ìŋĊјli2mۛٽè4ǽ>Ṕ'),(3880,1,'usr','ᓍ'),(3881,1,'addactionlistener','㈤2'),(3881,1,'free','>'),(3881,1,'infrastructure','ƧҢ⑉'),(3881,1,'module','Ҹϻ)Ĭ⅗ࣝQ#\ZO'),(3881,1,'usr','୪᠌ċF>\r\r$(!;;໗'),(3882,1,'usr','s'),(3883,1,'ranges','à'),(3884,1,'free','ܪ\n'),(3884,1,'substitutions','ჰ'),(3885,1,'usr','e'),(3886,1,'drives','б$'),(3887,1,'usr','಩¡'),(3888,1,'module','Ȭ]'),(3889,1,'ranges','Ƹ'),(3890,1,'usr','ٚ¥·ᖒ'),(3892,1,'substitutions','ƙ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict75` ENABLE KEYS */;

--
-- Table structure for table `dict76`
--

DROP TABLE IF EXISTS `dict76`;
CREATE TABLE `dict76` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict76`
--


/*!40000 ALTER TABLE `dict76` DISABLE KEYS */;
LOCK TABLES `dict76` WRITE;
INSERT INTO `dict76` VALUES (3879,1,'dn','ᲂ'),(3879,1,'dependency','㍖¬'),(3879,1,'contact','Ԥ\\:оᨛѰҴJ4ì%ź	Ƒ¬Ϙ#ϡ_x! ! UƱᐤ'),(3804,1,'dn','M'),(3871,1,'920808300','㡉z'),(3879,1,'activating','→'),(3872,1,'contact','Tؠė\n\Z,-\n\"*0 \r\rQ-%	\Z	\Z\n	'),(3881,1,'yyyy','᝶©'),(3811,1,'dn','#U'),(3870,1,'model','y'),(3871,1,'1day','ဠᆲԶ̓'),(3880,1,'dependency','ቹ'),(3880,1,'desktop','ᡲ'),(3880,1,'operates','໺'),(3880,1,'contact','ԧ\\:غbòϠ´Ū⡉ȱ\r\Z\rE¨	'),(3879,1,'newly','侷'),(3879,1,'operates','⃻'),(3879,1,'portable','㒫'),(3879,1,'windowheader','崶_÷'),(3881,1,'newly','ㇴ'),(3881,1,'model','ف᧜s'),(3780,1,'contact',''),(3766,1,'contact',''),(3765,1,'contact',')'),(3721,1,'model','@'),(3720,1,'docmentation','Ġ'),(3720,1,'accordioncontainer','ŵ'),(3881,1,'desktop','⨝'),(3881,1,'listeners','ệഢ'),(3880,1,'yyyy','⢩өƯ'),(3880,1,'prepares','ྠ'),(3884,1,'contact','НȘяΕȘˏ'),(3881,1,'contact','̷\\:⁀'),(3884,1,'dependency','Kݼ\Z\r ࢽ	l'),(3885,1,'contact','ؓþ'),(3885,1,'dependency','࢚'),(3885,1,'desktop',''),(3887,1,'contact','ࡔý§<Ȇ'),(3887,1,'dependency','࿫\r'),(3888,1,'contact','Č		\n'),(3888,1,'unmodified','ʶ'),(3889,1,'contact','̙d)É'),(3890,1,'contact','Ᏽ'),(3890,1,'iso8601','ᯣ'),(3890,1,'yyyy','ᯘ'),(3891,1,'contact','#%@-, +-D &\n	+-,D .');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict76` ENABLE KEYS */;

--
-- Table structure for table `dict77`
--

DROP TABLE IF EXISTS `dict77`;
CREATE TABLE `dict77` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict77`
--


/*!40000 ALTER TABLE `dict77` DISABLE KEYS */;
LOCK TABLES `dict77` WRITE;
INSERT INTO `dict77` VALUES (3883,1,'save',''),(3882,1,'save',''),(3881,1,'studio','ᰔ'),(3881,1,'save','⹵'),(3880,1,'foundation','9>ʳÁ˽å݌⡋ൌ'),(3878,1,'interfaces','Ϻ'),(3878,1,'foundation','0>Ĥ'),(3879,1,'mysql','͚\n༰]෾࿮ℯǸဴ[	\"#	§11'),(3721,1,'mysql','		8\"Y(\n\n\n\n('),(3880,1,'colored','㍯'),(3879,1,'save','ࣔႂ͎2ॺťԔʏȄ࢈ᒷ।'),(3884,1,'defines','مƠ'),(3879,1,'openmanage','ᔘ'),(3869,1,'mysql','b'),(3881,1,'mysql','೐ᙶ\nЊġ'),(3869,1,'interfaces',''),(3869,1,'foundation',''),(3780,1,'uprb','ü'),(3790,1,'mysql','		'),(3791,1,'mysql','#'),(3793,1,'mysql','	z'),(3820,1,'interfaces','ë'),(3830,1,'foundation',')'),(3837,1,'pop3','?@\n	U'),(3837,1,'save','±'),(3853,1,'insight',''),(3720,1,'waiting','_'),(3879,1,'hostgroupname','拿'),(3871,1,'xfiles','ৃ'),(3760,1,'pop3','\n'),(3740,1,'oidname','5l'),(3720,1,'foundation',''),(3734,1,'interfaces','º'),(3733,1,'foundation','\"'),(3872,1,'defines','ԌŢ'),(3881,1,'foundation','9>1\"Fäʌ>#?\r\"><\'	(#G\nʺ2	\r	\Z*\\\rŠ؊Ɗ*͑ƺ­;\'\nćP	!)¯3&ɾë%	\n'),(3881,1,'defines','ᴊ֣H੭'),(3881,1,'assignments','ቹ7'),(3880,1,'save','㖽'),(3880,1,'openmanage','৪'),(3880,1,'mysql','˄\nᒸ⬭͢'),(3880,1,'interfaces','࿒'),(3880,1,'insight','Ř۩´ÝţⰍØY˝ѱˏ'),(3881,1,'interfaces','⥗'),(3881,1,'insight','Ą⎕K\r'),(3881,1,'homepage','⾵ψد'),(3875,1,'save','̡'),(3875,1,'interfaces','káė\rr	\n'),(3871,1,'tz','ⶩ'),(3721,1,'interfaces','Ɍ'),(3879,1,'level01','嵏!@ú'),(3879,1,'insight','ȆၘʷⲸD໰uᏛ'),(3879,1,'preflight','ざ#'),(3878,1,'ease','Ć'),(3877,1,'foundation','7>'),(3877,1,'save','ǧþ'),(3881,1,'glue','Ⰽ'),(3879,1,'foundation','9>ŵǔ൫Á/൜ↅ.࠻&we©'),(3879,1,'defines','᫶⎕჊ঋ'),(3879,1,'belongs','姄'),(3878,1,'pop3','˕Ա'),(3878,1,'mysql','ƓΓ'),(3870,1,'mysql','¼\Z'),(3871,1,'averaged','䈠			'),(3871,1,'defines','ߎāöNઠ'),(3871,1,'insight','ߝ'),(3871,1,'interfaces','㾺Ö		,'),(3871,1,'pipes','ȹм'),(3871,1,'stage','௷'),(3884,1,'save','Ƒ֡ǫǣ,҃Âʧ'),(3885,1,'defines','ܱ'),(3885,1,'save','Åċװ	\Z'),(3886,1,'interfaces','Ӊ'),(3886,1,'save','a9ǆLJ'),(3887,1,'defines','ȸ'),(3887,1,'save','Á΍	rӄ3Ģ\Z²ͣ¢['),(3888,1,'save','ßڵí'),(3888,1,'stage','̬'),(3889,1,'save','ѐ'),(3890,1,'gid','ট'),(3890,1,'save','à?ͪ\rˆن\nªևֲǛA'),(3891,1,'save','¢̆@'),(3721,2,'mysql',''),(3721,6,'mysql','ʣ'),(3760,6,'pop3','M'),(3791,6,'mysql','º'),(3793,6,'mysql','²'),(3830,6,'foundation',''),(3853,6,'insight',']'),(3721,7,'mysql','ʢ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict77` ENABLE KEYS */;

--
-- Table structure for table `dict78`
--

DROP TABLE IF EXISTS `dict78`;
CREATE TABLE `dict78` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict78`
--


/*!40000 ALTER TABLE `dict78` DISABLE KEYS */;
LOCK TABLES `dict78` WRITE;
INSERT INTO `dict78` VALUES (3871,1,'logarithmic','Ċํؼ'),(3871,1,'kmh','㮮I5'),(3887,1,'returns','ܵ'),(3887,1,'selected','ϤüӸĻ²ѿ'),(3871,1,'properly','䨸'),(3886,1,'properly','Ł'),(3833,1,'initiate',''),(3831,1,'map','+<\n'),(3827,1,'returns','ɮ'),(3825,1,'returns','%%#'),(3824,1,'returns','$&=n\''),(3887,1,'map','Ɏ\r\rਁ«'),(3788,1,'returns','Ç'),(3787,1,'returns','\r'),(3880,1,'viewed','ᗂ'),(3880,1,'table','ߘÕϢͩޤŊTŜ,rČWįã0/ĖңQĒʢҕզ¬Q˺0uƣ&NN&\r̪&~4Ļ&ʁŎ'),(3880,1,'selected','᥍ ˰ӛϜ+ÐഖȞ\n\"ʓ˻؂Ѻ'),(3880,1,'initiate','⟏'),(3880,1,'map','࠾˚Ƭ'),(3880,1,'providing','२ʴⲧ'),(3880,1,'engine','ິ'),(3885,1,'table','ĭ¨՜!b'),(3885,1,'selected','ʳ'),(3885,1,'returns','Ӵ'),(3884,1,'table','ĦZ7঺\ZDѳ¹1ũ'),(3885,1,'engine','ų'),(3883,1,'table','Mf'),(3884,1,'returns','Ɏ਋'),(3884,1,'selected','Õë\ZܿƤࢰ2'),(3883,1,'selected',''),(3778,1,'properly','Î'),(3765,1,'root','ª'),(3764,1,'table',''),(3761,1,'initiate',''),(3757,1,'root','Ū'),(3753,1,'providing','B'),(3748,1,'initiate',''),(3752,1,'returns','|'),(3739,1,'initiate',''),(3737,1,'initiate',''),(3720,1,'ondomload','S'),(3878,1,'table','ś'),(3881,1,'root','ⴶĎbȤŀ'),(3886,1,'table','^võA>'),(3886,1,'selected','ƚÖMI'),(3879,1,'table','ГҼࡽ.ôE¶lLƨĤన4ζӼwę§<ƙMᤴ͖@6Mࢦ\nԞ]²=±}'),(3881,1,'properly','์⣟'),(3881,1,'meant','ൕᩪRɑ'),(3881,1,'engine','ೈ2'),(3881,1,'map','۸'),(3871,1,'root','㻿'),(3871,1,'returns','⦦ପ௹'),(3862,1,'root',''),(3879,1,'selected','ཫࠆ໤#νƕΉĜশneëሴǧd'),(3854,1,'initiate','³'),(3871,1,'engine','䀃'),(3873,1,'root','Ƃ'),(3874,1,'properly','ț'),(3874,1,'root','ſļ'),(3871,1,'oo','ᥗ'),(3871,1,'switch','ව޻Ã'),(3868,1,'providing',''),(3881,1,'providing','⚈ÍÂ˄'),(3732,1,'initiate',''),(3728,1,'initiate',''),(3887,1,'properly','ũ'),(3879,1,'engine','㷨ᤀѽŹ'),(3879,1,'returns','ᵹđ'),(3856,1,'keyfile',''),(3789,1,'table','?'),(3790,1,'root',';'),(3793,1,'properly',''),(3793,1,'table','}'),(3795,1,'properly','|'),(3795,1,'table','p'),(3798,1,'switch','!'),(3803,1,'root','Ç'),(3804,1,'returns',' '),(3809,1,'wloadn','A'),(3814,1,'initiate',''),(3818,1,'initiate',''),(3822,1,'meant',''),(3822,1,'switch','c'),(3823,1,'returns','*'),(3871,1,'920805603','侊'),(3871,1,'18','ጹᐋ'),(3870,1,'engine','s'),(3868,1,'viewed','b'),(3887,1,'table','lÉ7Ɣ\rçPңVā6TcЮK'),(3879,1,'switch','㠃'),(3780,1,'table','Đ	'),(3878,1,'selected','ऑ'),(3881,1,'table','ȟӡ1ሢVV(ڨʊ'),(3872,1,'notepage','չ'),(3872,1,'meant','܆'),(3882,1,'selected','Ž'),(3881,1,'returns','ऍ\r%fɱ\re0֋!¦)2į\ZäȊ\Zɽ\n'),(3878,1,'18','ͪ'),(3871,1,'selected','ះ\Zᖷ'),(3877,1,'root','Żǵ'),(3877,1,'keyfile','ۉ\'8\"'),(3876,1,'properly','Ķ'),(3779,1,'table','\n'),(3720,1,'engine','/'),(3875,1,'properly','ǔ'),(3785,1,'initiate',''),(3879,1,'map','౨\\ֻᚋཱྀᔺ§'),(3879,1,'properly','⡕ឯࣔ஘P'),(3863,1,'root','x'),(3722,1,'returns',''),(3871,1,'nicely','㨶'),(3873,1,'returns','Đ)Ƃ'),(3872,1,'returns','ѿ'),(3879,1,'providing','ᇜཹз'),(3879,1,'root','ጱň㶖Ή฾+8j\r	*	'),(3837,1,'returns','j'),(3837,1,'root','ē'),(3784,1,'initiate',''),(3888,1,'inactive','ƍ'),(3888,1,'table','Ü$'),(3889,1,'engine','ҥ'),(3889,1,'selected','ΖB'),(3889,1,'table','\"'),(3890,1,'selected','ᇚ\n¤਱'),(3890,1,'table','ºG³͘ʺރյ֠\Zǘȍ¨'),(3891,1,'selected','«ÌȻÆ'),(3891,1,'table','gVʬ]ae'),(3892,1,'selected','Lj'),(3892,1,'table','yf '),(3779,6,'table','?');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict78` ENABLE KEYS */;

--
-- Table structure for table `dict79`
--

DROP TABLE IF EXISTS `dict79`;
CREATE TABLE `dict79` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict79`
--


/*!40000 ALTER TABLE `dict79` DISABLE KEYS */;
LOCK TABLES `dict79` WRITE;
INSERT INTO `dict79` VALUES (3871,1,'coverage','὿'),(3871,1,'formatting','Ᲊ'),(3871,1,'input','ٜĨ̝̀ၥଃǶі\n\nባôªޣ\n'),(3881,1,'platform','ᰨ៼ÞȐ'),(3875,1,'input','T'),(3876,1,'bolded','ī'),(3878,1,'input','ϣ'),(3879,1,'aspect','㇋ʇǼ'),(3879,1,'formatting','埾'),(3879,1,'input','ٖᨳ؈	'),(3871,1,'ahead','᧐ᚽ'),(3855,1,'input','+'),(3869,1,'platform',''),(3871,1,'4294967297','䵝'),(3871,1,'920805900','㠭~᚛:'),(3881,1,'ahead','⛶ԟ'),(3880,1,'utilizes','ិ'),(3880,1,'manager','২'),(3879,1,'maxn','䕲'),(3880,1,'input','ٙࡗ@'),(3872,1,'input',''),(3872,1,'mailtools','ŚH'),(3874,1,'bolded','Ȑ'),(3881,1,'input','ѫᥪʲ¢Ī৹ψù¦Ɠ'),(3889,1,'manager','̾'),(3884,1,'dbservices','ᐘ'),(3872,1,'7b','ė'),(3720,1,'editor2','ų'),(3875,1,'bolded','ǉ'),(3879,1,'manager','ݸ۔ۊ'),(3744,1,'substr','ɇ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict79` ENABLE KEYS */;

--
-- Table structure for table `dict7A`
--

DROP TABLE IF EXISTS `dict7A`;
CREATE TABLE `dict7A` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict7A`
--


/*!40000 ALTER TABLE `dict7A` DISABLE KEYS */;
LOCK TABLES `dict7A` WRITE;
INSERT INTO `dict7A` VALUES (3891,1,'notify','ǫ,=ȹ<=<'),(3884,1,'process','֫Ϟح'),(3884,1,'retrying','ɣ਋'),(3885,1,'hand','ࡽ'),(3885,1,'notify','غ'),(3885,1,'process','ʿ'),(3884,1,'notify','ц਋'),(3879,1,'shm','ặ'),(3887,1,'retrying','݊'),(3881,1,'hand','㓮'),(3887,1,'process','є©'),(3887,1,'notify','ࡻ'),(3881,1,'itgroundwork','⎄\\'),(3881,1,'utils','┲'),(3881,1,'process','Ⴛᗠ̺'),(3881,1,'php','ÀϪ¡zǆ͙ \r\nA)\'೔Ք٣ƪaU·UYz$ŅŷÇö&ȠľѡO­ĳ'),(3881,1,'passeddb','ๅ'),(3880,1,'discipline','ᔮ'),(3880,1,'dual','ܸᰨ'),(3880,1,'enhanced','ຒ¹'),(3880,1,'hand','ๅ'),(3880,1,'php','ɹ׉މЊΫ⺼'),(3887,1,'hand','ɸ\"୐\"'),(3885,1,'retrying','ԉ'),(3879,1,'utils','兗ŧÍ6ǡ		%	'),(3720,1,'process','B'),(3880,1,'process','ຌ	ش၀ά'),(3890,1,'process','ɞpò+ຆӍ\n6%\n%\n=\n=\nő>Џ'),(3881,1,'notify','㊠'),(3721,1,'process',''),(3728,1,'cert','F'),(3729,1,'cert','G'),(3732,1,'cert','F'),(3737,1,'cert','F'),(3739,1,'cert','F'),(3740,1,'process','¾'),(3741,1,'process','\nS4'),(3744,1,'hand','ð'),(3744,1,'inclusive','ǉE'),(3748,1,'cert','I'),(3756,1,'process','Ë'),(3757,1,'process','Û'),(3758,1,'process',''),(3759,1,'process','\"		\n\n'),(3761,1,'cert','I'),(3766,1,'process','|'),(3778,1,'process','R'),(3782,1,'2f','Ű'),(3782,1,'process','İ'),(3784,1,'cert','I'),(3785,1,'cert','I'),(3789,1,'process','!+'),(3793,1,'process','|'),(3795,1,'process','o'),(3814,1,'cert','I'),(3818,1,'cert','I'),(3829,1,'php','F'),(3833,1,'cert','I'),(3854,1,'cert','L'),(3856,1,'process','Ũ'),(3868,1,'process','3'),(3870,1,'php','&	\nI%'),(3871,1,'bother','㬚'),(3871,1,'converts','―'),(3871,1,'digits','䮌'),(3871,1,'hand','▾Oೕᇣ'),(3871,1,'inclusive','ፃ'),(3871,1,'measured','㪒ÈघƧ'),(3871,1,'php','ɫ'),(3871,1,'process','٬ݶ⍀ö'),(3872,1,'notify','ܦ{Ĳ(<-)'),(3872,1,'process','ё¡'),(3873,1,'process','Έ'),(3874,1,'process','üÀČÃ'),(3875,1,'process','Ƒ'),(3876,1,'process','ó'),(3877,1,'process','£š~'),(3878,1,'process','ńˇȀ'),(3879,1,'cert','ᥩ'),(3879,1,'hand','‗'),(3879,1,'notify','⣘૿ϧГţ'),(3879,1,'php','̏ܥ̎IȻƿ'),(3879,1,'process','ᅾേ଄4Ï	4ֱbnƷơ¦೓_\Z\nь\rȮϨբ\'\'Êةх˴'),(3879,1,'scriptalias','ഓ'),(3892,1,'notify','ď'),(3870,2,'php',''),(3741,6,'process','Å'),(3870,6,'php','ĩ'),(3870,7,'php','Ĩ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict7A` ENABLE KEYS */;

--
-- Table structure for table `dict7B`
--

DROP TABLE IF EXISTS `dict7B`;
CREATE TABLE `dict7B` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict7B`
--


/*!40000 ALTER TABLE `dict7B` DISABLE KEYS */;
LOCK TABLES `dict7B` WRITE;
INSERT INTO `dict7B` VALUES (3765,1,'2000','\n'),(3867,1,'2000','\n'),(3829,1,'2000','\n'),(3726,1,'2000',''),(3803,1,'2000','\n'),(3782,1,'2000','\nĝ'),(3831,1,'2000','\n'),(3748,1,'2000','\n'),(3801,1,'2000','\n'),(3826,1,'2000','\n'),(3788,1,'2000','\n'),(3844,1,'2000','\n'),(3869,1,'2000','î'),(3868,1,'2000','ª'),(3756,1,'2000','\n'),(3815,1,'2000','\n'),(3784,1,'2000','\n'),(3727,1,'2000',''),(3856,1,'tmp','Ţ'),(3840,1,'ways','ˊ'),(3820,1,'2000','\n'),(3814,1,'2000','\n'),(3813,1,'2000','\n'),(3812,1,'2000','\n'),(3811,1,'2000','\n'),(3810,1,'2000','\n'),(3809,1,'2000','\n'),(3808,1,'2000','\n'),(3807,1,'2000','\n'),(3804,1,'2000','\n'),(3805,1,'2000','\n'),(3806,1,'2000','\n'),(3840,1,'2000','\n'),(3839,1,'2000','\n'),(3760,1,'2000','\n'),(3761,1,'2000','\n'),(3762,1,'2000','\n'),(3763,1,'2000','\n'),(3764,1,'2000','\n'),(3819,1,'2000','\n'),(3824,1,'2000','\n'),(3822,1,'abbreviations','³'),(3758,1,'2000','\n'),(3757,1,'2000','\n'),(3724,1,'2000',''),(3773,1,'2000','\n'),(3841,1,'2000','\n'),(3830,1,'2000','\n'),(3832,1,'2000','\n'),(3802,1,'2000','\n'),(3725,1,'2000',''),(3822,1,'2000','\n'),(3836,1,'2000','\n'),(3838,1,'2000','\n'),(3797,1,'2000','\n'),(3790,1,'2000','\n'),(3791,1,'2000','\n'),(3792,1,'2000','\n\Z'),(3793,1,'2000','\n*'),(3794,1,'2000','\n'),(3795,1,'2000','\n'),(3796,1,'2000','\n'),(3789,1,'2000','\n'),(3783,1,'2000','\n'),(3856,1,'2000','\n'),(3855,1,'2000','\n'),(3854,1,'2000','\n'),(3853,1,'2000','\n'),(3852,1,'2000','\n'),(3851,1,'2000','\n'),(3850,1,'2000','\n'),(3849,1,'2000','\n'),(3848,1,'2000','\n'),(3847,1,'2000','\n'),(3865,1,'2000','\n'),(3866,1,'2000','\n'),(3864,1,'2000','\n'),(3863,1,'2000','\n'),(3862,1,'2000','\n'),(3861,1,'2000','\n'),(3755,1,'2000','\n'),(3754,1,'2000','\n'),(3753,1,'2000','\n'),(3752,1,'2000','\n'),(3751,1,'2000','\n'),(3749,1,'2000','\n'),(3750,1,'2000','\n'),(3818,1,'2000','\n'),(3816,1,'2000','\n'),(3817,1,'2000','\n'),(3799,1,'2000','\n'),(3747,1,'2000','\n'),(3825,1,'2000','\n'),(3786,1,'2000','\n'),(3785,1,'2000','\n'),(3860,1,'2000','\n'),(3845,1,'2000','\n'),(3767,1,'2000','\n'),(3768,1,'2000','\n'),(3769,1,'2000','\n'),(3770,1,'2000','\n'),(3771,1,'2000','\n'),(3729,1,'2000',''),(3730,1,'2000',''),(3731,1,'2000',''),(3732,1,'2000',''),(3733,1,'2000',''),(3734,1,'2000','\n'),(3735,1,'2000',''),(3736,1,'2000',''),(3737,1,'2000',''),(3738,1,'2000',''),(3739,1,'2000',''),(3740,1,'2000',''),(3740,1,'indiciated',''),(3741,1,'2000',''),(3742,1,'2000','\n'),(3743,1,'2000',''),(3744,1,'2000',''),(3745,1,'2000',''),(3746,1,'2000','\n'),(3728,1,'2000',''),(3857,1,'2000','\n'),(3827,1,'2000','\n'),(3828,1,'2000','\n'),(3843,1,'2000','\n'),(3842,1,'2000','\n'),(3833,1,'2000','\n'),(3835,1,'2000','\n'),(3722,1,'2000',''),(3827,1,'reading','Ĥ'),(3772,1,'2000','\n'),(3759,1,'2000','\n'),(3837,1,'2000','\n'),(3846,1,'2000','\n'),(3800,1,'2000','\n'),(3766,1,'2000','\nL'),(3723,1,'2000',''),(3870,1,'vice','¬'),(3798,1,'2000','\n'),(3775,1,'2000','\n'),(3776,1,'2000','\n'),(3777,1,'2000','\n'),(3778,1,'2000','\n'),(3779,1,'2000','\n'),(3780,1,'2000','\n'),(3781,1,'2000','\n'),(3774,1,'2000','\n'),(3858,1,'2000','\n'),(3821,1,'2000','\n'),(3787,1,'vice',''),(3787,1,'2000',''),(3859,1,'2000','\n'),(3834,1,'2000','\n'),(3823,1,'2000','\n'),(3871,1,'2000','✓⌧'),(3871,1,'3th','❠'),(3871,1,'abbreviations','㕷'),(3871,1,'adjusted','╦'),(3871,1,'reading','࢛˥✂Ԓᢘ'),(3871,1,'stamp','㝢'),(3871,1,'straight','ኝ㛜'),(3871,1,'strftime','ᄖᲮ\r'),(3871,1,'ways','ݟचෞΪţܒ'),(3873,1,'89','Ģ'),(3874,1,'charts','͆'),(3875,1,'charts','ȓ'),(3876,1,'charts','Ż'),(3879,1,'4342','Ն¥'),(3879,1,'basepath','壾	¤±'),(3879,1,'charts','䏉'),(3879,1,'d2','䵴'),(3879,1,'planning','ۍ'),(3879,1,'reading','意ī'),(3879,1,'stamp','䢠'),(3879,1,'tmp','䦋ᣄ'),(3880,1,'4342','Չ¥'),(3880,1,'charts','ܪͤ\Zᆎᢔő৮¶'),(3880,1,'organizations','ޭ'),(3880,1,'planning','Э゘'),(3881,1,'4342','͙¥'),(3881,1,'adjusted','≍'),(3881,1,'dal','୧!'),(3881,1,'reading','┻'),(3885,1,'tmp','Ȝذ'),(3888,1,'writeable','́'),(3890,1,'planning','ᚌ'),(3890,1,'reading','ඈ'),(3890,1,'writeable','ැ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict7B` ENABLE KEYS */;

--
-- Table structure for table `dict7C`
--

DROP TABLE IF EXISTS `dict7C`;
CREATE TABLE `dict7C` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict7C`
--


/*!40000 ALTER TABLE `dict7C` DISABLE KEYS */;
LOCK TABLES `dict7C` WRITE;
INSERT INTO `dict7C` VALUES (3881,1,'views','ڙĜ⏐\r˗'),(3881,1,'receives','ḥ'),(3881,1,'gnu','-,'),(3721,1,'gnu','Ţ'),(3811,1,'objectclass','f'),(3827,1,'vhost','='),(3863,1,'gnu',','),(3869,1,'gnu','1'),(3871,1,'230','东'),(3871,1,'electricity','㷧ਛ'),(3871,1,'fddi','Ɲ'),(3871,1,'insufficient','ᣠ₥'),(3871,1,'interface1','ᷓ'),(3871,1,'interpret','Ⲗ᧳'),(3871,1,'receives','䷈'),(3871,1,'viewing','㫕'),(3871,1,'z0','ߋ'),(3872,1,'gnu','	'),(3872,1,'insufficient','˴'),(3872,1,'stricttap','Ԣ'),(3877,1,'gnu','+,'),(3878,1,'gnu','$,'),(3879,1,'eventhandler','䏤ѩ('),(3879,1,'gnu','-,'),(3879,1,'interpret','䔷'),(3879,1,'viewing','ᣞᥬ໇'),(3879,1,'views','୰ù'),(3880,1,'conveniently','ܔᆙ'),(3880,1,'detection','ⶽѕ'),(3880,1,'gnu','-,'),(3880,1,'viewing','öNᔽǴ৷[ĸ༷êQɁą࿷'),(3880,1,'views','Ή௺ZعĘ¤#\'ġķఙ¶౹SÎ0·7༈'),(3884,1,'detection','԰\r\n/ভ\r\n/'),(3885,1,'detection','̗\r\r1'),(3887,1,'detection','Օ\r\r1'),(3887,1,'viewing','గ'),(3890,1,'detection','ᖂ7		'),(3890,1,'viewing','Ќ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict7C` ENABLE KEYS */;

--
-- Table structure for table `dict7D`
--

DROP TABLE IF EXISTS `dict7D`;
CREATE TABLE `dict7D` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict7D`
--


/*!40000 ALTER TABLE `dict7D` DISABLE KEYS */;
LOCK TABLES `dict7D` WRITE;
INSERT INTO `dict7D` VALUES (3721,1,'links','\rɵ'),(3871,1,'vertical','༸ʒǈߢƬᴙà»'),(3870,1,'links','B½'),(3799,1,'variable','5BÜ'),(3834,1,'triggers',''),(3837,1,'transaction','ā'),(3840,1,'recommend','Ɇ'),(3757,1,'50000','ſ'),(3720,1,'links','\rƖ'),(3766,1,'variable','5#o'),(3762,1,'triggers',''),(3764,1,'50000','2'),(3798,1,'variable','g'),(3782,1,'variable',',S'),(3780,1,'variable','0#'),(3778,1,'variable','¹'),(3772,1,'maxprc',''),(3879,1,'links','Џ௯ශ╤ऽ஄÷ğ£'),(3871,1,'variable','ᆳժ̜ኣ$S஻Њם'),(3871,1,'sin','኉ը'),(3871,1,'devices','~ㅅ೩'),(3871,1,'links','傸'),(3871,1,'recommend','䜖'),(3744,1,'bounds','ȓ'),(3879,1,'5760','䙲ͷ'),(3878,1,'devices','Ç'),(3877,1,'links','̸'),(3871,1,'wan','ዮ\r'),(3872,1,'devices','r'),(3872,1,'links','X౶'),(3872,1,'variable','۱'),(3727,1,'variable',':¢'),(3722,1,'wan','z'),(3721,1,'uncomfortable','Ž'),(3879,1,'devices','͖ᬢϟ Ѭබ¡'),(3879,1,'analyzes','叼'),(3868,1,'links',''),(3864,1,'triggers','k'),(3846,1,'devices','ė'),(3869,1,'links','\r÷'),(3879,1,'variable','᧿ûⶄ'),(3880,1,'devices','ˀӜ	ਤၯ'),(3880,1,'links','๙ઈ಺Õs἖!<'),(3880,1,'transaction','ॖᑪ'),(3881,1,'devices','׾đ୕'),(3881,1,'links','⯑'),(3881,1,'transaction','Ⅶ'),(3881,1,'variable','ப'),(3884,1,'devices','ࢱ'),(3884,1,'variable','ጙ!'),(3885,1,'devices','Ӄ'),(3886,1,'recommend','Ӯ'),(3887,1,'devices','܄'),(3887,1,'variable','౪J)/0¡¢'),(3888,1,'links','Ǵ'),(3890,1,'frequency','኶'),(3890,1,'recommend','षؒȵ'),(3890,1,'variable','Ή^ĤNTūעı΢ŷÿ'),(3772,6,'maxprc','1');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict7D` ENABLE KEYS */;

--
-- Table structure for table `dict7E`
--

DROP TABLE IF EXISTS `dict7E`;
CREATE TABLE `dict7E` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict7E`
--


/*!40000 ALTER TABLE `dict7E` DISABLE KEYS */;
LOCK TABLES `dict7E` WRITE;
INSERT INTO `dict7E` VALUES (3880,1,'alternatively','ඕ'),(3830,1,'arguments','Y'),(3874,1,'arguments','wƚĩ'),(3873,1,'defined','Ȩy'),(3887,1,'defined','٣'),(3888,1,'currently','ϲ'),(3884,1,'required','ĸ\Z.2OőS/ݫ12OőS/˔	žĞ	('),(3879,1,'defined','ܫĨĥÌ-C[༛ࠓ}˥೎Ʊe֔gĆˊ)Ǘ̓ģĕ¬ÛŲɚàNÌf଻pߋ'),(3871,1,'required','ေᆱɪɫʣÓ'),(3823,1,'required','e7'),(3822,1,'required',''),(3735,1,'required',':'),(3734,1,'arguments',''),(3825,1,'required','Ħ'),(3823,1,'svr2','KU'),(3722,1,'defined','ķ'),(3721,1,'defined','ã'),(3889,1,'defined','ͩ'),(3889,1,'24x7','ǒ˿'),(3845,1,'required','='),(3830,1,'required','X'),(3832,1,'required','9'),(3881,1,'operator','ȸ᎖ąϪ\n '),(3873,1,'arguments','ĳ'),(3856,1,'arguments','Ĩ'),(3871,1,'levels','㉷'),(3871,1,'guarantees','Ռ'),(3871,1,'defined','كˁ{͑VO\Zѳܢƪҗg҂࢏'),(3871,1,'currently','ቕȢ\Zᇱ'),(3871,1,'arguments','ٽᗔᇼ0g'),(3871,1,'alternatively','⏿'),(3871,1,'2weeks','⹇'),(3868,1,'required','}'),(3863,1,'required','u'),(3890,1,'edits','Þ'),(3890,1,'defined','ȁқ/Ǉўইĩ'),(3889,1,'required','°^<'),(3872,1,'defined','Ӵ>ŗl$ԡ'),(3880,1,'currently','ߋ଒᫜Ыᅪś'),(3879,1,'sharing','㒯'),(3822,1,'forming','Ü'),(3822,1,'currently','<	{'),(3872,1,'required','ɂղƕIĤ'),(3872,1,'modem','¥њÈ'),(3890,1,'required','ůֹฬ'),(3885,1,'defined','ý̨Ѳ'),(3884,1,'arguments','ܐ঑ '),(3879,1,'arguments','䱬%ǈƢ'),(3878,1,'required','Ķ'),(3872,1,'currently','Ɗ'),(3872,1,'24x7','ऋ'),(3878,1,'defined','ࣉ_'),(3874,1,'defined','ɵ'),(3820,1,'required','/'),(3725,1,'currently',''),(3827,1,'required','ǁ'),(3879,1,'required','ៅȣ¦վ޿&!γ#_ıâǝZ¹ňǼ٣tţL̊Cᖤ>j'),(3879,1,'currently','᧋ⴒ'),(3859,1,'currently',''),(3727,1,'upsd','Ē'),(3871,1,'operator','᝝L\n\nv'),(3879,1,'edits','᫘'),(3879,1,'guarantees','䯀'),(3879,1,'operator','и˴̜Ȋ	wٸ䏘DÂ¥z		T7-Į'),(3843,1,'required',','),(3841,1,'arguments','1'),(3871,1,'multiply','ఔ⤑ãԎ,'),(3880,1,'defined','ଲźʅʛ&IឞmȎğŮ̡3ܯ'),(3888,1,'defined','ĢC	'),(3888,1,'levels','Ī'),(3888,1,'arguments','׍'),(3883,1,'required','Ã'),(3881,1,'required','ࣱᡤăęŐࠎċĎ֖н0'),(3884,1,'defined','˺Ѓ΁ʇ'),(3871,1,'stacked','ᯬ'),(3871,1,'slot','ϸ'),(3876,1,'arguments','SÙ'),(3875,1,'arguments','ŉ=Ĕ'),(3841,1,'24x7','l'),(3840,1,'arguments','ʥ'),(3832,1,'arguments','>'),(3887,1,'required','Ā!¸̚ȨĢL:ĩ#ç¸Ѹ'),(3881,1,'defined','๮@?A>@EBCWDP@AAI7765\Z6.!r*J̈ʓ¼a+Ȏʃ&0\rAŧ࿍ԓ'),(3881,1,'arguments','ᴁ'),(3881,1,'currently','೎cܰƊ*ឿ'),(3880,1,'stacked','㨖VJPǍZ\\LJBigƷ@@A'),(3880,1,'required','΢ࣅ޺ႋᷡ͢'),(3880,1,'operator','\r̀̇җ¨Ě\ZߊД⮡Ć	'),(3880,1,'levels','ᡖ฻Ļ'),(3886,1,'defined','Ҹ'),(3886,1,'required','Ø!QMIĎ'),(3886,1,'currently','Ο'),(3886,1,'arguments','Ф[$'),(3886,1,'alternatively','ԓ'),(3885,1,'required','ŁĥɖĢL:Ş'),(3820,1,'overhead','R'),(3819,1,'required','.'),(3816,1,'splits','\Z'),(3819,1,'overhead','Q'),(3811,1,'required','z'),(3736,1,'levels',''),(3745,1,'required',''),(3747,1,'smartctl','-'),(3751,1,'levels','Ƌ'),(3752,1,'24x7','û'),(3754,1,'arguments','ĕųu'),(3757,1,'arguments','Z\Zþ'),(3757,1,'levels','¦'),(3757,1,'required','\'2'),(3758,1,'arguments','1'),(3758,1,'required','0'),(3759,1,'arguments','>'),(3759,1,'required','='),(3765,1,'py','n'),(3776,1,'defined','\\'),(3776,1,'ora','_'),(3778,1,'ora','dc'),(3779,1,'ora',''),(3780,1,'currently','Ĭ'),(3783,1,'arguments',']	'),(3789,1,'arguments','~'),(3791,1,'levels',''),(3791,1,'required','\"'),(3792,1,'uunet','Y'),(3793,1,'arguments',''),(3793,1,'required',''),(3803,1,'arguments',''),(3803,1,'required',''),(3805,1,'required','i	%'),(3808,1,'required','@'),(3891,1,'defined','Ϻ'),(3891,1,'required','Á8CCĸCCK'),(3892,1,'arguments','ƅ'),(3892,1,'defined','£Ù'),(3880,2,'operator',''),(3779,6,'ora','>'),(3880,6,'operator','䡃'),(3880,7,'operator','䡂');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict7E` ENABLE KEYS */;

--
-- Table structure for table `dict7F`
--

DROP TABLE IF EXISTS `dict7F`;
CREATE TABLE `dict7F` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict7F`
--


/*!40000 ALTER TABLE `dict7F` DISABLE KEYS */;
LOCK TABLES `dict7F` WRITE;
INSERT INTO `dict7F` VALUES (3880,1,'max','ዊࡈ'),(3880,1,'benefit','ቓ'),(3880,1,'associated','၇൲ௐͳA,ěmI&ȁᖖ'),(3822,1,'max','S'),(3759,1,'associated','ï'),(3730,1,'max','?'),(3744,1,'max','Ǆ,'),(3757,1,'max','ĭ'),(3880,1,'remove','⳨	бѩ'),(3879,1,'symbol','ᥔ'),(3881,1,'concerned','⡛'),(3881,1,'associated','ᐉĮ?ᅍ'),(3881,1,'admin','℞'),(3881,1,'activates','⽄'),(3820,1,'max','ð'),(3820,1,'admin','â'),(3819,1,'max','þ'),(3815,1,'ircd','7'),(3813,1,'max',')'),(3762,1,'trip','*'),(3879,1,'admin','᭳'),(3879,1,'remove','০ȍ!ӮࡄჴƜͽ৾!)ŇűӾ᫟['),(3879,1,'mnogosearch','ᑁ&䉝'),(3834,1,'trip',''),(3827,1,'max','fÏ'),(3726,1,'remove','b'),(3879,1,'relation','姕'),(3759,1,'max',',Ot'),(3878,1,'associated','Ɩ'),(3877,1,'remove','Ǥþ'),(3872,1,'admin','ౙ'),(3871,1,'val2','ẏS'),(3871,1,'technical','ち'),(3871,1,'rpn','ľใ݊\rVcቄÈၕ'),(3871,1,'remove','⟗'),(3840,1,'confuses','Ɓ'),(3864,1,'trip','e'),(3870,1,'versa','­'),(3871,1,'12357','㖱ȰC<ǹึ'),(3871,1,'compact','T'),(3871,1,'max','ۭůn̓əΖ׉ҋΰ!½È׷¬\r᧕'),(3879,1,'benefit','≙'),(3879,1,'associated','ܥŏɛ᠁ÄȞ̩ƗƍײǋĬʻГժƽᎼƩ'),(3766,1,'cr','µ\n'),(3776,1,'max','ï'),(3780,1,'max','Ą'),(3787,1,'versa',''),(3798,1,'max','e.'),(3799,1,'max','3;'),(3802,1,'max','\''),(3881,1,'getdeviceparents','ጁ'),(3881,1,'relation','⹁'),(3881,1,'remove','㆒'),(3882,1,'associated','ƀ'),(3882,1,'remove','1hy3?'),(3883,1,'remove',''),(3884,1,'associated','¤ࣩঘ'),(3884,1,'depended','ᆘj,q'),(3884,1,'max','ȶiGज़iG'),(3884,1,'remove','ބƀઝ2'),(3885,1,'max','Ӝ'),(3885,1,'remove','ࢰOY'),(3886,1,'associated','Π'),(3886,1,'remove','iƞóÀ'),(3887,1,'associated','౻s[Ɵ'),(3887,1,'boutell','ඊ'),(3887,1,'depended','ၹ!g'),(3887,1,'max','ܝ'),(3887,1,'remove','ӞӸĻ²ѿ'),(3888,1,'remove','ç¨υÄ'),(3889,1,'associated','ϵ'),(3889,1,'remove','Ͻ'),(3889,1,'technical','̘'),(3890,1,'admin','᰽%'),(3890,1,'max','ሁŚQ'),(3890,1,'spread','མʬßR'),(3891,1,'remove','©̇Æ'),(3815,6,'ircd','v');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict7F` ENABLE KEYS */;

--
-- Table structure for table `dict80`
--

DROP TABLE IF EXISTS `dict80`;
CREATE TABLE `dict80` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict80`
--


/*!40000 ALTER TABLE `dict80` DISABLE KEYS */;
LOCK TABLES `dict80` WRITE;
INSERT INTO `dict80` VALUES (3754,1,'treat','Ș'),(3879,1,'rrds','䓲Ж'),(3879,1,'sleep','把'),(3879,1,'subcategory','娈'),(3871,1,'elements','ุʒYҁ̬'),(3871,1,'disambiguate','☪'),(3879,1,'elements','̊'),(3879,1,'edirectory','ाዉ'),(3879,1,'better','Ί䣏'),(3871,1,'rrds','س঄ᆧࢦנŘ'),(3871,1,'writes','㣨'),(3750,1,'param1',','),(3744,1,'authpassword','»'),(3740,1,'authpassword','/:'),(3720,1,'better',''),(3871,1,'counter','߼	\nF¸̅ǻ☝$+*2cĵCھ!ӌ҉Ì\"JªW		\r	 m*5ƞB5]'),(3871,1,'op','⡈߽⁾'),(3871,1,'better','㪱Ģ૚Ł'),(3851,1,'sar',''),(3855,1,'envmon',''),(3869,1,'perls','U'),(3799,1,'correspond','ô'),(3782,1,'counter','ķ'),(3871,1,'readability','㚽ƿ'),(3751,1,'algorythm','+'),(3751,1,'rrds','!'),(3879,1,'writes','⁗'),(3880,1,'better','˴иジ'),(3880,1,'elements','ĎŘఃޥtȝŖ੷ጩĜ&'),(3880,1,'treat','⣋өƓ'),(3881,1,'better','௟ၝćھ'),(3881,1,'counter','ԦᝨÇ'),(3881,1,'elements','ᗅĉᓽ'),(3881,1,'op','ᩭ\rµ('),(3887,1,'correspond','ɥ஑'),(3890,1,'sleep','ਸ਼ܯ	');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict80` ENABLE KEYS */;

--
-- Table structure for table `dict81`
--

DROP TABLE IF EXISTS `dict81`;
CREATE TABLE `dict81` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict81`
--


/*!40000 ALTER TABLE `dict81` DISABLE KEYS */;
LOCK TABLES `dict81` WRITE;
INSERT INTO `dict81` VALUES (3876,1,'ping','^\"§G'),(3877,1,'host','ś	ɒøɌ'),(3871,1,'directly','ɦᙔෆ⅛'),(3871,1,'7h','⓾'),(3871,1,'derive','ࡅ(\Zΰǻ㨦C3ÉǾ'),(3866,1,'team',''),(3766,1,'team',''),(3863,1,'exclude','Ó'),(3862,1,'team',''),(3782,1,'host','*'),(3781,1,'team',''),(3813,1,'team',''),(3803,1,'team',''),(3853,1,'team',''),(3762,1,'ping','.U'),(3762,1,'host',')]'),(3862,1,'host','\Zb'),(3826,1,'team',''),(3825,1,'team',''),(3873,1,'managed','C'),(3794,1,'host',''),(3794,1,'team',''),(3795,1,'host',' '),(3795,1,'team',''),(3796,1,'host',''),(3796,1,'team',''),(3797,1,'team',''),(3798,1,'team',''),(3799,1,'team',''),(3800,1,'team',''),(3801,1,'team',''),(3802,1,'team',''),(3761,1,'team',''),(3822,1,'ping',''),(3766,1,'host','1\Z'),(3765,1,'team',''),(3847,1,'team',''),(3846,1,'team',''),(3810,1,'team',''),(3809,1,'team',''),(3834,1,'host','!'),(3834,1,'ping','<'),(3870,1,'language','\Z'),(3868,1,'online','c'),(3868,1,'team','¯'),(3869,1,'language',''),(3867,1,'team',''),(3866,1,'host',' 6'),(3756,1,'team',''),(3757,1,'team',''),(3758,1,'team',''),(3759,1,'exclude','Ý'),(3759,1,'team',''),(3760,1,'host','*'),(3760,1,'team',''),(3761,1,'host',';'),(3722,1,'team','\r'),(3723,1,'host','\"'),(3723,1,'team','\r'),(3724,1,'online','_'),(3775,1,'team',''),(3774,1,'team',''),(3773,1,'team',''),(3770,1,'online',''),(3770,1,'team',''),(3771,1,'team',''),(3772,1,'team',''),(3769,1,'team',''),(3768,1,'team',''),(3767,1,'team',''),(3879,1,'81','䵦'),(3815,1,'team',''),(3816,1,'team',''),(3817,1,'team',''),(3818,1,'host',';'),(3818,1,'team',''),(3819,1,'exclude','x'),(3819,1,'host','#'),(3819,1,'team',''),(3820,1,'host','$'),(3820,1,'team',''),(3821,1,'team',''),(3822,1,'host',')R'),(3784,1,'team',''),(3784,1,'host',';'),(3787,1,'negates',''),(3865,1,'host','('),(3878,1,'warranty','@'),(3878,1,'ping','μ'),(3842,1,'host',','),(3842,1,'team',''),(3837,1,'host','#h'),(3836,1,'team',''),(3843,1,'team',''),(3765,1,'host','&'),(3764,1,'team',''),(3764,1,'host','\''),(3833,1,'host',';'),(3877,1,'warranty','G'),(3863,1,'upgrades','Ę'),(3835,1,'team',''),(3814,1,'host',';'),(3855,1,'team',''),(3811,1,'host',' \''),(3791,1,'host','B8'),(3791,1,'team',''),(3792,1,'host','*'),(3792,1,'team',''),(3793,1,'host','%'),(3793,1,'team',''),(3875,1,'host','Ĥƌ'),(3871,1,'host','⬋ᗅI'),(3840,1,'team',''),(3823,1,'team',''),(3879,1,'directly','࠿Ǉ˧⹶?`ó'),(3827,1,'team',''),(3827,1,'host','nū\r'),(3872,1,'host','ٵB;\ri%+êG!Q\Zǜ'),(3783,1,'host','!\nS'),(3782,1,'team',''),(3871,1,'plotted','ἇ0ᆂᩌ'),(3763,1,'team',''),(3878,1,'host','ࠜ'),(3879,1,'exclude','ṩႝ'),(3848,1,'team',''),(3853,1,'host','&'),(3864,1,'host','?'),(3856,1,'team',''),(3860,1,'team',''),(3720,1,'core','$'),(3849,1,'team',''),(3785,1,'team',''),(3785,1,'host',';'),(3779,1,'host','%'),(3863,1,'team',''),(3861,1,'team',''),(3861,1,'host','&'),(3845,1,'team',''),(3844,1,'team',''),(3870,1,'online','A'),(3823,1,'host',''),(3822,1,'team',''),(3783,1,'team',''),(3721,1,'language','ÕƠ'),(3720,1,'slider','Ŝ'),(3720,1,'directly','v'),(3879,1,'host','ᇽüa5kKࠤчd$\n\Z\"\nGc!\Z?,]Ae\r\r	¯/	\r	&\"\r\"U<(F;)E5(F\n\r\r%41)͎ɠl	 \n\n \n	 \n\r	\n*	.I1M`			(\n/\n\r\n&\n\n,É\n&źĊ\r6żǫ\nò\ZƬÛe¤½*Oĕ\n-\'$\r3\Z8=		\n6rl((Ǆ&ȗ\rમ£`;llí		Σ\n'),(3879,1,'gd2','㣓'),(3815,1,'host','\"\n*'),(3814,1,'team',''),(3787,1,'ping',''),(3787,1,'team','\r'),(3788,1,'host','DO'),(3788,1,'team',''),(3789,1,'team',''),(3790,1,'host','9	'),(3790,1,'team',''),(3804,1,'team',''),(3804,1,'host','14'),(3865,1,'team',''),(3857,1,'team',''),(3857,1,'host','%\n'),(3787,1,'host',''),(3874,1,'host','ϒ'),(3840,1,'host','#\"Ƣ'),(3839,1,'team',''),(3838,1,'team',''),(3839,1,'randomly',' '),(3812,1,'team',''),(3811,1,'team',''),(3871,1,'language','ɜད'),(3876,1,'host','Ŋ'),(3859,1,'team',''),(3858,1,'team',''),(3846,1,'exclude','á'),(3870,1,'team',']'),(3848,1,'host','%'),(3813,1,'host',''),(3879,1,'core','ᆟૂ'),(3864,1,'team',''),(3864,1,'ping','B'),(3879,1,'fields','⤑z\'ഭᒵʎ0ƴ'),(3777,1,'team',''),(3776,1,'team',''),(3833,1,'team',''),(3856,1,'host',' \r\ZS%R'),(3808,1,'team',''),(3807,1,'team',''),(3806,1,'team',''),(3805,1,'team',''),(3841,1,'team',''),(3841,1,'host','c'),(3763,1,'host','!'),(3762,1,'team',''),(3871,1,'kilo','ጥ6⌌'),(3852,1,'team',''),(3851,1,'team',''),(3850,1,'team',''),(3824,1,'team',''),(3862,1,'directly','>'),(3837,1,'team',''),(3837,1,'ping','Å5'),(3832,1,'team',''),(3832,1,'host','%'),(3831,1,'team',''),(3831,1,'ping','.E\n'),(3831,1,'host',''),(3830,1,'team',''),(3828,1,'host',':'),(3828,1,'team',''),(3829,1,'host','X'),(3829,1,'team',''),(3830,1,'host','6'),(3779,1,'team',''),(3780,1,'host',',\Z'),(3780,1,'team',''),(3781,1,'host',''),(3834,1,'team',''),(3844,1,'host','\''),(3778,1,'team',''),(3786,1,'team',''),(3862,1,'ping',''),(3854,1,'host','<'),(3854,1,'team',''),(3756,1,'retries','6C'),(3724,1,'team','\r'),(3725,1,'team','\r'),(3726,1,'team','\r'),(3727,1,'host',' XK*'),(3727,1,'online','±'),(3727,1,'team','\r'),(3728,1,'host','\Z;'),(3728,1,'team','\r'),(3729,1,'host','<`'),(3729,1,'team','\r'),(3730,1,'host','7'),(3730,1,'team','\r'),(3731,1,'host',' '),(3731,1,'team','\r'),(3732,1,'host','\Z<'),(3732,1,'team','\r'),(3733,1,'host',''),(3733,1,'team','\r'),(3734,1,'team',''),(3735,1,'team','\r'),(3736,1,'team','\r'),(3737,1,'host','\Z;'),(3737,1,'team','\r'),(3738,1,'host','+'),(3738,1,'team','\r'),(3739,1,'host','\Z;'),(3739,1,'team','\r'),(3740,1,'host','	t·'),(3740,1,'team','\r'),(3741,1,'host','\"'),(3741,1,'team','\r'),(3742,1,'host','+'),(3742,1,'team',''),(3743,1,'host',''),(3743,1,'team','\r'),(3744,1,'host',''),(3744,1,'retries',']'),(3744,1,'team','\r'),(3745,1,'host','&¦'),(3745,1,'team','\r'),(3746,1,'team',''),(3747,1,'team',''),(3748,1,'host',';'),(3748,1,'team',''),(3749,1,'team',''),(3750,1,'team',''),(3751,1,'team',''),(3752,1,'ping','Ô\n	'),(3752,1,'team',''),(3753,1,'host','\"	'),(3753,1,'team',''),(3754,1,'host','VÆ:#t3&E+#'),(3754,1,'team',''),(3755,1,'host','Z'),(3755,1,'team',''),(3756,1,'host','&\"'),(3879,1,'managed','࡞Î㈍0?ᾈ'),(3879,1,'ping','ㅺʦᓨ'),(3879,1,'team','͎'),(3879,1,'warranty','I'),(3880,1,'core','ਵ'),(3880,1,'delegate','ഩ'),(3880,1,'directly','ଦ-㮅'),(3880,1,'fields','⣱өƓ'),(3880,1,'host','úؓϧφFĪ\r0	!<(+F\n1>&às\r5Þ\rKĢK\r~)UC3\r\r	!\r)\Z\r\'#6Ƅ	&H%@\r#@(L	\n#!,U)P+B *)\r\n	G\n\Z	.@	d5¦\n\Z \Z\r\n%\n\r\r	\r	1\n\'\n\r		\r\r\rI)	\n@\n˴\"&.\'\n\n\"Ɯ\Z`\"#7Ħ/Ă\n\r@()	2\n\n%g(+@+\nƳ	b\nm\'+b\Z\Z	Q<'),(3880,1,'language','ហ'),(3880,1,'managed','ಹ¬Ɓ⨼!˚'),(3880,1,'online','॥౞'),(3880,1,'ping','ၧ㒛'),(3880,1,'retries','ኸ'),(3880,1,'team','ʸ'),(3880,1,'upgrades','۴'),(3880,1,'warranty','I'),(3881,1,'core','◀'),(3881,1,'directly','⤃ඖ'),(3881,1,'fields','ԫᝨ¤ʸÃz&ཎ'),(3881,1,'host','֧\nļ\n×v\r\r\n\n\'(ݹƳ\r\r\rQ\r	\r		_.(żŻ\n,\nҿǪžL&±j'),(3881,1,'language','ổӘϻ'),(3881,1,'managed','ݚ᛺أ'),(3881,1,'statetype','ࡣ'),(3881,1,'warranty','I'),(3882,1,'host','5t:C'),(3883,1,'exclude','ı'),(3884,1,'host','Ōτų·T	\n\nO>!F ҕƿșW)\rj'),(3885,1,'host','-}	\n\rD		c:-19\nd\r\n\n\".6p%3?/ŕ$\r\r '),(3885,1,'managed','ࢴB'),(3885,1,'ping','ҋ'),(3886,1,'directly','Ԗ'),(3886,1,'host','	\n\"\r	&\"	\r\n	!8\r'),(3886,1,'managed','Ȭ$0L'),(3887,1,'directly','ཙ_'),(3887,1,'gd2','൤'),(3887,1,'host','#\n	\n	\"\r	&\"j4\"	!/>!	:8:-19\nP\r\n\n\".6p%2?			f\r\Z<\n02s\r3f<*\n\n\n\n\n	\r\r9'),(3887,1,'managed','Ƿ8ഽ'),(3887,1,'ping','ی'),(3888,1,'downloadable','ˠ'),(3888,1,'host','5&´)1ǁ!(\"\\\Zß=\r%9¡h'),(3889,1,'host','!\n\nBMdS\r4	\r0	#@\n\nF'),(3890,1,'directly','ŕ'),(3890,1,'gd2','ٸ'),(3890,1,'host','Í\nɅƟN5ǔ\"àÒÅśpRƼ:>Ŋ\n=$\n!(ůC\n!X1hB \r<uz)K)4PKZ\n\\I#ĕ\r\n?,ǫk'),(3890,1,'online','ȍ'),(3890,1,'ping','܃\r'),(3890,1,'retries','࿢\n\n'),(3891,1,'host','ƃ\ZƳ¸^e\Z'),(3892,1,'host','ċ%S'),(3876,2,'ping',''),(3762,6,'ping','é'),(3770,6,'online','1'),(3830,6,'host',''),(3876,6,'ping','Ƶ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict81` ENABLE KEYS */;

--
-- Table structure for table `dict82`
--

DROP TABLE IF EXISTS `dict82`;
CREATE TABLE `dict82` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict82`
--


/*!40000 ALTER TABLE `dict82` DISABLE KEYS */;
LOCK TABLES `dict82` WRITE;
INSERT INTO `dict82` VALUES (3880,1,'delivery','ྣ'),(3877,1,'correct','݅'),(3873,1,'thresholds','Ĭ'),(3872,1,'pagejoe','஑'),(3871,1,'exact','਴ܱ⣮ᐏ'),(3871,1,'expand','Yᓰ'),(3871,1,'mode','ċᐵ'),(3871,1,'positioned','ᆏ'),(3871,1,'side','ᎉঝ'),(3872,1,'gz','ıp$'),(3872,1,'killproc','ͧ\r},'),(3872,1,'mode','Ԏ'),(3880,1,'side','෫[ᅹ͐⎧'),(3880,1,'releases','ᖗ'),(3880,1,'flap','ⶼѕ'),(3881,1,'propertynames','Ჱ'),(3881,1,'monitorservername','ࢧᦿŶ'),(3881,1,'delivery','❩'),(3881,1,'correct','⅘'),(3880,1,'webservice','۩'),(3744,1,'exact','ĭ'),(3727,1,'thresholds','í'),(3725,1,'thresholds',')'),(3720,1,'mode','ĸ'),(3720,1,'dom','Z'),(3880,1,'thresholds','ᅄج'),(3880,1,'exact','㠾'),(3880,1,'expand','ᤥ஠'),(3879,1,'mode','खᎡ㠌{'),(3879,1,'expand','ⱂóĥËૻ'),(3879,1,'exact','䒽'),(3877,1,'side','ք'),(3877,1,'mode','Ù'),(3879,1,'side','ࢴČ'),(3879,1,'pagejoe','⿫'),(3871,1,'escaping','᱒'),(3871,1,'decrease','䢬ûřÏ'),(3871,1,'correct','㞳œƛ\n7ཐɴƓ'),(3870,1,'dom','©'),(3868,1,'releases','Î'),(3856,1,'mode','èB'),(3846,1,'thresholds','·'),(3884,1,'expand','ޗ\n\n'),(3881,1,'side','⨼='),(3879,1,'correct','⫧᷷;\"'),(3880,1,'correct','ఐb'),(3744,1,'side','ñ'),(3747,1,'mode','(	'),(3757,1,'exact','Ģ'),(3757,1,'thresholds','y'),(3765,1,'sda1','²'),(3780,1,'thresholds','ƒ'),(3782,1,'ready2run','ƅ'),(3782,1,'thresholds','¶8'),(3798,1,'thresholds','C'),(3802,1,'ascend','&'),(3822,1,'decrease','Ù'),(3824,1,'precedence','Ú'),(3825,1,'precedence','û'),(3837,1,'emails','Ô\r'),(3837,1,'maxmsg','å'),(3844,1,'thresholds','¦'),(3846,1,'exact','Ą'),(3884,1,'flap','ԯ\r\nক\r\n'),(3884,1,'sda1','Łྕ'),(3885,1,'flap','̖\r\r\Z\Z'),(3886,1,'expand','IŨMJ¥'),(3887,1,'expand','ҏӳŘΔĀ'),(3887,1,'flap','Ք\r\r\Z\Z'),(3887,1,'side','ʘ଍'),(3889,1,'expand','cȧ»'),(3890,1,'expand','ߗ'),(3890,1,'flap','ᖁ7		'),(3890,1,'mode','џטϷ଼'),(3890,1,'side','഑'),(3890,1,'thresholds','ᗓ'),(3891,1,'expand','Û½'),(3891,1,'pagejoe','ĩ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict82` ENABLE KEYS */;

--
-- Table structure for table `dict83`
--

DROP TABLE IF EXISTS `dict83`;
CREATE TABLE `dict83` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict83`
--


/*!40000 ALTER TABLE `dict83` DISABLE KEYS */;
LOCK TABLES `dict83` WRITE;
INSERT INTO `dict83` VALUES (3879,1,'e4','䶒'),(3880,1,'remains','ᅪǣ'),(3880,1,'isp','ਈ'),(3880,1,'examples','ৎ'),(3880,1,'catalog','ᗧ'),(3823,1,'pub','g'),(3799,1,'examples','¡'),(3782,1,'examples','ū'),(3757,1,'based','É'),(3720,1,'automatic','ı'),(3734,1,'examples','#'),(3742,1,'based','^'),(3751,1,'based','\Z'),(3752,1,'examples','Ñ'),(3880,1,'updating','䔞'),(3878,1,'based','×'),(3878,1,'48','࡝'),(3879,1,'based','̟Ȁrಭಲ÷૎ࢋר፡'),(3878,1,'updating','ࡔ'),(3879,1,'examples','ᓼ'),(3879,1,'isp','ᔴ'),(3871,1,'48','☓'),(3824,1,'pub','¡\n'),(3863,1,'based','\"'),(3871,1,'19970703','❚'),(3871,1,'999999','䰀'),(3871,1,'automatic','ᘣᒁ'),(3871,1,'based','ǐњ࣑ǧĚj౽૪ʢ'),(3871,1,'examples','⛣ৄÝź2āːוً̼:#դʣ'),(3871,1,'remains','ຓ'),(3871,1,'updating','෱'),(3872,1,'based','ే'),(3877,1,'pub','֒ĩ'),(3877,1,'freeware','̕'),(3873,1,'pub','н%'),(3873,1,'based','9'),(3872,1,'remains','˂'),(3880,1,'based','ʉʙrˡ#AƓ˃ǫŽǳťN͘᱆ɻĶő˻'),(3881,1,'based','̲rʅńWཧ¨෽Ljih7Ú(ળ®»'),(3881,1,'collagefilter','ఠ഼\'*DO.=$'),(3881,1,'eventid','ᤩ'),(3881,1,'hierarchy','ⴻĎ5'),(3881,1,'ishostisflapping','ࡗ'),(3884,1,'based','ݧɀ'),(3885,1,'remains','ɩݢ'),(3887,1,'based','ˠ஑'),(3888,1,'automatic','҉'),(3888,1,'based','Ћ'),(3890,1,'automatic','ኋ'),(3890,1,'based','༂'),(3890,1,'updating','᷋'),(3824,6,'pub','ţ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict83` ENABLE KEYS */;

--
-- Table structure for table `dict84`
--

DROP TABLE IF EXISTS `dict84`;
CREATE TABLE `dict84` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict84`
--


/*!40000 ALTER TABLE `dict84` DISABLE KEYS */;
LOCK TABLES `dict84` WRITE;
INSERT INTO `dict84` VALUES (3884,1,'60','ʱ\\Ĩࢇ\\Ĩ'),(3890,1,'removing','́'),(3890,1,'60','ൖڦ	'),(3880,1,'reloading','ោ'),(3880,1,'belong','Ⴛʾ'),(3875,1,'companion','Ž'),(3880,1,'storm','ᄵ'),(3871,1,'2005',''),(3882,1,'removing','Ĵ'),(3881,1,'servlet','ߓ'),(3781,1,'60','M'),(3782,1,'1248','J'),(3782,1,'60','µ'),(3822,1,'60',''),(3865,1,'60','O+'),(3871,1,'18446744073709550816','䴤'),(3721,1,'licensing','Ɨ'),(3879,1,'3com','䵯'),(3720,1,'treev3','Ū'),(3871,1,'removing','⠙'),(3887,1,'printers','܁'),(3887,1,'coords','ɏ૭¢'),(3888,1,'removing','Ʋ\n'),(3889,1,'60','Ŧ'),(3881,1,'collagemonitorserverquery','࣐Ä'),(3885,1,'removing','ࢹP['),(3885,1,'printers','Ӏ'),(3881,1,'removing','ഷ≗'),(3879,1,'1248','㋋'),(3876,1,'companion','ß'),(3742,1,'printers','V'),(3751,1,'60','ɕ/'),(3884,1,'removing','ऌ'),(3885,1,'60','ր©'),(3881,1,'getseverity','ཟ'),(3887,1,'60','߁©'),(3879,1,'2005','搜'),(3874,1,'60','­\" '),(3874,1,'companion','ƭ'),(3754,1,'criticals','§	$'),(3879,1,'tbody','崛S´3§'),(3879,1,'60','䙷ͷ'),(3879,1,'belong','拒'),(3871,1,'backslash','ᱯ'),(3871,1,'60','䎥ǭ'),(3880,1,'removing','ⴉѥ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict84` ENABLE KEYS */;

--
-- Table structure for table `dict85`
--

DROP TABLE IF EXISTS `dict85`;
CREATE TABLE `dict85` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict85`
--


/*!40000 ALTER TABLE `dict85` DISABLE KEYS */;
LOCK TABLES `dict85` WRITE;
INSERT INTO `dict85` VALUES (3788,1,'comma','b'),(3819,1,'comma','z'),(3871,1,'1020613200','⮤'),(3820,1,'ifmib','´'),(3823,1,'comma',''),(3824,1,'comma','č\''),(3827,1,'5xx','þ'),(3866,1,'httpd','O'),(3869,1,'swig',''),(3819,1,'ifmib','_'),(3874,1,'detail','ǯ'),(3880,1,'relational','ˈ'),(3880,1,'practices','ቝ'),(3880,1,'merchantability','P'),(3880,1,'london','႞'),(3880,1,'detail','૷ଂŒൾU{ĜYRჳ×\nȦÕͫɥ'),(3883,1,'comma','Û'),(3881,1,'relational','Ṟ'),(3881,1,'proto','⏑'),(3881,1,'merchantability','P'),(3884,1,'detail','¼\n\Z<֮ͩ¯I'),(3881,1,'comma','㤽'),(3878,1,'practices','³'),(3881,1,'httpd','㖪'),(3879,1,'textbg','嵁hú'),(3871,1,'297','件9'),(3871,1,'3w','✮'),(3871,1,'comma','⛔'),(3879,1,'httpd','೼¿୼F\"ĸù'),(3879,1,'cell','俵'),(3879,1,'detail','ᆕ൵ኽÕƲYƣϰ)&?࡜െࢌ'),(3871,1,'1020613800','⯂'),(3764,1,'pfstate',''),(3879,1,'relational','͞'),(3876,1,'detail','Ċ'),(3778,1,'dbf','Y'),(3879,1,'comma','❘Ƕբᷗ'),(3879,1,'practices','≣፭'),(3875,1,'detail','ƨ'),(3743,1,'comma','Y'),(3744,1,'comma','Ƹ'),(3744,1,'proto','¨'),(3744,1,'separates','ũ'),(3759,1,'comma','W'),(3763,1,'dbms','*'),(3878,1,'merchantability','G'),(3879,1,'merchantability','P'),(3877,1,'merchantability','N'),(3871,1,'detail','㬦୆'),(3721,1,'relational','±'),(3884,1,'practices','ࣇ'),(3885,1,'detail','Ũv'),(3885,1,'eligible','^'),(3886,1,'detail','&|υl'),(3887,1,'detail','ཎI'),(3888,1,'detail','/£*ǰ'),(3890,1,'comma','Ђ?I7XN'),(3764,6,'pfstate','H');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict85` ENABLE KEYS */;

--
-- Table structure for table `dict86`
--

DROP TABLE IF EXISTS `dict86`;
CREATE TABLE `dict86` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict86`
--


/*!40000 ALTER TABLE `dict86` DISABLE KEYS */;
LOCK TABLES `dict86` WRITE;
INSERT INTO `dict86` VALUES (3881,1,'866','͗¥'),(3880,1,'ssl','ᮈ'),(3879,1,'uncomment','ഹ\'\"\''),(3871,1,'offsets','⓳Ý'),(3871,1,'naturally','⒈Q'),(3871,1,'describes','㻷'),(3871,1,'defs','䎸'),(3871,1,'undefined','䪁'),(3814,1,'maxbytes','½'),(3785,1,'ssl','M'),(3720,1,'library','&'),(3721,1,'library','ɘ'),(3728,1,'maxbytes','º'),(3784,1,'maxbytes','½'),(3873,1,'192','˞!\"!!%\'%%%)()((&'),(3778,1,'library','n'),(3818,1,'ssl','M'),(3871,1,'colortag','ུئ'),(3861,1,'ports',''),(3854,1,'ssl','P¹'),(3854,1,'maxbytes','á'),(3840,1,'reverse','Ę'),(3837,1,'arrived','R'),(3833,1,'ssl','M'),(3833,1,'maxbytes','½'),(3829,1,'hpjd','C'),(3827,1,'ssl','Èŵ$'),(3826,1,'changes','\Z'),(3819,1,'ports',''),(3871,1,'displayed','ᒺ┾=0'),(3871,1,'reverse','ᛚ␫'),(3871,1,'doc','㵑ৃ&'),(3761,1,'maxbytes','½'),(3870,1,'changes','ą'),(3862,1,'library',''),(3761,1,'ssl','M'),(3879,1,'ports','⪮'),(3879,1,'lookups','⫡'),(3879,1,'library','൳䣐G\n\né\n	\r\n«¥r\n\n3Y\n$\'N'),(3879,1,'displayed','ࢼČÅŜʁÙʁТ.úኙN§⇐ɿਈ'),(3879,1,'describes','݇㴕৑'),(3879,1,'changes','तឤׯÁߤÄ]੄ƭ଒ᒖԫ'),(3879,1,'866','Մ¥'),(3879,1,'192','䵈'),(3880,1,'library','ࡘ㮛'),(3880,1,'describes','ͬ㎸ಿĳ'),(3880,1,'desired','⎄'),(3880,1,'displayed','௡ɜ=ؤ\nΎåB\"3ɰ\"ArɻUȉò҆ෝ๎˝~¬6'),(3880,1,'changes','ᄮŢ̃ဃ۱ĝ͉âЅɴ'),(3880,1,'866','Շ¥'),(3756,1,'library','²'),(3748,1,'ssl','M'),(3748,1,'maxbytes','½'),(3739,1,'ssl','J'),(3739,1,'maxbytes','º'),(3737,1,'ssl','J'),(3737,1,'maxbytes','º'),(3732,1,'ssl','J'),(3732,1,'maxbytes','»'),(3728,1,'ssl','J'),(3729,1,'ssl','K'),(3877,1,'describes','¡'),(3879,1,'ssl','üۛᄌ F'),(3879,1,'reverse','⫟'),(3881,1,'describes','آ┩'),(3871,1,'changes','ᐋ'),(3871,1,'blame','㕓'),(3871,1,'bindings','ĭ8î'),(3870,1,'library',''),(3818,1,'maxbytes','½'),(3814,1,'ssl','M'),(3881,1,'changes','ݣ'),(3872,1,'changes','௖^'),(3785,1,'maxbytes','½'),(3784,1,'ssl','M'),(3881,1,'desired','ᦛ'),(3881,1,'getservice','ॾೖ'),(3881,1,'getsource','㋧'),(3881,1,'library','ӌ᫥׏'),(3881,1,'stream','ḗ«Ʊ·ʫ'),(3882,1,'displayed','Đ'),(3883,1,'changes','®'),(3884,1,'changes','প'),(3884,1,'desired','߆'),(3884,1,'displayed','ၴ9ʀp'),(3885,1,'changes','ࠛ\"R{'),(3885,1,'displayed','ߛ'),(3886,1,'changes','cƖǨ'),(3886,1,'displayed','»ˬ'),(3887,1,'changes','Ѓëӹķµͬď'),(3887,1,'displayed','ã஠'),(3887,1,'library','ඍ'),(3888,1,'changes','ش'),(3888,1,'displayed','ڇ'),(3890,1,'changes','ëШၶԬǤB'),(3890,1,'desired','᷺'),(3890,1,'displayed','·Ɓ˳́ቡćóý'),(3890,1,'eliminate','ጇ'),(3891,1,'changes','¹̈Æ'),(3829,6,'hpjd','');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict86` ENABLE KEYS */;

--
-- Table structure for table `dict87`
--

DROP TABLE IF EXISTS `dict87`;
CREATE TABLE `dict87` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict87`
--


/*!40000 ALTER TABLE `dict87` DISABLE KEYS */;
LOCK TABLES `dict87` WRITE;
INSERT INTO `dict87` VALUES (3871,1,'phrase','⛅'),(3740,1,'phrase','oa'),(3871,1,'resistant','඲'),(3871,1,'oss','ↂ'),(3766,1,'molitor','ä'),(3763,1,'accepts','á'),(3871,1,'mgrid','ᖾ'),(3871,1,'lecture','䑏'),(3863,1,'apt',','),(3871,1,'920808600','㡋{'),(3871,1,'accepts','ٺ٬'),(3877,1,'distant','ٴ\Z,'),(3822,1,'naming',''),(3879,1,'ldapauthmodule','र'),(3879,1,'crt','ᦂ!'),(3721,1,'phrase','ę'),(3721,1,'backends','ȼ'),(3872,1,'wizard','ޟ'),(3872,1,'contactgroup','ఖ@'),(3871,1,'upside','㻳'),(3878,1,'literally','¼'),(3877,1,'trusting','ٵ7'),(3804,1,'vv',''),(3802,1,'isdn','!'),(3879,1,'oss','ͱ'),(3879,1,'wizard','⏵Ɉ\rNʥǇ༉'),(3880,1,'oss','˛'),(3880,1,'phrase','䜯 '),(3880,1,'wizard','ਭ'),(3884,1,'naming',''),(3887,1,'accepts','ϳ\n'),(3887,1,'wizard','ς'),(3888,1,'exits','÷'),(3888,1,'naming','Á΅ɝ'),(3891,1,'contactgroup','͡'),(3892,1,'wizard','Ù'),(3863,6,'apt','Ņ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict87` ENABLE KEYS */;

--
-- Table structure for table `dict88`
--

DROP TABLE IF EXISTS `dict88`;
CREATE TABLE `dict88` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict88`
--


/*!40000 ALTER TABLE `dict88` DISABLE KEYS */;
LOCK TABLES `dict88` WRITE;
INSERT INTO `dict88` VALUES (3816,1,'ad0s1a','+'),(3879,1,'request','؋ᑘ'),(3877,1,'logging','ٕ'),(3875,1,'csv','ʟ'),(3804,1,'cn','S'),(3782,1,'service1','Ė'),(3874,1,'count','ýǌÃ'),(3871,1,'rrdupdate','൑'),(3871,1,'request','↼ᆩ'),(3871,1,'reach','䕋'),(3879,1,'csv','䲊\\³L\'ë\'i<'),(3871,1,'effectively','⠢'),(3879,1,'partitions','ᷴh'),(3879,1,'count','䭞ӽ'),(3880,1,'logging','үÍ׿&_ó୞'),(3880,1,'count','㧜cVKƹg[bKAjg'),(3879,1,'overwrite','㄁ᾖᒌ'),(3871,1,'ff00ff','㧥'),(3879,1,'logging','ҬÍ۞౩'),(3871,1,'overwrite','㚣'),(3871,1,'logging','$䎵'),(3871,1,'indicate','㽴'),(3837,1,'pendcrit','â'),(3840,1,'slow','Ñ'),(3846,1,'partitions','ĉ'),(3867,1,'3200s','!'),(3871,1,'count','ࣅ˫ ᬇ'),(3880,1,'indicate','ݗኯʋ!ᛁߟрɥ'),(3782,1,'request','QNC+3'),(3759,1,'slow','q'),(3736,1,'partitions',''),(3754,1,'count','ȩ'),(3757,1,'count','ŀ'),(3759,1,'count','¥'),(3720,1,'initialized','V'),(3880,1,'request','؎㳠'),(3881,1,'collageservicequery','࣎¯ʟਜ'),(3881,1,'count','ᑝƊġȊ'),(3881,1,'gethostbyid','ᔂ'),(3881,1,'logging','ʿÍ❝Ҍ'),(3881,1,'request','О'),(3885,1,'overwrite','w'),(3886,1,'partitions','һ'),(3890,1,'logging','ྟ:!H\"'),(3890,1,'overwrite','Ὓ'),(3890,1,'slow','๣');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict88` ENABLE KEYS */;

--
-- Table structure for table `dict89`
--

DROP TABLE IF EXISTS `dict89`;
CREATE TABLE `dict89` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict89`
--


/*!40000 ALTER TABLE `dict89` DISABLE KEYS */;
LOCK TABLES `dict89` WRITE;
INSERT INTO `dict89` VALUES (3871,1,'busy','䟼'),(3827,1,'encoded','Ė'),(3879,1,'stylesheet','崈'),(3879,1,'mass','₯'),(3879,1,'dtd','峮'),(3873,1,'user9','ʒ\rL\"!! \'\'%%%*\'*((%'),(3879,1,'collage','ኻ'),(3881,1,'collage','ӄϻĬ\'¶ĵ&\r&\"&¶\r	\nԨ<ĉŗÇϽ࣍'),(3880,1,'identifying','ࠈゕ'),(3780,1,'dcb','Ď'),(3780,1,'cbuff','Ě'),(3881,1,'executiontime','ࡵ'),(3881,1,'getstatetype','ᅓ'),(3887,1,'vrml','೙'),(3890,1,'finalize','ԥ'),(3890,1,'vrml','ڿ1');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict89` ENABLE KEYS */;

--
-- Table structure for table `dict8A`
--

DROP TABLE IF EXISTS `dict8A`;
CREATE TABLE `dict8A` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict8A`
--


/*!40000 ALTER TABLE `dict8A` DISABLE KEYS */;
LOCK TABLES `dict8A` WRITE;
INSERT INTO `dict8A` VALUES (3720,1,'written','|'),(3751,1,'predicted','ÁR-ù\r'),(3780,1,'volume','³		\n'),(3783,1,'nrpe','	J#'),(3792,1,'written','Q'),(3802,1,'4000',')'),(3831,1,'rpm','®'),(3871,1,'931225537','❌'),(3871,1,'channel','䃩	'),(3871,1,'gcc','ķ0'),(3871,1,'readable','ᾲ'),(3871,1,'rpm','ĩ'),(3871,1,'st','ᄁ'),(3871,1,'volume','౔'),(3871,1,'written','Ԅ®<%ݰɥخᇠࠨÀ฽ϫ'),(3873,1,'nrpe','ū	\ZU!\"!!%\'%%%)()((&'),(3877,1,'boston','|'),(3877,1,'keygen','ϼ4'),(3878,1,'boston','u'),(3878,1,'nrpe','ƺ\r̃33522220030%/'),(3879,1,'boston','~'),(3879,1,'cloning','㉂ߍ'),(3879,1,'editing','߻Ɣs'),(3879,1,'written','ᶌ͐⨲ᔲ'),(3880,1,'boston','~'),(3880,1,'cloning','䑕̽7\r2'),(3880,1,'editing','ň㋜ʐ'),(3880,1,'partial','➄'),(3880,1,'written','ភ'),(3881,1,'boston','~'),(3881,1,'cookie','㒽ǰ1ȁ'),(3881,1,'geteventsbyfilter','᥀ ?ş'),(3881,1,'written','௑ጁ	ÜǠ$ـfYǭঢ'),(3884,1,'written','ঀ'),(3888,1,'written','̼Ê'),(3890,1,'written','ᢁ-\'5'),(3783,6,'nrpe','á');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict8A` ENABLE KEYS */;

--
-- Table structure for table `dict8B`
--

DROP TABLE IF EXISTS `dict8B`;
CREATE TABLE `dict8B` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict8B`
--


/*!40000 ALTER TABLE `dict8B` DISABLE KEYS */;
LOCK TABLES `dict8B` WRITE;
INSERT INTO `dict8B` VALUES (3756,1,'substantial','¹'),(3879,1,'hostgroup','抣Bκ'),(3879,1,'critcal','Ṇ'),(3765,1,'807766570678168','½'),(3818,1,'received','É'),(3833,1,'received','É'),(3840,1,'retry','Ǧ'),(3846,1,'critcal','©'),(3854,1,'received','í'),(3871,1,'received','౳'),(3871,1,'yy','␣۔\n'),(3877,1,'received','a'),(3878,1,'received','Z'),(3761,1,'received','É'),(3748,1,'received','É'),(3756,1,'retry',''),(3784,1,'received','É'),(3785,1,'received','É'),(3814,1,'received','É'),(3739,1,'received','Æ'),(3737,1,'received','Æ'),(3880,1,'visual','Ή͓ᆗ'),(3880,1,'received','c'),(3879,1,'received','c䬢'),(3881,1,'hostgroup','ऌલe'),(3736,1,'critcal','q'),(3728,1,'received','Æ'),(3732,1,'received','Ç'),(3881,1,'received','c'),(3881,1,'visual','ᰓ'),(3884,1,'retry','ɇpॽp'),(3885,1,'retry','ӭ'),(3887,1,'coordinates','ɜT\Z*ાT\Z*'),(3887,1,'retry','ܮ'),(3888,1,'hostgroup','܅6Ĥ'),(3889,1,'hostgroup','҆.\n'),(3890,1,'coordinates','ڝ/'),(3890,1,'helping','ጅ࢐'),(3890,1,'received','᭎O'),(3890,1,'retry','࿘');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict8B` ENABLE KEYS */;

--
-- Table structure for table `dict8C`
--

DROP TABLE IF EXISTS `dict8C`;
CREATE TABLE `dict8C` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict8C`
--


/*!40000 ALTER TABLE `dict8C` DISABLE KEYS */;
LOCK TABLES `dict8C` WRITE;
INSERT INTO `dict8C` VALUES (3871,1,'ensure','╯'),(3880,1,'rights',''),(3881,1,'rights','⬄'),(3881,1,'question','⁠'),(3888,1,'rights',''),(3889,1,'escalate','ɉ'),(3888,1,'typically','ϔ'),(3876,1,'rights',''),(3875,1,'typically','Ȁ'),(3880,1,'question','⠬'),(3892,1,'rights','\r'),(3875,1,'rights',''),(3874,1,'titled','ǡ'),(3871,1,'connected','ஃ'),(3891,1,'rights','\rÊ'),(3890,1,'working','ᴑ'),(3883,1,'rights',''),(3882,1,'rights',''),(3881,1,'typically','܌	:'),(3871,1,'vname','ེۇbr+4Ŵ	¹\Z	»Cค»	'),(3871,1,'question','ఒ✌'),(3721,1,'rights',''),(3722,1,'connected','{'),(3726,1,'ensure','E'),(3758,1,'procr',''),(3763,1,'typically','ì'),(3782,1,'working','Ĥ'),(3792,1,'working',';'),(3815,1,'connected',':'),(3823,1,'hosting','3'),(3841,1,'04','S%'),(3841,1,'connected',''),(3862,1,'rights','Z'),(3866,1,'working','>'),(3868,1,'rights',''),(3870,1,'rights','\n'),(3871,1,'04','⃑៓ƾ'),(3871,1,'978300600','䤡'),(3871,1,'assume','பὐॄ^ǵংજĥȒ'),(3876,1,'typically','l'),(3874,1,'typically',''),(3885,1,'typically','҅˕'),(3877,1,'puts','Ͻ'),(3881,1,'connected','ಧ'),(3880,1,'ensure','ࠃ'),(3880,1,'assume','⑜ۅ'),(3879,1,'working','ἁ⥣'),(3758,6,'procr','L'),(3880,1,'working','ආἘѦ'),(3880,1,'typically','ᓊ'),(3880,1,'titled','㌍'),(3884,1,'puts','ჸ'),(3884,1,'ensure','ࠗ'),(3885,1,'rights','\r'),(3871,1,'behaves','᪼'),(3873,1,'working','Ȁ'),(3872,1,'pagers','ÌӃ'),(3872,1,'question','֨'),(3872,1,'rights','ି'),(3873,1,'rights','\n'),(3873,1,'typically','ʉ'),(3871,1,'october','⛦'),(3874,1,'rights',''),(3720,1,'working','Ű'),(3720,1,'rights',''),(3871,1,'mixture','઱⼞'),(3871,1,'locale','ᆷ'),(3890,1,'ensure','ሪŚࠓ'),(3889,1,'rights','\r'),(3887,1,'assume','ۡ'),(3886,1,'rights','\r'),(3887,1,'typically','ۆ'),(3887,1,'rights','\r'),(3892,1,'puts','ơ'),(3881,1,'ensure','⅞ᒎ'),(3879,1,'typically','࿑̮kKᓿຨ΂࢟ဵ'),(3879,1,'rights','℄๼'),(3879,1,'escalate','⑞#឴#'),(3879,1,'ensure','䟘P6Ꮸ'),(3879,1,'dialog','卦'),(3878,1,'rights',''),(3890,1,'rights','ϣ'),(3877,1,'rights',''),(3871,1,'workings','ਏ'),(3871,1,'working','ฬ⍸ቒف'),(3884,1,'rights','\r'),(3885,1,'assume','Ҡ'),(3881,1,'working','¢ϣ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict8C` ENABLE KEYS */;

--
-- Table structure for table `dict8D`
--

DROP TABLE IF EXISTS `dict8D`;
CREATE TABLE `dict8D` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict8D`
--


/*!40000 ALTER TABLE `dict8D` DISABLE KEYS */;
LOCK TABLES `dict8D` WRITE;
INSERT INTO `dict8D` VALUES (3892,1,'comment','Ë'),(3751,1,'extrapolating','Ș'),(3727,1,'russel','Ė'),(3881,1,'gethost','ᓪ'),(3881,1,'codes','෦׀'),(3880,1,'comment','ⲤJϹIĄr/\n'),(3879,1,'arg2','㋑'),(3879,1,'clicked','᝷'),(3879,1,'comment','ആس'),(3879,1,'numerous','ມ'),(3879,1,'passive','䟊'),(3879,1,'rapidly','Ở'),(3841,1,'arg2','¢'),(3856,1,'passive','ĺ'),(3867,1,'adaptec',' '),(3871,1,'comment','ྋ௉ǉ'),(3871,1,'gas','䗦'),(3871,1,'imgformat','ཇԘ'),(3871,1,'overflow','ࠞC0䎋'),(3873,1,'arg2','˩!\"!!\'\'%%%*\'*\'(%'),(3828,1,'filesystemid',''),(3822,1,'standardized','²'),(3764,1,'openbsd','\"'),(3885,1,'passive','ϲ\n'),(3881,1,'rapidly','➃'),(3884,1,'arg2','܃'),(3751,1,'specifiy','ǉ'),(3721,1,'standardized','Û'),(3751,1,'vaule','ɬ'),(3888,1,'passive','ϡ'),(3887,1,'passive','ذ\n'),(3884,1,'passive','̱\r	ৰ\r	'),(3890,1,'passive','਼oztRѼ	ઃN'),(3890,1,'codes','ʘ'),(3890,1,'comment','จ໒'),(3874,1,'arg2','·\"ĊA93-'),(3880,1,'passive','ₐØāſ`Bۯ\nѠ\n\Z\n\r'),(3880,1,'drag','⍺'),(3880,1,'leverage','ޯ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict8D` ENABLE KEYS */;

--
-- Table structure for table `dict8E`
--

DROP TABLE IF EXISTS `dict8E`;
CREATE TABLE `dict8E` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict8E`
--


/*!40000 ALTER TABLE `dict8E` DISABLE KEYS */;
LOCK TABLES `dict8E` WRITE;
INSERT INTO `dict8E` VALUES (3885,1,'saved','ɢ'),(3872,1,'recipient','Ԓ\r8\rþ'),(3872,1,'hosts','࢔'),(3877,1,'hosts','´ƨÓŧʥ'),(3879,1,'position','ᢍ'),(3872,1,'saved','ટŎØ'),(3879,1,'ignore','Ṭ❮'),(3881,1,'restarting','㎮͕'),(3879,1,'hosts','Š᯷ǥţùǝ?	æ[\nT9+j03%ĎA?+\n*f	+\nA(OSfI͆ʶoa@Î6\ZN!QYÞ\n#D\"Ō;ƶąŞ=Bî4ঘםཏʝ\r'),(3879,1,'derived','䡆'),(3879,1,'authtype','ിI'),(3878,1,'hosts','Į̏6785*770˱(\r'),(3887,1,'derived','Ϝ'),(3886,1,'hosts',':4+Ü1V;:Û'),(3879,1,'saved','Ჰᇛ'),(3871,1,'transfers','㑄'),(3720,1,'linker','5'),(3872,1,'acc','˘'),(3879,1,'safely','峋'),(3873,1,'hosts','	B	Ú'),(3874,1,'hosts','Y'),(3872,1,'restarting','̎˭'),(3881,1,'labs','㒫'),(3880,1,'saved','㑬·ƨ'),(3881,1,'authtype','㙌Ȗ'),(3881,1,'hosts','܇ʟ਱0ƌT*'),(3880,1,'hosts','ܳϒӼJ\"\r\'ĮUxrëŗÿ¾&ĩIÑBL`	\Zr\n#.ȊS&\n*	oV40-#-?_19*!34ui]#wPEřj\n\rĒ\n\n\Z.=ޢ¹#Ê,8ÅQŮƁ¯CɸU?\r͉?áŌƮ'),(3882,1,'hosts','²	2'),(3884,1,'hosts','źӠĮeĐ°c+5ࢴ'),(3884,1,'saved','ᅠƅ'),(3885,1,'hosts','à\ZzOcT#1ÀŅYO\"ĦĚ\r;=1'),(3871,1,'locations','ㄦ'),(3871,1,'ignore','䷑'),(3871,1,'distance','䖪'),(3744,1,'hosts','0'),(3752,1,'hosts','Í'),(3754,1,'hosts',';îŠ,'),(3754,1,'ignore','Ɏ'),(3775,1,'invobi',''),(3783,1,'hosts','Á'),(3788,1,'hosts','$+'),(3805,1,'position','G('),(3806,1,'position','M'),(3820,1,'ignore','×'),(3830,1,'hosts','Q'),(3837,1,'remailer','ė'),(3839,1,'hosts',''),(3840,1,'hosts','ż'),(3844,1,'smbclient','¢'),(3846,1,'ignore','å'),(3856,1,'ignore','r'),(3866,1,'1000','¤'),(3871,1,'1000','Ӟ6ၰ῭öϻcE0%ࣸ'),(3871,1,'2400','೐S㯍\r'),(3871,1,'demonstrates','ᶧ'),(3887,1,'hosts','l\r),ÃD8ǤfT#1ÁŇYO\"Ë»!A,\\\rĿ¡Ĥ=\r%<#\r'),(3887,1,'position','ɕ'),(3887,1,'saved','஌Ҕ'),(3888,1,'hosts','l\Z_\ZŔ	\'	L	/»HOõ'),(3888,1,'ignore','̍'),(3889,1,'hosts','Ϗ^H'),(3889,1,'saved',''),(3890,1,'hosts','Ŧ̰\r/\rW#(Os7ÄĴցô̫nƬ)ŞڠA'),(3890,1,'ignore','োost'),(3890,1,'restarting','໦'),(3890,1,'saved','๋»'),(3891,1,'hosts','˟˗'),(3892,1,'saved','ù'),(3877,2,'hosts',''),(3886,2,'hosts',''),(3887,2,'hosts',''),(3887,6,'hosts','ᄕ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict8E` ENABLE KEYS */;

--
-- Table structure for table `dict8F`
--

DROP TABLE IF EXISTS `dict8F`;
CREATE TABLE `dict8F` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict8F`
--


/*!40000 ALTER TABLE `dict8F` DISABLE KEYS */;
LOCK TABLES `dict8F` WRITE;
INSERT INTO `dict8F` VALUES (3849,1,'extra','c'),(3879,1,'checks','ᵯêغ47Άહɷ·եÁ~'),(3861,1,'axis',''),(3860,1,'checks',''),(3772,1,'checks',''),(3779,1,'checks',''),(3778,1,'checks',''),(3836,1,'checks',''),(3835,1,'extra',':'),(3826,1,'checks',''),(3791,1,'extra',','),(3790,1,'checks',''),(3788,1,'match','*'),(3846,1,'checks',''),(3796,1,'checks',''),(3795,1,'visible','l'),(3795,1,'checks',''),(3794,1,'checks',''),(3770,1,'checks',''),(3769,1,'checks',''),(3875,1,'list','ʄ'),(3754,1,'match','ć'),(3877,1,'extra','ə'),(3859,1,'checks',''),(3856,1,'list','°'),(3773,1,'checks',''),(3767,1,'checks',''),(3771,1,'checks',''),(3720,1,'chart','œ'),(3792,1,'checks',''),(3788,1,'checks','\Z'),(3788,1,'extra',''),(3788,1,'list',')$\"'),(3777,1,'checks',''),(3775,1,'checks',''),(3774,1,'checks',''),(3828,1,'hprsc',''),(3873,1,'match','ƴ'),(3853,1,'checks',''),(3852,1,'checks',''),(3839,1,'list','#'),(3791,1,'checks',''),(3879,1,'clients','᩿'),(3879,1,'chart','䊋ģՉ'),(3825,1,'clients',''),(3847,1,'checks',''),(3835,1,'dat','5'),(3835,1,'checks',''),(3830,1,'checks','\Z'),(3845,1,'checks',''),(3841,1,'checks',''),(3792,1,'sqhs','>'),(3824,1,'list','/Å\''),(3823,1,'clients','­'),(3822,1,'checks','\"'),(3820,1,'match','Éb'),(3819,1,'list','|\n'),(3815,1,'extra','b'),(3812,1,'checks',''),(3811,1,'checks',''),(3808,1,'checks',''),(3805,1,'match','¡'),(3803,1,'checks',''),(3800,1,'checks',''),(3879,1,'designated','⪱ි'),(3879,1,'contactpager','〈'),(3861,1,'checks',''),(3720,1,'scrollbar',' '),(3846,1,'match','ą'),(3793,1,'visible','y'),(3878,1,'checks','ЈȀ'),(3837,1,'checks','4'),(3789,1,'checks','%'),(3781,1,'checks','	'),(3872,1,'match','߱'),(3872,1,'list','ձŌΉ)ȵ'),(3872,1,'contactpager','۪U΁Ï'),(3872,1,'checks','ڌœ'),(3871,1,'odat1','ᷞ\Z'),(3871,1,'quote','⛂ءʴ'),(3871,1,'scaled','ጓݓ'),(3871,1,'match','᷉ϱ⟌'),(3871,1,'lt','᠉'),(3863,1,'checks',''),(3863,1,'list','ÀO	'),(3863,1,'match','Ă%'),(3866,1,'checks',''),(3868,1,'list',''),(3871,1,'axis','նۑeɠŨ)ć·J\rĮÙ.ম᪩\"ృ'),(3871,1,'checks','࠯G'),(3871,1,'ds0','ᷕ±ε'),(3871,1,'extra','⟀ನݭ'),(3871,1,'formated','ᲓႥ'),(3871,1,'gradually','լ'),(3871,1,'list','áൢⓂ0൑བ('),(3765,1,'list','z\r'),(3764,1,'checks',''),(3757,1,'extra',' '),(3757,1,'checks',''),(3757,1,'50k','Ƌ'),(3721,1,'list','m'),(3722,1,'clients','@N'),(3722,1,'list','('),(3725,1,'checks',''),(3730,1,'checks',''),(3730,1,'list','\"'),(3733,1,'match','5'),(3734,1,'checks','\n'),(3735,1,'checks',''),(3736,1,'checks',''),(3738,1,'match','f'),(3740,1,'extra','Ă'),(3740,1,'list','ś#'),(3740,1,'match','Û'),(3741,1,'checks',''),(3741,1,'extra','6'),(3743,1,'checks',''),(3743,1,'extra','2'),(3743,1,'list','['),(3744,1,'checks',''),(3744,1,'list','×å'),(3744,1,'match','Į'),(3749,1,'checks',''),(3751,1,'calulating','Ǚ'),(3751,1,'checks',' '),(3752,1,'checks',''),(3753,1,'checks','['),(3754,1,'checks','ʚ'),(3754,1,'list','ƈ'),(3856,1,'match','Ō'),(3879,1,'extra','᥎޻'),(3879,1,'filenames','天³'),(3879,1,'list','ࣤƻ«ಬఴӵ_?Vì?jrѩmȚǼ̺ŖǓąŎLඒ'),(3879,1,'match','Ɒᝪô»צKF¹(\nt^\n+Çક\Z'),(3879,1,'navigate','᱙j಩'),(3879,1,'visible','਱ӆ'),(3880,1,'chart','ᙐɯ‱LZ\r\Z7WIQ\n\nj\ZA[[LKA5%3%3l\n\n\nfA@AF\n\n'),(3880,1,'checks','ᇆࠅ̳΂µ\ra¡Á¥{α	ɣ\r.@7\Z̉\r<\r\r>*(ࢯन'),(3880,1,'designated','ṓD'),(3880,1,'list','ߨ্ᄬ\nL;ɻ\ZOs\nIAc*ሒUKOǎY_IJChgƸ?BA֛'),(3880,1,'navigate','᪦ુA⅂'),(3880,1,'visible','இý&Ł'),(3881,1,'checks','ᴢ'),(3881,1,'designated','घ'),(3881,1,'list','ቢ\Z\r(5¼ƛö׻ɋ಍'),(3881,1,'match','රӊ̓4Á4x(j(\\(ƢȾyᅪƗ࢔'),(3881,1,'visible','⨿'),(3882,1,'list','i^%'),(3883,1,'checks','Á'),(3884,1,'checks','ȯT\r\n\r\n&-	ΚŇΏT\r\n\r\n&-	Ѝ'),(3884,1,'list','۲±-cఓ'),(3885,1,'checks','Ϗ\nø\n'),(3885,1,'list','[ŴՇŨ'),(3886,1,'checks','Űɿ\"'),(3886,1,'list','µō˙'),(3887,1,'checks','Ƙѵ\n ú\n'),(3887,1,'extra','ʭ஑'),(3887,1,'list','Ý˧@)ԕÛ'),(3888,1,'checks','̃Â	ƏKȒ5­'),(3888,1,'failover','߷'),(3888,1,'list','ΦƛĴÃ0'),(3888,1,'match','Ԣŵ'),(3889,1,'list','ͧ'),(3890,1,'checks','਑pPpRƏƣŊ	¬$&A,	\ZS&\n:\'G24YįĞ͒B\r'),(3890,1,'list','Є?37b6-ᤓ'),(3890,1,'visible','ޗ'),(3891,1,'contactpager','ņ'),(3891,1,'list','Ȝâ˗<'),(3892,1,'checks','ı'),(3892,1,'list','e'),(3828,6,'hprsc','§'),(3861,6,'axis',';');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict8F` ENABLE KEYS */;

--
-- Table structure for table `dict90`
--

DROP TABLE IF EXISTS `dict90`;
CREATE TABLE `dict90` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict90`
--


/*!40000 ALTER TABLE `dict90` DISABLE KEYS */;
LOCK TABLES `dict90` WRITE;
INSERT INTO `dict90` VALUES (3871,1,'notice','㥯¨Âႝ'),(3871,1,'sasha','ሦ'),(3740,1,'advised','Ć'),(3878,1,'37','م'),(3827,1,'precede','Ŧ'),(3827,1,'lookup','³'),(3871,1,'interlaced','གҐ'),(3871,1,'mega','ጯ'),(3720,1,'copy','ö'),(3871,1,'temperature','IņЂמĮ()Ⓝxஆঊ,K׏'),(3871,1,'head','⼎Edೡ'),(3871,1,'fast','ࢠ㌒\Z#7कQ'),(3871,1,'copy','佫'),(3864,1,'appletalk',''),(3855,1,'temperature','\Z'),(3848,1,'lookup','\'0'),(3835,1,'advised','>'),(3840,1,'issues','ƃ'),(3840,1,'lookup','u'),(3841,1,'37','=%'),(3847,1,'temperature','\Z\"'),(3834,1,'fast','$'),(3834,1,'lookup','_'),(3803,1,'qmail','%W<'),(3798,1,'incoming','\Z c'),(3797,1,'netsaint',''),(3877,1,'issues','ɚĉ'),(3877,1,'copy','cÉϷ@'),(3875,1,'copy','ʌ'),(3872,1,'issues','ۚ'),(3872,1,'epager','8Ѯʂƭ('),(3872,1,'copy','ݶƫǅů'),(3879,1,'copy','e࿤ᄷ̩-ெᏫʙᎊ!'),(3879,1,'advised','げ'),(3878,1,'incoming','ЌȀ'),(3878,1,'copy','\\'),(3731,1,'37','K'),(3727,1,'temperature','gz'),(3722,1,'netsaint',''),(3721,1,'fast','ư'),(3871,1,'suggestion','䀊ϭ'),(3879,1,'discovered','⧍?ŝ?'),(3877,1,'notice','ǹą'),(3871,1,'incoming','ތ'),(3871,1,'improve','٥'),(3810,1,'netsaint',''),(3827,1,'head','ĳ'),(3720,1,'contrast','ķ'),(3879,1,'head','崁'),(3879,1,'improve','憇'),(3879,1,'incoming','䮵'),(3879,1,'issues','ۨይ'),(3879,1,'notice','⧓Ɯ͒'),(3879,1,'truncates','ẍ'),(3880,1,'copy','e'),(3880,1,'fast','᛬'),(3880,1,'grouped','ሌ⣟̍'),(3880,1,'improve','ᛴ↤,'),(3880,1,'issues','ьνⴝ'),(3880,1,'notice','݌'),(3880,1,'temperature','ॕᑪ'),(3881,1,'copy','e'),(3881,1,'incoming','ᲴżP·%\'ƺ'),(3881,1,'join','᪵'),(3881,1,'notice','ヽ'),(3881,1,'temperature','ὼ'),(3883,1,'copy','i'),(3883,1,'notice','ì'),(3884,1,'copy','ࡏ˕'),(3885,1,'grouped','ދ'),(3885,1,'stalk','܀'),(3886,1,'copy','w'),(3887,1,'copy','Аӯǩ'),(3887,1,'stalk','ी'),(3890,1,'copy','࠵ᔀ'),(3890,1,'improve','ᕚ'),(3891,1,'copy','y˾À'),(3864,6,'appletalk','');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict90` ENABLE KEYS */;

--
-- Table structure for table `dict91`
--

DROP TABLE IF EXISTS `dict91`;
CREATE TABLE `dict91` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict91`
--


/*!40000 ALTER TABLE `dict91` DISABLE KEYS */;
LOCK TABLES `dict91` WRITE;
INSERT INTO `dict91` VALUES (3829,1,'sourceforge','>'),(3820,1,'utility',''),(3819,1,'utility','Ù'),(3812,1,'utility',' '),(3811,1,'attribute','b'),(3783,1,'execution',''),(3781,1,'sys',')'),(3776,1,'sys','Ë'),(3763,1,'sourceforge','ę'),(3753,1,'numbers','|'),(3751,1,'numbers','ƉĚ'),(3744,1,'sourceforge','Ƨ'),(3881,1,'attached','⯛'),(3879,1,'utility','ᆳ㞽˳'),(3880,1,'readers','Ο䉍'),(3880,1,'yearly','㭛рɥ'),(3879,1,'centric','ᔇ'),(3879,1,'earlier','༤'),(3879,1,'execution','㻙ূ!ίং'),(3724,1,'validates',''),(3726,1,'invoked','6'),(3727,1,'utility','â'),(3880,1,'quick','᜹ŧƴ'),(3881,1,'earlier','⁪'),(3881,1,'centric','ࡃ'),(3881,1,'centralized','Ɵ'),(3881,1,'attribute','ઈᡱ'),(3881,1,'execution','Ⰱ'),(3871,1,'pops','᝞B#»'),(3868,1,'sourceforge','Û'),(3880,1,'numbers','᳏ˊȗ'),(3880,1,'centric','৙'),(3880,1,'centralized','ࢪ'),(3879,1,'quick','娠'),(3837,1,'pw','č'),(3840,1,'utility','ɨ+'),(3862,1,'utility','%'),(3831,1,'sourceforge','°'),(3721,1,'sourceforge','ʍ'),(3869,1,'tape','Á'),(3871,1,'earlier','֞䤿'),(3871,1,'fun','䇖'),(3871,1,'numbers','ဳᆱࡋ࡬ŉɆ\rIő	~Ǹ4ҷɃ?G̔	Ʈ¡ɗù)ŁÏw ~ġ'),(3872,1,'numbers','ցݤ'),(3877,1,'untar','ՙ'),(3872,1,'execution','ïλǚ'),(3871,1,'quick','㲝'),(3881,1,'foundationmodule','஫'),(3881,1,'getdeviceforhost','॰௽'),(3881,1,'gethostgroupsformonitorserver','ম'),(3881,1,'getparam','ਿ'),(3881,1,'invoked','ૉ⟻'),(3881,1,'numbers','ⅆᓅ'),(3881,1,'protect','㡢\r'),(3881,1,'sourceforge','హ‼ഽ'),(3884,1,'attribute','Þ'),(3884,1,'execution','ࡥࢣt`'),(3886,1,'numbers','ӗ'),(3887,1,'numbers','́க'),(3887,1,'utility','ආ'),(3888,1,'subsequent','Դ'),(3889,1,'sourceforge','ӣ'),(3890,1,'execution','ጞߝ'),(3892,1,'execution','Ʊ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict91` ENABLE KEYS */;

--
-- Table structure for table `dict92`
--

DROP TABLE IF EXISTS `dict92`;
CREATE TABLE `dict92` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict92`
--


/*!40000 ALTER TABLE `dict92` DISABLE KEYS */;
LOCK TABLES `dict92` WRITE;
INSERT INTO `dict92` VALUES (3821,8,'0','6'),(3822,8,'0','ú'),(3775,8,'0','$'),(3752,1,'config','3'),(3720,8,'0','ƶ'),(3880,1,'downtimes','⢒өƯ'),(3777,8,'0','%'),(3828,1,'hp','#'),(3806,8,'0','m'),(3741,8,'0','µ'),(3753,8,'0',''),(3827,1,'3xx','ü'),(3765,8,'0','Ù'),(3880,1,'presenting','ࢢ'),(3872,1,'0','ĖVjyÄFL\n/'),(3823,8,'0','Ç'),(3809,8,'0','_'),(3807,8,'0','<'),(3783,8,'0','Ò'),(3808,8,'0','u'),(3751,8,'0','ʦ'),(3750,8,'0','1'),(3762,8,'0','Ú'),(3880,1,'pending','ጔ\nڭŬm\nA\nnȔ;ʊÒ®>\\'),(3773,8,'0','%'),(3890,1,'config','ࠤ0'),(3748,8,'0','ę'),(3747,8,'0','V'),(3732,8,'0','ė'),(3733,8,'0',''),(3734,8,'0','ø'),(3735,8,'0','r'),(3736,8,'0','¸'),(3731,8,'0',''),(3730,8,'0',''),(3729,8,'0','Ï'),(3728,8,'0','Ė'),(3727,8,'0','ļ'),(3726,8,'0',''),(3725,8,'0','\\'),(3724,8,'0','b'),(3723,8,'0','?'),(3826,8,'0','!'),(3825,8,'0','ľ'),(3755,8,'0','º'),(3794,8,'0','\"'),(3793,8,'0','£'),(3790,8,'0','N'),(3786,8,'0',':'),(3771,8,'0','!'),(3810,8,'0',')'),(3756,8,'0','ð'),(3827,8,'0','ː'),(3811,8,'0','Á'),(3804,8,'0','°'),(3770,8,'0','!'),(3743,8,'0',''),(3764,8,'0','9'),(3752,1,'0','õ'),(3737,8,'0','Ė'),(3738,8,'0','|'),(3739,8,'0','Ė'),(3880,1,'located','ඛKᝠ'),(3879,1,'additionally','ۜ'),(3875,1,'0','Ē'),(3876,1,'0',''),(3877,1,'0','ر'),(3877,1,'600','і'),(3877,1,'config','Ǖör'),(3878,1,'measurements','ـ3352222003U/'),(3879,1,'0','Սᔙ⋔थƁǞ\Zெܥ%ĞʣkZá'),(3797,8,'0','g'),(3796,8,'0','$'),(3820,8,'0','ň'),(3819,8,'0','ě'),(3818,8,'0','ę'),(3817,8,'0','*'),(3816,8,'0','M'),(3815,8,'0','g'),(3814,8,'0','ę'),(3813,8,'0',''),(3812,8,'0','.'),(3871,1,'cooled','䠕'),(3871,1,'baesystems','ୟ'),(3871,1,'600','ೂ4Ꮘ!Ɩᑒ஄	F3׋ŋӅ3&'),(3837,1,'0','ě'),(3837,1,'pending','à'),(3841,1,'0','k'),(3847,1,'config','-	'),(3851,1,'0','U'),(3856,1,'0','Ů'),(3862,1,'0','s'),(3862,1,'oses','Ä'),(3862,1,'setuid',''),(3868,1,'0','C'),(3870,1,'0','U'),(3870,1,'soap','³'),(3871,1,'0','࢑жƉ˵Ĩ$0ʝȜ:9Ls»ĐǠ9ʏ!ķ܂ք׃ȑǋƗŖU=	ϻ)ýłŧНK)£bȎĈǋG@#'),(3758,8,'0','='),(3745,8,'0','÷'),(3776,1,'tablespaces','¡'),(3822,1,'0','_	'),(3744,8,'0','ɗ'),(3888,1,'conform','¾΅'),(3880,1,'soap','۪'),(3879,1,'measurements','⁀䘳'),(3879,1,'signed','ᥡ'),(3880,1,'0','Րဵড'),(3880,1,'additionally','р˶ທҢ࣊३Ѧ'),(3798,8,'0','ë'),(3752,8,'0','Ċ'),(3890,1,'0','໑NF̙Śؗ2̆k'),(3721,8,'0','ʗ'),(3801,8,'0','1'),(3872,1,'config','ϗ'),(3765,1,'0','§'),(3800,8,'0','@'),(3767,8,'0','#'),(3774,8,'0','&'),(3791,8,'0','«'),(3788,8,'0','×'),(3778,8,'0','ó'),(3780,8,'0','ƪ'),(3720,1,'0','!#'),(3768,8,'0','$'),(3871,1,'located','ᓦ'),(3782,8,'0','Ƒ'),(3760,8,'0','>'),(3795,8,'0',''),(3889,1,'0','ĿD͈'),(3872,1,'serialport','ŊS'),(3720,1,'measurements','¦'),(3888,1,'located','ˮ'),(3879,1,'config','ᯂ㢦N'),(3879,1,'graphdirectory','刷'),(3879,1,'hp','ᔓ'),(3879,1,'located','໻ޙ㏒ܶɋʔƣ݂'),(3873,1,'config','Ȯ'),(3802,8,'0','7'),(3749,8,'0','&'),(3881,1,'0','͠ࣇᨠčYƳϱۯ'),(3805,8,'0','È'),(3779,8,'0','/'),(3761,8,'0','ę'),(3766,8,'0','ì'),(3881,1,'config','㝌'),(3792,8,'0','_'),(3824,8,'0','Œ'),(3871,1,'exc','ᢴ'),(3740,8,'0','ƃ'),(3880,1,'measurements','㦄ࢾ\nm'),(3756,1,'config','('),(3799,8,'0','Ɗ'),(3772,8,'0','!'),(3890,1,'600','ࢫ'),(3746,8,'0','.'),(3722,8,'0','Ş'),(3754,8,'0','̊'),(3789,8,'0',''),(3785,8,'0','ę'),(3871,1,'pending','Ღ'),(3871,1,'shrink','➎C'),(3871,1,'squashes','ᳶ'),(3803,8,'0','Ù'),(3769,8,'0','!'),(3742,8,'0',''),(3763,8,'0','Ĵ'),(3759,8,'0','ù'),(3871,1,'hp','ᛥ'),(3721,1,'gallery','q'),(3873,1,'0','Ŀ'),(3787,8,'0','¥'),(3776,8,'0','Ģ'),(3890,1,'located','ᷞ'),(3751,1,'measurements','ȝ'),(3757,8,'0','Ƣ'),(3781,8,'0',''),(3784,8,'0','ę'),(3881,1,'myapp','㟮11'),(3881,1,'soap','ீဳ'),(3884,1,'0','ϥ\\ī/ࡕ\\ī/'),(3885,1,'0','͘1Ö=ƙ'),(3887,1,'0','ɱɿ1×?ƙ֌'),(3881,1,'located','ⓅY\r@ᇣ'),(3880,1,'hp','৥'),(3873,1,'exc','ϋ\'%%%'),(3829,1,'hp',''),(3734,1,'0','ô'),(3734,1,'config','0+L'),(3740,1,'0','ĺ'),(3742,1,'hp','/$'),(3828,8,'0',''),(3829,8,'0','r'),(3830,8,'0',''),(3831,8,'0','Ì'),(3832,8,'0','I'),(3833,8,'0','ę'),(3834,8,'0','´'),(3835,8,'0',''),(3836,8,'0','^'),(3837,8,'0','Ġ'),(3838,8,'0','='),(3839,8,'0','/'),(3840,8,'0','̓'),(3841,8,'0','¨'),(3842,8,'0','8'),(3843,8,'0','d'),(3844,8,'0','Ð'),(3845,8,'0','t'),(3846,8,'0','ĳ'),(3847,8,'0','O'),(3848,8,'0','ª'),(3849,8,'0','v'),(3850,8,'0','D'),(3851,8,'0',''),(3852,8,'0','%'),(3853,8,'0','M'),(3854,8,'0','Ľ'),(3855,8,'0','4'),(3856,8,'0','Ɗ'),(3857,8,'0','Y'),(3858,8,'0','.'),(3859,8,'0','<'),(3860,8,'0','A'),(3861,8,'0',','),(3862,8,'0','Ĕ'),(3863,8,'0','Ķ'),(3864,8,'0',''),(3865,8,'0',''),(3866,8,'0','±'),(3867,8,'0','*'),(3868,8,'0','Đ'),(3869,8,'0','ē'),(3870,8,'0','ĝ'),(3871,8,'0','僐'),(3872,8,'0','೩'),(3873,8,'0','՝'),(3874,8,'0','ϧ'),(3875,8,'0','̽'),(3876,8,'0','Ƥ'),(3877,8,'0','ޅ'),(3878,8,'0','़'),(3879,8,'0','未'),(3880,8,'0','䠸'),(3881,8,'0','㦸'),(3882,8,'0','ƨ'),(3883,8,'0','Ń'),(3884,8,'0','ᑜ'),(3885,8,'0','৓'),(3886,8,'0','԰'),(3887,8,'0','ᄊ'),(3888,8,'0','अ'),(3889,8,'0','ӵ'),(3890,8,'0','ᾉ'),(3891,8,'0','ل'),(3892,8,'0','ƺ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict92` ENABLE KEYS */;

--
-- Table structure for table `dict93`
--

DROP TABLE IF EXISTS `dict93`;
CREATE TABLE `dict93` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict93`
--


/*!40000 ALTER TABLE `dict93` DISABLE KEYS */;
LOCK TABLES `dict93` WRITE;
INSERT INTO `dict93` VALUES (3879,1,'tkt','ൟI਎'),(3828,1,'snmp',')'),(3827,1,'link','ƥ'),(3820,1,'snmp','2['),(3741,1,'snmp','3'),(3742,1,'snmp',''),(3743,1,'snmp','/'),(3744,1,'snmp','\'+¨8ª'),(3756,1,'watching','Ä'),(3757,1,'rs','Å'),(3762,1,'link','i'),(3765,1,'metrics','_'),(3786,1,'snmp','\''),(3819,1,'snmp','1'),(3740,1,'snmp','\r	7U'),(3855,1,'snmp','%'),(3871,1,'snmp','ㆇ6_ʖࣽÃ64ù9൶'),(3871,1,'picture','Ṳ┦ġő'),(3871,1,'mgmt','㼯'),(3871,1,'link','ɲ'),(3871,1,'image','࿩а\\\Z╭Ùࢇaʀ'),(3872,1,'snmp','Ũ'),(3872,1,'link','{ǣ̌'),(3871,1,'celsius','ᶜᆐ|ḱ'),(3871,1,'freely','ل⁨'),(3829,1,'snmp','#H'),(3843,1,'snmp',''),(3721,1,'picture','p'),(3879,1,'section2','嵨İ'),(3879,1,'orphan','拾'),(3879,1,'openldap','ीወ'),(3879,1,'link','࿞Ø/J⤆᰼ƣȅ	\rAɎ'),(3865,1,'snmp','#'),(3879,1,'image','㢁ᤛٱք'),(3879,1,'dbhost','刓'),(3880,1,'snmp','࡞㲥'),(3880,1,'metrics','࢔Ⱕ'),(3880,1,'link','ᒹ࢏ٜ̈́I௑ᑙ'),(3879,1,'102','䵪'),(3727,1,'celsius','l'),(3880,1,'temporarily','⬉Ŕ%Ѧħ'),(3878,1,'snmp','ƝȪ$'),(3879,1,'uninstall','჻ҷ䚰'),(3875,1,'snmp','($9 \n	\n \Z\'#\rF'),(3726,1,'link','('),(3853,1,'snmp','\"\Z'),(3720,1,'metrics',''),(3721,1,'link','ɜ'),(3879,1,'snmp','㓃ည୘'),(3880,1,'watching','㓁'),(3881,1,'image','ⓛ@'),(3881,1,'link','ț࢝Łᛧʞ'),(3881,1,'tkt','⫥ࢵ©!,\"/rƗ1'),(3882,1,'link',''),(3884,1,'image','጖'),(3884,1,'link','ٔ'),(3885,1,'link','݀'),(3886,1,'snmp','Ҿ'),(3887,1,'image','ɬBঊ	$	.\r/UB'),(3887,1,'link','ɇু'),(3890,1,'image','ع\"'),(3890,1,'temporarily','ᖭ'),(3875,2,'snmp',''),(3740,6,'snmp','ƒ'),(3741,6,'snmp','Ä'),(3742,6,'snmp',''),(3743,6,'snmp',''),(3744,6,'snmp','ɦ'),(3843,6,'snmp','t'),(3875,6,'snmp','͍');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict93` ENABLE KEYS */;

--
-- Table structure for table `dict94`
--

DROP TABLE IF EXISTS `dict94`;
CREATE TABLE `dict94` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict94`
--


/*!40000 ALTER TABLE `dict94` DISABLE KEYS */;
LOCK TABLES `dict94` WRITE;
INSERT INTO `dict94` VALUES (3862,1,'2007',''),(3861,1,'2007',''),(3823,1,'addresses','('),(3865,1,'2007',''),(3864,1,'2007',''),(3863,1,'2007',''),(3820,1,'2007',''),(3819,1,'2007',''),(3818,1,'2007',''),(3815,1,'2007',''),(3816,1,'2007',''),(3817,1,'2007',''),(3755,1,'2007',''),(3754,1,'2007',''),(3753,1,'2007',''),(3725,1,'2007','	'),(3724,1,'2007','	'),(3723,1,'2007','	'),(3867,1,'2007',''),(3866,1,'2007',''),(3871,1,'correction1','䴅\Z'),(3871,1,'click','㴎'),(3845,1,'2007',''),(3858,1,'2007',''),(3857,1,'2007',''),(3871,1,'dummy','ồ'),(3830,1,'2007',''),(3821,1,'2007',''),(3827,1,'2007',''),(3825,1,'2007',''),(3757,1,'2007',''),(3722,1,'addresses',' '),(3774,1,'2007',''),(3833,1,'2007',''),(3804,1,'2007',''),(3871,1,'1y6m','♞'),(3726,1,'2007','	'),(3813,1,'2007',''),(3824,1,'2007',''),(3838,1,'dummy',''),(3840,1,'2007',''),(3795,1,'2007',''),(3796,1,'2007',''),(3797,1,'2007',''),(3798,1,'2007',''),(3799,1,'2007',''),(3799,1,'novell','ę'),(3800,1,'2007',''),(3794,1,'2007',''),(3781,1,'timestamp',''),(3781,1,'2007',''),(3856,1,'2007',''),(3855,1,'2007',''),(3854,1,'2007',''),(3852,1,'2007',''),(3853,1,'2007',''),(3851,1,'2007',''),(3850,1,'2007',''),(3849,1,'2007',''),(3848,1,'2007',''),(3847,1,'2007',''),(3868,1,'2007',''),(3869,1,'2007',''),(3843,1,'2007',''),(3756,1,'2007',''),(3814,1,'2007',''),(3782,1,'2007',''),(3727,1,'2007','	'),(3859,1,'2007',''),(3812,1,'2007',''),(3811,1,'2007',''),(3810,1,'2007',''),(3809,1,'2007',''),(3808,1,'2007',''),(3807,1,'2007',''),(3806,1,'log2',''),(3806,1,'2007',''),(3840,1,'addresses','ʡ'),(3759,1,'2007',''),(3760,1,'2007',''),(3761,1,'2007',''),(3762,1,'2007',''),(3763,1,'2007',''),(3764,1,'2007',''),(3765,1,'2007',''),(3766,1,'2007',''),(3767,1,'2007',''),(3768,1,'2007',''),(3768,1,'statno','!'),(3769,1,'2007',''),(3770,1,'2007',''),(3729,1,'2007','	'),(3730,1,'2007','	'),(3731,1,'2007','	'),(3732,1,'2007','	'),(3733,1,'2007','	'),(3734,1,'2007',''),(3735,1,'2007','	'),(3736,1,'2007','	'),(3737,1,'2007','	'),(3738,1,'2007','	'),(3739,1,'2007','	'),(3740,1,'2007','	'),(3741,1,'2007','	'),(3742,1,'2007',''),(3743,1,'2007','	'),(3744,1,'2007','	'),(3745,1,'2007','	'),(3746,1,'2007',''),(3747,1,'2007',''),(3748,1,'2007',''),(3749,1,'2007',''),(3750,1,'2007',''),(3728,1,'2007','	'),(3860,1,'2007',''),(3828,1,'2007',''),(3831,1,'2007',''),(3842,1,'2007',''),(3841,1,'2007',''),(3836,1,'2007',''),(3838,1,'2007',''),(3837,1,'pa','Č'),(3721,1,'2007',''),(3829,1,'2007',''),(3773,1,'2007',''),(3758,1,'2007',''),(3839,1,'2007',''),(3826,1,'2007',''),(3846,1,'2007',''),(3803,1,'2007',''),(3772,1,'2007',''),(3722,1,'2007','	'),(3802,1,'2007',''),(3801,1,'2007',''),(3776,1,'2007',''),(3777,1,'2007',''),(3778,1,'2007',''),(3778,1,'dummy','^'),(3779,1,'2007',''),(3780,1,'2007',''),(3775,1,'2007',''),(3862,1,'addresses','='),(3823,1,'2007',''),(3822,1,'2007',''),(3844,1,'2007',''),(3793,1,'2007',''),(3784,1,'2007',''),(3785,1,'2007',''),(3786,1,'2007',''),(3787,1,'2007','	'),(3787,1,'retain','w*'),(3788,1,'2007',''),(3789,1,'2007',''),(3790,1,'2007',''),(3791,1,'2007',''),(3792,1,'2007',''),(3751,1,'2007',''),(3827,1,'append',''),(3783,1,'2007',''),(3771,1,'2007',''),(3870,1,'2007',''),(3832,1,'2007',''),(3726,1,'displaying',' '),(3805,1,'2007',''),(3780,1,'novell',' ř'),(3835,1,'2007',''),(3834,1,'2007',''),(3752,1,'2007',''),(3871,1,'meters','㕽:ҖÈŔ'),(3837,1,'2007',''),(3871,1,'mistake','㭻ଝ'),(3871,1,'rpath','ŉ'),(3871,1,'timestamp','൦ᩍŲ'),(3873,1,'2007',''),(3874,1,'2007','\n'),(3875,1,'2007','\n'),(3875,1,'addresses','ʉ'),(3876,1,'2007','\n'),(3877,1,'2007',''),(3878,1,'2007','	'),(3879,1,'2007',''),(3879,1,'addresses','⬓'),(3879,1,'append','摁'),(3879,1,'click','Қm¡˳ē±ȯ,ɺٳɽЛC፻਴ZৎҸν'),(3879,1,'dbpassword','刐'),(3879,1,'displaying','䋢'),(3879,1,'ibm','䶍'),(3879,1,'mistake','ᖩ'),(3879,1,'timestamp','搒4ȥ'),(3880,1,'2007',''),(3880,1,'click','ҝm¡ڟ᡹ႏ኉6'),(3880,1,'displaying','ବ'),(3880,1,'retain','㗊'),(3881,1,'2007',''),(3881,1,'click','ʭm¡⠩فR'),(3882,1,'2007',''),(3882,1,'addresses','Î'),(3882,1,'click','}'),(3883,1,'2007',''),(3884,1,'2007',''),(3884,1,'click','ߑǹŨֱʯ'),(3884,1,'retain','׎$%঴$%'),(3885,1,'2007',''),(3885,1,'retain','ˣ$$ԝT'),(3885,1,'utilize','í'),(3886,1,'2007',''),(3887,1,'2007',''),(3887,1,'click','к'),(3887,1,'retain','ԡ$$'),(3888,1,'2007',''),(3888,1,'arg','ց'),(3888,1,'click','ހ2'),(3889,1,'2007',''),(3889,1,'click','ͯ'),(3889,1,'displaying','ϛ'),(3890,1,'2007','\n'),(3890,1,'append','᥹'),(3890,1,'displaying','̛'),(3890,1,'retain','༭'),(3891,1,'2007',''),(3892,1,'2007',''),(3892,1,'click','ƌ'),(3806,6,'log2','|'),(3838,6,'dummy','L');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict94` ENABLE KEYS */;

--
-- Table structure for table `dict95`
--

DROP TABLE IF EXISTS `dict95`;
CREATE TABLE `dict95` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict95`
--


/*!40000 ALTER TABLE `dict95` DISABLE KEYS */;
LOCK TABLES `dict95` WRITE;
INSERT INTO `dict95` VALUES (3871,1,'turned','㑗'),(3871,1,'system',';΃ὴ፶৓'),(3724,1,'system','(\"'),(3870,1,'ii','t'),(3776,1,'tnsnames','^'),(3766,1,'system','+d'),(3764,1,'system','#'),(3725,1,'system',''),(3727,1,'system','Ĭ'),(3731,1,'difference','Y'),(3735,1,'system','\Z'),(3744,1,'system',''),(3756,1,'accepting',' '),(3756,1,'system','è'),(3763,1,'accepting',''),(3721,1,'system','C0(ż'),(3778,1,'sgadeforacle','W'),(3877,1,'system','ÄǔC\Z_ƴ\nÐ,'),(3873,1,'system','D'),(3871,1,'dot','㽚	'),(3868,1,'system','L-'),(3803,1,'system','³'),(3780,1,'system','&'),(3778,1,'tnsnames','Æ'),(3879,1,'system','̴~,\"ʛC	Ļjî\'¢͎4	7dİĠŘɶʣĪЅĆă2«Ӆ7å*mȠä dt яᆆ_Gî״ߞ֙AҖóܷ+¤7ĭ'),(3878,1,'system','ƮΘ'),(3879,1,'half','㫁'),(3879,1,'init','ශ௯Ȫ⻬ᔦ'),(3879,1,'s1chapter1b','巇C'),(3878,1,'remotely','ЧG785a'),(3877,1,'init','˵/'),(3874,1,'system','ɟ'),(3871,1,'half','ṥĉ។'),(3809,1,'system','\Z'),(3834,1,'system','a'),(3840,1,'turned','Œ'),(3845,1,'system',''),(3846,1,'system','\"'),(3851,1,'system','/'),(3856,1,'proto2','h'),(3871,1,'difference','㙓णיƠ8¿āYň'),(3871,1,'978303600','䤒'),(3871,1,'8601','❫'),(3879,1,'turned','䮍'),(3872,1,'init','ȉ1'),(3872,1,'system','Ɔ'),(3880,1,'accepting','⯹ҟ'),(3880,1,'system','Ʀø~BZKʷQţ-1A !Û&å&)6; 87ŅԜ	1ĽɆ؀ᙠBɦ³Á	৔2ǫU7ƕ'),(3881,1,'fastest','➌'),(3881,1,'init','⍂\Z̗࠻ƕ¶?ך'),(3881,1,'system','Đ°WGΛlG\n>0ӷਤך1Agǜ˦Ɖ¶͜ż¬~झÐ'),(3881,1,'turned','␎'),(3885,1,'difference','ơ'),(3885,1,'turned','Ӈ'),(3887,1,'turned','܈'),(3887,1,'uncompressed','ඕ'),(3890,1,'parallelization','ᇗ'),(3890,1,'system','Ϻ?ഹ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict95` ENABLE KEYS */;

--
-- Table structure for table `dict96`
--

DROP TABLE IF EXISTS `dict96`;
CREATE TABLE `dict96` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict96`
--


/*!40000 ALTER TABLE `dict96` DISABLE KEYS */;
LOCK TABLES `dict96` WRITE;
INSERT INTO `dict96` VALUES (3884,1,'returning','ᄃ'),(3881,1,'implementation','ɤˊᰝܗׯ'),(3881,1,'getvalue','㈺'),(3886,1,'appropriate','̎.'),(3871,1,'units','༰˯Ü\n)1*Ġ࿺\'<>'),(3884,1,'encapsulate','ࢽ'),(3884,1,'appropriate','٦Ǖ'),(3881,1,'protecting','㚿'),(3881,1,'doesn','Ẑ'),(3881,1,'appropriate','ὕ୥Ս'),(3880,1,'including','Ҋ̜ǹƸĉɯʑҲŎ˺Ǻ·әŢȐᇦ౛Rúl'),(3880,1,'implementation','ϕ䄑'),(3880,1,'appropriate','⏥ЁӺѦፈ'),(3879,1,'synchronizes','䪩ᓟ'),(3885,1,'including','ȷ'),(3799,1,'units','=k'),(3803,1,'appropriate','Ð'),(3840,1,'doesn','ŕÜ'),(3846,1,'units','_@\Z'),(3869,1,'including','^}'),(3869,1,'languages','L\"'),(3870,1,'including','ù'),(3871,1,'appropriate','ࡀЧگ͸ϛ$Ꮡ'),(3871,1,'counters','ࠂ⤧	ʮ8ഫׅѦ+'),(3871,1,'demo2','ຼ\r'),(3871,1,'doesn','ŗᱰᗪԟƪדȵ́Ѹϸ'),(3783,1,'appropriate','ª'),(3720,1,'datepicker','ű'),(3871,1,'including','ᑅᡡ՜'),(3885,1,'units','ի¡'),(3884,1,'including','Ŭ'),(3879,1,'including','҇ˇǔ౅čปخ˗ࡵºེኩ'),(3720,1,'including','Ŕ'),(3881,1,'gethostsbyfilter','ᖌճ'),(3881,1,'returning','ះ©â'),(3884,1,'units','ɶOő࡫Oő'),(3872,1,'appropriate','ȍ¹v'),(3874,1,'implementation',')ƶǞ'),(3875,1,'implementation',')ů¸'),(3876,1,'implementation',')Ñ'),(3877,1,'appropriate','̾'),(3878,1,'including','Ə'),(3879,1,'91','䵤'),(3879,1,'appropriate','᩶༿޳६ûĵᢽ'),(3879,1,'encapsulate','㗆'),(3879,1,'implementation','І෩ዻ&ᫀ'),(3871,1,'ysize','ᐻ'),(3871,1,'sorts','Ɖ'),(3871,1,'returning','㾒'),(3890,1,'appropriate','ਅost'),(3888,1,'implementation','Ϛ'),(3888,1,'appropriate','țǟ'),(3887,1,'units','̙ғ¡١'),(3887,1,'including','྘'),(3887,1,'origin','ɮૼ'),(3780,1,'nw','r	'),(3780,1,'hits',''),(3778,1,'doesn','°'),(3744,1,'units','aù'),(3740,1,'returning','ż'),(3738,1,'doesn','d'),(3736,1,'discrepancies',''),(3730,1,'todo','>'),(3722,1,'returning','Ë\r'),(3887,1,'appropriate','̹/'),(3892,1,'returning','Ƭ'),(3890,1,'including','Ḹ\Z'),(3881,1,'languages','↚ଁ'),(3881,1,'including','Ǹ¢ᡣۨɽ'),(3871,1,'stamps','㝚'),(3871,1,'escaped','ᱬ+');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict96` ENABLE KEYS */;

--
-- Table structure for table `dict97`
--

DROP TABLE IF EXISTS `dict97`;
CREATE TABLE `dict97` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict97`
--


/*!40000 ALTER TABLE `dict97` DISABLE KEYS */;
LOCK TABLES `dict97` WRITE;
INSERT INTO `dict97` VALUES (3879,1,'extracts','䖫'),(3879,1,'expression','䓣¾ǅ'),(3879,1,'sla','ᜍT'),(3879,1,'apis','䬵ᔲ'),(3878,1,'small','ŏ'),(3877,1,'tar','՘'),(3878,1,'defining','ࣟ'),(3877,1,'export','Ƀ'),(3879,1,'defining','Ŷه<ǛᕂȮͶǜ?!)ĬƟȈĉ®Ë̥n͘\ZmձǗËN؝ज'),(3872,1,'defining','પO'),(3871,1,'wave','䦛'),(3871,1,'small','ስ⠚ሉ$'),(3872,1,'tar','İp'),(3879,1,'export','ᓕɼ>ೱླྀ౩cï۬\rЦĂ'),(3720,1,'gfx','%'),(3721,1,'apis','ɍ'),(3723,1,'wave',''),(3728,1,'ipv6','n'),(3732,1,'ipv6','o'),(3733,1,'expression','/L'),(3737,1,'ipv6','n'),(3738,1,'ipv6','O'),(3739,1,'ipv6','n'),(3740,1,'expression','Ú'),(3741,1,'expression','j'),(3744,1,'expression','Ļ'),(3745,1,'ipv6','\\'),(3748,1,'ipv6','q'),(3751,1,'small','Ȅ'),(3752,1,'expression','¨'),(3752,1,'wathever','¥'),(3754,1,'expression','ĩ'),(3761,1,'ipv6','q'),(3762,1,'ipv6','F'),(3763,1,'ipv6','M'),(3784,1,'ipv6','q'),(3785,1,'ipv6','q'),(3805,1,'expression','4\r'),(3806,1,'expression','9\r'),(3811,1,'ipv6','['),(3814,1,'ipv6','q'),(3818,1,'ipv6','q'),(3827,1,'ipv6','Ã'),(3833,1,'ipv6','q'),(3854,1,'ipv6','|'),(3856,1,'ipv6','['),(3871,1,'1month','▍ŝ'),(3871,1,'acts','࠻'),(3871,1,'defining','ࣲࢡ'),(3871,1,'explanation','ਈي%ھ૊ ࠯&ᳫ'),(3871,1,'export','͖♲'),(3871,1,'expression','ྂ݊djɬЅ௓È'),(3871,1,'filling','㴹'),(3871,1,'miserably','ቾ'),(3871,1,'readings','ໞ'),(3879,1,'small','ͅ'),(3880,1,'apis','࿓'),(3880,1,'defining','ც'),(3880,1,'extracts','ྋ߱'),(3880,1,'small','ʯ'),(3881,1,'acts','⬯'),(3881,1,'apis','ҧôPö'),(3881,1,'isstatechanged','ᴑ'),(3881,1,'j2ee','◉'),(3881,1,'react','㇝'),(3882,1,'export','%1\n?'),(3883,1,'defining','%'),(3884,1,'defining','ݲʞ'),(3885,1,'export','¤Ŭص'),(3885,1,'exports','ࡆ'),(3888,1,'defining','z'),(3888,1,'export','}ƹ¥ך+'),(3889,1,'defining','ʂe'),(3890,1,'explanation','ᗞ'),(3890,1,'filling','͠'),(3890,1,'interleaved','ቜ'),(3891,1,'defining','>̀¿'),(3892,1,'defining','\"'),(3883,2,'defining',''),(3888,2,'defining',''),(3891,2,'defining',''),(3892,2,'defining',''),(3723,6,'wave','N');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict97` ENABLE KEYS */;

--
-- Table structure for table `dict98`
--

DROP TABLE IF EXISTS `dict98`;
CREATE TABLE `dict98` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict98`
--


/*!40000 ALTER TABLE `dict98` DISABLE KEYS */;
LOCK TABLES `dict98` WRITE;
INSERT INTO `dict98` VALUES (3722,1,'nmbd','ď'),(3736,1,'includes','¦'),(3756,1,'occur','í'),(3870,1,'includes','Ë'),(3871,1,'44','ຠ'),(3871,1,'airport','㉺'),(3871,1,'ends','㥆ᛗ'),(3871,1,'holiday','㉿'),(3871,1,'meaning','♷ᏴჁʍ'),(3871,1,'occur','ѻ'),(3871,1,'out1','ⰺ'),(3871,1,'remark','㸰'),(3872,1,'includes','׮س'),(3873,1,'disks','̐\n'),(3874,1,'includes','ņ'),(3875,1,'includes','Ğ'),(3876,1,'includes',''),(3877,1,'includes','ϖ'),(3878,1,'44','ި'),(3878,1,'includes','ЅȀ'),(3879,1,'44','䵖'),(3879,1,'detected','⸙'),(3879,1,'documents','ڄF伶G	)[Õ5U»v\n~º\rÚ'),(3879,1,'ends','俑'),(3879,1,'includes','ܮᙠݩФ஌ບᏃÓ'),(3879,1,'occur','⸸ᵝ'),(3879,1,'reviewed','ڱ'),(3880,1,'documents','ЀBԬ㪉l\"țqC'),(3880,1,'includes','ཙv᚜mZင໚[¤'),(3880,1,'lasts','⣥өƓ'),(3880,1,'occur','ጼ҅Ᏼ'),(3880,1,'reviewed','Г'),(3881,1,'detected','Ⅿ'),(3881,1,'documents','㖜'),(3881,1,'includes','؟bАᒢعӞ౷'),(3884,1,'detected','Ԓ਋'),(3884,1,'monarchexternals','ঃ5'),(3884,1,'occur','ʇ਋'),(3885,1,'detected','ׅ'),(3887,1,'detected','ࠆ'),(3890,1,'detected','ᶟ'),(3890,1,'monarchexternals','Ų\Z'),(3890,1,'occur','ሲŚķ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict98` ENABLE KEYS */;

--
-- Table structure for table `dict99`
--

DROP TABLE IF EXISTS `dict99`;
CREATE TABLE `dict99` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict99`
--


/*!40000 ALTER TABLE `dict99` DISABLE KEYS */;
LOCK TABLES `dict99` WRITE;
INSERT INTO `dict99` VALUES (3879,1,'things','ึ'),(3871,1,'tunable','⢙'),(3879,1,'built','ᇹ'),(3871,1,'things','ƋÁ֠×ƯẆއੳ͡ņฅ'),(3871,1,'represented','ᚤ૖'),(3871,1,'metric','ሖ'),(3879,1,'50','≧.<.ᇎᘤᔲ'),(3871,1,'300','ݧ՗--ƾήพƣࣥયѡ=ݴЊƚĶÛÕ͊\n.\"\nK'),(3880,1,'purposes','㓆ያ'),(3879,1,'85','㋞'),(3873,1,'built','O'),(3871,1,'consideration','᩾'),(3871,1,'lead','▁'),(3879,1,'08','搝'),(3871,1,'universe','䍀'),(3742,1,'listening','i'),(3879,1,'300','䙗ƑǦ'),(3879,1,'redundant','ᰦ⏨'),(3865,1,'85',''),(3865,1,'50',''),(3793,1,'listening',''),(3798,1,'things','Ë'),(3815,1,'50','B'),(3816,1,'50','-'),(3837,1,'50','ð'),(3837,1,'msg','û'),(3840,1,'things','Ź'),(3844,1,'85','{'),(3849,1,'listening','\\'),(3862,1,'arping','\"0'),(3863,1,'patches','½'),(3780,1,'engr','ƅ'),(3776,1,'things','ÿ'),(3871,1,'built','㜌ʠ'),(3871,1,'920808000','㡃}qƑà»'),(3880,1,'50','ࠬ'),(3880,1,'metric','ၟ'),(3880,1,'future','٣⎪ǯÎşƮÍ'),(3880,1,'built','ឭ'),(3879,1,'purposes','㎲FͿᇧ'),(3871,1,'920804400','㛉\ZƳƑà»'),(3871,1,'50','㠀Ȩ࠽`'),(3765,1,'metric','+Z'),(3763,1,'patches','Ħ'),(3757,1,'metric','#ć'),(3871,1,'3hours','⛿'),(3879,1,'nagiosfeeders','悆'),(3868,1,'patches','ê'),(3879,1,'metric','⎓๻'),(3879,1,'future','٠̇㮩ǔ޽'),(3871,1,'weird','乇'),(3870,1,'built','±'),(3880,1,'refreshed','◽0'),(3880,1,'represented','␨'),(3880,1,'sf','㧼ʽ'),(3881,1,'built','ǂ׉1ᢏত'),(3881,1,'future','ѵΠ␁Ɔ'),(3881,1,'gethosts','ॗ಺'),(3881,1,'inefficient','↔'),(3881,1,'listening','ὂ୵'),(3881,1,'represented','⮹'),(3881,1,'sf','⿧Μ'),(3885,1,'individually','ࣩZ'),(3887,1,'weird','ട'),(3888,1,'built','ͮ'),(3888,1,'individually','ѧU'),(3888,1,'redundant','ߡ'),(3889,1,'configurator','ҩ'),(3890,1,'future','ᑞ9R'),(3890,1,'purposes','ࠚ'),(3862,6,'arping','ģ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict99` ENABLE KEYS */;

--
-- Table structure for table `dict9A`
--

DROP TABLE IF EXISTS `dict9A`;
CREATE TABLE `dict9A` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict9A`
--


/*!40000 ALTER TABLE `dict9A` DISABLE KEYS */;
LOCK TABLES `dict9A` WRITE;
INSERT INTO `dict9A` VALUES (3752,1,'file','		'),(3881,1,'requirement','㡠'),(3847,1,'file','.	'),(3851,1,'report','s'),(3856,1,'file','§H'),(3860,1,'file','\Z'),(3868,1,'file','Ì'),(3871,1,'covered','䭤'),(3871,1,'dose','乁'),(3871,1,'file','܅ۚÉ Ģ$࿄޴o	\nւ9Đߒ͢³ȕ'),(3871,1,'follow','ڻⱅట	'),(3871,1,'outputs','࿌'),(3871,1,'push','ᛷƃ#(\'\n'),(3825,1,'file','»'),(3819,1,'report',''),(3808,1,'file','^'),(3807,1,'file',''),(3735,1,'file','\\'),(3890,1,'reaper','ኵ'),(3881,1,'outputs','଎'),(3881,1,'report','ߌṞ'),(3884,1,'covered','Ɓ̅਋'),(3879,1,'parseregx','䨄'),(3879,1,'report','ͬ)໧ކǇ\r㕁g.2ė;OÈ\nCჼ'),(3879,1,'outputs','䔣'),(3879,1,'follow','᪐⦸Ὧ'),(3882,1,'file','	'),(3881,1,'gethostsformonitorserver','ট'),(3885,1,'push','ট'),(3887,1,'absolutely','ނ'),(3887,1,'covered','ࢻ'),(3887,1,'follow','໿'),(3888,1,'file','Àʫ»^_'),(3890,1,'file','ͪ̊cAo \n7=ost@T6\n\rh_¦ÊIփ¦L\n\Z\n\Z!!\n\n\n\nʝª!)'),(3885,1,'covered','ٺ'),(3885,1,'absolutely','Ձ'),(3806,1,'file','&	'),(3805,1,'file','0		\n\n!'),(3754,1,'file','H-'),(3754,1,'report','İ'),(3756,1,'file',')D5\n'),(3778,1,'file','È '),(3780,1,'file','ŭ'),(3780,1,'report','ķ'),(3782,1,'file','ŭ'),(3788,1,'file',''),(3789,1,'file','M'),(3798,1,'file','b!'),(3799,1,'file','*\"k'),(3722,1,'file','>'),(3733,1,'file','0\Z'),(3845,1,'file','^'),(3880,1,'file','ᓕ᜔ҟ)ٳ୯Īѕ'),(3872,1,'file','éħɄ	;$Ǯǻǲű'),(3871,1,'russ','⦻'),(3884,1,'volatile','ȁ৲'),(3880,1,'tactical','૲'),(3880,1,'report','˖)㔻ŚĽN\n\n.\n\n9hZ`GIlfU\n\n.\n\n\n~b>C@\n\nf\n\nK.E>'),(3879,1,'file','೾͞˦ϴƍƑÕȠʺۂơ2\n\n݁Գ˂\n܇ЂǗ,)pƶę\r}ɧ\\e/s0\r\r¡\'i<ő (20NO-	V<$*50ŐŘO%>!ðwŉР	Z?2&NĹ8vcŁ'),(3878,1,'file','ȀʝȖ'),(3877,1,'file','ĿªÞTrċĐĭ1'),(3892,1,'file',''),(3827,1,'report','4'),(3832,1,'file','+'),(3835,1,'file','.'),(3836,1,'file',''),(3837,1,'file','¯'),(3840,1,'flaws','Ĕ'),(3840,1,'nameservers','ĞÔ'),(3842,1,'file',''),(3827,1,'follow','(,Š'),(3734,1,'file','\\0'),(3843,1,'file','N'),(3880,1,'responsible','Ⴋ='),(3877,1,'covered','ɟ'),(3890,1,'follow','ʐ'),(3751,1,'file','ɞ'),(3880,1,'follow','䜥'),(3881,1,'collagequeryfailed','਴'),(3881,1,'cluster','Ṭ'),(3881,1,'file','౎፹թկƚM\"X 5ڷ6\'źJŒ'),(3872,1,'follow','ɛ׳'),(3873,1,'file','Ƨ*^\\\r'),(3874,1,'file','ƪ'),(3875,1,'file','źĎ0'),(3875,1,'follow','ˈ'),(3876,1,'file','Ü'),(3871,1,'report','䶐'),(3846,1,'file','!'),(3836,6,'file','m');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict9A` ENABLE KEYS */;

--
-- Table structure for table `dict9B`
--

DROP TABLE IF EXISTS `dict9B`;
CREATE TABLE `dict9B` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict9B`
--


/*!40000 ALTER TABLE `dict9B` DISABLE KEYS */;
LOCK TABLES `dict9B` WRITE;
INSERT INTO `dict9B` VALUES (3871,1,'permit','⣖'),(3877,1,'add','ƀσ'),(3875,1,'add','̫'),(3872,1,'true','ԣ'),(3872,1,'add','٥ą°ɴōÔ'),(3871,1,'utc','ܺኘԠຸৄ'),(3880,1,'excluding','Ӷ'),(3871,1,'incrementing','ࠁ㗪'),(3877,1,'permit','͕'),(3878,1,'34','א'),(3871,1,'thing','䌔'),(3879,1,'true','ᯓ'),(3721,1,'add','}'),(3879,1,'excluding','ӳ'),(3871,1,'true','ᠧۻᐅ፣őʲΖ'),(3879,1,'add','ފĴƸ¥*UC஭\nɿDֈनƚñ,qwÂޗ̌ʜᭂ'),(3879,1,'utc','䚣Ȇ'),(3880,1,'add','⏴ǇőIଡLV\rɒ෪'),(3871,1,'fahrenheit','ᶡ'),(3847,1,'fahrenheit','>'),(3863,1,'true','m'),(3871,1,'add','ુႭ<ξԱ̖/˝჌ზ'),(3871,1,'cdefs','㬰'),(3881,1,'add','ᩥBւ¹đޱɄ'),(3880,1,'soft','ጰ'),(3880,1,'sill','⺔'),(3844,1,'netbios',':'),(3841,1,'add',''),(3797,1,'netbios',';'),(3722,1,'netbios','H'),(3723,1,'spedlan','\Z'),(3753,1,'rpcinfo',' '),(3759,1,'add','M'),(3774,1,'true','\"'),(3776,1,'excluding','Ê'),(3778,1,'add','ä'),(3793,1,'slave',''),(3795,1,'slave','t'),(3881,1,'excluding','̆'),(3881,1,'geteventbyid','ᤨ'),(3881,1,'incrementing','ԣᝨ'),(3881,1,'permit','㏭'),(3881,1,'true','ീَ÷2⊽'),(3883,1,'add','P'),(3884,1,'add','ĩjׯPZÖEw+Ŭ3ܜ /'),(3885,1,'add','İ؞2¨OY'),(3886,1,'add','˸»¿X'),(3887,1,'add','Ѝ		gӱƭ4ͥ\'¬C'),(3888,1,'add','ca΄âÎ.bU+.'),(3889,1,'add','U.ȧV];'),(3890,1,'add','ԂMUᚪÑT'),(3890,1,'soft','ᘌ#'),(3890,1,'thing','ᇎ'),(3891,1,'add','ĵ¾'),(3892,1,'add','â');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict9B` ENABLE KEYS */;

--
-- Table structure for table `dict9C`
--

DROP TABLE IF EXISTS `dict9C`;
CREATE TABLE `dict9C` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict9C`
--


/*!40000 ALTER TABLE `dict9C` DISABLE KEYS */;
LOCK TABLES `dict9C` WRITE;
INSERT INTO `dict9C` VALUES (3881,1,'portal','̙␜OQҶޗ'),(3881,1,'lastpluginoutput','࡜'),(3881,1,'include','ϟ̠ՄዐࠏӺ঑\"'),(3880,1,'smtp','ၗ୸'),(3720,1,'finishes','['),(3720,1,'soria','Ƌ'),(3745,1,'include',''),(3745,1,'smtp','	Q'),(3766,1,'include','_'),(3780,1,'include','Z'),(3782,1,'include','ţ'),(3786,1,'filer',''),(3827,1,'include',''),(3837,1,'smtp','&|	U'),(3840,1,'include','Ŷ'),(3863,1,'include','ª#Z'),(3868,1,'include','ü'),(3869,1,'include','8'),(3870,1,'include','p'),(3871,1,'24m','⚞'),(3871,1,'altogether','⚯'),(3871,1,'character','ᎄट\r'),(3871,1,'include','⏚FॢὍ'),(3871,1,'renamed','⪔'),(3871,1,'shift','㴓'),(3872,1,'include','ף§ŧ'),(3872,1,'prone','Ƙ'),(3878,1,'include','Î#'),(3878,1,'smtp','˷դ'),(3879,1,'character','效'),(3879,1,'include','׌ėҴ५ъॉՓܩྍ\'8ˆɶ'),(3879,1,'portal','̑ǵƉ䫟'),(3879,1,'smtp','㈆'),(3880,1,'hierarchical','เ঻ʾ࠮ɗ'),(3880,1,'include','чƈ̾ÇҜȍŔª࠹ƪJϠɟᖞ'),(3880,1,'portal','ɻʎ'),(3880,1,'shift','㕺'),(3883,1,'include','Ļ'),(3884,1,'character','ბ'),(3885,1,'include','࠴]'),(3886,1,'character','ӵ'),(3886,1,'shift','σ'),(3887,1,'include','Ϗ '),(3888,1,'character','ک$ą'),(3888,1,'include','˷'),(3890,1,'include','ۣ4ঃ'),(3745,6,'smtp','Ć');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict9C` ENABLE KEYS */;

--
-- Table structure for table `dict9D`
--

DROP TABLE IF EXISTS `dict9D`;
CREATE TABLE `dict9D` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict9D`
--


/*!40000 ALTER TABLE `dict9D` DISABLE KEYS */;
LOCK TABLES `dict9D` WRITE;
INSERT INTO `dict9D` VALUES (3880,1,'enter','ైǙ੘ྒྷâ̨ǁƓĒךၜ'),(3887,1,'enter','KëFƣôӳŗґ'),(3880,1,'monthly','㭙рɥ'),(3880,1,'managers','࿳'),(3720,1,'demos','*'),(3879,1,'unix','㓍'),(3880,1,'selectable','㡖'),(3880,1,'produce','㠴'),(3880,1,'contetns','䜒'),(3881,1,'variables','ഹ␠5'),(3881,1,'produce','⁰ਉ'),(3881,1,'enter','㏿'),(3880,1,'variables','㟼'),(3880,1,'unix','ި㉆ʽ࡜#'),(3886,1,'enter','YUĖLJǛ	'),(3885,1,'unix','ޯ¡'),(3885,1,'hostprofilename','ȟ'),(3885,1,'enter','ĩ·׭0'),(3884,1,'enter','Ģh࡮ľD2ҷdZƚ¢'),(3883,1,'enter','I'),(3879,1,'managers','ܗ'),(3879,1,'installs','៺'),(3740,1,'wasn','Ũ'),(3762,1,'produce','¶'),(3766,1,'unix','#'),(3766,1,'variables','^'),(3780,1,'variables','YŃ'),(3782,1,'variables',''),(3791,1,'unix',''),(3799,1,'variables','$Ę'),(3816,1,'variables',''),(3823,1,'enter',''),(3835,1,'managers','@'),(3840,1,'unix','ɧ'),(3854,1,'unix','G'),(3869,1,'artistic','-'),(3869,1,'unix','9'),(3871,1,'1020611700','⬷\"'),(3871,1,'alters','❼❖/'),(3871,1,'enter','ᱣ᩺'),(3871,1,'produce','ᑨᅦ᷈'),(3871,1,'unix','⍜ٌබ᠅'),(3871,1,'variables','឵Ʉ'),(3871,1,'wasn','䅭਽'),(3872,1,'variables','ĉ'),(3873,1,'enter','Ǝ'),(3874,1,'unix','\"\Zu$'),(3877,1,'enter','ˀŶimø'),(3877,1,'lad','ƞ'),(3877,1,'unix','\r´Ň͖'),(3877,1,'wq','ǫþ'),(3878,1,'unix','ƭɦ3 !\"!7\Z'),(3879,1,'csvmatch','什'),(3879,1,'enter','࣊Ʒଡ଼g֔ଟƘƹƵĨÄȢጾ8Ĝ\\Qᰎãſ\n\n\n'),(3721,1,'managers','Ǡ'),(3888,1,'dictate','Τ'),(3888,1,'enter','=ɦ5˴»'),(3889,1,'enter','{Ȧ'),(3890,1,'enter','Ưͪ˓ޕɷ«ɥֲȔ'),(3890,1,'monthly','ೌ'),(3890,1,'variables','༁'),(3891,1,'enter','b̂¾'),(3892,1,'enter','Û¦'),(3874,2,'unix',''),(3877,2,'unix',''),(3874,6,'unix','ϸ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict9D` ENABLE KEYS */;

--
-- Table structure for table `dict9E`
--

DROP TABLE IF EXISTS `dict9E`;
CREATE TABLE `dict9E` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict9E`
--


/*!40000 ALTER TABLE `dict9E` DISABLE KEYS */;
LOCK TABLES `dict9E` WRITE;
INSERT INTO `dict9E` VALUES (3879,1,'s1chapter2a','巜H'),(3879,1,'steps','ۏॊԸϘ2,ॻႅʭ෍ύw᫷'),(3880,1,'aspects','䕅'),(3880,1,'control','ȳٺقŻҮ'),(3880,1,'local','ᑙu'),(3880,1,'steps','Я؇᪽˦὎'),(3881,1,'aspects','⩜'),(3793,1,'local',''),(3789,1,'local',' f'),(3890,1,'local','ٛ¥·ᖒ'),(3890,1,'names','ӅXNᘕ'),(3879,1,'names','⟬Ԝ௑\n௮оűʅĂৗŔgțΦ'),(3881,1,'local','୫᠌ċF>\r\r$(!;;໗'),(3872,1,'steps','Ǻђ¿ĚЖ'),(3872,1,'control','â߹'),(3872,1,'local','ʀ\n\r'),(3865,1,'degrees','E'),(3856,1,'names','µ'),(3787,1,'local','|'),(3781,1,'local',''),(3778,1,'names',''),(3871,1,'zurich','ᦼ'),(3778,1,'local','K O'),(3765,1,'local','h'),(3754,1,'local','Ł1\Z%59%'),(3754,1,'names','ˢ'),(3757,1,'local','Ŧ'),(3759,1,'names','C'),(3763,1,'local','Î'),(3879,1,'local','ഀ+E˗ÂǬKo¥ү.\"ōÕᓽݝuࡧɋR18Ǳɛ²©ʹāɸ(L1D6M1(Sy		%	yƣܬĞx6ÀYVägǔ'),(3890,1,'aggressive','ᔥS'),(3856,1,'local','ð'),(3869,1,'dbi','Y¤'),(3871,1,'1020612600','⮆'),(3871,1,'control','ɓϼනᥗ'),(3871,1,'degrees','䷘'),(3871,1,'local','㝵'),(3871,1,'names','Łᑰ໑᪲'),(3871,1,'steps','۱ɶuł↖'),(3871,1,'waves','㉫'),(3751,1,'local','Ȼ'),(3751,1,'steps','Ƕ'),(3736,1,'local',''),(3727,1,'local','©'),(3725,1,'local',''),(3890,2,'control',''),(3847,1,'local',''),(3878,1,'local','Ź\"\n'),(3885,1,'local','f'),(3884,1,'names','ŏዿ'),(3892,1,'local','»'),(3742,1,'1759','['),(3742,1,'implementations','_'),(3872,1,'names','੊)ȵ'),(3881,1,'names','ध'),(3877,1,'steps','˳ß%'),(3877,1,'local','Ʋß%δSS'),(3877,1,'aspects','¾'),(3876,1,'names','Ħ'),(3875,1,'steps','ɻ'),(3875,1,'names','Ǆ'),(3875,1,'local','ʜ'),(3874,1,'user17','¯\" '),(3890,1,'control','í؜ଂʹؾ\rhz?Ō\''),(3890,1,'associations','Ḕ(C'),(3888,1,'steps','աƎŞ'),(3887,1,'names','ग़Û'),(3840,1,'inconsistent','Ɂ'),(3837,1,'oder',''),(3882,1,'local','t'),(3881,1,'hello','Ⱨß¤R9Ŏ¾>\"D!ċ'),(3890,6,'control','ᾔ'),(3891,1,'names','Ƞâ˗<'),(3882,1,'control','ĭ'),(3881,1,'flesh','⽚<'),(3881,1,'control','Ƣ᥶'),(3881,1,'steps','ᩞRدᐶȨ'),(3881,1,'isserviceflapping','ࡷ'),(3828,1,'local','F'),(3824,1,'names','2'),(3823,1,'names','l'),(3722,1,'names','Iæ'),(3722,1,'local','	\n'),(3881,1,'implementations','⢟'),(3846,1,'local','Ð'),(3889,1,'control','Õo'),(3888,1,'names','»͓2Ƚ\n'),(3884,1,'control','ূi'),(3882,1,'names','Ë'),(3885,1,'names','ܚų'),(3874,1,'names','ȋ'),(3740,1,'names','¹'),(3879,1,'control','ƸĜୗ೾ѷբ᭘႐̙཭'),(3886,1,'names','ӥ'),(3887,1,'local','ಪ¡'),(3873,1,'names','ɵ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict9E` ENABLE KEYS */;

--
-- Table structure for table `dict9F`
--

DROP TABLE IF EXISTS `dict9F`;
CREATE TABLE `dict9F` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dict9F`
--


/*!40000 ALTER TABLE `dict9F` DISABLE KEYS */;
LOCK TABLES `dict9F` WRITE;
INSERT INTO `dict9F` VALUES (3888,1,'sanction','ȏ'),(3871,1,'unplotted','䧬'),(3871,1,'prefer','ᆹ⹎'),(3871,1,'illegal','䪂'),(3888,1,'extend','վ'),(3880,1,'basically','䏍'),(3785,6,'nntp','Ĩ'),(3887,1,'work','ྞ'),(3891,1,'pagenet','Ī'),(3879,1,'pagenet','⿬'),(3889,1,'work','ǐ'),(3879,1,'extend','⓬᫦'),(3890,1,'illegal','ᯱ\n\n'),(3891,1,'recovery','Ǹ6G/ȏRG<'),(3822,1,'work','¥'),(3803,1,'work','Ö'),(3803,1,'mta','3'),(3799,1,'work','Ť'),(3798,1,'work','Õ'),(3785,1,'nntp',''),(3889,1,'recovery','Ȍ<'),(3888,1,'writable','ȤĄ'),(3879,1,'recovery','␲ᣪ'),(3879,1,'prefer','೰℺'),(3879,1,'work','匥ŜP'),(3879,1,'basically','㍧ӱྨ'),(3887,1,'update','ྎ'),(3887,1,'recovery','ࣵ'),(3885,1,'recovery','ڴ'),(3886,1,'update','ǿ'),(3880,1,'update','Ṉ֫'),(3880,1,'suppressed','ᔟఓ'),(3840,1,'work','ń'),(3727,1,'work','/'),(3871,1,'extend','ώ'),(3881,1,'deviceid','ᗷ'),(3871,1,'update','ʎ¾к³ɷŴ$\n0,\n\n౑՘+ᝪ\n\n\n\nҒ:W׫ֈڐ'),(3778,1,'work','²'),(3880,1,'recovery','ᄹÜ'),(3881,1,'extend','ᎶĝŪÚᐦΆ'),(3881,1,'update','ℛȏٓM'),(3878,1,'nntp','ʷ'),(3878,1,'42','݄'),(3879,1,'update','ᮼἳެϡ-PĘĻl/ᓋZƎ,'),(3881,1,'customizable','ᥙ'),(3881,1,'work','ᱩۈӭ'),(3871,1,'suppressed','࿶ೲ'),(3877,1,'work','ݷ'),(3876,1,'work','ĵ\r'),(3875,1,'work','Ǔ'),(3874,1,'work','Ț'),(3873,1,'mta','Ѐ	'),(3872,1,'recovery','੘)'),(3872,1,'pagenet','ஒ'),(3871,1,'work','ğĉƍ⫎԰઩VƝ˪ȳٞŰʳ'),(3884,1,'update','ঢ'),(3884,1,'recovery','Ӏ਋'),(3869,1,'y2k','t'),(3871,1,'42','ᘎМ⛑'),(3871,1,'congratulations','㦢'),(3890,1,'update','घ\"	՚෱Ađ0:c');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dict9F` ENABLE KEYS */;

--
-- Table structure for table `dictA0`
--

DROP TABLE IF EXISTS `dictA0`;
CREATE TABLE `dictA0` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictA0`
--


/*!40000 ALTER TABLE `dictA0` DISABLE KEYS */;
LOCK TABLES `dictA0` WRITE;
INSERT INTO `dictA0` VALUES (3885,1,'support','ƨ'),(3872,1,'support','ϤX'),(3872,1,'second','ࡉ'),(3879,1,'support','ĊƑǁ\n#\Z\n÷Ľȣ஁.|9U3Ía\ZǬ\r'),(3880,1,'weeks','䂝'),(3881,1,'button','⦡£ŲӾZ<`'),(3879,1,'blank','૤'),(3880,1,'network','ČĳӜêÊ4ò&ӻĚº͎|AżXZǌ̐ݏMƝQ෕࿬'),(3880,1,'call','ӆ|¥'),(3879,1,'indexing','国'),(3886,1,'button','ǽ'),(3881,1,'powerful','✋'),(3881,1,'network','Ʈ/'),(3881,1,'iterate','੶'),(3881,1,'handle','⳨'),(3881,1,'call','˖|¥޶◀'),(3880,1,'support','Ǻɥ\n#\Z\nɼ.'),(3880,1,'second','Ῥ'),(3880,1,'presented','࿽'),(3878,1,'network','Æ̂'),(3879,1,'handle','㻳'),(3885,1,'button','l'),(3873,1,'network','Y'),(3875,1,'network','çĻ/'),(3880,1,'button','్ᇽᝧš၁'),(3883,1,'20am','ģ'),(3884,1,'button','ღ'),(3884,1,'call','ॽ'),(3884,1,'support','ŉᇂ'),(3885,1,'blank','Ҫ'),(3872,1,'network','b<ƦرѠ'),(3872,1,'handle','ֹ'),(3872,1,'call','ӈ'),(3871,1,'weeks','ⓉBMîê௡'),(3721,1,'handle','ǫ'),(3721,1,'network','{'),(3721,1,'second','0'),(3722,1,'powerful',''),(3723,1,'network',''),(3727,1,'network',''),(3734,1,'blank',''),(3742,1,'network',''),(3752,1,'second',''),(3754,1,'handle','ŅĨ'),(3786,1,'second','/'),(3799,1,'second','¶'),(3804,1,'dialogue',''),(3811,1,'389','R'),(3812,1,'button',''),(3819,1,'network',''),(3820,1,'network','ÿ#'),(3823,1,'dialogue','!'),(3827,1,'handle','Ʒ'),(3827,1,'second','ʄ'),(3840,1,'presented','˗'),(3848,1,'53','U'),(3849,1,'network',''),(3866,1,'call',''),(3868,1,'network','w'),(3868,1,'support','Ñ'),(3869,1,'handle','×'),(3869,1,'network',''),(3870,1,'support','Y'),(3871,1,'black','㧘'),(3871,1,'call','ᚁ'),(3871,1,'corrected','䒀'),(3871,1,'covering','њ'),(3871,1,'handle','ē⺠ᱨÜ'),(3871,1,'increasingly','ᮠ'),(3871,1,'network','E8ᓻᰖ1஻'),(3871,1,'presented','ⶰ׳'),(3871,1,'rrdcreate','ҏɅ'),(3871,1,'second','ࠨϊ*ÔĈ̆قÒۊ௢˲ڶ\\5R\n:ևŘ݆TòHȣۋ'),(3720,1,'support','Ĩ'),(3879,1,'e9','䶓'),(3879,1,'call','Ӄ|¥䵩'),(3886,1,'network','ӂ'),(3881,1,'support','Żô\n#\Z\n⁙ܸÊôܒ'),(3881,1,'succeed','㦎'),(3881,1,'second','ᨪzᚍŕ٭'),(3885,1,'network','é'),(3720,1,'dimensions','Ô'),(3879,1,'20am','⻸'),(3875,1,'second','ȳ'),(3720,1,'button','Ɯ'),(3879,1,'53','搠'),(3879,1,'network','ˠuρ̴઼2গԖ7ɬЋঔǘżӀ©'),(3879,1,'presented','ろᵾᕹ'),(3879,1,'powerful','㾭'),(3879,1,'button','ାీᲗي๽є£4@Ɓ'),(3886,1,'second','Љ'),(3887,1,'blank','۫ن'),(3887,1,'button','େ'),(3887,1,'dimensions','˚஑'),(3887,1,'network','ඤ'),(3887,1,'support','౔'),(3888,1,'call','ȩ]Q'),(3888,1,'second','߳ '),(3889,1,'button','ʹ'),(3890,1,'network','ݩܼ'),(3890,1,'second','˃໅cŖ'),(3891,1,'button','Ů'),(3891,1,'call','ƨÊɉC'),(3891,1,'improper','ǄÊɉC'),(3892,1,'button','Ə'),(3875,2,'network',''),(3875,6,'network','͎');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictA0` ENABLE KEYS */;

--
-- Table structure for table `dictA1`
--

DROP TABLE IF EXISTS `dictA1`;
CREATE TABLE `dictA1` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictA1`
--


/*!40000 ALTER TABLE `dictA1` DISABLE KEYS */;
LOCK TABLES `dictA1` WRITE;
INSERT INTO `dictA1` VALUES (3879,1,'52','䶄'),(3879,1,'easily','Ờĝgʜʊʆӭࠥ'),(3879,1,'htaccess','ේ'),(3880,1,'easily','䕂'),(3890,1,'syntax','܄'),(3804,6,'lotus','¿'),(3874,6,'gwsp2','϶'),(3875,6,'gwsp2','͌'),(3876,6,'gwsp2','Ƴ'),(3881,1,'compiled','Ά'),(3880,1,'outages','ଅⲺࢂ\n\n0\Z'),(3881,1,'peeraddr','⏋'),(3872,1,'hoststate','ܾ'),(3871,1,'syntax','ᶩ'),(3871,1,'job','⍫'),(3881,1,'msgcount','ᵗ'),(3877,1,'compiled','Թ'),(3877,1,'easily','»'),(3878,1,'easily','ĩ'),(3878,1,'gwsp2','Ÿ)\n  \"  \"\"\Z\r\r:I6785*77003352222003\"3'),(3873,1,'syntax','ŊÏ'),(3782,1,'syntax','¦n '),(3722,1,'reply','î'),(3880,1,'aggregating','㦈'),(3805,1,'syntax','26'),(3881,1,'syntax','῞'),(3881,1,'ui','ᰞۀ'),(3884,1,'easily','ᐷ'),(3886,1,'easily','ø'),(3887,1,'easily','Ġआ'),(3888,1,'syntax','֕'),(3890,1,'easily','ᶨ'),(3890,1,'outages','ݎ'),(3881,1,'reply','Ẓ'),(3881,1,'normalized','ٹ'),(3881,1,'easily','ᰶț'),(3804,1,'lotus',''),(3871,1,'compiled','㿞'),(3870,1,'easily',''),(3871,1,'52','䃼'),(3871,1,'aggregating','ᶼ'),(3837,1,'smtphost',''),(3831,1,'dash','G'),(3825,1,'reply','P'),(3824,1,'reply','W');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictA1` ENABLE KEYS */;

--
-- Table structure for table `dictA2`
--

DROP TABLE IF EXISTS `dictA2`;
CREATE TABLE `dictA2` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictA2`
--


/*!40000 ALTER TABLE `dictA2` DISABLE KEYS */;
LOCK TABLES `dictA2` WRITE;
INSERT INTO `dictA2` VALUES (3884,1,'menu','ĘٴΊWַƘ¦'),(3880,1,'comments','Ȃоӂነ٠ǇňBୱ\nD#'),(3879,1,'xhtml1','峹'),(3879,1,'wmargins','嵄!\n\ZÕ\Z'),(3871,1,'parameter','ຍ᯹ᐽ'),(3872,1,'menu','ݳǇŭYõy_'),(3875,1,'execute','ɽ'),(3875,1,'graph2','Ń'),(3875,1,'menu','́'),(3877,1,'execute','ƓŇȃ'),(3878,1,'12','ʴ'),(3879,1,'12','Ʈᷨམ	ᅠާ'),(3871,1,'lineb','䤲'),(3882,1,'selecting','Ñ'),(3882,1,'menu','`X¬'),(3881,1,'troubleview','޸'),(3881,1,'streams','Ḩo'),(3881,1,'selecting','Ș'),(3881,1,'parameter','಻Óண²ٕ቏F؋*6'),(3881,1,'menu','⥖'),(3881,1,'lastchecktime','ࡋ'),(3881,1,'downgrading','ⷠ'),(3881,1,'comments','ƃˍ⠥'),(3881,1,'displayname','ࢢ'),(3889,1,'menu','aȞ¹'),(3888,1,'menu','ڇ'),(3888,1,'comments','Ĉ'),(3887,1,'selecting','༱'),(3887,1,'retained','Ե¶'),(3887,1,'menu','ЌӫŐ~	΋Ð0'),(3886,1,'selecting','Ω	Ŭ'),(3885,1,'retained','˷¶'),(3886,1,'menu','GšFC'),(3834,1,'fping',''),(3824,1,'app2','Ã'),(3823,1,'app2','N'),(3822,1,'12','Ç'),(3813,1,'javaprocs','$'),(3811,1,'binddn',')'),(3798,1,'ocl','B'),(3783,1,'execute','½'),(3782,1,'parameter','¢Cj'),(3765,1,'execute',''),(3734,1,'comments',''),(3720,1,'menu','Ơ'),(3885,1,'menu','MÏڤ'),(3885,1,'comments','Ŏĵլ'),(3890,1,'completely','ɨᯋ'),(3890,1,'comments','ท'),(3880,1,'troubleview','Ăؼ໚u¨\n±C¸Vय0¡|e\' P෵0'),(3883,1,'menu','?4'),(3883,1,'12','ę	'),(3879,1,'menu','ຍgäӆ¨µdK\r	û0੤	\"ШƐǇƺ}ĞÃïÑ_'),(3879,1,'execute','Ωྃᥭࣾ'),(3884,1,'retained','ע3৘3'),(3880,1,'parameter','၄Ǽ⿱'),(3880,1,'retained','ⳊѦ'),(3880,1,'selecting','ඏ`઎iՠ@Hӈǆ2#£o໹ĭɕ˻'),(3880,1,'execute','ᘬݑʭȇƳ÷˶,'),(3889,1,'selecting','˸Ź	'),(3856,1,'execute','r¦'),(3870,1,'completely',''),(3870,1,'streams','ô'),(3871,1,'12','೉Lᚸ̆%Gี\"#Ƴŀ6¡+ࠆ^˔³ɒ'),(3880,1,'treats','␎'),(3879,1,'selecting','Ќ߭Ȋǉ|Ӎ ᅿəĮnଢгᄮ­ÿ'),(3879,1,'parameter','≆ݠ॥Ɓ'),(3884,1,'execute','ॶcN'),(3880,1,'menu','෴/0ઐ⽄'),(3871,1,'execute','ᜀ'),(3879,1,'comments','ʣΚഅ'),(3879,1,'compatibility','垈'),(3880,1,'completely','ི'),(3879,1,'completely','㍽ӳࣈᬒ'),(3890,1,'execute','ƈþމîyઠ'),(3890,1,'improperly','ᑬ;R'),(3890,1,'menu','§þذ	ᒸġś3'),(3890,1,'retained','১\\`aɯH˛Ŗ'),(3890,1,'selecting','᷐'),(3891,1,'menu','S(˓*2'),(3891,1,'selecting','Ŭ'),(3892,1,'comments','p'),(3892,1,'menu','7'),(3834,6,'fping','Ã');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictA2` ENABLE KEYS */;

--
-- Table structure for table `dictA3`
--

DROP TABLE IF EXISTS `dictA3`;
CREATE TABLE `dictA3` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictA3`
--


/*!40000 ALTER TABLE `dictA3` DISABLE KEYS */;
LOCK TABLES `dictA3` WRITE;
INSERT INTO `dictA3` VALUES (3860,1,'time','\''),(3856,1,'time','È'),(3837,1,'time','»'),(3840,1,'behavior','ɂ'),(3840,1,'three','˔'),(3840,1,'time','Ȓ'),(3841,1,'ncp1','i'),(3848,1,'time',''),(3872,1,'restart','̗p2²'),(3879,1,'restart','ර௷Ȥ֨ྲྀᦾᔲ'),(3877,1,'restart','Ǯ\nò'),(3877,1,'three','р'),(3872,1,'three','ß'),(3871,1,'combination','▮'),(3871,1,'moved','㖣'),(3871,1,'parser','☬ڛ'),(3871,1,'rude','㌙'),(3871,1,'three','໓Ƕپë⁈Ъ૲'),(3871,1,'time','@ì`½&´\rW~$ŊJŗâă¢\r&v:æÙv\Z	sÂѾ˭\':Ĩ\n̲B\r˦	(\n\r\r	)\n\'\r\r6J\Z	*ª>\rE˓	̦\r\r\r\n͔ɗ3%ǰ1\n¹ÄlΜ	\r϶*ˬšđǸÍ!ÎÃˢ\"×'),(3785,1,'time','\'Å'),(3784,1,'time','\'Å'),(3780,1,'time','ç'),(3763,1,'time','&B'),(3881,1,'time','̂˰¦Ūࢷؤrғɟˇݟڐ'),(3881,1,'servicestatus','ࢅା̞௢'),(3881,1,'restart','㜍'),(3881,1,'fs','⺥Ȥŀ'),(3880,1,'visualization','ᛶ'),(3881,1,'behavior','ᳫൕŔ̟'),(3881,1,'chapters','⁫'),(3881,1,'combination','ܢ⻤'),(3881,1,'determines','ೃެ2'),(3818,1,'time','\'Å'),(3876,1,'time','Ɣ'),(3825,1,'time','ī'),(3827,1,'time','GƂv'),(3762,1,'time',''),(3761,1,'time','\'Å'),(3759,1,'time','i'),(3757,1,'time',''),(3720,1,'manifest','ĳ'),(3720,1,'measuring','Ò'),(3720,1,'moving','û'),(3883,1,'time',' \r\n-'),(3882,1,'time','Ō'),(3879,1,'time','Ɯ͓๯ॽʩħˈĸ\Zʳπɠ¶-Аф֤¥Ę;̔ϻȁéƮ	ϴᆑ12eå¥'),(3880,1,'time','ӲЏ1\ZࡡƦŚơɯ̑$ƿ£	 \nЎǞлǙ=ʽƓ7ʤƴþ\Zø8RࠪȰJϧ='),(3880,1,'three','ཚ㔡'),(3871,1,'vdef','ø'),(3872,1,'combination','াI'),(3872,1,'lsb','˚Çß'),(3854,1,'time','*æ'),(3880,1,'streamline','܆'),(3880,1,'screens','ݑࢯࢱò3৘'),(3880,1,'item','ቢ'),(3880,1,'chapters','䛯'),(3880,1,'combination','౷⟭1'),(3880,1,'determines','ಖىţ'),(3874,1,'time','κ'),(3804,1,'time',''),(3720,1,'combination','l'),(3879,1,'determines','愹'),(3721,1,'methodology',';'),(3872,1,'time','ॸ'),(3879,1,'pipe','፬'),(3879,1,'item','ᛷ0+\r	௘*'),(3831,1,'time','~'),(3833,1,'time','\'Å'),(3834,1,'bypasses',']'),(3834,1,'time',''),(3835,1,'three','c'),(3835,1,'time','E'),(3824,1,'time','ĥ'),(3814,1,'time','\'Å'),(3811,1,'time','.f'),(3879,1,'three','೛ᩥשގЕ⃤Ɣ'),(3872,1,'ucp',''),(3873,1,'time','Ӂ\r'),(3727,1,'time','q'),(3728,1,'time','$Å'),(3729,1,'time','%_'),(3730,1,'time','q'),(3731,1,'time','\r*\r'),(3732,1,'time','$Æ'),(3737,1,'time','$Å'),(3739,1,'time','$Å'),(3740,1,'time','í'),(3741,1,'time','¯'),(3745,1,'time','­'),(3748,1,'time','\'Å'),(3751,1,'time',';\rQ-Y\r'),(3755,1,'time','d'),(3884,1,'combination','ҡ਋̂'),(3721,1,'time','Ĩ'),(3879,1,'combination','∄'),(3878,1,'gained','µ'),(3877,1,'time','Қɧ'),(3856,1,'f46','%'),(3856,1,'c3','Ś)'),(3871,1,'behavior','኶ʷÖ෱'),(3871,1,'366','┱'),(3871,1,'12am','✡'),(3871,1,'00a000','ᶚᆐ|'),(3866,1,'time','D'),(3864,1,'time','h'),(3884,1,'determines','·਋'),(3884,1,'three','႕'),(3884,1,'time','Ȫ2OőUܖ2OőU'),(3885,1,'combination','ڛ['),(3885,1,'determines','ɈǍ˕'),(3885,1,'time','~Ċͅh9N'),(3886,1,'behavior','ϳ'),(3887,1,'combination','ࣜZݾ'),(3887,1,'determines','ٓ˗'),(3887,1,'time','܎h9Nҵ'),(3888,1,'screens','։'),(3888,1,'three','ࡌ'),(3888,1,'time','ȍ'),(3889,1,'combination','ɀ'),(3889,1,'time','ŗP0'),(3890,1,'determines','ȭӚǖp+pnsȅ</ˠPĊ¹X;Ùgy Ų553ńS+ (%'),(3890,1,'pipe','ᦁ3'),(3890,1,'restart','ь଄ထ'),(3890,1,'time','ࡃ$ӺǚĚ]şip¢÷ćؤÕ'),(3891,1,'combination','ǡÊʌK'),(3891,1,'time','ƒ£Ƅ!î<'),(3883,2,'time',''),(3731,6,'time','¥');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictA3` ENABLE KEYS */;

--
-- Table structure for table `dictA4`
--

DROP TABLE IF EXISTS `dictA4`;
CREATE TABLE `dictA4` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictA4`
--


/*!40000 ALTER TABLE `dictA4` DISABLE KEYS */;
LOCK TABLES `dictA4` WRITE;
INSERT INTO `dictA4` VALUES (3811,1,'port','%('),(3756,1,'port','2'),(3763,1,'port','#'),(3799,1,'grapher','Ů'),(3790,1,'port','.'),(3791,1,'port','DC'),(3793,1,'port','\'#P'),(3795,1,'port','\"'),(3798,1,'grapher','­'),(3833,1,'port','$?'),(3873,1,'prompt','Ȇ'),(3854,1,'port','\'G'),(3803,1,'domains',''),(3748,1,'port','$?'),(3729,1,'port','\"@U'),(3879,1,'daily','兀ᕋ+'),(3871,1,'daily','䊊'),(3759,1,'heavy','s'),(3859,1,'port','$'),(3802,1,'port','3'),(3755,1,'port','\''),(3753,1,'port','0'),(3728,1,'port','!?'),(3727,1,'port','8\"'),(3766,1,'port','32'),(3779,1,'port','\''),(3780,1,'port','.'),(3780,1,'purgeable','Á	\n'),(3870,1,'introductory',':'),(3871,1,'entirely','䟢'),(3848,1,'port','*&'),(3827,1,'port','DT'),(3820,1,'port','®'),(3820,1,'0x','z'),(3819,1,'port','Y'),(3819,1,'0x','Ó'),(3818,1,'port','$?'),(3815,1,'port','(*'),(3814,1,'port','$?'),(3855,1,'port',')'),(3871,1,'flow','䗜'),(3744,1,'port','c+'),(3856,1,'port','7'),(3874,1,'port','̵'),(3861,1,'port','\''),(3858,1,'port','*'),(3742,1,'port','30'),(3728,1,'udp2',''),(3760,1,'port','-'),(3871,1,'experience','㏐'),(3871,1,'dots','ビ'),(3831,1,'port','&.'),(3784,1,'port','$?'),(3785,1,'port','$?'),(3786,1,'port','+'),(3782,1,'port','.'),(3879,1,'beginner','₣'),(3761,1,'port','$?'),(3873,1,'checkcommand','ɦ'),(3872,1,'prompt','ߺ'),(3871,1,'prompt','⵬'),(3871,1,'grapher','«ㄻ'),(3871,1,'forward','▴8'),(3878,1,'experience','Ú'),(3878,1,'entirely','Ď'),(3877,1,'prompt','׻'),(3740,1,'port','<r'),(3739,1,'port','!?'),(3738,1,'port',''),(3737,1,'port','!?'),(3731,1,'port','##'),(3732,1,'port','!@'),(3844,1,'port','5['),(3841,1,'port',')'),(3801,1,'port','-'),(3783,1,'port','('),(3745,1,'port','$*'),(3879,1,'experience','ѱ'),(3879,1,'port','አ䂹'),(3879,1,'prompt','摙Ɨ\'.6'),(3879,1,'s2chapter1a','庹9'),(3880,1,'daily','㭗рɥ'),(3880,1,'disabling','⏬୔'),(3880,1,'experience','Ѵ'),(3880,1,'port','኉'),(3881,1,'entirely','ߢ'),(3881,1,'experience','ʄ'),(3881,1,'flow','ᶅÿ'),(3881,1,'heavy','⡠'),(3881,1,'port','ḝħѽz'),(3884,1,'disabling','ͨ਋'),(3888,1,'reinstated','Ư'),(3890,1,'15s','൲'),(3890,1,'daily','ಿ'),(3890,1,'disabling','ཊװ'),(3728,6,'udp2','ĥ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictA4` ENABLE KEYS */;

--
-- Table structure for table `dictA5`
--

DROP TABLE IF EXISTS `dictA5`;
CREATE TABLE `dictA5` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictA5`
--


/*!40000 ALTER TABLE `dictA5` DISABLE KEYS */;
LOCK TABLES `dictA5` WRITE;
INSERT INTO `dictA5` VALUES (3884,1,'applying','੬'),(3871,1,'abbreviation','☡'),(3822,1,'values','£'),(3879,1,'values','ᨲκ܎ిƝ/ޭПú՚lʞחࢩ'),(3780,1,'values','Ə'),(3721,1,'values','9'),(3879,1,'editor','–㖓'),(3875,1,'values','Ǉ'),(3837,1,'lostc','Ě'),(3827,1,'values','ȟ'),(3877,1,'editor','ֲ'),(3871,1,'applying','☳ᕔ'),(3871,1,'values','ùã¼Ǵ%.\nŧʑ8z͝¥ѕ`AH\rţ\nȋ@	(6#!0ǾėƬJȂočۚȹjʉμSȾÄɹЂƝĥֺ_ǭ9ś4ƶ3\ZɄƟ\nÅ'),(3871,1,'gear','䘫'),(3871,1,'lf','ᩋˋ'),(3871,1,'maximal','ࢸ'),(3871,1,'passes','㓝'),(3871,1,'positive','ᥓ׻⴯'),(3751,1,'values','{+-ĩ'),(3754,1,'values','ǈ'),(3755,1,'values','·'),(3874,1,'values','Ȏhá'),(3878,1,'values','ब'),(3878,1,'49','࢐'),(3726,1,'passes','I'),(3777,1,'autoext',''),(3879,1,'incorporates','㈢w'),(3879,1,'49','լ䟹'),(3871,1,'density','j'),(3742,1,'values','Q'),(3876,1,'values','ĩ'),(3879,1,'applying','⇯᪫'),(3840,1,'tough','˩'),(3846,1,'values','1'),(3851,1,'values','\"'),(3863,1,'values','·2'),(3871,1,'36893488143124135935','䴽'),(3871,1,'49','䃽'),(3840,1,'passes','ƨ'),(3819,1,'values',''),(3886,1,'values','Ǝ	\r'),(3799,1,'values','uÔ\n'),(3881,1,'values','੉ቱϰœÃ'),(3881,1,'49','Ϳ'),(3880,1,'values','ᱨ!'),(3880,1,'positive','ᯕ'),(3880,1,'incorporates','ۧ@ôოⶬ'),(3886,1,'applying','ϥ'),(3885,1,'values','ʧ	\ZT»ƘY'),(3884,1,'values','Ǫ\n਌'),(3751,1,'positive','pă'),(3745,1,'values','ô'),(3740,1,'values','ä'),(3884,1,'incorporates','¹°'),(3880,1,'49','կ'),(3882,1,'abbreviation','Ĉ'),(3887,1,'positive','ɡ m૵ q'),(3872,1,'values','˗Ƭ݆'),(3887,1,'values','ԖT»ƛY'),(3888,1,'applying','κɾ'),(3888,1,'values','όǾƵ%'),(3890,1,'values','Ⱦ੮ǖОɒQ~,1Ī͟)ǭØk'),(3891,1,'values','Ŵ'),(3892,1,'values','Ň'),(3777,6,'autoext','5');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictA5` ENABLE KEYS */;

--
-- Table structure for table `dictA6`
--

DROP TABLE IF EXISTS `dictA6`;
CREATE TABLE `dictA6` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictA6`
--


/*!40000 ALTER TABLE `dictA6` DISABLE KEYS */;
LOCK TABLES `dictA6` WRITE;
INSERT INTO `dictA6` VALUES (3880,1,'allocation','㢯'),(3877,1,'subdirectory','Ш'),(3732,1,'expiry','G'),(3730,1,'router','n'),(3870,1,'sqlite','å\n'),(3859,1,'router','\"'),(3881,1,'stored','᱕BмĲɺоܿण'),(3854,1,'refuse','@{'),(3729,1,'expiry','H'),(3729,1,'refuse',';'),(3799,1,'router','Ŭ'),(3881,1,'repsonse','̧ '),(3798,1,'router',' '),(3854,1,'expiry','M'),(3728,1,'refuse',':Z'),(3881,1,'redirect','㡐Ģ'),(3880,1,'stored','䒋'),(3879,1,'word','⡶'),(3879,1,'subdirectory','ၐ¯'),(3880,1,'router','㩐ˁ['),(3880,1,'ids','ᑵ'),(3880,1,'focus','έ'),(3880,1,'choosing','ᥟԬHߜയ'),(3871,1,'protection','䮲'),(3761,1,'expiry','J'),(3748,1,'refuse','=Z'),(3748,1,'expiry','J'),(3879,1,'stored','ሃ㜇'),(3879,1,'router','㠂'),(3879,1,'redirect','᝻'),(3879,1,'focus','ㆹʇǼτʬĊÍÌMÅ'),(3728,1,'expiry','G'),(3871,1,'july','❏'),(3876,1,'stored','I'),(3761,1,'refuse','=Z'),(3784,1,'expiry','J'),(3784,1,'refuse','=Z'),(3804,1,'domino','o'),(3880,1,'64bit','࠳'),(3737,1,'expiry','G'),(3737,1,'refuse',':Z'),(3739,1,'expiry','G'),(3739,1,'refuse',':Z'),(3871,1,'00c000','Ế'),(3871,1,'router','ơ٨ѭٴỺɄ༞рfש'),(3732,1,'refuse',':['),(3721,1,'stored',''),(3744,1,'peter','B'),(3871,1,'stuff','㎽d৓ػ఑'),(3871,1,'tidal','㉪'),(3852,1,'established',''),(3841,1,'router','p'),(3871,1,'neginf','ᥐة'),(3871,1,'maxima','⊕⊈ǖ'),(3871,1,'focus','や'),(3871,1,'978301500','䣯'),(3871,1,'stored','ʰŖ²ŶǶގᣚųۢ᷻'),(3875,1,'stored','w'),(3874,1,'stored','m'),(3797,1,'stuff','d'),(3785,1,'refuse','=Z'),(3785,1,'expiry','J'),(3873,1,'subdirectory','ʴ'),(3871,1,'word','ᇲ'),(3871,1,'64bit','࠸'),(3752,1,'router','ÿ'),(3879,1,'established','ࡦ¤Ʒ'),(3799,1,'stored','Ľ'),(3837,1,'ids','²'),(3836,1,'secs','('),(3833,1,'refuse','=Z'),(3833,1,'expiry','J'),(3827,1,'4xx','ý'),(3823,1,'word','v'),(3818,1,'refuse','=Z'),(3818,1,'expiry','J'),(3814,1,'refuse','=Z'),(3814,1,'expiry','J'),(3809,1,'wload5','\"'),(3884,1,'compulsive','Ζ਋'),(3886,1,'word','Ţ'),(3887,1,'subdirectory','ಡ¡'),(3887,1,'word','Ɗ'),(3890,1,'compulsive','ᜀ)'),(3890,1,'protection','ʤ'),(3890,1,'stored','࠻ěost'),(3890,1,'subdirectory','ޯ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictA6` ENABLE KEYS */;

--
-- Table structure for table `dictA7`
--

DROP TABLE IF EXISTS `dictA7`;
CREATE TABLE `dictA7` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictA7`
--


/*!40000 ALTER TABLE `dictA7` DISABLE KEYS */;
LOCK TABLES `dictA7` WRITE;
INSERT INTO `dictA7` VALUES (3881,1,'consumer','⛞͌'),(3865,1,'status','0'),(3879,1,'portals','ᔳ'),(3879,1,'status','̥ЏĦ\"͏їIÈ	\nOB\\¤Ƭࠪ¦৯Ƥŵ\'ߍ,ӱ/ࢃ¡ǁggUñ«ŀ6źh,\nغpîڗóʇ,T&\r$z«8$̋'),(3878,1,'status','ϠjǸ3352222003U/'),(3878,1,'ssh','З! !  '),(3867,1,'status',''),(3869,1,'postgres','a'),(3871,1,'datasources','⁂<,'),(3871,1,'definable','Ǡ'),(3871,1,'status','ƅ'),(3872,1,'shutdown','ȅ0'),(3872,1,'status','ɺ\n			\nw$$3\r \r'),(3874,1,'ssh','5H:>>400\''),(3857,1,'status','H'),(3856,1,'ssh','>.'),(3879,1,'105252112','Ẫ'),(3725,1,'status','C'),(3875,1,'status','Q'),(3880,1,'status','Î>ų÷ͩ ěĿˁƫCļ8<Óĭ	â\nǪ>\r4B7%)\ns:\"!¡>.\',/1?QĚ=HW0m#cG)8*\Z9H:Ă+Z\r]\nI\nef(ˁ݊Ö:ύÎŞ౐'),(3879,1,'uninstalling','ྐg\rL'),(3853,1,'status',' '),(3877,1,'shutdown','̯'),(3877,1,'ssh','\n!×kJ6&q*6g,!MU.Y'),(3879,1,'mountpoints','ṹ'),(3879,1,'graphcgi','䧻'),(3881,1,'datasources','☎'),(3880,1,'definable','ሾ'),(3880,1,'breach','ᝯ'),(3879,1,'ssh','㓌'),(3880,1,'ago','ầ'),(3728,1,'status','ñ'),(3729,1,'status',''),(3731,1,'status','a\r\r'),(3732,1,'status','ò'),(3735,1,'ssh',' '),(3736,1,'status','@'),(3737,1,'status','ñ'),(3738,1,'openssh','k'),(3738,1,'ssh','		'),(3739,1,'status','ñ'),(3740,1,'status','~'),(3742,1,'status',''),(3743,1,'mountpoints',']'),(3744,1,'status','ú'),(3745,1,'status','²'),(3746,1,'status',''),(3747,1,'status',''),(3748,1,'status','ô'),(3749,1,'status','\Z'),(3754,1,'status','	&\"	ŗ1\Z%59%'),(3755,1,'rtsp','^6'),(3755,1,'status','i'),(3757,1,'status','¼~'),(3761,1,'status','ô'),(3763,1,'status','p'),(3766,1,'status',''),(3767,1,'status',''),(3773,1,'status',''),(3774,1,'status',''),(3775,1,'status',''),(3776,1,'status','u\n\n\Z\n'),(3777,1,'status',''),(3778,1,'status','&'),(3780,1,'status','Ġ('),(3782,1,'params','4'),(3782,1,'status','\\'),(3784,1,'status','ô'),(3785,1,'status','ô'),(3787,1,'status','d'),(3788,1,'status','\r/'),(3789,1,'status',' >'),(3798,1,'status','4'),(3799,1,'status',')'),(3801,1,'status','%'),(3808,1,'ssh','5\"'),(3809,1,'status','<'),(3811,1,'status',''),(3812,1,'status',''),(3814,1,'status','ô'),(3818,1,'status','ô'),(3819,1,'status',''),(3820,1,'status',''),(3827,1,'status','ë\rÙ'),(3829,1,'status',''),(3830,1,'status','\Z'),(3831,1,'status',''),(3833,1,'status','ô'),(3843,1,'ssh',') '),(3845,1,'ssh',''),(3846,1,'mountpoints','Ę'),(3846,1,'status','Z'),(3847,1,'ssh','%'),(3848,1,'status',''),(3727,1,'status','v'),(3862,1,'status',''),(3880,1,'ssh','ᮂ#$⤽\r!'),(3863,1,'status','ÇC'),(3879,1,'definable','≄ቆ'),(3854,1,'status','Ę'),(3726,1,'status',','),(3861,1,'status','\Z'),(3880,1,'portals','ਇ'),(3866,1,'status','%\nh'),(3856,1,'status','Í'),(3881,1,'definable','Რ'),(3881,1,'portals','⠏'),(3881,1,'status','úүƆnčӶ\r	\nĂ	\n՗!F¾1\Z]৖ͩ,$'),(3884,1,'ssh','ٕ'),(3884,1,'status','ˮˡ\rĸ֏ˡ\rRʡ'),(3885,1,'ssh','ޮ¡'),(3885,1,'status','ˤÎ~'),(3886,1,'status','Ģ'),(3887,1,'status','Ŋă\r\rȪÑ~ԮM'),(3888,1,'status','ƈ'),(3890,1,'shutdown','ъ'),(3890,1,'status','ɚfÚĦ£(nħ(ŀ		&	!֓أ॥'),(3874,2,'ssh',''),(3877,2,'ssh',''),(3738,6,'ssh',''),(3754,6,'status','̛'),(3767,6,'status','3'),(3788,6,'status','ç'),(3856,6,'ssh','ƚ'),(3874,6,'ssh','Ϸ'),(3877,6,'ssh','ޒ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictA7` ENABLE KEYS */;

--
-- Table structure for table `dictA8`
--

DROP TABLE IF EXISTS `dictA8`;
CREATE TABLE `dictA8` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictA8`
--


/*!40000 ALTER TABLE `dictA8` DISABLE KEYS */;
LOCK TABLES `dictA8` WRITE;
INSERT INTO `dictA8` VALUES (3881,1,'minutes','ь'),(3878,1,'36','ؕ'),(3780,1,'minutes','é'),(3874,1,'80','ã Ʋ3'),(3872,1,'success','ˡC'),(3871,1,'standalone','ͮ'),(3871,1,'white','ᒟ┭'),(3881,1,'gwservices','⍄\Z̒'),(3879,1,'90886592','ẜ'),(3879,1,'80','ኡ䂹'),(3878,1,'nsca','ƛ'),(3871,1,'selects','ᢋ'),(3871,1,'problematic','⴯'),(3871,1,'month','ᄰኼµƿ'),(3885,1,'minutes','օ©'),(3720,1,'downloading','e'),(3721,1,'standalone','¦ǂ'),(3730,1,'success',''),(3733,1,'datatbase','?'),(3740,1,'ma4read',''),(3751,1,'80','ɍ\"'),(3751,1,'minutes','u/$: -f°'),(3754,1,'minutes','eĳ'),(3762,1,'cass','Á'),(3881,1,'frameworks','❑'),(3881,1,'changebutton','〟¢\r;°\Z¡P'),(3880,1,'success','؅'),(3880,1,'minutes','ؼ'),(3880,1,'customizing','̿'),(3879,1,'white','⧽Ɯ'),(3879,1,'upload','⤇:('),(3884,1,'minutes','ʶ\\Ĩࢇ\\Ĩ'),(3881,1,'success','Е'),(3881,1,'powered','⚐ࣈ'),(3879,1,'gwservices','ᯝ⻳ᔦ'),(3879,1,'success','؂'),(3876,1,'80','Í'),(3782,1,'minutes','\n'),(3789,1,'minutes','4'),(3798,1,'minutes','1?'),(3799,1,'minutes','@ {'),(3822,1,'80','G'),(3827,1,'80','º'),(3827,1,'minutes','Ŋ'),(3837,1,'minutes','½'),(3841,1,'sourcemac','#'),(3865,1,'minutes','.2'),(3869,1,'sectors','$'),(3871,1,'80','䷽'),(3871,1,'convert','౐'),(3871,1,'late','亨'),(3871,1,'loopback0','䄊'),(3871,1,'minutes','֗֡؆ᅋɁôf/,༠Ė#ٖ3ћ		|\n³>\n6ù'),(3878,1,'minutes','ŋ'),(3879,1,'minutes','ع'),(3875,1,'ifinerrors','È'),(3881,1,'pluggable','↹ۯ'),(3879,1,'customizing','ϕ'),(3885,1,'upload',''),(3887,1,'minutes','߆©'),(3889,1,'minutes','ū'),(3890,1,'convert','ᴱ'),(3890,1,'minutes','ˉ௷͌Ś'),(3890,1,'month','೒');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictA8` ENABLE KEYS */;

--
-- Table structure for table `dictA9`
--

DROP TABLE IF EXISTS `dictA9`;
CREATE TABLE `dictA9` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictA9`
--


/*!40000 ALTER TABLE `dictA9` DISABLE KEYS */;
LOCK TABLES `dictA9` WRITE;
INSERT INTO `dictA9` VALUES (3814,1,'print','Q'),(3815,1,'print','a'),(3818,1,'print','Q'),(3821,1,'script','\Z'),(3827,1,'data','ę'),(3782,1,'data',''),(3879,1,'data','υඁ!?ćഢًǍÂƜȓᓟ%oī%6<à­?^͈ҋ9g\n\rl\no\r	\r\nñşN\n;\n૟ѳ!5Ĩ'),(3871,1,'protected','㺺'),(3871,1,'print','ñࣹ֙Ҫ׾}H\nᏏ\nয়Ѻ'),(3881,1,'notified','㊗'),(3881,1,'loadmodule','㖲'),(3856,1,'uptime','Ŝ'),(3813,1,'script','\''),(3781,1,'ntpdate',''),(3877,1,'script','̪'),(3887,1,'configuration','ЌǛ̘ŘΔG0'),(3879,1,'cio','ܜ'),(3721,1,'cooperation','ǐ'),(3827,1,'print',''),(3877,1,'configuration','ǉ	Öӌ'),(3851,1,'data','G/'),(3836,1,'print','S'),(3830,1,'print','<'),(3872,1,'notified','ঀÆ)Ŏ`w'),(3872,1,'data','ߍ'),(3840,1,'print','U'),(3840,1,'data','Ť'),(3877,1,'print','ˌ'),(3885,1,'notified','ܢ'),(3885,1,'configuration','LÏ̎Ͷ '),(3887,1,'logos','ಠ{'),(3887,1,'data','ӿ'),(3853,1,'print','B'),(3851,1,'print','}'),(3853,1,'compaq',''),(3871,1,'coming','㝛'),(3871,1,'configuration','ޘैឪ੮'),(3871,1,'data','B-ĕ»Hb^)\r\Z@	$H&\r\r)\r+!&ä9\'-L@#\'$ũ)y!\Z\r@8,´˪̋((\nǿ\réÙ(T3kƔMĠĭ¦ %q(ֈ\n6( !!\n\r\n# />­5)ÏҠJ»EŶɖ¥!ţË2ĦcDõ¤Ðn¿ǂ	h&	\ryTmĆ-ĪT\n=_áǦ*̲»\r'),(3871,1,'absurd','䴳'),(3871,1,'1800','䣷\r©'),(3869,1,'data','Ú'),(3868,1,'configuration','%'),(3861,1,'print',' '),(3863,1,'print',';'),(3866,1,'print','p'),(3887,1,'notified','ॢë׈'),(3873,1,'loaded','@'),(3873,1,'data','K'),(3872,1,'script','ĪO)ˊ'),(3879,1,'lang','峿'),(3879,1,'holidays','Ӷ'),(3879,1,'contactalias','⾜'),(3811,1,'print',';'),(3810,1,'script',''),(3878,1,'script','Щ~'),(3881,1,'scratch','㑮'),(3874,1,'configuration','!Fĥ\Z+	ŉ'),(3873,1,'script','p,,(ť'),(3887,1,'notifying','ࡒ'),(3881,1,'script','ીƾ᛫'),(3881,1,'print','ਸ਼$ᦎ'),(3878,1,'configuration','мҙ\Z'),(3872,1,'contactalias','ୂ'),(3872,1,'configuration','èħʈ,İŒĻ	ƿť	'),(3879,1,'loaded','唐'),(3877,1,'csw','ʮ'),(3875,1,'uptime','Ĕ'),(3809,1,'uptime','Z'),(3809,1,'print','+'),(3725,1,'print','5'),(3721,1,'data','d\Z	'),(3875,1,'script','½Ķ'),(3830,1,'data','S'),(3829,1,'print','^'),(3879,1,'configuration','Į4>àСњԨm5!ƧKūþėȴ¨Ž5Ƣ*S\r\r\ne7tÊÝ\n\nG)t%&R\"8!ŻÂMRB$MÖ}TiiÊö \'C\Z0\n\r<Ȱ1	2ƃ!	ÀǮ%,\nłŠü	Ä	*C4\n&\r	Y?:.@$\ZER%(DǊ«E\rB4ěw\r%rzź<#ʫƅ ǈïĂ\r0Ý̼\rcwT(ˣÕʡ$5!\n'),(3856,1,'print',';'),(3856,1,'configuration','Â'),(3854,1,'print','U'),(3886,1,'configuration','FŨ\nMJ¥t '),(3780,1,'uptime',''),(3780,1,'print',':'),(3871,1,'micro','፨'),(3871,1,'loaded','ҩ'),(3875,1,'configuration','!Pë\Z	]¹+'),(3874,1,'script',''),(3881,1,'isprocessperformancedata','࡛'),(3885,1,'data','ƅļ'),(3850,1,'print','6'),(3849,1,'print','b	'),(3879,1,'logos','㢚'),(3879,1,'nmap','⪧'),(3879,1,'notified','ⷄီ'),(3879,1,'scratch','₟'),(3881,1,'loaded','⭵'),(3881,1,'protected','㥷'),(3871,1,'temperatures','߮Ԥ∏r'),(3871,1,'script','Ɀʴʺட'),(3876,1,'script','^'),(3876,1,'configuration','!\"{\Z	c'),(3885,1,'notifying','ؑ'),(3848,1,'print','>'),(3846,1,'print','K'),(3839,1,'script',''),(3838,1,'print','2'),(3837,1,'script',''),(3837,1,'popts','ď'),(3835,1,'print','8'),(3834,1,'print','G'),(3833,1,'print','Q'),(3831,1,'print','3'),(3881,1,'holidays','̉'),(3880,1,'print','䠚'),(3880,1,'uptime','ࠆ'),(3880,1,'visually','ݖ'),(3881,1,'buttons','⯏'),(3881,1,'configuration','߸͜\Z᧜­ŧù̊ਉ][	ôdÏ(['),(3881,1,'data','Þĥʜ#8¡\ry1oX\"\n\nȹ(ǒTÿȌ\nˇࣦġ^(5o9\nB\n\r\r0	*%I\n+B\n$C˯ïɯ˗Tsയ'),(3880,1,'notified','ᆚŖ൦'),(3880,1,'holidays','ӹ'),(3880,1,'data','̯Ã̱Oƒ=\rŊ(>αR&98&\rЙȿ¨	3Ą£ёb8/ېĆയH+	;~a\Z#&D¬£±ȝÀ͢ɇ?ǂ'),(3880,1,'configuration','ɾ׃Ǔ	\r\r\ZȈȟɍò̸ࢉڧŐ0༂ʨ£>'),(3880,1,'capture','ࣾ⺣'),(3879,1,'webapp','ዎ'),(3879,1,'structure','࿼࿊ǔ⽱(ӪXƤQjǘ'),(3879,1,'script','䢈Ɛóîࠛßdਪ'),(3884,1,'notified','ؽ਋Čʪ'),(3884,1,'data','֭Ϳ*؞'),(3884,1,'loaded','ۂ'),(3881,1,'webapp','஗'),(3882,1,'configuration','0L¬'),(3883,1,'configuration','>4'),(3884,1,'configuration','ĄٴÍ NW$2\r9®WַƘ¦'),(3881,1,'uroot','⍊'),(3881,1,'structure','þ\n\nᡉର\n\n(m46܉+ťŷ\n+'),(3807,1,'print','1'),(3805,1,'configuration','/7'),(3799,1,'inspect','~'),(3799,1,'print','G'),(3802,1,'tnt','+'),(3799,1,'data','Y$%('),(3798,1,'print','w'),(3797,1,'print','*'),(3795,1,'print','+'),(3793,1,'print','8'),(3791,1,'print','L'),(3789,1,'print','W'),(3788,1,'print',''),(3787,1,'print',']'),(3785,1,'print','Q'),(3784,1,'print','Q'),(3782,1,'uptime','Ì'),(3782,1,'print','u'),(3727,1,'print','H'),(3727,1,'temperatures','j'),(3728,1,'print','N'),(3729,1,'print','P'),(3731,1,'print','4'),(3732,1,'print','O'),(3733,1,'script',''),(3736,1,'print','1'),(3737,1,'print','N'),(3738,1,'print','/'),(3739,1,'print','N'),(3740,1,'print','Ā'),(3741,1,'print','5]'),(3742,1,'print','\\'),(3743,1,'print','1'),(3744,1,'data','èg'),(3744,1,'loaded','Ü'),(3744,1,'print','|'),(3745,1,'print','<'),(3746,1,'print','#'),(3748,1,'print','Q'),(3751,1,'data','18BW§\r@\n	'),(3751,1,'print','ē'),(3752,1,'data','8'),(3752,1,'script','®'),(3753,1,'print','x'),(3754,1,'print','Ƶ'),(3755,1,'print','2'),(3756,1,'configuration','l5\n'),(3756,1,'print','<'),(3759,1,'loaded','t'),(3760,1,'script',''),(3761,1,'print','Q'),(3762,1,'print','5'),(3763,1,'print','-'),(3766,1,'print','?'),(3766,1,'uptime','8'),(3778,1,'data',''),(3778,1,'print','¢'),(3780,1,'loaded','ĵA'),(3726,1,'data','L'),(3884,1,'notifying','Л਋'),(3888,1,'configuration','ŬH\r+e\"oLˀ'),(3888,1,'script','˚'),(3889,1,'configuration','YȭȌ'),(3889,1,'notified','̈́U'),(3890,1,'configuration','+\r\'D1Mʺ/B7(nƤ\rݲŷIʜĈƼ¦Lȉ> ƹm>B	ì9,'),(3890,1,'data','œޘ	&.ҰUk\nތĢ\n$\n$\n\n\n\n\n\n	!	!\n)\n))'),(3891,1,'configuration','R/˓1:'),(3891,1,'contactalias','Ú'),(3891,1,'notified','ƚ *¸ƑCă<'),(3892,1,'configuration','6'),(3892,1,'data','Ę'),(3882,2,'configuration',''),(3752,6,'data','Ě'),(3853,6,'compaq','\\');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictA9` ENABLE KEYS */;

--
-- Table structure for table `dictAA`
--

DROP TABLE IF EXISTS `dictAA`;
CREATE TABLE `dictAA` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictAA`
--


/*!40000 ALTER TABLE `dictAA` DISABLE KEYS */;
LOCK TABLES `dictAA` WRITE;
INSERT INTO `dictAA` VALUES (3743,1,'verbose','0'),(3739,1,'verbose','ĉ'),(3740,1,'processname','('),(3740,1,'verbose','ÿ'),(3741,1,'verbose','4'),(3727,1,'verbose',''),(3728,1,'verbose','ĉ'),(3729,1,'verbose','¤'),(3732,1,'verbose','Ċ'),(3849,1,'verbose','a'),(3848,1,'verbose',''),(3846,1,'verbose','Ħ'),(3845,1,'verbose','2='),(3737,1,'verbose','ĉ'),(3738,1,'verbose','o'),(3736,1,'verbose',''),(3733,1,'gw','H'),(3815,1,'verbose','`'),(3814,1,'verbose','Č'),(3744,1,'equal','Ǥ'),(3744,1,'verbose','ź'),(3721,1,'highly','ǹ'),(3735,1,'verbose','/>'),(3734,1,'lines',''),(3872,1,'verbose','ʕÄ)'),(3872,1,'administration','౛'),(3871,1,'replace','ḵᆿ'),(3871,1,'magenta','㧤'),(3871,1,'equal','ᨨଚ'),(3871,1,'lines','ĢႪE	ॣ⺳'),(3877,1,'1301',''),(3877,1,'gw','זH'),(3875,1,'user7','´*Ì'),(3873,1,'corporation',''),(3835,1,'verbose','7'),(3834,1,'verbose',''),(3833,1,'verbose','Č'),(3827,1,'verbose','ǩ'),(3825,1,'verbose','ĵ'),(3878,1,'1301','x'),(3854,1,'verbose','İ'),(3856,1,'lines','u'),(3840,1,'complicated','Æ'),(3813,1,'verbose','1'),(3805,1,'lines',''),(3808,1,'highly','9'),(3808,1,'verbose','1?'),(3811,1,'verbose','´'),(3803,1,'verbose','7Q'),(3818,1,'verbose','Č'),(3871,1,'efficient','Ʈ̔'),(3860,1,'equal','+'),(3871,1,'bits','⬧\'ۗȌ`s¨สࠓ¿'),(3843,1,'verbose','_'),(3822,1,'verbose','x'),(3823,1,'verbose','½'),(3824,1,'verbose','ŉ'),(3745,1,'verbose','Ê'),(3747,1,'verbose','M'),(3748,1,'verbose','Č'),(3751,1,'verbose','Đ'),(3753,1,'verbose','s'),(3754,1,'downtime','ōĸ'),(3755,1,'verbose',''),(3757,1,'verbose',''),(3761,1,'verbose','Č'),(3763,1,'verbose',''),(3780,1,'lrus','ê»'),(3781,1,'verbose',';'),(3784,1,'verbose','Č'),(3785,1,'verbose','Č'),(3788,1,'verbose',''),(3802,1,'lines','\"'),(3879,1,'1301',''),(3879,1,'administration','¾ف<ĉYv$ĳ-´ƺìĵ)HDθĩ.ȅВjࡆㅦӄ_ì'),(3879,1,'downtime','ፕ'),(3879,1,'equal','ᯑ'),(3879,1,'lines','ഋ'),(3879,1,'replace','㌉߽\rόᅇᒞ8>'),(3879,1,'selectively','⤖㴕6'),(3879,1,'verbose','ḏ'),(3880,1,'1301',''),(3880,1,'acknowledgments','ᅲ࠻'),(3880,1,'administration','ࡊŊ\ṛ'),(3880,1,'downtime','ଈ৘+\rӧۼ#ʯϧ\n\r1&\n\r1&д\r1&Ĝ\n\r0&'),(3880,1,'efficient','ჼ'),(3880,1,'revisited','㔩'),(3881,1,'1301',''),(3881,1,'administration','⑺'),(3881,1,'closely','۹'),(3881,1,'gwrkws','♈'),(3881,1,'highly','㐑'),(3881,1,'replace','➜Qࠟ\n!)	Ľͮ£'),(3881,1,'sophisticated','⥍ే'),(3884,1,'replace','૏'),(3884,1,'rescheduled','˒਋'),(3885,1,'replace','{ळ'),(3886,1,'replace','ȃ'),(3888,1,'administration','Ŕ'),(3888,1,'replace','ס'),(3890,1,'downtime','෷'),(3890,1,'equal','኱'),(3890,1,'highly','श'),(3890,1,'replace','Ḥ'),(3890,1,'rescheduled','ᓏإ'),(3733,6,'gw','');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictAA` ENABLE KEYS */;

--
-- Table structure for table `dictAB`
--

DROP TABLE IF EXISTS `dictAB`;
CREATE TABLE `dictAB` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictAB`
--


/*!40000 ALTER TABLE `dictAB` DISABLE KEYS */;
LOCK TABLES `dictAB` WRITE;
INSERT INTO `dictAB` VALUES (3890,1,'degrade','ᑤ;R'),(3878,7,'profiles','ै'),(3879,1,'package','̅Ŕ୪\r2\n\n\n	HȾǒǃ\nяŉʂ!⅂»Z঒Ұ\nӲ:ÐÓ¿ȉ,ó'),(3879,1,'operate','䆻'),(3858,1,'usage','#'),(3857,1,'usage',' '),(3726,1,'argn','='),(3726,1,'usage','8'),(3727,1,'package','Ĩ'),(3727,1,'smart','ę'),(3727,1,'usage','0'),(3728,1,'6','A+'),(3728,1,'usage',''),(3729,1,'6','B'),(3729,1,'usage',''),(3730,1,'usage','('),(3731,1,'usage',''),(3732,1,'6','A,'),(3732,1,'usage',''),(3733,1,'usage','V'),(3734,1,'usage','\"'),(3735,1,'usage',''),(3736,1,'comparisons',''),(3736,1,'usage',''),(3737,1,'6','A+'),(3737,1,'usage',''),(3738,1,'6','M'),(3738,1,'usage',' '),(3739,1,'6','A+'),(3739,1,'usage',''),(3740,1,'usage',''),(3741,1,'usage',''),(3742,1,'usage','%'),(3743,1,'usage',''),(3744,1,'package','\'Ū'),(3744,1,'query','/¤Ž'),(3744,1,'usage','D'),(3745,1,'6','8\"'),(3745,1,'usage',''),(3746,1,'usage',''),(3747,1,'smart',''),(3747,1,'usage',''),(3748,1,'6','D+'),(3748,1,'usage',''),(3749,1,'package',''),(3749,1,'usage',' '),(3750,1,'usage','%'),(3751,1,'usage','\\'),(3752,1,'usage','O'),(3753,1,'6','e'),(3753,1,'usage','&'),(3754,1,'usage','@'),(3755,1,'usage',''),(3756,1,'usage','\"'),(3757,1,'usage','<'),(3758,1,'usage',''),(3759,1,'usage','#\n\n'),(3760,1,'usage','&'),(3761,1,'6','D+'),(3761,1,'usage',''),(3762,1,'6','1'),(3762,1,'usage',''),(3763,1,'6','K'),(3763,1,'usage',''),(3764,1,'usage','$'),(3765,1,'usage',' .'),(3766,1,'usage','-'),(3767,1,'usage',''),(3768,1,'usage','\Z'),(3769,1,'usage','\Z'),(3770,1,'usage','\Z'),(3771,1,'usage','\Z'),(3772,1,'usage','\Z'),(3773,1,'usage','\Z'),(3774,1,'usage','\Z'),(3775,1,'usage',''),(3776,1,'usage','!'),(3777,1,'usage',''),(3778,1,'usage','\Z'),(3779,1,'usage',' '),(3780,1,'usage','('),(3781,1,'jitter',' B'),(3781,1,'usage','-'),(3782,1,'usage','&ň'),(3783,1,'usage','\"'),(3784,1,'6','D+'),(3784,1,'usage',''),(3785,1,'6','D+'),(3785,1,'usage',''),(3786,1,'usage',''),(3787,1,'usage','R'),(3788,1,'usage','>'),(3789,1,'usage','G'),(3790,1,'query',','),(3790,1,'usage','#\''),(3791,1,'query',''),(3791,1,'usage','4'),(3792,1,'usage','%'),(3793,1,'usage',''),(3794,1,'usage',''),(3795,1,'usage',''),(3796,1,'usage','\Z'),(3797,1,'usage',','),(3798,1,'usage',']'),(3799,1,'usage','+Ì'),(3800,1,'usage',''),(3801,1,'usage','&'),(3802,1,'usage',','),(3803,1,'usage','&]'),(3804,1,'usage','*'),(3805,1,'usage',''),(3806,1,'usage',' '),(3807,1,'query','\''),(3807,1,'usage',''),(3808,1,'usage',''),(3809,1,'usage',''),(3810,1,'usage',''),(3811,1,'6','7\"'),(3811,1,'usage',''),(3812,1,'package','$'),(3812,1,'usage','%'),(3813,1,'usage','\"'),(3814,1,'6','D+'),(3814,1,'usage',''),(3815,1,'usage',''),(3816,1,'usage',''),(3817,1,'usage','$'),(3818,1,'6','D+'),(3818,1,'usage',''),(3819,1,'query','-'),(3819,1,'usage','$ó'),(3820,1,'query','.'),(3820,1,'usage','%ä'),(3821,1,'usage','\"'),(3822,1,'usage','*R'),(3725,1,'usage','+'),(3871,1,'6','፟Ꮦฯūfгδ\r`ø4ÆũШyö\r\r'),(3875,1,'6','Č'),(3885,2,'profiles',''),(3884,1,'usage','Ⴕ'),(3877,1,'package','̙'),(3872,1,'6','˻'),(3874,1,'package','%Ğ'),(3880,1,'profiles','ߪˋݲ\r㊭		'),(3876,7,'profiles','ư'),(3866,1,'usage','Q'),(3863,1,'package','Û*'),(3863,1,'usage','.'),(3864,1,'usage','$'),(3865,1,'usage','&'),(3866,1,'apache','%'),(3856,1,'usage','!'),(3856,1,'6','Y'),(3878,1,'certified','ç'),(3874,1,'profiles','aĢ'),(3871,1,'major','იiϨ'),(3871,1,'package','㿟ဩ'),(3871,1,'pushing','ᜏƴ'),(3871,1,'usage','Υԛ⦝í'),(3878,2,'profiles',''),(3890,1,'package','ᵰ'),(3875,1,'profiles','œƄ'),(3878,1,'apache','И\r'),(3890,1,'substituted','ܮ'),(3862,1,'6','²'),(3861,1,'usage','\"'),(3855,1,'usage',''),(3890,1,'profiles','Ḛ\Z)'),(3879,1,'query','䫻\Zᔘ\Z'),(3879,1,'usage','㼚'),(3881,1,'apache','❠ƦǡࣉsY9!aÀÏ)Z'),(3721,1,'query','Ô'),(3720,1,'major',''),(3879,1,'smart','慁O'),(3869,1,'apache','ó'),(3867,1,'usage','$'),(3860,1,'usage','3'),(3859,1,'usage','%'),(3881,1,'major','⑩ࣨmً'),(3885,1,'profiles','<,/)§Ӡ	?åOQ	-A'),(3875,1,'package','%ö'),(3863,1,'debian','+ï'),(3862,1,'usage','?'),(3722,1,'usage','û_'),(3722,1,'query','5ÿ'),(3878,6,'profiles','ॉ'),(3791,6,'query','»'),(3747,6,'smart','e'),(3892,1,'usage','ŵ'),(3879,1,'listend','䟳'),(3879,1,'apache','೻௹ eE:)ýڒ'),(3885,6,'profiles','৞'),(3874,1,'6','Çǐ'),(3873,1,'usage','Ǽ'),(3872,1,'usage','Ғȭ'),(3872,1,'substituted','ۥĞ'),(3872,1,'package','Ƽ'),(3877,7,'profiles','ޑ'),(3871,1,'kilometers','㐫ĵ$WѪ1×řࡸ'),(3888,1,'substituted','ࣂ'),(3888,1,'profiles','ر'),(3887,1,'profiles','ƪȖK૆	'),(3888,1,'6','{࠹'),(3888,1,'hostgroup2','׻'),(3886,1,'profiles','!ƈ0\rǸ'),(3881,1,'package','ȊʜǚSñxងӈ91	g¹ɼ	Xǭ	ŷ-#		\n)\'!@ņђ0_Ȁó\n/'),(3879,1,'6','Ť°˓ᩡ᪬ࠧᴡφ'),(3824,1,'usage','²'),(3825,1,'usage','Ú`'),(3826,1,'usage',''),(3827,1,'10h','ŋ'),(3827,1,'6','jW'),(3827,1,'usage','9'),(3828,1,'usage','+'),(3829,1,'package','3'),(3829,1,'usage','T'),(3830,1,'query','3'),(3830,1,'usage','#'),(3831,1,'6','¼'),(3831,1,'package',''),(3831,1,'query','SB'),(3831,1,'usage',''),(3832,1,'usage',''),(3833,1,'6','D+'),(3833,1,'usage',''),(3834,1,'usage','7'),(3835,1,'usage',''),(3836,1,'usage','#'),(3838,1,'usage','('),(3839,1,'usage',')'),(3840,1,'query','%CøR'),(3840,1,'usage','A'),(3841,1,'usage',''),(3842,1,'usage','&'),(3843,1,'usage',''),(3844,1,'usage','!'),(3845,1,'usage','\Z'),(3846,1,'usage','2'),(3847,1,'usage',' '),(3848,1,'query',','),(3848,1,'usage','!'),(3849,1,'usage',' '),(3850,1,'usage','&'),(3851,1,'usage','#'),(3852,1,'usage',''),(3853,1,'usage','\''),(3854,1,'6','G3'),(3854,1,'usage','!'),(3724,1,'usage',''),(3723,1,'usage',''),(3870,1,'bundled','è'),(3869,1,'package','þ'),(3884,1,'profiles','ĆfڶY9$2)w@ģ'),(3881,1,'query','ӡʼĽĬú4ÁR	F&==;֤1\ZÂ-!îÓÎ -! #Hྫ'),(3880,1,'package','ɡƘ֪ì/⨍Ⴑ'),(3880,1,'major','ڸ'),(3880,1,'6','Ӫㅣੋ'),(3881,1,'6','ÜȞʆ៰'),(3880,1,'usage','ᇨ⋳ϩ൷'),(3876,1,'profiles','µ'),(3876,1,'package','%k'),(3875,7,'profiles','͉'),(3874,7,'profiles','ϳ'),(3824,1,'query','t	 '),(3823,1,'usage','B'),(3878,1,'profiles','}$	4D˦6785*770˧'),(3866,6,'apache','À'),(3879,1,'profiles','Ŕᷨſ\\ė\rlhmRb+<!\"6%	+%XRŲб\n\r	2ۓ8\Z&\Za&;$̗Ëj+icŊҳéŻࡀ'),(3890,1,'smart','ᆤJNZǥ'),(3881,1,'isnotificationsenabled','ࡓ '),(3881,1,'clicks','⦫ֶ̓');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictAB` ENABLE KEYS */;

--
-- Table structure for table `dictAC`
--

DROP TABLE IF EXISTS `dictAC`;
CREATE TABLE `dictAC` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictAC`
--


/*!40000 ALTER TABLE `dictAC` DISABLE KEYS */;
LOCK TABLES `dictAC` WRITE;
INSERT INTO `dictAC` VALUES (3743,7,'monitor',''),(3763,7,'monitor','ķ'),(3880,2,'monitor',''),(3881,1,'timeunknown','࡮'),(3880,1,'removed','⳽ѥ'),(3871,1,'represents','㣡4'),(3731,1,'monitor',''),(3732,1,'monitor',''),(3733,1,'monitor',''),(3734,1,'monitor',''),(3735,1,'monitor',''),(3736,1,'monitor',''),(3737,1,'monitor',''),(3738,1,'monitor',''),(3739,1,'monitor',''),(3740,1,'monitor',''),(3741,1,'monitor','\n'),(3742,1,'monitor',''),(3743,1,'monitor','\n'),(3744,1,'monitor',''),(3745,1,'monitor',''),(3746,1,'monitor',''),(3747,1,'monitor',''),(3748,1,'monitor',''),(3749,1,'monitor',''),(3750,1,'monitor',''),(3751,1,'monitor',''),(3752,1,'monitor',''),(3753,1,'monitor',''),(3754,1,'monitor',''),(3755,1,'monitor',''),(3756,1,'monitor',''),(3757,1,'monitor',''),(3758,1,'monitor',''),(3759,1,'monitor','F¡'),(3760,1,'monitor',''),(3761,1,'monitor',''),(3762,1,'monitor',''),(3763,1,'monitor',''),(3764,1,'monitor',''),(3765,1,'monitor','À'),(3766,1,'monitor',''),(3722,1,'monitor',''),(3772,7,'monitor','$'),(3872,1,'monitor','Ĕ'),(3785,7,'monitor','Ĝ'),(3764,7,'monitor','<'),(3797,7,'monitor','j'),(3724,7,'monitor','e'),(3725,1,'monitor',''),(3726,1,'monitor',''),(3880,1,'staff','Ⴊ'),(3880,1,'toolkit','ˮ䌟'),(3881,1,'monitor','\n^_\"\Z5\r.\n	&ǵ@Åԟˇ΄&\rൾɤ1XĜƜ֝'),(3881,1,'native','⥨'),(3801,7,'monitor','4'),(3752,7,'monitor','č'),(3885,1,'monitor','ݸ'),(3885,1,'removed','঴'),(3886,1,'monitor','є'),(3887,1,'staff','ౕ'),(3888,1,'monitor','Ƨ'),(3890,1,'monitor','๔Oງ'),(3890,1,'removed','ᑜ9R'),(3874,2,'monitor',''),(3875,2,'monitor',''),(3876,2,'monitor',''),(3871,1,'tomorrow','␑'),(3742,7,'monitor',''),(3882,1,'removed','Ąw'),(3856,1,'monitor',''),(3776,7,'monitor','ĥ'),(3725,7,'monitor','_'),(3726,7,'monitor',''),(3740,7,'monitor','Ɔ'),(3754,7,'monitor','̍'),(3755,7,'monitor','½'),(3756,7,'monitor','ó'),(3808,7,'monitor','x'),(3803,7,'monitor','Ü'),(3774,7,'monitor',')'),(3748,7,'monitor','Ĝ'),(3768,7,'monitor','\''),(3729,1,'monitor',''),(3791,7,'monitor','®'),(3780,7,'monitor','ƭ'),(3729,7,'monitor','Ò'),(3837,1,'monitor',''),(3838,1,'monitor',''),(3839,1,'monitor',''),(3840,1,'monitor','öǒ'),(3841,1,'monitor',''),(3842,1,'monitor',''),(3843,1,'monitor',''),(3844,1,'monitor',''),(3845,1,'monitor',''),(3846,1,'inode',''),(3846,1,'monitor',''),(3847,1,'monitor',''),(3848,1,'monitor',''),(3849,1,'monitor',''),(3850,1,'monitor',''),(3851,1,'monitor',''),(3852,1,'monitor',''),(3853,1,'monitor',''),(3730,1,'monitor',''),(3806,7,'monitor','p'),(3810,7,'monitor',','),(3795,7,'monitor',''),(3796,7,'monitor','\''),(3745,7,'monitor','ú'),(3744,7,'monitor','ɚ'),(3723,7,'monitor','B'),(3871,1,'monitor','ㆠౡ̒1'),(3769,7,'monitor','$'),(3884,1,'monitor','ņպ4ƮDÖ'),(3809,7,'monitor','b'),(3777,7,'monitor','('),(3854,1,'monitor',''),(3730,7,'monitor',''),(3881,1,'oriented','✩'),(3781,7,'monitor',''),(3871,1,'native','’ᗻ'),(3793,7,'monitor','¦'),(3787,7,'monitor','¨'),(3879,1,'1yr','䙚ƑǦ'),(3879,1,'monitor','\nl°N)!\n9*7P@I\rǳ#- \"e.¸If­!ěÐ>ÑIM\n?A%!9;(D@1HD9W8]E@7kKdcʼĂrŃ^j\"³Ģ<ü#ȔϸʥńƜķĢ٭÷E߲Ƽǻ\rď\\	\\Ẹ̀ʷT¬čϱևeL°j̐Só͗\r|ʪ	\'ş?'),(3807,7,'monitor','?'),(3802,7,'monitor',':'),(3773,7,'monitor','('),(3747,7,'monitor','Y'),(3767,7,'monitor','&'),(3762,7,'monitor','Ý'),(3728,1,'monitor',''),(3790,7,'monitor','Q'),(3779,7,'monitor','2'),(3728,7,'monitor','ę'),(3836,1,'monitor',''),(3760,7,'monitor','A'),(3784,7,'monitor','Ĝ'),(3881,2,'monitor',''),(3741,6,'monitor','Æ'),(3743,6,'monitor',''),(3824,6,'metaframe','Ţ'),(3720,7,'monitor','ƹ'),(3721,7,'monitor','ʚ'),(3879,2,'monitor',''),(3881,1,'represents','܍	!ሢ'),(3798,7,'monitor','î'),(3871,1,'removed','ộई'),(3871,1,'iftable','䃄		,'),(3805,7,'monitor','Ë'),(3794,7,'monitor','%'),(3879,1,'removed','ఌⶋ\"ީᨦ'),(3879,1,'represents','侐'),(3879,1,'restoring','ɂ⺘㉜\"xXƃ'),(3879,1,'staff','ܒ'),(3857,1,'monitor',''),(3858,1,'monitor',''),(3859,1,'monitor',''),(3860,1,'monitor',''),(3861,1,'monitor',''),(3862,1,'monitor',''),(3863,1,'monitor',''),(3864,1,'monitor',''),(3865,1,'monitor',''),(3866,1,'monitor',''),(3867,1,'monitor',''),(3868,1,'monitor','G'),(3869,1,'oriented','|'),(3870,1,'brand','¯'),(3870,1,'oriented','Î'),(3871,1,'accumulated','רѕ'),(3871,1,'ceil','៶'),(3871,1,'generations','৷'),(3724,1,'monitor',''),(3723,1,'monitor',''),(3722,7,'monitor','š'),(3880,1,'represents','᭜µ'),(3757,7,'monitor','ƥ'),(3758,7,'monitor','@'),(3759,7,'monitor','ü'),(3881,1,'removed','⑵'),(3782,7,'monitor','Ɣ'),(3800,7,'monitor','C'),(3751,7,'monitor','ʩ'),(3884,1,'staff','ጌ'),(3804,7,'monitor','³'),(3783,7,'monitor','Õ'),(3765,7,'monitor','Ü'),(3770,7,'monitor','$'),(3878,2,'monitor',''),(3811,7,'monitor','Ä'),(3741,7,'monitor','¸'),(3881,1,'toolkit','ґ!īA	⁣º'),(3855,1,'monitor',''),(3799,7,'monitor','ƍ'),(3775,7,'monitor','\''),(3788,7,'monitor','Ú'),(3750,7,'monitor','4'),(3749,7,'monitor',')'),(3739,7,'monitor','ę'),(3738,7,'monitor',''),(3737,7,'monitor','ę'),(3736,7,'monitor','»'),(3735,7,'monitor','u'),(3734,7,'monitor','û'),(3733,7,'monitor',''),(3732,7,'monitor','Ě'),(3731,7,'monitor',''),(3753,7,'monitor',''),(3792,7,'monitor','b'),(3786,7,'monitor','='),(3873,1,'monitor','úŋ'),(3874,1,'monitor','`û\'#Ơ'),(3875,1,'monitor','iÏ\Z#'),(3876,1,'monitor','¨#£'),(3877,1,'monitor','¼'),(3878,1,'monitor','ÌʓD6785*770\Ẓ'),(3746,7,'monitor','1'),(3766,7,'monitor','ï'),(3761,7,'monitor','Ĝ'),(3871,1,'year','Яೕ࢚ਬĴơ)⌧'),(3727,1,'monitor',''),(3880,1,'monitor','\n4^@\r 5\n6&7P@8\'$ǳ#\r&jK,>6/{)\Z\ZT	9	\r&\"(\r<	\":\rZ@M/\\4ò(@.\Z1ȉƜ=İcQȪ̧ƣ!.،;૓ѡq¤Ǯ9±˗̍Հ1?	!	+	\r	:Y^Ā\"'),(3789,7,'monitor',''),(3778,7,'monitor','ö'),(3727,7,'monitor','Ŀ'),(3767,1,'monitor',''),(3768,1,'monitor',''),(3769,1,'monitor',''),(3770,1,'monitor',''),(3771,1,'monitor',''),(3772,1,'monitor',''),(3773,1,'monitor',''),(3774,1,'monitor',''),(3775,1,'monitor',''),(3776,1,'monitor',''),(3777,1,'monitor',''),(3778,1,'monitor',''),(3779,1,'monitor',''),(3780,1,'monitor',''),(3780,1,'tsands','ļ'),(3781,1,'monitor',''),(3782,1,'monitor',''),(3783,1,'monitor',''),(3784,1,'monitor',''),(3785,1,'monitor',''),(3786,1,'monitor',''),(3787,1,'monitor',''),(3788,1,'monitor',''),(3789,1,'monitor',''),(3790,1,'monitor',''),(3791,1,'monitor',''),(3792,1,'monitor',''),(3793,1,'monitor',''),(3794,1,'monitor',''),(3795,1,'monitor',''),(3796,1,'monitor',''),(3797,1,'monitor',''),(3798,1,'monitor','Æ'),(3798,1,'staff','·'),(3799,1,'monitor','Y¨B'),(3799,1,'staff','Ÿ'),(3800,1,'monitor',''),(3801,1,'monitor',''),(3802,1,'monitor',''),(3803,1,'monitor',''),(3804,1,'monitor',''),(3805,1,'monitor',''),(3806,1,'monitor',''),(3807,1,'monitor',''),(3808,1,'monitor',''),(3809,1,'monitor',''),(3810,1,'monitor',''),(3811,1,'monitor',''),(3812,1,'monitor',''),(3813,1,'monitor',''),(3814,1,'monitor',''),(3815,1,'monitor',''),(3816,1,'inode','2\r'),(3816,1,'monitor',''),(3817,1,'monitor',''),(3818,1,'monitor',''),(3819,1,'cleartext','Ë'),(3819,1,'monitor',''),(3820,1,'cleartext','r'),(3820,1,'monitor',''),(3821,1,'monitor',''),(3822,1,'monitor',''),(3823,1,'metaframe','\ZF '),(3823,1,'monitor',''),(3824,1,'metaframe','¡'),(3824,1,'monitor',''),(3825,1,'monitor',''),(3826,1,'monitor',''),(3827,1,'monitor',''),(3828,1,'monitor',''),(3829,1,'monitor',''),(3830,1,'monitor',''),(3831,1,'monitor',''),(3832,1,'monitor',''),(3833,1,'monitor',''),(3834,1,'monitor',''),(3835,1,'monitor',''),(3720,1,'toolkit','Ɣ'),(3771,7,'monitor','$'),(3879,1,'toolkit','΄'),(3872,1,'areacode','Ԛ'),(3812,7,'monitor','1'),(3813,7,'monitor',''),(3814,7,'monitor','Ĝ'),(3815,7,'monitor','j'),(3816,7,'monitor','P'),(3817,7,'monitor','-'),(3818,7,'monitor','Ĝ'),(3819,7,'monitor','Ğ'),(3820,7,'monitor','ŋ'),(3821,7,'monitor','9'),(3822,7,'monitor','ý'),(3823,7,'monitor','Ê'),(3824,7,'monitor','ŕ'),(3825,7,'monitor','Ł'),(3826,7,'monitor','$'),(3827,7,'monitor','˓'),(3828,7,'monitor',''),(3829,7,'monitor','u'),(3830,7,'monitor',''),(3831,7,'monitor','Ï'),(3832,7,'monitor','L'),(3833,7,'monitor','Ĝ'),(3834,7,'monitor','·'),(3835,7,'monitor',''),(3836,7,'monitor','a'),(3837,7,'monitor','ģ'),(3838,7,'monitor','@'),(3839,7,'monitor','2'),(3840,7,'monitor','̖'),(3841,7,'monitor','«'),(3842,7,'monitor',';'),(3843,7,'monitor','g'),(3844,7,'monitor','Ó'),(3845,7,'monitor','w'),(3846,7,'monitor','Ķ'),(3847,7,'monitor','R'),(3848,7,'monitor','­'),(3849,7,'monitor','y'),(3850,7,'monitor','G'),(3851,7,'monitor',''),(3852,7,'monitor','('),(3853,7,'monitor','P'),(3854,7,'monitor','ŀ'),(3855,7,'monitor','7'),(3856,7,'monitor','ƍ'),(3857,7,'monitor','\\'),(3858,7,'monitor','1'),(3859,7,'monitor','?'),(3860,7,'monitor','D'),(3861,7,'monitor','/'),(3862,7,'monitor','ė'),(3863,7,'monitor','Ĺ'),(3864,7,'monitor',''),(3865,7,'monitor',''),(3866,7,'monitor','´'),(3867,7,'monitor','-'),(3868,7,'monitor','ē'),(3869,7,'monitor','Ė'),(3870,7,'monitor','Ġ'),(3871,7,'monitor','僓'),(3872,7,'monitor','೬'),(3873,7,'monitor','ՠ'),(3874,7,'monitor','Ϫ'),(3875,7,'monitor','̀'),(3876,7,'monitor','Ƨ'),(3877,7,'monitor','ވ'),(3878,7,'monitor','ि'),(3879,7,'monitor','札'),(3880,7,'monitor','䠻'),(3881,7,'monitor','㦻'),(3882,7,'monitor','ƫ'),(3883,7,'monitor','ņ'),(3884,7,'monitor','ᑟ'),(3885,7,'monitor','৖'),(3886,7,'monitor','Գ'),(3887,7,'monitor','ᄍ'),(3888,7,'monitor','ई'),(3889,7,'monitor','Ӹ'),(3890,7,'monitor','ᾌ'),(3891,7,'monitor','ه'),(3892,7,'monitor','ƽ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictAC` ENABLE KEYS */;

--
-- Table structure for table `dictAD`
--

DROP TABLE IF EXISTS `dictAD`;
CREATE TABLE `dictAD` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictAD`
--


/*!40000 ALTER TABLE `dictAD` DISABLE KEYS */;
LOCK TABLES `dictAD` WRITE;
INSERT INTO `dictAD` VALUES (3751,1,'simply','Ǒ'),(3751,1,'existing','ș'),(3750,1,'param3','.'),(3744,1,'regular','ĺ'),(3744,1,'crypt','Ä'),(3871,1,'calculated','੗Ñາ¼B'),(3871,1,'boring','ゝ'),(3879,1,'regular','䓢4ǅ'),(3873,1,'resources','ʛ'),(3877,1,'authentication','Ï '),(3871,1,'00','ᆈࠎ੄ǵ\nՈ௶¢y1Gmྲ'),(3870,1,'xmlsoft',''),(3838,1,'simply',''),(3840,1,'resources','̋'),(3841,1,'00',';%'),(3870,1,'resources','M'),(3763,1,'postgresql','*'),(3721,1,'computers',''),(3871,1,'vrule','ྑ٦ճ'),(3756,1,'regular','Ø'),(3879,1,'resources','฿௧\\ʗ'),(3871,1,'alter','̎ນᘁÕ~\Z'),(3828,1,'resources',' '),(3827,1,'regular','.'),(3756,1,'authentication','`'),(3721,1,'existing','Ǳ'),(3871,1,'dd','␢'),(3871,1,'regular','㵞'),(3754,1,'regular','Ĩ'),(3720,1,'radiogroup','Ť'),(3871,1,'simply','еޤྃ'),(3873,1,'authentication','ɋ\n'),(3763,1,'simply','¬'),(3793,1,'authentication','o'),(3791,1,'authentication','¥'),(3827,1,'authentication','Ƈ'),(3820,1,'authentication','p'),(3819,1,'authentication','É'),(3812,1,'joystick','\Z'),(3806,1,'regular','9\r'),(3805,1,'regular','4\r'),(3798,1,'calculated','Ü'),(3795,1,'authentication','b'),(3871,1,'basics','ΚⳔ+᪵Ҏ'),(3872,1,'existing','ކ'),(3760,1,'authentication','\"'),(3756,1,'resources','é'),(3879,1,'authentication','ĈۛĲࡿܸ˪s'),(3879,1,'existing','êεŷŤ:Ӛ߻WΓĥŶ]ӗӧ\rĩԜitԧߤÜॊʯॴڛѝó'),(3879,1,'00','Ө⧽ṥ޿<჉'),(3877,1,'existing','ש'),(3880,1,'dd','⢨өƯ'),(3880,1,'authentication','ടۼ?'),(3880,1,'00','ӫ'),(3879,1,'simply','⁩ծ¹aࠝ'),(3744,1,'authentication','¾'),(3741,1,'regular','i'),(3740,1,'regular','Ù'),(3740,1,'authentication','a'),(3733,1,'regular',')L'),(3721,1,'suit','ř'),(3880,1,'existing','ҢŷΙᵂI๶ƅ'),(3880,1,'resources','͸䉋Q '),(3881,1,'00','˻'),(3881,1,'authentication','⛦cekaǅࣔ>İL3EôR©<'),(3881,1,'basics','㖊'),(3881,1,'calculated','ᾋ'),(3881,1,'computers','㏳'),(3881,1,'dd','᝸'),(3881,1,'existing','ʲŷ¸ƟᕅÛ	֧ę̜ŕХ'),(3881,1,'lasthardstate','࡬'),(3881,1,'resources','⭒'),(3881,1,'robust','⟷'),(3881,1,'simply','㡏'),(3883,1,'00','Ĕ'),(3883,1,'existing','k'),(3883,1,'simply','ĸ'),(3884,1,'existing','ૐ'),(3884,1,'regular','ɽਆ'),(3885,1,'existing','xध'),(3886,1,'existing','yţ'),(3887,1,'existing',' ϨӴřΉÀ'),(3888,1,'existing','Ճ'),(3888,1,'simply','Ʊ¡'),(3889,1,'hostescalation','Ҳ1'),(3890,1,'authentication','̍^\n'),(3890,1,'dd','ᯗ	'),(3890,1,'existing','ᴲ'),(3890,1,'regular','໙Іܝ2'),(3890,1,'simply','ᴴH'),(3890,1,'sound','ݶ$'),(3891,1,'existing','{̃Æ'),(3892,1,'resources','¾');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictAD` ENABLE KEYS */;

--
-- Table structure for table `dictAE`
--

DROP TABLE IF EXISTS `dictAE`;
CREATE TABLE `dictAE` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictAE`
--


/*!40000 ALTER TABLE `dictAE` DISABLE KEYS */;
LOCK TABLES `dictAE` WRITE;
INSERT INTO `dictAE` VALUES (3855,1,'threshold','-'),(3864,1,'threshold','E'),(3871,1,'2700','䤓'),(3834,1,'threshold','e#'),(3840,1,'hacked','Ů'),(3846,1,'threshold','0'),(3828,1,'threshold','7'),(3834,1,'56','w'),(3827,1,'threshold','ʅ'),(3880,1,'deep','቗'),(3879,1,'pacific','Ӯ'),(3879,1,'threshold','ᷫ❴'),(3890,1,'hard','ᘧ'),(3887,1,'threshold','շ²	'),(3885,1,'threshold','̹±	'),(3879,1,'inside','囅Ƴƣò$Ô'),(3871,1,'multiplying','ౖ⼮'),(3822,1,'threshold',';K2'),(3879,1,'hard','䎬'),(3879,1,'deep','≝'),(3878,1,'deep','­'),(3876,1,'threshold','Ŏ'),(3873,1,'automating','*'),(3871,1,'partially','䌤'),(3880,1,'pacific','ӱ'),(3884,1,'threshold','ό\nŤ࠱\nŤ'),(3881,1,'pacific','́'),(3880,1,'retrieval','᪈'),(3881,1,'adapter','ᶽu¦h\nV-n7-%7\r'),(3881,1,'inside','ߐՖ8ᵥo̇ۃ(Ƣƥ3Y'),(3880,1,'hard','ᅢǒ☜'),(3871,1,'bri0','䃧		'),(3871,1,'hard','㓴'),(3871,1,'inf','᥏Ԍ+\"\nq'),(3871,1,'inside','᳹'),(3734,1,'threshold','Ö'),(3751,1,'threshold','q\n=H-'),(3757,1,'inside','ł'),(3757,1,'threshold','('),(3762,1,'threshold','R$'),(3766,1,'threshold','	'),(3780,1,'dsdb','ð'),(3780,1,'threshold','Ł'),(3782,1,'threshold','UK-'),(3786,1,'threshold','1'),(3791,1,'threshold',''),(3798,1,'threshold',''),(3799,1,'threshold','.');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictAE` ENABLE KEYS */;

--
-- Table structure for table `dictAF`
--

DROP TABLE IF EXISTS `dictAF`;
CREATE TABLE `dictAF` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictAF`
--


/*!40000 ALTER TABLE `dictAF` DISABLE KEYS */;
LOCK TABLES `dictAF` WRITE;
INSERT INTO `dictAF` VALUES (3881,1,'queries','௲ɏ೜'),(3881,1,'innovative','Ǐ'),(3881,1,'incorporate','㟊'),(3881,1,'included','ԼᳮБأु'),(3881,1,'fromdate','৩	ഷ3\r	S@\r	R3\r	'),(3881,1,'entry','ԝඟ5ˍĉ־]ء'),(3863,1,'included','â'),(3841,1,'entry','e'),(3840,1,'session','Ț'),(3840,1,'queries','ǹ'),(3798,1,'entry',','),(3795,1,'entry','q'),(3793,1,'entry','~'),(3764,1,'60000','6'),(3730,1,'r1','c'),(3744,1,'included','ƌ'),(3762,1,'linking','¹'),(3763,1,'queries','¦'),(3881,1,'approaches','ᶗɖ'),(3879,1,'entry','ै㬼@+ȑ\rñֽȟ͍૆|'),(3720,1,'filteringtable','Ś'),(3878,1,'27','ў'),(3877,1,'ls','Ɲ'),(3877,1,'included','ɬ'),(3880,1,'innovative','ރ'),(3879,1,'session','喑ว+¬?'),(3871,1,'internet','ㇷÜŷ૤°'),(3884,1,'included','ࠥఱ'),(3881,1,'samples','ޱ˵'),(3871,1,'included','㐗֭'),(3868,1,'guidelines','È'),(3869,1,'internet','Ä'),(3870,1,'interoperability','¶'),(3871,1,'27','◜#'),(3871,1,'entry','Д✲'),(3880,1,'samples','㡔ŢʻЩ'),(3880,1,'queries','ࢃ'),(3878,1,'included','š'),(3878,1,'queries','غ3352222003U/'),(3879,1,'queries','囯'),(3721,1,'internet','ŅÙ'),(3720,1,'included','Í'),(3880,1,'included','ᓆɩ'),(3871,1,'ls','ė'),(3871,1,'keeping','Ի㻠'),(3879,1,'samples','䬷ᔲ'),(3871,1,'office','ƒ'),(3871,1,'peaks','㉠'),(3871,1,'samples','ਧ!z/Ⱛɡࡡ		\Z	>\nƧþÕŬ'),(3877,1,'entry','Ǘþ'),(3873,1,'guidelines','Ŧ'),(3872,1,'unimplemented','ˮ'),(3872,1,'internet','~'),(3872,1,'included','ಮ'),(3871,1,'systemtime','ژ'),(3871,1,'summaries','ᬑ'),(3871,1,'sanity','ज़'),(3885,1,'session','ɮ'),(3885,1,'included','ࢁ'),(3879,1,'included','࿆ƬޮҊࡐkکጨ̋b¯'),(3880,1,'incorporate','ེ'),(3880,1,'expandable','㊎¹\Z1'),(3879,1,'linking','垜'),(3879,1,'internet','ᩮ'),(3887,1,'included','Н؞'),(3880,1,'summaries','᩟'),(3880,1,'examining','㢳'),(3880,1,'entry','߽'),(3888,1,'entry','͈'),(3888,1,'included','Ϋģ'),(3889,1,'entry','˿'),(3890,1,'entry','̅'),(3890,1,'spikes','ጊ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictAF` ENABLE KEYS */;

--
-- Table structure for table `dictB0`
--

DROP TABLE IF EXISTS `dictB0`;
CREATE TABLE `dictB0` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictB0`
--


/*!40000 ALTER TABLE `dictB0` DISABLE KEYS */;
LOCK TABLES `dictB0` WRITE;
INSERT INTO `dictB0` VALUES (3880,1,'customized','㑪'),(3872,1,'format','ۃ¿ƀǠŲ'),(3872,1,'extract','ǋ'),(3872,1,'complete','ڼ'),(3871,1,'subtracted','บ'),(3759,1,'memory','Ê'),(3782,1,'memory','÷'),(3881,1,'complete','ʦĽᜋ'),(3880,1,'trends','नȰ⥓˷èશ'),(3880,1,'memory','㓠,'),(3880,1,'scheduled','ᔕ\r୪pC݃Ɍ=ǸYƠƆ'),(3871,1,'001','ኈ$'),(3840,1,'arcane','ȼ'),(3822,1,'host1','/'),(3822,1,'format','2'),(3809,1,'format','T'),(3804,1,'complete',''),(3800,1,'memory','\Z'),(3791,1,'format','j'),(3720,1,'complete','ńW'),(3720,1,'layout','Ƃ'),(3730,1,'complete','z'),(3734,1,'format','x'),(3736,1,'memory','¨'),(3741,1,'memory','/d'),(3743,1,'format','~'),(3751,1,'complete','7'),(3751,1,'trends','$5Oÿ'),(3752,1,'pairs','?'),(3754,1,'scheduled','ŗ'),(3756,1,'format','¨'),(3757,1,'memory',''),(3759,1,'cumulated','['),(3871,1,'paranoia','乃'),(3871,1,'memory','ᕵ'),(3871,1,'layout','ᐎ'),(3871,1,'handy','䒬'),(3871,1,'format','ˤஆĜĲ[Lʟ#U\Z֚²ҧHRय़ă˶ ¤΍'),(3871,1,'extract','྿႔঵਀'),(3879,1,'format','䚤ȆօĎɑդH࣑'),(3881,1,'extract','㔍'),(3879,1,'customized','囏'),(3874,1,'memory','Þƻ¼'),(3877,1,'77','ؘ'),(3878,1,'complete','ࣙ'),(3879,1,'complete','ғĽઑኟ࠿ඒق཈'),(3871,1,'computerroom','䟈'),(3871,1,'complete','᜴⡒'),(3871,1,'12363','㟥9ၢ'),(3880,1,'complete','ɬȪĽƳrం␍'),(3880,1,'aware','ⲚѦ'),(3879,1,'scheduled','兛Й'),(3879,1,'memory','≲'),(3879,1,'lwp','凔į9\r	 Ž9W'),(3879,1,'impacts','נ'),(3880,1,'layout','᨞ȅᮽ'),(3880,1,'impacts','ף'),(3880,1,'host1','∔'),(3880,1,'format','ᒋ	ᐒөƯ'),(3881,1,'format','ࠫᛚႹ'),(3881,1,'getservicesforhost','ेĠ\r઻'),(3881,1,'impacts','ϳ'),(3881,1,'memory','⢙'),(3881,1,'pairs','Ỻ'),(3882,1,'complete','İ'),(3884,1,'scheduled','˴਋'),(3885,1,'memory','ݪ'),(3885,1,'scheduled','ϞŜ\Z'),(3887,1,'complete','ђ'),(3887,1,'format','൥1'),(3887,1,'pngtogd2','අ'),(3887,1,'scheduled','؜ş\Z'),(3890,1,'complete','Ἥ'),(3890,1,'format','ٹᕓǈ'),(3890,1,'layout','ټ	-	'),(3890,1,'scheduled','଩ɦаWÃCT'),(3890,1,'standby','ѣ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictB0` ENABLE KEYS */;

--
-- Table structure for table `dictB1`
--

DROP TABLE IF EXISTS `dictB1`;
CREATE TABLE `dictB1` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictB1`
--


/*!40000 ALTER TABLE `dictB1` DISABLE KEYS */;
LOCK TABLES `dictB1` WRITE;
INSERT INTO `dictB1` VALUES (3784,1,'truncate','ĕ'),(3784,1,'jail','¶'),(3761,1,'truncate','ĕ'),(3763,1,'truncate',''),(3727,1,'truncate',''),(3728,1,'jail','³'),(3728,1,'truncate','Ē'),(3729,1,'truncate','­'),(3732,1,'jail','´'),(3732,1,'truncate','ē'),(3737,1,'jail','³'),(3737,1,'truncate','Ē'),(3738,1,'truncate','x'),(3739,1,'jail','³'),(3739,1,'truncate','Ē'),(3744,1,'authpasswd','v'),(3744,1,'truncate','ƃ'),(3745,1,'truncate','Ó'),(3748,1,'jail','¶'),(3748,1,'truncate','ĕ'),(3754,1,'recovered','ɠ'),(3754,1,'tags','ɵs'),(3755,1,'truncate',''),(3757,1,'array','S¾'),(3761,1,'jail','¶'),(3865,1,'battery','D'),(3854,1,'truncate','Ĺ'),(3854,1,'jail','Ú'),(3848,1,'truncate','¦'),(3814,1,'truncate','ĕ'),(3814,1,'jail','¶'),(3871,1,'65','௉'),(3811,1,'truncate','½'),(3727,1,'battery','½'),(3726,1,'tags',''),(3833,1,'jail','¶'),(3833,1,'truncate','ĕ'),(3879,1,'deployed','៘'),(3871,1,'tags','ⲓ	#Ǐ'),(3871,1,'ideally','ᚘ'),(3827,1,'truncate','ǲ'),(3785,1,'truncate','ĕ'),(3827,1,'tags','ƚ'),(3818,1,'truncate','ĕ'),(3872,1,'sendpage','\n*-5!	S;>¼n?*!8u3KҏǼ'),(3872,1,'identified','ֿ'),(3871,1,'worse','㾚'),(3872,1,'daemons','ϙ'),(3805,1,'array',''),(3786,1,'netapp',''),(3871,1,'testhvt','⿑'),(3870,1,'redone',''),(3870,1,'objects','¢'),(3818,1,'jail','¶'),(3846,1,'truncate','į'),(3834,1,'truncate',''),(3785,1,'jail','¶'),(3877,1,'502','ú'),(3879,1,'identified','剻'),(3879,1,'insert','䨦Ń'),(3879,1,'objects','ᇦ࿙ᕳ'),(3879,1,'perfdata','䎕o\Z\n'),(3879,1,'sendpage','έ\n'),(3879,1,'tags','娚'),(3880,1,'focuses','ۗ'),(3880,1,'objects','◌'),(3880,1,'recovered','⊿'),(3880,1,'sendpage','̗\n䍑	'),(3881,1,'array','ඣÑ@?A>ADBCXCP@AA·ø'),(3881,1,'deployed','▪'),(3881,1,'hgname','ऻ૬'),(3881,1,'identified','᤭'),(3881,1,'insert','ᾉυ'),(3881,1,'objects','֡œ	1Òᔇ঴ʙg¿ð*Ęɞ6ĝÓǱ'),(3885,1,'objects','y'),(3890,1,'objects','᜔۬'),(3890,1,'perfdata','ឮLpGƏ>ƕ'),(3890,1,'recovered','ᕩ'),(3786,6,'netapp','I'),(3872,6,'sendpage','೵'),(3872,7,'sendpage','೴');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictB1` ENABLE KEYS */;

--
-- Table structure for table `dictB2`
--

DROP TABLE IF EXISTS `dictB2`;
CREATE TABLE `dictB2` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictB2`
--


/*!40000 ALTER TABLE `dictB2` DISABLE KEYS */;
LOCK TABLES `dictB2` WRITE;
INSERT INTO `dictB2` VALUES (3879,1,'erase','扂'),(3879,1,'open','	|l°N(3%@2I\r|ż#-Be.¸If­!ěÐ\Z$Ũ\n?A%!$9<\'D,14D$]@@7kKd*9ͥŭ˥Č	3ü(ȔЙ¿׽Ģ٭÷Eলǻ\rď\\	Ḥ̂ʷâƹϱȱǰ\Zŧj̐Só͗ʪ	&+¬?'),(3879,1,'contacting','Ӿ'),(3775,7,'open',','),(3759,7,'open','ā'),(3785,7,'open','ġ'),(3722,7,'open','Ŧ'),(3879,1,'recreate','攓'),(3721,1,'open','õ'),(3892,1,'open',''),(3880,1,'scroll','ḯᓙ'),(3881,1,'contacting','̑'),(3881,1,'open','	|^^5\Z\Z\r.\n	Ŭ2øƨ̈́᝴LXKÑƵքÀݿ'),(3799,7,'open','ƒ'),(3747,7,'open','^'),(3886,1,'open',''),(3802,7,'open','?'),(3772,7,'open',')'),(3760,7,'open','F'),(3763,7,'open','ļ'),(3879,1,'editable','㪨'),(3882,1,'open',''),(3879,1,'b1','䵓'),(3762,7,'open','â'),(3746,7,'open','6'),(3737,7,'open','Ğ'),(3753,7,'open',''),(3748,7,'open','ġ'),(3749,7,'open','.'),(3750,7,'open','9'),(3751,7,'open','ʮ'),(3752,7,'open','Ē'),(3723,7,'open','G'),(3885,1,'open',''),(3806,7,'open','u'),(3769,7,'open',')'),(3780,1,'open','«M'),(3728,7,'open','Ğ'),(3729,7,'open','×'),(3887,1,'open',''),(3782,7,'open','ƙ'),(3805,7,'open','Ð'),(3801,7,'open','9'),(3721,7,'open','ʟ'),(3742,7,'open',''),(3884,1,'open',''),(3827,1,'open','Ǻ'),(3797,7,'open','o'),(3767,7,'open','+'),(3726,7,'open',''),(3727,1,'uninterruptible','¤'),(3745,1,'open',''),(3750,1,'open','\Z'),(3752,1,'perlexp','m'),(3755,1,'open',''),(3743,7,'open',''),(3744,7,'open','ɟ'),(3879,2,'open',''),(3880,2,'open',''),(3881,2,'open',''),(3720,7,'open','ƾ'),(3880,1,'contacting','ԁ'),(3880,1,'open','	|4^@!G\"\"@28\'4ż#2=WF/{%\Z\ZT	9	\r&\"(<\":\rZ@M0\\4&¹	˗Ơ=Ħ\ndP \r¯łӊ!.،;ǅ0ේq¤Ǯ9±ତ\n&	!;	\r	:B^Ā\"&6'),(3856,1,'foo','ţ'),(3869,1,'open','\''),(3871,1,'47','☈'),(3871,1,'dup','ᢳ'),(3871,1,'gtm','Ⴝ'),(3871,1,'open','䠑'),(3871,1,'plenty','俸'),(3871,1,'recreate','㪥Ɉ'),(3871,1,'talked','㹥'),(3872,1,'foo','Ͼ,'),(3873,1,'rcvq','ύq'),(3874,1,'open',''),(3875,1,'open',''),(3876,1,'open',''),(3877,1,'open',''),(3878,1,'47','࠻'),(3878,1,'open','ŉ'),(3879,1,'47','䶂'),(3850,1,'open','$'),(3798,7,'open','ó'),(3764,7,'open','A'),(3768,7,'open',','),(3781,7,'open',''),(3779,7,'open','7'),(3771,7,'open',')'),(3773,7,'open','-'),(3720,1,'open',''),(3765,7,'open','á'),(3804,7,'open','¸'),(3738,7,'open',''),(3755,7,'open','Â'),(3774,7,'open','.'),(3776,7,'open','Ī'),(3889,1,'open',''),(3889,1,'register','ӕ'),(3890,1,'ddthh','ᯮ'),(3890,1,'open','̓ᘿ-ӧ'),(3891,1,'open',''),(3892,1,'editable','ķ'),(3888,1,'open','\n'),(3883,1,'open','\n'),(3807,7,'open','D'),(3796,7,'open',','),(3795,7,'open',''),(3794,7,'open','*'),(3793,7,'open','«'),(3790,7,'open','V'),(3791,7,'open','³'),(3792,7,'open','g'),(3789,7,'open',''),(3788,7,'open','ß'),(3786,7,'open','B'),(3787,7,'open','­'),(3783,7,'open','Ú'),(3756,7,'open','ø'),(3739,7,'open','Ğ'),(3740,7,'open','Ƌ'),(3741,7,'open','½'),(3725,7,'open','d'),(3724,7,'open','j'),(3736,7,'open','À'),(3735,7,'open','z'),(3734,7,'open','Ā'),(3733,7,'open',''),(3732,7,'open','ğ'),(3731,7,'open',''),(3730,7,'open',''),(3784,7,'open','ġ'),(3780,7,'open','Ʋ'),(3777,7,'open','-'),(3754,7,'open','̒'),(3803,7,'open','á'),(3800,7,'open','H'),(3766,7,'open','ô'),(3745,7,'open','ÿ'),(3761,7,'open','ġ'),(3758,7,'open','E'),(3721,1,'vast','t'),(3757,7,'open','ƪ'),(3778,7,'open','û'),(3770,7,'open',')'),(3727,7,'open','ń'),(3757,1,'args','Ę'),(3808,7,'open','}'),(3809,7,'open','g'),(3810,7,'open','1'),(3811,7,'open','É'),(3812,7,'open','6'),(3813,7,'open',''),(3814,7,'open','ġ'),(3815,7,'open','o'),(3816,7,'open','U'),(3817,7,'open','2'),(3818,7,'open','ġ'),(3819,7,'open','ģ'),(3820,7,'open','Ő'),(3821,7,'open','>'),(3822,7,'open','Ă'),(3823,7,'open','Ï'),(3824,7,'open','Ś'),(3825,7,'open','ņ'),(3826,7,'open',')'),(3827,7,'open','˘'),(3828,7,'open',' '),(3829,7,'open','z'),(3830,7,'open',''),(3831,7,'open','Ô'),(3832,7,'open','Q'),(3833,7,'open','ġ'),(3834,7,'open','¼'),(3835,7,'open',''),(3836,7,'open','f'),(3837,7,'open','Ĩ'),(3838,7,'open','E'),(3839,7,'open','7'),(3840,7,'open','̛'),(3841,7,'open','°'),(3842,7,'open','@'),(3843,7,'open','l'),(3844,7,'open','Ø'),(3845,7,'open','|'),(3846,7,'open','Ļ'),(3847,7,'open','W'),(3848,7,'open','²'),(3849,7,'open','~'),(3850,7,'open','L'),(3851,7,'open',''),(3852,7,'open','-'),(3853,7,'open','U'),(3854,7,'open','Ņ'),(3855,7,'open','<'),(3856,7,'open','ƒ'),(3857,7,'open','a'),(3858,7,'open','6'),(3859,7,'open','D'),(3860,7,'open','I'),(3861,7,'open','4'),(3862,7,'open','Ĝ'),(3863,7,'open','ľ'),(3864,7,'open',''),(3865,7,'open','£'),(3866,7,'open','¹'),(3867,7,'open','2'),(3868,7,'open','Ę'),(3869,7,'open','ě'),(3870,7,'open','ĥ'),(3871,7,'open','僘'),(3872,7,'open','ೱ'),(3873,7,'open','ե');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictB2` ENABLE KEYS */;

--
-- Table structure for table `dictB3`
--

DROP TABLE IF EXISTS `dictB3`;
CREATE TABLE `dictB3` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictB3`
--


/*!40000 ALTER TABLE `dictB3` DISABLE KEYS */;
LOCK TABLES `dictB3` WRITE;
INSERT INTO `dictB3` VALUES (3783,1,'running','6B'),(3790,1,'running','\"'),(3793,1,'running',''),(3782,1,'running','ĕI'),(3873,1,'running','Ǚ'),(3753,1,'running',''),(3840,1,'elapsed',''),(3781,1,'message','G'),(3766,1,'running','`V'),(3780,1,'nlm','ē/'),(3780,1,'running',''),(3760,1,'running',''),(3762,1,'echo','b'),(3754,1,'running','ƣ'),(3757,1,'elapsed',''),(3758,1,'running',''),(3743,1,'message',':'),(3877,1,'message','Ҡ\r'),(3879,1,'running','ᙫ⊇ќ'),(3879,1,'records','ㄡ'),(3866,1,'running','®'),(3741,1,'message','>'),(3740,1,'running','Ĝ'),(3877,1,'running','«֋'),(3879,1,'sonicwall','䵐ʢ'),(3880,1,'accessed','੃㨩'),(3866,1,'message',''),(3864,1,'echo','7'),(3871,1,'running','Ėֆˆᜓᴱ'),(3880,1,'layering','ႁ'),(3890,1,'records','ḷ\Z\Z'),(3797,6,'spooler','w'),(3872,1,'running','ǯĕ*'),(3872,1,'modems','Ҿr'),(3889,1,'schemes','̔'),(3856,1,'running','ę'),(3835,1,'running','v'),(3829,1,'running','*'),(3820,1,'message','ñ'),(3820,1,'authproto',''),(3819,1,'message','ÿ'),(3881,1,'records','ට˸ǆ5Ѯ¡'),(3881,1,'running','ߏӾڱฐ᐀'),(3881,1,'typerule','⊁'),(3881,1,'message','ԥૉæ	ײէ((4,ÅĮ%ƂFĦvgNʢΙ)ըÒ·U\r	â'),(3881,1,'deploy','ῦޠ'),(3881,1,'chapter','\n\n\n\n\n(ϩ\n\n̗ɖ:¸࿂Ēmڥ\n\n\n,q9Ąՠ©ڴ'),(3880,1,'running','ⱡֲጟ'),(3880,1,'message','ᒊ'),(3803,1,'accessed','Å'),(3803,1,'message','«'),(3813,1,'running','!T'),(3752,1,'message',''),(3872,1,'message','׃<$'),(3872,1,'echo','ƶŗ\'w,2L'),(3884,1,'chapter','āۺ{'),(3750,1,'message',' '),(3890,1,'message','ẛ'),(3802,1,'modems','$'),(3799,1,'nlm','Ě'),(3727,1,'running','*'),(3723,1,'message','.'),(3819,1,'authproto','ß'),(3840,1,'nis','Ž'),(3840,1,'isc','ɏ'),(3744,1,'authproto','t/'),(3884,1,'running','ࠄ'),(3871,1,'pointing','傞'),(3871,1,'message','෨ష'),(3871,1,'heuristics','☷'),(3871,1,'echo','⌢'),(3871,1,'555555','㧫'),(3871,1,'18446744073709551800','䴞'),(3876,1,'chapter','Ā'),(3875,1,'chapter','ƞ'),(3880,1,'arc627','∦%'),(3799,1,'message','æ'),(3877,1,'accessed','ɾ'),(3876,1,'echo','ƒ'),(3890,1,'running','ɾlãҚղR'),(3879,1,'message','᠒メƠÊ]5\nࣘોϲ'),(3879,1,'deploy','᠀'),(3874,1,'running','˕Á'),(3874,1,'chapter','ǎ'),(3879,1,'chapter','\"\n \n\"ϥ\nģ\n׳ƒԙ\nр	чǕչ=దʀ2ǃ2΋_Ɇ%Þ ¦ÄF¼		Ö\n\nࠅǩәԎǚԳ2		7\'\',ϝĕ	è'),(3879,1,'accessed','๶'),(3797,1,'spooler',''),(3795,1,'running','{'),(3734,1,'message','ï'),(3733,1,'message','8'),(3878,1,'running','ƎŎ\"'),(3816,1,'message','8\r'),(3815,1,'running','Z'),(3880,1,'installable','૗E\"'),(3880,1,'chapter','G\n҉ӳ̕׶±ĩ͓ࡖ	ʑ\nൕƢʀ¶஋QÎʟ\Z');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictB3` ENABLE KEYS */;

--
-- Table structure for table `dictB4`
--

DROP TABLE IF EXISTS `dictB4`;
CREATE TABLE `dictB4` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictB4`
--


/*!40000 ALTER TABLE `dictB4` DISABLE KEYS */;
LOCK TABLES `dictB4` WRITE;
INSERT INTO `dictB4` VALUES (3825,1,'distributed',''),(3824,1,'distributed',''),(3823,1,'distributed',''),(3822,1,'distributed',''),(3821,1,'distributed',''),(3820,1,'distributed',''),(3807,1,'distributed',''),(3808,1,'distributed',''),(3809,1,'distributed',''),(3810,1,'distributed',''),(3811,1,'distributed',''),(3812,1,'distributed',''),(3813,1,'distributed',''),(3814,1,'distributed',''),(3815,1,'distributed',''),(3816,1,'distributed',''),(3817,1,'distributed',''),(3818,1,'distributed',''),(3819,1,'distributed',''),(3803,1,'distributed',''),(3874,1,'distributed','ƅ'),(3856,1,'distributed',''),(3838,1,'distributed',''),(3839,1,'distributed',''),(3875,1,'distributed','ŕ'),(3801,1,'distributed',''),(3868,1,'distributed',''),(3852,1,'distributed',''),(3853,1,'distributed',''),(3854,1,'distributed',''),(3804,1,'distributed',''),(3805,1,'distributed',''),(3806,1,'distributed',''),(3846,1,'distributed',''),(3847,1,'distributed',''),(3848,1,'distributed',''),(3849,1,'distributed',''),(3850,1,'distributed',''),(3851,1,'distributed',''),(3845,1,'distributed',''),(3844,1,'distributed',''),(3843,1,'distributed',''),(3879,1,'dashboard','凇?±\r	\rNQ\r	\r	*	;%0+@\n\r\n$\nུ/\n;\nF'),(3872,1,'define','6ѡƙëtpI©IO)vQ.Ø;'),(3857,1,'distributed',''),(3857,1,'reports',''),(3858,1,'distributed',''),(3859,1,'distributed',''),(3860,1,'distributed',''),(3861,1,'distributed',''),(3862,1,'distributed',''),(3863,1,'distributed',''),(3864,1,'distributed',''),(3865,1,'distributed',''),(3827,1,'distributed',''),(3826,1,'distributed',''),(3871,1,'rounded','䋏'),(3855,1,'distributed',''),(3802,1,'distributed',''),(3875,1,'define','ˠ'),(3867,1,'distributed',''),(3866,1,'distributed',''),(3871,1,'room','Hޭw✉\nᢔ'),(3871,1,'reports','ؤঝᩉ'),(3871,1,'define','Ѵɸ६ϼĒWⵉ৷'),(3871,1,'1440','䖲'),(3871,1,'dashboard','㖚'),(3873,1,'define','ˑ!\"!!#\'%%%()()(\''),(3872,1,'recip','Ԥ'),(3878,1,'distributed','4'),(3877,1,'distributed',';'),(3830,1,'distributed',''),(3831,1,'distributed',''),(3832,1,'distributed',''),(3833,1,'distributed',''),(3834,1,'distributed',''),(3835,1,'distributed',''),(3835,1,'flexlm','0'),(3835,1,'quorum','g'),(3836,1,'distributed',''),(3837,1,'distributed',''),(3828,1,'distributed',''),(3722,1,'distributed',''),(3842,1,'distributed',''),(3878,1,'define',''),(3876,1,'distributed','·'),(3723,1,'distributed',''),(3724,1,'distributed',''),(3725,1,'distributed',''),(3726,1,'distributed',''),(3727,1,'distributed','Đ'),(3728,1,'distributed',''),(3729,1,'distributed',''),(3730,1,'distributed',''),(3731,1,'distributed',''),(3732,1,'distributed',''),(3733,1,'distributed',''),(3734,1,'distributed',''),(3735,1,'distributed',''),(3736,1,'distributed',''),(3737,1,'distributed',''),(3738,1,'distributed',''),(3739,1,'distributed',''),(3740,1,'distributed',''),(3741,1,'distributed',''),(3742,1,'distributed',''),(3742,1,'reports',''),(3743,1,'distributed',''),(3744,1,'distributed',''),(3745,1,'distributed',''),(3746,1,'distributed',''),(3747,1,'distributed',''),(3748,1,'distributed',''),(3749,1,'distributed',''),(3750,1,'distributed',''),(3751,1,'distributed',''),(3752,1,'distributed',''),(3753,1,'distributed',''),(3754,1,'distributed',''),(3755,1,'distributed',''),(3756,1,'distributed',''),(3757,1,'distributed',''),(3758,1,'distributed',''),(3759,1,'distributed',''),(3760,1,'distributed',''),(3761,1,'distributed',''),(3762,1,'distributed',''),(3763,1,'distributed',''),(3764,1,'distributed',''),(3765,1,'distributed',''),(3766,1,'distributed',''),(3767,1,'distributed',''),(3768,1,'distributed',''),(3769,1,'distributed',''),(3770,1,'distributed',''),(3771,1,'distributed',''),(3772,1,'distributed',''),(3773,1,'distributed',''),(3774,1,'distributed',''),(3775,1,'distributed',''),(3776,1,'distributed',''),(3777,1,'distributed',''),(3778,1,'distributed',''),(3779,1,'distributed',''),(3780,1,'distributed',''),(3781,1,'distributed',''),(3782,1,'distributed',''),(3783,1,'distributed',''),(3784,1,'distributed',''),(3785,1,'distributed',''),(3786,1,'distributed',''),(3787,1,'distributed',''),(3788,1,'distributed',''),(3789,1,'distributed',''),(3790,1,'distributed',''),(3791,1,'distributed',''),(3792,1,'distributed',''),(3793,1,'distributed',''),(3794,1,'distributed',''),(3795,1,'distributed',''),(3796,1,'distributed',''),(3797,1,'distributed',''),(3798,1,'distributed',''),(3798,1,'reports','è'),(3799,1,'define','Ŏ'),(3799,1,'distributed',''),(3800,1,'distributed',''),(3720,1,'keyboard','LŅ'),(3841,1,'distributed',''),(3829,1,'distributed',''),(3879,1,'allowing','ủܡ'),(3865,1,'reports',''),(3840,1,'distributed',''),(3721,1,'define','Ŭ'),(3721,1,'distributed',''),(3879,1,'define','݊ࢯᆵ%ď4\"7A9\'úɉ!ъìýÂ.ʈʳ4Ëok$#9ҬÍ©·PLÅώñA=\\|ێ૥6Vg3'),(3879,1,'distributed','=ٝY'),(3879,1,'implementing','▼Ᲊǽ͵'),(3879,1,'reports','ȈԮԯ]֙ޚŌ♱E໯\'(d8V:j\n6¥Pì		%	ཿr'),(3879,1,'scenarios','㚊ˎ'),(3880,1,'acknowledge','ᓁឩ	џ'),(3880,1,'allowing','㔚'),(3880,1,'define','ኛ'),(3880,1,'distributed','=υU཮ㅆĺ'),(3880,1,'implementing','വ'),(3880,1,'reports','ŒȬҴ¨\nȖŧȐVFҟἧʳ	\n *HL	7Ȉ|U4̙­7Šñ?\'*Ȯ'),(3880,1,'room','॔ᑪ'),(3881,1,'allowing','ٗ℔'),(3881,1,'dashboard','⏟ŕ'),(3881,1,'define','᳌Яైਚ'),(3881,1,'distributed','=⍔'),(3881,1,'getservicebystatusid','᛭'),(3881,1,'reports','Ćӱ¤᷊6J\r\nß'),(3881,1,'room','ᾇ'),(3881,1,'scenarios','ⷡ'),(3883,1,'define','È'),(3884,1,'define','SǏ2OÛvˌġóåƹ»2OÛvȻǁæ!ã'),(3884,1,'distributed','΢਄'),(3885,1,'define',')Ь¡Ń'),(3886,1,'define','ã!Σ'),(3887,1,'define','W!׺¡ǈǡ}J)_¡ʣ'),(3888,1,'define','e'),(3889,1,'define','/ǎ|ȸ'),(3890,1,'define','΋;ഩIփ+V¦Lȉ>'),(3890,1,'distributed','ਗ਼ĈଭJ/\"'),(3890,1,'implementing','ᚎJ'),(3891,1,'define','.ºKc6¤ĻKc<'),(3892,1,'define','\r÷'),(3835,6,'flexlm',''),(3886,6,'managinghosts','Ի');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictB4` ENABLE KEYS */;

--
-- Table structure for table `dictB5`
--

DROP TABLE IF EXISTS `dictB5`;
CREATE TABLE `dictB5` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictB5`
--


/*!40000 ALTER TABLE `dictB5` DISABLE KEYS */;
LOCK TABLES `dictB5` WRITE;
INSERT INTO `dictB5` VALUES (3871,1,'2290729126','䄶'),(3871,1,'1020615000','⯾'),(3871,1,'1020612000','⭨'),(3862,1,'test','ď'),(3880,1,'api','ធ'),(3871,1,'saving','ᦫ'),(3720,1,'test','p'),(3871,1,'59','✊'),(3871,1,'978301800','䣴'),(3834,1,'alive','*'),(3815,1,'6667','^'),(3808,1,'minute','\Z*'),(3879,1,'minute','⻾'),(3879,1,'delivered','䊼'),(3879,1,'consulting','䎳'),(3879,1,'concatenated','ẃ'),(3879,1,'api','ᆿ㥔ᔲ'),(3879,1,'alive','䦱'),(3879,1,'59','喵E'),(3878,1,'straightforward','࣬'),(3877,1,'test','׫ŵ'),(3876,1,'alive','{ËG'),(3875,1,'alive','ă'),(3879,1,'test','KϴହྨÀţ؀؜ '),(3879,1,'straightforward','⁑'),(3879,1,'saving','➾'),(3782,1,'minute','®'),(3780,1,'minute','\\'),(3779,1,'tablespace','*'),(3778,1,'tablespace','1K'),(3776,1,'tablespace','¬'),(3751,1,'turn','č'),(3753,1,'test','n'),(3756,1,'test',''),(3762,1,'ian','À'),(3765,1,'minute','U'),(3766,1,'minute','b'),(3871,1,'minute','Н§©஡%ᅔ#\nнᬹ	Φ'),(3871,1,'inouts','Ⱝ'),(3871,1,'ds1','ᷡ«'),(3871,1,'concatenated','⓶'),(3820,1,'v1',';	²'),(3874,1,'minute','ɬ'),(3803,1,'exim','~'),(3740,1,'authprotocol','-0'),(3872,1,'test','ɮδ'),(3871,1,'turn','ሪਠ'),(3871,1,'bash','⌐'),(3872,1,'consulting','ի'),(3871,1,'test','Ķ⺔ۡ\ZĮ\n\n\n\n?¢\'Ůà»ě٫'),(3798,1,'test',''),(3720,1,'api','u§'),(3872,1,'bash','ƈ'),(3830,1,'turn','F'),(3827,1,'test','!'),(3879,1,'repackaging','̖'),(3819,1,'v1',':	Á'),(3816,1,'freebsd',''),(3862,1,'freebsd','°'),(3859,1,'6509','!'),(3856,1,'outputfile','5'),(3840,1,'resolv','<æÓ'),(3840,1,'alive','˄'),(3880,1,'delivered','ષ'),(3880,1,'repackaging','ʀ'),(3880,1,'saving','㛃'),(3881,1,'api','º\nϩ\nsÄĕíǼ\Z\r	\rA!೻ǚÒ#Cȝޭ'),(3881,1,'bash','⌾'),(3881,1,'experts','Ǘ'),(3881,1,'intermediary','ؚ'),(3881,1,'requiring','ي'),(3881,1,'saving','⹦'),(3881,1,'test','૦ʢ'),(3883,1,'minute','ĩ'),(3884,1,'saving','ǁਘ'),(3884,1,'test','ܸট'),(3885,1,'alive','ғ'),(3885,1,'saving','ȭװ+jR'),(3887,1,'alive','۔'),(3888,1,'redundancy','š'),(3888,1,'saving','ý'),(3888,1,'test','ɤٛ'),(3888,1,'turn','ſ'),(3890,1,'concurrent','Ꭽ3'),(3890,1,'minute','൦ڝ'),(3890,1,'redundancy','੔Ĉ'),(3890,1,'saving','ý'),(3890,1,'test','ᴡŤ'),(3892,1,'saving','ï'),(3892,1,'test','ƀ'),(3816,6,'freebsd',']');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictB5` ENABLE KEYS */;

--
-- Table structure for table `dictB6`
--

DROP TABLE IF EXISTS `dictB6`;
CREATE TABLE `dictB6` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictB6`
--


/*!40000 ALTER TABLE `dictB6` DISABLE KEYS */;
LOCK TABLES `dictB6` WRITE;
INSERT INTO `dictB6` VALUES (3887,1,'association','ཥ'),(3879,1,'remote','ጎkK⁩̀ͷ'),(3880,1,'auto','㜂'),(3871,1,'auto','ᵣ'),(3847,1,'remote',''),(3845,1,'remote','	'),(3778,1,'remote','EX'),(3778,1,'opt','é'),(3775,1,'invobj',''),(3763,1,'remote','Ù'),(3766,1,'remote','\"¶'),(3884,1,'association','ୈ'),(3880,1,'instructions','΀㿣ɨ'),(3881,1,'minor','ͼ⧘m'),(3888,1,'instructions','Ċ'),(3878,1,'remote','ǈ \"  \"\"m\r\r\nOG785+	-7/033522220030%/'),(3878,1,'instructions','Ĝ'),(3877,1,'chown','ќ'),(3877,1,'exists','א'),(3877,1,'opt','ʭ'),(3880,1,'framework','ɼ´Ԭމѱ'),(3879,1,'validator','垧'),(3783,1,'remote',' 709'),(3808,1,'remote',''),(3822,1,'remote','('),(3843,1,'remote','/'),(3879,1,'longer','⟹ӬƸóᇌᮄ'),(3879,1,'instructions','᪒ῆ৳'),(3879,1,'exists','⧱Ɯ'),(3781,1,'ntp',''),(3720,1,'opera','ð\''),(3879,1,'minor','թ'),(3872,1,'exists','Ǥʋ	ΰ'),(3727,1,'remote','«K*'),(3879,1,'changeable','ฝ'),(3871,1,'instructions','ᴴᙫ'),(3871,1,'longer','Đ॓'),(3871,1,'minor','༮˻'),(3871,1,'remote','ȳϼ⛶	'),(3871,1,'tiny','㇫'),(3879,1,'framework','̒´Σǆğǹ໷\Z4㗑'),(3862,1,'remote','{'),(3887,1,'longer','čअ'),(3862,1,'compiling',''),(3856,1,'remote','q	w'),(3881,1,'longer','इ⢁'),(3881,1,'instructions','Ɫ'),(3881,1,'framework','Īϊģ|éᑜ?ǍD\Z8ĦךÝB@%ԧ22ǭײ'),(3881,1,'exists','ᬾ==ŧጿ'),(3880,1,'unrecognized','ᮽ'),(3880,1,'minor','լ'),(3880,1,'longer','दᯙዑ'),(3886,1,'longer','å'),(3886,1,'association','ȥ'),(3885,1,'longer','ࣁR'),(3885,1,'instructions','Őĵլ'),(3884,1,'opportunity','Ê'),(3884,1,'longer','ᐣ'),(3762,1,'remote',''),(3757,1,'pcpu','÷'),(3744,1,'remote',''),(3751,1,'longer','Ɩ'),(3754,1,'remote','/%Ŭ1\Z%59%'),(3735,1,'remote','	'),(3738,1,'remote','\'7'),(3740,1,'privpassword','Ë'),(3740,1,'remote','ŕ!'),(3879,1,'opportunity','⅘'),(3883,1,'longer','È'),(3881,1,'remote','⎼x'),(3871,1,'appending','Ვ'),(3871,1,'920805600','㠫}ᛖ'),(3876,1,'remote','>'),(3874,1,'remote','FĠɫ'),(3873,1,'remote','\nA\n'),(3873,1,'minor','K'),(3873,1,'framework','zK'),(3872,1,'longer','ଶŜ'),(3879,1,'association','㬲'),(3888,1,'longer','ԭ'),(3890,1,'appending','٥'),(3890,1,'auto','ᐕX\r0'),(3890,1,'exists','ᬀ'),(3890,1,'remote','ቮ'),(3891,1,'longer','Î̞'),(3892,1,'remote','½'),(3735,6,'remote',''),(3754,6,'remote','̙'),(3775,6,'invobj','4'),(3781,6,'ntp','¤'),(3808,6,'remote',''),(3845,6,'remote','');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictB6` ENABLE KEYS */;

--
-- Table structure for table `dictB7`
--

DROP TABLE IF EXISTS `dictB7`;
CREATE TABLE `dictB7` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictB7`
--


/*!40000 ALTER TABLE `dictB7` DISABLE KEYS */;
LOCK TABLES `dictB7` WRITE;
INSERT INTO `dictB7` VALUES (3887,1,'load','ග'),(3878,1,'servers','Ã͎Ȁ'),(3797,1,'plugins','\n'),(3892,1,'respective','ņ'),(3866,1,'load',':.'),(3813,1,'plugins','\n'),(3814,1,'plugins','\n'),(3760,2,'plugins',''),(3749,1,'plugins','\n'),(3880,1,'cost','޴'),(3860,1,'plugins','\n'),(3861,1,'plugins','\n'),(3861,1,'servers','!'),(3798,1,'plugins','\n'),(3816,2,'plugins',''),(3817,2,'plugins',''),(3809,1,'plugins','\n'),(3880,1,'plugins','ʫఌɘ㋿û'),(3865,1,'plugins','\n'),(3752,1,'cricket','Ý	'),(3780,2,'plugins',''),(3871,1,'load','K᎞̿ᝯ'),(3792,2,'plugins',''),(3835,2,'plugins',''),(3821,2,'plugins',''),(3752,2,'plugins',''),(3757,2,'plugins',''),(3789,2,'plugins',''),(3784,2,'plugins',''),(3765,2,'plugins',''),(3772,2,'plugins',''),(3727,1,'load','å'),(3755,1,'plugins','\n'),(3812,1,'plugins','\n'),(3779,2,'plugins',''),(3890,1,'load','mݳ৔¸Ĭ࣊k6#\nl*4'),(3800,2,'plugins',''),(3879,1,'load','⃣иÚşᦄȷขw\no6,#¼5K@\n\r\n$\n࿒'),(3744,1,'respective','ț'),(3877,1,'servers','יÔ'),(3843,1,'plugins','\n'),(3844,1,'plugins','\n'),(3845,1,'plugins','\n'),(3846,1,'plugins','\n'),(3847,1,'plugins','\n'),(3848,1,'plugins','\n'),(3835,1,'servers','d\n'),(3826,2,'plugins',''),(3884,1,'servers','ࢰ'),(3871,1,'assumed','ࣧᵤBഎ'),(3832,2,'plugins',''),(3769,2,'plugins',''),(3890,1,'servers','੍Ĉ'),(3749,2,'plugins',''),(3871,1,'rrdtune','⡒'),(3871,1,'uk','㟂'),(3871,1,'yields','▢'),(3872,1,'assumed','Վ'),(3873,1,'plugins','P@@û'),(3873,1,'servers','ɜ'),(3874,1,'load','½ãč'),(3874,1,'plugins','ŕ'),(3875,1,'plugins','Ĳ'),(3871,1,'netherlands','䗯'),(3864,1,'plugins','\n'),(3863,1,'plugins','\n'),(3838,1,'plugins','\n'),(3837,1,'plugins','\n'),(3836,1,'plugins','\n'),(3808,2,'plugins',''),(3806,2,'plugins',''),(3807,2,'plugins',''),(3804,2,'plugins',''),(3805,2,'plugins',''),(3803,2,'plugins',''),(3802,2,'plugins',''),(3801,2,'plugins',''),(3845,2,'plugins',''),(3844,2,'plugins',''),(3843,2,'plugins',''),(3842,2,'plugins',''),(3841,2,'plugins',''),(3840,2,'plugins',''),(3804,1,'plugins','\n'),(3803,1,'plugins','\n'),(3802,1,'plugins','\n'),(3801,1,'plugins','\n'),(3800,1,'plugins','\n'),(3799,1,'plugins','\n'),(3799,1,'load','¤'),(3839,2,'plugins',''),(3797,2,'plugins',''),(3810,2,'plugins',''),(3809,2,'plugins',''),(3850,2,'plugins',''),(3834,2,'plugins',''),(3833,2,'plugins',''),(3881,1,'attempted','ෳ'),(3881,1,'actions','ഴᲑ'),(3881,1,'achieved','௨'),(3880,1,'servers','Ƚӝdd๻໪ເ'),(3887,1,'assumed','ಛ¡'),(3828,2,'plugins',''),(3827,2,'plugins',''),(3759,2,'plugins',''),(3847,2,'plugins',''),(3838,2,'plugins',''),(3796,2,'plugins',''),(3827,1,'servers','\'i'),(3828,1,'load','0'),(3828,1,'plugins','\n'),(3829,1,'plugins','\n'),(3830,1,'plugins','\n'),(3795,1,'plugins','\n'),(3796,1,'plugins','\n'),(3881,1,'load','↋Ν'),(3877,1,'load','ؠ'),(3867,1,'plugins','\n'),(3854,1,'plugins','\n'),(3855,1,'plugins','\n'),(3856,1,'plugins','\n'),(3857,1,'plugins','\n'),(3858,1,'plugins','\n'),(3859,1,'plugins','\n'),(3879,1,'plugins','Ěȧ᧭	\n.\n૏ൄฃܔKᓧK'),(3823,2,'plugins',''),(3816,1,'plugins','\n'),(3754,1,'plugins','\n'),(3820,2,'plugins',''),(3751,2,'plugins',''),(3756,2,'plugins',''),(3788,2,'plugins',''),(3783,2,'plugins',''),(3764,2,'plugins',''),(3771,2,'plugins',''),(3722,1,'servers','F'),(3754,2,'plugins',''),(3832,1,'plugins','\n'),(3811,1,'plugins','\n'),(3778,2,'plugins',''),(3890,1,'assumed','َ¦'),(3799,2,'plugins',''),(3818,1,'plugins','\n'),(3744,1,'plugins','*'),(3839,1,'plugins','\n'),(3840,1,'plugins','\n'),(3841,1,'dlsw','\ZM'),(3841,1,'plugins','\n'),(3842,1,'plugins','\n'),(3835,1,'plugins','\n'),(3825,2,'plugins',''),(3884,1,'plugins','ڬȘ'),(3869,1,'servers','å'),(3831,2,'plugins',''),(3768,2,'plugins',''),(3890,1,'plugins','ʕ\n'),(3748,2,'plugins',''),(3871,1,'explains','ぼֲ'),(3791,2,'plugins',''),(3853,2,'plugins',''),(3809,1,'load',''),(3880,1,'load','ज़ᑪ᜘ු0'),(3865,1,'load','|'),(3751,1,'plugins','\n'),(3815,2,'plugins',''),(3886,1,'plugins','ŵ'),(3758,2,'plugins',''),(3846,2,'plugins',''),(3837,2,'plugins',''),(3795,2,'plugins',''),(3824,1,'plugins','\n'),(3824,1,'servers','æ'),(3825,1,'plugins','\n'),(3826,1,'plugins','\n'),(3827,1,'plugins','\n'),(3759,1,'plugins','\n'),(3759,1,'servers','u'),(3760,1,'plugins','\n'),(3761,1,'plugins','\n'),(3762,1,'plugins','\n'),(3763,1,'plugins','\n'),(3764,1,'plugins','\n'),(3765,1,'load','Z\Z'),(3765,1,'plugins','\n'),(3766,1,'load','e'),(3766,1,'plugins','\n'),(3767,1,'plugins','\n'),(3768,1,'plugins','\n'),(3769,1,'plugins','\n'),(3770,1,'plugins','\n'),(3771,1,'plugins','\n'),(3772,1,'plugins','\n'),(3773,1,'plugins','\n'),(3774,1,'plugins','\n'),(3775,1,'plugins','\n'),(3776,1,'plugins','\n'),(3777,1,'plugins','\n'),(3778,1,'plugins','\n'),(3779,1,'plugins','\n'),(3780,1,'load','_'),(3780,1,'plugins','\n'),(3780,1,'servers','ź'),(3781,1,'plugins','\n'),(3782,1,'load',''),(3782,1,'plugins','\n'),(3783,1,'plugins','\n­'),(3784,1,'plugins','\n'),(3785,1,'plugins','\n'),(3786,1,'plugins','\n'),(3788,1,'plugins','\n'),(3789,1,'plugins','\n'),(3790,1,'plugins','\n'),(3791,1,'plugins','\n'),(3792,1,'freetds','D'),(3792,1,'plugins','\n'),(3793,1,'plugins','\n'),(3794,1,'plugins','\n'),(3795,1,'mysql2',''),(3746,1,'plugins','\n'),(3833,1,'plugins','\n'),(3793,2,'plugins',''),(3781,2,'plugins',''),(3761,2,'plugins',''),(3762,2,'plugins',''),(3880,1,'actions','༔'),(3879,1,'w3','垩Ս'),(3747,1,'plugins','\n'),(3748,1,'plugins','\n'),(3889,1,'senior','̽'),(3774,2,'plugins',''),(3775,2,'plugins',''),(3879,1,'servers','˞y໢ူB=/ᇏ¸'),(3836,2,'plugins',''),(3794,2,'plugins',''),(3851,2,'plugins',''),(3819,1,'plugins','\n'),(3820,1,'plugins','\n'),(3821,1,'plugins','\n'),(3822,1,'plugins','\n'),(3823,1,'plugins','\n'),(3823,1,'servers','\n'),(3756,1,'plugins','\n'),(3757,1,'plugins','\n'),(3758,1,'plugins','\n'),(3785,2,'plugins',''),(3876,1,'plugins','¢'),(3866,1,'plugins','\n'),(3866,1,'servers','=p'),(3849,1,'plugins','\n'),(3849,1,'serverip','$\n'),(3849,1,'servers',''),(3850,1,'plugins','\n'),(3851,1,'plugins','\n'),(3852,1,'plugins','\n'),(3853,1,'plugins','\n'),(3811,2,'plugins',''),(3885,1,'load','ݳ'),(3822,2,'plugins',''),(3815,1,'plugins','\n'),(3819,2,'plugins',''),(3752,1,'plugins','\n'),(3753,1,'plugins','\n'),(3750,2,'plugins',''),(3848,2,'plugins',''),(3755,2,'plugins',''),(3786,2,'plugins',''),(3782,2,'plugins',''),(3763,2,'plugins',''),(3770,2,'plugins',''),(3721,1,'1992','ā'),(3776,2,'plugins',''),(3753,2,'plugins',''),(3849,2,'plugins',''),(3831,1,'plugins','\n'),(3810,1,'plugins','\n'),(3777,2,'plugins',''),(3889,1,'servers','ѻ@'),(3798,2,'plugins',''),(3879,1,'actions','ᶀ㔜༺'),(3817,1,'plugins','\n'),(3734,1,'plugins','\n'),(3742,1,'plugins','\n'),(3877,1,'plugins','¬(5ɶŬ:Ù'),(3887,1,'plugins','Ɲ'),(3888,1,'eliminates','şڝ'),(3888,1,'skills','ˌ'),(3773,2,'plugins',''),(3862,1,'plugins','\n'),(3766,2,'plugins',''),(3834,1,'plugins','\n'),(3834,1,'load','b'),(3829,2,'plugins',''),(3824,2,'plugins',''),(3881,1,'serverip','ஔ'),(3881,1,'servers','∘ટ'),(3868,1,'plugins','o\n'),(3818,2,'plugins',''),(3830,2,'plugins',''),(3767,2,'plugins',''),(3747,2,'plugins',''),(3746,2,'plugins',''),(3734,2,'plugins',''),(3742,2,'plugins',''),(3871,1,'british','⏢'),(3790,2,'plugins',''),(3852,2,'plugins',''),(3805,1,'plugins','\n'),(3806,1,'plugins','\n'),(3807,1,'plugins','\n'),(3808,1,'load',''),(3808,1,'plugins','\n'),(3750,1,'plugins','\n'),(3854,2,'plugins',''),(3814,2,'plugins',''),(3813,2,'plugins',''),(3812,2,'plugins',''),(3855,2,'plugins',''),(3856,2,'plugins',''),(3857,2,'plugins',''),(3858,2,'plugins',''),(3859,2,'plugins',''),(3860,2,'plugins',''),(3861,2,'plugins',''),(3862,2,'plugins',''),(3863,2,'plugins',''),(3864,2,'plugins',''),(3865,2,'plugins',''),(3866,2,'plugins',''),(3867,2,'plugins',''),(3808,6,'load',''),(3809,6,'load','n'),(3722,7,'plugins','ū'),(3723,7,'plugins','L'),(3724,7,'plugins','o'),(3725,7,'plugins','i'),(3726,7,'plugins',''),(3727,7,'plugins','ŉ'),(3728,7,'plugins','ģ'),(3729,7,'plugins','Ü'),(3730,7,'plugins',''),(3731,7,'plugins','£'),(3732,7,'plugins','Ĥ'),(3733,7,'plugins',''),(3734,7,'plugins','ą'),(3735,7,'plugins',''),(3736,7,'plugins','Å'),(3737,7,'plugins','ģ'),(3738,7,'plugins',''),(3739,7,'plugins','ģ'),(3740,7,'plugins','Ɛ'),(3741,7,'plugins','Â'),(3742,7,'plugins',''),(3743,7,'plugins',''),(3744,7,'plugins','ɤ'),(3745,7,'plugins','Ą'),(3746,7,'plugins',';'),(3747,7,'plugins','c'),(3748,7,'plugins','Ħ'),(3749,7,'plugins','3'),(3750,7,'plugins','>'),(3751,7,'plugins','ʳ'),(3752,7,'plugins','ė'),(3753,7,'plugins',''),(3754,7,'plugins','̗'),(3755,7,'plugins','Ç'),(3756,7,'plugins','ý'),(3757,7,'plugins','Ư'),(3758,7,'plugins','J'),(3759,7,'plugins','Ć'),(3760,7,'plugins','K'),(3761,7,'plugins','Ħ'),(3762,7,'plugins','ç'),(3763,7,'plugins','Ł'),(3764,7,'plugins','F'),(3765,7,'plugins','æ'),(3766,7,'plugins','ù'),(3767,7,'plugins','0'),(3768,7,'plugins','1'),(3769,7,'plugins','.'),(3770,7,'plugins','.'),(3771,7,'plugins','.'),(3772,7,'plugins','.'),(3773,7,'plugins','2'),(3774,7,'plugins','3'),(3775,7,'plugins','1'),(3776,7,'plugins','į'),(3777,7,'plugins','2'),(3778,7,'plugins','Ā'),(3779,7,'plugins','<'),(3780,7,'plugins','Ʒ'),(3781,7,'plugins','¢'),(3782,7,'plugins','ƞ'),(3783,7,'plugins','ß'),(3784,7,'plugins','Ħ'),(3785,7,'plugins','Ħ'),(3786,7,'plugins','G'),(3787,7,'plugins','²'),(3788,7,'plugins','ä'),(3789,7,'plugins',' '),(3790,7,'plugins','['),(3791,7,'plugins','¸'),(3792,7,'plugins','l'),(3793,7,'plugins','°'),(3794,7,'plugins','/'),(3795,7,'plugins',''),(3796,7,'plugins','1'),(3797,7,'plugins','t'),(3798,7,'plugins','ø'),(3799,7,'plugins','Ɨ'),(3800,7,'plugins','M'),(3801,7,'plugins','>'),(3802,7,'plugins','D'),(3803,7,'plugins','æ'),(3804,7,'plugins','½'),(3805,7,'plugins','Õ'),(3806,7,'plugins','z'),(3807,7,'plugins','I'),(3808,7,'plugins',''),(3809,7,'plugins','l'),(3810,7,'plugins','6'),(3811,7,'plugins','Î'),(3812,7,'plugins',';'),(3813,7,'plugins',''),(3814,7,'plugins','Ħ'),(3815,7,'plugins','t'),(3816,7,'plugins','Z'),(3817,7,'plugins','7'),(3818,7,'plugins','Ħ'),(3819,7,'plugins','Ĩ'),(3820,7,'plugins','ŕ'),(3821,7,'plugins','C'),(3822,7,'plugins','ć'),(3823,7,'plugins','Ô'),(3824,7,'plugins','ş'),(3825,7,'plugins','ŋ'),(3826,7,'plugins','.'),(3827,7,'plugins','˝'),(3828,7,'plugins','¥'),(3829,7,'plugins',''),(3830,7,'plugins',''),(3831,7,'plugins','Ù'),(3832,7,'plugins','V'),(3833,7,'plugins','Ħ'),(3834,7,'plugins','Á'),(3835,7,'plugins',''),(3836,7,'plugins','k'),(3837,7,'plugins','ĭ'),(3838,7,'plugins','J'),(3839,7,'plugins','<'),(3840,7,'plugins','̠'),(3841,7,'plugins','µ'),(3842,7,'plugins','E'),(3843,7,'plugins','q'),(3844,7,'plugins','Ý'),(3845,7,'plugins',''),(3846,7,'plugins','ŀ'),(3847,7,'plugins','\\'),(3848,7,'plugins','·'),(3849,7,'plugins',''),(3850,7,'plugins','Q'),(3851,7,'plugins',''),(3852,7,'plugins','2'),(3853,7,'plugins','Z'),(3854,7,'plugins','Ŋ'),(3855,7,'plugins','A'),(3856,7,'plugins','Ɨ'),(3857,7,'plugins','f'),(3858,7,'plugins',';'),(3859,7,'plugins','I'),(3860,7,'plugins','N'),(3861,7,'plugins','9'),(3862,7,'plugins','ġ'),(3863,7,'plugins','Ń'),(3864,7,'plugins',''),(3865,7,'plugins','¨'),(3866,7,'plugins','¾'),(3867,7,'plugins','7');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictB7` ENABLE KEYS */;

--
-- Table structure for table `dictB8`
--

DROP TABLE IF EXISTS `dictB8`;
CREATE TABLE `dictB8` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictB8`
--


/*!40000 ALTER TABLE `dictB8` DISABLE KEYS */;
LOCK TABLES `dictB8` WRITE;
INSERT INTO `dictB8` VALUES (3872,1,'1d','ౣ'),(3872,1,'controlled','Ү'),(3872,1,'creating','ȧ՘Ò\n>ˇm'),(3878,1,'solutions','ė'),(3879,1,'creating','վҽ׊ት੕Ëा̮ͺҹȚጿ'),(3880,1,'pie','ܩᔌ≌hg'),(3880,1,'solutions','ѯ'),(3880,1,'creating','ց⺵Ėŵ'),(3880,1,'groundworkopensource','٭'),(3879,1,'dbusername','刎'),(3879,1,'crontab','喞9'),(3881,1,'creating','ĴɝƉᝨħ̒ãҏ)Xպ<'),(3879,1,'expected','䡦'),(3880,1,'controlled','዇'),(3880,1,'browse','䞍'),(3879,1,'tabbed','໸'),(3879,1,'solutions','Ѭ'),(3879,1,'groundworkopensource','٪࿰'),(3884,1,'controlled','Ԩ਋'),(3881,1,'solutions','ɿ'),(3881,1,'groundworkopensource','ѿ'),(3881,1,'getpriority','ມ'),(3881,1,'getmonitorserver','ሕ'),(3879,1,'controlled','౷'),(3879,1,'browse','⥩'),(3871,1,'xsize','ᐹ'),(3721,1,'solutions','ǲ'),(3724,1,'expected','Y'),(3726,1,'expected','K'),(3728,1,'expected','¨'),(3732,1,'expected','©'),(3734,1,'dbsvr','.'),(3737,1,'expected','¨'),(3738,1,'expected','g'),(3739,1,'expected','¨'),(3745,1,'expected',''),(3748,1,'expected','«'),(3761,1,'expected','«'),(3763,1,'logname','Z'),(3780,1,'nwstat',''),(3784,1,'expected','«'),(3785,1,'expected','«'),(3814,1,'expected','«'),(3818,1,'expected','«'),(3823,1,'expected',''),(3823,1,'farm','2\n$	.'),(3833,1,'expected','«'),(3840,1,'expected','I.'),(3848,1,'expected','71'),(3854,1,'expected','Ï'),(3856,1,'logname',''),(3871,1,'1d','⿞'),(3871,1,'controlled','ȴ'),(3871,1,'creating','㪷ఠŘ'),(3871,1,'dinf','ü'),(3871,1,'expected','ࣴJ	᳔೑'),(3871,1,'surprises','⛷'),(3885,1,'controlled','ӕĆ'),(3886,1,'creating','Ү'),(3887,1,'controlled','ܖĆ'),(3888,1,'creating','֡ɟ'),(3890,1,'controlled','ᝬ'),(3891,1,'controlled','ɇâ˗<'),(3780,6,'nwstat','ƹ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictB8` ENABLE KEYS */;

--
-- Table structure for table `dictB9`
--

DROP TABLE IF EXISTS `dictB9`;
CREATE TABLE `dictB9` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictB9`
--


/*!40000 ALTER TABLE `dictB9` DISABLE KEYS */;
LOCK TABLES `dictB9` WRITE;
INSERT INTO `dictB9` VALUES (3880,1,'oetiker','˝'),(3876,1,'environment','Ņ'),(3873,1,'executed','ǰ'),(3871,1,'environment','ᆲᮎ'),(3871,1,'9am','✇'),(3871,1,'contributions','〰'),(3871,1,'1020612300','⭷'),(3873,1,'environment','0'),(3871,1,'trivial','Ƨ'),(3872,1,'environment','՞'),(3872,1,'executed','ߚɒ)'),(3871,1,'mailserver','ஸ'),(3758,1,'verify',':'),(3778,1,'environment','¸'),(3798,1,'newest','*'),(3880,1,'maintaining','ѭ'),(3879,1,'verify','℻'),(3880,1,'birt','˾'),(3880,1,'environment','ဦ'),(3880,1,'executed','ᄖfRģ෬'),(3881,1,'environment','Į╙Bːϙ۳'),(3881,1,'birt','߇'),(3880,1,'verify','௽K'),(3842,1,'verify',' '),(3840,1,'obsolete','ʆ'),(3820,1,'verify','ķ'),(3803,1,'mailserver','z'),(3799,1,'oetiker','ž'),(3879,1,'oetiker','ͳ'),(3879,1,'executed','䊨ϹwΛᓣϞ'),(3871,1,'shortest','㵍'),(3871,1,'oetiker','	ھټƥႝ]^çـȕ˖ή'),(3871,1,'march','⏼ጁ'),(3798,1,'oetiker','½'),(3879,1,'maintaining','ȞɌ庝'),(3879,1,'environment','Ôڨ3صകЖ⯷నऊ'),(3879,1,'birt','Δ'),(3877,1,'verify','Ң'),(3881,1,'executed','ℏгѦ'),(3881,1,'expensive','ⅩصQ'),(3881,1,'jmx','ặ'),(3881,1,'maintaining','ɽ'),(3881,1,'rejected','ᾫƗ1ñ'),(3881,1,'test1','ઽ'),(3881,1,'verify','⬇'),(3884,1,'executed','ᆑA'),(3888,1,'executed','̲'),(3890,1,'environment','ੜĈ'),(3890,1,'executed','ܹ٣͕Iʫ˛0LǪ\"\"'),(3890,1,'maintaining','๙'),(3891,1,'executed','ȳâ˗<'),(3892,1,'executed','ĥ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictB9` ENABLE KEYS */;

--
-- Table structure for table `dictBA`
--

DROP TABLE IF EXISTS `dictBA`;
CREATE TABLE `dictBA` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictBA`
--


/*!40000 ALTER TABLE `dictBA` DISABLE KEYS */;
LOCK TABLES `dictBA` WRITE;
INSERT INTO `dictBA` VALUES (3879,2,'administrator',''),(3769,1,'sh','	'),(3770,1,'sh','	'),(3771,1,'sh','	'),(3772,1,'sh','	'),(3773,1,'sh','	'),(3776,1,'pl','%\Z'),(3786,1,'pl',''),(3788,1,'pl','-'),(3790,1,'pl',''),(3792,1,'sh',')	'),(3792,1,'sqsh','A'),(3793,1,'sh','\r'),(3794,1,'sh',''),(3795,1,'sh',''),(3796,1,'sh','	'),(3797,1,'pl',''),(3800,1,'pl',''),(3801,1,'pl',''),(3802,1,'pl',''),(3803,1,'pl',''),(3804,1,'pl','\Z'),(3805,1,'pl',''),(3806,1,'pl',''),(3808,1,'pl',''),(3810,1,'pl',''),(3812,1,'sh',''),(3813,1,'pl',''),(3816,1,'pl',''),(3817,1,'pl',''),(3762,1,'pl',''),(3767,1,'sh',''),(3879,1,'pl','㣀5ઊљƔYňןÎÍ6Iß@.३,ŵķV'),(3824,1,'pl','¡'),(3819,1,'pl',''),(3871,1,'fly',';Ħ'),(3877,7,'administrator','ޏ'),(3723,1,'pl',''),(3724,1,'pl','\r'),(3730,1,'pl','\Z'),(3733,1,'pl','H'),(3735,1,'pl',''),(3740,1,'pl','*	'),(3741,1,'pl','\n'),(3742,1,'pl',''),(3743,1,'pl','\n'),(3746,1,'sh',''),(3747,1,'pl',''),(3749,1,'sh',''),(3750,1,'sh',''),(3751,1,'pl','	B'),(3751,1,'sacrificed','Ȍ'),(3752,1,'pl','>'),(3753,1,'pl',''),(3754,1,'pl','/%Ŭ1\Z%59%'),(3758,1,'sh',''),(3760,1,'pl',')'),(3722,1,'pl','í'),(3857,1,'pl',''),(3882,1,'accomplish','ń'),(3879,1,'fills',' '),(3880,1,'administrator','̴׷n\"1GË<6b-0K܆ॵᝃทÎY4	'),(3881,1,'dimensional','ඡࠎĉ'),(3879,7,'administrator','朴'),(3880,1,'dondich','ᗙ'),(3880,1,'abstraction','ྭ'),(3876,7,'administrator','Ʈ'),(3875,7,'administrator','͇'),(3879,1,'prior','ڲ㠤'),(3880,1,'common','ឯ'),(3851,1,'pl',''),(3871,1,'consumption','㉯'),(3890,1,'skew','བ'),(3890,1,'funny','ᜈ'),(3890,1,'prior','ຟɔI'),(3883,7,'administrator','ō'),(3882,7,'administrator','Ʋ'),(3889,1,'accomplish','̑'),(3881,1,'pl','ા.	'),(3881,1,'normalize','٥᭚'),(3828,1,'pl',''),(3830,1,'pl',''),(3832,1,'pl',''),(3834,1,'pl',''),(3837,1,'pl','ô'),(3840,1,'explicitly','Ȧ'),(3840,1,'sun','˱'),(3840,1,'traditional','÷'),(3841,1,'pl','\r*U'),(3842,1,'pl',''),(3843,1,'pl',''),(3844,1,'pl',''),(3845,1,'pl',''),(3847,1,'pl',''),(3850,1,'pl',''),(3826,1,'sh','\n'),(3721,1,'common','Ú'),(3853,1,'pl',''),(3856,1,'common','ç'),(3880,1,'producers','䎺'),(3881,1,'abstraction','ళ'),(3876,1,'administrator','û'),(3875,1,'pl','ŏĴ'),(3878,7,'administrator','ॆ'),(3768,1,'sh','	'),(3879,1,'br','帼'),(3880,1,'accounts','ᐲ'),(3881,1,'administrator','ɋ⤚ɇ'),(3881,1,'br','〓'),(3881,1,'common','❳ࣁ'),(3871,1,'traditional','ᛤ౥'),(3872,1,'br','І,'),(3872,1,'common','࡟'),(3872,1,'pl','ǧ'),(3872,1,'sh','ȸ'),(3872,1,'succeeds','Ύ'),(3873,1,'administrator','ɗ'),(3874,1,'administrator','^ū'),(3874,1,'pl','ó|'),(3871,1,'sun','⒐'),(3871,1,'prior','ᆺক'),(3888,1,'common','ρ'),(3858,1,'pl',''),(3821,1,'sh','	'),(3874,7,'administrator','ϱ'),(3884,1,'common','±'),(3890,1,'administrator','᱄&'),(3852,1,'sh',''),(3720,1,'charting','.'),(3859,1,'sh',''),(3860,1,'pl','\"'),(3861,1,'sh',''),(3862,1,'pl','.´'),(3864,1,'pl','K'),(3865,1,'pl',''),(3866,1,'pl','@'),(3867,1,'sh',''),(3869,1,'sh','R'),(3870,1,'traditional','Ô'),(3871,1,'12000','㗞Ҿ'),(3871,1,'br','㲷'),(3871,1,'common','㋀'),(3825,1,'pl','É'),(3879,1,'common','↧(Ⴢҋ&ق'),(3871,1,'minus','䎲'),(3820,1,'pl',''),(3879,6,'administrator','朵'),(3880,1,'prior','Д'),(3875,1,'administrator','ƙď'),(3879,1,'xhtml','垏ՠ'),(3879,1,'administrator','\rəŤɮêşēJ04ǻʦڑɽƚɽjࣤd5FLᰀƥفȴਸIșĕ÷ó'),(3879,1,'accounts','ࢷČ'),(3878,1,'administrator','ű˥6785*770'),(3884,7,'administrator','ᑦ'),(3885,7,'administrator','ঢ়'),(3886,7,'administrator','Ժ'),(3887,7,'administrator','ᄔ'),(3888,7,'administrator','ए'),(3889,7,'administrator','ӿ'),(3890,7,'administrator','ᾓ'),(3891,7,'administrator','َ'),(3892,7,'administrator','Ǆ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictBA` ENABLE KEYS */;

--
-- Table structure for table `dictBB`
--

DROP TABLE IF EXISTS `dictBB`;
CREATE TABLE `dictBB` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictBB`
--


/*!40000 ALTER TABLE `dictBB` DISABLE KEYS */;
LOCK TABLES `dictBB` WRITE;
INSERT INTO `dictBB` VALUES (3878,1,'user','ऀ'),(3879,1,'backing','ȼ惴 ć	¦'),(3871,1,'user','Ó⯯	ᎉI'),(3871,1,'realize','ㆻ'),(3871,1,'bar','ಏ%'),(3871,1,'calculates','ᘧ㓬'),(3871,1,'justify','ᴻ'),(3871,1,'newline','ᵒ'),(3777,1,'user','!'),(3881,1,'abstract','⻧'),(3795,1,'user','$'),(3797,1,'user','4'),(3799,1,'user','Ģ'),(3805,1,'user',''),(3827,1,'skips','õ'),(3827,1,'user','Ɠ'),(3832,1,'user','\''),(3837,1,'user',''),(3840,1,'user','Ƚ'),(3844,1,'user','+\''),(3851,1,'ouput','y'),(3851,1,'user','/'),(3856,1,'user','+k'),(3867,1,'sensors',''),(3871,1,'120','䕁'),(3778,1,'user','-B'),(3877,1,'user','Ü; ,	z	)2ð=ÚŽK\rĚ'),(3879,1,'section1chapter1','嵓a9'),(3880,1,'user','Ǝǭ^ɵǻòeò-Î\Z Ìĳºɓǐ9)ɑߴ࢞ಅћૣ\nIéĽ'),(3880,1,'realize','ײ'),(3880,1,'justify','㢻'),(3880,1,'bar','ݍҐᏝ̈́ᚒX\rI>\rCœZ\rMP=>6\r\\[Ź2\r343'),(3879,1,'user','ًÂì?=%#.	3\n	\n*\r#ÕæIËŶǘĊvkKɌţ˞Ā¹˜\'Ɍôᅒ෣໳ȖƄ࿎8>'),(3877,1,'securely','ͻ'),(3875,1,'imports','Ŷ'),(3881,1,'sensors','ᾃ'),(3881,1,'reduces','↉ቼ'),(3881,1,'realize','Ђ'),(3879,1,'repetitive','↻ᕳ'),(3874,1,'user','͔'),(3876,1,'user','Ɖ'),(3875,1,'user','ȡ'),(3876,1,'imports','Ø'),(3749,1,'sensors',''),(3751,1,'120','ɏ'),(3752,1,'user','»'),(3756,1,'user','XD'),(3757,1,'user','J¹'),(3763,1,'user','_'),(3773,1,'user','!'),(3774,1,'user',' '),(3775,1,'user',' '),(3776,1,'user','\'# '),(3739,6,'spop','ĥ'),(3892,1,'user',''),(3881,1,'user','ɨǸڳǿ౜ƦƋլ=˾˶hǥĮˉ0ǎ§Ã²<ɡ)Ǉ-õ\r'),(3890,1,'user','Άk8J7ǩ/ʜјЧŚঀ4'),(3890,1,'rotation','ಚ'),(3890,1,'newline','ᤠ0'),(3871,1,'originated','㇠'),(3739,1,'spop',''),(3734,1,'user','})'),(3721,1,'storeroom','Æ'),(3887,1,'versus','ཟ'),(3871,1,'versus','㗔'),(3879,1,'imports','亢'),(3879,1,'bar','᜴'),(3749,6,'sensors','5'),(3884,1,'user','ॿ'),(3879,1,'realize','ׯ'),(3791,1,'user','0'),(3793,1,'user',')'),(3790,1,'user','4'),(3782,1,'120','É'),(3781,1,'versus','\Z'),(3779,1,'user','('),(3781,1,'120','^'),(3873,1,'secured','ʳ'),(3873,1,'user','Ċ'),(3874,1,'imports','Ʀ'),(3881,1,'laststatechange','ࡌ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictBB` ENABLE KEYS */;

--
-- Table structure for table `dictBC`
--

DROP TABLE IF EXISTS `dictBC`;
CREATE TABLE `dictBC` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictBC`
--


/*!40000 ALTER TABLE `dictBC` DISABLE KEYS */;
LOCK TABLES `dictBC` WRITE;
INSERT INTO `dictBC` VALUES (3857,1,'crit','+'),(3881,1,'equipment','Ư'),(3880,1,'management','ࠚÕ¬;֟2❦'),(3884,1,'learn','Ĉ'),(3881,1,'foreach','੏+'),(3878,1,'management','آ33522220030%/'),(3879,1,'community','ә'),(3879,1,'enables','܅Ņ·SØĿং৫ᰄđԛŢҠ'),(3866,1,'crit','['),(3869,1,'encrypted','Ø'),(3871,1,'community','㺟Ҩ'),(3871,1,'equipment','㹺ʔ'),(3871,1,'graph','ï+įƯǧ٣ɑ`A!J<Ö@N7ôJ)@	6\n\"J vʴƖ^04¦@²ăǓೳ9-:0Dढ़JŇà©ܡǞ/ΉԠŒ#'),(3871,1,'learn','㏱૩ᇛ'),(3720,1,'updates','ƅ'),(3879,1,'management','ᆨͧ঵ų̧[ԣ҅೘֠ᚄ'),(3880,1,'graph','ḕD *	×̴Д๩	),	)H'),(3871,1,'incrementally','Ꮶ'),(3871,1,'interprets','␶'),(3886,1,'management','Ǣ'),(3881,1,'community','ˬ'),(3859,1,'community','4'),(3857,1,'equipment',''),(3879,1,'seek','扎'),(3879,1,'perfidstring','䨉'),(3881,1,'management','ǖ᳟ध,5VԂi'),(3880,1,'equipment','ɀن'),(3871,1,'management','㆏൝'),(3876,1,'graph','\"'),(3875,1,'graph','ę-'),(3873,1,'management',''),(3874,1,'graph','»!\r:'),(3875,1,'community','Ǧ'),(3873,1,'community','ʮ'),(3872,1,'pwd','ƺ'),(3872,1,'management','ȏ'),(3871,1,'updates','ࣚդ⻗'),(3871,1,'seek','亁'),(3871,1,'resampling','义'),(3871,1,'rader','㬳'),(3871,1,'pwd','í'),(3871,1,'parses','Ⲭ'),(3881,1,'enables','өᕖƎ'),(3880,1,'updates','ឿ٪?Q'),(3865,1,'management','$'),(3863,1,'updates','\Z'),(3880,1,'enables','ϭՌa%ÑME-૮ܔȟ͖āːഫ6Ô'),(3884,1,'parses','შ'),(3887,1,'management','Ƴ'),(3879,1,'learn','⛸'),(3879,1,'interprets','䊑'),(3890,1,'learn','̮'),(3885,1,'enables','ࢫOY>'),(3884,1,'enables','àБÙ24ªɪϴ»ëÙf'),(3880,1,'dealt','れ'),(3880,1,'community','Ӝ'),(3879,1,'graph','Ꮶ⼡Ŧ5Îļ\r#YƓs\\\nࠩ'),(3879,1,'equipment','ˡ'),(3879,1,'updates','䚲ωڞ²Β਋ǈS'),(3863,1,'management',' '),(3721,1,'management','C0'),(3871,1,'mrtgs','!'),(3890,1,'enables','Ʌ'),(3723,1,'crit','&'),(3727,1,'crit','?'),(3728,1,'crit','	'),(3732,1,'crit','	'),(3733,1,'seek','`'),(3735,1,'crit','+'),(3737,1,'crit','	'),(3739,1,'crit','	'),(3740,1,'community',':O'),(3740,1,'encrypted','Ó('),(3741,1,'community','\")'),(3742,1,'community','-'),(3742,1,'management','#'),(3743,1,'community','\"%'),(3744,1,'community','SW'),(3744,1,'crit','P'),(3745,1,'crit','/'),(3748,1,'crit','	'),(3752,1,'crit','['),(3754,1,'parses','\Z'),(3755,1,'crit','+'),(3761,1,'crit','	'),(3781,1,'crit','5'),(3781,1,'peer','*'),(3784,1,'crit','	'),(3785,1,'crit','	'),(3786,1,'community','('),(3791,1,'crit','>'),(3803,1,'crit','-'),(3805,1,'seek','F1\n-'),(3806,1,'seek',')#'),(3808,1,'crit',')'),(3811,1,'crit','0'),(3814,1,'crit','	'),(3815,1,'crit','&'),(3816,1,'crit','&'),(3816,1,'parses',''),(3818,1,'crit','	'),(3819,1,'community','0'),(3820,1,'community','1'),(3820,1,'crit','Ù'),(3822,1,'crit','A'),(3824,1,'crit','Ĉ'),(3828,1,'community','<\Z'),(3829,1,'community','Z'),(3833,1,'crit','	'),(3840,1,'crit','Q'),(3844,1,'crit','1'),(3845,1,'crit','.'),(3851,1,'crit','-'),(3852,1,'crit','\"'),(3853,1,'community','/'),(3853,1,'management',''),(3854,1,'crit','¾	'),(3855,1,'community','&'),(3857,1,'community','\''),(3890,1,'updates','ࣙM	/'),(3892,1,'parses','Ƒ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictBC` ENABLE KEYS */;

--
-- Table structure for table `dictBD`
--

DROP TABLE IF EXISTS `dictBD`;
CREATE TABLE `dictBD` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictBD`
--


/*!40000 ALTER TABLE `dictBD` DISABLE KEYS */;
LOCK TABLES `dictBD` WRITE;
INSERT INTO `dictBD` VALUES (3805,1,'arbitrary',''),(3879,1,'wrapping','ᗽ/'),(3751,1,'error','õ'),(3752,1,'returned','«'),(3781,1,'returned',''),(3751,1,'collected','ǻ'),(3744,1,'sha','¥'),(3871,1,'error','ڦ\r۴6ଃĴỠஅঢ'),(3871,1,'consists','ॺ᧷4đ'),(3871,1,'arbitrary','਩'),(3871,1,'collected','ǝ〽7ᑏ'),(3871,1,'imagination','㊈'),(3871,1,'valuable','䓔'),(3782,1,'returned','Ĩ'),(3872,1,'error','ƍaø'),(3806,1,'arbitrary',''),(3799,1,'returned','Ä'),(3820,1,'returned','Ë'),(3816,1,'returned',':\r'),(3868,1,'professional',''),(3867,1,'raid','\"'),(3862,1,'mac','<'),(3841,1,'mac','/'),(3840,1,'returned','ť'),(3827,1,'returned','ɹ\n'),(3827,1,'error','ʎ'),(3824,1,'returned',''),(3822,1,'microseconds','µ'),(3874,1,'firewall','ϗ'),(3873,1,'error','ņ'),(3871,1,'grid','༏ş&\r$>	\r	ʹ'),(3782,1,'nl','Ɔ'),(3720,1,'error','ñ'),(3871,1,'folks','⹂ঁ'),(3798,1,'owl','T'),(3873,1,'consists','kK'),(3871,1,'returned','ἥ๩'),(3798,1,'returned','6'),(3879,1,'tied','ᔫ'),(3879,1,'role','ܭÓHƇF$\r	\n\'		#@*Nǲۖ˕'),(3879,1,'consists','΅⇆'),(3879,1,'error','䣲'),(3879,1,'mac','䵆'),(3879,1,'pleasant','Ѯ'),(3879,1,'professional','·\Z፧Uʵゎĺᐊ'),(3878,1,'professional','Œߦ'),(3879,1,'coding','䏜'),(3878,1,'error','Ϭ'),(3877,1,'role','͇'),(3875,1,'error',']'),(3881,1,'error','਱ɳĞ	?@@>BDBCXBQ@AAŧ඼ኦ'),(3881,1,'consists','᷺ۻ¼֑͐'),(3880,1,'tied','৿'),(3880,1,'role','ঢǫú)\rݹ⎢'),(3880,1,'error','ᮋ'),(3880,1,'grid','ૻ'),(3880,1,'pleasant','ѱ'),(3880,1,'professional','˱\ZϭéࠏൾӎҟQ᷉'),(3880,1,'returned','㒊'),(3880,1,'collected','㑋'),(3880,1,'arbitrary','ྲ¿㌈'),(3744,1,'returned','çòE'),(3740,1,'error','ħ'),(3730,1,'returned','%'),(3725,1,'error','#'),(3720,1,'professional',''),(3721,1,'consists','Ȳ'),(3721,1,'role','¢'),(3722,1,'returned',','),(3881,1,'expose','⡔'),(3881,1,'pleasant','ʁ'),(3881,1,'professional','⁇ɤ'),(3881,1,'returned','ඦݡӇ'),(3884,1,'professional','ࢣDÙ'),(3890,1,'firewall','Ρ'),(3890,1,'professional','ᴫ'),(3890,1,'returned','រVL'),(3890,1,'transmits','ξ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictBD` ENABLE KEYS */;

--
-- Table structure for table `dictBE`
--

DROP TABLE IF EXISTS `dictBE`;
CREATE TABLE `dictBE` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictBE`
--


/*!40000 ALTER TABLE `dictBE` DISABLE KEYS */;
LOCK TABLES `dictBE` WRITE;
INSERT INTO `dictBE` VALUES (3881,1,'gethostgroups','य૆'),(3871,1,'fool','䨽'),(3871,1,'887457903','ໂ'),(3868,1,'rewarding','R'),(3850,1,'omreport','\"'),(3787,1,'libexec','~'),(3880,1,'configurations','ɉ'),(3877,1,'prompts','к'),(3877,1,'passwords','ä'),(3881,1,'passwords','˃シ'),(3879,1,'row1','幍\r'),(3879,1,'rewarding','Ѱ'),(3878,1,'snmptt','Ơ'),(3877,1,'libexec','ӭ '),(3873,1,'passwords','ʭ'),(3872,1,'pins','É'),(3872,1,'configurations','ొ'),(3881,1,'actionperformed','㈳{F'),(3880,1,'snmptt','྘'),(3880,1,'rewarding','ѳᄻ'),(3880,1,'passwords','ҳࠖ'),(3871,1,'skewing','ᬹ'),(3881,1,'inet','⏉'),(3879,1,'passwords','ҰșȞ'),(3765,1,'libexec','k'),(3881,1,'configurations','ƻ㔾ɑ'),(3879,1,'configurations','˪ᶡٚ'),(3879,1,'forces','ᨶ'),(3754,1,'libexec','Ǘ1\Z%59%'),(3881,1,'rewarding','ʃ'),(3884,1,'forces','ૡ'),(3887,1,'prompts','Ё'),(3888,1,'configurations','ƫ'),(3890,1,'passwords','οӟ'),(3892,1,'passwords','');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictBE` ENABLE KEYS */;

--
-- Table structure for table `dictBF`
--

DROP TABLE IF EXISTS `dictBF`;
CREATE TABLE `dictBF` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictBF`
--


/*!40000 ALTER TABLE `dictBF` DISABLE KEYS */;
LOCK TABLES `dictBF` WRITE;
INSERT INTO `dictBF` VALUES (3868,1,'latest','Ê'),(3824,1,'heaps','ú'),(3803,1,'sendmail','#X6'),(3763,1,'functioning','Å'),(3871,1,'odat2','ᷤ'),(3871,1,'latest','Ù̜'),(3881,1,'priority','ຢ\n\r	Ꭳ'),(3881,1,'color','⊯'),(3721,1,'latest','O'),(3721,1,'programming','ɋ'),(3754,1,'ack','Yå\nĪu'),(3759,1,'cputime',''),(3871,1,'snmpwalk','䂿Ñ'),(3871,1,'telling','Ӹ᡹'),(3871,1,'universal','ⶲ'),(3879,1,'color','⧗Ɯ'),(3878,1,'sendmail','֜'),(3873,1,'starters','tK'),(3872,1,'dialout','Ԙ'),(3879,1,'extras','ᦔ'),(3869,1,'programming','g1'),(3871,1,'color','ػसԮø׶8ʆᬇa'),(3880,1,'extremely','㑸'),(3880,1,'color','▃ᒞNDHǇRXBC<a`Ʊ8;:'),(3880,1,'sendmail','ᯊ'),(3880,1,'reviews','ᗷ'),(3880,1,'programming','Φఫ޿⹣'),(3880,1,'functioning','ᰊ'),(3881,1,'programming','⟅'),(3881,1,'simplification','Ὥ'),(3881,1,'transparently','⟌Ŋ,'),(3890,1,'latest','ᘦ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictBF` ENABLE KEYS */;

--
-- Table structure for table `dictC0`
--

DROP TABLE IF EXISTS `dictC0`;
CREATE TABLE `dictC0` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictC0`
--


/*!40000 ALTER TABLE `dictC0` DISABLE KEYS */;
LOCK TABLES `dictC0` WRITE;
INSERT INTO `dictC0` VALUES (3879,1,'mysqldump','揟ÙÝ'),(3879,1,'logmessage','晌'),(3879,1,'liststart','䟬'),(3871,1,'thought','.'),(3872,1,'sed','ǔ'),(3872,1,'uncompress','ż'),(3879,1,'initial','Ѕ⇎䀀'),(3880,1,'initial','ϔ⃛‶'),(3880,1,'measure','ࢌ'),(3880,1,'order','ኒĞ᭼ց԰cVKƹg[bKAjgƶA@A'),(3881,1,'initial','ɣ'),(3840,1,'order','ǽ'),(3840,1,'measure','ʶ'),(3766,1,'order','&'),(3721,1,'contributed','ɮ'),(3744,1,'order','Ǿ'),(3762,1,'contributed','¾'),(3871,1,'strange','⠕␗Ņ'),(3871,1,'order','Ȥ஀,p	״܃ϻ@ʮᅹᏂڣ'),(3871,1,'mileage','㒆'),(3871,1,'measure','ߙ̂	◪ĺੌôਈڵ'),(3871,1,'contributed','ሤz'),(3871,1,'gst','Ⴞ'),(3871,1,'920806800','㠷}'),(3871,1,'86400','ᆛ㇢0'),(3881,1,'logmessage','ᥒV̅ղ%\"'),(3879,1,'order','௮ŽI૥Ů8ö4௖˝ȐᘸƀɦЛԯૡ'),(3720,1,'thought',''),(3862,1,'strange','æ'),(3856,1,'order','Ŋ'),(3856,1,'banner','|'),(3869,1,'sed','Q'),(3881,1,'order','ᨡҀၑތ'),(3884,1,'order','ႋΦ'),(3885,1,'measure','ݨ'),(3886,1,'order','ò'),(3887,1,'order','Ěआ͸'),(3888,1,'measure','߸'),(3888,1,'order','ү'),(3890,1,'balance','ᐳ'),(3890,1,'initial','ཚÛǭÎ'),(3890,1,'mileage','ᐐ'),(3890,1,'order','˦ҙ'),(3890,1,'penalty','๳');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictC0` ENABLE KEYS */;

--
-- Table structure for table `dictC1`
--

DROP TABLE IF EXISTS `dictC1`;
CREATE TABLE `dictC1` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictC1`
--


/*!40000 ALTER TABLE `dictC1` DISABLE KEYS */;
LOCK TABLES `dictC1` WRITE;
INSERT INTO `dictC1` VALUES (3871,1,'drawing','Ꮀ֘Ɍ'),(3869,1,'simple','Ô'),(3840,1,'simple','ʗ'),(3827,1,'wrap','Ƨ'),(3765,1,'kernel','r'),(3880,1,'wrap','৓'),(3880,1,'wide','ޡగᨚҊ'),(3880,1,'upstream','ቾ'),(3880,1,'simple','ႍ'),(3880,1,'expertise','ቘ'),(3880,1,'drawing','ႀ'),(3751,1,'winters','*'),(3721,1,'simple','k'),(3878,1,'wide','Ý'),(3721,1,'wide','ɇ'),(3724,1,'offline','\\'),(3880,1,'dates','ổᦆ'),(3879,1,'wrap','ᔁ'),(3879,1,'simple','⚶ૃ'),(3879,1,'indexed','它'),(3871,1,'dates','᷂'),(3871,1,'increases','亅'),(3878,1,'expertise','®'),(3872,1,'simple','aĝୀ'),(3872,1,'fi','͔'),(3871,1,'wrap','䭱Ѯ'),(3871,1,'wide','ᮡ'),(3871,1,'simple','qࣩᜟᄔ'),(3871,1,'lst','Ⴢ'),(3881,1,'lamp','❜İ'),(3879,1,'expertise','≞'),(3881,1,'demo','਽%'),(3871,1,'demo','ᑝप>¸ႊ7i'),(3881,1,'getlogmessage','ᄄ'),(3879,1,'segment','㠜'),(3881,1,'propertytype','⃪'),(3881,1,'simple','᯴ȡÎB\ZăHí^ێ*ˌԃˇӖ'),(3881,1,'wrap','✵'),(3884,1,'wide','կ/ড়/'),(3885,1,'segment','ê'),(3885,1,'wide','͛1'),(3887,1,'drawing','ɰૡ'),(3887,1,'wide','֙1'),(3890,1,'dates','ᯒ'),(3890,1,'drawing','ڍ7'),(3890,1,'wide','͎');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictC1` ENABLE KEYS */;

--
-- Table structure for table `dictC2`
--

DROP TABLE IF EXISTS `dictC2`;
CREATE TABLE `dictC2` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictC2`
--


/*!40000 ALTER TABLE `dictC2` DISABLE KEYS */;
LOCK TABLES `dictC2` WRITE;
INSERT INTO `dictC2` VALUES (3879,1,'return','ẓ㵳'),(3837,1,'development',''),(3812,1,'development',''),(3754,1,'development',''),(3754,1,'return','\"Ś	\n6ó'),(3755,1,'development',''),(3755,1,'return',''),(3756,1,'development',''),(3757,1,'development',''),(3758,1,'development',''),(3775,1,'development',''),(3776,1,'development',''),(3777,1,'development',''),(3890,1,'asterisk','ЭJ7'),(3871,1,'driving','㖎'),(3867,1,'development',''),(3863,1,'return','ÅC'),(3773,1,'development',''),(3723,1,'development',''),(3871,1,'haven','䭧'),(3835,1,'return','l'),(3734,1,'development',''),(3861,1,'development',''),(3879,1,'gen','㢾5'),(3879,1,'hostgroups','哛'),(3826,1,'hw','\n'),(3871,1,'gauges','࡟'),(3854,1,'development',''),(3827,1,'return','ȃ'),(3819,1,'development',''),(3805,1,'development',''),(3806,1,'development',''),(3807,1,'development',''),(3808,1,'development',''),(3860,1,'development',''),(3832,1,'development',''),(3838,1,'development',''),(3887,1,'return','ۘ'),(3780,1,'sapentries','ĉ'),(3740,1,'development',''),(3886,1,'unique','Ѥ'),(3871,1,'circle','バ\Z'),(3871,1,'generation','ᗙ\Z'),(3880,1,'development','ʷ<¶䉍'),(3879,1,'unique','∤⯭'),(3872,1,'tested','ؔ'),(3721,1,'development','ȃ'),(3871,1,'development','Ш'),(3786,1,'development',''),(3774,1,'development',''),(3864,1,'development',''),(3830,1,'return','k'),(3781,1,'development',''),(3722,1,'return','µ'),(3828,1,'development',''),(3794,1,'development',''),(3795,1,'development',''),(3796,1,'development',''),(3797,1,'development',''),(3798,1,'development',''),(3788,1,'development',''),(3888,1,'unique','ڏ'),(3858,1,'development',''),(3824,1,'development',''),(3879,1,'5x','ĂۛᄉÇ'),(3866,1,'development',''),(3869,1,'development','¸'),(3826,1,'development',''),(3880,1,'growing','ṋ'),(3721,7,'docs','ʝ'),(3772,1,'development',''),(3846,1,'development',''),(3815,1,'development',''),(3737,1,'development',''),(3885,1,'return','җ'),(3886,1,'hostgroups','ʟ\n'),(3880,1,'unique','ᐴ'),(3850,6,'hw','T'),(3784,1,'development',''),(3789,1,'development',''),(3880,1,'purchasing','㢱'),(3862,1,'tested','¬'),(3863,1,'development',''),(3855,1,'development',''),(3750,1,'development',''),(3751,1,'development',''),(3751,1,'hw','KǦ'),(3853,1,'development',''),(3827,1,'development',''),(3817,1,'development',''),(3818,1,'development',''),(3799,1,'development',''),(3800,1,'development',''),(3801,1,'development',''),(3802,1,'development',''),(3803,1,'development',''),(3804,1,'development',''),(3890,1,'ajax','ẘ'),(3884,1,'parallelized','͠	৪	'),(3859,1,'development',''),(3831,1,'development',''),(3826,6,'hw','0'),(3837,1,'unique',':'),(3887,1,'obsessed','ٝ'),(3780,1,'development',''),(3738,1,'development',''),(3739,1,'development',''),(3870,1,'development','!'),(3880,1,'ajax','ី'),(3879,1,'tested','☇'),(3872,1,'term','ͻ'),(3879,1,'development','͍<'),(3720,1,'development',''),(3871,1,'daylight','ᦪ'),(3785,1,'development',''),(3879,1,'generation','ͭ䖋'),(3881,1,'hostgroups','ᑠ'),(3830,1,'development',''),(3780,1,'term',''),(3721,1,'generation','1'),(3722,1,'development',''),(3790,1,'development',''),(3791,1,'development',''),(3792,1,'development',''),(3793,1,'development',''),(3879,1,'docs','嚐ƚ[ɴµó'),(3813,1,'development',''),(3873,1,'return','ŉ'),(3787,1,'return','8\r'),(3787,1,'development',''),(3887,1,'unique','৹'),(3857,1,'development',''),(3856,1,'development',''),(3881,1,'ajax','⚏Ƴ'),(3823,1,'development',''),(3820,1,'development',''),(3821,1,'development',''),(3822,1,'development',''),(3878,1,'tested','å'),(3865,1,'development',''),(3825,1,'development',''),(3880,1,'generation','˗'),(3720,7,'docs','Ƽ'),(3769,1,'development',''),(3770,1,'development',''),(3771,1,'development',''),(3814,1,'development',''),(3862,1,'development',''),(3736,1,'development',''),(3885,1,'obsessed','П'),(3835,1,'development',''),(3833,1,'development',''),(3834,1,'development',''),(3844,1,'tested','F'),(3844,1,'development',''),(3843,1,'development',''),(3842,1,'development',''),(3841,1,'development',''),(3840,1,'return',''),(3840,1,'development',''),(3839,1,'development',''),(3838,1,'return',''),(3723,7,'docs','E'),(3722,7,'docs','Ť'),(3881,1,'actionevent','㋠\n'),(3845,1,'development',''),(3813,1,'return','2'),(3881,1,'return','া˫ôzB#%\r\r\r\rm&\"ÄE!è0µl0y/lñᤎ'),(3881,1,'myusername','㢾\\'),(3881,1,'middle','Ⅺ'),(3751,6,'hw','ʶ'),(3891,1,'unique','ϓ'),(3890,1,'tested','ᐌ'),(3890,1,'term','๛Ȏ'),(3890,1,'return','ʗᚆ5'),(3890,1,'parallelized','Ꮥ'),(3768,1,'development',''),(3767,1,'development',''),(3766,1,'dpu','r'),(3766,1,'development',''),(3765,1,'development',''),(3764,1,'development',''),(3763,1,'development',''),(3760,1,'development',''),(3761,1,'development',''),(3762,1,'development',''),(3759,1,'development',''),(3759,1,'return','(\n\n'),(3881,1,'docs','▀'),(3881,1,'development','୅჊஭Ȫ'),(3881,1,'unique','ܤ♶ࡤ'),(3890,1,'docs','ᄘIԸJá'),(3852,1,'development',''),(3851,1,'development',''),(3850,1,'hw',''),(3850,1,'development',''),(3849,1,'development',''),(3848,1,'development',''),(3847,1,'development',''),(3829,1,'development',''),(3889,1,'docs','ӧ'),(3752,1,'development',''),(3868,1,'development','¡\r\n'),(3816,1,'development',''),(3778,1,'development',''),(3779,1,'development',''),(3871,1,'h2','⽩\Z'),(3735,1,'development',''),(3836,1,'development',''),(3811,1,'development',''),(3809,1,'development',''),(3810,1,'development',''),(3871,1,'return','ᵧљߓ༈ͷ\Z'),(3871,1,'modulo','៖'),(3752,1,'return','*	'),(3753,1,'development',''),(3880,1,'term','ध⺪ä'),(3872,1,'return','˖b9åٍŎØ'),(3880,1,'return','ඪ⥪၃£'),(3782,1,'development',''),(3782,1,'return',''),(3783,1,'development',''),(3783,1,'return','³'),(3741,1,'development',''),(3742,1,'development',''),(3743,1,'development',''),(3744,1,'development',''),(3744,1,'return','ģ'),(3745,1,'development',''),(3745,1,'return','Ø'),(3746,1,'development',''),(3747,1,'development',''),(3748,1,'development',''),(3749,1,'development',''),(3724,1,'development',''),(3725,1,'development',''),(3726,1,'development',''),(3726,1,'return','/'),(3727,1,'development',''),(3727,1,'return','·'),(3728,1,'development',''),(3729,1,'development',''),(3729,1,'return','¿'),(3730,1,'development',''),(3731,1,'development',''),(3732,1,'development',''),(3733,1,'development',''),(3724,7,'docs','h'),(3725,7,'docs','b'),(3726,7,'docs',''),(3727,7,'docs','ł'),(3728,7,'docs','Ĝ'),(3729,7,'docs','Õ'),(3730,7,'docs',''),(3731,7,'docs',''),(3732,7,'docs','ĝ'),(3733,7,'docs',''),(3734,7,'docs','þ'),(3735,7,'docs','x'),(3736,7,'docs','¾'),(3737,7,'docs','Ĝ'),(3738,7,'docs',''),(3739,7,'docs','Ĝ'),(3740,7,'docs','Ɖ'),(3741,7,'docs','»'),(3742,7,'docs',''),(3743,7,'docs',''),(3744,7,'docs','ɝ'),(3745,7,'docs','ý'),(3746,7,'docs','4'),(3747,7,'docs','\\'),(3748,7,'docs','ğ'),(3749,7,'docs',','),(3750,7,'docs','7'),(3751,7,'docs','ʬ'),(3752,7,'docs','Đ'),(3753,7,'docs',''),(3754,7,'docs','̐'),(3755,7,'docs','À'),(3756,7,'docs','ö'),(3757,7,'docs','ƨ'),(3758,7,'docs','C'),(3759,7,'docs','ÿ'),(3760,7,'docs','D'),(3761,7,'docs','ğ'),(3762,7,'docs','à'),(3763,7,'docs','ĺ'),(3764,7,'docs','?'),(3765,7,'docs','ß'),(3766,7,'docs','ò'),(3767,7,'docs',')'),(3768,7,'docs','*'),(3769,7,'docs','\''),(3770,7,'docs','\''),(3771,7,'docs','\''),(3772,7,'docs','\''),(3773,7,'docs','+'),(3774,7,'docs',','),(3775,7,'docs','*'),(3776,7,'docs','Ĩ'),(3777,7,'docs','+'),(3778,7,'docs','ù'),(3779,7,'docs','5'),(3780,7,'docs','ư'),(3781,7,'docs',''),(3782,7,'docs','Ɨ'),(3783,7,'docs','Ø'),(3784,7,'docs','ğ'),(3785,7,'docs','ğ'),(3786,7,'docs','@'),(3787,7,'docs','«'),(3788,7,'docs','Ý'),(3789,7,'docs',''),(3790,7,'docs','T'),(3791,7,'docs','±'),(3792,7,'docs','e'),(3793,7,'docs','©'),(3794,7,'docs','('),(3795,7,'docs',''),(3796,7,'docs','*'),(3797,7,'docs','m'),(3798,7,'docs','ñ'),(3799,7,'docs','Ɛ'),(3800,7,'docs','F'),(3801,7,'docs','7'),(3802,7,'docs','='),(3803,7,'docs','ß'),(3804,7,'docs','¶'),(3805,7,'docs','Î'),(3806,7,'docs','s'),(3807,7,'docs','B'),(3808,7,'docs','{'),(3809,7,'docs','e'),(3810,7,'docs','/'),(3811,7,'docs','Ç'),(3812,7,'docs','4'),(3813,7,'docs',''),(3814,7,'docs','ğ'),(3815,7,'docs','m'),(3816,7,'docs','S'),(3817,7,'docs','0'),(3818,7,'docs','ğ'),(3819,7,'docs','ġ'),(3820,7,'docs','Ŏ'),(3821,7,'docs','<'),(3822,7,'docs','Ā'),(3823,7,'docs','Í'),(3824,7,'docs','Ř'),(3825,7,'docs','ń'),(3826,7,'docs','\''),(3827,7,'docs','˖'),(3828,7,'docs',''),(3829,7,'docs','x'),(3830,7,'docs',''),(3831,7,'docs','Ò'),(3832,7,'docs','O'),(3833,7,'docs','ğ'),(3834,7,'docs','º'),(3835,7,'docs',''),(3836,7,'docs','d'),(3837,7,'docs','Ħ'),(3838,7,'docs','C'),(3839,7,'docs','5'),(3840,7,'docs','̙'),(3841,7,'docs','®'),(3842,7,'docs','>'),(3843,7,'docs','j'),(3844,7,'docs','Ö'),(3845,7,'docs','z'),(3846,7,'docs','Ĺ'),(3847,7,'docs','U'),(3848,7,'docs','°'),(3849,7,'docs','|'),(3850,7,'docs','J'),(3851,7,'docs',''),(3852,7,'docs','+'),(3853,7,'docs','S'),(3854,7,'docs','Ń'),(3855,7,'docs',':'),(3856,7,'docs','Ɛ'),(3857,7,'docs','_'),(3858,7,'docs','4'),(3859,7,'docs','B'),(3860,7,'docs','G'),(3861,7,'docs','2'),(3862,7,'docs','Ě'),(3863,7,'docs','ļ'),(3864,7,'docs',''),(3865,7,'docs','¡'),(3866,7,'docs','·'),(3867,7,'docs','0'),(3868,7,'docs','Ė'),(3869,7,'docs','ę'),(3870,7,'docs','ģ'),(3871,7,'docs','僖'),(3872,7,'docs','೯'),(3873,7,'docs','գ'),(3874,7,'docs','ϭ'),(3875,7,'docs','̓'),(3876,7,'docs','ƪ'),(3877,7,'docs','ދ'),(3878,7,'docs','ू'),(3879,7,'docs','朰'),(3880,7,'docs','䠾'),(3881,7,'docs','㦾'),(3882,7,'docs','Ʈ'),(3883,7,'docs','ŉ'),(3884,7,'docs','ᑢ'),(3885,7,'docs','৙'),(3886,7,'docs','Զ'),(3887,7,'docs','ᄐ'),(3888,7,'docs','ऋ'),(3889,7,'docs','ӻ'),(3890,7,'docs','ᾏ'),(3891,7,'docs','ي'),(3892,7,'docs','ǀ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictC2` ENABLE KEYS */;

--
-- Table structure for table `dictC3`
--

DROP TABLE IF EXISTS `dictC3`;
CREATE TABLE `dictC3` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictC3`
--


/*!40000 ALTER TABLE `dictC3` DISABLE KEYS */;
LOCK TABLES `dictC3` WRITE;
INSERT INTO `dictC3` VALUES (3799,1,'errors','²\\'),(3805,1,'exp','\r\Z'),(3819,1,'usefull','ā'),(3820,1,'usefull','ó'),(3827,1,'errors','ȍ'),(3837,1,'errors','l'),(3721,1,'big','Å'),(3729,1,'errors','É'),(3745,1,'errors','â'),(3751,1,'errors','æ'),(3871,1,'big','ᜧ⸧'),(3846,1,'errors','ē'),(3880,1,'drop','ᢂో\nƋnW²6േʆ'),(3871,1,'exp','៴'),(3879,1,'images','฻⩮▋'),(3879,1,'errors','Ṵᇥ✯'),(3879,1,'drop','ଳôǖޒɑ0ᄌΏĦÊÿॳ፭ UᘭÃÝ'),(3875,1,'errors','ȯ'),(3875,1,'drop','ʸ'),(3872,1,'quiet','α'),(3880,1,'depth','क໔ʝ'),(3879,1,'mirroring','⃲'),(3879,1,'independent','်'),(3755,1,'errors','¥'),(3752,1,'exp','W'),(3751,1,'trendcitical','ƚ'),(3871,1,'pushed','ᜠ*ù\n¬ΰ'),(3871,1,'images','㉊'),(3872,1,'errors','ƚ'),(3871,1,'wright','⦼'),(3871,1,'errors','Ź䰠'),(3763,1,'big','d'),(3720,1,'images','a'),(3880,1,'grey','ᦅE'),(3880,1,'independent','݂೩༺ក̐'),(3881,1,'diagram','ھ'),(3881,1,'drop','૨'),(3881,1,'hellowworldapp','ⰾ'),(3882,1,'drop','Ŷ'),(3884,1,'drop','۰йࡗ2'),(3886,1,'drop','LŨMJ¥*'),(3887,1,'drop','ғӳŘX/άÀ\''),(3887,1,'images','ಗsE1'),(3889,1,'drop','fȦ¹Į'),(3890,1,'depth','ڦ'),(3890,1,'drop','ߜᛲ'),(3890,1,'images','ǢѴ'),(3891,1,'drop','X̂¾'),(3892,1,'drop','c');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictC3` ENABLE KEYS */;

--
-- Table structure for table `dictC4`
--

DROP TABLE IF EXISTS `dictC4`;
CREATE TABLE `dictC4` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictC4`
--


/*!40000 ALTER TABLE `dictC4` DISABLE KEYS */;
LOCK TABLES `dictC4` WRITE;
INSERT INTO `dictC4` VALUES (3879,1,'simplifies','⃀'),(3879,1,'program','4䗍Ɩࢬ]p±6¨8w௑-J8'),(3887,1,'program','Էa1$'),(3752,1,'hostaddress','Ú'),(3753,1,'program','2'),(3754,1,'code','Ƭ'),(3762,1,'statistics','\Z'),(3765,1,'program','K'),(3776,1,'allocate','ĉ'),(3776,1,'sessions','b	\n	\n'),(3778,1,'01017','e'),(3780,1,'ltch','ğ'),(3783,1,'code','´'),(3791,1,'program',''),(3793,1,'program',''),(3799,1,'code','ş'),(3800,1,'statistics',''),(3817,1,'statistics',''),(3822,1,'statistics','%'),(3823,1,'program',' '),(3832,1,'program',' '),(3840,1,'code','ƹ'),(3840,1,'discusses','é'),(3840,1,'program','\ZƤ	'),(3843,1,'program','$'),(3846,1,'icritical',''),(3856,1,'program','Ĭ'),(3858,1,'sessions','\"'),(3862,1,'program',''),(3871,1,'allocate','Ӧ'),(3871,1,'code','㵪ֽ'),(3871,1,'frontends','zZ'),(3871,1,'program','½⮼ٛΣەŭčA]I'),(3871,1,'statistics','め'),(3872,1,'code','֣4'),(3872,1,'hostaddress','ࠎ'),(3872,1,'program','˷ҏ'),(3873,1,'hostaddress','˧!\"!!\'\'%%%*\'*\'(%'),(3874,1,'hostaddress','«\" ǻ'),(3875,1,'hostaddress','²*'),(3875,1,'statistics','_'),(3876,1,'hostaddress',''),(3877,1,'09','ش'),(3877,1,'program','\Z4'),(3878,1,'program','4'),(3878,1,'statistics','Ϯ'),(3879,1,'hostaddress','⡗ੲ'),(3740,1,'program','ŏ'),(3743,1,'statistics','y'),(3885,1,'program','˹a1$'),(3741,1,'statistics','.'),(3890,1,'program','ɕ9ǐҋÿ\\`aɯ'),(3890,1,'hostaddress','ܫ'),(3890,1,'code','Ɗ'),(3887,1,'hostaddress','ū૷'),(3890,1,'statistics','๟Ȍ'),(3823,6,'program','×'),(3880,1,'indicator','㥖'),(3880,1,'program','4ፈƝፂҢGƯʔၐ)'),(3880,1,'statistics','ᨮD?Ʈ\r<͢6ąHCЧgᇙ'),(3881,1,'code','਌ʙĿᐄޟ>8c×ƕû#'),(3881,1,'discusses','㑦'),(3881,1,'hosted','Ⲵ'),(3881,1,'program','4ѐϻȠ⩐'),(3881,1,'servicedescription','ࢂŜ౧šગG'),(3884,1,'code','ਸ'),(3884,1,'program','ծ/G3ͪײ/G3'),(3879,1,'superuser','࡯Țo'),(3721,1,'code','Ŕ2'),(3720,1,'code','ğ'),(3886,1,'hostaddress','Ń');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictC4` ENABLE KEYS */;

--
-- Table structure for table `dictC5`
--

DROP TABLE IF EXISTS `dictC5`;
CREATE TABLE `dictC5` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictC5`
--


/*!40000 ALTER TABLE `dictC5` DISABLE KEYS */;
LOCK TABLES `dictC5` WRITE;
INSERT INTO `dictC5` VALUES (3871,1,'tree','㻵ĺ'),(3878,1,'proxy','ص002////--0%,'),(3877,1,'read','Ƒț'),(3879,1,'tree','ࢰ8Ô±6lƍʦţHDጺ؀ƜBů#ʮ٣̷țǆ\\'),(3877,1,'automatically','Ͽ'),(3875,1,'read','ǥ'),(3875,1,'automatically','ɭ'),(3873,1,'proxy','ſfO\'P'),(3871,1,'wrapped','䯖'),(3881,1,'percentstatechange','ࡘ '),(3886,1,'tree','̍.ǐ'),(3881,1,'automatically','⁖'),(3879,1,'read','䨠7Νê˯#pħ\nFીƐm}#'),(3879,1,'headings','埰'),(3879,1,'parsing','䔕OOƛń'),(3880,1,'tree','Ė׻ܱ6ޙdáã\'@Ħ5ࠜȑ	*¥0.`L7த^`ዲ\n'),(3880,1,'subnet','ቨ'),(3880,1,'read','ᖴ⳹'),(3880,1,'sendmaili','ᮃ'),(3880,1,'determined','ጀࡇ¢'),(3880,1,'automatically','⦙ॕЊ'),(3879,1,'wrapped','ᠮ5'),(3879,1,'turning','ⶕɻ'),(3885,1,'automatically','Ѩ'),(3884,1,'tree','٠Ķ'),(3884,1,'automatically','Ϯ਋'),(3881,1,'transparent','➭Ƴ'),(3881,1,'read','ẼĎ'),(3879,1,'experienced','๑ሡ'),(3879,1,'automatically','ࢀ∜౗Ꮡᓓ_ȸ'),(3720,1,'tree','ƚ'),(3722,1,'controller','Ï.'),(3733,1,'read','!'),(3740,1,'read',''),(3740,1,'tree','¥'),(3744,1,'parsing','æ'),(3747,1,'parsing',','),(3756,1,'determined','Á'),(3763,1,'postmaster','Ï'),(3787,1,'wrapped','0\r\n'),(3791,1,'read','d'),(3798,1,'read',''),(3819,1,'read','2'),(3820,1,'read','3'),(3824,1,'subnet','P'),(3825,1,'subnet','I'),(3853,1,'read','='),(3856,1,'proxy','ķ'),(3862,1,'subnet','7'),(3865,1,'pct',''),(3867,1,'controller','#'),(3868,1,'read','X'),(3869,1,'mission',''),(3871,1,'978300900','䣇'),(3871,1,'automatically','ϪĮB࿺ĮؙᏩ'),(3871,1,'gray','㧪'),(3871,1,'if1','Ⱜ'),(3871,1,'parsing','ຊᇡ'),(3871,1,'read','ࢱˈĨጘٶੇƲ`ʊž¬͕ʹ ƞůչįݟt'),(3871,1,'surprising','▄'),(3871,1,'transparent','ᒧ'),(3887,1,'automatically','˖ϑ߀'),(3887,1,'tree','̳*ۧ&'),(3888,1,'proxy','֎@T%UMhs\rg'),(3888,1,'read','Ҩ'),(3888,1,'tree','ǱH'),(3889,1,'tree',';Ʉ	\"	\nV(62\r5:'),(3890,1,'automatically','әvIAQࡢ͜Ś¨Y?ѭ5'),(3890,1,'determined','٣ˀ஦֜>'),(3890,1,'read','ĩǩզ6טɷIѡ¤Já'),(3890,1,'tree','ک3'),(3891,1,'read','ưÊɉC');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictC5` ENABLE KEYS */;

--
-- Table structure for table `dictC6`
--

DROP TABLE IF EXISTS `dictC6`;
CREATE TABLE `dictC6` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictC6`
--


/*!40000 ALTER TABLE `dictC6` DISABLE KEYS */;
LOCK TABLES `dictC6` WRITE;
INSERT INTO `dictC6` VALUES (3871,1,'4294967196','䴓'),(3862,1,'header','á'),(3852,1,'netstat',''),(3841,1,'sample','F'),(3840,1,'recommends','ɒ'),(3823,1,'runs','>'),(3827,1,'header','»8\n'),(3757,1,'parent','Ú'),(3880,1,'runs','໕Ž'),(3881,1,'fires','㋗'),(3881,1,'header','㠻'),(3881,1,'sample','ࣔĬ᚝ಠࠨ'),(3881,1,'runs','Ṣ'),(3721,1,'92','ú'),(3744,1,'delimiter','fws'),(3871,1,'sample','੷1vใ3&łχ઱ᢅ'),(3872,1,'folder','ࠬ'),(3871,1,'header','Ӯ᭢ࠫ'),(3879,1,'168','䵉'),(3873,1,'168','˟!\"!!%\'%%%)()((&'),(3879,1,'sample','Ṽ⬛ΦऌƑʌƜË'),(3879,1,'parent','⏽ቻų($̙ᴓÑ$i'),(3879,1,'runs','ㇵ'),(3880,1,'parent','์'),(3880,1,'duration','⣪ӣƍ'),(3880,1,'expanding','ᷙᖐ'),(3880,1,'folder','䓳Ņ'),(3881,1,'parent','ጋᬹƔŀؙ'),(3879,1,'header','ᨱ\'!㉄̛'),(3879,1,'delimiter','⥉'),(3881,1,'sock','⏔'),(3884,1,'parent','ߌ+ख़'),(3885,1,'folder','c'),(3885,1,'parent','࢓'),(3886,1,'parent','ɺ	'),(3887,1,'parent','MƤ	࣑	\nԨ'),(3888,1,'folder','ǉ\\pf \r	'),(3888,1,'parent','ƅ]ǺĿ'),(3888,1,'runs','ϟ'),(3888,1,'sample','ʽ'),(3889,1,'hasn','͉'),(3890,1,'folder','Đ	'),(3890,1,'messing','͒');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictC6` ENABLE KEYS */;

--
-- Table structure for table `dictC7`
--

DROP TABLE IF EXISTS `dictC7`;
CREATE TABLE `dictC7` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictC7`
--


/*!40000 ALTER TABLE `dictC7` DISABLE KEYS */;
LOCK TABLES `dictC7` WRITE;
INSERT INTO `dictC7` VALUES (3880,1,'set','ʭМڙϣʛ׼в\ZưԆႭe\Z#8F஻ª'),(3825,1,'debugging','Ķ'),(3722,1,'debugging','Ŗ'),(3808,1,'debugging','q'),(3803,1,'debugging',''),(3803,1,'set','Ï'),(3804,1,'debugging',''),(3755,1,'debugging',''),(3753,1,'versions','^'),(3751,1,'set','Å$'),(3751,1,'initially','Ǻ'),(3737,1,'debugging','ď'),(3738,1,'debugging','u'),(3739,1,'debugging','ď'),(3740,1,'debugging','ă'),(3740,1,'set','_a*B'),(3741,1,'debugging','7'),(3743,1,'debugging','3'),(3744,1,'debugging','ƀ'),(3744,1,'label','_ï'),(3745,1,'debugging','Ð'),(3748,1,'debugging','Ē'),(3751,1,'debugging','ď'),(3871,1,'singular','ⓓ'),(3881,1,'definition','⵨|ݣǂŞ'),(3824,1,'debugging','Ŋ'),(3787,1,'definition','V'),(3872,1,'definition','שknbĢv(Ƭ/Ķ4'),(3811,1,'debugging','º'),(3810,1,'lmmon',''),(3877,1,'versions','Ի'),(3883,1,'definition','ŀ'),(3881,1,'getlogperformancedata','ၧ'),(3833,1,'debugging','Ē'),(3827,1,'debugging','ǯ'),(3848,1,'set','v'),(3848,1,'debugging','£'),(3847,1,'critlevel','2'),(3846,1,'debugging','Ĭ'),(3845,1,'debugging','p'),(3840,1,'versions','ũ'),(3843,1,'debugging','`'),(3872,1,'set','ɿ\n2ԸΨ'),(3880,1,'versions','ݹ'),(3880,1,'launched','⋖'),(3776,1,'connectstring','+/'),(3763,1,'debugging',''),(3873,1,'initially','K'),(3873,1,'definition','ː!\"!@A\')()(\''),(3890,1,'versions','ቹǦ9R'),(3890,1,'unit','Ꮾ'),(3890,1,'rotate','ಧ\r'),(3879,1,'definition','ᵦҏ\Z\\^ӵڭ˗ƹŏƛÛϷ@Ľ©ٳ-ॊo࠮Ƨ%>D<pÕwŉ'),(3871,1,'versions','ᘏ᳀'),(3871,1,'unit','དྷՠֵ$	Ƚ'),(3881,1,'versions','㓃'),(3878,1,'versions','œ'),(3878,1,'set','̶'),(3876,1,'definition','kw{'),(3761,1,'debugging','Ē'),(3757,1,'set',''),(3720,1,'unit','o'),(3720,1,'debugging','ç'),(3875,1,'set','Eƈ'),(3785,1,'debugging','Ē'),(3784,1,'debugging','Ē'),(3879,1,'launched','ᛳ'),(3879,1,'label','䒣­ʚƮ'),(3879,1,'set','̓Ծďʳٗ\Z8r\Z1*׍҄ĒçԈ$>¹ǲ?ϠͻƢ˙ӢʗرыÊ³ĖɄň^ҧN|ËQઃūx\ZWá'),(3834,1,'debugging',''),(3721,1,'versions','ô'),(3721,1,'set','ǊB'),(3871,1,'label','Ĳ෢ŋ)MI$9Ƶ✮$¼»'),(3891,1,'set','Ū'),(3891,1,'label','Ϛ'),(3874,1,'set','ȔĶ'),(3874,1,'definition','ġ'),(3890,1,'definition','ᄀ>փ2t!+!GGŚ>'),(3880,1,'definition','ᆲư7ހմ್Ů̡'),(3879,1,'versions','ษ½?ୋ⣍i'),(3892,1,'definition','ƕ'),(3810,6,'lmmon','8'),(3818,1,'debugging','Ē'),(3815,1,'debugging','c'),(3854,1,'debugging','Ķ'),(3890,1,'initially','যprr՜Ō'),(3890,1,'set','෢ë1\n\n\nH,ƶц'),(3871,1,'set','ȢEϷb\'ȟ1ȇȂ»ǖǏ9ťĻlTʼ#Ѽő੖≎'),(3871,1,'realspeed','㫎'),(3889,1,'set','ÞẼ'),(3877,1,'openssl','̑'),(3876,1,'set','į)'),(3814,1,'debugging','Ē'),(3811,1,'unit','n'),(3800,1,'critlevel','%'),(3799,1,'set','{'),(3797,1,'debugging','_'),(3792,1,'versions','!'),(3788,1,'debugging',''),(3889,1,'definition','Ұ'),(3888,1,'label','ަ*C'),(3888,1,'set','uƎÅìȉĆ[ #,)'),(3887,1,'set','֏1×Èēؑ'),(3887,1,'label','਀'),(3887,1,'definition','Ž'),(3886,1,'set','Ɗiʊ,'),(3884,1,'set','zX˻_Ĩ/ġǳԪ_Ĩ/'),(3885,1,'set','͑1ÖÆē'),(3886,1,'definition','ŕ'),(3884,1,'definition','ٞǄࢅB'),(3881,1,'set','ڍؕൎЙ҅FຓŰѳ'),(3778,1,'set','»'),(3875,1,'definition','ç'),(3871,1,'definition','ᅐ⟽'),(3879,1,'openssl','ᥳ'),(3834,1,'set','1'),(3835,1,'debugging',';'),(3837,1,'set','ò'),(3840,1,'debugging','Ő'),(3879,1,'debugging','䄓'),(3782,1,'versions',''),(3782,1,'label','ũ'),(3877,1,'set','Ǟþnǂm'),(3824,1,'set','õ'),(3799,1,'label',';`'),(3873,1,'versions','Ʌ'),(3881,1,'label','ㅆ'),(3724,1,'set','X'),(3725,1,'set','A'),(3727,1,'debugging',''),(3728,1,'debugging','ď'),(3729,1,'debugging','ª'),(3732,1,'debugging','Đ'),(3735,1,'debugging','n');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictC7` ENABLE KEYS */;

--
-- Table structure for table `dictC8`
--

DROP TABLE IF EXISTS `dictC8`;
CREATE TABLE `dictC8` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictC8`
--


/*!40000 ALTER TABLE `dictC8` DISABLE KEYS */;
LOCK TABLES `dictC8` WRITE;
INSERT INTO `dictC8` VALUES (3883,1,'saturday','Ó$'),(3883,1,'day','è8'),(3879,1,'day','⺽8♀	4'),(3879,1,'checkbox','㪺'),(3879,1,'authorization','匪'),(3871,1,'grow','Ւ∻,ॴᏎ'),(3871,1,'totally','䘈'),(3879,1,'action','䍝'),(3819,1,'md5','â'),(3720,1,'checkbox','ŀW'),(3879,1,'saturday','⺨$'),(3824,1,'fit','Ă'),(3871,1,'fit','ঙ➹'),(3871,1,'explain','㱌'),(3871,1,'day','ା\n³\rΆñ@ၫ¢Ď	.ôcŏȍoÍγऌ୉(b¥\n!ůþÜʦ'),(3871,1,'collect','㙞ߋǲ˽ੳÎ'),(3871,1,'acquired','෵y'),(3827,1,'authorization','ž'),(3827,1,'serve','ɇ'),(3840,1,'serve','ĵ'),(3820,1,'md5',''),(3881,1,'authorization','㏫'),(3881,1,'action','㉧	(âR'),(3880,1,'serve','㟥'),(3880,1,'day','䋂'),(3880,1,'authorization','Ꮷ'),(3880,1,'action','㒲'),(3881,1,'nagioslog','ἆ'),(3881,1,'distributes','⪰'),(3881,1,'conditions','⎟'),(3881,1,'collect',' ż'),(3803,1,'postfix','}'),(3784,1,'nntps',''),(3782,1,'cpuload',''),(3751,1,'conditions','ƛ'),(3744,1,'md5','¤'),(3720,1,'fit','²'),(3879,1,'resides','፲ၵኵ'),(3885,1,'checkbox','ʷ'),(3885,1,'fit','ޘ'),(3886,1,'checkbox','ƞ'),(3890,1,'authorization','̱ìJ'),(3890,1,'day','ೃ\r'),(3890,1,'resides','ْ¦'),(3890,1,'saturday','೉'),(3891,1,'checkbox','Ż'),(3784,6,'nntps','Ĩ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictC8` ENABLE KEYS */;

--
-- Table structure for table `dictC9`
--

DROP TABLE IF EXISTS `dictC9`;
CREATE TABLE `dictC9` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictC9`
--


/*!40000 ALTER TABLE `dictC9` DISABLE KEYS */;
LOCK TABLES `dictC9` WRITE;
INSERT INTO `dictC9` VALUES (3871,1,'math','డⷯೋ'),(3840,1,'hostnames','ʞ'),(3828,1,'filesystems','P'),(3822,1,'plain','»'),(3872,1,'snpp','EĊ͘ě*Ý΁Ȋ'),(3780,1,'vpf','­ò'),(3807,1,'oldlog','%'),(3720,1,'integrated','>'),(3828,1,'vg00','g'),(3831,1,'game','	\n-'),(3871,1,'horizontal','᭧ẳ'),(3871,1,'filled','܎ᓌ̟'),(3871,1,'32bit','࠶'),(3871,1,'19','߃'),(3846,1,'filesystems','Ô'),(3879,1,'td','崝\ZF\n¥\n\n'),(3879,1,'noc','ܚ'),(3879,1,'integrated','Μ᳆ᖅ'),(3879,1,'genrsa','ᥴ'),(3872,1,'sighup','ϟ'),(3872,1,'contacted','࡬'),(3871,1,'thermometer','䷖'),(3871,1,'sum','Ἕഫ'),(3871,1,'plain','ː'),(3879,1,'contacted','␘ᣐ©'),(3879,1,'concepts','ῌǑ'),(3878,1,'integrated','ì'),(3878,1,'19','΃'),(3756,1,'radiusclient','±'),(3751,1,'5min','ɋ'),(3880,1,'32bit','࠱'),(3880,1,'brings','ނ'),(3880,1,'concepts','ºú%ಮ¦Ñ\n'),(3880,1,'contacted','ᇻ'),(3880,1,'integrated','̆ϥޣ¹۸ݗौK'),(3880,1,'snpp','䙹'),(3881,1,'filled','㋑'),(3881,1,'integrated','ҕǳH\r╁'),(3881,1,'intent','ع'),(3881,1,'timewarning','࡯'),(3884,1,'filled','܋'),(3884,1,'integrated','ࣞ'),(3890,1,'killing','ᙕ'),(3890,1,'plain','ᤢ5'),(3831,6,'game','Û');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictC9` ENABLE KEYS */;

--
-- Table structure for table `dictCA`
--

DROP TABLE IF EXISTS `dictCA`;
CREATE TABLE `dictCA` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictCA`
--


/*!40000 ALTER TABLE `dictCA` DISABLE KEYS */;
LOCK TABLES `dictCA` WRITE;
INSERT INTO `dictCA` VALUES (3757,1,'argument','R¾'),(3880,1,'upper','ඞ'),(3870,1,'access','ý'),(3871,1,'1020615300','Ⰽ'),(3871,1,'1824421548e','⃐'),(3871,1,'36893488143124135934','䵗'),(3871,1,'access','óⱣƒ'),(3873,1,'access','ƽ'),(3872,1,'argument','˫'),(3872,1,'access','Á'),(3880,1,'argument','ᬲ'),(3873,1,'argument','ʀ'),(3782,1,'requests','½'),(3877,1,'196','ؗ'),(3880,1,'averages','ड़ᑪ'),(3879,1,'access','ю2#_Ȍ!ƞĞ|5Ų6m_I6ғRTnG\'։	\Z\'\"״ѱᒩ᠋İ\nΜЍ'),(3871,1,'weekday','⒁'),(3871,1,'upper','ཙքS㨿#'),(3871,1,'marked','પ'),(3871,1,'english','㘕'),(3871,1,'biggest','䚗'),(3871,1,'averages','⊆Mፙø\n൝E)Úî'),(3881,1,'access','ǿj*#_Ɔ3ࢌ௳ʩ]Ϫךŝ¤-£,ǒ΅ҭ+9ɏQ:+'),(3877,1,'access','µʥ'),(3878,1,'access','Վ'),(3756,1,'access','å'),(3721,1,'access','~a'),(3780,1,'vknp','Ù'),(3879,1,'subscriptions','௕Ø'),(3879,1,'argument','倀'),(3879,1,'incorporating','ิ'),(3879,1,'requests','ᩏ$\'ǃ'),(3871,1,'argument','฻य̒Ǉၬ/Ð\Z_}'),(3866,1,'requests','G'),(3783,1,'argument','£'),(3783,1,'requests',''),(3789,1,'argument','F'),(3791,1,'access','3'),(3799,1,'argument','ŗ'),(3822,1,'argument','÷'),(3822,1,'marked','ó'),(3825,1,'requests','Î'),(3827,1,'argument','ƪ'),(3838,1,'argument','$'),(3853,1,'argument','6'),(3863,1,'argument','Y'),(3866,1,'argument','d'),(3744,1,'upper','ǧ)'),(3754,1,'argument','ȝ'),(3744,1,'requests','Ů'),(3734,1,'argument',''),(3722,1,'requests','d/.'),(3721,1,'buy','ƍ'),(3880,1,'access','ͯk#_ɪš½ń(-tĖ΃ΥIˎµ,ƍ˪YɔǢᔕ௦¯Ņ5I'),(3874,1,'access','ϡ'),(3881,1,'biggest','ⲋ'),(3881,1,'fieldsorttwo','ᨨ'),(3881,1,'marked','ି'),(3881,1,'traditionally','❇'),(3885,1,'argument','ҩ'),(3886,1,'access','Ԅ'),(3887,1,'access','శ'),(3887,1,'argument','۪ס'),(3887,1,'upper','ɶA୐A'),(3888,1,'argument','֭'),(3890,1,'access','īÑſG$\r-8XNᙰ!'),(3890,1,'argument','ʸอIփ¦Lȉ>'),(3890,1,'marked','ڮ'),(3890,1,'requests','඼');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictCA` ENABLE KEYS */;

--
-- Table structure for table `dictCB`
--

DROP TABLE IF EXISTS `dictCB`;
CREATE TABLE `dictCB` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictCB`
--


/*!40000 ALTER TABLE `dictCB` DISABLE KEYS */;
LOCK TABLES `dictCB` WRITE;
INSERT INTO `dictCB` VALUES (3784,1,'connections',''),(3784,1,'warn','	'),(3785,1,'connections',''),(3745,1,'warn','-'),(3881,1,'executing','ܚ'),(3881,1,'console','⁏ɣ'),(3880,1,'console','࠸ƻؒංӎҞQ'),(3866,1,'warn','Y'),(3864,1,'travel','g'),(3744,1,'warn','M'),(3741,1,'style','\\'),(3879,1,'executing','䣯ᚹ'),(3879,1,'inherit','㌃ޮ'),(3879,1,'kb','ẝ'),(3822,1,'warn','9'),(3820,1,'warn','Ó'),(3739,1,'warn','	'),(3739,1,'connections',''),(3738,1,'warn','a'),(3737,1,'warn','	'),(3732,1,'warn','	'),(3735,1,'warn',')'),(3737,1,'connections',''),(3871,1,'myrouter','䁹)Ȕ='),(3732,1,'connections',''),(3879,1,'setup','ݚӞজظऀ`ȡঝ	'),(3854,1,'warn','½	'),(3854,1,'connections',''),(3881,1,'setup','ᷙՄ'),(3871,1,'coffee','㷜ࠈ'),(3762,1,'travel',''),(3728,1,'connections',''),(3728,1,'warn','	'),(3875,1,'setup','Ų'),(3765,1,'warn','6!'),(3876,1,'setup','Ô'),(3831,1,'connections','\Z'),(3827,1,'warn','Fæ'),(3763,1,'connections','ä'),(3720,1,'console','å\r'),(3871,1,'executing','㚪'),(3785,1,'warn','	'),(3872,1,'inherit','ௌ'),(3800,1,'warn','6'),(3799,1,'connections','ģ'),(3872,1,'setup','Pߠ'),(3871,1,'prev','ᤅ\"'),(3808,1,'warn','\''),(3803,1,'warn','+'),(3879,1,'style','ຌe'),(3748,1,'connections',''),(3727,1,'warn','<'),(3852,1,'warn','!'),(3852,1,'connections',''),(3851,1,'warn',','),(3847,1,'warn','G'),(3846,1,'kb','½'),(3845,1,'warn',','),(3844,1,'warn','/'),(3840,1,'warn','O'),(3840,1,'setup','È'),(3884,1,'executing','਽'),(3881,1,'warn','⏴'),(3748,1,'warn','	'),(3752,1,'warn','X.'),(3755,1,'warn',')'),(3756,1,'connections','!'),(3761,1,'connections',''),(3871,1,'travel','㯦ॎ)0װ'),(3871,1,'adjustment','▶8'),(3871,1,'setup','̐Ʀw'),(3720,1,'setup',''),(3766,1,'connections',''),(3879,1,'perf','䍽ӻ\r'),(3872,1,'002','ŎQ'),(3834,1,'travel',''),(3723,1,'warn','$'),(3871,1,'kb','ᖄ'),(3874,1,'setup','Ƣ'),(3877,1,'setup','®ķē7'),(3814,1,'connections',''),(3814,1,'warn','	'),(3815,1,'warn','$'),(3816,1,'warn','$\r'),(3818,1,'connections',''),(3818,1,'warn','	'),(3819,1,'sha1','ä'),(3820,1,'sha1',''),(3811,1,'warn','-'),(3793,1,'connections',''),(3791,1,'warn','<'),(3776,1,'warn','c		\r'),(3780,1,'connections','Į'),(3780,1,'kb','·'),(3781,1,'warn','3'),(3880,1,'referencing','䠃'),(3880,1,'organized','㧤bɛf[ܖ'),(3880,1,'manpages','䙬'),(3879,1,'console','ᔡ㕺Ğ	ᐍԑ'),(3833,1,'warn','	'),(3833,1,'connections',''),(3871,1,'style','ݕࣲ%ᆌ ī۔&'),(3857,1,'warn',')'),(3824,1,'warn','į'),(3761,1,'warn','	'),(3881,1,'inherit','ᎸĝŪÚ'),(3881,1,'governing','⎮'),(3881,1,'getfilter','ᮦ'),(3884,1,'inherit','ìüӡ'),(3884,1,'perf','֬਋'),(3884,1,'setup','ৈ'),(3885,1,'inherit','ʥ\n'),(3886,1,'inherit','ƌ\n'),(3887,1,'perf','Ӿ'),(3888,1,'inherit','ֺ'),(3890,1,'executing','ᑊ'),(3890,1,'inherit','ϲ'),(3890,1,'perf','᠙LGG'),(3890,1,'setup','¡	A\rw4ᯗ'),(3891,1,'inherit','ų'),(3852,6,'connections','4');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictCB` ENABLE KEYS */;

--
-- Table structure for table `dictCC`
--

DROP TABLE IF EXISTS `dictCC`;
CREATE TABLE `dictCC` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictCC`
--


/*!40000 ALTER TABLE `dictCC` DISABLE KEYS */;
LOCK TABLES `dictCC` WRITE;
INSERT INTO `dictCC` VALUES (3881,1,'overview','²ǵʳ8ȤΠ`ᇋ>$ϑԤ͎Ȇ\"݄@'),(3871,1,'inlet','䞚'),(3819,1,'mib','©'),(3856,1,'tells','~'),(3868,1,'overview',''),(3871,1,'4113333333e','⮊'),(3871,1,'correction','㮉ᅱ'),(3879,1,'novelty','䀘'),(3878,1,'overview','t'),(3877,1,'refers','ή'),(3871,1,'retrieved','͚'),(3742,1,'mib',']'),(3740,1,'authenticated','e\r'),(3721,1,'refers','û	\n'),(3879,1,'reportdate','晎'),(3879,1,'overview','İͤ᢯uŐ'),(3721,1,'overview','Ƙ'),(3871,1,'mib','㻯B'),(3871,1,'overview','㉔'),(3871,1,'plotting','౩'),(3871,1,'refers','⑞ઓǂß'),(3881,1,'collageeventquery','࣒éɣ૴'),(3880,1,'overview','ªFÙˎǠ¯ύଏd¸^¸VÒ$SAīEE_ɜďąɀ༖1ᅞó$'),(3881,1,'specification','⠃'),(3881,1,'reportdate','ࢇᓗԊ'),(3881,1,'retrieved','٬³gᆦ'),(3880,1,'authenticated','Ꮠz'),(3873,1,'overview','`'),(3875,1,'mib','·'),(3880,1,'health','ݚᅊǊߊ'),(3872,1,'tells','һ'),(3871,1,'lies','ᬭ'),(3872,1,'overview','G'),(3871,1,'tells','㖝'),(3871,1,'specification','ݗࣲ%ᆌ ī\Zġǿ>X˺&'),(3871,1,'shouldn','䒡'),(3881,1,'topic','Ὂ'),(3720,1,'plotting','ř'),(3884,1,'instantiated','८'),(3884,1,'refers','૖'),(3887,1,'thomas','ඉ'),(3890,1,'authenticated','ϟ\rHJ7XN'),(3890,1,'purge','ḱ\Z'),(3868,2,'overview',''),(3871,2,'overview',''),(3878,2,'overview',''),(3720,6,'overview','ǃ'),(3721,6,'overview','ʤ'),(3868,6,'overview','ĝ'),(3869,6,'overview','Ġ'),(3870,6,'overview','Ī'),(3871,6,'overview','僝'),(3872,6,'overview','೶'),(3873,6,'overview','ժ'),(3878,6,'overview','ॊ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictCC` ENABLE KEYS */;

--
-- Table structure for table `dictCD`
--

DROP TABLE IF EXISTS `dictCD`;
CREATE TABLE `dictCD` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictCD`
--


/*!40000 ALTER TABLE `dictCD` DISABLE KEYS */;
LOCK TABLES `dictCD` WRITE;
INSERT INTO `dictCD` VALUES (3880,1,'replacement','Ᏸ'),(3880,1,'needed','࿯'),(3880,1,'created','Ȏ∡Ẍ'),(3871,1,'needed','ᜍ'),(3871,1,'imginfo','ངӑ>᩠Ġ'),(3722,1,'healthy','m'),(3879,1,'gap',' '),(3879,1,'needed','⛖৬'),(3881,1,'created','Ə৾߶ݡȀ࿅ҹ'),(3835,1,'needed','i'),(3827,1,'newlines','Ť'),(3822,1,'replacement',''),(3782,1,'needed','N'),(3733,1,'created',';'),(3765,1,'74285959344712','µ'),(3871,1,'created','܇ओЂ࣋᥮̋଑ø'),(3871,1,'replacement','᷅'),(3871,1,'healthy','乀'),(3881,1,'helloworldview','⹔GȠľ'),(3879,1,'created','ʯݵדGզᮞᆚðҡ́ɜ8Đ	(©8ᆏ˲'),(3878,1,'created','ÔR'),(3878,1,'35','؀'),(3881,1,'calculate','ό'),(3872,1,'created','Ӥ'),(3840,1,'grown','đ'),(3871,1,'calculate','઒ྜྷà᧧āըടßĻ '),(3871,1,'35','㟴'),(3860,1,'created','!'),(3881,1,'needed','࠱×ᣆऀக'),(3881,1,'serviceinhostgroup','ᒞ'),(3885,1,'needed','՘'),(3886,1,'created','о'),(3887,1,'calculate','˗஑'),(3887,1,'created','ൾ'),(3887,1,'needed','ޙ'),(3888,1,'needed','ޑČ'),(3890,1,'calculate','ዴ'),(3890,1,'created','ࡁ'),(3890,1,'needed','ǣ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictCD` ENABLE KEYS */;

--
-- Table structure for table `dictCE`
--

DROP TABLE IF EXISTS `dictCE`;
CREATE TABLE `dictCE` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictCE`
--


/*!40000 ALTER TABLE `dictCE` DISABLE KEYS */;
LOCK TABLES `dictCE` WRITE;
INSERT INTO `dictCE` VALUES (3784,9,'http','ė'),(3751,9,'http','ʤ'),(3880,1,'constant','᪍᧦'),(3880,1,'cover','Ưᑰℳ'),(3880,1,'create','㑕˶'),(3880,1,'definitions','Ʋঢ়ù/\rǘŷ\nƸòᜊm೏ෟ'),(3880,1,'developers','ཷ㘌'),(3880,1,'http','ʢ(഻ևⴍ'),(3880,1,'severity','Ռ	\n.'),(3881,1,'3','¾*$.ȷǵֱભĉ඀T¸ś೔¸4ϗ'),(3727,1,'http',' Ē'),(3730,1,'hops','J'),(3736,1,'3',''),(3738,1,'3','l'),(3740,1,'3','ç'),(3744,1,'3',''),(3744,1,'http','ơ'),(3752,1,'3','ü'),(3753,1,'3','d'),(3757,1,'3','¤'),(3762,1,'http','Ð'),(3766,1,'http','à'),(3778,1,'3','î'),(3780,1,'http','Ɓ'),(3782,1,'http','ƃ'),(3788,1,'worst','/'),(3791,1,'create','.'),(3792,1,'http','?'),(3798,1,'http','³'),(3799,1,'http','Ŵ'),(3804,1,'post',''),(3811,1,'3','5S'),(3819,1,'3','R'),(3822,1,'hops','Í'),(3823,1,'http',' '),(3827,1,'http','\n¶&c]c:'),(3827,1,'post','Đ'),(3828,1,'3','D'),(3829,1,'http','<'),(3831,1,'http','¦'),(3835,1,'3','r\n'),(3841,1,'3','`\r '),(3856,1,'create',''),(3862,1,'create','ç'),(3866,1,'http',''),(3870,1,'3','ĉ'),(3870,1,'consult','ċ'),(3870,1,'developers','Á'),(3870,1,'http',''),(3871,1,'18446744069414584319','䵁'),(3871,1,'3','ຟìΦϫ\r\nЖʚ ʞГuOĉ4Э£ƖŤ௅δ\r`£Uޙ\nĄ'),(3871,1,'constant','ωǜሊẎྑȦɶsj'),(3871,1,'create','ŐĨ.Ʊɺ\r&֟Ⱦ´Ѽʥ஀ਜ׉ǂɷO\ZɄ\ZɖĈԾûãɡǻӡƖº'),(3871,1,'fire','䟋'),(3871,1,'http','⿢'),(3871,1,'judge','䳙Ţ'),(3871,1,'miles','㐬Ļ'),(3871,1,'trusted','㋘'),(3871,1,'zone','ⶠ৶'),(3872,1,'3','Ȣ)¢œ9ݾ'),(3872,1,'consult','ՏZ'),(3872,1,'create','ّǋ\"ώ'),(3872,1,'definitions','ٸ#,UɁǄ'),(3872,1,'htm','փ'),(3872,1,'http','ľЅ'),(3872,1,'mypager','ԥM!¾ǚǷ3\rı'),(3872,1,'netsnpp','ƨ'),(3873,1,'3','Ņ'),(3873,1,'definitions','ǋDE'),(3874,1,'3','ÄƸ'),(3874,1,'definitions','iâ'),(3875,1,'3','ċȅ'),(3875,1,'create','̃'),(3875,1,'definitions','s°'),(3876,1,'3','Ő'),(3876,1,'definitions','ES'),(3877,1,'copied','ڌ2'),(3877,1,'create','̧8®Åh'),(3877,1,'definitions','ι'),(3878,1,'definitions','ŭ'),(3878,1,'http','ȝŲ¶Ȍ'),(3879,1,'3','Ü22°\\̒ə߃Ս˕ֈȄ࠴ɬÄÏ	Äϧƌ၃ਢಹ੪ȕ'),(3879,1,'alarms1','凡'),(3879,1,'create','ࣅƷXԕ܌ɨܯĸµ;ѕօŘjҎ\"u\ZųĖdÊEОԭį\"WĘ4ċq	ÏėŲąʛ	ffĨׁB8aÁޘ̤IreeB'),(3726,1,'http','T'),(3827,6,'http','˟'),(3778,9,'http','ñ'),(3775,9,'http','\"'),(3776,9,'http','Ġ'),(3777,9,'http','#'),(3750,9,'http','/'),(3813,9,'http',''),(3732,9,'http','ĕ'),(3890,1,'3','ᓻ'),(3828,9,'http',''),(3879,1,'post','₼'),(3759,9,'http','÷'),(3723,9,'http','='),(3831,9,'http','Ê'),(3823,9,'http','Å'),(3881,1,'http','ஓ¢ᝍ࣯ͲΜ­uԍ'),(3833,9,'http','ė'),(3781,9,'http',''),(3890,9,'http','ᾇ'),(3883,9,'http','Ł'),(3791,9,'http','©'),(3752,9,'http','Ĉ'),(3890,1,'definitions','࠸Dᕼ%!)'),(3827,9,'http','ˎ'),(3721,1,'developers',','),(3749,9,'http','$'),(3845,9,'http','r'),(3836,9,'http','\\'),(3795,9,'http','}'),(3890,1,'http','ț'),(3721,1,'constant','Ȃ'),(3748,9,'http','ė'),(3722,9,'http','Ŝ'),(3879,1,'developers','๒㳞ᔲ'),(3879,1,'http','̸(তIȠټϗ\'!߽ྒྷ⋯ʯՏ'),(3872,9,'http','೧'),(3873,9,'http','՛'),(3874,9,'http','ϥ'),(3875,9,'http','̻'),(3876,9,'http','Ƣ'),(3877,9,'http','ރ'),(3878,9,'http','ऺ'),(3879,9,'http','木'),(3880,9,'http','䠶'),(3881,9,'http','㦶'),(3882,9,'http','Ʀ'),(3881,1,'developers','ȑʉ⇲2.Jͣĥ'),(3881,1,'definitions','Ꭳਬѡ'),(3881,1,'constant','ⅈ'),(3881,1,'cover','Չ➓'),(3881,1,'create','࣭Ĵ͚Ì଺ÈCŔ܁ѦѹģĶÛॕ'),(3857,9,'http','W'),(3761,9,'http','ė'),(3886,9,'http','Ԯ'),(3808,9,'http','s'),(3737,9,'http','Ĕ'),(3729,9,'http','Í'),(3730,9,'http',''),(3890,1,'interleave','ቓ<'),(3880,1,'3','®F4¥Δ؋ओȀ࢝͏ᆀɏഒ'),(3832,9,'http','G'),(3780,9,'http','ƨ'),(3760,9,'http','<'),(3783,9,'http','Ð'),(3782,9,'http','Ə'),(3807,9,'http',':'),(3736,9,'http','¶'),(3871,9,'http','僎'),(3867,9,'http','('),(3868,9,'http','Ď'),(3869,9,'http','đ'),(3870,9,'http','ě'),(3733,9,'http',''),(3839,9,'http','-'),(3810,9,'http','\''),(3814,9,'http','ė'),(3734,9,'http','ö'),(3853,9,'http','K'),(3844,9,'http','Î'),(3841,9,'http','¦'),(3725,9,'http','Z'),(3838,9,'http',';'),(3875,7,'definitions','͋'),(3887,9,'http','ᄈ'),(3856,9,'http','ƈ'),(3879,1,'responsibility','㾳'),(3884,9,'http','ᑚ'),(3825,9,'http','ļ'),(3756,9,'http','î'),(3790,9,'http','L'),(3774,9,'http','$'),(3792,9,'http',']'),(3851,9,'http',''),(3846,9,'http','ı'),(3889,1,'3','âʖ'),(3889,1,'create','ʮ'),(3889,1,'definitions','қB'),(3889,1,'http','ӟ'),(3819,9,'http','ę'),(3786,9,'http','8'),(3812,9,'http',','),(3806,9,'http','k'),(3722,1,'constant','®'),(3849,9,'http','t'),(3830,9,'http',''),(3758,9,'http',';'),(3803,9,'http','×'),(3842,9,'http','6'),(3843,9,'http','b'),(3809,9,'http',']'),(3890,1,'create','᳀'),(3879,1,'severity','Չ	\n.'),(3885,9,'http','৑'),(3815,9,'http','e'),(3826,9,'http',''),(3852,9,'http','#'),(3850,9,'http','B'),(3837,9,'http','Ğ'),(3840,9,'http','̑'),(3720,1,'copied','Ċ'),(3816,9,'http','K'),(3891,1,'copied','̄Æ'),(3881,1,'shared','Ⱒ'),(3883,1,'copied','}'),(3884,1,'3','ú֏ৄ'),(3884,1,'cover','w'),(3884,1,'create','7شę¬ŋŷݍ«'),(3884,1,'definitions','Áࠕ!'),(3886,1,'create','ɢLƼ'),(3887,1,'3','ǽ'),(3887,1,'copied','Ӂӵǰ'),(3887,1,'create','9ईŘ)[Ҙ('),(3888,1,'113','ޡč'),(3888,1,'3','Uֲ'),(3888,1,'create','״mų'),(3888,1,'definitions','֑͜'),(3888,1,'proxy1','ۢ.;\Zı5'),(3820,9,'http','ņ'),(3821,9,'http','4'),(3822,9,'http','ø'),(3794,9,'http',' '),(3793,9,'http','¡'),(3892,9,'http','Ƹ'),(3891,9,'http','ق'),(3818,9,'http','ė'),(3817,9,'http','('),(3785,9,'http','ė'),(3811,9,'http','¿'),(3805,9,'http','Æ'),(3804,9,'http','®'),(3881,1,'severity','͜	\n.Әۍ\n\r	ཱ͟ž'),(3721,1,'http','Eġ6'),(3892,1,'definitions',''),(3891,1,'create',';ϊ'),(3855,9,'http','2'),(3854,9,'http','Ļ'),(3848,9,'http','¨'),(3847,9,'http','M'),(3829,9,'http','p'),(3835,9,'http',''),(3834,9,'http','²'),(3757,9,'http','Ơ'),(3879,1,'definitions','⇼ĝS8b,Ĳ\Z=ÃǾࠥä}ťउƆ#I੊́NĂ±9[\nŦ	-\rΨQ'),(3802,9,'http','5'),(3801,9,'http','/'),(3796,9,'http','\"'),(3797,9,'http','e'),(3798,9,'http','é'),(3799,9,'http','ƈ'),(3800,9,'http','>'),(3747,9,'http','T'),(3746,9,'http',','),(3779,9,'http','-'),(3861,9,'http','*'),(3858,9,'http',','),(3859,9,'http',':'),(3860,9,'http','?'),(3889,9,'http','ӳ'),(3888,9,'http','ः'),(3724,9,'http','`'),(3874,7,'definitions','ϵ'),(3731,9,'http',''),(3824,9,'http','Ő'),(3755,9,'http','¸'),(3754,9,'http','̈'),(3753,9,'http','}'),(3789,9,'http',''),(3788,9,'http','Õ'),(3787,9,'http','£'),(3773,9,'http','#'),(3765,9,'http','×'),(3766,9,'http','ê'),(3767,9,'http','!'),(3768,9,'http','\"'),(3769,9,'http',''),(3770,9,'http',''),(3771,9,'http',''),(3772,9,'http',''),(3738,9,'http','z'),(3739,9,'http','Ĕ'),(3740,9,'http','Ɓ'),(3741,9,'http','³'),(3742,9,'http','~'),(3743,9,'http',''),(3744,9,'http','ɕ'),(3745,9,'http','õ'),(3735,9,'http','p'),(3728,9,'http','Ĕ'),(3727,9,'http','ĺ'),(3726,9,'http',''),(3721,9,'http','ʕ'),(3720,9,'http','ƴ'),(3876,7,'definitions','Ʋ'),(3864,9,'http','~'),(3865,9,'http',''),(3866,9,'http','¯'),(3863,9,'http','Ĵ'),(3862,9,'http','Ē'),(3764,9,'http','7'),(3763,9,'http','Ĳ'),(3762,9,'http','Ø');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictCE` ENABLE KEYS */;

--
-- Table structure for table `dictCF`
--

DROP TABLE IF EXISTS `dictCF`;
CREATE TABLE `dictCF` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictCF`
--


/*!40000 ALTER TABLE `dictCF` DISABLE KEYS */;
LOCK TABLES `dictCF` WRITE;
INSERT INTO `dictCF` VALUES (3851,7,'reference',''),(3842,7,'reference','B'),(3841,7,'reference','²'),(3725,7,'reference','f'),(3879,1,'practice','஥᱀'),(3828,7,'reference','¢'),(3818,7,'reference','ģ'),(3834,7,'reference','¾'),(3757,7,'reference','Ƭ'),(3823,7,'reference','Ñ'),(3801,7,'reference',';'),(3800,7,'reference','J'),(3799,7,'reference','Ɣ'),(3798,7,'reference','õ'),(3797,7,'reference','q'),(3796,7,'reference','.'),(3795,7,'reference',''),(3794,7,'reference',','),(3793,7,'reference','­'),(3792,7,'reference','i'),(3811,7,'reference','Ë'),(3835,7,'reference',''),(3884,1,'display','Ꮟ'),(3886,1,'making','ǸǨ'),(3887,1,'reference','ʝqଠu'),(3849,7,'reference',''),(3821,7,'reference','@'),(3805,7,'reference','Ò'),(3814,7,'reference','ģ'),(3881,1,'contained','۵'),(3751,1,'reference','I6ÿI'),(3723,7,'reference','I'),(3837,7,'reference','Ī'),(3826,7,'reference','+'),(3827,7,'reference','˚'),(3766,7,'reference','ö'),(3767,7,'reference','-'),(3768,7,'reference','.'),(3769,7,'reference','+'),(3770,7,'reference','+'),(3771,7,'reference','+'),(3772,7,'reference','+'),(3773,7,'reference','/'),(3774,7,'reference','0'),(3775,7,'reference','.'),(3776,7,'reference','Ĭ'),(3777,7,'reference','/'),(3778,7,'reference','ý'),(3779,7,'reference','9'),(3780,7,'reference','ƴ'),(3781,7,'reference',''),(3782,7,'reference','ƛ'),(3783,7,'reference','Ü'),(3784,7,'reference','ģ'),(3785,7,'reference','ģ'),(3880,1,'display','۽঴մȤȩʏ²ߥ7ƛଠ?&ĩĠÊ'),(3875,1,'reference','̝'),(3843,7,'reference','n'),(3803,7,'reference','ã'),(3802,7,'reference','A'),(3852,7,'reference','/'),(3850,7,'reference','N'),(3840,7,'reference','̝'),(3890,1,'display','Ż'),(3890,1,'stripped','ᰚ'),(3890,1,'timing','Ꮶ'),(3737,6,'ssmtp','ĥ'),(3720,7,'reference','ǀ'),(3879,1,'escalated','ⷋ်'),(3879,1,'making','™֦෡ؿƭᾨ'),(3806,7,'reference','w'),(3786,7,'reference','D'),(3745,7,'reference','ā'),(3746,7,'reference','8'),(3734,7,'reference','Ă'),(3735,7,'reference','|'),(3736,7,'reference','Â'),(3737,7,'reference','Ġ'),(3738,7,'reference',''),(3739,7,'reference','Ġ'),(3740,7,'reference','ƍ'),(3741,7,'reference','¿'),(3742,7,'reference',''),(3743,7,'reference',''),(3888,1,'making','س'),(3879,1,'display','ᗐ࢑ᖁືЩ'),(3846,7,'reference','Ľ'),(3817,7,'reference','4'),(3824,7,'reference','Ŝ'),(3839,7,'reference','9'),(3838,7,'reference','G'),(3829,7,'reference','|'),(3830,7,'reference',''),(3720,1,'progressbar','Ũ'),(3819,7,'reference','ĥ'),(3721,7,'reference','ʡ'),(3881,1,'display','⾼'),(3833,7,'reference','ģ'),(3815,7,'reference','q'),(3844,7,'reference','Ú'),(3787,7,'reference','¯'),(3889,1,'escalated','Ο'),(3737,1,'ssmtp',''),(3749,1,'lm',''),(3728,7,'reference','Ġ'),(3729,7,'reference','Ù'),(3730,7,'reference',''),(3731,7,'reference',' '),(3732,7,'reference','ġ'),(3733,7,'reference',''),(3724,7,'reference','l'),(3847,7,'reference','Y'),(3807,7,'reference','F'),(3808,7,'reference',''),(3747,7,'reference','`'),(3748,7,'reference','ģ'),(3749,7,'reference','0'),(3750,7,'reference',';'),(3751,7,'reference','ʰ'),(3752,7,'reference','Ĕ'),(3753,7,'reference',''),(3754,7,'reference','̔'),(3755,7,'reference','Ä'),(3756,7,'reference','ú'),(3880,1,'making','㒴'),(3880,1,'obvious','ᆆ⍾'),(3880,1,'reference','Ζמ㪐d\Z±~ '),(3879,1,'contained','勎Í6൙ķV'),(3845,7,'reference','~'),(3822,7,'reference','Ą'),(3788,7,'reference','á'),(3789,7,'reference',''),(3791,7,'reference','µ'),(3809,7,'reference','i'),(3810,7,'reference','3'),(3881,1,'reference','ǯܠ\Z2\rª┩3Ʒöd'),(3881,1,'todate','৫	ഷ3\"	>@\"	=3\"	'),(3882,1,'display',''),(3848,7,'reference','´'),(3820,7,'reference','Œ'),(3804,7,'reference','º'),(3816,7,'reference','W'),(3813,7,'reference',''),(3881,1,'593','⏳'),(3751,1,'making','ʇ'),(3722,7,'reference','Ũ'),(3836,7,'reference','h'),(3825,7,'reference','ň'),(3790,7,'reference','X'),(3878,1,'reference','Ű˜	-	.	/	,	!	.	.	&\n'),(3812,7,'reference','8'),(3831,7,'reference','Ö'),(3832,7,'reference','S'),(3759,7,'reference','ă'),(3760,7,'reference','H'),(3761,7,'reference','ģ'),(3762,7,'reference','ä'),(3763,7,'reference','ľ'),(3764,7,'reference','C'),(3765,7,'reference','ã'),(3758,7,'reference','G'),(3744,7,'reference','ɡ'),(3726,7,'reference',''),(3727,7,'reference','ņ'),(3879,1,'reference','Юเ૽H௩ࠢʇǼଁӨZ௢ҽɃ_'),(3754,1,'dt','[ñ\rĘ*J'),(3762,1,'obsoleted','p'),(3765,1,'sdb1','º'),(3778,1,'oranames','9a'),(3797,1,'display',''),(3827,1,'obsoleted','Ƭ'),(3827,1,'pg','d'),(3840,1,'authoritative',''),(3846,1,'display','ü'),(3868,1,'reference',''),(3871,1,'603','伪`'),(3871,1,'collecting','䍬'),(3871,1,'contained','⻡'),(3871,1,'display','?˳࿮4\rṟ&ޘŋ£ࠉ'),(3871,1,'making','჋'),(3871,1,'possibilities','䞈'),(3871,1,'prints','ᵮȼ¸'),(3871,1,'recall','䐬'),(3871,1,'reference','޴ᯂ	©\rJjF'),(3871,1,'steve','㬲'),(3871,1,'timing','ڐޑ'),(3874,1,'reference','S\n'),(3853,7,'reference','W'),(3854,7,'reference','Ň'),(3855,7,'reference','>'),(3856,7,'reference','Ɣ'),(3857,7,'reference','c'),(3858,7,'reference','8'),(3859,7,'reference','F'),(3860,7,'reference','K'),(3861,7,'reference','6'),(3862,7,'reference','Ğ'),(3863,7,'reference','ŀ'),(3864,7,'reference',''),(3865,7,'reference','¥'),(3866,7,'reference','»'),(3867,7,'reference','4'),(3868,7,'reference','Ě'),(3869,7,'reference','ĝ'),(3870,7,'reference','ħ'),(3871,7,'reference','僚'),(3872,7,'reference','ೳ'),(3873,7,'reference','է'),(3874,7,'reference','ϰ'),(3875,7,'reference','͆'),(3876,7,'reference','ƭ'),(3877,7,'reference','ގ'),(3878,7,'reference','ॅ'),(3879,7,'reference','朳'),(3880,7,'reference','䡁'),(3881,7,'reference','㧁'),(3882,7,'reference','Ʊ'),(3883,7,'reference','Ō'),(3884,7,'reference','ᑥ'),(3885,7,'reference','ড়'),(3886,7,'reference','Թ'),(3887,7,'reference','ᄓ'),(3888,7,'reference','ऎ'),(3889,7,'reference','Ӿ'),(3890,7,'reference','ᾒ'),(3891,7,'reference','ٍ'),(3892,7,'reference','ǃ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictCF` ENABLE KEYS */;

--
-- Table structure for table `dictD0`
--

DROP TABLE IF EXISTS `dictD0`;
CREATE TABLE `dictD0` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictD0`
--


/*!40000 ALTER TABLE `dictD0` DISABLE KEYS */;
LOCK TABLES `dictD0` WRITE;
INSERT INTO `dictD0` VALUES (3879,1,'criteria','䮝\n\r¹(\nt	U\n+'),(3875,1,'speed','ɋ'),(3877,1,'safe','է'),(3879,1,'7','Ű΋ᩝᵌ'),(3871,1,'lc','ᆰ'),(3871,1,'interface2','ᷙ'),(3881,1,'iseventhandlersenabled','ࡔ'),(3881,1,'7','̎'),(3880,1,'serious','ՙ'),(3880,1,'criteria','অᷭ\n'),(3880,1,'7','Ӿᨷ'),(3879,1,'width','崱b´?'),(3879,1,'serious','Ֆ'),(3879,1,'rrdupdatestring','䧱	'),(3881,1,'rule','໫'),(3871,1,'speed','ዽἇϨ\rE>Ɩ\n´Àà\n\rG(\n	¢ں!QƋ'),(3871,1,'rule','᭨'),(3871,1,'opposed','ㅿ'),(3881,1,'criteria','ဦ\r	\n±҆ĉ£¨Ќ2	(ڶ\n'),(3879,1,'rule','䮿'),(3871,1,'acquisition','̠ȅࣇ'),(3720,1,'essentially','6'),(3720,1,'width','¡'),(3721,1,'speed','Éň'),(3738,1,'9p1','m'),(3778,1,'7','í'),(3780,1,'vkp','Æ'),(3792,1,'7','\"'),(3821,1,'speed',''),(3869,1,'7','='),(3869,1,'speed','ç'),(3871,1,'7','ໄ㭜Ǖ'),(3872,1,'7','̀¥'),(3871,1,'width','༼щ#«Ǒᑜиě'),(3881,1,'secondary','৐'),(3881,1,'serious','ͩ'),(3884,1,'criteria','ࡧओ		'),(3884,1,'serious','Ͱ਋'),(3887,1,'criteria','ၽ	'),(3889,1,'criteria','ǿ'),(3890,1,'essentially','Ꮞ'),(3890,1,'speed','ࡒ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictD0` ENABLE KEYS */;

--
-- Table structure for table `dictD1`
--

DROP TABLE IF EXISTS `dictD1`;
CREATE TABLE `dictD1` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictD1`
--


/*!40000 ALTER TABLE `dictD1` DISABLE KEYS */;
LOCK TABLES `dictD1` WRITE;
INSERT INTO `dictD1` VALUES (3890,1,'consecutive','ᑃ'),(3890,1,'corresponds','Ƕ'),(3720,1,'dropdowntimepicker','Ţ'),(3726,1,'attention','A'),(3752,1,'entries','Ï'),(3764,1,'entries',''),(3780,1,'entries','Č	'),(3844,1,'megabytes','¼'),(3846,1,'megabytes','Ê'),(3869,1,'procedural','y'),(3871,1,'3600','ᙙͫӢηᤇ !»'),(3871,1,'entries','ࣱₐ'),(3874,1,'megabytes','Ȳ'),(3877,1,'entries','͓'),(3879,1,'csvimport','䴦ā'),(3879,1,'entries','ᬎڮᕳħཱښȟΑƀႰ)*'),(3879,1,'generically','⋯ሢ'),(3880,1,'attention','᠇͹#ܛᗨ'),(3880,1,'corresponds','㨥UKOǎY_IJChgƸ?BA'),(3880,1,'entries','➐'),(3880,1,'majority','ᝲ'),(3880,1,'shutdowns','⢗өƯ'),(3880,1,'tracked','ျ'),(3881,1,'54','⏲'),(3881,1,'entries','๹\Z\Z\Z  0 k084\'\'קV'),(3881,1,'hoststatus','ࢄᩞ('),(3881,1,'subcomponent','ࢊ᭒'),(3884,1,'entries','௰'),(3889,1,'entries','Ʈ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictD1` ENABLE KEYS */;

--
-- Table structure for table `dictD2`
--

DROP TABLE IF EXISTS `dictD2`;
CREATE TABLE `dictD2` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictD2`
--


/*!40000 ALTER TABLE `dictD2` DISABLE KEYS */;
LOCK TABLES `dictD2` WRITE;
INSERT INTO `dictD2` VALUES (3747,1,'manual','L'),(3721,1,'manual','ø'),(3720,1,'widgets','į3'),(3721,1,'corporate','z'),(3880,1,'offering','ᕤ'),(3880,1,'ctrl','㕸'),(3871,1,'manual','ůⴋᇘ'),(3820,1,'changed','Ľ'),(3871,1,'millionths','፫'),(3880,1,'corporate','৺'),(3879,1,'robin','䊞'),(3879,1,'href','崌:		7\nÊ\n'),(3879,1,'corporate','ᔦ㠺'),(3879,1,'changed','๰ҙkK䴠'),(3879,1,'apart','㿧'),(3879,1,'carriage','ẉ	'),(3840,1,'apart','ȱ'),(3836,1,'changed','\"'),(3871,1,'robin','6ƾwǩ?\rRâã&ɵụ:ࠢʮ̅1'),(3823,1,'robin','§'),(3871,1,'hurting','Ὓ'),(3873,1,'carriage','ʺ'),(3878,1,'changed','п'),(3871,1,'changed','㯪'),(3871,1,'car','㐥eĆࠞٶïِ|Ț'),(3871,1,'apart','ྸᎎΆᐣ਍ড'),(3870,1,'manual','B'),(3863,1,'regexp','«$'),(3856,1,'flint','Ŭ'),(3881,1,'adapters','ᶑā,P°Œ'),(3881,1,'getdevice','ው'),(3884,1,'changed','ʫ/)Ĩࢋ/)Ĩ'),(3885,1,'changed','ն©'),(3886,1,'ctrl','ρ'),(3887,1,'changed','޷©'),(3889,1,'changed','Ŝ'),(3890,1,'carriage','ᤜ5'),(3892,1,'thirty','®');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictD2` ENABLE KEYS */;

--
-- Table structure for table `dictD3`
--

DROP TABLE IF EXISTS `dictD3`;
CREATE TABLE `dictD3` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictD3`
--


/*!40000 ALTER TABLE `dictD3` DISABLE KEYS */;
LOCK TABLES `dictD3` WRITE;
INSERT INTO `dictD3` VALUES (3824,1,'description',''),(3823,1,'farms',''),(3823,1,'description',''),(3828,1,'description',''),(3806,1,'description',''),(3878,1,'description','}Ýݣ'),(3752,1,'description',''),(3842,1,'description',''),(3791,1,'description',''),(3790,1,'description',''),(3766,1,'description',''),(3767,1,'description',''),(3851,1,'description',''),(3733,1,'expressions','*'),(3733,1,'description',''),(3812,1,'description',''),(3785,1,'description',''),(3786,1,'description',''),(3787,1,'description',''),(3752,1,'expressions','c'),(3753,1,'description',''),(3879,1,'class','Ӹ堛\n\n\n\r\n!\r\n\r'),(3871,1,'chosen','๺ࡎͩࢥⱪ'),(3871,1,'1st','㝬'),(3881,1,'class','̋ߗŶ\r á	ևĝŪÚᜒm\'Þîľ³ժ'),(3769,1,'description',''),(3779,1,'description',''),(3774,1,'description',''),(3775,1,'description',''),(3776,1,'description',''),(3777,1,'description',''),(3778,1,'description',''),(3737,1,'description',''),(3738,1,'description',''),(3739,1,'description',''),(3740,1,'description',''),(3740,1,'regex','×'),(3741,1,'description',''),(3741,1,'regex','c'),(3742,1,'description',''),(3743,1,'description',''),(3744,1,'description',''),(3744,1,'regex','WÚî'),(3745,1,'description',''),(3746,1,'description',''),(3747,1,'description',''),(3748,1,'description',''),(3749,1,'description',''),(3750,1,'description',''),(3751,1,'accurately','ǈ'),(3751,1,'description',''),(3751,1,'linear','#'),(3735,1,'description',''),(3736,1,'description',''),(3843,1,'description',''),(3850,1,'description',''),(3867,1,'description',''),(3855,1,'description',''),(3856,1,'description',''),(3720,1,'combobutton','ƞ'),(3844,1,'description',''),(3770,1,'description',''),(3763,1,'description',''),(3869,1,'description',''),(3860,1,'description',''),(3839,1,'description',''),(3807,1,'description',''),(3768,1,'description',''),(3720,1,'description',''),(3721,1,'description',''),(3805,1,'regex','©'),(3780,1,'cdbuff',''),(3780,1,'description',''),(3781,1,'description',''),(3782,1,'clientversion',''),(3782,1,'description','ĸ'),(3783,1,'description',''),(3784,1,'description',''),(3773,1,'description',''),(3876,1,'perfconfig',''),(3827,1,'regex','[Ă\n\n'),(3827,1,'description',''),(3827,1,'expressions','/'),(3792,1,'description',''),(3793,1,'description',''),(3794,1,'description',''),(3795,1,'description',''),(3796,1,'description',''),(3797,1,'description',''),(3798,1,'description',''),(3799,1,'description',''),(3800,1,'description',''),(3801,1,'description',''),(3757,1,'description',''),(3758,1,'description',''),(3759,1,'description',''),(3760,1,'description',''),(3734,1,'description',''),(3849,1,'description',''),(3848,1,'description',''),(3808,1,'description',''),(3809,1,'description',''),(3810,1,'description',''),(3818,1,'description',''),(3817,1,'description',''),(3816,1,'description',''),(3815,1,'description',''),(3814,1,'description',''),(3813,1,'description',''),(3813,1,'class','{'),(3830,1,'description',''),(3829,1,'description',''),(3871,1,'description','ǩҲEٹȸဂcUðقîĤNʇΩฅɟ'),(3879,1,'req','᥺'),(3868,1,'description',''),(3864,1,'description',''),(3863,1,'description',''),(3765,1,'description',''),(3764,1,'description',''),(3847,1,'description',''),(3880,1,'class','ӻˁ'),(3832,1,'description',''),(3831,1,'description',''),(3879,1,'description','ԃÒҷܑ҄	ݪ੖ӬƸó੉ࡏ6ߔǣ*˽\\8෈úǦ'),(3879,1,'archiving','敚/'),(3846,1,'description',''),(3845,1,'description',''),(3826,1,'description',''),(3825,1,'description',''),(3871,1,'formatted','⧺А'),(3841,1,'description',''),(3840,1,'description',''),(3837,1,'description',''),(3836,1,'description',''),(3762,1,'description',''),(3761,1,'description',''),(3723,1,'description',''),(3722,1,'description',''),(3835,1,'description',''),(3772,1,'description',''),(3771,1,'description',''),(3870,1,'description','\r'),(3866,1,'description',''),(3865,1,'description',''),(3853,1,'description',''),(3852,1,'description',''),(3854,1,'description',''),(3811,1,'description',''),(3732,1,'description',''),(3731,1,'description',''),(3820,1,'description',''),(3879,1,'perfconfig','䨚'),(3834,1,'description',''),(3833,1,'description',''),(3859,1,'description',''),(3858,1,'description',''),(3857,1,'description',''),(3879,1,'performing','⃠'),(3880,1,'accurately','ࡸ'),(3862,1,'description',''),(3861,1,'description',''),(3805,1,'description',''),(3804,1,'description',''),(3819,1,'description',''),(3871,1,'maxrows','⧟'),(3881,1,'description','̖ÒҸල	żଌ[৸Js'),(3880,1,'handler','ⰽ\r\r֤\r'),(3879,1,'06','䵒ᛏ'),(3879,1,'chosen','㛡'),(3822,1,'description',''),(3821,1,'description',''),(3876,1,'description','Ş'),(3838,1,'description',''),(3837,1,'keeporphaned','ñ'),(3880,1,'description','ԆÒ˱஻͌ŊTҤʒݧҬѦਞη¹ƬЩ\r'),(3803,1,'description',''),(3802,1,'description',''),(3789,1,'description',''),(3788,1,'description',''),(3875,1,'perfconfig','Į'),(3875,1,'description','Ȁ!'),(3874,1,'perfconfig','ő'),(3873,1,'formatted','Ś'),(3874,1,'description','̬'),(3873,1,'architectures','7'),(3873,1,'description','\r'),(3872,1,'sk','ɤ'),(3872,1,'description','ɏ࣪Ŝ'),(3871,1,'prepare','び'),(3871,1,'pdps','ਲÈ%%'),(3871,1,'microscope','䥕'),(3879,1,'handler','ᇵゑý\Zƕ£!	ɠ\\'),(3879,1,'expressions','䔗'),(3755,1,'description',''),(3756,1,'description',''),(3754,1,'regex','Q­ƞ'),(3754,1,'description',''),(3754,1,'1st','ź'),(3724,1,'description',''),(3725,1,'description',''),(3726,1,'description',''),(3727,1,'description',''),(3728,1,'description',''),(3729,1,'description',''),(3730,1,'description',''),(3881,1,'destruct','഻'),(3881,1,'gethostsforhostgroup','छ૩'),(3882,1,'chosen','Ċ'),(3883,1,'description','Ë'),(3884,1,'description','ᐦ'),(3884,1,'handler','ӗ\n-	শ\n-	'),(3884,1,'performing','Ρ਋'),(3885,1,'description','ŋĵլ'),(3885,1,'handler','և\r-	'),(3886,1,'description','è'),(3887,1,'description','Đअ'),(3887,1,'handler','߈\r-	'),(3888,1,'description','ąդŘt'),(3890,1,'chosen','೥'),(3890,1,'handler','ပÁ\n(\n؊'),(3890,1,'performing','᜗'),(3891,1,'description','Ñ̞');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictD3` ENABLE KEYS */;

--
-- Table structure for table `dictD4`
--

DROP TABLE IF EXISTS `dictD4`;
CREATE TABLE `dictD4` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictD4`
--


/*!40000 ALTER TABLE `dictD4` DISABLE KEYS */;
LOCK TABLES `dictD4` WRITE;
INSERT INTO `dictD4` VALUES (3872,1,'ttys0','ԗ'),(3879,1,'problem','טᑩ৙ᣐ7rଯ'),(3879,1,'outlined','ᆓ'),(3871,1,'treated','⚕'),(3871,1,'statement','᪩ဖ'),(3881,1,'fieldsortone','᧱'),(3881,1,'enabling','ҙ'),(3880,1,'quality','ى'),(3881,1,'descriptions','स'),(3879,1,'methods','❁ӵᚪဝ'),(3871,1,'problem','Ѥ⺎2ʪܸ྿'),(3871,1,'periodic','ኵ'),(3871,1,'markers','Ჳ'),(3871,1,'descriptions','㎕ᴁ'),(3871,1,'10e3','ጦ6'),(3871,1,'000007','䮔'),(3840,1,'problem','Ī¤'),(3751,1,'treated','ĸ-'),(3879,1,'launching','ᒭ7Ʋƪ'),(3879,1,'indexes','孧'),(3879,1,'enabling','⁝ᑐ'),(3879,1,'descriptions','冶'),(3877,1,'problem','Ӕ'),(3873,1,'outlined','Ț'),(3881,1,'sufficient','⇟'),(3879,1,'quality','ن'),(3872,1,'problem','ࡲǤ)'),(3880,1,'problem','כसȠÊ#œ֍ۮø˙ࠇ	\'J\r͂	\'HPۣ'),(3880,1,'outlined','ࢼ'),(3880,1,'oreilly','ᗣ'),(3880,1,'methods','༲⿤'),(3880,1,'histogram','୛⳨୎'),(3880,1,'enabling','ᢟ્'),(3880,1,'descriptions','⡗႐'),(3881,1,'statement','ൾ?ᕳቿ'),(3881,1,'quality','ћ'),(3881,1,'problem','ϫࢮ'),(3881,1,'methods','ٍɹÅ৽ĝŪÚϪ؏ුϽ́˝'),(3881,1,'jar','ι'),(3881,1,'fits','⢉'),(3881,1,'getschemainfo','፫'),(3744,1,'methods','Ɂ'),(3881,1,'textmessage','ࢆᙵ͞Ɔ'),(3884,1,'descriptions','ᑉ'),(3884,1,'enabling','৤'),(3884,1,'methods','ጔ'),(3884,1,'problem','я਋'),(3885,1,'problem','م'),(3887,1,'methods','౏'),(3887,1,'problem','ࢆ'),(3888,1,'enabling','ࠗ'),(3889,1,'problem','Ɣ'),(3890,1,'descriptions','ᰅ'),(3890,1,'enabling','ᑠ9R`xդK'),(3890,1,'lockfile','ฝ'),(3890,1,'problem','ݻ᎕'),(3891,1,'problem','Ȭâ˗<');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictD4` ENABLE KEYS */;

--
-- Table structure for table `dictD5`
--

DROP TABLE IF EXISTS `dictD5`;
CREATE TABLE `dictD5` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictD5`
--


/*!40000 ALTER TABLE `dictD5` DISABLE KEYS */;
LOCK TABLES `dictD5` WRITE;
INSERT INTO `dictD5` VALUES (3877,1,'hope','>'),(3871,1,'reasons','㺿'),(3878,1,'documentation','ࣖ'),(3871,1,'hope','╵თᒷ֌'),(3877,1,'find','ˈ'),(3877,1,'continue','ҥ<'),(3872,1,'documentation','Ւŧ'),(3878,1,'hope','7'),(3879,1,'missing','☗όƜ'),(3872,1,'continue','ડŎØ'),(3872,1,'depending','օב>'),(3880,1,'documentation','Ūǀʷǝė!ஹa\r⸓r͕'),(3871,1,'unkn','᣽֬\nP(Ơ!៿'),(3879,1,'hope','@'),(3879,1,'browsers','᧞§'),(3879,1,'bases','Ọ'),(3739,1,'hide','´'),(3741,1,'find','l'),(3748,1,'hide','·'),(3751,1,'missing','ȍ'),(3751,1,'trendwarning','Ƙ'),(3754,1,'depending',')'),(3761,1,'hide','·'),(3784,1,'hide','·'),(3785,1,'hide','·'),(3806,1,'scans',''),(3814,1,'hide','·'),(3818,1,'hide','·'),(3824,1,'find','E'),(3825,1,'find','>'),(3829,1,'showfiles','D'),(3833,1,'hide','·'),(3840,1,'resolver','ƫA'),(3854,1,'hide','Û'),(3866,1,'find','6'),(3868,1,'documentation','F$U'),(3871,1,'backend','¥'),(3871,1,'browsers','Ꮳ☜'),(3871,1,'continue','㾢ȟՍ'),(3871,1,'depending','ᥭʊֈΚࣿ'),(3871,1,'documentation','żנࣲ%᧗&.		֣ˉ஁ᄲ'),(3871,1,'edge','ᥩϞ᎐'),(3871,1,'find','ǩĵ⒕ᡋpʿ೔'),(3879,1,'find','਑༶ѷ଄ͽނϧŐĠƣůᧁ'),(3879,1,'documentation','ʕƕȾంⲩʾќZႤǙŕĿ,¨'),(3879,1,'depending','⾰>'),(3879,1,'continue','୍΍Ĝޡᜥݹ෍ै¼Z'),(3879,1,'constantly','䪻ᔲ'),(3880,1,'depending','൉ᙨ'),(3880,1,'continue','ᥢ൐ಶyᐠ'),(3879,1,'tables','ሎ'),(3737,1,'hide','´'),(3732,1,'hide','µ'),(3728,1,'hide','´'),(3722,1,'find','Ě'),(3721,1,'tables','¼'),(3721,1,'find','ǖ'),(3720,1,'documentation','ĝ'),(3880,1,'find','㣦˝Ѱ'),(3880,1,'hope','@'),(3880,1,'tables','ᤈũൽ၎'),(3881,1,'continue','೬෾'),(3881,1,'documentation','Ďg»ɍƣמඌଙ·੗݌ɍ'),(3881,1,'find','ෆ'),(3881,1,'hashes','ੰ'),(3881,1,'hope','@ⱀ'),(3881,1,'missing','᳞Ѡ'),(3881,1,'retrynumber','ࡢ'),(3884,1,'continue','÷ऍıحƅ'),(3886,1,'find',''),(3887,1,'continue','ѩܥҔ'),(3887,1,'find','Æ'),(3888,1,'continue','ݣ'),(3888,1,'depending','Ϭ'),(3888,1,'documentation','˃'),(3888,1,'find','ʌɥ'),(3889,1,'continue','ɀ'),(3890,1,'continue','԰ᖢ̐Ò%R'),(3890,1,'depending','˴'),(3890,1,'documentation','ȎĞܦĈ੧'),(3890,1,'missing','ᶖ'),(3890,1,'submenu','ᵵ'),(3891,1,'continue','ṽÆ'),(3891,1,'depending','î>'),(3891,1,'documentation','ƲÊɉC'),(3892,1,'continue','u'),(3892,1,'documentation','Ŋ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictD5` ENABLE KEYS */;

--
-- Table structure for table `dictD6`
--

DROP TABLE IF EXISTS `dictD6`;
CREATE TABLE `dictD6` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictD6`
--


/*!40000 ALTER TABLE `dictD6` DISABLE KEYS */;
LOCK TABLES `dictD6` WRITE;
INSERT INTO `dictD6` VALUES (3724,1,'hares',''),(3733,1,'flag','R'),(3747,1,'flag','H'),(3752,1,'user1','Õ'),(3757,1,'filters','2v'),(3781,1,'5000','o'),(3787,1,'vi',''),(3809,1,'wload15','#'),(3827,1,'5000','¢'),(3831,1,'rpmfind','¨'),(3834,1,'flag','4'),(3841,1,'user1',''),(3870,1,'13','X'),(3871,1,'1020612900','⮕'),(3871,1,'13','㠇ĵè'),(3871,1,'5000','ೄB'),(3871,1,'55','㠄'),(3871,1,'etcetera','㡡'),(3871,1,'linea','䤧'),(3871,1,'official','϶'),(3871,1,'plot','ా>໯Ψⱨ'),(3871,1,'talk','Ț⽫̊̏'),(3872,1,'collectors','ߎ'),(3872,1,'user1','ࠔ'),(3873,1,'technology','$'),(3873,1,'user1','˚!\"!!%\'%%%)()((&'),(3874,1,'user1','¦\" '),(3875,1,'user1','®*'),(3876,1,'5000',''),(3876,1,'user1','~'),(3877,1,'vi','Ǔý'),(3878,1,'13','˒'),(3879,1,'13','ƾᷨ⅙ౣ/'),(3879,1,'cron','兂'),(3879,1,'flag','匮|ഞ¸0â'),(3879,1,'user1','㋅'),(3879,1,'vi','喥'),(3880,1,'filters','ፅf'),(3880,1,'indicating','ᦤ'),(3880,1,'optimized','܁'),(3880,1,'persistent','ⳑѦƲ'),(3880,1,'technology','ຘ¹ࠩ'),(3881,1,'cron','╄'),(3881,1,'encryption','㗥'),(3881,1,'filters','᪸('),(3881,1,'indicating','ᵂ'),(3881,1,'interaction','ۆូ๰'),(3881,1,'law','⎊'),(3881,1,'optimized','௱'),(3881,1,'persistent','ಓ࿆'),(3881,1,'technology','⠍ć'),(3892,1,'collectors','ę'),(3892,1,'user1','²');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictD6` ENABLE KEYS */;

--
-- Table structure for table `dictD7`
--

DROP TABLE IF EXISTS `dictD7`;
CREATE TABLE `dictD7` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictD7`
--


/*!40000 ALTER TABLE `dictD7` DISABLE KEYS */;
LOCK TABLES `dictD7` WRITE;
INSERT INTO `dictD7` VALUES (3879,1,'strings','䖻'),(3879,1,'cancel','➷·Ыƿ'),(3877,1,'linux','Դ'),(3871,1,'hot','䞞'),(3871,1,'oct','⛤'),(3871,1,'regularly','㳡'),(3873,1,'strings','ɢM'),(3873,1,'cscript','ā'),(3872,1,'ditto','ʒ'),(3871,1,'strings','ᴒ'),(3871,1,'rrdtool','\nk\Z!%\rV8)	Ŀ	\r)äV5), éĨ»ƍK$#.[£ľ JÇҒ޽čME·ğ`Zçë ck\Zл/¦í3\"7ĬĦȊǝ/\rA0t5n-{e̚\ZwÈ\n\n\n\nIĽBà©ĝԉWΉÔA$66Íƞ87ģ/$T1P&#>3'),(3881,1,'collagedb','଼ÉC< î4'),(3881,1,'asc','᨝'),(3880,1,'rrdtool','˒䎂'),(3880,1,'regularly','ែ'),(3880,1,'originally','ⴔѥ'),(3880,1,'linux','ާẨ'),(3879,1,'tktauthloginurl','ൖI'),(3881,1,'getmonitorlistbydevice','ን'),(3871,1,'cv','ⳗ.ɕ\'\n'),(3871,1,'920805000','㠣;Dᚢ6'),(3871,1,'920804700','㠡9Fᚵ>#'),(3863,1,'linux','-'),(3862,1,'linux','¡'),(3721,1,'ab','3'),(3721,1,'originally','Ǩ'),(3727,1,'cv','>'),(3780,1,'csprocs','l'),(3827,1,'strings',','),(3831,1,'linux','¬'),(3879,1,'rrdtool','ͨၛ	⺨Ε3:ġOƗ)'),(3879,1,'misccommand','䐔'),(3879,1,'membership','䅆'),(3881,1,'linux','❟඘'),(3881,1,'menucommand','㠢'),(3881,1,'strings','᝟©'),(3881,1,'tktauthloginurl','㙑5Ǻ'),(3883,1,'cancel','¨'),(3885,1,'linux','ŉ؜!-'),(3885,1,'regularly','ϝŜ\Z'),(3887,1,'cancel','Өӹķµѻ'),(3887,1,'regularly','؛ş\Z'),(3888,1,'membership','җ'),(3889,1,'linux','ѯ@'),(3890,1,'cancel','ó᷿'),(3890,1,'linux','ො'),(3890,1,'regularly','ନɦҋŚ'),(3891,1,'cancel','³̈Æ'),(3892,1,'cancel','è'),(3871,2,'rrdtool',''),(3871,6,'rrdtool','僜'),(3871,7,'rrdtool','僛');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictD7` ENABLE KEYS */;

--
-- Table structure for table `dictD8`
--

DROP TABLE IF EXISTS `dictD8`;
CREATE TABLE `dictD8` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictD8`
--


/*!40000 ALTER TABLE `dictD8` DISABLE KEYS */;
LOCK TABLES `dictD8` WRITE;
INSERT INTO `dictD8` VALUES (3871,1,'point','঩Cʰਂ⨀Ÿࡳ'),(3871,1,'performed','׾'),(3871,1,'caveat','ᬆ'),(3871,1,'cmu','䀍'),(3871,1,'collection','㸺'),(3871,1,'disabled','㺷'),(3880,1,'point','࢞'),(3879,1,'performed','ᶈॅ㩲˚'),(3879,1,'effective','▝ઞ'),(3879,1,'disabled','ऩysಙだᢱ('),(3881,1,'collagehostgroupquery','࣊?̏ޙ'),(3880,1,'sign','ᑦ'),(3827,1,'logic','ú'),(3825,1,'preferred','y'),(3813,1,'java','a'),(3881,1,'logic','ἕ૗דȚ'),(3881,1,'java','ߤᐲȉąȄ֑°#8<'),(3751,1,'point','ȡ'),(3721,1,'collection','b'),(3881,1,'collection','⦋'),(3879,1,'point','ጋkKŇĆ̅ђ͕⎿'),(3871,1,'sign','Ⓒƽ'),(3872,1,'collection','ࡻ'),(3879,1,'collection','∴፝'),(3879,1,'cgis','㍲Ѫ᥈'),(3878,1,'performed','Ň'),(3877,1,'point','ͦɒ'),(3877,1,'performed','Έß'),(3873,1,'collection','nK'),(3872,1,'issuing','ؖ'),(3881,1,'performed','ἶ'),(3871,1,'preferred','㿧'),(3880,1,'performed','ⅳ'),(3880,1,'logic','༢'),(3880,1,'eleven','㮻'),(3880,1,'disabled','౾ᑂ0m#ۄˬœŋŅƿů'),(3880,1,'collection','ၲĵ'),(3880,1,'cgis','Ꮓ'),(3879,1,'sign','ݭÂϿò(!(ܝ ğp\n4㬻'),(3881,1,'pieces','ほ'),(3881,1,'point','ࠀ⢑'),(3881,1,'sign','ľ╺ɚǋࢭ0%dRǰJčÜ'),(3884,1,'cgis','ጴ'),(3884,1,'collection','ࢉ'),(3885,1,'collection','ݒ'),(3885,1,'performed','Ո'),(3887,1,'cgis','సRN'),(3887,1,'performed','މ'),(3888,1,'performed','հ'),(3888,1,'preferred','ӂ'),(3889,1,'effective','ÇW'),(3889,1,'point','˅'),(3890,1,'cgis','ǩ 4(^iͭĄ@'),(3890,1,'disabled','०Pprrʩ܃ըI'),(3890,1,'effective','ॺ'),(3892,1,'dollar','Ŧ\r'),(3892,1,'point','h'),(3892,1,'sign','ŧ\r');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictD8` ENABLE KEYS */;

--
-- Table structure for table `dictD9`
--

DROP TABLE IF EXISTS `dictD9`;
CREATE TABLE `dictD9` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictD9`
--


/*!40000 ALTER TABLE `dictD9` DISABLE KEYS */;
LOCK TABLES `dictD9` WRITE;
INSERT INTO `dictD9` VALUES (3829,8,'1','t'),(3877,1,'takes','ύ'),(3871,1,'732','䉧b'),(3826,8,'1','#'),(3798,8,'1','í'),(3797,8,'1','i'),(3740,8,'1','ƅ'),(3821,8,'1','8'),(3742,1,'1','u'),(3881,1,'lot','ႷᢝȢˁҢ'),(3881,1,'finished','ઞ'),(3768,8,'1','&'),(3789,8,'1',''),(3828,8,'1',''),(3884,1,'5','Ăߵ'),(3814,8,'1','ě'),(3810,8,'1','+'),(3809,8,'1','a'),(3774,8,'1','('),(3832,8,'1','K'),(3831,8,'1','Î'),(3791,8,'1','­'),(3822,8,'1','ü'),(3756,8,'1','ò'),(3755,8,'1','¼'),(3734,8,'1','ú'),(3735,8,'1','t'),(3736,8,'1','º'),(3737,8,'1','Ę'),(3738,8,'1','~'),(3732,8,'1','ę'),(3733,8,'1',''),(3731,8,'1',''),(3730,8,'1',''),(3729,8,'1','Ñ'),(3728,8,'1','Ę'),(3727,8,'1','ľ'),(3725,8,'1','^'),(3726,8,'1',''),(3724,8,'1','d'),(3723,8,'1','A'),(3892,1,'1','/'),(3847,6,'digitemp','^'),(3720,8,'1','Ƹ'),(3721,8,'1','ʙ'),(3722,8,'1','Š'),(3760,8,'1','@'),(3761,8,'1','ě'),(3762,8,'1','Ü'),(3763,8,'1','Ķ'),(3764,8,'1',';'),(3765,8,'1','Û'),(3766,8,'1','î'),(3767,8,'1','%'),(3752,8,'1','Č'),(3771,8,'1','#'),(3752,1,'1','ö'),(3801,8,'1','3'),(3794,8,'1','$'),(3793,8,'1','¥'),(3783,8,'1','Ô'),(3784,8,'1','ě'),(3890,1,'lot','ए'),(3739,8,'1','Ę'),(3769,8,'1','#'),(3888,1,'5','i٭'),(3889,1,'1','IƳȉ-'),(3889,1,'5','ħˢ'),(3889,1,'prevent','Ƒ'),(3889,1,'takes','Σ'),(3888,1,'1','IöлťV2ŋ'),(3884,1,'lot','۸'),(3884,1,'external','৪'),(3879,1,'5','P^v>>$ːĢ-ĘؾZ͍ÒɊu˨ªîQB* =\rˀ§ᛴ୥~эƁǞథԏȋb´F'),(3879,1,'c2','䵵'),(3879,1,'external','षਿ˫E՛ŗ'),(3879,1,'finished','℡ࡐGƜ'),(3879,1,'takes','㇒ʇ'),(3880,1,'1',':^$\r2-Ȥ4čOTåஃ+ŎÿãS൛;࿆ͅ௤Ï\r'),(3880,1,'5','*\\L WȬ4Ĕſධŵ൧*ฝᅸ'),(3880,1,'external','໿ȑ࿊ੲҟ)'),(3880,1,'finished','අ'),(3880,1,'prevent','ኔ᝔)X)xŔÐŘ(o)ɍ}'),(3880,1,'resolved','༫'),(3881,1,'1','($.!ŤOTčG(¦ӫᄾ݅Ó%ƘT!ڑۭ'),(3871,1,'possibility','䐙'),(3871,1,'prevent','፲৚ॻ⛐'),(3871,1,'manpage','ɺ\Z\Z*àƸٽƦ႞^_èف×Ŀ6ʡΰ'),(3871,1,'lot','ɊȂ㕐ָȰູ{'),(3871,1,'ground','ᖳ'),(3871,1,'executable','䨵'),(3871,1,'byte','ᖇổ/'),(3835,1,'1','o\n'),(3831,1,'1','¾'),(3834,1,'1',''),(3825,8,'1','ŀ'),(3824,8,'1','Ŕ'),(3807,8,'1','>'),(3806,8,'1','o'),(3805,8,'1','Ê'),(3804,8,'1','²'),(3827,8,'1','˒'),(3820,8,'1','Ŋ'),(3819,8,'1','ĝ'),(3818,8,'1','ě'),(3817,8,'1',','),(3750,8,'1','3'),(3749,8,'1','('),(3748,8,'1','ě'),(3747,8,'1','X'),(3746,8,'1','0'),(3742,8,'1',''),(3743,8,'1',''),(3744,8,'1','ə'),(3745,8,'1','ù'),(3887,1,'1','y!Ġʬʽ%ȑŘҖ'),(3823,8,'1','É'),(3802,8,'1','9'),(3876,1,'1',''),(3876,1,'5','Ţ'),(3877,1,'5','آ'),(3877,1,'lot','Ϭ'),(3796,8,'1','&'),(3753,8,'1',''),(3890,1,'1','Ɯ)؀ՓGŶUȚc?̂ٓ'),(3887,1,'5','̘ī੪'),(3881,1,'takes','ۤ᤭ઘމn'),(3882,1,'1','×'),(3882,1,'lab','ø'),(3883,1,'1','5'),(3884,1,'1','uŊऊāҶƗ¨'),(3890,1,'external','ਆost[6$\'ʪ'),(3890,1,'possibility','ᬂ'),(3812,8,'1','0'),(3874,1,'5','Âƫ\r'),(3871,1,'5','ӡٖèՆՕ૘I࢐ા_াĜ-\rK)W×ҥĊՁ'),(3811,8,'1','Ã'),(3777,8,'1','\''),(3781,8,'1',''),(3799,8,'1','ƌ'),(3795,8,'1',''),(3785,8,'1','ě'),(3787,8,'1','§'),(3740,1,'1','È'),(3778,8,'1','õ'),(3878,1,'1','ϱ'),(3872,1,'5','Ūº(\'°'),(3788,8,'1','Ù'),(3772,8,'1','#'),(3803,8,'1','Û'),(3890,1,'5','ኀ'),(3875,1,'1','bG&Ç	\n		&'),(3741,1,'1',''),(3881,1,'5','<x¨ūƩj¸өċူ'),(3881,1,'clause','㙂'),(3881,1,'completeness','ᾛ'),(3881,1,'external','⇢'),(3813,8,'1',''),(3757,8,'1','Ƥ'),(3887,1,'lot','൯'),(3816,8,'1','O'),(3786,8,'1','<'),(3773,8,'1','\''),(3776,8,'1','Ĥ'),(3887,1,'takes','ʪtଝx'),(3800,8,'1','B'),(3830,8,'1',''),(3871,1,'takes','ࠜߡ܇©Ǽె⁓ݤ'),(3741,8,'1','·'),(3885,1,'1','ēϭ%ʒ'),(3808,8,'1','w'),(3780,8,'1','Ƭ'),(3754,8,'1','̌'),(3891,1,'1','Ḱ½'),(3758,8,'1','?'),(3759,8,'1','û'),(3790,8,'1','P'),(3872,1,'excess','˪'),(3873,1,'1','ŁƟ!\"!!%\'%%%)()((&'),(3874,1,'1','ɫ'),(3815,8,'1','i'),(3779,8,'1','1'),(3751,8,'1','ʨ'),(3775,8,'1','&'),(3770,8,'1','#'),(3872,1,'1','Ō\"0ĪD*,,17˂įƽ\nū'),(3744,1,'1','Ƙ'),(3879,1,'1','>²L)ˁOT-ß>\rجZ̲ÒȧĂ̬ÇQB* =\rśǚ¡ގǽ<Ƌ̮޾¾ʾࡢ=ǏâƎdįCQĢ	%͒˳ǥ¿@nǊC.Ŭ˪ZȰ(!=S\r9$<Ƹ*$n%\r%á)̕4'),(3792,8,'1','a'),(3782,8,'1','Ɠ'),(3890,1,'finished','Ẳ'),(3836,1,'1',''),(3841,1,'1','^'),(3841,1,'5','n'),(3844,1,'139',''),(3847,1,'digitemp','\r'),(3856,1,'1','_'),(3856,1,'c2','ř'),(3856,1,'external','¨'),(3864,1,'5','Z'),(3868,1,'1','6'),(3868,1,'takes',''),(3869,1,'external',''),(3869,1,'takes','F'),(3870,1,'1','Å'),(3870,1,'5','T'),(3871,1,'1','Ý̴·ԈaĹıƾÈͭ@Ø;Ø¤ǨȂ˚ŰÅÌø\nUIĥԎ®ȫóԣXŦࡪ[?,(bK֌|u_ŴY]ǽ:#'),(3752,1,'5','ý'),(3753,1,'prevent','Z'),(3754,1,'1','º$$Î	'),(3755,1,'1','_'),(3762,1,'5','g'),(3765,1,'1','¯'),(3765,1,'5','T'),(3766,1,'1','a'),(3766,1,'5','g'),(3780,1,'5','a	'),(3789,1,'5',''),(3799,1,'1',''),(3816,1,'takes',''),(3819,1,'1','@'),(3820,1,'1','A'),(3822,1,'5','P'),(3824,1,'1','Ĭ'),(3824,1,'takes','Ù'),(3825,1,'1','Ĳ'),(3825,1,'takes','ú'),(3827,1,'1','ò'),(3827,1,'5','ɡ'),(3828,1,'1','B'),(3828,1,'5','-'),(3833,8,'1','ě'),(3834,8,'1','¶'),(3835,8,'1',''),(3836,8,'1','`'),(3837,8,'1','Ģ'),(3838,8,'1','?'),(3839,8,'1','1'),(3840,8,'1','̕'),(3841,8,'1','ª'),(3842,8,'1',':'),(3843,8,'1','f'),(3844,8,'1','Ò'),(3845,8,'1','v'),(3846,8,'1','ĵ'),(3847,8,'1','Q'),(3848,8,'1','¬'),(3849,8,'1','x'),(3850,8,'1','F'),(3851,8,'1',''),(3852,8,'1','\''),(3853,8,'1','O'),(3854,8,'1','Ŀ'),(3855,8,'1','6'),(3856,8,'1','ƌ'),(3857,8,'1','['),(3858,8,'1','0'),(3859,8,'1','>'),(3860,8,'1','C'),(3861,8,'1','.'),(3862,8,'1','Ė'),(3863,8,'1','ĸ'),(3864,8,'1',''),(3865,8,'1',''),(3866,8,'1','³'),(3867,8,'1',','),(3868,8,'1','Ē'),(3869,8,'1','ĕ'),(3870,8,'1','ğ'),(3871,8,'1','僒'),(3872,8,'1','೫'),(3873,8,'1','՟'),(3874,8,'1','ϩ'),(3875,8,'1','̿'),(3876,8,'1','Ʀ'),(3877,8,'1','އ'),(3878,8,'1','ा'),(3879,8,'1','本'),(3880,8,'1','䠺'),(3881,8,'1','㦺'),(3882,8,'1','ƪ'),(3883,8,'1','Ņ'),(3884,8,'1','ᑞ'),(3885,8,'1','৕'),(3886,8,'1','Բ'),(3887,8,'1','ᄌ'),(3888,8,'1','इ'),(3889,8,'1','ӷ'),(3890,8,'1','ᾋ'),(3891,8,'1','ن'),(3892,8,'1','Ƽ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictD9` ENABLE KEYS */;

--
-- Table structure for table `dictDA`
--

DROP TABLE IF EXISTS `dictDA`;
CREATE TABLE `dictDA` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictDA`
--


/*!40000 ALTER TABLE `dictDA` DISABLE KEYS */;
LOCK TABLES `dictDA` WRITE;
INSERT INTO `dictDA` VALUES (3880,1,'issue','׿'),(3880,1,'discuss','လ׮'),(3880,1,'changelog','ᖑ'),(3879,1,'submitted','֝'),(3881,1,'helloworld','⴨!şȤŀ'),(3881,1,'discuss','⟤ಖ'),(3881,1,'conflict','ⶠ'),(3880,1,'weekly','㞹Οрɥ'),(3880,1,'submitted','֠'),(3879,1,'sheet','ฺ'),(3879,1,'scale','㿻'),(3879,1,'rename','⽣៦'),(3879,1,'recover','৾'),(3879,1,'physical','⏡ኵ'),(3879,1,'joe','ᳵ'),(3858,1,'bgpstate',''),(3870,1,'changelog','č'),(3871,1,'12345','㖤ȹE:Ǿิ'),(3871,1,'bps','㔰Ĩ'),(3831,1,'xqf','¶'),(3875,1,'issue','H'),(3877,1,'issue','ɣԞ'),(3877,1,'permissions','Əǁ$$ĩŨR7'),(3825,1,'directed','Å'),(3881,1,'permissions','⎯'),(3872,1,'submitted','ӹć'),(3872,1,'permissions','ǐ'),(3871,1,'weekly','䊆'),(3871,1,'scale','ಁ'),(3871,1,'rigid','ཫ֩9㩒#'),(3880,1,'physical','ိm⦰ˁ['),(3803,1,'permissions','Ñ'),(3765,1,'issue','7'),(3763,1,'issue','f'),(3880,1,'rename','㗛'),(3880,1,'reinforces','ᔫ'),(3759,1,'eat','	'),(3756,1,'issue','»'),(3752,1,'issue','¼'),(3879,1,'discuss','ށҴ'),(3879,1,'comparing','䲡'),(3881,1,'issue','Џ'),(3880,1,'newyork','႟'),(3879,1,'9480','䟿'),(3879,1,'issue','׼䓒ડ੧έ×ñ'),(3878,1,'issue','ϗ'),(3871,1,'isn','㻢'),(3871,1,'rename','⡸Ç'),(3881,1,'physical','܎'),(3881,1,'scale','ᰰ'),(3881,1,'submitted','ΰ'),(3883,1,'rename','¢'),(3884,1,'rename','Ƭਖքʩ'),(3885,1,'rename','Ȅظ'),(3886,1,'rename','m'),(3887,1,'physical','ɧ஑'),(3887,1,'rename','ӢӹǬ'),(3888,1,'permissions','͏'),(3888,1,'rename','ë'),(3889,1,'recover','ɍ'),(3890,1,'issue','щ¡\rWe$'),(3890,1,'permissions','ࢪԵ'),(3890,1,'physical','ǌ-Ãα'),(3890,1,'rigid','ᑏ'),(3890,1,'submitted','෉'),(3890,1,'weekly','೅'),(3891,1,'rename','­̈Æ'),(3858,6,'bgpstate','=');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictDA` ENABLE KEYS */;

--
-- Table structure for table `dictDB`
--

DROP TABLE IF EXISTS `dictDB`;
CREATE TABLE `dictDB` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictDB`
--


/*!40000 ALTER TABLE `dictDB` DISABLE KEYS */;
LOCK TABLES `dictDB` WRITE;
INSERT INTO `dictDB` VALUES (3887,1,'modified','әӷļ²Ѿ'),(3871,1,'flows','䠟'),(3721,1,'connectivity','Ȑ'),(3721,1,'iso','ç'),(3722,1,'perl',''),(3741,1,'perl','d'),(3871,1,'iso','❩ែ'),(3880,1,'perl','䙇'),(3880,1,'modified','ૃ'),(3881,1,'perl','¸ϱ\n¢ǇĂ\nǱᎾfˀǝո(Ά'),(3881,1,'modified','ଇ▰ľ'),(3881,1,'labeled','ㆧ'),(3880,1,'trees','஻ɷ'),(3890,1,'indicates','᧱2'),(3885,1,'modified','ࣨZ'),(3888,1,'perl','ˋ'),(3862,1,'perl','n'),(3867,1,'modified',''),(3869,1,'perl','	  \n	\n	\n'),(3871,1,'establish','⎆'),(3887,1,'trees','ੰ\''),(3884,1,'perl','঵'),(3883,1,'modified',''),(3871,1,'labeled','㨝'),(3871,1,'modified','⟸ը'),(3871,1,'perl','wǱ⃃᨟'),(3871,1,'worth','ှᆱࡋᥢΑ֝'),(3872,1,'perl','ǥ̏'),(3872,1,'trees','ࢋ'),(3879,1,'guided','₳י'),(3879,1,'indicates','⧞Ż'),(3879,1,'modified','࿣ίޞௐ'),(3879,1,'nagiosdowntime','፨'),(3879,1,'perl','⃞Ⓣգߴശ'),(3879,1,'trees','ℙ̀,हศ:Ī^'),(3880,1,'indicates','᧗ە0J##'),(3889,1,'trees','ʓ'),(3858,1,'perl',''),(3855,1,'perl',''),(3752,1,'perl','V'),(3757,1,'perl','Ũ'),(3786,1,'perl',''),(3792,1,'connectivity',''),(3797,1,'perl',''),(3801,1,'perl',''),(3802,1,'perl',''),(3804,1,'perl',''),(3815,1,'perl',''),(3819,1,'authpass','È'),(3820,1,'authpass','o'),(3824,1,'perl',''),(3825,1,'perl',''),(3828,1,'lvol1','h'),(3844,1,'perl','\Z'),(3850,1,'perl',''),(3890,1,'perl','ŏ'),(3890,1,'worth','๸'),(3891,1,'modified','¤̆Æ'),(3869,2,'perl',''),(3869,6,'perl','ğ'),(3869,7,'perl','Ğ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictDB` ENABLE KEYS */;

--
-- Table structure for table `dictDC`
--

DROP TABLE IF EXISTS `dictDC`;
CREATE TABLE `dictDC` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictDC`
--


/*!40000 ALTER TABLE `dictDC` DISABLE KEYS */;
LOCK TABLES `dictDC` WRITE;
INSERT INTO `dictDC` VALUES (3886,1,'single','϶hd'),(3881,1,'single','ļӓ࡛A>B>?EBBXEO@AA࢜௻ɛǋࢭ/dRǰJĎÜ'),(3724,8,'127','a'),(3720,8,'127','Ƶ'),(3887,1,'server','ࡗ'),(3721,1,'version','Ēp'),(3881,1,'log','ၲ\rjרּ+˹0¼3/̇®'),(3809,1,'version','0'),(3880,1,'familiar','ί䉍'),(3734,8,'127','÷'),(3804,1,'server','%'),(3725,1,'version',':'),(3831,1,'server','z'),(3831,1,'version','8'),(3833,1,'server','}'),(3833,1,'version','V'),(3834,1,'version','L'),(3751,8,'127','ʥ'),(3752,8,'127','ĉ'),(3753,8,'127','~'),(3754,8,'127','̉'),(3755,8,'127','¹'),(3756,8,'127','ï'),(3757,8,'127','ơ'),(3758,8,'127','<'),(3759,8,'127','ø'),(3760,8,'127','='),(3782,1,'server','%ř'),(3780,1,'server','!_Ö'),(3780,1,'version','?º='),(3778,1,'version','<j'),(3778,1,'server','GY'),(3766,1,'version','D'),(3776,1,'version','A'),(3766,1,'server','$µ'),(3763,1,'version','2'),(3765,1,'version','L'),(3883,1,'morning','ĝ'),(3885,1,'server','ؖ'),(3879,1,'morning','⻲'),(3853,1,'version','G'),(3775,8,'127','#'),(3888,1,'version','ʡ'),(3880,1,'log','ఖ;0Ϩ▛୙²'),(3880,1,'version','1ŧӬ&Õcᰶₔ'),(3854,1,'server','¡'),(3727,8,'127','Ļ'),(3800,8,'127','?'),(3782,1,'version','z'),(3849,1,'server','4'),(3748,1,'server','}'),(3762,8,'127','Ù'),(3881,1,'restricted','⡈'),(3872,1,'1234567','ԧ'),(3765,8,'127','Ø'),(3872,1,'fails','̵7'),(3744,1,'version','n'),(3887,1,'proceed','δ'),(3871,1,'log','ƻɄĥ኶'),(3873,1,'server','ƀM	F\'P'),(3840,1,'server',')	#\nƃ'),(3850,1,'version','2'),(3835,1,'server','a'),(3835,1,'single','`\n'),(3835,1,'version','*\"'),(3836,1,'version',':'),(3811,1,'version','@D'),(3722,8,'127','ŝ'),(3792,6,'log','o'),(3794,1,'version',''),(3795,1,'version','0'),(3796,1,'version','!'),(3797,1,'log','L'),(3880,1,'searching','䑏ˈx'),(3784,1,'version','V'),(3728,1,'server','z'),(3728,1,'version','S'),(3729,1,'server','s'),(3729,1,'version','U'),(3879,1,'log','࢒ē±ȯHɞШɓ˰Πj<❶ϼ3Ə ŕÐѤ`yC\'NO	:.B-Lb*ջó˗@Ď	H\r	\r\'\''),(3879,1,'fails','⡿'),(3878,1,'version','('),(3871,1,'server','J¢\"ⶀ'),(3871,1,'searching','㌠'),(3770,8,'127',' '),(3849,1,'version','p'),(3798,1,'log','\'6%'),(3797,1,'server','O'),(3791,8,'127','ª'),(3793,8,'127','¢'),(3792,8,'127','^'),(3745,1,'version','A'),(3745,1,'server','j'),(3807,6,'log','K'),(3873,1,'log','ө\r'),(3838,1,'version','7'),(3837,1,'server','\'s'),(3871,1,'fails','ች'),(3886,1,'fails','ū'),(3762,1,'version',':'),(3782,8,'127','Ɛ'),(3880,1,'proceed','ఓ'),(3848,1,'server',')'),(3846,1,'version','P'),(3779,8,'127','.'),(3732,8,'127','Ė'),(3805,1,'version','$'),(3806,1,'log','\Z'),(3806,1,'version','3'),(3807,1,'log','	'),(3807,1,'version','-	'),(3753,1,'version','3\n'),(3754,1,'log','\Z	Ė'),(3871,1,'morning','ᆇᕺ'),(3803,1,'version',''),(3799,1,'version','L'),(3724,1,'version','.'),(3722,1,'server','?'),(3805,1,'log','\Z.'),(3818,1,'version','V'),(3728,8,'127','ĕ'),(3780,8,'127','Ʃ'),(3781,8,'127',''),(3879,1,'version','1ቨ]ۘEؘ⍰๺๧'),(3753,1,'server','A'),(3723,8,'127','>'),(3887,1,'fails','Ɠ'),(3772,8,'127',' '),(3797,8,'127','f'),(3879,1,'searching','㉆'),(3879,1,'restricted','ᨴ'),(3795,8,'127','~'),(3875,1,'log','ʠ'),(3874,1,'server','?ěǊ'),(3778,8,'127','ò'),(3768,8,'127','#'),(3767,8,'127','\"'),(3799,1,'log',')&k'),(3764,8,'127','8'),(3763,8,'127','ĳ'),(3751,1,'version','{'),(3750,1,'server','!'),(3751,1,'single','Ĵ-'),(3881,1,'isconnected','ാ'),(3796,8,'127','#'),(3881,1,'server','׏ǾÙϝPՃ\r	\n\rèఀصҐ=૩'),(3729,8,'127','Î'),(3890,1,'inter','ᆉi2¦´'),(3889,1,'server','Ѱ'),(3721,1,'server','ĝ\n\n	\Z/'),(3720,1,'single','µ'),(3808,1,'version','n'),(3808,1,'server','>'),(3890,1,'server','ĦɽHJ7ञ٢ः'),(3871,1,'single','ԩɗ̺q΍ᕨ¥Hú࣫࿱'),(3786,8,'127','9'),(3731,8,'127',''),(3730,8,'127',''),(3754,1,'version','qŉ'),(3755,1,'server','['),(3755,1,'version','7'),(3756,1,'server','\Z]'),(3756,1,'version','A'),(3758,1,'version','*'),(3759,1,'version',':'),(3761,1,'server','}'),(3761,1,'version','V'),(3730,1,'inter','@'),(3731,1,'version','9'),(3732,1,'server','{'),(3732,1,'version','T'),(3733,1,'log','7\Z'),(3733,1,'version','g'),(3734,1,'server','¶'),(3734,1,'version','ñ'),(3735,1,'server','>'),(3735,1,'version','k'),(3736,1,'version','6'),(3737,1,'server','z'),(3737,1,'version','S'),(3738,1,'server','\ZK'),(3738,1,'version','()\n'),(3739,1,'server','z'),(3739,1,'version','S'),(3740,1,'version','W)'),(3742,1,'version','64'),(3798,1,'version','|'),(3748,1,'version','V'),(3820,1,'version','@Ä'),(3821,1,'version','3'),(3823,1,'server','&	.'),(3823,1,'version','Ä'),(3824,1,'server','fX!'),(3825,1,'server','_7*$'),(3827,1,'server','îĸ*'),(3827,1,'version',''),(3828,1,'version','A'),(3829,1,'version','c'),(3830,1,'version','-'),(3818,1,'server','}'),(3814,1,'version','V'),(3814,1,'server','}'),(3769,8,'127',' '),(3853,1,'server','8'),(3851,1,'version','M3'),(3774,8,'127','%'),(3773,8,'127','$'),(3794,8,'127','!'),(3888,1,'group2','ࡖ'),(3771,8,'127',' '),(3736,8,'127','·'),(3735,8,'127','q'),(3880,1,'single','ᑥ'),(3880,1,'server','ࡀĚς\n̈ZǤŨıͤ՛͕ᤵˁ['),(3785,8,'127','Ę'),(3784,8,'127','Ę'),(3726,8,'127',''),(3725,8,'127','['),(3799,8,'127','Ɖ'),(3798,8,'127','ê'),(3848,1,'version','C'),(3746,1,'version','('),(3761,8,'127','Ę'),(3871,1,'vms','佱'),(3871,1,'version','ᨈڧᾐ'),(3777,8,'127','$'),(3776,8,'127','ġ'),(3878,1,'server','Ż\n,\r\r	\r\r	\r	\r\n\r\r\r\r\n\r\n\r\n\r\n		\n	\r\r\n^ \r$\r#\"\rN\r!!8\n'),(3877,1,'version','/'),(3877,1,'standardize','Ĕ'),(3877,1,'single','ڝ'),(3877,1,'server','àĺ4H9Ïo \"'),(3877,1,'log','ŸǱ'),(3876,1,'server','?'),(3801,8,'127','0'),(3879,1,'server','ርnykKȄːB!aəw÷ר˼ȰVଡ଼ǆǢᪿ\nȽଝ	'),(3733,8,'127',''),(3789,8,'127',''),(3721,8,'127','ʖ'),(3788,6,'log','è'),(3793,1,'version','='),(3785,1,'server','}'),(3785,1,'version','V'),(3787,1,'single',''),(3787,1,'version','b'),(3788,1,'log','/'),(3788,1,'version','WB'),(3789,1,'log',', \Z$'),(3789,1,'version','\\'),(3790,1,'server',''),(3790,1,'version','E'),(3791,1,'version','Q'),(3792,1,'log','	'),(3792,1,'server','2'),(3792,1,'version','-'),(3793,1,'server','z'),(3784,1,'server','}'),(3727,1,'version','M'),(3726,1,'single','d'),(3811,1,'server','\Z'),(3842,6,'dl','G'),(3881,1,'warranties','⎝'),(3881,1,'version','1૗ࡱඋౌjܱ'),(3744,1,'authpriv',''),(3783,8,'127','Ñ'),(3879,1,'single','ݬÁϿò(!(ܜ¡ğp\n4⠽֖֕ߓോ'),(3766,8,'127','ë'),(3890,1,'proceed','ƻؽޕև'),(3890,1,'log','ˁԼҊ\nˇ\r\n@6ಚľ'),(3788,8,'127','Ö'),(3787,8,'127','¤'),(3790,8,'127','M'),(3890,1,'version','Ąᱸ'),(3856,1,'restricted','ė'),(3856,1,'server','Ġ'),(3856,1,'version','@'),(3862,1,'version','r'),(3863,1,'version','@'),(3864,1,'version',';'),(3866,1,'server','+'),(3866,1,'version','u'),(3868,1,'version','ý'),(3869,1,'awk','P'),(3869,1,'server','õ'),(3869,1,'vms','C'),(3854,1,'version','Z'),(3845,1,'version','m'),(3845,1,'server','A'),(3840,1,'version','Z'),(3842,1,'dl',''),(3843,1,'server','0'),(3843,1,'version',']'),(3844,1,'log','V'),(3844,1,'server','>'),(3820,1,'authpriv','^'),(3819,1,'version','?Ó'),(3819,1,'authpriv','·'),(3750,8,'127','0'),(3742,8,'127',''),(3743,8,'127',''),(3744,8,'127','ɖ'),(3745,8,'127','ö'),(3746,8,'127','-'),(3747,8,'127','U'),(3748,8,'127','Ę'),(3749,8,'127','%'),(3741,8,'127','´'),(3740,8,'127','Ƃ'),(3737,8,'127','ĕ'),(3738,8,'127','{'),(3739,8,'127','ĕ'),(3802,8,'127','6'),(3803,8,'127','Ø'),(3804,8,'127','¯'),(3805,8,'127','Ç'),(3806,8,'127','l'),(3807,8,'127',';'),(3808,8,'127','t'),(3809,8,'127','^'),(3810,8,'127','('),(3811,8,'127','À'),(3812,8,'127','-'),(3813,8,'127',''),(3814,8,'127','Ę'),(3815,8,'127','f'),(3816,8,'127','L'),(3817,8,'127',')'),(3818,8,'127','Ę'),(3819,8,'127','Ě'),(3820,8,'127','Ň'),(3821,8,'127','5'),(3822,8,'127','ù'),(3823,8,'127','Æ'),(3824,8,'127','ő'),(3825,8,'127','Ľ'),(3826,8,'127',' '),(3827,8,'127','ˏ'),(3828,8,'127',''),(3829,8,'127','q'),(3830,8,'127',''),(3831,8,'127','Ë'),(3832,8,'127','H'),(3833,8,'127','Ę'),(3834,8,'127','³'),(3835,8,'127',''),(3836,8,'127',']'),(3837,8,'127','ğ'),(3838,8,'127','<'),(3839,8,'127','.'),(3840,8,'127','̒'),(3841,8,'127','§'),(3842,8,'127','7'),(3843,8,'127','c'),(3844,8,'127','Ï'),(3845,8,'127','s'),(3846,8,'127','Ĳ'),(3847,8,'127','N'),(3848,8,'127','©'),(3849,8,'127','u'),(3850,8,'127','C'),(3851,8,'127',''),(3852,8,'127','$'),(3853,8,'127','L'),(3854,8,'127','ļ'),(3855,8,'127','3'),(3856,8,'127','Ɖ'),(3857,8,'127','X'),(3858,8,'127','-'),(3859,8,'127',';'),(3860,8,'127','@'),(3861,8,'127','+'),(3862,8,'127','ē'),(3863,8,'127','ĵ'),(3864,8,'127',''),(3865,8,'127',''),(3866,8,'127','°'),(3867,8,'127',')'),(3868,8,'127','ď'),(3869,8,'127','Ē'),(3870,8,'127','Ĝ'),(3871,8,'127','像'),(3872,8,'127','೨'),(3873,8,'127','՜'),(3874,8,'127','Ϧ'),(3875,8,'127','̼'),(3876,8,'127','ƣ'),(3877,8,'127','ބ'),(3878,8,'127','ऻ'),(3879,8,'127','朩'),(3880,8,'127','䠷'),(3881,8,'127','㦷'),(3882,8,'127','Ƨ'),(3883,8,'127','ł'),(3884,8,'127','ᑛ'),(3885,8,'127','৒'),(3886,8,'127','ԯ'),(3887,8,'127','ᄉ'),(3888,8,'127','ऄ'),(3889,8,'127','Ӵ'),(3890,8,'127','ᾈ'),(3891,8,'127','ك'),(3892,8,'127','ƹ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictDC` ENABLE KEYS */;

--
-- Table structure for table `dictDD`
--

DROP TABLE IF EXISTS `dictDD`;
CREATE TABLE `dictDD` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictDD`
--


/*!40000 ALTER TABLE `dictDD` DISABLE KEYS */;
LOCK TABLES `dictDD` WRITE;
INSERT INTO `dictDD` VALUES (3833,1,'mismatches','­'),(3846,1,'unspecified','ë'),(3785,1,'mismatches','­'),(3784,1,'mismatches','­'),(3778,1,'ps','O'),(3761,1,'mismatches','­'),(3871,1,'substitute','㺐'),(3727,1,'supply','¦'),(3737,1,'mismatches','ª'),(3739,1,'mismatches','ª'),(3741,1,'ps','Z'),(3734,1,'ps','_'),(3732,1,'mismatches','«'),(3822,1,'echoreply','Þ'),(3827,1,'supply','ȭ'),(3814,1,'mismatches','­'),(3736,1,'allswaps','}'),(3871,1,'calculation','ąᣫÈⷅ'),(3854,1,'mismatches','Ñ'),(3869,1,'comprehensive',''),(3757,1,'ps','¶'),(3751,1,'calculation','Ȕ'),(3748,1,'mismatches','­'),(3744,1,'oids','Ʋa'),(3744,1,'delimited','ƻ'),(3743,1,'delimited','Z'),(3871,1,'xff','۰ɶ['),(3818,1,'mismatches','­'),(3871,1,'solar','㉬'),(3871,1,'oids','㽯\nǆS'),(3871,1,'extreme','ମ'),(3871,1,'derivative','ࡊ'),(3871,1,'totals','ڝ'),(3728,1,'mismatches','ª'),(3872,1,'touch','ϻ,'),(3872,1,'unspecified','˥'),(3879,1,'comprehensive','ʻ'),(3879,1,'delimited','ₖײࠩ'),(3879,1,'reside','ၞ௣㱿'),(3879,1,'unspecified','Ṟ'),(3880,1,'comprehensive','Ț'),(3880,1,'consolidates','㙕'),(3880,1,'drilldown','࣠'),(3880,1,'totals','ᩳṻ6f+ƲOT$	+̷gŢƕ'),(3881,1,'destroy','ࣹƧ'),(3881,1,'reside','୨⇋'),(3883,1,'delimited','Ü'),(3885,1,'instantiate','࣒R'),(3888,1,'reside','ͧ6&'),(3890,1,'calculation','ᆦOM]'),(3890,1,'ps','ˣ'),(3890,1,'recognizing','ᕥ'),(3890,1,'reside','Ǚ'),(3890,1,'supply','঄');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictDD` ENABLE KEYS */;

--
-- Table structure for table `dictDE`
--

DROP TABLE IF EXISTS `dictDE`;
CREATE TABLE `dictDE` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictDE`
--


/*!40000 ALTER TABLE `dictDE` DISABLE KEYS */;
LOCK TABLES `dictDE` WRITE;
INSERT INTO `dictDE` VALUES (3879,1,'firewalls','㟵΂'),(3868,1,'extensive','N'),(3868,1,'users','×'),(3880,1,'representing','ᱏ'),(3879,1,'users','Æ٘/LIR\nC?Õ\'AX*Ϳ੅ï>ƽuΠټଫௐȍ'),(3880,1,'editions','ڔĵ'),(3880,1,'extensive','ᖪ'),(3871,1,'5s2m','♨'),(3871,1,'thousands','፝㴓'),(3871,1,'relative','ૼԹᆱʌֿϼ'),(3871,1,'negative','ถȜण࡯ࡋ͏ᬵ̭qæÆ'),(3871,1,'ifoutoctets','䄺'),(3871,1,'hands','㏎'),(3875,1,'ifoutoctets','¼*'),(3751,1,'tc','ɒ'),(3755,1,'timeouts',' '),(3757,1,'children','×'),(3763,1,'users','ė'),(3766,1,'overcr','¹'),(3804,1,'users','¡'),(3815,1,'users',';'),(3819,1,'unused',''),(3822,1,'users','©'),(3827,1,'timeouts','Ȉ'),(3879,1,'hda1','ẩ'),(3879,1,'relative','刾'),(3879,1,'unused','敢c'),(3877,1,'users','ךé'),(3879,1,'architectural','ιēÅ'),(3877,1,'vary','Հ'),(3720,1,'refactoring','Ī'),(3878,1,'thousands','½'),(3745,1,'timeouts','Ý'),(3729,1,'timeouts','Ä'),(3721,1,'users','Ǔ'),(3725,1,'users',''),(3880,1,'users','͞;೓ۧࠞ┌Ĭ='),(3871,1,'vary','┘'),(3871,1,'users','㍣ᴋ'),(3881,1,'develop','⚎Ⴀ'),(3881,1,'users','⡂¼Ǭ५'),(3886,1,'numbered','ӝ'),(3887,1,'children','ହ'),(3887,1,'negative','˿க'),(3887,1,'relative','న'),(3887,1,'users','ੵ'),(3890,1,'users','іE-W($ެ'),(3890,1,'vary','˳ᄡ'),(3891,1,'users','˔˗'),(3725,6,'users','k'),(3766,6,'overcr','û');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictDE` ENABLE KEYS */;

--
-- Table structure for table `dictDF`
--

DROP TABLE IF EXISTS `dictDF`;
CREATE TABLE `dictDF` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictDF`
--


/*!40000 ALTER TABLE `dictDF` DISABLE KEYS */;
LOCK TABLES `dictDF` WRITE;
INSERT INTO `dictDF` VALUES (3890,1,'disables','ɀ૩'),(3884,1,'disables','ӬÙ23܉~>êÙe'),(3720,1,'teardown',''),(3720,1,'wipe',''),(3736,1,'solaris',''),(3862,1,'reachability','}'),(3869,1,'mark','l'),(3871,1,'260','ኆ$'),(3871,1,'29','▞E\"'),(3871,1,'mark','ૌHᅈ'),(3877,1,'solaris','­\r¶\rÛ ȧ'),(3878,1,'29','Ӌ'),(3879,1,'distinguished','᰹'),(3880,1,'materials','䐅d2'),(3881,1,'driven','⚒\Z'),(3881,1,'impl','⏸');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictDF` ENABLE KEYS */;

--
-- Table structure for table `dictE0`
--

DROP TABLE IF EXISTS `dictE0`;
CREATE TABLE `dictE0` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictE0`
--


/*!40000 ALTER TABLE `dictE0` DISABLE KEYS */;
LOCK TABLES `dictE0` WRITE;
INSERT INTO `dictE0` VALUES (3769,7,'packages','%'),(3768,7,'packages','('),(3767,7,'packages','\''),(3766,7,'packages','ð'),(3763,7,'packages','ĸ'),(3764,7,'packages','='),(3765,7,'packages','Ý'),(3786,7,'packages','>'),(3785,7,'packages','ĝ'),(3807,7,'packages','@'),(3805,7,'packages','Ì'),(3787,7,'packages','©'),(3829,7,'packages','v'),(3828,7,'packages',''),(3827,7,'packages','˔'),(3795,7,'packages',''),(3779,7,'packages','3'),(3792,7,'packages','c'),(3881,1,'packages','⑯F8iEİǫȧ	JȣĄȤŀܗ'),(3770,7,'packages','%'),(3803,7,'packages','Ý'),(3749,7,'packages','*'),(3750,7,'packages','5'),(3751,7,'packages','ʪ'),(3752,7,'packages','Ď'),(3753,7,'packages',''),(3754,7,'packages','̎'),(3755,7,'packages','¾'),(3888,1,'cfg','®ƝĔҀ5	'),(3883,1,'optional','Ô'),(3884,1,'obsessive','Ε਋'),(3884,1,'optional','ŷvĩa$è;//#3@ú׆a$è;//#3Űë\Z!'),(3885,1,'optional','Ōø=A$3!11ÏÉU!:ăI½'),(3886,1,'optional','ǗMJ.'),(3887,1,'optional','ƨA8F/Á\rÏ$3!11ÒÉU!:Ăĺ\'Ņ}J!_¡¢'),(3823,7,'packages','Ë'),(3822,7,'packages','þ'),(3820,7,'packages','Ō'),(3821,7,'packages',':'),(3819,7,'packages','ğ'),(3811,7,'packages','Å'),(3812,7,'packages','2'),(3813,7,'packages',''),(3814,7,'packages','ĝ'),(3815,7,'packages','k'),(3816,7,'packages','Q'),(3817,7,'packages','.'),(3818,7,'packages','ĝ'),(3879,1,'packages','ކĠ۬0\Z=12(Ѻ,䁍Ċ̍\"Cå'),(3879,1,'power','ϑ㯟'),(3879,1,'round','䊝'),(3879,1,'optional','⣢ׇú.έӳ֮ܽᏍ¾'),(3879,1,'node','ⱅ'),(3879,1,'gwir','冣ʚñ'),(3879,1,'fixes','ۥ'),(3879,1,'filename','姍/±'),(3879,1,'column','倾ด	'),(3879,1,'cfg','㺳Էඏ­1ƴNPS୘'),(3873,1,'cfg','Ʀ*[\\'),(3875,1,'application','ɳ'),(3877,1,'cfg','ά'),(3877,1,'challenge','ܥ'),(3879,1,'application','̐Į˚ċT&ē±ÿL.!7ƽVHT3?Ɯ5\n*	ś×8\ZG\r\'\n	:9\nF.-!ĩӑĈ@̠ÍʟᬨėƊأɒु43gÉsˮª*qͥ'),(3879,1,'authenticate','ᰗ'),(3879,1,'bind','᳥'),(3777,7,'packages',')'),(3776,7,'packages','Ħ'),(3775,7,'packages','('),(3772,7,'packages','%'),(3773,7,'packages',')'),(3774,7,'packages','*'),(3726,7,'packages',''),(3723,7,'packages','C'),(3872,1,'optional','ਾ)â.'),(3825,7,'packages','ł'),(3872,1,'cfg','ö׊ǻǲű'),(3809,7,'packages','c'),(3881,1,'column','ᗈû'),(3881,1,'couldn','⑃'),(3881,1,'getmessagefilter','࿣'),(3881,1,'getoperationstatus','ྜྷ'),(3881,1,'gwir','╖'),(3871,1,'progressive','᏶'),(3871,1,'round','5ƾvǪ?\rRâã&ɵụ:ࠢʮ̅1'),(3780,7,'packages','Ʈ'),(3756,7,'packages','ô'),(3727,7,'packages','ŀ'),(3728,7,'packages','Ě'),(3890,1,'filename','ɒ'),(3890,1,'obsessive','᛿)'),(3890,1,'optional','ɩӯĝ'),(3891,1,'optional','á.ąâ;ʜ<'),(3892,1,'optional','H'),(3724,6,'vcs','q'),(3790,6,'mysqlslave',']'),(3720,7,'packages','ƺ'),(3721,7,'packages','ʛ'),(3722,7,'packages','Ţ'),(3881,1,'authenticate','⡁kkୋҩ'),(3871,1,'optional','ࣰᬃ'),(3871,1,'power','㉮'),(3796,7,'packages','('),(3808,7,'packages','y'),(3806,7,'packages','q'),(3798,7,'packages','ï'),(3799,7,'packages','Ǝ'),(3720,1,'node','¿'),(3793,7,'packages','§'),(3881,1,'optional','᧖࢔ࣨхए]'),(3880,1,'column','᭛´'),(3802,7,'packages',';'),(3790,7,'packages','R'),(3810,7,'packages','-'),(3758,7,'packages','A'),(3881,1,'cfg','╗'),(3721,1,'application','ƊÀ'),(3757,7,'packages','Ʀ'),(3888,1,'optional','Ć'),(3889,1,'cfg','Ҧ'),(3889,1,'optional','ǅ,'),(3890,1,'application','ၥ'),(3890,1,'cfg','ēۏᕢØf'),(3826,7,'packages','%'),(3781,7,'packages',''),(3782,7,'packages','ƕ'),(3729,7,'packages','Ó'),(3730,7,'packages',''),(3731,7,'packages',''),(3732,7,'packages','ě'),(3733,7,'packages',''),(3734,7,'packages','ü'),(3735,7,'packages','v'),(3736,7,'packages','¼'),(3737,7,'packages','Ě'),(3738,7,'packages',''),(3739,7,'packages','Ě'),(3880,1,'application','ɺ҂ǌõ\Zκ@3\rƀ(޿Ŋ-ऌፂ̾Ǆˁ[יљ?'),(3771,7,'packages','%'),(3725,7,'packages','`'),(3871,1,'node','㻼'),(3824,7,'packages','Ŗ'),(3759,7,'packages','ý'),(3760,7,'packages','B'),(3762,7,'packages','Þ'),(3783,7,'packages','Ö'),(3784,7,'packages','ĝ'),(3804,7,'packages','´'),(3880,1,'fixes','щ'),(3880,1,'optional','ᅸམ'),(3880,1,'power','̻ㆽ'),(3881,1,'application','ɂ֩γኬeŗ$&\rpö$&\nɤĚ\",·ß;&	^\rk3/g	v{¼I*\r\ZP a-_^)\n¬Ó5´ƾÎ;Æ\nć'),(3794,7,'packages','&'),(3778,7,'packages','÷'),(3791,7,'packages','¯'),(3789,7,'packages',''),(3880,1,'cfg','ᓓ'),(3722,1,'optional','Ģ'),(3797,7,'packages','k'),(3761,7,'packages','ĝ'),(3871,1,'line2','ᮜǼᆋ|ঔƙᓚ#'),(3788,7,'packages','Û'),(3800,7,'packages','D'),(3801,7,'packages','5'),(3741,7,'packages','¹'),(3742,7,'packages',''),(3743,7,'packages',''),(3744,7,'packages','ɛ'),(3745,7,'packages','û'),(3746,7,'packages','2'),(3747,7,'packages','Z'),(3748,7,'packages','ĝ'),(3740,7,'packages','Ƈ'),(3724,7,'packages','f'),(3881,1,'power','⡫'),(3881,1,'unregister','⾋Ŧ{Ä'),(3872,1,'packages','Ɣ'),(3724,1,'vcs','\n\n'),(3727,1,'power','¥'),(3729,1,'optional','k\r'),(3734,1,'cfg','4'),(3740,1,'optional','Â'),(3744,1,'optional','¬'),(3750,1,'application','#'),(3752,1,'cfg','Î'),(3756,1,'authenticate','Z'),(3756,1,'filename','j'),(3757,1,'optional','s4'),(3757,1,'rss','L;d'),(3759,1,'optional','Ú'),(3762,1,'round','*'),(3763,1,'optional',''),(3766,1,'load15','l'),(3776,1,'extent','ē'),(3780,1,'conns','ĩ'),(3780,1,'load15','f'),(3782,1,'optional','Fċ'),(3783,1,'optional','\\'),(3788,1,'application','±'),(3789,1,'filename','a'),(3790,1,'mysqlslave',''),(3791,1,'column','^'),(3793,1,'authenticate','h'),(3795,1,'authenticate','['),(3798,1,'filename',''),(3799,1,'conns','¢'),(3803,1,'optional',''),(3805,1,'optional',''),(3811,1,'attr','\'9'),(3811,1,'bind','u'),(3822,1,'optional',''),(3823,1,'application','o('),(3823,1,'round','¦'),(3824,1,'application','.ä\''),(3824,1,'optional','á+\''),(3825,1,'application',''),(3825,1,'optional','Ă'),(3831,1,'optional','U'),(3834,1,'round',''),(3835,1,'filename',' \r'),(3838,1,'optional','&'),(3840,1,'bind','òĹØ'),(3840,1,'optional','\'E'),(3841,1,'cfg','d2'),(3842,1,'filename','.'),(3856,1,'optional','		'),(3863,1,'optional','W'),(3863,1,'packages','®#'),(3864,1,'round','d'),(3871,1,'application','⢴'),(3871,1,'decreases','ࠓ'),(3871,1,'filename','śք3ص+Ž¼є୪5)5Ou#B؅ÀxÊϓń'),(3871,1,'fixes','æ\''),(3830,7,'packages',''),(3831,7,'packages','Ð'),(3832,7,'packages','M'),(3833,7,'packages','ĝ'),(3834,7,'packages','¸'),(3835,7,'packages',''),(3836,7,'packages','b'),(3837,7,'packages','Ĥ'),(3838,7,'packages','A'),(3839,7,'packages','3'),(3840,7,'packages','̗'),(3841,7,'packages','¬'),(3842,7,'packages','<'),(3843,7,'packages','h'),(3844,7,'packages','Ô'),(3845,7,'packages','x'),(3846,7,'packages','ķ'),(3847,7,'packages','S'),(3848,7,'packages','®'),(3849,7,'packages','z'),(3850,7,'packages','H'),(3851,7,'packages',''),(3852,7,'packages',')'),(3853,7,'packages','Q'),(3854,7,'packages','Ł'),(3855,7,'packages','8'),(3856,7,'packages','Ǝ'),(3857,7,'packages',']'),(3858,7,'packages','2'),(3859,7,'packages','@'),(3860,7,'packages','E'),(3861,7,'packages','0'),(3862,7,'packages','Ę'),(3863,7,'packages','ĺ'),(3864,7,'packages',''),(3865,7,'packages',''),(3866,7,'packages','µ'),(3867,7,'packages','.'),(3868,7,'packages','Ĕ'),(3869,7,'packages','ė'),(3870,7,'packages','ġ'),(3871,7,'packages','僔'),(3872,7,'packages','೭'),(3873,7,'packages','ա'),(3874,7,'packages','ϫ'),(3875,7,'packages','́'),(3876,7,'packages','ƨ'),(3877,7,'packages','މ'),(3878,7,'packages','ी'),(3879,7,'packages','朮'),(3880,7,'packages','䠼'),(3881,7,'packages','㦼'),(3882,7,'packages','Ƭ'),(3883,7,'packages','Ň'),(3884,7,'packages','ᑠ'),(3885,7,'packages','ৗ'),(3886,7,'packages','Դ'),(3887,7,'packages','ᄎ'),(3888,7,'packages','उ'),(3889,7,'packages','ӹ'),(3890,7,'packages','ᾍ'),(3891,7,'packages','و'),(3892,7,'packages','ƾ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictE0` ENABLE KEYS */;

--
-- Table structure for table `dictE1`
--

DROP TABLE IF EXISTS `dictE1`;
CREATE TABLE `dictE1` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictE1`
--


/*!40000 ALTER TABLE `dictE1` DISABLE KEYS */;
LOCK TABLES `dictE1` WRITE;
INSERT INTO `dictE1` VALUES (3879,1,'target','ԓ '),(3871,1,'180','䖡'),(3840,1,'underlying','Ĉ'),(3832,1,'checker',''),(3880,1,'target','Ԗ '),(3880,1,'printing','䑙̽\ZA'),(3880,1,'javascript','˭䌟'),(3879,1,'underlying','ё'),(3873,1,'243','ˡ!\"!!%\'%%%)()((&'),(3872,1,'target','ƅ'),(3872,1,'suitable','஼'),(3871,1,'wanted','ѕೠᄑ°'),(3871,1,'suitable','ᑃ㉥'),(3871,1,'rateup','ቒ'),(3871,1,'preceded','ᰒ'),(3881,1,'javascript','ⓙ՛'),(3879,1,'prerequisite','▘✓'),(3879,1,'javascript','΃ᛑ'),(3871,1,'4294967200','䴈'),(3877,1,'continuing','Ӛ'),(3822,1,'target','7%'),(3829,1,'card','!'),(3820,1,'target','#'),(3819,1,'target','\"'),(3881,1,'inserts','۬'),(3881,1,'fed','ᴖԅ'),(3871,1,'inserts','ᴠྐྵ.'),(3871,1,'fed','ݴʲṹ'),(3877,1,'target','Ś	>a[#Ī½\n5\n>	+\"'),(3720,1,'target','Ā'),(3871,1,'continuing','䂠ڱ'),(3720,1,'javascript','Ɠ'),(3880,1,'underlying','ϱႀԧ⯜'),(3879,1,'setenvlf','ᬍ'),(3881,1,'preceded','᧼6'),(3881,1,'target','̦ ࢣy!⌋\n+&(i-Mũ'),(3881,1,'underlying','Ȃʜ'),(3884,1,'inserts','ჱ'),(3884,1,'suitable','Ǚਥ'),(3885,1,'suitable','Śĵ'),(3886,1,'suitable','Ɔ'),(3887,1,'suitable','Ǥ'),(3888,1,'continuing','ؽ'),(3888,1,'target','͠'),(3891,1,'suitable','Ŕ'),(3892,1,'inserts','ƚ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictE1` ENABLE KEYS */;

--
-- Table structure for table `dictE2`
--

DROP TABLE IF EXISTS `dictE2`;
CREATE TABLE `dictE2` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictE2`
--


/*!40000 ALTER TABLE `dictE2` DISABLE KEYS */;
LOCK TABLES `dictE2` WRITE;
INSERT INTO `dictE2` VALUES (3727,1,'tools','ý'),(3721,1,'tools','Ʉ'),(3727,1,'attempts',''),(3727,1,'details',''),(3785,1,'details','Ď'),(3748,1,'details','Ď'),(3745,1,'details','Ì'),(3744,1,'details','ż'),(3739,1,'details','ċ'),(3738,1,'details','q'),(3879,1,'inserted','劬༺'),(3879,1,'details','_Ίచᚐ'),(3878,1,'tools','ø'),(3879,1,'attempts','൒I੣Ӹ㿫'),(3877,1,'introduction',''),(3878,1,'details','V'),(3878,1,'syslog','؂'),(3880,1,'decide','บ'),(3880,1,'computed','㦆'),(3880,1,'attempts','ዌࡈ'),(3879,1,'tools','ǈ4ၘൗCԭ᎙ÐݹÇ<ਜ	('),(3755,1,'details',''),(3877,1,'details',']'),(3875,1,'tools','ɩ2'),(3872,1,'syslog','Ɇ'),(3761,1,'details','Ď'),(3729,1,'details','¦'),(3763,1,'details',''),(3728,1,'details','ċ'),(3834,1,'details',''),(3833,1,'details','Ď'),(3827,1,'details','ǫ'),(3818,1,'details','Ď'),(3814,1,'details','Ď'),(3880,1,'obsessing','Ⱈ'),(3854,1,'details','Ĳ'),(3871,1,'decide','⢜'),(3871,1,'details','⪠'),(3754,1,'details','Ű	'),(3811,1,'details','¶'),(3880,1,'details','_˴ᓸ⠠Ȧȸ8'),(3766,1,'attempts',''),(3880,1,'introduction','ƟK\Z'),(3806,1,'negpattern','.2'),(3750,1,'attempts',''),(3737,1,'details','ċ'),(3733,1,'syslog','A'),(3732,1,'details','Č'),(3848,1,'details',''),(3846,1,'details','Ĩ'),(3720,1,'details','0'),(3879,1,'parsed','䡌'),(3879,1,'introduction','ʋ\Z'),(3872,1,'details','֮'),(3872,1,'provider','Ü$϶\ZF'),(3842,1,'attempts',''),(3879,1,'syslog','㥲'),(3879,1,'respond','ح'),(3872,1,'cook','ņ'),(3871,1,'inserted','᳘৓ğ'),(3840,1,'introduction','·'),(3840,1,'tools','Ȯ'),(3871,1,'stays','䈡ण'),(3805,1,'negpattern','Z'),(3871,1,'introduction','΢䎦'),(3784,1,'details','Ď'),(3780,1,'attempts',''),(3871,1,'relax','⢻'),(3871,1,'tools','⑺ᦺ'),(3880,1,'respond','ذ'),(3880,1,'syslog','ྖ'),(3880,1,'tools','޸ܼ݇ǷGRἡ>	'),(3881,1,'attempts','඗᫞Ɲ'),(3881,1,'cms','⠊'),(3881,1,'decide','ῼÎ'),(3881,1,'details','_ƑᮙĔľ'),(3881,1,'inserted','ℯɍ'),(3881,1,'introduction','ū\Z'),(3881,1,'respond','р'),(3881,1,'tools','ٙᖷଶL#ǁi'),(3882,1,'tools','	HJ'),(3884,1,'attempts','ȸhGड़hG'),(3884,1,'details','ৼ'),(3884,1,'tools','ख़'),(3885,1,'attempts','Ӟ'),(3886,1,'details','ԍ'),(3887,1,'attempts','ܟ'),(3888,1,'tools','ࢶ>'),(3890,1,'evenly','ᆰƠ'),(3890,1,'syslog','ྞ\n'),(3890,1,'tools','Š'),(3882,2,'tools',''),(3733,6,'syslog',''),(3882,6,'tools','Ƴ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictE2` ENABLE KEYS */;

--
-- Table structure for table `dictE3`
--

DROP TABLE IF EXISTS `dictE3`;
CREATE TABLE `dictE3` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictE3`
--


/*!40000 ALTER TABLE `dictE3` DISABLE KEYS */;
LOCK TABLES `dictE3` WRITE;
INSERT INTO `dictE3` VALUES (3726,1,'urlize','*\Z'),(3872,1,'device','ňTҞ'),(3846,1,'kilobytes','Ä'),(3827,1,'urlize','Ʈ'),(3873,1,'procs','ͻ'),(3873,1,'latch','Ҙ'),(3871,1,'kilometer','㕻'),(3871,1,'multiples','≃'),(3879,1,'projects','μ'),(3871,1,'device','ु⬁\nख5r.Ƹ	Ħ॒˰'),(3871,1,'binary','˪'),(3871,1,'978301200','䣪'),(3871,1,'550000','㲑'),(3871,1,'3y','ⓣ'),(3869,1,'projects',''),(3869,1,'cross',''),(3849,1,'ipaddress','/\r'),(3871,1,'ds2','ẘ'),(3875,1,'device','Aȟ\'^'),(3880,1,'projects','̦Ѡ'),(3871,1,'fewer','Ŧ'),(3799,1,'ethz','ź'),(3720,1,'deprecated','Ę'),(3823,1,'svr1','J'),(3874,1,'procs','Ē_'),(3880,1,'presentation','ཫY'),(3844,1,'kilobytes','»'),(3831,1,'device','O'),(3879,1,'kilobytes','Ḝ '),(3879,1,'ipaddress','䵃ǿ'),(3879,1,'device','ᔟ\Z࣑`ƳȚƪფǑ'),(3879,1,'configures','ᅿ'),(3879,1,'binary','ᒌ䉭'),(3878,1,'device','ϐ'),(3880,1,'ng','ྗ'),(3880,1,'graphically','㨃WIQǌ[[LKAjgƶA@A'),(3880,1,'device','ৱؤȃظ⇟ˁ['),(3798,1,'ethz','¹'),(3787,1,'procs',''),(3757,1,'procs','+@Ê'),(3762,1,'urlize','r'),(3757,1,'portsentry','Ő	'),(3747,1,'device','$'),(3733,1,'ipaddress','~'),(3740,1,'procs','*	Ĭ'),(3846,1,'device','<¦'),(3720,1,'projects',''),(3871,1,'ethz','یٽƦ႞^_èفȖ˗ί̀'),(3880,1,'tier','ཥL'),(3881,1,'binary','㔥'),(3881,1,'deprecated','ୁÆഒ'),(3881,1,'device','׋ƐņPञ\rȌ\rŃֽԾŻ'),(3881,1,'presentation','ⱀͶ '),(3881,1,'projects','ⱛ'),(3886,1,'device','Ӄ'),(3888,1,'administer','ǒ'),(3890,1,'binary','ĝ؇'),(3890,1,'logo','ǡ'),(3726,6,'urlize',''),(3740,6,'procs','Ɠ'),(3757,6,'procs','Ʊ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictE3` ENABLE KEYS */;

--
-- Table structure for table `dictE4`
--

DROP TABLE IF EXISTS `dictE4`;
CREATE TABLE `dictE4` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictE4`
--


/*!40000 ALTER TABLE `dictE4` DISABLE KEYS */;
LOCK TABLES `dictE4` WRITE;
INSERT INTO `dictE4` VALUES (3872,1,'direct','¨'),(3871,1,'val3','ẕ\ZP'),(3871,1,'duplicate','ᢽ'),(3884,1,'executes','ᄂ'),(3880,1,'offer','⚒ቋ˝ѱ'),(3824,1,'direct','a'),(3824,1,'udp','ą$'),(3825,1,'direct','Z'),(3825,1,'locate',''),(3825,1,'udp','į'),(3837,1,'testxy','Ę'),(3868,1,'galstad','\n'),(3871,1,'978303000','䤈'),(3871,1,'dangerous','͎'),(3871,1,'digit','⏷'),(3871,1,'gd','ཊԘ	\Z'),(3871,1,'megabits','䐍'),(3880,1,'direct','૞෗'),(3879,1,'executes','哴'),(3879,1,'eventlog','䪇ţᏂʬ'),(3877,1,'locate','ǖï'),(3876,1,'pings','ŉ'),(3875,1,'udp','ÿą'),(3881,1,'nextchecktime','ࡥ'),(3881,1,'reflects','ค'),(3720,1,'chain',''),(3728,1,'udp',''),(3729,1,'udp',''),(3731,1,'udp','M'),(3753,1,'udp','o'),(3778,1,'locate','Ê'),(3887,1,'gd','ඌ'),(3890,1,'locate','ǟ,Ý'),(3892,1,'executes','ƫ'),(3729,6,'udp','Þ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictE4` ENABLE KEYS */;

--
-- Table structure for table `dictE5`
--

DROP TABLE IF EXISTS `dictE5`;
CREATE TABLE `dictE5` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictE5`
--


/*!40000 ALTER TABLE `dictE5` DISABLE KEYS */;
LOCK TABLES `dictE5` WRITE;
INSERT INTO `dictE5` VALUES (3890,1,'actively','ਪď'),(3881,1,'4','ÆPȋBNƥᙝ࣠ìàຄ'),(3887,1,'4','΢'),(3879,1,'deleting','߿ƒ䲮ئࣻc'),(3879,1,'chronological','䯆'),(3856,1,'4','S'),(3856,1,'skiplines','p'),(3854,1,'4','F.'),(3846,1,'tb','À'),(3888,1,'4','a׺'),(3886,1,'problems','ľ'),(3885,1,'problems','ؽé'),(3881,1,'scenes','⻖'),(3884,1,'problems','Ͳ×ऴ×'),(3879,1,'reducing','憊'),(3875,1,'4','Ɯ'),(3872,1,'4','ĕǞ'),(3871,1,'interested','Ф㮒'),(3871,1,'problems','㊣᨜'),(3871,1,'reducing','խ'),(3871,1,'timezone','ᦢ4௄>'),(3871,1,'wipeout','Ậ'),(3862,1,'4','±'),(3728,1,'4','@&'),(3729,1,'4','A'),(3732,1,'4','@\''),(3734,1,'4','õ'),(3737,1,'4','@&'),(3738,1,'4','G'),(3739,1,'4','@&'),(3745,1,'4','7'),(3748,1,'4','C&'),(3751,1,'4','ɾ'),(3754,1,'problems','Ň$'),(3761,1,'4','C&'),(3762,1,'4','0'),(3763,1,'4','E'),(3778,1,'4','ï'),(3780,1,'dsver','ù'),(3784,1,'4','C&'),(3785,1,'4','C&'),(3798,1,'icl','@'),(3811,1,'4','6'),(3814,1,'4','C&'),(3818,1,'4','C&'),(3827,1,'4','iR'),(3833,1,'4','C&'),(3834,1,'reducing','`'),(3836,1,'4',''),(3840,1,'intro','˵'),(3840,1,'problems','Í\n7'),(3841,1,'4','a-'),(3720,1,'4','\"#'),(3871,1,'4','ቚᆜȧ᫦(,ǥǕϛ\nð˴'),(3880,1,'4','¶PH̈́BN࣌ࠌࣁӇéሚ'),(3877,1,'suggested','č'),(3890,1,'4','᪭ '),(3720,1,'toaster','Š'),(3879,1,'problems','⡒ׄחϧ'),(3880,1,'category','䚾'),(3880,1,'problems','ࠎ˯ϝɣ\nKཀΚ/ߡѦǼȽ'),(3875,1,'discovery','ň'),(3881,1,'interested','❓'),(3889,1,'4','͏m'),(3722,1,'problems','h'),(3879,1,'category','⍓㐊ĕ\Z				.\"*3'),(3870,1,'4','Ä'),(3876,1,'4','þ'),(3887,1,'problems','Ŧܘèë'),(3874,1,'4','Ãĉ¬'),(3872,1,'providers','Ӆ '),(3880,1,'actively','⬍'),(3872,1,'problems','ঃ'),(3879,1,'suggested','ॴ'),(3879,1,'discovery','₴ךЋ'),(3879,1,'4','æ8.´̖BNȋࡴѣ;̞ۈ{༏׵ී໳৆'),(3890,1,'problems','ݦ2෉ֺ'),(3890,1,'suggested','᷃'),(3891,1,'problems','Ɲ\"¨\"ȧ\"!\"');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictE5` ENABLE KEYS */;

--
-- Table structure for table `dictE6`
--

DROP TABLE IF EXISTS `dictE6`;
CREATE TABLE `dictE6` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictE6`
--


/*!40000 ALTER TABLE `dictE6` DISABLE KEYS */;
LOCK TABLES `dictE6` WRITE;
INSERT INTO `dictE6` VALUES (3875,1,'ifouterrors','Ì'),(3759,1,'hey','X'),(3754,1,'occurred','Ȼ'),(3751,1,'unitl','Ń-'),(3879,1,'s1chapter2b','巤'),(3879,1,'training','ᜊU6'),(3879,1,'populated','㛴'),(3879,1,'occurred','䭵'),(3879,1,'independently','䆼'),(3879,1,'contactemail','⿊'),(3879,1,'columns','⨜Ɯ'),(3871,1,'columns','⭂'),(3872,1,'contactemail','୰'),(3871,1,'occurred','㉢'),(3880,1,'columns','Ჱ'),(3872,1,'couple','ܠ'),(3880,1,'occurred','☃0'),(3881,1,'occurred','ᝁ¨'),(3881,1,'populated','ᦺ'),(3884,1,'columns','᎚2'),(3891,1,'contactemail','Ĉ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictE6` ENABLE KEYS */;

--
-- Table structure for table `dictE7`
--

DROP TABLE IF EXISTS `dictE7`;
CREATE TABLE `dictE7` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictE7`
--


/*!40000 ALTER TABLE `dictE7` DISABLE KEYS */;
LOCK TABLES `dictE7` WRITE;
INSERT INTO `dictE7` VALUES (3789,1,'matching','C'),(3801,1,'e1','\"'),(3804,1,'alpha','\\'),(3811,1,'ver3',''),(3815,1,'daemon','X'),(3825,1,'client',''),(3872,1,'daemon','ɓÜ6nĔĖ'),(3871,1,'upwards','ᓹ'),(3871,1,'gt','᠋⎪R	\n=	'),(3871,1,'general','Ǫ⺔'),(3871,1,'cgi','ªʾ⣽ !2![Ȫ'),(3871,1,'alpha','᢯'),(3871,1,'called','ೢֺ̲ޞ֑[᎗ނ$+Ǯĥֺ¨'),(3766,1,'daemon','µ'),(3783,1,'daemon','9+'),(3864,1,'wrta','*'),(3871,1,'continuously','䥞'),(3872,1,'called','Ӗ++7O¨ɶ'),(3874,1,'called','I:'),(3873,1,'called','ɶ'),(3881,1,'desirable','㐒'),(3879,1,'cgi','ìڠ;ՎʜӉW௨й᭘?ǿΓ-ŪÇݵO4ê;đ'),(3890,1,'daemon','อ'),(3871,1,'18446744069414583520','䴨'),(3870,1,'general',''),(3765,1,'client',''),(3762,1,'cgi','½'),(3762,1,'downloads','Í'),(3762,1,'wrta','&'),(3881,1,'called','ҹŹʂLȥዷݍٹ̒+&ɞj'),(3880,1,'general','.,ᧁƵ\r<͢Ȅиn'),(3880,1,'matching','ংḍ'),(3874,1,'cgi','Ź'),(3862,1,'manticore','©'),(3828,1,'daemon','*'),(3721,1,'client','ȣ'),(3720,1,'alpha','9'),(3869,1,'general','2'),(3869,1,'cgi','Ç'),(3887,1,'cgi','˔ि(ÇNP ,y)'),(3884,1,'unchecked','ǹ\ZĔ.P>êX23׵Ĕ.P>êX23'),(3881,1,'client','ுᬙ͎%7'),(3890,1,'general','ሹŚƦ'),(3879,1,'called','ᵚԷ⍥	Ѥబƣܫ'),(3878,1,'general','%,'),(3878,1,'called','бG785a7/'),(3890,1,'cgi','AřżÑ:º¦O>7ZY֨¨'),(3888,1,'cgi','ǹC\n'),(3886,1,'matching','·'),(3863,1,'matching','¯?'),(3881,1,'sample3','ଂ'),(3881,1,'geteventsforhost','৷๽'),(3881,1,'general','.,'),(3760,1,'daemon','4'),(3880,1,'cgi','ৄਢό⭈à'),(3881,1,'cgi','૙ᨡ	'),(3875,1,'called',''),(3875,1,'cgi','ń'),(3876,1,'called','_'),(3876,1,'cgi','±'),(3877,1,'called','ω'),(3877,1,'client','ɈĠ\Z_ƴ\n'),(3877,1,'daemon','Ŧk\rï['),(3877,1,'general',',,'),(3877,1,'hat','­\r)'),(3721,1,'general','ţ'),(3880,1,'called','᏷'),(3879,1,'unchecked','䜏'),(3754,1,'matching','=×ǌ'),(3887,1,'matching','ß'),(3887,1,'unchecked','ԗ3!&ƛY'),(3888,1,'called','ʫ'),(3890,1,'unchecked','ȿİহʓ S7ҵPÛĪ͞*'),(3885,1,'unchecked','˙3!&ƶY'),(3872,1,'general',''),(3879,1,'general','.,ᑐ¯Rƛ'),(3890,1,'detect','ᖎ'),(3881,1,'matching','ា©ðࣨG'),(3881,1,'hat','㓵'),(3754,1,'hostdown',']āµ'),(3751,1,'called','ɢ'),(3727,1,'daemon','ē'),(3890,1,'continuously','ͥ'),(3885,1,'matching','}');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictE7` ENABLE KEYS */;

--
-- Table structure for table `dictE8`
--

DROP TABLE IF EXISTS `dictE8`;
CREATE TABLE `dictE8` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictE8`
--


/*!40000 ALTER TABLE `dictE8` DISABLE KEYS */;
LOCK TABLES `dictE8` WRITE;
INSERT INTO `dictE8` VALUES (3879,1,'appended','គ'),(3886,1,'appended','ӧ'),(3817,1,'df',''),(3837,1,'popu','Ď'),(3871,1,'lan','֏'),(3871,1,'implicit','ᜓ'),(3871,1,'graphical','Ǘёঅ'),(3879,1,'retrieve','Ꮴ'),(3871,1,'retrieve','Ⅸბَ༈܈'),(3871,1,'rrdinfo','⁒'),(3873,1,'retrieve','~K'),(3843,1,'rsh','+'),(3881,1,'retrieve','Ӂϻ౛ॣ'),(3880,1,'graphical','ܨЁ໷FPůMηȇзmน'),(3735,1,'rsh',''),(3736,1,'allocated','£'),(3780,1,'imesync','ğ'),(3808,1,'rsh','7'),(3816,1,'df',''),(3871,1,'friends','㘗'),(3871,1,'applies','ៅ'),(3871,1,'appended','䉰'),(3845,1,'rsh','!'),(3888,1,'appended','޽t'),(3890,1,'dumb','ᆷ,œ'),(3890,1,'expense','ᑈ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictE8` ENABLE KEYS */;

--
-- Table structure for table `dictE9`
--

DROP TABLE IF EXISTS `dictE9`;
CREATE TABLE `dictE9` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictE9`
--


/*!40000 ALTER TABLE `dictE9` DISABLE KEYS */;
LOCK TABLES `dictE9` WRITE;
INSERT INTO `dictE9` VALUES (3881,1,'active','౜TĔ'),(3879,1,'active','आ\r)ዉᓶȧ׾'),(3878,1,'franklin','q'),(3877,1,'generally','ْ'),(3825,1,'browser','\n\Z\' 		'),(3824,1,'browser','*\"\'_0\''),(3783,1,'arglist','.-'),(3776,1,'active','a'),(3881,1,'attributevalue','Ủ'),(3880,1,'architect','ʄ'),(3877,1,'franklin','x'),(3862,1,'prokopyev','N'),(3766,1,'active',''),(3880,1,'organization','ࡽ'),(3755,1,'554','I'),(3763,1,'active','£'),(3840,1,'microsystems','˳'),(3871,1,'border','࠹ಱÂ'),(3879,1,'border','崕Ģ'),(3871,1,'rrdgraph','هࢰٛÔ!ـãഞ		Ζ<'),(3871,1,'preceding','⻝'),(3871,1,'modifications','䜠'),(3871,1,'browser','〇ɀ઱'),(3879,1,'architect','̚ᴊ'),(3872,1,'preceding','౑'),(3871,1,'backslashes','ᱶ\n'),(3880,1,'active','᧙̳΂·\r\rŢөʂ`μ\rb\r¤M'),(3879,1,'nagioscomment','ፒ'),(3879,1,'modifications','℣'),(3879,1,'franklin','z'),(3879,1,'expanded','࣢㽡'),(3879,1,'browser','᪼*⹀࡛Ɗ'),(3720,1,'inline','Ğ'),(3720,1,'browser','ư'),(3880,1,'browser','ࢗ̓ǜ⪸མ\\'),(3880,1,'generally','ή䉍'),(3880,1,'franklin','z'),(3880,1,'expanded','◥07࿅'),(3881,1,'browser','⛜ʧ\Z*OHࢾͪ'),(3881,1,'collageapi','ో'),(3881,1,'expanded','࠯'),(3881,1,'franklin','z'),(3881,1,'getmonitorlist','ቖ'),(3881,1,'limitations','⎱'),(3882,1,'browser',' '),(3884,1,'active','Ȯå\r	еӖå\r	'),(3884,1,'backslashes','Ŋ'),(3885,1,'active','ώ'),(3885,1,'uncehcked','І'),(3887,1,'active','،'),(3888,1,'active','Ϫ'),(3890,1,'active','ѡ֕ostޤ଱'),(3890,1,'browser','ݡ'),(3890,1,'generally','ጏ'),(3890,1,'vessels','ṹ'),(3825,6,'browser','ŏ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictE9` ENABLE KEYS */;

--
-- Table structure for table `dictEA`
--

DROP TABLE IF EXISTS `dictEA`;
CREATE TABLE `dictEA` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictEA`
--


/*!40000 ALTER TABLE `dictEA` DISABLE KEYS */;
LOCK TABLES `dictEA` WRITE;
INSERT INTO `dictEA` VALUES (3875,1,'select','ʩ\r	8'),(3875,1,'reserved',''),(3875,1,'configuring','Ƣ'),(3874,1,'reserved',''),(3874,1,'configuring','ǒ'),(3873,1,'template','ʎ'),(3727,1,'kroll','ė'),(3721,1,'reserved','	'),(3883,1,'reserved',''),(3887,2,'configuring',''),(3889,2,'configuring',''),(3885,2,'configuring',''),(3891,1,'select','N\r©ȁ\r\r	`	'),(3891,1,'template','ō	ʊ\''),(3892,1,'reserved',''),(3892,1,'select','0\Ze'),(3884,2,'configuring',''),(3885,1,'reserved',''),(3885,1,'configuring','<܆ʊ'),(3890,1,'equivalent','ቶ'),(3889,1,'select','\\ǯ®H8C\n'),(3890,1,'configuring','੊Ĉ'),(3889,1,'configuring',':\rȪ'),(3889,1,'reserved',''),(3890,1,'template','ᢕG$$'),(3890,1,'sort','਴'),(3880,1,'reserved','\Z'),(3880,1,'helps','㒿'),(3880,1,'configuring','̽'),(3879,1,'template','⇍\Z¤ໆC\rH\n>6ȔØ7\rīƌǨÌ+॓Ɩഭƌ\nӳ'),(3745,1,'connects','×'),(3755,1,'connects',''),(3759,1,'select',''),(3763,1,'connects','­'),(3778,1,'select','|'),(3791,1,'minimal','2'),(3805,1,'template','l\r'),(3820,1,'administratively','é'),(3827,1,'connects','Ȃ'),(3831,1,'recognized','A'),(3837,1,'lost','H'),(3840,1,'rest','ȗ'),(3840,1,'select','ȣ'),(3840,1,'sort','ɴ'),(3862,1,'reserved','['),(3868,1,'reserved','\r'),(3869,1,'commerce','Ý'),(3870,1,'reserved',''),(3871,1,'configuring','㋴'),(3871,1,'h1','⼕F	`'),(3871,1,'helps','ᜱ　अ'),(3871,1,'minimal','ॅ᝷!'),(3871,1,'printout','Ⲣ'),(3871,1,'relevant','⿷'),(3871,1,'rest','㤛෶&ä~ޅ'),(3871,1,'select','ɬⳟ'),(3871,1,'sort','ਗ≤Ù᱄'),(3871,1,'template','ൟ4,ặ\"'),(3871,1,'xport','͒♲«\'ć'),(3872,1,'configuring','׸'),(3872,1,'deliver','¬$'),(3872,1,'select','ݮ¤ĜŎ[³%{?'),(3872,1,'template','ࡈE5-ľRª'),(3873,1,'reserved',''),(3884,1,'sort','Ȧ਋'),(3877,1,'reserved',''),(3876,1,'reserved',''),(3882,1,'select','[ 1\n73'),(3881,1,'configuring','㎜ƫ·ó9'),(3882,1,'reserved',''),(3880,1,'select','ดႳӠė\nÇŦAȅ:ʔǶ˟Ȥ\ZT1<Й\nС\nɆ\nՋ<Y3'),(3881,1,'template','⿯Ģ'),(3881,1,'statusid','ᛮ\n'),(3881,1,'rest','⻞ٹ'),(3881,1,'reserved','\Z'),(3876,1,'configuring','Ą'),(3720,1,'reserved','	'),(3720,1,'fallback','î'),(3878,1,'reflect','ª'),(3878,1,'configuring','·'),(3884,1,'template','=T\n\r^{υċs>ɍ{\n%τ¼\r	ľ\n	'),(3884,1,'configuring','h߸ȊRֶƗ«'),(3883,1,'select','9'),(3879,1,'select','ࢠâ\rv\r|:	ƀôţ\n83ҘĆ$ò\"?Rӳ৩\rŻ1t\n+>t\nl,\n.	gĆ¨	\r\nXb\'ŬڎSÑìࢋӠͶ¢3AæO(સ7\n	#ۊ\"'),(3734,1,'connects',''),(3729,1,'connects','¾'),(3881,1,'getservices','উ೽'),(3890,1,'extinfo','И̴'),(3884,1,'relevant','ࠨ'),(3884,1,'reflect','܎'),(3884,1,'select','uh@ђ+j_9\n\n\r ,ó*)#Ä*14щ§)Ŗw\r	##'),(3884,1,'reserved',''),(3879,1,'reserved','\Z䓴ǔ'),(3879,1,'relevant','ᴞ'),(3885,1,'select','H\Z\"&oԑ0\rLĜ	'),(3890,1,'select','¢6ͪʡޕևֲǀ\n\r4É&\Z&\''),(3890,1,'reserved',''),(3891,1,'reserved',''),(3879,1,'sort','⧕3Ŗ3'),(3881,1,'sort','੒+ཫ'),(3881,1,'select','૟˵'),(3879,1,'h1','崠\n\n			À			'),(3879,1,'extinfo','㍱ӳ'),(3878,1,'reserved',''),(3879,1,'configuring','Ä\\&$N\ZƽόJ޳»E?\'വt$׷޲ı͚ʝǼݺϩQ\ZġۨĉֿԔम'),(3879,1,'equivalent','ॊ១'),(3888,1,'select','2ʶXŅ	ĝ¡ !õ'),(3888,1,'reserved',''),(3888,1,'reflect','Ύ'),(3888,1,'recognized','ԯ'),(3887,1,'configuring','lЋӲŘҘ'),(3887,1,'reserved',''),(3887,1,'select','|ĥ/ġ/?\n	d	\n \rҠ\',3̭\n)k\n'),(3887,1,'template','1Ƭ_ɁT#1ÁŇYO\"ʲ\nsɳ	*m	\n'),(3886,1,'template','ſ'),(3885,1,'template','œİ	\ZT#1ÀŅYO\"Ë'),(3886,1,'reserved',''),(3886,1,'select','B\rĆ)\r+W\r,\r*\r.B\r:ņ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictEA` ENABLE KEYS */;

--
-- Table structure for table `dictEB`
--

DROP TABLE IF EXISTS `dictEB`;
CREATE TABLE `dictEB` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictEB`
--


/*!40000 ALTER TABLE `dictEB` DISABLE KEYS */;
LOCK TABLES `dictEB` WRITE;
INSERT INTO `dictEB` VALUES (3871,1,'low','䔗'),(3871,1,'imagine','㶲'),(3721,1,'ansi','æ'),(3884,1,'low','Ս৤'),(3890,1,'prompted','äṭ'),(3890,1,'contents','\Zʴ׃2	Ԩ'),(3889,1,'prompted',''),(3871,1,'contents','\nʼͷීਕų\"(ಸØ'),(3887,1,'contents',''),(3887,1,'low','յ'),(3887,1,'prompted','ஆ'),(3886,1,'contents',''),(3891,1,'prompted','n̂Æ'),(3883,1,'prompted','Z'),(3877,1,'pubkeyauthentication','ǘþ'),(3877,1,'contents',''),(3876,1,'contents','\Z'),(3888,1,'contents',''),(3892,1,'contents',''),(3884,1,'contents',''),(3881,1,'contents','Ü·\ṛC̭ʐ¸࿇Ęܽ1v=Ĭդш̜'),(3878,1,'contents','z'),(3879,1,'consoles','ᔐ'),(3871,1,'algorithm','ሁn'),(3891,1,'contents',''),(3892,1,'prompted','ó'),(3880,1,'low','޳ঔ'),(3880,1,'finds','⯤ҟ)'),(3889,1,'contents',''),(3883,1,'contents',''),(3875,1,'contents','\Z'),(3885,1,'low','̷'),(3884,1,'prompted','ƚᅅ'),(3721,1,'contents','\n'),(3722,1,'shares','_'),(3727,1,'low','Î'),(3740,1,'low','@0'),(3741,1,'low','&'),(3743,1,'low','%'),(3751,1,'low','m\nB#V-'),(3837,1,'smtph','Đ'),(3840,1,'algorithm','ǧ'),(3869,1,'contents','\n'),(3870,1,'contents',''),(3870,1,'low','þ'),(3720,1,'contents','\n'),(3881,1,'low','൘'),(3879,1,'contents','ǨƋɑġT׺ƗԣщхǓ¨տ౩ːǼϱɲąБDࠊǱʣȼ°ѢǖՎ͐ʑ4Ğì'),(3879,1,'labeln','䕓'),(3880,1,'contents','ĳ3Ґӷ̙ڬħ^͛࡟ʛ൜Ơ&ʆ´ஏĝʼ'),(3880,1,'consoles','ৢ'),(3879,1,'prompted','⺀¾Ƹ'),(3881,1,'triggered','⧇'),(3882,1,'contents',''),(3885,1,'prompted','Ǳذ'),(3885,1,'contents',''),(3871,1,'ltm','Ⴡ'),(3872,1,'contents',''),(3873,1,'contents',''),(3874,1,'contents','\Z');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictEB` ENABLE KEYS */;

--
-- Table structure for table `dictEC`
--

DROP TABLE IF EXISTS `dictEC`;
CREATE TABLE `dictEC` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictEC`
--


/*!40000 ALTER TABLE `dictEC` DISABLE KEYS */;
LOCK TABLES `dictEC` WRITE;
INSERT INTO `dictEC` VALUES (3858,7,'bookshelf','3'),(3859,7,'bookshelf','A'),(3832,7,'bookshelf','N'),(3879,1,'gwrk','዇xlK'),(3797,7,'bookshelf','l'),(3762,7,'bookshelf','ß'),(3763,7,'bookshelf','Ĺ'),(3764,7,'bookshelf','>'),(3880,1,'grouping','Ⴂ'),(3880,1,'intervening','ቲ'),(3890,1,'cached','࠴'),(3890,1,'starts','঱ PKRUDVUȐ̲sŚ'),(3823,7,'bookshelf','Ì'),(3767,7,'bookshelf','('),(3744,7,'bookshelf','ɜ'),(3745,7,'bookshelf','ü'),(3782,1,'servicestate','Ą2'),(3803,1,'applied',''),(3827,1,'eregi','Ŵ'),(3868,1,'bookshelf',''),(3871,1,'applied','ਖ਼ᝀǸ'),(3871,1,'incomprehensible','ᜪ'),(3871,1,'interesting','㙿'),(3871,1,'laptop','䑔'),(3871,1,'legend','Ţ฀\'شԍ_7n	ඈËx'),(3871,1,'ltime','ᦜ'),(3871,1,'seconds1','佒'),(3871,1,'starts','ⴢܯ#ʀɍ'),(3872,1,'applied','ࢅ'),(3872,1,'servicestate','ݝ'),(3874,1,'bookshelf','ǆ '),(3875,1,'bookshelf','ƖÜ'),(3876,1,'bookshelf','ø'),(3877,1,'gwrk','ğ	êı*V1-ç'),(3878,1,'applied','¥ޢ<\r'),(3878,1,'bookshelf','ũ'),(3879,1,'applied','ᷱФĘǎ஬Ϸ Vހʏ࿑'),(3879,1,'bookshelf','ȐԨќֺćƒ\'\nॽ␉Gᏹ\Z-\n	$zG\Z67ȇ/\"@%*\n\r6	%:Yhú״'),(3765,1,'70','Ô'),(3728,7,'bookshelf','ě'),(3845,7,'bookshelf','y'),(3834,7,'bookshelf','¹'),(3796,7,'bookshelf',')'),(3879,1,'grouping','⑮ៗǆ'),(3819,7,'bookshelf','Ġ'),(3840,7,'bookshelf','̘'),(3820,7,'bookshelf','ō'),(3856,7,'bookshelf','Ə'),(3855,7,'bookshelf','9'),(3847,7,'bookshelf','T'),(3842,7,'bookshelf','='),(3830,7,'bookshelf',''),(3829,7,'bookshelf','w'),(3805,7,'bookshelf','Í'),(3804,7,'bookshelf','µ'),(3803,7,'bookshelf','Þ'),(3802,7,'bookshelf','<'),(3801,7,'bookshelf','6'),(3800,7,'bookshelf','E'),(3799,7,'bookshelf','Ə'),(3863,7,'bookshelf','Ļ'),(3848,7,'bookshelf','¯'),(3843,7,'bookshelf','i'),(3885,1,'applied','ċŝٻUH'),(3885,1,'interesting','ݶ'),(3885,1,'recovers','׍ē'),(3887,1,'recovers','ࠎĒ'),(3812,7,'bookshelf','3'),(3825,7,'bookshelf','Ń'),(3822,7,'bookshelf','ÿ'),(3884,1,'applied','࣋a@'),(3757,1,'applied','5'),(3821,7,'bookshelf',';'),(3727,7,'bookshelf','Ł'),(3844,7,'bookshelf','Õ'),(3833,7,'bookshelf','Ğ'),(3795,7,'bookshelf',''),(3818,7,'bookshelf','Ğ'),(3839,7,'bookshelf','4'),(3837,7,'bookshelf','ĥ'),(3838,7,'bookshelf','B'),(3783,7,'bookshelf','×'),(3784,7,'bookshelf','Ğ'),(3785,7,'bookshelf','Ğ'),(3786,7,'bookshelf','?'),(3787,7,'bookshelf','ª'),(3788,7,'bookshelf','Ü'),(3789,7,'bookshelf',''),(3790,7,'bookshelf','S'),(3782,7,'bookshelf','Ɩ'),(3781,7,'bookshelf',''),(3766,7,'bookshelf','ñ'),(3765,7,'bookshelf','Þ'),(3862,7,'bookshelf','ę'),(3730,7,'bookshelf',''),(3731,7,'bookshelf',''),(3732,7,'bookshelf','Ĝ'),(3733,7,'bookshelf',''),(3734,7,'bookshelf','ý'),(3735,7,'bookshelf','w'),(3736,7,'bookshelf','½'),(3737,7,'bookshelf','ě'),(3738,7,'bookshelf',''),(3739,7,'bookshelf','ě'),(3740,7,'bookshelf','ƈ'),(3741,7,'bookshelf','º'),(3742,7,'bookshelf',''),(3743,7,'bookshelf',''),(3881,1,'hgid','ᑁ'),(3881,1,'bookshelf','⑧ā\Z'),(3881,1,'applied','ৢᎈϐ'),(3880,1,'starts','⣒өƓ'),(3880,1,'recovers','ⲋѦ'),(3782,1,'memuse','ö'),(3857,7,'bookshelf','^'),(3861,7,'bookshelf','1'),(3860,7,'bookshelf','F'),(3852,7,'bookshelf','*'),(3811,7,'bookshelf','Æ'),(3806,7,'bookshelf','r'),(3807,7,'bookshelf','A'),(3808,7,'bookshelf','z'),(3809,7,'bookshelf','d'),(3810,7,'bookshelf','.'),(3721,7,'bookshelf','ʜ'),(3722,7,'bookshelf','ţ'),(3723,7,'bookshelf','D'),(3724,7,'bookshelf','g'),(3725,7,'bookshelf','a'),(3880,1,'bookshelf','ŮۋĖ͘㜓\"+ȩ	@*S		*'),(3849,7,'bookshelf','{'),(3813,7,'bookshelf',''),(3791,7,'bookshelf','°'),(3768,7,'bookshelf',')'),(3769,7,'bookshelf','&'),(3752,7,'bookshelf','ď'),(3753,7,'bookshelf',''),(3754,7,'bookshelf','̏'),(3755,7,'bookshelf','¿'),(3756,7,'bookshelf','õ'),(3757,7,'bookshelf','Ƨ'),(3758,7,'bookshelf','B'),(3759,7,'bookshelf','þ'),(3760,7,'bookshelf','C'),(3761,7,'bookshelf','Ğ'),(3891,1,'starts','˛˗'),(3879,1,'regx','䦭'),(3879,1,'starts','‴⾛ጌ'),(3826,7,'bookshelf','&'),(3831,7,'bookshelf','Ñ'),(3850,7,'bookshelf','I'),(3846,7,'bookshelf','ĸ'),(3835,7,'bookshelf',''),(3836,7,'bookshelf','c'),(3720,1,'firebug','é+'),(3827,7,'bookshelf','˕'),(3726,7,'bookshelf',''),(3884,1,'grouping','Ċ'),(3841,7,'bookshelf','­'),(3824,7,'bookshelf','ŗ'),(3851,7,'bookshelf',''),(3792,7,'bookshelf','d'),(3720,7,'bookshelf','ƻ'),(3720,1,'grouping',''),(3744,1,'eregi','Ŀ'),(3746,7,'bookshelf','3'),(3747,7,'bookshelf','['),(3748,7,'bookshelf','Ğ'),(3749,7,'bookshelf','+'),(3750,7,'bookshelf','6'),(3751,7,'bookshelf','ʫ'),(3729,7,'bookshelf','Ô'),(3854,7,'bookshelf','ł'),(3814,7,'bookshelf','Ğ'),(3815,7,'bookshelf','l'),(3770,7,'bookshelf','&'),(3771,7,'bookshelf','&'),(3772,7,'bookshelf','&'),(3773,7,'bookshelf','*'),(3774,7,'bookshelf','+'),(3775,7,'bookshelf',')'),(3776,7,'bookshelf','ħ'),(3777,7,'bookshelf','*'),(3778,7,'bookshelf','ø'),(3779,7,'bookshelf','4'),(3780,7,'bookshelf','Ư'),(3881,1,'sample2','૔7'),(3881,1,'starts','⼏ɗ'),(3881,1,'timecritical','ࡰ'),(3882,1,'starts','á'),(3879,1,'recovers','⢿૿ϧГů'),(3853,7,'bookshelf','R'),(3828,7,'bookshelf',''),(3793,7,'bookshelf','¨'),(3794,7,'bookshelf','\''),(3798,7,'bookshelf','ð'),(3816,7,'bookshelf','R'),(3817,7,'bookshelf','/'),(3884,1,'recovers','Ԛ਋'),(3864,7,'bookshelf',''),(3865,7,'bookshelf',' '),(3866,7,'bookshelf','¶'),(3867,7,'bookshelf','/'),(3868,7,'bookshelf','ĕ'),(3869,7,'bookshelf','Ę'),(3870,7,'bookshelf','Ģ'),(3871,7,'bookshelf','僕'),(3872,7,'bookshelf','೮'),(3873,7,'bookshelf','բ'),(3874,7,'bookshelf','Ϭ'),(3875,7,'bookshelf','͂'),(3876,7,'bookshelf','Ʃ'),(3877,7,'bookshelf','ފ'),(3878,7,'bookshelf','ु'),(3879,7,'bookshelf','术'),(3880,7,'bookshelf','䠽'),(3881,7,'bookshelf','㦽'),(3882,7,'bookshelf','ƭ'),(3883,7,'bookshelf','ň'),(3884,7,'bookshelf','ᑡ'),(3885,7,'bookshelf','৘'),(3886,7,'bookshelf','Ե'),(3887,7,'bookshelf','ᄏ'),(3888,7,'bookshelf','ऊ'),(3889,7,'bookshelf','Ӻ'),(3890,7,'bookshelf','ᾎ'),(3891,7,'bookshelf','ى'),(3892,7,'bookshelf','ƿ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictEC` ENABLE KEYS */;

--
-- Table structure for table `dictED`
--

DROP TABLE IF EXISTS `dictED`;
CREATE TABLE `dictED` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictED`
--


/*!40000 ALTER TABLE `dictED` DISABLE KEYS */;
LOCK TABLES `dictED` WRITE;
INSERT INTO `dictED` VALUES (3872,1,'uncheck','ொ'),(3873,1,'archive','ũ'),(3871,1,'tcl','Ťā'),(3871,1,'estimate','䕘'),(3871,1,'drove','㘺෾Ƕ'),(3871,1,'archive','Ԏê΁=<⤖\rིʆ	'),(3870,1,'archive','F'),(3879,1,'archive','呑ᄮ'),(3873,1,'hit','Ѱ\r'),(3884,1,'uncheck','Ü'),(3880,1,'uncheck','ⳢѦ'),(3884,1,'protocols','࣑'),(3880,1,'reschedule','〝'),(3879,1,'uncheck','㌂޵'),(3879,1,'protocols','⪚ୀ'),(3881,1,'tcl','ⲭ'),(3881,1,'mechanism','㏢'),(3881,1,'alike','Ԣᝨ'),(3869,1,'archive',''),(3778,1,'hit','r'),(3751,1,'tw','Ɏ'),(3880,1,'searched','䞀'),(3871,1,'netscape','㴁'),(3871,1,'frontend',''),(3885,1,'uncheck','ʠ'),(3886,1,'uncheck','Ɯ'),(3890,1,'archive','೔'),(3890,1,'reschedule','ᐣYR'),(3891,1,'uncheck','ť');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictED` ENABLE KEYS */;

--
-- Table structure for table `dictEE`
--

DROP TABLE IF EXISTS `dictEE`;
CREATE TABLE `dictEE` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictEE`
--


/*!40000 ALTER TABLE `dictEE` DISABLE KEYS */;
LOCK TABLES `dictEE` WRITE;
INSERT INTO `dictEE` VALUES (3881,1,'actionlistener','ㇽ'),(3880,1,'maintenance','ਆڀќ'),(3880,1,'hh','⢪өƯ'),(3871,1,'subscribe','㍕Ჲ'),(3871,1,'store','=ŬéȳˉĨϢ␞¦ࡴࡲһ'),(3871,1,'maintenance','ㄴ'),(3871,1,'hh','⎻'),(3871,1,'forcing','ᵐ'),(3871,1,'answer','䂁\rĥ໏-'),(3877,1,'answer','܎'),(3878,1,'maintenance','ġ'),(3878,1,'mssql','ʙº҈'),(3879,1,'alarm','䬠ᔲ'),(3871,1,'18446744073709551615','䰧Ü+'),(3834,1,'alarm','°'),(3879,1,'conf','೽ా \"\nō'),(3873,1,'mssql','ѭ\r\r\r\r\r\r'),(3867,1,'alarm',''),(3866,1,'conf','P'),(3879,1,'confirm','অ〷'),(3848,1,'answer','8:'),(3840,1,'routines','Ƭ'),(3881,1,'maintenance','␣'),(3864,1,'alarm','|'),(3880,1,'alarm','ऎ׊ֱb∵2k2	6p@cVKBĀ ٭'),(3879,1,'workspace','ⅈ'),(3879,1,'store','዗່ጾȷޚℑȺ'),(3879,1,'relates','ᇻ'),(3879,1,'hh','⻚'),(3879,1,'maintenance','ᔲஏ'),(3881,1,'conf','㕼/'),(3840,1,'conf','=æÓ'),(3837,1,'lostcrit','Õ'),(3744,1,'ereg','İ'),(3751,1,'store','jB'),(3762,1,'alarm',''),(3765,1,'loadaverage','V'),(3788,1,'alarm','¸	'),(3792,1,'mssql','	'),(3793,1,'mssql','/'),(3796,1,'mssql','	'),(3805,1,'conf',','),(3805,1,'store','D*'),(3806,1,'store','J'),(3827,1,'ereg','Ŭ'),(3881,1,'routines','⭙'),(3881,1,'store','ՏąǒˑᅣƜđh)ƪ໠'),(3883,1,'hh','ą'),(3885,1,'store','ōĵլ'),(3888,1,'store','ć'),(3889,1,'alarm','͈'),(3890,1,'adminpager','ᱽ'),(3890,1,'hh','ᯙ'),(3890,1,'store','รh'),(3792,6,'mssql','n'),(3796,6,'mssql','3');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictEE` ENABLE KEYS */;

--
-- Table structure for table `dictEF`
--

DROP TABLE IF EXISTS `dictEF`;
CREATE TABLE `dictEF` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictEF`
--


/*!40000 ALTER TABLE `dictEF` DISABLE KEYS */;
LOCK TABLES `dictEF` WRITE;
INSERT INTO `dictEF` VALUES (3880,1,'advances','ۂ'),(3871,1,'hour','Էݏ ЂӸࢤΜ̀ླྀ}ŀЌř֢ϣ'),(3879,1,'rrdcreatestring','䧈('),(3879,1,'licenses','˽'),(3880,1,'899','Ո¥'),(3879,1,'total','⻿⅛'),(3879,1,'sslcertificatefile','ᦜ'),(3780,1,'gather','#'),(3776,1,'total',''),(3759,1,'total','Ó'),(3766,1,'gather','('),(3721,1,'reliable','Ʊ'),(3720,1,'font','¤'),(3721,1,'amounts','u$'),(3721,1,'licenses','Ū'),(3871,1,'gather','Ƅ='),(3879,1,'899','Յ¥'),(3872,1,'escalates','ష'),(3817,1,'gather',''),(3795,1,'mssql2',''),(3871,1,'permitted','ⶸ'),(3856,1,'cat','Ť'),(3840,1,'reliable','ɿ'),(3827,1,'fully','Ȱ'),(3871,1,'fully','ظ㎐ൟ'),(3871,1,'font','ᗁ'),(3871,1,'exported','⨠='),(3871,1,'360','≨'),(3866,1,'facilities','&'),(3862,1,'finding','*'),(3880,1,'licenses','ə'),(3880,1,'fully','ݪᏫ'),(3872,1,'phonenum','Ԡ'),(3877,1,'exported','ɏ'),(3871,1,'rooma','⽴'),(3871,1,'total','֖ԆǓ㘄'),(3872,1,'206','ԛ'),(3872,1,'attributes','ࡠ'),(3780,1,'total',''),(3879,1,'hour','⻥'),(3879,1,'horizontally','ᛩ'),(3879,1,'fully','∝\rϩ'),(3879,1,'finding','ⱔ'),(3879,1,'exported','㒟ᒶ.ͺ'),(3879,1,'cat','摝ƾ\nÚ'),(3879,1,'attributes','ࠃRľ@\r£⠏жڽ'),(3880,1,'total','᭗­ᬈφрÊƛՁ'),(3881,1,'899','͘¥'),(3881,1,'attributes','֥ƋÙ¶,\rjᒒƱƳp൤'),(3881,1,'facilities','➲Ħ઱ΘĔ'),(3881,1,'fully','ᦹ'),(3881,1,'hybrid','✬'),(3883,1,'hour','Đ'),(3883,1,'total','Ī'),(3884,1,'attributes','²&\rڕ'),(3887,1,'2d','Ɍ\r஀'),(3887,1,'cubes','̔க'),(3888,1,'172','ޟč'),(3890,1,'hour','ಽ'),(3795,6,'mssql2','');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictEF` ENABLE KEYS */;

--
-- Table structure for table `dictF0`
--

DROP TABLE IF EXISTS `dictF0`;
CREATE TABLE `dictF0` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictF0`
--


/*!40000 ALTER TABLE `dictF0` DISABLE KEYS */;
LOCK TABLES `dictF0` WRITE;
INSERT INTO `dictF0` VALUES (3880,1,'entered','ಕ⚀ᑐ'),(3880,1,'friday','ӵ'),(3871,1,'invalid','▇$˽'),(3871,1,'internally','࡭ᣨ'),(3871,1,'entered','঒Ἒᆖက'),(3787,1,'wrapper','#\r'),(3819,1,'exclusion','¤'),(3822,1,'200','=X'),(3865,1,'runtime','`'),(3871,1,'200','侗#'),(3720,1,'runtime','q'),(3881,1,'friday','̅'),(3874,1,'communicating','ώ'),(3874,1,'200','¥ƫ'),(3873,1,'agentless','Ŵ'),(3873,1,'98','ħ'),(3872,1,'runtime','۷'),(3872,1,'invalid','˨'),(3881,1,'internally','ଭ'),(3879,1,'myorganization','Ოp'),(3879,1,'invalid','൐I'),(3879,1,'entered','঍ഺ6㆗'),(3879,1,'200','㥹'),(3778,1,'invalid','f'),(3871,1,'retrieves','ㅀ'),(3879,1,'friday','Ӳ'),(3877,1,'wrapper','͢'),(3871,1,'wrapper','r'),(3879,1,'wrapper','࿴'),(3881,1,'paradigm','⨣'),(3881,1,'sysmodules','ⴛ'),(3881,1,'wrapper','୥Xᵻ	ଓ΂u'),(3884,1,'entered','Ǎ'),(3885,1,'200','Þ'),(3890,1,'adminemail','ᱜ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictF0` ENABLE KEYS */;

--
-- Table structure for table `dictF1`
--

DROP TABLE IF EXISTS `dictF1`;
CREATE TABLE `dictF1` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictF1`
--


/*!40000 ALTER TABLE `dictF1` DISABLE KEYS */;
LOCK TABLES `dictF1` WRITE;
INSERT INTO `dictF1` VALUES (3880,1,'schedule','ᓧఛ&ܷ\n\nƎ	8\rɘƥ\n'),(3874,1,'loads','ɲ'),(3751,1,'hours','ɲ\r'),(3879,1,'length','᝔'),(3879,1,'loads','吀'),(3880,1,'apc','৭'),(3879,1,'schedule','ᜋU㸮\\'),(3879,1,'clock','⻦'),(3879,1,'resource','ᚧΧ̔ޣ᭖_'),(3881,1,'numofresults','᥂'),(3879,1,'manually','ỶɦϧȁZ⌖ჇЫИ'),(3880,1,'queue','⭖Ӛ'),(3883,1,'clock','đ'),(3881,1,'hours','˵'),(3881,1,'resource','❌QQލ܉Ƙ Ea'),(3881,1,'reacting','ⱆհ'),(3871,1,'summer','ᦾ'),(3871,1,'rrdcgi','ⱵÃƢDc'),(3751,1,'loads','N'),(3757,1,'100000','Ɓ'),(3762,1,'loss',''),(3781,1,'clock','#'),(3803,1,'queue','  '),(3818,1,'imap',''),(3822,1,'loss',' '),(3827,1,'hours','ō'),(3834,1,'loss','¬'),(3862,1,'xs','Ą'),(3863,1,'upgradable','Ā'),(3864,1,'loss','x'),(3865,1,'apc',''),(3869,1,'xs',''),(3871,1,'100000','⥐'),(3871,1,'12373','㟱?'),(3871,1,'clock','⏎ži8'),(3871,1,'dropped','⻗'),(3871,1,'gif','཈4ή\r([pࢢEᄅLrBɮ	ۤ³	­	·Wڵ֤Ĉժ#'),(3871,1,'hours','ഖч!࠷ҙեþzzJ@í࿻Vઃ!W\n	àǮ:'),(3871,1,'length','ট֖ч'),(3872,1,'queue','ӱÖ'),(3720,1,'clock','Ŧ'),(3883,1,'hours','Ċ'),(3880,1,'resource','㢮'),(3878,1,'imap','ɻӹ'),(3880,1,'manually','㛣'),(3729,1,'initiating',''),(3880,1,'initiating','⟟'),(3879,1,'apc','ᔛ'),(3880,1,'identifies','ᆳ'),(3876,1,'loss','ŕ'),(3724,1,'resource','%'),(3879,1,'hours','Ӣ⧽♯	'),(3879,1,'gif','㣐໐ᚖ'),(3880,1,'hours','ӥ᤾;Fᜄ%'),(3884,1,'length','ʧ_Ĩࢄ_Ĩ'),(3885,1,'length','չ©'),(3887,1,'gif','౳sr'),(3887,1,'length','޺©'),(3888,1,'manually','Ѿ'),(3888,1,'resource','ȆO'),(3889,1,'hours','Ǒ'),(3889,1,'identifies','¾W'),(3889,1,'length','ş'),(3890,1,'length','ൈ:٦'),(3890,1,'loads','अ'),(3890,1,'manually','˾'),(3890,1,'queue','የ'),(3890,1,'resource','cࠍ\rᏹ*'),(3890,1,'schedule','ጭģ'),(3892,1,'resource','1\r#'),(3818,6,'imap','Ĩ'),(3865,6,'apc','ª');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictF1` ENABLE KEYS */;

--
-- Table structure for table `dictF2`
--

DROP TABLE IF EXISTS `dictF2`;
CREATE TABLE `dictF2` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictF2`
--


/*!40000 ALTER TABLE `dictF2` DISABLE KEYS */;
LOCK TABLES `dictF2` WRITE;
INSERT INTO `dictF2` VALUES (3879,1,'readme','پ2'),(3877,1,'dir','ݍ'),(3871,1,'website','僌'),(3721,1,'practical','ǉ'),(3823,1,'app','AE#'),(3778,1,'app','ê'),(3754,1,'dir',''),(3751,1,'supercede','Ɯ'),(3724,1,'validated','>\n'),(3722,1,'validated',''),(3721,1,'website','ʅ\n'),(3880,1,'readme','Ͻ4'),(3880,1,'refresh','ờܨ0Ⴏ\n'),(3880,1,'scalability','ۜ'),(3880,1,'troubled','øᖋ¿ˮЧ\n	GƠÚ|&9'),(3880,1,'peering','ᐑ'),(3869,1,'website','ĉ'),(3837,1,'mails','I\n'),(3879,1,'identifiers','备«z'),(3879,1,'finally','⛗'),(3870,1,'website','ó'),(3879,1,'dir','只'),(3871,1,'rrdfetch','ݛࣲ%჈|࢓&'),(3871,1,'refresh','⵿ཱུ'),(3871,1,'mails','倱:'),(3871,1,'inoctets','ࠅ㭺.'),(3871,1,'finally','⑐'),(3871,1,'1200','ೊMᕌ⚃'),(3823,1,'app1','M'),(3823,1,'pn','\' M'),(3880,1,'menus','๔⦛'),(3879,1,'website','垣'),(3879,1,'refresh','㫬'),(3824,1,'app','Ċ\''),(3824,1,'app1','Á'),(3881,1,'modern','ǅ'),(3881,1,'normalizers','ḱķ'),(3881,1,'readme','㖛'),(3881,1,'refresh','⩖෪'),(3881,1,'website','㒶̻'),(3886,1,'refresh','Ǽ'),(3888,1,'menus','Ⱥ'),(3890,1,'dir','ĭᰴ'),(3890,1,'refresh','ܺ	');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictF2` ENABLE KEYS */;

--
-- Table structure for table `dictF3`
--

DROP TABLE IF EXISTS `dictF3`;
CREATE TABLE `dictF3` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictF3`
--


/*!40000 ALTER TABLE `dictF3` DISABLE KEYS */;
LOCK TABLES `dictF3` WRITE;
INSERT INTO `dictF3` VALUES (3881,1,'note','ΪبÞ'),(3871,1,'note','ȭ۴Ō௮͸Ĭą̿ĠŉɟÃĚÞںčıഀ'),(3871,1,'ascii','˒'),(3871,1,'defaults','⎜'),(3880,1,'modify','&'),(3880,1,'edit','㝈'),(3879,1,'note','֗іäÐ\\ʴєkKŊÅ,̿ÝඹɺɋĉĜĆ࿟ႾڙɃ״֌'),(3871,1,'dod','㼭'),(3878,1,'modify','Ĝ'),(3878,1,'11','ʖ'),(3728,1,'ipv4','h'),(3730,1,'defaults','{'),(3732,1,'ipv4','i'),(3737,1,'ipv4','h'),(3738,1,'ipv4','I'),(3739,1,'ipv4','h'),(3740,1,'161','·'),(3742,1,'161','k'),(3744,1,'161',''),(3721,1,'modify','ĺ'),(3721,1,'fsf','Ũ'),(3720,1,'tab','łV'),(3879,1,'defaults','ㅒ\r'),(3881,1,'modify','&㝆ýB'),(3871,1,'modify','዆ᓑ¼'),(3871,1,'showing','ᇟৣĊ'),(3871,1,'producing','ʹ'),(3873,1,'instrumentation',''),(3873,1,'edit','ƣ'),(3873,1,'agents','K'),(3872,1,'startup','Ȅ0'),(3872,1,'note','̅äɣr4Ñː<'),(3872,1,'deviceserial','ƛ#'),(3872,1,'defaults','Ռ͏'),(3872,1,'adding','ݤ'),(3871,1,'usertime','ږ'),(3871,1,'startup','ɀ'),(3884,1,'modify','ࠋFŦW'),(3884,1,'adding','ऊĚ'),(3883,1,'note','g'),(3883,1,'modify',''),(3878,1,'instrumentation','أ33522220030%/'),(3879,1,'showing','ଣôʥ䙟'),(3879,1,'modify','&ᥨਢï-ݣL˃ഫ߰ݹҠԉڥ'),(3879,1,'edit','ৎ\rȹ\nॿΡΉȰſ໠ᅊϞ௡Ԡ'),(3754,1,'defaults','ŉ'),(3757,1,'1024','Ŏ'),(3757,1,'defaults',','),(3760,1,'defaults','9'),(3761,1,'ipv4','k'),(3762,1,'ipv4','@'),(3763,1,'ipv4','G'),(3781,1,'defaults','K'),(3784,1,'ipv4','k'),(3785,1,'ipv4','k'),(3797,1,'samba','\''),(3804,1,'defaults','q'),(3811,1,'ipv4','U'),(3814,1,'ipv4','k'),(3816,1,'defaults',';\r'),(3818,1,'ipv4','k'),(3819,1,'161',']'),(3819,1,'ascii','Ì'),(3819,1,'defaults','4'),(3820,1,'161','²'),(3820,1,'ascii','s'),(3820,1,'defaults','5'),(3827,1,'ipv4','½'),(3827,1,'note','rµ'),(3832,1,'merged','0'),(3833,1,'ipv4','k'),(3834,1,'note','+'),(3844,1,'defaults','N\''),(3851,1,'note','>'),(3853,1,'agents',''),(3854,1,'ipv4','v'),(3856,1,'ipv4','U'),(3862,1,'modify','g'),(3863,1,'note','p±'),(3866,1,'defaults',''),(3868,1,'goundwork',''),(3871,1,'0001','₰'),(3871,1,'1024','ᖀ'),(3871,1,'11','✗Ⴏ'),(3871,1,'5h','⓺'),(3871,1,'978304200','䤣'),(3871,1,'adding','࠾䎨¤\n'),(3752,1,'note','¯'),(3748,1,'ipv4','k'),(3746,1,'samba','\Z'),(3745,1,'ipv4','V'),(3744,1,'note','Ȳ'),(3744,1,'agents','?'),(3722,1,'samba','Ď'),(3879,1,'adding','è٬o0q\\ţS¦ÇÄʞ\nіÛႽĵ⺿â'),(3879,1,'11','Ƣᷨ ව,ࡣอ'),(3881,1,'isacknowledged','ࡍ'),(3881,1,'adding','Ӥᛥ'),(3881,1,'tab','⸔'),(3879,1,'tab','౳ࡈUÝHiF\nഏᔨCHV\'?U'),(3880,1,'correlations','㒶'),(3880,1,'agents','ູϓ'),(3880,1,'adding','㌸'),(3883,1,'edit','{'),(3882,1,'note','ħ'),(3880,1,'showing','Ⓓᓰʻё'),(3880,1,'note','֚ࡷ༲ֿɀ°ƉŹͷD,ǆȯᙘ'),(3877,1,'variants','ȉ'),(3877,1,'startup','̭'),(3877,1,'note','ϣ'),(3877,1,'modify','$'),(3877,1,'edit','ˍt'),(3875,1,'tab','̺'),(3875,1,'modify','̗'),(3873,1,'note','Ȟ'),(3884,1,'note','łઙ'),(3884,1,'tab','ƉՇóś¨*? Ĩ2\rѫ\r'),(3885,1,'adding','Ʃ޹'),(3885,1,'modify','ޖ'),(3885,1,'note','Ȇɻ\''),(3885,1,'tab','ǈׁv+$.+('),(3886,1,'edit',''),(3886,1,'modify','θ'),(3886,1,'note','uÔʂ'),(3886,1,'tab','ƿLJ¥Ů'),(3887,1,'adding','༷M'),(3887,1,'edit','¿ЀӟļѾ'),(3887,1,'modify','ϐ ÌӰĴ¬͇ī'),(3887,1,'note','ÕőǪʣ\'ȪV{˄āÏ'),(3887,1,'tab','༵'),(3888,1,'modify','ؖ'),(3888,1,'note','Ţ¯΅Vş'),(3888,1,'tab','ÖȜǁÓĳ'),(3889,1,'adding','˵\"4'),(3889,1,'modify','˹[w'),(3889,1,'note','ƪʭ\r'),(3889,1,'tab','ф'),(3890,1,'adding','༽'),(3890,1,'edit','7ŉحᑣ'),(3890,1,'modify','ű'),(3890,1,'note','ʬ̾Ĵȝostĸ˾൪k'),(3890,1,'opened','᥵3'),(3890,1,'showing','®ᷱ'),(3890,1,'startup','ๆ5'),(3890,1,'tab','ᤙ5К'),(3891,1,'edit','ˮ®'),(3891,1,'modify','˿¿'),(3891,1,'note','wáȢVp'),(3892,1,'note','ŕ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictF3` ENABLE KEYS */;

--
-- Table structure for table `dictF4`
--

DROP TABLE IF EXISTS `dictF4`;
CREATE TABLE `dictF4` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictF4`
--


/*!40000 ALTER TABLE `dictF4` DISABLE KEYS */;
LOCK TABLES `dictF4` WRITE;
INSERT INTO `dictF4` VALUES (3824,1,'timeout','ģ'),(3871,1,'99','ᨊഌ'),(3856,1,'timeout','\'´'),(3859,1,'bandwidth','\r'),(3819,1,'timeout','Ĉ'),(3864,1,'timeout','2'),(3804,1,'timeout',':H'),(3803,1,'timeout','5='),(3799,1,'timeout','B'),(3799,1,'bandwidth','ö	'),(3762,1,'timeout','.F'),(3860,1,'timeout','>'),(3731,1,'timeout','0['),(3730,1,'timeout','2'),(3757,1,'timeout','W>'),(3739,1,'timeout','7È'),(3851,1,'nice','/'),(3849,1,'timeout','(\"'),(3871,1,'tobias','ھٽƦ႞^_èفȖ˗ί'),(3831,1,'timeout',''),(3848,1,'timeout','5^'),(3822,1,'ttl','bw'),(3766,1,'timeout',';o'),(3748,1,'timeout',':È'),(3755,1,'timeout','-J'),(3732,1,'timeout','7É'),(3734,1,'dbname','5'),(3734,1,'timeout','ä'),(3735,1,'timeout','-4'),(3737,1,'timeout','7È'),(3738,1,'timeout','%/'),(3728,1,'timeout','7È'),(3729,1,'timeout','8b'),(3787,1,'timeout','U\n'),(3862,1,'timeout','H'),(3854,1,'timeout','=é'),(3808,1,'timeout','/5'),(3811,1,'timeout','3w'),(3818,1,'timeout',':È'),(3820,1,'timeout','ú'),(3741,1,'timeout',','),(3763,1,'timeout','~'),(3740,1,'timeout','>­'),(3722,1,'timeout','Œ'),(3871,1,'translate','㕂\"\\ཧ'),(3744,1,'bare','ȝ'),(3744,1,'timeout','[ĕ'),(3745,1,'timeout','1'),(3743,1,'99','j'),(3761,1,'timeout',':È'),(3759,1,'timeout','x'),(3863,1,'timeout','7'),(3822,1,'timeout','j'),(3833,1,'timeout',':È'),(3814,1,'timeout',':È'),(3825,1,'timeout','ĩ'),(3856,1,'null','Ā'),(3798,1,'timeout','r'),(3756,1,'timeout','4P'),(3727,1,'timeout',''),(3785,1,'timeout',':È'),(3784,1,'timeout',':È'),(3783,1,'timeout','*'),(3782,1,'timeout','81'),(3781,1,'jwarn','`'),(3780,1,'vpnp','Ï'),(3780,1,'timeout','6Ħ'),(3872,1,'dtown','Ť'),(3827,1,'timeout','LƓ'),(3743,1,'timeout','+'),(3846,1,'timeout','>Þ'),(3845,1,'timeout','03'),(3843,1,'timeout','S'),(3842,1,'timeout','5'),(3840,1,'timeout','M_Ĺ'),(3840,1,'frequently','ɀ'),(3837,1,'timeout',''),(3835,1,'timeout','C'),(3871,1,'slots','জ'),(3871,1,'paragraphs','䐔ɠ'),(3871,1,'nice','㗊'),(3871,1,'holds','㜃'),(3871,1,'fill','⣅༇L'),(3871,1,'bandwidth','F'),(3872,1,'fill','ޡƨǅů'),(3872,1,'null','͎\\^,'),(3873,1,'argx','ɿ'),(3875,1,'bandwidth','Òlǉ'),(3877,1,'rw','ۤ'),(3879,1,'broaden','ᶑ'),(3879,1,'dbname','刑'),(3879,1,'escalations','Ƅᷨӛ঺అ;Ŧ)Ŗ5'),(3879,1,'holds','够'),(3879,1,'lowercase','ॻ'),(3879,1,'timeout','Ḍ'),(3879,1,'uploaded','⥡'),(3880,1,'bandwidth','ॐഇɯӴᜨ'),(3880,1,'escalations','ࠨڼǶ,Ȳ'),(3880,1,'fill','ⲳѦ'),(3880,1,'frequently','ᄱ'),(3880,1,'lowercase','౥'),(3881,1,'fill','ᦕᡂ'),(3881,1,'null','ঢ়ϐ°(\')\'(\Z+))\Z>\Z+8\'(((?845Ϻ Äឬ¹'),(3884,1,'timeout','ԭ਋'),(3885,1,'escalations','Ȼ'),(3885,1,'frequently','ӆ'),(3885,1,'timeout','ӚĆ'),(3886,1,'escalations','5ʟ'),(3887,1,'escalations','੮\''),(3887,1,'frequently','܇'),(3887,1,'largest','ˣ஑'),(3887,1,'timeout','ܛĆ'),(3889,1,'escalations',':Ȑl\rQ\nZ'),(3890,1,'escalations','᷸)'),(3890,1,'frequently','ᖠ'),(3890,1,'myhost','ȝ'),(3890,1,'nice','Ṍ'),(3890,1,'timeout','ʣᎠĭ'),(3891,1,'timeout','ɋâ˗<'),(3889,2,'escalations',''),(3859,6,'bandwidth','K'),(3889,6,'escalations','Ԁ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictF4` ENABLE KEYS */;

--
-- Table structure for table `dictF5`
--

DROP TABLE IF EXISTS `dictF5`;
CREATE TABLE `dictF5` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictF5`
--


/*!40000 ALTER TABLE `dictF5` DISABLE KEYS */;
LOCK TABLES `dictF5` WRITE;
INSERT INTO `dictF5` VALUES (3872,1,'1a','ݢ'),(3873,1,'msdn','['),(3871,1,'precision','ధӦM\Z!'),(3888,1,'child','źmǾĢ'),(3888,1,'builds','̭'),(3785,1,'accept',''),(3784,1,'tcp',''),(3784,1,'accept',''),(3780,1,'15','g'),(3766,1,'tcp',''),(3766,1,'15','m'),(3763,1,'tcp','â'),(3728,1,'accept',''),(3728,1,'tcp',''),(3730,1,'15','}'),(3731,1,'tcp','S'),(3732,1,'accept',''),(3732,1,'tcp','}'),(3735,1,'15','g'),(3737,1,'accept',''),(3737,1,'tcp',''),(3739,1,'accept',''),(3739,1,'tcp',''),(3740,1,'15','ò'),(3744,1,'designed',';'),(3748,1,'accept',''),(3748,1,'tcp',''),(3753,1,'tcp','q'),(3761,1,'accept',''),(3761,1,'tcp',''),(3720,1,'designed',''),(3880,1,'alarms','ࠦڶκ◌\n\nR7>/%(=\nЊɖ'),(3835,1,'15','J'),(3880,1,'designed','ʲªċኋ⺵'),(3878,1,'15','̖'),(3877,1,'tcp','͡'),(3877,1,'15','ؤ'),(3874,1,'tcp','ĵǶ	'),(3874,1,'15','ɰ'),(3873,1,'workq','Ё	'),(3879,1,'flight','℩ϴɩ࣮ྭË'),(3879,1,'designed','͈Ĝ'),(3804,1,'tcp',''),(3808,1,'15','j'),(3814,1,'accept',''),(3814,1,'tcp',''),(3818,1,'accept',''),(3818,1,'tcp',''),(3819,1,'15','Đ'),(3820,1,'15','Ă'),(3803,1,'15','x'),(3785,1,'tcp',''),(3833,1,'tcp',''),(3880,1,'accept','⯝Ӥ'),(3879,1,'child','⏾ቼųX̟ᷮ'),(3879,1,'ld','൲'),(3879,1,'occasionally','ױ'),(3871,1,'15','ßමᏨ\Z#ᔝ0'),(3871,1,'accept','ܧ!;ⅴ'),(3871,1,'builds','ਮ'),(3871,1,'fooled','䅫'),(3871,1,'occasionally','䶓'),(3854,1,'tcp','À'),(3845,1,'15','i'),(3844,1,'kmg','n'),(3843,1,'15','Y'),(3887,1,'child','Oƣ	࣑	\n'),(3886,1,'child','ɻ	'),(3883,1,'15','ĕ'),(3881,1,'tcp','⏒'),(3881,1,'occasionally','Є'),(3881,1,'jms','Ḣħ'),(3881,1,'designed','ƜÛ╱ª'),(3881,1,'child','጖࢟'),(3881,1,'15','⏱'),(3880,1,'webservices','ݦ'),(3880,1,'occasionally','״'),(3888,1,'flight','ɣ­֮'),(3833,1,'accept',''),(3879,1,'section1','婲PʅW'),(3879,1,'15','⻪ជͷᢤ'),(3880,1,'child','ํ'),(3879,1,'critn','䕤'),(3854,1,'accept','¿'),(3872,1,'designed',''),(3879,1,'confirmation','ই'),(3888,1,'subroutines','ʾ'),(3889,1,'15','Ż͓'),(3890,1,'accept','਻o{tS'),(3890,1,'flight','ᴢŤ'),(3732,6,'tcp','Ħ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictF5` ENABLE KEYS */;

--
-- Table structure for table `dictF6`
--

DROP TABLE IF EXISTS `dictF6`;
CREATE TABLE `dictF6` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictF6`
--


/*!40000 ALTER TABLE `dictF6` DISABLE KEYS */;
LOCK TABLES `dictF6` WRITE;
INSERT INTO `dictF6` VALUES (3880,1,'commandview','ᡶ᪬'),(3872,1,'final','ǹਔ'),(3871,1,'operators','ňᙰ'),(3880,1,'capacity','㓂'),(3871,1,'midnight','⏛˟\"'),(3856,1,'passphrase','ā'),(3862,1,'pcap','A'),(3840,1,'expect',''),(3837,1,'interval','¹'),(3833,1,'expect','/P'),(3879,1,'setting','࠳Ǎɼǆ٥ဏśھ࿡ᕾລ'),(3879,1,'operators','ܛօ'),(3879,1,'midnight','⻹⚁'),(3879,1,'loading','✎ਛ‛'),(3879,1,'26','搞'),(3879,1,'accidentally','ਔ'),(3879,1,'db','䍾¯љ'),(3827,1,'expect','V'),(3877,1,'simplicity','ì'),(3761,1,'expect','/P'),(3778,1,'capacity',''),(3778,1,'db','$$'),(3784,1,'expect','/P'),(3785,1,'expect','/P'),(3792,1,'dbserver','3'),(3793,1,'dbserver','1'),(3814,1,'expect','/P'),(3818,1,'expect','/P'),(3819,1,'v2c','<¼'),(3820,1,'v2c','=­'),(3821,1,'setting','!'),(3822,1,'interval','R'),(3822,1,'verbosity','ys'),(3757,1,'verbosity','¥'),(3720,1,'loading','\\Ø'),(3873,1,'setting','ǣ'),(3873,1,'final','ʐ'),(3854,1,'expect','2q'),(3877,1,'setting','('),(3848,1,'interval','0'),(3872,1,'setting','׊\"ʤ'),(3871,1,'setting','ѫృᡀ '),(3871,1,'interval','ϊT;lUnǆɟm\n7\'ϡ˰࿝yʇ{Ñ⢱I'),(3871,1,'final','܊ᾗ'),(3871,1,'expect','与'),(3871,1,'887457267','຾'),(3870,1,'final','c'),(3865,1,'equipped',' '),(3880,1,'contract','ᤷమ'),(3878,1,'setting','ࣵ'),(3878,1,'26','Е'),(3877,1,'passphrase','й'),(3728,1,'expect',',P'),(3729,1,'expect','-<'),(3730,1,'verbosity',']'),(3732,1,'expect',',Q'),(3737,1,'expect',',P'),(3739,1,'expect',',P'),(3744,1,'expect','Ɉ'),(3745,1,'expect','&;'),(3748,1,'expect','/P'),(3751,1,'interval','JH°b'),(3754,1,'setting','Đ'),(3754,1,'verbosity','ŵ'),(3755,1,'expect','%-'),(3756,1,'expect','87'),(3757,1,'ppid','H'),(3880,1,'interval','፛ૂᨄóy_VJPMÚ9aZ\\LJBigbĤ,@@AO'),(3880,1,'operators','Ϳ౲㗙'),(3880,1,'setting','ℽᐈཛྷ'),(3881,1,'critiques','Ȿ'),(3881,1,'final','᧑Ἧ'),(3881,1,'simplicity','⢓'),(3883,1,'midnight','Ĥ'),(3884,1,'interval','ɪ<(ą#ࡈ<(ą#'),(3884,1,'setting','ïŧ਋'),(3885,1,'interval','ԳE!'),(3885,1,'setting','Ӽ'),(3887,1,'final','η0'),(3887,1,'interval','ݴE!'),(3887,1,'setting','ܽ'),(3888,1,'automated','ː'),(3888,1,'figures','գ'),(3888,1,'setting','Ř11'),(3889,1,'interval','ŉ\r\"̑'),(3889,1,'setting','Ļ'),(3890,1,'interval','न\"ϕ:ĸпäoբ*R/µH'),(3890,1,'loading','ỳ'),(3890,1,'midnight','ು'),(3890,1,'setting','ͫׅ\n\ne\n~\ni\nj\nÃz</ĳş¸ƉݨH'),(3877,2,'setting','');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictF6` ENABLE KEYS */;

--
-- Table structure for table `dictF7`
--

DROP TABLE IF EXISTS `dictF7`;
CREATE TABLE `dictF7` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictF7`
--


/*!40000 ALTER TABLE `dictF7` DISABLE KEYS */;
LOCK TABLES `dictF7` WRITE;
INSERT INTO `dictF7` VALUES (3879,1,'products','ӕ'),(3879,1,'mapped','严'),(3879,1,'leave','㬠'),(3879,1,'functionality',' Ċ␀ǔ'),(3880,1,'functionality','ཝ୿'),(3880,1,'breaches','ᩛ'),(3807,1,'detector',''),(3763,1,'5432','D'),(3754,1,'unknowns','æ'),(3752,1,'mapped','*'),(3881,1,'webapps','◡'),(3881,1,'products','˨̃'),(3881,1,'functionality','ỉ౵'),(3879,1,'simplifying','㎴υ'),(3871,1,'920807400','㠿{'),(3751,1,'shortened','ȶ'),(3871,1,'meaningful','ガ'),(3871,1,'mailing','㌄1#᳉'),(3871,1,'live','㘧਑'),(3871,1,'leave','㍹ఓ'),(3871,1,'glad','㴮'),(3871,1,'functionality','ǰ'),(3871,1,'chooses','ᚃ'),(3871,1,'abort','ڷܭ'),(3879,1,'underscore','堓'),(3884,1,'rechecked','ʞ਋'),(3871,1,'pretty','Ɓⲵ'),(3871,1,'underscore','⛒'),(3872,1,'leave','Ձ'),(3873,1,'functionality','L'),(3879,1,'abort','を㉣\''),(3880,1,'rechecked','ᬊ'),(3880,1,'products','Ә㎹'),(3880,1,'mapped','ွ'),(3885,1,'leave','ҧ'),(3886,1,'leave','ș'),(3886,1,'underscore','Ӣ'),(3887,1,'leave','ۨو'),(3888,1,'underscore','ߎs'),(3890,1,'abort','ẽ2'),(3890,1,'ensuring','ᭊ'),(3890,1,'functionality','ᇘ'),(3890,1,'leave','͌ਃȓ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictF7` ENABLE KEYS */;

--
-- Table structure for table `dictF8`
--

DROP TABLE IF EXISTS `dictF8`;
CREATE TABLE `dictF8` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictF8`
--


/*!40000 ALTER TABLE `dictF8` DISABLE KEYS */;
LOCK TABLES `dictF8` WRITE;
INSERT INTO `dictF8` VALUES (3871,1,'rises','䠉'),(3877,1,'24','ز'),(3878,1,'24','υ'),(3879,1,'24','ӹi\r⥵♫'),(3879,1,'698','幈'),(3811,1,'pass','|'),(3805,1,'pass',''),(3790,1,'pass','='),(3782,1,'24','´'),(3778,1,'pass','.'),(3873,1,'pass','Č'),(3880,1,'creates','᥇ฯ໵'),(3877,1,'199','ؕ'),(3832,1,'pass',')'),(3871,1,'cos','៲'),(3840,1,'pass','ű'),(3871,1,'24','ᅶ!ስŸśံ_ા<Ãä ঱'),(3871,1,'pass','ࣗࠣᯚíῧ'),(3871,1,'millimeters','㩸'),(3871,1,'height','༿ѾƑᣔěᾼ#'),(3871,1,'draw','᭥¹ɂቲዤ'),(3871,1,'creates','⹐´'),(3871,1,'replacing','ᒫ'),(3840,1,'abcabcabc','Ǿ'),(3837,1,'pass','À'),(3880,1,'24','Ӽi\r'),(3879,1,'tr','峸$H¥\r\r'),(3879,1,'pass','䉽'),(3879,1,'creates','✒ᐞ'),(3766,1,'molitors','Ï'),(3744,1,'str','Ʌ'),(3740,1,'pass','na'),(3734,1,'pass','q.'),(3881,1,'24','̌i\r'),(3881,1,'8080','க'),(3881,1,'creates','಑ຠ<'),(3881,1,'encourages','ⱑ'),(3881,1,'expedite','➷Ȫ'),(3881,1,'getchecktype','Ⴞ'),(3881,1,'getconsolidationcriteria','ဥ'),(3881,1,'pass','඀⎻'),(3881,1,'replacing','ⴤ'),(3881,1,'timeunreachable','ࡐ'),(3883,1,'24','ď'),(3884,1,'pass','ܒ'),(3886,1,'creates','ȣ'),(3887,1,'creates','˟஑ó'),(3888,1,'draw','Ѭ'),(3889,1,'creates','Ҫ'),(3892,1,'pass','Ť');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictF8` ENABLE KEYS */;

--
-- Table structure for table `dictF9`
--

DROP TABLE IF EXISTS `dictF9`;
CREATE TABLE `dictF9` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictF9`
--


/*!40000 ALTER TABLE `dictF9` DISABLE KEYS */;
LOCK TABLES `dictF9` WRITE;
INSERT INTO `dictF9` VALUES (3881,1,'inputmessage','〙¦ß×'),(3880,1,'start','ᘅࡏ  ড়#W#ʠÝĞ#ŭ&Ų۳ˤоɣ'),(3879,1,'accesses','መ)	'),(3879,1,'choose','ᱣФ┕/'),(3879,1,'png','㢌E'),(3879,1,'start','෉߭අ?ޒŃ׷ѡྣƑǦĈ\nĥऴુ\nȁ%'),(3880,1,'choose','ᵔٶᆧ '),(3871,1,'start','ْHߜĔඥ·54­\nŐ\"ʎʖ>í2˄ƼȳǪ#ǢC\ZƳDōà»ǈ¶ÍÍȒø3ͭƪȂҌ>#'),(3871,1,'roomb','⽾'),(3763,1,'start','å'),(3780,1,'tcb','ą'),(3791,1,'start','k'),(3797,1,'queues','#'),(3803,1,'queues','$}'),(3809,1,'cloadn','O'),(3820,1,'reboot','Ņ'),(3846,1,'choose','»'),(3868,1,'start',''),(3871,1,'999987','䭻{'),(3871,1,'choose','䶉'),(3871,1,'counting','㒿'),(3871,1,'disappeared','㫝'),(3871,1,'guaranteed','○'),(3871,1,'png','ཉѪਛ'),(3877,1,'start','Ǧþ'),(3872,1,'queues','Ӽ'),(3872,1,'start','ɃØJ+H'),(3873,1,'start','Ǣ'),(3877,1,'choose','ģ'),(3881,1,'opengroup','㏛'),(3881,1,'start','ំ©Ý؊ͨɻࢲ'),(3882,1,'choose','Ğ'),(3884,1,'choose','ߊo˧'),(3885,1,'choose','ɐ՛'),(3886,1,'start','Ì'),(3887,1,'png','౴so*'),(3887,1,'start','ô'),(3888,1,'start','ࡄ'),(3889,1,'start','¶ɢ'),(3890,1,'choose','ФJᩩ'),(3890,1,'fresher','ˑ'),(3890,1,'start','˸ᨠ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictF9` ENABLE KEYS */;

--
-- Table structure for table `dictFA`
--

DROP TABLE IF EXISTS `dictFA`;
CREATE TABLE `dictFA` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictFA`
--


/*!40000 ALTER TABLE `dictFA` DISABLE KEYS */;
LOCK TABLES `dictFA` WRITE;
INSERT INTO `dictFA` VALUES (3888,1,'advanced',''),(3880,1,'listing','ɭ᩶ˀțï␔'),(3880,1,'measurement','㟂}űƯñ̴ðňu'),(3879,1,'img','帲'),(3880,1,'consolidate','㘆'),(3880,1,'advanced','ࠑ4ǯ Ⳕ࿕'),(3879,1,'suggestions','ك!'),(3879,1,'measurement','‹┕	;'),(3871,1,'measurement','ᖊ'),(3871,1,'modification','ⷌ'),(3871,1,'silently','ᙥ'),(3871,1,'weekdays','䕅'),(3881,1,'getcomponent','༡'),(3881,1,'advanced','᱉॰/\''),(3881,1,'forwards','Ἤ'),(3881,1,'consolidate','؈ᜪۧ'),(3871,1,'interpolates','书j'),(3720,1,'measurement','¢'),(3874,1,'advanced','ǐ'),(3872,1,'misccommands','Ą׊'),(3884,1,'advanced','ă߸³'),(3879,1,'listing','ຎ༖⋍ྦྷஊà'),(3721,1,'successful','>'),(3729,1,'successful','½'),(3881,1,'attributename','ụ'),(3881,1,'phptal','⿜'),(3881,1,'rendered','ㄯ'),(3881,1,'suggestions','ј!⠃'),(3879,1,'advanced','łၩഁýԀėૈʝǼτʬĊÍËNÃ¨'),(3880,1,'misccommands','ᓒ'),(3878,1,'measurement','फ'),(3876,1,'advanced','Ă'),(3888,1,'deployment','ȓLI'),(3890,1,'advanced','ň'),(3890,1,'listing','᳥'),(3748,6,'simap','Ĩ'),(3871,1,'impose','ຆ'),(3871,1,'img','ᑁᨉ[Ě೙'),(3850,1,'pint','/'),(3827,1,'successful','ȁ'),(3798,1,'modification','Ú'),(3763,1,'template1','Xf'),(3756,1,'listing','Ì'),(3755,1,'successful',''),(3745,1,'successful','Ö'),(3748,1,'simap',''),(3880,1,'suggestions','ن!'),(3879,1,'consolidate','䭊'),(3875,1,'advanced','Ơ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictFA` ENABLE KEYS */;

--
-- Table structure for table `dictFB`
--

DROP TABLE IF EXISTS `dictFB`;
CREATE TABLE `dictFB` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictFB`
--


/*!40000 ALTER TABLE `dictFB` DISABLE KEYS */;
LOCK TABLES `dictFB` WRITE;
INSERT INTO `dictFB` VALUES (3880,1,'solid','ᧉ'),(3871,1,'commonly','䀑'),(3871,1,'correction2','䴩\"'),(3871,1,'def','ġ๘ۮ΀ΌE¥ସÅaęˢxG॑ƕà»ܜ֢ٚ#'),(3887,1,'retention','Ղ¥\r'),(3886,1,'instance','ϭ\nÚG'),(3776,6,'instance','Ĳ'),(3892,1,'commonly',''),(3890,1,'rules','ʓ'),(3890,1,'retention','েnBlFGƲP7\n'),(3823,1,'published','p&'),(3803,1,'domain','Z'),(3776,1,'instance','\ZÛ'),(3765,1,'instance','1'),(3740,1,'2c','æ'),(3889,1,'instance','ÉW'),(3881,1,'custom','ƺбឭ̣eԃӸ'),(3881,1,'exercises','ુ'),(3881,1,'getmonitorstatus','๠'),(3879,1,'custom','˩୒䨥'),(3879,1,'commonly','░'),(3879,1,'arg3','㋓'),(3878,1,'published','+'),(3877,1,'published','2'),(3878,1,'domain','ƿǋÝąà'),(3871,1,'cel','ᶍᆊr\n'),(3885,1,'retention','̄¥\r'),(3884,1,'retention','ׯ#৆#'),(3884,1,'commonly','ۇ'),(3881,1,'instance','ౕƹള='),(3881,1,'oda','☘1'),(3881,1,'published','4'),(3881,1,'rules','ṀĮ'),(3884,1,'arg3','܄'),(3827,1,'domain','Ȳ'),(3828,1,'2c','C'),(3840,1,'domain','$ɇ'),(3841,1,'arg3','£'),(3844,1,'domain','L'),(3866,1,'lhv','W'),(3870,1,'excellent',''),(3870,1,'manipulating',''),(3871,1,'4294967295','㓃ᝠÛ'),(3871,1,'worked','㦡'),(3871,1,'rules','ཨڇዏ'),(3871,1,'manipulating','ῌ'),(3871,1,'legends','ᴫ'),(3871,1,'instance','ጳἣᔴ'),(3871,1,'forget','䆟'),(3871,1,'enjoyed','傑'),(3880,1,'published','4'),(3880,1,'instance','ℓ'),(3824,1,'published','-NY\r'),(3890,1,'excellent','ᴮ'),(3890,1,'domain','Μ'),(3879,1,'domain','ᚘ'),(3890,1,'instance','᫼'),(3888,1,'instance','ǆ>\"\r\r,9F$Z*ʲ'),(3879,1,'published','4'),(3879,1,'manipulating','⃽'),(3722,1,'domain','\"	B	!*\n'),(3825,1,'published',''),(3880,1,'custom','Ɉ㔅°'),(3880,1,'forget','൬'),(3871,1,'domain','ᘰ'),(3874,1,'arg3','¹Ƙ'),(3873,1,'arg3','˪!\"!?\'\'%%%*\'*\'(%'),(3879,1,'instance','ᙧ4');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictFB` ENABLE KEYS */;

--
-- Table structure for table `dictFC`
--

DROP TABLE IF EXISTS `dictFC`;
CREATE TABLE `dictFC` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictFC`
--


/*!40000 ALTER TABLE `dictFC` DISABLE KEYS */;
LOCK TABLES `dictFC` WRITE;
INSERT INTO `dictFC` VALUES (3740,1,'protocol','b'),(3740,1,'connect','Œ\Z'),(3738,1,'connect',''),(3734,1,'orders','-'),(3734,1,'connect','6g'),(3828,1,'protocol','?'),(3798,1,'transfer',''),(3795,1,'connect','N'),(3755,1,'connect','M'),(3881,1,'rollback','Ⅷ'),(3831,1,'connect','Z'),(3776,1,'connect','V'),(3881,1,'protocol','᯷ȗÏ'),(3879,1,'transfer','䥗'),(3881,1,'consist','Ნ'),(3827,1,'virtual','Ƒ'),(3827,1,'headers','Ą'),(3871,1,'protocol','㆐'),(3880,1,'consist','Პ'),(3878,1,'protocol','ȂȷG85\':7/Ê'),(3874,1,'protocol','Q'),(3871,1,'transfer','῁'),(3871,1,'virtual','᙮b'),(3731,1,'connect','*$!\r'),(3759,1,'mem','\Z1^'),(3757,1,'virtual',''),(3722,1,'connect',']'),(3780,1,'abended','y'),(3792,1,'connect','J'),(3793,1,'connect','['),(3871,1,'headers','ⲪÖ'),(3871,1,'fact','⪊'),(3856,1,'protocol','e'),(3844,1,'connect',''),(3837,1,'pendc','Ĝ'),(3747,1,'smartmontools',''),(3881,1,'fact','ᬃ'),(3879,1,'headers','丆ȕ'),(3879,1,'connect','切'),(3881,1,'connect','਩	ˍᜪ'),(3880,1,'virtual','∓ᠿˁ['),(3878,1,'transfer','ȁʝȖ'),(3880,1,'protocol','㫭̍'),(3880,1,'fact','④8'),(3745,1,'220','m'),(3744,1,'protocol',''),(3729,1,'connect','µ'),(3763,1,'connect','Ë$'),(3885,1,'fact','Ƭ'),(3890,1,'shuts','ใP'),(3800,6,'mem','O'),(3827,1,'connect','É'),(3820,1,'protocol',''),(3819,1,'protocol','á'),(3871,1,'noon','ᆑቍ̟࿹'),(3871,1,'het','⡊߽⁾'),(3874,1,'mem','â|'),(3873,1,'mem','̱\n\r'),(3872,1,'protocol','d!౔'),(3800,1,'mem',''),(3811,1,'protocol','');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictFC` ENABLE KEYS */;

--
-- Table structure for table `dictFD`
--

DROP TABLE IF EXISTS `dictFD`;
CREATE TABLE `dictFD` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictFD`
--


/*!40000 ALTER TABLE `dictFD` DISABLE KEYS */;
LOCK TABLES `dictFD` WRITE;
INSERT INTO `dictFD` VALUES (3874,2,'profile',''),(3875,2,'profile',''),(3876,2,'profile',''),(3874,7,'profile','ϴ'),(3875,7,'profile','͊'),(3876,7,'profile','Ʊ'),(3887,1,'profile','Ƨơ.-\r\n੫\Z#/\n'),(3890,1,'profile','Ḓk'),(3720,1,'improved','æ'),(3724,1,'lives','T'),(3747,1,'exitcode','0'),(3762,1,'contrib','É'),(3799,1,'vwl','¾'),(3870,1,'improved','ø'),(3871,1,'exhaust','ᶐᆖ|᠀'),(3871,1,'ff0000','ᶟīᩰ¥ôúÉಘڂ#'),(3871,1,'goodfor','ⵞ'),(3871,1,'improved','ć'),(3871,1,'interests','և㪪'),(3871,1,'maintained','ҡ'),(3871,1,'opposite','丨'),(3872,1,'recipients','Ӌ'),(3874,1,'profile','*ÝW$'),(3875,1,'profile','\Z\'¶J$2'),(3876,1,'profile','T<$'),(3878,1,'profile','M\r5-!\n  \"  \"\"\Z\r\r\'C012/$11*03352222003\"3,#%'),(3879,1,'companydocumentation','塒qrŕs'),(3879,1,'lastservicecheck','䐯ɭ*̰'),(3879,1,'profile','Ǻၙ࿙_\rG««ļ̫ĴƁÃ®ϧˇR65 \r:\'´ǝEgíSĕǆ5;;ɀ՛mȕ!<!ĺ'),(3879,1,'trailing','ᙶ䊭'),(3880,1,'archosts','​)'),(3880,1,'essence','㒆'),(3880,1,'improved','ۙ'),(3880,1,'maintained','ḹ'),(3880,1,'profile','ࡕ৒㋎)'),(3881,1,'getwebserviceurl','ய'),(3881,1,'hostid','ᑭƗ\r'),(3884,1,'profile','ࢆ\r9\rȝ'),(3885,1,'profile','!	\nI?)*\Z	#ҹh@	\r	\Z\Z \"$#\rA'),(3886,1,'profile','ƾ\n7Ʒ');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictFD` ENABLE KEYS */;

--
-- Table structure for table `dictFE`
--

DROP TABLE IF EXISTS `dictFE`;
CREATE TABLE `dictFE` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictFE`
--


/*!40000 ALTER TABLE `dictFE` DISABLE KEYS */;
LOCK TABLES `dictFE` WRITE;
INSERT INTO `dictFE` VALUES (3872,1,'inheritance','ூ'),(3880,1,'monday','Ӵ'),(3879,1,'useddiskspace','㋍'),(3864,1,'atalk','	'),(3871,1,'cope','║'),(3782,1,'shot','Ã'),(3827,1,'pagesize','ƻ'),(3793,1,'f00bar','3'),(3871,1,'monday','ᅾኃ'),(3792,1,'f00bar','5'),(3879,1,'monday','ӱ'),(3879,1,'inheritance','㪫'),(3879,1,'css','ู䧂Ԑ'),(3877,1,'sshd','ťo#Î&*'),(3782,1,'useddiskspace','Û'),(3881,1,'monday','̄'),(3884,1,'inheritance','Óਁ'),(3871,1,'rate','ࠩ<ØĘ(Ã×㣃'),(3884,1,'rate','˸਋'),(3885,1,'inheritance','ʔ'),(3885,1,'nonstatus','Κ'),(3886,1,'inheritance','Ƌ'),(3887,1,'nonstatus','ט'),(3890,1,'rate','ܻ	'),(3891,1,'inheritance','ř');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictFE` ENABLE KEYS */;

--
-- Table structure for table `dictFF`
--

DROP TABLE IF EXISTS `dictFF`;
CREATE TABLE `dictFF` (
  `url_id` int(11) NOT NULL default '0',
  `secno` tinyint(3) unsigned NOT NULL default '0',
  `word` varchar(255) NOT NULL default '',
  `intag` blob NOT NULL,
  KEY `url_id` (`url_id`),
  KEY `word_url` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `dictFF`
--


/*!40000 ALTER TABLE `dictFF` DISABLE KEYS */;
LOCK TABLES `dictFF` WRITE;
INSERT INTO `dictFF` VALUES (3881,1,'additional','ɐέ㌴'),(3840,1,'intended','ʂ'),(3840,1,'10','´'),(3837,1,'messages','´'),(3762,1,'10','|'),(3878,1,'assigning','࠺\"'),(3729,1,'10','¢'),(3846,1,'10','Ĥ'),(3780,1,'10','Ť'),(3780,1,'extension','Ų'),(3782,1,'10','rJ'),(3783,1,'10','N'),(3784,1,'10','Ċ'),(3785,1,'10','Ċ'),(3794,1,'mssql2000',''),(3798,1,'outgoing','!b'),(3799,1,'track','ğ'),(3803,1,'messages','\"'),(3805,1,'messages',''),(3744,1,'10','Ÿ·'),(3888,1,'box','Ҷ'),(3881,1,'10','ᾂ'),(3880,1,'track','थ⺪'),(3880,1,'networks','ࣩ๿'),(3880,1,'topics','τ'),(3881,1,'intended','ᷨ'),(3881,1,'facility','⢇Ι'),(3887,1,'box','ସϛÀ'),(3822,1,'outgoing','e'),(3822,1,'25',''),(3813,1,'intended','5'),(3813,1,'additional','3'),(3888,1,'assigning','Ńۄ'),(3871,1,'box','Ç'),(3871,1,'additional','ᄊ॑ˇ'),(3873,1,'move','ȉ'),(3879,1,'105','䶈'),(3879,1,'10','ƔᴃåῊ⇒'),(3885,1,'move','ƿ'),(3887,1,'assigning','ཕ'),(3887,1,'additional','А'),(3833,1,'10','Ċ'),(3827,1,'10','ǧ|'),(3827,1,'messages','Ȗ'),(3830,1,'messages','I'),(3831,1,'10',''),(3763,1,'10',''),(3881,1,'messages','ԟȲ৊୉Ȃ͛Ȯ	جF'),(3740,1,'messages','g\ra('),(3880,1,'messages','ྡ'),(3880,1,'move','๣'),(3880,1,'navigational','๨'),(3871,1,'messages','ࣉˠ+\n'),(3871,1,'magnitude','ᩫ$	'),(3871,1,'intended','⸳'),(3871,1,'ffffffff','䰼'),(3738,1,'10','\\'),(3739,1,'10','ć'),(3728,1,'10','ć'),(3881,1,'track','⦑Ť'),(3737,1,'10','ć'),(3734,1,'25','Ú'),(3720,1,'spinner','Ş'),(3727,1,'10',''),(3885,1,'box','ʗ\r'),(3880,1,'intended','Α㐞ฯ'),(3884,1,'parallel','ͩ਋'),(3734,1,'10','â'),(3733,1,'messages',''),(3732,1,'10','Ĉ'),(3878,1,'25','Ͽ'),(3878,1,'additional','Ϲ'),(3879,1,'intended','㇀ʇǼ'),(3731,1,'10',''),(3879,1,'extension','̫'),(3871,1,'extension','ܢ'),(3871,1,'exponent','༱Ϗ?'),(3881,1,'assigning','イ'),(3872,1,'box','௄'),(3880,1,'extension','ʕ'),(3880,1,'facility','ॺ'),(3884,1,'additional','ጇ'),(3884,1,'box','ۘǢĔİ-׌ɷ2'),(3884,1,'10','ܜ'),(3880,1,'box','௼CాӷĉHŜȇʠ\n	ƋnW²6ұђМƛဿ'),(3880,1,'assigning','䔜'),(3880,1,'additional','ρદĔԺჸὛ'),(3881,1,'topics','ɓ'),(3889,1,'assigning','θR:'),(3879,1,'additional','ЬಘËز״═țྶ{Ǔř'),(3879,1,'25','䶐'),(3878,1,'track','प'),(3811,1,'10','²'),(3871,1,'track','㸐'),(3822,1,'10','o'),(3814,1,'10','Ċ'),(3818,1,'10','Ċ'),(3889,1,'additional','σ'),(3874,1,'10','Å[ŵ'),(3873,1,'nologo','Ă'),(3878,1,'10','ɸ'),(3871,1,'thu','⚻\"'),(3871,1,'887457521','ເ'),(3871,1,'25','ᨋ౿ᅑ౜'),(3871,1,'10','ᄽÐåཊ0ጪİk0ȏ܋+Ƕ֚ú?Ϗ'),(3870,1,'extension','\n	\r'),(3868,1,'additional',''),(3863,1,'nolocking','l'),(3863,1,'10','M'),(3856,1,'additional','Ĵ'),(3856,1,'10','ã'),(3854,1,'clamd',''),(3854,1,'10','Į'),(3848,1,'10',''),(3890,1,'additional','๺'),(3872,1,'pc','¿ЛB'),(3872,1,'messages','o҈'),(3879,1,'messages','䪖ĭ֠ŨN`\ZÙ<T(੫ăU£>ͺ+'),(3886,1,'box','ΰ'),(3886,1,'assigning','Ȟ'),(3879,1,'box','ଵôᴧŻҏ֚Ϝ፪ Yȕ͐'),(3879,1,'assigning','ᓟ͌kകƱພԒ'),(3748,1,'10','Ċ'),(3752,1,'10','ã'),(3755,1,'10',''),(3755,1,'messages','®'),(3756,1,'10',''),(3757,1,'10','Æ#'),(3761,1,'10','Ċ'),(3876,1,'additional','Ó'),(3871,1,'referring','⹡'),(3871,1,'outgoing','ގ'),(3871,1,'move','ᓸ'),(3875,1,'box','ʺ'),(3882,1,'box','Ĕt'),(3875,1,'additional','jć'),(3874,1,'additional','ơ'),(3745,1,'messages','ë'),(3745,1,'25','S'),(3745,1,'10','È'),(3720,1,'jum','f'),(3766,1,'10','²'),(3765,1,'10','f'),(3878,1,'messages','؎'),(3890,1,'box','È®᭄'),(3890,1,'facility','ྩ'),(3890,1,'intended','ࡐ'),(3890,1,'messages','ɵയ'),(3890,1,'parallel','Ꮑ'),(3891,1,'box','Ŝ\r'),(3892,1,'box','Ơ'),(3794,6,'mssql2000','1'),(3854,6,'clamd','Ō');
UNLOCK TABLES;
/*!40000 ALTER TABLE `dictFF` ENABLE KEYS */;

--
-- Table structure for table `links`
--

DROP TABLE IF EXISTS `links`;
CREATE TABLE `links` (
  `ot` int(11) NOT NULL default '0',
  `k` int(11) NOT NULL default '0',
  `weight` float NOT NULL default '0',
  UNIQUE KEY `links_links` (`ot`,`k`),
  KEY `links_ot` (`ot`),
  KEY `links_k` (`k`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `links`
--


/*!40000 ALTER TABLE `links` DISABLE KEYS */;
LOCK TABLES `links` WRITE;
INSERT INTO `links` VALUES (0,3818,0),(0,3804,0),(0,3865,0),(0,3812,0),(0,3798,0),(0,3883,0),(0,3807,0),(0,3742,0),(0,3744,0),(0,3892,0),(0,3755,0),(0,3759,0),(0,3856,0),(0,3814,0),(0,3774,0),(0,3874,0),(0,3811,0),(0,3878,0),(0,3813,0),(0,3745,0),(0,3783,0),(0,3737,0),(0,3766,0),(0,3880,0),(0,3854,0),(0,3751,0),(0,3781,0),(0,3852,0),(0,3789,0),(0,3795,0),(0,3868,0),(0,3729,0),(0,3788,0),(0,3845,0),(0,3784,0),(3871,3894,0),(0,3722,0),(0,3767,0),(0,3819,0),(0,3820,0),(0,3840,0),(0,3803,0),(0,3866,0),(0,3793,0),(0,3872,0),(0,3791,0),(0,3778,0),(0,3738,0),(0,3736,0),(3879,3887,0),(0,3853,0),(0,3747,0),(0,3826,0),(0,3779,0),(0,3805,0),(0,3828,0),(0,3799,0),(0,3808,0),(0,3761,0),(0,3877,0),(0,3725,0),(0,3861,0),(0,3728,0),(0,3863,0),(3879,3885,0),(0,3844,0),(0,3727,0),(0,3730,0),(0,3876,0),(0,3746,0),(0,3756,0),(0,3867,0),(0,3832,0),(0,3796,0),(0,3831,0),(0,3815,0),(0,3835,0),(0,3802,0),(0,3875,0),(0,3758,0),(0,3740,0),(0,3741,0),(0,3860,0),(0,3762,0),(0,3780,0),(3879,3888,0),(0,3824,0),(0,3797,0),(3879,3891,0),(3879,3883,0),(0,3721,0),(0,3776,0),(0,3837,0),(0,3806,0),(0,3760,0),(0,3850,0),(3879,3892,0),(0,3833,0),(0,3881,0),(3879,3889,0),(0,3817,0),(0,3839,0),(0,3846,0),(0,3723,0),(3879,3886,0),(0,3777,0),(0,3724,0),(0,3830,0),(0,3870,0),(0,3834,0),(0,3809,0),(3879,3884,0),(0,3731,0),(0,3827,0),(0,3775,0),(0,3787,0),(0,3848,0),(0,3884,0),(0,3782,0),(0,3733,0),(0,3800,0),(0,3891,0),(0,3879,0),(3879,3882,0),(0,3841,0),(0,3720,0),(0,3816,0),(0,3739,0),(0,3734,0),(3881,3893,0),(0,3754,0),(3879,3890,0),(0,3768,0),(0,3885,0),(0,3842,0),(0,3887,0),(0,3851,0),(0,3735,0),(0,3801,0),(0,3847,0),(0,3752,0),(0,3790,0),(0,3882,0),(0,3869,0),(0,3871,0),(0,3823,0),(0,3836,0),(0,3859,0),(0,3772,0),(0,3765,0),(0,3769,0),(0,3757,0),(0,3886,0),(0,3753,0),(0,3743,0),(0,3785,0),(0,3786,0),(0,3750,0),(0,3889,0),(0,3792,0),(0,3732,0),(0,3849,0),(0,3890,0),(0,3888,0),(0,3843,0),(0,3821,0),(0,3748,0),(0,3749,0),(0,3838,0),(0,3810,0),(0,3764,0),(0,3829,0),(0,3855,0),(0,3763,0),(0,3794,0),(0,3862,0),(0,3771,0),(0,3726,0),(0,3822,0),(0,3825,0),(0,3864,0),(0,3773,0),(0,3858,0),(0,3857,0),(0,3873,0),(0,3770,0);
UNLOCK TABLES;
/*!40000 ALTER TABLE `links` ENABLE KEYS */;

--
-- Table structure for table `privileges`
--

DROP TABLE IF EXISTS `privileges`;
CREATE TABLE `privileges` (
  `id` int(7) NOT NULL auto_increment,
  `dashboard_id` int(7) NOT NULL default '0',
  `type` enum('group','role','user') NOT NULL default 'user',
  `target_id` int(7) NOT NULL default '0',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='Dashboard Privileges';

--
-- Dumping data for table `privileges`
--


/*!40000 ALTER TABLE `privileges` DISABLE KEYS */;
LOCK TABLES `privileges` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `privileges` ENABLE KEYS */;

--
-- Table structure for table `qinfo`
--

DROP TABLE IF EXISTS `qinfo`;
CREATE TABLE `qinfo` (
  `q_id` int(11) default NULL,
  `name` varchar(128) default NULL,
  `value` varchar(255) default NULL,
  KEY `qinfo_id` (`q_id`),
  KEY `qinfo_nv` (`name`,`value`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `qinfo`
--


/*!40000 ALTER TABLE `qinfo` DISABLE KEYS */;
LOCK TABLES `qinfo` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `qinfo` ENABLE KEYS */;

--
-- Table structure for table `qtrack`
--

DROP TABLE IF EXISTS `qtrack`;
CREATE TABLE `qtrack` (
  `rec_id` int(11) NOT NULL auto_increment,
  `ip` varchar(16) NOT NULL default '',
  `qwords` text NOT NULL,
  `qtime` int(11) NOT NULL default '0',
  `found` int(11) NOT NULL default '0',
  PRIMARY KEY  (`rec_id`),
  KEY `qtrack_ipt` (`ip`,`qtime`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `qtrack`
--


/*!40000 ALTER TABLE `qtrack` DISABLE KEYS */;
LOCK TABLES `qtrack` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `qtrack` ENABLE KEYS */;

--
-- Table structure for table `server`
--

DROP TABLE IF EXISTS `server`;
CREATE TABLE `server` (
  `rec_id` int(11) NOT NULL default '0',
  `enabled` int(11) NOT NULL default '0',
  `url` blob NOT NULL,
  `tag` text NOT NULL,
  `category` int(11) NOT NULL default '0',
  `command` char(1) NOT NULL default 'S',
  `ordre` int(11) NOT NULL default '0',
  `parent` int(11) NOT NULL default '0',
  `weight` float NOT NULL default '1',
  `pop_weight` float NOT NULL default '0',
  PRIMARY KEY  (`rec_id`),
  UNIQUE KEY `srv_url` (`url`(255)),
  KEY `srv_ordre` (`ordre`),
  KEY `srv_parent` (`parent`),
  KEY `srv_command` (`command`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `server`
--


/*!40000 ALTER TABLE `server` DISABLE KEYS */;
LOCK TABLES `server` WRITE;
INSERT INTO `server` VALUES (-1009981404,1,'*.b','',0,'F',2,0,0,0),(-1755608737,1,'*.sh','',0,'F',3,0,0,0),(-72068437,1,'*.md5','',0,'F',4,0,0,0),(-2001060016,1,'*.rpm','',0,'F',5,0,0,0),(-1624862788,1,'*.arj','',0,'F',7,0,0,0),(-342154600,1,'*.tar','',0,'F',8,0,0,0),(-390560371,1,'*.zip','',0,'F',9,0,0,0),(1592841473,1,'*.tgz','',0,'F',10,0,0,0),(584786851,1,'*.gz','',0,'F',11,0,0,0),(-1235997132,1,'*.z','',0,'F',12,0,0,0),(1845106943,1,'*.bz2','',0,'F',13,0,0,0),(-1556947301,1,'*.lha','',0,'F',15,0,0,0),(-327508057,1,'*.lzh','',0,'F',16,0,0,0),(-346550046,1,'*.rar','',0,'F',17,0,0,0),(1277563317,1,'*.zoo','',0,'F',18,0,0,0),(-1490227352,1,'*.ha','',0,'F',19,0,0,0),(1271584016,1,'*.tar.Z','',0,'F',20,0,0,0),(2138651258,1,'*.gif','',0,'F',22,0,0,0),(114069986,1,'*.jpg','',0,'F',23,0,0,0),(785482458,1,'*.jpeg','',0,'F',24,0,0,0),(-1109799009,1,'*.bmp','',0,'F',25,0,0,0),(-1705563717,1,'*.tiff','',0,'F',26,0,0,0),(-678677854,1,'*.tif','',0,'F',27,0,0,0),(1428230503,1,'*.xpm','',0,'F',28,0,0,0),(-204063145,1,'*.xbm','',0,'F',29,0,0,0),(275641168,1,'*.pcx','',0,'F',30,0,0,0),(2054448292,1,'*.vdo','',0,'F',32,0,0,0),(774245887,1,'*.mpeg','',0,'F',33,0,0,0),(-1056758496,1,'*.mpe','',0,'F',34,0,0,0),(-605417413,1,'*.mpg','',0,'F',35,0,0,0),(1248070425,1,'*.avi','',0,'F',36,0,0,0),(-1010324178,1,'*.movie','',0,'F',37,0,0,0),(-59514947,1,'*.mov','',0,'F',38,0,0,0),(-2081326282,1,'*.wmv','',0,'F',39,0,0,0),(-1139378359,1,'*.mid','',0,'F',41,0,0,0),(868233274,1,'*.mp3','',0,'F',42,0,0,0),(1182357873,1,'*.rm','',0,'F',43,0,0,0),(-1892930561,1,'*.ram','',0,'F',44,0,0,0),(-388895979,1,'*.wav','',0,'F',45,0,0,0),(1420788177,1,'*.aiff','',0,'F',46,0,0,0),(2046423764,1,'*.ra','',0,'F',47,0,0,0),(1178932541,1,'*.vrml','',0,'F',49,0,0,0),(614406935,1,'*.wrl','',0,'F',50,0,0,0),(557889343,1,'*.png','',0,'F',51,0,0,0),(-1773849172,1,'*.ico','',0,'F',52,0,0,0),(482746213,1,'*.psd','',0,'F',53,0,0,0),(197990351,1,'*.dat','',0,'F',54,0,0,0),(1137348529,1,'*.exe','',0,'F',56,0,0,0),(-1258315430,1,'*.com','',0,'F',57,0,0,0),(-784521992,1,'*.cab','',0,'F',58,0,0,0),(-1714321870,1,'*.dll','',0,'F',59,0,0,0),(-252565531,1,'*.bin','',0,'F',60,0,0,0),(912494834,1,'*.class','',0,'F',61,0,0,0),(464999411,1,'*.ex_','',0,'F',62,0,0,0),(-1159974666,1,'*.tex','',0,'F',64,0,0,0),(384533294,1,'*.texi','',0,'F',65,0,0,0),(-995775740,1,'*.xls','',0,'F',66,0,0,0),(-1519435788,1,'*.doc','',0,'F',67,0,0,0),(1306691934,1,'*.texinfo','',0,'F',68,0,0,0),(-199371280,1,'*.rtf','',0,'F',70,0,0,0),(30013236,1,'*.pdf','',0,'F',71,0,0,0),(648492231,1,'*.cdf','',0,'F',71,0,0,0),(1049847163,1,'*.ps','',0,'F',72,0,0,0),(752243032,1,'*.ai','',0,'F',74,0,0,0),(-1416287592,1,'*.eps','',0,'F',75,0,0,0),(2007963022,1,'*.ppt','',0,'F',76,0,0,0),(-861680858,1,'*.hqx','',0,'F',77,0,0,0),(-1937387122,1,'*.cpt','',0,'F',79,0,0,0),(-1376835098,1,'*.bms','',0,'F',80,0,0,0),(-516766697,1,'*.oda','',0,'F',81,0,0,0),(-1510773345,1,'*.tcl','',0,'F',82,0,0,0),(-310937748,1,'*.o','',0,'F',84,0,0,0),(-303415946,1,'*.a','',0,'F',85,0,0,0),(-944636503,1,'*.la','',0,'F',86,0,0,0),(969154153,1,'*.so','',0,'F',87,0,0,0),(-1674775020,1,'*.pat','',0,'F',89,0,0,0),(497306035,1,'*.pm','',0,'F',90,0,0,0),(-1636145178,1,'*.m4','',0,'F',91,0,0,0),(-1487048733,1,'*.am','',0,'F',92,0,0,0),(-1744002416,1,'*.css','',0,'F',93,0,0,0),(-1426189027,1,'*.map','',0,'F',95,0,0,0),(1653070540,1,'*.aif','',0,'F',96,0,0,0),(-1123784825,1,'*.sit','',0,'F',97,0,0,0),(1878198905,1,'*.sea','',0,'F',98,0,0,0),(-45025424,1,'*.m3u','',0,'F',100,0,0,0),(936478137,1,'*.qt','',0,'F',101,0,0,0),(-39226065,1,'*D=A','',0,'F',103,0,0,0),(-1891421144,1,'*D=D','',0,'F',104,0,0,0),(1177200378,1,'*M=A','',0,'F',105,0,0,0),(-224724630,1,'*M=D','',0,'F',106,0,0,0),(-877971084,1,'*N=A','',0,'F',107,0,0,0),(309142739,1,'*N=D','',0,'F',108,0,0,0),(689968446,1,'*S=A','',0,'F',109,0,0,0),(-1287468087,1,'*S=D','',0,'F',110,0,0,0),(-1000453941,1,'\\.r[0-9][0-9]$','',0,'F',112,0,0,0),(836863171,1,'\\.a[0-9][0-9]$','',0,'F',113,0,0,0),(1961936161,1,'\\.so\\.[0-9]$','',0,'F',114,0,0,0),(151966379,1,'http://localhost/monitor/bookshelf/docs/Groundwork_Reference/Console/','',0,'S',0,0,1,0),(-766859750,1,'http://localhost/','',0,'S',0,151966379,1,0),(-1204776029,1,'http://localhost/monitor/packages/bookshelf/docs/Groundwork_Reference/Console/','',0,'S',0,0,1,0),(-920121209,1,'http://localhost/monitor/packages/bookshelf/docs/Groundwork_Reference/Groundwork_Assist_How_To/','',0,'S',0,0,1,0),(-1375342091,1,'/usr/local/groundwork/mango/packages/bookshelf/docs/Groundwork_Reference/Console/','',0,'S',0,0,1,0),(478501149,1,'/usr/local/groundwork/mango/packages/bookshelf/docs/Groundwork_Reference/Groudwork_Assist_How_To/','',0,'S',0,0,1,0),(-875888342,1,'/usr/local/groundwork/mango/packages/bookshelf/docs/Groundwork_Reference/Groundwork_Assist_How_To/','',0,'S',0,0,1,0),(-1994060152,1,':///','',0,'S',0,478501149,1,0),(909260075,1,'file:///usr/local/groundwork/mango/packages/bookshelf/docs/Groundwork_Reference/Console/','',0,'S',0,0,1,0),(-1333164941,1,'file:///usr/local/groundwork/mango/packages/bookshelf/docs/Groundwork_Reference/Groudwork_Assist_How_To/','',0,'S',0,0,1,0),(-1547339094,1,'file:///usr/local/groundwork/mango/packages/bookshelf/docs/Groundwork_Reference/Groundwork_Assist_How_To/','',0,'S',0,0,1,0),(-1519382294,1,'file:///','',0,'S',0,-1333164941,1,0),(1173607539,1,'http://192.168.2.220/monitor/packages/bookshelf/docs/Groundwork_Reference/Console/','',0,'S',0,0,1,0),(1425818384,1,'http://192.168.2.220/monitor/packages/bookshelf/docs/Groundwork_Reference/Groudwork_Assist_How_To/','',0,'S',0,0,1,0),(458372969,1,'http://192.168.2.220/monitor/packages/bookshelf/docs/Groundwork_Reference/Groundwork_Assist_How_To/','',0,'S',0,0,1,0),(1463151577,1,'http://192.168.2.220/','',0,'S',0,458372969,1,0),(1884141941,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitor/GroundWork_Reference/Administrator/','',0,'S',0,0,1,0),(-976027547,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitor/GroundWork_Reference/Developer/','',0,'S',0,0,1,0),(519569210,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitor/GroundWork_Reference/Operator/','',0,'S',0,0,1,0),(-1783393467,1,'http://127.0.0.1/','',0,'S',0,1884141941,1,0),(-2127342638,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorprofessional/GroundWork_Reference/Administrator/GroundWork_Monitor_Profiles/Profile_Definitions/','',0,'S',0,0,1,0),(1263981379,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorprofessional/GroundWork_Reference/Administrator/','',0,'S',0,0,1,0),(1371388389,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorprofessional/GroundWork_Reference/Developer/','',0,'S',0,0,1,0),(-1475959109,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorprofessional/GroundWork_Reference/Operator/','',0,'S',0,0,1,0),(974011152,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorprofessional/Open_Source_Reference/MySQL/','',0,'S',0,0,1,0),(-1970644423,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorprofessional/Open_Source_Reference/Nagios/','',0,'S',0,0,1,0),(1185210320,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorprofessional/Open_Source_Reference/Perl/','',0,'S',0,0,1,0),(-1156299735,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorprofessional/Open_Source_Reference/PHP/','',0,'S',0,0,1,0),(1919980703,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorprofessional/Open_Source_Reference/RRDtool/','',0,'S',0,0,1,0),(695889792,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorprofessional/Open_Source_Reference/WMI/','',0,'S',0,0,1,0),(423986946,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitor/GroundWork_Reference/Administrator/','',0,'S',0,0,1,0),(-848635036,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitor/GroundWork_Reference/Administrator/GroundWork_Monitor_Profiles/','',0,'S',0,0,1,0),(-1027837456,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitor/GroundWork_Reference/Administrator/GroundWork_Monitor_Profiles/Profile_Definitions/','',0,'S',0,0,1,0),(547118035,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitor/GroundWork_Reference/Developer/','',0,'S',0,0,1,0),(-1349095151,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitor/GroundWork_Reference/Operator/','',0,'S',0,0,1,0),(-737295299,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitor/Open_Source_Reference/Nagios/Nagios_Plugins/','',0,'S',0,0,1,0),(-837075513,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitor/Open_Source_Reference/MySQL/','',0,'S',0,0,1,0),(-641400926,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitor/Open_Source_Reference/Nagios/','',0,'S',0,0,1,0),(-782363088,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitor/Open_Source_Reference/Perl/','',0,'S',0,0,1,0),(1769101917,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitor/Open_Source_Reference/PHP/','',0,'S',0,0,1,0),(362612081,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitor/Open_Source_Reference/RRDtool/','',0,'S',0,0,1,0),(-763241724,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitor/Open_Source_Reference/WMI/','',0,'S',0,0,1,0),(-961988269,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitorenterprise/GroundWork_Reference/Administrator/','',0,'S',0,0,1,0),(1237619594,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitorenterprise/GroundWork_Reference/Developer/','',0,'S',0,0,1,0),(1423625661,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitorenterprise/GroundWork_Reference/Operator/','',0,'S',0,0,1,0),(-171097504,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitorenterprise/Open_Source_Reference/MySQL/','',0,'S',0,0,1,0),(1406792259,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitorenterprise/Open_Source_Reference/Nagios/','',0,'S',0,0,1,0),(-1337349758,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitorenterprise/Open_Source_Reference/Perl/','',0,'S',0,0,1,0),(333223404,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitorenterprise/Open_Source_Reference/PHP/','',0,'S',0,0,1,0),(-280849816,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitorenterprise/Open_Source_Reference/RRDtool/','',0,'S',0,0,1,0),(1486465872,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitorenterprise/Open_Source_Reference/WMI/','',0,'S',0,0,1,0),(-850050420,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitorprofessional/GroundWork_Reference/Administrator/','',0,'S',0,0,1,0),(1389518315,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitorprofessional/GroundWork_Reference/Administrator/GroundWork_Monitor_Profiles/','',0,'S',0,0,1,0),(-56417367,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitorprofessional/GroundWork_Reference/Administrator/GroundWork_Monitor_Profiles/Profile_Definitions/','',0,'S',0,0,1,0),(-1734043788,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitorprofessional/GroundWork_Reference/Developer/','',0,'S',0,0,1,0),(-2028032039,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitorprofessional/GroundWork_Reference/Operator/','',0,'S',0,0,1,0),(419974889,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitorprofessional/Open_Source_Reference/MySQL/','',0,'S',0,0,1,0),(-2115257443,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitorprofessional/Open_Source_Reference/Nagios/','',0,'S',0,0,1,0),(-1048757676,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitorprofessional/Open_Source_Reference/Nagios/Nagios_Plugins/','',0,'S',0,0,1,0),(-1028678795,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitorprofessional/Open_Source_Reference/Perl/','',0,'S',0,0,1,0),(-354222877,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitorprofessional/Open_Source_Reference/PHP/','',0,'S',0,0,1,0),(-446889658,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitorprofessional/Open_Source_Reference/RRDtool/','',0,'S',0,0,1,0),(1612748395,1,'http://127.0.0.1/guava/packages/bookshelf/docs/groundworkmonitorprofessional/Open_Source_Reference/WMI/','',0,'S',0,0,1,0),(-1460022819,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/','',0,'S',0,0,1,0),(6754301,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/GroundWork_Monitor_Profiles/','',0,'S',0,0,1,0),(1489135615,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/GroundWork_Monitor_Profiles/Profile_Definitions/','',0,'S',0,0,1,0),(-405105146,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Developer/','',0,'S',0,0,1,0),(-346938837,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Operator/','',0,'S',0,0,1,0),(328013356,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/','',0,'S',0,0,1,0),(1247300804,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/MySQL/','',0,'S',0,0,1,0),(1478705573,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/','',0,'S',0,0,1,0),(368963103,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Perl/','',0,'S',0,0,1,0),(-265238827,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/PHP/','',0,'S',0,0,1,0),(-292609038,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/RRDtool/','',0,'S',0,0,1,0),(1431329223,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/WMI/','',0,'S',0,0,1,0),(1591136704,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/GroundWork_Profiles/','',0,'S',0,0,1,0),(-940110355,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/GroundWork_Profiles/Profile_Definitions/','',0,'S',0,0,1,0),(-693710674,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorsmallbusiness/Open_Source_Reference/Nagios/Nagios_Plugins/','',0,'S',0,0,1,0),(-1090194015,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorsmallbusiness/Open_Source_Reference/Nagios/','',0,'S',0,0,1,0),(1603991451,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorsmallbusiness/Open_Source_Reference/MySQL/','',0,'S',0,0,1,0),(1438149566,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorsmallbusiness/GroundWork_Reference/Operator/','',0,'S',0,0,1,0),(-758219674,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorsmallbusiness/GroundWork_Reference/Developer/','',0,'S',0,0,1,0),(-1404395419,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorsmallbusiness/GroundWork_Reference/Administrator/GroundWork_Profiles/Profile_Definitions/','',0,'S',0,0,1,0),(-254154474,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorsmallbusiness/GroundWork_Reference/Administrator/GroundWork_Profiles/','',0,'S',0,0,1,0),(-1531341351,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorsmallbusiness/GroundWork_Reference/Administrator/','',0,'S',0,0,1,0),(-472504224,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorsmallbusiness/Open_Source_Reference/Perl/','',0,'S',0,0,1,0),(157397846,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorsmallbusiness/Open_Source_Reference/PHP/','',0,'S',0,0,1,0),(582742939,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorsmallbusiness/Open_Source_Reference/RRDtool/','',0,'S',0,0,1,0),(-1301322202,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorsmallbusiness/Open_Source_Reference/SNMPTT/','',0,'S',0,0,1,0),(-433746697,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorsmallbusiness/Open_Source_Reference/WMI/','',0,'S',0,0,1,0),(116684345,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorprofessional/Open_Source_Reference/Nagios/Nagios_Plugins/','',0,'S',0,0,1,0),(-1336117105,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorprofessional/GroundWork_Reference/Administrator/GroundWork_Profiles/Profile_Definitions/','',0,'S',0,0,1,0),(-915964481,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorprofessional/GroundWork_Reference/Administrator/GroundWork_Profiles/','',0,'S',0,0,1,0),(2071952128,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorprofessional/Open_Source_Reference/SNMPTT/','',0,'S',0,0,1,0),(1223578368,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/sendPage/','',0,'S',0,0,1,0),(760126362,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorsmallbusiness/Open_Source_Reference/sendPage/','',0,'S',0,0,1,0),(-524948532,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorprofessional/Open_Source_Reference/sendPage/','',0,'S',0,0,1,0),(1385432781,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/dojo/','',0,'S',0,0,1,0),(1470004335,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorsmallbusiness/Open_Source_Reference/dojo/','',0,'S',0,0,1,0),(458910558,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorprofessional/Open_Source_Reference/dojo/','',0,'S',0,0,1,0),(-1460296193,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitorprofessional/Open_Source_Reference/SYSLOGNG/','',0,'S',0,0,1,0);
UNLOCK TABLES;
/*!40000 ALTER TABLE `server` ENABLE KEYS */;

--
-- Table structure for table `servertable`
--

DROP TABLE IF EXISTS `servertable`;
CREATE TABLE `servertable` (
  `rec_id` int(11) NOT NULL auto_increment,
  `enabled` int(11) NOT NULL default '1',
  `url` blob NOT NULL,
  `tag` text NOT NULL,
  `category` int(11) NOT NULL default '0',
  `command` char(1) NOT NULL default 'S',
  `ordre` int(11) NOT NULL default '0',
  `parent` int(11) NOT NULL default '0',
  `weight` float NOT NULL default '1',
  `pop_weight` float NOT NULL default '0',
  `library_id` int(11) unsigned NOT NULL default '0',
  PRIMARY KEY  (`rec_id`),
  UNIQUE KEY `srv_url` (`url`(255)),
  KEY `srv_ordre` (`ordre`),
  KEY `srv_parent` (`parent`),
  KEY `srv_command` (`command`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `servertable`
--


/*!40000 ALTER TABLE `servertable` DISABLE KEYS */;
LOCK TABLES `servertable` WRITE;
INSERT INTO `servertable` VALUES (3944,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/dojo/dojo_Overview.html','',0,'S',0,0,1,0,19),(3943,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/MySQL/MySQL_Overview.html','',0,'S',0,0,1,0,19),(3942,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_wins.html','',0,'S',0,0,1,0,19),(3941,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_wave.html','',0,'S',0,0,1,0,19),(3940,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_vcs.html','',0,'S',0,0,1,0,19),(3939,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_users.html','',0,'S',0,0,1,0,19),(3938,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/urlize.html','',0,'S',0,0,1,0,19),(3937,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ups.html','',0,'S',0,0,1,0,19),(3936,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_udp2.html','',0,'S',0,0,1,0,19),(3935,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_udp.html','',0,'S',0,0,1,0,19),(3934,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_traceroute.html','',0,'S',0,0,1,0,19),(3933,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_time.html','',0,'S',0,0,1,0,19),(3932,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_tcp.html','',0,'S',0,0,1,0,19),(3931,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_syslog_gw.html','',0,'S',0,0,1,0,19),(3930,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_sybase.html','',0,'S',0,0,1,0,19),(3929,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_swap_remote.html','',0,'S',0,0,1,0,19),(3928,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_swap.html','',0,'S',0,0,1,0,19),(3927,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ssmtp.html','',0,'S',0,0,1,0,19),(3926,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ssh.html','',0,'S',0,0,1,0,19),(3925,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_spop.html','',0,'S',0,0,1,0,19),(3924,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_snmp_procs.html','',0,'S',0,0,1,0,19),(3923,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_snmp_process_monitor.html','',0,'S',0,0,1,0,19),(3922,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_snmp_printer.html','',0,'S',0,0,1,0,19),(3921,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_snmp_disk_monitor.html','',0,'S',0,0,1,0,19),(3920,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_snmp.html','',0,'S',0,0,1,0,19),(3919,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_smtp.html','',0,'S',0,0,1,0,19),(3918,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_smb.html','',0,'S',0,0,1,0,19),(3917,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_smart.html','',0,'S',0,0,1,0,19),(3916,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_simap.html','',0,'S',0,0,1,0,19),(3915,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_sensors.html','',0,'S',0,0,1,0,19),(3914,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_sap.html','',0,'S',0,0,1,0,19),(3913,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_rrd_hw.html','',0,'S',0,0,1,0,19),(3912,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_rrd_data.html','',0,'S',0,0,1,0,19),(3911,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_rpc.html','',0,'S',0,0,1,0,19),(3910,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_remote_nagios_status.html','',0,'S',0,0,1,0,19),(3909,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_real.html','',0,'S',0,0,1,0,19),(3908,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_radius.html','',0,'S',0,0,1,0,19),(3907,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_procs.html','',0,'S',0,0,1,0,19),(3906,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_procr.html','',0,'S',0,0,1,0,19),(3905,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_procl.html','',0,'S',0,0,1,0,19),(3904,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_pop3.html','',0,'S',0,0,1,0,19),(3903,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_pop.html','',0,'S',0,0,1,0,19),(3902,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ping.html','',0,'S',0,0,1,0,19),(3901,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_pgsql.html','',0,'S',0,0,1,0,19),(3900,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_pfstate.html','',0,'S',0,0,1,0,19),(3899,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_pcpmetric.html','',0,'S',0,0,1,0,19),(3898,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_overcr.html','',0,'S',0,0,1,0,19),(3897,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_oracle_status.html','',0,'S',0,0,1,0,19),(3896,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_oracle_stats.html','',0,'S',0,0,1,0,19),(3895,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_oracle_spcrit.html','',0,'S',0,0,1,0,19),(3894,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_oracle_online.html','',0,'S',0,0,1,0,19),(3893,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_oracle_maxssn.html','',0,'S',0,0,1,0,19),(3892,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_oracle_maxprc.html','',0,'S',0,0,1,0,19),(3891,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_oracle_maxext.html','',0,'S',0,0,1,0,19),(3890,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_oracle_logmode_new.html','',0,'S',0,0,1,0,19),(3889,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_oracle_invobj.html','',0,'S',0,0,1,0,19),(3888,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_oracle_instance.html','',0,'S',0,0,1,0,19),(3887,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_oracle_autoext.html','',0,'S',0,0,1,0,19),(3886,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_oracle.html','',0,'S',0,0,1,0,19),(3885,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ora_table_space.html','',0,'S',0,0,1,0,19),(3884,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_nwstat.html','',0,'S',0,0,1,0,19),(3883,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ntp.html','',0,'S',0,0,1,0,19),(3882,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_nt.html','',0,'S',0,0,1,0,19),(3881,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_nrpe.html','',0,'S',0,0,1,0,19),(3880,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_nntps.html','',0,'S',0,0,1,0,19),(3879,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_nntp.html','',0,'S',0,0,1,0,19),(3878,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_netapp.html','',0,'S',0,0,1,0,19),(3877,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/negate.html','',0,'S',0,0,1,0,19),(3876,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_nagios_status_log.html','',0,'S',0,0,1,0,19),(3875,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_nagios.html','',0,'S',0,0,1,0,19),(3874,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_mysqlslave.html','',0,'S',0,0,1,0,19),(3873,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_mysql_query.html','',0,'S',0,0,1,0,19),(3872,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_mssql_log.html','',0,'S',0,0,1,0,19),(3871,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_mysql.html','',0,'S',0,0,1,0,19),(3870,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_mssql2000.html','',0,'S',0,0,1,0,19),(3869,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_mssql2.html','',0,'S',0,0,1,0,19),(3868,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_mssql.html','',0,'S',0,0,1,0,19),(3867,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ms_spooler.html','',0,'S',0,0,1,0,19),(3866,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_mrtgtraf.html','',0,'S',0,0,1,0,19),(3865,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_mrtg.html','',0,'S',0,0,1,0,19),(3864,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_mem.html','',0,'S',0,0,1,0,19),(3863,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_maxwanstate.html','',0,'S',0,0,1,0,19),(3862,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_maxchannels.html','',0,'S',0,0,1,0,19),(3861,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_mailq.html','',0,'S',0,0,1,0,19),(3860,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_lotus.html','',0,'S',0,0,1,0,19),(3859,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_logs.html','',0,'S',0,0,1,0,19),(3858,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_log2.html','',0,'S',0,0,1,0,19),(3857,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_log.html','',0,'S',0,0,1,0,19),(3856,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_load_remote.html','',0,'S',0,0,1,0,19),(3855,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_load.html','',0,'S',0,0,1,0,19),(3854,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_lmmon.html','',0,'S',0,0,1,0,19),(3853,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ldap.html','',0,'S',0,0,1,0,19),(3852,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_joy.html','',0,'S',0,0,1,0,19),(3851,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_javaproc.html','',0,'S',0,0,1,0,19),(3850,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_jabber.html','',0,'S',0,0,1,0,19),(3849,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ircd.html','',0,'S',0,0,1,0,19),(3848,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_inodes-freebsd.html','',0,'S',0,0,1,0,19),(3847,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_inodes.html','',0,'S',0,0,1,0,19),(3846,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_imap.html','',0,'S',0,0,1,0,19),(3845,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ifstatus.html','',0,'S',0,0,1,0,19),(3844,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ifoperstatus.html','',0,'S',0,0,1,0,19),(3843,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_if.html','',0,'S',0,0,1,0,19),(3842,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_icmp.html','',0,'S',0,0,1,0,19),(3841,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ica_program_neighbourhood.html','',0,'S',0,0,1,0,19),(3840,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ica_metaframe_pub_apps.html','',0,'S',0,0,1,0,19),(3839,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ica_master_browser.html','',0,'S',0,0,1,0,19),(3838,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_hw.html','',0,'S',0,0,1,0,19),(3837,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_http.html','',0,'S',0,0,1,0,19),(3836,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_hprsc.html','',0,'S',0,0,1,0,19),(3835,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_hpjd.html','',0,'S',0,0,1,0,19),(3834,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_host_foundation.html','',0,'S',0,0,1,0,19),(3833,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_game.html','',0,'S',0,0,1,0,19),(3832,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ftpget.html','',0,'S',0,0,1,0,19),(3831,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ftp.html','',0,'S',0,0,1,0,19),(3830,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_fping.html','',0,'S',0,0,1,0,19),(3829,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_flexlm.html','',0,'S',0,0,1,0,19),(3828,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_file_age.html','',0,'S',0,0,1,0,19),(3827,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_email_loop.html','',0,'S',0,0,1,0,19),(3826,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_dummy.html','',0,'S',0,0,1,0,19),(3825,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_dns_random.html','',0,'S',0,0,1,0,19),(3824,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_dns.html','',0,'S',0,0,1,0,19),(3823,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_dlswcircuit.html','',0,'S',0,0,1,0,19),(3822,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_dl_size.html','',0,'S',0,0,1,0,19),(3821,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_disk_snmp.html','',0,'S',0,0,1,0,19),(3820,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_disk_smb.html','',0,'S',0,0,1,0,19),(3819,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_disk_remote.html','',0,'S',0,0,1,0,19),(3818,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_disk.html','',0,'S',0,0,1,0,19),(3817,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_digitemp.html','',0,'S',0,0,1,0,19),(3816,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_dig.html','',0,'S',0,0,1,0,19),(3815,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_dhcp.html','',0,'S',0,0,1,0,19),(3814,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_dell_hw.html','',0,'S',0,0,1,0,19),(3813,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_cpu.html','',0,'S',0,0,1,0,19),(3812,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_connections.html','',0,'S',0,0,1,0,19),(3811,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_compaq_insight.html','',0,'S',0,0,1,0,19),(3810,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_clamd.html','',0,'S',0,0,1,0,19),(3809,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ciscotemp.html','',0,'S',0,0,1,0,19),(3808,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_by_ssh.html','',0,'S',0,0,1,0,19),(3807,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_breeze.html','',0,'S',0,0,1,0,19),(3806,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_bgpstate.html','',0,'S',0,0,1,0,19),(3805,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_bandwidth.html','',0,'S',0,0,1,0,19),(3804,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_backup.html','',0,'S',0,0,1,0,19),(3803,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_axis.html','',0,'S',0,0,1,0,19),(3802,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_arping.html','',0,'S',0,0,1,0,19),(3801,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_apt.html','',0,'S',0,0,1,0,19),(3800,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_appletalk.html','',0,'S',0,0,1,0,19),(3799,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_apc_ups.html','',0,'S',0,0,1,0,19),(3798,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_apache.html','',0,'S',0,0,1,0,19),(3797,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_adptraid.html','',0,'S',0,0,1,0,19),(3796,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Overview.html','',0,'S',0,0,1,0,19),(3795,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Perl/Perl_Overview.html','',0,'S',0,0,1,0,19),(3794,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/PHP/PHP_Overview.html','',0,'S',0,0,1,0,19),(3793,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/RRDtool/RRDtool_Overview.html','',0,'S',0,0,1,0,19),(3792,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/sendPage/sendPage_Overview.html','',0,'S',0,0,1,0,19),(3791,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/WMI/WMI_Overview.html','',0,'S',0,0,1,0,19),(3790,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/GroundWork_Profiles/Profile_Definitions/gwsp2-ssh_UNIX.html','',0,'S',0,0,1,0,19),(3789,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/GroundWork_Profiles/Profile_Definitions/gwsp2-snmp_network.html','',0,'S',0,0,1,0,19),(3788,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/GroundWork_Profiles/Profile_Definitions/gwsp2-service_ping.html','',0,'S',0,0,1,0,19),(3787,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/GroundWork_Profiles/SSH_Monitoring.html','',0,'S',0,0,1,0,19),(3786,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/GroundWork_Profiles/Profiles_Overview.html','',0,'S',0,0,1,0,19),(3785,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/Administrator_Guide.html','',0,'S',0,0,1,0,19),(3784,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Operator/Operator_Guide.html','',0,'S',0,0,1,0,19),(3783,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Developer/Developer_Guide.html','',0,'S',0,0,1,0,19),(3782,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/tools.html','',0,'S',0,0,1,0,19),(3781,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/timeperiods.html','',0,'S',0,0,1,0,19),(3780,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/services.html','',0,'S',0,0,1,0,19),(3779,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/profiles.html','',0,'S',0,0,1,0,19),(3778,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/managinghosts.html','',0,'S',0,0,1,0,19),(3777,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/hosts.html','',0,'S',0,0,1,0,19),(3776,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/groups.html','',0,'S',0,0,1,0,19),(3775,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/escalations.html','',0,'S',0,0,1,0,19),(3774,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/control.html','',0,'S',0,0,1,0,19),(3773,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/contacts.html','',0,'S',0,0,1,0,19),(3772,1,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/commands.html','',0,'S',0,0,1,0,19);
UNLOCK TABLES;
/*!40000 ALTER TABLE `servertable` ENABLE KEYS */;

--
-- Table structure for table `srvinfo`
--

DROP TABLE IF EXISTS `srvinfo`;
CREATE TABLE `srvinfo` (
  `srv_id` int(11) NOT NULL default '0',
  `sname` text NOT NULL,
  `sval` text NOT NULL,
  KEY `srvinfo_id` (`srv_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `srvinfo`
--


/*!40000 ALTER TABLE `srvinfo` DISABLE KEYS */;
LOCK TABLES `srvinfo` WRITE;
INSERT INTO `srvinfo` VALUES (-1009981404,'nomatch','0'),(-1755608737,'nomatch','0'),(-2001060016,'nomatch','0'),(1371388389,'nomatch','0'),(1263981379,'nomatch','0'),(1592841473,'nomatch','0'),(1592841473,'match_type','5'),(1592841473,'case_sense','1'),(-1235997132,'nomatch','0'),(-1235997132,'match_type','5'),(-1235997132,'case_sense','1'),(-1556947301,'nomatch','0'),(-1556947301,'match_type','5'),(-1556947301,'case_sense','1'),(-346550046,'nomatch','0'),(-346550046,'match_type','5'),(-346550046,'case_sense','1'),(458372969,'nomatch','0'),(458372969,'match_type','1'),(-1490227352,'nomatch','0'),(-1490227352,'match_type','5'),(-1490227352,'case_sense','1'),(1425818384,'nomatch','0'),(1425818384,'match_type','1'),(1425818384,'case_sense','1'),(458372969,'case_sense','1'),(1173607539,'nomatch','0'),(1173607539,'match_type','1'),(2138651258,'nomatch','0'),(2138651258,'match_type','5'),(2138651258,'case_sense','1'),(1173607539,'case_sense','1'),(785482458,'nomatch','0'),(785482458,'match_type','5'),(785482458,'case_sense','1'),(-1705563717,'nomatch','0'),(-1705563717,'match_type','5'),(-1705563717,'case_sense','1'),(1428230503,'nomatch','0'),(1428230503,'match_type','5'),(1428230503,'case_sense','1'),(-1547339094,'nomatch','0'),(-1547339094,'match_type','1'),(-1547339094,'case_sense','1'),(909260075,'nomatch','0'),(275641168,'nomatch','0'),(275641168,'match_type','5'),(275641168,'case_sense','1'),(-875888342,'nomatch','0'),(-875888342,'match_type','1'),(-875888342,'case_sense','1'),(478501149,'nomatch','0'),(774245887,'nomatch','0'),(774245887,'match_type','5'),(774245887,'case_sense','1'),(-1375342091,'nomatch','0'),(478501149,'match_type','1'),(478501149,'case_sense','1'),(-605417413,'nomatch','0'),(-605417413,'match_type','5'),(-605417413,'case_sense','1'),(-1010324178,'nomatch','0'),(-1010324178,'match_type','5'),(-1010324178,'case_sense','1'),(-2081326282,'nomatch','0'),(-2081326282,'match_type','5'),(-2081326282,'case_sense','1'),(868233274,'nomatch','0'),(868233274,'match_type','5'),(868233274,'case_sense','1'),(-1892930561,'nomatch','0'),(-1892930561,'match_type','5'),(-1892930561,'case_sense','1'),(1420788177,'nomatch','0'),(1420788177,'match_type','5'),(1420788177,'case_sense','1'),(1178932541,'nomatch','0'),(1178932541,'match_type','5'),(1178932541,'case_sense','1'),(557889343,'nomatch','0'),(557889343,'match_type','5'),(557889343,'case_sense','1'),(482746213,'nomatch','0'),(482746213,'match_type','5'),(482746213,'case_sense','1'),(1137348529,'nomatch','0'),(1137348529,'match_type','5'),(1137348529,'case_sense','1'),(-784521992,'nomatch','0'),(-784521992,'match_type','5'),(-784521992,'case_sense','1'),(912494834,'nomatch','0'),(464999411,'nomatch','0'),(464999411,'match_type','5'),(-1159974666,'nomatch','0'),(-1159974666,'match_type','5'),(384533294,'nomatch','0'),(384533294,'match_type','5'),(-995775740,'nomatch','0'),(-995775740,'match_type','5'),(-1519435788,'nomatch','0'),(-1519435788,'match_type','5'),(1306691934,'nomatch','0'),(1306691934,'match_type','5'),(-199371280,'nomatch','0'),(-199371280,'match_type','5'),(30013236,'match_type','5'),(648492231,'nomatch','0'),(648492231,'match_type','5'),(1049847163,'nomatch','0'),(1049847163,'match_type','5'),(752243032,'nomatch','0'),(752243032,'match_type','5'),(-1416287592,'nomatch','0'),(-1416287592,'match_type','5'),(2007963022,'nomatch','0'),(2007963022,'match_type','5'),(-861680858,'nomatch','0'),(-861680858,'match_type','5'),(-1937387122,'nomatch','0'),(-1937387122,'match_type','5'),(-1376835098,'nomatch','0'),(-1376835098,'match_type','5'),(-516766697,'nomatch','0'),(-516766697,'match_type','5'),(-1510773345,'nomatch','0'),(-1510773345,'match_type','5'),(-310937748,'nomatch','0'),(-310937748,'match_type','5'),(-303415946,'nomatch','0'),(-303415946,'match_type','5'),(-944636503,'nomatch','0'),(-944636503,'match_type','5'),(969154153,'nomatch','0'),(969154153,'match_type','5'),(-1674775020,'nomatch','0'),(-1674775020,'match_type','5'),(497306035,'nomatch','0'),(497306035,'match_type','5'),(-1636145178,'nomatch','0'),(-1636145178,'match_type','5'),(-1487048733,'nomatch','0'),(-1487048733,'match_type','5'),(-1744002416,'nomatch','0'),(-1744002416,'match_type','5'),(-1426189027,'nomatch','0'),(-1426189027,'match_type','5'),(1653070540,'nomatch','0'),(1653070540,'match_type','5'),(-1123784825,'nomatch','0'),(-1123784825,'match_type','5'),(1878198905,'nomatch','0'),(1878198905,'match_type','5'),(-45025424,'nomatch','0'),(-45025424,'match_type','5'),(936478137,'nomatch','0'),(936478137,'match_type','5'),(-39226065,'nomatch','0'),(-39226065,'match_type','5'),(-1891421144,'nomatch','0'),(-1891421144,'match_type','5'),(1177200378,'nomatch','0'),(1177200378,'match_type','5'),(-224724630,'nomatch','0'),(-224724630,'match_type','5'),(-877971084,'nomatch','0'),(-877971084,'match_type','5'),(309142739,'nomatch','0'),(309142739,'match_type','5'),(689968446,'nomatch','0'),(689968446,'match_type','5'),(-1287468087,'nomatch','0'),(-1287468087,'match_type','5'),(-1000453941,'nomatch','0'),(-1000453941,'match_type','4'),(836863171,'nomatch','0'),(836863171,'match_type','4'),(1961936161,'nomatch','0'),(1961936161,'match_type','4'),(1961936161,'case_sense','1'),(151966379,'nomatch','0'),(-1009981404,'match_type','5'),(-1755608737,'match_type','5'),(-72068437,'nomatch','0'),(-1624862788,'nomatch','0'),(-390560371,'nomatch','0'),(-390560371,'match_type','5'),(-390560371,'case_sense','1'),(1845106943,'nomatch','0'),(1845106943,'match_type','5'),(1845106943,'case_sense','1'),(1277563317,'nomatch','0'),(1277563317,'match_type','5'),(1277563317,'case_sense','1'),(114069986,'nomatch','0'),(114069986,'match_type','5'),(114069986,'case_sense','1'),(-678677854,'nomatch','0'),(-678677854,'match_type','5'),(-678677854,'case_sense','1'),(2054448292,'nomatch','0'),(2054448292,'match_type','5'),(2054448292,'case_sense','1'),(1248070425,'nomatch','0'),(1248070425,'match_type','5'),(1248070425,'case_sense','1'),(-59514947,'nomatch','0'),(-59514947,'match_type','5'),(-59514947,'case_sense','1'),(-1139378359,'nomatch','0'),(-1139378359,'match_type','5'),(-1139378359,'case_sense','1'),(1182357873,'nomatch','0'),(1182357873,'match_type','5'),(1182357873,'case_sense','1'),(-388895979,'nomatch','0'),(-388895979,'match_type','5'),(-388895979,'case_sense','1'),(2046423764,'nomatch','0'),(2046423764,'match_type','5'),(2046423764,'case_sense','1'),(-252565531,'nomatch','0'),(-252565531,'match_type','5'),(-1009981404,'case_sense','1'),(-1755608737,'case_sense','1'),(-1624862788,'match_type','5'),(-1624862788,'case_sense','1'),(584786851,'nomatch','0'),(584786851,'match_type','5'),(584786851,'case_sense','1'),(-327508057,'nomatch','0'),(-327508057,'match_type','5'),(-327508057,'case_sense','1'),(1271584016,'nomatch','0'),(1271584016,'match_type','5'),(1271584016,'case_sense','1'),(-1109799009,'nomatch','0'),(-1109799009,'match_type','5'),(-1109799009,'case_sense','1'),(-1333164941,'nomatch','0'),(-1333164941,'match_type','1'),(-204063145,'nomatch','0'),(-204063145,'match_type','5'),(-204063145,'case_sense','1'),(909260075,'match_type','1'),(909260075,'case_sense','1'),(-1333164941,'case_sense','1'),(-1375342091,'match_type','1'),(-1375342091,'case_sense','1'),(-1056758496,'nomatch','0'),(-1056758496,'match_type','5'),(-1056758496,'case_sense','1'),(614406935,'nomatch','0'),(614406935,'match_type','5'),(614406935,'case_sense','1'),(-1773849172,'nomatch','0'),(-1773849172,'match_type','5'),(-1773849172,'case_sense','1'),(197990351,'nomatch','0'),(197990351,'match_type','5'),(197990351,'case_sense','1'),(-1258315430,'nomatch','0'),(-1258315430,'match_type','5'),(-1258315430,'case_sense','1'),(-1714321870,'nomatch','0'),(-1714321870,'match_type','5'),(-1714321870,'case_sense','1'),(-1714321870,'Arg','Disallow'),(-1204776029,'nomatch','0'),(912494834,'match_type','5'),(30013236,'nomatch','0'),(-920121209,'nomatch','0'),(-920121209,'match_type','1'),(-920121209,'case_sense','1'),(1961936161,'Arg','Disallow'),(-1009981404,'Arg','Disallow'),(-1755608737,'Arg','Disallow'),(-72068437,'match_type','5'),(-2001060016,'match_type','5'),(-1624862788,'Arg','Disallow'),(-342154600,'nomatch','0'),(-390560371,'Arg','Disallow'),(1592841473,'Arg','Disallow'),(584786851,'Arg','Disallow'),(-1235997132,'Arg','Disallow'),(1845106943,'Arg','Disallow'),(-1556947301,'Arg','Disallow'),(-327508057,'Arg','Disallow'),(-346550046,'Arg','Disallow'),(1277563317,'Arg','Disallow'),(-1490227352,'Arg','Disallow'),(1271584016,'Arg','Disallow'),(2138651258,'Arg','Disallow'),(114069986,'Arg','Disallow'),(785482458,'Arg','Disallow'),(-1109799009,'Arg','Disallow'),(-1705563717,'Arg','Disallow'),(-678677854,'Arg','Disallow'),(1428230503,'Arg','Disallow'),(-204063145,'Arg','Disallow'),(275641168,'Arg','Disallow'),(2054448292,'Arg','Disallow'),(774245887,'Arg','Disallow'),(-1056758496,'Arg','Disallow'),(-605417413,'Arg','Disallow'),(1248070425,'Arg','Disallow'),(-1010324178,'Arg','Disallow'),(-59514947,'Arg','Disallow'),(-2081326282,'Arg','Disallow'),(-1139378359,'Arg','Disallow'),(868233274,'Arg','Disallow'),(1182357873,'Arg','Disallow'),(-1892930561,'Arg','Disallow'),(-388895979,'Arg','Disallow'),(1420788177,'Arg','Disallow'),(2046423764,'Arg','Disallow'),(1178932541,'Arg','Disallow'),(614406935,'Arg','Disallow'),(557889343,'Arg','Disallow'),(-1773849172,'Arg','Disallow'),(482746213,'Arg','Disallow'),(197990351,'Arg','Disallow'),(1137348529,'Arg','Disallow'),(-1258315430,'Arg','Disallow'),(-784521992,'Arg','Disallow'),(-252565531,'case_sense','1'),(912494834,'case_sense','1'),(464999411,'case_sense','1'),(-1159974666,'case_sense','1'),(384533294,'case_sense','1'),(-995775740,'case_sense','1'),(-1519435788,'case_sense','1'),(1306691934,'case_sense','1'),(-199371280,'case_sense','1'),(30013236,'case_sense','1'),(648492231,'case_sense','1'),(1049847163,'case_sense','1'),(752243032,'case_sense','1'),(-1416287592,'case_sense','1'),(2007963022,'case_sense','1'),(-861680858,'case_sense','1'),(-1937387122,'case_sense','1'),(-1376835098,'case_sense','1'),(-516766697,'case_sense','1'),(-1510773345,'case_sense','1'),(-310937748,'case_sense','1'),(-303415946,'case_sense','1'),(-944636503,'case_sense','1'),(969154153,'case_sense','1'),(-1674775020,'case_sense','1'),(497306035,'case_sense','1'),(-1636145178,'case_sense','1'),(-1487048733,'case_sense','1'),(-1744002416,'case_sense','1'),(-1426189027,'case_sense','1'),(1653070540,'case_sense','1'),(-1123784825,'case_sense','1'),(1878198905,'case_sense','1'),(-45025424,'case_sense','1'),(936478137,'case_sense','1'),(-39226065,'case_sense','1'),(-1891421144,'case_sense','1'),(1177200378,'case_sense','1'),(-224724630,'case_sense','1'),(-877971084,'case_sense','1'),(309142739,'case_sense','1'),(689968446,'case_sense','1'),(-1287468087,'case_sense','1'),(-1000453941,'case_sense','1'),(836863171,'case_sense','1'),(151966379,'match_type','1'),(151966379,'case_sense','1'),(30013236,'Arg','Disallow'),(-252565531,'Arg','Disallow'),(912494834,'Arg','Disallow'),(464999411,'Arg','Disallow'),(-1159974666,'Arg','Disallow'),(384533294,'Arg','Disallow'),(-995775740,'Arg','Disallow'),(-1519435788,'Arg','Disallow'),(1306691934,'Arg','Disallow'),(-199371280,'Arg','Disallow'),(648492231,'Arg','Disallow'),(1049847163,'Arg','Disallow'),(752243032,'Arg','Disallow'),(-1416287592,'Arg','Disallow'),(2007963022,'Arg','Disallow'),(-861680858,'Arg','Disallow'),(-1937387122,'Arg','Disallow'),(-1376835098,'Arg','Disallow'),(-516766697,'Arg','Disallow'),(-1510773345,'Arg','Disallow'),(-310937748,'Arg','Disallow'),(-303415946,'Arg','Disallow'),(-944636503,'Arg','Disallow'),(969154153,'Arg','Disallow'),(-1674775020,'Arg','Disallow'),(497306035,'Arg','Disallow'),(-1636145178,'Arg','Disallow'),(-1487048733,'Arg','Disallow'),(-1744002416,'Arg','Disallow'),(-1426189027,'Arg','Disallow'),(1653070540,'Arg','Disallow'),(-1123784825,'Arg','Disallow'),(1878198905,'Arg','Disallow'),(-45025424,'Arg','Disallow'),(936478137,'Arg','Disallow'),(-39226065,'Arg','Disallow'),(-1891421144,'Arg','Disallow'),(1177200378,'Arg','Disallow'),(-224724630,'Arg','Disallow'),(-877971084,'Arg','Disallow'),(309142739,'Arg','Disallow'),(689968446,'Arg','Disallow'),(-1287468087,'Arg','Disallow'),(-1000453941,'Arg','Disallow'),(836863171,'Arg','Disallow'),(-1204776029,'match_type','1'),(-1204776029,'case_sense','1'),(1884141941,'case_sense','1'),(-72068437,'case_sense','1'),(1919980703,'nomatch','0'),(-342154600,'match_type','5'),(-342154600,'case_sense','1'),(-2001060016,'case_sense','1'),(-342154600,'Arg','Disallow'),(1884141941,'nomatch','0'),(1884141941,'match_type','1'),(-976027547,'match_type','1'),(-976027547,'case_sense','1'),(519569210,'match_type','1'),(519569210,'case_sense','1'),(519569210,'nomatch','0'),(-976027547,'nomatch','0'),(-2127342638,'case_sense','1'),(-2127342638,'match_type','1'),(1263981379,'match_type','1'),(1263981379,'case_sense','1'),(-2127342638,'nomatch','0'),(1371388389,'match_type','1'),(-1475959109,'nomatch','0'),(974011152,'nomatch','0'),(-1970644423,'nomatch','0'),(-1970644423,'match_type','1'),(1919980703,'match_type','1'),(1371388389,'case_sense','1'),(974011152,'match_type','1'),(-72068437,'Arg','Disallow'),(-1475959109,'match_type','1'),(974011152,'case_sense','1'),(-2001060016,'Arg','Disallow'),(-1970644423,'case_sense','1'),(1919980703,'case_sense','1'),(-1475959109,'case_sense','1'),(-848635036,'match_type','1'),(-1027837456,'nomatch','0'),(-1027837456,'match_type','1'),(-737295299,'nomatch','0'),(-737295299,'match_type','1'),(-737295299,'case_sense','1'),(423986946,'match_type','1'),(-848635036,'nomatch','0'),(547118035,'nomatch','0'),(547118035,'match_type','1'),(-1349095151,'match_type','1'),(-837075513,'match_type','1'),(-641400926,'match_type','1'),(-782363088,'match_type','1'),(1769101917,'match_type','1'),(362612081,'match_type','1'),(-763241724,'match_type','1'),(-961988269,'case_sense','1'),(-961988269,'match_type','1'),(-961988269,'nomatch','0'),(1237619594,'case_sense','1'),(1237619594,'match_type','1'),(1237619594,'nomatch','0'),(1423625661,'case_sense','1'),(1423625661,'match_type','1'),(1423625661,'nomatch','0'),(-171097504,'case_sense','1'),(-171097504,'match_type','1'),(-171097504,'nomatch','0'),(1406792259,'case_sense','1'),(1406792259,'match_type','1'),(1406792259,'nomatch','0'),(-1337349758,'case_sense','1'),(-1337349758,'match_type','1'),(-1337349758,'nomatch','0'),(333223404,'case_sense','1'),(333223404,'match_type','1'),(333223404,'nomatch','0'),(-280849816,'case_sense','1'),(-280849816,'match_type','1'),(-280849816,'nomatch','0'),(1486465872,'case_sense','1'),(1486465872,'match_type','1'),(1486465872,'nomatch','0'),(-850050420,'case_sense','1'),(-850050420,'match_type','1'),(-850050420,'nomatch','0'),(1389518315,'match_type','1'),(1389518315,'case_sense','1'),(1389518315,'nomatch','0'),(-56417367,'nomatch','0'),(-56417367,'match_type','1'),(-56417367,'case_sense','1'),(-1734043788,'case_sense','1'),(-1734043788,'match_type','1'),(-1734043788,'nomatch','0'),(-2028032039,'case_sense','1'),(-2028032039,'match_type','1'),(-2028032039,'nomatch','0'),(419974889,'case_sense','1'),(419974889,'match_type','1'),(419974889,'nomatch','0'),(-2115257443,'case_sense','1'),(-2115257443,'match_type','1'),(-2115257443,'nomatch','0'),(-1048757676,'nomatch','0'),(-1048757676,'match_type','1'),(-1048757676,'case_sense','1'),(-1028678795,'case_sense','1'),(-1028678795,'match_type','1'),(-1028678795,'nomatch','0'),(-354222877,'case_sense','1'),(-354222877,'match_type','1'),(-354222877,'nomatch','0'),(-446889658,'case_sense','1'),(-446889658,'match_type','1'),(-446889658,'nomatch','0'),(1612748395,'case_sense','1'),(1612748395,'match_type','1'),(1612748395,'nomatch','0'),(-1027837456,'case_sense','1'),(547118035,'case_sense','1'),(423986946,'case_sense','1'),(-1349095151,'case_sense','1'),(-837075513,'case_sense','1'),(-641400926,'case_sense','1'),(-782363088,'case_sense','1'),(1769101917,'case_sense','1'),(362612081,'case_sense','1'),(-763241724,'case_sense','1'),(423986946,'nomatch','0'),(-848635036,'case_sense','1'),(-1349095151,'nomatch','0'),(-837075513,'nomatch','0'),(-641400926,'nomatch','0'),(-782363088,'nomatch','0'),(1769101917,'nomatch','0'),(362612081,'nomatch','0'),(-763241724,'nomatch','0'),(6754301,'match_type','1'),(1489135615,'nomatch','0'),(328013356,'nomatch','0'),(328013356,'match_type','1'),(328013356,'case_sense','1'),(-1460022819,'nomatch','0'),(6754301,'nomatch','0'),(1489135615,'case_sense','1'),(-405105146,'nomatch','0'),(-346938837,'nomatch','0'),(1247300804,'nomatch','0'),(1478705573,'nomatch','0'),(368963103,'nomatch','0'),(-265238827,'nomatch','0'),(-292609038,'nomatch','0'),(1431329223,'nomatch','0'),(-1460022819,'match_type','1'),(-405105146,'match_type','1'),(-346938837,'match_type','1'),(1247300804,'match_type','1'),(1478705573,'match_type','1'),(368963103,'match_type','1'),(-265238827,'match_type','1'),(-292609038,'match_type','1'),(1431329223,'match_type','1'),(-346938837,'case_sense','1'),(-405105146,'case_sense','1'),(-1460022819,'case_sense','1'),(1247300804,'case_sense','1'),(1478705573,'case_sense','1'),(368963103,'case_sense','1'),(-265238827,'case_sense','1'),(-292609038,'case_sense','1'),(1431329223,'case_sense','1'),(1489135615,'match_type','1'),(6754301,'case_sense','1'),(1591136704,'match_type','1'),(-693710674,'nomatch','0'),(-693710674,'match_type','1'),(-693710674,'case_sense','1'),(-1090194015,'match_type','1'),(1603991451,'match_type','1'),(1438149566,'match_type','1'),(-758219674,'match_type','1'),(-1404395419,'nomatch','0'),(-1404395419,'match_type','1'),(-1404395419,'case_sense','1'),(-254154474,'match_type','1'),(-1531341351,'nomatch','0'),(-1531341351,'match_type','1'),(-1531341351,'case_sense','1'),(-472504224,'match_type','1'),(157397846,'match_type','1'),(582742939,'match_type','1'),(-1301322202,'match_type','1'),(-433746697,'match_type','1'),(116684345,'nomatch','0'),(116684345,'match_type','1'),(116684345,'case_sense','1'),(-1336117105,'nomatch','0'),(-1336117105,'match_type','1'),(-1336117105,'case_sense','1'),(1223578368,'match_type','1'),(760126362,'match_type','1'),(1385432781,'match_type','1'),(-1090194015,'case_sense','1'),(1603991451,'case_sense','1'),(1438149566,'case_sense','1'),(-758219674,'case_sense','1'),(1470004335,'match_type','1'),(1470004335,'case_sense','1'),(-472504224,'case_sense','1'),(157397846,'case_sense','1'),(582742939,'case_sense','1'),(760126362,'case_sense','1'),(-1301322202,'case_sense','1'),(-433746697,'case_sense','1'),(-1090194015,'nomatch','0'),(1603991451,'nomatch','0'),(1438149566,'nomatch','0'),(-758219674,'nomatch','0'),(-254154474,'case_sense','1'),(-254154474,'nomatch','0'),(1470004335,'nomatch','0'),(-472504224,'nomatch','0'),(157397846,'nomatch','0'),(582742939,'nomatch','0'),(760126362,'nomatch','0'),(-1301322202,'nomatch','0'),(-433746697,'nomatch','0'),(-915964481,'nomatch','0'),(-915964481,'match_type','1'),(-1460296193,'nomatch','0'),(-915964481,'case_sense','1'),(458910558,'match_type','1'),(1185210320,'match_type','1'),(-1156299735,'match_type','1'),(2071952128,'match_type','1'),(-524948532,'match_type','1'),(695889792,'match_type','1'),(458910558,'case_sense','1'),(1185210320,'case_sense','1'),(-1156299735,'case_sense','1'),(2071952128,'case_sense','1'),(-524948532,'case_sense','1'),(-1460296193,'match_type','1'),(695889792,'case_sense','1'),(-1460296193,'case_sense','1'),(1385432781,'case_sense','1'),(1223578368,'case_sense','1'),(458910558,'nomatch','0'),(1185210320,'nomatch','0'),(-1156299735,'nomatch','0'),(-524948532,'nomatch','0'),(2071952128,'nomatch','0'),(695889792,'nomatch','0'),(1385432781,'nomatch','0'),(1223578368,'nomatch','0'),(-940110355,'match_type','1'),(-940110355,'case_sense','1'),(-940110355,'nomatch','0'),(1591136704,'case_sense','1'),(1591136704,'nomatch','0');
UNLOCK TABLES;
/*!40000 ALTER TABLE `srvinfo` ENABLE KEYS */;

--
-- Table structure for table `url`
--

DROP TABLE IF EXISTS `url`;
CREATE TABLE `url` (
  `rec_id` int(11) NOT NULL auto_increment,
  `status` smallint(6) NOT NULL default '0',
  `docsize` int(11) NOT NULL default '0',
  `next_index_time` int(11) NOT NULL default '0',
  `last_mod_time` int(11) NOT NULL default '0',
  `referrer` int(11) NOT NULL default '0',
  `hops` smallint(6) NOT NULL default '0',
  `crc32` int(11) NOT NULL default '-1',
  `seed` smallint(6) NOT NULL default '0',
  `bad_since_time` int(11) NOT NULL default '0',
  `site_id` int(11) default NULL,
  `server_id` int(11) default NULL,
  `shows` int(11) NOT NULL default '0',
  `pop_rank` float NOT NULL default '0',
  `url` blob NOT NULL,
  PRIMARY KEY  (`rec_id`),
  UNIQUE KEY `url` (`url`(255)),
  KEY `key_crc` (`crc32`),
  KEY `key_seed` (`seed`),
  KEY `key_referrer` (`referrer`),
  KEY `key_bad_since_time` (`bad_since_time`),
  KEY `key_next_index_time` (`next_index_time`),
  KEY `key_site_id` (`site_id`),
  KEY `key_status` (`status`),
  KEY `key_hops` (`hops`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `url`
--


/*!40000 ALTER TABLE `url` DISABLE KEYS */;
LOCK TABLES `url` WRITE;
INSERT INTO `url` VALUES (3855,200,1592,1178140188,1169840807,0,0,-1177115177,130,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ciscotemp.html'),(3856,200,4676,1178140188,1169840807,0,0,119942934,202,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_by_ssh.html'),(3857,200,1841,1178140188,1175534261,0,0,-1875320479,155,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_breeze.html'),(3858,200,1301,1178140187,1175534261,0,0,-2099437362,114,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_bgpstate.html'),(3859,200,1410,1178140186,1175534261,0,0,-64507746,5,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_bandwidth.html'),(3860,200,1351,1178140187,1175534261,0,0,146632225,115,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_backup.html'),(3861,200,1280,1178140187,1175534261,0,0,1766670013,71,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_axis.html'),(3862,200,3194,1178140189,1175534261,0,0,-996518080,249,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_arping.html'),(3863,200,3537,1178140188,1175534261,0,0,2035301408,159,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_apt.html'),(3864,200,2226,1178140187,1175534261,0,0,993475659,98,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_appletalk.html'),(3865,200,2361,1178140187,1175534261,0,0,-1537826830,72,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_apc_ups.html'),(3866,200,2651,1178140187,1175534261,0,0,1073149314,39,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_apache.html'),(3867,200,1239,1178140189,1175534261,0,0,-1849814895,242,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_adptraid.html'),(3868,200,3470,1178140188,1175881122,0,0,1043545815,171,1177535386,-1783393467,1478705573,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Overview.html'),(3869,200,3805,1178140188,1175881122,0,0,-1030634134,162,1177535386,-1783393467,368963103,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Perl/Perl_Overview.html'),(3870,200,3667,1178140187,1175881122,0,0,925672331,100,1177535386,-1783393467,-265238827,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/PHP/PHP_Overview.html'),(3871,200,134175,1178140187,1165614638,0,0,747331803,78,1177535386,-1783393467,-292609038,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/RRDtool/RRDtool_Overview.html'),(3872,200,26604,1178140187,1165614638,0,0,-1327067830,60,1177535386,-1783393467,1223578368,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/sendPage/sendPage_Overview.html'),(3873,200,11644,1178140188,1176745206,0,0,-367150744,183,1177535386,-1783393467,1431329223,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/WMI/WMI_Overview.html'),(3874,200,11968,1178140189,1176745201,0,0,-1311534236,224,1177535386,-1783393467,-940110355,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/GroundWork_Profiles/Profile_Definitions/gwsp2-ssh_UNIX.html'),(3875,200,10758,1178140187,1176745201,0,0,2027791179,104,1177535386,-1783393467,-940110355,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/GroundWork_Profiles/Profile_Definitions/gwsp2-snmp_network.html'),(3876,200,6589,1178140188,1176745201,0,0,884531447,178,1177535386,-1783393467,-940110355,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/GroundWork_Profiles/Profile_Definitions/gwsp2-service_ping.html'),(3877,200,18807,1178140186,1175534258,0,0,-1065159261,14,1177535386,-1783393467,1591136704,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/GroundWork_Profiles/SSH_Monitoring.html'),(3878,200,22433,1178140189,1175534258,0,0,-1577500897,255,1177535386,-1783393467,1591136704,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/GroundWork_Profiles/Profiles_Overview.html'),(3879,200,253798,1178140189,1176745204,0,0,1412661930,246,1177535386,-1783393467,-1460022819,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/Administrator_Guide.html'),(3880,200,189321,1178140188,1177087219,0,0,1502273774,207,1177535386,-1783393467,-346938837,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Operator/Operator_Guide.html'),(3881,200,147790,1178140187,1175534259,0,0,-1894446516,37,1177535386,-1783393467,-405105146,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Developer/Developer_Guide.html'),(3882,200,5850,1178140189,1175534259,0,0,-718762513,222,1177535386,-1783393467,-1460022819,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/tools.html'),(3883,200,3985,1178140188,1175534259,0,0,-110207431,217,1177535386,-1783393467,-1460022819,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/timeperiods.html'),(3884,200,48751,1178140188,1175534259,0,0,-1384717038,173,1177535386,-1783393467,-1460022819,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/services.html'),(3885,200,23501,1178140186,1175534259,0,0,1614479174,21,1177535386,-1783393467,-1460022819,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/profiles.html'),(3886,200,15269,1178140187,1175534259,0,0,1499078185,87,1177535386,-1783393467,-1460022819,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/managinghosts.html'),(3887,200,38622,1178140189,1175534259,0,0,411748567,222,1177535386,-1783393467,-1460022819,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/hosts.html'),(3888,200,21288,1178140188,1175534259,0,0,836459863,147,1177535386,-1783393467,-1460022819,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/groups.html'),(3889,200,12700,1178140189,1175534259,0,0,-1604873297,220,1177535386,-1783393467,-1460022819,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/escalations.html'),(3890,200,68532,1178140188,1175534259,0,0,-1361382848,218,1177535386,-1783393467,-1460022819,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/control.html'),(3891,200,15614,1178140189,1175534259,0,0,266199270,253,1177535386,-1783393467,-1460022819,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/contacts.html'),(3892,200,5654,1178140187,1175534259,0,0,-2063029932,80,1177535386,-1783393467,-1460022819,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Administrator/commands.html'),(3893,404,0,1178140189,0,3881,1,0,53,1177535389,-1783393467,-405105146,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/GroundWork_Reference/Developer/packages/bookshelf/docs/groundworkmonitorprofessional/GroundWork_Reference/Developer/PHP_Foundation_API/FoundationAPIDoc/index.html'),(3894,404,0,1178140189,0,3871,1,0,7,1177535389,-1783393467,-292609038,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/RRDtool/alex@ergens.op.het.net'),(3790,200,1861,1178140188,1175706422,0,0,248898961,191,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_mysqlslave.html'),(3791,200,2688,1178140186,1175534261,0,0,386702078,10,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_mysql_query.html'),(3792,200,1656,1178140187,1175706422,0,0,-306892132,80,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_mssql_log.html'),(3793,200,2638,1178140187,1175706422,0,0,-857350725,125,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_mysql.html'),(3794,200,1241,1178140186,1175544210,0,0,-1446847927,7,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_mssql2000.html'),(3795,200,2343,1178140187,1175544209,0,0,2040615899,93,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_mssql2.html'),(3796,200,1248,1178140188,1175544209,0,0,1339374188,211,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_mssql.html'),(3797,200,1934,1178140187,1175544209,0,0,-142833565,64,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ms_spooler.html'),(3798,200,3050,1178140188,1169840807,0,0,-596482877,140,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_mrtgtraf.html'),(3799,200,4058,1178140187,1169840807,0,0,574224152,97,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_mrtg.html'),(3800,200,1638,1178140187,1175544209,0,0,-1480380947,73,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_mem.html'),(3801,200,1330,1178140187,1175544209,0,0,159171337,107,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_maxwanstate.html'),(3802,200,1352,1178140189,1175544209,0,0,1954875875,235,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_maxchannels.html'),(3803,200,3055,1178140189,1175544209,0,0,1152041549,255,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_mailq.html'),(3804,200,2486,1178140189,1175544210,0,0,1664234808,238,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_lotus.html'),(3805,200,2776,1178140187,1175534261,0,0,101280851,115,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_logs.html'),(3806,200,1975,1178140187,1175534261,0,0,-1502897233,38,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_log2.html'),(3807,200,1502,1178140188,1175534261,0,0,-1899932811,160,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_log.html'),(3808,200,2335,1178140186,1175544209,0,0,1060534653,25,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_load_remote.html'),(3809,200,1862,1178140187,1169840807,0,0,37249698,81,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_load.html'),(3810,200,1264,1178140187,1175544209,0,0,-431367036,98,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_lmmon.html'),(3811,200,3275,1178140188,1169840807,0,0,-701480625,198,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ldap.html'),(3812,200,1291,1178140189,1175544209,0,0,-1011577523,242,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_joy.html'),(3813,200,2179,1178140189,1175534261,0,0,-1213003905,244,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_javaproc.html'),(3814,200,4131,1178140187,1169840807,0,0,1222672895,113,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_jabber.html'),(3815,200,1973,1178140187,1169840807,0,0,506501089,101,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ircd.html'),(3816,200,1577,1178140188,1175534261,0,0,1803576832,145,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_inodes-freebsd.html'),(3817,200,1226,1178140187,1175534261,0,0,1383305719,100,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_inodes.html'),(3818,200,4125,1178140189,1169840807,0,0,39011753,234,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_imap.html'),(3819,200,3849,1178140189,1175534261,0,0,-78405964,254,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ifstatus.html'),(3820,200,4383,1178140188,1175534261,0,0,-2048569465,158,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ifoperstatus.html'),(3821,200,1335,1178140187,1175534261,0,0,-354719130,56,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_if.html'),(3822,200,3279,1178140187,1169840807,0,0,-304880241,97,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_icmp.html'),(3823,200,2684,1178140187,1169840807,0,0,1724346031,88,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ica_program_neighbourhood.html'),(3824,200,3664,1178140187,1175534261,0,0,2036017522,55,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ica_metaframe_pub_apps.html'),(3825,200,3370,1178140188,1175534261,0,0,843977268,217,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ica_master_browser.html'),(3826,200,1179,1178140187,1175534261,0,0,926336353,77,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_hw.html'),(3827,200,7471,1178140188,1169840807,0,0,-1541777521,165,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_http.html'),(3828,200,2384,1178140186,1175534261,0,0,48794376,6,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_hprsc.html'),(3829,200,1892,1178140187,1169840807,0,0,1314666182,45,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_hpjd.html'),(3830,200,2181,1178140188,1175534261,0,0,635932049,176,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_host_foundation.html'),(3831,200,2824,1178140188,1169840807,0,0,779708588,191,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_game.html'),(3832,200,1728,1178140188,1175534261,0,0,-563974240,200,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ftpget.html'),(3833,200,4141,1178140187,1169840807,0,0,-1857277710,92,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ftp.html'),(3834,200,2643,1178140188,1175534261,0,0,-989330654,128,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_fping.html'),(3835,200,2229,1178140189,1175534261,0,0,1327192443,255,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_flexlm.html'),(3836,200,1850,1178140189,1175534261,0,0,-146956762,233,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_file_age.html'),(3837,200,3710,1178140186,1175534261,0,0,-2117162149,35,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_email_loop.html'),(3838,200,1534,1178140187,1169840807,0,0,871421068,78,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_dummy.html'),(3839,200,1249,1178140187,1169840807,0,0,-492364463,51,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_dns_random.html'),(3840,200,6835,1178140188,1175534261,0,0,1891305581,176,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_dns.html'),(3841,200,2114,1178140189,1175534261,0,0,-855457558,243,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_dlswcircuit.html'),(3842,200,1322,1178140188,1175534261,0,0,1488087004,148,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_dl_size.html'),(3843,200,2187,1178140187,1175534261,0,0,653986139,72,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_disk_snmp.html'),(3844,200,2917,1178140187,1175534261,0,0,1996261829,73,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_disk_smb.html'),(3845,200,2329,1178140188,1175534261,0,0,-1703562578,211,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_disk_remote.html'),(3846,200,4203,1178140188,1175534261,0,0,-1161274337,129,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_disk.html'),(3847,200,1910,1178140187,1175534261,0,0,1549172109,97,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_digitemp.html'),(3848,200,2822,1178140188,1169840807,0,0,1718179028,144,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_dig.html'),(3849,200,2170,1178140186,1169840807,0,0,1121828787,12,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_dhcp.html'),(3850,200,1594,1178140186,1175534261,0,0,-637255864,12,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_dell_hw.html'),(3851,200,2415,1178140187,1175534261,0,0,299292135,116,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_cpu.html'),(3852,200,1244,1178140188,1175534261,0,0,-1375053151,207,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_connections.html'),(3853,200,1755,1178140189,1175534261,0,0,-1847258219,232,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_compaq_insight.html'),(3854,200,4359,1178140188,1175534261,0,0,1022706061,213,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_clamd.html'),(3720,200,5391,1178140187,1165614638,0,0,1381095946,89,1177535386,-1783393467,1385432781,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/dojo/dojo_Overview.html'),(3721,200,6640,1178140188,1175881122,0,0,929220011,134,1177535386,-1783393467,1247300804,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/MySQL/MySQL_Overview.html'),(3722,200,3599,1178140188,1175881121,0,0,-250254719,185,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_wins.html'),(3723,200,1433,1178140188,1175881122,0,0,1214903882,212,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_wave.html'),(3724,200,1895,1178140189,1175881121,0,0,-1030852915,248,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_vcs.html'),(3725,200,1816,1178140187,1169840807,0,0,-22387784,66,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_users.html'),(3726,200,1964,1178140187,1169840807,0,0,-1916027756,63,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/urlize.html'),(3727,200,3629,1178140186,1169840807,0,0,1390292191,30,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ups.html'),(3728,200,4104,1178140187,1169840807,0,0,242108261,71,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_udp2.html'),(3729,200,3074,1178140187,1175881121,0,0,1483175332,91,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_udp.html'),(3730,200,2310,1178140187,1175881121,0,0,110440598,84,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_traceroute.html'),(3731,200,2595,1178140187,1169840807,0,0,68159552,94,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_time.html'),(3732,200,4086,1178140187,1175881122,0,0,261939589,104,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_tcp.html'),(3733,200,1931,1178140187,1175881122,0,0,-2042118104,46,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_syslog_gw.html'),(3734,200,3510,1178140186,1175534261,0,0,550157248,36,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_sybase.html'),(3735,200,2383,1178140187,1175881122,0,0,995786841,58,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_swap_remote.html'),(3736,200,2649,1178140188,1175881122,0,0,-1337663649,202,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_swap.html'),(3737,200,4157,1178140186,1175881122,0,0,-1129825585,0,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ssmtp.html'),(3738,200,2371,1178140187,1169840807,0,0,1015098160,51,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ssh.html'),(3739,200,4090,1178140188,1175881121,0,0,122717447,152,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_spop.html'),(3740,200,5145,1178140188,1175881122,0,0,1995245514,149,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_snmp_procs.html'),(3741,200,2793,1178140188,1175881121,0,0,-581255162,157,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_snmp_process_monitor.html'),(3742,200,2219,1178140188,1175881121,0,0,-941630990,217,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_snmp_printer.html'),(3743,200,2367,1178140188,1175881121,0,0,-520178666,152,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_snmp_disk_monitor.html'),(3744,200,6450,1178140188,1169840807,0,0,-1411108707,195,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_snmp.html'),(3745,200,3680,1178140188,1169840807,0,0,299601047,128,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_smtp.html'),(3746,200,1260,1178140188,1175881122,0,0,253271493,129,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_smb.html'),(3747,200,1863,1178140188,1175881121,0,0,-2036943182,127,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_smart.html'),(3748,200,4131,1178140188,1169840807,0,0,-155400802,179,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_simap.html'),(3749,200,1215,1178140186,1175881122,0,0,1979566036,21,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_sensors.html'),(3750,200,1314,1178140189,1175881121,0,0,1665350930,251,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_sap.html'),(3751,200,5841,1178140188,1175881121,0,0,684369606,148,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_rrd_hw.html'),(3752,200,2746,1178140187,1175881121,0,0,-1096348712,59,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_rrd_data.html'),(3753,200,2133,1178140186,1175881122,0,0,-601857935,28,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_rpc.html'),(3754,200,8229,1178140186,1175881121,0,0,1437354613,8,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_remote_nagios_status.html'),(3755,200,2844,1178140188,1169840807,0,0,1294140793,131,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_real.html'),(3756,200,3236,1178140187,1169840807,0,0,-1869968821,73,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_radius.html'),(3757,200,4756,1178140188,1169840807,0,0,-1583524655,172,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_procs.html'),(3758,200,1425,1178140187,1175881122,0,0,-551195730,64,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_procr.html'),(3759,200,3154,1178140187,1169840807,0,0,585679408,84,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_procl.html'),(3760,200,1432,1178140188,1175881122,0,0,-7841864,177,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_pop3.html'),(3761,200,4124,1178140189,1169840807,0,0,-724779516,223,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_pop.html'),(3762,200,3072,1178140187,1169840807,0,0,1001467293,78,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ping.html'),(3763,200,3951,1178140188,1169840807,0,0,-1218105126,130,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_pgsql.html'),(3764,200,1404,1178140188,1169840807,0,0,-393750324,151,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_pfstate.html'),(3765,200,2826,1178140188,1169840807,0,0,425274133,144,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_pcpmetric.html'),(3766,200,3553,1178140189,1169840807,0,0,252321393,248,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_overcr.html'),(3767,200,1194,1178140188,1175881122,0,0,2046536456,196,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_oracle_status.html'),(3768,200,1219,1178140187,1175881122,0,0,-1295356643,44,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_oracle_stats.html'),(3769,200,1182,1178140187,1175881122,0,0,-1893098908,105,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_oracle_spcrit.html'),(3770,200,1182,1178140188,1175881122,0,0,1052663736,192,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_oracle_online.html'),(3771,200,1183,1178140186,1175881121,0,0,312810065,31,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_oracle_maxssn.html'),(3772,200,1182,1178140187,1175881122,0,0,-716733347,42,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_oracle_maxprc.html'),(3773,200,1241,1178140188,1175706422,0,0,-622431916,195,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_oracle_maxext.html'),(3774,200,1262,1178140187,1169840807,0,0,1473352698,87,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_oracle_logmode_new.html'),(3775,200,1241,1178140188,1169840807,0,0,137651165,150,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_oracle_invobj.html'),(3776,200,3565,1178140186,1169840807,0,0,-1067517063,11,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_oracle_instance.html'),(3777,200,1247,1178140188,1169840807,0,0,919998707,136,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_oracle_autoext.html'),(3778,200,3302,1178140187,1169840807,0,0,1381087379,104,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_oracle.html'),(3779,200,1355,1178140186,1169840807,0,0,385518526,7,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ora_table_space.html'),(3780,200,6174,1178140188,1169840807,0,0,1594712112,156,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_nwstat.html'),(3781,200,2205,1178140186,1169840807,0,0,989460318,16,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_ntp.html'),(3782,200,4856,1178140189,1169840807,0,0,302789415,234,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_nt.html'),(3783,200,2652,1178140188,1169840807,0,0,-421636679,184,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_nrpe.html'),(3784,200,4144,1178140188,1169840807,0,0,-1157887339,205,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_nntps.html'),(3785,200,4127,1178140188,1169840807,0,0,176423780,162,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_nntp.html'),(3786,200,1614,1178140187,1175706422,0,0,138742861,68,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_netapp.html'),(3787,200,2185,1178140186,1169840807,0,0,-944008089,3,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/negate.html'),(3788,200,2806,1178140188,1175706422,0,0,618184507,200,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_nagios_status_log.html'),(3789,200,2278,1178140187,1169840807,0,0,2018800641,110,1177535386,-1783393467,328013356,0,0,'http://127.0.0.1/monitor/packages/bookshelf/docs/groundworkmonitoropensource/Open_Source_Reference/Nagios/Nagios_Plugins/check_nagios.html');
UNLOCK TABLES;
/*!40000 ALTER TABLE `url` ENABLE KEYS */;

--
-- Table structure for table `urlinfo`
--

DROP TABLE IF EXISTS `urlinfo`;
CREATE TABLE `urlinfo` (
  `url_id` int(11) NOT NULL default '0',
  `sname` varchar(32) NOT NULL default '',
  `sval` text NOT NULL,
  KEY `urlinfo_id` (`url_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `urlinfo`
--


/*!40000 ALTER TABLE `urlinfo` DISABLE KEYS */;
LOCK TABLES `urlinfo` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `urlinfo` ENABLE KEYS */;

--
-- Table structure for table `wrdstat`
--

DROP TABLE IF EXISTS `wrdstat`;
CREATE TABLE `wrdstat` (
  `word` varchar(64) NOT NULL default '',
  `snd` varchar(16) NOT NULL default '',
  `cnt` int(11) NOT NULL default '0',
  KEY `word` (`word`),
  KEY `snd` (`snd`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `wrdstat`
--


/*!40000 ALTER TABLE `wrdstat` DISABLE KEYS */;
LOCK TABLES `wrdstat` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `wrdstat` ENABLE KEYS */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
