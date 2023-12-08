package com.george.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.george.usercenter.model.domain.UserTeam;
import com.george.usercenter.service.UserTeamService;
import com.george.usercenter.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author Pgeorge
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2023-11-28 21:56:36
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




