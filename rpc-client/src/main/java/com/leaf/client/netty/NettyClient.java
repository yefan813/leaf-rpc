package com.leaf.client.netty;

import com.leaf.client.future.DefaultFuture;
import com.leaf.client.netty.handler.ClientHandler;
import com.leaf.protocol.RpcDecoder;
import com.leaf.protocol.RpcEncoder;
import com.leaf.protocol.RpcRequest;
import com.leaf.protocol.RpcResponse;
import com.leaf.protocol.serializer.impl.JsonSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 *
 *
 * netty 服务端
 * @author yefan
 *
 * Channel:
 * 是Netty中的网络操作抽象类，对应JDK底层的Socket,它除了包含基本的I/O操作，
 * 如bind(),connect()，read()，write()之外，还包括了Netty框架相关的一些功能，
 * 如获取 Channel的EventLoop
 *
 *
 * EventLoop:
 * 定义了Netty的核心抽象，用于处理连接的生命周期中所发生的事件。EventLoop 为Channel处理I/O操作
 *
 *
 * EventLoopGroup:
 * 实际上就是处理I/O操作的线程池，负责为每个新注册的Channel分配一个EventLoop，
 * Channel在整个生命周期都有其绑定的 EventLoop来服务。
 * NioEventLoop 就是 EventLoop的一个重要实现类，NioEventLoop 是Netty内部的I/O线程，
 * 而 NioEventLoopGroup是拥有 NioEventLoop的线程池，在Netty服务端中一般存在两个这样的NioEventLoopGroup线程池，
 * 一个 “Boss” 线程池，用于接收客户端连接，实际上该线程池中只有一个线程，
 * 一个 “Worker”线程池用于处理每个连接的读写。而Netty客户端只需一个线程池即可，
 * 主要用于处理连接中的读写操作。
 *
 */
@Slf4j
public class NettyClient {
    /**
     * 事件处理器，相当于线程池
     */

    private EventLoopGroup eventLoopGroup;
    /**
     * 相当于 socket
     */

    private Channel channel;
    private ClientHandler clientHandler;
    private String host;
    private Integer port;
    private static final int MAX_RETRY = 5;

    public NettyClient(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    public void connect() throws InterruptedException {
        log.info("=========初始化一下资源，准备建立连接========");
        clientHandler = new ClientHandler();
        eventLoopGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                //指定所使用的NIO传输 Channel
                .channel(NioSocketChannel.class)
                //使用指定的端口设置套接字地址
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        //添加编码器
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(65535, 0, 4));
                        pipeline.addLast(new RpcEncoder(RpcRequest.class, new JsonSerializer()));
                        //添加解码器
                        pipeline.addLast(new RpcDecoder(RpcResponse.class, new JsonSerializer()));
                        //请求处理类
                        pipeline.addLast(clientHandler);
                    }
                });


        connect(bootstrap,host,port,MAX_RETRY);
    }

    /**
     * 失败重连机制
     * @param bootstrap
     * @param host
     * @param port
     * @param retry
     */
    private void connect(Bootstrap bootstrap, String host, int port, int retry) throws InterruptedException {
        log.info("=========开始建立连接========");
        ChannelFuture channelFuture = bootstrap.connect(host, port)
                .addListener(future -> {
                    if(future.isSuccess()){
                        log.info("========连接服务端成功========");
                    } else if(retry == 0) {
                        log.error("==========重试次数已用完，放弃连接=========");
                    }else {
                        //第几次重连：
                        int order = (MAX_RETRY - retry) + 1;
                        //本次重连的间隔
                        int delay = 1 << order;
                        log.error("{} : 连接失败，第 {} 重连....", new Date(), order);
                        bootstrap.config().group().schedule(() -> {
                                    try {
                                        connect(bootstrap, host, port, retry - 1);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                },
                                delay, TimeUnit.SECONDS);
                    }
                });
        channel = channelFuture.sync().channel();
    }

    public RpcResponse send(RpcRequest rpcRequest) {
        log.info("=============发送RPC调用请求=============");
        try {
            channel.writeAndFlush(rpcRequest).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return clientHandler.getRpcResponse(rpcRequest.getRequestId());
    }

    @PreDestroy
    public void close() {
        log.info("=============关闭网络连接=============");
        //优雅关闭时间处理线程池
        eventLoopGroup.shutdownGracefully();
        //管理网络连接
        channel.closeFuture().syncUninterruptibly();
    }
}
