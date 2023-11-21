package com.hk.walk.config.cache;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @author : HK意境
 * @ClassName : Cache
 * @date : 2023/11/15 20:27
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Data
public class Cache {

    /**
     * 是否启用缓存
     */
    private Boolean enable = false;

    /**
     * 最大缓存秒数
     */
    private Long maxAge = 65535L;

}
