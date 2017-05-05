#include "znetty.h"

#ifdef UNI_NETTY_MODULE

static znetty_t znetty = 
{
    .url = "http://10.30.11.16:8088/rest/v1/client/register",
    .req = "appKey=%s&udid=%s&subsystemId=%d&appOsType=%d",

    .param.raw_key = "4vv55dzjavtvm7kc4pcskzpze7p3jnkwpeb6bjaw",
    .param.raw_udid = "wangggtest011",
    .param.subsystemId = 0,
    .param.appOsType = 4,

    .link.fd = -1,
    .link.state = NT_LINK_INCONNECTED,
    .link.dataId = 0,
    .link.dataflag = 0,

    .event.loop = 1,
    .event.ctx = {0},
};

typedef uni_s32 (*nt_func)(uni_s32);

static uni_s32 nt_send_dataack(uni_void);

static uni_s32 nt_beg(uni_s32 step);
static uni_s32 nt_register(uni_s32 step);
static uni_s32 nt_link_connect(uni_s32 step);
static uni_s32 nt_link_exit(uni_s32 step);
static uni_s32 nt_channel_connect(uni_s32 step);
static uni_s32 nt_channel_exit(uni_s32 step);
static uni_s32 nt_end(uni_s32 step);
static uni_s32 nt_msg_loop(uni_s32 step);
static uni_s32 nt_init(uni_s32 step);
static uni_s32 nt_deinit(uni_s32 step);

static nt_func steps[]= {
    &nt_beg,
    &nt_init,
    &nt_register,
    &nt_link_connect,
    &nt_channel_connect,
    &nt_msg_loop,
    &nt_channel_exit,
    &nt_link_exit,
    &nt_deinit,
    &nt_end,
    NULL,
};

static uni_s32 nt_send(uni_s32 sockfd, const uni_void *buf, size_t len, uni_s32 flags) {
    uni_s32 left = len;
    while(left){
        left = uni_send(sockfd, (uni_void*)(intptr_t)buf, len, flags);
        if (left == -1){
            uni_printf("%s send error\n", __func__);
            return -1;
        }
        left = len - left;
    }
    return len;
}

static uni_s32 nt_recv(uni_s32 fildes, uni_void *buf, size_t nbyte, uni_s32 flags){
    uni_s32 len = 0;
    while(len != nbyte){
        len += uni_recv(fildes, buf, nbyte-len, flags);
    }
    return nbyte;
}

static uni_s32 nt_beg(uni_s32 step){
    uni_printf("netty start\n");
    uni_printf("netty step %d\n", step);
    return 0;
}

static uni_s32 nt_end(uni_s32 step){
    uni_printf("netty step %d\n", step);
    uni_printf("netty exit\n");
    return 0;
}

static uni_s32 nt_res_parse(cJSON * res) {
    memset((uni_char*)&znetty.linkprm, 0, sizeof(znetty.linkprm));
    /*
        "result":   {
        "clientId": "djgY2A1ukhSasAC4gkjUdd",
        "connection":   {
            "ip":   "10.20.0.45",
            "keepAlive":    "60",
            "password": "7c5bb1415e714a6a8aa2e14f46424cbe",
            "port": "8113",
            "protocol": "tcp",
            "username": "f034dfaaa3384a09beddd615cd6760e8"
        }
     * */
    strncpy(znetty.linkprm.clientId, cJSON_GetObjectItem(res, "clientId")->valuestring, UNI_NETTY_CLINETID_LEN);
    cJSON *conn = cJSON_GetObjectItem(res, "connection");
    if (conn == NULL){
        uni_printf("%s json does not have \'connection\' filed\n", __func__);
        return -1;
    }

    znetty.linkprm.keepAlive = atoi(cJSON_GetObjectItem(conn, "keepAlive")->valuestring);
    uni_printf("keepAlive=%d\n", znetty.linkprm.keepAlive);
    znetty.linkprm.port = atoi(cJSON_GetObjectItem(conn, "port")->valuestring);
    uni_printf("port=%d\n", znetty.linkprm.port);
    strncpy(znetty.linkprm.ip, cJSON_GetObjectItem(conn, "ip")->valuestring, UNI_NETTY_IP_LEN);
    strncpy(znetty.linkprm.password, cJSON_GetObjectItem(conn, "password")->valuestring, UNI_NETTY_PASSWORD_LEN);
    strncpy(znetty.linkprm.username, cJSON_GetObjectItem(conn, "username")->valuestring, UNI_NETTY_USERNAME_LEN);
    strncpy(znetty.linkprm.protocol, cJSON_GetObjectItem(conn, "protocol")->valuestring, UNI_NETTY_PROTOCAL_LEN);

    if (!strncmp(znetty.linkprm.protocol, "ssl", strlen("ssl"))) {
        /*TODO: ssl param*/
    }
    
    return 0;
}

