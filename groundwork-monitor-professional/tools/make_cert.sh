#!/bin/bash

#
# Copyright 2017 GroundWork Inc. (GroundWork)
# All rights reserved.  Use is subject to GroundWork commercial license terms.
#
# make_cert.sh -- generate x509 certificates for use with Groundwork.

# uncomment set -x to debug
# set -x

while getopts h:d:e:b:n:c:s:l:o:if option
do
  case "${option}" in
    h) COMMONNAME="${OPTARG}";;
    d) DOMAIN="${OPTARG}";;
    e) DIGEST="${OPTARG}";;
    b) NUMBITS="${OPTARG}";;
    n) DAYS="${OPTARG}";;
    c) C="${OPTARG}";;
    s) ST="${OPTARG}";;
    l) L="${OPTARG}";;
    o) OU="${OPTARG}";;
    f) FORCE=true;;
  esac
done
shift $[$OPTIND-1]

: ${CA_NAME:="groundwork-ca"}
: ${COMMONNAME:="$(hostname -s)"}
: ${NUMBITS:="4096"}
: ${DAYS:="3650"}
: ${DIGEST:="sha256"}
: ${C:="US"}
: ${ST:="CA"}
: ${L:="San Francisco"}
: ${OU:="IT"}
: ${OPENSSL:="/usr/local/groundwork/common/bin/openssl"}
: ${OPENSSL_HOME:="/usr/local/groundwork/common/openssl"}
: ${C_REHASH:="/usr/local/groundwork/common/bin/c_rehash"}
: ${CRT_DIR:="${OPENSSL_HOME}/certs"}
: ${KEY_DIR:="${OPENSSL_HOME}/private"}
: ${CSR_DIR:="${OPENSSL_HOME}/csr"}

getname() {
  HOSTNAME="${1}"
  if [ -n "${DOMAIN}" ];then
    NAME="${HOSTNAME}.${DOMAIN}"
  else
    NAME="${HOSTNAME}"
  fi
  echo "${NAME}"
}

gencert() {
  CN=$(getname "${1}")
  CA=$(getname "${CA_NAME}")
  CA_CRT="${CRT_DIR}/${CA}.pem"
  CA_KEY="${KEY_DIR}/${CA}.key"
  KEY="${KEY_DIR}/${CN}.key"
  CSR="${CSR_DIR}/${CN}.csr"
  CRT="${CRT_DIR}/${CN}.pem"

  if [ "${CA}" == "${CN}" ]; then
    if [ ! -s "${CA_KEY}" ]; then
      "${OPENSSL}" genrsa -out "${CA_KEY}" "${NUMBITS}"
    fi
    if [ ! -s "${CA_CRT}" ]; then
      "${OPENSSL}" req -x509 -new -nodes -"${DIGEST}" -subj "/C=${C}/ST=${ST}/L=${L}/OU=${OU}/CN=${CA}" -key "${CA_KEY}" -days $((DAYS + 1)) -out "${CA_CRT}"
    fi
  else
    if [ ! -s "${KEY}" ] || [ ${FORCE} ]; then
      rm -f "${KEY}"
      "${OPENSSL}" genrsa -out "${KEY}" "${NUMBITS}"
    fi
    if [ ! -s "${CSR}" ] || [ ${FORCE} ]; then
      rm -f "${CSR}"
      "${OPENSSL}" req -new -nodes -"${DIGEST}" -subj "/C=${C}/ST=${ST}/L=${L}/OU=${OU}/CN=${CN}" -key "${KEY}" -out "${CSR}"
    fi
    if [ ! -s "${CRT}" ] || [ ${FORCE} ]; then
      rm -f "${CRT}"
      "${OPENSSL}" x509 -req -in "${CSR}" -extfile <(printf "subjectAltName=DNS:${CN}") -CA "${CA_CRT}" -CAkey "${CA_KEY}" -CAcreateserial -days $((DAYS / 2)) -out "${CRT}"
    fi
  fi
}

mkdir -p "${KEY_DIR}"
mkdir -p "${CSR_DIR}"
mkdir -p "${CRT_DIR}"

gencert "${CA_NAME}"
gencert "${COMMONNAME}"
"${C_REHASH}"
