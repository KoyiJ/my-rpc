package com.th.cluster;

import com.th.loadbalance.LoadBalancer;
import com.th.protocol.MethodInvokData;
import com.th.protocol.Result;
import com.th.registry.HostAndPort;
import com.th.transport.Transport;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class FailFastExecutor implements Executor {

    @Override
    public Result execute(List<HostAndPort> hostAndPorts, LoadBalancer loadBalancer, Transport transport, MethodInvokData methodInvokData) {

        HostAndPort hostAndPort = loadBalancer.select(hostAndPorts);

        Result result = null;

        try {
            result = transport.invoke(hostAndPort, methodInvokData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            transport.close();
        }

//        小小的思考
//        if (result.getException()!=null){
//            throw new RuntimeException();
//        }

        return result;
    }
}
