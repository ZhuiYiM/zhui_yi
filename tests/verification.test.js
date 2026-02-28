// 测试前端注册验证码功能
describe('注册验证码功能测试', () => {
    it('应该能够发送邮箱验证码', async () => {
        const email = '1067589535@qq.com';
        
        try {
            // 测试直接调用API
            const response = await fetch('http://localhost:8080/user/send-verification-code?email=' + encodeURIComponent(email), {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                }
            });
            
            console.log('响应状态:', response.status);
            const result = await response.json();
            console.log('响应数据:', result);
            
            expect(response.status).toBe(200);
            expect(result.code).toBe(200);
            expect(result.message).toBe('success');
            
        } catch (error) {
            console.error('测试失败:', error);
            throw error;
        }
    });
    
    it('应该验证邮箱格式', () => {
        const validEmail = 'test@example.com';
        const invalidEmail = 'invalid-email';
        
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        
        expect(emailRegex.test(validEmail)).toBe(true);
        expect(emailRegex.test(invalidEmail)).toBe(false);
    });
});