package com.george.usercenter.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.*;

import com.george.usercenter.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

@SpringBootTest
@ActiveProfiles("my")
public class InsertUsersTest {
    @Resource
    private UserService userService;


    /**
     * CPU 密集型（比如加减乘除很多，需要高频CPU计算） 分配的核心数 = CPU -1
     * IO 密集型： 网络传输、数据库、写磁盘 分配的核心线程数可以大于CPU核数。
     */

    /**
     * 核心线程数量（默认运行的数量）、最大多少个线程、线程存活时间、时间的单位（与存活时间对应）、任务队列（size个任务)、策略（可选）
     * 什么时候会超过默认的线程数，当任务数达到了10000，需要更多的线程，线程会大于60到1000.当加到1000时仍然不够，会采取对应的策略，默认是中断策略。
     */
    private Executor executorService = new ThreadPoolExecutor(60,1000,10000, TimeUnit.MINUTES,new ArrayBlockingQueue<>(10000));

    /**
     * 批量插入用户
     */
    @Test
    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 100000;
        ArrayList<User> userList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUserAccount("fakezxj");
            user.setUsername("假zxj");
            user.setAvatarUrl("https://pgeorge-1310330018.cos.ap-chongqing.myqcloud.com/202311020941065.png");
            user.setUserPassword("123456789");
            user.setGender(0);
            user.setPhone("123456");
            user.setEmail("123@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("1111");
            user.setTags("[]");
//            userService.save(user);
            userList.add(user);
        }
        //0.4秒1000条
        //8s 10万条
        userService.saveBatch(userList, 10000);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeSeconds());
    }




    /**
     * 并发批量插入用户
     */
    @Test
    public void doConcurrencyInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 100000;
        //分10组
        int batchSize = 5000;
        int j = 0;
        ArrayList<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            ArrayList<User> userList = new ArrayList<>();
            while (true) {
                j++;
                User user = new User();
                user.setUserAccount("fakezxj");
                user.setUsername("假zxj");
                user.setAvatarUrl("https://pgeorge-1310330018.cos.ap-chongqing.myqcloud.com/202311020941065.png");
                user.setUserPassword("123456789");
                user.setGender(0);
                user.setPhone("123456");
                user.setEmail("123@qq.com");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setPlanetCode("1111");
                user.setTags("[]");
                userList.add(user);
                if (j % batchSize == 0) {
                    break;
                }
            }

            //异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                userService.saveBatch(userList, 10000);
            },executorService);
            futures.add(future);
        }

        //并发 2.77s
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();

        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeSeconds());
    }
}
