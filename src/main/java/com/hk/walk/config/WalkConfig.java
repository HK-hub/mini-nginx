package com.hk.walk.config;

import com.hk.walk.config.cache.Cache;
import com.hk.walk.config.cache.CacheControl;
import com.hk.walk.config.error.ErrorItem;
import com.hk.walk.constant.WalkConstants;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.FileSystemAccess;
import io.vertx.ext.web.handler.StaticHandler;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author : HK意境
 * @ClassName : WalkConfig
 * @date : 2023/11/12 20:43
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Data
public class WalkConfig {

    /**
     * 端口
     */
    private Integer port;

    /**
     * 日志
     */
    private Logs logs;

    /**
     * 前端静态资源
     */
    private List<Frontend> frontends;

    /**
     * 根路径/对应的前端资源, 会成为默认的首页，静态资源加载目录
     */
    private String root;


    /**
     * 代理的服务器
     */
    private List<Upstream> upstreams;

    /**
     * 错误处理
     */
    private List<ErrorItem> errors;



    /**
     * 初始化上游
     * @param vertx
     */
    private void initUpstream(Vertx vertx) throws Exception {

        if (CollectionUtils.isEmpty(this.upstreams)) {
            this.upstreams = new ArrayList<>();
        }

        // 配置客户端
        for (Upstream upstream : this.upstreams) {
            upstream.init(vertx);
        }
    }


    /**
     * 配置初始化
     * @param vertx
     * @param router
     * @throws Exception
     */
    public void init(Vertx vertx, Router router) throws Exception {

        // 设置端口
        if (Objects.isNull(this.port)) {
            this.setPort(WalkConstants.defaultPort);
        }

        // 初始化服务器上游
        this.initUpstream(vertx);

        // 初始化前端资源配置
        this.initFrontend(router);

        // 初始化日志
        this.initLog();

        // 初始化错误处理
        this.initErrorHandle();
    }


    /**
     * 初始化错误处理
     */
    private void initErrorHandle() {

    }


    /**
     * 初始化日志
     */
    private void initLog() {

    }


    /**
     * 初始化前端资源
     */
    private void initFrontend(Router router) {

        if (CollectionUtils.isEmpty(this.frontends)) {
            this.frontends = new ArrayList<>();
        }

        // 遍历获取前端资源路径
        for (Frontend frontend : this.frontends) {

            // 静态资源处理器
            StaticHandler staticHandler = StaticHandler.create(FileSystemAccess.ROOT, frontend.getDir());

            // 缓存配置
            Cache cache = frontend.getCache();
            if (Objects.nonNull(cache) && BooleanUtils.isTrue(cache.getEnable())) {
                staticHandler.setCachingEnabled(cache.getEnable());
                // 缓存时间
                if (Objects.nonNull(cache.getMaxAge())) {
                    staticHandler.setMaxAgeSeconds(cache.getMaxAge());
                }
            }

            // 设置路由
            router.route(frontend.getPath())
                    .handler(routerContext -> {
                        // 禁用缓存
                        if (Objects.nonNull(cache) && BooleanUtils.isFalse(cache.getEnable())) {
                            MultiMap headers = routerContext.response().headers();
                            headers.add("Cache-Control", CacheControl.NO_CACHE)
                                    .add("Cache-Control", CacheControl.NO_STORE);
                        }

                        routerContext.next();
                    })
                    // 允许读取文件系统文件,资源文件绝对地址
                    .handler(staticHandler);
        }

        // /static静态资源
        router.route("/static/*")
                .handler(StaticHandler.create(FileSystemAccess.ROOT, this.root + "/static"));


        // 如果此时前端没有读取到数据
        router.errorHandler(404, context -> {
            String path = context.request().path();
            for (Frontend frontend : this.frontends) {
                if (path.startsWith(frontend.getPath()) && StringUtils.isNotBlank(frontend.getRewrite())) {
                    // 重新路由
                    context.reroute(frontend.getRewrite());
                    return;
                }
            }

            // 没有找到重路由地址
            // TODO 后期执行全局错误状态策略
            context.response().setStatusCode(404).end("not found");
        });

    }

}
