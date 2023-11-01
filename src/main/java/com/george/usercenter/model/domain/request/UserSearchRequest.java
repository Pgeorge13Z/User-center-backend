package com.george.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserSearchRequest implements Serializable {

    private static final long serialVersionUID = -597264507484242749L;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;


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
