-- phpMyAdmin SQL Dump
-- version 2.6.4-pl4
-- http://www.phpmyadmin.net
-- 
-- Host: localhost
-- Generation Time: Dec 08, 2005 at 12:23 PM
-- Server version: 5.0.15
-- PHP Version: 5.0.5
-- 
-- Database: `guava`
-- 

-- --------------------------------------------------------

-- 
-- Table structure for table `chat_messages`
-- 

CREATE TABLE `chat_messages` (
  `message_id` int(11) unsigned NOT NULL auto_increment,
  `timestamp` decimal(16,3) NOT NULL,
  `text` varchar(255) NOT NULL,
  PRIMARY KEY  (`message_id`)
) TYPE=MyISAM AUTO_INCREMENT=1 ;

-- 
-- Dumping data for table `chat_messages`
-- 


-- --------------------------------------------------------

-- 
-- Table structure for table `chat_users`
-- 

CREATE TABLE `chat_users` (
  `user_id` int(11) unsigned NOT NULL auto_increment,
  `timestamp` double(16,3) NOT NULL,
  `username` varchar(50) NOT NULL,
  `nickname` varchar(50) NOT NULL,
  PRIMARY KEY  (`user_id`)
) TYPE=MyISAM;

-- 
-- Dumping data for table `chat_users`
-- 

