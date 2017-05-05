#ifndef __ZNETTY_H__
#define __ZNETTY_H__

#include "uni_stdio.h"
#include "uni_stdlib.h"
#include "uni_unistd.h"
#include "uni_string.h"
#include "uni_types.h"
#include "uni_http.h"
#include "uni_socket.h"
#include "uni_select.h"
#include "uni_inet.h"
#include "uni_url.h"
#include "uni_pthread.h"
#include "cJSON.h"
#include "uni_err.h"
#include "uni_sys.h"
#include "uni_errno.h"

#define UNI_NETTY_KEY_LEN (48)
#define UNI_NETTY_UDID_LEN (16)

#define UNI_NETTY_CLINETID_LEN (32)
#define UNI_NETTY_IP_LEN (16)
#define UNI_NETTY_PASSWORD_LEN (36)
#define UNI_NETTY_PROTOCAL_LEN (8)
#define UNI_NETTY_USERNAME_LEN (32)

/*register param*/
typedef struct netty{
    uni_char raw_key[UNI_NETTY_KEY_LEN];
    uni_char raw_udid[UNI_NETTY_UDID_LEN];
    uni_s32 subsystemId;
    uni_s32 appOsType;
} netty_t;

/*for https param, not used*/
typedef struct zssl{
    uni_char ssl[32];
    uni_char sslPasswrod[32];
}zssl_t;

/*setup netty link param*/
typedef struct linkprm {
    uni_char clientId[UNI_NETTY_CLINETID_LEN];
    uni_char ip[UNI_NETTY_IP_LEN];
    uni_s32 keepAlive; /*s*/
    uni_s16 port;
    uni_char password[UNI_NETTY_PASSWORD_LEN];
    uni_char protocol[UNI_NETTY_PROTOCAL_LEN];
    uni_char username[UNI_NETTY_PASSWORD_LEN];
    uni_char reserved;
} linkprm_t;

enum link_status{
    NT_LINK_INCONNECTED,
    NT_LINK_CONNECTED,
};

/*tcp link*/
typedef struct llink {
    uni_s32 fd;
    uni_s32 state; /* connected, or disconnect */
    uni_u32 dataId; /*for data send*/
    uni_u32 dataflag;/*1: data send; 0: ack recv*/
}llink_t;

/*evnet loop backend*/
struct select_ctx{
    uni_s32 nfds;
    uni_fd_set *rfds;
    uni_fd_set *wfds;
    uni_fd_set *efds;
};

/*event loop object*/
typedef struct event {
   struct select_ctx ctx; /*backend*/ 
   uni_s32 loop; /*for event loop, True or False*/
   uni_s32 alive; /* for heartbeat, True or False*/
   uni_u32 dataId; /*for data ack send*/
} event_t;

/*netty object*/
typedef struct tag_znetty{
    const uni_char * url;
    const uni_char * req;
    linkprm_t linkprm;
    netty_t param;
    llink_t link;
    event_t event;
    uni_mutex_t lock;/* */
} znetty_t;

/*msg type*/
enum netty_msg_type{
    NT_INVALID, 
    NT_CONNECT, /*c2s*/
    NT_CONNACK, /*s2c*/
    NT_PINGREQ, /*c2s*/
    NT_PINGRESP, /*s2c*/
    NT_DISCONNECT,/*c2s*/
    NT_EVENT, /*s2c*/
    NT_DATA, /*c2s or s2c*/
    NT_DATAACK,/*c2s or s2c*/

    NT_COUNT
};

/* messgae --- HLLLD...*/
/*msg format*/
struct nt_msg_raw {
   uni_char head;  /* bit 0-3, msg type; bit4-7 reserved*/
   uni_u32 len;    /*3bytes, need BE, local  LE*/
   uni_char *data;
};

/*msg entity*/
struct nt_msg{
    uni_char version; /*just for register*/
    struct nt_msg_raw raw;
    uni_char *buf;
};

/*channel steup status*/
enum netty_con_status{
    NT_ST_SUCC,
    NT_ST_ERR_VER,
    NT_ST_ERR_CLID,
    NT_ST_ERR_SRV,
    NT_ST_ERR_AUTH,
    NT_ST_ERR_CREDIT,
};

/*event msg*/
enum netty_event{
    NT_EV_INV_DEV_TOKEN=1, /*设备token失效*/
    NT_EV_INV_USR_TOKEN,/*用户登录token失效*/
    NT_EV_CHG_PASS,/*用户修改密码*/
    NT_EV_PASSIVE,/*用户被提出*/
};

#endif
