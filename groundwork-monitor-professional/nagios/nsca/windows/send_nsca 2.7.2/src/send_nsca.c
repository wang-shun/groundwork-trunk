/**********************************************************************************
 *
 * SEND_NSCA.C - NSCA Client
 * License: GPL v2
 * Copyright (c) 2000-2007 Ethan Galstad (nagios@nagios.org)
 *
 * Last Modified: 07-03-2007 (Ethan Galstad)
 * Last Modified: 08-05-2010 (GroundWork)
 *
 * Command line: SEND_NSCA <host_address> [-p port] [-to to_sec] [-c config_file] [-od] [-wp|-sp]
 *
 * Description:
 *
 *
 *********************************************************************************/

/*#define DEBUG*/

#include "../include/common.h"
#include "../include/config.h"
#include "../include/netutils.h"
#include "../include/utils.h"
#include <errno.h>

time_t start_time,end_time;

int server_port=DEFAULT_SERVER_PORT;
char server_name[MAX_HOST_ADDRESS_LENGTH];
char password[MAX_INPUT_BUFFER]="";
char config_file[MAX_INPUT_BUFFER]="send_nsca.cfg";
char delimiter[2]="\t";
char alignment_padding[] = "\xA5\x69\x5A\x96";  /* semi-random garbage */

char received_iv[TRANSMITTED_IV_SIZE];

int socket_timeout=DEFAULT_SOCKET_TIMEOUT;

int warning_time=0;
int check_warning_time=FALSE;
int critical_time=0;
int check_critical_time=FALSE;
int encryption_method=ENCRYPT_XOR;
time_t packet_timestamp;
struct crypt_instance *CI=NULL;
int total_packets=0;

int show_help=FALSE;
int show_license=FALSE;
int show_version=FALSE;
int custom_timestamp=FALSE;
int allow_wide_plugin_output=TRUE;
int got_packet_config_file_option=FALSE;
int got_packet_command_line_option=FALSE;


int process_arguments(int,char **);
int read_config_file(char *);
int read_init_packet(int);
void alarm_handler(int);
void pipe_handler(int);
void clear_password(void);
static void do_exit(int);

// Sleep takes ms instead of seconds on Windows
void sleep (int seconds) {
	Sleep(100*seconds);
	}

// Windows replacement for bzero unix call
void bzero(void * dest, size_t length) {
	ZeroMemory( (char *) dest, (int) length);
	}

