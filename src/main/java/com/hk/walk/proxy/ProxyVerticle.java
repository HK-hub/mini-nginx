package com.hk.walk.proxy;

import com.hk.walk.server.ServerVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.*;
import io.vertx.ext.web.Router;

import java.util.Map;

/**
 * @author : HK意境
 * @ClassName : ProxyVerticle
 * @date : 2023/11/11 21:12
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
public class ProxyVerticle extends AbstractVerticle {

    private Router router;

    private HttpServer server;


    /**
     * 代理服务器地址
     */
    private String proxyHost = "127.0.0.1";

    /**
     * 客户端端口
     */
    private int port = 9090;

    /**
     * 启动代理服务器
     * @param startPromise
     * @throws Exception
     */
    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        server = this.vertx.createHttpServer();

        // 客户端
        HttpClientOptions clientOptions = new HttpClientOptions();
        clientOptions.setDefaultHost(this.proxyHost);
        clientOptions.setDefaultPort(ServerVerticle.port);
        HttpClient client = this.vertx.createHttpClient(clientOptions);

        // 反向代理转发所有的请求
        this.proxyHandler(client);

        // 监听端口
        server.listen(port, event -> {
            if (event.succeeded()) {
                // log.info("mini nginx proxy start success on port:{}", port);
            }
            // 启动失败
            if (event.failed()) {
                // log.info("mini nginx proxy start fail on port:{}, cause:", port, event.cause());
            }
        });
    }


    /**
     * 代理处理
     */
    private void proxyHandler(HttpClient client) {

        this.server.requestHandler(request -> {

            // 获取响应对象
            HttpServerResponse response = request.response();
            // 暂停流的读取
            request.pause();
            // 持续传输数据
            response.setChunked(true);

            // 发送请求
            client.request(request.method(), request.uri(), result -> {
                // 异步封装的请求结果
                if (result.succeeded()) {

                    // 获取请求对象
                    HttpClientRequest clientRequest = result.result();
                    clientRequest.setChunked(true);
                    // 设置header
                    clientRequest.headers().addAll(request.headers());

                    // 发送请求
                    clientRequest.send(request).onSuccess(clientResponse -> {

                        // 获取客户端响应结果
                        response.setStatusCode(clientResponse.statusCode())
                                .setStatusMessage(clientResponse.statusMessage());
                        // 响应体
                        clientResponse.handler(response::write);
                        // 结束客户端请求
                        response.send(clientResponse);

                    }).onFailure(error -> {
                        // 请求异常
                        error.printStackTrace();
                        response.setStatusCode(500)
                                .setStatusMessage(error.getMessage());
                    });

                } else {
                    result.cause().printStackTrace();
                    response.setStatusCode(500)
                            .setStatusMessage(result.cause().getMessage());
                }
            });

        });

    }


}
