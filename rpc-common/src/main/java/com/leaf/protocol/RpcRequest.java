package com.leaf.protocol;

import lombok.Data;
import lombok.ToString;

/**
 *
 * @author yefan
 */
@Data
@ToString
public class RpcRequest {
    /**
     * 请求 id
     */
    private String requestId;
    /**
     * 请求类名
     */
    private String className;
    /**
     * 请求方法名
     */
    private String methodName;
    /**
     * 请求参数类型
     */
    private Class<?>[] parameterTypes;
    /**
     * 请求参数
     */
    private Object[] parameters;
}
