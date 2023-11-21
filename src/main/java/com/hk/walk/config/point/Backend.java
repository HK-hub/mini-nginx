package com.hk.walk.config.point;

import com.hk.walk.constant.WalkConstants;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @author : HK意境
 * @ClassName : Backend
 * @date : 2023/11/20 10:16
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Data
public class Backend {

    /**
     * 服务端地址
     */
    private String server;

    /**
     * 权重: 1-N, N表示服务器的数量
     */
    private Integer weight = WalkConstants.DEFAULT_SERVER_WEIGHT;

}
