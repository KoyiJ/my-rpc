package com.th.transport;

import com.th.codec.RPCMessageToMessageCodec;
import com.th.protocol.MethodInvokData;
import com.th.protocol.Result;
import com.th.registry.HostAndPort;
import com.th.test.UserService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

public class NettyTransport implements Transport {

    private Bootstrap bootstrap;

    private EventLoopGroup worker;

    private int workerThreads;

    public NettyTransport(int workerThreads) {
        bootstrap = new Bootstrap();
        this.workerThreads = workerThreads;
        worker = new NioEventLoopGroup(workerThreads);
        bootstrap.group(worker);
        bootstrap.channel(NioSocketChannel.class);
    }

    public NettyTransport() {
        this(1);
    }

    @Override
    public Result invoke(HostAndPort hostAndPort, MethodInvokData methodInvokData) throws Exception {
        RPCClientChannelInitializer rpcClientChannelInitializer = new RPCClientChannelInitializer(methodInvokData);
        bootstrap.handler(rpcClientChannelInitializer);
        ChannelFuture channelFuture = bootstrap.connect(hostAndPort.getHostName(), hostAndPort.getPort()).sync();
        channelFuture.channel().closeFuture().sync();
        return rpcClientChannelInitializer.getResult();
    }

    @Override
    public void close() {
        worker.shutdownGracefully();
    }
}

@Slf4j
class RPCClientChannelInitializer extends ChannelInitializer<NioSocketChannel> {

    private MethodInvokData methodInvokData;

    private Result result;

    public RPCClientChannelInitializer(MethodInvokData methodInvokData) {
        this.methodInvokData = methodInvokData;
    }

    @Override
    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
        ChannelPipeline pipeline = nioSocketChannel.pipeline();
        pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 6, 4, 0, 0));
        pipeline.addLast(new LoggingHandler());
        pipeline.addLast(new RPCMessageToMessageCodec());
        pipeline.addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                log.error("开始对服务端进行RPC功能调用....{}", methodInvokData);
                //MethodInvokeDate
                ChannelFuture channelFuture = ctx.writeAndFlush(methodInvokData);
                channelFuture.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        });
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.error("接收到服务端RPC调用的返回....{}", msg);
        result = (Result) msg;
    }

    public Result getResult() {
        return result;
    }
}