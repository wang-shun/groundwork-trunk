/*
 *  bronx_listener_utils.c
 *
 *  Copyright (C) 2009 Groundwork Open Source
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

#include "bronx.h"
#include "bronx_listener_common.h"
#include "bronx_listener_utils.h"
#include "bronx_log.h"
#include "bronx_safe_fork.h"

static unsigned long crc32_table[256];
static void generate_transmitted_iv(char *transmitted_iv);

/* build the crc table - must be called before calculating the crc value */
void generate_crc32_table(void) {
    unsigned long crc, poly;
    int i, j;

    poly=0xEDB88320L;
    for(i=0;i<256;i++) {
	crc=i;
	for(j=8;j>0;j--) {
	    if(crc & 1)
		crc=(crc>>1)^poly;
	    else
		crc>>=1;
	}
	crc32_table[i]=crc;
    }

    return;
}

/* calculates the CRC 32 value for a buffer */
unsigned long calculate_crc32(char *buffer, int buffer_size) {
    register unsigned long crc;
    int this_char;
    int current_index;

    crc=0xFFFFFFFF;

    for(current_index=0;current_index<buffer_size;current_index++) {
	this_char=(int)buffer[current_index];
	crc=((crc>>8) & 0x00FFFFFF) ^ crc32_table[(crc ^ this_char) & 0xFF];
    }

    return (crc ^ 0xFFFFFFFF);
}

/* initializes encryption routines */
int encrypt_init(char *password,int encryption_method,char *received_iv,struct crypt_instance **CIptr) {
    int i;
    int iv_size;
    struct crypt_instance *CI;

    CI=malloc(sizeof(struct crypt_instance));
    *CIptr=CI;

    if(CI==NULL) {
	bronx_log("{encrypt_init} Could not allocate memory for crypt instance", BRONX_LOGGING_ERROR);
	return BRONX_ERROR;
    }

    /* server generates IV used for encryption */
    if(received_iv==NULL)
	generate_transmitted_iv(CI->transmitted_iv);

    /* client recieves IV from server */
    else
	memcpy(CI->transmitted_iv,received_iv,TRANSMITTED_IV_SIZE);

    CI->blocksize=1;                        /* block size = 1 byte w/ CFB mode */
    CI->keysize=7;                          /* default to 56 bit key length */
    CI->mcrypt_mode="cfb";                  /* CFB = 8-bit cipher-feedback mode */
    CI->mcrypt_algorithm="unknown";

    // We need to ensure that these values are set properly so that a cleanup of CI could
    // reliably operate even on a copy of CI returned after a partial construction.
    CI->key=NULL;
    CI->IV=NULL;

    /* XOR or no encryption */
    if(encryption_method==ENCRYPT_NONE || encryption_method==ENCRYPT_XOR)
	return BRONX_OK;

    /* get the name of the mcrypt encryption algorithm to use */
    switch(encryption_method) {
	case ENCRYPT_DES:		CI->mcrypt_algorithm=MCRYPT_DES;		break;
	case ENCRYPT_3DES:		CI->mcrypt_algorithm=MCRYPT_3DES;		break;
	case ENCRYPT_CAST128:		CI->mcrypt_algorithm=MCRYPT_CAST_128;		break;
	case ENCRYPT_CAST256:		CI->mcrypt_algorithm=MCRYPT_CAST_256;		break;
	case ENCRYPT_XTEA:		CI->mcrypt_algorithm=MCRYPT_XTEA;		break;
	case ENCRYPT_3WAY:		CI->mcrypt_algorithm=MCRYPT_3WAY;		break;
	case ENCRYPT_BLOWFISH:		CI->mcrypt_algorithm=MCRYPT_BLOWFISH;		break;
	case ENCRYPT_TWOFISH:		CI->mcrypt_algorithm=MCRYPT_TWOFISH;		break;
	case ENCRYPT_LOKI97:		CI->mcrypt_algorithm=MCRYPT_LOKI97;		break;
	case ENCRYPT_RC2:		CI->mcrypt_algorithm=MCRYPT_RC2;		break;
	case ENCRYPT_ARCFOUR:		CI->mcrypt_algorithm=MCRYPT_ARCFOUR;		break;
	case ENCRYPT_RIJNDAEL128:	CI->mcrypt_algorithm=MCRYPT_RIJNDAEL_128;	break;
	case ENCRYPT_RIJNDAEL192:	CI->mcrypt_algorithm=MCRYPT_RIJNDAEL_192;	break;
	case ENCRYPT_RIJNDAEL256:	CI->mcrypt_algorithm=MCRYPT_RIJNDAEL_256;	break;
	case ENCRYPT_WAKE:		CI->mcrypt_algorithm=MCRYPT_WAKE;		break;
	case ENCRYPT_SERPENT:		CI->mcrypt_algorithm=MCRYPT_SERPENT;		break;
	case ENCRYPT_ENIGMA:		CI->mcrypt_algorithm=MCRYPT_ENIGMA;		break;
	case ENCRYPT_GOST:		CI->mcrypt_algorithm=MCRYPT_GOST;		break;
	case ENCRYPT_SAFER64:		CI->mcrypt_algorithm=MCRYPT_SAFER_SK64;		break;
	case ENCRYPT_SAFER128:		CI->mcrypt_algorithm=MCRYPT_SAFER_SK128;	break;
	case ENCRYPT_SAFERPLUS:		CI->mcrypt_algorithm=MCRYPT_SAFERPLUS;		break;
	default:			CI->mcrypt_algorithm="unknown";			break;
    }

    /* open encryption module */
    if((CI->td=mcrypt_module_open(CI->mcrypt_algorithm,NULL,CI->mcrypt_mode,NULL))==MCRYPT_FAILED) {
	bronx_logprintf(BRONX_LOGGING_ERROR,
	    "{encrypt_init} Could not open mcrypt algorithm '%s' with mode '%s'",CI->mcrypt_algorithm,CI->mcrypt_mode);
	return BRONX_ERROR;
    }

    /* determine size of IV buffer for this algorithm */
    iv_size=mcrypt_enc_get_iv_size(CI->td);
    if(iv_size>TRANSMITTED_IV_SIZE) {
	bronx_log("{encrypt_init} IV size for crypto algorithm exceeds limits", BRONX_LOGGING_ERROR);
	return BRONX_ERROR;
    }

    /* allocate memory for IV buffer */
    if((CI->IV=(char *)malloc(iv_size))==NULL) {
	bronx_log("{encrypt_init} Could not allocate memory for IV buffer", BRONX_LOGGING_ERROR);
	return BRONX_ERROR;
    }

    /* fill IV buffer with first bytes of IV that is going to be used to crypt (determined by server) */
    for(i=0;i<iv_size;i++)
	CI->IV[i]=CI->transmitted_iv[i];

    /* get maximum key size for this algorithm */
    CI->keysize=mcrypt_enc_get_key_size(CI->td);

    /* generate an encryption/decription key using the password */
    if((CI->key=(char *)malloc(CI->keysize))==NULL) {
	bronx_log("{encrypt_init} Could not allocate memory for encryption/decryption key", BRONX_LOGGING_ERROR);
	return BRONX_ERROR;
    }
    bzero(CI->key,CI->keysize);

    if(CI->keysize < (int) strlen(password))
	strncpy(CI->key,password,CI->keysize);
    else
	strncpy(CI->key,password,strlen(password));

    /* initialize encryption buffers */
    mcrypt_generic_init(CI->td,CI->key,CI->keysize,CI->IV);

    return BRONX_OK;
}

