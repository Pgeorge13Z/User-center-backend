package com.george.usercenter.service;
import java.util.Date;

import com.george.usercenter.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务测试
 *
 * @author george
 */
@SpringBootTest
public class UserServiceTest {
    @Resource
    private UserService userService;

    @Test
    public void testAddUser() {
        User user = new User();
        user.setUserAccount("123");
        user.setUsername("zxj");
        user.setAvatarUrl("https://images.zsxq.com/FtYI3LbaxIGsWneGQiZtCkACfq6o?e=1701359999&token=kIxbL07-8jAj8w1n4s9zv64FuZZNEATmlU_Vm6zD:NgKZ5tTQKvXO6PiX1_YJiyNt7Pc=");
        user.setUserPassword("123");
        user.setGender(0);
        user.setPhone("1365");
        user.setEmail("aqaqzxs.com");


        boolean res = userService.save(user);

        assertTrue(res);
        System.out.println(user.getId());
    }

    @Test
    void userRegister() {
        String userAccount = "zxjzxj";
        String userPassword = "";
        String checkPassword = "123456";
        String planetCode = "1";
        long result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        Assertions.assertEquals(-1,result);
        userAccount = "zxj";
        result= userService.userRegister(userAccount,userPassword,checkPassword,planetCode);
        Assertions.assertEquals(-1,result);
        userAccount = "zxjzxj";
        userPassword= "123";
        result= userService.userRegister(userAccount,userPassword,checkPassword,planetCode);
        Assertions.assertEquals(-1,result);
        userPassword= "123456";
        result= userService.userRegister(userAccount,userPassword,checkPassword,planetCode);
        Assertions.assertEquals(-1,result);

        userPassword= "123456789";
        checkPassword = "123456789";
        result= userService.userRegister(userAccount,userPassword,checkPassword,planetCode);
        Assertions.assertEquals(-1,result);

        userAccount = "yu  pi";
        userPassword= "12345678912";
        checkPassword = "12345678912";
        result= userService.userRegister(userAccount,userPassword,checkPassword,planetCode);
        Assertions.assertEquals(-1,result);


        userAccount = "zxjPlanet2";
        userPassword= "123456789";
        checkPassword = "123456789";
        result= userService.userRegister(userAccount,userPassword,checkPassword,planetCode);
        Assertions.assertTrue(result>0);

    }
}