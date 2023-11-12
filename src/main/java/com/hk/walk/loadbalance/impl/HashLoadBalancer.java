package com.hk.walk.loadbalance.impl;

import com.hk.walk.loadbalance.AbstractLoadBalancer;
import com.hk.walk.wrapper.HttpClientWrapper;
import io.vertx.core.http.HttpClient;
import java.util.List;

/**
 * @author : HK意境
 * @ClassName : ConsistentHashLoadBalancer
 * @date : 2023/11/12 21:57
 * @description : hash 负载均衡
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
public class HashLoadBalancer extends AbstractLoadBalancer {


    /**
     * ip hash负载均衡策略
     * @param ip
     * @param httpClientList
     * @return
     */
    @Override
    public HttpClientWrapper select(String ip, List<HttpClient> httpClientList) {

        // 其实同一个ip 的 hashCode() 在同一个应用实例中是相同的，也就是说具有一致性
        int hashedCode = ip.hashCode();
        int index = hashedCode % httpClientList.size();

        return HttpClientWrapper.of(index, httpClientList.get(index));
    }
}
