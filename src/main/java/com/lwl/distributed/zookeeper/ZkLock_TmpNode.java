package com.lwl.distributed.zookeeper;

import com.lwl.distributed.IDistributedLock;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


/**
 * @author liuweilong
 * @description
 * @date 2019/5/17 11:44
 */
@Service("zkTmp")
public class ZkLock_TmpNode implements IDistributedLock {
    @Value("${zk.client}")
    private String connectionString;

    @Override
    public boolean lock(String key, String value) {
        ZooKeeper client;
        try {
            client = new ZooKeeper(connectionString, 3000, null);
            client.create(key, value.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL);
            return true;
        } catch (Exception e){
            return false;
        }
    }

    @Override
    public boolean unlock(String key, String value) {
        ZooKeeper client;
        try {
            client = new ZooKeeper(connectionString, 3000, null);
            client.delete(key, 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
