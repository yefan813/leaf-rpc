package com.leaf.server.netty;

import com.leaf.protocol.RpcDecoder;
import com.leaf.protocol.RpcEncoder;
import com.leaf.protocol.RpcRequest;
import com.leaf.protocol.RpcResponse;
import com.leaf.protocol.serializer.impl.JsonSerializer;
import com.leaf.registry.ServiceRegistry;
import com.leaf.registry.zookeeper.ZkServiceRegistry;
import com.leaf.server.handler.ServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;


/**
 * netty 服务端
 * @author yefan
 */
@Slf4j
@Component
public class NettyServer implements InitializingBean {
    private EventLoopGroup boss;
    private EventLoopGroup worker;

    @Autowired
    private ServerHandler serverHandler;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("=============服务启动向注册中心注册==============");
        ServiceRegistry registry = new ZkServiceRegistry("127.0.0.1:2181");
        start(registry);
    }

    public void start(ServiceRegistry registry) throws Exception {
        log.info("=============服务提供方，准备资源启动远程服务==============");
        boss = new NioEventLoopGroup();
        worker = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boss,worker)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(
                                65535, 0, 4));
                        pipeline.addLast(new RpcEncoder(RpcResponse.class, new JsonSerializer()));
                        pipeline.addLast(new RpcDecoder(RpcRequest.class, new JsonSerializer()));
                        pipeline.addLast(serverHandler);
                    }
                });
        bind(serverBootstrap,8888);
        log.info("=============将服务 ip 和端口注册到 注册中心==============");
//        registry.registry("127.0.0.1:8888");
    }

    public void bind(ServerBootstrap serverBootstrap,int port) {
        log.info("=============服务提供方，开始启动服务==============");
        serverBootstrap.bind(port).addListener(future -> {
            if(future.isSuccess()){
                log.info("服务提供方，启动完毕，端口[ {} ] 绑定成功",port);
            }else {
                log.error("端口[ {} ] 绑定失败", port);
                bind(serverBootstrap, port + 1);
            }
        });
    }

    @PreDestroy
    public void destory() throws InterruptedException {
        log.info("=============服务提供方，服务关闭==============");
        boss.shutdownGracefully().sync();
        worker.shutdownGracefully().sync();
        log.info("关闭Netty");
    }
}
