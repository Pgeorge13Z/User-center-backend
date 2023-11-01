package com.george.usercenter.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.george.usercenter.common.BaseResponse;
import com.george.usercenter.common.ErrorCode;
import com.george.usercenter.common.ResultUtils;
import com.george.usercenter.exception.BusinessException;
import com.george.usercenter.exception.ThrowUtils;
import com.george.usercenter.mapper.UserMapper;
import com.george.usercenter.model.domain.User;
import com.george.usercenter.model.domain.request.*;
import com.george.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.george.usercenter.constant.UserConstant.*;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://129.226.152.209:80","http://localhost:8081","http://user.code-club.fun","http://user.code-club.fun:80"},allowCredentials = "true")
public class UserController {

    @Resource
    UserService userService;

    @Resource
    UserMapper userMapper;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if (userRegisterRequest == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();

        if (StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,planetCode))
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");

        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        if (userLoginRequest == null)
//            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        if (StringUtils.isAnyBlank(userAccount,userPassword))
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");

        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request){
        if (request==null)
            throw new BusinessException(ErrorCode.NO_LOGIN,"无法获取登录信息");
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }


    @GetMapping("/search")
    public BaseResponse<List<User>> userSearch(UserSearchRequest userSearchRequest, HttpServletRequest request){
        //todo 一定要记得用户鉴权

        if (!isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH,"无管理员权限");
        }

        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();

        String username = userSearchRequest.getUsername();
        String userAccount = userSearchRequest.getUserAccount();
        String phone = userSearchRequest.getPhone();
        String email = userSearchRequest.getEmail();
        String planetCode = userSearchRequest.getPlanetCode();

        if (StringUtils.isNotBlank(username))
            userQueryWrapper.like("username",username);

        if (StringUtils.isNotBlank(userAccount))
            userQueryWrapper.like("userAccount",userAccount);

        if (StringUtils.isNotBlank(phone))
            userQueryWrapper.like("phone",phone);

        if (StringUtils.isNotBlank(email))
            userQueryWrapper.like("email",email);

        if (StringUtils.isNotBlank(planetCode))
            userQueryWrapper.like("planetCode",planetCode);

        List<User> userList = userService.list(userQueryWrapper);
        List<User> result = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(result);
    }

    @PostMapping("/add")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest,HttpServletRequest request){
        if (!isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH,"无管理员权限");
        }
        if (userAddRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest,user);


        String encryptPassword = DigestUtils.md5DigestAsHex((userAddRequest.getUserPassword()+SALT).getBytes());
        user.setUserPassword(encryptPassword);

        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result,ErrorCode.SYSTEM_ERROR,"数据存储出错");
        return ResultUtils.success(user.getId(),"用户添加成功");
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
        User loginUser = userService.getLoginUser(request);
        User safetyUser = userService.getSafetyUser(loginUser);
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

    @PostMapping("/update/password")
    public BaseResponse<Boolean> updateUserPassword(@RequestBody UserUpdatePasswordRequest userUpdatePasswordRequest, HttpServletRequest request){
        String oldPassword = userUpdatePasswordRequest.getOldPassword();
        String newPassword = userUpdatePasswordRequest.getNewPassword();

        boolean updateUserPassword = userService.updateUserPassword(oldPassword, newPassword, request);

        if (updateUserPassword)
            return ResultUtils.success(true,"修改成功");
        else
            return ResultUtils.error(ErrorCode.PARAMS_ERROR,null,"参数有误");


    }

    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest,HttpServletRequest request){
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"数据为空");
        }

        User loginUser = userService.getLoginUser(request);
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest,user);
        user.setId(loginUser.getId());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result,ErrorCode.SYSTEM_ERROR);
        return ResultUtils.success(true,"更新信息成功");
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest,HttpServletRequest request){
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限");
        }

        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"数据为空");
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest,user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result,ErrorCode.SYSTEM_ERROR);
        return ResultUtils.success(true,"更新信息成功");
    }




}

