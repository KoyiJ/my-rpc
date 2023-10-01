package com.th.cluster;

import com.th.loadbalance.LoadBalancer;
import com.th.protocol.MethodInvokData;
import com.th.protocol.Result;
import com.th.registry.HostAndPort;
import com.th.transport.Transport;

import java.util.List;

//进行RPC调用的Executor，对RPC调用异常，不同的容错机制对应不同的实现
public interface Executor {

    Result execute(List<HostAndPort> hostAndPorts, LoadBalancer loadBalancer, Transport transport, MethodInvokData methodInvokData);
}
