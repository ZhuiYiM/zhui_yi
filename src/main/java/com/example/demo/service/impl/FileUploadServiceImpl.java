package com.example.demo.service.impl;

import com.example.demo.common.Result;
import com.example.demo.service.FileUploadService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    private static final String UPLOAD_DIR = "uploads/images/";

    @Override
    public Result uploadImage(MultipartFile file, Integer userId) {
        try {
            // 校验文件类型和大小
            if (!file.getContentType().startsWith("image/")) {
                return Result.error("文件类型不支持");
            }
            if (file.getSize() > 2 * 1024 * 1024) {
                return Result.error("文件大小不能超过2MB");
            }

            // 创建上传目录
            Path uploadPath = Paths.get(UPLOAD_DIR);
            Files.createDirectories(uploadPath);

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf('.')) : ".jpg";
            String fileName = UUID.randomUUID().toString() + extension;

            // 构建完整文件路径
            Path filePath = uploadPath.resolve(fileName);

            // 保存文件
            file.transferTo(filePath.toFile());

            // 返回文件访问路径
            String fileUrl = "/images/" + fileName;

            return Result.success(fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }

    @Override
    public Result uploadImages(MultipartFile[] files, Integer userId) {
        try {
            java.util.List<String> uploadedUrls = new java.util.ArrayList<>();
            
            for (MultipartFile file : files) {
                // 校验文件类型和大小
                if (!file.getContentType().startsWith("image/")) {
                    return Result.error("文件类型不支持：" + file.getOriginalFilename());
                }
                if (file.getSize() > 5 * 1024 * 1024) {
                    return Result.error("文件大小不能超过 5MB: " + file.getOriginalFilename());
                }

                // 创建上传目录
                Path uploadPath = Paths.get(UPLOAD_DIR);
                Files.createDirectories(uploadPath);

                // 生成唯一文件名
                String originalFilename = file.getOriginalFilename();
                String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf('.')) : ".jpg";
                String fileName = UUID.randomUUID().toString() + extension;

                // 构建完整文件路径
                Path filePath = uploadPath.resolve(fileName);

                // 保存文件
                file.transferTo(filePath.toFile());

                // 返回文件访问路径
                String fileUrl = "/images/" + fileName;
                uploadedUrls.add(fileUrl);
            }

            return Result.success(uploadedUrls);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }

    @Override
    public Result deleteFile(String fileUrl, Integer userId) {
        try {
            // 从 URL 中提取文件路径
            String relativePath = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
            Path filePath = Paths.get(UPLOAD_DIR).resolve(Paths.get(relativePath).getFileName());

            // 检查文件是否存在
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return Result.success("文件删除成功");
            } else {
                return Result.error("文件不存在");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件删除失败：" + e.getMessage());
        }
    }
}