package com.george.usercenter.model.domain.request;

import lombok.Data;

@Data
public class UserUpdateMyRequest {
    /**
     * 用户昵称
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;


    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private String gender;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户编号
     */
    private String planetCode;


}
