package com.leaf.registry;

/**
 * 服务注册
 * @author yefan
 */
public interface ServiceRegistry {
    /**
     * 服务注册
     * @param data
     * @throws Exception
     */
    void registry(String data) throws Exception;
}
