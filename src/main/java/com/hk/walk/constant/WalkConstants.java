package com.hk.walk.constant;

import com.google.gson.Gson;

/**
 * @author : HK意境
 * @ClassName : WalkConstants
 * @date : 2023/11/12 21:18
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
public class WalkConstants {


    public static final Gson gson = new Gson();


    /**
     * 配置文件位置参数项目
     */
    public static final String CONFIG_PATH_ARGUMENT = "-walkConfig";

    /**
     * 默认配置文件所在文件夹名称
     */
    public static final String DEFAULT_CONFIG_DIRECTORY = "walkConfig";

    /**
     * 配置应用启动端口参数项目
     */
    public static final String CONFIG_PORT_ARGUMENT = "-walkPort";


    /**
     * 默认监听端口
     */
    public static final int defaultPort = 80;

    /**
     * 默认日志文件大小
     */
    public static final int DEFAULT_LOG_SIZE = 1024;

    /**
     * 默认日志大小单位
     */
    public static final String DEFAULT_LOG_SIZE_UNIT = "KB";

    /**
     * 静态资源路径
     */
    public static final String STATIC_RESOURCE_PATH = "/static";

    /**
     * websocket升级头
     */
    public static final String WEBSOCKET_UPGRADE_HEADER = "Upgrade";


    /**
     * websocket升级协议
     */
    public static final String WEBSOCKET_UPGRADE_PROTOCOL = "websocket";

    /**
     * WebSocket 扩展
     */
    public static final String HEADER_EXTENSIONS = "Sec-WebSocket-Extensions";

    /**
     * 默认服务端权重
     */
    public static final int DEFAULT_SERVER_WEIGHT = 1;

}
