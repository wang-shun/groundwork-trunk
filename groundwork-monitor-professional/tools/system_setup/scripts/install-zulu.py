#!/usr/local/groundwork/python/bin/python
# -*- coding: utf-8 -*-
"""
Switch to the Zulu OpenJDK from Oracle JDK in GroundWork.

This tool will setup a repository to install the zulu-7 Openjdk
package and switch Groundwork to use this instead of the embeded Oracle JDK.
"""

import os
import argparse
import subprocess
import logging
import sys
import yaml
import pwd
import grp


def execute(cmd, path=os.getcwd()):
    """Execute shell command."""
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
            logger.debug(stdout_line)
        popen.stdout.close()
        for stderr_line in iter(popen.stderr.readline, ""):
            logger.warning(stderr_line)
        popen.stderr.close()
        return_code = popen.wait()
        if return_code:
            raise subprocess.CalledProcessError(return_code, cmd)
    except subprocess.CalledProcessError:
        print("Unexpected error:", sys.exc_info()[0])
        raise


def get_config(path):
    """Parse yaml values from path."""
    with open(path, 'rt') as f:
        data = f.read()
        return(yaml.load(data))


def main():
    """Main program."""
    groundwork_dir = '/usr/local/groundwork/'
    system_setup_dir = groundwork_dir + 'tools/system_setup/'
    log_dir = system_setup_dir + 'log/'
    ansible_pb_bin = groundwork_dir + 'python/bin/ansible-playbook'
    ansible_pb_inventory = system_setup_dir + 'inventory.local'
    ansible_pb_file = system_setup_dir + 'zulu-jdk.yml'
    ansible_pb_ev_str = ''
    ansible_pb_flags = ''
    extra_vars = {}
    nagios_uid = pwd.getpwnam("nagios").pw_uid
    nagios_gid = grp.getgrnam("nagios").gr_gid

    # make sure log directory exists
    if not os.path.exists(log_dir):
        os.makedirs(log_dir)
        os.chown(log_dir, nagios_uid, nagios_gid)

    simple_formatter = logging.Formatter('%(levelname)s: %(message)s')
    detail_formatter = logging.Formatter('%(asctime)s - %(levelname)s: %(message)s')
    logger = logging.getLogger(__name__)
    logger.setLevel(logging.DEBUG)
    consolelogger = logging.StreamHandler()
    consolelogger.setLevel(logging.ERROR)
    consolelogger.setFormatter(simple_formatter)
    logger.addHandler(consolelogger)

    filelogger = logging.FileHandler(log_dir + 'install-zulu.log')
    filelogger.setLevel(logging.DEBUG)
    filelogger.setFormatter(detail_formatter)
    logger.addHandler(filelogger)

    if not os.path.isdir(system_setup_dir):
        logging.error('%s is not a valid directory. missing dependencies required to continue.' % system_setup_dir)
        exit(1)
    if os.getuid() != 0:
        logging.error('This script needs to be run as root or via sudo.')
        exit(1)

    parser = argparse.ArgumentParser(description='''
    Tool to switch from Oracle JDK to Zulu JDK for Groundwork Monitor Enterprise.
    Settings taken from extra-vars.yml if present. Flags take precedence.'''
                                     )
    parser.add_argument('--java_keystore_pass',
                        help='keystore password if different than default, (default: changeit)')
    parser.add_argument('--extra_vars_file',
                        default='/usr/local/groundwork/tools/system_setup/extra-vars.yml',
                        help='path to extra-vars.yml file if different than default')
    parser.add_argument('--save', action='store_true',
                        help='Only update the extra-vars.yml file and exit')
    parser.add_argument('--print', dest='print_and_exit', action='store_true',
                        help='print the content of the extra-vars.yml file and exit')
    parser.add_argument('--purge_extra_vars', action='store_true',
                        help='delete extra-vars.yml file and exit')
    parser.add_argument('--info', action='store_true',
                        help='set log level to INFO')
    parser.add_argument('--debug', action='store_true',
                        help='set log level to DEBUG')
    args = parser.parse_args()

    if args.info:
        consolelogger.setLevel(logging.INFO)
        ansible_pb_flags = '-v'

    if args.debug:
        filelogger.setLevel(logging.DEBUG)
        consolelogger.setLevel(logging.DEBUG)
        consolelogger.setFormatter(detail_formatter)
        ansible_pb_flags = '-vv'

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

    # write config to extra-vars for
    ansible_pb_ev_str = ''
    if extra_vars:
        with open(extra_vars_file, 'wt') as f:
            yaml.dump(extra_vars, f, default_flow_style=False)
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

    print('Groundwork Monitor Enterprise is now configured to use Zulu OpenJDK.')


if __name__ == '__main__':
    main()
