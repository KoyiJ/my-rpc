package com.th.serializar;


import com.th.protocol.Protocol;

public interface Serializar {
    //1 序列化
    public byte[] serializar(Protocol protocol) throws Exception;

    //2 反序列化
    public Protocol deserializar(byte[] bytes) throws Exception;
}
