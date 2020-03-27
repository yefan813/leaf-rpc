package com.leaf.client.proxy;


import com.leaf.client.netty.NettyClient;
import com.leaf.protocol.RpcRequest;
import com.leaf.protocol.RpcResponse;
import com.leaf.registry.ServiceDiscover;
import com.leaf.registry.zookeeper.ZkServiceDiscover;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

/**
 * RPC client调用代理，使用JDK静态代理
 *
 * @author yefan
 */
@Slf4j
public class RpcClientDynamicProxy<T> implements InvocationHandler {
    private Class<T> clazz;

    private ServiceDiscover discover = new ZkServiceDiscover("127.0.0.1:2181");

    public RpcClientDynamicProxy(Class<T> clazz) throws Exception {
        this.clazz = clazz;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
        log.info("=======调用代理类，构造RpcRequest==========");
        RpcRequest rpcRequest = new RpcRequest();
        String requestId = UUID.randomUUID().toString();

        String clazzName = method.getDeclaringClass().getName();
        String methodName = method.getName();

        Class<?>[] parameterTypes = method.getParameterTypes();

        rpcRequest.setRequestId(requestId);
        rpcRequest.setClassName(clazzName);
        rpcRequest.setParameterTypes(parameterTypes);
        rpcRequest.setMethodName(methodName);
        rpcRequest.setParameters(params);
        log.info("请求内容: {}", rpcRequest);

        //治理可以用 ZK 服务发现获取注册地址
        String address = discover.discover();
        String[] arrays = address.split(":");
        if(ArrayUtils.isEmpty(arrays)){
            throw new RuntimeException("服务发现失败！");
        }
        String host = arrays[0];
        int port = Integer.parseInt(arrays[1]);

        NettyClient nettyClient = new NettyClient("127.0.0.1", 8888);
        log.info("开始连接服务端：{}", new Date());
        nettyClient.connect();
        RpcResponse rpcResponse = nettyClient.send(rpcRequest);
        log.info("请求调用返回结果：{}", rpcResponse.getResult());
        return rpcResponse.getResult();
    }
}
