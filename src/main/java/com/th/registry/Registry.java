package com.th.registry;

import java.util.List;

/**
 * 定义一个服务注册发现的接口，如果以后有其他服务治理的策略，实现该接口即可
 */
public interface Registry {

    //服务的注册
    void registerService(String targetInterfaceName,HostAndPort hostAndPort);

    //服务发现，获取对应的微服务集群列表
    List<HostAndPort> receiveService(String targetInterfaceName);

    //服务订阅（在有微服务集群中节点变动时，我们作为客户端可以知晓）
    void subscribeService(String targetInterfaceName,List<HostAndPort> existingHostAndPort);
}
