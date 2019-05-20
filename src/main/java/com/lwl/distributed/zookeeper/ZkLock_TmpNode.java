package com.lwl.distributed.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;

/**
 * @author liuweilong
 * @description
 * @date 2019/5/17 11:44
 */
public class ZkLock_TmpNode {
    public boolean lock(String key){
        CuratorFramework client = CuratorFrameworkFactory.newClient("119.3.210.17:2181",
                new RetryNTimes(3, 1000));
        client.start();
        try {
            client.create().withMode(CreateMode.EPHEMERAL)
                    .forPath("/lock/" + key, "value".getBytes());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) {
        boolean asdf = new ZkLock_TmpNode().lock("asdf");
        boolean aasd = new ZkLock_TmpNode().lock("asdf");
    }
}
