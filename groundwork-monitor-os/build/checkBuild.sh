#!/bin/bash
# description: monitor-core build script
#
# Copyright 2007 GroundWork Open Source, Inc. (GroundWork)
# All rights reserved. This program is free software; you can redistribute it
# and/or modify it under the terms of the GNU General Public License version 2
# as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
# more details.
#
# You should have received a copy of the GNU General Public License along with
# this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
# Street, Fifth Floor, Boston, MA 02110-1301, USA.
#

LOG_DIR=$PWD/log
log_files=($(ls $LOG_DIR))

cd $LOG_DIR
n=0 #Reset counter
m=0 #Reset counter
p=0 #Reset counter

for i in ${log_files[@]}; do
  fgrep -in "[ERROR] Result:" ${log_files[n]}
  if [ $? -eq 0 ] ; then
    filter=1
    fgrep -in --after-context=2 "[ERROR] Result:" ${log_files[n]} | grep "gcc -o sadc.o -c -g -O2 -Wall -Wstrict-prototypes -pipe -O2 -fno-strength-reduce"
    if [ $? -eq 0 ] ; then
      filter=0
    fi
    fgrep -in --before-context=3 "[ERROR] Result:" ${log_files[n]} | grep "../../configure.tlc8.64"
    if [ $? -eq 0 ] ; then
      filter=0
    fi
    fgrep -in --after-context=3 "[ERROR] Result:" ${log_files[n]} | grep "checking for working mkdir -p... yes"
    if [ $? -eq 0 ] ; then
      filter=0
    fi
    fgrep -in --before-context=3 "[ERROR] Result:" ${log_files[n]} | grep "Usage: autoconf [-h] [--help] [-m dir] [--macrodir=dir]"
    if [ $? -eq 0 ] ; then
      filter=0
    fi
    fgrep -in --before-context=3 "[ERROR] Result:" ${log_files[n]} | grep "/home/nagios/groundwork-monitor/monitor-core/apache/mnogosearch-3.2.39"
    if [ $? -eq 0 ] ; then
      filter=0
    fi
    fgrep -in --before-context=3 "[ERROR] Result:" ${log_files[n]} | grep "configure: warning: LDFLAGS=-L/usr/local/groundwork/lib"
    if [ $? -eq 0 ] ; then
      filter=0
    fi
    fgrep -in --before-context=3 failed ${log_files[n]} | grep GTK
    if [ $? -eq 0 ] ; then
      filter=0
    fi
    fgrep -in --before-context=3 failed ${log_files[n]} | grep "/home/nagios/groundwork-monitor/monitor-core/apache/mnogosearch-3.2.39"
    if [ $? -eq 0 ] ; then
      filter=0
    fi
    if [ $filter -eq 1 ] ; then
      echo ${log_files[n]} >> $LOG_DIR/errors.log
      fgrep -in --before-context=3 "[ERROR] Result:" ${log_files[n]} >> $LOG_DIR/errors.log
      echo "" >> $LOG_DIR/errors.log
    fi
  fi
  let n+=1
  fgrep -in FAILED ${log_files[m]}
  if [ $? -eq 0 ] ; then
    filter=1
    fgrep -in --before-context=3 failed ${log_files[m]} | grep "The can-we-talk-to-ourself test failed"
    if [ $? -eq 0 ] ; then
      filter=0
    fi
    fgrep -in --before-context=3 failed ${log_files[m]} | grep "or some tests failed! (Installing anyway.)"
    if [ $? -eq 0 ] ; then
      filter=0
    fi
    fgrep -in --before-context=3 failed ${log_files[m]} | grep "configure: warning: LDFLAGS=-L/usr/local/groundwork/lib"
    if [ $? -eq 0 ] ; then
      filter=0
    fi
    fgrep -in --before-context=3 failed ${log_files[m]} | grep GTK
    if [ $? -eq 0 ] ; then
      filter=0
    fi
    fgrep -in failed ${log_files[m]} | grep "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
    if [ $? -eq 0 ] ; then
      filter=0
    fi
    if [ $filter -eq 1 ] ; then
      echo ${log_files[m]} >> $LOG_DIR/errors.log
      fgrep -in --before-context=3 FAILED ${log_files[m]} >> $LOG_DIR/errors.log
      echo "" >> $LOG_DIR/errors.log
    fi
  fi
  let m+=1
  fgrep -in " undefined symbol" ${log_files[p]}
  if [ $? -eq 0 ] ; then
    echo ${log_files[p]} >> $LOG_DIR/errors.log
    fgrep -in --before-context=3 " undefined symbol" ${log_files[p]} >> $LOG_DIR/errors.log
    echo "" >> $LOG_DIR/errors.log
  fi
  let p+=1
done
cd ..
