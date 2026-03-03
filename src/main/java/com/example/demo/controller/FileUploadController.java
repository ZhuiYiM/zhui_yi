package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.service.FileUploadService;
import com.example.demo.utils.JwtUtil;  // 添加导入
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private JwtUtil jwtUtil;  // 添加 JwtUtil 注入

    // 上传图片
    @PostMapping("/image")
    public Result uploadImage(@RequestParam MultipartFile file,
                              @RequestHeader("Authorization") String token) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        Integer userId = extractUserIdFromToken(token);
        return fileUploadService.uploadImage(file, userId);
    }

    // 从 Token 中提取用户 ID 的辅助方法
    private Integer extractUserIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token != null && !token.trim().isEmpty()) {
            return jwtUtil.getUserIdFromToken(token);
        }

        return null; // 表示未登录用户
    }

    // 批量上传图片
    @PostMapping("/images")
    public Result uploadImages(@RequestParam("files") MultipartFile[] files,
                               @RequestHeader("Authorization") String token) {
        if (files == null || files.length == 0) {
            return Result.error("请选择要上传的文件");
        }

        Integer userId = extractUserIdFromToken(token);
        return fileUploadService.uploadImages(files, userId);
    }

    // 删除文件
    @DeleteMapping("/file")
    public Result deleteFile(@RequestBody Map<String, String> requestBody,
                             @RequestHeader("Authorization") String token) {
        Integer userId = extractUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        
        String fileUrl = requestBody.get("fileUrl");
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            return Result.error(400, "文件 URL 不能为空");
        }
        return fileUploadService.deleteFile(fileUrl, userId);
    }
}
