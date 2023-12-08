package com.george.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdatePasswordRequest implements Serializable {

    private static final long serialVersionUID = -2005487017801345371L;

    private String oldPassword;
    private String newPassword;

}
