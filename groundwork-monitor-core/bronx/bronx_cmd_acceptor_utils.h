/* 
 *  bronx_cmd_acceptor_utils.h
 *  Headers for bronx_cmd_acceptor_utils.c
 *
 *  Copyright (C) 2009 Groundwork Open Source
 *  Written by Hrisheekesh Kale 
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
 *      Hrisheekesh Created Feb 23, 2009, 12:00 PM
 */

#ifndef _BRONX_CMD_ACCEPTOR_UTILS_H
#define _BRONX_CMD_ACCEPTOR_UTILS_H

#include <mcrypt.h>

#define ENCRYPT_DES             2       /* DES */
#define ENCRYPT_NONE        	0       /* Plaintext */

#define MAX_MCRYPT_ALGO_LEN     32      /* Encryptin algorithm name length */
#define MAX_MCRYPT_MODE_LEN     16      /* Encryptin algorithm mode length */

struct ca_crypt_instance
{
    MCRYPT td;                                   /* Encryption handle. */
    char *key;                                   /* Encryption Key */
    char *IV;                                    /* Initialization vector */ 
    int iv_size;                                 /* IV size for the current encryption method */
    int keysize;                                 /* key size for the current encryption method */
    char mcrypt_algorithm[MAX_MCRYPT_ALGO_LEN];  /* Encryption algorithm used */
    char mcrypt_mode[MAX_MCRYPT_MODE_LEN];       /* Encryption mode. CFB, CBC, etc. */
};

int cmd_acceptor_encrypt_init(char *, int, struct ca_crypt_instance **);
void cmd_acceptor_encrypt_cleanup(struct ca_crypt_instance *);
int cmd_acceptor_decrypt_buffer(char *, int, int, struct ca_crypt_instance *);
void cmd_acceptor_clear_buffer(char *, int);
int cmd_acceptor_sendall(int s, char *buf, int *len);
int cmd_acceptor_recvall(int s, char *buf, int *len, int timeout);

#endif	/* _BRONX_CMD_ACCEPTOR_UTILS_H */
