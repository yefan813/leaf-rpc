package com.leaf.registry.zookeeper;

import com.leaf.constant.Constant;
import com.leaf.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

/**
 * zk 服务注册
 * @author yefan
 */
@Slf4j
public class ZkServiceRegistry implements ServiceRegistry {

    private final CuratorFramework curatorFramework;
    public ZkServiceRegistry(String address) {
        log.info("========连接 zookeeper========");
        this.curatorFramework = CuratorFrameworkFactory.builder()
                .connectString(address)
                .sessionTimeoutMs(Constant.ZK_SESSION_TIMEOUT)
                .retryPolicy(new ExponentialBackoffRetry(1000,3))
                .build();
    }

    @Override
    public void registry(String data) throws Exception {
        log.info("========ZK 服务注册data:[{}]========", data);
        curatorFramework.start();
        String path = Constant.ZK_CHILDREN_PATH;

        curatorFramework.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath(path ,data.getBytes());
    }
}
