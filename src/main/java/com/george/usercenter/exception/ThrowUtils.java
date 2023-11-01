package com.george.usercenter.exception;


import com.george.usercenter.common.ErrorCode;

/**
 * 抛异常工具类
 */
public class ThrowUtils {
    public static void throwIf(boolean condition,RuntimeException runtimeException){
        if (condition)
            throw runtimeException;
    }

    public static void throwIf(boolean condition, ErrorCode errorCode){
        throwIf(condition,new BusinessException(errorCode));
    }

    public static void throwIf(boolean condition, ErrorCode errorCode,String msg){
        throwIf(condition,new BusinessException(errorCode,msg));
    }
}
