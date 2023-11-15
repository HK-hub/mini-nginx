package com.hk.walk.config;

import com.hk.walk.config.log.LogItem;
import lombok.Getter;
import lombok.Setter;

/**
 * @author : HK意境
 * @ClassName : Logs
 * @date : 2023/11/15 20:09
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Getter
@Setter
public class Logs {

    /**
     * 访问日志
     */
    private LogItem access;

    /**
     * 错误日志
     */
    private LogItem error;
}