static uni_s32 nt_reg_parse(const char *res){

    uni_s32 ret = 0;
    //uni_printf("%s\n", res);
    cJSON* root = cJSON_Parse(res);
    if (!root){
        printf("Error Before: [%s]\n", cJSON_GetErrorPtr());
        return -1;
    }
    uni_printf("%s\n", cJSON_Print(root));

    cJSON *retCode = cJSON_GetObjectItem(root, "returnCode");
    cJSON *costTime = cJSON_GetObjectItem(root, "costTime");
    cJSON *msg = cJSON_GetObjectItem(root, "message");
    cJSON *result = cJSON_GetObjectItem(root, "result");

    char *code = msg->valuestring;

    uni_printf("cost:%s\tretcode:%s\tmsg:%s\n", 
               costTime->valuestring,
               retCode->valuestring,
               msg->valuestring
              );
    
    if (!(strncmp(code, "mc_0000", strlen("mc_0000")) && strncmp(code, "mc_0008", strlen("mc_0008") ))){
        uni_printf("netty clinet registe error\n"); 
        return -1;
    }

    ret = nt_res_parse(result);

    cJSON_Delete(root);
    return ret;
}

static uni_s32 nt_register(uni_s32 step){
    uni_printf("netty step %d\n", step);

    uni_char *post_buf = uni_malloc(255);
    if (post_buf == NULL){
        return -1;
    }

    uni_s32 len = 0;
    uni_char *key = uni_url_encode(znetty.param.raw_key, strlen(znetty.param.raw_key), &len);
    uni_char *udid = uni_url_encode(znetty.param.raw_udid, strlen(znetty.param.raw_udid), &len);

    snprintf(post_buf, 255, znetty.req, key, udid, znetty.param.subsystemId, znetty.param.appOsType);

    //uni_printf("%s\n", post_buf);

    uni_char *res = uni_http_post(znetty.url, post_buf);

    if (res == NULL){
        uni_printf("%s http post error\n", __func__);
        return -1;
    }

    uni_s32 ret = nt_reg_parse(res);
    if (ret == -1){
        return -1;
    }

    free(res);
    free(udid);
    free(key);
    free(post_buf);

    return 0;
}

static uni_s32 nt_link_connect(uni_s32 step){
    uni_printf("netty step %d\n", step);

    struct sockaddr_in srv = {0};
    uni_s32 ret = 0;

    uni_printf("port=%d ip=%s\n", znetty.linkprm.port, znetty.linkprm.ip);

    srv.sin_family = AF_INET;
    srv.sin_port = htons(znetty.linkprm.port);
    ret = uni_inet_aton(znetty.linkprm.ip, &srv.sin_addr);
    if (ret == 0){
        uni_printf("%s ip addr error\n", __func__);
        return -1;
    }

    uni_printf("%x %x\n", (uni_u32)srv.sin_addr.s_addr, (uni_u32)srv.sin_port);

    znetty.link.fd = uni_socket(AF_INET, SOCK_STREAM, 0);
    if (znetty.link.fd == -1){
        uni_printf("%s socket error\n", __func__);
        return -1;
    }

    ret = uni_connect(znetty.link.fd, (struct sockaddr *)&srv, sizeof(srv));
    if (ret == -1){
        uni_printf("%s connect error\n", __func__);
        return -1;
    }

    uni_printf("link connect\n");
    return 0;
}

