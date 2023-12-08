package com.george.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.george.usercenter.common.ErrorCode;
import com.george.usercenter.constant.RedisKeyName;
import com.george.usercenter.exception.BusinessException;
import com.george.usercenter.model.domain.Team;
import com.george.usercenter.model.domain.User;
import com.george.usercenter.model.domain.UserTeam;
import com.george.usercenter.model.dto.TeamQuery;
import com.george.usercenter.model.enums.TeamStatus;
import com.george.usercenter.model.request.TeamJoinRequest;
import com.george.usercenter.model.request.TeamQuitRequest;
import com.george.usercenter.model.request.TeamUpdateRequest;
import com.george.usercenter.model.vo.TeamUserVO;
import com.george.usercenter.model.vo.UserVO;
import com.george.usercenter.service.TeamService;
import com.george.usercenter.mapper.TeamMapper;
import com.george.usercenter.service.UserService;
import com.george.usercenter.service.UserTeamService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Pgeorge
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2023-11-28 21:56:18
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        //1. 请求参数是否为空？
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍人数不满足要求");
        }

        //2. 是否登录，未登录不允许创建
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN, "未登录");
        }
        final long userId = loginUser.getId();

        //3. 校验信息
        //   1. 队伍人数 > 1 且 <= 20
        if (team.getMaxNum() < 1 || team.getMaxNum() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不满足要求");
        }

        //   2. 队伍标题 <= 20
        String teamName = team.getName();
        if (StringUtils.isBlank(teamName) || teamName.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题不满足要求");
        }

        //   3. 描述 <= 512
        String description = team.getDescription();
        if (description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述不满足要求");
        }

        //   4. status 是否公开（int）不传默认为 0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatus statusEnum = TeamStatus.getEnumByValue(status);

        //   5. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();
        if (TeamStatus.SECRETE.equals(statusEnum)) {
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
            }
        }

        //   6. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (expireTime == null || new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建的时间出错");
        }

        //   7. 校验用户最多创建 5 个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long hasTeam = this.count(queryWrapper);
        if (hasTeam >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建5个队伍");
        }

        //4. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result || teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }

        //5. 插入用户  => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        userTeam.setUserId(userId);
        userTeam.setJoinTime(new Date());

        boolean save = userTeamService.save(userTeam);
        if (!save) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }

        return teamId;
    }

    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        //组合查询条件
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            List<Long> idList = teamQuery.getIdList();
            String searchText = teamQuery.getSearchText();
            String name = teamQuery.getName();
            String description = teamQuery.getDescription();
            Integer maxNum = teamQuery.getMaxNum();
            Long userId = teamQuery.getUserId();
            Integer status = teamQuery.getStatus();

            if (id != null && id > 0) {
                teamQueryWrapper.eq("id", id);
            }
            if (CollectionUtils.isNotEmpty(idList)) {
                teamQueryWrapper.in("id", idList);
            }
            if (StringUtils.isNotBlank(searchText)) {
                teamQueryWrapper.like("name", searchText).or().like("description", searchText);
            }
            if (StringUtils.isNotBlank(name)) {
                teamQueryWrapper.like("name", name);
            }
            if (StringUtils.isNotBlank(description)) {
                teamQueryWrapper.like("description", description);
            }
            if (maxNum != null && maxNum > 0) {
                teamQueryWrapper.eq("maxNum", maxNum);
            }
            if (userId != null && userId > 0) {
                teamQueryWrapper.eq("userId", userId);
            }
            if (status != null && status >= 0) {
                TeamStatus enumByValue = TeamStatus.getEnumByValue(status);
//            if (enumByValue == null) {
//                enumByValue = TeamStatus.PUBLIC;
//            }
                if (!isAdmin && enumByValue.equals(TeamStatus.PRIVATE)) {
                    throw new BusinessException(ErrorCode.NO_AUTH);
                }
                teamQueryWrapper.eq("status", enumByValue.getValue());
            }


        }

        //不展示已经过期的队伍
        // expireTime is null or expireTime > now
        teamQueryWrapper.gt("expireTime", new Date()).or().isNull("expireTime");

        List<Team> teamList = this.list(teamQueryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }

        ArrayList<TeamUserVO> teamUserVOS = new ArrayList<>();

        for (Team team : teamList) {
            Long userId = team.getUserId();
            User createUser = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            if (createUser != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(createUser, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOS.add(teamUserVO);
        }

        return teamUserVOS;
    }


    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍更新请求为空");
        }
        Long id = teamUpdateRequest.getId();
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(id);
        if (oldTeam == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        //权限验证
        if (oldTeam.getUserId() != loginUser.getId() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        //加密房间必须有密码
        TeamStatus enumByValue = TeamStatus.getEnumByValue(teamUpdateRequest.getStatus());
        if (enumByValue.equals(TeamStatus.SECRETE)) {
            if (StringUtils.isBlank(teamUpdateRequest.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密房间必须要设置密码");
            }
        }

        Team team = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, team);
        return this.updateById(team);
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "加入队伍请求为空");
        }
        Long teamId = teamJoinRequest.getTeamId();
        Team team = getTeamById(teamId);

        Date expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }

        Integer status = team.getStatus();
        TeamStatus enumByValue = TeamStatus.getEnumByValue(status);
        if (TeamStatus.PRIVATE.equals(enumByValue)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "禁止加入私有队伍");
        }
        String password = teamJoinRequest.getPassword();
        if (TeamStatus.SECRETE.equals(enumByValue)) {
            if (StringUtils.isBlank(password) || !password.equals(team.getPassword()))
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }

        //这三个数据库操作需要分布式锁，因为判断逻辑和加入逻辑有距离

        //用户最多创建和加入五个队伍
        Long userId = loginUser.getId();

        //只有一个线程能获取锁
        RLock lock = redissonClient.getLock(RedisKeyName.TEAM_JOIN_LOCK);
        try {
            while (true){
                if (lock.tryLock(0,-1, TimeUnit.MILLISECONDS)) {
                    QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("userId", userId);
                    long hasJoinNum = userTeamService.count(userTeamQueryWrapper);
                    if (hasJoinNum > 5) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多创建和加入5个队伍");
                    }

                    //不能重复加入已经加入的队伍
                    userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("userId", userId);
                    userTeamQueryWrapper.eq("teamId", teamId);
                    long hasUserJoinTeam = userTeamService.count(userTeamQueryWrapper);
                    if (hasUserJoinTeam > 0) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已经加入该队伍");
                    }

                    //已经加入队伍的人数不能超过上限
                    long teamHasJoinNum = countTeamUserByTeamId(teamId);
                    if (teamHasJoinNum > team.getMaxNum()) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
                    }

                    //修改队伍信息
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    return userTeamService.save(userTeam);
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheJoinTeam error",e);
            return false;
        } finally {
            //只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock" + Thread.currentThread().getId());
                lock.unlock();
            }
        }


    }

    @Override
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        // 1. 验证请求参数
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "退出请求为空");
        }
        // 2. 验证队伍是否存在
        // 3. 验证我是否已经加入队伍
        Long teamId = teamQuitRequest.getTeamId();
        Long userId = loginUser.getId();
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(userTeam);
        long count = userTeamService.count(queryWrapper);
        if (count == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未加入队伍或队伍不存在");
        }

        // 4.1队伍只剩一人则解散
        long teamHasJoinNum = countTeamUserByTeamId(teamId);

        if (teamHasJoinNum == 1) {
            this.removeById(teamId);
        } else {
            // 4.2队长退出队伍，权限转移，先来后到
            Team team = getTeamById(teamId);
            if (team.getUserId() == userId) {
                // 1.查询已加入队伍 的所有用户和加入时间
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId", teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> list = userTeamService.list(userTeamQueryWrapper);
                if (CollectionUtils.isEmpty(list) || list.size() <= 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队长退出队伍失败");
                }
                UserTeam nextUserTeam = list.get(1);
                Long nextTeamLeaderId = nextUserTeam.getUserId();
                //更新当前队伍的队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTeamLeaderId);
                boolean result = this.updateById(updateTeam);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍信息失败");
                }
            }
        }
        // 4.3非队长，自己退出队伍
        return userTeamService.remove(queryWrapper);

    }

    @Override
    public boolean deleteTeam(Long id, User loginUser) {
        // 检验队伍是否合法
        Team team = getTeamById(id);

        //检验是否是队长
        if (team.getUserId() != loginUser.getId() || !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无删除权限");
        }

        //移除队伍信息和管理信息
        Long teamId = team.getId();
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除关联信息失败");
        }

        return this.removeById(teamId);

    }

    /**
     * 根据teamId 获取team
     *
     * @param teamId
     * @return
     */
    private Team getTeamById(Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "teamId错误");
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }

        return team;
    }

    /**
     * 获取某队伍的当前人数
     */
    private long countTeamUserByTeamId(long teamId) {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        return userTeamService.count(userTeamQueryWrapper);
    }
}




