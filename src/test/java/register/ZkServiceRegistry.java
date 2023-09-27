package register;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

public class ZkServiceRegistry implements ServiceRegistry {


    /**
     * curator
     */
    private CuratorFramework client;

    /**
     * 注册到zk上的ip地址
     */
    private String ip;

    /**
     * 注册到zk上的端口号
     */
    private String port;

    /**
     * zk中默认的base路径
     */
    private String basePath = "/zk-register";

    /**
     * 该微服务集群的路径
     */
    private String servicePath;


    @Override
    public void register() {

        String serviceNamePath = basePath + "/" + servicePath;
        try {
            //zk上是否已经存在对应路径，不存在则创建
            if (client.checkExists().forPath(serviceNamePath) == null) {
                this.client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(serviceNamePath);
            }
            //将该微服务的ip:port挂上临时节点
            String nodeUrl = this.client.create().withMode(CreateMode.EPHEMERAL).forPath(serviceNamePath + "/" + ip + ":" + port);
            System.out.println(nodeUrl);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * @param ip
     * @param port
     * @param servicePath
     * @param zkConnectString zk集群所在的ip以及端口号
     */
    public ZkServiceRegistry(String ip, String port, String servicePath, String zkConnectString) {
        this.ip = ip;
        this.port = port;
        this.servicePath = servicePath;

        this.client = CuratorFrameworkFactory.newClient(zkConnectString, 1000, 1000, new ExponentialBackoffRetry(1000, 3, 1000));
    }
}
