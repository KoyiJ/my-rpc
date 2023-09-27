package com.th.protocol;

import java.io.Serializable;

public interface Protocol extends Serializable {

    /**
     * 幻术
     */
    String MAGIC_NUM = "THRPC";

    /**
     * 协议
     */
    byte PROTOCOL_VERSION = 1;


}
