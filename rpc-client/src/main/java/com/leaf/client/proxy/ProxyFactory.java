package com.leaf.client.proxy;


import java.lang.reflect.Proxy;

/**
 * 创建client 调用 service 代理对象
 */
public class ProxyFactory {
    public static <T> T create(Class<T> interfaceClass) throws Exception {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[] {interfaceClass},
                new RpcClientDynamicProxy<T>(interfaceClass));
    }
}
