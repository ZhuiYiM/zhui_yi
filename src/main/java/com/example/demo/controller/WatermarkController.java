package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.utils.ImageWatermarkUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 图片水印检测控制器
 * 用于从上传的图片中提取隐藏的用户 ID 信息
 */
@RestController
@RequestMapping("/api/watermark")
@CrossOrigin
public class WatermarkController {

    @Autowired
    private ImageWatermarkUtil watermarkUtil;

    /**
     * 检测图片中的水印并提取用户 ID
     * 
     * @param file 上传的图片文件
     * @return 包含用户 ID 的检测结果
     */
    @PostMapping("/extract")
    public Result extractWatermark(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return Result.error("请选择要检测的图片");
            }

            // 读取图片字节
            byte[] imageBytes = file.getBytes();

            // 提取水印
            Integer userId = watermarkUtil.extractWatermark(imageBytes);

            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            if (userId != null) {
                data.put("hasWatermark", true);
                data.put("userId", userId);
                data.put("message", "成功提取到用户 ID: " + userId);
            } else {
                data.put("hasWatermark", false);
                data.put("userId", null);
                data.put("message", "未检测到有效水印");
            }

            return Result.success(data);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("检测失败：" + e.getMessage());
        }
    }

    /**
     * 批量检测多张图片的水印
     * 
     * @param files 多个图片文件
     * @return 包含所有图片检测结果的列表
     */
    @PostMapping("/extract/batch")
    public Result batchExtractWatermark(@RequestParam("files") MultipartFile[] files) {
        try {
            if (files == null || files.length == 0) {
                return Result.error("请选择要检测的图片");
            }

            // 构建结果列表
            java.util.List<Map<String, Object>> results = new java.util.ArrayList<>();

            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue;
                }

                byte[] imageBytes = file.getBytes();
                Integer userId = watermarkUtil.extractWatermark(imageBytes);

                Map<String, Object> result = new HashMap<>();
                result.put("filename", file.getOriginalFilename());
                result.put("hasWatermark", userId != null);
                result.put("userId", userId);

                results.add(result);
            }

            return Result.success(results);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("批量检测失败：" + e.getMessage());
        }
    }

    /**
     * 验证图片是否包含特定用户的水印
     * 
     * @param file 上传的图片文件
     * @param userId 要验证的用户 ID
     * @return 验证结果
     */
    @PostMapping("/verify")
    public Result verifyWatermark(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Integer userId
    ) {
        try {
            if (file == null || file.isEmpty()) {
                return Result.error("请选择要验证的图片");
            }

            if (userId == null) {
                return Result.error("用户 ID 不能为空");
            }

            // 读取图片字节
            byte[] imageBytes = file.getBytes();

            // 提取水印
            Integer extractedUserId = watermarkUtil.extractWatermark(imageBytes);

            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            boolean isMatch = extractedUserId != null && extractedUserId.equals(userId);
            
            data.put("isMatch", isMatch);
            data.put("extractedUserId", extractedUserId);
            data.put("expectedUserId", userId);
            
            String message = isMatch 
                    ? "验证通过：图片属于用户 " + userId 
                    : "验证失败：图片不属于指定用户";
            data.put("message", message);

            return Result.success(data);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("验证失败：" + e.getMessage());
        }
    }
}