static uni_s32 nt_mk_msg(struct nt_msg *msg){
#define NT_HEAD_LEN_SIZE 4
#define NT_HEAD_SIZE 1
#define NT_DATA_ID_SIZE 4

    enum{
        NO_ID,
        LOCAL_ID,
        REMOTE_ID,
    };
    uni_s32 total = 0;
    uni_s32 withdata = 0;
    uni_s32 withlen = 0;
    uni_s32 withdataid = 0;

    switch(msg->raw.head){
        case NT_DISCONNECT:
        case NT_PINGREQ:
            if (msg->raw.data != NULL && msg->raw.len != 0){ /*no data msg*/
                return -1;
            }
            total = NT_HEAD_SIZE + msg->raw.len;
            withlen = 0;
            withdata = 0;
            withdataid = NO_ID;
            break;
        case NT_CONNECT:
            if (msg->raw.data == NULL || msg->raw.len == 0){ /*with data msg*/
                return -1;
            }
            total = NT_HEAD_LEN_SIZE + msg->raw.len;
            withlen = 1;
            withdata = 1;
            withdataid = NO_ID;
            break;
        case NT_DATA:
            if (msg->raw.data == NULL || msg->raw.len == 0){ /*with data msg*/
                return -1;
            }
            msg->raw.len += NT_DATA_ID_SIZE;
            total = NT_HEAD_LEN_SIZE + msg->raw.len;
            withlen = 1;
            withdata = 1;
            withdataid = LOCAL_ID;/*from local host*/
            break;
        case NT_DATAACK:
            if (msg->raw.data != NULL && msg->raw.len != 0){ /*no data msg*/
                return -1;
            }
            msg->raw.len += NT_DATA_ID_SIZE;
            total = NT_HEAD_LEN_SIZE + msg->raw.len;
            withlen = 1;
            withdata = 0;
            withdataid = REMOTE_ID;/*from remote end*/
            break;
        default:
            return -1;
            break;
    }

    msg->buf = uni_malloc(total);
    if (msg->buf == NULL){
        uni_printf("%s malloc error\n", __func__);
        return -1;
    }

    msg->buf[0] = msg->raw.head; /*head*/
    if (withlen){
        /*be, but local is le*/
        msg->buf[3] = ((uni_char*)&msg->raw.len)[0]; /*len*/
        msg->buf[2] = ((uni_char*)&msg->raw.len)[1];
        msg->buf[1] = ((uni_char*)&msg->raw.len)[2];

        uni_printf("0x%x 0x%x 0x%x 0x%x\n", msg->buf[0], msg->buf[1], msg->buf[2], (uni_char)msg->buf[3]);

        if (withdataid == NO_ID){
            if (withdata){
                strncpy(&msg->buf[4], msg->raw.data, msg->raw.len); /*data*/
            }
        } else {
            uni_s32 dataId = 0;

            if (withdataid == LOCAL_ID){
                if (znetty.link.dataflag == 0){
                    dataId = ++znetty.link.dataId;
                    znetty.link.dataflag = 1;
                } else {
                    uni_printf("%s not recv dataid ack(%d)\n", __func__, znetty.link.dataId);
                    return -1;
                }
            }else {/*REMOTE_ID*/
                dataId = znetty.event.dataId;
            }

            /*neeed BE, but local is LE*/
            msg->buf[7] = ((uni_char*)&dataId)[0];/*id*/
            msg->buf[6] = ((uni_char*)&dataId)[1];
            msg->buf[5] = ((uni_char*)&dataId)[2];
            msg->buf[4] = ((uni_char*)&dataId)[3];

            if (withdata){
                strncpy(&msg->buf[8], msg->raw.data, msg->raw.len - NT_DATA_ID_SIZE); /*data*/
            }

            uni_printf("0x%x 0x%x 0x%x 0x%x\n", msg->buf[4], msg->buf[5], msg->buf[6], msg->buf[7]);
        }

        uni_printf("data = \"%s\"\n", msg->raw.data);
        uni_printf("len = %d(0x%x)\n", msg->raw.len, msg->raw.len);
    }

    return total;
}

