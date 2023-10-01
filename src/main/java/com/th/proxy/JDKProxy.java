package com.th.proxy;

import com.th.cluster.Executor;
import com.th.loadbalance.LoadBalancer;
import com.th.protocol.MethodInvokData;
import com.th.protocol.Result;
import com.th.registry.HostAndPort;
import com.th.registry.Registry;
import com.th.transport.Transport;
import lombok.Data;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

@Data
public class JDKProxy implements InvocationHandler {

    //下面这几个属性做成成员变量，是为了能使其与spring做整合，让spring帮我们注入实现类
    private Class targetInterface;
    private Executor executor;
    private LoadBalancer loadBalancer;
    private Transport transport;
    private Registry registry;

    private List<HostAndPort> hostAndPorts;

    public JDKProxy(Class targetInterface) {
        this.targetInterface = targetInterface;
    }

    public Object createProxy() {

        return Proxy.newProxyInstance(targetInterface.getClass().getClassLoader(), new Class[]{targetInterface}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        MethodInvokData methodInvokData = new MethodInvokData(targetInterface, method.getName(), method.getParameterTypes(), args);
        Result result = executor.execute(hostAndPorts, loadBalancer, transport, methodInvokData);

        if (result.getException()!=null){
            throw result.getException();
        }

        return result;
    }
}
