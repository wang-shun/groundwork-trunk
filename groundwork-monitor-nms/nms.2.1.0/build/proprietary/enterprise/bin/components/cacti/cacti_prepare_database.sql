create user cactiuser;
GRANT ALL ON cacti.* TO cactiuser@localhost IDENTIFIED BY 'cactiuser';
flush privileges;
lock tables settings write;
insert into settings VALUES ('path_spine', '/usr/local/groundwork/nms/applications/cacti-spine/bin/spine');
insert into settings VALUES ('poller_type', '2');
unlock tables;

