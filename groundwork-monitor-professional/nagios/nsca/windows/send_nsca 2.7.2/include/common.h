/************************************************************************
 *
 * COMMON.H - NSCA Common Include File
 * Copyright (c) 1999-2003 Ethan Galstad (nagios@nagios.org)
 * Last Modified: 01-07-2003
 *
 * License:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 ************************************************************************/

#include "config.h"
#include "windows_stdint.h"


#define PROGRAM_VERSION "2.7.2 (plus GroundWork extensions)"
#define MODIFICATION_DATE "08-05-2010"


#define OK		0
#define ERROR		-1

#define TRUE		1
#define FALSE		0

#define STATE_UNKNOWN  	3	/* service state return codes */
#define	STATE_CRITICAL 	2
#define STATE_WARNING 	1
#define STATE_OK       	0


#define DEFAULT_SOCKET_TIMEOUT	10	/* timeout after 10 seconds */

#define MAX_INPUT_BUFFER	2048	/* max size of most buffers we use */
#define MAX_WIDE_INPUT_BUFFER	5120	/* max size of most buffers we use, allowing for wide plugin output */

#define MAX_HOST_ADDRESS_LENGTH	256	/* max size of a host address */

#define MAX_HOSTNAME_LENGTH	64
#define MAX_DESCRIPTION_LENGTH	128
#define MAX_PLUGINOUTPUT_LENGTH	512
#define MAX_WIDE_PLUGINOUTPUT_LENGTH	4096
#define MAX_ALIGNMENT_PADDING_LENGTH	3	/* Max extra padding in a V101 packet, up to a 32-bit boundary */

/*
// This value should compute out to be zero, because all of the individual variable-length fields we are
// concatenating in the variable data packet payload already have lengths divisible by 4 (and thus the
// collection at the maximum length of all fields needs no additional bytes to align to a 32-bit boundary).
// We only define and use it as a reminder that the variable_data_packet.variable_data field may contain some
// extra padding in addition to the useful host name, service description, and plugin output payload fields.
*/
#define ALIGNMENT_PADDING_LENGTH	(3 - (MAX_HOSTNAME_LENGTH + MAX_DESCRIPTION_LENGTH + MAX_WIDE_PLUGINOUTPUT_LENGTH + 3) % 4)

#define MAX_PASSWORD_LENGTH     512

#define NUL_TERM_LEN		1	/* size of '\0' string termination byte */


/********************* ENCRYPTION TYPES ****************/

#define ENCRYPT_NONE            0       /* no encryption */
#define ENCRYPT_XOR             1       /* not really encrypted, just obfuscated */

#ifdef HAVE_LIBMCRYPT
#define ENCRYPT_DES             2       /* DES */
#define ENCRYPT_3DES            3       /* 3DES or Triple DES */
#define ENCRYPT_CAST128         4       /* CAST-128 */
#define ENCRYPT_CAST256         5       /* CAST-256 */
#define ENCRYPT_XTEA            6       /* xTEA */
#define ENCRYPT_3WAY            7       /* 3-WAY */
#define ENCRYPT_BLOWFISH        8       /* SKIPJACK */
#define ENCRYPT_TWOFISH         9       /* TWOFISH */
#define ENCRYPT_LOKI97          10      /* LOKI97 */
#define ENCRYPT_RC2             11      /* RC2 */
#define ENCRYPT_ARCFOUR         12      /* RC4 */
#define ENCRYPT_RC6             13      /* RC6 */            /* UNUSED */
#define ENCRYPT_RIJNDAEL128     14      /* RIJNDAEL-128 */
#define ENCRYPT_RIJNDAEL192     15      /* RIJNDAEL-192 */
#define ENCRYPT_RIJNDAEL256     16      /* RIJNDAEL-256 */
#define ENCRYPT_MARS            17      /* MARS */           /* UNUSED */
#define ENCRYPT_PANAMA          18      /* PANAMA */         /* UNUSED */
#define ENCRYPT_WAKE            19      /* WAKE */
#define ENCRYPT_SERPENT         20      /* SERPENT */
#define ENCRYPT_IDEA            21      /* IDEA */           /* UNUSED */
#define ENCRYPT_ENIGMA          22      /* ENIGMA (Unix crypt) */
#define ENCRYPT_GOST            23      /* GOST */
#define ENCRYPT_SAFER64         24      /* SAFER-sk64 */
#define ENCRYPT_SAFER128        25      /* SAFER-sk128 */
#define ENCRYPT_SAFERPLUS       26      /* SAFER+ */
#endif



