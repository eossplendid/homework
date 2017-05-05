#include "uni_http.h"  

#define BUFFER_SIZE 256  

#define HTTP_POST "POST /%s HTTP/1.1\r\nHOST: %s:%d\r\nAccept: */*\r\n" \
"Content-Type:application/x-www-form-urlencoded\r\nContent-Length: %d\r\n\r\n%s"

#define HTTP_GET "GET /%s HTTP/1.1\r\nHOST: %s:%d\r\nAccept: */*\r\n\r\n"  

static int http_tcpclient_create(const char *host, int port){  
    //struct hostent *he;  
    struct sockaddr_in server_addr;   
    int socket_fd;  
    uni_in_addr_t ip;

    if(uni_gethostbyname((uni_char*)(intptr_t)host, &ip)){
       return -1;
    }

    server_addr.sin_family = AF_INET;  
    server_addr.sin_port = htons(port);  
    //server_addr.sin_addr = *((struct in_addr *)he->h_addr);  
    server_addr.sin_addr.s_addr = ip;  

    uni_printf("ip: %x port: %x\n", (int)server_addr.sin_addr.s_addr, (int)server_addr.sin_port);

    socket_fd = uni_socket(AF_INET, SOCK_STREAM, 0);  
    if(socket_fd  == -1) {  
        uni_printf("%s scoket error\n", __func__);
        return -1;  
    }  

    int ret = uni_connect(socket_fd, (struct sockaddr *)&server_addr, sizeof(struct sockaddr));  
    if(ret == -1) {  
        uni_printf("%s connect error\n", __func__);
        return -1;  
    }  

    return socket_fd;  
}  

static void http_tcpclient_close(int socket){  
    uni_socket_close(socket);  
}  

static int http_parse_url(const char *url, char *host, char *file,int *port)  
{  
    char *ptr1,*ptr2;  
    int len = 0;  
    if(!url || !host || !file || !port){  
        return -1;  
    }  

    ptr1 = (char *)(intptr_t)url;  

    if(!strncmp(ptr1,"http://",strlen("http://"))){  
        ptr1 += strlen("http://");  
    }else{  
        return -1;  
    }  

    ptr2 = strchr(ptr1,'/');  
    if(ptr2){  
        len = strlen(ptr1) - strlen(ptr2);  
        memcpy(host,ptr1,len);  
        host[len] = '\0';  
        if(*(ptr2 + 1)){  
            memcpy(file, ptr2 + 1,strlen(ptr2) - 1 );  
            file[strlen(ptr2) - 1] = '\0';  
        }  
    }else{  
        memcpy(host,ptr1,strlen(ptr1));  
        host[strlen(ptr1)] = '\0';  
    }  

    //get host and ip  
    ptr1 = strchr(host,':');  
    if(ptr1){  
        *ptr1++ = '\0';  
        *port = atoi(ptr1);  
    }else{  
        *port = MY_HTTP_DEFAULT_PORT;  
    }  

    return 0;  
}  


static int http_tcpclient_recv(int socket, char *lpbuff){  
    int recvnum = 0;  

    recvnum = uni_recv(socket, lpbuff, BUFFER_SIZE*4, 0);  

    return recvnum;  
}  

static int http_tcpclient_send(int socket, char *buff, int size){  
    int sent=0,tmpres=0;  

    while(sent < size){  
        tmpres = uni_send(socket,buff+sent,size-sent,0);  
        if(tmpres == -1){  
            return -1;  
        }  
        sent += tmpres;  
    }  
    return sent;  
}  

static int htoi(const char * buf, int len){
    int c = 0;
    int i = 0;
    int value = 0;

    while(i < len){
        c = buf[i]; 
        if (isupper(c))
            c = tolower(c);
        value += (c >= '0' && c <= '9' ? c - '0' : c - 'a' + 10);
        if (i != len -1)
            value <<= 4;
        ++i;
    }
    return value;
}

