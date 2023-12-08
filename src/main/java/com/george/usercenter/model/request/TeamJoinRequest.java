package com.george.usercenter.model.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class TeamJoinRequest implements Serializable {

    private static final long serialVersionUID = -7691124285896533126L;

    /**
     * id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;
}
