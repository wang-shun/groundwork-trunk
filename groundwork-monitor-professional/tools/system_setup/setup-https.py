#!/usr/local/groundwork/python/bin/python
# -*- coding: utf-8 -*-
"""Tool to drive automated setup of https for Groundwork Monitor Enterprise."""

from __future__ import print_function
import argparse
import ConfigParser
import StringIO
import shutil
import yaml
import os
import stat
import subprocess
import logging
import sys
import socket
import time
import pwd
import grp


def execute(cmd, path=os.getcwd()):
    """Execute popen."""
    logger = logging.getLogger(__name__)
    logger.info('Execute: ' + cmd)
    try:
        popen = subprocess.Popen(cmd,
                                 shell=True,
                                 cwd=path,
                                 stdout=subprocess.PIPE,
                                 stderr=subprocess.PIPE,
                                 universal_newlines=True)
        for stdout_line in iter(popen.stdout.readline, ""):
            logger.info(stdout_line)
        popen.stdout.close()
        for stderr_line in iter(popen.stderr.readline, ""):
            logger.warning(stderr_line)
        popen.stderr.close()
        return_code = popen.wait()
        if return_code:
            raise subprocess.CalledProcessError(return_code, cmd)
    except subprocess.CalledProcessError:
        logger.error("Unexpected error:", sys.exc_info()[0])
        raise


def get_config(path):
    """Load yaml values from extra-vars.yml."""
    with open(path, 'rt') as f:
        data = f.read()
        return(yaml.load(data))


def certname(filename):
    """Parse a path to a certificate or key and return the filename without path or extension."""
    return os.path.splitext(os.path.basename(filename))[0]


def read_properties_file(path):
    """Read a java properties file and return as a dict.

    Arguments:
        path {[string]} -- [path to java properties file]

    Returns:
        [dict] -- [a dict of key/values from the properties file]
    """
    properties = {}
    if os.path.isfile(path):
        with open(path) as f:
            config = StringIO.StringIO()
            config.write('[dummy]\n')
            config.write(f.read().replace('%', '%%'))
            config.seek(0, os.SEEK_SET)
            cp = ConfigParser.SafeConfigParser()
            cp.readfp(config)
            properties = dict(cp.items('dummy'))
    return properties


def write_properties_file(new_properties, path):
    """Write a dict to a java properties file.

    Arguments:
        properties {[dict] -- [a dict of properties to write to the properties file]}
        path {[string]} -- [path to java properties file]
    """
    properties = read_properties_file(path)
    properties.update(new_properties)
    with open(path, 'wt') as f:
        for key, value in properties.items():
            f.write("%s=%s\n" % (key, value))


def get_hostname(prop_file):
    """Return the currently configured hostname.
    Check properties file if present.  Fall back to socket.getfqdn().

    Arguments:
        prop_file {[filepath]} -- [path to groundwork.properties file]
    Returns:
        [string] -- [the configured hostname for the system]
    """
    gw_properties = {}
    hostname = None
    if os.path.isfile(prop_file):
        gw_properties = read_properties_file(prop_file)
        if gw_properties['hostname'] is not None:
            if gw_properties['hostname'] != '':
                hostname = gw_properties['hostname']
    if hostname is None:
        hostname = socket.getfqdn()
    return hostname


groundwork_dir = '/usr/local/groundwork/'
system_setup_dir = groundwork_dir + 'tools/system_setup/'
log_dir = system_setup_dir + 'log/'
log_file = log_dir + 'setup-https.log'
backup_file = groundwork_dir + 'backup/%s-pre-https-config-backup.tgz' % time.strftime('%Y-%m-%d-%H%M')

