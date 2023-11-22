package com.hk.walk.enumerate;


import io.vertx.core.http.HttpVersion;
import lombok.Getter;

/**
 * @author : HK意境
 * @ClassName : HttpProtocolEnum
 * @date : 2023/11/22 9:19
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Getter
public enum HttpProtocolEnum {


    HTTP_1_0("http1"), HTTP_1_1("http1.1"), HTTP_2("http2"),
    WEBSOCKET("websocket");

    /**
     * 使用协议及其版本
     */
    public final String protocol;

    HttpProtocolEnum(String protocol) {
        this.protocol = protocol;
    }


    /**
     * 是否是Websocket协议
     * @param protocol
     * @return
     */
    public boolean isWebSocket(String protocol) {

        return WEBSOCKET.protocol.equalsIgnoreCase(protocol);
    }


    /**
     * 返回对应的Http 协议版本
     * @param protocol
     * @return
     */
    public static HttpVersion getHttpVersion(String protocol) {

        if (HTTP_1_0.protocol.equalsIgnoreCase(protocol)) {
            return HttpVersion.HTTP_1_0;
        } else if (HTTP_1_1.protocol.equalsIgnoreCase(protocol)) {
            return HttpVersion.HTTP_1_1;
        } else if (HTTP_2.protocol.equalsIgnoreCase(protocol)) {
            return HttpVersion.HTTP_2;
        }

        // 都不是
        return HttpVersion.HTTP_1_1;
    }

}