int main(int argc, char **argv){
	int sd;
	int rc;
	int result;
	WSADATA wsaData;
	data_packet send_packet;
	wide_data wide_send_packet;
	int bytes_to_send;
	size_t unpadded_packet_size;
	size_t full_packet_size;
	char input_buffer[MAX_WIDE_INPUT_BUFFER];
	char host_name[MAX_HOSTNAME_LENGTH];
	char svc_description[MAX_DESCRIPTION_LENGTH];
	char plugin_output[MAX_PLUGINOUTPUT_LENGTH];
	char long_plugin_output[MAX_WIDE_PLUGINOUTPUT_LENGTH];
	int16_t return_code;
	u_int32_t calculated_crc32;
	char *ptr0, *ptr1, *ptr2, *ptr3, *ptr4;


	/* process command-line arguments */
	result=process_arguments(argc,argv);

	if(result!=OK || show_help==TRUE || show_license==TRUE || show_version==TRUE){

		if(result!=OK)
			printf("Incorrect command line arguments supplied\n");
		printf("\n");
		printf("NSCA Client %s\n",PROGRAM_VERSION);
		printf("Copyright (c) 2000-2007 Ethan Galstad (www.nagios.org)\n");
		printf("Last Modified: %s\n",MODIFICATION_DATE);
		printf("License: GPL v2\n");
		printf("Encryption Routines: ");
#ifdef HAVE_LIBMCRYPT
		printf("AVAILABLE");
#else
		printf("NOT AVAILABLE");
#endif		
		printf("\n");
		printf("\n");
		}

	if(result!=OK || show_help==TRUE){
		printf("Usage: %s -H <host_address> [-p port] [-to to_sec] [-d delim] [-c config_file] [-od] [-wp|-sp]\n",argv[0]);
		printf("\n");
		printf("Options:\n");
		printf(" <host_address> = The IP address of the host running the NSCA daemon\n");
		printf(" [port]         = The port on which the daemon is running - default is %d\n",DEFAULT_SERVER_PORT);
		printf(" [to_sec]       = Number of seconds before connection attempt times out.\n");
		printf("                  (default timeout is %d seconds)\n",DEFAULT_SOCKET_TIMEOUT);
		printf(" [delim]        = Delimiter to use when parsing input (defaults to a tab)\n");
		printf(" [config_file]  = Name of config file to use\n");
		printf(" [-od]          = Specify old timestamp\n");
		printf(" [-wp]          = Allow wide plugin output (up to 4095 characters, inclusive)\n");
		printf(" [-sp]          = Restrict plugin output to 511 characters\n");
		printf("\n");
		printf("Note:\n");
		printf("This utility is used to send passive check results to the NSCA daemon, or\n");
		printf("to the Bronx Nagios event broker in a GroundWork Monitor system.  Host and\n");
		printf("Service check data that is to be sent is read from the standard input stream.\n");
		printf("Input should be provided in the following format (tab-delimited unless\n");
		printf("overridden with the -d command line argument, one entry per line):\n");
		printf("\n");
		printf("Service Checks:\n");
		printf("<host_name>[tab]<svc_description>[tab]<return_code>[tab]<plugin_output>[newline]\n\n");
		printf("Host Checks:\n");
		printf("<host_name>[tab]<return_code>[tab]<plugin_output>[newline]\n\n");
		printf("Use the -od argument to send timestamps in the past, instead of the current time.\n");
		printf("The input will then have to be specified as:\n");
		printf("\n");
		printf("Service Checks:\n");
		printf("<timestamp>[tab]<host_name>[tab]<svc_description>[tab]<return_code>[tab]<plugin_output>[newline]\n\n");
		printf("Host Checks:\n");
		printf("<timestamp>[tab]<host_name>[tab]<return_code>[tab]<plugin_output>[newline]\n");
		printf("\n");
		}

	if(show_license==TRUE)
		display_license();

	if(result!=OK || show_help==TRUE || show_license==TRUE || show_version==TRUE)
		do_exit(STATE_UNKNOWN);



	/* read the config file */
	result=read_config_file(config_file);	

	/* exit if there are errors... */
	if(result==ERROR){
		printf("Error: Config file '%s' contained errors...\n",config_file);
		do_exit(STATE_CRITICAL);
		}

	/* generate the CRC 32 table */
	generate_crc32_table();

	/* initialize alarm signal handling */
//	signal(SIGALRM,alarm_handler);

	/* initialize pipe signal handling */
//	signal(SIGPIPE,pipe_handler);

	/* set socket timeout */
//	alarm(socket_timeout);

	time(&start_time);

	// WSAStartup
	if ((result = WSAStartup(0x202,&wsaData)) != 0) {
		printf("Error initializing winsock\n");
		WSACleanup();
		return STATE_UNKNOWN;
	}

	/* try to connect to the host at the given port number */
	result=my_tcp_connect(server_name,server_port,&sd);

	/* we couldn't connect */
	if(result!=STATE_OK){
		printf("Error: Could not connect to host %s on port %d\n",server_name,server_port);
		WSACleanup();
		do_exit(STATE_CRITICAL);
		}

#ifdef DEBUG
	printf("Connected okay...\n");
#endif

	/* read the initialization packet containing the IV and timestamp */
	result=read_init_packet(sd);
	if(result!=OK){
		printf("Error: Could not read init packet from server\n");
		closesocket(sd);
		WSACleanup();
		do_exit(STATE_CRITICAL);
		}

#ifdef DEBUG
	printf("Got init packet from server\n");
#endif

	/* initialize encryption/decryption routines with the IV we received from the server */
	if(encrypt_init(password,encryption_method,received_iv,&CI)!=OK){
		printf("Error: Failed to initialize encryption libraries for method %d\n",encryption_method);
		closesocket(sd);
		WSACleanup();
		do_exit(STATE_CRITICAL);
		}

#ifdef DEBUG
	printf("Initialized encryption routines\n");
#endif


	/**** WE'RE CONNECTED AND READY TO SEND ****/

	/* read all data from STDIN until there isn't anymore */
	while(fgets(input_buffer,sizeof(input_buffer)-1,stdin)){
		if(feof(stdin))
			break;

		strip(input_buffer);

		if(!strcmp(input_buffer,""))
			continue;

		if(custom_timestamp){
			/* get the timestamp */
			ptr0=strtok(input_buffer,delimiter);
			if(ptr0==NULL)
				continue;
		}

		/* get the host name */
		ptr1=strtok(custom_timestamp ? NULL : input_buffer,delimiter);
		if(ptr1==NULL)
			continue;

		/* get the service description or return code */
		ptr2=strtok(NULL,delimiter);
		if(ptr2==NULL)
			continue;

		/* get the return code or plugin output */
		ptr3=strtok(NULL,delimiter);
		if(ptr3==NULL)
			continue;

		/* get the plugin output - if NULL, this is a host check result */
		ptr4=strtok(NULL,"\n");
	
		if(custom_timestamp){
			packet_timestamp=(time_t)atol(ptr0);
		}
		strncpy(host_name,ptr1,sizeof(host_name)-1);
		host_name[sizeof(host_name)-1]='\x0';
		if(ptr4==NULL){
			strcpy(svc_description,"");
			return_code=atoi(ptr2);
			if (allow_wide_plugin_output) {
				if(long_plugin_output[strlen(long_plugin_output)-1]=='\n')
					long_plugin_output[strlen(long_plugin_output)-1]='\x0';
				strncpy(long_plugin_output,ptr3,sizeof(long_plugin_output)-1);
				}
			else {
				if(plugin_output[strlen(plugin_output)-1]=='\n')
					plugin_output[strlen(plugin_output)-1]='\x0';
				strncpy(plugin_output,ptr3,sizeof(plugin_output)-1);
				}
			}
		else{
			strncpy(svc_description,ptr2,sizeof(svc_description)-1);
			return_code=atoi(ptr3);
			if (allow_wide_plugin_output) {
				strncpy(long_plugin_output,ptr4,sizeof(long_plugin_output)-1);
				}
			else {
				strncpy(plugin_output,ptr4,sizeof(plugin_output)-1);
				}
			}
		svc_description[sizeof(svc_description)-1]='\x0';
		if (allow_wide_plugin_output) {
			long_plugin_output[sizeof(long_plugin_output)-1]='\x0';
		}
		else {
			plugin_output[sizeof(plugin_output)-1]='\x0';
		}

		/*
		** The whole mechanism used here for bringing the incoming data into individual fields above,
		** and then separately copying them into the packet structure below, is silly and wasteful.
		** The efficiency gets even worse with wide plugin output.  We should revisit this in the
		** next round of improvements to eliminate this unnecessary overhead.
		*/
		if (allow_wide_plugin_output) {
			uint16_t real_host_name_size;
			uint16_t real_svc_description_size;
			uint16_t real_plugin_output_size;
			uint16_t real_alignment_padding_size;

			/*
			** There's no point in clearing the wide packet buffer or filling it with random data,
			** because the sent portion contains no holes that aren't overwritten by each packet.
			*/

			/* copy the data we want to send into the packet */
			wide_send_packet.wide_packet_version=(int16_t)htons(NSCA_PACKET_VERSION_101);
			wide_send_packet.wide_return_code=(int16_t)htons(return_code);

			real_host_name_size = strlen(host_name) + NUL_TERM_LEN;
			wide_send_packet.wide_host_name_size = htons(real_host_name_size);
			wide_send_packet.wide_host_name = wide_send_packet.wide_variable_data;
			strncpy(wide_send_packet.wide_host_name,host_name,real_host_name_size);
			wide_send_packet.wide_host_name[real_host_name_size - 1] = '\0';

			real_svc_description_size = strlen(svc_description) + NUL_TERM_LEN;
			wide_send_packet.wide_svc_description_size = htons(real_svc_description_size);
			wide_send_packet.wide_svc_description = wide_send_packet.wide_host_name + real_host_name_size;
			strncpy(wide_send_packet.wide_svc_description,svc_description,real_svc_description_size);
			wide_send_packet.wide_svc_description[real_svc_description_size - 1] = '\0';

			real_plugin_output_size = strlen(long_plugin_output) + NUL_TERM_LEN;
			wide_send_packet.wide_plugin_output_size = htons(real_plugin_output_size);
			wide_send_packet.wide_plugin_output = wide_send_packet.wide_svc_description + real_svc_description_size;
			strncpy(wide_send_packet.wide_plugin_output,long_plugin_output,real_plugin_output_size);
			wide_send_packet.wide_plugin_output[real_plugin_output_size - 1] = '\0';

			unpadded_packet_size = sizeof(wide_send_packet.variable_packet.fixed_data) +
				real_host_name_size + real_svc_description_size + real_plugin_output_size;

			/*
			** Any of these formulas should produce the same result, to pad (if necessary) up to an even multiple of 4 bytes:
			**	real_alignment_padding_size = ((unpadded_packet_size + 1) % 4) ^ 1;
			**	real_alignment_padding_size = 3 - ((unpadded_packet_size + 3) & 3);
			**	real_alignment_padding_size = 3 - ((unpadded_packet_size + 3) % 4);
			** We will use the last, as it is probably easiest to understand.
			*/
			real_alignment_padding_size = 3 - ((unpadded_packet_size + 3) % 4);
			wide_send_packet.wide_alignment_padding_size = htons(real_alignment_padding_size);
			if (real_alignment_padding_size) {
				wide_send_packet.wide_alignment_padding = wide_send_packet.wide_plugin_output + real_plugin_output_size;
				memcpy(wide_send_packet.wide_alignment_padding,alignment_padding,real_alignment_padding_size);
			}

			/* use timestamp provided by the server (or perhaps the client) */
			wide_send_packet.wide_timestamp=(u_int32_t)htonl(packet_timestamp);

			full_packet_size = unpadded_packet_size + real_alignment_padding_size;
			bytes_to_send = (int) full_packet_size;

			/* calculate the crc 32 value of the packet */
			wide_send_packet.wide_crc32_value=(u_int32_t)0L;
			calculated_crc32=calculate_crc32((char *)&wide_send_packet,bytes_to_send);
			wide_send_packet.wide_crc32_value=(u_int32_t)htonl(calculated_crc32);

			/* encrypt the packet */
			encrypt_buffer((char *)&wide_send_packet,bytes_to_send,password,encryption_method,CI);

			/* send the packet */
			rc=sendall(sd,(char *)&wide_send_packet,&bytes_to_send);
			}
		else {
			/* clear the packet buffer */
			bzero(&send_packet,sizeof(send_packet));

			/* fill the packet with semi-random data */
			randomize_buffer((char *)&send_packet,sizeof(send_packet));

			/* copy the data we want to send into the packet */
			send_packet.packet_version=(int16_t)htons(NSCA_PACKET_VERSION_3);
			send_packet.return_code=(int16_t)htons(return_code);
			strncpy(&send_packet.host_name[0],host_name,sizeof(send_packet.host_name));
			send_packet.host_name[sizeof(send_packet.host_name) -1] = '\0';
			strncpy(&send_packet.svc_description[0],svc_description,sizeof(send_packet.svc_description));
			send_packet.svc_description[sizeof(send_packet.svc_description) -1] = '\0';
			strncpy(&send_packet.plugin_output[0],plugin_output,sizeof(send_packet.plugin_output));
			send_packet.plugin_output[sizeof(send_packet.plugin_output) -1] = '\0';

			/* use timestamp provided by the server (or perhaps the client) */
			send_packet.timestamp=(u_int32_t)htonl(packet_timestamp);

			/* calculate the crc 32 value of the packet */
			send_packet.crc32_value=(u_int32_t)0L;
			calculated_crc32=calculate_crc32((char *)&send_packet,sizeof(send_packet));
			send_packet.crc32_value=(u_int32_t)htonl(calculated_crc32);

			/* encrypt the packet */
			encrypt_buffer((char *)&send_packet,(int)sizeof(send_packet),password,encryption_method,CI);

			/* send the packet */
			bytes_to_send=sizeof(send_packet);
			rc=sendall(sd,(char *)&send_packet,&bytes_to_send);
			}

		/* there was an error sending the packet */
		if(rc==-1){
			/* first, tell the caller how many packets got sent okay, so they don't need to be re-sent */
			printf("%d data packet(s) sent to host successfully.\n",total_packets);
			printf("Error: Could not send data to host\n");
			closesocket(sd);
			WSACleanup();
			do_exit(STATE_UNKNOWN);
			}

		/* for some reason we didn't send all the bytes we were supposed to */
		else if(allow_wide_plugin_output && bytes_to_send<full_packet_size){
			/* first, tell the caller how many packets got sent okay, so they don't need to be re-sent */
			printf("%d data packet(s) sent to host successfully.\n",total_packets);
			printf("Warning: Sent only %d of %zd bytes to host\n",bytes_to_send,full_packet_size);
			closesocket(sd);
			WSACleanup();
			return STATE_UNKNOWN;
			}
		else if(!allow_wide_plugin_output && bytes_to_send<sizeof(send_packet)){
			/* first, tell the caller how many packets got sent okay, so they don't need to be re-sent */
			printf("%d data packet(s) sent to host successfully.\n",total_packets);
			printf("Warning: Sent only %d of %zd bytes to host\n",bytes_to_send,sizeof(send_packet));
			closesocket(sd);
			WSACleanup();
			return STATE_UNKNOWN;
			}

		/* increment count of packets we have successfully sent */
		total_packets++;
		}

#ifdef DEBUG
	printf("Done sending data\n");
#endif

	/* close the connection */
	closesocket(sd);

	printf("%d data packet(s) sent to host successfully.\n",total_packets);

	/* exit cleanly */
	WSACleanup();
	do_exit(STATE_OK);

	/* no compiler complaints here... */
	return STATE_OK;
	}



