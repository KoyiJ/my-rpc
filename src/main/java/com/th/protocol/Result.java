package com.th.protocol;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Result implements Protocol {

    /**
     * 所调用方法的返回结果
     */
    private Object resultValue;

    /**
     * 被调用方出错时返回的异常
     */
    private Exception exception;

}
