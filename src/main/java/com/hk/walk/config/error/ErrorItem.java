package com.hk.walk.config.error;

import lombok.Getter;
import lombok.Setter;

/**
 * @author : HK意境
 * @ClassName : ErrorItem
 * @date : 2023/11/15 20:11
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Setter
@Getter
public class ErrorItem {

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 错误资源页面
     */
    private String dir;

}
