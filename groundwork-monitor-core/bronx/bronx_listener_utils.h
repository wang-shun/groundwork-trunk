/* 
 *  bronx_listener_utils.h
 *
 *  Copyright (C) 2007 Groundwork Open Source
 *  Written by Daniel Emmanuel Feinsmith
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
 *      DEF Created on September 17, 2007, 1:00 PM
 */

#ifndef _BRONX_LISTENER_UTILS_H
#define _BRONX_LISTENER_UTILS_H

#include "bronx_listener_defines.h"

struct crypt_instance
{
    char transmitted_iv[TRANSMITTED_IV_SIZE];
    MCRYPT td;
    char *key;
    char *IV;
    char block_buffer;
    int blocksize;
    int keysize;
    char *mcrypt_algorithm;
    char *mcrypt_mode;
    int IV_position;
    int password_position;
};

void generate_crc32_table(void);
unsigned long calculate_crc32(char *, int);

int encrypt_init(char *,int,char *,struct crypt_instance **);
void encrypt_cleanup(int,struct crypt_instance *);

void encrypt_buffer(char *,int,char *,int,struct crypt_instance *,int);
void decrypt_buffer(char *,int,char *,int,struct crypt_instance *,int);

void randomize_buffer(char *,int);

void strip(char *);

void clear_buffer(char *,int);

void display_license(void);

#endif	/* _BRONX_LISTENER_UTILS_H */
