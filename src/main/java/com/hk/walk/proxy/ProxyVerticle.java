package com.hk.walk.proxy;

import com.google.gson.Gson;
import com.hk.walk.config.Upstream;
import com.hk.walk.config.WalkConfig;
import com.hk.walk.constant.WalkConstants;
import com.hk.walk.wrapper.HttpClientWrapper;
import io.netty.channel.SimpleChannelInboundHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import java.util.Objects;

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
@Slf4j
public class ProxyVerticle extends AbstractVerticle {

    private HttpServer server;

    private WalkConfig walkConfig;

    /**
     * 代理服务器地址
     */
    private String proxyHost = "127.0.0.1";

    /**
     * 客户端端口
     */
    private int port = 80;


    /**
     * 初始化配置
     */
    private void initConfig() throws Exception {

        // 获取配置文件
        JsonObject jsonConfig = this.config();
        log.info("deploy proxyVerticle use config:{}", jsonConfig);

        // 解析成为对象
        WalkConfig config = new Gson().fromJson(jsonConfig.toString(), WalkConfig.class);
        // 设置配置客户端
        config.initUpstream(this.vertx);

        // 设置端口
        if (Objects.isNull(config.getPort())) {
            config.setPort(WalkConstants.defaultPort);
        }

        log.info("parse config.json to object:{}", config);
        this.port = config.getPort();
        this.walkConfig = config;
    }

    /**
     * 启动代理服务器
     * @param startPromise
     * @throws Exception
     */
    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        // 初始化工作
        this.initConfig();

        this.server = this.vertx.createHttpServer();

        // 反向代理转发所有的请求
        this.proxyHandler();

        // 监听端口
        this.server.listen(this.port, event -> {
            if (event.succeeded()) {
                log.info("mini nginx proxy start success on port:{}", this.port);
            }
            // 启动失败
            if (event.failed()) {
                log.info("mini nginx proxy start fail on port:{}, cause:", this.port, event.cause());
            }
        });
    }


    /**
     * 代理处理
     */
    private void proxyHandler() {

        this.server.requestHandler(request -> {

            // 获取响应对象
            HttpServerResponse response = request.response();
            // 暂停流的读取
            request.pause();
            // 持续传输数据
            response.setChunked(true);

            // 获取请求路径
            String requestPath = request.path();

            //  根据请求路径获取到对应的upstream
            for (Upstream upstream : this.walkConfig.getUpstreams()) {
                // 获取匹配路径的upstream
                // TODO 后续支持正则表达式
                String prefix = upstream.getPath();
                if (requestPath.startsWith(prefix)) {
                    // 请求路径以配置路径开头
                    // 负载均衡选择
                    HttpClientWrapper clientWrapper = upstream.loadBalanceSelect(request.localAddress().host());
                    // 获取目标请求路径
                    String serverUri = upstream.getServerUriList().get(clientWrapper.getIndex());
                    String uri = request.uri().replace(prefix, serverUri);

                    // 发送请求
                    this.sendRequest(clientWrapper.getHttpClient(), request, uri, response);
                    // 结束循环
                    return;
                }
            }

            // 没有找到匹配的upstream
            // 没有找到匹配的
            response.setStatusCode(404).setStatusMessage("no page 404");
            response.end("not found page 404");
        });
    }


    /**
     * 代理发送请求
     * @param client
     * @param request
     * @param uri 请求uri, 如果有请求参数会进行携带
     * @param response
     */
    private void sendRequest(HttpClient client, HttpServerRequest request, String uri, HttpServerResponse response) {

        client.request(request.method(), uri, result -> {
            // 异步封装的请求结果
            if (result.succeeded()) {

                // 获取请求对象
                HttpClientRequest requestUpstream = result.result();
                requestUpstream.setChunked(true);
                // 设置header
                requestUpstream.headers().addAll(request.headers());

                // 发送请求
                requestUpstream.send(request).onSuccess(responseUpstream -> {

                    // 获取客户端响应结果
                    response.setStatusCode(responseUpstream.statusCode())
                            .setStatusMessage(responseUpstream.statusMessage());
                    // 响应header
                    response.headers().addAll(responseUpstream.headers());
                    // 响应体
                    responseUpstream.handler(response::write);
                    // 结束客户端请求
                    response.send(responseUpstream);

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
    }


}
