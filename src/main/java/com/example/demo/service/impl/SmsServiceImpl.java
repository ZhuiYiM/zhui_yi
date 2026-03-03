package com.example.demo.service.impl;

import com.example.demo.entity.VerificationCode;
import com.example.demo.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class SmsServiceImpl implements SmsService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final int EXPIRE_MINUTES = 5;
    private static final String SMS_CODE_PREFIX = "sms_code:";

    @Override
    public void sendVerificationCode(String phone) {
        String code = generateVerificationCode();

        // 存储到Redis
        VerificationCode verificationCode = new VerificationCode(code, EXPIRE_MINUTES);
        String redisKey = SMS_CODE_PREFIX + phone;
        redisTemplate.opsForValue().set(redisKey, verificationCode, EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 模拟发送短信（实际项目中需要调用第三方短信服务商API）
        System.out.println("向手机号 " + phone + " 发送验证码: " + code);
    }

    @Override
    public VerificationResult validateCode(String phone, String code) {
        String redisKey = SMS_CODE_PREFIX + phone;
        VerificationCode storedCode = (VerificationCode) redisTemplate.opsForValue().get(redisKey);

        if (storedCode == null) {
            return VerificationResult.CODE_NOT_FOUND;
        }

        if (storedCode.isExpired()) {
            redisTemplate.delete(redisKey);
            return VerificationResult.CODE_EXPIRED;
        }

        if (storedCode.getCode().equals(code)) {
            redisTemplate.delete(redisKey);
            return VerificationResult.CODE_VALID;
        }

        return VerificationResult.CODE_INVALID;
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