/* exit */
static void do_exit(int return_code){

	/* reset the alarm */
//	alarm(0);

	/* encryption/decryption routine cleanup */
	encrypt_cleanup(encryption_method,CI);

#ifdef DEBUG
	printf("Cleaned up encryption routines\n");
#endif

	/*** CLEAR SENSITIVE INFO FROM MEMORY ***/

	/* overwrite password */
	clear_buffer(password,sizeof(password));

	/* disguise decryption method */
	encryption_method=-1;

	exit(return_code);
	}



/* reads initialization packet (containing IV and timestamp) from server */
int read_init_packet(int sock){
	int rc;
	init_packet receive_packet;
	int bytes_to_recv;

	/* clear the IV and timestamp */
	bzero(&received_iv,TRANSMITTED_IV_SIZE);
	packet_timestamp=(time_t)0;

	/* get the init packet from the server */
	bytes_to_recv=sizeof(receive_packet);
	rc=recvall(sock,(char *)&receive_packet,&bytes_to_recv,socket_timeout);

	/* recv() error or server disconnect */
	if(rc<=0){
		printf("Error: Server closed connection before init packet was received\n");
		return ERROR;
		}

	/* we couldn't read the correct amount of data, so bail out */
	else if(bytes_to_recv!=sizeof(receive_packet)){
		printf("Error: Init packet from server was too short (%d bytes received, %d expected)\n",bytes_to_recv,sizeof(receive_packet));
		return ERROR;
		}

	/* transfer the IV and timestamp */
	memcpy(&received_iv,&receive_packet.iv[0],TRANSMITTED_IV_SIZE);
	packet_timestamp=(time_t)ntohl(receive_packet.timestamp);

	return OK;
	}



