/*
 *  bronx_listener_common.h
 *
 *  Copyright (c) 2007-2010 Groundwork Open Source
 *  Originally written by Daniel Emmanuel Feinsmith
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor
 *  Boston, MA  02110-1301, USA.
 *
 *  Change Log:
 *  Sep 17 2007 DEF	Initial creation.
 *  Jul 20 2010 GH	Added support for the version 101 packet,
 *			based on earlier work by HK.
 */

#ifndef _BRONX_LISTENER_COMMON_H
#define	_BRONX_LISTENER_COMMON_H

#include    "bronx_listener_defines.h"

#ifndef TRUE
#define TRUE                            1
#elif (TRUE!=1)
#define TRUE                            1
#endif
#ifndef FALSE
#define FALSE                           0
#elif (FALSE!=0)
#define FALSE                           0
#endif

#define STATE_UNKNOWN  	3	/* service state return codes */
#define	STATE_CRITICAL 	2
#define STATE_WARNING 	1
#define STATE_OK       	0

#define NSCA_DEFAULT_SOCKET_TIMEOUT		10	/* timeout after 10 seconds */
#define MAX_HOST_ADDRESS_LENGTH			256	/* max size of a host address */
#define MAX_NSCA_PLUGINOUTPUT_LENGTH_V3		512	/* Max plugin output in a V3 packet */
#define MAX_NSCA_PLUGINOUTPUT_LENGTH_V101	4096	/* Max plugin output in a V101 packet */
#define MAX_ALIGNMENT_PADDING_LENGTH_V101	3	/* Max extra padding in a V101 packet, up to a 32-bit boundary */
#define MAX_HOSTNAME_LENGTH			64
#define MAX_DESCRIPTION_LENGTH			128
#define MAX_PASSWORD_LENGTH			512
#define MAX_IPADDR_LENGTH			32	/* Max length of IP address (or substitute string) */

// Packet format note 1:  The original NSCA packet format is badly designed, in that it presumes the
// compilers on the client and server will necessarily agree on whether 16 bits of padding is required
// between the first field (int16_t packet_version) and the second field (u_int32_t crc32_value).
// (In fact, the standard Linux gcc compiler does insert two bytes of padding there, along with two bytes
// of padding after the last (char plugin_output[]) field, to pad the structure to a 32-bit boundary.)
// In the real world, though, that could depend on the particular machine architectures of the client
// and server machines, and the compilers used.  We have followed the same practice in the version 101
// format not because we like it, but to maintain compatibility with the previous assumptions.  So this
// may break under some circumstances and require repair, for both V3 and V101 protocols.

// Packet format note 2:  The version 101 packet format is defined to be an even multiple of 32 bits,
// even though the trailing fields are all of variable length, to allow a server implementation to
// inhale large buffers of data from the socket if they are already available in the kernel, possibly
// containing more than one packet, even before we know the packet versions and sizes of the constituent
// incoming packets.  With this alignment, we should be able to efficiently access any fields requiring
// hardware alignment straight out of the buffer returned from the kernel without triggering bus errors,
// even when we are processing a downfield packet within such a large buffer.

// This value should compute out to be zero, because all of the individual variable-length fields we are
// concatenating in the variable data packet payload already have lengths divisible by 4 (and thus the
// collection at the maximum length of all fields needs no additional bytes to align to a 32-bit boundary).
// We only define and use it as a reminder that the variable_data_packet.variable_data field may contain some
// extra padding in addition to the useful host name, service description, and plugin output payload fields.
#define ALIGNMENT_PADDING_LENGTH_V101	(3 - (MAX_HOSTNAME_LENGTH + MAX_DESCRIPTION_LENGTH + MAX_NSCA_PLUGINOUTPUT_LENGTH_V101 + 3) % 4)

/********************* ENCRYPTION TYPES ****************/

#define ENCRYPT_NONE            0       /* no encryption */
#define ENCRYPT_XOR             1       /* not really encrypted, just obfuscated */

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

/******************** MISC DEFINITIONS *****************/

#define TRANSMITTED_IV_SIZE     128     /* size of IV to transmit - must be as big as largest IV needed for any crypto algorithm */


/*************** PACKET STRUCTURE DEFINITIONS **********/
/* New packet version, supporting 4kb plugin output and using dynamic field lengths. */
#define NSCA_PACKET_VERSION_101	101
/* packet version identifier */
#define NSCA_PACKET_VERSION_3	3
/* older packet version identifiers */
#define NSCA_PACKET_VERSION_2	2
#define NSCA_PACKET_VERSION_1	1

