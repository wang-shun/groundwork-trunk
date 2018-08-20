/*
 *  bronx_cmd_acceptor_utils.c
 *  Utility functions for command acceptor thread.
 * 
 *  Copyright (C) 2009-2012 Groundwork Open Source
 *  Originally written by Hrisheekesh Kale
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
 *	2009-02-20 HK;	Created.
 *	2012-05-14 GH;	Added comments about possible failures.
 *	2012-06-21 GH;	Pull more entropy from the system.
 *			Use a thread-safe random number generator.
 *			Normalize indentation whitespace.
 */

#include "bronx.h"
#include "bronx_config.h"
#include "bronx_log.h"
#include "bronx_cmd_acceptor_utils.h"
#include "bronx_cmd_acceptor.h"
#include "bronx_safe_fork.h"

/*
 * cmd_acceptor_generate_transmitted_iv()
 * generate the random IV to use for encrypted communication
 * Parameters -
 * char *transmitted_iv         - IV buffer to fill.
 * int iv_size                  - Max IV size.
 */
static void
cmd_acceptor_generate_transmitted_iv(char *transmitted_iv, int iv_size)
{
    FILE *fp;
    int x;
    unsigned int seed = 0;
    size_t seeded = 0;

    /*
     * Fill IV buffer with data that's as random as possible.
     * We first try to get seed value from /dev/urandom, as
     * it's a better source of entropy than the current time.
     */
    fp = bronx_safe_fopen("/dev/urandom", "r");
    if(fp != NULL){
	seeded = fread(&seed, sizeof(seed), (size_t) 1, fp);
	bronx_safe_fclose(fp);
    }
    /* fall back to using the current time as the seed */
    if (!seeded) {
	seed = (int)time(NULL);
    }

    // Generate a pseudo-random IV.  We need to call rand_r(), not rand(), because
    // we are operating in a multi-threaded program and the latter call is neither
    // reentrant nor thread-safe (at least, on the Linux platform).  However, because
    // of the constrained amount of state retained across calls, the rand_r() routine
    // produces a limited amount of randomness, which might actually mean that we get
    // less overall randomness using rand_r() than we would have by just using the
    // entropy returned by /dev/urandom in the first place (except that we might want
    // to fill in a larger buffer here than we were willing to ask /dev/urandom to
    // fill).  Our choices seem constrained, if we restrict ourselves to portable,
    // thread-safe random number generators which are widely available.  So this is
    // perhaps an area for future evolution of the code.

    // We write only to the portion of the buffer that is suggested by the function
    // arguments.  If they caller wants to initialize anything outside of that region,
    // that is the responsibility of the caller, not this function.
    for (x = 0; x < iv_size; x++) {
	/* Generate a random string for IV. */
	transmitted_iv[x] = (int)((256.0 * rand_r(&seed))/(RAND_MAX + 1.0));
    }
}

/* 
 * cmd_acceptor_encrypt_init()
 * Initialize the crypt instance structure.
 * Set the encryption method, mode, randomly generated IV.
 * Initialize the mcrypt library.
 * Parameters -
 * char *key                        : Encryption/Decryption key.
 * int encryption_method            : Encryption method.
 * struct ca_crypt_instance **CIptr : Crypt instance structure.
 * Return -
 * CA_OK on success, CA_ERROR on failure.
 */
