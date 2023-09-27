package com.th.transport;

import com.th.protocol.MethodInvokData;
import com.th.protocol.Result;
import com.th.registry.HostAndPort;

/**
 * 客户端进行网络通信方式的封装，之所以是接口，是因为想提供netty，nio，bio，mina多种选择
 */
public interface Transport {

    Result invoke(HostAndPort hostAndPort, MethodInvokData methodInvokData) throws Exception;

    void close();
}