def main():
    openssl_dir = groundwork_dir + 'common/openssl/'
    openssl_cert_dir = openssl_dir + 'certs/'
    openssl_priv_dir = openssl_dir + 'private/'
    jpp2_standalone = groundwork_dir + 'foundation/container/jpp2/standalone/configuration/standalone.xml'
    groundwork_prop_file = groundwork_dir + 'config/groundwork.properties'
    ansible_pb_bin = groundwork_dir + 'python/bin/ansible-playbook'
    ansible_pb_inventory = system_setup_dir + 'inventory.local'
    ansible_pb_file = system_setup_dir + 'config-https-playbook.yml'
    ansible_pb_ev_str = ''
    ansible_pb_flags = '-v'
    extra_vars = {}

    root_uid = pwd.getpwnam("root").pw_uid
    nagios_uid = pwd.getpwnam("nagios").pw_uid
    nagios_gid = grp.getgrnam("nagios").gr_gid
    cert_mode = stat.S_IRUSR | stat.S_IWUSR | stat.S_IRGRP | stat.S_IROTH
    key_mode = stat.S_IRUSR | stat.S_IWUSR | stat.S_IRGRP

    # make sure log directory exists
    if not os.path.exists(log_dir):
        os.makedirs(log_dir)
        os.chown(log_dir, nagios_uid, nagios_gid)

    # setup up the logger --info and --debug will adjust what is printed to the screen
    simple_formatter = logging.Formatter('%(levelname)s: %(message)s')
    detail_formatter = logging.Formatter('%(asctime)s - %(levelname)s: %(message)s')
    logger = logging.getLogger(__name__)
    logger.setLevel(logging.DEBUG)
    consolelogger = logging.StreamHandler()
    consolelogger.setLevel(logging.ERROR)
    consolelogger.setFormatter(simple_formatter)
    logger.addHandler(consolelogger)

    filelogger = logging.FileHandler(log_file)
    filelogger.setLevel(logging.DEBUG)
    filelogger.setFormatter(detail_formatter)
    logger.addHandler(filelogger)

    # verify that the dependencies of this script exist on the system
    if not os.path.isdir(system_setup_dir):
        logging.error('%s is not a valid directory. missing dependencies required to continue.' % system_setup_dir)
        exit(1)
    if os.getuid() != root_uid:
        logging.error('This script needs to be run as root or via sudo.')
        exit(1)

    parser = argparse.ArgumentParser(description='Tool to drive automated setup of https for Groundwork Monitor Enterprise.\nSettings taken from extra-vars.yml if present. Flags take precedence.')
    parser.add_argument('--create_certs', action='store_true',
                        help='generate self signed certificates, (default)')
    parser.add_argument('--redirect', action='store_true',
                        help='listen on port 80 to redirect to port 443.\n if neither --redirect nor --noredirect is specified. --redirect is assumed.')
    parser.add_argument('--noredirect', action='store_true',
                        help='do not listen on port 80 to redirect to port 443')
    parser.add_argument('--certfile', help='path to user supplied certificate')
    parser.add_argument('--certkey', help='path to user supplied key')
    parser.add_argument('--certca', help='path to user supplied ca certificate')
    parser.add_argument('--servername',
                        help='servername if different than discovered hostname')
    parser.add_argument('--josso_servername',
                        help='servername for josso auth if different than localhost:8888')
    parser.add_argument('--java_keystore_pass',
                        help='keystore password if different than default, (default: changeit)')
    parser.add_argument('--db_host',
                        help='host where database is setup, (default: localhost)')
    parser.add_argument('--db_host_idm',
                        help='the host to connect to for the JBoss IDM database. to be used on a cacti child server with a value set to the parent hostname, (default: same value as db_host)')
    parser.add_argument('--extra_vars_file',
                        default='/usr/local/groundwork/tools/system_setup/extra-vars.yml',
                        help='path to extra-vars.yml file if different than default')
    parser.add_argument('--save', action='store_true',
                        help='only update the extra-vars.yml file and exit')
    parser.add_argument('--print', dest='print_and_exit', action='store_true',
                        help='print the content of the extra-vars.yml file and exit')
    parser.add_argument('--purge_extra_vars', action='store_true',
                        help='delete extra-vars.yml file and exit')
    # TODO add support for force option
    # parser.add_argument('--force', action='store_true',
    #                     help='recreate certificates and apply https configuration even if already setup')
    parser.add_argument('--info', action='store_true',
                        help='set log level to INFO')
    parser.add_argument('--debug', action='store_true',
                        help='set log level to DEBUG')
    args = parser.parse_args()

    if args.info:
        consolelogger.setLevel(logging.INFO)
        ansible_pb_flags = '-vv'

    if args.debug:
        filelogger.setLevel(logging.DEBUG)
        consolelogger.setLevel(logging.DEBUG)
        consolelogger.setFormatter(detail_formatter)
        ansible_pb_flags = '-vvv'

    logger.debug('Arguments passed: ')
    logger.debug(vars(args))

    extra_vars_file = args.extra_vars_file

    if args.purge_extra_vars:
        if not os.path.isfile(extra_vars_file):
            logger.error('--purge_extra_vars selected but extra-vars.yml does not exist')
            exit(1)
        os.remove(extra_vars_file)
        print(extra_vars_file + ' deleted')
        exit(0)

    # load previously saved values from extra-vars.yml
    if os.path.isfile(extra_vars_file):
        extra_vars = get_config(extra_vars_file)
        logger.debug('Values read from ' + extra_vars_file + ': ')
        logger.debug(extra_vars)

    if args.print_and_exit:
        if not extra_vars:
            logger.error("--print selected but extra-vars.yml is empty.")
            exit(1)
        logger.info("Values from extra-vars.yml:")
        print(yaml.dump(extra_vars, default_flow_style=False))
        exit(0)

    if args.redirect:
        extra_vars['redirect'] = True

    if args.noredirect:
        if args.redirect:
            logging.error('Cannot specify both --redirect and --nodirect at the same time')
            exit(1)
        extra_vars['redirect'] = False

    # set servername to configured hostname, dsicovered fqdn, or name passed in --servername flag
    servername = get_hostname(groundwork_prop_file)
    if args.servername is not None:
        if args.servername != servername:
            logger.debug('%s is different than %s' % (args.servername, servername))
            servername = args.servername
            extra_vars['servername'] = args.servername
            extra_vars['use_cname'] = True

    if args.josso_servername is not None:
        extra_vars['jossoservername'] = args.josso_servername
        extra_vars['jossoendpoint'] = args.josso_servername

    if args.java_keystore_pass is not None:
        extra_vars['java_keystore_pass'] = args.java_keystore_pass

    if args.db_host is not None:
        extra_vars['db_host'] = args.db_host

    if args.db_host_idm is not None:
        extra_vars['db_host_idm'] = args.db_host_idm

    # if args.force is False:
    #     extra_vars['force'] = args.force

    # only backup the jpp2 standlone.xml if it exists (if dual jboss already installed)
    jpp2_file = ''
    if os.path.isfile(jpp2_standalone):
        jpp2_file = jpp2_standalone

    # create backup file
    if args.save is False:
        print('Backing up config files to %s.' % backup_file)
        cmd = """tar czvf %s --ignore-failed-read \
        /usr/local/groundwork/apache2/conf/httpd.conf \
        /usr/local/groundwork/apache2/conf/extra/httpd-ssl.conf \
        /usr/local/groundwork/apache2/conf/groundwork/foundation-ui.conf \
        /usr/local/groundwork/apache2/conf/groundwork/apache2-noma.conf \
        /usr/local/groundwork/config/cacti.properties \
        /usr/local/groundwork/config/foundation.properties \
        /usr/local/groundwork/config/groundwork.properties \
        /usr/local/groundwork/config/influxdb.properties \
        /usr/local/groundwork/config/ntop.properties \
        /usr/local/groundwork/config/status-viewer.properties \
        /usr/local/groundwork/config/report-viewer.properties \
        /usr/local/groundwork/config/cloudhub/cloudhub-*.xml \
        /usr/local/groundwork/grafana/conf/defaults.ini \
        /usr/local/groundwork/influxdb/etc/influxdb.conf \
        /usr/local/groundwork/noma/etc/NoMa.yaml \
        /usr/local/groundwork/foundation/container/jpp/standalone/configuration/gatein/configuration.properties \
        /usr/local/groundwork/foundation/container/jpp/modules/org/josso/generic-ee/agent/main/josso-agent-config.xml \
        /usr/local/groundwork/foundation/container/josso-1.8.4/conf/server.xml \
        /usr/local/groundwork/foundation/container/jpp/standalone/configuration/standalone.xml \
        /usr/local/groundwork/foundation/container/jpp/dual-jboss-installer/standalone.xml \
        /usr/local/groundwork/foundation/container/jpp/dual-jboss-installer/standalone2.xml \
        %s""" % (backup_file, jpp2_file)
        execute(cmd, groundwork_dir)

    if args.certkey is not None:
        if args.certfile is None:
            logger.error('certfile is require if certkey is specified')
            exit(1)
        if not os.path.isfile(args.certkey):
            logger.error('file ' + args.certkey + ' does not exist')
            exit(1)
        src = os.path.normpath(args.certkey)
        dest = os.path.normpath('%s/%s.key' % (openssl_priv_dir, servername))
        if src == dest:
            logger.warning('Source and destination file for cert key are the same. Not Copying.')
        else:
            logger.info('Copying %s to %s.' % (src, dest))
            shutil.copy(src, dest)
        os.chown(dest, root_uid, nagios_gid)
        os.chmod(dest, key_mode)

    if args.certca is not None:
        if args.certfile is None:
            logger.error('certfile is require if certa is specified')
            exit(1)
        if not os.path.isfile(args.certca):
            logger.error('file ' + args.certca + ' does not exist')
            exit(1)
        src = os.path.normpath(args.certca)
        dest = os.path.normpath('%s/%s.pem' % (openssl_cert_dir, certname(args.certca)))
        if src == dest:
            logger.warning('Source and destination file for cacert are the same. Not Copying.')
        else:
            logger.info('Copying %s to %s.' % (src, dest))
            shutil.copy(src, dest)
        os.chown(dest, root_uid, nagios_gid)
        os.chmod(dest, cert_mode)
        extra_vars['caname'] = certname(args.certca)
        extra_vars['cacert'] = True

    if args.certfile is not None:
        if args.create_certs:
            logger.error('cannot create certs and specify custom certificate')
            exit(1)
        if args.certkey is None:
            logger.error('certkey is require if certfile is specified')
            exit(1)
        if not os.path.isfile(args.certfile):
            logger.error('file ' + args.certfile + ' does not exist')
            exit(1)
        src = os.path.normpath(args.certfile)
        dest = os.path.normpath('%s/%s.pem' % (openssl_cert_dir, servername))
        if src == dest:
            logger.warning('Source and destination file for cert are the same. Not Copying.')
        else:
            logger.info('Copying %s to %s.' % (src, dest))
            shutil.copy(src, dest)
        os.chown(dest, root_uid, nagios_gid)
        os.chmod(dest, cert_mode)
        cmd = groundwork_dir + 'common/bin/c_rehash'
        execute(cmd, openssl_cert_dir)
        extra_vars['create_certs'] = False

    # write config to extra-vars for
    if extra_vars:
        with open(extra_vars_file, 'wt') as f:
            yaml.dump(extra_vars, f, default_flow_style=False)
        write_properties_file(extra_vars, groundwork_prop_file)
        ansible_pb_ev_str = '--extra-vars=@' + extra_vars_file
        logger.debug('Values stored in ' + extra_vars_file + ':')
        logger.debug(extra_vars)

    if args.save:
        if not os.path.isfile(extra_vars_file):
            logger.error('nothing to save')
            exit(1)
        print('%s updated successfully' % extra_vars_file)
        exit(0)

    print('Running ansible-playbook.')
    cmd = ('%s %s -i %s %s %s' % (ansible_pb_bin,
                                ansible_pb_flags,
                                ansible_pb_inventory,
                                ansible_pb_ev_str,
                                ansible_pb_file))
    execute(cmd, system_setup_dir)


if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        print("""
There was a problem configuring https.

Please run the diagnostic script and submit a support ticket:
https://kb.gwos.com/display/SUPPORT/Running+the+gwdiags+diagnostic+tool

The steps to revert this partially configured state can be found here:
https://kb.gwos.com/display/DOC/How+to+enable+HTTPS#HowtoenableHTTPS-Revertingtopreviousconfiguration

The backup file mentioned in that document can be found here:
%s

The log file for this script can be found here and is included when you run the diagnostic script:
%s
""" % (backup_file,log_file))
        exit(1)
    else:
        print('Groundwork Monitor Enterprise is now configured for https.')
        exit(0)