/******************** MISC DEFINITIONS *****************/

#define TRANSMITTED_IV_SIZE     128     /* size of IV to transmit - must be as big as largest IV needed for any crypto algorithm */
#define snprintf		_snprintf

/*************** PACKET STRUCTURE DEFINITIONS **********/

#define NSCA_PACKET_VERSION_101	101		/* wide-packet version identifier */
#define NSCA_PACKET_VERSION_3	3		/* packet version identifier */
#define NSCA_PACKET_VERSION_2	2		/* older packet version identifiers */
#define NSCA_PACKET_VERSION_1	1

/* data packet containing service check results */
typedef struct data_packet_struct{
	int16_t   packet_version;
	u_int32_t crc32_value;
	u_int32_t timestamp;
	int16_t   return_code;
	char      host_name[MAX_HOSTNAME_LENGTH];
	char      svc_description[MAX_DESCRIPTION_LENGTH];
	char      plugin_output[MAX_PLUGINOUTPUT_LENGTH];
	}data_packet;

typedef struct fixed_data_subpacket_struct{
	int16_t   packet_version;
	u_int32_t crc32_value;
	u_int32_t timestamp;
	int16_t   return_code;
	uint16_t  host_name_size;		/* includes size of trailing NUL byte */
	uint16_t  svc_description_size;		/* includes size of trailing NUL byte */
	uint16_t  plugin_output_size;		/* includes size of trailing NUL byte */
	uint16_t  alignment_padding_size;	/* length of 0 to 3 extra bytes for overall packet 32-bit alignment */
	}fixed_data_subpacket;

typedef struct variable_data_packet_struct{
	fixed_data_subpacket	fixed_data;
	char			variable_data
	    [
	    MAX_HOSTNAME_LENGTH +
	    MAX_DESCRIPTION_LENGTH +
	    MAX_WIDE_PLUGINOUTPUT_LENGTH +
	    ALIGNMENT_PADDING_LENGTH
	    ];
	}variable_data_packet;

typedef struct wide_data_struct{
	variable_data_packet variable_packet;
	struct {
		char *host_name;		/* points to host_name       within variable_packet.variable_data */
		char *svc_description;		/* points to svc_description within variable_packet.variable_data */
		char *plugin_output;		/* points to plugin_output   within variable_packet.variable_data */
		char *alignment_padding;	/* might not point to anything, if alignment_padding_size is 0 */
		}wide_fields;
	}wide_data;  /* not a network-sendable wide_data_packet in itself, because it contains extra pointer fields */

#define	wide_packet_version		variable_packet.fixed_data.packet_version
#define	wide_crc32_value		variable_packet.fixed_data.crc32_value
#define	wide_timestamp			variable_packet.fixed_data.timestamp
#define	wide_return_code		variable_packet.fixed_data.return_code
#define	wide_host_name_size		variable_packet.fixed_data.host_name_size
#define	wide_svc_description_size	variable_packet.fixed_data.svc_description_size
#define	wide_plugin_output_size		variable_packet.fixed_data.plugin_output_size
#define	wide_alignment_padding_size	variable_packet.fixed_data.alignment_padding_size
#define	wide_variable_data		variable_packet.variable_data
#define	wide_host_name			wide_fields.host_name
#define	wide_svc_description		wide_fields.svc_description
#define	wide_plugin_output		wide_fields.plugin_output
#define	wide_alignment_padding		wide_fields.alignment_padding

/* initialization packet containing IV and timestamp */
typedef struct init_packet_struct{
	char      iv[TRANSMITTED_IV_SIZE];
	u_int32_t timestamp;
	}init_packet;




