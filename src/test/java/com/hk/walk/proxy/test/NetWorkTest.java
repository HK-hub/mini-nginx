package com.hk.walk.proxy.test;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author : HK意境
 * @ClassName : NetWorkTest
 * @date : 2023/11/22 10:50
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
public class NetWorkTest {

    @Test
    public void testInternet() throws Exception {

        InetAddress baidu = InetAddress.getByName("wwww.baidu.com");
        System.out.println(baidu.getHostName());
        System.out.println(baidu.getHostAddress());
        System.out.println(baidu.isReachable(1000));
    }


}
