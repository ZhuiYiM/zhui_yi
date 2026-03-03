package com.example.demo.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * 身份证号码验证工具类
 */
public class IdCardValidator {

    // 18位身份证正则表达式
    private static final String ID_CARD_PATTERN_18 = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$";
    
    // 15位身份证正则表达式（老版本）
    private static final String ID_CARD_PATTERN_15 = "^[1-9]\\d{5}\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}$";
    
    // 省份代码
    private static final String[] PROVINCE_CODES = {
        "11", "12", "13", "14", "15", "21", "22", "23", "31", "32", "33", "34", "35", "36", "37",
        "41", "42", "43", "44", "45", "46", "50", "51", "52", "53", "54", "61", "62", "63", "64", "65", "71", "81", "82"
    };

    /**
     * 验证身份证号码格式是否正确
     * @param idCard 身份证号码
     * @return 是否有效
     */
    public static boolean isValid(String idCard) {
        if (idCard == null || idCard.trim().isEmpty()) {
            return false;
        }
        
        idCard = idCard.trim().toUpperCase();
        
        // 长度验证
        if (idCard.length() != 15 && idCard.length() != 18) {
            return false;
        }
        
        // 格式验证
        if (idCard.length() == 18) {
            if (!Pattern.matches(ID_CARD_PATTERN_18, idCard)) {
                return false;
            }
        } else {
            if (!Pattern.matches(ID_CARD_PATTERN_15, idCard)) {
                return false;
            }
        }
        
        // 省份代码验证
        String provinceCode = idCard.substring(0, 2);
        if (!isValidProvinceCode(provinceCode)) {
            return false;
        }
        
        // 日期验证
        if (!isValidDate(idCard)) {
            return false;
        }
        
        // 18位身份证校验码验证
        if (idCard.length() == 18) {
            return isValidCheckCode(idCard);
        }
        
        return true;
    }

    /**
     * 验证省份代码是否有效
     */
    private static boolean isValidProvinceCode(String provinceCode) {
        for (String code : PROVINCE_CODES) {
            if (code.equals(provinceCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 验证出生日期是否有效
     */
    private static boolean isValidDate(String idCard) {
        try {
            String dateStr;
            if (idCard.length() == 18) {
                dateStr = idCard.substring(6, 14);
            } else {
                dateStr = "19" + idCard.substring(6, 12);
            }
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate.parse(dateStr, formatter);
            
            // 验证年份范围（不能是未来日期，也不能太古老）
            int year = Integer.parseInt(dateStr.substring(0, 4));
            int currentYear = LocalDate.now().getYear();
            
            return year >= 1900 && year <= currentYear;
            
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * 验证18位身份证的校验码
     */
    private static boolean isValidCheckCode(String idCard) {
        char[] idChars = idCard.toCharArray();
        int[] weights = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        char[] checkCodes = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
        
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (idChars[i] - '0') * weights[i];
        }
        
        int remainder = sum % 11;
        char expectedCheckCode = checkCodes[remainder];
        
        return idChars[17] == expectedCheckCode;
    }

    /**
     * 提取身份证中的出生日期
     * @param idCard 身份证号码
     * @return 出生日期字符串 (yyyy-MM-dd格式)
     */
    public static String extractBirthDate(String idCard) {
        if (!isValid(idCard)) {
            return null;
        }
        
        String dateStr;
        if (idCard.length() == 18) {
            dateStr = idCard.substring(6, 14);
        } else {
            dateStr = "19" + idCard.substring(6, 12);
        }
        
        return dateStr.substring(0, 4) + "-" + dateStr.substring(4, 6) + "-" + dateStr.substring(6, 8);
    }

    /**
     * 提取身份证中的性别信息
     * @param idCard 身份证号码
     * @return 性别 (M-男, F-女)
     */
    public static String extractGender(String idCard) {
        if (!isValid(idCard)) {
            return null;
        }
        
        int genderDigit;
        if (idCard.length() == 18) {
            genderDigit = idCard.charAt(16) - '0';
        } else {
            genderDigit = idCard.charAt(14) - '0';
        }
        
        return genderDigit % 2 == 1 ? "M" : "F";
    }

    /**
     * 提取身份证中的年龄
     * @param idCard 身份证号码
     * @return 年龄
     */
    public static Integer extractAge(String idCard) {
        String birthDate = extractBirthDate(idCard);
        if (birthDate == null) {
            return null;
        }
        
        try {
            LocalDate birth = LocalDate.parse(birthDate);
            LocalDate now = LocalDate.now();
            int age = now.getYear() - birth.getYear();
            
            // 如果今年的生日还没到，则年龄减1
            if (now.getMonthValue() < birth.getMonthValue() || 
                (now.getMonthValue() == birth.getMonthValue() && now.getDayOfMonth() < birth.getDayOfMonth())) {
                age--;
            }
            
            return age >= 0 ? age : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取身份证详细信息
     * @param idCard 身份证号码
     * @return 包含所有信息的Map
     */
    public static java.util.Map<String, Object> getDetailedInfo(String idCard) {
        java.util.Map<String, Object> info = new java.util.HashMap<>();
        
        if (!isValid(idCard)) {
            info.put("valid", false);
            return info;
        }
        
        info.put("valid", true);
        info.put("birthDate", extractBirthDate(idCard));
        info.put("gender", extractGender(idCard));
        info.put("age", extractAge(idCard));
        info.put("provinceCode", idCard.substring(0, 2));
        
        return info;
    }
}