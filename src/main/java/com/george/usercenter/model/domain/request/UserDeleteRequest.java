package com.george.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserDeleteRequest implements Serializable {
    private Long id;
    private static final long serialVersionUID = 921959081482565088L;
}
