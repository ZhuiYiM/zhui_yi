package com.example.demo.service;

import com.example.demo.common.Result;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
    Result uploadImage(MultipartFile file, Integer userId);
}
