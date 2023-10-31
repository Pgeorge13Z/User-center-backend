package com.george.usercenter.controller;

import com.george.usercenter.common.BaseResponse;
import com.george.usercenter.common.ResultUtils;
import com.george.usercenter.service.OssService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping("/fileOss")
@CrossOrigin(origins = "http://user.code-club.fun",allowCredentials = "true")
public class OssController {
    @Resource
    private OssService ossService;

    @PostMapping("/upload")
    public BaseResponse<String> uploadOssFile(@RequestParam(required = false)MultipartFile file){
        //获取上传的文件
        if (file.isEmpty()) {
            return null;
        }
        //返回上传到oss的路径
        String url = ossService.uploadFileAvatar(file);
        //返回r对象
        return ResultUtils.success(url);
    }
}
