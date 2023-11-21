package com.hk.walk.function;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @ClassName : SerializableFunction
 * @author : HK意境
 * @date : 2023/11/21 15:12
 * @description :
 * @Todo :
 * @Bug :
 * @Modified :
 * @Version : 1.0
 */
@FunctionalInterface
public interface SerializableFunction<T, R> extends Function<T, R>, Serializable {
 
}