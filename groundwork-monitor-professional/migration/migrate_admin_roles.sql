use jbossportal;
update ignore JBP_INSTANCE_SECURITY set ROLE='GWAdmin' where ROLE='Admin';
update ignore JBP_OBJECT_NODE_SEC set ROLE='GWAdmin' where ROLE='Admin';
update ignore jbp_roles set jbp_name='GWAdmin' where jbp_name='Admin';
update ignore JBP_INSTANCE_SECURITY set ROLE='GWOperator' where ROLE='Operator';
update ignore JBP_OBJECT_NODE_SEC set ROLE='GWOperator' where ROLE='Operator';
update ignore jbp_roles set jbp_name='GWOperator' where jbp_name='Operator';
update ignore JBP_INSTANCE_SECURITY set ROLE='GWUser' where ROLE='User';
update ignore JBP_OBJECT_NODE_SEC set ROLE='GWUser' where ROLE='User';
update ignore jbp_roles set jbp_name='GWUser' where jbp_name='User';
insert ignore into jbp_roles (jbp_name, jbp_displayname) values('msp-sample','Msp sample role');
insert ignore into jbp_roles (jbp_name, jbp_displayname) values('ro-dashboard','Read Only links in dashboard');
insert ignore into jbp_roles (jbp_name, jbp_displayname) values('wsuser','Webservice user');
create table if not exists gw_ext_role_attributes (
  jbp_rid bigint(20) NOT NULL,
  jbp_name varchar(255) default NULL,
  dashboard_links_disabled bit(1) default NULL,
  hg_list varchar(255),
  sg_list varchar(255),
  defaultHostGroup varchar(255) default NULL,
  defaultServiceGroup varchar(255) default NULL,
  restrictionType varchar(255) default NULL,
  PRIMARY KEY  (jbp_rid)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;	
alter table gw_ext_role_attributes modify hg_list text;
alter table gw_ext_role_attributes modify sg_list text;
insert ignore into jbp_users (jbp_uname, jbp_password, jbp_realemail, jbp_regdate, jbp_viewrealemail, jbp_enabled) values ('wsuser', MD5('wsuser'), 'portal@example.com', NOW(), '1', '1');
insert ignore into jbp_role_membership (jbp_uid, jbp_rid) values ((select jbp_uid from jbp_users where jbp_uname='wsuser'), (select jbp_rid from jbp_roles where jbp_name='wsuser'));
update ignore jbp_users set jbp_givenname='System Account',jbp_familyname='DO NOT disable/delete this user! If you change the password, don''t forget to update the /usr/local/groundwork/config/ws_client.properties file!' where jbp_uname='wsuser';
