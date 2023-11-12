package com.hk.walk.config;

import io.vertx.core.Vertx;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

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


    private List<Upstream> upstreams;



    /**
     * 初始化上游
     * @param vertx
     */
    public void initUpstream(Vertx vertx) throws Exception {

        if (CollectionUtils.isEmpty(this.upstreams)) {
            this.upstreams = new ArrayList<>();
        }

        // 配置客户端
        for (Upstream upstream : this.upstreams) {
            upstream.init(vertx);
        }
    }

}
