package com.george.usercenter.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.george.usercenter.constant.RedisKeyName;
import com.george.usercenter.model.domain.User;
import com.george.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Component
@Slf4j
public class PreCacheJob {

    @Resource
    UserService userService;

    @Resource
    RedisTemplate<String,Object> redisTemplate;

    @Resource
    RedissonClient redissonClient;

    private List<Long> mainUserList = Arrays.asList(1L);

    @Scheduled(cron = "0 05  23 * * *")
    public void doCacheRecommendUser() {
        RLock lock = redissonClient.getLock(RedisKeyName.PRECACHEJOB_DOCACHE_LOCK);
        try {

            if (lock.tryLock(0,-1,TimeUnit.MILLISECONDS)){
                for (Long userId : mainUserList) {
                    QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
                    Page<User> userPage = userService.page(new Page<>(1 ,20),userQueryWrapper);
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    String redisKey = String.format(RedisKeyName.USER_RECOMMEND+":%s",userId);

                    //写缓存
                    try {
                        valueOperations.set(redisKey,userPage,30000, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        log.error("redis set key error",e);
                    }
                }
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