/* process command line arguments */
int process_arguments(int argc, char **argv){
	int x;

	/* no options were supplied */
	if(argc<2){
		show_help=TRUE;
		return OK;
		}

	/* support old command-line syntax (host name first argument) */
	strncpy(server_name,argv[1],sizeof(server_name)-1);
	server_name[sizeof(server_name)-1]='\x0';

	/* process arguments (host name is usually 1st argument) */
	for(x=2;x<=argc;x++){

		/* show usage */
		if(!strcmp(argv[x-1],"-h") || !strcmp(argv[x-1],"--help"))
			show_help=TRUE;

		/* show license */
		else if(!strcmp(argv[x-1],"-l") || !strcmp(argv[x-1],"--license"))
			show_license=TRUE;

		/* show version */
		else if(!strcmp(argv[x-1],"-V") || !strcmp(argv[x-1],"--version"))
			show_version=TRUE;

		/* server name/address */
		else if(!strcmp(argv[x-1],"-H")){
			if(x<argc){
				strncpy(server_name,argv[x],sizeof(server_name));
				server_name[sizeof(server_name)-1]='\x0';
				x++;
				}
			else {
				printf ("Error:  -H argument is missing.\n");
				return ERROR;
				}
			}

		/* port to connect to */
		else if(!strcmp(argv[x-1],"-p")){
			if(x<argc){
				server_port=atoi(argv[x]);
				x++;
				}
			else {
				printf ("Error:  -p argument is missing.\n");
				return ERROR;
				}
			}

		/* timeout when connecting */
		else if(!strcmp(argv[x-1],"-to")){
			if(x<argc){
				socket_timeout=atoi(argv[x]);
				if(socket_timeout<=0) {
					printf ("Error:  -to argument is not a positive integer.\n");
					return ERROR;
					}
				x++;
				}
			else {
				printf ("Error:  -to argument is missing.\n");
				return ERROR;
				}
			}

		/* config file */
		else if(!strcmp(argv[x-1],"-c")){
			if(x<argc){
				snprintf(config_file,sizeof(config_file),"%s",argv[x]);
				config_file[sizeof(config_file)-1]='\x0';
				x++;
				}
			else {
				printf ("Error:  -c argument is missing.\n");
				return ERROR;
				}
			}

		/* delimiter to use when parsing input */
		else if(!strcmp(argv[x-1],"-d")){
			if(x<argc){
				snprintf(delimiter,sizeof(delimiter),"%s",argv[x]);
				delimiter[sizeof(delimiter)-1]='\x0';
				x++;
				}
			else {
				printf ("Error:  -d argument is missing.\n");
				return ERROR;
				}
			}

		/* use custom timestamp */
		else if(!strcmp(argv[x-1],"-od")){
			custom_timestamp = TRUE;
			}

		/* disallow wide plugin output from being sent */
		else if(!strcmp(argv[x-1],"-sp")){
			if (got_packet_command_line_option && allow_wide_plugin_output) {
				/* got contradictory command-line options */
				printf ("Error:  Both -wp and -sp options are specified.\n");
				return ERROR;
				}
			allow_wide_plugin_output = FALSE;
			got_packet_command_line_option = TRUE;
			}

		/* allow wide plugin output to be sent */
		else if(!strcmp(argv[x-1],"-wp")){
			if (got_packet_command_line_option && !allow_wide_plugin_output) {
				/* got contradictory command-line options */
				printf ("Error:  Both -sp and -wp options are specified.\n");
				return ERROR;
				}
			allow_wide_plugin_output = TRUE;
			got_packet_command_line_option = TRUE;
			}

		else if(x>2) {
			printf ("Error:  \"%s\" is not a recognized option.\n", argv[x-1]);
			return ERROR;
			}
		}

	return OK;
	}



