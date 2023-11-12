package com.hk.walk.loadbalance;

import com.hk.walk.wrapper.HttpClientWrapper;
import io.vertx.core.http.HttpClient;

import java.util.List;

/**
 * @author : HK意境
 * @ClassName : LoadBalancer
 * @date : 2023/11/12 21:52
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
public interface LoadBalancer {

    public HttpClientWrapper select(String ip, List<HttpClient> httpClientList);

}
