package com.george.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserAddRequest implements Serializable {

    private static final long serialVersionUID = 4606377256810538540L;
    /**
     * 用户昵称
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

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

    /**
     * 用户角色: user, admin
     */
    private String userRole;
}
