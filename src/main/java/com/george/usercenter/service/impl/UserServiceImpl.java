package com.george.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.george.usercenter.common.ErrorCode;
import com.george.usercenter.exception.BusinessException;
import com.george.usercenter.exception.ThrowUtils;
import com.george.usercenter.model.domain.User;
import com.george.usercenter.service.UserService;
import com.george.usercenter.mapper.UserMapper;
import com.george.usercenter.utils.AlgorithmUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.george.usercenter.constant.UserConstant.*;


/**
 * @author Pgeorge
 * @description 针对表【user】的数据库操作Service实现
 * @createDate 2023-10-16 21:06:26
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {


    @Resource
    UserMapper userMapper;


    @Override

    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        // todo 校验
        //todo 1.非空
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        //todo 2.账户长度不小于4位
        if (userAccount.length() < 4)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");

        //todo 3.密码不小于8位
        if (userPassword.length() < 8)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");

        //todo 4.密码与校验密码相同
        if (!userPassword.equals(checkPassword))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码与校验密码相同");

        //todo 5.账户不包含特殊字符
        String validPattern = "^[a-zA-Z0-9_]+$";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (!matcher.matches())
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户包含特殊字符");

        //todo 6.账户不重复
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount", userAccount);
        if (this.count(userQueryWrapper) != 0)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户重复");

        //todo 7. 校验编号不大于8位
        if (planetCode.length() > 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号过长");
        }

        //todo 8.星球不重复
        userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("planetCode", planetCode);
        if (this.count(userQueryWrapper) != 0)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编号重复");

        // 加密

        String encryptPassword = DigestUtils.md5DigestAsHex((userPassword + SALT).getBytes());

        // 向数据库插入数据
        User user = new User();

        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        user.setAvatarUrl("https://pgeorge-1310330018.cos.ap-chongqing.myqcloud.com/202311020941065.png");
        user.setUsername(userAccount);

        boolean res = this.save(user);
        if (!res) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据库保存错误");
        }

        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //todo 1.校验

        //todo .非空
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码为空");
        }

        //todo .账户长度不小于4位
        if (userAccount.length() < 4)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度过短");

        //todo .密码不小于8位
        if (userPassword.length() < 8)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码过短");


        //todo .账户不包含特殊字符
        String validPattern = "^[a-zA-Z0-9_]+$";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (!matcher.matches())
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户包含特殊字符");

        //todo 2.检验密码
        String encryptPassword = DigestUtils.md5DigestAsHex((userPassword + SALT).getBytes());

        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount", userAccount);
        userQueryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(userQueryWrapper);

        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount con not match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不存在或密码不正确");
        }


        //todo 3.脱敏
        User safetyUser = getSafetyUser(user);

        // TODO: 4. 记录用户的登录态，存入session中
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);

        //todo 5.返回脱敏后的信息
        return safetyUser;
    }

    @Override
    public User getSafetyUser(User user) {
        if (user == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "调用清洗后的用户错误");
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
        safetyUser.setTags(user.getTags());
        return safetyUser;
    }

    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null)
            throw new BusinessException(ErrorCode.NO_LOGIN, "未登录");

        // 数据可能是实时变化的，用id去查库
//        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
//        userQueryWrapper.eq("id",currentUser.getId());
//
//        userMapper.selectOne(userQueryWrapper);
        //todo 校验用户是否合法，是否有状态位标记为不能使用之类的。

        User user = userMapper.selectById(currentUser.getId());
        //this.getById(currentUser.getId())
        return user;
    }

    /**
     * Mybatis-plus的更新操作
     * 1. 根据id 更新
     * User user = new User();
     * user.setUserId(1);
     * user.setAge(29);
     * <p>
     * user.updateById();
     * or
     * Integer rows = userMapper.updateById(user);
     * <p>
     * 2. 条件参数更新
     * UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
     * updateWrapper.eq("name","shimin");
     * <p>
     * User user = new User();
     * user.setAge(18);
     * <p>
     * Integer rows = userMapper.update(user, updateWrapper);
     */

    @Override
    public boolean updateUserPassword(String oldPassword, String newPassword, HttpServletRequest request) {
        if (StringUtils.isAnyBlank(oldPassword, newPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码为空");
        }

        if (oldPassword.equals(newPassword))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新密码与旧密码相同");

        if (newPassword.length() < 8)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码过短");

        User loginUser = getLoginUser(request);

        String oldEncryptPassword = DigestUtils.md5DigestAsHex((oldPassword + SALT).getBytes());
        if (!loginUser.getUserPassword().equals(oldEncryptPassword))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "当前密码输入错误");

        Long id = loginUser.getId();

        User user = new User();
        user.setId(id);

        //获取加密后新密码
        String encryptPassword = DigestUtils.md5DigestAsHex((newPassword + SALT).getBytes());

        user.setUserPassword(encryptPassword);
        boolean result = updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "数据更新失败");

        return true;


    }

    /**
     * 在内存中查询，更灵活
     *
     * @param tagNameList 用户拥有的标签
     * @return
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "无请求标签");
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(userQueryWrapper);
        Gson gson = new Gson();

        return userList.stream().filter(user -> {
                    String tags = user.getTags();
                    if (StringUtils.isBlank(tags))
                        return false;
                    Set<String> tagNameSet = gson.fromJson(tags, new TypeToken<Set<String>>() {
                    }.getType());
                    //tagNameSet = Optional.ofNullable(tagNameSet).orElse(new HashSet<>());
                    //对象转回json
                    // gson.toJson(tagNameSet)
                    for (String tagName : tagNameList) {
                        if (!tagNameSet.contains(tagName))
                            return false;
                    }
                    return true;
                }
        ).map(this::getSafetyUser).collect(Collectors.toList());

    }

    /**
     * 数据库查询
     *
     * @param tagNameList
     * @return
     */
    @Deprecated
    private List<User> searchUsersByTagsBySql(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "无请求标签");
        }


        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        for (String tagName : tagNameList) {
            userQueryWrapper.like("tags", tagName);
        }

        List<User> users = userMapper.selectList(userQueryWrapper);
        return users.stream().map(this::getSafetyUser).collect(Collectors.toList());


    }

    /**
     * 检验是否为管理员
     *
     * @param request
     * @return false:不是管理员 true:为管理员
     */
    public boolean isAdmin(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;

        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 是否为管理员
     *
     * @param loginUser
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public List<User> matchUser(long num, User loginUser) {
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.select("id", "tags");
        userQueryWrapper.isNotNull("tags");
        List<User> userList = this.list(userQueryWrapper);
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        //用户列表下标=》相似度
//        ArrayList<Pair<User, Long>> list = new ArrayList<>();
        HashMap<User, Long> map = new HashMap<>();
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            if (StringUtils.isBlank(userTags) || user.getId() == loginUser.getId())
                continue;
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
//            list.add(new Pair<>(user, distance));
            map.put(user,distance);
        }
            //按距离从小到大排序
//            List<Pair<User, Long>> topUserDistanceList
//                    = list.stream().
//                    sorted((a, b) -> (int) (a.getValue() - b.getValue()))
//                    .limit(num)
//                    .collect(Collectors.toList());
        ArrayList<Map.Entry<User, Long>> list = new ArrayList<>(map.entrySet());
                    List<Map.Entry<User, Long>> topUserDistanceList
                    = list.stream().
                    sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                    .limit(num)
                    .collect(Collectors.toList());

        List<Long> userIdList = topUserDistanceList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("id", userIdList);
            Map<Long, List<User>> userIdUserListMap =
                    this.list(queryWrapper).stream().
                            map(this::getSafetyUser).collect(Collectors.groupingBy(User::getId));
            List<User> ResultList = new ArrayList<>();
            for (Long userId : userIdList) {
                ResultList.add(userIdUserListMap.get(userId).get(0));
            }
        return ResultList;

        }


    }





