package com.lwl.client.zookeeper;

import org.apache.zookeeper.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author liuweilong
 * @description
 * @date 2019/5/17 11:44
 */
@Service("zkTmpSort")
public class ZkLock_TmpSortNode{
    @Value("${zk.client}")
    private String connectionString;

    public static final String ROOT_PATH = "/lock";

    public String lock(String key, String value) {
        try {
            ZooKeeper client = new ZooKeeper(connectionString, 3000, null);
            String nodeCreated = client.create(ROOT_PATH + "/" + key, value.getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL_SEQUENTIAL);

            List<String> children = client.getChildren(ROOT_PATH, false);
            Collections.sort(children);
            String minNode = children.get(0);

            if ((ROOT_PATH + "/" + minNode).equals(nodeCreated)){
                return nodeCreated;
            }else {
                //等待锁
                Integer currentIndex = children.indexOf(nodeCreated.substring(nodeCreated.lastIndexOf("/") + 1));
                String pre = children.get(currentIndex - 1);
                final boolean[] success = {false};
                CountDownLatch countDownLatch = new CountDownLatch(1);
                client.exists(ROOT_PATH + "/" + pre, event -> {
                    if (Watcher.Event.EventType.NodeDeleted.equals(event.getType())){
                        System.out.println("线程： " + Thread.currentThread().getName() + "监听到节点删除事件");
                        List<String> childrenNew;
                        try {
                            childrenNew = client.getChildren(ROOT_PATH, false);
                            if (childrenNew == null){
                                success[0] = true;
                                countDownLatch.countDown();
                                return;
                            }
                            Collections.sort(childrenNew);
                            String minNodeNew = childrenNew.get(0);
                            success[0] = (ROOT_PATH + "/" + minNodeNew).equals(nodeCreated);
                            countDownLatch.countDown();
                        } catch (KeeperException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                countDownLatch.await(3000, TimeUnit.MILLISECONDS);
                if (success[0]){
                    return nodeCreated;
                }
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean unlock(String key) {
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