static char *http_parse_result(const char*lpbuf)  
{  
    char *ptmp = NULL;   
    char *response = NULL;  
    int chunked = 0;
    int nchunk = 0;

    if (lpbuf == NULL){
        uni_printf("%s input param is NULL\n", __func__);
        return NULL;
    }
    //printf("%s\n", lpbuf);
    
    ptmp = (char*)strstr(lpbuf,"HTTP/1.1");  
    if(!ptmp){  
        printf("http/1.1 not found\n");  
        return NULL;  
    }  

    if(atoi(ptmp + 9) !=200){  
        printf("result:\n%s\n",lpbuf);  
        return NULL;  
    }  

    ptmp = (char*)strstr(lpbuf, "Transfer-Encoding: chunked");
    if (!ptmp){
        printf("Transfer-Encoding: chunked not found\n");  
        chunked = 0;
    }else {
        chunked = 1;
    }

    ptmp = (char*)strstr(lpbuf,"\r\n\r\n");  
    if(!ptmp){  
        printf("ptmp is NULL\n");  
        return NULL;  
    }  

    if (chunked == 1){
        char *start = ptmp + 4;
        char *end = NULL;   
        end = (char*)strstr(start, "\r\n");
        int len = end - start; 

        len = htoi(start, len);
        //printf("len = %d\n", len);

        while(len != 0){ //TODO: now is only support one chunk
            response = (char*)malloc(len + 1);
            if(!response){  
                printf("malloc failed \n");  
                return NULL;  
            }  
            strncpy(response, end+2, len);  
            response[len] = '\0';

            start = ptmp + 2;
            end = (char*)strstr(start, "\r\n");
            len = end - start; 
            len = htoi(start, len);
            //printf("len = %d\n", len);
            nchunk++;
        }

        printf("chunks = %d\n", nchunk);
        return response;
    }

    response = (char *)malloc(strlen(ptmp)+1);  
    if(!response){  
        printf("malloc failed \n");  
        return NULL;  
    }  
    strcpy(response, ptmp+4);  

    return response;  
}  

char * uni_http_post(const char *url, const char *post_str){  

    int socket_fd = -1;  
    char lpbuf[BUFFER_SIZE*4] = {'\0'};  
    char host_addr[BUFFER_SIZE] = {'\0'};  
    char file[BUFFER_SIZE] = {'\0'};  
    int port = 0;  

    if(!url || !post_str){  
        printf("failed!\n");  
        return NULL;  
    }  

    if(http_parse_url(url, host_addr, file, &port)){  
        printf("http_parse_url failed!\n");  
        return NULL;  
    }  

    printf("host_addr : %s\tfile:%s\t%d\n", host_addr, file, port);  

    socket_fd = http_tcpclient_create(host_addr, port);  
    if(socket_fd < 0){  
        printf("http_tcpclient_create failed\n");  
        return NULL;  
    }  

    sprintf(lpbuf, HTTP_POST, file, host_addr, port, (int)strlen(post_str), post_str);

    if(http_tcpclient_send(socket_fd, lpbuf, strlen(lpbuf)) < 0){  
        printf("http_tcpclient_send failed..\n");  
        return NULL;  
    }  

    printf("发送请求:\n%s\n",lpbuf);  

    /*it's time to recv from server*/  
    if(http_tcpclient_recv(socket_fd, lpbuf) <= 0){  
        printf("http_tcpclient_recv failed\n");  
        return NULL;  
    }  

    http_tcpclient_close(socket_fd);  

    //printf("%s\n", lpbuf);
    return http_parse_result(lpbuf);  
}  

char * uni_http_get(const char *url)  
{  

    int socket_fd = -1;  
    char lpbuf[BUFFER_SIZE*4] = {'\0'};  
    char host_addr[BUFFER_SIZE] = {'\0'};  
    char file[BUFFER_SIZE] = {'\0'};  
    int port = 0;  

    if(!url){  
        printf("      failed!\n");  
        return NULL;  
    }  

    if(http_parse_url(url, host_addr, file ,&port)){  
        printf("http_parse_url failed!\n");  
        return NULL;  
    }  
    //printf("host_addr : %s\tfile:%s\t,%d\n",host_addr,file,port);  

    socket_fd =  http_tcpclient_create(host_addr, port);  
    if(socket_fd < 0){  
        printf("http_tcpclient_create failed\n");  
        return NULL;  
    }  

    sprintf(lpbuf, HTTP_GET, file, host_addr,port);  

    if(http_tcpclient_send(socket_fd,lpbuf,strlen(lpbuf)) < 0){  
        printf("http_tcpclient_send failed..\n");  
        return NULL;  
    }  
    //printf("发送请求:\n%s\n",lpbuf);  

    memset(lpbuf, 0,  BUFFER_SIZE*4);

    if(http_tcpclient_recv(socket_fd,lpbuf) <= 0){  
        printf("http_tcpclient_recv failed\n");  
        return NULL;  
    }  
    http_tcpclient_close(socket_fd);  

    return http_parse_result(lpbuf);  
}  

