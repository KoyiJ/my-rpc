package com.th.test;

import com.th.cluster.FailFastExecutor;
import com.th.loadbalance.RandomLoadBalancer;
import com.th.proxy.JDKProxy;
import com.th.transport.NettyTransport;

public class TestClient2 {
    public static void main(String[] args) {
        JDKProxy jdkProxy = new JDKProxy(UserService.class);
        jdkProxy.setRegistry(null);
        jdkProxy.setExecutor(new FailFastExecutor());
        jdkProxy.setTransport(new NettyTransport());
        jdkProxy.setLoadBalancer(new RandomLoadBalancer());

        UserService userService = (UserService) jdkProxy.createProxy();
        userService.login("hi", "123");

    }
}
