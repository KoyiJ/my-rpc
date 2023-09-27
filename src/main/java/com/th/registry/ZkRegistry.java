package com.th.registry;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.List;
import java.util.stream.Collectors;

public class ZkRegistry implements Registry {

    public static final String SERVICE_PREFIX = "/rpc";

    public static final String SERVICE_SUFFIX = "/provider";

    private CuratorFramework curatorClient;

    public ZkRegistry(String zkServerAddress) {

        this.curatorClient = CuratorFrameworkFactory.newClient(zkServerAddress, 1000, 1000, new ExponentialBackoffRetry(1000, 3, 1000));
        this.curatorClient.start();
    }

    @Override
    public void registerService(String targetInterfaceName, HostAndPort hostAndPort) {
        String servicePath = splicingServicePath(targetInterfaceName);
        try {
            //zk上路径不存在，则创建
            if (curatorClient.checkExists().forPath(servicePath) == null) {
                this.curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(servicePath);
            }

            //挂ip:port
            String nodeUrl = this.curatorClient.create().withMode(CreateMode.EPHEMERAL).forPath(servicePath + "/" + hostAndPort.getHostName() + ":" + hostAndPort.getPort());


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<HostAndPort> receiveService(String targetInterfaceName) {

        String servicePath = splicingServicePath(targetInterfaceName);

        try {
            if (this.curatorClient.checkExists().forPath(servicePath) != null) {

                List<String> strings = this.curatorClient.getChildren().forPath(servicePath);

                return stringsToHostAndPort(strings);

            } else {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void subscribeService(String targetInterfaceName, List<HostAndPort> existingHostAndPort) {

        String servicePath = splicingServicePath(targetInterfaceName);

        CuratorCache curatorCache = CuratorCache.build(curatorClient,servicePath);
        CuratorCacheListener curatorCacheListener = CuratorCacheListener.builder().forPathChildrenCache(servicePath, curatorClient, new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                // 1 目前服务列表中的数据清除掉
                existingHostAndPort.clear();

                existingHostAndPort.addAll(stringsToHostAndPort(curatorClient.getChildren().forPath(servicePath)));
            }
        }).build();

        curatorCache.listenable().addListener(curatorCacheListener);
        curatorCache.start();

    }

    private List<HostAndPort> stringsToHostAndPort(List<String> strings) {
        return strings.stream()
                .map(s -> s.split(":"))
                .map(sa -> new HostAndPort(sa[0], Integer.parseInt(sa[1])))
                .collect(Collectors.toList());
    }

    private String splicingServicePath(String targetInterfaceName) {
        return SERVICE_PREFIX + "/" + targetInterfaceName + SERVICE_SUFFIX;
    }
}
