package com.george.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class TeamDeleteRequest implements Serializable {

    private static final long serialVersionUID = 63275792189836088L;

    /**
     * id
     */
    private Long id;
}
