#!/bin/sh

service="$1"
state="$2"
host="$3"
user="$4"
comment="$5"
nsca_host="$6"

message="$host;$service;$state;$user:$comment"

nsca_config=/usr/local/groundwork/common/etc/send_nsca.cfg

/bin/echo -e "$message" | /usr/local/groundwork/common/bin/send_nsca -H $nsca_host -d \; -c $nsca_config