/* encryption routine cleanup */
void encrypt_cleanup(int encryption_method, struct crypt_instance *CI) {

    /* no crypt instance */
    if(CI==NULL)
	return;

    /* mcrypt cleanup */
    if(encryption_method!=ENCRYPT_NONE && encryption_method!=ENCRYPT_XOR) {
	mcrypt_generic_end(CI->td);
	free(CI->key);
	CI->key=NULL;
	free(CI->IV);
	CI->IV=NULL;
    }

    free(CI);

    return;
}

/* generates IV to use for encrypted communications (function is called by server only, client uses IV it receives from server) */
static void generate_transmitted_iv(char *transmitted_iv) {
    FILE *fp;
    int x;
    int seed=0;

    /*********************************************************/
    /* fill IV buffer with data that's as random as possible */
    /*********************************************************/

    /* try to get seed value from /dev/urandom, as its a better source of entropy */
    fp=bronx_safe_fopen("/dev/urandom","r");
    if(fp!=NULL) {
	seed=fgetc(fp);
	bronx_safe_fclose(fp);
    }

    /* else fallback to using the current time as the seed */
    else
	seed=(int)time(NULL);

    /* generate pseudo-random IV */
    srand(seed);
    for(x=0;x<TRANSMITTED_IV_SIZE;x++)
	transmitted_iv[x]=(int)((256.0*rand())/(RAND_MAX+1.0));

    return;
}

