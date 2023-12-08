package com.george.usercenter.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Scanner;

@SpringBootTest
@ActiveProfiles("my")
public class F1_test {
    @Test
    void compute() {
        double FP=(double)3274/8296;
        double TP=(double)5022/8296;
        double FN=(double)6/36;
        double TN=(double)30/36;

        double precision = TP/(TP+FP);
        double Recall = TP/(TP+FN);

        precision=0.7327;
        Recall = 0.8691;
        double F1 = 2*(precision * Recall)/(precision+Recall);

        System.out.println(precision+"  "+Recall+"   "+F1);
    }

    @Test
    void compute2() {
        Scanner scanner = new Scanner(System.in);

        double precision ;
        double Recall ;
        Double[] precisions = new Double[]{0.7932,0.9621,0.9093};
        Double[] recalls = new Double[]{0.5834,0.6375,0.9179};


        for (int i = 0; i < precisions.length; i++) {
            precision = precisions[i];
            Recall =recalls[i];
            double F1 = 2*(precision * Recall)/(precision+Recall);
            System.out.println(F1);
        }

    }
}
