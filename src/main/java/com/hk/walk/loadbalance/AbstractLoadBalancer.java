package com.hk.walk.loadbalance;


import java.util.concurrent.atomic.AtomicLong;

/**
 * @author : HK意境
 * @ClassName : AbstractLoadBalancer
 * @date : 2023/11/12 22:02
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
public abstract class AbstractLoadBalancer implements LoadBalancer{

    protected AtomicLong counter = new AtomicLong(0);


}
