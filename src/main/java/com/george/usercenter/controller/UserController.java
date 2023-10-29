package com.george.usercenter.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.george.usercenter.common.BaseResponse;
import com.george.usercenter.common.ErrorCode;
import com.george.usercenter.common.ResultUtils;
import com.george.usercenter.exception.BusinessException;
import com.george.usercenter.mapper.UserMapper;
import com.george.usercenter.model.domain.User;
import com.george.usercenter.model.domain.request.UserLoginRequest;
import com.george.usercenter.model.domain.request.UserRegisterRequest;
import com.george.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.george.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.george.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
//@CrossOrigin(origins = {"http://82.156.242.159:81","http://localhost:8081"},allowCredentials = "true")
public class UserController {

    @Resource
    UserService userService;

    @Resource
    UserMapper userMapper;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if (userRegisterRequest == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();

        if (StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,planetCode))
            throw new BusinessException(ErrorCode.PARAMS_ERROR);

        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        if (userLoginRequest == null)
//            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
            throw new BusinessException(ErrorCode.PARAMS_ERROR);

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        if (StringUtils.isAnyBlank(userAccount,userPassword))
            return ResultUtils.error(ErrorCode.NULL_ERROR);

        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request){
        if (request==null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }


    @GetMapping("/search")
    public BaseResponse<List<User>> userSearch(String username, HttpServletRequest request){
        //todo 一定要记得用户鉴权

        if (!isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH,"无管理员权限");
        }

        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username))
            userQueryWrapper.like("username",username);

        List<User> userList = userService.list(userQueryWrapper);
        List<User> result = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(result);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id,HttpServletRequest request){
        //todo 一定要记得用户鉴权
        if (!isAdmin(request))
            throw new BusinessException(ErrorCode.NO_AUTH,"无管理员权限");

        if (id<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        boolean result = userService.removeById(id);
        return ResultUtils.success(result);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request){
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User)userObj;
        if (currentUser == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);

        // 数据可能是实时变化的，用id去查库
//        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
//        userQueryWrapper.eq("id",currentUser.getId());
//
//        userMapper.selectOne(userQueryWrapper);
        //todo 校验用户是否合法，是否有状态位标记为不能使用之类的。

        User user = userMapper.selectById(currentUser.getId());

        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }


    /**
     * 检验是否为管理员
     * @param request
     * @return false:不是管理员 true:为管理员
     */
    public boolean isAdmin(HttpServletRequest request){
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User)userObj;

        return user!=null && user.getUserRole()==ADMIN_ROLE;
    }



}

