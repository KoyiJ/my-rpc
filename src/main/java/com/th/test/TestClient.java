package com.th.test;

import com.th.codec.RPCMessageToMessageCodec;
import com.th.protocol.MethodInvokData;
import com.th.protocol.Result;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;

public class TestClient {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(eventLoopGroup);
        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {

            @Override
            protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                ChannelPipeline pipeline = nioSocketChannel.pipeline();
                pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 6, 4, 0, 0));
                pipeline.addLast(new LoggingHandler());
                pipeline.addLast(new RPCMessageToMessageCodec());
                pipeline.addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                        //MethodInvokeDate
                        MethodInvokData methodInvokData = new MethodInvokData(UserService.class, "login", new Class[]{String.class, String.class}, new Object[]{"Hi", "123"});

                        ctx.writeAndFlush(methodInvokData);
                    }
                });
            }


            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                System.out.println("接收到服务端RPC调用的返回....{}" + msg);
            }
        });

        ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(8080));
        channelFuture.sync();
    }
}
