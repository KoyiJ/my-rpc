package com.th.loadbalance;

import com.th.registry.HostAndPort;

import java.util.List;
import java.util.Random;

/**
 * 随机的负载均衡实现
 */
public class RandomLoadBalancer implements LoadBalancer {

    private Random random = new Random();


    @Override
    public HostAndPort select(List<HostAndPort> hostAndPorts) {

        if (hostAndPorts == null || hostAndPorts.size() == 0){
            throw new RuntimeException("hostAndPorts set null");
        }

        int index = random.nextInt(hostAndPorts.size());

        return hostAndPorts.get(index);
    }
}