/* handle timeouts */
void alarm_handler(int sig){

	printf("%d data packet(s) sent to host successfully.\n",total_packets);
	printf("Error: Timeout after %d seconds\n",socket_timeout);

	do_exit(STATE_CRITICAL);
	}



/* handle writes to a remotely closed socket */
void pipe_handler(int sig){

	printf("%d data packet(s) sent to host successfully.\n",total_packets);
	printf("Error: write to a closed socket\n");

	do_exit(STATE_CRITICAL);
	}


char *trim(char *str) {
	char *end;
	while (*str == ' ' || *str == '\t') {
		++str;
		}
	end = str + strlen(str);
	while (end > str) {
		--end;
		if (*end != ' ' && *end != '\t') {
			*++end;
			break;
			}
		}
	*end = '\0';
	return str;
	}


/* read in the configuration file */
int read_config_file(char *filename){
	FILE *fp;
	char input_buffer[MAX_INPUT_BUFFER];
	char *varname;
	char *varvalue;
	int line;


	/* open the config file for reading */
	fp=fopen(filename,"r");

	/* exit if we couldn't open the config file */
	if(fp==NULL){
		printf("Could not open config file '%s' for reading.\n",filename);
		return ERROR;
		}	

	line=0;
	while(fgets(input_buffer,MAX_INPUT_BUFFER-1,fp)){

		line++;

		/* skip comments and blank lines */
		if(input_buffer[0]=='#')
			continue;
		if(input_buffer[0]=='\x0')
			continue;
		if(input_buffer[0]=='\n')
			continue;

		/* get the variable name */
		varname=strtok(input_buffer,"=");
		if(varname==NULL){

			printf("No variable name specified in config file '%s' - Line %d\n",filename,line);

			return ERROR;
			}

		/* get the variable value */
		varvalue=strtok(NULL,"\n");
		if(varvalue==NULL){

			printf("No variable value specified in config file '%s' - Line %d\n",filename,line);

			return ERROR;
			}

		if(strstr(input_buffer,"password")){
			if(strlen(varvalue)>sizeof(password)-1){

				printf("Password is too long in config file '%s' - Line %d\n",filename,line);

				return ERROR;
				}
			strncpy(password,varvalue,sizeof(password));
			password[sizeof(password)-1]='\x0';
			}

		else if(strstr(input_buffer,"encryption_method")){

			encryption_method=atoi(varvalue);

			switch(encryption_method){
			case ENCRYPT_NONE:
				break;
			case ENCRYPT_XOR:
				break;

#ifdef HAVE_LIBMCRYPT
			case ENCRYPT_DES:
				break;
			case ENCRYPT_3DES:
				break;
			case ENCRYPT_CAST128:
				break;
			case ENCRYPT_CAST256:
				break;
			case ENCRYPT_XTEA:
				break;
			case ENCRYPT_3WAY:
				break;
			case ENCRYPT_BLOWFISH:
				break;
			case ENCRYPT_TWOFISH:
				break;
			case ENCRYPT_LOKI97:
				break;
			case ENCRYPT_RC2:
				break;
			case ENCRYPT_ARCFOUR:
				break;
			case ENCRYPT_RIJNDAEL128:
				break;
			case ENCRYPT_RIJNDAEL192:
				break;
			case ENCRYPT_RIJNDAEL256:
				break;
			case ENCRYPT_WAKE:
				break;
			case ENCRYPT_SERPENT:
				break;
			case ENCRYPT_ENIGMA:
				break;
			case ENCRYPT_GOST:
				break;
			case ENCRYPT_SAFER64:
				break;
			case ENCRYPT_SAFER128:
				break;
			case ENCRYPT_SAFERPLUS:
				break;
#endif
			default:
				printf("Invalid encryption method (%d) in config file '%s' - Line %d\n",encryption_method,filename,line);
#ifndef HAVE_LIBMCRYPT
				if(encryption_method>=2)
					printf("Client was not compiled with mcrypt library, so encryption is unavailable.\n");
#endif
				return ERROR;
				}
			}

		else if(strstr(input_buffer,"wide_plugin_output")){
			varvalue = trim(varvalue);
			if(!strcmp(varvalue, "off")){
				if (got_packet_config_file_option && allow_wide_plugin_output) {
					printf("Contradictory wide_plugin_output options are specified in config file '%s'.\n", filename);
					return ERROR;
					}
				if (!got_packet_command_line_option) {
					allow_wide_plugin_output = FALSE;
					got_packet_config_file_option = TRUE;
					}
				}
			else if(!strcmp(varvalue, "on")){
				if (got_packet_config_file_option && !allow_wide_plugin_output) {
					printf("Contradictory wide_plugin_output options are specified in config file '%s'.\n", filename);
					return ERROR;
					}
				if (!got_packet_command_line_option) {
					allow_wide_plugin_output = TRUE;
					got_packet_config_file_option = TRUE;
					}
				}
			else {
				printf("Invalid value for wide_plugin_output (must be on or off) in config file '%s' - Line %d\n",filename,line);
				return ERROR;
				}
			}

		else{
			printf("Unknown option specified in config file '%s' - Line %d\n",filename,line);

			return ERROR;
			}

		}


	/* close the config file */
	fclose(fp);

	return OK;
	}

