package com.hk.walk.enumerate;

import lombok.Getter;

/**
 * @author : HK意境
 * @ClassName : LoadBalanceEnum
 * @date : 2023/11/12 20:53
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@Getter
public enum LoadBalanceEnum {

    Default("roll"),
    Random("random"),
    Roll("roll"),
    Hash("hash"),
    ConsistentHash("consistent_hash")
    ;


    private String type;


    LoadBalanceEnum(String type) {
        this.type = type;
    }
}
