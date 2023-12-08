package com.george.usercenter.service;

import com.george.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author Pgeorge
* @description 针对表【user】的数据库操作Service
* @createDate 2023-10-16 21:06:26
*/
public interface UserService extends IService<User> {



    /**
     *
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */

    long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);

    /**
     *
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);


    /**
     * 用户脱敏
     * @param user
     * @return
     */
    User getSafetyUser(User user);

    /**
     * 用户注销
     * @param request
     */
    int userLogout(HttpServletRequest request);

    /**
     * 获取当前登录用户
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户修改密码
     * @param oldPassword
     * @param newPassword
     * @param request
     * @return
     */
    boolean updateUserPassword(String oldPassword,String newPassword,HttpServletRequest request);

    /**
     * 根据用户标签搜索用户
     * @param tagNameList 用户拥有的标签
     * @return 脱敏后的用户
     */
    List<User> searchUsersByTags(List<String> tagNameList);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param loginUser
     * @return
     */
    boolean isAdmin(User loginUser);


    List<User> matchUser(long num, User loginUser);
}
