package com.lwl.distributed.zookeeper;

import com.lwl.distributed.IDistributedLock;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author liuweilong
 * @description
 * @date 2019/5/17 11:44
 */
@Service("zkTmp")
public class ZkLock_TmpSortNode implements IDistributedLock {
    @Value("${zk.client}")
    private String connectionString;
    @Override
    public boolean lock(String key, String value) {
        CuratorFramework client = CuratorFrameworkFactory.newClient(connectionString,
                new RetryNTimes(3, 1000));
        client.start();
        try {
            client.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath("/" + key, value.getBytes());
            return true;
        } catch (Exception e) {
            return false;
        }finally {
            client.close();
        }
    }

    @Override
    public boolean unlock(String key, String value) {
        CuratorFramework client = CuratorFrameworkFactory.newClient(connectionString,
                new RetryNTimes(3, 1000));
        client.start();
        try {
            client.delete().forPath("/" + key);
            return true;
        } catch (Exception e) {
            return false;
        }finally {
            client.close();
        }
    }
}
