package com.leaf.client.netty.handler;

import com.leaf.client.future.DefaultFuture;
import com.leaf.protocol.RpcRequest;
import com.leaf.protocol.RpcResponse;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;
import sun.misc.Request;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * 响应处理类
 * @author yefan
 */
@Slf4j
public class ClientHandler extends ChannelDuplexHandler {
    /**
     * 使用Map维护请求对象ID与响应结果Future的映射关系
     */
    private final Map<String, DefaultFuture> futureMap = new ConcurrentHashMap<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof RpcResponse){
            //获取响应对象
            RpcResponse response = (RpcResponse) msg;
            log.info("========获取到服务端响应，得到响应结果，获取 requestId:[{}],对应的DefaultFuture=========",
                    response.getRequestId());
            DefaultFuture defaultFuture = futureMap.get(response.getRequestId());
            //将结果写入DefaultFuture
            defaultFuture.setRpcResponse(response);
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if(msg instanceof RpcRequest){
            RpcRequest request = (RpcRequest) msg;
            log.info("=============发送 Rpc 请求之前，构建一个与响应Future的映射关系requestId:[{}]=============",
                    request.getRequestId());
            //发送请求对象之前，先把请求ID保存下来，并构建一个与响应Future的映射关系
            futureMap.putIfAbsent(request.getRequestId(), new DefaultFuture());
        }
        super.write(ctx, msg, promise);
    }

    public RpcResponse getRpcResponse(String requestId) {
        log.info("=============从futureMap获取响应requestId:[{}]=============", requestId);
        try {
            DefaultFuture future = futureMap.get(requestId);
            return future.getRpcResponse(5000);
        } finally {
            //获取成功以后，从map中移除
            futureMap.remove(requestId);
        }
    }
}
