package com.george.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = -2672925408928677207L;

    private String userAccount;
    private String userPassword;

}
