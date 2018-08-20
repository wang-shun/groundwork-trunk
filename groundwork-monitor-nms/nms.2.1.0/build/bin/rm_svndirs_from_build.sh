#!/bin/bash

source ./error_handling.sh

find $NMSDIR -name ".svn" -exec rm -rf {} \; -prune
find $GWDIR/enterprise -name ".svn" -exec rm -rf {} \; -prune
