package com.leaf.registry.zookeeper;

import com.leaf.constant.Constant;
import com.leaf.registry.ServiceDiscover;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.Watcher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Zookeeper 服务发现
 * @author yefan
 */
public class ZkServiceDiscover implements ServiceDiscover {

    private final CuratorFramework curatorFramework;
    private volatile List<String> dataList = new ArrayList<>();

    public ZkServiceDiscover(String zkAddress) throws Exception {
        curatorFramework = CuratorFrameworkFactory.builder()
                .connectString(zkAddress)
                .sessionTimeoutMs(Constant.ZK_SESSION_TIMEOUT)
                .connectionTimeoutMs(Constant.ZK_CONNECTION_TIMEOUT)
                .retryPolicy(new ExponentialBackoffRetry(1000,3))
                .build();
        watchChildNode(curatorFramework);
    }

    private void watchChildNode(CuratorFramework curatorFramework) throws Exception {
        curatorFramework.start();
        List<String> nodeList = curatorFramework.getChildren().usingWatcher((Watcher) watchedEvent -> {
            if (watchedEvent.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                try {
                    watchChildNode(curatorFramework);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).forPath(Constant.ZK_REGISTRY_PATH);

        for (String node: nodeList) {
            byte[] path = curatorFramework.getData()
                    .forPath(Constant.ZK_REGISTRY_PATH + "/" + node);
            dataList.add(new String(path));
        }
    }

    @Override
    public String discover() {
        String data = null;
        int size = dataList.size();
        if (size > 0) {
            if (size == 1) {
                data = dataList.get(0);
            } else {
                data = dataList.get(ThreadLocalRandom.current().nextInt(size));
            }
        }
        return data;
    }
}
