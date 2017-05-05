#ifndef _MY_HTTP_H  
#define _MY_HTTP_H  

#include "uni_stdio.h"
#include "uni_stdlib.h"
#include "uni_string.h" 

#include "uni_unistd.h"
#include "uni_os.h"
#include "uni_socket.h"
#include "uni_types.h"
#include "uni_inet.h"
#include "uni_netdb.h"  

#ifdef __cplusplus
extern "C"{
#endif 

#define MY_HTTP_DEFAULT_PORT 80  

char * uni_http_get(const char *url);  
char * uni_http_post(const char *url,const char * post_str);  

#ifdef __cplusplus
}
#endif 

#endif 
