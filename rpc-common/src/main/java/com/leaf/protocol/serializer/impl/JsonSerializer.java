package com.leaf.protocol.serializer.impl;

import com.alibaba.fastjson.JSON;
import com.leaf.protocol.serializer.Serializer;

import java.io.IOException;

/**
 * json 序列化方式实现
 * @author yefan
 */
public class JsonSerializer implements Serializer {

    @Override
    public byte[] serializer(Object object) throws IOException {
        return JSON.toJSONBytes(object);
    }

    @Override
    public <T> T deserializer(Class<T> clazz, byte[] bytes) throws IOException {
        return JSON.parseObject(bytes,clazz);
    }
}
