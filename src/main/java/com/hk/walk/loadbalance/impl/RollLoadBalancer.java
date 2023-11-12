package com.hk.walk.loadbalance.impl;

import com.hk.walk.loadbalance.AbstractLoadBalancer;
import com.hk.walk.wrapper.HttpClientWrapper;
import io.vertx.core.http.HttpClient;
import java.util.List;

/**
 * @author : HK意境
 * @ClassName : RollLoadBalancer
 * @date : 2023/11/12 21:58
 * @description : 轮询负载均衡器
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
public class RollLoadBalancer extends AbstractLoadBalancer {

    /**
     * 轮询选择
     * @param httpClientList
     * @return
     */
    @Override
    public HttpClientWrapper select(String ip, List<HttpClient> httpClientList) {

        // 获取下标
        long count = this.counter.getAndIncrement();
        int index = (int) (count % httpClientList.size());

        HttpClient httpClient = httpClientList.get(index);

        // 如果获取的index 达到最大值
        if (count >= Integer.MAX_VALUE) {
            this.counter.set(index + 1);
        }

        return HttpClientWrapper.of(index, httpClient);
    }
}
