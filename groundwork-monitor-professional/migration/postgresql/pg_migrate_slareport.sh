#!/bin/bash

/usr/local/groundwork/foundation/container/rstools/php/bsmCheck/protected/yiic migrate --interactive=0

chown -R nagios.nagios /usr/local/groundwork/foundation/container/rstools
chmod -R 755 /usr/local/groundwork/foundation/container/rstools
