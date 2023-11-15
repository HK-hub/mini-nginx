package com.hk.walk.config;

import com.hk.walk.config.cache.Cache;
import lombok.Getter;
import lombok.Setter;

/**
 * @author : HK意境
 * @ClassName : Frontend
 * @date : 2023/11/15 17:40
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Getter
@Setter
public class Frontend {

    /**
     * 请求路径
     */
    private String path;

    /**
     * 资源对应路径
     */
    private String dir;

    /**
     * 缓存策略
     */
    private Cache cache;

    /**
     * 资源未找到重新路由
     */
    private String rewrite;
}
