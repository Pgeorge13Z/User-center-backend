package com.george.usercenter.utils;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AlgorithmUtilsTest {

    @Test
    void minDistance() {
        List<String> list1 = Arrays.asList("java", "大二", "男");
        List<String> list2 = Arrays.asList("java", "大二", "男");
        List<String> list3 = Arrays.asList("python", "大三", "女");
        int score1 = AlgorithmUtils.minDistance(list1, list2);
        int score2 = AlgorithmUtils.minDistance(list1, list3);
        System.out.println(score1+" "+score2);
    }

    @Test
    void testMinDistance() {
    }
}