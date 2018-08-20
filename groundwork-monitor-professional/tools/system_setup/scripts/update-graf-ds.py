#!/usr/local/groundwork/python/bin/python
# -*- coding: utf-8 -*-
"""Fix Grafana when grafbridge-control can't reach the api endpoint."""

import sqlite3
import json
import re
import argparse
import logging
import socket
from collections import OrderedDict
from datetime import datetime


def create_connection(db_file):
    """
    Create a database connection to the SQLite database specified by the db_file.

    :param db_file: database file
    :return: Connection object or None
    """
    try:
        conn = sqlite3.connect(db_file)
        return conn
    except Exception as e:
        raise e

    return None


def exec_query(conn, sql, params=()):
    """
    Run sqlite query.

    :param conn: Connection object
    :param sql: SQL Query to run
    :param params: Optional tuple of query parameters.
    :return: Query handler
    """
    c = conn.cursor()
    if params == ():
        return c.execute(sql)
    else:
        return c.execute(sql, params)


def get_data_sources(conn):
    """
    Get Datasources.

    :param conn: Connection object
    :return: Query handler
    """
    sql = "SELECT id,json_data FROM data_source WHERE type = 'groundwork';"
    with conn:
        return exec_query(conn, sql)


def update_data_source(conn, params):
    """
    Update Data Sources.

    :param conn: Connection object
    :param sql: SQL Query to run
    :param params: Optional tuple of query parameters.
    :return: Query handler
    """
    sql = "UPDATE data_source SET json_data = ?, updated = ? WHERE id = ?;"
    with conn:
        return exec_query(conn, sql, params)


def decode_db_json(encoded_json):
    """
    Take json from db record and return json object.

    :param encoded_json: raw buffer from sqlite field containing json
    :return: utf8 encoded json object
    """
    return json.loads(str(encoded_json).encode('utf8'), object_pairs_hook=OrderedDict)


def encode_db_json(json_obj):
    """
    Take json object and converts it back to string for sqlite db.

    :param json_obj: standard json object
    :return: json string with stripped whitespace, compatible with grafana data_sources table
    """
    return json.dumps(json_obj, separators=(',', ':'))


def main():
    """Main."""
    tool_desc = """
    Fix Grafana when grafbridge-control can't reach the api endpoint.
    """

    grafanadb = '/usr/local/groundwork/grafana/data/grafana.db'
    updated_date = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    protocol = ''
    servername = ''
    username = ''
    password = ''
    url_re = re.compile('(https?)\:\/\/(.+)')

    simple_formatter = logging.Formatter('%(levelname)s: %(message)s')
    detail_formatter = logging.Formatter('%(asctime)s - %(levelname)s: %(message)s')
    logger = logging.getLogger(__name__)
    logger.setLevel(logging.DEBUG)
    consolelogger = logging.StreamHandler()
    consolelogger.setLevel(logging.WARNING)
    consolelogger.setFormatter(simple_formatter)
    logger.addHandler(consolelogger)

    parser = argparse.ArgumentParser(description=tool_desc)
    parser.add_argument('--protocol',
                        help='http or https?')
    parser.add_argument('--servername',
                        help='servername if different than discovered FQDN')
    parser.add_argument('--username',
                        help='api username')
    parser.add_argument('--password',
                        help='encoded api password')
    parser.add_argument('--grafanadb',
                        help='path to grafanadb')
    parser.add_argument('--print', dest='print_and_exit', action='store_true',
                        help='print the proposed changes and exit with out making changes (noop)')
    # parser.add_argument('--info', action='store_true',
    #                     help='set log level to INFO')
    parser.add_argument('--debug', action='store_true',
                        help='set log level to DEBUG')
    args = parser.parse_args()

    if args.debug:
        consolelogger.setLevel(logging.DEBUG)
        consolelogger.setFormatter(detail_formatter)

    if args.grafanadb is not None:
        logger.debug('Using %s as grafanadb instead of default' % args.grafanadb)
        grafanadb = args.grafanadb

    if args.protocol is not None:
        if args.protocol in ['http', 'https']:
            protocol = args.protocol
        else:
            logger.error('Invalid protocol %s. Please use "http" or "https".' % protocol)
            exit(1)
    else:
        protocol = r"\1"

    if args.servername is not None:
        if args.servername != servername:
            logger.debug('%s is different than %s' % (args.servername, servername))
            servername = args.servername
        if servername != socket.getfqdn():
            logger.warn('servername: %s is different than the system FQDN: %s' % (servername, socket.getfqdn()))
    else:
        servername = r"\2"

    conn = create_connection(grafanadb)
    with conn:
        try:
            results = get_data_sources(conn)
            rows = results.fetchall()
        except Exception as e:
            logger.error("Problem reading from sqlite db: %s" % grafanadb)
            raise e

    for row in rows:
        data_source_id = row[0]
        json_data = decode_db_json(row[1])

        logger.debug('Read Query Results:')
        logger.debug('id, json_data')
        logger.debug("%s, '%s'" % (data_source_id, json.dumps(json_data)))

        new_url = url_re.sub(r"%s://%s" % (protocol, servername), json_data['url'])
        if new_url != json_data['url']:
            logger.debug('url will be set to %s' % new_url)
            json_data['url'] = new_url
        else:
            logger.debug('no change to url')

        if args.username is not None:
            username = args.username
            if json_data['username'] != username:
                logger.debug('username will be set to %s' % username)
                json_data['username'] = username
            else:
                logger.debug('username flag set but no change to username')

        if args.password is not None:
            password = args.password
            if json_data['password'] != password:
                logger.debug('password will be set to %s' % password)
                json_data['password'] = password
            else:
                logger.debug('password flag set but no change to password')

        json_data_str = encode_db_json(json_data)

        logger.debug('Data to update db:')
        logger.debug('id|json_data|updated')
        logger.debug("%s|%s|%s" % (data_source_id, json_data_str, updated_date))

        if args.print_and_exit:
            print('id|json_data')
            print("%s|%s" % (data_source_id, json_data_str))
        else:
            with conn:
                try:
                    results = update_data_source(conn, (json_data_str, updated_date, data_source_id))
                except Exception as e:
                    logger.error("Problem updating sqlite db: %s." % grafanadb)
                    raise e

    conn.close()
    exit()


if __name__ == '__main__':
    main()
