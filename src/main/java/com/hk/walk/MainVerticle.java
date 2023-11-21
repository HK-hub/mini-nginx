package com.hk.walk;

import com.hk.walk.context.WalkContext;
import com.hk.walk.server.ServerVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class MainVerticle extends AbstractVerticle {

    /**
     * 上下文
     */
    public WalkContext walkContext = WalkContext.getInstance();

    /**
     * 启动参数
     */
    private static String[] arguments = null;


    public static void main(String[] args) {

        arguments = args;
        // 获取配置文件位置
        Vertx.vertx().deployVerticle(new MainVerticle());
        Vertx.vertx().deployVerticle(new ServerVerticle());
    }


    /**
     * 处理命令请求
     */
    public void handler() {


    }



    /**
     * 启动
     * @param startPromise
     * @throws Exception
     */
    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        this.walkContext.readConfig(arguments);
    }




}
