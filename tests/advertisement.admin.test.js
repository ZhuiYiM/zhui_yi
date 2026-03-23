/**
 * 广告管理功能测试用例
 * 测试范围：广告 CRUD、状态切换、分页查询
 */

const API_BASE_URL = 'http://localhost:8080';
let adminToken = '';

// 模拟管理员登录获取 Token
async function adminLogin() {
    try {
        const response = await fetch(`${API_BASE_URL}/admin/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username: 'admin',
                password: 'admin123'
            })
        });
        
        const data = await response.json();
        if (data.success || data.code === 200) {
            adminToken = data.data || data.token;
            console.log('✓ 管理员登录成功');
            return true;
        } else {
            console.error('✗ 管理员登录失败:', data.message);
            return false;
        }
    } catch (error) {
        console.error('✗ 管理员登录异常:', error);
        return false;
    }
}

// 测试 1：查询广告列表
async function testGetAdvertisements() {
    console.log('\n=== 测试 1: 查询广告列表 ===');
    
    try {
        const response = await fetch(`${API_BASE_URL}/admin/advertisements?pageNum=1&pageSize=10`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${adminToken}`,
                'Content-Type': 'application/json'
            }
        });
        
        const data = await response.json();
        if (data.success || data.code === 200) {
            console.log('✓ 查询广告列表成功');
            console.log('  广告总数:', data.data?.total || data.result?.total);
            console.log('  当前页广告数:', (data.data?.records || data.result?.records || []).length);
            return true;
        } else {
            console.error('✗ 查询广告列表失败:', data.message);
            return false;
        }
    } catch (error) {
        console.error('✗ 查询广告列表异常:', error);
        return false;
    }
}

// 测试 2：创建广告
async function testCreateAdvertisement() {
    console.log('\n=== 测试 2: 创建广告 ===');
    
    const newAd = {
        title: '测试广告-' + Date.now(),
        imageUrl: 'https://placehold.co/800x300/FF6B6B/FFFFFF?text=测试广告',
        linkUrl: '/test',
        content: '这是一个测试广告',
        position: 'home',
        sortOrder: 100,
        isActive: 1,
        startTime: new Date().toISOString().replace('T', ' ').substring(0, 19),
        endTime: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().replace('T', ' ').substring(0, 19)
    };
    
    try {
        const response = await fetch(`${API_BASE_URL}/admin/advertisements`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${adminToken}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(newAd)
        });
        
        const data = await response.json();
        if (data.success || data.code === 200) {
            console.log('✓ 创建广告成功');
            console.log('  广告 ID:', data.data?.id || data.result?.id);
            console.log('  广告标题:', data.data?.title || data.result?.title);
            return data.data?.id || data.result?.id;
        } else {
            console.error('✗ 创建广告失败:', data.message);
            return null;
        }
    } catch (error) {
        console.error('✗ 创建广告异常:', error);
        return null;
    }
}

// 测试 3：获取广告详情
async function testGetAdvertisement(id) {
    console.log('\n=== 测试 3: 获取广告详情 (ID=' + id + ') ===');
    
    try {
        const response = await fetch(`${API_BASE_URL}/admin/advertisements/${id}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${adminToken}`,
                'Content-Type': 'application/json'
            }
        });
        
        const data = await response.json();
        if (data.success || data.code === 200) {
            console.log('✓ 获取广告详情成功');
            console.log('  标题:', data.data?.title || data.result?.title);
            console.log('  位置:', data.data?.position || data.result?.position);
            console.log('  状态:', data.data?.isActive || data.result?.isActive ? '启用' : '禁用');
            return true;
        } else {
            console.error('✗ 获取广告详情失败:', data.message);
            return false;
        }
    } catch (error) {
        console.error('✗ 获取广告详情异常:', error);
        return false;
    }
}

// 测试 4：更新广告
async function testUpdateAdvertisement(id) {
    console.log('\n=== 测试 4: 更新广告 (ID=' + id + ') ===');
    
    const updateData = {
        title: '更新后的测试广告',
        sortOrder: 99,
        isActive: 0
    };
    
    try {
        const response = await fetch(`${API_BASE_URL}/admin/advertisements/${id}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${adminToken}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(updateData)
        });
        
        const data = await response.json();
        if (data.success || data.code === 200) {
            console.log('✓ 更新广告成功');
            console.log('  新标题:', data.data?.title || data.result?.title);
            console.log('  新排序:', data.data?.sortOrder || data.result?.sortOrder);
            console.log('  新状态:', data.data?.isActive || data.result?.isActive ? '启用' : '禁用');
            return true;
        } else {
            console.error('✗ 更新广告失败:', data.message);
            return false;
        }
    } catch (error) {
        console.error('✗ 更新广告异常:', error);
        return false;
    }
}

// 测试 5：更新广告状态
async function testUpdateAdvertisementStatus(id) {
    console.log('\n=== 测试 5: 更新广告状态 (ID=' + id + ') ===');
    
    try {
        const response = await fetch(`${API_BASE_URL}/admin/advertisements/${id}/status?isActive=1`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${adminToken}`,
                'Content-Type': 'application/json'
            }
        });
        
        const data = await response.json();
        if (data.success || data.code === 200) {
            console.log('✓ 更新广告状态成功');
            console.log('  新状态：启用');
            return true;
        } else {
            console.error('✗ 更新广告状态失败:', data.message);
            return false;
        }
    } catch (error) {
        console.error('✗ 更新广告状态异常:', error);
        return false;
    }
}

// 测试 6：删除广告
async function testDeleteAdvertisement(id) {
    console.log('\n=== 测试 6: 删除广告 (ID=' + id + ') ===');
    
    try {
        const response = await fetch(`${API_BASE_URL}/admin/advertisements/${id}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${adminToken}`,
                'Content-Type': 'application/json'
            }
        });
        
        const data = await response.json();
        if (data.success || data.code === 200) {
            console.log('✓ 删除广告成功');
            return true;
        } else {
            console.error('✗ 删除广告失败:', data.message);
            return false;
        }
    } catch (error) {
        console.error('✗ 删除广告异常:', error);
        return false;
    }
}

// 主测试流程
async function runAdvertisementTests() {
    console.log('========================================');
    console.log('广告管理功能测试开始');
    console.log('========================================');
    
    // 1. 管理员登录
    const loginSuccess = await adminLogin();
    if (!loginSuccess) {
        console.error('\n✗ 管理员登录失败，无法继续测试');
        return;
    }
    
    // 2. 查询广告列表
    await testGetAdvertisements();
    
    // 3. 创建广告
    const createdId = await testCreateAdvertisement();
    if (!createdId) {
        console.error('\n✗ 创建广告失败，无法继续后续测试');
        return;
    }
    
    // 4. 获取广告详情
    await testGetAdvertisement(createdId);
    
    // 5. 更新广告
    await testUpdateAdvertisement(createdId);
    
    // 6. 更新广告状态
    await testUpdateAdvertisementStatus(createdId);
    
    // 7. 删除广告（清理测试数据）
    await testDeleteAdvertisement(createdId);
    
    console.log('\n========================================');
    console.log('广告管理功能测试完成');
    console.log('========================================');
}

// 运行测试
runAdvertisementTests().catch(console.error);
