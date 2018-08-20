#!/bin/bash

if [ `uname` == 'Darwin' ]
then
    /Applications/Firefox.app/Contents/MacOS/firefox-bin -P watir -jssh
else
    echo Please edit $0 to add the firefox path for your system
fi
