package com.hk.walk.config;

import com.hk.walk.config.cache.Cache;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private String location;

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


    /**
     * 自定义请求头
     * TODO 后续支持表达式方法，OGNL表达式
     */
    private Map<String, List<String>> headers = new LinkedHashMap<>();
}
