package com.th.codec;


import com.th.protocol.Protocol;
import com.th.serializar.HessianSerializar;
import com.th.serializar.Serializar;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.List;


@Slf4j
public class RPCMessageToMessageCodec extends MessageToMessageCodec<ByteBuf, Protocol> {

    //此处可更换序列化实现类
    private Serializar serializar = new HessianSerializar();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        //获取魔术 进行魔术的对比
        CharSequence charSequence = msg.readCharSequence(5, StandardCharsets.UTF_8);
        if (!Protocol.MAGIC_NUM.equals(charSequence.toString())) {
            throw new RuntimeException("MagicNumber error...");
        }

        byte protocolVersion = msg.readByte();
        if (Protocol.PROTOCOL_VERSION != protocolVersion) {
            throw new RuntimeException("ProtocolVersion Error...");
        }


        //1. ByteBuf msg ---> byte[]
        //byte[]长度
        int protocolLength = msg.readInt();
        byte[] bytes = new byte[protocolLength];
        msg.readBytes(bytes);

        //2. 反序列化操作
        //Serializar serializar = new HessianSerializar();
        Protocol protocol = (Protocol) serializar.deserializar(bytes);

        //3 解码器如何把封装转换的对象 交给后面的操作呢？
        out.add(protocol);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Protocol protocol, List<Object> out) throws Exception {
        log.debug("编码器运行了....");
        ByteBufAllocator alloc = ctx.alloc();
        ByteBuf byteBuf = alloc.buffer();

        try {
            byte[] bytes = serializar.serializar(protocol);

            //1幻术 5个字节
            byteBuf.writeBytes(Protocol.MAGIC_NUM.getBytes(StandardCharsets.UTF_8));
            //2设置协议版本 1个字节
            byteBuf.writeByte(Protocol.PROTOCOL_VERSION);

            //封帧的解码器 数据大小是多少
            byteBuf.writeInt(bytes.length);
            byteBuf.writeBytes(bytes);

            out.add(byteBuf);
        } catch (Exception e) {
            log.error("编码器出现了一场", e);
        }


    }

}
