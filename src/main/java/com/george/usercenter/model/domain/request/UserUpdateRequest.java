package com.george.usercenter.model.domain.request;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

@Data
public class UserUpdateRequest {
    private Long id;
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
     * 用户状态 0-正常 1-注销 2-封号
     */
    private Integer userStatus;


    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 0: 普通用户 1：管理员
     */
    private Integer userRole;
}
