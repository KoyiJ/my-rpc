package com.th.cluster;

import com.th.loadbalance.LoadBalancer;
import com.th.protocol.MethodInvokData;
import com.th.protocol.Result;
import com.th.registry.HostAndPort;
import com.th.transport.NettyTransport;
import com.th.transport.Transport;

import java.util.List;

public class FailOverExecutor implements Executor {
    @Override
    public Result execute(List<HostAndPort> hostAndPorts, LoadBalancer loadBalancer, Transport transport, MethodInvokData methodInvokData) {
        HostAndPort hostAndPort = loadBalancer.select(hostAndPorts);

        Result result = null;
        try {
            result = transport.invoke(hostAndPort, methodInvokData);
            transport.close();
        } catch (Exception e) {
            transport.close();
            hostAndPorts.remove(hostAndPort);
            if (hostAndPorts.size() == 0) {
                throw new RuntimeException();
            } else {
                //注意这里第三个参数，因为之前失败后关闭了连接，所以这里需要一个新的连接（递归调用）
                return execute(hostAndPorts, loadBalancer, new NettyTransport(), methodInvokData);
            }
        }


        return result;
    }
}
