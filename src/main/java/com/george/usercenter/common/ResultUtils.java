package com.george.usercenter.common;

/**
 * 返回结果工具类
 */
public class ResultUtils {

    /**
     * 成功
     * @param data
     * @return
     * @param <T>
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0,data,"ok");
    }

    public static <T> BaseResponse<T> success(T data,String message) {
        return new BaseResponse<>(0,data,message);
    }

    /**
     * 失败
     * @param errorCode
     * @return
     */
    public static BaseResponse error (ErrorCode errorCode){
        return new BaseResponse<>(errorCode);
    }

    public static BaseResponse error (ErrorCode errorCode,String message,String description){
        return new BaseResponse<>(errorCode.getCode(),null,message,description);
    }

    public static BaseResponse error (int Code,String message,String description){
        return new BaseResponse<>(Code,null,message,description);
    }
}
