package com.george.usercenter.constant;

public interface UserConstant {
    //interface 里的变量都是 public static的

    /**
     * 用户登录状态键
     */
    String USER_LOGIN_STATE = "userLoginState";

    // --------用户权限----------

    /**
     * 普通用户
     */
    int DEFAULT_ROLE = 0;

    /**
     * 管理员
     */
    int ADMIN_ROLE = 1;
}
