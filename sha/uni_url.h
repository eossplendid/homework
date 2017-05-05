#ifndef URL_H  
#define URL_H  
  
#ifdef __cplusplus  
extern "C" {  
#endif  

#include <uni_stdlib.h>  
#include <uni_string.h>  
#include <uni_types.h>
#include <uni_os.h>

int uni_url_decode(char *str, int len);  
char * uni_url_encode(char const *s, int len, int *new_length);  

#ifdef __cplusplus  
}  
#endif  

#endif /* URL_H */  

