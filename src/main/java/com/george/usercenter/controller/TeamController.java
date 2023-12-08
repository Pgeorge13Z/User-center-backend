package com.george.usercenter.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.george.usercenter.common.BaseResponse;
import com.george.usercenter.common.ErrorCode;
import com.george.usercenter.common.ResultUtils;
import com.george.usercenter.exception.BusinessException;
import com.george.usercenter.model.domain.Team;
import com.george.usercenter.model.domain.User;
import com.george.usercenter.model.domain.UserTeam;
import com.george.usercenter.model.dto.TeamQuery;
import com.george.usercenter.model.request.*;
import com.george.usercenter.model.vo.TeamUserVO;
import com.george.usercenter.service.TeamService;
import com.george.usercenter.service.UserService;
import com.george.usercenter.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户接口
 */
@RestController
@Slf4j
@RequestMapping("/team")
//@CrossOrigin(origins = {"http://129.226.152.209:80","http://localhost:8081","http://127.0.0.1:5173/",
//        "http://user.code-club.fun","http://user.code-club.fun:80"},allowCredentials = "true")
//@CrossOrigin(origins ={"http://127.0.0.1:5173"},allowCredentials = "true")
public class TeamController {

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @Resource
    private UserTeamService userTeamService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest httpServletRequest) {
        if (teamAddRequest==null) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍添加请求信息为空");
        }

        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest,team);
        User loginUser = userService.getLoginUser(httpServletRequest);
        long teamId = teamService.addTeam(team,loginUser);
        return ResultUtils.success(teamId);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody TeamDeleteRequest teamDeleteRequest, HttpServletRequest httpServletRequest) {
        if (teamDeleteRequest==null || teamDeleteRequest.getId()==null)
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍删除请求信息为空");
        Long id = teamDeleteRequest.getId();
        User loginUser = userService.getLoginUser(httpServletRequest);
        boolean result = teamService.deleteTeam(id,loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除失败");
        }
        return ResultUtils.success(true);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest httpServletRequest) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍更新请求信息为空");
        }
        User loginUser = userService.getLoginUser(httpServletRequest);
        boolean result = teamService.updateTeam(teamUpdateRequest,loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新队伍信息失败");
        }
        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Team team = teamService.getById(id);
        if (team == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);

        return ResultUtils.success(team);
    }

    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery,HttpServletRequest request) {
        if (teamQuery==null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"TeamList参数为空");
        }
        //1. 查询队伍列表
        boolean isAdmin = userService.isAdmin(request);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery,isAdmin);
        final List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        if (teamIdList.size()==0)
            return ResultUtils.success(teamList);

        //2.判断当前用户是否已经加入队伍
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try {
            User loginUser = userService.getLoginUser(request);
            userTeamQueryWrapper.eq("userId",loginUser.getId());
            userTeamQueryWrapper.in("teamId",teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            // 已加入的队伍id集合
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamList.forEach(team->{
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        } catch (Exception e) {}
        // 3.查询已经加入队伍的人数
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId",teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team->{
            List<UserTeam> list = teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>());
            team.setHasJoinNum(list.size());
        });
        return ResultUtils.success(teamList);


    }



    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVO>> listMyCreateTeams(TeamQuery teamQuery,HttpServletRequest request) {
        if (teamQuery==null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"TeamList参数为空");
        }
        User loginUser = userService.getLoginUser(request);
        teamQuery.setUserId(loginUser.getId());
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery,true);
        return ResultUtils.success(teamList);
    }


    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVO>> listMyJoinTeams(TeamQuery teamQuery,HttpServletRequest request) {
        if (teamQuery==null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"TeamList参数为空");
        }
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId",loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
        Map<Long, List<UserTeam>> listMap = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        ArrayList<Long> teamIdList = new ArrayList<>(listMap.keySet());
        teamQuery.setIdList(teamIdList);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery,true);
        return ResultUtils.success(teamList);
    }

    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQuery teamQuery) {
        if (teamQuery==null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"TeamPage参数为空");
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery,team);
        Page<Team> page = new Page<>(teamQuery.getPageNum(),teamQuery.getPageSize());
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>(team);
        Page<Team> list = teamService.page(page,teamQueryWrapper);
        return ResultUtils.success(list);
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest,HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"加入队伍请求参数为空");
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest,loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest,HttpServletRequest request) {
        if (teamQuitRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"退出队伍请求参数为空");
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest,loginUser);
        return ResultUtils.success(result);
    }



}

