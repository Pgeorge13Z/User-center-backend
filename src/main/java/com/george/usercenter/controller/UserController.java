package com.george.usercenter.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.george.usercenter.common.BaseResponse;
import com.george.usercenter.common.ErrorCode;
import com.george.usercenter.common.ResultUtils;
import com.george.usercenter.constant.RedisKeyName;
import com.george.usercenter.exception.BusinessException;
import com.george.usercenter.exception.ThrowUtils;
import com.george.usercenter.mapper.UserMapper;
import com.george.usercenter.model.domain.User;
import com.george.usercenter.model.request.*;
import com.george.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.george.usercenter.constant.UserConstant.*;

/**
 * 用户接口
 */
@RestController
@Slf4j
@RequestMapping("/user")
//@CrossOrigin(origins = {"http://129.226.152.209:80","http://localhost:8081","http://127.0.0.1:5173/",
//        "http://user.code-club.fun","http://user.code-club.fun:80"},allowCredentials = "true")
//@CrossOrigin(origins ={"http://127.0.0.1:5173"},allowCredentials = "true")
public class UserController {

    @Resource
    UserService userService;

    @Resource
    UserMapper userMapper;

    @Resource
    RedisTemplate<String,Object> redisTemplate;

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

        if (!userService.isAdmin(request)){
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

    @GetMapping("/recommend")
    public BaseResponse<Page<User>> userRecommend(@RequestParam long pageSize,@RequestParam long pageNum, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
       //设置该方法的redisKey
        String redisKey = String.format(RedisKeyName.USER_RECOMMEND+":%s",loginUser.getId());

        //如果redis有数据直接取出来
        Page<User> userPage = (Page<User>)valueOperations.get(redisKey);
        if (userPage!=null) {
            return ResultUtils.success(userPage);
        }
        //如果没有数据，从数据库读，并存入redis中
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userPage = userService.page(new Page<>(pageNum ,pageSize),userQueryWrapper);
        //写缓存
        try {
            valueOperations.set(redisKey,userPage,30000, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("redis set key error",e);
        }

        return ResultUtils.success(userPage);
    }


    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        List<User> users = userService.searchUsersByTags(tagNameList);
        System.out.println(users);
        return ResultUtils.success(users);
    }

    @PostMapping("/add")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request){
        if (!userService.isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH,"无管理员权限");
        }
        if (userAddRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        User user = new User();
        if (userAddRequest.getAvatarUrl()==null){
            userAddRequest.setAvatarUrl("https://pgeorge-1310330018.cos.ap-chongqing.myqcloud.com/202311020941065.png");
        }
        BeanUtils.copyProperties(userAddRequest,user);


        String encryptPassword = DigestUtils.md5DigestAsHex((userAddRequest.getUserPassword()+SALT).getBytes());
        user.setUserPassword(encryptPassword);

        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result,ErrorCode.SYSTEM_ERROR,"数据存储出错");
        return ResultUtils.success(user.getId(),"用户添加成功");
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody UserDeleteRequest userDeleteRequest, HttpServletRequest request){
        //todo 一定要记得用户鉴权
        if (!userService.isAdmin(request))
            throw new BusinessException(ErrorCode.NO_AUTH,"无管理员权限");

        if (userDeleteRequest == null || userDeleteRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        boolean result = userService.removeById(userDeleteRequest.getId());
        return ResultUtils.success(result);
    }

//    @GetMapping("/current")
//    public BaseResponse<User> getCurrentUser(HttpServletRequest request){
//        User loginUser = userService.getLoginUser(request);
//        User safetyUser = userService.getSafetyUser(loginUser);
//        return ResultUtils.success(safetyUser);
//    }
@GetMapping("/current")
public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
    Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
    User currentUser = (User) userObj;
    if (currentUser == null) {
        throw new BusinessException(ErrorCode.NO_LOGIN);
    }
    long userId = currentUser.getId();
    // TODO 校验用户是否合法
    User user = userService.getById(userId);
    User safetyUser = userService.getSafetyUser(user);
    return ResultUtils.success(safetyUser);
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
    public BaseResponse<Integer> updateUser(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request){
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限");
        }

        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"数据为空");
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest,user);
        int res = userMapper.updateById(user);
        return ResultUtils.success(res,"更新信息成功");
//        boolean result = userService.updateById(user);
//        ThrowUtils.throwIf(!result,ErrorCode.SYSTEM_ERROR);
//        return ResultUtils.success(true,"更新信息成功");
    }

    @GetMapping("/match")
    public BaseResponse<List<User>> matchUser(long num,HttpServletRequest request){
        if (num<=0 || num>20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数错误");
        }

        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.matchUser(num,loginUser));
    }


}

