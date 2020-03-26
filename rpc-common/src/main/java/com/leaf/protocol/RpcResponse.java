package com.leaf.protocol;

import lombok.Data;

/**
 * @author yefan
 */
@Data
public class RpcResponse {
    /**
     * 请求 id
     */
    private String requestId;

    /**
     * 错误信息
     */
    private String error;


    /**
     * 返回结果
     */
    private Object result;
}
