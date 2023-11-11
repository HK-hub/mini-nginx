package com.hk.walk;

import com.hk.walk.proxy.ProxyVerticle;
import com.hk.walk.server.ServerVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

/**
 * @author : HK意境
 * @ClassName : MainVerticle
 * @date : 2023/11/11 20:22
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
public class MainVerticle extends AbstractVerticle {

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new MainVerticle());
        Vertx.vertx().deployVerticle(new ServerVerticle());
        Vertx.vertx().deployVerticle(new ProxyVerticle());
    }

    /**
     * 启动
     * @param startPromise
     * @throws Exception
     */
    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        super.start(startPromise);
    }
}