static uni_s32 nt_channel_connect(uni_s32 step) {
#define NT_CONNT_BUF_SIZE 512
#define NT_CONNT_VALUE "version=%d;clientId=%s;username=%s;password=%s;keepAlive=%d"

    uni_printf("netty step %d\n", step);
    uni_s32 len = 0;

    struct nt_msg msg = {0};
    msg.version = 1;
    msg.raw.head = NT_CONNECT;
    msg.raw.data = uni_malloc(NT_CONNT_BUF_SIZE);
    if (msg.raw.data == NULL){
        uni_printf("%s malloc error \n", __func__);
        return -1;
    }

    snprintf(msg.raw.data, NT_CONNT_BUF_SIZE,\
                           NT_CONNT_VALUE, \
                           msg.version, \
                           znetty.linkprm.clientId, \
                           znetty.linkprm.username, \
                           znetty.linkprm.password, \
                           znetty.linkprm.keepAlive
                           );

    msg.raw.len = strlen(msg.raw.data);
    len = nt_mk_msg(&msg);
    if (len <= 0){
        uni_printf("%s make netty message error\n", __func__);
        return -1;
    }

    uni_s32 ret = nt_send(znetty.link.fd, msg.buf, len, 0);
    if (ret != len){
        uni_printf("%s send error\n", __func__);
        return -1; 
    }

    if (msg.raw.data)
        uni_free(msg.raw.data);

    if (msg.buf)
        uni_free(msg.buf);

    return 0;
}

static uni_u32 nt_check_len(uni_char *head){
    uni_u8 * phead = (uni_u8*)head;
    uni_u32 len = 0;
    //len =  (head[1] & 0xff) << 16 | (head[2] & 0xff) << 8 | (head[3] & 0xff);
    len =  phead[1] << 16 | phead[2] << 8 | phead[3];
    uni_printf("head len %#4x(%#x %#x %#x)\n", len, head[1], head[2], head[3]);
    return len;
}

static uni_s32 nt_check_status(uni_char *state){
    uni_s32 ret = 0;
    char *p = uni_strstr(state, "=");
    if (p == NULL){
        uni_printf("%s not find string\n", __func__);
        return -1;
    }

    uni_s32 st = atoi(p+1);
    //uni_printf("%d\n", st);
    switch(st){
        case NT_ST_SUCC:
            znetty.link.state = NT_LINK_CONNECTED;
            uni_printf("%s Channel setup\n", __func__);
            ret = 0;
            break;
        case NT_ST_ERR_VER:
            ret = -1;
            break;
        case NT_ST_ERR_CLID:
            ret = -1;
            break;
        case NT_ST_ERR_SRV:
            ret = -1;
            break;
        case NT_ST_ERR_AUTH:
            ret = -1;
            break;
        case NT_ST_ERR_CREDIT:
            ret = -1;
            break;
        default:
            uni_printf("%s invalid param\n", __func__);
            return -1;
            break;
    }

    uni_printf("data = \"%s\"\n", state);
    uni_printf("%s CONNECT ACK response %d\n", __func__, st);
    return ret;
}

static uni_s32 nt_check_event(uni_char *state){
    uni_s32 ret = 0;
    char *p = uni_strstr(state, "=");
    if (p == NULL){
        uni_printf("%s not find string\n", __func__);
        return -1;
    }

    uni_s32 st = atoi(p+1);
    //uni_printf("%d\n", st);
    switch(st){
        case NT_EV_INV_DEV_TOKEN:
            ret = -1;
            break;
        case NT_EV_INV_USR_TOKEN:
            ret = -1;
            break;
        case NT_EV_CHG_PASS:
            ret = -1;
            break;
        case NT_EV_PASSIVE:
            ret = -1;
            break;
        default:
            uni_printf("%s invalid param\n", __func__);
            return -1;
            break;
    }

    uni_printf("data = \"%s\"\n", state);
    uni_printf("%s event %d\n", __func__, st);
    return ret;
}

static uni_s32 nt_check_data(uni_char *data){
    uni_u8 * pdata = (uni_u8*)data;
    uni_s32 ret = 0;
    uni_u32 id = 0;
    /*be, but local is le*/
    id = pdata[0] << 24 | pdata[1] << 16 | pdata[2] << 8 | pdata[3];
    znetty.event.dataId = id;

    uni_printf("data = \"%s\"\n", data+4);
    uni_printf("%s DATA recv id(%d)\n", __func__, id);
    return ret;
}

