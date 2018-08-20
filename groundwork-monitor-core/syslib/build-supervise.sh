#!/bin/sh

patch -p0 < ../daemontools-linux-patch
cd admin/daemontools-0.76/
./package/compile
