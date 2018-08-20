#!/bin/bash
#
# copy Icinga2 ca.crt file from /etc/icinga2/pki
#
rm -f icinga2-keystore.*
keytool -genkeypair -alias private -keyalg RSA -keysize 2048 -dname "CN=Unknown, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown" -keypass icinga2 -keystore icinga2-keystore.jks -storepass icinga2
keytool -importcert -trustcacerts -noprompt -alias root -file ca.crt -keystore icinga2-keystore.jks -storepass icinga2
keytool -list -keystore icinga2-keystore.jks -storepass icinga2