static uni_s32 nt_check_dataack(uni_char *data){

    uni_pthread_mutex_lock(&znetty.lock);
    uni_u8 * pdata = (uni_u8*)data;
    uni_s32 ret = 0;
    uni_u32 id = 0;
    id = pdata[0] << 24 | pdata[1] << 16 | pdata[2] << 8 | pdata[3];
    if (id != znetty.link.dataId){
        uni_printf("%s DATA ACK id(%d) is not same to send DATA id(%d)\n", __func__, id, znetty.link.dataId);
        uni_pthread_mutex_unlock(&znetty.lock);
        ret = -1;
    }
    znetty.link.dataflag = 0;
    uni_pthread_mutex_unlock(&znetty.lock);
    uni_printf("%s DATA ACK response id(%d)\n", __func__, id);
    return ret;
}

static uni_s32 nt_channel_exit(uni_s32 step){
    uni_printf("netty step %d\n", step);

    struct nt_msg msg= {0};
    msg.raw.head = NT_DISCONNECT;
    msg.raw.data = NULL;
    msg.raw.len = 0;

    uni_s32 len = nt_mk_msg(&msg);
    if (len <= 0){
        uni_printf("%s make msg error\n", __func__);
        return -1;
    }

    uni_s32 ret = nt_send(znetty.link.fd, msg.buf, len, 0);
    if (ret != len){
        uni_printf("%s send msg error\n", __func__);
        return -1;
    }

    if (msg.raw.data){
        uni_free(msg.raw.data);
    }
    if (msg.buf){
        uni_free(msg.buf);
    }

    return 0;
}

static uni_s32 nt_link_exit(uni_s32 step){
    uni_printf("netty step %d\n", step);

    if (znetty.link.fd != -1){
        uni_socket_close(znetty.link.fd);
    }

    znetty.link.fd = -1;
    znetty.link.state = NT_LINK_INCONNECTED;
    return 0;
}

static uni_s32 nt_heart_beat(uni_s32 step){
    uni_printf("netty step %d\n", step);

    uni_pthread_mutex_lock(&znetty.lock);
    if (znetty.event.alive == 1){
        uni_printf("heartbeat has send but not recive response\n");
        uni_pthread_mutex_unlock(&znetty.lock);
        return -1;
    }

    struct nt_msg msg= {0};
    msg.raw.head = NT_PINGREQ;
    msg.raw.data = NULL;
    msg.raw.len = 0;
    uni_s32 len = nt_mk_msg(&msg);
    if (len <= 0){
        uni_printf("%s make msg error\n", __func__);
        uni_pthread_mutex_unlock(&znetty.lock);
        return -1;
    }

    uni_s32 ret = nt_send(znetty.link.fd, msg.buf, len, 0);
    if (ret != len){
        uni_printf("%s send msg error\n", __func__);
        uni_pthread_mutex_unlock(&znetty.lock);
        return -1;
    }

    if (msg.raw.data){
        uni_free(msg.raw.data);
    }

    if (msg.buf){
        uni_free(msg.buf);
    }

    /*heartbeat req send*/
    znetty.event.alive = 1;

    uni_pthread_mutex_unlock(&znetty.lock);
    uni_printf("send heartbeat request\n");
    return 0;
}

