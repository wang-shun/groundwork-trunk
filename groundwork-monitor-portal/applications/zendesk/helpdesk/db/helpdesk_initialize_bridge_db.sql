--
-- Database: `HelpDeskBridgeDB`
--

DROP DATABASE IF EXISTS `HelpDeskBridgeDB`;
CREATE DATABASE IF NOT EXISTS `HelpDeskBridgeDB`;

use `HelpDeskBridgeDB`;

-- --------------------------------------------------------

--
-- Table structure for table `HelpDeskConcurrencyTable`
--

DROP TABLE IF EXISTS `HelpDeskConcurrencyTable`;
CREATE TABLE IF NOT EXISTS `HelpDeskConcurrencyTable` (
  `LogMessageID` int(11) NOT NULL,
  UNIQUE KEY `LogMessageID` (`LogMessageID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `HelpDeskLookupTable`
--

DROP TABLE IF EXISTS `HelpDeskLookupTable`;
CREATE TABLE IF NOT EXISTS `HelpDeskLookupTable` (
  `LogMessageID` int(11) NOT NULL,
  `DeviceIdentification` varchar(128) collate utf8_unicode_ci default NULL,
  `Operator` varchar(64) collate utf8_unicode_ci NOT NULL,
  `TicketNo` varchar(64) collate utf8_unicode_ci NOT NULL,
  `TicketStatus` varchar(64) collate utf8_unicode_ci NOT NULL,
  `ClientData` text collate utf8_unicode_ci,
  PRIMARY KEY  (`LogMessageID`),
  KEY `TicketStatus` (`TicketStatus`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