int
cmd_acceptor_encrypt_init(char *key, int encryption_method, struct ca_crypt_instance **CIptr)
{
    int iv_size;
    struct ca_crypt_instance *CI;

    bronx_log("{cmd_acceptor_encrypt_init}", BRONX_LOGGING_DEBUG);
    CI = (struct ca_crypt_instance *)malloc(sizeof(struct ca_crypt_instance));
    *CIptr = CI;

    if(CI == NULL)
    {
	bronx_log("{cmd_acceptor_encrypt_init} Could not allocate memory for crypt instance", BRONX_LOGGING_ERROR);
	return CA_ERROR;
    }

    /* CFB = 8-bit cipher-feedback mode */
    strncpy(CI->mcrypt_mode, "cfb", (sizeof(CI->mcrypt_mode) - 1));
    CI->mcrypt_mode[sizeof(CI->mcrypt_mode) - 1] = '\0';

    CI->key = NULL;
    CI->IV = NULL;

    /* We are talking paintext. We are done. */
    if(encryption_method == ENCRYPT_NONE)
    {
	return CA_OK;
    }
    /* Get the name of the mcrypt encryption algorithm to use */
    switch(encryption_method)
    {
	/* Cases may be added here, in future. */
	case ENCRYPT_DES:
	    strncpy(CI->mcrypt_algorithm, MCRYPT_DES, (sizeof(CI->mcrypt_algorithm) - 1));
	    CI->mcrypt_algorithm[sizeof(CI->mcrypt_algorithm) - 1] = '\0';
	    break;
	default:
	    strncpy(CI->mcrypt_algorithm, "unknown", (sizeof(CI->mcrypt_algorithm) - 1));
	    CI->mcrypt_algorithm[sizeof(CI->mcrypt_algorithm) - 1] = '\0';
	    break;
    }
 
    /* Open encryption module */
    CI->td = mcrypt_module_open(CI->mcrypt_algorithm, NULL, CI->mcrypt_mode, NULL); 
    if(CI->td == MCRYPT_FAILED){
	bronx_logprintf(BRONX_LOGGING_ERROR, "{cmd_acceptor_encrypt_init} Could not open mcrypt algorithm '%s' with mode '%s'",
		       CI->mcrypt_algorithm, CI->mcrypt_mode);
	return CA_ERROR;
    } 

    /* Determine the size of IV buffer for this algorithm */
    iv_size = mcrypt_enc_get_iv_size(CI->td);
    if (iv_size < 0)
    {
	bronx_log("{cmd_acceptor_encrypt_init} Failed to get the IV size", BRONX_LOGGING_ERROR);
	return CA_ERROR;
    }

    /* Allocate memory for IV buffer */
    CI->IV = (char *)malloc((iv_size + 1) * sizeof(char));
    if(CI->IV == NULL) {
	bronx_log("{cmd_acceptor_encrypt_init} Could not allocate memory for IV buffer", BRONX_LOGGING_ERROR);
	return CA_ERROR;
    }
    /* Generate the random IV to be transmitted */
    cmd_acceptor_generate_transmitted_iv(CI->IV, iv_size);
    // Adding a NUL byte to the end of the IV buffer is kinda dumb, given that NUL
    // is generally a valid value for some bytes within the IV buffer.  So we cannot
    // really depend on using NUL termination of the buffer when processing it.
    // Let's hope we're not that dumb.
    CI->IV[iv_size] = '\0';

    /* Record the IV size */
    CI->iv_size = iv_size;

    /* Get the maximum key size for this algorithm */
    CI->keysize = mcrypt_enc_get_key_size(CI->td);
    if (CI->keysize < 0)
    {
	bronx_log("{cmd_acceptor_encrypt_init} Failed to get the key size", BRONX_LOGGING_ERROR);
	return CA_ERROR;
    }

    /* Allocate space for encryption/decription key */
    CI->key = (char *)malloc((CI->keysize + 1) * sizeof(char));
    if(CI->key == NULL)
    {
	bronx_log("{cmd_acceptor_encrypt_init} Could not allocate memory for encryption/decryption key", BRONX_LOGGING_ERROR);
	return CA_ERROR;
    }
    bzero(CI->key, CI->keysize + 1);

    if ((int) strlen(key) > CI->keysize)
    {
	bronx_logprintf(BRONX_LOGGING_WARNING, "{cmd_acceptor_encrypt_init} The configured key is too big for %s", CI->mcrypt_algorithm);
	return CA_ERROR;
    }
    /* Record the key in the crypt instance structure */
    strncpy(CI->key, key, CI->keysize);
    CI->key[CI->keysize] = '\0';

    /* Initialize encryption library */
    if ((mcrypt_generic_init(CI->td, CI->key, CI->keysize, CI->IV)) < 0)
    {
	bronx_log("{cmd_acceptor_encrypt_init} Failed to initialize Encryption library", BRONX_LOGGING_ERROR);
	return CA_ERROR;
    }

    return CA_OK;
}

/*
 * cmd_acceptor_encrypt_cleanup() 
 * Cleanup the crypt instance structure members.
 * Parameters - 
 * struct ca_crypt_instance *CI   : The command acceptor crypt instance structure.  
 */
void
cmd_acceptor_encrypt_cleanup(struct ca_crypt_instance *CI)
{

    bronx_log("{cmd_acceptor_encrypt_cleanup}", BRONX_LOGGING_DEBUG);

    /* No crypt instance */
    if(CI==NULL)
	return;

    /* Close the mcrypt handle */
    if(_configuration->cmd_acceptor_encryption_method != ENCRYPT_NONE)
    {
	mcrypt_generic_end(CI->td);
    }
    /* Free the key... */
    if (CI->key) {
	free(CI->key);
	CI->key = NULL;
    }
    /* the IV... */
    if (CI->IV) {
	free(CI->IV);
	CI->IV = NULL;
    }
    /* Finally the structure itself. */ 
    free(CI);
}

