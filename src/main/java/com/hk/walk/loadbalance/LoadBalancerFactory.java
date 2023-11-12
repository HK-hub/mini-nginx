package com.hk.walk.loadbalance;

import com.hk.walk.enumerate.LoadBalanceEnum;
import com.hk.walk.loadbalance.impl.ConsistentHashLoadBalancer;
import com.hk.walk.loadbalance.impl.HashLoadBalancer;
import com.hk.walk.loadbalance.impl.RandomLoadBalancer;
import com.hk.walk.loadbalance.impl.RollLoadBalancer;

/**
 * @author : HK意境
 * @ClassName : LoadBalancerFactory
 * @date : 2023/11/12 21:53
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
public class LoadBalancerFactory {

    /**
     * 获取负载均衡器
     * @param type
     * @return
     */
    public static LoadBalancer getLoadBalancer(String type) {

        // 轮询
        if (LoadBalanceEnum.Roll.getType().equals(type) || LoadBalanceEnum.Default.getType().equals(type)) {
            return new RollLoadBalancer();
        } else if (LoadBalanceEnum.Random.getType().equals(type)) {
            // 随机
            return new RandomLoadBalancer();
        } else if (LoadBalanceEnum.Hash.getType().equals(type)) {
            // hash
            return new HashLoadBalancer();
        } else if (LoadBalanceEnum.ConsistentHash.getType().equals(type)) {
            return new ConsistentHashLoadBalancer();
        }

        // 默认
        return new RollLoadBalancer();
    }

}
