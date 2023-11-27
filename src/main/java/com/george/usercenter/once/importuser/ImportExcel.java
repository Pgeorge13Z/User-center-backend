package com.george.usercenter.once.importuser;

import com.alibaba.excel.EasyExcel;

import java.util.List;

public class ImportExcel {
    public static void main(String[] args) {
        String fileName =  "D:\\projects\\用户中心系统\\user-center\\src\\main\\resources\\testExcel.xlsx";
        //readByListener(fileName);
        synchronousRead(fileName);
    }

    public static void readByListener(String fileName) {
        // 写法1：JDK8+ ,不用额外写一个DemoDataListener
        EasyExcel.read(fileName, TableUserInfo.class, new DemoDataListener()).sheet().doRead();

    }

    public static void synchronousRead(String fileName) {
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<TableUserInfo> totalDataList =
                EasyExcel.read(fileName).head(TableUserInfo.class).sheet().doReadSync();
        for (TableUserInfo tableUserInfo : totalDataList) {
            System.out.println(tableUserInfo);
        }

    }
}
