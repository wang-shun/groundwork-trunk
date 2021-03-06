#!/bin/sh

# setenv-euca.sh

# Set up environment for using Amazon EC2 API tools with Eucalyptus

# Source Eucalyptus RC file generated by the Eucalyptus admin
if [ -f ~/euca2-admin/eucarc ]; then
	. ~/euca2-admin/eucarc
fi

if [ -d ~/ec2-api-tools-1.3-19403 ]; then
	export EC2_HOME=~/ec2-api-tools-1.3-19403
	export PATH=$PATH:$EC2_HOME/bin
fi


