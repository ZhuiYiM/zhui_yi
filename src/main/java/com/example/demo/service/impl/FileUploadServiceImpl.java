package com.example.demo.service.impl;

import com.example.demo.common.Result;
import com.example.demo.service.FileUploadService;
import com.example.demo.utils.ImageWatermarkUtil;
import org.springframework.beans.factory.annotation.Autowired;
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

    // 上传目录配置
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/images/";
    
    @Autowired
    private ImageWatermarkUtil watermarkUtil;

    @Override
    public Result uploadImage(MultipartFile file, Integer userId) {
        try {
            // 校验文件是否为空
            if (file == null || file.isEmpty()) {
                return Result.error("请选择要上传的文件");
            }
                
            // 校验文件类型（允许的图片类型）
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("只支持图片文件格式（jpg、png、gif 等）");
            }
                
            // 校验文件大小（2MB）
            if (file.getSize() > 2 * 1024 * 1024) {
                return Result.error("文件大小不能超过 2MB");
            }
    
            // 创建上传目录
            Path uploadPath = Paths.get(UPLOAD_DIR);
            Files.createDirectories(uploadPath);
    
            // 生成唯一文件名：时间戳 + UUID + 原扩展名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf('.')) 
                    : ".jpg";
            String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "") + extension;
    
            // 构建完整文件路径
            Path filePath = uploadPath.resolve(fileName);
    
            // 读取文件字节
            byte[] fileBytes = file.getBytes();
            
            // 嵌入数字水印（仅对 PNG 格式有效，因为 PNG 是无损压缩）
            if (".png".equalsIgnoreCase(extension)) {
                try {
                    fileBytes = watermarkUtil.embedWatermark(fileBytes, userId);
                } catch (Exception e) {
                    // 水印嵌入失败不影响正常上传
                    e.printStackTrace();
                }
            }
    
            // 保存文件（如果嵌入了水印，保存的是带水印的字节）
            Files.write(filePath, fileBytes);
    
            // 返回文件访问 URL（相对于 WebConfig 中配置的 /images/**）
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
            if (files == null || files.length == 0) {
                return Result.error("请选择要上传的文件");
            }
            
            java.util.List<String> uploadedUrls = new java.util.ArrayList<>();
            
            for (MultipartFile file : files) {
                // 校验文件是否为空
                if (file == null || file.isEmpty()) {
                    continue; // 跳过空文件
                }
                
                // 校验文件类型
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return Result.error("只支持图片文件格式，问题文件：" + file.getOriginalFilename());
                }
                
                // 校验文件大小（5MB，批量上传放宽限制）
                if (file.getSize() > 5 * 1024 * 1024) {
                    return Result.error("单个文件大小不能超过 5MB: " + file.getOriginalFilename());
                }

                // 创建上传目录
                Path uploadPath = Paths.get(UPLOAD_DIR);
                Files.createDirectories(uploadPath);

                // 生成唯一文件名
                String originalFilename = file.getOriginalFilename();
                String extension = originalFilename != null && originalFilename.contains(".") 
                        ? originalFilename.substring(originalFilename.lastIndexOf('.')) 
                        : ".jpg";
                String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "") + extension;

                // 构建完整文件路径
                Path filePath = uploadPath.resolve(fileName);

                // 读取文件字节
                byte[] fileBytes = file.getBytes();
                
                // 嵌入数字水印（仅对 PNG 格式有效）
                if (".png".equalsIgnoreCase(extension)) {
                    try {
                        fileBytes = watermarkUtil.embedWatermark(fileBytes, userId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // 保存文件
                Files.write(filePath, fileBytes);

                // 返回文件访问 URL
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