static uni_s32 nt_msg_route(uni_s32 step){
    uni_printf("netty step %d\n", step);

    uni_char head[4] = {0};
    uni_s32 ret = nt_recv(znetty.link.fd , head, 1, 0);
    if (ret != 1){
        uni_printf("%s read error\n", __func__);
        return -1;
    }

    switch(head[0]) {
        case NT_PINGRESP:
            {
                uni_pthread_mutex_lock(&znetty.lock);
                /*recv heartbeat response*/
                znetty.event.alive = 0;
                uni_pthread_mutex_unlock(&znetty.lock);
                uni_printf("recv heartbeat response\n");
            }
            return 0;
            break;
        case NT_CONNACK:
        case NT_EVENT:
        case NT_DATA:
        case NT_DATAACK:
            {
                uni_s32 ret = nt_recv(znetty.link.fd , &head[1], 3, 0);
                if (ret != 3){
                    uni_printf("%s read error\n", __func__);
                    return -1;
                }

                uni_s32 len = nt_check_len(head);
                if (len < 0){
                    uni_printf("%s recv head len error\n", __func__);
                    return -1; 
                }

                char * data = uni_malloc(len + 1);
                if (data == NULL){
                    uni_printf("%s malloc error\n", __func__);
                    return -1;
                }
                memset(data, 0, len + 1);

                ret = nt_recv(znetty.link.fd, data, len, 0);
                if (ret != len){
                    uni_printf("%s read error \n", __func__);
                }

                ret = 0;
                if (head[0] == NT_EVENT){
                    uni_printf("recv EVENT\n");
                    ret = nt_check_event(data);

               }else if (head[0] == NT_CONNACK){
                   uni_printf("recv CONNECT ACK\n");
                   ret = nt_check_status(data);
                   if (ret == 0){
                       nt_heart_beat(step);
                   }
               }else if (head[0] == NT_DATA){
                   uni_printf("recv DATA \n");
                   ret = nt_check_data(data);
                   ret = nt_send_dataack();

                   //nt_proc_data(data+4);

               }else if (head[0] == NT_DATAACK){
                   uni_printf("recv DATA ACK\n");
                   ret = nt_check_dataack(data);

               }else{
                    ret = -1;
                }

                uni_free(data);
                return ret;
            }
            break;
        default:
            return -1;
            break;
    }

    return 0;
}

static uni_s32 nt_msg_loop(uni_s32 step){
    uni_s32 cnt = 0;
    uni_fd_set rfds;
    znetty.event.loop = 1;
    uni_printf("event loop\n");

    while(znetty.event.loop) {
        FD_ZERO(&rfds);
        FD_SET(znetty.link.fd, &rfds);
        znetty.event.ctx.nfds = znetty.link.fd + 1;
        znetty.event.ctx.rfds = &rfds;

        struct timeval timeout = {0};
        timeout.tv_sec = znetty.linkprm.keepAlive;
        timeout.tv_usec = 0;

        struct select_ctx *pctx = &znetty.event.ctx;

        uni_s32 n = uni_select(pctx->nfds, pctx->rfds, pctx->wfds, pctx->efds, &timeout);
        if (n == -1){
            if (errno == EINTR){
                continue;
            }
            uni_printf("%s select error\n", __func__);
            perror("slect error\n");
            znetty.event.loop = 0; /*exit loop*/
        }else if (n == 0){
            /*heartbeat*/
            int ret = nt_heart_beat(step);

            if (ret != 0){
                znetty.event.loop = 0;/*exit loop*/
            }

        }else{
            cnt++;
            uni_s32 i = 0;
            for(i=0; i < n; i++){
                if (FD_ISSET(znetty.link.fd, pctx->rfds)){
                    /*msg type: pingresp, event, dataack*/
                    uni_s32 ret = nt_msg_route(step);
                    if (cnt == 10){
                        /*heartbeat*/
                        nt_heart_beat(step);
                        cnt = 0;
                    }

#if 1
                    if (cnt % 20 == 0){

                        uni_s32 nt_send_data(uni_char* data, uni_u32 nbytes);
                        char * data = "test xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxddddddddddddddddddddddddddddddddddddddddddddddddddsdfsdfadfsfalllllllll";
                        nt_send_data(data, strlen(data));
                    }
#endif


                    if (ret != 0){
                        znetty.event.loop = 0;/*exit loop*/
                    }
                }
            }
        }
    }
    return 0;
}

static uni_s32 nt_init(uni_s32 step){
    uni_printf("netty step %d\n", step);
    uni_pthread_mutex_init(&znetty.lock, NULL);
    return 0;
}

static uni_s32 nt_deinit(uni_s32 step){
    uni_printf("netty step %d\n", step);
    uni_pthread_mutex_destroy(&znetty.lock);
    return 0;
}

static uni_s32 nt_send_dataack(uni_void){
    int len = 0;

    uni_pthread_mutex_lock(&znetty.lock);

    if (znetty.link.state != NT_LINK_CONNECTED) {
        uni_printf("%s netty link not setuped\n", __func__);
        uni_pthread_mutex_unlock(&znetty.lock);
        return -1;
    }

    struct nt_msg msg = {0};
    msg.raw.head = NT_DATAACK;
    msg.raw.data = NULL;
    msg.raw.len = 0;

    len = nt_mk_msg(&msg);
    if (len <= 0){
        uni_printf("%s make netty message error\n", __func__);
        uni_pthread_mutex_unlock(&znetty.lock);
        return -1;
    }

    uni_s32 ret = nt_send(znetty.link.fd, msg.buf, len, 0);
    if (ret == len){
        ret = 0;
    }

    if (msg.buf){
        uni_free(msg.buf);
    }

    uni_pthread_mutex_unlock(&znetty.lock);
    return ret;
}