/*
 * Data packets containing service check results.
 * A version 3 packet has a max plugin output size of 512 bytes,
 * while a version 101 packet has max plugin output size of 4KB.
 * We need both definitions because we can receive and process
 * both versions of packets.
 */
typedef struct data_packet_struct_v3{
	int16_t   packet_version;
	u_int32_t crc32_value;
	u_int32_t timestamp;
	int16_t   return_code;
	char      host_name[MAX_HOSTNAME_LENGTH];
	char      svc_description[MAX_DESCRIPTION_LENGTH];
	char      plugin_output[MAX_NSCA_PLUGINOUTPUT_LENGTH_V3];
	}data_packet_v3;

/*
 * The protocol version 101 implements dynamic sizes for character
 * fields like plugin output.  This means that we may receive a
 * different number of bytes for plugin output for different
 * instances of the incoming packet.  The sizes of these fields
 * are ultimately limited by Nagios.
 */

typedef struct fixed_data_subpacket_struct{
	int16_t   packet_version;
	u_int32_t crc32_value;
	u_int32_t timestamp;
	int16_t   return_code;
	uint16_t  host_name_size;               /* includes size of trailing NUL byte */
	uint16_t  svc_description_size;         /* includes size of trailing NUL byte */
	uint16_t  plugin_output_size;           /* includes size of trailing NUL byte */
	uint16_t  alignment_padding_size;       /* length of 0 to 3 extra bytes for overall packet 32-bit alignment */
	}fixed_data_subpacket;

typedef struct variable_data_packet_struct{
	fixed_data_subpacket    fixed_data;
	char                    variable_data
	    [
	    MAX_HOSTNAME_LENGTH +
	    MAX_DESCRIPTION_LENGTH +
	    MAX_NSCA_PLUGINOUTPUT_LENGTH_V101 +
	    ALIGNMENT_PADDING_LENGTH_V101
	    ];
	}variable_data_packet;

typedef struct wide_data_struct{
	variable_data_packet variable_packet;
	struct {
		char      *host_name;           /* points to host_name       within variable_packet.variable_data */
		char      *svc_description;     /* points to svc_description within variable_packet.variable_data */
		char      *plugin_output;       /* points to plugin_output   within variable_packet.variable_data */
		char      *alignment_padding;   /* might not point to anything, if alignment_padding_size is 0 */ 
	}wide_fields;
	}wide_data;  /* not a network-sendable wide_data_packet in itself, because it contains extra pointer fields */

#define wide_packet_version             variable_packet.fixed_data.packet_version
#define wide_crc32_value                variable_packet.fixed_data.crc32_value
#define wide_timestamp                  variable_packet.fixed_data.timestamp
#define wide_return_code                variable_packet.fixed_data.return_code
#define wide_host_name_size             variable_packet.fixed_data.host_name_size
#define wide_svc_description_size       variable_packet.fixed_data.svc_description_size
#define wide_plugin_output_size         variable_packet.fixed_data.plugin_output_size
#define wide_alignment_padding_size     variable_packet.fixed_data.alignment_padding_size
#define wide_variable_data              variable_packet.variable_data
#define wide_host_name                  wide_fields.host_name
#define wide_svc_description            wide_fields.svc_description
#define wide_plugin_output              wide_fields.plugin_output
#define wide_alignment_padding          wide_fields.alignment_padding

typedef union{
	data_packet_v3	v3_data;
	wide_data	v101_data;
	}arbitrary_data_packet;

// The minimum size of all packets, no matter what their version.
// This can be used to pull in just the beginning part of a packet,
// enough to identify which packet version we're dealing with
#define	MIN_COMMON_PACKET_SIZE					\
	(							\
	sizeof(data_packet_v3) < sizeof(fixed_data_subpacket) ?	\
	sizeof(data_packet_v3) : sizeof(fixed_data_subpacket)	\
	)

// All the packet version formats are designed to have their packet_version fields
// of the same size and in the same position within the packet.  So this symbol can
// be used to access the field even before we know what the packet version is.
#define	arbitrary_packet_version	v3_data.packet_version

/* initialization packet containing IV and timestamp */
typedef struct init_packet_struct{
	char      iv[TRANSMITTED_IV_SIZE];
	u_int32_t timestamp;
	}init_packet;


#endif	/* _BRONX_LISTENER_COMMON_H */
