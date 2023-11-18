package com.hk.walk.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.*;
import io.vertx.core.http.impl.WebSocketImplBase;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import lombok.extern.slf4j.Slf4j;

import javax.security.auth.login.AccountException;
import java.text.DateFormat;
import java.time.Instant;
import java.util.Date;

/**
 * @author : HK意境
 * @ClassName : ServerVerticle
 * @date : 2023/11/11 20:25
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Slf4j
public class ServerVerticle extends AbstractVerticle {

    public static int port = 8080;

    private Router router;

    /**
     * 启动服务端
     *
     * @param startPromise
     *
     * @throws Exception
     */
    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        // 服务和路由
        HttpServer server = this.vertx.createHttpServer();
        this.router = Router.router(vertx);


        // webSocket 支持
        router.route("/websocket").handler(context -> {
            HttpServerRequest request = context.request();
            request.toWebSocket().onSuccess(webSocket -> {
                log.info("8080 websocket connect success");
                webSocket.writeTextMessage("hello");
                /*webSocket.writeFrame(WebSocketFrame.textFrame("this is a ping message", true));
                webSocket.frameHandler(frame -> {
                    log.info("8080 receive message:{}", frame.textData());
                    webSocket.writeFrame(WebSocketFrame.textFrame(frame.textData() + ":" + System.currentTimeMillis(), true));
                });*/
            });
        });


        SockJSBridgeOptions opts = new SockJSBridgeOptions()
                .addOutboundPermitted(new PermittedOptions().setAddress("feed"));

        SockJSHandler ebHandler = SockJSHandler.create(vertx);
        router.mountSubRouter("/eventbus", ebHandler.bridge(opts));


        // Create a router endpoint for the static content.
        router.route().handler(StaticHandler.create());


        EventBus eb = vertx.eventBus();
        vertx.setPeriodic(1000, t -> {
            // Create a timestamp string
            String timestamp = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
                    .format(Date.from(Instant.now()));

            eb.publish("feed", new JsonObject().put("now", timestamp));
        });



        // 静态资源
        /*this.router.get("/index.html").handler(StaticHandler.create("")
                .setCachingEnabled(true).setMaxAgeSeconds(65535));*/


        // 设置路由和处理器
        this.router.get("/hello").handler(context -> {

            HttpServerResponse response = context.response();
            response.end("hello vert.x");
        });

        this.router.get("/a/hello").handler(context -> {

            HttpServerResponse response = context.response();
            response.end("hello vert.x by a");
        });

        this.router.get("/b/hello").handler(context -> {

            HttpServerResponse response = context.response();
            response.end("hello vert.x by b");
        });

        this.router.post("/post")
                .handler(BodyHandler.create())
                .handler(context -> {
                    JsonObject jsonObject = context.body().asJsonObject();
                    HttpServerResponse response = context.response();
                    response.end(jsonObject.toString());
                });



        this.router.post("/b/post")
                .handler(BodyHandler.create())
                .handler(context -> {
                    JsonObject jsonObject = context.body().asJsonObject();
                    HttpServerResponse response = context.response();
                    response.end(jsonObject.toString());
                });


        // 错误路由处理
        this.errorHandle();

        // 启动服务
        server.requestHandler(router).listen(port, event -> {
            if (event.succeeded()) {
                log.info("mini nginx server start success on port:{}", port);
            }
            // 启动失败
            if (event.failed()) {
                log.info("mini nginx server start fail on port:{}, cause:", port, event.cause());
            }
        });
    }


    /**
     * 错误路由处理
     */
    public void errorHandle() {

        // 404 错误
        this.router.errorHandler(404, context -> {

        });

        // 500 错误
        this.router.errorHandler(500, context -> {

        });
    }


}
