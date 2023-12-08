package com.george.usercenter.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.george.usercenter.constant.RedisKeyName;
import com.george.usercenter.model.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Slf4j
@ActiveProfiles("my")
public class RedissonTest {
    @Resource
    RedissonClient redissonClient;

    @Test
    public void test() {
        RList<Object> list = redissonClient.getList("test-list");
        list.add("zxj");
        System.out.println(list.get(0));
        list.remove(0);
    }

    @Test
    public void doCacheRecommendUserTest() {
        RList<Object> list = redissonClient.getList("test-list");
        list.add("zxj");

        RLock lock = redissonClient.getLock(RedisKeyName.PRECACHEJOB_DOCACHE_LOCK);
        try {
            if (lock.tryLock(0,-1, TimeUnit.MILLISECONDS)){
                Thread.sleep(100000);
                System.out.println("lock");
            }

        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser lock error",e);
        }
        finally {
            //只释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: "+Thread.currentThread().getId());
                lock.unlock();
            }
        }


    }
}
