package com.hk.walk.proxy;


import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author : HK意境
 * @ClassName : ProxyVerticleTest
 * @date : 2023/11/13 22:49
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Slf4j
@ExtendWith(VertxExtension.class)
class ProxyVerticleTest {

    /**
     * 启动Server 服务
     * @param vertx
     * @param testContext
     */
    @BeforeEach
    public void setUp(Vertx vertx, VertxTestContext testContext) throws IOException {
        log.info("deploy server...");
        vertx.deployVerticle(new ServerVerticle(), testContext.succeedingThenComplete());

        // 配置文件读取
        DeploymentOptions options = new DeploymentOptions();
        String configContent = Files.readString(Path.of("src/main/resources/config.json"));
        options.setConfig(new JsonObject(configContent));

        // 部署代理服务器
        vertx.deployVerticle(new ProxyVerticle(), options, testContext.succeedingThenComplete());
        testContext.completeNow();
    }


    @Test
    public void testServer(Vertx vertx, VertxTestContext testContext) {

        WebClient client = WebClient.create(vertx);
        client.get(8080, "127.0.0.1", "/hello")
                .send()
                .onSuccess(response -> {
                    log.info("request response:{}", response.bodyAsString());
                    Assertions.assertEquals(response.statusCode(), 200);
                    Assertions.assertEquals(response.bodyAsString(), "hello vert.x");
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);

    }

    /**
     * 测试代理服务
     * @param vertx
     * @param testContext
     */
    @Test
    public void testProxyServer(Vertx vertx, VertxTestContext testContext) {

        WebClient client = WebClient.create(vertx);
        client.get(9090, "127.0.0.1", "/a/hello")
                .send()
                .onSuccess(response -> {
                    log.info("request response:{}", response.bodyAsString());
                    Assertions.assertEquals(response.statusCode(), 200);
                    Assertions.assertEquals(response.bodyAsString(), "hello vert.x");
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }

}