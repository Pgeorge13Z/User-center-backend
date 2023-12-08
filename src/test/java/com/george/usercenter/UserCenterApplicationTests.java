package com.george.usercenter;

import com.george.usercenter.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.List;
import java.util.Scanner;

@SpringBootTest
class UserCenterApplicationTests {

    @Resource
    private UserMapper userMapper;

    @Test
    void contextLoads() {

    }



}
