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

    @Test
    void compute() {
        double FP=(double)3274/8296;
        double TP=(double)5022/8296;
        double FN=(double)6/36;
        double TN=(double)30/36;

        double precision = TP/(TP+FP);
        double Recall = TP/(TP+FN);
        double F1 = 2*(precision * Recall)/(precision+Recall);

        System.out.println(precision+"  "+Recall+"   "+F1);
    }

    @Test
    void compute2() {
        Scanner scanner = new Scanner(System.in);

        double precision ;
        double Recall ;
        Double[] precisions = new Double[]{0.9723,0.9601,0.9833};
        Double[] recalls = new Double[]{0.9891,0.9663,0.9627};


        for (int i = 0; i < precisions.length; i++) {
            precision = precisions[i];
            Recall =recalls[i];
            double F1 = 2*(precision * Recall)/(precision+Recall);
            System.out.println(F1);
        }

    }

}
