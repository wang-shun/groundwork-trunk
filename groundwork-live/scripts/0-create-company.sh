#!/bin/bash 

if [ $1 == "" ] ; then
echo "missing required company name as argument"
exit
fi
`cp -Ra ../input/PROSPECT ../input/$1`
`mv ../input/$1/PROSPECT.xls ../input/$1/$1.xls`
echo "created $1 as a new set of files in the input tree"
echo "download $1.xls from the $1 directory and make changes in the ALLGROUPS sheet to reflect your customer needs"