uni_void do_netty(uni_void){
    uni_s32 i = 0;
    uni_s32 ret = 0;
    while(1) {
        if (steps[i] != NULL){
            ret = (*steps[i])(i);
            if (ret != 0){
                uni_printf("step %d error\n", i);
                return ;
            }
        }else{
            break;
        }
        ++i;
    }
}

static uni_void* uni_netty_thread_main(uni_void* param){

    while(1){
        do_netty();
    }
    return NULL;
}


#define NETTY_TASK "netty_task"
uni_pthread_t netty_thread;

#if defined(UNI_REALTEK_CHIP_8711A)
#define NETTY_PARAM "netty_params"
TaskHandle_t netty_task = NULL;
#endif

#if defined(UNI_PLATFORM_QCOM)
#define NETTY_THREAD_SIZE (5 * 1024)
#elif defined(UNI_REALTEK_CHIP_8711A)
#define NETTY_THREAD_SIZE     (2028)   // 512 * size_t(rtl8195a:16bit --> 2world)
#define NETTY_PRIORITY        tskIDLE_PRIORITY + 1
#endif

#endif

uni_s32 nt_send_data(uni_char* data, uni_u32 nbytes){
    int len = 0;
    uni_pthread_mutex_lock(&znetty.lock);
    if (znetty.link.state != NT_LINK_CONNECTED) {
        uni_printf("%s netty link not setuped\n", __func__);
        uni_pthread_mutex_unlock(&znetty.lock);
        return -1;
    }

    struct nt_msg msg = {0};
    msg.raw.head = NT_DATA;
    msg.raw.data = data;
    msg.raw.len = nbytes;

    len = nt_mk_msg(&msg);
    if (len <= 0){
        uni_printf("%s make netty message error\n", __func__);
        uni_pthread_mutex_unlock(&znetty.lock);
        return -1;
    }

    uni_s32 ret = nt_send(znetty.link.fd, msg.buf, len, 0);

    if (ret == len){
        ret = 0;
    }

    if (msg.buf){
        uni_free(msg.buf);
    }

    uni_pthread_mutex_unlock(&znetty.lock);
    return ret;
}



uni_s32 uni_netty_start(uni_void)
{
#ifdef UNI_NETTY_MODULE
    uni_s32 val = -1;

#if defined(UNI_PLATFORM_QCOM)
    uni_memset(&netty_thread, 0, sizeof( uni_pthread_t ));
    netty_thread.mem_size = NETTY_THREAD_SIZE;
    netty_thread.entry_input = UNI_PTHREAD_PLAYLIST_ENTYR_INPUT;
    uni_memcpy(netty_thread.name_ptr, NETTY_TASK, strlen(NETTY_TASK));
#elif defined(UNI_REALTEK_CHIP_8711A)
    uni_memset(&netty_thread, 0, sizeof( uni_pthread_t ));
    uni_memcpy(netty_thread.name, NETTY_TASK, strlen(NETTY_TASK));
    netty_thread.us_stack_depth = NETTY_THREAD_SIZE;
    netty_thread.param = NETTY_PARAM;
    netty_thread.priority = NETTY_PRIORITY;
    netty_thread.task = &netty_task;
#endif  // UNI_PLATFORM_QCOM

    val = uni_pthread_create(&netty_thread, NULL, uni_netty_thread_main, NULL);
    if (val != 0) {
       uni_printf("Failed to create the %s thread .\n", NETTY_TASK);
       uni_errno(UNI_NETTY_M, UNI_START_ERR);
       return UNI_FAIL;
    }

    uni_printf("netty start\n");
    uni_msleep(10);
#endif
    return UNI_OK;
}

uni_s32 uni_netty_stop(uni_void){
#ifdef UNI_NETTY_MODULE
    uni_printf("netty stop\n");
#endif
    return 0;
} 
