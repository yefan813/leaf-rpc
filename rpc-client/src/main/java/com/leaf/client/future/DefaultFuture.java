package com.leaf.client.future;

import com.leaf.protocol.RpcResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yefan
 */
@Slf4j
public class DefaultFuture {
    private RpcResponse rpcResponse;
    private volatile boolean isSucceed = false;
    private final Object object = new Object();

    public RpcResponse getRpcResponse(int timeout) {
        synchronized (object) {
            while (!isSucceed) {
                try {
                    log.info("=============wait(),从DefaultFuture获取数据=============");
                    object.wait(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return rpcResponse;
        }
    }

    public void setRpcResponse(RpcResponse response) {
        if(isSucceed){
            return;
        }
        synchronized (object) {
            log.info("=============设置同步调用结果到DefaultFuture对象,唤醒等待中线程获取结果=============");
            this.rpcResponse = response;
            this.isSucceed = true;
            object.notify();
        }
    }
}
