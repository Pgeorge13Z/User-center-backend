package com.george.usercenter.service;

import com.george.usercenter.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.george.usercenter.model.domain.User;
import com.george.usercenter.model.dto.TeamQuery;
import com.george.usercenter.model.request.TeamJoinRequest;
import com.george.usercenter.model.request.TeamQuitRequest;
import com.george.usercenter.model.request.TeamUpdateRequest;
import com.george.usercenter.model.vo.TeamUserVO;

import java.util.List;

/**
* @author Pgeorge
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2023-11-28 21:56:18
*/
public interface TeamService extends IService<Team> {
    /**
     * 创建队伍
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);

    List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin);

    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    boolean deleteTeam(Long id, User loginUser);
}
