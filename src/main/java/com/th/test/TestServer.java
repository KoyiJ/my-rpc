package com.th.test;

import com.th.RpcServerProvider;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class TestServer {
    public static void main(String[] args) throws UnknownHostException, InterruptedException {
        var exposeBeans = new HashMap<String, Object>();
        exposeBeans.put(UserService.class.getName(), new UserServiceImpl());
        RpcServerProvider rpcServerProvider = new RpcServerProvider(null, exposeBeans);
        rpcServerProvider.startServer();
        System.out.println("miao");
    }
}
