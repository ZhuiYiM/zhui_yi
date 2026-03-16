package com.example.demo.utils;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 图片数字水印工具类
 * 用于在图片中嵌入不可见的用户 ID 信息，实现隐蔽追踪
 */
@Component
public class ImageWatermarkUtil {

    /**
     * 在图片中嵌入数字水印（用户 ID）
     * 使用 LSB（最低有效位）算法，修改像素点的最低位来存储信息
     * 
     * @param imageBytes 原始图片字节数组
     * @param userId 用户 ID
     * @return 嵌入水印后的图片字节数组
     */
    public byte[] embedWatermark(byte[] imageBytes, Integer userId) {
        try {
            // 读取图片
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(inputStream);
            
            if (image == null) {
                return imageBytes; // 如果读取失败，返回原图
            }
            
            // 将用户 ID 转换为二进制字符串
            String binaryId = Integer.toBinaryString(userId);
            // 填充到 32 位
            while (binaryId.length() < 32) {
                binaryId = "0" + binaryId;
            }
            
            // 添加开始和结束标记
            String watermarkData = "11110000" + binaryId + "00001111";
            
            // 检查图片是否有足够的像素来存储水印
            int requiredPixels = (int) Math.ceil(watermarkData.length() / 3.0);
            if (image.getWidth() * image.getHeight() < requiredPixels) {
                return imageBytes; // 图片太小，不嵌入水印
            }
            
            // 创建可编辑的图片副本
            BufferedImage watermarkedImage = new BufferedImage(
                image.getWidth(), 
                image.getHeight(), 
                BufferedImage.TYPE_INT_ARGB
            );
            
            // 复制原始图片数据
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    watermarkedImage.setRGB(x, y, image.getRGB(x, y));
                }
            }
            
            // 嵌入水印数据
            int dataBitIndex = 0;
            outerLoop:
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    if (dataBitIndex >= watermarkData.length()) {
                        break outerLoop;
                    }
                    
                    int rgb = watermarkedImage.getRGB(x, y);
                    
                    // 提取 RGB 分量
                    int alpha = (rgb >> 24) & 0xFF;
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;
                    
                    // 修改最低有效位（每个通道存储 1 bit）
                    if (dataBitIndex < watermarkData.length()) {
                        red = (red & 0xFE) | Integer.parseInt(String.valueOf(watermarkData.charAt(dataBitIndex)));
                        dataBitIndex++;
                    }
                    if (dataBitIndex < watermarkData.length()) {
                        green = (green & 0xFE) | Integer.parseInt(String.valueOf(watermarkData.charAt(dataBitIndex)));
                        dataBitIndex++;
                    }
                    if (dataBitIndex < watermarkData.length()) {
                        blue = (blue & 0xFE) | Integer.parseInt(String.valueOf(watermarkData.charAt(dataBitIndex)));
                        dataBitIndex++;
                    }
                    
                    // 合并回 ARGB 格式
                    int newRgb = (alpha << 24) | (red << 16) | (green << 8) | blue;
                    watermarkedImage.setRGB(x, y, newRgb);
                }
            }
            
            // 输出图片到字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(watermarkedImage, "png", outputStream);
            
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            e.printStackTrace();
            return imageBytes; // 处理失败时返回原图
        }
    }
    
    /**
     * 从图片中提取数字水印（用户 ID）
     * 
     * @param imageBytes 图片字节数组
     * @return 提取的用户 ID，如果未找到水印则返回 null
     */
    public Integer extractWatermark(byte[] imageBytes) {
        try {
            // 读取图片
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(inputStream);
            
            if (image == null) {
                return null;
            }
            
            // 提取二进制数据
            StringBuilder extractedBits = new StringBuilder();
            outerLoop:
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int rgb = image.getRGB(x, y);
                    
                    // 提取 RGB 分量
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;
                    
                    // 提取最低有效位
                    extractedBits.append(red & 1);
                    if (extractedBits.length() >= 48) { // 8 位开始标记 + 32 位 ID + 8 位结束标记
                        break outerLoop;
                    }
                    
                    extractedBits.append(green & 1);
                    if (extractedBits.length() >= 48) {
                        break outerLoop;
                    }
                    
                    extractedBits.append(blue & 1);
                    if (extractedBits.length() >= 48) {
                        break outerLoop;
                    }
                }
            }
            
            String extractedData = extractedBits.toString();
            
            // 验证开始和结束标记
            if (!extractedData.startsWith("11110000") || !extractedData.endsWith("00001111")) {
                return null; // 未找到有效水印
            }
            
            // 提取中间的 32 位用户 ID
            String userIdBinary = extractedData.substring(8, 40);
            int userId = Integer.parseInt(userIdBinary, 2);
            
            // 验证用户 ID 的有效性（合理范围）
            if (userId <= 0 || userId > 1000000) {
                return null;
            }
            
            return userId;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 提取失败
        }
    }
    
    /**
     * 检测图片是否包含水印
     * 
     * @param imageBytes 图片字节数组
     * @return true 如果包含水印，false 否则
     */
    public boolean hasWatermark(byte[] imageBytes) {
        return extractWatermark(imageBytes) != null;
    }
}
