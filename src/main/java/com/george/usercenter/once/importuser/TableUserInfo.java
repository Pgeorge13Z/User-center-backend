package com.george.usercenter.once.importuser;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class TableUserInfo {

    /**
     * 用户昵称
     */
    @ExcelProperty("成员昵称")
    private String username;


    /**
     * 成员编号（权限验证）
     */
    @ExcelProperty("成员编号")
    private String planetCode;


}