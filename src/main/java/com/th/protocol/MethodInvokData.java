package com.th.protocol;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MethodInvokData implements Protocol {

    /**
     * 调用远程服务的哪个接口
     */
    private Class targetInterface;

    /**
     * 所调用的方法名
     */
    private String methodName;

    /**
     * 所调用方法的参数类型
     */
    private Class<?>[] parameterTypes;

    /**
     * 方法实参
     */
    private Object[] args;



}
