package com.th;

import com.th.codec.RPCMessageToMessageCodec;
import com.th.protocol.MethodInvokData;
import com.th.protocol.Result;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class RpcServerProviderInitializer extends ChannelInitializer<NioSocketChannel> {

    //Netty的编解码器，内置的handler通过这个线程组服务
    private EventLoopGroup eventLoopGroupHandler;

    //自己开发的handler通过这个线程组服务
    private EventLoopGroup eventLoopGroupService;

    private Map<String, Object> exposeBean;

    public RpcServerProviderInitializer(EventLoopGroup eventLoopGroupHandler, EventLoopGroup eventLoopGroupService, Map<String, Object> exposeBean) {
        this.eventLoopGroupHandler = eventLoopGroupHandler;
        this.eventLoopGroupService = eventLoopGroupService;
        this.exposeBean = exposeBean;
    }

    @Override
    protected void initChannel(NioSocketChannel ch) throws Exception {

        ChannelPipeline pipeline = ch.pipeline();

        //封帧
        pipeline.addLast(this.eventLoopGroupHandler, new LengthFieldBasedFrameDecoder(1024, 6, 4, 0, 0));

        //LoggingHandler
        pipeline.addLast(this.eventLoopGroupHandler, new LoggingHandler());

        //编解码器
        pipeline.addLast(this.eventLoopGroupService, new RPCMessageToMessageCodec());

        //RPC功能调用
        pipeline.addLast(this.eventLoopGroupService, new ChannelInboundHandlerAdapter() {


            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

                //强转一下解码后的数据
                MethodInvokData methodInvokData = (MethodInvokData) msg;

                //执行RPC功能
                Result result = executeTargetObject(methodInvokData, exposeBean);

                //响应RPC结果
                ChannelFuture channelFuture = ctx.writeAndFlush(result);

                //关闭连接
                channelFuture.addListener(ChannelFutureListener.CLOSE);   //监听到写出完数据后，关闭连接？
                channelFuture.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);   //监听到有异常时，关闭连接？

            }
        });
    }


    private Result executeTargetObject(MethodInvokData methodInvokData, Map<String, Object> exposeBean) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        System.out.println("mua");

        //获取调用哪个接口的信息（之所以定义一个接口，而不是直接具体类，是为了解耦，可以更换实现类）
        Class<?> targetInterface = methodInvokData.getTargetInterface();

        //获取对应的具体对象
        Object nativeObj = exposeBean.get(targetInterface.getName());

        //获取所需被执行的方法
        Method method = targetInterface.getDeclaredMethod(methodInvokData.getMethodName(), methodInvokData.getParameterTypes());

        //方法调用
        Result result = new Result();
        try {

            Object ret = method.invoke(nativeObj, methodInvokData.getArgs());
            result.setResultValue(ret);

        } catch (Exception e) {
            result.setException(e);
        }


        return result;
    }
}
