--
-- Copyright 2013 RealStuff Informatik AG, ("RealStuff")
-- All rights reserved.
--

CREATE FUNCTION fn_migrate_slareport() RETURNS VOID AS $$
DECLARE
        currentSchemaVersion varchar;
        schematable varchar;
        prioritycount varchar;
        
BEGIN
    -- Query if infotable already exists
    SELECT relname INTO schematable FROM pg_class WHERE relname='schemainfo';
    IF schematable IS NULL THEN
        CREATE TABLE schemainfo (name character varying(254), value character varying(254));
        ALTER TABLE public.schemainfo OWNER TO slareport;
        
        currentSchemaVersion := '0.1';
        INSERT INTO schemainfo (name,value ) VALUES ('CurrentSchemaVersion',currentSchemaVersion);
        INSERT INTO schemainfo (name,value ) VALUES ('SchemaUpdated',now());
    END IF;
    
    -- Query if we still have version 0.1
    SELECT value INTO currentSchemaVersion FROM schemainfo WHERE name = 'CurrentSchemaVersion';
    IF currentSchemaVersion = '0.1' THEN
        ALTER TABLE "public"."calendar" ALTER COLUMN "idcalendar" TYPE int8;
        ALTER TABLE "public"."timevacationsdays" ALTER COLUMN "idtimevacationsdays" TYPE int8;
        ALTER TABLE "public"."timevacationsdaysonetime" ALTER COLUMN "idtimevacationsdaysonetime" TYPE int8;
        ALTER TABLE "public"."calendar_has_timevacationsdaysonetime" ALTER COLUMN "timevacationsdaysonetime_idtimevacationsdaysonetime" TYPE int8;
        ALTER TABLE "public"."calendar_has_timevacationsdaysonetime" ALTER COLUMN "calendar_idcalendar" TYPE int8;
        ALTER TABLE "public"."calendar_has_timevacationsdays" ALTER COLUMN "calendar_idcalendar" TYPE int8;
        ALTER TABLE "public"."calendar_has_timevacationsdays" ALTER COLUMN "timevacationsdays_idtimevacationsdays" TYPE int8;
        ALTER TABLE "public"."timeworkinghours" ALTER COLUMN "idtimeworkinghours" TYPE int8;
        ALTER TABLE "public"."sla" ALTER COLUMN "idsla" TYPE int8;
        ALTER TABLE "public"."sla" ALTER COLUMN "idcalendar" TYPE int8;
        ALTER TABLE "public"."sla" ALTER COLUMN "idtimeworkinghours" TYPE int8;
        ALTER TABLE "public"."object" ALTER COLUMN "idobject" TYPE int8;
        ALTER TABLE "public"."object" ALTER COLUMN "idsla" TYPE int8;
        ALTER TABLE "public"."downtime" ALTER COLUMN "iddowntime" TYPE int8;
        ALTER TABLE "public"."object_has_downtime" ALTER COLUMN "object_idobject" TYPE int8;
        ALTER TABLE "public"."object_has_downtime" ALTER COLUMN "downtime_iddowntime" TYPE int8;
        currentSchemaVersion := '0.2';
        UPDATE schemainfo SET value = currentSchemaVersion WHERE name='CurrentSchemaVersion';
        UPDATE schemainfo SET value = now() WHERE name='SchemaUpdated';
    END IF;
    
    -- Query if we still have version 0.2
    IF currentSchemaVersion = '0.2' THEN
        CREATE SEQUENCE group_idgroup_seq START WITH 1 INCREMENT BY 1 NO MAXVALUE NO MINVALUE CACHE 1;
        CREATE SEQUENCE host_idhost_seq START WITH 1 INCREMENT BY 1 NO MAXVALUE NO MINVALUE CACHE 1;
        CREATE SEQUENCE priority_idpriority_seq START WITH 1 INCREMENT BY 1 NO MAXVALUE NO MINVALUE CACHE 1;
        CREATE SEQUENCE service_idservice_seq START WITH 1 INCREMENT BY 1 NO MAXVALUE NO MINVALUE CACHE 1;
        CREATE SEQUENCE servicegroup_idservicegroup_seq START WITH 1 INCREMENT BY 1 NO MAXVALUE NO MINVALUE CACHE 1;
        CREATE TABLE "group" (
            description text NOT NULL,
            display text NOT NULL,
            note text,
            infotext text,
            infourl text,
            primarygroup boolean NOT NULL,
            idgroup bigint DEFAULT nextval('group_idgroup_seq'::regclass) NOT NULL,
            critical smallint DEFAULT 0,
            warning smallint DEFAULT 0,
            percentthresholds boolean,
            idpriority bigint NOT NULL,
            hostdefinition text,
            servicedefinition text
        );
        CREATE TABLE group_has_group (
            group_idgroup bigint NOT NULL,
            essential boolean DEFAULT false,
            idgroup_child_group bigint NOT NULL
        );
        CREATE TABLE group_has_host (
            group_idgroup bigint NOT NULL,
            essential boolean DEFAULT false,
            host_idhost bigint NOT NULL
        );
        CREATE TABLE group_has_service (
            group_idgroup bigint NOT NULL,
            essential boolean DEFAULT false,
            service_idservice bigint NOT NULL
        );
        CREATE TABLE group_has_servicegroup (
            group_idgroup bigint NOT NULL,
            essential boolean,
            servicegroup_idservicegroup bigint NOT NULL
        );
        CREATE TABLE "host" (
            idhost bigint DEFAULT nextval('host_idhost_seq'::regclass) NOT NULL,
            hostname text NOT NULL,
            hostid bigint NOT NULL
        );
        CREATE TABLE priority (
            idpriority bigint DEFAULT nextval('priority_idpriority_seq'::regclass) NOT NULL,
            description text NOT NULL
        );
        CREATE TABLE service (
            servicedescription text NOT NULL,
            servicestatusid bigint NOT NULL,
            idservice bigint DEFAULT nextval('service_idservice_seq'::regclass) NOT NULL,
            idhost bigint
        );
        CREATE TABLE servicegroup (
            idservicegroup bigint DEFAULT nextval('servicegroup_idservicegroup_seq'::regclass) NOT NULL,
            name character varying(255) NOT NULL
        );
        ALTER TABLE "group" OWNER TO slareport;
        ALTER TABLE group_has_group OWNER TO slareport;
        ALTER TABLE group_has_host OWNER TO slareport;
        ALTER TABLE group_has_service OWNER TO slareport;
        ALTER TABLE group_has_servicegroup OWNER TO slareport;
        ALTER TABLE "host" OWNER TO slareport;
        ALTER TABLE priority OWNER TO slareport;
        ALTER TABLE service OWNER TO slareport;
        ALTER TABLE servicegroup OWNER TO slareport;
        ALTER TABLE "group" ADD CONSTRAINT group_pkey PRIMARY KEY (idgroup);
        ALTER TABLE group_has_group ADD CONSTRAINT group_has_group_pkey PRIMARY KEY (group_idgroup, idgroup_child_group);
        ALTER TABLE group_has_host ADD CONSTRAINT group_has_host_pkey PRIMARY KEY (group_idgroup, host_idhost);
        ALTER TABLE group_has_service ADD CONSTRAINT group_has_service_pkey PRIMARY KEY (group_idgroup, service_idservice);
        ALTER TABLE group_has_servicegroup ADD CONSTRAINT group_has_servicegroup_pkey PRIMARY KEY (group_idgroup, servicegroup_idservicegroup);
        ALTER TABLE "host" ADD CONSTRAINT host_pkey PRIMARY KEY (idhost);
        ALTER TABLE priority ADD CONSTRAINT priority_pkey PRIMARY KEY (idpriority);
        ALTER TABLE service ADD CONSTRAINT service_pkey PRIMARY KEY (idservice);
        ALTER TABLE servicegroup ADD CONSTRAINT servicegroup_pkey PRIMARY KEY (idservicegroup);
        ALTER TABLE "group" ADD CONSTRAINT fk_group_priority_1 FOREIGN KEY (idpriority) REFERENCES priority(idpriority);
        ALTER TABLE group_has_group ADD CONSTRAINT fk_group_has_group_group_1 FOREIGN KEY (group_idgroup) REFERENCES "group"(idgroup);
        ALTER TABLE group_has_group ADD CONSTRAINT fk_group_has_group_group_2 FOREIGN KEY (idgroup_child_group) REFERENCES "group"(idgroup);
        ALTER TABLE group_has_host ADD CONSTRAINT fk_group_has_host_group_1 FOREIGN KEY (group_idgroup) REFERENCES "group"(idgroup);
        ALTER TABLE group_has_host ADD CONSTRAINT fk_group_has_host_host_1 FOREIGN KEY (host_idhost) REFERENCES host(idhost);
        ALTER TABLE group_has_service ADD CONSTRAINT fk_group_has_service_group_1 FOREIGN KEY (group_idgroup) REFERENCES "group"(idgroup);
        ALTER TABLE group_has_service ADD CONSTRAINT fk_group_has_service_service_1 FOREIGN KEY (service_idservice) REFERENCES service(idservice);
        ALTER TABLE group_has_servicegroup ADD CONSTRAINT fk_group_has_servicegroup_group_1 FOREIGN KEY (group_idgroup) REFERENCES "group"(idgroup);
        ALTER TABLE group_has_servicegroup ADD CONSTRAINT fk_group_has_servicegroup_servicegroup_1 FOREIGN KEY (servicegroup_idservicegroup) REFERENCES servicegroup(idservicegroup);
        ALTER TABLE service ADD CONSTRAINT fk_service_host_1 FOREIGN KEY (idhost) REFERENCES host(idhost);
        
        currentSchemaVersion := '0.3';
        UPDATE schemainfo SET value = currentSchemaVersion WHERE name='CurrentSchemaVersion';
        UPDATE schemainfo SET value = now() WHERE name='SchemaUpdated';
    END IF;
    
    -- Query if we still have version 0.3
    IF currentSchemaVersion = '0.3' THEN
        -- Fix for 7.0.0 there is an Issue with the schema version. 0.3 is alredy having priority values...
        SELECT count(*) INTO prioritycount FROM priority;
        IF prioritycount = '0' THEN
            INSERT INTO priority VALUES (1, 'High');
            INSERT INTO priority VALUES (2, 'Medium');
            INSERT INTO priority VALUES (3, 'Low');
        END IF;
        
        ALTER SEQUENCE group_idgroup_seq OWNER TO slareport;
        ALTER SEQUENCE host_idhost_seq OWNER TO slareport;
        ALTER SEQUENCE priority_idpriority_seq OWNER TO slareport;
        ALTER SEQUENCE service_idservice_seq OWNER TO slareport;
        ALTER SEQUENCE servicegroup_idservicegroup_seq OWNER TO slareport;
        
        currentSchemaVersion := '0.4';
        UPDATE schemainfo SET value = currentSchemaVersion WHERE name='CurrentSchemaVersion';
        UPDATE schemainfo SET value = now() WHERE name='SchemaUpdated';
    END IF;
    
    -- Query if we still have version 0.4
    IF currentSchemaVersion = '0.4' THEN
        ALTER TABLE "group" ADD COLUMN hostgroup text;
        ALTER TABLE "group" ADD COLUMN stateproperty text;
        UPDATE "group" SET hostgroup = 'BSM:Business Objects' WHERE hostgroup ISNULL AND primarygroup = TRUE;
        UPDATE "group" SET stateproperty = '{"UNSCHEDULED DOWN":{"weight":6,"count":0,"problem":1,"bsm":"UNSCHEDULED CRITICAL"},"UNSCHEDULED CRITICAL":{"weight":6,"count":0,"problem":1,"bsm":"UNSCHEDULED CRITICAL"},"ACKNOWLEDGED UNSCHEDULED CRITICAL":{"weight":5,"count":0,"problem":1,"bsm":"UNSCHEDULED CRITICAL"},"ACKNOWLEDGED UNSCHEDULED DOWN":{"weight":5,"count":0,"problem":1,"bsm":"UNSCHEDULED CRITICAL"},"PENDING":{"weight":4,"count":0,"problem":1,"bsm":"PENDING"},"UNKNOWN":{"weight":4,"count":0,"problem":1,"bsm":"UNKNOWN"},"ACKNOWLEDGED PENDING":{"weight":4,"count":0,"problem":1,"bsm":"PENDING"},"ACKNOWLEDGED UNKNOWN":{"weight":4,"count":0,"problem":1,"bsm":"UNKNOWN"},"SCHEDULED DOWN":{"weight":3,"count":0,"problem":1,"bsm":"SCHEDULED CRITICAL"},"SCHEDULED CRITICAL":{"weight":3,"count":0,"problem":1,"bsm":"SCHEDULED CRITICAL"},"ACKNOWLEDGED SCHEDULED CRITICAL":{"weight":2,"count":0,"problem":1,"bsm":"SCHEDULED CRITICAL"},"ACKNOWLEDGED SCHEDULED DOWN":{"weight":2,"count":0,"problem":1,"bsm":"SCHEDULED CRITICAL"},"WARNING":{"weight":1,"count":0,"problem":1,"bsm":"WARNING"},"ACKNOWLEDGED WARNING":{"weight":1,"count":0,"problem":1,"bsm":"WARNING"},"UP":{"weight":0,"count":0,"problem":0,"bsm":"OK"},"OK":{"weight":0,"count":0,"problem":0,"bsm":"OK"}}'
            WHERE stateproperty ISNULL;
        ALTER TABLE "group" ADD CONSTRAINT display_must_be_unique UNIQUE (display);
        
        currentSchemaVersion := '0.5';
        UPDATE schemainfo SET value = currentSchemaVersion WHERE name='CurrentSchemaVersion';
        UPDATE schemainfo SET value = now() WHERE name='SchemaUpdated';
    END IF;
    
    -- Query if we still have version 0.5
    IF currentSchemaVersion = '0.5' THEN
        CREATE TABLE audittrail (
            idaudittrail bigserial PRIMARY KEY,
            old_value text,
            new_value text,
            "action" varchar(255) NOT NULL,
            model varchar(255) NOT NULL,
            field varchar(255),
            stamp timestamp(6) NOT NULL,
            user_id varchar(255),
            model_id varchar(255) NOT NULL
        );
        CREATE TABLE downtimeschedule (
            iddowntimeschedule bigserial PRIMARY KEY,
            fixed bool,
            "host" text,
            service text,
            hostgroup text,
            servicegroup text,
            author text NOT NULL,
            description text NOT NULL,
            "start" timestamp(6) NOT NULL,
            "end" timestamp(6) NULL,
            duration int8,
            apptype text
        );
        CREATE TABLE downtimeactive (
            iddowntimeactive bigserial PRIMARY KEY,
            "start" timestamp(6) NOT NULL,
            "end" timestamp(6) NOT NULL,
            fk_iddowntimeschedule int8 NOT NULL,
            gwstuff text NOT NULL
        );
        CREATE TABLE downtimeschedulerepeat (
            iddowntimeschedulerepeat bigserial PRIMARY KEY,
            "year" varchar(4),
            "month" varchar(2),
            "day" varchar(2),
            "week" varchar(1),
            weekday_0 bool,
            weekday_1 bool,
            weekday_2 bool,
            weekday_3 bool,
            weekday_4 bool,
            weekday_5 bool,
            weekday_6 bool,
            "count" int2,
            "enddate" date,
            fk_iddowntimeschedule int8
        );
        CREATE TABLE excludedate (
            idexcludedate bigserial PRIMARY KEY,
            "date" date NOT NULL,
            "fk_iddowntimeschedulerepeat" int8 NOT NULL
        );
        ALTER TABLE ONLY downtimeschedulerepeat
	       ADD CONSTRAINT fk_iddowntimeschedule FOREIGN KEY (fk_iddowntimeschedule) REFERENCES downtimeschedule(iddowntimeschedule);
        ALTER TABLE ONLY downtimeactive
	       ADD CONSTRAINT fk_iddowntimeschedule FOREIGN KEY (fk_iddowntimeschedule) REFERENCES downtimeschedule(iddowntimeschedule);
        ALTER TABLE ONLY excludedate
	       ADD CONSTRAINT fk_iddowntimeschedulerepeat FOREIGN KEY (fk_iddowntimeschedulerepeat) REFERENCES downtimeschedulerepeat(iddowntimeschedulerepeat);
        ALTER TABLE downtimeschedulerepeat OWNER TO "slareport";
        ALTER TABLE audittrail OWNER TO "slareport";
        ALTER TABLE downtimeactive OWNER TO "slareport";
        ALTER TABLE excludedate OWNER TO "slareport";
        ALTER TABLE downtimeschedule OWNER TO "slareport";
        
        currentSchemaVersion := '0.6';
        UPDATE schemainfo SET value = currentSchemaVersion WHERE name='CurrentSchemaVersion';
        UPDATE schemainfo SET value = now() WHERE name='SchemaUpdated';
    END IF;
    
    -- Query if we still have version 0.6
    if currentSchemaVersion = '0.6' THEN
        CREATE TABLE "monitoredservice" (
            "idmonitoredservice" serial8 NOT NULL,
            "servicestatusid" int8 NOT NULL,
            "servicedescription" text,
            "info" text,
            "active" bool,
            PRIMARY KEY ("idmonitoredservice")
        );
        CREATE TABLE "monitoredservice_has_monitoredserviceclient" (
            "idmonitoredservice" serial8 NOT NULL,
            "idmonitoredserviceclient" int8 NOT NULL,
            PRIMARY KEY ("idmonitoredservice", "idmonitoredserviceclient")
        );
        CREATE TABLE "monitoredserviceclient" (
            "idmonitoredserviceclient" serial8 NOT NULL,
            "filename" varchar(256) NOT NULL,
            "name" text NOT NULL,
            "description" text NOT NULL,
            "transfer_method" text,
            "tm_ssh_prk" text,
            "tm_ssh_pub" text,
            "tm_user" text,
            "tm_pass" text,
            "tm_path" text,
            "tm_host" text,
            PRIMARY KEY ("idmonitoredserviceclient")
        );
        CREATE TABLE "monitoredservicecomment" (
            "idmonitoredservicecomment" serial8 NOT NULL,
            "timestamp" timestamp(6) NOT NULL,
            "message" text NOT NULL,
            "fk_idmonitoredservice" int8 NOT NULL,
            "active" bool,
            PRIMARY KEY ("idmonitoredservicecomment")
        );
        ALTER TABLE "monitoredservice_has_monitoredserviceclient"
            ADD CONSTRAINT "fk_idmonitoredservice" FOREIGN KEY ("idmonitoredservice") REFERENCES "monitoredservice" ("idmonitoredservice");
        ALTER TABLE "monitoredservice_has_monitoredserviceclient"
            ADD CONSTRAINT "fk_idmonitoredserviceclient" FOREIGN KEY ("idmonitoredserviceclient") REFERENCES "monitoredserviceclient" ("idmonitoredserviceclient") ON UPDATE NO ACTION ON DELETE CASCADE;
        ALTER TABLE "monitoredservicecomment"
            ADD CONSTRAINT "fk_idmonitoredservice" FOREIGN KEY ("fk_idmonitoredservice") REFERENCES "monitoredservice" ("idmonitoredservice");
        ALTER TABLE "monitoredservice" OWNER TO "slareport";
        ALTER TABLE "monitoredservice_has_monitoredserviceclient" OWNER TO "slareport";
        ALTER TABLE "monitoredserviceclient" OWNER TO "slareport";
        ALTER TABLE "monitoredservicecomment" OWNER TO "slareport";
        
        currentSchemaVersion := '0.7';
        UPDATE schemainfo SET value = currentSchemaVersion WHERE name='CurrentSchemaVersion';
        UPDATE schemainfo SET value = now() WHERE name='SchemaUpdated';
    END IF;
    
END;
$$ LANGUAGE plpgsql;

SELECT fn_migrate_slareport();

DROP FUNCTION IF EXISTS fn_migrate_slareport();

