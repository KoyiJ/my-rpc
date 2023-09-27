package com.th.test;

import com.th.protocol.MethodInvokData;
import com.th.protocol.Result;
import com.th.registry.HostAndPort;
import com.th.transport.NettyTransport;
import com.th.transport.Transport;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;


@Slf4j
public class TestClient1 {
    public static void main(String[] args) throws Exception {
        Transport transport = new NettyTransport();

        HostAndPort hostAndPort = new HostAndPort(InetAddress.getLocalHost().getHostName(), 8080);
        MethodInvokData methodInvokData = new MethodInvokData(UserService.class, "login", new Class[]{String.class, String.class}, new Object[]{"Hi", "123"});

        Result result = transport.invoke(hostAndPort,methodInvokData);

        log.debug("result....{}",result.getResultValue());

    }
}
