package com.hk.walk.proxy;

import com.google.gson.Gson;
import com.hk.walk.config.Frontend;
import com.hk.walk.config.Upstream;
import com.hk.walk.config.WalkConfig;
import com.hk.walk.constant.WalkConstants;
import com.hk.walk.wrapper.HttpClientWrapper;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.ProxyOptions;
import io.vertx.ext.web.Router;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
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

    private Router router;

    private WalkConfig walkConfig;

    /**
     * 代理服务器地址
     */
    private String proxyHost = "127.0.0.1";

    /**
     * 客户端端口
     */
    private int port;


    /**
     * 初始化配置
     */
    private void initConfig() throws Exception {

        // 获取配置文件
        JsonObject jsonConfig = this.config();
        log.info("deploy proxyVerticle use config:{}", jsonConfig);

        // 解析成为对象
        this.walkConfig = new Gson().fromJson(jsonConfig.toString(), WalkConfig.class);
        // 设置配置客户端
        this.walkConfig.init(this.vertx, this.router);

        log.info("parse config.json to object:{}", this.walkConfig);
        this.port = this.walkConfig.getPort();
    }

    /**
     * 启动代理服务器
     *
     * @param startPromise
     *
     * @throws Exception
     */
    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        this.server = this.vertx.createHttpServer();
        this.router = Router.router(this.vertx);

        // 初始化工作
        this.initConfig();

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
     * WebSocket处理
     * @param client
     * @param request
     * @param uri
     * @param response
     * @return
     */
    private boolean webSocketHandle(HttpClient client, HttpServerRequest request, String uri, HttpServerResponse response) {

        String upgrade = request.getHeader(WalkConstants.WEBSOCKET_UPGRADE_HEADER);
        if (Objects.isNull(upgrade) || BooleanUtils.isFalse(WalkConstants.WEBSOCKET_UPGRADE_PROTOCOL.equalsIgnoreCase(upgrade))) {
            // 非websocket 请求
            return false;
        }

        // 解决WebSocket 扩展压缩的问题
        String header = request.getHeader(WalkConstants.HEADER_EXTENSIONS);

        // 升级成为WebSocket 协议
        request.toWebSocket()
                .onSuccess(serverWebSocket -> {
                    // 结束握手
                    serverWebSocket.accept();

                    // 进行连接服务器WebSocket
                    WebSocketConnectOptions options = new WebSocketConnectOptions();
                    options.setURI(uri).setHeaders(request.headers()).setAllowOriginHeader(true);

                    // 连接WebSocket
                    client.webSocket(options)
                            // 连接成功
                            .onSuccess(clientWebSocket -> {
                                // server(浏览器)端携带的frame -> 写入代理端
                                serverWebSocket.frameHandler(frame -> {
                                    log.info("browser send message:{}", frame.textData());
                                    clientWebSocket.writeFrame(frame);
                                });

                                // 代理端产生frame -> 写入server端
                                clientWebSocket.frameHandler(frame -> {
                                    log.info("upstream response message:{}", frame.textData());
                                    serverWebSocket.writeFrame(frame);
                                });

                                // server端关闭WebSocket 连接
                                serverWebSocket.closeHandler(close -> clientWebSocket.close());

                                // 客户端关闭WebSocket 连接
                                clientWebSocket.closeHandler(close -> serverWebSocket.close());

                            }).onFailure(error -> {
                                errorResponse(request.response(), request, error);
                            });
                }).onFailure(error -> {
                    // 升级失败
                    log.info("request:{} upgrade to websocket failed:", request.uri(), error);
                    errorResponse(response, request, error);
                });

        // 命中webSocket,此处异步执行
        return true;
    }


    /**
     * 代理处理
     */
    private void proxyHandler() {

        this.server.requestHandler(request -> {

            // 获取响应对象
            HttpServerResponse response = request.response();
            // 获取请求路径
            String requestPath = request.path();

            // 处理前端资源请求
            boolean proxyFronted = proxyFronted(request, requestPath);
            if (BooleanUtils.isTrue(proxyFronted)) {
                // 前端代理成功
                return;
            }

            // 暂停流的读取
            request.pause();
            // 持续传输数据
            response.setChunked(true);

            //  根据请求路径获取到对应的upstream
            boolean proxyUpstream = proxyUpstream(request, requestPath, response);
            if (BooleanUtils.isTrue(proxyUpstream)) {
                // 上游代理成功
                return;
            }

            // 没有找到匹配的upstream
            response.setStatusCode(404).setStatusMessage("no page 404");
            response.end("not found page 404");
        });
    }



    /**
     * 上游代理
     * @param request
     * @param requestPath
     * @param response
     * @return
     */
    private boolean proxyUpstream(HttpServerRequest request, String requestPath, HttpServerResponse response) {
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

                // 处理WebSocket 请求
                boolean webSocketHandle = this.webSocketHandle(clientWrapper.getHttpClient(), request, uri, response);
                if (BooleanUtils.isTrue(webSocketHandle)) {
                    // ws 处理成功，结束链路
                    return true;
                }

                // 发送请求
                this.requestHandle(clientWrapper.getHttpClient(), request, uri, response);
                // 结束循环
                return true;
            }
        }
        return false;
    }


    /**
     * 前端静态页面，资源代理
     * @param request
     * @param requestPath
     * @return
     */
    private boolean proxyFronted(HttpServerRequest request, String requestPath) {
        for (Frontend frontend : this.walkConfig.getFrontends()) {
            if (requestPath.startsWith(frontend.getPath())) {
                this.router.handle(request);
                return true;
            }
        }

        // 解决/static/*请求
        if (requestPath.startsWith(WalkConstants.STATIC_RESOURCE_PATH)) {
            if (Objects.nonNull(this.walkConfig.getRoot())) {
                this.router.handle(request);
                return true;
            }
        }
        return false;
    }


    /**
     * 代理发送请求
     *
     * @param client
     * @param request
     * @param uri      请求uri, 如果有请求参数会进行携带
     * @param response
     */
    private void requestHandle(HttpClient client, HttpServerRequest request, String uri, HttpServerResponse response) {

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
                    log.info("proxy client request failure:", error);
                    errorResponse(response, request, error);
                });

            } else {
                log.info("proxy client request failure:", result.cause());
                errorResponse(response, request, result.cause());
            }
        });
    }


    /**
     * 错误处理
     *
     * @param response
     * @param error
     */
    private void errorResponse(HttpServerResponse response, HttpServerRequest request, Throwable error) {

        // 携带header
        response.headers().addAll(request.headers());
        // 错误信息
        response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).setStatusMessage(error.getMessage())
                .end(error.getMessage());
    }


}
