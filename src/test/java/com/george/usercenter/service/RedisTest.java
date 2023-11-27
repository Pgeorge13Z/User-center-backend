package com.george.usercenter.service;
import java.util.Date;

import com.george.usercenter.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.Resource;

@SpringBootTest
@ActiveProfiles("my")
public class RedisTest {
    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void test() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //增
        valueOperations.set("zxjString","dog");
        valueOperations.set("zxjInt",1);
        valueOperations.set("zxjDouble",2.0);
        User user = new User();
        user.setId(1L);
        user.setUsername("zxjUserName");

        valueOperations.set("zxjUser",user);
        //查
        Object zxj = valueOperations.get("zxjString");
        Assertions.assertEquals("dog",zxj);
         zxj = valueOperations.get("zxjInt");
        Assertions.assertEquals(1,zxj);
         zxj = valueOperations.get("zxjDouble");
        Assertions.assertEquals(2.0,zxj);
         zxj = valueOperations.get("zxjUser");
        System.out.println(zxj);

        //删
        redisTemplate.delete("zxjString");
    }
}