/*
 * cmd_acceptor_decrypt_buffer()
 * Decrypt the buffer in place.
 * Parameters -
 * char *buffer                 : The buffer to be decrypted.
 * int buffer_size              : Buffer size.
 * int encryption_method        : The encryption algorithm used.
 * struct ca_crypt_instance *CI : A pointer to crypt instance structure. 
 * CA_OK on success, CA_ERROR on failure.
 */
int
cmd_acceptor_decrypt_buffer(char *buffer, int buffer_size, int encryption_method, struct ca_crypt_instance *CI)
{
    int x = 0;

    bronx_log("{cmd_acceptor_decrypt_buffer}", BRONX_LOGGING_DEBUG);
    /* No crypt instance */
    if(CI == NULL)
	return CA_ERROR;

    /* Check the encryption method. */
    if(encryption_method != ENCRYPT_DES && encryption_method != ENCRYPT_NONE)
    {
	return CA_ERROR;
    }

    if (encryption_method == ENCRYPT_NONE)
    {
	/* Nothing to be done. */
	return CA_OK;
    }
    
    /* Decrypt each byte of buffer, one byte at a time (CFB mode) in place */
    for(x = 0; x < buffer_size; x++)
    {
	mdecrypt_generic(CI->td, &buffer[x], 1);
    }
    return CA_OK;
}

/*
 *  cmd_acceptor_clear_buffer()
 *  Wipe an area of memory clean.
 *  Parameters - 
 *  char *buffer             : The buffer to be wiped.
 *  int buffer_length        : Buffer length.
 */
void
cmd_acceptor_clear_buffer(char *buffer, int buffer_length)
{

    /* NULL all bytes of buffer */
    memset(buffer, '\x0', buffer_length);
    return;
}

/*
 *  cmd_acceptor_sendall()
 *  sends all data in the buffer - thanks to Beej's Guide to Network Programming
 *  Parameters -
 *  int s                    : The socket fd to write the data on.
 *  char *buf                : Data buffer.
 *  int *len                 : data length. Overwrite this with the actual size written.
 *  CA_OK on success, CA_ERROR on failure.
 */
int
cmd_acceptor_sendall(int s, char *buf, int *len)
{

    int total = 0;
    int bytesleft = *len;
    int n = 0;
 
    while(total < *len){
	n = send(s, buf + total, bytesleft, 0);
	if(n == -1)
	    break;
	total += n;
	bytesleft -= n;
    }
 
    /* Return number of bytes actually sent here. */
    *len = total;
 
    /* Return CA_ERROR on failure, CA_OK on success */
    return ((n < 0) ? CA_ERROR : CA_OK);
}

/*
 * cmd_acceptor_recvall()
 * Receives all data - modeled after sendall()
 * Parameters -
 * int s              : Socket fd
 * char *buf          : Buffer to read the data in.
 * int *len           : Maximum length of the data to be read.
 *                      Overwrite this with actual read data length.
 * int timeout        : Timeout for the entire read operation. 
 *
 * return -
 * CA_OK on success, CA_ERROR on failure.
 */
int
cmd_acceptor_recvall(int s, char *buf, int *len, int timeout)
{
    int total = 0;
    int bytesleft = *len;
    int n = 0;
    time_t start_time;
    time_t current_time;
 
    /* Clear the receive buffer */
    bzero(buf, *len);
 
    time(&start_time);
 
    /* Until we read all the data...*/
    while(total < *len) {
	/* Receive some data */
	n = recv(s, buf + total, bytesleft, 0);
 
	/* No data has arrived yet (non-blocking socket) */
	if(n == -1 && errno == EAGAIN) {
	    time(&current_time);
	    if(current_time - start_time > timeout)
		break;
	    sleep(1);
	    continue;
	}
	/* Receive error or client disconnect */
	else if(n <= 0) {
	    break;
	}
	/* Apply bytes we received */
	total += n;
	bytesleft -= n;
    }
 
    /* Return the number of bytes actually received here */
    *len = total;
 
    /* Return CA_ERROR on failure, CA_OK on success */
    if (n < 0 || (n == 0 && total == 0)) {
	return CA_ERROR;
    }
    return CA_OK;
}
