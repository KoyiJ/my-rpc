package com.th;

import com.th.registry.HostAndPort;
import com.th.registry.Registry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class RpcServerProvider {

    private int port;

    private EventLoopGroup eventLoopGroupBoss;


    private EventLoopGroup eventLoopGroupWorker;


    //Netty的编解码器，内置的handler通过这个线程组服务
    private EventLoopGroup eventLoopGroupHandler;

    //自己开发的handler通过这个线程组服务
    private EventLoopGroup eventLoopGroupService;

    //指定worker线程组线程数  todo 这几个int类型不需要设置为成员变量嘛
    private int workerThreads;

    private int handlerThreads;

    private int serviceThreads;

    //行进服务注册所需的register
    private Registry registry;

    //将需要提供RPC服务的对象放入这个Map，以便接收到RPC请求后使用反射调用RPC功能
    private Map<String, Object> exposeBeans;

    //不想让使用者可以连续调两次startServer方法，因为会端口冲突，如果要启两个server的话，再去搞一个RpcServerProvider吧
    private boolean isStarted;

    public RpcServerProvider(int port, int workerThreads, int handlerThreads, int serviceThreads, Registry registry, Map<String, Object> exposeBeans) {
        this.port = port;
        this.workerThreads = workerThreads;
        this.handlerThreads = handlerThreads;
        this.serviceThreads = serviceThreads;

        this.eventLoopGroupBoss = new NioEventLoopGroup(1);
        this.eventLoopGroupWorker = new NioEventLoopGroup(workerThreads);
        this.eventLoopGroupHandler = new DefaultEventLoopGroup(handlerThreads);
        this.eventLoopGroupService = new DefaultEventLoopGroup(serviceThreads);

        this.registry = registry;
        this.exposeBeans = exposeBeans;

        this.isStarted = false;
    }

    public RpcServerProvider(Registry registry, Map<String, Object> exposeBeans) {
        this(8080, 1, 1, 1, registry, exposeBeans);
    }


    //开启服务
    public void startServer() throws InterruptedException, UnknownHostException {

        if (isStarted) {
            throw new RuntimeException("server is already started");
        }

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(eventLoopGroupBoss, eventLoopGroupWorker);
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.childHandler(new RpcServerProviderInitializer(eventLoopGroupHandler, eventLoopGroupService, exposeBeans));

        //因为下面两行阻塞，所以采用异步监听的方式，其实把下面这两行扔到一个新线程里面也行，还不用写监听这些代码
//        Channel channel = serverBootstrap.bind(port).sync().channel();
//        channel.closeFuture().sync();

        ChannelFuture channelFuture = serverBootstrap.bind(port);
        channelFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if (future.isSuccess()) {

                    //将服务注册到注册中心
                    registerService(InetAddress.getLocalHost().getHostAddress(), port, exposeBeans, registry);

                    isStarted = true;

                    Channel channel = channelFuture.channel();
                    ChannelFuture closeFuture = channel.closeFuture();
                    closeFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
                        @Override
                        public void operationComplete(Future<? super Void> future) throws Exception {
                            if (future.isSuccess()) {
                                shutDownServer();
                            }
                        }
                    });
                }

            }
        });

        //异常关闭的话，也执行shutDownServer方法
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutDownServer()));
    }

    //关闭服务，释放资源
    public void shutDownServer() {
        eventLoopGroupBoss.shutdownGracefully();
        eventLoopGroupWorker.shutdownGracefully();
        eventLoopGroupHandler.shutdownGracefully();
        eventLoopGroupService.shutdownGracefully();

    }


    private void registerService(String ip, int port, Map<String, Object> exposeBeans, Registry registry) {

        Set<String> keySet = exposeBeans.keySet();

        HostAndPort hostAndPort = new HostAndPort(ip, port);

        if (registry == null){
            return;
        }

        for (String targetInterface : keySet) {
            registry.registerService(targetInterface, hostAndPort);
        }
    }
}
