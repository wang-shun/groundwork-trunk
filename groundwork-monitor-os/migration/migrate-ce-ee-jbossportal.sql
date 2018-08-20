
select * into outfile '/tmp/jbp_roles.txt' fields terminated by ',' from jbp_roles;
select * into outfile '/tmp/jbp_role_membership.txt' fields terminated by ',' from jbp_role_membership;
select * into outfile '/tmp/jbp_users.txt' fields terminated by ',' from jbp_users;
select * into outfile '/tmp/jbp_user_prop.txt' fields terminated by ',' from jbp_user_prop;
drop database jbossportal;
create database jbossportal;
use jbossportal;
CREATE TABLE `jbp_roles` (
  `jbp_rid` bigint(20) NOT NULL auto_increment,
  `jbp_name` varchar(255) default NULL,
  `jbp_displayname` varchar(255) default NULL,
  PRIMARY KEY  (`jbp_rid`),
  UNIQUE KEY `jbp_name` (`jbp_name`),
  UNIQUE KEY `jbp_displayname` (`jbp_displayname`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
CREATE TABLE `jbp_users` (
  `jbp_uid` bigint(20) NOT NULL auto_increment,
  `jbp_uname` varchar(255) default NULL,
  `jbp_givenname` varchar(255) default NULL,
  `jbp_familyname` varchar(255) default NULL,
  `jbp_password` varchar(255) default NULL,
  `jbp_realemail` varchar(255) default NULL,
  `jbp_fakeemail` varchar(255) default NULL,
  `jbp_regdate` datetime default NULL,
  `jbp_viewrealemail` bit(1) default NULL,
  `jbp_enabled` bit(1) default NULL,
  PRIMARY KEY  (`jbp_uid`),
  UNIQUE KEY `jbp_uname` (`jbp_uname`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
CREATE TABLE `jbp_role_membership` (
  `jbp_uid` bigint(20) NOT NULL,
  `jbp_rid` bigint(20) NOT NULL,
  PRIMARY KEY  (`jbp_uid`,`jbp_rid`),
  KEY `FKF410173866F4DA65` (`jbp_uid`),
  KEY `FKF410173866F3164D` (`jbp_rid`),
  CONSTRAINT `FKF410173866F3164D` FOREIGN KEY (`jbp_rid`) REFERENCES `jbp_roles` (`jbp_rid`),
  CONSTRAINT `FKF410173866F4DA65` FOREIGN KEY (`jbp_uid`) REFERENCES `jbp_users` (`jbp_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
CREATE TABLE `jbp_user_prop` (
  `jbp_uid` bigint(20) NOT NULL,
  `jbp_value` varchar(255) default NULL,
  `jbp_name` varchar(255) NOT NULL,
  PRIMARY KEY  (`jbp_uid`,`jbp_name`),
  KEY `FK93CC461066F4DA65` (`jbp_uid`),
  CONSTRAINT `FK93CC461066F4DA65` FOREIGN KEY (`jbp_uid`) REFERENCES `jbp_users` (`jbp_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
LOAD DATA LOCAL INFILE '/tmp/jbp_roles.txt' INTO TABLE jbp_roles FIELDS TERMINATED BY ',' LINES TERMINATED BY '\n' (jbp_rid, jbp_name, jbp_displayname);
LOAD DATA LOCAL INFILE '/tmp/jbp_users.txt' INTO TABLE jbp_users FIELDS TERMINATED BY ',' LINES TERMINATED BY '\n' (jbp_uid, jbp_uname, jbp_givenname, jbp_familyname, jbp_password, jbp_realemail, jbp_fakeemail, jbp_regdate, jbp_viewrealemail, jbp_enabled);
LOAD DATA LOCAL INFILE '/tmp/jbp_role_membership.txt' INTO TABLE jbp_role_membership FIELDS TERMINATED BY ',' LINES TERMINATED BY '\n' (jbp_uid, jbp_rid);
LOAD DATA LOCAL INFILE '/tmp/jbp_user_prop.txt' INTO TABLE jbp_user_prop FIELDS TERMINATED BY ',' LINES TERMINATED BY '\n' (jbp_uid, jbp_value, jbp_name);
