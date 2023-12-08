package com.george.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class TeamQuitRequest implements Serializable {

    private static final long serialVersionUID = 6415296421370444823L;

    /**
     * teamId
     */
    private Long teamId;
}
