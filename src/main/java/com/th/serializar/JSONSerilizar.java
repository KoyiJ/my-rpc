package com.th.serializar;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.th.protocol.Protocol;

public class JSONSerilizar implements Serializar {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serializar(Protocol protocol) throws Exception {
      /*  String jsonString = objectMapper.writeValueAsString(obj);
        return jsonString.getBytes(Charset.defaultCharset());*/
        //return new byte[0];
        return objectMapper.writeValueAsBytes(protocol);
    }

    @Override
    public Protocol deserializar(byte[] bytes) throws Exception {
        return objectMapper.readValue(bytes, Protocol.class);
    }
}
