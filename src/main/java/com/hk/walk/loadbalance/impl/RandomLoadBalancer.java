package com.hk.walk.loadbalance.impl;


import com.hk.walk.loadbalance.AbstractLoadBalancer;
import com.hk.walk.wrapper.HttpClientWrapper;
import io.vertx.core.http.HttpClient;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author : HK意境
 * @ClassName : ConsistentHashLoadBalancer
 * @date : 2023/11/12 21:57
 * @description : 随机负载均衡
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
public class RandomLoadBalancer extends AbstractLoadBalancer {

    private ThreadLocalRandom random = ThreadLocalRandom.current();


    /**
     * 随机负载均衡
     * @param httpClientList
     * @return
     */
    @Override
    public HttpClientWrapper select(String ip, List<HttpClient> httpClientList) {

        int index = random.nextInt(0, httpClientList.size());
        return HttpClientWrapper.of(index, httpClientList.get(index));
    }
}
