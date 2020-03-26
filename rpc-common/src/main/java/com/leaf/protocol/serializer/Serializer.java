package com.leaf.protocol.serializer;

import java.io.IOException;

/**
 * 序列化接口
 * @author yefan
 */
public interface Serializer {

    /**
     * 将对象序列化
     * @param object
     * @return
     * @throws IOException
     */
    byte[] serializer(Object object) throws IOException;


    /**
     * 反序列化
     * @param clazz
     * @param bytes
     * @param <T>
     * @return
     * @throws IOException
     */
    <T> T deserializer(Class<T> clazz, byte[] bytes) throws IOException;


}
