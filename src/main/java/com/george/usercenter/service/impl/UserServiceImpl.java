package com.george.usercenter.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.george.usercenter.common.ErrorCode;
import com.george.usercenter.exception.BusinessException;
import com.george.usercenter.model.domain.User;
import com.george.usercenter.service.UserService;
import com.george.usercenter.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.george.usercenter.constant.UserConstant.USER_LOGIN_STATE;


/**
* @author Pgeorge
* @description 针对表【user】的数据库操作Service实现
* @createDate 2023-10-16 21:06:26
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    /**
     * 盐值、混淆密码
     */
    public static final String salt = "salt";


    @Resource
    UserMapper userMapper;

    @Override

    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        // todo 校验
        //todo 1.非空
        if (StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,planetCode)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }

        //todo 2.账户长度不小于4位
        if (userAccount.length()<4)
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号过短");

        //todo 3.密码不小于8位
        if (userPassword.length()<8)
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码过短");

        //todo 4.密码与校验密码相同
        if (!userPassword.equals(checkPassword))
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码与校验密码相同");

        //todo 5.账户不包含特殊字符
        String validPattern = "^[a-zA-Z0-9_]+$";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (!matcher.matches())
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户包含特殊字符");

        //todo 6.账户不重复
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount",userAccount);
        if (this.count(userQueryWrapper)!=0)
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户重复");

        //todo 7. 校验编号不大于8位
        if (planetCode.length() > 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"星球编号过长");
        }

        //todo 8.星球不重复
        userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("planetCode",planetCode);
        if (this.count(userQueryWrapper)!=0)
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"编号重复");

        // 加密

        String encryptPassword = DigestUtils.md5DigestAsHex((userPassword+salt).getBytes());

        // 向数据库插入数据
        User user = new User();

        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);

        boolean res = this.save(user);
        if (!res){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"数据库保存错误");
        }

        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //todo 1.校验

        //todo .非空
        if (StringUtils.isAnyBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号或密码为空");
        }

        //todo .账户长度不小于4位
        if (userAccount.length()<4)
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号长度过短");

        //todo .密码不小于8位
        if (userPassword.length()<8)
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码过短");


        //todo .账户不包含特殊字符
        String validPattern = "^[a-zA-Z0-9_]+$";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (!matcher.matches())
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户包含特殊字符");

        //todo 2.检验密码
        String encryptPassword = DigestUtils.md5DigestAsHex((userPassword+salt).getBytes());

        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount",userAccount);
        userQueryWrapper.eq("userPassword",encryptPassword);
        User user = userMapper.selectOne(userQueryWrapper);

        // 用户不存在
        if (user==null){
            log.info("user login failed, userAccount con not match userPassword");
               return null;
        }


        //todo 3.脱敏
        User safetyUser = getSafetyUser(user);

        // TODO: 4. 记录用户的登录态，存入session中
        request.getSession().setAttribute(USER_LOGIN_STATE,safetyUser);

        //todo 5.返回脱敏后的信息
        return safetyUser;
    }

    @Override
    public User getSafetyUser(User user){
        if (user == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"调用清洗后的用户错误");
        User safetyUser = new User();
        safetyUser.setId(user.getId());
        safetyUser.setUserAccount(user.getUserAccount());
        safetyUser.setUsername(user.getUsername());
        safetyUser.setAvatarUrl(user.getAvatarUrl());
        safetyUser.setGender(user.getGender());
        safetyUser.setPhone(user.getPhone());
        safetyUser.setEmail(user.getEmail());
        safetyUser.setUserStatus(user.getUserStatus());
        safetyUser.setCreateTime(user.getCreateTime());
        safetyUser.setUserRole(user.getUserRole());
        safetyUser.setPlanetCode(user.getPlanetCode());
        return safetyUser;
    }

    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }
}




