package com.th.loadbalance;

import com.th.registry.HostAndPort;

import java.util.List;

/**
 * 负载均衡策略的接口，其下可有不同的负载均衡策略实现
 */
public interface LoadBalancer {

    HostAndPort select(List<HostAndPort> hostAndPorts);

}
