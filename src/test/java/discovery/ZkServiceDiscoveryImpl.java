package discovery;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.List;

public class ZkServiceDiscoveryImpl implements ServiceDiscovery {

    private CuratorFramework client;

    private String basePath = "/ak-register";


    /**
     * @param serviceName
     * @return
     */
    @Override
    public List<String> discover(String serviceName) {
        String serviceNamePath = basePath + "/" + serviceName;

        //zk上是否已经存在对应路径，不存在则创建
        try {
            if (client.checkExists().forPath(serviceNamePath) != null) {
                return this.client.getChildren().forPath(serviceNamePath);
            } else {
                throw new RuntimeException("未找到任何可用服务");
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    @Override
    public void watchAndDiscover(String serviceName) {
        String serviceNamePath = basePath + "/" + serviceName;
        CuratorCache curatorCache = CuratorCache.build(client,serviceNamePath);
        CuratorCacheListener curatorCacheListener = CuratorCacheListener.builder().forPathChildrenCache(serviceNamePath, client, new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                List<String> strings = client.getChildren().forPath(serviceNamePath);
            }
        }).build();

        curatorCache.listenable().addListener(curatorCacheListener);
        curatorCache.start();
    }

    public ZkServiceDiscoveryImpl(String zkConnectString) {
        this.client = CuratorFrameworkFactory.newClient(zkConnectString, 1000, 1000, new ExponentialBackoffRetry(1000, 3, 1000));
        client.start();
    }
}
