package com.example.demo.service.impl;

import com.example.demo.entity.VerificationCode;
import com.example.demo.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class MailServiceImpl implements MailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 验证码过期时间（分钟）
    private static final int EXPIRE_MINUTES = 5;
    // 验证码存储前缀
    private static final String VERIFICATION_CODE_PREFIX = "verification_code:";

    @Override
    public void sendVerificationCode(String to) {
        String code = generateVerificationCode();

        // 创建验证码对象并存储到Redis
        VerificationCode verificationCode = new VerificationCode(code, EXPIRE_MINUTES);
        String redisKey = VERIFICATION_CODE_PREFIX + to;

        // 存储到Redis，设置过期时间
        redisTemplate.opsForValue().set(redisKey, verificationCode, EXPIRE_MINUTES, TimeUnit.MINUTES);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("校园信息平台邮箱验证");
        message.setText("您的验证码为：" + code + "，有效期为" + EXPIRE_MINUTES + "分钟，请勿泄露给他人。");
        message.setFrom("2690835582@qq.com");
        System.out.println("验证码已存储到Redis: " + redisKey + " -> " + verificationCode);
        mailSender.send(message);
    }

    @Override
    public VerificationResult validateCode(String email, String code) {
        String redisKey = VERIFICATION_CODE_PREFIX + email;
        VerificationCode storedCode = (VerificationCode) redisTemplate.opsForValue().get(redisKey);

        if (storedCode == null) {
            return VerificationResult.CODE_NOT_FOUND;
        }

        if (storedCode.isExpired()) {
            // 验证码已过期，删除Redis中的记录
            redisTemplate.delete(redisKey);
            return VerificationResult.CODE_EXPIRED;
        }

        if (storedCode.getCode().equals(code)) {
            // 验证码正确，删除Redis中的记录
            redisTemplate.delete(redisKey);
            return VerificationResult.CODE_VALID;
        }

        return VerificationResult.CODE_INVALID;
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000; // 生成6位随机数
        return String.valueOf(code);
    }
}
