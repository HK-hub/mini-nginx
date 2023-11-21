package com.hk.walk.config.log;

import com.hk.walk.constant.WalkConstants;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @author : HK意境
 * @ClassName : LogItem
 * @date : 2023/11/15 19:58
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Data
public class LogItem {

    /**
     * 存放路径
     */
    private String dir;

    /**
     * 日志名称及滚动规则: 支持参数%date表示日期(yyy-MM-dd), %i表示日志索引(从1开始),
     */
    private String rule;

    /**
     * 单个日志最大大小,默认1024超过进行滚动
     */
    private Integer size = WalkConstants.DEFAULT_LOG_SIZE;


    /**
     * 日志大小单位,可选[KB,MB,GB],默认KB
     */
    private String unit = WalkConstants.DEFAULT_LOG_SIZE_UNIT;


}