// Note about the "fresh" flags in encrypt_buffer() and decrypt_buffer():
// The usage of this flag is set by how the send_nsca client works.  The standard packet-generation process
// in that particular client resets the encryption engine on each packet if it is using XOR obfuscation, but
// leaves the encryption engine free-running across multiple packets if using stronger encryption.  So we
// need to mirror that behavior here, and ignore the "fresh" flag when we're performing strong encryption.
// For XOR encryption, the calling application is responsible for setting this flag to a true value when
// decrypting the first fragment of each incoming packet, and to a false value when decrypting any remaining
// fragments of the same incoming packet.  Thus it will be necessary to only decrypt at most one packet at
// a time, even if a much larger buffer allows us to read multiple packets at a time from the kernel.

/* encrypt a buffer */
void encrypt_buffer(char *buffer,int buffer_size, char *password, int encryption_method, struct crypt_instance *CI, int fresh) {
    int x;
    int y;
    int password_length;

    /* no crypt instance */
    if(CI==NULL)
	return;

    /* no encryption */
    if(encryption_method==ENCRYPT_NONE)
	return;

    /* simple XOR "encryption" - not meant for any real security, just obfuscates data, but it's fast... */
    else if(encryption_method==ENCRYPT_XOR) {
	/* rotate over IV we received from the server... */
	for(y = 0, x = fresh ? 0 : CI->IV_position; y < buffer_size; ++y, ++x) {
	    /* keep rotating over IV */
	    if(x>=TRANSMITTED_IV_SIZE)
		x=0;

	    buffer[y]^=CI->transmitted_iv[x];
	}
	CI->IV_position = x;

	/* rotate over password... */
	password_length=strlen(password);
	for(y = 0, x = fresh ? 0 : CI->password_position; y < buffer_size; ++y, ++x) {
	    /* keep rotating over password */
	    if(x>=password_length)
		x=0;

	    buffer[y]^=password[x];
	}
	CI->password_position = x;

	return;
    }
    else {
	/* use mcrypt routines */
	/* encrypt each byte of buffer, one byte at a time (CFB mode) */
	for(x=0;x<buffer_size;x++)
	    mcrypt_generic(CI->td,&buffer[x],1);
    }

    return;
}

/* decrypt a buffer */
void decrypt_buffer(char *buffer,int buffer_size, char *password, int encryption_method, struct crypt_instance *CI, int fresh) {
    int x=0;

    /* no crypt instance */
    if(CI==NULL)
	return;

    /* no encryption */
    if(encryption_method==ENCRYPT_NONE) {
	return;
    }

    /* XOR "decryption" is the same as encryption */
    else if(encryption_method==ENCRYPT_XOR) {
	encrypt_buffer(buffer,buffer_size,password,encryption_method,CI,fresh);
    }
    else {
	/* use mcrypt routines */
	/* encrypt each byte of buffer, one byte at a time (CFB mode) */
	for(x=0;x<buffer_size;x++)
	    mdecrypt_generic(CI->td,&buffer[x],1);
    }
    return;
}

/* fill a buffer with semi-random data */
void randomize_buffer(char *buffer,int buffer_size) {
    FILE *fp;
    int x;
    int seed;

    /**** FILL BUFFER WITH RANDOM ALPHA-NUMERIC CHARACTERS ****/

    /***************************************************************
    Only use alpha-numeric characters becase plugins usually
    only generate numbers and letters in their output.  We
    want the buffer to contain the same set of characters as
    plugins, so its harder to distinguish where the real output
    ends and the rest of the buffer (padded randomly) starts.
    ***************************************************************/

    /* try to get seed value from /dev/urandom, as its a better source of entropy */
    fp=bronx_safe_fopen("/dev/urandom","r");
    if(fp!=NULL) {
	seed=fgetc(fp);
	bronx_safe_fclose(fp);
    }

    /* else fallback to using the current time as the seed */
    else
	seed=(int)time(NULL);

    srand(seed);
    for(x=0;x<buffer_size;x++)
	buffer[x]=(int)'0'+(int)(72.0*rand()/(RAND_MAX+1.0));

    return;
}

/* strips trailing newlines, carriage returns, spaces, and tabs from a string */
void strip(char *buffer) {
    int index;

    for (index = strlen(buffer); --index >= 0; ) {
	if(buffer[index]==' ' || buffer[index]=='\r' || buffer[index]=='\n' || buffer[index]=='\t')
	    buffer[index]='\x0';
	else
	    break;
    }

    return;
}

/* wipes an area of memory clean */
void clear_buffer(char *buffer, int buffer_length) {

    /* NULL all bytes of buffer */
    memset(buffer,'\x0',buffer_length);

    return;
}

