package com.hk.walk.launcher;

import com.hk.walk.server.ServerVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Launcher;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

/**
 * @author : HK意境
 * @ClassName : WalkLauncher
 * @date : 2023/11/12 17:44
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
public class WalkLauncher extends Launcher {

    public static void main(String[] args) {

        // 处理运行和配置文件

        // 启动代理实例
        new WalkLauncher().dispatch(args);
        // 部署Server
        Vertx.vertx().deployVerticle(new ServerVerticle());

    }



    /**
     * 配置解析之后
     * @param config the read config, empty if none are provided.
     */
    @Override
    public void afterConfigParsed(JsonObject config) {

        // 解析配置文件

        super.afterConfigParsed(config);
    }

    /**
     * 启动实例之前
     * @param options the configured Vert.x options. Modify them to customize the Vert.x instance.
     */
    @Override
    public void beforeStartingVertx(VertxOptions options) {
        super.beforeStartingVertx(options);
    }


    /**
     * 启动之后
     * @param vertx the created Vert.x instance
     */
    @Override
    public void afterStartingVertx(Vertx vertx) {
        super.afterStartingVertx(vertx);
    }


    /**
     * 部署实例之前
     * @param deploymentOptions the current deployment options. Modify them to customize the deployment.
     */
    @Override
    public void beforeDeployingVerticle(DeploymentOptions deploymentOptions) {
        super.beforeDeployingVerticle(deploymentOptions);
    }


    /**
     * 停止实例之前
     * @param vertx the {@link Vertx} instance, cannot be {@code null}
     */
    @Override
    public void beforeStoppingVertx(Vertx vertx) {
        super.beforeStoppingVertx(vertx);
    }


    /**
     * 停止实例之后
     */
    @Override
    public void afterStoppingVertx() {
        super.afterStoppingVertx();
    }


    /**
     * 部署失败
     * @param vertx             the vert.x instance
     * @param mainVerticle      the verticle
     * @param deploymentOptions the verticle deployment options
     * @param cause             the cause of the failure
     */
    @Override
    public void handleDeployFailed(Vertx vertx, String mainVerticle, DeploymentOptions deploymentOptions, Throwable cause) {
        super.handleDeployFailed(vertx, mainVerticle, deploymentOptions, cause);
    }
}
