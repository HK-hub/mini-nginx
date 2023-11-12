package com.hk.walk.wrapper;

import io.vertx.core.http.HttpClient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author : HK意境
 * @ClassName : HttpClientWrapper
 * @date : 2023/11/12 23:09
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Setter
@Getter
@NoArgsConstructor
public class HttpClientWrapper {

    /**
     * 负载均衡列表中的索引
     */
    private int index;

    private HttpClient httpClient;

    /**
     * client 请求目标uri
     */
    private String uri;

    public HttpClientWrapper(int index, HttpClient httpClient) {
        this.index = index;
        this.httpClient = httpClient;
    }

    /**
     * 构造包装器
     * @param index
     * @param httpClient
     * @return
     */
    public static HttpClientWrapper of(int index, HttpClient httpClient) {
        return new HttpClientWrapper(index, httpClient);
    }

}
