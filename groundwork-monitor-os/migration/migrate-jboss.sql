create user jboss identified by 'jboss';

create database if not exists jbossdb;
grant all on jbossdb.* to 'jboss'@'localhost' identified by 'jboss';

create database if not exists jbossportal;
grant all on jbossportal.* to 'jboss'@'localhost' identified by 'jboss';
